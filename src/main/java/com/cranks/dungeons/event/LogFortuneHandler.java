package com.cranks.dungeons.event;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.TreeHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LogFortuneHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(LogFortuneHandler::onBlockBroken);
    }

    private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos,
                                      BlockState state, BlockEntity blockEntity) {
        if (world.isClient()) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        if (!state.isIn(BlockTags.LOGS)) return;

        boolean natural = TreeHelper.isNaturalLogForFortune(serverWorld, pos, state);
        System.out.println("[LogFortune] Broken log at " + pos + " natural=" + natural);
        if (!natural) return;

        var instance = player.getAttributeInstance(ModAttributes.LOG_FORTUNE);
        if (instance == null) {
            System.out.println("[LogFortune] No LOG_FORTUNE attribute instance on player");
            return;
        }

        double fortuneChance = instance.getValue();
        System.out.println("[LogFortune] LOG_FORTUNE=" + fortuneChance);
        if (fortuneChance <= 0.0) return;

        double roll = world.getRandom().nextDouble();
        System.out.println("[LogFortune] roll=" + roll);

        if (roll < fortuneChance) {
            System.out.println("[LogFortune] Bonus log granted at " + pos);
            ItemStack bonusDrop = new ItemStack(state.getBlock().asItem(), 1);
            if (!bonusDrop.isEmpty()) {
                Block.dropStack(world, pos, bonusDrop);
            }
        } else {
            System.out.println("[LogFortune] No bonus this time");
        }
    }
}