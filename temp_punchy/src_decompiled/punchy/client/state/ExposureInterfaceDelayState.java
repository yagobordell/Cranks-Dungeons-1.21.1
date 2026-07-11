/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.state;

public final class ExposureInterfaceDelayState {
    private static long delayUntilNanos;

    private ExposureInterfaceDelayState() {
    }

    public static void schedule(float seconds) {
        if (seconds <= 0.0f) {
            return;
        }
        delayUntilNanos = Math.max(delayUntilNanos, System.nanoTime() + (long)(seconds * 1.0E9f));
    }

    public static boolean shouldBlockExposureInterface() {
        return delayUntilNanos > 0L && System.nanoTime() < delayUntilNanos;
    }

    public static void clear() {
        delayUntilNanos = 0L;
    }
}

