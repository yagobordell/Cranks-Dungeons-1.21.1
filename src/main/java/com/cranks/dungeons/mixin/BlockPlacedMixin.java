package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.PlacedBlockTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockPlacedMixin {

    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void cranks_dungeons$onPlaced(World world, BlockPos pos, BlockState state,
                                          LivingEntity placer, ItemStack itemStack,
                                          CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (!(placer instanceof PlayerEntity)) return;

        if (state.isIn(ModBlockTags.LOG_FORTUNE_LOGS) || state.isIn(BlockTags.LOGS)) {
            PlacedBlockTracker.markPlaced(serverWorld, pos, PlacedBlockTracker.LOGS_ID);
            PlacedBlockTracker.markPlaced(serverWorld, pos, PlacedBlockTracker.GATHERING_BLOCKS_ID);
        }

        boolean isOre = state.isIn(ModBlockTags.ORE_FORTUNE_ORES)
                || state.streamTags().anyMatch(tag -> tag.id().getPath().contains("ores"));
        if (isOre) {
            PlacedBlockTracker.markPlaced(serverWorld, pos, PlacedBlockTracker.ORES_ID);
            PlacedBlockTracker.markPlaced(serverWorld, pos, PlacedBlockTracker.GATHERING_BLOCKS_ID);
        }

        if (state.isIn(ModBlockTags.DIG_FORTUNE_BLOCKS)
                || state.isOf(Blocks.GRAVEL)
                || state.isOf(Blocks.CLAY)) {
            PlacedBlockTracker.markPlaced(serverWorld, pos, PlacedBlockTracker.DIG_BLOCKS_ID);
        }
    }
}
