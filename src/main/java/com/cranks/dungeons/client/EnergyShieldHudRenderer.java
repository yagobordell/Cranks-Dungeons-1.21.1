package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class EnergyShieldHudRenderer implements HudRenderCallback {
    
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");
    
    private static final boolean OVERFLOWING_BARS_LOADED = FabricLoader.getInstance().isModLoaded("overflowingbars");
    private static final long SHIELD_BLINK_DURATION = 1000;
    private float lastRenderedShield = -1;
    private long lastShieldDamageTime = 0;
    
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (client.options.hudHidden) return;
        
        PlayerEntity player = client.player;
        if (!(player instanceof EnergyShieldProvider shieldProvider)) return;
        
        float currentShield = shieldProvider.cranksdungeons$getCurrentEnergyShield();
        double maxShield = player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (maxShield <= 0) return;
        
        if (currentShield < 1.0f) currentShield = 0.0f;
        
        // Get screen dimensions
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Calculate health bar position (vanilla places it at screenWidth/2 - 91, screenHeight - 39)
        int healthBarX = screenWidth / 2 - 91;
        int healthBarY = screenHeight - 39;
        
        // Calculate health rows to position shields above
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();
        int totalHealthHearts = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        int healthRows = (int) Math.ceil(totalHealthHearts / 10.0);
        
        // Calculate shield info
        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int fullShieldHearts = (int) (currentShield / 2.0);
        boolean hasHalfShield = (currentShield % 2.0f) >= 1.0f;
        
        // Check for shield damage for blinking
        long currentTime = System.currentTimeMillis();
        if (lastRenderedShield >= 0 && currentShield != lastRenderedShield) {
            lastShieldDamageTime = currentTime;
        }
        
        long timeSinceChange = currentTime - lastShieldDamageTime;
        boolean shieldBlinking = timeSinceChange < SHIELD_BLINK_DURATION;
        if (shieldBlinking) {
            long phase = (timeSinceChange / 250) % 2;
            shieldBlinking = phase == 0;
        }
        
        // Calculate shield rows
        int shieldRows = (int) Math.ceil(totalShieldHearts / 10.0);
        
        if (OVERFLOWING_BARS_LOADED) {
            // With Overflowing Bars: Stack shields - show only last row with Nx count
            drawStackedShields(drawContext, client, healthBarX, healthBarY, healthRows, 
                             totalShieldHearts, fullShieldHearts, hasHalfShield, shieldBlinking, shieldRows);
        } else {
            // Without Overflowing Bars: Show all rows normally, no Nx count
            drawAllShieldRows(drawContext, client, healthBarX, healthBarY, healthRows, 
                            totalShieldHearts, fullShieldHearts, hasHalfShield, shieldBlinking);
        }
        
        lastRenderedShield = currentShield;
    }
    
    private void drawStackedShields(DrawContext drawContext, MinecraftClient client, int healthBarX, int healthBarY, 
                                   int healthRows, int totalShieldHearts, int fullShieldHearts, boolean hasHalfShield, 
                                   boolean shieldBlinking, int shieldRows) {
        // Show only the LAST row (like Overflowing Bars stacking)
        int startHeartIndex = (shieldRows - 1) * 10;
        int endHeartIndex = Math.min(startHeartIndex + 10, totalShieldHearts);
        
        int rowFullHearts = fullShieldHearts - startHeartIndex;
        if (rowFullHearts < 0) rowFullHearts = 0;
        
        // Position at the top (single row above health)
        int heartY = healthBarY - healthRows * 10 - 3;
        
        for (int i = startHeartIndex; i < endHeartIndex; i++) {
            int col = i % 10;
            int heartX = healthBarX + col * 8;
            
            // Skip if off-screen
            if (heartY < 0) continue;
            
            // Draw container
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("hud/heart/container_blinking") : 
                Identifier.of("hud/heart/container");
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            
            // Draw filled heart
            int relativeIndex = i - startHeartIndex;
            boolean isFilled = relativeIndex < rowFullHearts;
            boolean isHalf = (relativeIndex == rowFullHearts) && hasHalfShield && fullShieldHearts == i;
            
            Identifier texture = null;
            if (isFilled) {
                texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
            } else if (isHalf) {
                texture = shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF;
            }
            
            if (texture != null) {
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        
        // Draw row count (Nx) - only when Overflowing Bars is loaded and multiple rows exist
        if (shieldRows > 1) {
            String countText = shieldRows + "x";
            int textX = healthBarX - 12;
            int textY = heartY - 1;
            drawBorderedText(drawContext, client.textRenderer, countText, textX, textY, 0xFF55AAFF);
        }
    }

    private void drawBorderedText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, 
                                  String text, int x, int y, int color) {
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        int shadowColor = (alpha << 24);
        
        // Draw shadow on all four sides for border effect
        context.drawText(textRenderer, text, x - 1, y, shadowColor, false);
        context.drawText(textRenderer, text, x + 1, y, shadowColor, false);
        context.drawText(textRenderer, text, x, y - 1, shadowColor, false);
        context.drawText(textRenderer, text, x, y + 1, shadowColor, false);
        
        // Draw actual text
        context.drawText(textRenderer, text, x, y, color, false);
    }
    
    private void drawAllShieldRows(DrawContext drawContext, MinecraftClient client, int healthBarX, int healthBarY, 
                                  int healthRows, int totalShieldHearts, int fullShieldHearts, boolean hasHalfShield, 
                                  boolean shieldBlinking) {
        // Show all rows normally (no stacking)
        for (int i = 0; i < totalShieldHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = healthBarX + col * 8;
            int heartY = healthBarY - (row + healthRows) * 10 - 3;
            
            // Skip if off-screen
            if (heartY < 0) continue;
            
            // Draw container
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("hud/heart/container_blinking") : 
                Identifier.of("hud/heart/container");
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            
            // Draw filled heart
            boolean isFilled = i < fullShieldHearts;
            boolean isHalf = (i == fullShieldHearts) && hasHalfShield;
            
            Identifier texture = null;
            if (isFilled) {
                texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
            } else if (isHalf) {
                texture = shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF;
            }
            
            if (texture != null) {
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        // No row count drawn when Overflowing Bars is not loaded
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new EnergyShieldHudRenderer());
    }
}
