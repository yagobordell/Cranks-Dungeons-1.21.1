package com.cranks.dungeons.damage;

import net.minecraft.util.math.Vec3d;

public class DamageNumber {
    public final double damage;
    public final int color;
    public final Vec3d position;
    public final long spawnTime;
    public final double offsetX;
    public final double offsetZ;

    public static final long LIFETIME = 1500;
    public static final double FLOAT_HEIGHT = 0.4;

    // NEW: fixed vertical stack row for this number
    public final int rowIndex;

    // client-side, for smoothing in screen space
    public float lastScreenX = Float.NaN;
    public float lastScreenY = Float.NaN;

    public DamageNumber(double damage, int color, Vec3d position, double offsetX, double offsetZ, int rowIndex) {
        this.damage = damage;
        this.color = color;
        this.position = position;
        this.spawnTime = System.currentTimeMillis();
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.rowIndex = rowIndex;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.spawnTime > LIFETIME;
    }

    public float getProgress() {
        return Math.min(1.0f, (System.currentTimeMillis() - this.spawnTime) / (float) LIFETIME);
    }

    public float getAlpha() {
        float progress = getProgress();
        if (progress > 0.7f) {
            return 1.0f - ((progress - 0.7f) / 0.3f);
        }
        return 1.0f;
    }

    public double getCurrentY() {
        float progress = getProgress();
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        return position.y + FLOAT_HEIGHT * eased;
    }
}