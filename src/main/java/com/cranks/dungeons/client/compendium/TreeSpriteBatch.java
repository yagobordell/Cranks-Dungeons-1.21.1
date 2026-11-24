package com.cranks.dungeons.client.compendium;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.GuiAtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/** Visible tree sprites share the GUI atlas, including the node art and connections. */
final class TreeSpriteBatch implements AutoCloseable {
    private static final Identifier GENERIC = Identifier.of("cranks-dungeons", "passive/generic");
    private static final Identifier DOT = Identifier.of("cranks-dungeons", "compendium/tree_marker");
    private final GuiAtlasManager atlas = MinecraftClient.getInstance().getGuiAtlasManager();
    private final double pixelScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
    private final Map<Identifier, Sprite> sprites = new HashMap<>();
    private final Matrix4f matrix;
    private final BufferBuilder vertices;

    TreeSpriteBatch(DrawContext context) {
        matrix = context.getMatrices().peek().getPositionMatrix();
        vertices = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    static double nodeSize(String shape, double baseSize) {
        double multiplier = switch (shape == null ? "" : shape) {
            case "hexagon" -> 3.2;
            case "diamond" -> 2.2;
            case "circle" -> 1.4;
            default -> 1.0;
        };
        double overviewSize = 3.0 + (multiplier - 1.0) * 0.75;
        return Math.max(overviewSize, baseSize * multiplier);
    }

    void node(Identifier texture, double centerX, double centerY, double size) {
        node(texture, centerX, centerY, size, -1);
    }

    void node(Identifier texture, double centerX, double centerY, double size, int color) {
        int pixels = physicalPixels(size);
        // Choose native art that fits the framebuffer so the one-pixel rim stays continuous.
        FrameResolution resolution = pixels >= 16 ? FrameResolution.FULL
                : pixels >= 12 ? FrameResolution.MEDIUM
                : pixels >= 8 ? FrameResolution.SMALL
                : FrameResolution.COMPACT;
        Identifier sprite = resolution.texture(texture);
        centeredSprite(sprite, centerX, centerY, pixels, color);
    }

    private enum FrameResolution {
        COMPACT("_compact"), SMALL("_small"), MEDIUM("_medium"), FULL("");

        private final String suffix;
        private final Map<Identifier, Identifier> textures = new HashMap<>();

        FrameResolution(String suffix) {
            this.suffix = suffix;
        }

        Identifier texture(Identifier base) {
            if (this == FULL) return base;
            return textures.computeIfAbsent(base, id -> id.withPath(path -> path + suffix));
        }
    }

    void icon(Identifier texture, double centerX, double centerY, double nodeSize) {
        if (texture == null) return;
        switch (texture.getPath()) {
            case "passive/circle", "passive/square", "passive/diamond", "passive/hexagon" -> { return; }
        }
        if (GENERIC.equals(texture)) {
            centeredSprite(DOT, centerX, centerY, Math.max(3, physicalPixels(nodeSize * 0.7)), -1);
        } else {
            centeredSprite(texture, centerX, centerY, Math.max(6, physicalPixels(nodeSize * 0.9)), -1);
        }
    }

    private int physicalPixels(double size) {
        return Math.max(2, (int) Math.round(Math.min(size, 64) * pixelScale));
    }

    private void centeredSprite(Identifier texture, double cx, double cy, int pixels, int color) {
        float left = (float) (Math.round(cx * pixelScale - pixels / 2.0) / pixelScale);
        float top = (float) (Math.round(cy * pixelScale - pixels / 2.0) / pixelScale);
        float size = (float) (pixels / pixelScale);
        Sprite sprite = sprites.computeIfAbsent(texture, atlas::getSprite);
        quad(left, top, left, top + size, left + size, top + size, left + size, top,
                sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), color);
    }

    void connection(Identifier texture, float x1, float y1, float x2, float y2,
                    double zoom, float startInset, float endInset, boolean highlight) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.hypot(dx, dy);
        float visibleLength = length - startInset - endInset;
        if (visibleLength <= 0) return;
        float ux = dx / length;
        float uy = dy / length;
        x1 += startInset * ux;
        y1 += startInset * uy;
        Sprite sprite = sprites.computeIfAbsent(texture, atlas::getSprite);
        if (zoom < 0.6) {
            float row = highlight ? 0 : 1;
            float coreWidth = (float) Math.max(0.5, 1.0 / pixelScale);
            boolean showKeyline = pixelScale >= 3 && !highlight;
            float halfWidth = coreWidth * (showKeyline ? 1.5f : 0.5f);
            float top = showKeyline ? sprite.getMinV() : sprite.getFrameV(row / 3);
            float bottom = showKeyline ? sprite.getMaxV() : sprite.getFrameV((row + 1) / 3);
            line(x1, y1, x1 + ux * visibleLength, y1 + uy * visibleLength, -uy * halfWidth, ux * halfWidth,
                    sprite.getMinU(), sprite.getFrameU(1 / 8f), top, bottom);
            return;
        }

        float scale = (float) Math.sqrt(zoom);
        float tileLength = 8 * scale;
        for (float offset = 0; offset < visibleLength; offset += tileLength) {
            float end = Math.min(visibleLength, offset + tileLength);
            line(x1 + ux * offset, y1 + uy * offset, x1 + ux * end, y1 + uy * end,
                    -uy * 1.5f * scale, ux * 1.5f * scale, sprite.getMinU(),
                    sprite.getFrameU((end - offset) / tileLength), sprite.getMinV(), sprite.getMaxV());
        }
    }

    private void line(float x1, float y1, float x2, float y2, float nx, float ny,
                      float u1, float u2, float v1, float v2) {
        quad(x1 - nx, y1 - ny, x1 + nx, y1 + ny, x2 + nx, y2 + ny, x2 - nx, y2 - ny, u1, u2, v1, v2, -1);
    }

    private void quad(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy,
                      float u1, float u2, float v1, float v2, int color) {
        vertices.vertex(matrix, ax, ay, 0).texture(u1, v1).color(color);
        vertices.vertex(matrix, bx, by, 0).texture(u1, v2).color(color);
        vertices.vertex(matrix, cx, cy, 0).texture(u2, v2).color(color);
        vertices.vertex(matrix, dx, dy, 0).texture(u2, v1).color(color);
    }

    @Override
    public void close() {
        var built = vertices.endNullable();
        if (built == null) return;
        Identifier texture = atlas.getSprite(GENERIC).getAtlasId();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            BufferRenderer.drawWithGlobalProgram(built);
        } finally {
            RenderSystem.disableBlend();
        }
    }
}
