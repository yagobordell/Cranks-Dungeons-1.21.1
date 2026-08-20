package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class HungerActionSpeedMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyHungerModifiers(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        if (player.age % 20 != 0) return;

        int foodLevel = player.getHungerManager().getFoodLevel();

        boolean isHungerHigh = foodLevel >= 16;
        boolean hasHungerHigh = player.hasStatusEffect(ModEffects.WELL_FED);

        if (isHungerHigh && !hasHungerHigh) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.WELL_FED, StatusEffectInstance.INFINITE, 0, false, false, true));
        } else if (!isHungerHigh && hasHungerHigh) {
            player.removeStatusEffect(ModEffects.WELL_FED);
        }

        boolean isHungerLow = foodLevel <= 6;
        boolean hasHungerLow = player.hasStatusEffect(ModEffects.STARVING);

        if (isHungerLow && !hasHungerLow) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.STARVING, StatusEffectInstance.INFINITE, 0, false, false, true));
        } else if (!isHungerLow && hasHungerLow) {
            player.removeStatusEffect(ModEffects.STARVING);
        }
    }
}
