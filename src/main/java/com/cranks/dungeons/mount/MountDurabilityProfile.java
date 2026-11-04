package com.cranks.dungeons.mount;

/** Baseline durability for mounts intended to be used as a primary travel mount. */
public record MountDurabilityProfile(double baseHealth, double baseArmor) {
    public static final int HEALTH_PER_LEVEL = 10;
    // The mod scales each iron armor piece by 8, so a full 15-point vanilla iron set is 120 armor.
    private static final MountDurabilityProfile HORSE = new MountDurabilityProfile(100.0, 120.0);
    private static final MountDurabilityProfile DONKEY = new MountDurabilityProfile(115.0, 132.0);
    private static final MountDurabilityProfile MULE = new MountDurabilityProfile(130.0, 150.0);
    private static final MountDurabilityProfile CAMEL = new MountDurabilityProfile(160.0, 180.0);
    private static final MountDurabilityProfile STRIDER = new MountDurabilityProfile(100.0, 120.0);
    private static final MountDurabilityProfile LLAMA = new MountDurabilityProfile(100.0, 120.0);
    private static final MountDurabilityProfile SKELETON_HORSE = new MountDurabilityProfile(250.0, 350.0);

    public static MountDurabilityProfile forKind(MountKind kind) {
        return switch (kind) {
            case HORSE -> HORSE;
            case DONKEY -> DONKEY;
            case MULE -> MULE;
            case CAMEL -> CAMEL;
            case STRIDER -> STRIDER;
            case LLAMA -> LLAMA;
            case SKELETON_HORSE -> SKELETON_HORSE;
            case PIG, UNSUPPORTED -> null;
        };
    }
}
