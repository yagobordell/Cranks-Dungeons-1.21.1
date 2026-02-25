package com.cranks.dungeons.event;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.PlacedDigBlockTracker;
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

        // Tag-based detection OR vanilla gravel/clay fallback
        boolean inTag = state.isIn(ModBlockTags.DIG_FORTUNE_BLOCKS);
        boolean isVanillaDig = state.isOf(Blocks.GRAVEL) || state.isOf(Blocks.CLAY);
        if (!inTag && !isVanillaDig) return;

        var inst = player.getAttributeInstance(ModAttributes.DIG_FORTUNE);
        if (inst == null) return;
        double fortune = inst.getValue();
        if (fortune <= 0.0) return;

        // Ignore player-placed dig blocks
        if (PlacedDigBlockTracker.isPlacedByPlayer(serverWorld, pos)) {
            PlacedDigBlockTracker.removePlaced(serverWorld, pos);
            System.out.println("[DigFortune] " + pos + " was player-placed");
            return;
        }

        ItemStack tool = player.getMainHandStack();

        // Simulate vanilla drops (doesn't affect real world drops)
        List<ItemStack> vanillaDrops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, tool);

        double roll = world.getRandom().nextDouble();
        System.out.println("[DigFortune] fortune=" + fortune + " roll=" + roll);

        // ===== GRAVEL: chance to get flint, double if vanilla already gave flint =====
        if (state.isOf(Blocks.GRAVEL)) {

            boolean vanillaHasFlint = vanillaDrops.stream().anyMatch(s -> s.isOf(Items.FLINT));

            if (roll < fortune) {
                // Always add 1 flint on success:
                // - If vanilla already gave flint => you now have 2 (double)
                // - If vanilla didn't give flint => you now get 1 flint anyway
                System.out.println("[DigFortune] Extra flint at " + pos +
                        " (vanillaHadFlint=" + vanillaHasFlint + ")");
                Block.dropStack(world, pos, new ItemStack(Items.FLINT, 1));
            }

            return;
        }

        // ===== CLAY: chance to double clay balls =====
        if (state.isOf(Blocks.CLAY)) {
            // Count how many clay balls vanilla would drop (normally 4)
            int clayCount = vanillaDrops.stream()
                    .filter(s -> s.isOf(Items.CLAY_BALL))
                    .mapToInt(ItemStack::getCount)
                    .sum();
            if (clayCount <= 0) {
                clayCount = 4; // fallback if loot table changed
            }

            if (roll < fortune) {
                // Add the same amount again: effectively "double clay"
                System.out.println("[DigFortune] Extra clay balls at " + pos +
                        " (+" + clayCount + ")");
                Block.dropStack(world, pos, new ItemStack(Items.CLAY_BALL, clayCount));
            }

            return;
        }

        // ===== Generic dig-fortune blocks (modded) =====
        // For other blocks in #cranks-dungeons:dig_fortune_blocks, we use a generic rule:
        // - If they have any non-block drops, we duplicate them on success.
        if (roll < fortune) {
            for (ItemStack stack : vanillaDrops) {
                if (stack.isEmpty()) continue;
                // Don't double the block item itself; only special drops
                if (stack.getItem() == state.getBlock().asItem()) continue;

                System.out.println("[DigFortune] Extra generic drop " + stack + " at " + pos);
                Block.dropStack(world, pos, stack.copy());
            }
        }
    }
}