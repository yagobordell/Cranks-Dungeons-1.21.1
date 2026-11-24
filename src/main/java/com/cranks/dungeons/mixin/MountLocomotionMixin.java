package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MountLocomotionMixin implements MountMotionAccess {
    @Unique private MountMotionState cranks$motion;
    @Override public MountMotionState cranksdungeons$motion(){
        if(cranks$motion==null)cranks$motion=new MountMotionState();return cranks$motion;
    }
    @Inject(method="tick",at=@At("HEAD"))
    private void updateMountMotion(CallbackInfo ci){
        if(cranks$motion!=null)MountLocomotion.tick((LivingEntity)(Object)this);
    }
    @Redirect(method={"travel","applyMovementInput"},at=@At(value="INVOKE",target="Lnet/minecraft/entity/LivingEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    private void groundedMountMove(LivingEntity mount,MovementType type,Vec3d movement){MountLocomotion.move(mount,type,movement);}
}
