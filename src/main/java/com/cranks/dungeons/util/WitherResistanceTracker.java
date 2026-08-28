package com.cranks.dungeons.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WitherResistanceTracker {
    private static final Map<UUID, Integer> ENTITY_LEVELS = new HashMap<>();

    private WitherResistanceTracker() {}

    public static int getLevel(UUID entityUuid) {
        return ENTITY_LEVELS.getOrDefault(entityUuid, 0);
    }

    public static void setLevel(UUID entityUuid, int level) {
        if (level <= 0) {
            ENTITY_LEVELS.remove(entityUuid);
        } else {
            ENTITY_LEVELS.put(entityUuid, level);
        }
    }

    public static void clearEntity(UUID entityUuid) {
        ENTITY_LEVELS.remove(entityUuid);
    }

    public static void clearAll() {
        ENTITY_LEVELS.clear();
    }
}
