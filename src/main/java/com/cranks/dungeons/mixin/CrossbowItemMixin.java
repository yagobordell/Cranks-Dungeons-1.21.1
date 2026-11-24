package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void cranks$aimLoadedCrossbow(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        var stack=player.getStackInHand(hand);
        if(CrossbowItem.isCharged(stack)) {
            player.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(stack));
        }
    }
    @Inject(method="usageTick",at=@At("HEAD"),cancellable=true)
    private void cranks$finishReloadOnTime(World world, LivingEntity user, ItemStack stack, int remaining, CallbackInfo ci) {
        if(!(user instanceof PlayerEntity))return;
        if(CrossbowItem.isCharged(stack)){ci.cancel();return;}
        int used=stack.getMaxUseTime(user)-remaining;
        if(!world.isClient()&&used>=CrossbowItem.getPullTime(stack,user)) {
            ((CrossbowItem)(Object)this).onStoppedUsing(stack,world,user,remaining);
            ci.cancel();
        }
    }
}
