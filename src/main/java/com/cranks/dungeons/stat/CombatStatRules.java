package com.cranks.dungeons.stat;

/** Shared combat curves used by gameplay, previews and regression tests. */
public final class CombatStatRules {
    public static final double ARMOUR_HIT_FACTOR = 10;
    public static final double MAX_ARMOUR_REDUCTION = 0.90;
    public static final double MAX_EVASION = 0.75;
    public static final double MAX_BLOCK_FRACTION = 0.85;
    public static final double MAX_REGEN_FRACTION = 0.10;
    public static final double MAX_PERCENT_REGEN = 0.05;
    public static final double LEECH_PER_SECOND = 0.10;
    public static final double LEECH_PER_HIT = 0.10;
    public static final double LEECH_POOL = 0.20;

    private CombatStatRules() {}

    public static double armourReduction(double armour, double hit) {
        if (armour <= 0 || hit <= 0) return 0;
        return Math.min(MAX_ARMOUR_REDUCTION, armour / (armour + ARMOUR_HIT_FACTOR * hit));
    }

    public static double evasionChance(double rating, double hit) {
        return rating <= 0 ? 0 : Math.min(MAX_EVASION, rating / (rating + Math.max(1, 8 * hit)));
    }

    public static double blockedDamage(double value, double hit) {
        return Math.min(Math.max(0, value), Math.max(0, hit) * MAX_BLOCK_FRACTION);
    }

    public static double healthRegeneration(double maximum, double flat, double percent, double movingBonus) {
        double base = Math.max(0, flat) + maximum * Math.clamp(percent, 0, MAX_PERCENT_REGEN);
        return Math.min(maximum * MAX_REGEN_FRACTION, base * (1 + Math.max(0, movingBonus)));
    }

    public static double manaRegeneration(double maximum, double flat, double percent, double regeneration, double recovery) {
        return (Math.max(0, flat) + maximum * Math.max(0, percent))
                * (1 + Math.max(0, regeneration)) * (1 + Math.max(0, recovery));
    }

    public static double criticalProtection(double armour, double legacyArmourFactor, double reduction) {
        double armourPart = Math.clamp(armour * legacyArmourFactor, 0, 1);
        return 1 - (1 - armourPart) * (1 - Math.clamp(reduction, 0, 1));
    }

    public static double shieldRechargeDelay(double fasterStart) { return Math.max(1,4/(1+Math.max(0,fasterStart))); }
    public static double shieldRechargeFraction(double increasedRate) { return Math.min(0.5,0.125*(1+Math.max(0,increasedRate))); }
}
