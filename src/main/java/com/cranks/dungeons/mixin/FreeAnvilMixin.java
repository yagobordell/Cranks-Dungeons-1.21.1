package com.cranks.dungeons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class FreeAnvilMixin {

    @Redirect(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;set(I)V")
    )
    private void constantZeroCost(Property property, int value) {
        // Ignore the 'value' vanilla calculated, always set 0.
        property.set(0);
    }

    @Inject(
            method = "canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void forceAllowTake(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}