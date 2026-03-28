package com.cranks.dungeons.mixin;

import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageUtil.class)
public class ArmorDamageMixin {

    @Inject(method = "getDamageLeft", at = @At("HEAD"), cancellable = true)
    private static void applyEffectiveHealthLogic(LivingEntity armorWearer, float damageAmount, DamageSource damageSource, float armor, float armorToughness, CallbackInfoReturnable<Float> cir) {
        // Bypass armor for lightning damage (shock chain damage)
        if (damageSource.isOf(net.minecraft.entity.damage.DamageTypes.LIGHTNING_BOLT)) {
            cir.setReturnValue(damageAmount);
            return;
        }
        
        double armorEfficiency = 20.0 + (armorToughness / 2.0);
        double reductionMultiplier = 1.0 / (1.0 + (armor / armorEfficiency));
        float finalDamage = (float) (damageAmount * reductionMultiplier);
        float minimumDamage = damageAmount * 0.01f;

        cir.setReturnValue(Math.max(finalDamage, minimumDamage));
    }
}