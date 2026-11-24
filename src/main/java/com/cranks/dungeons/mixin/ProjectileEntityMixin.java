package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin {
    /** Canonical launch path shared by bows, crossbows and thrown projectiles; apply once. */
    @ModifyVariable(method="setVelocity(DDDFF)V",at=@At("HEAD"),argsOnly=true,index=7)
    private float modifyProjectileSpeed(float speed) {
        if(((ProjectileEntity)(Object)this).getOwner() instanceof LivingEntity owner)
            return (float)(speed*(1+Math.max(0,owner.getAttributeValue(ModAttributes.PROJECTILE_SPEED))));
        return speed;
    }
}
