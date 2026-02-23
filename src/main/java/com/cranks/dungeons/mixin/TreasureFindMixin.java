package com.cranks.dungeons.mixin;

import com.cranks.dungeons.treasure.TreasureFindHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class TreasureFindMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Unique
    private BlockState dungeons$capturedState;

    @Inject(
            method = "tryBreakBlock",
            at = @At("HEAD")
    )
    private void dungeons$captureBlockState(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.dungeons$capturedState = this.world.getBlockState(pos);
    }

    @Inject(
            method = "tryBreakBlock",
            at = @At("RETURN")
    )
    private void dungeons$onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            // Block break was cancelled/failed
            return;
        }

        if (this.dungeons$capturedState == null) {
            return;
        }

        System.out.println("[TreasureFind Mixin] Block broken successfully at " + pos);

        ItemStack tool = this.player.getMainHandStack();
        TreasureFindHandler.tryFindTreasure(this.player, this.world, pos, this.dungeons$capturedState, tool);

        this.dungeons$capturedState = null;
    }
}