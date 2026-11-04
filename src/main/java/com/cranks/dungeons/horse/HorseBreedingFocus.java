package com.cranks.dungeons.horse;

import java.util.Locale;

public enum HorseBreedingFocus {
    NONE("None"),
    HEALTH("Health"),
    SPEED("Speed"),
    JUMP("Jump"),
    EVASION("Evasion"),
    ENDURANCE("Endurance"),
    ARMOR("Armor");

    private final String displayName;

    HorseBreedingFocus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static HorseBreedingFocus fromId(String id) {
        if (id == null || id.isBlank()) return NONE;
        try {
            return valueOf(id.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
