package com.cranks.dungeons.mixin;

import com.cranks.dungeons.screen.LoadoutAlchemyHandler;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
abstract class BrewingLoadoutMixin {
    @Inject(method = "createScreenHandler", at = @At("HEAD"), cancellable = true)
    private void alchemyPanel(int syncId, PlayerInventory inventory, CallbackInfoReturnable<ScreenHandler> result) {
        var block = (BrewingStandBlockEntity) (Object) this;
        result.setReturnValue(new LoadoutAlchemyHandler(syncId, inventory, ScreenHandlerContext.create(block.getWorld(), block.getPos())));
    }
}
