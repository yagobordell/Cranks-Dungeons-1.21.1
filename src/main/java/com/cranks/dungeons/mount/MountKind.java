package com.cranks.dungeons.mount;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;

/** Gameplay identity and cargo limits for every supported rideable animal. */
public enum MountKind {
    HORSE("Horse", 6),
    DONKEY("Donkey", 33),
    MULE("Mule", 24),
    CAMEL("Camel", 12),
    STRIDER("Strider", 6),
    SKELETON_HORSE("Skeleton Horse", 6),
    PIG("Pig", 3),
    UNSUPPORTED("Mount", 0);

    private final String displayName;
    private final int maximumCargoSlots;

    MountKind(String displayName, int maximumCargoSlots) {
        this.displayName = displayName;
        this.maximumCargoSlots = maximumCargoSlots;
    }

    public String displayName() {
        return displayName;
    }

    public int maximumCargoSlots() {
        return maximumCargoSlots;
    }

    public boolean hasGenetics() {
        return this == DONKEY || this == MULE || this == CAMEL || this == STRIDER;
    }

    public static MountKind of(Entity entity) {
        if (entity instanceof HorseEntity) return HORSE;
        if (entity instanceof DonkeyEntity) return DONKEY;
        if (entity instanceof MuleEntity) return MULE;
        if (entity instanceof CamelEntity) return CAMEL;
        if (entity instanceof StriderEntity) return STRIDER;
        if (entity instanceof SkeletonHorseEntity) return SKELETON_HORSE;
        if (entity instanceof PigEntity) return PIG;
        return UNSUPPORTED;
    }
}
