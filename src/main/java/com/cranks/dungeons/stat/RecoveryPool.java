package com.cranks.dungeons.stat;

/** Bounded recovery owed to one resource. No per-hit allocation or discarded excess healing. */
public final class RecoveryPool {
    private double pending;
    private double instantBudget;
    private long budgetTick = Long.MIN_VALUE;

    public void offer(double amount, double maximum) {
        if (!Double.isFinite(amount) || amount <= 0 || maximum <= 0) return;
        pending = Math.min(maximum * CombatStatRules.LEECH_POOL,
                pending + Math.min(amount, maximum * CombatStatRules.LEECH_PER_HIT));
    }

    public double drain(double maximum) {
        pending = Math.min(pending, maximum * CombatStatRules.LEECH_POOL);
        double recovered = Math.min(pending, maximum * CombatStatRules.LEECH_PER_SECOND / 20);
        pending -= recovered;
        return recovered;
    }

    public double drainInstant(double maximum, long tick) {
        double cap = maximum * CombatStatRules.LEECH_PER_SECOND;
        if (budgetTick == Long.MIN_VALUE) instantBudget = cap;
        else instantBudget = Math.min(cap, instantBudget + Math.max(0, tick - budgetTick) * cap / 20);
        budgetTick = tick;
        double recovered = Math.min(pending, Math.min(cap, instantBudget));
        pending = 0;
        instantBudget = Math.max(0, instantBudget - recovered);
        return recovered;
    }

    public boolean empty() { return pending <= 0.000001; }
}
