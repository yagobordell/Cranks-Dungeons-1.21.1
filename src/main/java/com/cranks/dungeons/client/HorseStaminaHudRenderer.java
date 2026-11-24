package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.network.HorseGeneticsSyncPacket;
import com.cranks.dungeons.client.render.StatBarHudRenderer;
import com.cranks.dungeons.client.render.HotbarHudRenderer;
import com.cranks.dungeons.client.render.HudStyle;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.LivingEntity;

/** Shows the server-authoritative stamina value while the player controls a horse. */
public final class HorseStaminaHudRenderer implements HudRenderCallback {
    public static final int WIDTH = StatBarHudRenderer.BAR_WIDTH;
    public static final int HEIGHT = StatBarHudRenderer.SHIELD_HEIGHT;

    private HorseStaminaHudRenderer() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new HorseStaminaHudRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen) return;
        if (client.player == null || client.options.hudHidden
                || !(client.player.getVehicle() instanceof LivingEntity mount)) {
            return;
        }
        double current;
        double maximum;
        boolean active;
        boolean exhausted;
        if (mount instanceof HorseEntity horse) {
            HorseGeneticsSyncPacket data = HorseGeneticsClientData.get(horse.getId());
            if (data == null || data.maxStamina() <= 0.0) return;
            current = data.stamina();
            maximum = data.maxStamina();
            active = data.galloping();
            exhausted = data.exhausted();
        } else {
            var data = MountClientData.get(mount.getId());
            if (data == null || data.maximumResource() <= 0.0) return;
            current = data.resource();
            maximum = data.maximumResource();
            active = data.abilityActive() || mount instanceof CamelEntity camel && camel.isDashing();
            exhausted = current <= 0.001;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int defaultX = screenWidth / 2 - WIDTH / 2;
        int defaultY = defaultY(screenHeight);
        int x = CranksDungeons.CONFIG.horseStaminaBarX() >= 0
                ? CranksDungeons.CONFIG.horseStaminaBarX() : defaultX;
        int y = CranksDungeons.CONFIG.horseStaminaBarY() >= 0
                ? CranksDungeons.CONFIG.horseStaminaBarY() : defaultY;
        renderBar(context, client, x, y, current, maximum, active, exhausted);
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y) {
        renderBar(context, client, x, y, 75, 100, false, false);
    }

    public static int defaultY(int screenHeight) {
        return screenHeight - 59 - HotbarHudRenderer.DEFAULT_LIFT;
    }

    private static void renderBar(DrawContext context, MinecraftClient client, int x, int y,
                                  double stamina, double maximum, boolean active, boolean exhausted) {
        HudStyle.Ink ink = exhausted ? HudStyle.Ink.STAMINA_EXHAUSTED
                : active ? HudStyle.Ink.STAMINA_ACTIVE : HudStyle.Ink.STAMINA;
        HudStyle.meter(context, x, y, WIDTH, HEIGHT, ink, (float) (stamina / maximum));
        HudStyle.meterLabel(context, client.textRenderer, Math.round(stamina) + "/" + Math.round(maximum),
                x, y, WIDTH, HEIGHT, exhausted ? 0xFFF1BD9F : 0xFFE7F3EF);
    }
}
