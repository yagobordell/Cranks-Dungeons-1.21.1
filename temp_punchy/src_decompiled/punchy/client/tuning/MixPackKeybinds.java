/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.tuning;

public final class MixPackKeybinds {
    private static boolean registered = false;

    private MixPackKeybinds() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
    }
}

