/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 */
package punchy.client.render;

import net.minecraft.class_1268;

public final class UseAnimationSuppressor {
    private static boolean mainHandSuppressed;
    private static boolean offHandSuppressed;
    private static boolean forced;
    private static boolean mainHandPendingReset;
    private static boolean offHandPendingReset;
    private static long mainHandReleaseStartTick;
    private static long offHandReleaseStartTick;
    private static int mainHandReleaseStartRemainingTicks;
    private static int offHandReleaseStartRemainingTicks;
    private static int mainHandMeshStartRemainingTicks;
    private static int offHandMeshStartRemainingTicks;

    private UseAnimationSuppressor() {
    }

    public static void suppress() {
        UseAnimationSuppressor.suppress(class_1268.field_5808);
        UseAnimationSuppressor.suppress(class_1268.field_5810);
    }

    public static void suppress(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            offHandSuppressed = true;
            offHandPendingReset = true;
            offHandReleaseStartTick = -1L;
            offHandReleaseStartRemainingTicks = -1;
            return;
        }
        mainHandSuppressed = true;
        mainHandPendingReset = true;
        mainHandReleaseStartTick = -1L;
        mainHandReleaseStartRemainingTicks = -1;
    }

    public static void release(long gameTime, int remainingTicks) {
        UseAnimationSuppressor.release(class_1268.field_5808, gameTime, remainingTicks);
        UseAnimationSuppressor.release(class_1268.field_5810, gameTime, remainingTicks);
    }

    public static void release(class_1268 hand, long gameTime, int remainingTicks) {
        if (hand == class_1268.field_5810) {
            offHandSuppressed = false;
            if (offHandPendingReset) {
                offHandReleaseStartTick = gameTime;
                offHandReleaseStartRemainingTicks = remainingTicks;
                offHandPendingReset = false;
            }
            return;
        }
        mainHandSuppressed = false;
        if (mainHandPendingReset) {
            mainHandReleaseStartTick = gameTime;
            mainHandReleaseStartRemainingTicks = remainingTicks;
            mainHandPendingReset = false;
        }
    }

    public static void clear() {
        UseAnimationSuppressor.clear(class_1268.field_5808);
        UseAnimationSuppressor.clear(class_1268.field_5810);
    }

    public static void clear(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            offHandSuppressed = false;
            offHandPendingReset = false;
            offHandReleaseStartTick = -1L;
            offHandReleaseStartRemainingTicks = -1;
            offHandMeshStartRemainingTicks = -1;
            return;
        }
        mainHandSuppressed = false;
        mainHandPendingReset = false;
        mainHandReleaseStartTick = -1L;
        mainHandReleaseStartRemainingTicks = -1;
        mainHandMeshStartRemainingTicks = -1;
    }

    public static boolean isSuppressed() {
        return mainHandSuppressed || offHandSuppressed;
    }

    public static boolean isSuppressed(class_1268 hand) {
        return hand == class_1268.field_5810 ? offHandSuppressed : mainHandSuppressed;
    }

    public static void force(boolean value) {
        forced = value;
    }

    public static boolean isForced() {
        return forced;
    }

    public static long getReleaseStartTick() {
        return mainHandReleaseStartTick >= 0L ? mainHandReleaseStartTick : offHandReleaseStartTick;
    }

    public static long getReleaseStartTick(class_1268 hand) {
        return hand == class_1268.field_5810 ? offHandReleaseStartTick : mainHandReleaseStartTick;
    }

    public static int getReleaseStartRemainingTicks() {
        return mainHandReleaseStartRemainingTicks >= 0 ? mainHandReleaseStartRemainingTicks : offHandReleaseStartRemainingTicks;
    }

    public static int getReleaseStartRemainingTicks(class_1268 hand) {
        return hand == class_1268.field_5810 ? offHandReleaseStartRemainingTicks : mainHandReleaseStartRemainingTicks;
    }

    public static void clearReleaseStart() {
        UseAnimationSuppressor.clearReleaseStart(class_1268.field_5808);
        UseAnimationSuppressor.clearReleaseStart(class_1268.field_5810);
    }

    public static void clearReleaseStart(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            offHandReleaseStartTick = -1L;
            offHandReleaseStartRemainingTicks = -1;
            return;
        }
        mainHandReleaseStartTick = -1L;
        mainHandReleaseStartRemainingTicks = -1;
    }

    public static void recordMeshStart(int remainingTicks) {
        UseAnimationSuppressor.recordMeshStart(class_1268.field_5808, remainingTicks);
        UseAnimationSuppressor.recordMeshStart(class_1268.field_5810, remainingTicks);
    }

    public static void recordMeshStart(class_1268 hand, int remainingTicks) {
        if (remainingTicks < 0) {
            return;
        }
        if (hand == class_1268.field_5810) {
            if (offHandMeshStartRemainingTicks < 0) {
                offHandMeshStartRemainingTicks = remainingTicks;
            }
            return;
        }
        if (mainHandMeshStartRemainingTicks < 0) {
            mainHandMeshStartRemainingTicks = remainingTicks;
        }
    }

    public static int getMeshStartRemainingTicks() {
        return mainHandMeshStartRemainingTicks >= 0 ? mainHandMeshStartRemainingTicks : offHandMeshStartRemainingTicks;
    }

    public static int getMeshStartRemainingTicks(class_1268 hand) {
        return hand == class_1268.field_5810 ? offHandMeshStartRemainingTicks : mainHandMeshStartRemainingTicks;
    }

    public static void clearMeshStart() {
        UseAnimationSuppressor.clearMeshStart(class_1268.field_5808);
        UseAnimationSuppressor.clearMeshStart(class_1268.field_5810);
    }

    public static void clearMeshStart(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            offHandMeshStartRemainingTicks = -1;
            return;
        }
        mainHandMeshStartRemainingTicks = -1;
    }

    static {
        mainHandReleaseStartTick = -1L;
        offHandReleaseStartTick = -1L;
        mainHandReleaseStartRemainingTicks = -1;
        offHandReleaseStartRemainingTicks = -1;
        mainHandMeshStartRemainingTicks = -1;
        offHandMeshStartRemainingTicks = -1;
    }
}

