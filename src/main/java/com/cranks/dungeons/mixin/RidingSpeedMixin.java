package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.RidingStats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractHorseEntity.class, CamelEntity.class, PigEntity.class, StriderEntity.class})
public abstract class RidingSpeedMixin {
    // Each vanilla override calculates its own speed without calling super.
    @Inject(method = "getSaddledSpeed", at = @At("RETURN"), cancellable = true)
    private void riderSpeed(PlayerEntity rider, CallbackInfoReturnable<Float> cir) {
        LivingEntity mount = (LivingEntity) (Object) this;
        if (mount.getControllingPassenger() == rider) {
            cir.setReturnValue(RidingStats.speed(mount, cir.getReturnValue()));
        }
    }
}
