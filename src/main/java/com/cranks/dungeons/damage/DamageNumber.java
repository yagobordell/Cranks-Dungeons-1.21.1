package com.cranks.dungeons.damage;

import net.minecraft.util.math.Vec3d;

public class DamageNumber {
    public final double damage;
    public final int color;
    public final Vec3d position;
    public final long spawnTime;
    public final double offsetX;
    public final double offsetZ;

    public final String formattedDamageText;
    public final int formattedDamageWidth;

    public static final long LIFETIME = 1500;
    public static final double FLOAT_HEIGHT = 0.4;

    // NEW: fixed vertical stack row for this number
    public int rowIndex;

    public void setRowIndex(int index) {
        this.rowIndex = index;
    }

    // client-side, for smoothing in screen space
    public float lastScreenX = Float.NaN;
    public float lastScreenY = Float.NaN;

    public final boolean forceDisplay;

    public DamageNumber(double damage, int color, Vec3d position, double offsetX, double offsetZ, int rowIndex, String formattedDamageText, int formattedDamageWidth, boolean forceDisplay) {
        this.damage = damage;
        this.color = color;
        this.position = position;
        this.spawnTime = System.currentTimeMillis();
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.rowIndex = rowIndex;
        this.formattedDamageText = formattedDamageText;
        this.formattedDamageWidth = formattedDamageWidth;
        this.forceDisplay = forceDisplay;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.spawnTime > LIFETIME;
    }

    public float getProgress() {
        return Math.min(1.0f, (System.currentTimeMillis() - this.spawnTime) / (float) LIFETIME);
    }

    public float getAlpha() {
        float progress = getProgress();
        if (progress < 0.1f) {
            return progress / 0.1f; // Fade in
        }
        if (progress > 0.8f) {
            return 1.0f - ((progress - 0.8f) / 0.2f); // Fade out
        }
        return 1.0f;
    }

    public double getCurrentY() {
        float progress = getProgress();
        // Burst up at the start (0.0 to 0.2), then slow float
        double yOffset;
        if (progress < 0.2f) {
            float burstProgress = progress / 0.2f;
            yOffset = burstProgress * 0.2; // Initial burst
        } else {
            yOffset = 0.2 + (progress - 0.2f) * 0.3; // Slower float after burst
        }
        return position.y + yOffset;
    }
}