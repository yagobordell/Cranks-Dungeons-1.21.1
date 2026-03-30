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
                // Apply additive boost in the direction the player is looking
                // This gives consistent speed that matches the tooltip values
                // instead of compounding multiplicatively every tick
                Vec3d velocity = entity.getVelocity();
                Vec3d look = player.getRotationVec(1.0F);
                // Scale: 30% bonus = +0.015 velocity per tick (roughly 30% faster total)
                // Sweet spot between too slow and too fast
                Vec3d boost = look.multiply(flyingSpeedBonus * 0.05);
                entity.setVelocity(velocity.add(boost));
            }
        }
    }
}