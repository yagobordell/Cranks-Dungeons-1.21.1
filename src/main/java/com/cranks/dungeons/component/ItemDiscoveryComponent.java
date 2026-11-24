package com.cranks.dungeons.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/** Item types a player has held in their inventory in this save. */
public final class ItemDiscoveryComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final PlayerEntity player;
    private final Set<Identifier> items = new HashSet<>();
    private int revision;
    private boolean pendingSync;
    private boolean loadedData;

    public ItemDiscoveryComponent(PlayerEntity player) {
        this.player = player;
    }

    public void record(ItemStack stack) {
        if (!stack.isEmpty() && items.add(Registries.ITEM.getId(stack.getItem()))) {
            revision++;
            pendingSync = true;
        }
    }

    public void recordInventory() {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) record(inventory.getStack(slot));
    }

    public Set<Identifier> knownIds() { return Set.copyOf(items); }

    public boolean contains(Item item) {
        return items.contains(Registries.ITEM.getId(item));
    }

    public boolean hasMatching(Predicate<Item> test) {
        for (Identifier id : items) {
            var item = Registries.ITEM.getOrEmpty(id);
            if (item.isPresent() && test.test(item.get())) return true;
        }
        return false;
    }

    public int revision() {
        return revision;
    }
    public boolean hasLoadedData() { return loadedData; }

    @Override
    public void serverTick() {
        recordInventory();
        if (pendingSync) {
            pendingSync = false;
            ModComponents.ITEM_DISCOVERIES.sync(player);
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity viewer) {
        return viewer == player;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        loadedData = true;
        Set<Identifier> loaded = new HashSet<>();
        NbtList saved = tag.getList("items", NbtElement.STRING_TYPE);
        for (int i = 0; i < saved.size(); i++) {
            Identifier id = Identifier.tryParse(saved.getString(i));
            // Follow registry aliases so discoveries survive renamed or merged items.
            if (id != null) id = Registries.ITEM.getOrEmpty(id).map(Registries.ITEM::getId).orElse(id);
            if (id != null) loaded.add(id);
        }
        if (!items.equals(loaded)) {
            items.clear();
            items.addAll(loaded);
            revision++;
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        NbtList saved = new NbtList();
        for (Identifier item : items) saved.add(NbtString.of(item.toString()));
        tag.put("items", saved);
    }
}
