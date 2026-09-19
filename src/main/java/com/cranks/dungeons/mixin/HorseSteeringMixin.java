package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class HorseSteeringMixin {
    @Unique private float cranks$turnVelocity;
    @Unique private Vec3d cranks$velocityBeforeTravel = Vec3d.ZERO;

    /**
     * AbstractHorseEntity uses updateAnger while starting a jump.  If the
     * rider/vehicle relationship is briefly out of sync at landing, that call
     * can leave the horse in the vanilla rearing state for several ticks and
     * block movement.  A ridden horse has no reason to enter its ambient rear;
     * keep that state reserved for unmounted horses.
     */
    @Inject(method = "updateAnger", at = @At("HEAD"), cancellable = true)
    private void cranks$preventRiddenHorseRear(CallbackInfo ci) {
        if (((Object) this instanceof AbstractHorseEntity horse)
                && horse.getControllingPassenger() instanceof PlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "getControlledRotation", at = @At("HEAD"), cancellable = true)
    private void cranks$steerIndependentlyFromCamera(
            LivingEntity rider, CallbackInfoReturnable<Vec2f> cir) {
        if (!((Object) this instanceof AbstractHorseEntity mount)
                || !(rider instanceof PlayerEntity player)) return;

        double horizontalSpeed = mount.getVelocity().horizontalLength();
        double movementSpeed = Math.max(0.05,
                mount.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        float speedRatio = (float) MathHelper.clamp(horizontalSpeed / movementSpeed, 0.0, 1.0);

        float maximumTurnRate;
        if (!mount.isOnGround()) {
            maximumTurnRate = 7.0f;
        } else if (horizontalSpeed < 0.025) {
            maximumTurnRate = 15.0f;
        } else {
            maximumTurnRate = MathHelper.lerp(speedRatio, 13.0f, 9.0f);
        }

        float steering = MathHelper.clamp(player.sidewaysSpeed, -1.0f, 1.0f);
        float targetTurnVelocity = -steering * maximumTurnRate;
        float response = Math.abs(steering) > 0.001f ? 0.60f : 0.65f;
        cranks$turnVelocity += (targetTurnVelocity - cranks$turnVelocity) * response;
        cranks$turnVelocity = MathHelper.clamp(
                cranks$turnVelocity, -maximumTurnRate, maximumTurnRate);
        if (Math.abs(cranks$turnVelocity) < 0.01f) cranks$turnVelocity = 0.0f;

        cir.setReturnValue(new Vec2f(0.0f, mount.getYaw() + cranks$turnVelocity));
    }

    @Inject(method = "getControlledMovementInput", at = @At("RETURN"), cancellable = true)
    private void cranks$useSidewaysInputOnlyForSteering(
            PlayerEntity rider, Vec3d movementInput, CallbackInfoReturnable<Vec3d> cir) {
        if (!((Object) this instanceof AbstractHorseEntity)) return;
        Vec3d vanillaInput = cir.getReturnValue();
        cir.setReturnValue(new Vec3d(0.0, vanillaInput.y, vanillaInput.z));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cranks$rememberVelocityBeforeTravel(CallbackInfo ci) {
        if (!((Object) this instanceof AbstractHorseEntity mount)
                || !(mount.getControllingPassenger() instanceof PlayerEntity)) {
            cranks$velocityBeforeTravel = Vec3d.ZERO;
            return;
        }
        Vec3d velocity = mount.getVelocity();
        cranks$velocityBeforeTravel = new Vec3d(velocity.x, 0.0, velocity.z);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cranks$applyTurningDrift(CallbackInfo ci) {
        if (!((Object) this instanceof AbstractHorseEntity mount)
                || !(mount.getControllingPassenger() instanceof PlayerEntity rider)
                || !mount.isOnGround() || mount.horizontalCollision || mount.isTouchingWater()
                || rider.forwardSpeed <= 0.01f || Math.abs(rider.sidewaysSpeed) <= 0.01f) {
            return;
        }

        Vec3d velocity = mount.getVelocity();
        Vec3d horizontal = new Vec3d(velocity.x, 0.0, velocity.z);
        double previousSpeed = cranks$velocityBeforeTravel.horizontalLength();
        double currentSpeed = horizontal.horizontalLength();
        if (previousSpeed <= 0.025 || currentSpeed <= 0.025) return;

        double movementSpeed = Math.max(0.05,
                mount.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        float speedRatio = (float) MathHelper.clamp(currentSpeed / movementSpeed, 0.0, 1.0);
        double inertia = MathHelper.lerp(speedRatio, 0.25, 0.60);
        Vec3d blendedDirection = cranks$velocityBeforeTravel.normalize().multiply(inertia)
                .add(horizontal.normalize().multiply(1.0 - inertia));
        if (blendedDirection.lengthSquared() <= 0.000001) return;

        Vec3d drifted = blendedDirection.normalize().multiply(currentSpeed);
        mount.setVelocity(drifted.x, velocity.y, drifted.z);
        mount.velocityDirty = true;
    }
}
