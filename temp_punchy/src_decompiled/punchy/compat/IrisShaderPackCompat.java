/*
 * Decompiled with CFR 0.152.
 */
package punchy.compat;

import java.lang.reflect.Method;

public final class IrisShaderPackCompat {
    private static boolean checked;
    private static Object irisApiInstance;
    private static Method shaderPackInUseMethod;

    private IrisShaderPackCompat() {
    }

    public static boolean isShaderPackInUse() {
        return Boolean.TRUE.equals(IrisShaderPackCompat.queryShaderPackInUse());
    }

    public static Boolean queryShaderPackInUse() {
        Method method = IrisShaderPackCompat.getShaderPackInUseMethod();
        if (method == null || irisApiInstance == null) {
            return null;
        }
        try {
            Boolean active;
            Object value = method.invoke(irisApiInstance, new Object[0]);
            return value instanceof Boolean ? (active = (Boolean)value) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static Method getShaderPackInUseMethod() {
        if (checked) {
            return shaderPackInUseMethod;
        }
        checked = true;
        IrisShaderPackCompat.resolve("net.irisshaders.iris.api.v0.IrisApi");
        if (shaderPackInUseMethod == null) {
            IrisShaderPackCompat.resolve("net.coderbot.iris.api.v0.IrisApi");
        }
        return shaderPackInUseMethod;
    }

    private static void resolve(String className) {
        try {
            Class<?> apiClass = Class.forName(className);
            Object instance = apiClass.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            Method method = apiClass.getMethod("isShaderPackInUse", new Class[0]);
            irisApiInstance = instance;
            shaderPackInUseMethod = method;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

