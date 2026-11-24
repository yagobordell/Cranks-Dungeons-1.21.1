package com.cranks.dungeons.ailment;
/** Shared combat arithmetic. Values are fractions, not percentage points. */
public final class AilmentRules {
    public static final int BASE_POISON_CAP = 20;
    public static final int MAX_POISON_CAP = 120;
    public static final double CONTROL_RECOVERY_SECONDS = 1.0;
    public static final float FIRE_BUILDUP_PER_HEALTH_FRACTION = 4f;
    private AilmentRules() {
    }
    public static double bounded(double value, double min, double max) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : min;
    }
    public static double penetratedResistance(double resistance, double penetration) {
        double res = bounded(resistance, -1, .75);
        return res > 0 ? res * (1 - bounded(penetration, 0, 1)) : res;
    }
    public static double criticalChance(double base, double local, double global, double conditional) {
        return bounded(base * (1 + local) * (1 + global) + conditional, 0, 1);
    }
    public static double bleedFraction(double increasedMagnitude) {
        return bounded(.4 * (1 + increasedMagnitude), 0, 1);
    }
    public static double chillSlow(double buildup, double magnitude) {
        return bounded(buildup * Math.max(1, magnitude), 0, .9);
    }
}
