package com.cranks.dungeons.util;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TreeHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("TreeHelper");

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
            LOGGER.debug("[LogFortune] {} is not treated as a log", state);
            return false;
        }

        if (PlacedLogTracker.isPlacedByPlayer(world, pos)) {
            // Once broken, forget it to keep the set small
            PlacedLogTracker.removePlaced(world, pos);
            LOGGER.debug("[LogFortune] {} was player-placed", pos);
            return false;
        }

        LOGGER.debug("[LogFortune] {} is natural log for fortune", pos);
        return true;
    }
}