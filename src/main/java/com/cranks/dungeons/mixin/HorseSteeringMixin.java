package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountLocomotion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(AbstractHorseEntity.class)
public abstract class HorseSteeringMixin {
    @Inject(method="setJumpStrength",at=@At("HEAD"))
    private void requestedJump(int strength,CallbackInfo ci){
        var mount=(AbstractHorseEntity)(Object)this;
        if(strength>0&&mount.isOnGround())MountLocomotion.beginJump(mount);
    }
    @Inject(method="updateAnger",at=@At("HEAD"),cancellable=true)
    private void preventRiddenRear(CallbackInfo ci){
        if(((AbstractHorseEntity)(Object)this).getControllingPassenger() instanceof PlayerEntity)ci.cancel();
    }
    @Inject(method="getControlledRotation",at=@At("HEAD"),cancellable=true)
    private void independentSteering(LivingEntity rider,CallbackInfoReturnable<Vec2f> cir){
        if(rider instanceof PlayerEntity player)cir.setReturnValue(new Vec2f(0,MountLocomotion.yaw((LivingEntity)(Object)this,player)));
    }
    @Inject(method="getControlledMovementInput",at=@At("RETURN"),cancellable=true)
    private void progressiveInput(PlayerEntity rider,Vec3d input,CallbackInfoReturnable<Vec3d> cir){
        var mount=(AbstractHorseEntity)(Object)this;
        mount.setAngry(false);
        cir.setReturnValue(MountLocomotion.input(mount,rider,cir.getReturnValue()));
    }
    @Inject(method="jump",at=@At("HEAD"))
    private void intentionalJump(float strength,Vec3d input,CallbackInfo ci){MountLocomotion.beginJump((LivingEntity)(Object)this);}
    @Inject(method="jump",at=@At("TAIL"))
    private void preventJumpSpeedStacking(float strength,Vec3d input,CallbackInfo ci){MountLocomotion.capJumpImpulse((LivingEntity)(Object)this);}
}
