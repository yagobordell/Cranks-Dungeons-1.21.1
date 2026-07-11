/*
 * Decompiled with CFR 0.152.
 */
package punchy.compat;

import java.lang.reflect.Field;

public final class SpellEngineCompat {
    private static final String CLIENT_CLASS = "net.spell_engine.client.SpellEngineClient";
    private static final String CONFIG_FIELD = "config";
    private static final String FIRST_PERSON_FIELD = "firstPersonAnimations";
    private static final String TRI_STATE_CLASS = "net.spell_engine.config.TriStateAuto";
    private static boolean available = false;
    private static boolean initialized = false;
    private static Object previousFirstPersonMode;

    private SpellEngineCompat() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        available = SpellEngineCompat.isClassPresent(CLIENT_CLASS);
    }

    public static boolean isAvailable() {
        return available;
    }

    public static void applyFirstPersonAnimations(boolean enabled) {
        if (!available) {
            return;
        }
        try {
            Class<?> client = Class.forName(CLIENT_CLASS);
            Field configField = client.getField(CONFIG_FIELD);
            Object config = configField.get(null);
            if (config == null) {
                return;
            }
            Field firstPersonField = config.getClass().getField(FIRST_PERSON_FIELD);
            if (enabled) {
                Object noValue;
                if (previousFirstPersonMode == null) {
                    previousFirstPersonMode = firstPersonField.get(config);
                }
                if ((noValue = SpellEngineCompat.findEnumValue(firstPersonField.getType(), "NO")) != null) {
                    firstPersonField.set(config, noValue);
                }
            } else if (previousFirstPersonMode != null) {
                firstPersonField.set(config, previousFirstPersonMode);
                previousFirstPersonMode = null;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        }
        catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static Object findEnumValue(Class<?> type, String name) {
        if (type == null || name == null || name.isEmpty()) {
            return null;
        }
        if (!type.isEnum()) {
            return null;
        }
        ?[] constants = type.getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (Object constant : constants) {
            if (constant == null || !name.equalsIgnoreCase(constant.toString())) continue;
            return constant;
        }
        return null;
    }
}

