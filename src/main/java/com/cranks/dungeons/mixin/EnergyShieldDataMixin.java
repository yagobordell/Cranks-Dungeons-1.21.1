package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EnergyShieldDataMixin implements EnergyShieldProvider {

    @Unique
    private float currentEnergyShield = 0.0f;

    @Unique
    private long lastDamageTime = 0;

    @Unique
    private float lastSyncedShield = -1.0f;

    @Override
    public float cranksdungeons$getCurrentEnergyShield() {
        return currentEnergyShield;
    }

    @Override
    public void cranksdungeons$setCurrentEnergyShield(float shield) {
        this.currentEnergyShield = shield;
    }

    @Override
    public long cranksdungeons$getLastDamageTime() {
        return lastDamageTime;
    }

    @Override
    public void cranksdungeons$setLastDamageTime(long time) {
        this.lastDamageTime = time;
    }

    @Unique
    private int syncTimer = 0;
    
    @Unique
    private int lifeTicks = 0;

    // Sync shield to client every tick if it changed
    @Inject(method = "tick", at = @At("HEAD"))
    private void syncShieldToClient(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayerEntity player) {
            syncTimer++;
            lifeTicks++;
            // Check every 10 ticks to be responsive but not spammy
            if (syncTimer >= 10) {
                syncTimer = 0;
                // Force sync for the first 5 seconds (100 ticks) to guarantee the client receives it regardless of load times
                boolean forceSync = lifeTicks <= 100;
                // Only sync if changed significantly, or if forcing
                if (forceSync || Math.abs(currentEnergyShield - lastSyncedShield) > 0.05f) {
                    lastSyncedShield = currentEnergyShield;
                    
                    // Send to owner (original packet)
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.cranks.dungeons.network.EnergyShieldSyncPacket(currentEnergyShield));
                    
                    // Send to trackers (new packet with ID)
                    com.cranks.dungeons.network.EntityShieldSyncPacket trackingPacket = new com.cranks.dungeons.network.EntityShieldSyncPacket(player.getId(), currentEnergyShield);
                    for (net.minecraft.server.network.ServerPlayerEntity tracker : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(player)) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(tracker, trackingPacket);
                    }
                }
            }
        }
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    protected void writeShieldData(WriteView view, CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity) {
            view.putFloat("EnergyShield", currentEnergyShield);
            view.putLong("EnergyShieldLastDamage", lastDamageTime);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    protected void readShieldData(ReadView view, CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity) {
            this.currentEnergyShield = view.getFloat("EnergyShield", 0.0f);
            this.lastDamageTime = view.getLong("EnergyShieldLastDamage", 0L);
        }
    }
}