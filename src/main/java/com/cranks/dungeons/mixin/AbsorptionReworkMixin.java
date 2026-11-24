package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.StatusEffectRules;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.effect.AbsorptionStatusEffect")
abstract class AbsorptionReworkMixin {
    @Inject(method = "onApplied", at = @At("HEAD"), cancellable = true)
    private void grantAbsorption(LivingEntity entity, int amplifier, CallbackInfo ci) {
        entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(),
                (float) StatusEffectRules.ABSORPTION.amount(amplifier)));
        ci.cancel();
    }
}
