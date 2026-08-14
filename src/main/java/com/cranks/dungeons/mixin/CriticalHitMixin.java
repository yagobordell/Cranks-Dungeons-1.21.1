package com.cranks.dungeons.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class CriticalHitMixin {

    /**
     * Disable vanilla falling/jump critical hits completely so only the mod's
     * RPG crit chance pipeline (BonusAttackDamageMixin) handles crits.
     */
    @ModifyVariable(
            method = "attack",
            at = @At(value = "STORE"),
            ordinal = 2,
            require = 0
    )
    private boolean disableCriticalHitsOrdinal2(boolean isCritical) {
        return false;
    }

    @ModifyVariable(
            method = "attack",
            at = @At(value = "STORE"),
            ordinal = 3,
            require = 0
    )
    private boolean disableCriticalHitsOrdinal3(boolean isCritical) {
        return false;
    }

    @ModifyConstant(
            method = "attack",
            constant = @Constant(floatValue = 1.5F),
            require = 0
    )
    private float disableVanillaCritMultiplier(float constant) {
        return 1.0F;
    }

    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addCritParticles(Lnet/minecraft/entity/Entity;)V"),
            require = 0
    )
    private void disableVanillaCritParticles(PlayerEntity player, Entity target) {
        // Suppress vanilla jump-crit particles - custom crit effects are handled by BonusAttackDamageMixin
    }
}