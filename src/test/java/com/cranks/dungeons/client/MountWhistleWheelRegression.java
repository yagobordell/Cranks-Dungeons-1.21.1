package com.cranks.dungeons.client;

public final class MountWhistleWheelRegression {
    public static void run() {
        for (int radius : new int[]{30, 55, 85}) {
            var wheel = new MountWhistleWheel(radius);
            require(wheel.hit(0, -radius * .65) == 1, "Come sector missing");
            require(wheel.hit(radius * .55, radius * .35) == 2, "Follow sector missing");
            require(wheel.hit(-radius * .55, radius * .35) == 3, "Stay sector missing");
            require(wheel.hit(0, 0) == 0, "Center must cancel");
            require(wheel.hit(0, radius * .65) == 0, "Divider must cancel");
            for (int y = -radius - 2; y <= radius + 2; y++) {
                for (int x = -radius - 2; x <= radius + 2; x++) {
                    int hit = wheel.hit(x, y), mirror = wheel.hit(-x, y);
                    require(mirror == (hit == 2 ? 3 : hit == 3 ? 2 : hit), "Wheel is asymmetric");
                    if (x * x + y * y > radius * radius) require(hit == 0, "Hit outside the circle");
                }
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
