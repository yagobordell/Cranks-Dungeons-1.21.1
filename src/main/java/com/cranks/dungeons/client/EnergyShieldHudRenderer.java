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
        if (com.cranks.dungeons.CranksDungeons.CONFIG.useStatBars()) return;
        if (client.player.isCreative() || client.player.isSpectator()) return;
        
        PlayerEntity player = client.player;
        if (!(player instanceof EnergyShieldProvider shieldProvider)) return;
        
        float currentShield = shieldProvider.cranksdungeons$getCurrentEnergyShield();
        double maxShield = player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) {
            maxShield = 0;
        }
        if (maxShield <= 0) return;
        
        if (currentShield < 1.0f) currentShield = 0.0f;
        
        // Get screen dimensions
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Calculate health bar position (vanilla places it at screenWidth/2 - 91, screenHeight - 39)
        int healthBarX = screenWidth / 2 - 91;
        int healthBarY = screenHeight - 39;
        
        // Calculate shield info
        // Apply a small epsilon to avoid lingering half-hearts when effectively full.
        float effectiveShield = (currentShield >= maxShield - 0.1f) ? (float) maxShield : currentShield + 0.001f;

        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int fullShieldHearts = (int) (effectiveShield / 2.0);
        boolean hasHalfShield = (effectiveShield % 2.0f) >= 1.0f;
        
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
        
        // Calculate health rows based on MAX health (not current) so shields stay in fixed position
        // Use maxHealth from attributes to ensure consistent positioning
        int maxHealthHearts = (int) Math.ceil(player.getMaxHealth() / 2.0f);
        int absorptionHearts = (int) Math.ceil(player.getAbsorptionAmount() / 2.0f);
        int totalHealthRows = (int) Math.ceil((maxHealthHearts + absorptionHearts) / 10.0);
        
        // Shields replace armor bar - position above all health rows with proper spacing
        // Match vanilla Minecraft's dynamic spacing formula: Math.max(10 - (rows - 2), 3)
        // 1-2 rows: 10px spacing, 3 rows: 9px, 4 rows: 8px, etc. (min 3px for 9+ rows)
        int healthRowsToAccountFor = OVERFLOWING_BARS_LOADED ? 1 : Math.max(1, totalHealthRows);
        int vanillaSpacing = Math.max(10 - (healthRowsToAccountFor - 2), 3);
        // Armor bar is at -10px, then add spacing for each additional row above
        int armorBarY = healthBarY - 10 - ((healthRowsToAccountFor - 1) * vanillaSpacing);
        
        if (OVERFLOWING_BARS_LOADED) {
            drawStackedShields(drawContext, client, healthBarX, armorBarY, 
                             totalShieldHearts, (float)maxShield, effectiveShield, shieldBlinking, shieldRows);
        } else {
            drawAllShieldRows(drawContext, client, healthBarX, armorBarY, 
                            totalShieldHearts, effectiveShield, shieldBlinking);
        }
        
        lastRenderedShield = currentShield;
    }
    
    private void drawStackedShields(DrawContext drawContext, MinecraftClient client, int healthBarX, int armorBarY, 
                                   int totalShieldHearts, float maxShield, float currentShield, 
                                   boolean shieldBlinking, int shieldRows) {
        // Calculate which row we're currently on based on CURRENT shield
        int highestIconIndex = (int) Math.ceil(currentShield / 2.0f) - 1;
        int currentRow = Math.max(1, (highestIconIndex / 10) + 1);
        if (currentRow > shieldRows) currentRow = shieldRows;
        
        int heartY = armorBarY;
        int heartsInFullRowsBelow = (currentRow - 1) * 10;
        
        // Always show 10 hearts if total capacity is more than 10, to keep bar wide
        int heartsToShow = totalShieldHearts > 10 ? 10 : totalShieldHearts;

        for (int i = 0; i < heartsToShow; i++) {
            int iconIndex = heartsInFullRowsBelow + i;
            int heartX = healthBarX + i * 8;
            
            if (heartY < 0) continue;
            
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("minecraft", "hud/heart/container_blinking") : 
                Identifier.of("minecraft", "hud/heart/container");
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            
            float baseVisibility = iconIndex * 2.0f;
            Identifier texture = null;

            boolean isLastRow = (currentRow == shieldRows);
            boolean isFirstRow = (currentRow == 1);
            boolean useCurrentTextures = !isFirstRow;

            // 1. If this icon slot is within our current row's capacity
            if (iconIndex < totalShieldHearts) {
                if (currentShield >= baseVisibility + 1.01f) {
                    texture = useCurrentTextures ? (shieldBlinking ? SHIELD_FULL_CURRENT_BLINKING : SHIELD_FULL_CURRENT) 
                                               : (shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL);
                } else if (currentShield >= baseVisibility + 0.01f) {
                    texture = useCurrentTextures ? (shieldBlinking ? SHIELD_HALF_CURRENT_BLINKING : SHIELD_HALF_CURRENT) 
                                               : (shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF);
                } else if (!isFirstRow) {
                    // Show filled hearts from row below as background in ALL rows above 1
                    texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
                }
            } 
            // 2. If this icon is PAST our current row capacity but we are showing it for width
            else if (totalShieldHearts > 10) {
                // Should show the filled hearts from the COMPLETED row underneath
                texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
            }
            
            if (texture != null) {
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        
        if (currentRow > 1 && totalShieldHearts > 10) {
            drawSpriteRowCount(drawContext, currentRow, healthBarX - 13, heartY - 2);
        }
    }
    
    private void drawSpriteRowCount(DrawContext context, int currentRow, int x, int y) {
        Identifier spriteId = ShieldNumberSprites.getSpriteId(currentRow);
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
                                  int totalShieldHearts, float currentShield, 
                                  boolean shieldBlinking) {
        int shieldRows = (int) Math.ceil(totalShieldHearts / 10.0);
        int vanillaSpacing = Math.max(10 - (shieldRows - 2), 3);
        
        for (int i = 0; i < totalShieldHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = healthBarX + col * 8;
            int heartY = armorBarY - row * vanillaSpacing;
            
            if (heartY < 0) continue;
            
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("minecraft", "hud/heart/container_blinking") : 
                Identifier.of("minecraft", "hud/heart/container");
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            
            float baseVisibility = i * 2.0f;
            Identifier texture = null;
            if (currentShield >= baseVisibility + 1.01f) {
                texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
            } else if (currentShield >= baseVisibility + 0.01f) {
                texture = shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF;
            }
            
            if (texture != null) {
                drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new EnergyShieldHudRenderer());
    }
}
