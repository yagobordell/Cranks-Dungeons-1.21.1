package com.cranks.dungeons.util;

public class CombatXpContext {
    private static final ThreadLocal<Boolean> IS_COMBAT_XP = ThreadLocal.withInitial(() -> false);

    public static void setCombatXp(boolean isCombatXp) {
        IS_COMBAT_XP.set(isCombatXp);
    }

    public static boolean isCombatXp() {
        return IS_COMBAT_XP.get();
    }
}
