package com.cranks.dungeons.mixin;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractHorseEntity.class)
public abstract class HorsePerfectJumpMixin {
    @ModifyVariable(method = "setJumpStrength", at = @At("HEAD"), argsOnly = true)
    private int cranks$alwaysUseFullJumpStrength(int strength) {
        return (Object) this instanceof HorseEntity ? 100 : strength;
    }
}
