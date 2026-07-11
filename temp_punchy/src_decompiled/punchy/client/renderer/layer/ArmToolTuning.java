/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 */
package punchy.client.renderer.layer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import punchy.client.renderer.layer.PlayerArmModelLayer;

public final class ArmToolTuning {
    private static Profile RIGHT_GLOBAL;
    private static Profile LEFT_GLOBAL;
    private static final EnumMap<PlayerArmModelLayer.ToolKind, Profile> PROFILES_RIGHT;
    private static final EnumMap<PlayerArmModelLayer.ToolKind, Profile> PROFILES_LEFT;
    private static final EnumMap<PlayerArmModelLayer.ToolKind, TransformOverride> OVERRIDES_RIGHT;
    private static final EnumMap<PlayerArmModelLayer.ToolKind, TransformOverride> OVERRIDES_LEFT;
    private static final Map<class_2960, SpecificEntry> ITEM_SPECIFICS;
    private static final EnumMap<PlayerArmModelLayer.ToolKind, Boolean> ATTACHED_RIGHT;
    private static final EnumMap<PlayerArmModelLayer.ToolKind, Boolean> ATTACHED_LEFT;

    private ArmToolTuning() {
    }

    public static void resetDefaults() {
        RIGHT_GLOBAL = new Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        LEFT_GLOBAL = RIGHT_GLOBAL.mirror();
        PROFILES_RIGHT.clear();
        PROFILES_LEFT.clear();
        OVERRIDES_RIGHT.clear();
        OVERRIDES_LEFT.clear();
        ATTACHED_RIGHT.clear();
        ATTACHED_LEFT.clear();
        for (PlayerArmModelLayer.ToolKind kind : PlayerArmModelLayer.ToolKind.values()) {
            PROFILES_RIGHT.put(kind, ArmToolTuning.copyProfile(RIGHT_GLOBAL));
            PROFILES_LEFT.put(kind, ArmToolTuning.copyProfile(LEFT_GLOBAL));
            OVERRIDES_RIGHT.put(kind, new TransformOverride());
            OVERRIDES_LEFT.put(kind, new TransformOverride());
            ATTACHED_RIGHT.put(kind, true);
            ATTACHED_LEFT.put(kind, true);
        }
        ITEM_SPECIFICS.clear();
    }

    public static boolean isKindAttached(PlayerArmModelLayer.ToolKind kind, boolean left) {
        if (kind == null) {
            return true;
        }
        return (left ? ATTACHED_LEFT : ATTACHED_RIGHT).getOrDefault((Object)kind, true);
    }

    public static void markKindDetached(PlayerArmModelLayer.ToolKind kind, boolean left) {
        if (kind == null) {
            return;
        }
        (left ? ATTACHED_LEFT : ATTACHED_RIGHT).put(kind, false);
    }

    public static void markKindAttached(PlayerArmModelLayer.ToolKind kind, boolean left) {
        if (kind == null) {
            return;
        }
        (left ? ATTACHED_LEFT : ATTACHED_RIGHT).put(kind, true);
        ArmToolTuning.refreshKind(kind, left);
    }

    public static Profile get(PlayerArmModelLayer.ToolKind kind, boolean left) {
        if (kind == null) {
            return ArmToolTuning.getGlobal(left);
        }
        return (left ? PROFILES_LEFT : PROFILES_RIGHT).get((Object)kind);
    }

    public static Profile getGlobal(boolean left) {
        return left ? LEFT_GLOBAL : RIGHT_GLOBAL;
    }

    public static void apply(PlayerArmModelLayer.ToolKind kind, boolean left, Profile p) {
        if (p == null) {
            return;
        }
        if (kind == null) {
            ArmToolTuning.applyGlobal(left, p);
            return;
        }
        Profile dst = ArmToolTuning.get(kind, left);
        if (dst != null) {
            dst.copyFrom(p);
            ArmToolTuning.commitKind(kind, left);
        }
    }

    public static void applyGlobal(boolean left, Profile p) {
        if (p == null) {
            return;
        }
        ArmToolTuning.getGlobal(left).copyFrom(p);
        ArmToolTuning.refresh(left);
    }

    public static void commitGlobal(boolean left) {
        ArmToolTuning.refresh(left);
    }

    public static Profile snapshot(PlayerArmModelLayer.ToolKind kind, boolean left) {
        Profile s = ArmToolTuning.get(kind, left);
        return ArmToolTuning.copyProfile(s);
    }

    public static Profile snapshotGlobal(boolean left) {
        return ArmToolTuning.snapshot(null, left);
    }

    private static Profile copyProfile(Profile src) {
        return new Profile(src.scale, src.offX, src.offY, src.offZ, src.rotX, src.rotY, src.rotZ);
    }

    public static void commitKind(PlayerArmModelLayer.ToolKind kind, boolean left) {
        Profile base = ArmToolTuning.getGlobal(left);
        Profile dst = ArmToolTuning.get(kind, left);
        TransformOverride override = ArmToolTuning.getOverride(kind, left);
        override.scale = dst.scale - base.scale;
        override.offX = dst.offX - base.offX;
        override.offY = dst.offY - base.offY;
        override.offZ = dst.offZ - base.offZ;
        override.rotX = dst.rotX;
        override.rotY = dst.rotY;
        override.rotZ = dst.rotZ;
    }

    private static TransformOverride getOverride(PlayerArmModelLayer.ToolKind kind, boolean left) {
        return (left ? OVERRIDES_LEFT : OVERRIDES_RIGHT).get((Object)kind);
    }

    private static void refresh(boolean left) {
        for (PlayerArmModelLayer.ToolKind kind : PlayerArmModelLayer.ToolKind.values()) {
            ArmToolTuning.refreshKind(kind, left);
        }
    }

    private static void refreshKind(PlayerArmModelLayer.ToolKind kind, boolean left) {
        Profile dst = ArmToolTuning.get(kind, left);
        Profile base = ArmToolTuning.getGlobal(left);
        if (dst == null || base == null) {
            return;
        }
        if (ArmToolTuning.isKindAttached(kind, left)) {
            dst.copyFrom(base);
            return;
        }
        TransformOverride override = ArmToolTuning.getOverride(kind, left);
        if (override == null) {
            return;
        }
        dst.scale = base.scale + override.scale;
        dst.offX = base.offX + override.offX;
        dst.offY = base.offY + override.offY;
        dst.offZ = base.offZ + override.offZ;
        dst.rotX = override.rotX;
        dst.rotY = override.rotY;
        dst.rotZ = override.rotZ;
    }

    private static void refreshSpecifics(boolean left) {
        ITEM_SPECIFICS.values().forEach(entry -> ArmToolTuning.refreshSpecificEntry(entry, left));
    }

    private static void refreshSpecifics(PlayerArmModelLayer.ToolKind kind, boolean left) {
        ITEM_SPECIFICS.values().forEach(entry -> {
            if (entry != null && entry.kind == kind) {
                ArmToolTuning.refreshSpecificEntry(entry, left);
            }
        });
    }

    private static Profile getSpecificBase(boolean left) {
        Profile global = ArmToolTuning.getGlobal(left);
        if (global == null) {
            return new Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        return new Profile(global.scale, global.offX, global.offY, global.offZ, 0.0f, 0.0f, 0.0f);
    }

    private static void refreshSpecificEntry(SpecificEntry entry, boolean left) {
        if (entry == null) {
            return;
        }
        Profile dst = entry.get(left);
        if (dst == null) {
            return;
        }
        Profile base = ArmToolTuning.getSpecificBase(left);
        if (base == null) {
            return;
        }
        TransformOverride override = entry.getOverride(left);
        dst.scale = base.scale + override.scale;
        dst.offX = base.offX + override.offX;
        dst.offY = base.offY + override.offY;
        dst.offZ = base.offZ + override.offZ;
        dst.rotX = override.rotX;
        dst.rotY = override.rotY;
        dst.rotZ = override.rotZ;
    }

    public static void commitSpecific(class_2960 id, boolean left) {
        if (id == null) {
            return;
        }
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        if (entry == null) {
            return;
        }
        Profile profile = entry.get(left);
        if (profile == null) {
            return;
        }
        Profile base = ArmToolTuning.getSpecificBase(left);
        if (base == null) {
            return;
        }
        TransformOverride override = entry.getOverride(left);
        override.scale = profile.scale - base.scale;
        override.offX = profile.offX - base.offX;
        override.offY = profile.offY - base.offY;
        override.offZ = profile.offZ - base.offZ;
        override.rotX = profile.rotX;
        override.rotY = profile.rotY;
        override.rotZ = profile.rotZ;
        ArmToolTuning.refreshSpecificEntry(entry, left);
        ArmToolTuning.pruneSpecificEntry(id);
    }

    private static class_2960 idFor(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        return class_7923.field_41178.method_10221((Object)stack.method_7909());
    }

    public static Profile resolveProfile(class_1799 stack, PlayerArmModelLayer.ToolKind kind, boolean left) {
        Profile profile;
        SpecificEntry entry;
        class_2960 id = ArmToolTuning.idFor(stack);
        if (id != null && (entry = ITEM_SPECIFICS.get(id)) != null && (profile = entry.get(left)) != null) {
            ArmToolTuning.refreshSpecificEntry(entry, left);
            return profile;
        }
        return ArmToolTuning.get(kind, left);
    }

    public static PlayerArmModelLayer.ToolKind resolveSpecificKind(class_2960 id) {
        if (id == null) {
            return null;
        }
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        return entry != null ? entry.kind : null;
    }

    public static boolean hasSpecificProfile(class_1799 stack, boolean left) {
        class_2960 id = ArmToolTuning.idFor(stack);
        if (id == null) {
            return false;
        }
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        return entry != null && entry.get(left) != null;
    }

    public static void applySpecific(class_2960 id, PlayerArmModelLayer.ToolKind kind, Profile right, Profile left) {
        Profile base;
        if (id == null || kind == null) {
            return;
        }
        if (right == null && left == null) {
            ITEM_SPECIFICS.remove(id);
            return;
        }
        SpecificEntry entry = ITEM_SPECIFICS.computeIfAbsent(id, key -> new SpecificEntry(kind));
        entry.kind = kind;
        entry.right = right != null ? ArmToolTuning.copyProfile(right) : null;
        Profile profile = entry.left = left != null ? ArmToolTuning.copyProfile(left) : null;
        if (entry.right != null) {
            base = ArmToolTuning.getSpecificBase(false);
            if (base != null) {
                entry.rightOverride.scale = entry.right.scale - base.scale;
                entry.rightOverride.offX = entry.right.offX - base.offX;
                entry.rightOverride.offY = entry.right.offY - base.offY;
                entry.rightOverride.offZ = entry.right.offZ - base.offZ;
                entry.rightOverride.rotX = entry.right.rotX;
                entry.rightOverride.rotY = entry.right.rotY;
                entry.rightOverride.rotZ = entry.right.rotZ;
            }
            ArmToolTuning.refreshSpecificEntry(entry, false);
        }
        if (entry.left != null) {
            base = ArmToolTuning.getSpecificBase(true);
            if (base != null) {
                entry.leftOverride.scale = entry.left.scale - base.scale;
                entry.leftOverride.offX = entry.left.offX - base.offX;
                entry.leftOverride.offY = entry.left.offY - base.offY;
                entry.leftOverride.offZ = entry.left.offZ - base.offZ;
                entry.leftOverride.rotX = entry.left.rotX;
                entry.leftOverride.rotY = entry.left.rotY;
                entry.leftOverride.rotZ = entry.left.rotZ;
            }
            ArmToolTuning.refreshSpecificEntry(entry, true);
        }
        ArmToolTuning.pruneSpecificEntry(id);
    }

    public static Profile getSpecificProfile(class_2960 id, PlayerArmModelLayer.ToolKind kind, boolean left) {
        Profile profile;
        if (id == null) {
            return null;
        }
        SpecificEntry entry = ITEM_SPECIFICS.computeIfAbsent(id, key -> new SpecificEntry(kind));
        if (entry.kind == null) {
            entry.kind = kind;
        }
        if ((profile = entry.get(left)) == null) {
            Profile seed = kind != null ? ArmToolTuning.snapshot(kind, left) : ArmToolTuning.getSpecificBase(left);
            profile = seed != null ? ArmToolTuning.copyProfile(seed) : ArmToolTuning.copyProfile(ArmToolTuning.getSpecificBase(left));
            entry.set(left, profile);
            ArmToolTuning.commitSpecific(id, left);
        } else {
            ArmToolTuning.refreshSpecificEntry(entry, left);
        }
        return profile;
    }

    public static Map<class_2960, SpecificSnapshot> snapshotSpecifics() {
        HashMap<class_2960, SpecificSnapshot> copy = new HashMap<class_2960, SpecificSnapshot>();
        ITEM_SPECIFICS.forEach((id, entry) -> {
            ArmToolTuning.refreshSpecificEntry(entry, false);
            ArmToolTuning.refreshSpecificEntry(entry, true);
            copy.put((class_2960)id, new SpecificSnapshot(entry.kind, entry.right != null ? ArmToolTuning.copyProfile(entry.right) : null, entry.left != null ? ArmToolTuning.copyProfile(entry.left) : null));
        });
        return copy;
    }

    public static void clearSpecificOverride(class_2960 id) {
        if (id == null) {
            return;
        }
        ITEM_SPECIFICS.remove(id);
    }

    public static void clearSpecifics() {
        ITEM_SPECIFICS.clear();
    }

    private static void pruneSpecificEntry(class_2960 id) {
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        if (entry == null) {
            return;
        }
        if (entry.right == null && entry.left == null) {
            ITEM_SPECIFICS.remove(id);
        }
    }

    private static boolean profileMatchesBase(Profile profile, Profile base) {
        if (profile == null || base == null) {
            return false;
        }
        return ArmToolTuning.nearlyEqual(profile.scale, base.scale) && ArmToolTuning.nearlyEqual(profile.offX, base.offX) && ArmToolTuning.nearlyEqual(profile.offY, base.offY) && ArmToolTuning.nearlyEqual(profile.offZ, base.offZ) && ArmToolTuning.nearlyEqual(profile.rotX, base.rotX) && ArmToolTuning.nearlyEqual(profile.rotY, base.rotY) && ArmToolTuning.nearlyEqual(profile.rotZ, base.rotZ);
    }

    private static boolean nearlyEqual(float a, float b) {
        return Math.abs(a - b) <= 1.0E-6f;
    }

    static {
        PROFILES_RIGHT = new EnumMap(PlayerArmModelLayer.ToolKind.class);
        PROFILES_LEFT = new EnumMap(PlayerArmModelLayer.ToolKind.class);
        OVERRIDES_RIGHT = new EnumMap(PlayerArmModelLayer.ToolKind.class);
        OVERRIDES_LEFT = new EnumMap(PlayerArmModelLayer.ToolKind.class);
        ITEM_SPECIFICS = new HashMap<class_2960, SpecificEntry>();
        ATTACHED_RIGHT = new EnumMap(PlayerArmModelLayer.ToolKind.class);
        ATTACHED_LEFT = new EnumMap(PlayerArmModelLayer.ToolKind.class);
        ArmToolTuning.resetDefaults();
    }

    public static final class Profile {
        public float scale;
        public float offX;
        public float offY;
        public float offZ;
        public float rotX;
        public float rotY;
        public float rotZ;

        public Profile(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ) {
            this.scale = scale;
            this.offX = offX;
            this.offY = offY;
            this.offZ = offZ;
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
        }

        public Profile mirror() {
            return new Profile(this.scale, -this.offX, this.offY, this.offZ, this.rotX, -this.rotY, -this.rotZ);
        }

        public void copyFrom(Profile other) {
            this.scale = other.scale;
            this.offX = other.offX;
            this.offY = other.offY;
            this.offZ = other.offZ;
            this.rotX = other.rotX;
            this.rotY = other.rotY;
            this.rotZ = other.rotZ;
        }
    }

    private static final class TransformOverride {
        float scale;
        float offX;
        float offY;
        float offZ;
        float rotX;
        float rotY;
        float rotZ;

        private TransformOverride() {
        }

        void set(Profile base, Profile target) {
            this.scale = target.scale - base.scale;
            this.offX = target.offX - base.offX;
            this.offY = target.offY - base.offY;
            this.offZ = target.offZ - base.offZ;
            this.rotX = target.rotX - base.rotX;
            this.rotY = target.rotY - base.rotY;
            this.rotZ = target.rotZ - base.rotZ;
        }
    }

    public static final class SpecificEntry {
        PlayerArmModelLayer.ToolKind kind;
        Profile right;
        Profile left;
        TransformOverride rightOverride = new TransformOverride();
        TransformOverride leftOverride = new TransformOverride();

        SpecificEntry(PlayerArmModelLayer.ToolKind kind) {
            this.kind = kind;
        }

        Profile get(boolean leftHand) {
            return leftHand ? this.left : this.right;
        }

        void set(boolean leftHand, Profile profile) {
            if (leftHand) {
                this.left = profile;
            } else {
                this.right = profile;
            }
        }

        TransformOverride getOverride(boolean leftHand) {
            return leftHand ? this.leftOverride : this.rightOverride;
        }

        public PlayerArmModelLayer.ToolKind kind() {
            return this.kind;
        }

        public Profile right() {
            return this.right;
        }

        public Profile left() {
            return this.left;
        }
    }

    public static final class SpecificSnapshot {
        private final PlayerArmModelLayer.ToolKind kind;
        private final Profile right;
        private final Profile left;

        public SpecificSnapshot(PlayerArmModelLayer.ToolKind kind, Profile right, Profile left) {
            this.kind = kind;
            this.right = right;
            this.left = left;
        }

        public PlayerArmModelLayer.ToolKind kind() {
            return this.kind;
        }

        public Profile right() {
            return this.right;
        }

        public Profile left() {
            return this.left;
        }
    }
}

