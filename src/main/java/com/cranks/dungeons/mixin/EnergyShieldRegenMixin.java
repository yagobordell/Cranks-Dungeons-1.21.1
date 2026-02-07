package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class EnergyShieldRegenMixin {

    @Unique
    private boolean wasRegenerating = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void regenerateEnergyShield(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!(self instanceof ServerPlayerEntity serverPlayer)) return;
        if (self.getEntityWorld().isClient()) return;

        ServerWorld world = (ServerWorld) self.getEntityWorld();
        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (maxShield <= 0) {
            provider.cranksdungeons$setCurrentEnergyShield(0);
            wasRegenerating = false;
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

        // 4 seconds = 80 ticks
        if (timeSinceLastDamage >= 80 && currentShield < maxShield) {
            // Play sound when regeneration STARTS (wasn't regenerating before, but is now)
            if (!wasRegenerating) {
                world.playSound(
                        null,
                        self.getX(),
                        self.getY(),
                        self.getZ(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                        SoundCategory.PLAYERS,
                        0.5f,
                        2.0f
                );
                wasRegenerating = true;
            }

            // Regenerate 12% of max shield per second = 0.6% per tick
            float regenPerTick = (float) (maxShield * 0.006);

            float newShield = Math.min(currentShield + regenPerTick, (float) maxShield);
            provider.cranksdungeons$setCurrentEnergyShield(newShield);

            // Sync to client every tick during regen
            ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(newShield));
        } else {
            // Not regenerating anymore
            wasRegenerating = false;
        }
    }
}