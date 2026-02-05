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
    private float lastSyncedShield = 0.0f;

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

    // Sync shield to client every tick if it changed
    @Inject(method = "tick", at = @At("HEAD"))
    private void syncShieldToClient(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayerEntity player) {
            // Only sync if changed significantly
            if (Math.abs(currentEnergyShield - lastSyncedShield) > 0.1f) {
                lastSyncedShield = currentEnergyShield;

                // Send custom packet or use existing sync mechanism
                // For now, we'll rely on the client updating from attribute changes
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