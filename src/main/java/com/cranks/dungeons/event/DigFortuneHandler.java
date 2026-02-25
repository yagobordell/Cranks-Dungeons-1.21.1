package com.cranks.dungeons.event;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.PlacedDigBlockTracker;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

        // Only care about blocks in our dig fortune tag
        if (!state.isIn(ModBlockTags.DIG_FORTUNE_BLOCKS)) return;

        var inst = player.getAttributeInstance(ModAttributes.DIG_FORTUNE);
        if (inst == null) return;
        double fortune = inst.getValue();
        if (fortune <= 0.0) return;

        // Ignore player-placed dig blocks
        if (PlacedDigBlockTracker.isPlacedByPlayer(serverWorld, pos)) {
            PlacedDigBlockTracker.removePlaced(serverWorld, pos);
            return;
        }

        // Roll once for "extra special drops"
        double roll = world.getRandom().nextDouble();
        if (roll >= fortune) return;

        // Compute what this block would drop (using its loot table)
        ItemStack tool = player.getMainHandStack();
        List<ItemStack> theoreticalDrops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, tool);

        // For each drop that is NOT the block itself, drop one more stack: "double non-block drops"
        for (ItemStack stack : theoreticalDrops) {
            if (stack.isEmpty()) continue;

            // Don't double the block item itself; only special drops
            if (stack.getItem() == state.getBlock().asItem()) continue;

            Block.dropStack(world, pos, stack.copy());
        }
    }
}