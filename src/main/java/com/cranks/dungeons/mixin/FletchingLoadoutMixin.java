package com.cranks.dungeons.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FletchingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FletchingTableBlock.class)
abstract class FletchingLoadoutMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void openFletching(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit,
                              CallbackInfoReturnable<ActionResult> result) {
        if (!world.isClient()) player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        result.setReturnValue(ActionResult.success(world.isClient()));
    }
}
