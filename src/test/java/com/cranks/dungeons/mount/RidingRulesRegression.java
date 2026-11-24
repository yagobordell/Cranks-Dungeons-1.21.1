package com.cranks.dungeons.mount;

public final class RidingRulesRegression {
    public static void run() {
        close(RidingRules.increase(.3, .375), .4125, "Custom riding percentage");
        close(RidingRules.increase(.3, 2.5), 1.05, "Large custom riding percentage was capped to a balance target");
        close(RidingRules.increase(.3, -.25), .225, "Negative modifier");
        close(RidingRules.increase(.3, -2), 0, "Negative speed");
        close(RidingRules.reduce(4, .375), 2.5, "Custom stamina reduction");
        close(RidingRules.reduce(4, 2), 0, "Reduction created negative cost");
        close(RidingRules.reduce(4, -.25), 5, "Negative reduction");
        close(RidingRules.increase(4, Double.NaN), 4, "Invalid bonus corrupted a resource");
        require(Double.isFinite(RidingRules.increase(4, Double.MAX_VALUE)), "Overflowed increase");
        require(Double.isFinite(RidingRules.reduce(4, -Double.MAX_VALUE)), "Overflowed cost");
        var normal = new MountMotionState();
        var agile = new MountMotionState();
        normal.approach(1, .065, .13);
        agile.approach(1, RidingRules.increase(.065, .5), RidingRules.increase(.13, .5));
        close(agile.throttle / normal.throttle, 1.5, "Acceleration responsiveness");
        normal.throttle = agile.throttle = 1;
        normal.approach(0, .065, .13);
        agile.approach(0, RidingRules.increase(.065, .5), RidingRules.increase(.13, .5));
        close((1 - agile.throttle) / (1 - normal.throttle), 1.5, "Braking responsiveness");
        System.out.println("Rider stat arithmetic and responsiveness regression checks passed");
    }

    private static void close(double actual, double expected, String message) {
        require(Math.abs(actual - expected) < .000001, message + ": " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
