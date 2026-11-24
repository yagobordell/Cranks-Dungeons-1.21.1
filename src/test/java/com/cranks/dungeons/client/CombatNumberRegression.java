package com.cranks.dungeons.client;

import com.cranks.dungeons.damage.CombatNumberStyle;
import com.cranks.dungeons.damage.HealingNumberContext;

public final class CombatNumberRegression {
    public static void run() {
        require(!CombatNumberStyle.valid(Double.NaN) && !CombatNumberStyle.valid(Double.POSITIVE_INFINITY)
                && !CombatNumberStyle.valid(0) && !CombatNumberStyle.valid(-1), "invalid combat values accepted");
        require(CombatNumberStyle.scale(500,false,false) > CombatNumberStyle.scale(48,false,false)
                && CombatNumberStyle.scale(5000,false,false) > CombatNumberStyle.scale(500,false,false), "high damage loses impact");
        require(CombatNumberStyle.scale(20,true,false) > CombatNumberStyle.scale(20,false,false), "critical has no presence");
        require(CombatNumberStyle.scale(Double.MAX_VALUE,true,false) <= 1.9f, "unbounded number size");
        require(CombatNumberStyle.alpha(0,1400) == 1 && CombatNumberStyle.alpha(800,1400) == 1
                && CombatNumberStyle.alpha(1300,1400) > 0 && CombatNumberStyle.alpha(1400,1400) == 0, "reading/fade window broken");
        require(CombatNumberStyle.entrance(0,false) == 1 && CombatNumberStyle.entrance(800,true) == 1, "text shrinks away");
        require("<0.1".equals(CombatNumberStyle.format(0.01)) && "48".equals(CombatNumberStyle.format(48))
                && "12.5".equals(CombatNumberStyle.format(12.5)) && "15k".equals(CombatNumberStyle.format(15000))
                && "2.5m".equals(CombatNumberStyle.format(2500000)), "number formatting misleading");
        require(!HealingNumberContext.gradual(), "leaked recovery scope");
        try {
            HealingNumberContext.gradual(() -> {
                require(HealingNumberContext.gradual(), "recovery scope missing");
                HealingNumberContext.gradual(() -> require(HealingNumberContext.gradual(), "nested recovery scope missing"));
                throw new IllegalStateException("expected");
            });
        } catch (IllegalStateException expected) { }
        require(!HealingNumberContext.gradual(), "failed recovery leaks into instant healing");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
