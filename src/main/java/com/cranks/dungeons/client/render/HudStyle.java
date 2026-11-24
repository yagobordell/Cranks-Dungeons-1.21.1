package com.cranks.dungeons.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/** Pixel trim shared by gameplay meters, nameplates and effect tiles. */
public final class HudStyle {
    private static final int KEYLINE = 0xFF000000;
    private static final int WELL = 0xFF141414;
    private static final int SILVER = 0xFF929292;
    private static final int DIM_SILVER = 0xFF555555;

    public enum Ink {
        HEALTH(0xFF913340, 0xFFBD5462, 0xFF61242F),
        BLEED(0xFF7F2B31, 0xFFBA4B4A, 0xFF542028),
        POISON(0xFF536B30, 0xFF819950, 0xFF394925),
        BURN(0xFFA4542E, 0xFFD77C46, 0xFF64341F),
        ABSORPTION(0xFFC89142, 0xFFE6BB69, 0xFF77512A),
        AIR(0xBC39758B, 0xDE72B9CF, 0xBC244957),
        SHIELD(0xFF748696, 0xFFAFC3CF, 0xFF495661),
        MANA(0xFF405E91, 0xFF688DBE, 0xFF2E415D),
        FOOD(0xFF87612E, 0xFFBB8C48, 0xFF594125),
        FOOD_HIGH(0xFFA67A39, 0xFFD5AA64, 0xFF6E4A23),
        FOOD_LOW(0xFF8C4930, 0xFFBA6B44, 0xFF5E3022),
        XP(0xFF698447, 0xFF9FB976, 0xFF415430),
        STAMINA(0xFF41766B, 0xFF71A99A, 0xFF2D5148),
        STAMINA_ACTIVE(0xFF598B7C, 0xFF8CC2B2, 0xFF3F6155),
        STAMINA_EXHAUSTED(0xFF72503D, 0xFFB87C5C, 0xFF53372B),
        BOSS_PURPLE(0xFF77558E, 0xFFA481BD, 0xFF503762),
        BOSS_PINK(0xFF945476, 0xFFBF83A4, 0xFF64344D);

        private final int fill;
        private final int highlight;
        private final int trail;

        Ink(int fill, int highlight, int trail) {
            this.fill = fill;
            this.highlight = highlight;
            this.trail = trail;
        }
    }

    private HudStyle() {}

    public static void meter(DrawContext context, int x, int y, int width, int height,
                             Ink ink, float progress) {
        meter(context, x, y, width, height, ink, progress, progress);
    }

    public static void meter(DrawContext context, int x, int y, int width, int height,
                             Ink ink, float progress, float trail) {
        if (width < 6 || height < 4) return;
        frame(context, x, y, width, height, SILVER);
        int fillWidth = fillWidth(width, progress);
        int trailWidth = fillWidth(width, trail);
        int bottom = fillBottom(y, height);
        if (trailWidth > fillWidth) {
            context.fill(x + 2 + fillWidth, y + 2, x + 2 + trailWidth, bottom, ink.trail);
        }
        fill(context, x, y, fillWidth, bottom, ink);
    }

    public static void overlayMeter(DrawContext context, int x, int y, int width, int height,
                                    Ink ink, float progress) {
        fill(context, x, y, fillWidth(width, progress), fillBottom(y, height), ink);
    }

    private static int fillWidth(int width, float progress) {
        return Math.round(MathHelper.clamp(progress, 0, 1) * Math.max(0, width - 4));
    }

    private static int fillBottom(int y, int height) {
        return y + height - (height == 4 ? 1 : 2);
    }

    private static void fill(DrawContext context, int x, int y, int width, int bottom, Ink ink) {
        if (width <= 0) return;
        int right = x + 2 + width;
        context.fill(x + 2, y + 2, right, y + 3, ink.highlight);
        if (bottom > y + 3) {
            context.fill(x + 2, y + 3, right, bottom, ink.fill);
        }
    }

    public static void plate(DrawContext context, int x, int y, int width, int height, boolean muted) {
        if (width < 6 || height < 4) return;
        frame(context, x, y, width, height, muted ? DIM_SILVER : SILVER);
    }

    private static void frame(DrawContext context, int x, int y, int width, int height, int edge) {
        int right = x + width;
        int bottom = y + height;
        context.fill(x, y, right, bottom, KEYLINE);
        context.fill(x + 1, y + 1, right - 1, bottom - 1, DIM_SILVER);
        context.fill(x + 1, y + 1, right - 1, y + 2, edge);
        context.fill(x + 2, y + 2, right - 2, fillBottom(y, height), WELL);
    }

    public static void divider(DrawContext context, int left, int right, int y) {
        context.fill(left, y, right, y + 1, 0xFF333333);
    }

    public static void meterLabel(DrawContext context, TextRenderer font, String text, int x, int y,
                                  int width, int height, int color) {
        float scale = Math.min(height >= 9 ? 0.85f : 0.72f,
                (width - 8f) / Math.max(1, font.getWidth(text)));
        drawLabel(context, font, text, x + width / 2f, y + height / 2f, scale, color);
    }

    public static void badge(DrawContext context, TextRenderer font, String text, float centerX, float centerY,
                             float scale, int color) {
        int width = Math.max(8, MathHelper.ceil(font.getWidth(text) * scale) + 8);
        int height = Math.max(7, MathHelper.ceil(font.fontHeight * scale) + 1);
        plate(context, Math.round(centerX - width / 2f), Math.round(centerY - height / 2f), width, height, false);
        drawLabel(context, font, text, centerX, centerY, scale, color);
    }

    public static void drawLabel(DrawContext context, TextRenderer font, String text, float centerX, float centerY,
                                 float scale, int color) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX - font.getWidth(text) * scale / 2,
                centerY - font.fontHeight * scale / 2, 0);
        matrices.scale(scale, scale, 1);
        context.drawTextWithShadow(font, text, 0, 0, color);
        matrices.pop();
    }

}
