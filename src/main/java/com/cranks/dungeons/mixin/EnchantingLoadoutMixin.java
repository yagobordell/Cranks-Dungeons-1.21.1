package com.cranks.dungeons.mixin;

import com.cranks.dungeons.screen.LoadoutEnchantingHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
abstract class EnchantingLoadoutMixin {
    @Inject(method = "createScreenHandlerFactory", at = @At("HEAD"), cancellable = true)
    private void openEnchantingPanel(BlockState state, World world, BlockPos pos,
                                    CallbackInfoReturnable<NamedScreenHandlerFactory> result) {
        if (!(world.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table)) return;
        var title = table.hasCustomName() ? table.getDisplayName() : state.getBlock().getName();
        result.setReturnValue(new SimpleNamedScreenHandlerFactory(
                (id, inventory, player) -> new LoadoutEnchantingHandler(id, inventory, ScreenHandlerContext.create(world, pos)),
                title));
    }
}
