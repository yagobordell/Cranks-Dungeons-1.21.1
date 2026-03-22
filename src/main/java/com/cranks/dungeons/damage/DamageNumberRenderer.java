package com.cranks.dungeons.damage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class DamageNumberRenderer {

    // Smoothing factor: lower = smoother, less jitter
    private static final float SMOOTHING = 0.1f;

    // Maximum render distance squared (32 blocks)
    private static final double MAX_RENDER_DISTANCE_SQ = 32.0 * 32.0;

    // Screen margin for culling
    private static final int SCREEN_MARGIN = 50;

    public static void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        List<DamageNumber> numbers = DamageNumberManager.getActiveNumbers();
        if (numbers.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) cameraEntity = client.player;

        double camX = cameraEntity.getX();
        double camY = cameraEntity.getEyeY();
        double camZ = cameraEntity.getZ();

        float yawRad = (float) Math.toRadians(cameraEntity.getYaw() + 180);
        float pitchRad = (float) Math.toRadians(cameraEntity.getPitch());

        double fov = Math.toRadians(client.options.getFov().getValue());
        double aspectRatio = (double) screenWidth / (double) screenHeight;
        double halfHeight = Math.tan(fov / 2.0);
        double halfWidth = halfHeight * aspectRatio;

        for (DamageNumber number : numbers) {
            if (number.isExpired()) continue;

            // Update position to follow entity if it exists
            updateNumberPosition(number, client);

            double worldX = number.position.x + number.offsetX;
            double worldY = number.getCurrentY();
            double worldZ = number.position.z + number.offsetZ;

            double dx = worldX - camX;
            double dy = worldY - camY;
            double dz = worldZ - camZ;

            // Early culling: skip if too far away, unless it's a forced display (direct source)
            double distanceSq = dx*dx + dy*dy + dz*dz;
            if (distanceSq > MAX_RENDER_DISTANCE_SQ && !number.forceDisplay) continue;

            double cosYaw = Math.cos(yawRad);
            double sinYaw = Math.sin(yawRad);
            double rotX = dx * cosYaw + dz * sinYaw;
            double rotZ = -dx * sinYaw + dz * cosYaw;
            double rotY = dy;

            double cosPitch = Math.cos(pitchRad);
            double sinPitch = Math.sin(pitchRad);
            double viewX = rotX;
            double viewY = rotY * cosPitch - rotZ * sinPitch;
            double viewZ = rotY * sinPitch + rotZ * cosPitch;

            if (viewZ >= 0) continue; // behind camera

            double depth = -viewZ;
            double projDepth = Math.max(depth, 0.5);

            double ndcX = viewX / (halfWidth * projDepth);
            double ndcY = viewY / (halfHeight * projDepth);

            float targetSX = (float) ((ndcX + 1.0) / 2.0 * screenWidth);
            float targetSY = (float) ((1.0 - ndcY) / 2.0 * screenHeight);

            // Screen-space smoothing
            if (Float.isNaN(number.lastScreenX)) {
                number.lastScreenX = targetSX;
                number.lastScreenY = targetSY;
            } else {
                number.lastScreenX = MathHelper.lerp(SMOOTHING, number.lastScreenX, targetSX);
                number.lastScreenY = MathHelper.lerp(SMOOTHING, number.lastScreenY, targetSY);
            }

            int sX = Math.round(number.lastScreenX);
            int sY = Math.round(number.lastScreenY);

            // Special handling for "Self" numbers (very close to camera)
            boolean isSelf = distanceSq < 0.5;
            if (isSelf && client.options.getPerspective().isFirstPerson()) {
                // Shift self-numbers to the side so they don't block the crosshair
                // Damage to the left, Healing to the right (heuristic based on color)
                boolean isHeal = (number.color & 0x00FFFFFF) == 0x55FF55; 
                float progress = number.getProgress();
                sX = screenWidth / 2 + (isHeal ? 60 : -60);
                sY = (int) (screenHeight / 2 + 30 - progress * 40); // Float up from center-ish
            }

            // Cull if off-screen with margin
            if (sX < -SCREEN_MARGIN || sX > screenWidth + SCREEN_MARGIN
                    || sY < -SCREEN_MARGIN || sY > screenHeight + SCREEN_MARGIN) {
                continue;
            }

            String text = number.formattedDamageText;
            int textWidth = number.formattedDamageWidth;
            int rowSpacing = (int)((textRenderer.fontHeight + 2) * 1.2);

            float progress = number.getProgress();
            float scale = 1.0f;
            if (progress < 0.1f) {
                scale = progress / 0.1f; // Scale up
            } else if (progress > 0.8f) {
                scale = 1.0f - (progress - 0.8f) / 0.2f; // Scale down
            }
            
            // Add a slight "bounce" or "pop" at the start
            if (progress < 0.2f) {
                scale *= 1.2f;
            }

            int drawX = sX - (int)(textWidth * scale / 2);
            int drawY = sY - (int)(textRenderer.fontHeight * scale / 2) - number.rowIndex * rowSpacing;

            // Calculate color with alpha
            int alpha = (int) (number.getAlpha() * 255) << 24;
            int colorWithAlpha = (number.color & 0x00FFFFFF) | alpha;

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(sX, (float) (sY - number.rowIndex * rowSpacing));
            context.getMatrices().scale(scale, scale);
            
            context.drawText(
                    textRenderer,
                    text,
                    -textWidth / 2,
                    -textRenderer.fontHeight / 2,
                    colorWithAlpha,
                    true
            );
            context.getMatrices().popMatrix();
        }
    }

    /**
     * Updates the damage number's position to follow its entity smoothly.
     * If the entity despawned, freezes at last known position.
     */
    private static void updateNumberPosition(DamageNumber number, MinecraftClient client) {
        if (number.entityDespawned) return;
        if (number.victimUuid == null) return;

        Entity entity = client.world.getEntity(number.victimUuid);
        
        if (entity != null && entity.isAlive()) {
            // Entity exists - calculate target position with rotation
            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
            
            // Apply entity rotation to hit offset
            Vec3d rotatedOffset = rotateOffsetByEntity(number.hitOffset, entity);
            
            // Target position is entity center + rotated hit offset
            Vec3d targetPos = entityPos.add(rotatedOffset);
            
            // Smoothly interpolate to target position
            number.updatePositionToFollow(targetPos);
        } else {
            // Entity despawned - freeze at last known position
            number.onEntityDespawned();
        }
    }

    /**
     * Rotates the hit offset based on the entity's current yaw rotation
     */
    private static Vec3d rotateOffsetByEntity(Vec3d offset, Entity entity) {
        float yaw = entity.getYaw();
        float yawRad = (float) Math.toRadians(yaw);
        
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        
        // Rotate the offset around Y axis
        double newX = offset.x * cosYaw - offset.z * sinYaw;
        double newZ = offset.x * sinYaw + offset.z * cosYaw;
        
        return new Vec3d(newX, offset.y, newZ);
    }
}
