package com.cranks.dungeons.ailment;

public enum AilmentType {
    BURN("burn", 4.0, 0.5),
    BLEED("bleed", 5.0, 0.5),
    POISON("poison", 2.0, 0.5),
    SHOCK("shock", 10.0, 0.0),
    FREEZE("freeze", 4.0, 0.5);

    private final String id;
    private final double baseDuration;
    private final double tickInterval;

    AilmentType(String id, double baseDuration, double tickInterval) {
        this.id = id;
        this.baseDuration = baseDuration;
        this.tickInterval = tickInterval;
    }

    public String getId() {
        return id;
    }

    public double getBaseDuration() {
        return baseDuration;
    }

    public double getTickInterval() {
        return tickInterval;
    }

    public static AilmentType fromId(String id) {
        for (AilmentType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    public boolean dealsDamage() {
        return this == BURN || this == BLEED || this == POISON;
    }
}
