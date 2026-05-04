package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin {

    @ModifyVariable(method = "setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V", at = @At("HEAD"), argsOnly = true, index = 5)
    private float modifyProjectileSpeed(float speed, Entity shooter, float pitch, float yaw, float roll, float divergence) {
        if (shooter instanceof LivingEntity livingShooter) {
            double bonus = livingShooter.getAttributeValue(ModAttributes.PROJECTILE_SPEED);
            if (bonus > 0) {
                return (float) (speed * (1.0 + bonus));
            }
        }
        return speed;
    }
}
