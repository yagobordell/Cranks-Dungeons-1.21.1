package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ranged.RangedActions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
abstract class BowControlMixin {
    @Inject(method="onStoppedUsing",at=@At("HEAD"),cancellable=true)
    private void cranks$lowerWithoutShooting(ItemStack stack, World world, LivingEntity user, int remaining, CallbackInfo ci) {
        if (user instanceof PlayerEntity && !RangedActions.firingBow(user)) ci.cancel();
    }
}
