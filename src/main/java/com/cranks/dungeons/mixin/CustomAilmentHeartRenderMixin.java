package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.registry.ModAttributes;
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
    private static float cranks$lastRenderedShield = -1;
    @Unique
    private static long cranks$lastShieldDamageTime = 0;
    @Unique
    private static final long SHIELD_BLINK_DURATION = 1000;

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void drawCustomHearts(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
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
        
        // If no ailment, let vanilla handle it normally
        if (ailment == null) {
            cranks$lastRenderedHealth = -1; // Reset
            return;
        }
        
        // Cancel vanilla rendering - we'll draw everything ourselves
        ci.cancel();
        
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
        
        boolean hardcore = player.getEntityWorld().getLevelProperties().isHardcore();
        
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
        
        // Draw Energy Shield hearts after ailment hearts
        drawEnergyShieldHearts(context, player, x, y, maxHealth, absorption, blinking);
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

        for (int i = 0; i < totalShieldHearts; i++) {
            // Render shields on a new row ABOVE the health bar
            int row = i / 10;
            int col = i % 10;
            int heartX = x + col * 8;
            // Position above health bar: subtract (row + healthRows) * 10
            int heartY = y - (row + healthRows) * 10;

            // Draw vanilla container first (background)
            Identifier containerTexture = shieldBlinking ? 
                Identifier.of("hud/heart/container_blinking") : 
                Identifier.of("hud/heart/container");
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, containerTexture, heartX, heartY, 9, 9);

            // Draw filled heart on top
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
        
        // Update last rendered shield for next frame
        cranks$lastRenderedShield = currentShield;
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
