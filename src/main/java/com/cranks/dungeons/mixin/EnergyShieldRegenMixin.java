package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class EnergyShieldRegenMixin {

    @Unique
    private boolean wasRegenerating = false;

    @Unique
    private float lastSyncedShield = -1.0f;

    @Unique
    private int ticksSinceLastSync = 0;

    @Unique
    private boolean shieldInitialized = false;

    @Unique
    private static final float SYNC_THRESHOLD = 0.1f;

    @Unique
    private static final int SYNC_INTERVAL_TICKS = 5;

    @Unique
    private static final int REGEN_DELAY_TICKS = 80; // 4 seconds at 20 TPS

    @Unique
    private static final float REGEN_PERCENT_PER_TICK = 0.00625f; // 12.5% per second
    
    @Unique
    private static final int MOB_SYNC_INTERVAL_TICKS = 10; // Sync mobs every 0.5s to reduce network overhead

    @Inject(method = "tick", at = @At("HEAD"))
    private void initializeShield(CallbackInfo ci) {
        if (shieldInitialized) return;

        LivingEntity self = (LivingEntity) (Object) this;
        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);

        // Initialize shield ONLY ONCE when the entity first has Energy Shield
        if (maxShield > 0) {
            if (!self.getEntityWorld().isClient()) {
                if (!(self instanceof PlayerEntity)) {
                    // Mobs spawn with full Energy Shield
                    if (provider.cranksdungeons$getCurrentEnergyShield() <= 0.0f) {
                        provider.cranksdungeons$setCurrentEnergyShield((float) maxShield);
                        com.cranks.dungeons.network.EntityShieldSyncPacket packet = new com.cranks.dungeons.network.EntityShieldSyncPacket(self.getId(), (float) maxShield, (float) maxShield);
                        net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(self).forEach(p -> ServerPlayNetworking.send(p, packet));
                    }
                } else {
                    // Players start at 0 if no NBT loaded
                    if (provider.cranksdungeons$getCurrentEnergyShield() <= 0.0f) {
                        provider.cranksdungeons$setCurrentEnergyShield(0.0f);
                        
                        if (self instanceof ServerPlayerEntity serverPlayer) {
                            ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(0.0f, (float) maxShield));
                        }
                    }
                }
            }

            shieldInitialized = true;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void regenerateEnergyShield(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.getEntityWorld().isClient())
            return;

        // Delay processing for the first second of life
        if (self.age < 20)
            return;

        net.minecraft.world.World world = self.getEntityWorld();
        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (self.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) {
            maxShield = 0;
        }
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
        
        // Ghost Reaver prevents Energy Shield Recharge
        boolean ghostReaver = self.getAttributeValue(ModAttributes.KEYSTONE_GHOST_REAVER) > 0;
        
        double fasterStartPercent = self.getAttributeValue(ModAttributes.FASTER_START_OF_ENERGY_SHIELD_RECHARGE);
        int actualRegenDelayTicks = (int)Math.ceil(20*com.cranks.dungeons.stat.CombatStatRules.shieldRechargeDelay(fasterStartPercent));

        // Check if we should be regenerating
        if (!ghostReaver && timeSinceLastDamage >= actualRegenDelayTicks && currentShield < maxShield) {
            // Play sound when regeneration STARTS
            if (!wasRegenerating) {
                world.playSoundFromEntity(
                        null,
                        self,
                        ModSounds.ENERGY_SHIELD_REGEN,
                        self instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE,
                        1.0f,
                        1.0f);
                wasRegenerating = true;
            }

            // Regenerate shield
            double rechargeMultiplier = 1.0 + self.getAttributeValue(ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT);
            float regenPerTick = (float) (maxShield * com.cranks.dungeons.stat.CombatStatRules.shieldRechargeFraction(rechargeMultiplier-1)/20);
            float newShield = Math.min(currentShield + regenPerTick, (float) maxShield);

            provider.cranksdungeons$setCurrentEnergyShield(newShield);

            // Sync shield updates (Owner for players, Trackers for mobs)
            ticksSinceLastSync++;
            boolean shouldSyncMob = ! (self instanceof ServerPlayerEntity) && ticksSinceLastSync >= MOB_SYNC_INTERVAL_TICKS;
            
            if (Math.abs(newShield - lastSyncedShield) >= SYNC_THRESHOLD || ticksSinceLastSync >= SYNC_INTERVAL_TICKS || shouldSyncMob) {
                if (self instanceof ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(newShield, (float) maxShield));
                }
                
                // Sync to tracking players for EVERYONE (mobs AND players)
                com.cranks.dungeons.network.EntityShieldSyncPacket packet = new com.cranks.dungeons.network.EntityShieldSyncPacket(self.getId(), newShield, (float) maxShield);
                net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(self).forEach(p -> ServerPlayNetworking.send(p, packet));
                
                lastSyncedShield = newShield;
                ticksSinceLastSync = 0;
            }
        } else {
            // Not regenerating anymore
            if (wasRegenerating) {
                // Sync final value when regeneration stops (owner only)
                if (self instanceof ServerPlayerEntity serverPlayer) {
                    if (currentShield != lastSyncedShield) {
                        ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(currentShield, (float) maxShield));
                        lastSyncedShield = currentShield;
                    }
                }
            }
            wasRegenerating = false;
            ticksSinceLastSync = 0;
        }
    }
}
