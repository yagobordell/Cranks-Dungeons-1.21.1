package com.cranks.dungeons.client;

import com.cranks.dungeons.client.render.HotbarHudRenderer;
import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.CranksDungeonsClient;
import com.cranks.dungeons.ManaProvider;
import com.cranks.dungeons.registry.ModAttributes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ManaHudRenderer implements HudRenderCallback {

    public static final Identifier MANA_EMPTY = Identifier.of(CranksDungeons.MOD_ID, "hud/mana/empty");
    public static final Identifier MANA_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/mana/full");
    public static final Identifier MANA_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/mana/full_blinking");
    public static final Identifier MANA_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/mana/half");
    public static final Identifier MANA_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/mana/half_blinking");

    private static final long MANA_BLINK_DURATION = 1000;
    private float lastRenderedMana = -1;
    private long lastManaDamageTime = 0;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen) return;
        if (client.player == null) return;
        if (client.options.hudHidden || !client.player.isAlive() || CranksDungeonsClient.isCinematicRespawnActive()) return;
        if (CranksDungeons.CONFIG.useStatBars()) return;
        if (client.player.isCreative() || client.player.isSpectator()) return;

        PlayerEntity player = client.player;
        if (!(player instanceof ManaProvider manaProvider)) return;

        float maxMana = (float) player.getAttributeValue(ModAttributes.MAX_MANA);
        if (maxMana <= 0) return;

        float currentMana = manaProvider.cranksdungeons$getCurrentMana();
        currentMana = MathHelper.clamp(currentMana, 0.0f, maxMana);

        // Blinking logic when mana is spent
        long currentTime = System.currentTimeMillis();
        if (lastRenderedMana >= 0 && currentMana < lastRenderedMana - 0.05f) {
            lastManaDamageTime = currentTime;
        }
        lastRenderedMana = currentMana;

        long timeSinceChange = currentTime - lastManaDamageTime;
        boolean manaBlinking = timeSinceChange < MANA_BLINK_DURATION;
        if (manaBlinking) {
            long phase = (timeSinceChange / 250) % 2;
            manaBlinking = (phase == 0);
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // 1 icon = 5 mana
        int totalIcons = (int) Math.ceil(maxMana / 5.0f);
        int totalRows = (int) Math.ceil(totalIcons / 10.0f);
        int spacing = totalRows > 2 ? Math.max(10 - (totalRows - 2), 3) : 10;

        // Position above hunger / mount health
        LivingEntity vehicle = player.getVehicle() instanceof LivingEntity living ? living : null;
        int vehicleRows = HorseMountHudLayout.visibleHeartRows(vehicle);

        int hungerBaseY = screenHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        int manaBaseY = vehicleRows > 0 ? (hungerBaseY - (vehicleRows - 1) * 10 - 10) : (hungerBaseY - 10);
        int right = screenWidth / 2 + 91;

        RenderSystem.enableBlend();

        boolean overflowingBars = com.cranks.dungeons.util.CombatUtility.isOverflowingBarsLoaded();

        if (overflowingBars) {
            drawOverflowingManaRow(drawContext, client, right, manaBaseY, maxMana, currentMana, manaBlinking);
        } else {
            drawMultiRowMana(drawContext, totalIcons, right, manaBaseY, spacing, currentMana, manaBlinking);
        }

        RenderSystem.disableBlend();
    }

    private void drawOverflowingManaRow(DrawContext drawContext, MinecraftClient client, int right, int manaBaseY,
                                        float maxMana, float currentMana, boolean manaBlinking) {
        // 50 mana = 10 icons = 1 full bar
        int currentManaLayer = Math.max(1, (int) Math.ceil(currentMana / 50.0f));
        float layerMana = currentMana - (currentManaLayer - 1) * 50.0f;
        if (currentMana <= 0.0f) {
            layerMana = 0.0f;
        }

        for (int col = 0; col < 10; col++) {
            int iconX = right - col * 8 - 9;
            int iconY = manaBaseY;

            drawContext.drawGuiTexture(MANA_EMPTY, iconX, iconY, 9, 9);

            float base = col * 5.0f;
            float manaInIcon = layerMana - base;

            Identifier fillTexture = null;
            if (manaInIcon >= 5.0f - 0.05f) {
                fillTexture = manaBlinking ? MANA_FULL_BLINKING : MANA_FULL;
            } else if (manaInIcon >= 0.05f) {
                fillTexture = manaBlinking ? MANA_HALF_BLINKING : MANA_HALF;
            }

            if (fillTexture != null) {
                drawContext.drawGuiTexture(fillTexture, iconX, iconY, 9, 9);
            }
        }

        if (currentManaLayer > 1) {
            String badge = currentManaLayer + "x";
            int textX = right + 3;
            int textY = manaBaseY + 1;
            drawBorderedText(drawContext, client.textRenderer, badge, textX, textY, 0xFFFFFFFF);
        }
    }

    private void drawMultiRowMana(DrawContext drawContext, int totalIcons, int right, int manaBaseY, int spacing,
                                  float currentMana, boolean manaBlinking) {
        for (int i = 0; i < totalIcons; i++) {
            int row = i / 10;
            int col = i % 10;
            int iconX = right - col * 8 - 9;
            int iconY = manaBaseY - row * spacing;

            if (iconY < 0) continue;

            // Draw container
            drawContext.drawGuiTexture(MANA_EMPTY, iconX, iconY, 9, 9);

            float base = i * 5.0f;
            float manaInIcon = currentMana - base;

            Identifier fillTexture = null;
            if (manaInIcon >= 5.0f - 0.05f) {
                fillTexture = manaBlinking ? MANA_FULL_BLINKING : MANA_FULL;
            } else if (manaInIcon >= 0.05f) {
                fillTexture = manaBlinking ? MANA_HALF_BLINKING : MANA_HALF;
            }

            if (fillTexture != null) {
                drawContext.drawGuiTexture(fillTexture, iconX, iconY, 9, 9);
            }
        }
    }

    public static void drawBorderedText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer,
                                        String text, int x, int y, int color) {
        int shadowColor = 0xFF000000;
        context.drawText(textRenderer, text, x - 1, y, shadowColor, false);
        context.drawText(textRenderer, text, x + 1, y, shadowColor, false);
        context.drawText(textRenderer, text, x, y - 1, shadowColor, false);
        context.drawText(textRenderer, text, x, y + 1, shadowColor, false);
        context.drawText(textRenderer, text, x, y, color, false);
    }

    public static int getManaRows(PlayerEntity player) {
        if (player == null || CranksDungeons.CONFIG.useStatBars()) return 0;
        double maxMana = player.getAttributeValue(ModAttributes.MAX_MANA);
        if (maxMana <= 0) return 0;
        if (com.cranks.dungeons.util.CombatUtility.isOverflowingBarsLoaded()) {
            return 1;
        }
        int totalIcons = (int) Math.ceil(maxMana / 5.0);
        return (int) Math.ceil(totalIcons / 10.0);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new ManaHudRenderer());
    }
}
