package com.cranks.dungeons.event;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.PlacedBlockTracker;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class DigFortuneHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(DigFortuneHandler::onBlockBroken);
    }

    private static void onBlockBroken(World world, PlayerEntity player, BlockPos pos,
                                      BlockState state, BlockEntity blockEntity) {
        if (world.isClient()) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        boolean inTag = state.isIn(ModBlockTags.DIG_FORTUNE_BLOCKS);
        boolean isVanillaDig = state.isOf(Blocks.GRAVEL) || state.isOf(Blocks.CLAY);
        if (!inTag && !isVanillaDig) return;

        var inst = player.getAttributeInstance(ModAttributes.DIG_FORTUNE);
        if (inst == null) return;
        double fortune = inst.getValue();
        if (fortune <= 0.0) return;

        if (PlacedBlockTracker.isPlacedByPlayer(serverWorld, pos, PlacedBlockTracker.DIG_BLOCKS_ID)) {
            PlacedBlockTracker.removePlaced(serverWorld, pos, PlacedBlockTracker.DIG_BLOCKS_ID);
            return;
        }

        ItemStack tool = player.getMainHandStack();

        List<ItemStack> vanillaDrops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, tool);

        double roll = world.getRandom().nextDouble();

        if (state.isOf(Blocks.GRAVEL)) {

            boolean vanillaHasFlint = vanillaDrops.stream().anyMatch(s -> s.isOf(Items.FLINT));

            if (roll < fortune) {
                Block.dropStack(world, pos, new ItemStack(Items.FLINT, 1));
            }

            return;
        }

        if (state.isOf(Blocks.CLAY)) {
            int clayCount = vanillaDrops.stream()
                    .filter(s -> s.isOf(Items.CLAY_BALL))
                    .mapToInt(ItemStack::getCount)
                    .sum();
            if (clayCount <= 0) {
                clayCount = 4;
            }

            if (roll < fortune) {
                Block.dropStack(world, pos, new ItemStack(Items.CLAY_BALL, clayCount));
            }

            return;
        }

        if (roll < fortune) {
            for (ItemStack stack : vanillaDrops) {
                if (stack.isEmpty()) continue;
                if (stack.getItem() == state.getBlock().asItem()) continue;

                Block.dropStack(world, pos, stack.copy());
            }
        }
    }
}