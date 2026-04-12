package com.cranks.dungeons.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class LightningArcRenderer {
    private static final Random RANDOM = Random.create();

    public static void render(WorldRenderContext context) {
        List<LightningArcManager.ArcInstance> arcs = LightningArcManager.getActiveArcs();
        if (arcs.isEmpty()) return;

        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();
        // Use the discovered lightning() method in RenderLayers (plural)
        VertexConsumer buffer = context.consumers().getBuffer(RenderLayers.lightning());
        
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        MatrixStack.Entry entry = matrices.peek();

        for (LightningArcManager.ArcInstance arc : arcs) {
            drawJaggedArc(buffer, entry, arc.start(), arc.end(), arc.color());
        }

        matrices.pop();
    }

    private static void drawJaggedArc(VertexConsumer buffer, MatrixStack.Entry entry, Vec3d start, Vec3d end, int color) {
        int segments = 8;
        Vec3d current = start;
        
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        for (int i = 1; i <= segments; i++) {
            float progress = (float) i / segments;
            Vec3d targetBase = start.lerp(end, progress);
            
            // Add jitter for jagged look
            Vec3d target;
            if (i < segments) {
                target = targetBase.add(
                    (RANDOM.nextFloat() - 0.5) * 0.2,
                    (RANDOM.nextFloat() - 0.5) * 0.2,
                    (RANDOM.nextFloat() - 0.5) * 0.2
                );
            } else {
                target = end;
            }

            drawBeamSegment(buffer, entry, current, target, r, g, b);
            
            current = target;
        }
    }

    private static void drawBeamSegment(VertexConsumer buffer, MatrixStack.Entry entry, Vec3d start, Vec3d end, float r, float g, float b) {
        float width = 0.05f;
        Vec3d dir = end.subtract(start).normalize();
        Vec3d up = new Vec3d(0, 1, 0);
        if (Math.abs(dir.y) > 0.9) up = new Vec3d(1, 0, 0);
        Vec3d side = dir.crossProduct(up).normalize().multiply(width);
        up = dir.crossProduct(side).normalize().multiply(width);

        // Quad 1
        pos(buffer, entry, start.subtract(side)).color(r, g, b, 1f).light(0xF000F0);
        pos(buffer, entry, end.subtract(side)).color(r, g, b, 1f).light(0xF000F0);
        pos(buffer, entry, end.add(side)).color(r, g, b, 1f).light(0xF000F0);
        pos(buffer, entry, start.add(side)).color(r, g, b, 1f).light(0xF000F0);

        // Quad 2 (Orthogonal)
        pos(buffer, entry, start.subtract(up)).color(r, g, b, 1f).light(0xF000F0);
        pos(buffer, entry, end.subtract(up)).color(r, g, b, 1f).light(0xF000F0);
        pos(buffer, entry, end.add(up)).color(r, g, b, 1f).light(0xF000F0);
        pos(buffer, entry, start.add(up)).color(r, g, b, 1f).light(0xF000F0);
    }

    private static VertexConsumer pos(VertexConsumer buffer, MatrixStack.Entry entry, Vec3d pos) {
        return buffer.vertex(entry, (float)pos.x, (float)pos.y, (float)pos.z);
    }
}
