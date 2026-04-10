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
        // Physical reduction is now handled by Path of Exile 2 formula in DamageConversionHelper.
        // We return the raw amount here to avoid double-dipping.
        cir.setReturnValue(damageAmount);
    }
}