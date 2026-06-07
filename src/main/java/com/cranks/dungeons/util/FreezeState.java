package com.cranks.dungeons.util;

public class FreezeState {
    private static final ThreadLocal<Boolean> IS_RENDERING_FROZEN = ThreadLocal.withInitial(() -> false);

    public static boolean isCurrentlyRenderingFrozen() {
        return IS_RENDERING_FROZEN.get();
    }

    public static void setRenderingFrozen(boolean frozen) {
        IS_RENDERING_FROZEN.set(frozen);
    }
}
