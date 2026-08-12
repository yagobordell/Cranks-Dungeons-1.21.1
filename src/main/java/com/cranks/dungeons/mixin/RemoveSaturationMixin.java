package com.cranks.dungeons.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class RemoveSaturationMixin {

    @Shadow
    private float saturationLevel;

    @Inject(method = "update", at = @At("HEAD"))
    private void clearSaturationOnUpdate(net.minecraft.entity.player.PlayerEntity player, CallbackInfo ci) {
        this.saturationLevel = 0.0f;
    }

    @Inject(method = "add", at = @At("RETURN"))
    private void clearSaturationOnAdd(int food, float saturationModifier, CallbackInfo ci) {
        this.saturationLevel = 0.0f;
    }

    @org.spongepowered.asm.mixin.injection.Redirect(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canFoodHeal()Z")
    )
    private boolean disableNaturalRegeneration(net.minecraft.entity.player.PlayerEntity instance) {
        // Returning false completely disables vanilla natural regeneration (and the exhaustion penalty for it).
        return false;
    }
}
