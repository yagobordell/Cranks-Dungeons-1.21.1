package com.cranks.dungeons.util;

public class CriticalHitState {
    private static final ThreadLocal<Boolean> isCriticalHit = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Float> critMultiplier = ThreadLocal.withInitial(() -> 1.0f);

    public static void setCriticalHit(boolean value) {
        isCriticalHit.set(value);
    }

    public static boolean isCriticalHit() {
        return isCriticalHit.get();
    }

    public static void setCritMultiplier(float mult) {
        critMultiplier.set(mult);
    }

    public static float getCritMultiplier() {
        return critMultiplier.get();
    }

    public static void reset() {
        isCriticalHit.set(false);
        critMultiplier.set(1.0f);
    }
}