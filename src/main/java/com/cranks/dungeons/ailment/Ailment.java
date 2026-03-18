package com.cranks.dungeons.ailment;

public class Ailment {
    private final AilmentType type;
    private double remainingDuration;
    private double timeSinceLastTick;
    private final float baseDamage;

    public Ailment(AilmentType type, float baseDamage) {
        this.type = type;
        this.remainingDuration = type.getBaseDuration();
        this.timeSinceLastTick = 0;
        this.baseDamage = baseDamage;
    }

    public Ailment(AilmentType type, double duration, float baseDamage) {
        this.type = type;
        this.remainingDuration = duration;
        this.timeSinceLastTick = 0;
        this.baseDamage = baseDamage;
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
