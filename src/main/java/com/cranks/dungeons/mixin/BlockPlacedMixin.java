package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.PlacedLogTracker;
import com.cranks.dungeons.util.PlacedDigBlockTracker;
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

        // Logs for Log Fortune: use our tag OR vanilla logs as fallback
        if (state.isIn(ModBlockTags.LOG_FORTUNE_LOGS) || state.isIn(BlockTags.LOGS)) {
            PlacedLogTracker.markPlaced(serverWorld, pos);
        }

        // Dig fortune blocks: use our tag OR vanilla gravel/clay as fallback
        if (state.isIn(ModBlockTags.DIG_FORTUNE_BLOCKS)
                || state.isOf(Blocks.GRAVEL)
                || state.isOf(Blocks.CLAY)) {
            PlacedDigBlockTracker.markPlaced(serverWorld, pos);
        }
    }
}