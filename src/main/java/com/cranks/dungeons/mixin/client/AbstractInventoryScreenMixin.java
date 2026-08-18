package com.cranks.dungeons.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import com.cranks.dungeons.client.StatsPanelScrollHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {

    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void hideStatusEffectsWhenStatsPanelOpen(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if ((Object) this instanceof InventoryScreen && StatsPanelScrollHandler.isStatsVisible()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "drawStatusEffectDescriptions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
        )
    )
    private int hideInfinitySymbol(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color) {
        if (text.getString().equals("∞")) {
            return 0; // Skip drawing the text
        }
        return instance.drawTextWithShadow(textRenderer, text, x, y, color);
    }
}
