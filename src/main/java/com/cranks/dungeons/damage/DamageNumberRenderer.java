package com.cranks.dungeons.damage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class DamageNumberRenderer {

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
        float pitchRad = (float) Math.toRadians(cameraEntity.getPitch()); // no minus

        double fov = Math.toRadians(client.options.getFov().getValue());
        double aspectRatio = (double) screenWidth / (double) screenHeight;
        double halfHeight = Math.tan(fov / 2.0);
        double halfWidth = halfHeight * aspectRatio;

        // Track already-used screen positions this frame to stack overlapping numbers
        List<int[]> usedSpots = new ArrayList<>();

        for (DamageNumber number : numbers) {
            if (number.isExpired()) continue;

            double worldX = number.position.x + number.offsetX;
            double worldY = number.getCurrentY();
            double worldZ = number.position.z + number.offsetZ;

            double dx = worldX - camX;
            double dy = worldY - camY;
            double dz = worldZ - camZ;

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

            if (viewZ >= 0) {
                continue; // behind camera
            }

            double depth = -viewZ;

            // Avoid crazy projection when extremely close
            double projDepth = Math.max(depth, 0.5);

            double ndcX = viewX / (halfWidth * projDepth);
            double ndcY = viewY / (halfHeight * projDepth);

            // Target screen position
            float targetSX = (float) ((ndcX + 1.0) / 2.0 * screenWidth);
            float targetSY = (float) ((1.0 - ndcY) / 2.0 * screenHeight);

            // Screen-space smoothing to reduce shake
            float lerpFactor = 0.6f; // 0..1; lower = smoother
            if (Float.isNaN(number.lastScreenX)) {
                number.lastScreenX = targetSX;
                number.lastScreenY = targetSY;
            } else {
                number.lastScreenX += (targetSX - number.lastScreenX) * lerpFactor;
                number.lastScreenY += (targetSY - number.lastScreenY) * lerpFactor;
            }

            int sX = Math.round(number.lastScreenX);
            int sY = Math.round(number.lastScreenY);

            if (sX < -50 || sX > screenWidth + 50
                    || sY < -50 || sY > screenHeight + 50) {
                continue;
            }

            String text = formatDamage(number.damage);
            float alpha = number.getAlpha();
            int alphaInt = (int) (alpha * 255) & 0xFF;
            int colorWithAlpha = (alphaInt << 24) | (number.color & 0x00FFFFFF);

            int textWidth = textRenderer.getWidth(text);
            int rowSpacing = textRenderer.fontHeight + 2;

            // Compute stack index: how many numbers are already near this screen position
            int stackIndex = 0;
            for (int[] spot : usedSpots) {
                int prevX = spot[0];
                int prevY = spot[1];
                if (Math.abs(prevX - sX) < textWidth   // roughly same horizontal area
                        && Math.abs(prevY - sY) < rowSpacing) { // roughly same vertical row
                    stackIndex++;
                }
            }
            usedSpots.add(new int[]{sX, sY});

            int drawX = sX - textWidth / 2;
            // Stack upwards: each additional number goes one row higher
            int drawY = sY - textRenderer.fontHeight / 2 - stackIndex * rowSpacing;

            context.drawText(
                    textRenderer,
                    text,
                    drawX,
                    drawY,
                    colorWithAlpha,
                    true
            );
        }
    }

    private static String formatDamage(double damage) {
        if (damage >= 1000) {
            return String.format("%.1fk", damage / 1000.0);
        }
        if (damage < 10) {
            return String.format("%.1f", damage);
        }
        return String.format("%.0f", damage);
    }
}