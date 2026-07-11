/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package punchy.compat;

import java.lang.reflect.Method;
import net.minecraft.class_1268;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_746;

public final class PunchyPuppiesCompat {
    private static boolean checked;
    private static Method getTuningIdForHandMethod;

    private PunchyPuppiesCompat() {
    }

    public static class_2960 getCarryTuningId(class_1268 hand) {
        class_746 player;
        if (hand == null || !PunchyPuppiesCompat.ensureLoaded()) {
            return null;
        }
        class_310 client = class_310.method_1551();
        class_746 class_7462 = player = client == null ? null : client.field_1724;
        if (player == null) {
            return null;
        }
        try {
            class_2960 id;
            Object value = getTuningIdForHandMethod.invoke(null, player, hand);
            return value instanceof class_2960 ? (id = (class_2960)value) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean isCarryTuningId(class_2960 id) {
        return id != null && "punchy".equals(id.method_12836()) && id.method_12832().startsWith("baby_carry_");
    }

    private static boolean ensureLoaded() {
        if (checked) {
            return getTuningIdForHandMethod != null;
        }
        checked = true;
        try {
            Class<?> stateClass = Class.forName("punchypuppies.client.state.BabyCarryState", false, PunchyPuppiesCompat.class.getClassLoader());
            getTuningIdForHandMethod = stateClass.getMethod("getTuningIdForHand", class_746.class, class_1268.class);
            getTuningIdForHandMethod.setAccessible(true);
        }
        catch (Throwable ignored) {
            getTuningIdForHandMethod = null;
        }
        return getTuningIdForHandMethod != null;
    }
}

