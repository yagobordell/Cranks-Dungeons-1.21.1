package com.cranks.dungeons.mixin;

import com.cranks.dungeons.block.entity.ModBlockEntities;
import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTableBlock.class)
public class SmithingTableBlockMixin implements net.minecraft.block.BlockEntityProvider {

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QualityStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        if (type == ModBlockEntities.QUALITY_STATION_BE) {
            return (world1, pos, state1, blockEntity) -> ((QualityStationBlockEntity) blockEntity).tick(world1, pos, state1);
        }
        return null;
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void cranksDungeons$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof QualityStationBlockEntity) {
            NamedScreenHandlerFactory screenHandlerFactory = (NamedScreenHandlerFactory) blockEntity;
            player.openHandledScreen(screenHandlerFactory);
        }

        cir.setReturnValue(ActionResult.CONSUME);
    }
}
