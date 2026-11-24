package com.cranks.dungeons.damage;

/** Marks gradual recovery without changing the healing calculation. */
public final class HealingNumberContext {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private HealingNumberContext() {}
    public static boolean gradual() { return DEPTH.get() > 0; }
    public static void gradual(Runnable action) {
        int previous = DEPTH.get();
        DEPTH.set(previous + 1);
        try { action.run(); }
        finally { if (previous == 0) DEPTH.remove(); else DEPTH.set(previous); }
    }
}
