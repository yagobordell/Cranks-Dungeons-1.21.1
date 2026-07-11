/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.render;

public final class ChestInHandContext {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ChestInHandContext() {
    }

    public static void push() {
        ACTIVE.set(Boolean.TRUE);
    }

    public static void pop() {
        ACTIVE.set(Boolean.FALSE);
    }

    public static boolean isActive() {
        return Boolean.TRUE.equals(ACTIVE.get());
    }
}

