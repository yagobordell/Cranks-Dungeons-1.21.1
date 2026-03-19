package com.cranks.dungeons.ailment;

import java.util.UUID;

public class Ailment {
    private final AilmentType type;
    private double remainingDuration;
    private double timeSinceLastTick;
    private final float baseDamage;
    private final UUID sourceUuid;
    private final UUID parentUuid;
    private final UUID chainUuid;
    private final int generation;

    public Ailment(AilmentType type, float baseDamage, UUID sourceUuid) {
        this(type, type.getBaseDuration(), baseDamage, sourceUuid, null, UUID.randomUUID(), 0);
    }

    public Ailment(AilmentType type, double duration, float baseDamage, UUID sourceUuid, UUID parentUuid, UUID chainUuid, int generation) {
        this.type = type;
        this.remainingDuration = duration;
        this.timeSinceLastTick = 0;
        this.baseDamage = baseDamage;
        this.sourceUuid = sourceUuid;
        this.parentUuid = parentUuid;
        this.chainUuid = chainUuid;
        this.generation = generation;
    }

    public UUID getChainUuid() {
        return chainUuid;
    }

    public int getGeneration() {
        return generation;
    }

    public UUID getParentUuid() {
        return parentUuid;
    }

    public AilmentType getType() {
        return type;
    }

    public double getRemainingDuration() {
        return remainingDuration;
    }

    public void setRemainingDuration(double duration) {
        this.remainingDuration = duration;
    }

    public double getTimeSinceLastTick() {
        return timeSinceLastTick;
    }

    public void addTimeSinceLastTick(double delta) {
        this.timeSinceLastTick += delta;
    }

    public void resetTimeSinceLastTick() {
        this.timeSinceLastTick = 0;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public UUID getSourceUuid() {
        return sourceUuid;
    }

    public boolean isReadyToTick() {
        return type.getTickInterval() > 0 && timeSinceLastTick >= type.getTickInterval();
    }

    public boolean isExpired() {
        return remainingDuration <= 0;
    }

    public float getDamagePerTick(float damagePercent) {
        return baseDamage * (damagePercent / 100.0f);
    }
}
