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
    
    // New textures for current row (stacking effect with Overflowing Bars)
    private static final Identifier SHIELD_FULL_CURRENT = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_current");
    private static final Identifier SHIELD_HALF_CURRENT = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_current");
    private static final Identifier SHIELD_FULL_CURRENT_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_current_blinking");
    private static final Identifier SHIELD_HALF_CURRENT_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_current_blinking");
    
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
        
        // Shields replace armor bar - position at armor bar height (above health)
        int armorBarY = healthBarY - 10;
        
        if (OVERFLOWING_BARS_LOADED) {
            // With Overflowing Bars: Stack shields - show only last row with Nx count
            drawStackedShields(drawContext, client, healthBarX, armorBarY, 
                             totalShieldHearts, fullShieldHearts, hasHalfShield, shieldBlinking, shieldRows);
        } else {
            // Without Overflowing Bars: Show all rows normally, no Nx count
            drawAllShieldRows(drawContext, client, healthBarX, armorBarY, 
                            totalShieldHearts, fullShieldHearts, hasHalfShield, shieldBlinking);
        }
        
        lastRenderedShield = currentShield;
    }
    
    private void drawStackedShields(DrawContext drawContext, MinecraftClient client, int healthBarX, int armorBarY, 
                                   int totalShieldHearts, int fullShieldHearts, boolean hasHalfShield, 
                                   boolean shieldBlinking, int shieldRows) {
        // Calculate which row we're currently on based on current shield (not max)
        int currentRow = (fullShieldHearts / 10) + 1;
        if (currentRow < 1) currentRow = 1;
        if (currentRow > shieldRows) currentRow = shieldRows;
        
        // Calculate filled hearts in the current row
        int filledInCurrentRow = fullShieldHearts % 10;
        if (filledInCurrentRow == 0 && fullShieldHearts > 0) {
            filledInCurrentRow = 10; // Full row
        }
        
        // Position at armor bar height (replacing armor bar)
        int heartY = armorBarY;
        
        // Draw up to 10 hearts for the current row
        for (int i = 0; i < 10; i++) {
            int col = i;
            int heartX = healthBarX + col * 8;
            
            // Skip if off-screen
            if (heartY < 0) continue;
            
            // Draw container
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("hud/heart/container_blinking") : 
                Identifier.of("hud/heart/container");
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            
            // Draw filled heart if within filled count
            boolean isFilled = i < filledInCurrentRow;
            boolean isHalf = (i == filledInCurrentRow) && hasHalfShield;
            boolean isEmpty = i >= filledInCurrentRow && !isHalf;
            
            Identifier texture = null;
            // Use special "current" textures when you're on row 2+ (stacking effect)
            // Row 1 (0-10 shield): use normal textures
            // Row 2+ (11+ shield): use current textures for filled, normal for empty
            if (currentRow > 1) {
                // On row 2 or higher: use new textures for current row filled/half hearts
                if (isFilled) {
                    texture = shieldBlinking ? SHIELD_FULL_CURRENT_BLINKING : SHIELD_FULL_CURRENT;
                } else if (isHalf) {
                    texture = shieldBlinking ? SHIELD_HALF_CURRENT_BLINKING : SHIELD_HALF_CURRENT;
                } else if (isEmpty) {
                    // Empty hearts show normal shield texture (simulates row underneath)
                    texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                }
            } else {
                // On row 1: use normal textures
                if (isFilled) {
                    texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                } else if (isHalf) {
                    texture = shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF;
                }
                // Note: Empty hearts in single row don't need a texture - the container is enough
            }
            
            if (texture != null) {
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        
        // Draw row count (Nx) - only show when on row 2 or higher
        if (currentRow > 1) {
            drawSpriteRowCount(drawContext, currentRow, healthBarX - 13, heartY - 2);
        }
    }
    
    private void drawSpriteRowCount(DrawContext context, int currentRow, int x, int y) {
        // Use individual sprite textures: 1x.png, 2x.png, etc.
        Identifier spriteId = ShieldNumberSprites.getSpriteId(currentRow);
        
        // Draw the sprite (assuming 16x16 or similar size)
        // Position adjusted: moved 1 pixel up and 1 pixel left from original
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, spriteId, x, y, 16, 16);
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
    
    private void drawScaledBorderedText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, 
                                        String text, int x, int y, int color, float scale) {
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        int shadowColor = (alpha << 24);
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        
        // Draw shadow on all four sides for border effect
        context.drawText(textRenderer, text, -1, 0, shadowColor, false);
        context.drawText(textRenderer, text, 1, 0, shadowColor, false);
        context.drawText(textRenderer, text, 0, -1, shadowColor, false);
        context.drawText(textRenderer, text, 0, 1, shadowColor, false);
        
        // Draw actual text
        context.drawText(textRenderer, text, 0, 0, color, false);
        
        context.getMatrices().popMatrix();
    }
    
    private void drawAllShieldRows(DrawContext drawContext, MinecraftClient client, int healthBarX, int armorBarY, 
                                  int totalShieldHearts, int fullShieldHearts, boolean hasHalfShield, 
                                  boolean shieldBlinking) {
        // Show all rows normally at armor bar height (replacing armor bar)
        for (int i = 0; i < totalShieldHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = healthBarX + col * 8;
            // Position at armor bar height, with additional rows going upward
            int heartY = armorBarY - row * 10;
            
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
