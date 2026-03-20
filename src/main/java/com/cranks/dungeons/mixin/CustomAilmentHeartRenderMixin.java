package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
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

    @Inject(method = "renderHealthBar", at = @At("TAIL"))
    private void drawCustomHearts(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        // Check if player has custom ailment
        AilmentType ailment = null;
        if (AilmentManager.hasAilment(player, AilmentType.POISON)) {
            ailment = AilmentType.POISON;
        } else if (AilmentManager.hasAilment(player, AilmentType.BURN)) {
            ailment = AilmentType.BURN;
        }
        
        if (ailment == null) return;
        
        // Get player's actual health
        int actualHealth = (int) player.getHealth();
        boolean hardcore = player.getEntityWorld().getLevelProperties().isHardcore();
        
        // Calculate how many hearts to draw and their types
        int fullHearts = actualHealth / 2;
        boolean hasHalf = (actualHealth % 2) == 1;
        int totalHeartPositions = fullHearts + (hasHalf ? 1 : 0);
        
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
                context.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, texture, heartX, heartY, 9, 9);
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
        } else {
            if (hardcore) {
                if (half) return blinking ? BURN_HARDCORE_HALF_BLINKING : BURN_HARDCORE_HALF;
                return blinking ? BURN_HARDCORE_FULL_BLINKING : BURN_HARDCORE_FULL;
            } else {
                if (half) return blinking ? BURN_HALF_BLINKING : BURN_HALF;
                return blinking ? BURN_FULL_BLINKING : BURN_FULL;
            }
        }
    }
}
