/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 */
package punchy.client.input;

import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import punchy.client.state.InspectActionTracker;
import punchy.platform.Services;

public final class AnimationKeybinds {
    private static final String CATEGORY = "key.categories.punchy.tuning";
    private static class_304 inspectKey;
    private static boolean registered;

    private AnimationKeybinds() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        inspectKey = new class_304("key.punchy.inspect", class_3675.class_307.field_1668, 73, CATEGORY);
        Services.CLIENT.registerKeyMapping(inspectKey);
        registered = true;
        Services.CLIENT.registerClientTickEnd(AnimationKeybinds::handleClientTick);
    }

    private static void handleClientTick(class_310 client) {
        if (!registered || client == null || client.field_1724 == null) {
            return;
        }
        if (client.field_1755 != null) {
            return;
        }
        while (inspectKey.method_1436()) {
            InspectActionTracker.record();
        }
    }
}

