package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountDataProvider;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CamelEntity.class)
public abstract class CamelMountAbilityMixin {
    @Inject(method="jump",at=@At("HEAD"))
    private void cranks$markDeliberateDash(float strength,Vec3d movementInput,CallbackInfo ci){
        com.cranks.dungeons.mount.MountLocomotion.beginJump((CamelEntity)(Object)this);
    }
    @ModifyVariable(method = "jump", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float cranks$applyDashPowerGene(float strength) {
        MountData data = ((MountDataProvider) this).cranksdungeons$getMountData();
        if (!data.initialized) return strength;
        double quality = Math.max(0.0, Math.min(1.0, (data.power - 0.45) / 0.45));
        double traitBonus = "Powerful".equals(data.trait) ? 0.10 : 0.0;
        return (float) (strength * (0.90 + quality * 0.35 + traitBonus));
    }

    // Vanilla implements camel dash as a jump with a forward impulse. Keep the dash
    // velocity while removing the upward launch so Space reads as dash only.
    @Inject(method = "jump", at = @At("TAIL"))
    private void cranks$removeVerticalDashImpulse(float strength, Vec3d movementInput, CallbackInfo ci) {
        CamelEntity camel = (CamelEntity) (Object) this;
        Vec3d velocity = camel.getVelocity();
        camel.setVelocity(velocity.x, 0.0, velocity.z);
        camel.velocityDirty = true;
    }
}
