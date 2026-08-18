package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
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

import com.cranks.dungeons.config.HealthBarDisplayMode;

public class EntityStatBarRenderer {

    public enum MobCategory {
        HOSTILE,
        NEUTRAL,
        FRIENDLY
    }

    public static MobCategory getMobCategory(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return MobCategory.FRIENDLY;
        }

        // Hostile monsters
        if (entity instanceof net.minecraft.entity.mob.Monster
                || entity instanceof net.minecraft.entity.mob.SlimeEntity
                || entity instanceof net.minecraft.entity.mob.GhastEntity
                || entity instanceof net.minecraft.entity.mob.PhantomEntity
                || entity instanceof net.minecraft.entity.boss.WitherEntity
                || entity instanceof net.minecraft.entity.boss.dragon.EnderDragonEntity
                || entity instanceof net.minecraft.entity.mob.ShulkerEntity
                || entity instanceof net.minecraft.entity.mob.HoglinEntity
                || entity instanceof net.minecraft.entity.mob.ZoglinEntity) {
            if (entity instanceof net.minecraft.entity.mob.Angerable angerable) {
                return angerable.hasAngerTime() ? MobCategory.HOSTILE : MobCategory.NEUTRAL;
            }
            return MobCategory.HOSTILE;
        }

        // Neutral / Angerable entities
        if (entity instanceof net.minecraft.entity.mob.Angerable) {
            return MobCategory.NEUTRAL;
        }
        if (entity instanceof net.minecraft.entity.passive.IronGolemEntity
                || entity instanceof net.minecraft.entity.passive.SnowGolemEntity
                || entity instanceof net.minecraft.entity.passive.DolphinEntity
                || entity instanceof net.minecraft.entity.passive.BatEntity) {
            return MobCategory.NEUTRAL;
        }

        // Passive / Friendly (Cows, Sheep, Pigs, Villagers, Horses, Pets, etc.)
        return MobCategory.FRIENDLY;
    }

    public static boolean canSeeEntity(PlayerEntity player, LivingEntity entity) {
        if (player.getWorld() != entity.getWorld()) return false;

        net.minecraft.util.math.Vec3d eyePos = new net.minecraft.util.math.Vec3d(player.getX(), player.getEyeY(), player.getZ());

        // 1. Check head / eyes
        net.minecraft.util.math.Vec3d headPos = new net.minecraft.util.math.Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        if (player.getWorld().raycast(new net.minecraft.world.RaycastContext(eyePos, headPos, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, player)).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
            return true;
        }

        // 2. Check chest / center of entity
        net.minecraft.util.math.Vec3d centerPos = entity.getBoundingBox().getCenter();
        if (player.getWorld().raycast(new net.minecraft.world.RaycastContext(eyePos, centerPos, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, player)).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
            return true;
        }

        // 3. Check feet / base of entity
        net.minecraft.util.math.Vec3d feetPos = new net.minecraft.util.math.Vec3d(entity.getX(), entity.getY() + 0.2, entity.getZ());
        if (player.getWorld().raycast(new net.minecraft.world.RaycastContext(eyePos, feetPos, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, player)).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
            return true;
        }

        return false;
    }

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
    private static final Identifier HEALTH_BURN_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/health_burn.png");
    private static final Identifier HEALTH_POISON_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/health_poison.png");
    private static final Identifier HEALTH_BLEED_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/health_bleed.png");
    private static final Identifier SHIELD_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/energy_shield.png");
    private static final Identifier ABSORPTION_FILL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/sprites/hud/stat_bar/absorption.png");

    public static Identifier getEntityHealthFillTexture(LivingEntity entity) {
        if (entity != null) {
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.BLEED) || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BLEED)) {
                return HEALTH_BLEED_FILL;
            }
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.POISON) || entity.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON) || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.POISON)) {
                return HEALTH_POISON_FILL;
            }
            if (entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.BURN) || entity.isOnFire() || com.cranks.dungeons.ailment.AilmentManager.hasAilment(entity, com.cranks.dungeons.ailment.AilmentType.BURN)) {
                return HEALTH_BURN_FILL;
            }
        }
        return HEALTH_FILL;
    }

    public static void render(WorldRenderContext context, VertexConsumerProvider consumers) {
        if (!CranksDungeons.CONFIG.useStatBars()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrixStack();
        float tickDelta = client.getRenderTickCounter().getTickDelta(true);
        Camera camera = client.gameRenderer.getCamera();
        
        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

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
                
                double dist = living.squaredDistanceTo(client.player);
                if (dist > 32 * 32) continue;
                
                // Only show nameplate if the entity itself is visible
                if (!canSeeEntity(client.player, living)) continue;

                matrices.push();
                
                // Position above head
                double wx = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX()) - cameraX;
                double wy = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) - cameraY + living.getHeight() + 0.1;
                double wz = MathHelper.lerp(tickDelta, living.lastRenderZ, living.getZ()) - cameraZ;

                matrices.translate(wx, wy, wz);
                
                // Billboard
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                renderEntityStats(matrices, living, consumers, client);

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
            
            if (!CranksDungeons.CONFIG.showEntityHealthBars() || !CranksDungeons.CONFIG.showMobsBars()) {
                showBars = false;
            } else {
                MobCategory category = getMobCategory(entity);
                HealthBarDisplayMode mode = switch (category) {
                    case HOSTILE -> CranksDungeons.CONFIG.hostileMobHealthBars();
                    case NEUTRAL -> CranksDungeons.CONFIG.neutralMobHealthBars();
                    case FRIENDLY -> CranksDungeons.CONFIG.friendlyMobHealthBars();
                };

                if (mode == HealthBarDisplayMode.NEVER) {
                    showBars = false;
                } else if (mode == HealthBarDisplayMode.WHEN_DAMAGED) {
                    boolean isDamaged = entity.getHealth() < entity.getMaxHealth() || entity.getAbsorptionAmount() > 0;
                    ShieldInfo sInfo = ENTITY_SHIELD_CACHE.get(entity.getId());
                    if (sInfo != null && sInfo.current < sInfo.max) {
                        isDamaged = true;
                    }
                    showBars = isDamaged;
                } else {
                    showBars = true;
                }
            }
            showParty = false;
        }

        if (!showName && !showLevel && !showBars && !showParty) return;

        float scale = 0.025f;
        matrices.scale(-scale, -scale, scale); // Standard vanilla nametag scaling logic

        PlayerInfo info = isPlayer ? PLAYER_INFO_CACHE.get(entity.getId()) : null;
        PlayerInfo mobInfo = !isPlayer && showLevel ? PLAYER_INFO_CACHE.get(entity.getId()) : null;

        // Dynamic yOffset: calculate total height based on which elements are active
        float totalHeight = 0;
        if (showName) totalHeight += 10;
        if (isPlayer && info != null) {
            if (showLevel) totalHeight += 10;
            if (showParty && !info.partyLeaderName.isEmpty()) totalHeight += 10;
        } else if (mobInfo != null) {
            totalHeight += 10;
        }

        if (showBars) {
            ShieldInfo sInfo = ENTITY_SHIELD_CACHE.get(entity.getId());
            if (sInfo != null && sInfo.max > 0) {
                totalHeight += 3.2f;
            }
            totalHeight += 5f; // Health bar height + spacing
        }

        float yOffset = -totalHeight;
        TextRenderer textRenderer = client.textRenderer;

        if (showName) {
            String name = entity.getName().getString();
            int nameWidth = textRenderer.getWidth(name);
            drawText(matrices, textRenderer, name, -nameWidth / 2f, yOffset, 0xFFFFFFFF, consumers);
            flush(consumers);
            yOffset += 10;
        }

        if (isPlayer && info != null) {
            if (showLevel) {
                // Second Row: Level and Class
                String topText = "Lv." + info.level + " " + (info.className.isEmpty() ? "Novice" : capitalize(info.className));
                int textWidth = textRenderer.getWidth(topText);
                drawText(matrices, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFCCCCCC, consumers);
                flush(consumers);
                yOffset += 10;
            }

            if (showParty && !info.partyLeaderName.isEmpty()) {
                // Third Row: Party Name
                String partyText = info.partyLeaderName + "'s Party";
                int pWidth = textRenderer.getWidth(partyText);
                drawText(matrices, textRenderer, partyText, -pWidth / 2f, yOffset, 0xFF55FFFF, consumers);
                flush(consumers);
                yOffset += 10;
            }
        } else if (showLevel && mobInfo != null) {
            // Mobs can show level too if sync data exists (future proofing)
            String topText = "Lv." + mobInfo.level;
            int textWidth = textRenderer.getWidth(topText);
            drawText(matrices, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFCCCCCC, consumers);
            flush(consumers);
            yOffset += 10;
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
            flush(consumers);
            // Fill - Left-Aligned
            if (shieldPercent > 0) {
                float fillWidth = barWidth * shieldPercent;
                drawBar(matrices, consumers, SHIELD_FILL, xStart, yOffset, fillWidth, sHeight, 1.0f, -0.1f, 0, shieldPercent);
                flush(consumers);
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
        flush(consumers);
        
        // HP Fill - Left-Aligned
        if (hpPercent > 0) {
            float fillWidth = barWidth * hpPercent;
            Identifier entityHealthTexture = getEntityHealthFillTexture(entity);
            drawBar(matrices, consumers, entityHealthTexture, xStart, yOffset, fillWidth, barHeight, 1.0f, -0.1f, 0, hpPercent);
            flush(consumers);
        }

        // Absorption Fill (Z = -0.20)
        if (absorption > 0) {
            float absPercent = MathHelper.clamp(absorption / maxHealth, 0, 1);
            float absWidth = barWidth * absPercent;
            drawBar(matrices, consumers, ABSORPTION_FILL, xStart, yOffset, absWidth, barHeight, 1.0f, -0.2f, 0, absPercent);
            flush(consumers);
        }
        
        yOffset += 5;
    }

    private static void flush(VertexConsumerProvider consumers) {
        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    private static void drawBar(MatrixStack matrices, VertexConsumerProvider consumers, Identifier texture, float x, float y, float width, float height, float alpha, float zOffset, float uStart, float uEnd) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getTextSeeThrough(texture));
        Matrix4f model = matrices.peek().getPositionMatrix();
        
        buffer.vertex(model, x, y, zOffset).color(1f, 1f, 1f, alpha).texture(uStart, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(model, x, y + height, zOffset).color(1f, 1f, 1f, alpha).texture(uStart, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(model, x + width, y + height, zOffset).color(1f, 1f, 1f, alpha).texture(uEnd, 1).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(model, x + width, y, zOffset).color(1f, 1f, 1f, alpha).texture(uEnd, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    private static void drawText(MatrixStack matrices, TextRenderer textRenderer, String text, float x, float y, int color, VertexConsumerProvider consumers) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 1. See-through pass (renders through ceiling blocks and obstacles)
        textRenderer.draw(text, x, y, color, false, matrix, consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        flush(consumers);

        // 2. Normal pass (renders crisp full-brightness when in direct view)
        matrices.push();
        matrices.translate(0.0f, 0.0f, -0.02f);
        textRenderer.draw(text, x, y, color, false, matrices.peek().getPositionMatrix(), consumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        flush(consumers);
        matrices.pop();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
