package com.cranks.dungeons.ailment;

import java.util.UUID;

public class Ailment {
    private final AilmentType type;
    private double remainingDuration;
    private double timeSinceLastTick;
    private float baseDamage;
    private final UUID sourceUuid;
    private final UUID parentUuid;
    private final UUID chainUuid;
    private final int generation;
    private int stacks;
    private float magnitude;
    private double maxDuration; // Store original duration for reset
    private float damagePerStack; // Track damage contribution per stack
    private float storedDamage; // For BLEED: stores accumulated damage

    public Ailment(AilmentType type, float baseDamage, UUID sourceUuid) {
        this(type, type.getBaseDuration(), baseDamage, sourceUuid, null, UUID.randomUUID(), 0, 1.0f);
    }

    public Ailment(AilmentType type, double duration, float baseDamage, UUID sourceUuid, UUID parentUuid, UUID chainUuid, int generation, float magnitude) {
        this.type = type;
        this.remainingDuration = duration;
        this.maxDuration = duration;
        this.timeSinceLastTick = 0;
        this.baseDamage = baseDamage;
        this.damagePerStack = baseDamage;
        this.sourceUuid = sourceUuid;
        this.parentUuid = parentUuid;
        this.chainUuid = chainUuid;
        this.generation = generation;
        this.stacks = 1;
        this.magnitude = magnitude;
        this.storedDamage = 0.0f;
    }
    
    public float getMagnitude() {
        return magnitude;
    }
    
    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
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
        // Return base damage per tick (stacks are handled separately for POISON escalation)
        return baseDamage * damagePercent;
    }
    
    public float getDamagePerTickWithStacks(float damagePercent) {
        // For POISON: stacks multiply the damage directly
        return getDamagePerTick(damagePercent) * stacks;
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

    public void addStack() {
        this.stacks++;
    }

    public void addStack(double duration, float damage) {
        // Increment stack count first
        this.stacks++;
        // Average the damage per stack when adding a new stack
        float totalDamage = this.damagePerStack * (this.stacks - 1) + damage;
        this.damagePerStack = totalDamage / this.stacks;
        // Use the longer duration
        this.maxDuration = Math.max(this.maxDuration, duration);
        this.remainingDuration = this.maxDuration;
    }

    public void removeStack() {
        if (this.stacks > 0) {
            this.stacks--;
            // Reset timer to max duration
            this.remainingDuration = this.maxDuration;
        }
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(double duration) {
        this.maxDuration = duration;
    }

    public float getDamagePerStack() {
        return damagePerStack;
    }

    public void setDamagePerStack(float damage) {
        this.damagePerStack = damage;
    }

    public float getStoredDamage() {
        return storedDamage;
    }

    public void setStoredDamage(float damage) {
        this.storedDamage = damage;
    }

    public void addStoredDamage(float damage) {
        this.storedDamage += damage;
    }
}
