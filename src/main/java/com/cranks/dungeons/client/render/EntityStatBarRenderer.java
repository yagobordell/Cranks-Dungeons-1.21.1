package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderLayers;
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
    private static final Map<Integer, ShieldInfo> ENTITY_SHIELD_CACHE = new ConcurrentHashMap<>();

    public record PlayerInfo(int level, String className, String partyLeaderName) {}
    
    public static class ShieldInfo {
        public float current;
        public float max;
        public float displayed;
        public long lastUpdateTime;
        
        public ShieldInfo(float current, float max) {
            this.current = current;
            this.max = max;
            this.displayed = current;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }

    public static void updatePlayerInfo(int entityId, int level, String className, String partyLeaderName) {
        PLAYER_INFO_CACHE.put(entityId, new PlayerInfo(level, className, partyLeaderName));
    }

    public static void updateEntityShield(int entityId, float current, float max) {
        ENTITY_SHIELD_CACHE.compute(entityId, (id, old) -> {
            if (old == null) return new ShieldInfo(current, max);
            old.current = current;
            old.max = max;
            old.lastUpdateTime = System.currentTimeMillis();
            return old;
        });
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
                // Hide local player nameplate in first person
                if (living == client.player && client.options.getPerspective().isFirstPerson()) continue;
                
                boolean isPlayer = living instanceof PlayerEntity;
                if (isPlayer && !CranksDungeons.CONFIG.showPlayerInfoAboveHead()) continue;
                if (!isPlayer && !CranksDungeons.CONFIG.showEntityHealthBars()) continue;
                
                double dist = living.squaredDistanceTo(client.player);
                if (dist > 32 * 32) continue;
                
                // Only show nameplate if the entity itself is visible
                if (!client.player.canSee(living)) continue;

                matrices.push();
                
                // Position above head - lowered slightly as requested
                double wx = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX()) - cameraX;
                double wy = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) - cameraY + living.getHeight() + 0.25;
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
        boolean isSelf = entity == client.player;
        boolean isPlayer = entity instanceof PlayerEntity;
        
        // Config shortcuts
        boolean showName, showLevel, showBars, showParty;
        if (isSelf) {
            showName = CranksDungeons.CONFIG.showSelfName();
            showLevel = CranksDungeons.CONFIG.showSelfLevel();
            showBars = CranksDungeons.CONFIG.showSelfBars();
            showParty = CranksDungeons.CONFIG.showSelfParty();
        } else if (isPlayer) {
            showName = CranksDungeons.CONFIG.showOtherPlayersName();
            showLevel = CranksDungeons.CONFIG.showOtherPlayersLevel();
            showBars = CranksDungeons.CONFIG.showOtherPlayersBars();
            showParty = CranksDungeons.CONFIG.showOtherPlayersParty();
        } else {
            showName = CranksDungeons.CONFIG.showMobsName();
            showLevel = CranksDungeons.CONFIG.showMobsLevel();
            showBars = CranksDungeons.CONFIG.showMobsBars();
            showParty = false;
        }

        if (!showName && !showLevel && !showBars && !showParty) return;

        float scale = 0.02f;
        matrices.scale(-scale, -scale, scale); // Standard vanilla nametag scaling logic

        // Dynamic yOffset: Start higher if we have more rows to render
        float yOffset = -30;
        PlayerInfo info = isPlayer ? PLAYER_INFO_CACHE.get(entity.getId()) : null;
        
        if (info != null && !info.partyLeaderName.isEmpty() && showParty) {
            yOffset -= 10; // Shift everything up to make room for party name
        }
        
        TextRenderer textRenderer = client.textRenderer;

        if (showName) {
            String name = entity.getName().getString();
            int nameWidth = textRenderer.getWidth(name);
            drawText(matrices, textRenderer, name, -nameWidth / 2f, yOffset, 0xFFFFFFFF, consumers);
            yOffset += 10;
        }

        if (isPlayer && info != null) {
            if (showLevel) {
                // Second Row: Level and Class
                String topText = "Lv." + info.level + " " + (info.className.isEmpty() ? "Novice" : capitalize(info.className));
                int textWidth = textRenderer.getWidth(topText);
                drawText(matrices, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFCCCCCC, consumers);
                yOffset += 10;
            }

            if (showParty && !info.partyLeaderName.isEmpty()) {
                // Third Row: Party Name
                String partyText = info.partyLeaderName + "'s Party";
                int pWidth = textRenderer.getWidth(partyText);
                drawText(matrices, textRenderer, partyText, -pWidth / 2f, yOffset, 0xFF55FFFF, consumers);
                yOffset += 10;
            }
        } else if (showLevel) {
            // Mobs can show level too if sync data exists (future proofing)
            PlayerInfo mobInfo = PLAYER_INFO_CACHE.get(entity.getId());
            if (mobInfo != null) {
                String topText = "Lv." + mobInfo.level;
                int textWidth = textRenderer.getWidth(topText);
                drawText(matrices, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFCCCCCC, consumers);
                yOffset += 10;
            }
        }

        if (!showBars) return;

        float barWidth = 40;
        float xStart = -barWidth / 2f;

        // --- Shield Bar (Above HP) ---
        ShieldInfo sInfo = ENTITY_SHIELD_CACHE.get(entity.getId());
        if (sInfo != null && sInfo.max > 0) {
            // Client-side interpolation for smooth recharge/damage
            float delta = (System.currentTimeMillis() - sInfo.lastUpdateTime) / 1000f;
            sInfo.lastUpdateTime = System.currentTimeMillis();
            
            if (Math.abs(sInfo.displayed - sInfo.current) > 0.01f) {
                if (sInfo.displayed < sInfo.current) {
                    // Smoothly rise (recharge) - increased speed to be snappier
                    sInfo.displayed = Math.min(sInfo.current, sInfo.displayed + (sInfo.max * 0.4f) * delta);
                } else {
                    // Instant drop for damage
                    sInfo.displayed = sInfo.current;
                }
            } else {
                sInfo.displayed = sInfo.current;
            }

            float shieldPercent = MathHelper.clamp(sInfo.displayed / sInfo.max, 0, 1);
            float sHeight = 2.66f; // 6px height (6/9 * 4)
            // Background
            drawBar(matrices, consumers, EMPTY_SHIELD_BAR, xStart, yOffset, barWidth, sHeight, 1.0f, 0, 0, 1.0f);
            // Fill - Left-Aligned
            if (shieldPercent > 0) {
                float fillWidth = barWidth * shieldPercent;
                drawBar(matrices, consumers, SHIELD_FILL, xStart, yOffset, fillWidth, sHeight, 1.0f, -0.01f, 0, shieldPercent);
            }
            yOffset += 3.2f;
        }

        // --- Health Bar ---
        float maxHealth = entity.getMaxHealth();
        float health = entity.getHealth();
        float absorption = entity.getAbsorptionAmount();
        float hpPercent = MathHelper.clamp(health / maxHealth, 0, 1);
        float barHeight = 4.0f; // 9px height (9/9 * 4)

        // Background
        drawBar(matrices, consumers, EMPTY_BAR, xStart, yOffset, barWidth, barHeight, 1.0f, 0, 0, 1.0f);
        
        // HP Fill - Left-Aligned
        if (hpPercent > 0) {
            float fillWidth = barWidth * hpPercent;
            drawBar(matrices, consumers, HEALTH_FILL, xStart, yOffset, fillWidth, barHeight, 1.0f, -0.01f, 0, hpPercent);
        }

        // Absorption Fill (Z = -0.02)
        if (absorption > 0) {
            float absPercent = MathHelper.clamp(absorption / maxHealth, 0, 1);
            float absWidth = barWidth * absPercent;
            drawBar(matrices, consumers, ABSORPTION_FILL, xStart, yOffset, absWidth, barHeight, 1.0f, -0.02f, 0, absPercent);
        }
        
        yOffset += 5;
    }

    private static void drawBar(MatrixStack matrices, VertexConsumerProvider consumers, Identifier texture, float x, float y, float width, float height, float alpha, float zOffset, float uStart, float uEnd) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayers.textSeeThrough(texture));
        Matrix4f model = matrices.peek().getPositionMatrix();
        
        buffer.vertex(model, x, y, zOffset).color(1f, 1f, 1f, alpha).texture(uStart, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        buffer.vertex(model, x, y + height, zOffset).color(1f, 1f, 1f, alpha).texture(uStart, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        buffer.vertex(model, x + width, y + height, zOffset).color(1f, 1f, 1f, alpha).texture(uEnd, 1).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
        buffer.vertex(model, x + width, y, zOffset).color(1f, 1f, 1f, alpha).texture(uEnd, 0).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
    }

    private static void drawText(MatrixStack matrices, TextRenderer textRenderer, String text, float x, float y, int color, VertexConsumerProvider consumers) {
        textRenderer.draw(net.minecraft.text.Text.literal(text), x, y, color, true, matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
