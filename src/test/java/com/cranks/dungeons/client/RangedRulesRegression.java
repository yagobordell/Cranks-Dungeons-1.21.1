package com.cranks.dungeons.client;

import com.cranks.dungeons.ranged.RangedRules;

public final class RangedRulesRegression {
    public static void run() {
        check(RangedRules.duration(20,1)==20 && RangedRules.duration(25,.4)==63,"base draw/reload durations wrong");
        check(RangedRules.duration(20,2)==10 && RangedRules.duration(20,.5)==40,"speed applied incorrectly");
        check(RangedRules.duration(25,1000)==2 && RangedRules.duration(20,0)==400,"unbounded charging duration");
        check(Math.abs(RangedRules.speed(2.8,.2)-.84)<1e-6,"attack speed double-counted");
        check(RangedRules.speed(Double.NaN,1)==1 && RangedRules.speed(-10,-2)>=.05,"invalid speed propagated");
        check(RangedRules.power(3)==1 && RangedRules.power(6)==1 && RangedRules.power(1.5f)==.5f,"draw strength changed by velocity upgrades");
        System.out.println("Ranged timing and charge arithmetic regression checks passed");
    }
    private static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
}
