/*
 * Decompiled with CFR 0.152.
 */
package punchy.compat.entityculling;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;
import punchy.platform.Services;

public final class EntityCullingCompat {
    private static final String MOD_ID = "entityculling";
    private static final String MOD_BASE = "dev.tr7zw.entityculling.EntityCullingModBase";
    private static volatile boolean initialized;
    private static volatile boolean applied;
    private static volatile boolean loaded;
    private static volatile Field configField;
    private static volatile Field frustumField;

    private EntityCullingCompat() {
    }

    public static void tick() {
        if (applied) {
            return;
        }
        if (!EntityCullingCompat.ensureLoaded()) {
            applied = true;
            return;
        }
        EntityCullingCompat.tryWhitelistEnchantingTable();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T withBlockEntityFrustumCullingDisabled(Supplier<T> supplier) {
        if (supplier == null) {
            return null;
        }
        if (!EntityCullingCompat.ensureLoaded()) {
            return supplier.get();
        }
        try {
            Object config = EntityCullingCompat.resolveConfig();
            if (config == null) {
                return supplier.get();
            }
            Field field = EntityCullingCompat.resolveFrustumField(config);
            if (field == null) {
                return supplier.get();
            }
            boolean prev = field.getBoolean(config);
            if (!prev) {
                return supplier.get();
            }
            field.setBoolean(config, false);
            try {
                T t = supplier.get();
                return t;
            }
            finally {
                field.setBoolean(config, prev);
            }
        }
        catch (Throwable ignored) {
            return supplier.get();
        }
    }

    private static void tryWhitelistEnchantingTable() {
        try {
            Class<?> base = Class.forName(MOD_BASE);
            Object instance = base.getField("instance").get(null);
            if (instance == null) {
                return;
            }
            Method addWhitelist = base.getMethod("addDynamicBlockEntityWhitelist", Function.class);
            Function<Object, Boolean> whitelist = blockEntity -> {
                if (blockEntity == null) {
                    return false;
                }
                String name = blockEntity.getClass().getName();
                return name.endsWith("EnchantmentTableBlockEntity") || name.endsWith("EnchantingTableBlockEntity");
            };
            addWhitelist.invoke(instance, whitelist);
            applied = true;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean ensureLoaded() {
        if (!initialized) {
            initialized = true;
            loaded = Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    private static Object resolveConfig() throws ReflectiveOperationException {
        Class<?> base = Class.forName(MOD_BASE);
        Object instance = base.getField("instance").get(null);
        if (instance == null) {
            return null;
        }
        Field field = configField;
        if (field == null) {
            configField = field = base.getSuperclass().getField("config");
        }
        return field.get(instance);
    }

    private static Field resolveFrustumField(Object config) throws ReflectiveOperationException {
        if (config == null) {
            return null;
        }
        Field field = frustumField;
        if (field == null) {
            frustumField = field = config.getClass().getField("blockEntityFrustumCulling");
        }
        return field;
    }
}

