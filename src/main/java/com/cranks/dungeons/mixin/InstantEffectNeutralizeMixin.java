package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.StatusEffectStatManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.effect.InstantHealthOrDamageStatusEffect")
public abstract class InstantEffectNeutralizeMixin {

    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    private void applyCustomUpdateInstantEffect(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        StatusEffect self = (StatusEffect) (Object) this;
        String key = self.getTranslationKey();
        if ("effect.minecraft.instant_health".equals(key)) {
            StatusEffectStatManager.applyInstantHealth(null, null, entity, amplifier, 1.0);
            cir.setReturnValue(true);
        } else if ("effect.minecraft.instant_damage".equals(key)) {
            StatusEffectStatManager.applyInstantDamage(null, null, entity, amplifier, 1.0);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "applyInstantEffect", at = @At("HEAD"), cancellable = true)
    private void applyCustomInstantHealthAndDamage(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo ci) {
        StatusEffect self = (StatusEffect) (Object) this;
        String key = self.getTranslationKey();
        if ("effect.minecraft.instant_health".equals(key)) {
            StatusEffectStatManager.applyInstantHealth(source, attacker, target, amplifier, proximity);
            ci.cancel();
        } else if ("effect.minecraft.instant_damage".equals(key)) {
            StatusEffectStatManager.applyInstantDamage(source, attacker, target, amplifier, proximity);
            ci.cancel();
        }
    }
}
