package com.cranks.dungeons.economy;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.mixin.EconomyPlayerManagerAccess;
import net.minecraft.SharedConstants;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.PersistentState;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** One world ledger keeps both sides of coin exchanges together, independent of respawns. */
public final class EconomyState extends PersistentState {
    public static final long MAX_COINS = 9_000_000_000_000_000L;
    private static final String KEY = "cranks_dungeons_economy";
    private final Map<UUID, Long> balances = new HashMap<>();
    private final Map<UUID, Set<UUID>> chestClaims = new HashMap<>();
    private final Map<UUID, NbtCompound> inventoryJournal = new HashMap<>();

    public static EconomyState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(new Type<>(EconomyState::new, EconomyState::read, null), KEY);
    }
    public long balance(UUID player) { return balances.getOrDefault(player, 0L); }
    public void setBalance(UUID player, long coins) { balances.put(player, Math.clamp(coins, 0, MAX_COINS)); markDirty(); }
    public long credit(ServerPlayerEntity player, long coins) {
        long added = Math.min(Math.max(0, coins), MAX_COINS - balance(player.getUuid()));
        if (added > 0) { setBalance(player.getUuid(), balance(player.getUuid()) + added); ModComponents.WALLET.sync(player); }
        return added;
    }
    public boolean claimChest(UUID chest, UUID player) {
        boolean fresh = chestClaims.computeIfAbsent(chest, key -> new HashSet<>()).add(player);
        if (fresh) markDirty();
        return fresh;
    }
    private static EconomyState read(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var state = new EconomyState();
        var accounts = nbt.getList("accounts", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < accounts.size(); i++) {
            var tag = accounts.getCompound(i);
            if (tag.containsUuid("player")) state.balances.put(tag.getUuid("player"), Math.clamp(tag.getLong("coins"), 0, MAX_COINS));
        }
        var claims = nbt.getList("chests", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < claims.size(); i++) {
            var tag = claims.getCompound(i);
            if (!tag.containsUuid("chest")) continue;
            var players = new HashSet<UUID>();
            var list = tag.getList("players", NbtElement.INT_ARRAY_TYPE);
            for (int j = 0; j < list.size(); j++) try { players.add(NbtHelper.toUuid(list.get(j))); } catch (IllegalArgumentException ignored) { }
            state.chestClaims.put(tag.getUuid("chest"), players);
        }
        var journal = nbt.getList("inventoryJournal", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < journal.size(); i++) {
            var tag = journal.getCompound(i);
            if (tag.containsUuid("player") && tag.containsUuid("transaction")) state.inventoryJournal.put(tag.getUuid("player"), tag.copy());
        }
        return state;
    }
    @Override public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var accounts = new NbtList();
        balances.forEach((player, coins) -> { var tag = new NbtCompound(); tag.putUuid("player", player); tag.putLong("coins", coins); accounts.add(tag); });
        nbt.put("accounts", accounts);
        var claims = new NbtList();
        chestClaims.forEach((chest, players) -> { var tag = new NbtCompound(); tag.putUuid("chest", chest); var list = new NbtList(); players.forEach(id -> list.add(NbtHelper.fromUuid(id))); tag.put("players", list); claims.add(tag); });
        nbt.put("chests", claims);
        var journal = new NbtList(); inventoryJournal.values().forEach(tag -> journal.add(tag.copy())); nbt.put("inventoryJournal", journal);
        return nbt;
    }
    private void flush(MinecraftServer server) throws IOException {
        var path = server.getSavePath(WorldSavePath.ROOT).resolve("data/" + KEY + ".dat");
        Files.createDirectories(path.getParent());
        var root = new NbtCompound(); root.put("data", writeNbt(new NbtCompound(), server.getRegistryManager()));
        root.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
        var temporary = path.resolveSibling(KEY + ".dat.tmp");
        NbtIo.writeCompressed(root, temporary);
        try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        setDirty(false);
    }
    private NbtCompound journal(ServerPlayerEntity player, DefaultedList<ItemStack> inventory, UUID transaction) {
        var tag = Inventories.writeNbt(new NbtCompound(), inventory, player.getRegistryManager());
        tag.putBoolean("secondSet",PlayerLoadout.of(player.getInventory()).secondSetActive());
        tag.putUuid("player", player.getUuid()); tag.putUuid("transaction", transaction); return tag;
    }
    public boolean exchange(ServerPlayerEntity a, DefaultedList<ItemStack> afterA, long coinsA,
                            ServerPlayerEntity b, DefaultedList<ItemStack> afterB, long coinsB) {
        if(a==b||inventoryJournal.containsKey(a.getUuid())||inventoryJournal.containsKey(b.getUuid()))return false;
        if (coinsA < 0 || coinsB < 0 || coinsA > MAX_COINS || coinsB > MAX_COINS) return false;
        long oldA = balance(a.getUuid()), oldB = balance(b.getUuid());
        UUID transaction = UUID.randomUUID();
        balances.put(a.getUuid(), coinsA); balances.put(b.getUuid(), coinsB);
        inventoryJournal.put(a.getUuid(), journal(a, afterA, transaction)); inventoryJournal.put(b.getUuid(), journal(b, afterB, transaction));
        try { flush(a.getServer()); }
        catch (IOException error) {
            balances.put(a.getUuid(), oldA); balances.put(b.getUuid(), oldB);
            inventoryJournal.remove(a.getUuid()); inventoryJournal.remove(b.getUuid()); markDirty();
            com.cranks.dungeons.CranksDungeons.LOGGER.error("Could not persist trade; inventories were not modified", error); return false;
        }
        apply(a, afterA, transaction); apply(b, afterB, transaction);
        acknowledge(a); acknowledge(b);
        try { flush(a.getServer()); } catch (IOException error) { markDirty(); com.cranks.dungeons.CranksDungeons.LOGGER.warn("Trade recovery journal retained", error); }
        return true;
    }
    private void apply(ServerPlayerEntity player, DefaultedList<ItemStack> inventory, UUID transaction) {
        for (int slot = 0; slot < PlayerLoadout.END; slot++) player.getInventory().setStack(slot, inventory.get(slot).copy());
        var journal=inventoryJournal.get(player.getUuid());
        if(journal!=null&&journal.contains("secondSet"))PlayerLoadout.of(player.getInventory()).setSecondSetActive(journal.getBoolean("secondSet"));
        ModComponents.WALLET.get(player).transaction(transaction);
        player.getInventory().markDirty(); player.playerScreenHandler.syncState(); ModComponents.WALLET.sync(player);
    }
    private void acknowledge(ServerPlayerEntity player) {
        ((EconomyPlayerManagerAccess) player.getServer().getPlayerManager()).cranks$savePlayer(player);
        try {
            var file = player.getServer().getSavePath(WorldSavePath.PLAYERDATA).resolve(player.getUuidAsString() + ".dat");
            var saved = NbtIo.readCompressed(file, NbtSizeTracker.ofUnlimitedBytes());
            if (saved.containsUuid("CranksEconomyTransaction") && saved.getUuid("CranksEconomyTransaction").equals(ModComponents.WALLET.get(player).transaction())) inventoryJournal.remove(player.getUuid());
            else throw new IOException("Player inventory save was not acknowledged");
        } catch (IOException error) {
            com.cranks.dungeons.CranksDungeons.LOGGER.error("Keeping inventory recovery journal for {}", player.getUuid(), error);
            player.networkHandler.disconnect(net.minecraft.text.Text.literal("The trade completed, but your inventory could not be saved. Reconnect to recover it safely."));
        }
    }
    public void recover(ServerPlayerEntity player) {
        var tag = inventoryJournal.get(player.getUuid());
        if (tag == null) return;
        if (!tag.getUuid("transaction").equals(ModComponents.WALLET.get(player).transaction())) {
            var inventory = DefaultedList.ofSize(PlayerLoadout.END, ItemStack.EMPTY);
            Inventories.readNbt(tag, inventory, player.getRegistryManager()); apply(player, inventory, tag.getUuid("transaction"));
        }
        acknowledge(player); markDirty();
        try { flush(player.getServer()); } catch (IOException error) { markDirty(); com.cranks.dungeons.CranksDungeons.LOGGER.warn("Could not checkpoint recovered trade", error); }
    }
}
