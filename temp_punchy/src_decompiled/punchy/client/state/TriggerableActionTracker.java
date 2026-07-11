/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 */
package punchy.client.state;

import net.minecraft.class_1268;

public final class TriggerableActionTracker {
    private static boolean triggered;
    private static class_1268 triggerHand;
    private static int triggerKeyCode;

    private TriggerableActionTracker() {
    }

    public static void record(class_1268 hand, int keyCode) {
        triggered = true;
        triggerHand = hand;
        triggerKeyCode = keyCode;
    }

    public static Trigger consumeTrigger() {
        if (!triggered) {
            return null;
        }
        triggered = false;
        return new Trigger(triggerHand, triggerKeyCode);
    }

    public static void clear() {
        triggered = false;
        triggerHand = null;
        triggerKeyCode = -1;
    }

    static {
        triggerKeyCode = -1;
    }

    public record Trigger(class_1268 hand, int keyCode) {
    }
}

