/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_310
 */
package punchy.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import punchy.client.state.UseItemStateMachine;

public final class SwordBlockingMechanicsCompat {
    private static final String[] HANDLER_CLASSES = new String[]{"fuzs.swordblockingmechanics.handler.SwordBlockingHandler", "fuzs.swordblockingmechanics.common.handler.SwordBlockingHandler"};
    private static final String EVENT_RESULT_CLASS = "fuzs.puzzleslib.api.event.v1.core.EventResult";
    private static boolean available = false;
    private static boolean initialized = false;
    private static Method isActiveItemStackBlocking;
    private static Method getParryStrengthScale;
    private static Object eventResultPass;
    private static class_1268 callbackBlockingHand;

    private SwordBlockingMechanicsCompat() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        Class<?> handler = SwordBlockingMechanicsCompat.findClass(HANDLER_CLASSES);
        boolean bl = available = handler != null;
        if (handler == null) {
            return;
        }
        try {
            isActiveItemStackBlocking = SwordBlockingMechanicsCompat.findMethod(handler, "isActiveItemStackBlocking", 1);
            getParryStrengthScale = SwordBlockingMechanicsCompat.findMethod(handler, "getParryStrengthScale", 1);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static boolean shouldUseBlockingAnimation(class_1268 hand) {
        return hand != null && callbackBlockingHand == hand;
    }

    public static void recordBlockingStart(class_1657 player, class_1268 hand) {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null || player != client.field_1724 || hand == null) {
            return;
        }
        callbackBlockingHand = hand;
        UseItemStateMachine.requestSwordBlockingCompat(hand);
    }

    public static void recordBlockingStop(class_1657 player, class_1268 hand) {
        if (player == null || hand == null) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null || player != client.field_1724) {
            return;
        }
        if (callbackBlockingHand == hand) {
            callbackBlockingHand = null;
        }
    }

    public static boolean isBlocking(class_1309 livingEntity) {
        if (!available || livingEntity == null) {
            return false;
        }
        if (isActiveItemStackBlocking == null) {
            return false;
        }
        try {
            Boolean value;
            Object result = isActiveItemStackBlocking.invoke(null, livingEntity);
            return result instanceof Boolean && (value = (Boolean)result) != false;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public static double getParryStrengthScale(class_1309 livingEntity) {
        if (!available || livingEntity == null) {
            return 1.0;
        }
        if (getParryStrengthScale == null) {
            return 1.0;
        }
        try {
            Object result = getParryStrengthScale.invoke(null, livingEntity);
            if (result instanceof Double) {
                Double value = (Double)result;
                return value;
            }
            if (result instanceof Float) {
                Float value = (Float)result;
                return value.doubleValue();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return 1.0;
    }

    public static Object getEventResultPass() {
        if (!available) {
            return null;
        }
        if (eventResultPass != null) {
            return eventResultPass;
        }
        try {
            Class<?> result = Class.forName(EVENT_RESULT_CLASS);
            Field passField = result.getField("PASS");
            eventResultPass = passField.get(null);
            return eventResultPass;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static Class<?> findClass(String ... names) {
        if (names == null) {
            return null;
        }
        for (String name : names) {
            try {
                return Class.forName(name);
            }
            catch (ClassNotFoundException classNotFoundException) {
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, int paramCount) {
        if (type == null || name == null) {
            return null;
        }
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != paramCount) continue;
            return method;
        }
        return null;
    }
}

