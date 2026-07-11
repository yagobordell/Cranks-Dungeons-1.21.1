/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2336
 *  net.minecraft.class_2531
 *  net.minecraft.class_2960
 *  net.minecraft.class_3300
 *  net.minecraft.class_4739
 */
package punchy.compat;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_2248;
import net.minecraft.class_2336;
import net.minecraft.class_2531;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_4739;
import punchy.platform.Services;

public final class EmfEtfCemCompat {
    private static final String[] EMF_IDS = new String[]{"entity_model_features", "emf"};
    private static final String[] ETF_IDS = new String[]{"entity_texture_features", "etf"};
    private static final String[] CEM_IDS = new String[]{"cem"};
    private static final String[] CEM_PATHS = new String[]{"emf/cem", "optifine/cem", "cem"};
    private static final String[] ETF_PATHS = new String[]{"etf", "optifine/random/entity", "optifine/emissive"};
    private static final EnumSet<ProxyPhysicsTarget> DISABLED = EnumSet.noneOf(ProxyPhysicsTarget.class);
    private static boolean ACTIVE;

    private EmfEtfCemCompat() {
    }

    public static void onResourceReload(class_3300 manager) {
        DISABLED.clear();
        ACTIVE = EmfEtfCemCompat.isAnyModLoaded();
        if (!ACTIVE || manager == null) {
            return;
        }
        for (String path : CEM_PATHS) {
            EmfEtfCemCompat.scan(manager, path, true);
        }
        if (EmfEtfCemCompat.isEtfLoaded()) {
            for (String path : ETF_PATHS) {
                EmfEtfCemCompat.scan(manager, path, false);
            }
        }
    }

    public static boolean disableProxyPhysics(ProxyPhysicsTarget target) {
        return ACTIVE && DISABLED.contains((Object)target);
    }

    public static ProxyPhysicsTarget resolveChestTarget(class_2248 block) {
        if (block instanceof class_2336) {
            return ProxyPhysicsTarget.ENDER_CHEST;
        }
        if (block instanceof class_2531) {
            return ProxyPhysicsTarget.TRAPPED_CHEST;
        }
        if (block instanceof class_4739) {
            return ProxyPhysicsTarget.CHEST;
        }
        return null;
    }

    private static boolean isAnyModLoaded() {
        return EmfEtfCemCompat.isEmfLoaded() || EmfEtfCemCompat.isEtfLoaded() || EmfEtfCemCompat.isCemLoaded();
    }

    private static boolean isEmfLoaded() {
        return EmfEtfCemCompat.isAnyLoaded(EMF_IDS);
    }

    private static boolean isEtfLoaded() {
        return EmfEtfCemCompat.isAnyLoaded(ETF_IDS);
    }

    private static boolean isCemLoaded() {
        return EmfEtfCemCompat.isAnyLoaded(CEM_IDS);
    }

    private static boolean isAnyLoaded(String[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        for (String id : ids) {
            if (id == null || id.isBlank() || !Services.PLATFORM.isModLoaded(id)) continue;
            return true;
        }
        return false;
    }

    private static void scan(class_3300 manager, String searchPath, boolean modelFilesOnly) {
        Map found = manager.method_14488(searchPath, id -> true);
        if (found == null || found.isEmpty()) {
            return;
        }
        for (class_2960 id2 : found.keySet()) {
            String normalized;
            String path;
            if (id2 == null || (path = id2.method_12832()) == null || !EmfEtfCemCompat.matchesExtension(normalized = path.toLowerCase(Locale.ROOT), modelFilesOnly)) continue;
            EmfEtfCemCompat.registerMatch(normalized);
        }
    }

    private static boolean matchesExtension(String path, boolean modelFilesOnly) {
        if (path.endsWith(".jem") || path.endsWith(".jpm")) {
            return true;
        }
        return !modelFilesOnly && (path.endsWith(".properties") || path.endsWith(".png"));
    }

    private static void registerMatch(String path) {
        String name = EmfEtfCemCompat.stripExtension(EmfEtfCemCompat.fileName(path));
        if (name.isEmpty()) {
            return;
        }
        if (EmfEtfCemCompat.containsAny(name, "ender_chest")) {
            DISABLED.add(ProxyPhysicsTarget.ENDER_CHEST);
        }
        if (EmfEtfCemCompat.containsAny(name, "trapped_chest", "trapped_chest_left", "trapped_chest_right")) {
            DISABLED.add(ProxyPhysicsTarget.TRAPPED_CHEST);
        }
        if (EmfEtfCemCompat.containsAny(name, "chest", "chest_left", "chest_right")) {
            DISABLED.add(ProxyPhysicsTarget.CHEST);
        }
        if (EmfEtfCemCompat.containsAny(name, "enchanting_table", "enchantment_table")) {
            DISABLED.add(ProxyPhysicsTarget.ENCHANTING_TABLE);
        }
        if (EmfEtfCemCompat.containsAny(name, "piston", "sticky_piston")) {
            DISABLED.add(ProxyPhysicsTarget.PISTON);
        }
    }

    private static boolean containsAny(String name, String ... tokens) {
        if (tokens == null || tokens.length == 0) {
            return false;
        }
        for (String token : tokens) {
            if (token == null || token.isBlank() || !name.contains(token)) continue;
            return true;
        }
        return false;
    }

    private static String fileName(String path) {
        int slash = path.lastIndexOf(47);
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf(46);
        return dot >= 0 ? name.substring(0, dot) : name;
    }

    public static enum ProxyPhysicsTarget {
        ENCHANTING_TABLE,
        PISTON,
        CHEST,
        TRAPPED_CHEST,
        ENDER_CHEST;

    }
}

