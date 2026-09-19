package com.cranks.dungeons.horse;

import java.util.Locale;

public enum HorseTrait {
    NONE("No Trait"),
    SWIFT("Swift"),
    HARDY("Hardy"),
    SURE_FOOTED("Sure-Footed"),
    WARHORSE("Warhorse");

    private final String displayName;

    HorseTrait(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static HorseTrait fromId(String id) {
        if (id == null || id.isBlank()) return NONE;
        try {
            return valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
