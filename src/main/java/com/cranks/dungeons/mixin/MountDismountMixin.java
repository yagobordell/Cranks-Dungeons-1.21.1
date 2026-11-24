package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountDismount;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractHorseEntity.class,PigEntity.class,StriderEntity.class})
public abstract class MountDismountMixin {
    @Inject(method="updatePassengerForDismount",at=@At("RETURN"),cancellable=true)
    private void chooseSafeLanding(LivingEntity passenger,CallbackInfoReturnable<Vec3d> cir){
        if(passenger instanceof PlayerEntity)cir.setReturnValue(MountDismount.choose((Entity)(Object)this,passenger,cir.getReturnValue()));
    }
}
