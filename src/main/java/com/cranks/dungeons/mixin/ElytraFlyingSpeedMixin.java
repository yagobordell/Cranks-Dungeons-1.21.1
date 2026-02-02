package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class ElytraFlyingSpeedMixin {

    @Inject(method = "travel", at = @At("TAIL"))
    private void boostElytraSpeed(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Check if entity is fall flying using accessor
        boolean isFallFlying = ((EntityAccessor) entity).invokeGetFlag(7);

        if (isFallFlying && entity instanceof PlayerEntity player) {
            double flyingSpeedBonus = player.getAttributeValue(ModAttributes.FLYING_SPEED);

            if (flyingSpeedBonus > 0) {
                Vec3d velocity = entity.getVelocity();
                // Only boost horizontal movement to avoid weird falling behavior
                entity.setVelocity(new Vec3d(
                        velocity.x * (1.0 + flyingSpeedBonus * 0.1),
                        velocity.y,
                        velocity.z * (1.0 + flyingSpeedBonus * 0.1)
                ));
            }
        }
    }
}