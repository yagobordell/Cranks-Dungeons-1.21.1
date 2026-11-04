package com.cranks.dungeons.mount;

/** Who may ride a tamed mount or open its inventory. */
public enum MountAccessMode {
    OWNER_ONLY("Only You"),
    PARTY("Party"),
    EVERYONE("Everyone");

    private final String displayName;

    MountAccessMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public MountAccessMode cycle(int direction) {
        MountAccessMode[] modes = values();
        return modes[Math.floorMod(ordinal() + direction, modes.length)];
    }

    public static MountAccessMode fromId(String id) {
        if (id == null) return OWNER_ONLY;
        try {
            return valueOf(id.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return OWNER_ONLY;
        }
    }
}
