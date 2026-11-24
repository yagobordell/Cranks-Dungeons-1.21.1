package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountLocomotion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PigEntity.class,StriderEntity.class})
public abstract class NonHorseMountSteeringMixin {
    @Redirect(method="tickControlled",at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float independentYaw(PlayerEntity rider){return MountLocomotion.yaw((LivingEntity)(Object)this,rider);}
    @Redirect(method="tickControlled",at=@At(value="INVOKE",target="Lnet/minecraft/entity/player/PlayerEntity;getPitch()F"))
    private float independentPitch(PlayerEntity rider){return 0;}
    @Inject(method="getControlledMovementInput",at=@At("RETURN"),cancellable=true)
    private void progressiveInput(PlayerEntity rider,Vec3d input,CallbackInfoReturnable<Vec3d> cir){
        cir.setReturnValue(MountLocomotion.input((LivingEntity)(Object)this,rider,cir.getReturnValue()));
    }
}
