package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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

    @Unique
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    @Unique
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    @Unique
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    @Unique
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");

    @Shadow @Final private MinecraftClient client;
    @Unique private static int shieldRowCount = 0;
    @Unique private static float cranks$lastRenderedShield = -1;
    @Unique private static long cranks$lastShieldDamageTime = 0;
    @Unique private static final long SHIELD_BLINK_DURATION = 1000; // Blink for 1000ms (1 second) to allow 2 blinks

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
        int absorptionHearts = (int) Math.ceil(absorption / 2.0f);
        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int totalHearts = maxHealthHearts + absorptionHearts + totalShieldHearts;
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
        int absorptionHearts = (int) Math.ceil(absorption / 2.0f);

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
            shieldBlinking = phase == 0; // Show blinking texture in phases 0 and 2
        }

        for (int i = 0; i < totalShieldHearts; i++) {
            // Shield hearts start after health hearts AND absorption hearts
            int heartIndex = maxHealthHearts + absorptionHearts + i;
            int row = heartIndex / 10;
            int col = heartIndex % 10;
            int heartX = x + col * 8;
            int heartY = y - row * 10;

            // Draw vanilla container first (background) - use blinking container when shield is damaged
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
        
        // Update last rendered shield for next frame comparison
        cranks$lastRenderedShield = currentShield;
    }
}
