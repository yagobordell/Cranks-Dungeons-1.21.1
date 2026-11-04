package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.network.HorseGeneticsSyncPacket;
import com.cranks.dungeons.client.render.StatBarHudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.LivingEntity;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Shows the server-authoritative stamina value while the player controls a horse. */
public final class HorseStaminaHudRenderer implements HudRenderCallback {
    public static final int WIDTH = StatBarHudRenderer.BAR_WIDTH;
    public static final int HEIGHT = StatBarHudRenderer.SHIELD_HEIGHT;
    private static final Identifier VANILLA_BACKGROUND = Identifier.ofVanilla("hud/experience_bar_background");
    private static final Identifier VANILLA_PROGRESS = Identifier.ofVanilla("hud/experience_bar_progress");
    private static Identifier backgroundTexture = VANILLA_BACKGROUND;
    private static Identifier progressTexture = VANILLA_PROGRESS;
    private static boolean texturesResolved;

    private HorseStaminaHudRenderer() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new HorseStaminaHudRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden
                || !(client.player.getVehicle() instanceof LivingEntity mount)) {
            return;
        }
        double current;
        double maximum;
        boolean active;
        boolean exhausted;
        String label;
        if (mount instanceof HorseEntity horse) {
            HorseGeneticsSyncPacket data = HorseGeneticsClientData.get(horse.getId());
            if (data == null || data.maxStamina() <= 0.0) return;
            current = data.stamina();
            maximum = data.maxStamina();
            active = data.galloping();
            exhausted = data.exhausted();
            label = active ? "Gallop" : "Horse";
        } else {
            var data = MountClientData.get(mount.getId());
            if (data == null || data.maximumResource() <= 0.0) return;
            current = data.resource();
            maximum = data.maximumResource();
            active = data.abilityActive();
            exhausted = current <= 0.001;
            label = switch (MountKind.of(mount)) {
                case DONKEY, MULE -> active ? "Pack Trot" : "Endurance";
                case CAMEL -> {
                    boolean dashing = mount instanceof CamelEntity camel && camel.isDashing();
                    active |= dashing;
                    yield dashing ? "Dash" : active ? "Canter" : "Endurance";
                }
                case STRIDER -> active ? "Lava Rush" : "Heat";
                case LLAMA -> "Pack Speed";
                default -> "Mount";
            };
        }
        resolveTextures();

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int defaultX = screenWidth / 2 - WIDTH / 2;
        int defaultY = screenHeight - 59;
        int x = CranksDungeons.CONFIG.horseStaminaBarX() >= 0
                ? CranksDungeons.CONFIG.horseStaminaBarX() : defaultX;
        int y = CranksDungeons.CONFIG.horseStaminaBarY() >= 0
                ? CranksDungeons.CONFIG.horseStaminaBarY() : defaultY;
        renderBarDirect(context, client, x, y, current, maximum, active, exhausted, label);
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, int x, int y) {
        renderBarDirect(context, client, x, y, 75.0, 100.0, false, false, "Horse");
    }

    public static void renderBarDirect(DrawContext context, MinecraftClient client, int x, int y,
                                       double stamina, double maxStamina) {
        renderBarDirect(context, client, x, y, stamina, maxStamina, false, false, "Horse");
    }

    private static void renderBarDirect(DrawContext context, MinecraftClient client, int x, int y,
                                        double stamina, double maxStamina,
                                        boolean galloping, boolean exhausted) {
        renderBarDirect(context, client, x, y, stamina, maxStamina, galloping, exhausted, "Horse");
    }

    private static void renderBarDirect(DrawContext context, MinecraftClient client, int x, int y,
                                        double stamina, double maxStamina,
                                        boolean galloping, boolean exhausted, String label) {
        if (maxStamina <= 0.0) return;
        resolveTextures();
        double ratio = MathHelper.clamp(stamina / maxStamina, 0.0, 1.0);
        int fillWidth = (int) Math.round(WIDTH * ratio);

        context.drawGuiTexture(backgroundTexture, x, y, WIDTH, HEIGHT);
        if (fillWidth > 0) {
            context.drawGuiTexture(progressTexture, WIDTH, HEIGHT, 0, 0,
                    x, y, fillWidth, HEIGHT);
        }

        // Keep this bar compact: the bar itself already identifies the
        // resource, so only the current/max value is shown.
        String text = Math.round(stamina) + "/" + Math.round(maxStamina);
        int textWidth = client.textRenderer.getWidth(text);
        context.getMatrices().push();
        context.getMatrices().translate(x + WIDTH / 2.0f, y + HEIGHT / 2.0f, 0.0f);
        context.getMatrices().scale(0.8f, 0.8f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, text,
                -textWidth / 2, -client.textRenderer.fontHeight / 2, 0xFFFFFFFF);
        context.getMatrices().pop();
    }

    private static void resolveTextures() {
        if (texturesResolved) return;
        texturesResolved = true;
        backgroundTexture = GuiTextureResolver.customOrVanilla(
                "hud/horse_stamina_background", VANILLA_BACKGROUND);
        progressTexture = GuiTextureResolver.customOrVanilla(
                "hud/horse_stamina_progress", VANILLA_PROGRESS);
    }
}
