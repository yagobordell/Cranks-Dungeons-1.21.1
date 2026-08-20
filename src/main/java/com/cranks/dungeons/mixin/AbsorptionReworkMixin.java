package com.cranks.dungeons.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AbsorptionReworkMixin {

    @Shadow public abstract float getAbsorptionAmount();
    @Shadow public abstract void setAbsorptionAmount(float amount);

    @Inject(method = "onStatusEffectApplied", at = @At("HEAD"), cancellable = true)
    private void onApplyAbsorption(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
        if (effect.getEffectType() == StatusEffects.ABSORPTION) {
            int amp = effect.getAmplifier();
            float add = (amp >= 10) ? (float) amp : (float) ((amp + 1) * 20);
            this.setAbsorptionAmount(this.getAbsorptionAmount() + add);
            ci.cancel();
        }
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"), cancellable = true)
    private void onRemoveAbsorption(StatusEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffectType() == StatusEffects.ABSORPTION) {
            int amp = effect.getAmplifier();
            float sub = (amp >= 10) ? (float) amp : (float) ((amp + 1) * 20);
            this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() - sub, 0.0F));
            ci.cancel();
        }
    }
}
