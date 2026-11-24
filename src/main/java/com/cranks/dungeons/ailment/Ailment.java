package com.cranks.dungeons.ailment;
import java.util.UUID;
/** One independently timed application; baseDamage is damage per second. */
public final class Ailment {
    private final AilmentType type;
    private double remainingDuration, maxDuration, timeSinceLastTick;
    private float baseDamage, magnitude, storedDamage;
    private UUID sourceUuid;
    private final UUID parentUuid, chainUuid;
    private final int generation;
    private boolean playerSource;
    public Ailment(AilmentType type, float damagePerSecond, UUID source) {
        this(type, type.getBaseDuration(), damagePerSecond, source, null, UUID.randomUUID(), 0, 1);
    }
    public Ailment(AilmentType type, double duration, float damagePerSecond, UUID source, UUID parent, UUID chain, int generation, float magnitude) {
        this.type=type;
        this.remainingDuration=duration;
        this.maxDuration=duration;
        this.baseDamage=damagePerSecond;
        this.sourceUuid=source;
        this.parentUuid=parent;
        this.chainUuid=chain;
        this.generation=generation;
        this.magnitude=magnitude;
    }
    public AilmentType getType() {
        return type;
    }
    public double getRemainingDuration() {
        return remainingDuration;
    }
    public void setRemainingDuration(double value) {
        remainingDuration=value;
    }
    public double getMaxDuration() {
        return maxDuration;
    }
    public void setMaxDuration(double value) {
        maxDuration=value;
    }
    public float getBaseDamage() {
        return baseDamage;
    }
    public void setBaseDamage(float value) {
        baseDamage=value;
    }
    public UUID getSourceUuid() {
        return sourceUuid;
    }
    public void setSourceUuid(UUID value) {
        sourceUuid=value;
    }
    public boolean isPlayerSource() {
        return playerSource;
    }
    public void setPlayerSource(boolean value) {
        playerSource=value;
    }
    public UUID getParentUuid() {
        return parentUuid;
    }
    public UUID getChainUuid() {
        return chainUuid;
    }
    public int getGeneration() {
        return generation;
    }
    public float getMagnitude() {
        return magnitude;
    }
    public void setMagnitude(float value) {
        magnitude=value;
    }
    public float getStoredDamage() {
        return storedDamage;
    }
    public void setStoredDamage(float value) {
        storedDamage=value;
    }
    public void addStoredDamage(float value) {
        storedDamage=Math.min(1_000_000,storedDamage+value);
    }
    public double getTimeSinceLastTick() {
        return timeSinceLastTick;
    }
    public void addTimeSinceLastTick(double value) {
        timeSinceLastTick+=value;
    }
    public void resetTimeSinceLastTick() {
        timeSinceLastTick=0;
    }
    public boolean isExpired() {
        return remainingDuration<=.000001;
    }
    public boolean isReadyToTick() {
        return type.getTickInterval()>0 && timeSinceLastTick+.000001>=type.getTickInterval();
    }
    public int getStacks() {
        return 1;
    }
    /** A final partial interval preserves total damage even for very short applications. */
    public float advance(double seconds, boolean wet) {
        double rate=wet && type==AilmentType.BURN ? 2 : 1;
        double elapsed=Math.min(seconds,Math.max(0,remainingDuration)/rate);
        timeSinceLastTick+=elapsed;
        remainingDuration-=seconds*rate;
        if ((type==AilmentType.BURN || type==AilmentType.POISON) && (isReadyToTick() || isExpired())) {
            float damage=(float)(baseDamage*timeSinceLastTick*(wet && type==AilmentType.BURN?.5:1));
            timeSinceLastTick=0;
            return damage;
        }
        return 0;
    }
}
