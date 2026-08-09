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
}
