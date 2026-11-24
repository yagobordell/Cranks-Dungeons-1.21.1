package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
public abstract class GatheringSurvivalMixin {
    @ModifyArg(method="travel",at=@At(value="INVOKE",target="Lnet/minecraft/entity/LivingEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"),index=0)
    private float swim(float speed){
        var entity=(LivingEntity)(Object)this;
        return entity instanceof PlayerEntity&&entity.isTouchingWater()?(float)(speed*(1+entity.getAttributeValue(ModAttributes.SWIMMING_SPEED_PERCENT))):speed;
    }
    @WrapMethod(method="getNextAirUnderwater")
    private int breathe(int air,Operation<Integer> original){
        int next=original.call(air);var entity=(LivingEntity)(Object)this;
        return entity instanceof PlayerEntity&&next<air&&entity.getRandom().nextDouble()<entity.getAttributeValue(ModAttributes.BREATH_EFFICIENCY)?air:next;
    }
}
