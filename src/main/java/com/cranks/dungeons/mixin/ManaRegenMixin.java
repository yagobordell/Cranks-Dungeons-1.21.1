package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ManaProvider;
import com.cranks.dungeons.network.ManaSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ManaRegenMixin {

    @Unique
    private float lastSyncedMana = -1.0f;

    @Unique
    private int ticksSinceLastSync = 0;

    @Unique
    private boolean manaInitialized = false;

    @Unique
    private int loginGraceTicks = 20;

    @Unique
    private static final float SYNC_THRESHOLD = 0.1f;

    @Unique
    private static final int SYNC_INTERVAL_TICKS = 5;

    @Inject(method = "tick", at = @At("HEAD"))
    private void initializeMana(CallbackInfo ci) {
        if (manaInitialized) return;

        PlayerEntity self = (PlayerEntity) (Object) this;
        ManaProvider provider = (ManaProvider) self;
        double maxMana = self.getAttributeValue(ModAttributes.MAX_MANA);

        if (!self.getEntityWorld().isClient()) {
            if (provider.cranksdungeons$getCurrentMana() <= 0.0f && !self.getCommandTags().contains("ManaDrained")) {
                provider.cranksdungeons$setCurrentMana((float) maxMana);
                if (self instanceof ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new ManaSyncPacket((float) maxMana, (float) maxMana));
                }
            }
        }
        manaInitialized = true;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void regenerateMana(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (self.getEntityWorld().isClient() || self.isDead())
            return;

        ManaProvider provider = (ManaProvider) self;
        double maxMana = self.getAttributeValue(ModAttributes.MAX_MANA);

        if (maxMana <= 0) {
            provider.cranksdungeons$setCurrentMana(0);
            return;
        }

        float currentMana = provider.cranksdungeons$getCurrentMana();

        if (loginGraceTicks > 0) {
            loginGraceTicks--;
        } else {
            if (currentMana > maxMana) {
                currentMana = (float) maxMana;
                provider.cranksdungeons$setCurrentMana(currentMana);
            }
        }

        if (currentMana < maxMana) {
            double regenPercent = self.getAttributeValue(ModAttributes.MANA_REGENERATION_PERCENT);
            float regenPerTick = (float) (maxMana * regenPercent / 20.0f);
            
            double regenRate = self.getAttributeValue(ModAttributes.MANA_REGENERATION_RATE);
            double recoveryRate = self.getAttributeValue(ModAttributes.MANA_RECOVERY_RATE);
            
            regenPerTick *= (float) (1.0 + regenRate);
            regenPerTick *= (float) (1.0 + recoveryRate);

            double movingRegen = self.getAttributeValue(ModAttributes.REGENERATION_RATE_WHILE_MOVING_PERCENT);
            if (movingRegen > 0) {
                boolean isMoving = Math.abs(self.getX() - self.prevX) > 0.001 || Math.abs(self.getZ() - self.prevZ) > 0.001 || self.getVelocity().horizontalLengthSquared() > 0.0001;
                if (isMoving) {
                    regenPerTick *= (float) (1.0 + movingRegen);
                }
            }
            
            float newMana = Math.min(currentMana + regenPerTick, (float) maxMana);
            provider.cranksdungeons$setCurrentMana(newMana);

            ticksSinceLastSync++;
            if (Math.abs(newMana - lastSyncedMana) >= SYNC_THRESHOLD || ticksSinceLastSync >= SYNC_INTERVAL_TICKS) {
                if (self instanceof ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new ManaSyncPacket(newMana, (float) maxMana));
                }
                lastSyncedMana = newMana;
                ticksSinceLastSync = 0;
            }
        } else {
            ticksSinceLastSync = 0;
        }
    }
}
