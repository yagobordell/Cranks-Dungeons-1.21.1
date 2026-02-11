package com.cranks.dungeons.util;

public class CriticalHitState {
    private static final ThreadLocal<Boolean> isCriticalHit = ThreadLocal.withInitial(() -> false);

    public static void setCriticalHit(boolean value) {
        isCriticalHit.set(value);
    }

    public static boolean isCriticalHit() {
        return isCriticalHit.get();
    }

    public static void reset() {
        isCriticalHit.set(false);
    }
}