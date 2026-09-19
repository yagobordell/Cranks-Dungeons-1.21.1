package com.cranks.dungeons.mixin;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives non-equine mounts the same camera-independent WASD steering as horses. */
@Mixin({PigEntity.class, StriderEntity.class})
public abstract class NonHorseMountSteeringMixin {
    @Unique private float cranks$turnVelocity;

    @Inject(method = "tickControlled", at = @At("TAIL"))
    private void cranks$steerIndependentlyFromCamera(
            PlayerEntity rider, Vec3d movementInput, CallbackInfo ci) {
        if (!((Object) this instanceof PigEntity) && !((Object) this instanceof StriderEntity)) {
            return;
        }

        net.minecraft.entity.LivingEntity mount = (net.minecraft.entity.LivingEntity) (Object) this;
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

        float steering = MathHelper.clamp(rider.sidewaysSpeed, -1.0f, 1.0f);
        float targetTurnVelocity = -steering * maximumTurnRate;
        float response = Math.abs(steering) > 0.001f ? 0.60f : 0.65f;
        cranks$turnVelocity += (targetTurnVelocity - cranks$turnVelocity) * response;
        cranks$turnVelocity = MathHelper.clamp(
                cranks$turnVelocity, -maximumTurnRate, maximumTurnRate);
        if (Math.abs(cranks$turnVelocity) < 0.01f) cranks$turnVelocity = 0.0f;

        float yaw = mount.getYaw() + cranks$turnVelocity;
        mount.setYaw(yaw);
        mount.setBodyYaw(yaw);
        mount.setHeadYaw(yaw);
    }
}
