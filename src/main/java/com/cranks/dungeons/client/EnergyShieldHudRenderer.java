package com.cranks.dungeons.client;

import com.cranks.dungeons.client.render.HotbarHudRenderer;
import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class EnergyShieldHudRenderer implements HudRenderCallback {
    
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");
    

    private static final long SHIELD_BLINK_DURATION = 1000;
    private float lastRenderedShield = -1;
    private long lastShieldDamageTime = 0;
    
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen) return;
        if (client.player == null) return;
        if (client.options.hudHidden || !client.player.isAlive() || com.cranks.dungeons.CranksDungeonsClient.isCinematicRespawnActive()) return;
        if (com.cranks.dungeons.CranksDungeons.CONFIG.useStatBars()) return;
        if (client.player.isCreative() || client.player.isSpectator()) return;
        
        PlayerEntity player = client.player;
        if (!(player instanceof EnergyShieldProvider shieldProvider)) return;
        
        float currentShield = shieldProvider.cranksdungeons$getCurrentEnergyShield() / 2.5f;
        double maxShield = player.getAttributeValue(ModAttributes.ENERGY_SHIELD) / 2.5;
        if (player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) {
            maxShield = 0;
        }
        if (maxShield <= 0) return;
        
        if (currentShield < 0.4f) currentShield = 0.0f;
        
        // Get screen dimensions
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Calculate health bar position (vanilla places it at screenWidth/2 - 91, screenHeight - 39)
        int healthBarX = screenWidth / 2 - 91;
        int healthBarY = screenHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        
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
        
        boolean overflowingBars = com.cranks.dungeons.util.CombatUtility.isOverflowingBarsLoaded();

        // Calculate shield rows
        int shieldRows = (int) Math.ceil(totalShieldHearts / 10.0);
        
        // Position shields directly above the health hearts.
        // Anchor the shield above the scaled health rows.
        int scaledMaxHealthHearts = (int) Math.ceil((player.getMaxHealth() / 2.5f) / 2.0f);
        int scaledAbsorptionHearts = (int) Math.ceil((player.getAbsorptionAmount() / 2.5f) / 2.0f);
        int scaledHealthRows = overflowingBars ? 1 : Math.max(1, (int) Math.ceil((scaledMaxHealthHearts + scaledAbsorptionHearts) / 10.0));
        int scaledSpacing = Math.max(10 - (scaledHealthRows - 2), 3);
        int shieldBaseY = healthBarY - 10 - ((scaledHealthRows - 1) * scaledSpacing);
        
        if (overflowingBars) {
            drawOverflowingShieldRow(drawContext, client, healthBarX, shieldBaseY, (float) maxShield, effectiveShield, shieldBlinking);
        } else {
            drawAllShieldRows(drawContext, client, healthBarX, shieldBaseY, 
                            totalShieldHearts, effectiveShield, shieldBlinking);
        }
        
        lastRenderedShield = currentShield;
    }

    private void drawOverflowingShieldRow(DrawContext drawContext, MinecraftClient client, int healthBarX, int shieldBaseY,
                                          float maxShield, float currentShield, boolean shieldBlinking) {
        // maxShield is in 2-points-per-heart units (20.0 = 10 hearts = 1 full bar = 50 ES)
        int currentShieldLayer = Math.max(1, (int) Math.ceil(currentShield / 20.0f));
        float layerShield = currentShield - (currentShieldLayer - 1) * 20.0f;
        if (currentShield <= 0.0f) {
            layerShield = 0.0f;
        }

        Identifier containerTexture = shieldBlinking ?
                Identifier.of("minecraft", "hud/heart/container_blinking") :
                Identifier.of("minecraft", "hud/heart/container");

        for (int col = 0; col < 10; col++) {
            int heartX = healthBarX + col * 8;
            int heartY = shieldBaseY;

            drawContext.drawGuiTexture(containerTexture, heartX, heartY, 9, 9);

            float baseVisibility = col * 2.0f;
            Identifier texture = null;
            if (layerShield >= baseVisibility + 1.01f) {
                texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
            } else if (layerShield >= baseVisibility + 0.01f) {
                texture = shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF;
            }

            if (texture != null) {
                drawContext.drawGuiTexture(texture, heartX, heartY, 9, 9);
            }
        }

        if (currentShieldLayer > 1) {
            String badge = currentShieldLayer + "x";
            int textWidth = client.textRenderer.getWidth(badge);
            int textX = healthBarX - textWidth - 2;
            int textY = shieldBaseY + 1;
            drawBorderedText(drawContext, client.textRenderer, badge, textX, textY, 0xFFFFFFFF);
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
    
    private void drawScaledBorderedText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, 
                                        String text, int x, int y, int color, float scale) {
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        int shadowColor = (alpha << 24);
        
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        
        // Draw shadow on all four sides for border effect
        context.drawText(textRenderer, text, -1, 0, shadowColor, false);
        context.drawText(textRenderer, text, 1, 0, shadowColor, false);
        context.drawText(textRenderer, text, 0, -1, shadowColor, false);
        context.drawText(textRenderer, text, 0, 1, shadowColor, false);
        
        // Draw actual text
        context.drawText(textRenderer, text, 0, 0, color, false);
        
        context.getMatrices().pop();
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
            drawContext.drawGuiTexture(containerTexture, heartX, heartY, 9, 9);
            
            float baseVisibility = i * 2.0f;
            Identifier texture = null;
            if (currentShield >= baseVisibility + 1.01f) {
                texture = shieldBlinking ? SHIELD_FULL_BLINKING : SHIELD_FULL;
            } else if (currentShield >= baseVisibility + 0.01f) {
                texture = shieldBlinking ? SHIELD_HALF_BLINKING : SHIELD_HALF;
            }
            
            if (texture != null) {
                drawContext.drawGuiTexture(texture, heartX, heartY, 9, 9);
            }
        }
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new EnergyShieldHudRenderer());
    }
}
