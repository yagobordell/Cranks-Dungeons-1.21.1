package com.cranks.dungeons.mixin;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A hopper must not bypass individual experiment knowledge through the old vanilla brewing system. */
@Mixin(BrewingStandBlockEntity.class)
abstract class BrewingAutomationMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void stopLegacyBrewing(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity block, CallbackInfo callback) {
        callback.cancel();
    }
}
