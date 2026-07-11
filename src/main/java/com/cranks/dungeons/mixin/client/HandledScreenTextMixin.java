package com.cranks.dungeons.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public class HandledScreenTextMixin {

    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    protected void suppressForegroundText(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
    }
}
