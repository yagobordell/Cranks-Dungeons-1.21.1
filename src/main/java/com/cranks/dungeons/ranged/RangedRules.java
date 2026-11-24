package com.cranks.dungeons.ranged;

public final class RangedRules {
    public static final int MIN_BOW_DRAW = 6;
    private RangedRules() {}
    public static double speed(double generic, double bonus) {
        if (!Double.isFinite(generic) || !Double.isFinite(bonus)) return 1;
        return Math.clamp(generic / 4.0 * (1 + bonus), 0.05, 20);
    }
    public static int duration(int baseTicks, double speed) {
        return Math.max(2, (int)Math.ceil(baseTicks / Math.clamp(speed, 0.05, 20)));
    }
    public static float power(float velocity) { return Math.clamp(velocity / 3f, 0, 1); }
}
