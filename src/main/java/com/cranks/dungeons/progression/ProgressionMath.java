package com.cranks.dungeons.progression;

/** Fixed-point XP keeps small party rewards without rounding every kill up to an XP. */
public final class ProgressionMath {
    public static final long UNIT = 1000000;
    private ProgressionMath() {}
    public static double partyMultiplier(int members) { return Math.min(2, 1 + .2 * Math.max(0, members-1)); }
    public static long boosted(long units, double bonus) {
        if (units <= 0) return 0;
        if (!Double.isFinite(bonus)) bonus = 0;
        return (long)Math.min(100000000L * UNIT, units * Math.clamp(1+bonus, 0, 100));
    }
    public static long[] split(long pool, int members, int rotation) {
        if (members <= 0) return new long[0];
        var result = new long[members];
        java.util.Arrays.fill(result, Math.max(0,pool)/members);
        for (int i=0;i<Math.max(0,pool)%members;i++) result[Math.floorMod(rotation+i,members)]++;
        return result;
    }
    public static long deathPenalty(long xp, int required) { return Math.max(0, xp - required*UNIT*3/10); }
}
