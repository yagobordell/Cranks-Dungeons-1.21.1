package com.cranks.dungeons.client.compendium;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ShapeRenderer {

    private static BufferBuilder activeBatchBuffer = null;
    private static Matrix4f activeBatchMatrix = null;

    public static void beginBatch(DrawContext context) {
        if (activeBatchBuffer != null) return;
        activeBatchMatrix = context.getMatrices().peek().getPositionMatrix();
        activeBatchBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
    }

    public static void endBatch() {
        if (activeBatchBuffer == null) return;
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.drawWithGlobalProgram(activeBatchBuffer.end());
        activeBatchBuffer = null;
        activeBatchMatrix = null;
    }

    public static void drawBatchedLine(float x1, float y1, float x2, float y2, float width, int color) {
        if (activeBatchBuffer == null) return;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0) return;

        float nx = -dy / len;
        float ny = dx / len;

        float hw = Math.max(0.5f, width / 2.0f - 0.5f);
        float hww = hw + 1.0f; // 1px fade edge

        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Matrix4f m = activeBatchMatrix;

        // Core quad (2 triangles)
        float c1x = x1 + nx * hw, c1y = y1 + ny * hw;
        float c2x = x2 + nx * hw, c2y = y2 + ny * hw;
        float c3x = x2 - nx * hw, c3y = y2 - ny * hw;
        float c4x = x1 - nx * hw, c4y = y1 - ny * hw;

        activeBatchBuffer.vertex(m, c1x, c1y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, c2x, c2y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, c3x, c3y, 0).color(r, g, b, a);

        activeBatchBuffer.vertex(m, c1x, c1y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, c3x, c3y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, c4x, c4y, 0).color(r, g, b, a);

        // Top soft edge quad (fade to 0 alpha)
        float f1x = x1 + nx * hww, f1y = y1 + ny * hww;
        float f2x = x2 + nx * hww, f2y = y2 + ny * hww;

        activeBatchBuffer.vertex(m, c1x, c1y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, f1x, f1y, 0).color(r, g, b, 0);
        activeBatchBuffer.vertex(m, f2x, f2y, 0).color(r, g, b, 0);

        activeBatchBuffer.vertex(m, c1x, c1y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, f2x, f2y, 0).color(r, g, b, 0);
        activeBatchBuffer.vertex(m, c2x, c2y, 0).color(r, g, b, a);

        // Bottom soft edge quad (fade to 0 alpha)
        float b1x = x1 - nx * hww, b1y = y1 - ny * hww;
        float b2x = x2 - nx * hww, b2y = y2 - ny * hww;

        activeBatchBuffer.vertex(m, c4x, c4y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, c3x, c3y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, b2x, b2y, 0).color(r, g, b, 0);

        activeBatchBuffer.vertex(m, c4x, c4y, 0).color(r, g, b, a);
        activeBatchBuffer.vertex(m, b2x, b2y, 0).color(r, g, b, 0);
        activeBatchBuffer.vertex(m, b1x, b1y, 0).color(r, g, b, 0);
    }

    public static void drawBatchedShape(double cx, double cy, double size, String shape, int color) {
        if (activeBatchBuffer == null || size <= 0) return;
        if (shape == null) shape = "square";
        switch (shape.toLowerCase()) {
            case "circle" -> appendPolygonToBatch(cx, cy, size / 2.0, 16, color, 0);
            case "hexagon" -> appendPolygonToBatch(cx, cy, size / 2.0, 6, color, Math.PI / 2);
            case "diamond" -> appendPolygonToBatch(cx, cy, size / 2.0, 4, color, 0);
            default -> appendPolygonToBatch(cx, cy, size / 2.0, 4, color, Math.PI / 4);
        }
    }

    private static void appendPolygonToBatch(double cx, double cy, double radius, int sides, int color, double angleOffset) {
        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Matrix4f m = activeBatchMatrix;

        for (int i = 0; i < sides; i++) {
            double angle1 = angleOffset + (2 * Math.PI * i / sides);
            double angle2 = angleOffset + (2 * Math.PI * (i + 1) / sides);

            float x1 = (float) (cx + radius * Math.cos(angle1));
            float y1 = (float) (cy + radius * Math.sin(angle1));
            float x2 = (float) (cx + radius * Math.cos(angle2));
            float y2 = (float) (cy + radius * Math.sin(angle2));

            activeBatchBuffer.vertex(m, (float) cx, (float) cy, 0).color(r, g, b, a);
            activeBatchBuffer.vertex(m, x2, y2, 0).color(r, g, b, a);
            activeBatchBuffer.vertex(m, x1, y1, 0).color(r, g, b, a);
        }
    }

    public static void drawBatchedShapeGlow(double cx, double cy, double size, double spread, String shape, int color) {
        if (activeBatchBuffer == null || size <= 0 || spread <= 0) return;
        if (shape == null) shape = "square";
        double radius = size / 2.0;
        switch (shape.toLowerCase()) {
            case "circle" -> appendPolygonGlowToBatch(cx, cy, radius, spread, 24, color, 0);
            case "hexagon" -> appendPolygonGlowToBatch(cx, cy, radius, spread, 6, color, Math.PI / 2);
            case "diamond" -> appendPolygonGlowToBatch(cx, cy, radius, spread, 4, color, 0);
            default -> appendPolygonGlowToBatch(cx, cy, radius, spread, 4, color, Math.PI / 4);
        }
    }

    private static void appendPolygonGlowToBatch(double cx, double cy, double innerRadius, double spread, int sides, int color, double angleOffset) {
        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        double outerRadius = innerRadius + spread;
        Matrix4f m = activeBatchMatrix;

        for (int i = 0; i < sides; i++) {
            double angle1 = angleOffset + (2 * Math.PI * i / sides);
            double angle2 = angleOffset + (2 * Math.PI * (i + 1) / sides);

            float inX1 = (float) (cx + innerRadius * Math.cos(angle1));
            float inY1 = (float) (cy + innerRadius * Math.sin(angle1));
            float inX2 = (float) (cx + innerRadius * Math.cos(angle2));
            float inY2 = (float) (cy + innerRadius * Math.sin(angle2));

            float outX1 = (float) (cx + outerRadius * Math.cos(angle1));
            float outY1 = (float) (cy + outerRadius * Math.sin(angle1));
            float outX2 = (float) (cx + outerRadius * Math.cos(angle2));
            float outY2 = (float) (cy + outerRadius * Math.sin(angle2));

            activeBatchBuffer.vertex(m, inX1, inY1, 0).color(r, g, b, a);
            activeBatchBuffer.vertex(m, inX2, inY2, 0).color(r, g, b, a);
            activeBatchBuffer.vertex(m, outX2, outY2, 0).color(r, g, b, 0);

            activeBatchBuffer.vertex(m, inX1, inY1, 0).color(r, g, b, a);
            activeBatchBuffer.vertex(m, outX2, outY2, 0).color(r, g, b, 0);
            activeBatchBuffer.vertex(m, outX1, outY1, 0).color(r, g, b, 0);
        }
    }

    public static void drawShape(DrawContext context, double cx, double cy, double size, String shape, int color) {
        if (shape == null) shape = "square";

        switch (shape.toLowerCase()) {
            case "circle" -> drawPolygon(context, cx, cy, size / 2.0, 16, color, 0);
            case "hexagon" -> drawPolygon(context, cx, cy, size / 2.0, 6, color, Math.PI / 2);
            case "diamond" -> drawPolygon(context, cx, cy, size / 2.0, 4, color, 0);
            default -> drawPolygon(context, cx, cy, size / 2.0, 4, color, Math.PI / 4); // Square aligned
        }
    }

    public static void drawShapeGlow(DrawContext context, double cx, double cy, double size, double spread, String shape, int color) {
        if (shape == null) shape = "square";
        double radius = size / 2.0;

        switch (shape.toLowerCase()) {
            case "circle" -> drawPolygonGlow(context, cx, cy, radius, spread, 24, color, 0);
            case "hexagon" -> drawPolygonGlow(context, cx, cy, radius, spread, 6, color, Math.PI / 2);
            case "diamond" -> drawPolygonGlow(context, cx, cy, radius, spread, 4, color, 0);
            default -> drawPolygonGlow(context, cx, cy, radius, spread, 4, color, Math.PI / 4); // Square aligned
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

    private static void drawPolygonGlow(DrawContext context, double cx, double cy, double innerRadius, double spread, int sides, int color, double angleOffset) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        double outerRadius = innerRadius + spread;

        for (int i = 0; i < sides; i++) {
            double angle1 = angleOffset + (2 * Math.PI * i / sides);
            double angle2 = angleOffset + (2 * Math.PI * (i + 1) / sides);

            float inX1 = (float) (cx + innerRadius * Math.cos(angle1));
            float inY1 = (float) (cy + innerRadius * Math.sin(angle1));
            float inX2 = (float) (cx + innerRadius * Math.cos(angle2));
            float inY2 = (float) (cy + innerRadius * Math.sin(angle2));

            float outX1 = (float) (cx + outerRadius * Math.cos(angle1));
            float outY1 = (float) (cy + outerRadius * Math.sin(angle1));
            float outX2 = (float) (cx + outerRadius * Math.cos(angle2));
            float outY2 = (float) (cy + outerRadius * Math.sin(angle2));

            // Triangle 1 (CCW)
            buffer.vertex(matrix, inX1, inY1, 0).color(r, g, b, a);
            buffer.vertex(matrix, inX2, inY2, 0).color(r, g, b, a);
            buffer.vertex(matrix, outX2, outY2, 0).color(r, g, b, 0);

            // Triangle 2 (CCW)
            buffer.vertex(matrix, inX1, inY1, 0).color(r, g, b, a);
            buffer.vertex(matrix, outX2, outY2, 0).color(r, g, b, 0);
            buffer.vertex(matrix, outX1, outY1, 0).color(r, g, b, 0);
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

    /**
     * Draws an outset 3D beveled stone panel (chiseled stone plate/dialog).
     */
    public static void drawBeveledPanel(DrawContext context, int x, int y, int width, int height) {
        drawBeveledPanel(context, x, y, width, height, 0xFF24242C, 0xFF585864, 0xFF141418, 0xFF0E0E12);
    }

    public static void drawBeveledPanel(DrawContext context, int x, int y, int width, int height, int centerColor, int lightBevel, int darkBevel, int outerBorder) {
        // Outer 1px frame
        context.fill(x, y, x + width, y + height, outerBorder);
        // Center fill
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, centerColor);
        // Light bevel on Top & Left
        context.fill(x + 1, y + 1, x + width - 1, y + 2, lightBevel);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, lightBevel);
        // Dark bevel on Bottom & Right
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, darkBevel);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, darkBevel);
    }

    /**
     * Draws an inset 3D sunken well (container cavity for scroll lists / content).
     */
    public static void drawInsetPanel(DrawContext context, int x, int y, int width, int height) {
        drawInsetPanel(context, x, y, width, height, 0xFF141418, 0xFF0E0E12, 0xFF383842, 0xFF0A0A0E);
    }

    public static void drawInsetPanel(DrawContext context, int x, int y, int width, int height, int centerColor, int topShadow, int bottomHighlight, int outerBorder) {
        // Outer 1px frame
        context.fill(x, y, x + width, y + height, outerBorder);
        // Center fill
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, centerColor);
        // Top & Left inner shadow (sunken depth)
        context.fill(x + 1, y + 1, x + width - 1, y + 2, topShadow);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, topShadow);
        // Bottom & Right inner highlight (catches ambient light)
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, bottomHighlight);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, bottomHighlight);
    }
}
