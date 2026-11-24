package com.cranks.dungeons.component;

import com.cranks.dungeons.economy.EconomyState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import java.util.UUID;

/** Private client mirror; the world economy ledger is the sole balance authority. */
public final class WalletComponent implements AutoSyncedComponent {
    private final PlayerEntity player;
    private long displayed;
    private double tradeDistance=3;
    private UUID transaction;
    private boolean incomingRequests = true;
    public WalletComponent(PlayerEntity player) { this.player = player; }
    public long balance() { return player instanceof ServerPlayerEntity server ? EconomyState.get(server.getServer()).balance(player.getUuid()) : displayed; }
    public UUID transaction() { return transaction; }
    public double tradeDistance() { return player.getWorld().isClient()?tradeDistance:com.cranks.dungeons.economy.EconomyConfig.RULES.tradeDistance; }
    public void transaction(UUID id) { transaction = id; }
    public boolean incomingRequests() { return incomingRequests; }
    public void incomingRequests(boolean enabled) { incomingRequests = enabled; }
    @Override public boolean shouldSyncWith(ServerPlayerEntity viewer) { return viewer == player; }
    @Override public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) { buf.writeLong(balance());buf.writeDouble(tradeDistance());buf.writeBoolean(incomingRequests); }
    @Override public void applySyncPacket(RegistryByteBuf buf) { displayed = Math.clamp(buf.readLong(), 0, EconomyState.MAX_COINS);tradeDistance=Math.clamp(buf.readDouble(),1,6);incomingRequests=buf.readBoolean(); }
    @Override public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) { transaction = nbt.containsUuid("transaction") ? nbt.getUuid("transaction") : null;incomingRequests=!nbt.contains("incomingRequests")||nbt.getBoolean("incomingRequests"); }
    @Override public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) { if (transaction != null) nbt.putUuid("transaction", transaction);nbt.putBoolean("incomingRequests",incomingRequests); }
}
