package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class EnergyShieldRenderMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderHealthBar", at = @At("RETURN"))
    private void renderEnergyShield(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (client.player == null) return;

        EnergyShieldProvider shieldProvider = (EnergyShieldProvider) client.player;
        float currentShield = shieldProvider.cranksdungeons$getCurrentEnergyShield();
        double maxShield = client.player.getAttributeValue(ModAttributes.ENERGY_SHIELD);

        if (maxShield <= 0) return;

        // Calculate hearts (2 HP per heart)
        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int filledShieldHearts = (int) Math.floor(currentShield / 2.0);
        boolean hasHalfHeart = (currentShield % 2.0f) >= 1.0f;

        int heartsPerRow = 10;
        int shieldRows = (totalShieldHearts + heartsPerRow - 1) / heartsPerRow;

        for (int row = 0; row < shieldRows; row++) {
            int heartsInRow = Math.min(heartsPerRow, totalShieldHearts - (row * heartsPerRow));
            int rowY = y - ((shieldRows - row) * 10);

            for (int i = 0; i < heartsInRow; i++) {
                int heartIndex = row * heartsPerRow + i;
                int heartX = x + i * 8;

                boolean isFilled = heartIndex < filledShieldHearts;
                boolean isHalf = heartIndex == filledShieldHearts && hasHalfHeart;

                renderVanillaStyleHeart(context, heartX, rowY, isFilled, isHalf);
            }
        }
    }

    @Unique
    private void renderVanillaStyleHeart(DrawContext context, int x, int y, boolean filled, boolean half) {
        int black = 0xFF000000;
        int blue = 0xFF5599FF;
        int darkGray = 0xFF555555;

        // Background (container)
        drawHeartShape(context, x, y, black, darkGray, false);

        // Foreground (fill)
        if (filled || half) {
            drawHeartShape(context, x, y, black, blue, half);
        }
    }

    @Unique
    private void drawHeartShape(DrawContext context, int x, int y, int outline, int fill, boolean halfOnly) {
        // This draws a heart that looks like vanilla
        int w = halfOnly ? 4 : 9;

        // Top bumps outline
        context.fill(x + 2, y, x + 3, y + 1, outline);
        context.fill(x + 6, y, x + 7, y + 1, outline);

        // Sides outline
        context.fill(x, y + 2, x + 1, y + 5, outline);
        if (!halfOnly) context.fill(x + 8, y + 2, x + 9, y + 5, outline);

        // Point outline
        context.fill(x + 1, y + 5, x + 2, y + 7, outline);
        if (!halfOnly) context.fill(x + 7, y + 5, x + 8, y + 7, outline);
        context.fill(x + 2, y + 7, x + 3, y + 8, outline);
        if (!halfOnly) context.fill(x + 6, y + 7, x + 7, y + 8, outline);
        if (!halfOnly) context.fill(x + 3, y + 8, x + 6, y + 9, outline);
        else context.fill(x + 3, y + 8, x + 4, y + 9, outline);

        // Fill
        context.fill(x + 2, y + 1, x + (halfOnly ? 4 : 7), y + 2, fill);
        context.fill(x + 1, y + 2, x + (halfOnly ? 4 : 8), y + 5, fill);

        if (!halfOnly) {
            context.fill(x + 2, y + 5, x + 7, y + 7, fill);
            context.fill(x + 3, y + 7, x + 6, y + 8, fill);
        } else {
            context.fill(x + 2, y + 5, x + 4, y + 7, fill);
            context.fill(x + 3, y + 7, x + 4, y + 8, fill);
        }
    }
}