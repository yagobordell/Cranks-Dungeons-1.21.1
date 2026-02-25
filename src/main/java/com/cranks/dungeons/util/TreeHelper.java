package com.cranks.dungeons.util;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TreeHelper {

    private TreeHelper() {}
    public static boolean isNaturalLogForFortune(ServerWorld world, BlockPos pos, BlockState state) {
        if (!state.isIn(BlockTags.LOGS)) {
            return false;
        }

        if (PlacedLogTracker.isPlacedByPlayer(world, pos)) {
            PlacedLogTracker.removePlaced(world, pos);
            return false;
        }

        return true;
    }
}