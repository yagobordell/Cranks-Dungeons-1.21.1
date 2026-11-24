package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.effect.RegenerationStatusEffect")
public abstract class RegenerationNeutralizeMixin {

    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    private void neutralizeVanillaRegenUpdate(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        // LifeRegenerationMixin applies the stat-driven recovery each tick.
        cir.setReturnValue(true);
    }
}
