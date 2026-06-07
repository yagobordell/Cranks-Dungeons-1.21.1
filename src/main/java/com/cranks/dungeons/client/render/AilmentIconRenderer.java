package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.effect.ModEffects;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import com.cranks.dungeons.network.AilmentSyncPayload;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AilmentIconRenderer {

    // Cache to store synced ailment data from the server
    private static final Map<Integer, List<AilmentSyncPayload.AilmentEntry>> AILMENT_CACHE = new ConcurrentHashMap<>();

    public static void updateAilments(int entityId, List<AilmentSyncPayload.AilmentEntry> ailments) {
        if (ailments.isEmpty()) {
            AILMENT_CACHE.remove(entityId);
        } else {
            AILMENT_CACHE.put(entityId, new ArrayList<>(ailments));
        }
    }

    public static List<AilmentSyncPayload.AilmentEntry> getAilments(int entityId) {
        return AILMENT_CACHE.getOrDefault(entityId, List.of());
    }



    public static void render(WorldRenderContext context, VertexConsumerProvider consumers) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrices();
        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        net.minecraft.client.render.Camera camera = client.gameRenderer.getCamera();
        
        double cameraX = camera.getCameraPos().x;
        double cameraY = camera.getCameraPos().y;
        double cameraZ = camera.getCameraPos().z;

        // Cleanup cache for entities no longer in the world
        if (client.world.getTime() % 100 == 0) {
            AILMENT_CACHE.keySet().removeIf(id -> client.world.getEntityById(id) == null);
        }

        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive() && !living.isInvisibleTo(client.player)) {
                // Skip the local player to prevent blocking their own view if they look up
                if (living == client.player) continue;
                
                if (living.distanceTo(client.player) > 32) continue;

                List<AilmentSyncPayload.AilmentEntry> activeAilments = AILMENT_CACHE.get(living.getId());
                if (activeAilments == null || activeAilments.isEmpty()) continue;

                matrices.push();
                
                // Position above head, high enough to clear the stat bar
                double wx = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX()) - cameraX;
                double wy = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) - cameraY + living.getHeight() + 1.1;
                double wz = MathHelper.lerp(tickDelta, living.lastRenderZ, living.getZ()) - cameraZ;

                matrices.translate(wx, wy, wz);
                
                // Billboard rotation
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                // Bobbing animation
                float time = (client.world.getTime() + tickDelta) * 0.1f;
                float bob = MathHelper.sin(time) * 0.05f;
                matrices.translate(0, bob, 0);

                float size = 0.4f;
                float spacing = size * 1.1f;
                float totalWidth = (activeAilments.size() - 1) * spacing;
                matrices.translate(-totalWidth / 2f, 0, 0);

                for (int i = 0; i < activeAilments.size(); i++) {
                    AilmentSyncPayload.AilmentEntry info = activeAilments.get(i);
                    matrices.push();
                    matrices.translate(i * spacing, 0, 0);
                    
                    renderIcon(matrices, info.texture(), size, consumers);
                    
                    // Flush the icon so that the see-through text draws completely over it
                    if (consumers instanceof net.minecraft.client.render.VertexConsumerProvider.Immediate immediate) {
                        immediate.draw();
                    }
                    
                    if (info.texture().getPath().contains("buildup")) {
                        renderStacks(matrices, info.stacks() + "%", size, consumers);
                    } else if (info.stacks() > 1) {
                        renderStacks(matrices, "x" + info.stacks(), size, consumers);
                    }
                    
                    // Flush the text
                    if (consumers instanceof net.minecraft.client.render.VertexConsumerProvider.Immediate immediate) {
                        immediate.draw();
                    }
                    
                    matrices.pop();
                }

                matrices.pop();
            }
        }
    }

    private static void renderStacks(MatrixStack matrices, String text, float iconSize, VertexConsumerProvider consumers) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        
        matrices.push();
        
        // Position vertically centered, and slightly pulled forward in text space
        matrices.translate(0, 0, -0.05f);
        
        // Scale down from world space to text space
        float textScale = iconSize * 0.035f;
        matrices.scale(-textScale, -textScale, textScale);
        
        // Center the text
        float textWidth = textRenderer.getWidth(text);
        
        textRenderer.draw(
            text,
            -textWidth / 2f,
            0,
            0xFFFFFFFF, // white with full alpha
            true, // shadow for readability
            matrices.peek().getPositionMatrix(),
            consumers,
            TextRenderer.TextLayerType.NORMAL,
            0x40000000, // semi-transparent black background
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
        
        matrices.pop();
    }

    private static void renderIcon(MatrixStack matrices, Identifier texture, float size, VertexConsumerProvider consumers) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayers.text(texture));
        MatrixStack.Entry entry = matrices.peek();
        
        float half = size / 2f;
        // +X is left on screen, +Y is up. Trace CCW: Top-Left -> Bottom-Left -> Bottom-Right -> Top-Right
        vertex(buffer, entry, half, half, 0, 0, 0, 255);      // Top-Left (U=0, V=0)
        vertex(buffer, entry, half, -half, 0, 0, 1, 255);     // Bottom-Left (U=0, V=1)
        vertex(buffer, entry, -half, -half, 0, 1, 1, 255);    // Bottom-Right (U=1, V=1)
        vertex(buffer, entry, -half, half, 0, 1, 0, 255);     // Top-Right (U=1, V=0)
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry entry, float x, float y, float z, float u, float v) {
        vertex(buffer, entry, x, y, z, u, v, 255, 255, 255, 255);
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry entry, float x, float y, float z, float u, float v, int alpha) {
        vertex(buffer, entry, x, y, z, u, v, alpha, 255, 255, 255);
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry entry, float x, float y, float z, float u, float v, int alpha, int r, int g, int b) {
        buffer.vertex(entry, x, y, z)
            .color(r, g, b, alpha)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
            .normal(entry, 0, 1, 0);
    }
}
