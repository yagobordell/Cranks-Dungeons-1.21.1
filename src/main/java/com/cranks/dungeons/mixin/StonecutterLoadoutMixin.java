package com.cranks.dungeons.mixin;

import com.cranks.dungeons.screen.LoadoutCuttingBenchHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StonecutterBlock.class)
abstract class StonecutterLoadoutMixin {
    @Inject(method = "createScreenHandlerFactory", at = @At("HEAD"), cancellable = true)
    private void openCuttingPanel(BlockState state, World world, BlockPos pos,
                                  CallbackInfoReturnable<NamedScreenHandlerFactory> result) {
        result.setReturnValue(new SimpleNamedScreenHandlerFactory(
                (id, inventory, player) -> new LoadoutCuttingBenchHandler(id, inventory, ScreenHandlerContext.create(world, pos)),
                Blocks.STONECUTTER.getName()));
    }
}
