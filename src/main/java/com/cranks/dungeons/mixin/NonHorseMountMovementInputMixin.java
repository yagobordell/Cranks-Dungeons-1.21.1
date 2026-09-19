package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps A/D reserved for turning on pigs and striders instead of strafing. */
@Mixin(LivingEntity.class)
public abstract class NonHorseMountMovementInputMixin {
    @Inject(method = "getControlledMovementInput", at = @At("RETURN"), cancellable = true)
    private void cranks$useSidewaysInputOnlyForSteering(
            PlayerEntity rider, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
        if (!((Object) this instanceof PigEntity) && !((Object) this instanceof StriderEntity)) {
            return;
        }
        Vec3d vanillaInput = cir.getReturnValue();
        cir.setReturnValue(new Vec3d(0.0, vanillaInput.y, vanillaInput.z));
    }
}
