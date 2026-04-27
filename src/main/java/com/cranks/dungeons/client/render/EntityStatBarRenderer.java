package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityStatBarRenderer {

    private static final Map<Integer, PlayerInfo> PLAYER_INFO_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> ENTITY_SHIELD_CACHE = new ConcurrentHashMap<>();

    public record PlayerInfo(int level, String className, String partyLeaderName) {}

    public static void updatePlayerInfo(int entityId, int level, String className, String partyLeaderName) {
        PLAYER_INFO_CACHE.put(entityId, new PlayerInfo(level, className, partyLeaderName));
    }

    public static void updateEntityShield(int entityId, float shield) {
        ENTITY_SHIELD_CACHE.put(entityId, shield);
    }

    private static final Identifier EMPTY_BAR = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/empty.png");
    private static final Identifier EMPTY_SHIELD_BAR = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/empty_energy_shield.png");
    private static final Identifier HEALTH_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/health.png");
    private static final Identifier SHIELD_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/energy_shield.png");
    private static final Identifier ABSORPTION_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/absorption.png");

    public static void render(WorldRenderContext context) {
        if (!CranksDungeons.CONFIG.useStatBars()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrices();
        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        Camera camera = client.gameRenderer.getCamera();
        
        double cameraX = camera.getCameraPos().x;
        double cameraY = camera.getCameraPos().y;
        double cameraZ = camera.getCameraPos().z;

        // Cleanup caches
        if (client.world.getTime() % 100 == 0) {
            PLAYER_INFO_CACHE.keySet().removeIf(id -> client.world.getEntityById(id) == null);
            ENTITY_SHIELD_CACHE.keySet().removeIf(id -> client.world.getEntityById(id) == null);
        }

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity living && living.isAlive() && !living.isInvisibleTo(client.player)) {
                // Skip local player
                if (living == client.player) continue;
                
                boolean isPlayer = living instanceof PlayerEntity;
                if (isPlayer && !CranksDungeons.CONFIG.showPlayerInfoAboveHead()) continue;
                if (!isPlayer && !CranksDungeons.CONFIG.showEntityHealthBars()) continue;
                
                double dist = living.squaredDistanceTo(client.player);
                if (dist > 32 * 32) continue;
                
                // Occlusion check (if not visible through walls)
                if (!client.player.canSee(living)) continue;

                matrices.push();
                
                // Position above head
                double wx = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX()) - cameraX;
                double wy = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) - cameraY + living.getHeight() + 0.5;
                double wz = MathHelper.lerp(tickDelta, living.lastRenderZ, living.getZ()) - cameraZ;

                matrices.translate(wx, wy, wz);
                
                // Billboard
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                renderEntityStats(matrices, living, context.consumers(), client);

                matrices.pop();
            }
        }
    }

    private static void renderEntityStats(MatrixStack matrices, LivingEntity entity, VertexConsumerProvider consumers, MinecraftClient client) {
        boolean isPlayer = entity instanceof PlayerEntity;
        
        float scale = 0.02f;
        matrices.scale(scale, -scale, scale); // Scale down and flip Y for 2D-like coords

        float yOffset = 0;
        TextRenderer textRenderer = client.textRenderer;

        if (isPlayer) {
            PlayerInfo info = PLAYER_INFO_CACHE.get(entity.getId());
            if (info != null) {
                // Top Row: Level and Class
                String topText = "Lv." + info.level + " " + (info.className.isEmpty() ? "Novice" : capitalize(info.className));
                int textWidth = textRenderer.getWidth(topText);
                drawText(matrices, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFFFFFFF, consumers);
                yOffset += 10;
            }
        }

        float barWidth = 40;
        float xStart = -barWidth / 2f;

        // --- Shield Bar (Above HP) ---
        float shield = 0;
        float maxShield = (float) entity.getAttributeValue(ModAttributes.ENERGY_SHIELD);

        if (entity instanceof EnergyShieldProvider provider) {
            shield = provider.cranksdungeons$getCurrentEnergyShield();
        } else if (ENTITY_SHIELD_CACHE.containsKey(entity.getId())) {
            shield = ENTITY_SHIELD_CACHE.get(entity.getId());
        }

        if (maxShield > 0) {
            float shieldPercent = MathHelper.clamp(shield / maxShield, 0, 1);
            float sHeight = 2.66f; // 6px height (6/9 * 4)
            // Background (Z = 0)
            drawBar(matrices, consumers, EMPTY_SHIELD_BAR, xStart, yOffset, barWidth, sHeight, 1.0f, 0.00f, 0.0f, 1.0f);
            
            if (shieldPercent > 0) {
                // Flipped: filled part stays on the RIGHT
                float fillWidth = barWidth * shieldPercent;
                float fillX = xStart + (barWidth - fillWidth);
                drawBar(matrices, consumers, SHIELD_FILL, fillX, yOffset, fillWidth, sHeight, 1.0f, -0.01f, 1.0f - shieldPercent, 1.0f);
            }
            yOffset += 4;
        }

        // --- Health Bar ---
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float absorption = entity.getAbsorptionAmount();
        float barHeight = 4; // 9px height

        // Background (Z = 0)
        drawBar(matrices, consumers, EMPTY_BAR, xStart, yOffset, barWidth, barHeight, 1.0f, 0.0f, 0.0f, 1.0f);

        // HP Fill (Z = -0.01)
        float hpPercent = MathHelper.clamp(health / maxHealth, 0, 1);
        if (hpPercent > 0) {
            // Depletion from right to left (right-aligned fill)
            float fillWidth = barWidth * hpPercent;
            float fillX = xStart + (barWidth - fillWidth);
            drawBar(matrices, consumers, HEALTH_FILL, fillX, yOffset, fillWidth, barHeight, 1.0f, -0.01f, 1.0f - hpPercent, 1.0f);
        }

        // Absorption Fill (Z = -0.02)
        if (absorption > 0) {
            float absPercent = MathHelper.clamp(absorption / maxHealth, 0, 1);
            float absWidth = barWidth * absPercent;
            float absX = xStart + (barWidth - absWidth);
            drawBar(matrices, consumers, ABSORPTION_FILL, absX, yOffset, absWidth, barHeight, 1.0f, -0.02f, 1.0f - absPercent, 1.0f);
        }
        
        yOffset += 5;

        // Party info at the bottom
        if (isPlayer) {
            PlayerInfo info = PLAYER_INFO_CACHE.get(entity.getId());
            if (info != null && !info.partyLeaderName.isEmpty()) {
                String partyText = info.partyLeaderName + "'s Party";
                int pWidth = textRenderer.getWidth(partyText);
                drawText(matrices, textRenderer, partyText, -pWidth / 2f, yOffset, 0xFF55FFFF, consumers);
            }
        }
    }

    private static void drawBar(MatrixStack matrices, VertexConsumerProvider consumers, Identifier texture, float x, float y, float width, float height, float alpha, float zOffset, float uStart, float uEnd) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayers.entityTranslucent(texture));
        Matrix4f model = matrices.peek().getPositionMatrix();
        
        buffer.vertex(model, x, y, zOffset).color(1f, 1f, 1f, alpha).texture(uStart, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        buffer.vertex(model, x, y + height, zOffset).color(1f, 1f, 1f, alpha).texture(uStart, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        buffer.vertex(model, x + width, y + height, zOffset).color(1f, 1f, 1f, alpha).texture(uEnd, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        buffer.vertex(model, x + width, y, zOffset).color(1f, 1f, 1f, alpha).texture(uEnd, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
    }

    private static void drawText(MatrixStack matrices, TextRenderer textRenderer, String text, float x, float y, int color, VertexConsumerProvider consumers) {
        textRenderer.draw(text, x, y, color, false, matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
