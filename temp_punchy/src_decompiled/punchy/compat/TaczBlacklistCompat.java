/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package punchy.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import punchy.platform.Services;

public final class TaczBlacklistCompat {
    private static final String TACZ_MOD_ID = "tacz";
    private static final String TIMELESS_API_CLASS = "com.tacz.guns.api.TimelessAPI";
    private static final String GUN_ITEM_MANAGER_CLASS = "com.tacz.guns.api.item.gun.GunItemManager";
    private static final String GUN_ITEM_INTERFACE = "com.tacz.guns.api.item.IGun";
    private static final String CLIENT_GUN_INDEX_CLASS = "com.tacz.guns.client.resource.index.ClientGunIndex";
    private static final String REGISTRY_OBJECT_CLASS = "net.minecraftforge.registries.RegistryObject";
    private static final String GUN_BASE_ITEM_ID = "tacz:modern_kinetic_gun";

    private TaczBlacklistCompat() {
    }

    public static boolean isAvailable() {
        return Services.PLATFORM != null && Services.PLATFORM.isModLoaded(TACZ_MOD_ID);
    }

    public static List<VirtualEntry> collectGunEntries() {
        if (!TaczBlacklistCompat.isAvailable()) {
            return List.of();
        }
        try {
            Class<?> timelessApiClass = Class.forName(TIMELESS_API_CLASS);
            Method getAllClientGunIndexMethod = timelessApiClass.getMethod("getAllClientGunIndex", new Class[0]);
            Object rawEntries = getAllClientGunIndexMethod.invoke(null, new Object[0]);
            if (!(rawEntries instanceof Iterable)) {
                return List.of();
            }
            Iterable iterable = (Iterable)rawEntries;
            ArrayList<VirtualEntry> entries = new ArrayList<VirtualEntry>();
            for (Object rawEntry : iterable) {
                Map.Entry entry;
                class_2960 id;
                if (!(rawEntry instanceof Map.Entry) || (id = TaczBlacklistCompat.asResourceLocation((entry = (Map.Entry)rawEntry).getKey())) == null) continue;
                String displayName = TaczBlacklistCompat.resolveGunDisplayName(entry.getValue(), id);
                class_1799 icon = TaczBlacklistCompat.createGunIconStack(id);
                entries.add(new VirtualEntry(id.toString().toLowerCase(Locale.ROOT), id.method_12836().toLowerCase(Locale.ROOT), displayName, "gun", icon));
            }
            return entries;
        }
        catch (Throwable ignored) {
            return List.of();
        }
    }

    public static void appendVirtualKeys(class_1799 stack, Set<String> keys) {
        if (!TaczBlacklistCompat.isAvailable() || stack == null || stack.method_7960() || keys == null) {
            return;
        }
        class_2960 gunId = TaczBlacklistCompat.resolveGunId(stack);
        if (gunId != null) {
            keys.add(gunId.toString());
        }
    }

    private static String resolveGunDisplayName(Object clientGunIndex, class_2960 id) {
        if (clientGunIndex != null) {
            try {
                String name;
                Method getNameMethod;
                Object result;
                Class<?> indexClass = Class.forName(CLIENT_GUN_INDEX_CLASS);
                if (indexClass.isInstance(clientGunIndex) && (result = (getNameMethod = indexClass.getMethod("getName", new Class[0])).invoke(clientGunIndex, new Object[0])) instanceof String && !(name = (String)result).isBlank()) {
                    return name;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return TaczBlacklistCompat.humanizePath(id.method_12832());
    }

    private static class_1799 createGunIconStack(class_2960 gunId) {
        class_1799 exactStack = TaczBlacklistCompat.createRegisteredGunStack(gunId);
        if (!exactStack.method_7960()) {
            return exactStack;
        }
        class_1799 fallback = TaczBlacklistCompat.createFallbackGunBaseStack();
        if (!fallback.method_7960()) {
            return fallback;
        }
        return new class_1799((class_1935)class_1802.field_8399);
    }

    private static class_1799 createRegisteredGunStack(class_2960 gunId) {
        try {
            Class<?> gunItemManagerClass = Class.forName(GUN_ITEM_MANAGER_CLASS);
            Method getGunItemRegistryObjectMethod = gunItemManagerClass.getMethod("getGunItemRegistryObject", String.class);
            Object registryObject = getGunItemRegistryObjectMethod.invoke(null, gunId.toString());
            if (registryObject == null) {
                return class_1799.field_8037;
            }
            Class<?> registryObjectClass = Class.forName(REGISTRY_OBJECT_CLASS);
            if (!registryObjectClass.isInstance(registryObject)) {
                return class_1799.field_8037;
            }
            Method getMethod = registryObjectClass.getMethod("get", new Class[0]);
            Object itemObject = getMethod.invoke(registryObject, new Object[0]);
            if (!(itemObject instanceof class_1792)) {
                return class_1799.field_8037;
            }
            class_1792 item = (class_1792)itemObject;
            class_1799 stack = item.method_7854();
            return stack == null ? class_1799.field_8037 : stack;
        }
        catch (Throwable throwable) {
            return class_1799.field_8037;
        }
    }

    private static class_1799 createFallbackGunBaseStack() {
        try {
            Optional item = class_7923.field_41178.method_17966(class_2960.method_60654((String)GUN_BASE_ITEM_ID));
            if (item.isPresent()) {
                class_1799 stack = ((class_1792)item.get()).method_7854();
                return stack == null ? class_1799.field_8037 : stack;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return class_1799.field_8037;
    }

    private static class_2960 resolveGunId(class_1799 stack) {
        try {
            Class<?> gunInterfaceClass = Class.forName(GUN_ITEM_INTERFACE);
            Method getIGunOrNullMethod = gunInterfaceClass.getMethod("getIGunOrNull", class_1799.class);
            Object gunAccessor = getIGunOrNullMethod.invoke(null, stack);
            if (gunAccessor == null) {
                return null;
            }
            Method getGunIdMethod = gunInterfaceClass.getMethod("getGunId", class_1799.class);
            Object result = getGunIdMethod.invoke(gunAccessor, stack);
            return TaczBlacklistCompat.asResourceLocation(result);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static class_2960 asResourceLocation(Object value) {
        class_2960 id;
        return value instanceof class_2960 ? (id = (class_2960)value) : null;
    }

    private static String humanizePath(String path) {
        if (path == null || path.isBlank()) {
            return "Unknown TACZ Gun";
        }
        String[] parts = path.split("[_\\-]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) continue;
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() <= 1) continue;
            builder.append(part.substring(1));
        }
        return builder.length() > 0 ? builder.toString() : path;
    }

    public record VirtualEntry(String id, String namespace, String displayName, String type, class_1799 icon) {
        public String searchName() {
            return (this.displayName + " " + this.id + " " + this.namespace + " " + this.type).toLowerCase(Locale.ROOT);
        }
    }
}

