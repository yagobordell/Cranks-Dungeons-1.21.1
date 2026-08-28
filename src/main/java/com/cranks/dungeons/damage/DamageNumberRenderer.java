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
        if (client.player == null || client.world == null)
            return;

        List<DamageNumber> numbers = DamageNumberManager.getActiveNumbers();
        if (numbers.isEmpty())
            return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        net.minecraft.client.render.Camera camera = client.gameRenderer.getCamera();
        net.minecraft.util.math.Vec3d camPos = camera.getPos();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        float yawRad = (float) Math.toRadians(camera.getYaw() + 180);
        float pitchRad = (float) Math.toRadians(camera.getPitch());

        double fov = Math.toRadians(client.options.getFov().getValue());
        double aspectRatio = (double) screenWidth / (double) screenHeight;
        double halfHeight = Math.tan(fov / 2.0);
        double halfWidth = halfHeight * aspectRatio;

        for (DamageNumber number : numbers) {
            if (number.isExpired())
                continue;

            // Update position to follow entity if it exists
            updateNumberPosition(number, client);

            boolean isSelf = number.victimUuid != null && number.victimUuid.equals(client.player.getUuid());
            int sX, sY;

            // SPECIAL CASE: Smooth UI rendering for local player in first person
            if (isSelf && client.options.getPerspective().isFirstPerson()) {
                float progress = number.getProgress();
                sX = screenWidth / 2 + (number.isHeal ? 60 : -60);

                // Fix: Apply rowIndex stacking to the first-person Y coordinate
                int rowSpacing = (int) ((textRenderer.fontHeight + 2) * 1.5); // Slightly more spacing for UI
                sY = (int) (screenHeight / 2 + 30 - progress * 40 + (number.rowIndex * rowSpacing));
            } else {
                // Standard world-space projection for Mobs and other players
                double worldX = number.position.x + number.offsetX;
                double worldY = number.getCurrentY();
                double worldZ = number.position.z + number.offsetZ;

                double dx = worldX - camX;
                double dy = worldY - camY;
                double dz = worldZ - camZ;

                // Early culling: skip if too far away, unless it's a forced display (direct
                // source)
                double distanceSq = dx * dx + dy * dy + dz * dz;
                if (distanceSq > MAX_RENDER_DISTANCE_SQ && !number.forceDisplay)
                    continue;

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

                if (viewZ >= 0)
                    continue; // behind camera (standard culling)

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

                sX = Math.round(number.lastScreenX);
                sY = Math.round(number.lastScreenY);

                // Final screen-space culling with margin
                if (sX < -SCREEN_MARGIN || sX > screenWidth + SCREEN_MARGIN
                        || sY < -SCREEN_MARGIN || sY > screenHeight + SCREEN_MARGIN) {
                    continue;
                }
            }

            String text = number.formattedDamageText;
            int textWidth = number.formattedDamageWidth;
            int rowSpacing = (int) ((textRenderer.fontHeight + 2) * 1.2);

            float progress = number.getProgress();

            // 1. Calculate Dynamic Scale based on damage magnitude and Crit status
            float baseScale = 1.0f + (float) Math.min(number.damage / 80.0, 0.6); // Scale up based on damage
            if (number.isCritical)
                baseScale *= 1.3f; // Crits are naturally larger
            baseScale = Math.min(baseScale, 2.0f); // HARD CAP per user request

            float scale = baseScale;
            if (progress < 0.1f) {
                scale *= progress / 0.1f; // Scale up at start
            } else if (progress > 0.8f) {
                scale *= 1.0f - (progress - 0.8f) / 0.2f; // Scale down at end
            }

            // 2. Critical "Heartbeat" Pulse
            if (number.isCritical && progress < 0.25f) {
                // Pulse up then back to baseScale
                float pulse = (float) Math.sin(progress * Math.PI / 0.25f) * 0.25f;
                scale += pulse;
            } else if (progress < 0.2f) {
                // Normal hits get a smaller pop
                scale *= 1.15f;
            }

            int sX_final = sX;
            int sY_final = sY;

            // Calculate color with alpha
            int alphaValue = (int) (number.getAlpha() * 255);
            int alpha = alphaValue << 24;
            int colorWithAlpha = (number.color & 0x00FFFFFF) | alpha;

            context.getMatrices().push();
            context.getMatrices().translate((float) sX_final, (float) sY_final, 0);
            context.getMatrices().scale(scale, scale, 1.0f);

            // 4. Main Text render
            context.drawText(
                    textRenderer,
                    text,
                    -textWidth / 2,
                    -textRenderer.fontHeight / 2,
                    colorWithAlpha,
                    true // Keep vanilla shadow for clarity
            );
            context.getMatrices().pop();
        }
    }

    /**
     * Updates the damage number's position to follow its entity smoothly.
     * If the entity despawned, freezes at last known position.
     */
    private static void updateNumberPosition(DamageNumber number, MinecraftClient client) {
        if (number.entityDespawned)
            return;
        if (number.victimUuid == null)
            return;

        Entity entity = client.world.getEntityById(number.victimEntityId);
        if (entity != null && !entity.getUuid().equals(number.victimUuid)) entity = null;

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
