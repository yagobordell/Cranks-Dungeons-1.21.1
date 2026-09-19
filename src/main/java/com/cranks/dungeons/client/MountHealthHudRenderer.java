package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.render.StatBarHudRenderer;
import com.cranks.dungeons.mount.MountKind;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Renders the mounted animal's health above its stamina/resource bar. */
public final class MountHealthHudRenderer implements HudRenderCallback {
    public static final int WIDTH = HorseStaminaHudRenderer.WIDTH;
    public static final int HEIGHT = HorseStaminaHudRenderer.HEIGHT;
    private static final int MAX_FILL_WIDTH = WIDTH;
    private static final long TRAIL_DELAY_MS = 600;
    private static final float TRAIL_SHRINK_SPEED = 15.0f;
    private static final float TEXT_SCALE = 0.9f;

    private static final Identifier VANILLA_BACKGROUND =
            Identifier.ofVanilla("hud/experience_bar_background");
    private static final Identifier VANILLA_FILL =
            Identifier.ofVanilla("hud/experience_bar_progress");

    private static Identifier backgroundTexture = VANILLA_BACKGROUND;
    private static Identifier fillTexture = VANILLA_FILL;
    private static Identifier trailTexture = VANILLA_FILL;
    private static boolean texturesResolved;

    private float displayedHealth = -1.0f;
    private long lastHealthChangeTime;
    private long lastRenderTime;
    private int trackedMountId = Integer.MIN_VALUE;

    private MountHealthHudRenderer() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new MountHealthHudRenderer());
    }

    /**
     * Mounted stat bars occupy the vanilla action-bar's lower-center area.  The
     * action bar is also where progression notifications (including horse
     * training level-ups) are rendered, so reserve a small strip above the bars
     * while they are visible.
     */
    public static boolean shouldRaiseOverlayMessage(MinecraftClient client) {
        if (!CranksDungeons.CONFIG.useStatBars() || client == null || client.player == null) {
            return false;
        }
        if (client.options.hudHidden) return false;
        if (!(client.player.getVehicle() instanceof LivingEntity mount)
                || MountKind.of(mount) == MountKind.UNSUPPORTED) {
            return false;
        }
        return mount.isAlive();
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!CranksDungeons.CONFIG.useStatBars()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden
                || !(client.player.getVehicle() instanceof LivingEntity mount)
                || MountKind.of(mount) == MountKind.UNSUPPORTED
                || !mount.isAlive()) {
            resetTracking();
            return;
        }

        float health = mount.getHealth();
        float maxHealth = Math.max(1.0f, mount.getMaxHealth());
        updateTrail(mount.getId(), health);
        resolveTextures();

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int staminaDefaultX = screenWidth / 2 - HorseStaminaHudRenderer.WIDTH / 2;
        int staminaDefaultY = screenHeight - 59;
        int staminaX = CranksDungeons.CONFIG.horseStaminaBarX() >= 0
                ? CranksDungeons.CONFIG.horseStaminaBarX() : staminaDefaultX;
        int staminaY = CranksDungeons.CONFIG.horseStaminaBarY() >= 0
                ? CranksDungeons.CONFIG.horseStaminaBarY() : staminaDefaultY;
        int defaultX = staminaX;
        int defaultY = staminaY - HEIGHT - 1;
        int x = CranksDungeons.CONFIG.mountHealthBarX() >= 0
                ? CranksDungeons.CONFIG.mountHealthBarX() : defaultX;
        int y = CranksDungeons.CONFIG.mountHealthBarY() >= 0
                ? CranksDungeons.CONFIG.mountHealthBarY() : defaultY;

        renderBarDirect(context, client, x, y, health, maxHealth, displayedHealth);
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y) {
        renderBarDirect(context, client, x, y, 75.0f, 100.0f, 90.0f);
    }

    public static void renderBarDirect(DrawContext context, MinecraftClient client,
                                       int x, int y, float health, float maxHealth,
                                       float displayedHealth) {
        if (maxHealth <= 0.0f) return;
        resolveTextures();
        context.drawGuiTexture(backgroundTexture, x, y, WIDTH, HEIGHT);

        int healthWidth = (int) (MathHelper.clamp(health / maxHealth, 0.0f, 1.0f)
                * MAX_FILL_WIDTH);
        if (displayedHealth > health) {
            int trailWidth = (int) (MathHelper.clamp(displayedHealth / maxHealth, 0.0f, 1.0f)
                    * MAX_FILL_WIDTH);
            if (trailWidth > healthWidth) {
                context.drawGuiTexture(trailTexture, MAX_FILL_WIDTH, HEIGHT,
                        healthWidth, 0, x + healthWidth, y,
                        trailWidth - healthWidth, HEIGHT);
            }
        }
        if (healthWidth > 0) {
            context.drawGuiTexture(fillTexture, MAX_FILL_WIDTH, HEIGHT,
                    0, 0, x, y, healthWidth, HEIGHT);
        }

        String text = Math.round(health) + "/" + Math.round(maxHealth);
        TextRenderer renderer = client.textRenderer;
        int textWidth = renderer.getWidth(text);
        context.getMatrices().push();
        context.getMatrices().translate(x + WIDTH / 2.0f, y + HEIGHT / 2.0f, 0.0f);
        context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        context.drawTextWithShadow(renderer, text, -(textWidth / 2),
                -(renderer.fontHeight / 2), 0xFFFFFFFF);
        context.getMatrices().pop();
    }

    private void updateTrail(int mountId, float health) {
        long now = net.minecraft.util.Util.getMeasuringTimeMs();
        if (mountId != trackedMountId) {
            trackedMountId = mountId;
            displayedHealth = health;
            lastHealthChangeTime = now;
            lastRenderTime = now;
            return;
        }

        if (lastRenderTime == 0) lastRenderTime = now;
        float deltaSeconds = Math.min(0.5f, Math.max(0.0f, (now - lastRenderTime) / 1000.0f));
        lastRenderTime = now;
        if (displayedHealth < 0.0f || health >= displayedHealth) {
            displayedHealth = health;
            lastHealthChangeTime = now;
        } else if (now - lastHealthChangeTime > TRAIL_DELAY_MS) {
            displayedHealth -= TRAIL_SHRINK_SPEED * deltaSeconds;
            if (displayedHealth < health) displayedHealth = health;
        }
    }

    private void resetTracking() {
        trackedMountId = Integer.MIN_VALUE;
        displayedHealth = -1.0f;
        lastRenderTime = 0;
    }

    private static void resolveTextures() {
        if (texturesResolved) return;
        texturesResolved = true;
        backgroundTexture = GuiTextureResolver.customOrVanilla(
                "hud/empty_mount_health", VANILLA_BACKGROUND);
        fillTexture = GuiTextureResolver.customOrVanilla(
                "hud/mount_health", VANILLA_FILL);
        trailTexture = GuiTextureResolver.customOrVanilla(
                "hud/mount_health_trail", fillTexture);
    }
}
