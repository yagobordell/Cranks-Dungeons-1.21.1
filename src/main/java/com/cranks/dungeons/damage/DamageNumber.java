package com.cranks.dungeons.damage;

import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class DamageNumber {
    public final double damage;
    public final int color;
    public Vec3d position;  // Now mutable for entity following
    public final long spawnTime;
    public final double offsetX;
    public final double offsetZ;

    public final String formattedDamageText;
    public final int formattedDamageWidth;

    public static final long LIFETIME = 1500;
    public static final double FLOAT_HEIGHT = 0.4;
    private static final double LERP_FACTOR = 0.2; // 20% per frame for smooth following

    // NEW: Entity tracking fields
    public final int victimEntityId;
    public final UUID victimUuid;
    public final Vec3d hitOffset;  // Offset from entity center where hit occurred
    public Vec3d lastKnownPosition;
    public boolean entityDespawned;

    // NEW: fixed vertical stack row for this number
    public int rowIndex;

    public void setRowIndex(int index) {
        this.rowIndex = index;
    }

    // client-side, for smoothing in screen space
    public float lastScreenX = Float.NaN;
    public float lastScreenY = Float.NaN;

    public final boolean forceDisplay;
    public final DamageElement element;
    public final boolean isCritical;
    public final boolean isHeal;

    public DamageNumber(double damage, int color, Vec3d position, double offsetX, double offsetZ, 
                       int rowIndex, String formattedDamageText, int formattedDamageWidth, 
                       boolean forceDisplay, int victimEntityId, UUID victimUuid, Vec3d hitOffset,
                       DamageElement element, boolean isCritical, boolean isHeal, long spawnTime) {
        this.damage = damage;
        this.color = color;
        this.position = position;
        this.spawnTime = spawnTime;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.rowIndex = rowIndex;
        this.formattedDamageText = formattedDamageText;
        this.formattedDamageWidth = formattedDamageWidth;
        this.forceDisplay = forceDisplay;
        this.victimEntityId = victimEntityId;
        this.victimUuid = victimUuid;
        this.hitOffset = hitOffset;
        this.lastKnownPosition = position;
        this.entityDespawned = false;
        this.element = element;
        this.isCritical = isCritical;
        this.isHeal = isHeal;
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

        // Apply vertical stacking for multi-element hits
        // Each index moves the number down by 0.45 meters so they don't overlap
        double stackOffset = rowIndex * -0.45;

        return position.y + yOffset + stackOffset;
    }

    /**
     * Smoothly updates position to follow an entity
     * @param targetPosition The entity's current position plus hit offset
     */
    public void updatePositionToFollow(Vec3d targetPosition) {
        if (entityDespawned) return;
        
        // Smooth lerp to target position
        double newX = lerp(position.x, targetPosition.x, LERP_FACTOR);
        double newY = lerp(position.y, targetPosition.y, LERP_FACTOR);
        double newZ = lerp(position.z, targetPosition.z, LERP_FACTOR);
        
        position = new Vec3d(newX, newY, newZ);
        lastKnownPosition = position;
    }

    /**
     * Call when entity despawns - freezes at last known position
     */
    public void onEntityDespawned() {
        entityDespawned = true;
        position = lastKnownPosition;
    }

    private double lerp(double start, double end, double factor) {
        return start + (end - start) * factor;
    }
}
