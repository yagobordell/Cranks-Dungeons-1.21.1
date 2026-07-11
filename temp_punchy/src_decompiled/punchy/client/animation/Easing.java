/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.animation;

public final class Easing {
    private static final float C1 = 1.9f;
    private static final float C3 = 2.9f;

    private Easing() {
    }

    public static float easeInBack(float t) {
        return 2.9f * t * t * t - 1.9f * t * t;
    }

    public static float easeOutBack(float t) {
        float x = t - 1.0f;
        return 1.0f + 2.9f * x * x * x + 1.9f * x * x;
    }

    public static float easeOutBackStrong(float t) {
        float strongC1 = 3.61f;
        float strongC3 = strongC1 + 1.0f;
        float x = t - 1.0f;
        return 1.0f + strongC3 * x * x * x + strongC1 * x * x;
    }

    public static float smoothStep(float t) {
        float clamped = Math.max(0.0f, Math.min(1.0f, t));
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }
}

