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

        boolean isFallFlying = ((EntityAccessor) entity).invokeGetFlag(7);

        if (isFallFlying && entity instanceof PlayerEntity player) {
            double flyingSpeedBonus = player.getAttributeValue(ModAttributes.FLYING_SPEED);

            if (flyingSpeedBonus > 0) {
                Vec3d velocity = entity.getVelocity();
                entity.setVelocity(new Vec3d(
                        velocity.x * (1.0 + flyingSpeedBonus * 0.1),
                        velocity.y,
                        velocity.z * (1.0 + flyingSpeedBonus * 0.1)
                ));
            }
        }
    }
}