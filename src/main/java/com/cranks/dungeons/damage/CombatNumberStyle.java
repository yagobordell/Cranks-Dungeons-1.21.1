package com.cranks.dungeons.damage;

import java.util.Locale;

/** Pure presentation rules shared by the renderer and regression checks. */
public final class CombatNumberStyle {
    private CombatNumberStyle() {}

    public static boolean valid(double amount) { return Double.isFinite(amount) && amount > 0; }

    public static float scale(double amount, boolean critical, boolean periodic) {
        double weight = Math.log10(1 + Math.max(0, amount));
        return (float)Math.min(critical ? 1.9 : 1.7,
                (periodic ? 0.95 : 1.10) + weight * 0.16 + (critical ? 0.23 : 0));
    }

    public static long lifetime(double amount, boolean critical, boolean periodic) {
        return periodic ? 1150 : 1400 + (critical ? 250 : 0) + (amount >= 100 ? 150 : 0);
    }

    public static float alpha(long age, long lifetime) {
        if (age < 0 || age >= lifetime) return 0;
        return age > lifetime - 280 ? (lifetime - age) / 280f : 1;
    }

    public static float entrance(long age, boolean critical) {
        double t = Math.max(0, Math.min(1, age / 160.0));
        return (float)(1 + (critical ? 0.23 : 0.10) * Math.sin(t * Math.PI) * (1 - t));
    }

    public static String format(double amount) {
        if (!valid(amount)) return "0";
        if (amount < 0.1) return "<0.1";
        if (amount >= 1_000_000_000_000.0) return String.format(Locale.ROOT, "%.1e", amount).replace(".0e", "e").replace("e+", "e");
        double divisor = amount >= 1_000_000_000 ? 1_000_000_000 : amount >= 1_000_000 ? 1_000_000 : amount >= 10_000 ? 1000 : 1;
        String suffix = divisor == 1_000_000_000 ? "b" : divisor == 1_000_000 ? "m" : divisor == 1000 ? "k" : "";
        double value = amount / divisor;
        return (Math.abs(value - Math.rint(value)) < 0.05 ? String.format(Locale.ROOT, "%.0f", value)
                : String.format(Locale.ROOT, "%.1f", value)) + suffix;
    }
}
