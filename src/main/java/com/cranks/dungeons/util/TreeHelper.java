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

    public static boolean isNaturalLogForFortune(ServerWorld world, BlockPos pos, BlockState state) {
        boolean isLog = state.isIn(ModBlockTags.LOG_FORTUNE_LOGS) || state.isIn(BlockTags.LOGS);
        if (!isLog) {
            LOGGER.debug("[LogFortune] {} is not treated as a log", state);
            return false;
        }

        if (PlacedBlockTracker.isPlacedByPlayer(world, pos, PlacedBlockTracker.LOGS_ID)) {
            PlacedBlockTracker.removePlaced(world, pos, PlacedBlockTracker.LOGS_ID);
            LOGGER.debug("[LogFortune] {} was player-placed", pos);
            return false;
        }

        LOGGER.debug("[LogFortune] {} is natural log for fortune", pos);
        return true;
    }
}