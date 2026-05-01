package com.cranks.dungeons.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {

    private static final Random SHARED_RANDOM = new Random();

    private RandomUtil() {
    }

    public static Random getRandom() {
        return SHARED_RANDOM;
    }

    public static double nextDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    public static double nextDouble(double origin, double bound) {
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    public static int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static int nextInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
