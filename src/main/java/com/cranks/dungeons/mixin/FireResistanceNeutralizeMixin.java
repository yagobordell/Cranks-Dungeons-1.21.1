package com.cranks.dungeons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class FireResistanceNeutralizeMixin {

    @WrapOperation(method = "damage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean useFireResistanceStat(LivingEntity entity, RegistryEntry<StatusEffect> effect, Operation<Boolean> original) {
        return !effect.equals(StatusEffects.FIRE_RESISTANCE) && original.call(entity, effect);
    }
}
