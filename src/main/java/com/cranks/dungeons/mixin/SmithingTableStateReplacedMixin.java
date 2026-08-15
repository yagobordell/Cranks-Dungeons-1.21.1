package com.cranks.dungeons.mixin;

import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles item dropping when a Smithing Table (now Quality Station) is broken.
 * We inject into AbstractBlock.onStateReplaced since SmithingTableBlock doesn't override it.
 */
@Mixin(AbstractBlock.class)
public class SmithingTableStateReplacedMixin {

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void cranksDungeons$onSmithingTableStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        // Only act on Smithing Tables being replaced by a different block
        if (!state.isOf(Blocks.SMITHING_TABLE)) return;
        if (state.isOf(newState.getBlock())) return;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof QualityStationBlockEntity station) {
            station.stopSounds();
            ItemScatterer.spawn(world, pos, station);
            world.updateComparators(pos, state.getBlock());
        }
    }
}
