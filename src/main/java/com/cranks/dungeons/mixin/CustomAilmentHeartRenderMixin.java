package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class CustomAilmentHeartRenderMixin {

    @Unique
    private static final Identifier POISON_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/full");
    @Unique
    private static final Identifier POISON_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/half");
    @Unique
    private static final Identifier POISON_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/full_blinking");
    @Unique
    private static final Identifier POISON_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/half_blinking");
    @Unique
    private static final Identifier POISON_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_full");
    @Unique
    private static final Identifier POISON_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_half");
    @Unique
    private static final Identifier POISON_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_full_blinking");
    @Unique
    private static final Identifier POISON_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_half_blinking");

    @Unique
    private static final Identifier BURN_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/full");
    @Unique
    private static final Identifier BURN_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/half");
    @Unique
    private static final Identifier BURN_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/full_blinking");
    @Unique
    private static final Identifier BURN_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/half_blinking");
    @Unique
    private static final Identifier BURN_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_full");
    @Unique
    private static final Identifier BURN_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_half");
    @Unique
    private static final Identifier BURN_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_full_blinking");
    @Unique
    private static final Identifier BURN_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_half_blinking");

    @Unique
    private static final Identifier FREEZE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/full");
    @Unique
    private static final Identifier FREEZE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/half");
    @Unique
    private static final Identifier FREEZE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/full_blinking");
    @Unique
    private static final Identifier FREEZE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/half_blinking");
    @Unique
    private static final Identifier FREEZE_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_full");
    @Unique
    private static final Identifier FREEZE_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_half");
    @Unique
    private static final Identifier FREEZE_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_full_blinking");
    @Unique
    private static final Identifier FREEZE_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_half_blinking");

    @Unique
    private static final Identifier BLEED_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/full");
    @Unique
    private static final Identifier BLEED_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/half");
    @Unique
    private static final Identifier BLEED_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/full_blinking");
    @Unique
    private static final Identifier BLEED_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/half_blinking");
    @Unique
    private static final Identifier BLEED_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_full");
    @Unique
    private static final Identifier BLEED_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_half");
    @Unique
    private static final Identifier BLEED_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_full_blinking");
    @Unique
    private static final Identifier BLEED_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_half_blinking");

    // Energy Shield textures
    @Unique
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    @Unique
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    @Unique
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    @Unique
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");

    // Store the last rendered health for smooth interpolation
    @Unique
    private float cranks$lastRenderedHealth = -1;
    @Unique
    private float cranks$lastRenderedShield = -1;
    @Unique
    private long cranks$lastShieldDamageTime = 0;
    @Unique
    private static final long SHIELD_BLINK_DURATION = 1000;
    @Unique
    private static long lastRenderFrame = -1;
    
    // Check if Overflowing Bars is loaded
    @Unique
    private static final boolean OVERFLOWING_BARS_LOADED = FabricLoader.getInstance().isModLoaded("overflowingbars");

    @Unique
    private static boolean debugLogged = false;

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void drawCustomHearts(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (!debugLogged) {
            CranksDungeons.LOGGER.info("CustomAilmentHeartRenderMixin.drawCustomHearts called! OverflowingBars loaded: {}", OVERFLOWING_BARS_LOADED);
            debugLogged = true;
        }
        // Check if player has custom ailment (Priority: Poison > Burn > Bleed > Freeze)
        AilmentType ailment = null;
        if (AilmentManager.hasAilment(player, AilmentType.POISON)) {
            ailment = AilmentType.POISON;
        } else if (AilmentManager.hasAilment(player, AilmentType.BURN)) {
            ailment = AilmentType.BURN;
        } else if (AilmentManager.hasAilment(player, AilmentType.BLEED)) {
            ailment = AilmentType.BLEED;
        } else if (AilmentManager.hasAilment(player, AilmentType.FREEZE)) {
            ailment = AilmentType.FREEZE;
        }
        
        // If no ailment, let vanilla/Overflowing Bars handle it normally
        if (ailment == null) {
            cranks$lastRenderedHealth = -1; // Reset
            if (OVERFLOWING_BARS_LOADED) {
                ci.cancel(); // Cancel vanilla, we'll render everything
                renderWithOverflowingBars(context, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking, ailment);
            }
            // Without Overflowing Bars: EnergyShieldRenderMixin will handle shield rendering at RETURN
            return;
        }
        
        if (OVERFLOWING_BARS_LOADED) {
            // Use compatibility mode with Overflowing Bars
            ci.cancel();
            renderWithOverflowingBars(context, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking, ailment);
        } else {
            // Full custom rendering (no Overflowing Bars)
            ci.cancel();
            renderFullCustom(context, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking, ailment);
        }
    }
    
    @Unique
    private void renderWithOverflowingBars(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, AilmentType ailment) {
        boolean hardcore = player.getEntityWorld().getLevelProperties().isHardcore();
        
        // Calculate health values
        int displayHealth = health;
        int totalHealthRows = (int) Math.ceil((health + absorption) / 20.0);
        int currentHealthHearts = (int) Math.ceil(health / 2.0);
        int fullHearts = health / 2;
        boolean hasHalf = (health % 2) == 1;
        
        // Calculate absorption hearts
        int absorptionHearts = (int) Math.ceil(absorption / 2.0);
        int totalFilledHearts = currentHealthHearts + absorptionHearts;
        
        // Draw heart containers (for all hearts including absorption)
        int maxHeartPositions = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        Identifier containerTexture = getContainerTexture(hardcore, blinking);
        for (int i = 0; i < maxHeartPositions; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            
            if (containerTexture != null) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            }
        }
        
        // Draw vanilla-style filled hearts (red for health, gold for absorption)
        // Health hearts (red)
        for (int i = 0; i < currentHealthHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            boolean isHalf = (i == fullHearts) && hasHalf && i == currentHealthHearts - 1;
            
            // Use vanilla texture or ailment overlay
            if (ailment != null) {
                Identifier texture = getTexture(ailment, hardcore, blinking, isHalf);
                if (texture != null) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
                }
            } else {
                // Vanilla red hearts
                Identifier texture = getVanillaHealthTexture(hardcore, blinking, isHalf);
                if (texture != null) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
                }
            }
        }
        
        // Absorption hearts (gold/yellow)
        for (int i = 0; i < absorptionHearts; i++) {
            int heartIndex = currentHealthHearts + i;
            int row = heartIndex / 10;
            int col = heartIndex % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            
            int absorptionFull = absorption / 2;
            boolean isHalfAbsorption = (i == absorptionFull) && (absorption % 2) == 1 && i == absorptionHearts - 1;
            
            // Apply ailment overlay to absorption hearts too
            if (ailment != null) {
                Identifier texture = getTexture(ailment, hardcore, blinking, isHalfAbsorption);
                if (texture != null) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
                }
            } else {
                // Vanilla absorption texture
                Identifier texture = getVanillaAbsorptionTexture(hardcore, blinking, isHalfAbsorption);
                if (texture != null) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
                }
            }
        }
        
        // Draw regenerating heart
        if (regeneratingHeartIndex >= 0 && regeneratingHeartIndex < totalFilledHearts) {
            int row = regeneratingHeartIndex / 10;
            int col = regeneratingHeartIndex % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            
            // Determine if this was a half heart
            int lostFullHearts = lastHealth / 2;
            boolean lostHasHalf = (lastHealth % 2) == 1;
            boolean wasHalf = (regeneratingHeartIndex == lostFullHearts) && lostHasHalf;
            
            if (ailment != null) {
                Identifier texture = getTexture(ailment, hardcore, true, wasHalf);
                if (texture != null) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
                }
            } else {
                Identifier texture = getVanillaHealthTexture(hardcore, true, wasHalf);
                if (texture != null) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
                }
            }
        }
        
        // Draw row count for health
        if (totalHealthRows > 1 || absorption > 0) {
            int rowCount = totalHealthRows;
            String countText = rowCount + "x";
            int textX = x - 12;
            int textY = y - (totalHealthRows - 1) * 10 - 1;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, countText, textX, textY, 0xFFAA5500); // Orange color for row count
        }
        
        // Energy shields are now rendered independently by EnergyShieldHudRenderer
        // drawEnergyShieldHearts(context, player, x, y, maxHealth, absorption, blinking);
    }

    @Unique
    private void renderFullCustom(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, AilmentType ailment) {
        boolean hardcore = player.getEntityWorld().getLevelProperties().isHardcore();
        
        // Smoothly interpolate the rendered health value
        float actualHealth = player.getHealth();
        if (cranks$lastRenderedHealth < 0) {
            cranks$lastRenderedHealth = actualHealth;
        }
        // Smoothly approach actual health (same speed as vanilla)
        if (cranks$lastRenderedHealth > actualHealth) {
            cranks$lastRenderedHealth = Math.max(actualHealth, cranks$lastRenderedHealth - 0.1f);
        } else if (cranks$lastRenderedHealth < actualHealth) {
            cranks$lastRenderedHealth = Math.min(actualHealth, cranks$lastRenderedHealth + 0.1f);
        }
        
        // Use interpolated health for rendering
        int displayHealth = (int) cranks$lastRenderedHealth;
        int fullHearts = displayHealth / 2;
        boolean hasHalf = (displayHealth % 2) == 1;
        int totalHeartPositions = fullHearts + (hasHalf ? 1 : 0);
        
        // Calculate max hearts for container drawing (includes absorption)
        int maxHeartPositions = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        
        // Draw heart containers first (empty hearts)
        Identifier containerTexture = getContainerTexture(hardcore, blinking);
        for (int i = 0; i < maxHeartPositions; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            
            if (containerTexture != null) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);
            }
        }
        
        // Draw custom heart texture over each health heart position
        for (int i = 0; i < totalHeartPositions; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            
            // Determine if this is the half heart
            boolean isHalf = (i == fullHearts) && hasHalf;
            
            // Get the appropriate texture
            Identifier texture = getTexture(ailment, hardcore, blinking, isHalf);
            
            if (texture != null) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        
        // Draw custom texture for the regenerating (lost health) heart position
        if (regeneratingHeartIndex >= 0 && regeneratingHeartIndex < totalHeartPositions) {
            int row = regeneratingHeartIndex / 10;
            int col = regeneratingHeartIndex % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;
            
            // Determine if this was a half heart
            int lostFullHearts = lastHealth / 2;
            boolean lostHasHalf = (lastHealth % 2) == 1;
            boolean wasHalf = (regeneratingHeartIndex == lostFullHearts) && lostHasHalf;
            
            // Use blinking texture for regenerating heart
            Identifier texture = getTexture(ailment, hardcore, true, wasHalf);
            
            if (texture != null) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        
        // Energy shields are now rendered independently by EnergyShieldHudRenderer
        // drawEnergyShieldHearts(context, player, x, y, maxHealth, absorption, blinking);
    }

    @Unique
    private Identifier getVanillaHealthTexture(boolean hardcore, boolean blinking, boolean half) {
        if (hardcore) {
            if (half) return blinking ? Identifier.of("hud/heart/hardcore_half_blinking") : Identifier.of("hud/heart/hardcore_half");
            return blinking ? Identifier.of("hud/heart/hardcore_full_blinking") : Identifier.of("hud/heart/hardcore_full");
        } else {
            if (half) return blinking ? Identifier.of("hud/heart/half_blinking") : Identifier.of("hud/heart/half");
            return blinking ? Identifier.of("hud/heart/full_blinking") : Identifier.of("hud/heart/full");
        }
    }
    
    @Unique
    private Identifier getVanillaAbsorptionTexture(boolean hardcore, boolean blinking, boolean half) {
        if (half) return blinking ? Identifier.of("hud/heart/absorbing_half_blinking") : Identifier.of("hud/heart/absorbing_half");
        return blinking ? Identifier.of("hud/heart/absorbing_full_blinking") : Identifier.of("hud/heart/absorbing_full");
    }
    
    @Unique
    private void drawEnergyShieldHearts(DrawContext context, PlayerEntity player, int x, int y, float maxHealth, int absorption, boolean blinking) {
        if (!(player instanceof EnergyShieldProvider shieldProvider)) return;
        
        float currentShield = shieldProvider.cranksdungeons$getCurrentEnergyShield();
        double maxShield = player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (maxShield <= 0) return;

        if (currentShield < 1.0f) currentShield = 0.0f;

        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int fullShieldHearts = (int) (currentShield / 2.0);
        boolean hasHalfShield = (currentShield % 2.0f) >= 1.0f;

        // Check if shield changed (damage or regeneration) and update blink timer
        long currentTime = System.currentTimeMillis();
        if (cranks$lastRenderedShield >= 0 && currentShield != cranks$lastRenderedShield) {
            cranks$lastShieldDamageTime = currentTime;
        }

        // Determine if shield should blink (within blink duration of shield change)
        long timeSinceChange = currentTime - cranks$lastShieldDamageTime;
        boolean shieldBlinking = timeSinceChange < SHIELD_BLINK_DURATION;
        
        // Make it blink twice by alternating every 250ms
        if (shieldBlinking) {
            long phase = (timeSinceChange / 250) % 2;
            shieldBlinking = phase == 0;
        }

        // Calculate how many rows the health bar takes up
        int totalHealthHearts = (int) Math.ceil((maxHealth + absorption) / 2.0f);
        int healthRows = (int) Math.ceil(totalHealthHearts / 10.0);
        
        // Calculate shield rows
        int shieldRows = (int) Math.ceil(totalShieldHearts / 10.0);
        
        if (OVERFLOWING_BARS_LOADED) {
            // With Overflowing Bars: Show only last row with row count
            drawStackedShieldRow(context, x, y, healthRows, totalShieldHearts, fullShieldHearts, 
                               hasHalfShield, shieldBlinking, shieldRows);
        } else {
            // Without Overflowing Bars: Show all rows normally
            drawAllShieldRows(context, x, y, healthRows, totalShieldHearts, fullShieldHearts, 
                            hasHalfShield, shieldBlinking);
        }
        
        // Update last rendered shield for next frame
        cranks$lastRenderedShield = currentShield;
    }

    @Unique
    private void drawStackedShieldRow(DrawContext context, int x, int y, int healthRows, 
                                     int totalShieldHearts, int fullShieldHearts, boolean hasHalfShield, 
                                     boolean shieldBlinking, int shieldRows) {
        // Show only the LAST row (stacked display like Overflowing Bars)
        int startHeartIndex = (shieldRows - 1) * 10;
        int endHeartIndex = Math.min(startHeartIndex + 10, totalShieldHearts);
        
        int rowFullHearts = fullShieldHearts - startHeartIndex;
        if (rowFullHearts < 0) rowFullHearts = 0;

        for (int i = startHeartIndex; i < endHeartIndex; i++) {
            int col = i % 10;
            int heartX = x + col * 8;
            int heartY = y - healthRows * 10 - 3;

            // Draw container
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("hud/heart/container_blinking") : 
                Identifier.of("hud/heart/container");
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);

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
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
        
        // Draw row count
        if (shieldRows > 1) {
            String countText = shieldRows + "x";
            int textX = x - 12;
            int textY = y - healthRows * 10 - 3 - 1;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, countText, textX, textY, 0xFF55AAFF);
        }
    }

    @Unique
    private void drawAllShieldRows(DrawContext context, int x, int y, int healthRows,
                                  int totalShieldHearts, int fullShieldHearts, boolean hasHalfShield,
                                  boolean shieldBlinking) {
        // Show all rows normally (no stacking, no row count)
        for (int i = 0; i < totalShieldHearts; i++) {
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            // Position above health bar
            int heartY = y - (row + healthRows) * 10 - 3;

            // Draw container
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("hud/heart/container_blinking") : 
                Identifier.of("hud/heart/container");
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);

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
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
            }
        }
    }

    @Unique
    private Identifier getContainerTexture(boolean hardcore, boolean blinking) {
        // Use vanilla container textures - white ones when blinking
        if (hardcore) {
            if (blinking) {
                return Identifier.of("hud/heart/container_hardcore_blinking");
            } else {
                return Identifier.of("hud/heart/container_hardcore");
            }
        } else {
            if (blinking) {
                return Identifier.of("hud/heart/container_blinking");
            } else {
                return Identifier.of("hud/heart/container");
            }
        }
    }

    @Unique
    private Identifier getTexture(AilmentType ailment, boolean hardcore, boolean blinking, boolean half) {
        if (ailment == AilmentType.POISON) {
            if (hardcore) {
                if (half) return blinking ? POISON_HARDCORE_HALF_BLINKING : POISON_HARDCORE_HALF;
                return blinking ? POISON_HARDCORE_FULL_BLINKING : POISON_HARDCORE_FULL;
            } else {
                if (half) return blinking ? POISON_HALF_BLINKING : POISON_HALF;
                return blinking ? POISON_FULL_BLINKING : POISON_FULL;
            }
        } else if (ailment == AilmentType.BURN) {
            if (hardcore) {
                if (half) return blinking ? BURN_HARDCORE_HALF_BLINKING : BURN_HARDCORE_HALF;
                return blinking ? BURN_HARDCORE_FULL_BLINKING : BURN_HARDCORE_FULL;
            } else {
                if (half) return blinking ? BURN_HALF_BLINKING : BURN_HALF;
                return blinking ? BURN_FULL_BLINKING : BURN_FULL;
            }
        } else if (ailment == AilmentType.BLEED) {
            if (hardcore) {
                if (half) return blinking ? BLEED_HARDCORE_HALF_BLINKING : BLEED_HARDCORE_HALF;
                return blinking ? BLEED_HARDCORE_FULL_BLINKING : BLEED_HARDCORE_FULL;
            } else {
                if (half) return blinking ? BLEED_HALF_BLINKING : BLEED_HALF;
                return blinking ? BLEED_FULL_BLINKING : BLEED_FULL;
            }
        } else if (ailment == AilmentType.FREEZE) {
            if (hardcore) {
                if (half) return blinking ? FREEZE_HARDCORE_HALF_BLINKING : FREEZE_HARDCORE_HALF;
                return blinking ? FREEZE_HARDCORE_FULL_BLINKING : FREEZE_HARDCORE_FULL;
            } else {
                if (half) return blinking ? FREEZE_HALF_BLINKING : FREEZE_HALF;
                return blinking ? FREEZE_FULL_BLINKING : FREEZE_FULL;
            }
        }
        return null;
    }
}
