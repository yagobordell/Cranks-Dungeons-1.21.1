package com.cranks.dungeons.event;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.WorldTreeDataComponent;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;

public class GatheringXpHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || player.isCreative() || player.isSpectator()) {
                return;
            }

            int xpGained = determineXpForBlock(state);
            if (xpGained > 0) {
                WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
                if (data != null) {
                    data.addGatheringXp(xpGained);
                    com.cranks.dungeons.component.WorldTreePersistentState.get(world.getServer()).savePlayerData(player.getUuid(), data);
                }
            }
        });
    }

    private static int determineXpForBlock(BlockState state) {
        if (state.isIn(BlockTags.COAL_ORES)) return 5;
        if (state.isIn(BlockTags.IRON_ORES) || state.isIn(BlockTags.COPPER_ORES)) return 10;
        if (state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.REDSTONE_ORES) || state.isIn(BlockTags.LAPIS_ORES)) return 15;
        if (state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.EMERALD_ORES)) return 50;
        if (state.isIn(BlockTags.LOGS)) return 3;
        if (state.isIn(BlockTags.CROPS)) return 2; // Crops include wheat, carrots, etc.
        // Generic stone or dirt could be 0 or 1, let's keep it 0 to avoid afk macro mining for XP
        return 0;
    }
}
