package com.cranks.dungeons.mixin;

import com.cranks.dungeons.screen.LoadoutCraftingTableHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingTableBlock.class)
abstract class CraftingTableLoadoutMixin {
    @Inject(method = "createScreenHandlerFactory", at = @At("HEAD"), cancellable = true)
    private void openCraftingPanel(BlockState state, World world, BlockPos pos,
                                   CallbackInfoReturnable<NamedScreenHandlerFactory> result) {
        if (state.isOf(net.minecraft.block.Blocks.FLETCHING_TABLE)) {
            result.setReturnValue(new SimpleNamedScreenHandlerFactory(
                    (id, inventory, player) -> new com.cranks.dungeons.screen.LoadoutFletchingHandler(id, inventory, ScreenHandlerContext.create(world, pos)), state.getBlock().getName()));
            return;
        }
        result.setReturnValue(new SimpleNamedScreenHandlerFactory(
                (id, inventory, player) -> new LoadoutCraftingTableHandler(id, inventory, ScreenHandlerContext.create(world, pos)),
                Text.translatable("block.minecraft.crafting_table")));
    }
}
