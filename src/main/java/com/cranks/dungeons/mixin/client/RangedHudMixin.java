package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.RangedHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract class RangedHudMixin {
    @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
    private void cranks$drawRangedProgress(DrawContext context,RenderTickCounter counter,CallbackInfo ci) {
        var client=MinecraftClient.getInstance();
        if(RangedHud.progress(client)>=0){RangedHud.crosshair(context,client);ci.cancel();}
    }
}
