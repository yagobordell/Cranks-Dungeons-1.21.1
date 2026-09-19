package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    private static final double MAX_RENDER_DISTANCE_SQ = 32.0 * 32.0;
    private static final float WORLD_UI_SCALE = 0.025f;
    private static final int SCREEN_MARGIN = 64;

    /**
     * Draws projected entity plates in the GUI pass. Keeping the actual draw calls
     * out of the world render pipeline prevents shader packs from replacing their
     * shader, depth test, blend mode, or framebuffer.
     */
    public static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        float tickDelta = client.getRenderTickCounter().getTickDelta(true);
        Camera camera = client.gameRenderer.getCamera();

        // Cleanup caches
        if (client.world.getTime() % 100 == 0) {
            PLAYER_INFO_CACHE.keySet().removeIf(id -> client.world.getEntityById(id) == null);
            ENTITY_SHIELD_CACHE.keySet().removeIf(id -> client.world.getEntityById(id) == null);
        }

        List<Entity> nearbyEntities = new ArrayList<>(client.world.getOtherEntities(
                null,
                client.player.getBoundingBox().expand(32.0),
                entity -> entity instanceof LivingEntity));
        // getOtherEntities is allowed to omit the client player. Add the
        // local player explicitly so self nameplate/bar settings work in
        // third person, without duplicating it when it is already present.
        if (!nearbyEntities.contains(client.player)) {
            nearbyEntities.add(client.player);
        }

        List<ProjectedEntity> projectedEntities = new ArrayList<>();
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity living && living.isAlive() && !living.isInvisibleTo(client.player)) {
                // The mounted HUD already represents the local rider's horse. Keeping its
                // world plate would place the name and bars directly in the rider's view.
                if (living == client.player.getVehicle()) continue;

                // Hide local player nameplate in first person
                if (living == client.player && client.options.getPerspective().isFirstPerson()) continue;
                
                boolean isPlayer = living instanceof PlayerEntity;
                if (isPlayer && living != client.player && !CranksDungeons.CONFIG.showPlayerInfoAboveHead()) continue;
                
                if (living.squaredDistanceTo(client.player) > MAX_RENDER_DISTANCE_SQ) continue;
                
                // Only show nameplate if the entity itself is visible
                if (!canSeeEntity(client.player, living)) continue;

                // Position above head
                double worldX = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX());
                double worldY = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) + living.getHeight() + 0.1;
                double worldZ = MathHelper.lerp(tickDelta, living.lastRenderZ, living.getZ());
                ScreenPosition screenPosition = projectToScreen(client, camera, worldX, worldY, worldZ);
                if (screenPosition != null) {
                    projectedEntities.add(new ProjectedEntity(living, screenPosition));
                }
            }
        }

        // Match natural world ordering: distant plates first, nearby plates last.
        projectedEntities.sort(Comparator.comparingDouble(value -> -value.position.depth));
        for (ProjectedEntity projected : projectedEntities) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(projected.position.x, projected.position.y, 0.0f);
            matrices.scale(projected.position.scale, projected.position.scale, 1.0f);
            renderEntityStats(context, projected.entity, client);
            matrices.pop();
        }
    }

    private static ScreenPosition projectToScreen(MinecraftClient client, Camera camera,
                                                   double worldX, double worldY, double worldZ) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        if (screenWidth <= 0 || screenHeight <= 0) return null;

        Vec3d cameraPos = camera.getPos();
        double dx = worldX - cameraPos.x;
        double dy = worldY - cameraPos.y;
        double dz = worldZ - cameraPos.z;

        double yaw = Math.toRadians(camera.getYaw() + 180.0f);
        double pitch = Math.toRadians(camera.getPitch());
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double rotatedX = dx * cosYaw + dz * sinYaw;
        double rotatedZ = -dx * sinYaw + dz * cosYaw;

        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double viewY = dy * cosPitch - rotatedZ * sinPitch;
        double viewZ = dy * sinPitch + rotatedZ * cosPitch;
        if (viewZ >= -0.05) return null;

        double depth = -viewZ;
        double projectedDepth = Math.max(depth, 0.5);
        double halfHeight = Math.tan(Math.toRadians(client.options.getFov().getValue()) / 2.0);
        double halfWidth = halfHeight * ((double) screenWidth / screenHeight);
        if (halfHeight <= 0.0 || halfWidth <= 0.0) return null;

        double normalizedX = rotatedX / (halfWidth * projectedDepth);
        double normalizedY = viewY / (halfHeight * projectedDepth);
        float screenX = (float) ((normalizedX + 1.0) * 0.5 * screenWidth);
        float screenY = (float) ((1.0 - normalizedY) * 0.5 * screenHeight);
        if (screenX < -SCREEN_MARGIN || screenX > screenWidth + SCREEN_MARGIN
                || screenY < -SCREEN_MARGIN || screenY > screenHeight + SCREEN_MARGIN) {
            return null;
        }

        float scale = (float) (WORLD_UI_SCALE * screenHeight / (2.0 * halfHeight * projectedDepth));
        return new ScreenPosition(screenX, screenY, scale, depth);
    }

    private record ScreenPosition(float x, float y, float scale, double depth) {}

    private record ProjectedEntity(LivingEntity entity, ScreenPosition position) {}

    private static int lastAttackedEntityId = -1;
    private static long lastAttackedTime = 0;

    public static void onEntityAttacked(int entityId) {
        lastAttackedEntityId = entityId;
        lastAttackedTime = System.currentTimeMillis();
    }

    public static boolean isEntityInAttackHitboxOrTargeted(MinecraftClient client, LivingEntity target) {
        if (client.player == null || target == null) return false;
        if (target == client.player) return true;

        // 1. Sticky combat target (within last 2.5 seconds of hitting them)
        if (target.getId() == lastAttackedEntityId && (System.currentTimeMillis() - lastAttackedTime) < 2500) {
            return true;
        }

        // 2. Direct cursor target
        if (client.targetedEntity == target) {
            return true;
        }

        double maxAimDist = 10.0;
        double distSq = client.player.squaredDistanceTo(target);
        if (distSq > maxAimDist * maxAimDist) {
            return false;
        }

        net.minecraft.util.math.Vec3d eyePos = client.player.getEyePos();
        net.minecraft.util.math.Vec3d lookVec = client.player.getRotationVec(1.0f);
        net.minecraft.util.math.Vec3d targetCenter = target.getBoundingBox().getCenter();
        net.minecraft.util.math.Vec3d toTarget = targetCenter.subtract(eyePos);
        double dist = toTarget.length();
        if (dist < 0.01) return true;

        // 3. 10-Block Aim / Crosshair Raycast
        net.minecraft.util.math.Box targetBox = target.getBoundingBox().expand(0.35);
        net.minecraft.util.math.Vec3d aimEndPos = eyePos.add(lookVec.multiply(maxAimDist));
        if (targetBox.raycast(eyePos, aimEndPos).isPresent()) {
            return true;
        }

        net.minecraft.util.math.Vec3d dir = toTarget.normalize();
        double dot = lookVec.dotProduct(dir);

        // Generous crosshair aim angular tolerance within 10 blocks
        if (dist <= maxAimDist && dot > 0.97) {
            return true;
        }

        // 4. Melee Attack Hitbox Cone (covers weapon swing arc and Better Combat sweeps)
        double reach = 4.0;
        try {
            reach = client.player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
            reach = Math.max(reach, 3.5);
        } catch (Throwable ignored) {}

        if (dist <= reach + 1.2) {
            if (dot > 0.70) {
                return true;
            }
            net.minecraft.util.math.Box expandedBox = target.getBoundingBox().expand(0.6);
            net.minecraft.util.math.Vec3d endPos = eyePos.add(lookVec.multiply(reach + 1.2));
            if (expandedBox.raycast(eyePos, endPos).isPresent()) {
                return true;
            }
        }

        return false;
    }

    private static void renderEntityStats(DrawContext context, LivingEntity entity, MinecraftClient client) {
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
            // Mobs: Only show HUD when targeted / in attack hitbox
            boolean isTargeted = isEntityInAttackHitboxOrTargeted(client, entity);
            if (!isTargeted) return;

            MobCategory category = getMobCategory(entity);
            HealthBarDisplayMode mode = switch (category) {
                case HOSTILE -> CranksDungeons.CONFIG.hostileMobHealthBars();
                case NEUTRAL -> CranksDungeons.CONFIG.neutralMobHealthBars();
                case FRIENDLY -> CranksDungeons.CONFIG.friendlyMobHealthBars();
            };

            boolean isDamaged = entity.getHealth() < entity.getMaxHealth() || entity.getAbsorptionAmount() > 0;
            ShieldInfo sInfo = getShieldInfo(entity, client);
            if (sInfo != null && sInfo.current < sInfo.max) {
                isDamaged = true;
            }

            // The Hostile/Neutral/Friendly settings ONLY affect the health bar & energy shield
            showBars = false;
            if (CranksDungeons.CONFIG.showEntityHealthBars() && CranksDungeons.CONFIG.showMobsBars()) {
                if (mode == HealthBarDisplayMode.ALWAYS) {
                    showBars = true;
                } else if (mode == HealthBarDisplayMode.WHEN_DAMAGED) {
                    showBars = isDamaged;
                } else {
                    showBars = false;
                }
            }

            // Name and Level always appear when targeted
            showName = CranksDungeons.CONFIG.showMobsName();
            showLevel = CranksDungeons.CONFIG.showMobsLevel();
            showParty = false;
        }

        if (!showName && !showLevel && !showBars && !showParty) return;

        PlayerInfo info = isPlayer ? PLAYER_INFO_CACHE.get(entity.getId()) : null;
        PlayerInfo mobInfo = !isPlayer && showLevel ? PLAYER_INFO_CACHE.get(entity.getId()) : null;

        List<com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry> activeAilments = AilmentIconRenderer.getAilments(entity.getId());
        boolean hasAilments = activeAilments != null && !activeAilments.isEmpty();

        // Dynamic yOffset: calculate total height based on which elements are active
        float totalHeight = 0;
        if (hasAilments) totalHeight += 9.5f;
        if (showName) totalHeight += 10.0f;
        if (isPlayer && info != null) {
            if (showLevel) totalHeight += 10.0f;
            if (showParty && !info.partyLeaderName.isEmpty()) totalHeight += 10.0f;
        } else if (mobInfo != null) {
            totalHeight += 10.0f;
        }

        if (showBars) {
            ShieldInfo sInfo = getShieldInfo(entity, client);
            if (sInfo != null && sInfo.max > 0) {
                totalHeight += 3.5f;
            }
            totalHeight += 5.0f; // Health bar height + spacing
        }

        float yOffset = -totalHeight;
        TextRenderer textRenderer = client.textRenderer;

        // --- 1. Ailment Icons (Top of stack) ---
        if (hasAilments) {
            int count = activeAilments.size();
            float iconSize = 7.0f;
            float spacing = 8.5f;
            float rowWidth = (count - 1) * spacing + iconSize;
            float startX = -rowWidth / 2f;

            for (int i = 0; i < count; i++) {
                com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry entry = activeAilments.get(i);
                float iconX = startX + i * spacing;

                drawTexture(context, entry.texture(), iconX, yOffset, iconSize, iconSize, 18, 18, 1.0f);

                if (entry.texture().getPath().contains("buildup")) {
                    drawSmallText(context, textRenderer, entry.stacks() + "%", iconX + iconSize / 2f, yOffset + iconSize - 2.5f, 0xFFFFFFFF);
                } else if (entry.stacks() > 1) {
                    drawSmallText(context, textRenderer, "x" + entry.stacks(), iconX + iconSize / 2f, yOffset + iconSize - 2.5f, 0xFFFFFFFF);
                }
            }
            yOffset += 9.5f;
        }

        // --- 2. Entity Name ---
        if (showName) {
            String name = entity.getName().getString();
            int nameWidth = textRenderer.getWidth(name);
            drawText(context, textRenderer, name, -nameWidth / 2f, yOffset, 0xFFFFFFFF);
            yOffset += 10.0f;
        }

        // --- 3. Level & Class / Party ---
        if (isPlayer && info != null) {
            if (showLevel) {
                String topText = "Lv." + info.level + " " + (info.className.isEmpty() ? "Novice" : capitalize(info.className));
                int textWidth = textRenderer.getWidth(topText);
                drawText(context, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFCCCCCC);
                yOffset += 10.0f;
            }

            if (showParty && !info.partyLeaderName.isEmpty()) {
                String partyText = info.partyLeaderName + "'s Party";
                int pWidth = textRenderer.getWidth(partyText);
                drawText(context, textRenderer, partyText, -pWidth / 2f, yOffset, 0xFF55FFFF);
                yOffset += 10.0f;
            }
        } else if (showLevel && mobInfo != null) {
            String topText = "Lv." + mobInfo.level;
            int textWidth = textRenderer.getWidth(topText);
            drawText(context, textRenderer, topText, -textWidth / 2f, yOffset, 0xFFCCCCCC);
            yOffset += 10.0f;
        }

        if (!showBars) return;

        float barWidth = 40;
        float xStart = -barWidth / 2f;

        // --- 4. Shield Bar ---
        ShieldInfo sInfo = getShieldInfo(entity, client);
        if (sInfo != null && sInfo.max > 0) {
            // Client-side interpolation for smooth recharge/damage
            float delta = (System.currentTimeMillis() - sInfo.lastUpdateTime) / 1000f;
            sInfo.lastUpdateTime = System.currentTimeMillis();
            
            if (Math.abs(sInfo.displayed - sInfo.current) > 0.01f) {
                if (sInfo.displayed < sInfo.current) {
                    sInfo.displayed = Math.min(sInfo.current, sInfo.displayed + (sInfo.max * 0.4f) * delta);
                } else {
                    sInfo.displayed = sInfo.current;
                }
            } else {
                sInfo.displayed = sInfo.current;
            }

            float shieldPercent = MathHelper.clamp(sInfo.displayed / sInfo.max, 0, 1);
            float sHeight = 2.66f;
            drawTexture(context, EMPTY_SHIELD_BAR, xStart, yOffset, barWidth, sHeight, 81, 6, 1.0f);
            if (shieldPercent > 0) {
                float fillWidth = barWidth * shieldPercent;
                drawTexture(context, SHIELD_FILL, xStart, yOffset, fillWidth, sHeight, 81, 6, shieldPercent);
            }
            yOffset += 3.5f;
        }

        // --- 5. Health Bar (Bottom of stack) ---
        float maxHealth = entity.getMaxHealth();
        float health = entity.getHealth();
        float absorption = entity.getAbsorptionAmount();
        float hpPercent = MathHelper.clamp(health / maxHealth, 0, 1);
        float barHeight = 4.0f;

        // Background
        drawTexture(context, EMPTY_BAR, xStart, yOffset, barWidth, barHeight, 81, 9, 1.0f);
        
        // HP Fill - Left-Aligned
        if (hpPercent > 0) {
            float fillWidth = barWidth * hpPercent;
            Identifier entityHealthTexture = getEntityHealthFillTexture(entity);
            drawTexture(context, entityHealthTexture, xStart, yOffset, fillWidth, barHeight, 81, 9, hpPercent);
        }

        // Absorption overlay is drawn last so it remains on top of the health fill.
        if (absorption > 0) {
            float absPercent = MathHelper.clamp(absorption / maxHealth, 0, 1);
            float absWidth = barWidth * absPercent;
            drawTexture(context, ABSORPTION_FILL, xStart, yOffset, absWidth, barHeight, 81, 9, absPercent);
        }
    }

    private static void drawSmallText(DrawContext context, TextRenderer textRenderer, String text, float centerX, float y, int color) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, y, 0.0f);
        matrices.scale(0.5f, 0.5f, 1.0f);
        int textWidth = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, -textWidth / 2, 0, color, false);
        matrices.pop();
    }

    private static ShieldInfo getShieldInfo(LivingEntity entity, MinecraftClient client) {
        ShieldInfo cached = ENTITY_SHIELD_CACHE.get(entity.getId());

        // The local player receives an owner-only shield packet, which updates
        // EnergyShieldProvider rather than the tracker cache used by other
        // entities. Read that authoritative local value as a fallback.
        if (entity == client.player && entity instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
            float max = (float) entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
            if (max > 0.0f) {
                float current = MathHelper.clamp(provider.cranksdungeons$getCurrentEnergyShield(), 0.0f, max);
                if (cached == null || Math.abs(cached.current - current) > 0.01f || Math.abs(cached.max - max) > 0.01f) {
                    updateEntityShield(entity.getId(), current, max);
                }
                return ENTITY_SHIELD_CACHE.get(entity.getId());
            }
        }

        return cached;
    }

    private static void drawTexture(DrawContext context, Identifier texture, float x, float y,
                                    float width, float height, int textureWidth, int textureHeight,
                                    float visibleFraction) {
        int drawWidth = Math.max(1, Math.round(width));
        int drawHeight = Math.max(1, Math.round(height));
        int sourceWidth = Math.max(1, Math.round(textureWidth * visibleFraction));
        context.drawTexture(texture, Math.round(x), Math.round(y), drawWidth, drawHeight,
                0.0f, 0.0f, sourceWidth, textureHeight, textureWidth, textureHeight);
    }

    private static void drawText(DrawContext context, TextRenderer textRenderer, String text, float x, float y, int color) {
        context.drawText(textRenderer, text, Math.round(x), Math.round(y), color, false);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
