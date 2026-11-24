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
        PlayerEntity self = (PlayerEntity) (Object) this;
        if(!self.getWorld().isClient()) {
            var maximum=self.getAttributeInstance(ModAttributes.MAX_MANA);
            var id=net.minecraft.util.Identifier.of("cranks-dungeons","legacy_max_mana_bonus");
            double bonus=self.getAttributeValue(ModAttributes.MAX_MANA_BONUS);
            var modifier=maximum.getModifier(id);
            if(modifier==null&&bonus>0||modifier!=null&&modifier.value()!=bonus) {
                maximum.removeModifier(id);
                if(bonus>0)maximum.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(id,bonus,
                        net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (manaInitialized) return;
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
            
            double regenRate = self.getAttributeValue(ModAttributes.MANA_REGENERATION_RATE);
            double recoveryRate = self.getAttributeValue(ModAttributes.MANA_RECOVERY_RATE);
            
            float regenPerTick = (float)(com.cranks.dungeons.stat.CombatStatRules.manaRegeneration(maxMana,
                    self.getAttributeValue(ModAttributes.MANA_REGENERATION),regenPercent,regenRate,recoveryRate)/20);
            
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
