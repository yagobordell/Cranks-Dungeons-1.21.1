package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ResistanceNeutralizeMixin {

    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"), cancellable = true)
    private void bypassVanillaResistanceAndProtection(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        // Vanilla protection enchantments and 20%/level Resistance cuts are superseded by Cranks Dungeons
        // Armor, Resistances, and StatusEffectStatManager (which gives flat Bonus Armor for Resistance).
        cir.setReturnValue(amount);
    }
}
