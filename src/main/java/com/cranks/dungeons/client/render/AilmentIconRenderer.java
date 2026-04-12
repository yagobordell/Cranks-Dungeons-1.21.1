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
            // Debug logging to verify server data
            for (AilmentSyncPayload.AilmentEntry entry : ailments) {
                if (entry.stacks() > 1) {
                    String msg = "AILMENT SYNC: [Entity " + entityId + "] " + entry.texture() + " (Stacks: " + entry.stacks() + ")";
                    com.cranks.dungeons.CranksDungeons.LOGGER.info(msg);
                    System.out.println(msg); // Direct output to ensure it's seen
                }
            }
        }
    }

    private static final Identifier FONT_TEXTURE = Identifier.ofVanilla("textures/font/ascii.png");

    public static void render(WorldRenderContext context) {
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
                
                // Position above head
                double wx = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX()) - cameraX;
                double wy = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) - cameraY + living.getHeight() + 0.5;
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
                    
                    renderIcon(matrices, info.texture(), size, context.consumers());
                    
                    if (info.stacks() > 1) {
                        renderStacks(matrices, info.stacks(), size, context.consumers());
                    }
                    
                    matrices.pop();
                }

                matrices.pop();
            }
        }
    }

    private static void renderStacks(MatrixStack matrices, int stacks, float iconSize, VertexConsumerProvider consumers) {
        matrices.push();
        // Shift to bottom-right corner, push forward by -0.2f
        matrices.translate(-iconSize * 0.45f, -iconSize * 0.3f, -0.2f);
        
        String text = "x" + stacks;
        float charSize = iconSize * 0.4f;
        float charSpacing = charSize * 0.6f;
        
        VertexConsumer buffer = consumers.getBuffer(RenderLayers.entityTranslucent(FONT_TEXTURE));
        MatrixStack.Entry entry = matrices.peek();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            drawChar(buffer, entry, c, i * charSpacing, 0, charSize);
        }
        
        matrices.pop();
    }

    private static void drawChar(VertexConsumer buffer, MatrixStack.Entry entry, char c, float x, float y, float size) {
        // ASCII font texture is 16x16 grid
        int uIdx = c % 16;
        int vIdx = c / 16;
        float u1 = uIdx / 16f;
        float v1 = vIdx / 16f;
        float u2 = (uIdx + 1) / 16f;
        float v2 = (vIdx + 1) / 16f;

        float half = size / 2f;
        // Flip UVs (u1/u2 swap) to fix mirroring
        vertex(buffer, entry, x - half, y - half, 0, u2, v2);
        vertex(buffer, entry, x + half, y - half, 0, u1, v2);
        vertex(buffer, entry, x + half, y + half, 0, u1, v1);
        vertex(buffer, entry, x - half, y + half, 0, u2, v1);
    }

    private static void renderIcon(MatrixStack matrices, Identifier texture, float size, VertexConsumerProvider consumers) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayers.entityTranslucent(texture));
        MatrixStack.Entry entry = matrices.peek();
        
        float half = size / 2f;
        vertex(buffer, entry, -half, -half, 0, 0, 1);
        vertex(buffer, entry, half, -half, 0, 1, 1);
        vertex(buffer, entry, half, half, 0, 1, 0);
        vertex(buffer, entry, -half, half, 0, 0, 0);
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry entry, float x, float y, float z, float u, float v) {
        buffer.vertex(entry, x, y, z)
            .color(255, 255, 255, 255)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
            .normal(entry, 0, 1, 0);
    }
}
