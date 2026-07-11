/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1294
 *  net.minecraft.class_310
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_310;

public final class AttackActionTracker {
    private static boolean triggered;
    private static class_1268 lastHand;
    private static boolean lastCritical;
    private static boolean pendingCritical;
    private static long triggerEpoch;

    private AttackActionTracker() {
    }

    public static void recordCriticalCandidate(class_310 client) {
        if (client == null || client.field_1724 == null) {
            pendingCritical = false;
            return;
        }
        pendingCritical = AttackActionTracker.isCriticalHit(client);
    }

    public static void clearPendingCritical() {
        pendingCritical = false;
    }

    public static void recordAttack() {
        boolean wasTriggered = triggered;
        triggered = true;
        if (!wasTriggered) {
            ++triggerEpoch;
            lastHand = class_1268.field_5808;
            lastCritical = pendingCritical;
            pendingCritical = false;
        }
    }

    public static void recordAttack(class_1268 hand) {
        boolean wasTriggered = triggered;
        triggered = true;
        if (hand != null) {
            lastHand = hand;
        }
        if (!wasTriggered) {
            ++triggerEpoch;
            lastCritical = pendingCritical;
            pendingCritical = false;
        }
    }

    public static boolean isTriggered() {
        return triggered;
    }

    public static boolean consumeTrigger() {
        if (!triggered) {
            return false;
        }
        triggered = false;
        return true;
    }

    public static class_1268 peekHand() {
        if (!triggered) {
            return null;
        }
        return lastHand;
    }

    public static long peekEpoch() {
        return triggerEpoch;
    }

    public static class_1268 peekLastRecordedHand() {
        return lastHand;
    }

    public static class_1268 consumeHand() {
        if (!triggered) {
            return null;
        }
        triggered = false;
        return lastHand;
    }

    public static boolean consumeCritical() {
        boolean critical = lastCritical;
        lastCritical = false;
        return critical;
    }

    public static void clear() {
        triggered = false;
        lastHand = class_1268.field_5808;
        lastCritical = false;
        pendingCritical = false;
    }

    private static boolean isCriticalHit(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (client.field_1724.method_7261(1.0f) < 0.9f) {
            return false;
        }
        if (client.field_1724.method_24828()) {
            return false;
        }
        if (client.field_1724.field_6017 <= 0.0f) {
            return false;
        }
        if (client.field_1724.method_5799() || client.field_1724.method_5771()) {
            return false;
        }
        if (client.field_1724.method_6101()) {
            return false;
        }
        if (client.field_1724.method_5765()) {
            return false;
        }
        if (client.field_1724.method_5624()) {
            return false;
        }
        return !client.field_1724.method_6059(class_1294.field_5919);
    }

    static {
        lastHand = class_1268.field_5808;
    }
}

