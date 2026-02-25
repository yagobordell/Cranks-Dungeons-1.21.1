package com.cranks.dungeons.util;

import com.cranks.dungeons.registry.ModBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TreeHelper {

    private TreeHelper() {}

    /**
     * Natural for Log Fortune:
     * - The broken block's state is considered a log
     *   (in our custom tag OR in the vanilla logs tag)
     * - That position is NOT marked as player-placed
     */
    public static boolean isNaturalLogForFortune(ServerWorld world, BlockPos pos, BlockState state) {
        // Fallback: accept vanilla logs even if the custom tag is not set up
        boolean isLog = state.isIn(ModBlockTags.LOG_FORTUNE_LOGS) || state.isIn(BlockTags.LOGS);
        if (!isLog) {
            System.out.println("[LogFortune] " + state + " is not treated as a log");
            return false;
        }

        if (PlacedLogTracker.isPlacedByPlayer(world, pos)) {
            // Once broken, forget it to keep the set small
            PlacedLogTracker.removePlaced(world, pos);
            System.out.println("[LogFortune] " + pos + " was player-placed");
            return false;
        }

        System.out.println("[LogFortune] " + pos + " is natural log for fortune");
        return true;
    }
}