package com.cranks.dungeons.client.compendium;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ShapeRenderer {

    public static void drawShape(DrawContext context, double cx, double cy, double size, String shape, int color) {
        if (shape == null) shape = "square";

        switch (shape.toLowerCase()) {
            case "circle" -> drawPolygon(context, cx, cy, size / 2.0, 16, color, 0);
            case "hexagon" -> drawPolygon(context, cx, cy, size / 2.0, 6, color, Math.PI / 2);
            case "diamond" -> drawPolygon(context, cx, cy, size / 2.0, 4, color, 0);
            default -> drawPolygon(context, cx, cy, size / 2.0, 4, color, Math.PI / 4); // Square aligned
        }
    }

    private static void drawPolygon(DrawContext context, double cx, double cy, double radius, int sides, int color, double angleOffset) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        for (int i = 0; i < sides; i++) {
            double angle1 = angleOffset + (2 * Math.PI * i / sides);
            double angle2 = angleOffset + (2 * Math.PI * (i + 1) / sides);

            float x1 = (float) (cx + radius * Math.cos(angle1));
            float y1 = (float) (cy + radius * Math.sin(angle1));
            float x2 = (float) (cx + radius * Math.cos(angle2));
            float y2 = (float) (cy + radius * Math.sin(angle2));

            // Center
            buffer.vertex(matrix, (float) cx, (float) cy, 0).color(r, g, b, a);
            // Point 2 (Counter-Clockwise winding order to prevent backface culling)
            buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
            // Point 1
            buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        }

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, float width, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float angle = (float) Math.atan2(dy, dx);
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len <= 0) return;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x1, y1, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(angle));

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float hw = Math.max(0.5f, width / 2.0f - 0.5f);
        float hww = hw + 1.0f; // 1px fade edge

        // Core line (solid)
        buffer.vertex(matrix, 0, -hw, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, hw, 0).color(r, g, b, a);
        buffer.vertex(matrix, len, hw, 0).color(r, g, b, a);
        buffer.vertex(matrix, len, -hw, 0).color(r, g, b, a);

        // Top soft edge
        buffer.vertex(matrix, 0, hw, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, hww, 0).color(r, g, b, 0);
        buffer.vertex(matrix, len, hww, 0).color(r, g, b, 0);
        buffer.vertex(matrix, len, hw, 0).color(r, g, b, a);

        // Bottom soft edge
        buffer.vertex(matrix, 0, -hww, 0).color(r, g, b, 0);
        buffer.vertex(matrix, 0, -hw, 0).color(r, g, b, a);
        buffer.vertex(matrix, len, -hw, 0).color(r, g, b, a);
        buffer.vertex(matrix, len, -hww, 0).color(r, g, b, 0);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
    }
}
