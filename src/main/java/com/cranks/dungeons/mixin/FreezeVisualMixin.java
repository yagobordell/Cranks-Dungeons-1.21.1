package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class FreezeVisualMixin {

    @Inject(method = "getFreezingScale", at = @At("HEAD"), cancellable = true)
    private void applyFreezeVisuals(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof LivingEntity living) {
            if (living.hasStatusEffect(ModEffects.FREEZE)) {
                // A light edge frost leaves the first-person combat view clear.
                cir.setReturnValue(0.2f);
            }
        }
    }
}
