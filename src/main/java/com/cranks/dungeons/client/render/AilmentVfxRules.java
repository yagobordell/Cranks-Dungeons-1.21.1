package com.cranks.dungeons.client.render;

/** Bounded presentation policy. Has no effect on damage, duration or application rules. */
public final class AilmentVfxRules {
    public static final int PARTICLES_PER_TICK = 48;
    public static final int AMBIENT_PER_ENTITY = 6;
    public static final int EVENT_PER_ENTITY = 12;
    public static final int RANGE = 32;
    private AilmentVfxRules() {}

    public static boolean due(long tick, int entityId, int interval, int salt) {
        return Math.floorMod(tick + (long)entityId * 13 + salt, interval) == 0;
    }

    public static int buildupInterval(int percentage) {
        return percentage >= 75 ? 12 : percentage >= 35 ? 24 : 40;
    }

    public static float applicationFade(long age) {
        if (age < 0 || age >= 12) return 0;
        return (float)Math.sin(Math.PI * (age + 1) / 13.0);
    }
}
