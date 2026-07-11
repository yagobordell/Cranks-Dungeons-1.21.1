/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 */
package punchy.client.state;

import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;

public final class MiningActionTracker {
    private static boolean miningActive;
    private static boolean triggered;
    private static boolean miningMud;
    private static class_2338 miningPos;
    private static class_2350 miningFace;
    private static class_243 miningHitLocation;
    private static long triggerEpoch;
    private static long lastRecordNano;
    private static class_2338 lastTrackablePos;
    private static class_2350 lastTrackableFace;
    private static class_243 lastTrackableHitLocation;
    private static class_2680 lastTrackableState;
    private static long lastTrackableNano;

    private MiningActionTracker() {
    }

    public static void recordStart() {
        MiningActionTracker.recordStart(null, null);
    }

    public static void recordContinue() {
        MiningActionTracker.recordContinue(null, null);
    }

    public static void recordStart(class_2338 pos) {
        MiningActionTracker.recordStart(pos, null);
    }

    public static void recordStart(class_2338 pos, class_2350 face) {
        MiningActionTracker.recordStart(pos, face, null);
    }

    public static void recordStart(class_2338 pos, class_2350 face, class_243 hitLocation) {
        miningActive = true;
        triggered = true;
        ++triggerEpoch;
        miningPos = pos;
        if (face != null) {
            miningFace = face;
        }
        miningHitLocation = hitLocation;
        class_2680 state = MiningActionTracker.resolveState(pos);
        miningMud = MiningActionTracker.isMudBlock(state);
        MiningActionTracker.cacheTrackableTarget(pos, face, hitLocation, state);
        lastRecordNano = System.nanoTime();
    }

    public static void recordContinue(class_2338 pos) {
        MiningActionTracker.recordContinue(pos, null);
    }

    public static void recordContinue(class_2338 pos, class_2350 face) {
        MiningActionTracker.recordContinue(pos, face, null);
    }

    public static void recordContinue(class_2338 pos, class_2350 face, class_243 hitLocation) {
        miningActive = true;
        miningPos = pos;
        if (face != null) {
            miningFace = face;
        }
        miningHitLocation = hitLocation;
        class_2680 state = MiningActionTracker.resolveState(pos);
        miningMud = MiningActionTracker.isMudBlock(state);
        MiningActionTracker.cacheTrackableTarget(pos, face, hitLocation, state);
        lastRecordNano = System.nanoTime();
    }

    public static void recordAttempt(class_2338 pos, class_2350 face, class_243 hitLocation) {
        triggered = true;
        ++triggerEpoch;
        miningPos = pos;
        if (face != null) {
            miningFace = face;
        }
        miningHitLocation = hitLocation;
        class_2680 state = MiningActionTracker.resolveState(pos);
        miningMud = MiningActionTracker.isMudBlock(state);
        MiningActionTracker.cacheTrackableTarget(pos, face, hitLocation, state);
        lastRecordNano = System.nanoTime();
    }

    public static void recordStop() {
        miningActive = false;
        miningMud = false;
        miningPos = null;
        miningFace = class_2350.field_11043;
        miningHitLocation = null;
    }

    public static boolean isMiningActive() {
        return miningActive;
    }

    public static boolean isMiningMud() {
        return miningMud;
    }

    public static boolean consumeTrigger() {
        if (!triggered) {
            return false;
        }
        triggered = false;
        return true;
    }

    public static Target peekTarget() {
        if (!miningActive) {
            return null;
        }
        class_2680 state = MiningActionTracker.resolveState(miningPos);
        if (miningPos == null || state == null || state.method_26215()) {
            return null;
        }
        return new Target(miningPos, miningFace, state, miningHitLocation);
    }

    public static Target peekRecentTarget(long windowNanos) {
        if (windowNanos <= 0L || lastTrackableNano <= 0L) {
            return null;
        }
        if (System.nanoTime() - lastTrackableNano > windowNanos) {
            return null;
        }
        if (lastTrackablePos == null || lastTrackableState == null || lastTrackableState.method_26215()) {
            return null;
        }
        return new Target(lastTrackablePos, lastTrackableFace, lastTrackableState, lastTrackableHitLocation);
    }

    public static class_2338 peekPos() {
        return miningPos;
    }

    public static class_2350 peekFace() {
        return miningFace;
    }

    public static class_2680 peekState() {
        return MiningActionTracker.resolveState(miningPos);
    }

    public static long peekEpoch() {
        return triggerEpoch;
    }

    public static boolean wasRecentMining(long windowNanos) {
        if (windowNanos <= 0L || lastRecordNano <= 0L) {
            return false;
        }
        return System.nanoTime() - lastRecordNano <= windowNanos;
    }

    private static class_2680 resolveState(class_2338 pos) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1687 == null || pos == null) {
            return null;
        }
        return mc.field_1687.method_8320(pos);
    }

    private static boolean isMudBlock(class_2680 state) {
        if (state == null) {
            return false;
        }
        return state.method_27852(class_2246.field_37576) || state.method_27852(class_2246.field_37556) || state.method_27852(class_2246.field_37557) || state.method_27852(class_2246.field_37547);
    }

    private static void cacheTrackableTarget(class_2338 pos, class_2350 face, class_243 hitLocation, class_2680 state) {
        if (pos == null || state == null || state.method_26215()) {
            return;
        }
        lastTrackablePos = pos.method_10062();
        lastTrackableFace = face != null ? face : miningFace;
        lastTrackableHitLocation = hitLocation;
        lastTrackableState = state;
        lastTrackableNano = System.nanoTime();
    }

    static {
        miningFace = class_2350.field_11043;
        lastTrackableFace = class_2350.field_11043;
    }

    public record Target(class_2338 pos, class_2350 face, class_2680 state, class_243 hitLocation) {
    }
}

