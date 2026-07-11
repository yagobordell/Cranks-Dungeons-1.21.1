/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_3965
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_3965;
import punchy.client.state.ConsumeStateMachine;

public final class UseActionTracker {
    private static final long TRIGGER_VALID_NANOS = 1250000000L;
    private static final long FALLBACK_VALID_NANOS = 900000000L;
    private static Trigger mainConfirmed;
    private static Trigger offConfirmed;
    private static Trigger mainFallback;
    private static Trigger offFallback;
    private static class_1268 triggerHand;
    private static long triggerEpoch;
    private static long lastRecordNano;
    private static long lastMainUseNano;
    private static long lastOffUseNano;

    private UseActionTracker() {
    }

    public static void recordUse(class_1268 hand, TriggerType type) {
        UseActionTracker.recordUse(hand, type, null);
    }

    public static void recordUse(class_1268 hand, TriggerType type, class_3965 blockHit) {
        if (ConsumeStateMachine.isUseSuppressedAfterConsume()) {
            return;
        }
        long now = System.nanoTime();
        class_1268 resolvedHand = hand == null ? class_1268.field_5808 : hand;
        Trigger trigger = new Trigger(resolvedHand, type == null ? TriggerType.USE_ITEM : type, blockHit, false, now);
        UseActionTracker.setConfirmed(resolvedHand, trigger);
        UseActionTracker.discardFallbackForHand(resolvedHand);
        ++triggerEpoch;
        triggerHand = resolvedHand;
        lastRecordNano = now;
        UseActionTracker.markRecentUse(resolvedHand, now);
    }

    public static void recordFallbackUse(class_1268 hand, TriggerType type, class_3965 blockHit) {
        if (ConsumeStateMachine.isUseSuppressedAfterConsume()) {
            return;
        }
        long now = System.nanoTime();
        class_1268 resolvedHand = hand == null ? class_1268.field_5808 : hand;
        Trigger fallback = new Trigger(resolvedHand, type == null ? TriggerType.USE_ITEM : type, blockHit, true, now);
        UseActionTracker.setFallback(resolvedHand, fallback);
        UseActionTracker.markRecentUse(resolvedHand, now);
    }

    public static Trigger peekTrigger() {
        UseActionTracker.trimExpired();
        return UseActionTracker.latest(mainConfirmed, offConfirmed);
    }

    public static class_1268 peekLastHand() {
        return triggerHand;
    }

    public static long peekEpoch() {
        return triggerEpoch;
    }

    public static boolean wasRecentUse(long windowNanos) {
        if (windowNanos <= 0L || lastRecordNano <= 0L) {
            return false;
        }
        return System.nanoTime() - lastRecordNano <= windowNanos;
    }

    public static Trigger consumeTrigger() {
        UseActionTracker.trimExpired();
        Trigger latestConfirmed = UseActionTracker.latest(mainConfirmed, offConfirmed);
        if (latestConfirmed == null) {
            return null;
        }
        UseActionTracker.clearConfirmed(latestConfirmed.hand());
        return latestConfirmed;
    }

    public static Trigger consumeLatestTrigger() {
        UseActionTracker.trimExpired();
        Trigger latestConfirmed = UseActionTracker.latest(mainConfirmed, offConfirmed);
        Trigger latestFallback = UseActionTracker.latest(mainFallback, offFallback);
        if (latestFallback != null && (latestConfirmed == null || latestFallback.recordNano() >= latestConfirmed.recordNano())) {
            UseActionTracker.clearFallback(latestFallback.hand());
            return latestFallback;
        }
        if (latestConfirmed != null) {
            UseActionTracker.clearConfirmed(latestConfirmed.hand());
        }
        return latestConfirmed;
    }

    public static void pushBack(Trigger trigger) {
        if (trigger == null) {
            return;
        }
        if (trigger.fallback()) {
            UseActionTracker.setFallback(trigger.hand(), trigger);
        } else {
            UseActionTracker.setConfirmed(trigger.hand(), trigger);
        }
    }

    public static Trigger consumeFallbackTrigger() {
        UseActionTracker.trimExpired();
        Trigger latestFallback = UseActionTracker.latest(mainFallback, offFallback);
        if (latestFallback == null) {
            return null;
        }
        UseActionTracker.clearFallback(latestFallback.hand());
        return latestFallback;
    }

    public static void discardOppositeOlderThan(class_1268 hand, long recordNano) {
        Trigger oppositeFallback;
        if (hand == null || recordNano <= 0L) {
            return;
        }
        class_1268 opposite = hand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
        Trigger oppositeConfirmed = UseActionTracker.getConfirmed(opposite);
        if (oppositeConfirmed != null && oppositeConfirmed.recordNano() <= recordNano) {
            UseActionTracker.clearConfirmed(opposite);
        }
        if ((oppositeFallback = UseActionTracker.getFallback(opposite)) != null && oppositeFallback.recordNano() <= recordNano) {
            UseActionTracker.clearFallback(opposite);
        }
    }

    public static void discardFallbackForHand(class_1268 hand) {
        if (hand == null) {
            return;
        }
        UseActionTracker.clearFallback(hand);
    }

    public static boolean wasRecentUseForHand(class_1268 hand, long windowNanos) {
        long stamp;
        if (hand == null || windowNanos <= 0L) {
            return false;
        }
        long l = stamp = hand == class_1268.field_5810 ? lastOffUseNano : lastMainUseNano;
        if (stamp <= 0L) {
            return false;
        }
        return System.nanoTime() - stamp <= windowNanos;
    }

    public static void clear() {
        mainConfirmed = null;
        offConfirmed = null;
        mainFallback = null;
        offFallback = null;
    }

    private static void trimExpired() {
        long now = System.nanoTime();
        mainConfirmed = UseActionTracker.keepIfFresh(mainConfirmed, now, 1250000000L);
        offConfirmed = UseActionTracker.keepIfFresh(offConfirmed, now, 1250000000L);
        mainFallback = UseActionTracker.keepIfFresh(mainFallback, now, 900000000L);
        offFallback = UseActionTracker.keepIfFresh(offFallback, now, 900000000L);
    }

    private static Trigger keepIfFresh(Trigger trigger, long now, long window) {
        if (trigger == null) {
            return null;
        }
        if (trigger.recordNano() <= 0L) {
            return null;
        }
        return now - trigger.recordNano() <= window ? trigger : null;
    }

    private static Trigger latest(Trigger first, Trigger second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return second.recordNano() >= first.recordNano() ? second : first;
    }

    private static void setConfirmed(class_1268 hand, Trigger trigger) {
        if (hand == class_1268.field_5810) {
            offConfirmed = UseActionTracker.newer(offConfirmed, trigger);
        } else {
            mainConfirmed = UseActionTracker.newer(mainConfirmed, trigger);
        }
    }

    private static void clearConfirmed(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            offConfirmed = null;
        } else {
            mainConfirmed = null;
        }
    }

    private static Trigger getConfirmed(class_1268 hand) {
        return hand == class_1268.field_5810 ? offConfirmed : mainConfirmed;
    }

    private static void setFallback(class_1268 hand, Trigger trigger) {
        if (hand == class_1268.field_5810) {
            offFallback = UseActionTracker.newer(offFallback, trigger);
        } else {
            mainFallback = UseActionTracker.newer(mainFallback, trigger);
        }
    }

    private static void clearFallback(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            offFallback = null;
        } else {
            mainFallback = null;
        }
    }

    private static Trigger getFallback(class_1268 hand) {
        return hand == class_1268.field_5810 ? offFallback : mainFallback;
    }

    private static Trigger newer(Trigger current, Trigger incoming) {
        if (incoming == null) {
            return current;
        }
        if (current == null) {
            return incoming;
        }
        return incoming.recordNano() >= current.recordNano() ? incoming : current;
    }

    private static void markRecentUse(class_1268 hand, long nanos) {
        if (hand == null || nanos <= 0L) {
            return;
        }
        if (hand == class_1268.field_5810) {
            lastOffUseNano = nanos;
        } else {
            lastMainUseNano = nanos;
        }
    }

    static {
        triggerHand = class_1268.field_5808;
    }

    public static enum TriggerType {
        USE_ITEM,
        USE_ITEM_ON,
        DROP_ITEM,
        INTERACT,
        INTERACT_AT;

    }

    public record Trigger(class_1268 hand, TriggerType type, class_3965 blockHit, boolean fallback, long recordNano) {
    }
}

