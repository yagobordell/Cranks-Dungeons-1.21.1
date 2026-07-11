/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 *  net.minecraft.class_9334
 *  org.joml.Matrix3f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package punchy.client.render;

import com.google.gson.JsonElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import punchy.client.render.ToolKindResolver;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.config.PunchyTuningConfig;

public final class ToolTuning {
    private static Profile GLOBAL_RIGHT;
    private static Profile GLOBAL_LEFT;
    private static Profile EMPTY_R;
    private static Profile EMPTY_L;
    private static Profile AXE_R;
    private static Profile AXE_L;
    private static Profile PICKAXE_R;
    private static Profile PICKAXE_L;
    private static Profile HOE_R;
    private static Profile HOE_L;
    private static Profile SWORD_R;
    private static Profile SWORD_L;
    private static Profile SHOVEL_R;
    private static Profile SHOVEL_L;
    private static Profile SHIELD_R;
    private static Profile SHIELD_L;
    private static Profile BLOCK_R;
    private static Profile BLOCK_L;
    private static Profile FULL_BLOCK_R;
    private static Profile FULL_BLOCK_L;
    private static Profile BUILDING_BLOCKS_R;
    private static Profile BUILDING_BLOCKS_L;
    private static Profile COLORED_BLOCKS_R;
    private static Profile COLORED_BLOCKS_L;
    private static Profile NATURAL_BLOCKS_R;
    private static Profile NATURAL_BLOCKS_L;
    private static Profile FUNCTIONAL_BLOCKS_R;
    private static Profile FUNCTIONAL_BLOCKS_L;
    private static Profile CHEST_R;
    private static Profile CHEST_L;
    private static Profile FLOWER_PENDULUM_R;
    private static Profile FLOWER_PENDULUM_L;
    private static Profile BOW_R;
    private static Profile BOW_L;
    private static Profile ARROW_R;
    private static Profile ARROW_L;
    private static Profile CROSSBOW_R;
    private static Profile CROSSBOW_L;
    private static Profile SPYGLASS_R;
    private static Profile SPYGLASS_L;
    private static Profile FLINT_R;
    private static Profile FLINT_L;
    private static Profile FOOD_R;
    private static Profile FOOD_L;
    private static Profile SHUBBA_R;
    private static Profile SHUBBA_L;
    private static Profile MACE_R;
    private static Profile MACE_L;
    private static Profile SPEAR_R;
    private static Profile SPEAR_L;
    private static Profile TRIDENT_R;
    private static Profile TRIDENT_L;
    private static Profile FISHING_ROD_R;
    private static Profile FISHING_ROD_L;
    private static Profile TORCH_R;
    private static Profile TORCH_L;
    private static Profile LANTERN_R;
    private static Profile LANTERN_L;
    private static Profile REDSTONE_R;
    private static Profile REDSTONE_L;
    private static Profile CREATURE_BUCKET_R;
    private static Profile CREATURE_BUCKET_L;
    private static Profile BUCKET_R;
    private static Profile BUCKET_L;
    private static Profile MINECART_R;
    private static Profile MINECART_L;
    private static Profile BOAT_R;
    private static Profile BOAT_L;
    private static Profile BUTTON_PLATE_R;
    private static Profile BUTTON_PLATE_L;
    private static Profile TRAPDOOR_R;
    private static Profile TRAPDOOR_L;
    private static Profile CARPET_R;
    private static Profile CARPET_L;
    private static Profile BED_R;
    private static Profile BED_L;
    private static Profile INGREDIENT_R;
    private static Profile INGREDIENT_L;
    private static final Map<VanillaFirstPersonItemLayer.ToolKind, TransformOverride> OVERRIDE_RIGHT;
    private static final Map<VanillaFirstPersonItemLayer.ToolKind, TransformOverride> OVERRIDE_LEFT;
    private static final Map<class_2960, SpecificEntry> ITEM_SPECIFICS;
    private static final Map<class_2960, Map<Integer, SpecificEntry>> ITEM_SPECIFICS_BY_MODEL;
    private static final Map<class_2960, Map<String, SpecificEntry>> ITEM_SPECIFICS_BY_RENAMED;
    private static final Map<class_2960, Map<Integer, Map<String, SpecificEntry>>> ITEM_SPECIFICS_BY_MODEL_AND_RENAMED;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, Boolean> ATTACHED_RIGHT;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, Boolean> ATTACHED_LEFT;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, List<PunchyTuningConfig.CustomAnimationEntry>> GROUP_ANIMATIONS;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, List<PunchyTuningConfig.CustomAnimationEntry>> GROUP_ITEM_ANIMATIONS;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, List<String>> GROUP_PARTICLE_EMITTERS;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, List<PunchyTuningConfig.BetterCombatEntry>> GROUP_BETTER_COMBAT;
    private static final Map<String, List<PunchyTuningConfig.CustomAnimationEntry>> CUSTOM_ITEM_ANIMATION_CACHE;
    private static List<PunchyTuningConfig.CustomAnimationEntry> DEFAULT_ANIMATIONS;
    private static List<PunchyTuningConfig.BetterCombatEntry> DEFAULT_BETTER_COMBAT;

    private ToolTuning() {
    }

    private static void clearCustomItemAnimationCache() {
        CUSTOM_ITEM_ANIMATION_CACHE.clear();
    }

    public static RotationOrder rotationOrderForItemId(class_2960 itemId) {
        if (itemId == null) {
            return RotationOrder.XYZ;
        }
        String path = itemId.method_12832();
        if ("chest".equals(path) || "trapped_chest".equals(path) || "ender_chest".equals(path) || "piston".equals(path) || "sticky_piston".equals(path)) {
            return RotationOrder.ZXY;
        }
        return RotationOrder.XYZ;
    }

    public static void rotateProfileLocal(Profile profile, RotationOrder order, char axis, float deltaDegrees) {
        if (profile == null || order == null || deltaDegrees == 0.0f) {
            return;
        }
        Quaternionf rotation = ToolTuning.quaternionFromProfile(profile, order);
        float radians = (float)Math.toRadians(deltaDegrees);
        Quaternionf delta = new Quaternionf();
        switch (Character.toUpperCase(axis)) {
            case 'X': {
                delta.rotationX(radians);
                break;
            }
            case 'Y': {
                delta.rotationY(radians);
                break;
            }
            case 'Z': {
                delta.rotationZ(radians);
                break;
            }
            default: {
                return;
            }
        }
        rotation.mul((Quaternionfc)delta);
        Vector3f euler = ToolTuning.eulerFromQuaternion(rotation, order);
        profile.rotX = ToolTuning.normalizeDegrees((float)Math.toDegrees(euler.x));
        profile.rotY = ToolTuning.normalizeDegrees((float)Math.toDegrees(euler.y));
        profile.rotZ = ToolTuning.normalizeDegrees((float)Math.toDegrees(euler.z));
        profile.recomputeRadians();
    }

    private static Quaternionf quaternionFromProfile(Profile profile, RotationOrder order) {
        Quaternionf rotation = new Quaternionf().identity();
        float rotX = (float)Math.toRadians(profile.rotX);
        float rotY = (float)Math.toRadians(profile.rotY);
        float rotZ = (float)Math.toRadians(profile.rotZ);
        if (order == RotationOrder.ZXY) {
            rotation.rotateZ(rotZ).rotateX(rotX).rotateY(rotY);
        } else {
            rotation.rotateX(rotX).rotateY(rotY).rotateZ(rotZ);
        }
        return rotation;
    }

    private static Vector3f eulerFromQuaternion(Quaternionf rotation, RotationOrder order) {
        Matrix3f matrix = new Matrix3f().set((Quaternionfc)rotation);
        return order == RotationOrder.ZXY ? ToolTuning.extractEulerZxy(matrix) : ToolTuning.extractEulerXyz(matrix);
    }

    private static Vector3f extractEulerXyz(Matrix3f matrix) {
        float z;
        float x;
        float y = (float)Math.asin(ToolTuning.clampUnit(matrix.m02()));
        float cy = (float)Math.cos(y);
        if (Math.abs(cy) > 1.0E-5f) {
            x = (float)Math.atan2(-matrix.m12(), matrix.m22());
            z = (float)Math.atan2(-matrix.m01(), matrix.m00());
        } else {
            x = (float)Math.atan2(matrix.m21(), matrix.m11());
            z = 0.0f;
        }
        return new Vector3f(x, y, z);
    }

    private static Vector3f extractEulerZxy(Matrix3f matrix) {
        float z;
        float y;
        float x = (float)Math.asin(ToolTuning.clampUnit(matrix.m21()));
        float cx = (float)Math.cos(x);
        if (Math.abs(cx) > 1.0E-5f) {
            y = (float)Math.atan2(-matrix.m20(), matrix.m22());
            z = (float)Math.atan2(-matrix.m01(), matrix.m11());
        } else {
            y = 0.0f;
            z = (float)Math.atan2(matrix.m10(), matrix.m00());
        }
        return new Vector3f(x, y, z);
    }

    private static float clampUnit(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }

    private static float normalizeDegrees(float degrees) {
        float normalized = degrees % 360.0f;
        if (normalized > 180.0f) {
            normalized -= 360.0f;
        } else if (normalized < -180.0f) {
            normalized += 360.0f;
        }
        return normalized;
    }

    public static void resetDefaults() {
        ToolTuning.resetAttachments();
        GROUP_ANIMATIONS.clear();
        GROUP_ITEM_ANIMATIONS.clear();
        GROUP_PARTICLE_EMITTERS.clear();
        GROUP_BETTER_COMBAT.clear();
        ToolTuning.clearCustomItemAnimationCache();
        DEFAULT_ANIMATIONS = null;
        DEFAULT_BETTER_COMBAT = null;
        GLOBAL_RIGHT = new Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        GLOBAL_LEFT = GLOBAL_RIGHT.mirror();
        EMPTY_R = ToolTuning.blankProfile();
        EMPTY_L = ToolTuning.blankProfile();
        AXE_R = ToolTuning.blankProfile();
        AXE_L = ToolTuning.blankProfile();
        PICKAXE_R = ToolTuning.blankProfile();
        PICKAXE_L = ToolTuning.blankProfile();
        HOE_R = ToolTuning.blankProfile();
        HOE_L = ToolTuning.blankProfile();
        SWORD_R = ToolTuning.blankProfile();
        SWORD_L = ToolTuning.blankProfile();
        SHOVEL_R = ToolTuning.blankProfile();
        SHOVEL_L = ToolTuning.blankProfile();
        SHIELD_R = ToolTuning.blankProfile();
        SHIELD_L = ToolTuning.blankProfile();
        BLOCK_R = ToolTuning.blankProfile();
        BLOCK_L = ToolTuning.blankProfile();
        FULL_BLOCK_R = ToolTuning.blankProfile();
        FULL_BLOCK_L = ToolTuning.blankProfile();
        BUILDING_BLOCKS_R = ToolTuning.blankProfile();
        BUILDING_BLOCKS_L = ToolTuning.blankProfile();
        COLORED_BLOCKS_R = ToolTuning.blankProfile();
        COLORED_BLOCKS_L = ToolTuning.blankProfile();
        NATURAL_BLOCKS_R = ToolTuning.blankProfile();
        NATURAL_BLOCKS_L = ToolTuning.blankProfile();
        FUNCTIONAL_BLOCKS_R = ToolTuning.blankProfile();
        FUNCTIONAL_BLOCKS_L = ToolTuning.blankProfile();
        CHEST_R = ToolTuning.blankProfile();
        CHEST_L = ToolTuning.blankProfile();
        FLOWER_PENDULUM_R = new Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, PhysicsMode.FORWARD_PENDULUM);
        FLOWER_PENDULUM_L = FLOWER_PENDULUM_R.mirror();
        TORCH_R = ToolTuning.blankProfile();
        TORCH_L = ToolTuning.blankProfile();
        LANTERN_R = ToolTuning.blankProfile();
        LANTERN_L = LANTERN_R.mirror();
        REDSTONE_R = ToolTuning.blankProfile();
        REDSTONE_L = ToolTuning.blankProfile();
        CREATURE_BUCKET_R = ToolTuning.blankProfile();
        CREATURE_BUCKET_L = ToolTuning.blankProfile();
        BUCKET_R = ToolTuning.blankProfile();
        BUCKET_L = ToolTuning.blankProfile();
        MINECART_R = ToolTuning.blankProfile();
        MINECART_L = ToolTuning.blankProfile();
        BOAT_R = ToolTuning.blankProfile();
        BOAT_L = ToolTuning.blankProfile();
        BUTTON_PLATE_R = ToolTuning.blankProfile();
        BUTTON_PLATE_L = ToolTuning.blankProfile();
        TRAPDOOR_R = ToolTuning.blankProfile();
        TRAPDOOR_L = ToolTuning.blankProfile();
        CARPET_R = ToolTuning.blankProfile();
        CARPET_L = ToolTuning.blankProfile();
        BED_R = ToolTuning.blankProfile();
        BED_L = ToolTuning.blankProfile();
        INGREDIENT_R = ToolTuning.blankProfile();
        INGREDIENT_L = ToolTuning.blankProfile();
        BOW_R = ToolTuning.blankProfile();
        BOW_L = ToolTuning.blankProfile();
        ARROW_R = ToolTuning.blankProfile();
        ARROW_L = ToolTuning.blankProfile();
        CROSSBOW_R = ToolTuning.blankProfile();
        CROSSBOW_L = ToolTuning.blankProfile();
        SPYGLASS_R = ToolTuning.blankProfile();
        SPYGLASS_L = ToolTuning.blankProfile();
        FLINT_R = ToolTuning.blankProfile();
        FLINT_L = ToolTuning.blankProfile();
        FOOD_R = ToolTuning.blankProfile();
        FOOD_L = ToolTuning.blankProfile();
        SHUBBA_R = ToolTuning.blankProfile();
        SHUBBA_L = ToolTuning.blankProfile();
        MACE_R = ToolTuning.blankProfile();
        MACE_L = ToolTuning.blankProfile();
        SPEAR_R = ToolTuning.blankProfile();
        SPEAR_L = ToolTuning.blankProfile();
        TRIDENT_R = ToolTuning.blankProfile();
        TRIDENT_L = ToolTuning.blankProfile();
        FISHING_ROD_R = ToolTuning.blankProfile();
        FISHING_ROD_L = ToolTuning.blankProfile();
        OVERRIDE_RIGHT.clear();
        OVERRIDE_LEFT.clear();
        for (VanillaFirstPersonItemLayer.ToolKind kind : VanillaFirstPersonItemLayer.ToolKind.values()) {
            OVERRIDE_RIGHT.put(kind, new TransformOverride());
            OVERRIDE_LEFT.put(kind, new TransformOverride());
        }
        ITEM_SPECIFICS.clear();
        ToolTuning.refreshAll();
    }

    public static void applyGroupAnimation(VanillaFirstPersonItemLayer.ToolKind kind, List<PunchyTuningConfig.CustomAnimationEntry> anims) {
        if (kind != null && anims != null) {
            GROUP_ANIMATIONS.put(kind, anims);
            ToolTuning.clearCustomItemAnimationCache();
        }
    }

    public static void setDefaultAnimations(List<PunchyTuningConfig.CustomAnimationEntry> anims) {
        if (anims == null || anims.isEmpty()) {
            DEFAULT_ANIMATIONS = null;
            ToolTuning.clearCustomItemAnimationCache();
            return;
        }
        DEFAULT_ANIMATIONS = anims;
        ToolTuning.clearCustomItemAnimationCache();
    }

    public static void setDefaultBetterCombat(List<PunchyTuningConfig.BetterCombatEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            DEFAULT_BETTER_COMBAT = null;
            return;
        }
        DEFAULT_BETTER_COMBAT = entries;
    }

    public static void setGroupAnimation(VanillaFirstPersonItemLayer.ToolKind kind, List<PunchyTuningConfig.CustomAnimationEntry> anims) {
        if (kind == null) {
            return;
        }
        if (anims == null || anims.isEmpty()) {
            GROUP_ANIMATIONS.remove((Object)kind);
            ToolTuning.clearCustomItemAnimationCache();
            return;
        }
        GROUP_ANIMATIONS.put(kind, anims);
        ToolTuning.clearCustomItemAnimationCache();
    }

    public static void applyGroupItemAnimation(VanillaFirstPersonItemLayer.ToolKind kind, List<PunchyTuningConfig.CustomAnimationEntry> anims) {
        if (kind != null && anims != null) {
            GROUP_ITEM_ANIMATIONS.put(kind, anims);
            ToolTuning.clearCustomItemAnimationCache();
        }
    }

    public static void setGroupItemAnimation(VanillaFirstPersonItemLayer.ToolKind kind, List<PunchyTuningConfig.CustomAnimationEntry> anims) {
        if (kind == null) {
            return;
        }
        if (anims == null || anims.isEmpty()) {
            GROUP_ITEM_ANIMATIONS.remove((Object)kind);
            ToolTuning.clearCustomItemAnimationCache();
            return;
        }
        GROUP_ITEM_ANIMATIONS.put(kind, anims);
        ToolTuning.clearCustomItemAnimationCache();
    }

    public static void applyGroupParticleEmitters(VanillaFirstPersonItemLayer.ToolKind kind, List<String> emitters) {
        if (kind != null && emitters != null) {
            GROUP_PARTICLE_EMITTERS.put(kind, new ArrayList<String>(emitters));
        }
    }

    public static void applyGroupBetterCombat(VanillaFirstPersonItemLayer.ToolKind kind, List<PunchyTuningConfig.BetterCombatEntry> anims) {
        if (kind != null && anims != null) {
            GROUP_BETTER_COMBAT.put(kind, anims);
        }
    }

    public static void resetAttachments() {
        for (VanillaFirstPersonItemLayer.ToolKind kind : VanillaFirstPersonItemLayer.ToolKind.values()) {
            ATTACHED_RIGHT.put(kind, true);
            ATTACHED_LEFT.put(kind, true);
        }
    }

    private static void setAttachmentState(VanillaFirstPersonItemLayer.ToolKind kind, boolean left, boolean attached) {
        if (kind == null) {
            return;
        }
        EnumMap<VanillaFirstPersonItemLayer.ToolKind, Boolean> map = left ? ATTACHED_LEFT : ATTACHED_RIGHT;
        map.put(kind, attached);
    }

    public static boolean isKindAttached(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        if (kind == null) {
            return true;
        }
        return (left ? ATTACHED_LEFT : ATTACHED_RIGHT).getOrDefault((Object)kind, true);
    }

    public static void markKindDetached(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        ToolTuning.setAttachmentState(kind, left, false);
    }

    public static void markKindAttached(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        ToolTuning.setAttachmentState(kind, left, true);
        ToolTuning.refreshKind(kind, left);
    }

    private static Profile blankProfile() {
        return new Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public static Profile get(VanillaFirstPersonItemLayer.ToolKind kind) {
        return ToolTuning.get(kind, false);
    }

    public static Profile get(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        return switch (kind) {
            default -> throw new MatchException(null, null);
            case VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND -> {
                if (left) {
                    yield EMPTY_L;
                }
                yield EMPTY_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.AXE -> {
                if (left) {
                    yield AXE_L;
                }
                yield AXE_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.PICKAXE -> {
                if (left) {
                    yield PICKAXE_L;
                }
                yield PICKAXE_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.HOE -> {
                if (left) {
                    yield HOE_L;
                }
                yield HOE_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.SWORD -> {
                if (left) {
                    yield SWORD_L;
                }
                yield SWORD_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.SHOVEL -> {
                if (left) {
                    yield SHOVEL_L;
                }
                yield SHOVEL_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.SHIELD -> {
                if (left) {
                    yield SHIELD_L;
                }
                yield SHIELD_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BLOCK -> {
                if (left) {
                    yield BLOCK_L;
                }
                yield BLOCK_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.FULL_BLOCK -> {
                if (left) {
                    yield FULL_BLOCK_L;
                }
                yield FULL_BLOCK_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BUILDING_BLOCKS -> {
                if (left) {
                    yield BUILDING_BLOCKS_L;
                }
                yield BUILDING_BLOCKS_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.COLORED_BLOCKS -> {
                if (left) {
                    yield COLORED_BLOCKS_L;
                }
                yield COLORED_BLOCKS_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.NATURAL_BLOCKS -> {
                if (left) {
                    yield NATURAL_BLOCKS_L;
                }
                yield NATURAL_BLOCKS_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.FUNCTIONAL_BLOCKS -> {
                if (left) {
                    yield FUNCTIONAL_BLOCKS_L;
                }
                yield FUNCTIONAL_BLOCKS_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.CHEST -> {
                if (left) {
                    yield CHEST_L;
                }
                yield CHEST_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.FLOWER_PENDULUM -> {
                if (left) {
                    yield FLOWER_PENDULUM_L;
                }
                yield FLOWER_PENDULUM_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.CREATURE_BUCKET -> {
                if (left) {
                    yield CREATURE_BUCKET_L;
                }
                yield CREATURE_BUCKET_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BUCKET -> {
                if (left) {
                    yield BUCKET_L;
                }
                yield BUCKET_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.MINECART -> {
                if (left) {
                    yield MINECART_L;
                }
                yield MINECART_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BOAT -> {
                if (left) {
                    yield BOAT_L;
                }
                yield BOAT_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BUTTON_PLATE -> {
                if (left) {
                    yield BUTTON_PLATE_L;
                }
                yield BUTTON_PLATE_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.TRAPDOOR -> {
                if (left) {
                    yield TRAPDOOR_L;
                }
                yield TRAPDOOR_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.CARPET -> {
                if (left) {
                    yield CARPET_L;
                }
                yield CARPET_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BED -> {
                if (left) {
                    yield BED_L;
                }
                yield BED_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.TORCH -> {
                if (left) {
                    yield TORCH_L;
                }
                yield TORCH_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.LANTERN -> {
                if (left) {
                    yield LANTERN_L;
                }
                yield LANTERN_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.REDSTONE -> {
                if (left) {
                    yield REDSTONE_L;
                }
                yield REDSTONE_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.INGREDIENT -> {
                if (left) {
                    yield INGREDIENT_L;
                }
                yield INGREDIENT_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.BOW -> {
                if (left) {
                    yield BOW_L;
                }
                yield BOW_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.ARROW -> {
                if (left) {
                    yield ARROW_L;
                }
                yield ARROW_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.CROSSBOW -> {
                if (left) {
                    yield CROSSBOW_L;
                }
                yield CROSSBOW_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.SPYGLASS -> {
                if (left) {
                    yield SPYGLASS_L;
                }
                yield SPYGLASS_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.FLINT -> {
                if (left) {
                    yield FLINT_L;
                }
                yield FLINT_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.FOOD -> {
                if (left) {
                    yield FOOD_L;
                }
                yield FOOD_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.SHUBBA_DUCK -> {
                if (left) {
                    yield SHUBBA_L;
                }
                yield SHUBBA_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.MACE -> {
                if (left) {
                    yield MACE_L;
                }
                yield MACE_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.SPEAR -> {
                if (left) {
                    yield SPEAR_L;
                }
                yield SPEAR_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.TRIDENT -> {
                if (left) {
                    yield TRIDENT_L;
                }
                yield TRIDENT_R;
            }
            case VanillaFirstPersonItemLayer.ToolKind.FISHING_ROD -> left ? FISHING_ROD_L : FISHING_ROD_R;
        };
    }

    public static Profile getGlobal(boolean left) {
        return left ? GLOBAL_LEFT : GLOBAL_RIGHT;
    }

    public static void applyGlobal(boolean left, Profile p) {
        if (p == null) {
            return;
        }
        Profile dst = ToolTuning.getGlobal(left);
        ToolTuning.copyAll(p, dst);
        dst.recomputeRadians();
        ToolTuning.refresh(left);
    }

    public static Profile snapshotGlobal(boolean left) {
        Profile base = ToolTuning.getGlobal(left);
        return ToolTuning.copyProfile(base);
    }

    public static void commitGlobal(boolean left) {
        ToolTuning.getGlobal(left).recomputeRadians();
        ToolTuning.refresh(left);
    }

    public static void apply(VanillaFirstPersonItemLayer.ToolKind kind, boolean left, Profile p) {
        if (p == null) {
            return;
        }
        Profile dst = ToolTuning.get(kind, left);
        ToolTuning.copyAll(p, dst);
        dst.recomputeRadians();
        ToolTuning.commitKind(kind, left);
    }

    public static void commitKind(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Profile base = ToolTuning.getGlobal(left);
        Profile dst = ToolTuning.get(kind, left);
        TransformOverride override = ToolTuning.getOverride(kind, left);
        override.set(base, dst);
        ToolTuning.refreshKind(kind, left);
    }

    public static Profile snapshot(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Profile s = ToolTuning.get(kind, left);
        return ToolTuning.copyProfile(s);
    }

    private static void refresh(boolean left) {
        for (VanillaFirstPersonItemLayer.ToolKind kind : VanillaFirstPersonItemLayer.ToolKind.values()) {
            ToolTuning.refreshKind(kind, left);
        }
    }

    public static void refreshAll() {
        ToolTuning.refresh(false);
        ToolTuning.refresh(true);
    }

    private static void refreshKind(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Profile dst = ToolTuning.get(kind, left);
        if (dst == null) {
            return;
        }
        if (!ToolTuning.isKindAttached(kind, left)) {
            dst.recomputeRadians();
            return;
        }
        Profile base = ToolTuning.getGlobal(left);
        TransformOverride override = ToolTuning.getOverride(kind, left);
        if (base == null || override == null) {
            return;
        }
        ToolTuning.copyAnchor(base, dst);
        dst.scale += override.scale;
        dst.offX += override.offX;
        dst.offY += override.offY;
        dst.offZ += override.offZ;
        dst.pivotX += override.pivotX;
        dst.pivotY += override.pivotY;
        dst.pivotZ += override.pivotZ;
        dst.rotX = base.rotX + override.rotX;
        dst.rotY = base.rotY + override.rotY;
        dst.rotZ = base.rotZ + override.rotZ;
        dst.recomputeRadians();
    }

    private static TransformOverride getOverride(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Map<VanillaFirstPersonItemLayer.ToolKind, TransformOverride> map = left ? OVERRIDE_LEFT : OVERRIDE_RIGHT;
        TransformOverride ovr = map.get((Object)kind);
        if (ovr == null) {
            ovr = new TransformOverride();
            map.put(kind, ovr);
        }
        return ovr;
    }

    private static void copyAnchor(Profile src, Profile dst) {
        dst.scale = src.scale;
        dst.offX = src.offX;
        dst.offY = src.offY;
        dst.offZ = src.offZ;
        dst.pivotX = src.pivotX;
        dst.pivotY = src.pivotY;
        dst.pivotZ = src.pivotZ;
    }

    private static void copyAll(Profile src, Profile dst) {
        ToolTuning.copyAnchor(src, dst);
        dst.rotX = src.rotX;
        dst.rotY = src.rotY;
        dst.rotZ = src.rotZ;
        dst.disableSprintLateralTilt = src.disableSprintLateralTilt;
        dst.keepArmTuning = src.keepArmTuning;
        dst.keepTriggerArmTuning = src.keepTriggerArmTuning;
        dst.physicsMode = src.physicsMode;
        dst.physicsSettings = src.physicsSettings != null ? src.physicsSettings.copy() : null;
    }

    private static Profile copyProfile(Profile src) {
        Profile copy = new Profile(src.scale, src.offX, src.offY, src.offZ, src.rotX, src.rotY, src.rotZ, src.pivotX, src.pivotY, src.pivotZ, src.physicsMode);
        copy.disableSprintLateralTilt = src.disableSprintLateralTilt;
        copy.keepArmTuning = src.keepArmTuning;
        copy.keepTriggerArmTuning = src.keepTriggerArmTuning;
        if (src.physicsSettings != null) {
            copy.physicsSettings = src.physicsSettings.copy();
        }
        return copy;
    }

    private static Profile createSpecificSeedProfile(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Profile seeded = ToolTuning.copyProfile(ToolTuning.snapshot(kind, left));
        if (kind == VanillaFirstPersonItemLayer.ToolKind.BUCKET || kind == VanillaFirstPersonItemLayer.ToolKind.CREATURE_BUCKET) {
            seeded.physicsMode = PhysicsMode.NONE;
            seeded.physicsSettings = null;
        }
        return seeded;
    }

    private static class_2960 idFor(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        return class_7923.field_41178.method_10221((Object)stack.method_7909());
    }

    public static VanillaFirstPersonItemLayer.ToolKind resolveOverriddenKind(class_1799 stack) {
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && entry.kind != null) {
            return entry.kind;
        }
        return null;
    }

    public static VanillaFirstPersonItemLayer.ToolKind resolveSpecificKind(class_2960 id) {
        if (id == null) {
            return null;
        }
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        return entry != null ? entry.kind : null;
    }

    public static Profile resolveProfile(class_1799 stack, VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Profile profile;
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && (profile = entry.get(left)) != null) {
            return profile;
        }
        return ToolTuning.get(kind, left);
    }

    public static boolean hasSpecificProfile(class_1799 stack) {
        return ToolTuning.getSpecificEntry(stack) != null;
    }

    public static Profile getSpecificProfileIfPresent(class_2960 id, boolean left) {
        if (id == null) {
            return null;
        }
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        return entry != null ? entry.get(left) : null;
    }

    public static Profile getSpecificProfileOnly(class_2960 id, boolean left) {
        return ToolTuning.getSpecificProfileIfPresent(id, left);
    }

    public static Profile getSpecificProfile(class_2960 id, VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        Profile profile;
        if (id == null) {
            return null;
        }
        SpecificEntry entry = ITEM_SPECIFICS.computeIfAbsent(id, key -> new SpecificEntry(kind));
        if (entry.kind == null) {
            entry.kind = kind;
        }
        if ((profile = entry.get(left)) == null) {
            profile = ToolTuning.createSpecificSeedProfile(kind, left);
            entry.set(left, profile);
        }
        return profile;
    }

    public static Profile getSpecificProfile(class_1799 stack, VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (id == null) {
            return null;
        }
        SpecificEntry resolved = ToolTuning.getSpecificEntry(stack);
        if (resolved != null) {
            Profile existing;
            if (resolved.kind == null) {
                resolved.kind = kind;
            }
            if ((existing = resolved.get(left)) != null) {
                return existing;
            }
        }
        return ToolTuning.getSpecificProfile(id, kind, left);
    }

    public static Map<class_2960, SpecificSnapshot> snapshotSpecifics() {
        HashMap<class_2960, SpecificSnapshot> copy = new HashMap<class_2960, SpecificSnapshot>();
        ITEM_SPECIFICS.forEach((id, entry) -> copy.put((class_2960)id, new SpecificSnapshot(entry.kind, entry.right != null ? ToolTuning.copyProfile(entry.right) : null, entry.left != null ? ToolTuning.copyProfile(entry.left) : null)));
        return copy;
    }

    public static List<SpecificSnapshotDetailed> snapshotSpecificsDetailed() {
        ArrayList<SpecificSnapshotDetailed> out = new ArrayList<SpecificSnapshotDetailed>();
        ITEM_SPECIFICS.forEach((id, entry) -> {
            SpecificSnapshotDetailed snap = ToolTuning.toDetailedSnapshot(id, null, null, entry);
            if (snap != null) {
                out.add(snap);
            }
        });
        ITEM_SPECIFICS_BY_MODEL.forEach((id, perModel) -> {
            if (perModel == null) {
                return;
            }
            perModel.forEach((cmd, entry) -> {
                SpecificSnapshotDetailed snap = ToolTuning.toDetailedSnapshot(id, cmd, null, entry);
                if (snap != null) {
                    out.add(snap);
                }
            });
        });
        ITEM_SPECIFICS_BY_RENAMED.forEach((id, perRename) -> {
            if (perRename == null) {
                return;
            }
            perRename.forEach((renamed, entry) -> {
                SpecificSnapshotDetailed snap = ToolTuning.toDetailedSnapshot(id, null, renamed, entry);
                if (snap != null) {
                    out.add(snap);
                }
            });
        });
        ITEM_SPECIFICS_BY_MODEL_AND_RENAMED.forEach((id, perModel) -> {
            if (perModel == null) {
                return;
            }
            perModel.forEach((cmd, perRename) -> {
                if (perRename == null) {
                    return;
                }
                perRename.forEach((renamed, entry) -> {
                    SpecificSnapshotDetailed snap = ToolTuning.toDetailedSnapshot(id, cmd, renamed, entry);
                    if (snap != null) {
                        out.add(snap);
                    }
                });
            });
        });
        return out;
    }

    private static SpecificSnapshotDetailed toDetailedSnapshot(class_2960 id, Integer cmd, String renamed, SpecificEntry entry) {
        if (id == null || entry == null) {
            return null;
        }
        return new SpecificSnapshotDetailed(id, cmd, renamed, entry.model, entry.kind, entry.right != null ? ToolTuning.copyProfile(entry.right) : null, entry.left != null ? ToolTuning.copyProfile(entry.left) : null);
    }

    public static List<PunchyTuningConfig.CustomAnimationEntry> getCustomAnimation(class_1799 stack) {
        return ToolTuning.getCustomAnimationInternal(stack, true);
    }

    public static List<PunchyTuningConfig.CustomAnimationEntry> getCustomAnimationWithoutDefaults(class_1799 stack) {
        return ToolTuning.getCustomAnimationInternal(stack, false);
    }

    private static List<PunchyTuningConfig.CustomAnimationEntry> getCustomAnimationInternal(class_1799 stack, boolean includeDefaults) {
        List<PunchyTuningConfig.CustomAnimationEntry> group;
        VanillaFirstPersonItemLayer.ToolKind kind;
        List<PunchyTuningConfig.CustomAnimationEntry> resolved = null;
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && entry.customAnimation != null) {
            resolved = entry.customAnimation;
        }
        if (resolved == null && (kind = ToolKindResolver.resolveItemKind(stack, null)) != null && (group = GROUP_ANIMATIONS.get((Object)kind)) != null) {
            resolved = group;
        }
        return includeDefaults ? ToolTuning.mergeAnimationEntries(resolved, DEFAULT_ANIMATIONS) : resolved;
    }

    public static boolean hasCustomAnimationType(class_1799 stack, String targetType) {
        if (targetType == null || targetType.isBlank()) {
            return false;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return false;
        }
        String normalizedTarget = targetType.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            String normalizedType;
            if (entry == null || entry.type == null || !normalizedTarget.equals(normalizedType = entry.type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", ""))) continue;
            return true;
        }
        return false;
    }

    public static List<PunchyTuningConfig.CustomAnimationEntry> getCustomItemAnimation(class_1799 stack) {
        VanillaFirstPersonItemLayer.ToolKind kind;
        String cacheKey = ToolTuning.customItemAnimationCacheKey(stack);
        if (cacheKey != null && CUSTOM_ITEM_ANIMATION_CACHE.containsKey(cacheKey)) {
            return CUSTOM_ITEM_ANIMATION_CACHE.get(cacheKey);
        }
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        List<PunchyTuningConfig.CustomAnimationEntry> resolved = null;
        if (entry != null) {
            if (entry.customItemAnimations != null) {
                resolved = entry.customItemAnimations;
            } else if (entry.customAnimation != null) {
                resolved = entry.customAnimation;
            }
        }
        if (resolved == null && (kind = ToolKindResolver.resolveItemKind(stack, null)) != null) {
            List<PunchyTuningConfig.CustomAnimationEntry> itemAnimations = GROUP_ITEM_ANIMATIONS.get((Object)kind);
            resolved = itemAnimations != null ? itemAnimations : GROUP_ANIMATIONS.get((Object)kind);
        }
        List<PunchyTuningConfig.CustomAnimationEntry> merged = ToolTuning.mergeAnimationEntries(resolved, DEFAULT_ANIMATIONS);
        if (cacheKey != null) {
            CUSTOM_ITEM_ANIMATION_CACHE.put(cacheKey, merged);
        }
        return merged;
    }

    private static String customItemAnimationCacheKey(class_1799 stack) {
        class_2960 id = ToolTuning.idFor(stack);
        if (id == null) {
            return null;
        }
        Integer cmd = ToolTuning.resolveCustomModelData(stack);
        String renamed = ToolTuning.resolveStackRenamedKey(stack);
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, null);
        return String.valueOf(id) + "|" + String.valueOf(cmd == null ? "" : cmd) + "|" + (renamed == null ? "" : renamed) + "|" + (kind == null ? "" : kind.name());
    }

    private static List<PunchyTuningConfig.CustomAnimationEntry> mergeAnimationEntries(List<PunchyTuningConfig.CustomAnimationEntry> primary, List<PunchyTuningConfig.CustomAnimationEntry> defaults) {
        String key;
        if ((primary == null || primary.isEmpty()) && (defaults == null || defaults.isEmpty())) {
            return null;
        }
        if (primary == null || primary.isEmpty()) {
            return defaults;
        }
        if (defaults == null || defaults.isEmpty()) {
            return primary;
        }
        LinkedHashMap<String, PunchyTuningConfig.CustomAnimationEntry> merged = new LinkedHashMap<String, PunchyTuningConfig.CustomAnimationEntry>();
        for (PunchyTuningConfig.CustomAnimationEntry entry : primary) {
            key = ToolTuning.animationMergeKey(entry);
            if (key == null) continue;
            merged.put(key, entry);
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : defaults) {
            key = ToolTuning.animationMergeKey(entry);
            if (key == null || merged.containsKey(key)) continue;
            merged.put(key, entry);
        }
        return new ArrayList<PunchyTuningConfig.CustomAnimationEntry>(merged.values());
    }

    private static String animationMergeKey(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null || entry.type == null) {
            return null;
        }
        String type = entry.type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        String key = entry.key == null ? "" : entry.key.trim().toLowerCase(Locale.ROOT);
        return type + "|" + key;
    }

    public static List<PunchyTuningConfig.BetterCombatEntry> getBetterCombat(class_1799 stack) {
        List<PunchyTuningConfig.BetterCombatEntry> group;
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && entry.betterCombat != null) {
            return entry.betterCombat;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, null);
        if (kind != null && (group = GROUP_BETTER_COMBAT.get((Object)kind)) != null && !group.isEmpty()) {
            return group;
        }
        return DEFAULT_BETTER_COMBAT;
    }

    public static List<String> getParticleEmitters(class_1799 stack) {
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && entry.particleEmitters != null) {
            return entry.particleEmitters;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, null);
        if (kind != null) {
            return GROUP_PARTICLE_EMITTERS.get((Object)kind);
        }
        return null;
    }

    public static boolean shouldHideAboveInterface(class_1799 stack) {
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        return entry != null && Boolean.TRUE.equals(entry.hideAboveInterface);
    }

    public static JsonElement getSpecificBonePhysics(class_1799 stack) {
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry == null || entry.bonePhysics == null || entry.bonePhysics.isJsonNull()) {
            return null;
        }
        return entry.bonePhysics.deepCopy();
    }

    public static List<PunchyTuningConfig.CustomAnimationEntry> getSpecificCustomAnimation(class_1799 stack) {
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && entry.customAnimation != null) {
            return entry.customAnimation;
        }
        return null;
    }

    public static List<PunchyTuningConfig.CustomAnimationEntry> getSpecificCustomItemAnimation(class_1799 stack) {
        SpecificEntry entry = ToolTuning.getSpecificEntry(stack);
        if (entry != null && entry.customItemAnimations != null) {
            return entry.customItemAnimations;
        }
        return null;
    }

    public static void applySpecific(class_2960 id, VanillaFirstPersonItemLayer.ToolKind kind, String model, List<PunchyTuningConfig.CustomAnimationEntry> customAnim, List<PunchyTuningConfig.CustomAnimationEntry> customItemAnim, List<String> particleEmitters, List<PunchyTuningConfig.BetterCombatEntry> betterCombat, Boolean hideAboveInterface, Profile right, Profile left) {
        ToolTuning.applySpecific(id, kind, null, model, customAnim, customItemAnim, particleEmitters, betterCombat, hideAboveInterface, null, right, left);
    }

    public static void applySpecific(class_2960 id, VanillaFirstPersonItemLayer.ToolKind kind, String renamed, String model, List<PunchyTuningConfig.CustomAnimationEntry> customAnim, List<PunchyTuningConfig.CustomAnimationEntry> customItemAnim, List<String> particleEmitters, List<PunchyTuningConfig.BetterCombatEntry> betterCombat, Boolean hideAboveInterface, Profile right, Profile left) {
        ToolTuning.applySpecific(id, kind, renamed, model, customAnim, customItemAnim, particleEmitters, betterCombat, hideAboveInterface, null, right, left);
    }

    public static void applySpecific(class_2960 id, VanillaFirstPersonItemLayer.ToolKind kind, String renamed, String model, List<PunchyTuningConfig.CustomAnimationEntry> customAnim, List<PunchyTuningConfig.CustomAnimationEntry> customItemAnim, List<String> particleEmitters, List<PunchyTuningConfig.BetterCombatEntry> betterCombat, Boolean hideAboveInterface, JsonElement bonePhysics, Profile right, Profile left) {
        SpecificEntry entry;
        if (id == null || kind == null) {
            return;
        }
        ToolTuning.clearCustomItemAnimationCache();
        String renamedKey = ToolTuning.normalizeRenamed(renamed);
        if (renamedKey != null) {
            Map perRename = ITEM_SPECIFICS_BY_RENAMED.computeIfAbsent(id, key -> new HashMap());
            entry = (SpecificEntry)perRename.get(renamedKey);
            if (entry == null) {
                entry = new SpecificEntry(kind);
                perRename.put(renamedKey, entry);
            } else if (entry.kind == null) {
                entry.kind = kind;
            }
        } else {
            entry = ITEM_SPECIFICS.get(id);
            if (entry == null) {
                entry = new SpecificEntry(kind);
                ITEM_SPECIFICS.put(id, entry);
            } else if (entry.kind == null) {
                entry.kind = kind;
            }
        }
        entry.renamed = renamedKey != null ? renamedKey : entry.renamed;
        entry.model = model != null ? model : entry.model;
        entry.customAnimation = customAnim != null ? customAnim : entry.customAnimation;
        entry.customItemAnimations = customItemAnim != null ? customItemAnim : entry.customItemAnimations;
        entry.particleEmitters = particleEmitters != null ? new ArrayList<String>(particleEmitters) : entry.particleEmitters;
        entry.betterCombat = betterCombat != null ? betterCombat : entry.betterCombat;
        entry.hideAboveInterface = hideAboveInterface != null ? hideAboveInterface : entry.hideAboveInterface;
        JsonElement jsonElement = entry.bonePhysics = bonePhysics != null && !bonePhysics.isJsonNull() ? bonePhysics.deepCopy() : entry.bonePhysics;
        if (right != null) {
            entry.right = ToolTuning.copyProfile(right);
        }
        if (left != null) {
            entry.left = ToolTuning.copyProfile(left);
        }
    }

    public static void applySpecific(class_2960 id, VanillaFirstPersonItemLayer.ToolKind kind, String model, Profile right, Profile left) {
        ToolTuning.applySpecific(id, kind, model, null, null, null, null, null, right, left);
    }

    public static void applySpecific(class_2960 id, Integer customModelData, VanillaFirstPersonItemLayer.ToolKind kind, String renamed, String model, List<PunchyTuningConfig.CustomAnimationEntry> customAnim, List<PunchyTuningConfig.CustomAnimationEntry> customItemAnim, List<String> particleEmitters, List<PunchyTuningConfig.BetterCombatEntry> betterCombat, Boolean hideAboveInterface, Profile right, Profile left) {
        ToolTuning.applySpecific(id, customModelData, kind, renamed, model, customAnim, customItemAnim, particleEmitters, betterCombat, hideAboveInterface, null, right, left);
    }

    public static void applySpecific(class_2960 id, Integer customModelData, VanillaFirstPersonItemLayer.ToolKind kind, String renamed, String model, List<PunchyTuningConfig.CustomAnimationEntry> customAnim, List<PunchyTuningConfig.CustomAnimationEntry> customItemAnim, List<String> particleEmitters, List<PunchyTuningConfig.BetterCombatEntry> betterCombat, Boolean hideAboveInterface, JsonElement bonePhysics, Profile right, Profile left) {
        SpecificEntry entry;
        if (id == null || kind == null) {
            return;
        }
        ToolTuning.clearCustomItemAnimationCache();
        if (customModelData == null) {
            ToolTuning.applySpecific(id, kind, renamed, model, customAnim, customItemAnim, particleEmitters, betterCombat, hideAboveInterface, bonePhysics, right, left);
            return;
        }
        String renamedKey = ToolTuning.normalizeRenamed(renamed);
        if (renamedKey != null) {
            Map perModelAndRename = ITEM_SPECIFICS_BY_MODEL_AND_RENAMED.computeIfAbsent(id, key -> new HashMap());
            Map perRename = perModelAndRename.computeIfAbsent(customModelData, key -> new HashMap());
            entry = (SpecificEntry)perRename.get(renamedKey);
            if (entry == null) {
                entry = new SpecificEntry(kind);
                perRename.put(renamedKey, entry);
            } else if (entry.kind == null) {
                entry.kind = kind;
            }
        } else {
            Map perModel = ITEM_SPECIFICS_BY_MODEL.computeIfAbsent(id, key -> new HashMap());
            entry = (SpecificEntry)perModel.get(customModelData);
            if (entry == null) {
                entry = new SpecificEntry(kind);
                perModel.put(customModelData, entry);
            } else if (entry.kind == null) {
                entry.kind = kind;
            }
        }
        entry.renamed = renamedKey != null ? renamedKey : entry.renamed;
        entry.model = model != null ? model : entry.model;
        entry.customAnimation = customAnim != null ? customAnim : entry.customAnimation;
        entry.customItemAnimations = customItemAnim != null ? customItemAnim : entry.customItemAnimations;
        entry.particleEmitters = particleEmitters != null ? new ArrayList<String>(particleEmitters) : entry.particleEmitters;
        entry.betterCombat = betterCombat != null ? betterCombat : entry.betterCombat;
        entry.hideAboveInterface = hideAboveInterface != null ? hideAboveInterface : entry.hideAboveInterface;
        JsonElement jsonElement = entry.bonePhysics = bonePhysics != null && !bonePhysics.isJsonNull() ? bonePhysics.deepCopy() : entry.bonePhysics;
        if (right != null) {
            entry.right = ToolTuning.copyProfile(right);
        }
        if (left != null) {
            entry.left = ToolTuning.copyProfile(left);
        }
    }

    public static void clearSpecifics() {
        ITEM_SPECIFICS.clear();
        ITEM_SPECIFICS_BY_MODEL.clear();
        ITEM_SPECIFICS_BY_RENAMED.clear();
        ITEM_SPECIFICS_BY_MODEL_AND_RENAMED.clear();
        ToolTuning.clearCustomItemAnimationCache();
    }

    public static void clearSpecificOverride(class_2960 id) {
        if (id == null) {
            return;
        }
        ITEM_SPECIFICS.remove(id);
        ITEM_SPECIFICS_BY_MODEL.remove(id);
        ITEM_SPECIFICS_BY_RENAMED.remove(id);
        ITEM_SPECIFICS_BY_MODEL_AND_RENAMED.remove(id);
        ToolTuning.clearCustomItemAnimationCache();
    }

    public static void clearSpecificAnimationOverrides(class_2960 id) {
        Map<Integer, Map<String, SpecificEntry>> map;
        Map<String, SpecificEntry> perRename;
        Map<Integer, SpecificEntry> perModel;
        if (id == null) {
            return;
        }
        SpecificEntry entry = ITEM_SPECIFICS.get(id);
        if (entry != null) {
            entry.customAnimation = null;
            entry.customItemAnimations = null;
        }
        if ((perModel = ITEM_SPECIFICS_BY_MODEL.get(id)) != null) {
            for (SpecificEntry specificEntry : perModel.values()) {
                if (specificEntry == null) continue;
                specificEntry.customAnimation = null;
                specificEntry.customItemAnimations = null;
            }
        }
        if ((perRename = ITEM_SPECIFICS_BY_RENAMED.get(id)) != null) {
            for (SpecificEntry renamedEntry : perRename.values()) {
                if (renamedEntry == null) continue;
                renamedEntry.customAnimation = null;
                renamedEntry.customItemAnimations = null;
            }
        }
        if ((map = ITEM_SPECIFICS_BY_MODEL_AND_RENAMED.get(id)) == null) {
            ToolTuning.clearCustomItemAnimationCache();
            return;
        }
        for (Map<String, SpecificEntry> byRename : map.values()) {
            if (byRename == null) continue;
            for (SpecificEntry renamedEntry : byRename.values()) {
                if (renamedEntry == null) continue;
                renamedEntry.customAnimation = null;
                renamedEntry.customItemAnimations = null;
            }
        }
        ToolTuning.clearCustomItemAnimationCache();
    }

    public static void clearGroups() {
        GROUP_ANIMATIONS.clear();
        GROUP_ITEM_ANIMATIONS.clear();
        GROUP_PARTICLE_EMITTERS.clear();
        GROUP_BETTER_COMBAT.clear();
        ToolTuning.clearCustomItemAnimationCache();
        DEFAULT_ANIMATIONS = null;
        DEFAULT_BETTER_COMBAT = null;
    }

    private static SpecificEntry getSpecificEntry(class_1799 stack) {
        Map<Integer, SpecificEntry> perModel;
        SpecificEntry entry;
        Map<String, SpecificEntry> perRename;
        SpecificEntry entry2;
        Map<String, SpecificEntry> perRename2;
        Map<Integer, Map<String, SpecificEntry>> perModelAndRename;
        class_2960 id = ToolTuning.idFor(stack);
        if (id == null) {
            return null;
        }
        Integer cmd = ToolTuning.resolveCustomModelData(stack);
        String renamedKey = ToolTuning.resolveStackRenamedKey(stack);
        if (cmd != null && renamedKey != null && (perModelAndRename = ITEM_SPECIFICS_BY_MODEL_AND_RENAMED.get(id)) != null && (perRename2 = perModelAndRename.get(cmd)) != null && (entry2 = perRename2.get(renamedKey)) != null) {
            return entry2;
        }
        if (renamedKey != null && (perRename = ITEM_SPECIFICS_BY_RENAMED.get(id)) != null && (entry = perRename.get(renamedKey)) != null) {
            return entry;
        }
        if (cmd != null && (perModel = ITEM_SPECIFICS_BY_MODEL.get(id)) != null && (entry = perModel.get(cmd)) != null) {
            return entry;
        }
        return ITEM_SPECIFICS.get(id);
    }

    private static String resolveStackRenamedKey(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        try {
            if (!stack.method_57826(class_9334.field_49631)) {
                return null;
            }
        }
        catch (Throwable ignored) {
            return null;
        }
        try {
            return ToolTuning.normalizeRenamed(stack.method_7964().getString());
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static String normalizeRenamed(String renamed) {
        if (renamed == null) {
            return null;
        }
        String normalized = renamed.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static Integer resolveCustomModelData(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        try {
            Object value;
            Method method2;
            Object cmd = stack.method_57824(class_9334.field_49637);
            if (cmd == null) {
                return null;
            }
            if (cmd instanceof Integer) {
                Integer value2 = (Integer)cmd;
                return value2;
            }
            try {
                method2 = cmd.getClass().getMethod("value", new Class[0]);
                value = method2.invoke(cmd, new Object[0]);
                if (value instanceof Integer) {
                    Integer i = (Integer)value;
                    return i;
                }
            }
            catch (Throwable method2) {
                // empty catch block
            }
            try {
                method2 = cmd.getClass().getMethod("getValue", new Class[0]);
                value = method2.invoke(cmd, new Object[0]);
                if (value instanceof Integer) {
                    Integer i = (Integer)value;
                    return i;
                }
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    public static String resolveStackRenamedKeyForDebug(class_1799 stack) {
        return ToolTuning.resolveStackRenamedKey(stack);
    }

    public static Integer resolveCustomModelDataForDebug(class_1799 stack) {
        return ToolTuning.resolveCustomModelData(stack);
    }

    static {
        OVERRIDE_RIGHT = new EnumMap<VanillaFirstPersonItemLayer.ToolKind, TransformOverride>(VanillaFirstPersonItemLayer.ToolKind.class);
        OVERRIDE_LEFT = new EnumMap<VanillaFirstPersonItemLayer.ToolKind, TransformOverride>(VanillaFirstPersonItemLayer.ToolKind.class);
        ITEM_SPECIFICS = new HashMap<class_2960, SpecificEntry>();
        ITEM_SPECIFICS_BY_MODEL = new HashMap<class_2960, Map<Integer, SpecificEntry>>();
        ITEM_SPECIFICS_BY_RENAMED = new HashMap<class_2960, Map<String, SpecificEntry>>();
        ITEM_SPECIFICS_BY_MODEL_AND_RENAMED = new HashMap<class_2960, Map<Integer, Map<String, SpecificEntry>>>();
        ATTACHED_RIGHT = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        ATTACHED_LEFT = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        GROUP_ANIMATIONS = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        GROUP_ITEM_ANIMATIONS = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        GROUP_PARTICLE_EMITTERS = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        GROUP_BETTER_COMBAT = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        CUSTOM_ITEM_ANIMATION_CACHE = new HashMap<String, List<PunchyTuningConfig.CustomAnimationEntry>>();
        ToolTuning.resetDefaults();
    }

    public static enum RotationOrder {
        XYZ,
        ZXY;

    }

    public static final class Profile {
        public float scale;
        public float offX;
        public float offY;
        public float offZ;
        public float rotX;
        public float rotY;
        public float rotZ;
        public float pivotX;
        public float pivotY;
        public float pivotZ;
        public Boolean disableSprintLateralTilt;
        public Boolean keepArmTuning;
        public Boolean keepTriggerArmTuning;
        public PhysicsMode physicsMode;
        public PhysicsSettings physicsSettings;
        private float rotXRad;
        private float rotYRad;
        private float rotZRad;

        public Profile(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ) {
            this(scale, offX, offY, offZ, rotX, rotY, rotZ, 0.0f, 0.0f, 0.0f, PhysicsMode.NONE);
        }

        public Profile(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ, PhysicsMode physicsMode) {
            this(scale, offX, offY, offZ, rotX, rotY, rotZ, 0.0f, 0.0f, 0.0f, physicsMode);
        }

        public Profile(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ, float pivotX, float pivotY, float pivotZ) {
            this(scale, offX, offY, offZ, rotX, rotY, rotZ, pivotX, pivotY, pivotZ, PhysicsMode.NONE);
        }

        public Profile(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ, float pivotX, float pivotY, float pivotZ, PhysicsMode physicsMode) {
            this.scale = scale;
            this.offX = offX;
            this.offY = offY;
            this.offZ = offZ;
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
            this.physicsMode = physicsMode != null ? physicsMode : PhysicsMode.NONE;
            this.recomputeRadians();
        }

        public void recomputeRadians() {
            this.rotXRad = (float)Math.toRadians(this.rotX);
            this.rotYRad = (float)Math.toRadians(this.rotY);
            this.rotZRad = (float)Math.toRadians(this.rotZ);
        }

        public float rotXRad() {
            return this.rotXRad;
        }

        public float rotYRad() {
            return this.rotYRad;
        }

        public float rotZRad() {
            return this.rotZRad;
        }

        public Profile mirror() {
            Profile mirrored = new Profile(this.scale, -this.offX, this.offY, this.offZ, this.rotX, -this.rotY, -this.rotZ, -this.pivotX, this.pivotY, this.pivotZ, this.physicsMode);
            mirrored.disableSprintLateralTilt = this.disableSprintLateralTilt;
            mirrored.keepArmTuning = this.keepArmTuning;
            mirrored.keepTriggerArmTuning = this.keepTriggerArmTuning;
            if (this.physicsSettings != null) {
                mirrored.physicsSettings = this.physicsSettings.copy();
            }
            return mirrored;
        }
    }

    public static enum PhysicsMode {
        NONE,
        FULL_PENDULUM,
        FORWARD_PENDULUM;

    }

    private static final class TransformOverride {
        float scale;
        float offX;
        float offY;
        float offZ;
        float rotX;
        float rotY;
        float rotZ;
        float pivotX;
        float pivotY;
        float pivotZ;

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
            this.pivotX = target.pivotX - base.pivotX;
            this.pivotY = target.pivotY - base.pivotY;
            this.pivotZ = target.pivotZ - base.pivotZ;
        }
    }

    public static final class PhysicsSettings {
        public Float limitDeg;
        public Float limitDegUp;
        public Float limitDegDown;
        public Float force;
        public Float speed;
        public Float cameraSens;
        public Float gravity;
        public Float motionSens;
        public Boolean invert;
        public Boolean handSpace;
        public Boolean enableCreaturePhysics;

        public PhysicsSettings copy() {
            PhysicsSettings copy = new PhysicsSettings();
            copy.limitDeg = this.limitDeg;
            copy.limitDegUp = this.limitDegUp;
            copy.limitDegDown = this.limitDegDown;
            copy.force = this.force;
            copy.speed = this.speed;
            copy.cameraSens = this.cameraSens;
            copy.gravity = this.gravity;
            copy.motionSens = this.motionSens;
            copy.invert = this.invert;
            copy.handSpace = this.handSpace;
            copy.enableCreaturePhysics = this.enableCreaturePhysics;
            return copy;
        }
    }

    private static final class SpecificEntry {
        VanillaFirstPersonItemLayer.ToolKind kind;
        String model;
        String renamed;
        List<PunchyTuningConfig.CustomAnimationEntry> customAnimation;
        List<PunchyTuningConfig.CustomAnimationEntry> customItemAnimations;
        List<String> particleEmitters;
        List<PunchyTuningConfig.BetterCombatEntry> betterCombat;
        Boolean hideAboveInterface;
        JsonElement bonePhysics;
        Profile right;
        Profile left;

        SpecificEntry(VanillaFirstPersonItemLayer.ToolKind kind) {
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
    }

    public static final class SpecificSnapshotDetailed {
        private final class_2960 itemId;
        private final Integer customModelData;
        private final String renamed;
        private final String model;
        private final VanillaFirstPersonItemLayer.ToolKind kind;
        private final Profile right;
        private final Profile left;

        public SpecificSnapshotDetailed(class_2960 itemId, Integer customModelData, String renamed, String model, VanillaFirstPersonItemLayer.ToolKind kind, Profile right, Profile left) {
            this.itemId = itemId;
            this.customModelData = customModelData;
            this.renamed = renamed;
            this.model = model;
            this.kind = kind;
            this.right = right;
            this.left = left;
        }

        public class_2960 itemId() {
            return this.itemId;
        }

        public Integer customModelData() {
            return this.customModelData;
        }

        public String renamed() {
            return this.renamed;
        }

        public String model() {
            return this.model;
        }

        public VanillaFirstPersonItemLayer.ToolKind kind() {
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
        private final VanillaFirstPersonItemLayer.ToolKind kind;
        private final Profile right;
        private final Profile left;

        public SpecificSnapshot(VanillaFirstPersonItemLayer.ToolKind kind, Profile right, Profile left) {
            this.kind = kind;
            this.right = right;
            this.left = left;
        }

        public VanillaFirstPersonItemLayer.ToolKind kind() {
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

