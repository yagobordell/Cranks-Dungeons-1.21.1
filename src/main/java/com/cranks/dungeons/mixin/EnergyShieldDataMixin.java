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

    @Unique
    private double cranks$lastGlobalESPercentBonus = Double.NaN;

    // Sync shield to client every tick if it changed
    // Sync shield to tracking clients periodically if it changed
    @Inject(method = "tick", at = @At("HEAD"))
    private void syncShieldToClient(CallbackInfo ci) {
        if (!((Object)this instanceof net.minecraft.entity.LivingEntity living)) return;
        if (living.getEntityWorld().isClient()) return;

        // Apply Global Energy Shield Percent
        var esAttr = living.getAttributeInstance(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
        if (esAttr != null) {
            double globalESPercent = living.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD_PERCENT);
            net.minecraft.util.Identifier percentModifierId = net.minecraft.util.Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "global_energy_shield_percent");
            if (Double.isNaN(cranks$lastGlobalESPercentBonus) || globalESPercent != cranks$lastGlobalESPercentBonus) {
                esAttr.removeModifier(percentModifierId);
                if (globalESPercent > 0) {
                    esAttr.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                            percentModifierId,
                            globalESPercent,
                            net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
                cranks$lastGlobalESPercentBonus = globalESPercent;
            }
        }

        syncTimer++;
        // Check periodically (10 ticks for players, 20 for mobs to save bandwidth)
        int interval = (living instanceof PlayerEntity) ? 10 : 20;
        
        if (syncTimer >= interval) {
            syncTimer = 0;
            if (Math.abs(currentEnergyShield - lastSyncedShield) > 0.05f) {
                lastSyncedShield = currentEnergyShield;
                
                // Send to owner if it's a player
                if (living instanceof ServerPlayerEntity player) {
                    double maxShield = living.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new com.cranks.dungeons.network.EnergyShieldSyncPacket(currentEnergyShield, (float) maxShield));
                }
                
                // Send to all trackers
                double maxShield = living.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                com.cranks.dungeons.network.EntityShieldSyncPacket trackingPacket = new com.cranks.dungeons.network.EntityShieldSyncPacket(living.getId(), currentEnergyShield, (float) maxShield);
                for (net.minecraft.server.network.ServerPlayerEntity tracker : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(living)) {
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(tracker, trackingPacket);
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