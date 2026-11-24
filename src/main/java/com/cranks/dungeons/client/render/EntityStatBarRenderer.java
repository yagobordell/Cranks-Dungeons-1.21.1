package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

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

    private static final double MAX_RENDER_DISTANCE_SQ = 32.0 * 32.0;
    private static final int HEAD_CLEARANCE = 4;
    private static final int SCREEN_MARGIN = 64;

    private static final Matrix4f WORLD_TO_CLIP = new Matrix4f();
    private static final Vector4f CLIP_POSITION = new Vector4f();
    private static ClientWorld renderedWorld;
    private static Vec3d cameraPosition = Vec3d.ZERO;

    public static void captureProjection(WorldRenderContext context) {
        renderedWorld = context.world();
        cameraPosition = context.camera().getPos();
        WORLD_TO_CLIP.set(context.projectionMatrix()).mul(context.positionMatrix());
    }

    public static void clearProjection() {
        renderedWorld = null;
    }

    /**
     * Draws projected entity plates in the GUI pass. Keeping the actual draw calls
     * out of the world render pipeline prevents shader packs from replacing their
     * shader, depth test, blend mode, or framebuffer.
     */
    public static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.world != renderedWorld || client.player == null || client.options.hudHidden) return;

        float tickDelta = client.getRenderTickCounter().getTickDelta(true);

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

                Vec3d labelAnchor = living.getAttachments().getPointNullable(
                        EntityAttachmentType.NAME_TAG, 0, living.getYaw(tickDelta));
                if (labelAnchor == null) labelAnchor = new Vec3d(0, living.getHeight(), 0);
                double worldX = MathHelper.lerp(tickDelta, living.lastRenderX, living.getX()) + labelAnchor.x;
                double worldY = MathHelper.lerp(tickDelta, living.lastRenderY, living.getY()) + labelAnchor.y + 0.28;
                double worldZ = MathHelper.lerp(tickDelta, living.lastRenderZ, living.getZ()) + labelAnchor.z;
                ScreenPosition screenPosition = projectToScreen(client, worldX, worldY, worldZ);
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
            matrices.translate(projected.position.x, projected.position.y - HEAD_CLEARANCE, 0.0f);
            renderEntityStats(context, projected.entity, client);
            matrices.pop();
        }
    }

    public static ScreenPosition projectToScreen(MinecraftClient client, double worldX, double worldY, double worldZ) {
        if (client.world != renderedWorld) return null;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        if (screenWidth <= 0 || screenHeight <= 0) return null;

        WORLD_TO_CLIP.transform((float) (worldX - cameraPosition.x), (float) (worldY - cameraPosition.y),
                (float) (worldZ - cameraPosition.z), 1, CLIP_POSITION);
        float depth = CLIP_POSITION.w;
        if (depth <= 0.05f) return null;

        float screenX = (CLIP_POSITION.x / depth + 1) * screenWidth / 2;
        float screenY = (1 - CLIP_POSITION.y / depth) * screenHeight / 2;
        if (screenX < -SCREEN_MARGIN || screenX > screenWidth + SCREEN_MARGIN
                || screenY < -SCREEN_MARGIN || screenY > screenHeight + SCREEN_MARGIN) {
            return null;
        }

        return new ScreenPosition(screenX, screenY, depth);
    }

    public record ScreenPosition(float x, float y, float depth) {}

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

        ShieldInfo shield = showBars ? getShieldInfo(entity, client) : null;
        boolean showShield = shield != null && shield.max > 0;
        String name = showName ? entity.getName().getString() : null;
        String level = null;
        if (showLevel && info != null) {
            level = "Lv." + info.level + " " + (info.className.isEmpty() ? "Novice" : capitalize(info.className));
        } else if (mobInfo != null) {
            level = "Lv." + mobInfo.level;
        }
        String party = showParty && info != null && !info.partyLeaderName.isEmpty() ? info.partyLeaderName + "'s Party" : null;
        String defense = !isPlayer && showBars && getMobCategory(entity) == MobCategory.HOSTILE
                ? defenseLabel(entity) : null;

        TextRenderer font = client.textRenderer;
        List<PlateLine> lines = new ArrayList<>(4);
        addPlateLine(lines, font, name, 1, 0xFFF2F2F2);
        addPlateLine(lines, font, level, 0.82f, 0xFFC9C9C9);
        addPlateLine(lines, font, party, 0.82f, 0xFF99C0BF);
        addPlateLine(lines, font, defense, 0.82f, 0xFFE1C697);

        int textHeight = 0;
        int width = showBars ? 52 : 0;
        for (PlateLine line : lines) {
            width = Math.max(width, MathHelper.ceil(font.getWidth(line.text()) * line.scale()) + 10);
            textHeight += line.height();
        }
        int separatorGap = showBars && textHeight > 0 ? 2 : 0;
        int height = 6 + textHeight + separatorGap + (showBars ? 6 : 0) + (showShield ? 5 : 0);
        int y = -height;

        int ailmentHeight=hasAilments?ailmentRowHeight(activeAilments):0;
        if (hasAilments) drawAilments(context, font, activeAilments, entity.getId(), y, ailmentHeight);
        if (AilmentIconRenderer.resistedTicks(entity)>0) {
            String feedback=net.minecraft.text.Text.translatable("hud.cranks-dungeons.ailment.resisted").getString();
            int feedbackWidth=MathHelper.ceil(font.getWidth(feedback)*.7f)+8;
            float feedbackX=-feedbackWidth/2f;
            int feedbackY=y-(hasAilments?ailmentHeight+3:0)-14;
            HudStyle.plate(context,MathHelper.floor(feedbackX),feedbackY,feedbackWidth,11,true);
            HudStyle.drawLabel(context,font,feedback,0,feedbackY+5.5f,.7f,0xFFD8D8D8);
        }
        if (width == 0) return;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(-width / 2f, y, 0);
        HudStyle.plate(context, 0, 0, width, height, false);
        int rowY = 3;
        for (PlateLine line : lines) {
            HudStyle.drawLabel(context, font, line.text(), width / 2f, rowY + line.height() / 2f,
                    line.scale(), line.color());
            rowY += line.height();
        }
        if (separatorGap > 0) {
            HudStyle.divider(context, 4, width - 4, rowY);
            rowY += separatorGap;
        }

        if (showShield) {
            long now = System.currentTimeMillis();
            float delta = (now - shield.lastUpdateTime) / 1000f;
            shield.lastUpdateTime = now;
            if (shield.displayed + 0.01f < shield.current) {
                shield.displayed = Math.min(shield.current, shield.displayed + shield.max * 0.4f * delta);
            } else {
                shield.displayed = shield.current;
            }
            HudStyle.meter(context, 4, rowY, width - 8, 4, HudStyle.Ink.SHIELD,
                    StatBarHudRenderer.ratio(shield.displayed, shield.max));
            rowY += 5;
        }
        if (showBars) {
            StatBarHudRenderer.drawHealthMeter(context, 4, rowY, width - 8, 5, entity,
                    entity.getHealth(), entity.getMaxHealth(), entity.getAbsorptionAmount(), entity.getHealth());
        }
        matrices.pop();
    }

    private static void addPlateLine(List<PlateLine> lines, TextRenderer font, String text, float scale, int color) {
        if (text == null || text.isEmpty()) return;
        int maxWidth = MathHelper.floor(100 / scale);
        if (font.getWidth(text) > maxWidth) {
            text = font.trimToWidth(text, maxWidth - font.getWidth("â€¦")) + "â€¦";
        }
        lines.add(new PlateLine(text, scale, color, MathHelper.ceil(font.fontHeight * scale)));
    }

    private record PlateLine(String text, float scale, int color, int height) {}

    private static int ailmentRowHeight(List<com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry> ailments) {
        return ailments.stream().mapToInt(a -> AilmentPresentation.tileHeight(a.buildup(),a.stacks(),a.remainingSeconds()))
                .max().orElse(0);
    }

    private static void drawAilments(DrawContext context, TextRenderer font,
                                    List<com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry> ailments,
                                    int entityId, int y, int rowHeight) {
        int tileSize = AilmentPresentation.ICON_SIZE;
        int spacing = tileSize + 2;
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(-((ailments.size() - 1) * spacing + tileSize) / 2f, y - rowHeight - 3, 0);
        for (int i = 0; i < ailments.size(); i++) {
            var entry = ailments.get(i);
            int x = i * spacing;
            boolean counter = entry.buildup() || entry.stacks() > 1;
            HudStyle.plate(context, x, 0, tileSize,
                    AilmentPresentation.tileHeight(entry.buildup(),entry.stacks(),entry.remainingSeconds()), true);
            if(entry.buildup())context.setShaderColor(.48f,.48f,.48f,1);
            try {
                context.drawTexture(entry.texture(), x + 2, 2, 12, 12, 0.0f, 0.0f, 18, 18, 18, 18);
            } finally {
                if(entry.buildup())context.setShaderColor(1,1,1,1);
            }
            int footerY=tileSize+4;
            if (counter) {
                String label = entry.buildup() ? entry.stacks() + "%" : "x" + entry.stacks();
                float scale = Math.min(0.6f, (tileSize - 4f) / font.getWidth(label));
                HudStyle.drawLabel(context, font, label, x + tileSize / 2f, footerY, scale,
                        entry.buildup()?0xFFC2C2C2:0xFFFFFFFF);
                footerY+=AilmentPresentation.FOOTER_HEIGHT;
            }
            if (AilmentPresentation.hasTimer(entry.buildup(),entry.remainingSeconds())) {
                String timer=AilmentPresentation.timer(AilmentIconRenderer.remainingSeconds(entityId,entry));
                float scale=Math.min(.6f,(tileSize-4f)/font.getWidth(timer));
                HudStyle.drawLabel(context,font,timer,x+tileSize/2f,footerY,scale,0xFFC9C9C9);
            }
        }
        matrices.pop();
    }

    private static String defenseLabel(LivingEntity entity) {
        if (entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD) > 0) return "Shielded";
        if (entity.getArmor() >= 75) return "Armored";
        if (entity.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.EVASION_RATING) >= 30) return "Evasive";
        return null;
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

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
