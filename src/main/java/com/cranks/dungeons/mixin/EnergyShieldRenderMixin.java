package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class EnergyShieldRenderMixin {

    @Shadow @Final private MinecraftClient client;
    private static final Identifier HEARTS = Identifier.ofVanilla("textures/gui/icons.png");

    @Inject(method = "renderHealthBar", at = @At("RETURN"))
    private void renderEnergyShield(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (client.player == null) return;

        EnergyShieldProvider shieldProvider = (EnergyShieldProvider) client.player;
        float currentShield = shieldProvider.cranks$getShield();
        double maxShield = shieldProvider.cranks$getMaxShield();

        if (maxShield <= 0 || currentShield <= 0) return;

        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int filledShieldHearts = (int) Math.floor(currentShield / 2.0);
        boolean hasHalfHeart = (currentShield % 2.0f) >= 1.0f;

        // --- RESTORED MISSING VARIABLE ---
        int shieldRows = (totalShieldHearts + 9) / 10;
        int shieldColor = 0xFF3399FF; // Light Blue (ARGB)

        for (int row = 0; row < shieldRows; row++) {
            int heartsInRow = Math.min(10, totalShieldHearts - (row * 10));
            int rowY = y - (row + 1) * 10;

            for (int i = 0; i < heartsInRow; i++) {
                int heartIndex = row * 10 + i;
                int heartX = x + i * 8;

                // 1. Draw Background (White/Normal)
                context.drawTexture(
                        net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                        HEARTS, heartX, rowY, 16.0f, 0.0f, 9, 9, 256, 256
                );

                // 2. Draw Shield Tints
                if (heartIndex <= filledShieldHearts) {
                    float u;
                    if (heartIndex < filledShieldHearts) {
                        u = 52.0f; // Full Heart
                    } else if (hasHalfHeart) {
                        u = 61.0f; // Half Heart
                    } else {
                        continue;
                    }

                    // Using the 10-parameter signature with color at the end
                    context.drawTexture(
                            net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                            HEARTS,
                            heartX, rowY,
                            u, 0.0f,
                            9, 9,
                            256, 256,
                            shieldColor
                    );
                }
            }
        }
    }
}