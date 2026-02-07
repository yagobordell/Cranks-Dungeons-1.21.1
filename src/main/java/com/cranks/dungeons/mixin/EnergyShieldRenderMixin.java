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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class EnergyShieldRenderMixin {

    @Shadow @Final private MinecraftClient client;
    @Unique private static int shieldRowCount = 0;

    @ModifyVariable(method = "renderHealthBar", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int modifyArmorY(int originalY) {
        return originalY - (shieldRowCount * 10);
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"))
    private void calculateShieldRows(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (client.player == null) { shieldRowCount = 0; return; }
        double maxShield = client.player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (maxShield <= 0) { shieldRowCount = 0; return; }

        int maxHealthHearts = (int) Math.ceil(maxHealth / 2.0f);
        int absorptionHearts = (int) Math.ceil(absorption / 2.0f); // Add absorption hearts count
        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int totalHearts = maxHealthHearts + absorptionHearts + totalShieldHearts; // Include absorption
        int totalRows = (int) Math.ceil(totalHearts / 10.0);
        shieldRowCount = totalRows - lines;
    }

    @Inject(method = "renderHealthBar", at = @At("RETURN"))
    private void renderEnergyShieldHearts(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (client.player == null) return;
        EnergyShieldProvider shieldProvider = (EnergyShieldProvider) client.player;
        float currentShield = shieldProvider.cranksdungeons$getCurrentEnergyShield();
        double maxShield = client.player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (maxShield <= 0) return;

        if (currentShield < 1.0f) currentShield = 0.0f;

        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int fullShieldHearts = (int) (currentShield / 2.0);
        boolean hasHalfShield = (currentShield % 2.0f) >= 1.0f;
        int maxHealthHearts = (int) Math.ceil(maxHealth / 2.0f);
        int absorptionHearts = (int) Math.ceil(absorption / 2.0f); // Calculate absorption hearts

        for (int i = 0; i < totalShieldHearts; i++) {
            // Shield hearts start after health hearts AND absorption hearts
            int heartIndex = maxHealthHearts + absorptionHearts + i;
            int row = heartIndex / 10;
            int col = heartIndex % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;

            boolean isFilled = i < fullShieldHearts;
            boolean isHalf = (i == fullShieldHearts) && hasHalfShield;
            drawShieldHeart(context, heartX, heartY, isFilled, isHalf);
        }
    }

    @Unique
    private void drawShieldHeart(DrawContext context, int x, int y, boolean filled, boolean half) {
        int outline = 0xFF000000;
        int heartBlue = 0xFF527FBF;
        int white = 0xFFFFFFFF;
        int bgInside = 0xFF222222;

        if (half) {
            drawPixelatedHeartHalf(context, x, y, heartBlue, white, outline, bgInside);
        } else if (filled) {
            drawPixelatedHeart(context, x, y, heartBlue, white, outline, false);
        } else {
            drawPixelatedHeart(context, x, y, bgInside, bgInside, outline, false);
        }
    }

    @Unique
    private void drawPixelatedHeart(DrawContext context, int x, int y, int color, int shine, int outline, boolean leftHalfOnly) {
        // Row 0: Top flat parts
        context.fill(x + 2, y, x + 4, y + 1, outline);
        if (!leftHalfOnly) context.fill(x + 5, y, x + 7, y + 1, outline);

        // Row 1: Radius/Shoulder
        context.fill(x + 1, y + 1, x + 2, y + 2, outline);
        context.fill(x + 2, y + 1, x + 4, y + 2, color);
        context.fill(x + 4, y + 1, x + 5, y + 2, outline); // Middle Dip
        if (!leftHalfOnly) {
            context.fill(x + 5, y + 1, x + 7, y + 2, color);
            context.fill(x + 7, y + 1, x + 8, y + 2, outline);
        }

        // Row 2: Shine Row (Shine at x+2)
        context.fill(x, y + 2, x + 1, y + 3, outline);
        context.fill(x + 1, y + 2, x + 2, y + 3, color);
        context.fill(x + 2, y + 2, x + 3, y + 3, shine);
        context.fill(x + 3, y + 2, x + 4, y + 3, color);
        if (!leftHalfOnly) {
            context.fill(x + 4, y + 2, x + 8, y + 3, color);
            context.fill(x + 8, y + 2, x + 9, y + 3, outline);
        } else {
            context.fill(x + 4, y + 2, x + 5, y + 3, color); // Removed border
        }

        // Rows 3 & 4: Full Body
        for (int r = 3; r <= 4; r++) {
            context.fill(x, y + r, x + 1, y + r + 1, outline);
            context.fill(x + 1, y + r, x + 4, y + r + 1, color);
            if (!leftHalfOnly) {
                context.fill(x + 4, y + r, x + 8, y + r + 1, color);
                context.fill(x + 8, y + r, x + 9, y + r + 1, outline);
            } else {
                context.fill(x + 4, y + r, x + 5, y + r + 1, color); // Removed border
            }
        }

        // Row 5: Start Taper
        context.fill(x + 1, y + 5, x + 2, y + 6, outline);
        context.fill(x + 2, y + 5, x + 4, y + 6, color);
        if (!leftHalfOnly) {
            context.fill(x + 4, y + 5, x + 7, y + 6, color);
            context.fill(x + 7, y + 5, x + 8, y + 6, outline);
        } else {
            context.fill(x + 4, y + 5, x + 5, y + 6, color); // Removed border
        }

        // Row 6: Narrowing
        context.fill(x + 2, y + 6, x + 3, y + 7, outline);
        context.fill(x + 3, y + 6, x + 4, y + 7, color);
        if (!leftHalfOnly) {
            context.fill(x + 4, y + 6, x + 6, y + 7, color);
            context.fill(x + 6, y + 6, x + 7, y + 7, outline);
        } else {
            context.fill(x + 4, y + 6, x + 5, y + 7, color); // Removed border
        }

        // Row 7: Tip
        context.fill(x + 3, y + 7, x + 4, y + 8, outline);
        if (!leftHalfOnly) {
            context.fill(x + 4, y + 7, x + 5, y + 8, color);
            context.fill(x + 5, y + 7, x + 6, y + 8, outline);
        } else {
            context.fill(x + 4, y + 7, x + 5, y + 8, color); // Removed border
        }

        // Row 8: Bottom Point
        if (!leftHalfOnly) {
            context.fill(x + 4, y + 8, x + 5, y + 9, outline);
        }
    }

    @Unique
    private void drawPixelatedHeartHalf(DrawContext context, int x, int y, int bColor, int bShine, int outline, int bgColor) {
        // Draw blue left side (no right border)
        drawPixelatedHeart(context, x, y, bColor, bShine, outline, true);

        // Draw gray right side (no left border)
        context.fill(x + 5, y, x + 7, y + 1, outline); // Row 0
        context.fill(x + 5, y + 1, x + 7, y + 2, bgColor); // Row 1
        context.fill(x + 7, y + 1, x + 8, y + 2, outline);

        context.fill(x + 5, y + 2, x + 8, y + 3, bgColor); // Row 2-4
        context.fill(x + 8, y + 2, x + 9, y + 3, outline);
        context.fill(x + 5, y + 3, x + 8, y + 4, bgColor);
        context.fill(x + 8, y + 3, x + 9, y + 4, outline);
        context.fill(x + 5, y + 4, x + 8, y + 5, bgColor);
        context.fill(x + 8, y + 4, x + 9, y + 5, outline);

        context.fill(x + 5, y + 5, x + 7, y + 6, bgColor); // Row 5
        context.fill(x + 7, y + 5, x + 8, y + 6, outline);
        context.fill(x + 5, y + 6, x + 6, y + 7, bgColor); // Row 6
        context.fill(x + 6, y + 6, x + 7, y + 7, outline);

        context.fill(x + 5, y + 7, x + 6, y + 8, outline); // Row 7 Tip
        context.fill(x + 4, y + 8, x + 5, y + 9, outline); // Row 8 Point
    }
}