package com.cranks.dungeons.mount;

/** Rider percentages use fractions: 0.1 means 10%. Balance targets are not hard caps. */
public final class RidingRules {
    private RidingRules() {}

    public static double increase(double base, double bonus) {
        if (!Double.isFinite(base) || base <= 0) return 0;
        if (!Double.isFinite(bonus)) return base;
        return Math.min(Double.MAX_VALUE, base * Math.max(0, 1 + bonus));
    }

    public static double reduce(double base, double reduction) {
        if (!Double.isFinite(reduction)) return Math.max(0, base);
        return increase(base, -Math.min(1, reduction));
    }
}
