package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class EnergyShieldRegenMixin {

    @Unique
    private boolean wasRegenerating = false;

    @Unique
    private float lastSyncedShield = -1.0f;

    @Unique
    private int ticksSinceLastSync = 0;

    @Unique
    private static final float SYNC_THRESHOLD = 0.1f;

    @Unique
    private static final int SYNC_INTERVAL_TICKS = 5;

    @Unique
    private static final int REGEN_DELAY_TICKS = 48; // 4 seconds at 20 TPS

    @Unique
    private static final float REGEN_PERCENT_PER_TICK = 0.01f; // 1% per tick = 20% per second

    @Inject(method = "tick", at = @At("TAIL"))
    private void regenerateEnergyShield(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!(self instanceof ServerPlayerEntity serverPlayer))
            return;
        if (self.getEntityWorld().isClient())
            return;

        ServerWorld world = (ServerWorld) self.getEntityWorld();
        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (maxShield <= 0) {
            provider.cranksdungeons$setCurrentEnergyShield(0);
            wasRegenerating = false;
            lastSyncedShield = -1.0f;
            ticksSinceLastSync = 0;
            return;
        }

        float currentShield = provider.cranksdungeons$getCurrentEnergyShield();

        // Cap shield at max
        if (currentShield > maxShield) {
            currentShield = (float) maxShield;
            provider.cranksdungeons$setCurrentEnergyShield(currentShield);
        }

        long currentTime = world.getTime();
        long timeSinceLastDamage = currentTime - provider.cranksdungeons$getLastDamageTime();

        // Check if we should be regenerating
        if (timeSinceLastDamage >= REGEN_DELAY_TICKS && currentShield < maxShield) {
            // Play sound when regeneration STARTS (wasn't regenerating before, but is now)
            if (!wasRegenerating) {
                world.playSoundFromEntity(
                        null,
                        self,
                        ModSounds.ENERGY_SHIELD_REGEN,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f);
                wasRegenerating = true;
            }

            // Regenerate shield
            float regenPerTick = (float) (maxShield * REGEN_PERCENT_PER_TICK);
            float newShield = Math.min(currentShield + regenPerTick, (float) maxShield);

            provider.cranksdungeons$setCurrentEnergyShield(newShield);

            // Sync to client only when threshold reached or interval passed
            ticksSinceLastSync++;
            if (Math.abs(newShield - lastSyncedShield) >= SYNC_THRESHOLD || ticksSinceLastSync >= SYNC_INTERVAL_TICKS) {
                ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(newShield));
                lastSyncedShield = newShield;
                ticksSinceLastSync = 0;
            }
        } else {
            // Not regenerating anymore
            if (wasRegenerating) {
                // Sync final value when regeneration stops
                if (currentShield != lastSyncedShield) {
                    ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(currentShield));
                    lastSyncedShield = currentShield;
                }
            }
            wasRegenerating = false;
            ticksSinceLastSync = 0;
        }
    }
}