/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3300
 *  net.minecraft.class_3414
 *  net.minecraft.class_3532
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 */
package punchy.client.animation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import net.minecraft.class_3414;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import punchy.client.PunchyClient;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.io.PunchyAnimationLoader;
import punchy.client.render.UseAnimationSuppressor;
import punchy.client.state.ConsumeStateMachine;
import punchy.client.state.HandEquipStateMachine;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public class PunchyAnimationManager {
    private static final String EFFECT_DUAL_HANDED = "dual_handed";
    private static final String EFFECT_DUAL_HANDED_IF_HAS_OFF_ITEM = "dual_handed_if_has_off_item";
    private static final String EFFECT_START_WITH_OFFHAND = "start_here_if_off_hand_item";
    private static final String EFFECT_START_WITH_OFFHAND_ALIAS = "start_if_offhand_item";
    private static final String EFFECT_STOP_WITH_OFFHAND = "stop_if_offhand_item";
    public static final PoseHandler POSE_HANDLER = new PoseHandler();
    public static final PoseHandler SWIM_HANDLER = new PoseHandler();
    public static AnimationClip PUNCH_CLIP;
    public static AnimationClip INTERACT_CLIP;
    public static AnimationClip USE_BOW_RIGHT_CLIP;
    public static AnimationClip USE_BOW_LEFT_CLIP;
    public static AnimationClip ATTACK_CLIP;
    public static AnimationClip ATTACK_SPEAR_CLIP;
    public static AnimationClip ATTACK_TRIDENT_CLIP;
    public static AnimationClip ATTACK_MACE_CLIP;
    public static AnimationClip USE_SPEAR_POSE_1;
    public static AnimationClip USE_SPEAR_POSE_2;
    public static AnimationClip USE_SPEAR_POSE_3_RIGHT;
    public static AnimationClip USE_SPEAR_POSE_3_LEFT;
    public static AnimationClip USE_SHIELD_RIGHT_CLIP;
    public static AnimationClip USE_SHIELD_LEFT_CLIP;
    public static AnimationClip USE_CROSSBOW_RIGHT_CLIP;
    public static AnimationClip USE_CROSSBOW_LEFT_CLIP;
    public static AnimationClip USE_CROSSBOW_SHOT_CLIP;
    public static AnimationClip USE_TRIDENT_CLIP;
    public static AnimationClip USE_TRIDENT_THROW_CLIP;
    public static AnimationClip USE_EAT_CLIP;
    public static AnimationClip USE_DRINK_CLIP;
    public static AnimationClip SWIMING_CLIP;
    public static AnimationClip SWIMING_ABOVE_CLIP;
    public static AnimationClip ROWING_CLIP;
    public static AnimationClip ROWING_BACK_CLIP;
    public static AnimationClip FALLING_IN_WATER_CLIP;
    public static AnimationClip USE_TRIDENT_THROW_LOYALTY_CLIP;
    public static AnimationClip USE_TRIDENT_THROW_RIPTIDE_CLIP;
    public static AnimationClip MINING_FORWARD_CLIP;
    public static AnimationClip MINING_SIDEWAYS_CLIP;
    public static AnimationClip MINING_DIGGING_CLIP;
    public static AnimationClip MINING_USE_FORWARD_CLIP;
    public static AnimationClip MINING_USE_DIGGING_CLIP;
    public static AnimationClip PUT_OUT_FIRE_CLIP;
    public static AnimationClip HAND_IN_CLIP;
    public static AnimationClip HAND_OUT_CLIP;
    public static AnimationClip WAKE_UP_CLIP;
    public static float ANIMATION_SPEED;
    private static final EnumMap<class_1306, ItemOverrideState> ITEM_OVERRIDES;
    private static final EnumMap<class_1306, EquipOverrideState> EQUIP_OVERRIDES;
    private static class_1306 activeSourceArm;
    private static AnimationClip lastSoundClip;
    private static float lastSoundTime;
    public static boolean SOUND_ENABLED;
    private static boolean suppressOffhandForAttack;
    private static AnimationClip suppressOffhandClip;
    private static final Map<String, AnimationClip> ALL_CLIPS;
    private static final Map<String, LinkedHashMap<String, AnimationClip>> ALL_CLIP_VARIANTS;
    private static final Map<String, List<String>> TYPE_CLIP_ALIASES;
    private static final Map<String, ClipResolution> CLIP_RESOLUTION_CACHE;
    private static final String LEFT_SWITCH_PREFIX = "left_switch_item_";
    private static final String RIGHT_SWITCH_PREFIX = "right_switch_item_";
    private static final String HIDE_LEFT_SWITCH_PREFIX = "hide_left_switch_item_";
    private static final String HIDE_RIGHT_SWITCH_PREFIX = "hide_right_switch_item_";
    private static final String SHOW_LEFT_SWITCH = "show_left_switch_item";
    private static final String SHOW_RIGHT_SWITCH = "show_right_switch_item";
    private static final String SHOW_LEFT_SWITCH_PREFIX = "show_left_switch_item_";
    private static final String SHOW_RIGHT_SWITCH_PREFIX = "show_right_switch_item_";
    private static final String HIDE_ITEM_EFFECT = "hide_item";
    private static final String REDUCE_MOVEMENT_FORCE_PREFIX = "reduce_movement_force_";

    private static Map<String, List<String>> buildTypeClipAliases() {
        HashMap<String, List<String>> aliases = new HashMap<String, List<String>>();
        aliases.put(PunchyAnimationManager.normalizeTypeKey("attack"), List.of("attack"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("mining"), List.of("mining", "mining_foward", "mining_forward", "mining_sideways", "mining_digging"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("mining_v2"), List.of("mining", "mining_foward", "mining_forward", "mining_sideways", "mining_digging"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("useItem"), List.of("use_item", "useitem", "interaction", "interact", "mining_use_foward", "mining_use_forward", "mining_use_digging"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("interaction"), List.of("interaction", "interact", "use_item", "useitem"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("spear_charge"), List.of("use_spear_pose_1", "use_spear_pose_1_left", "use_spear_pose_1_right", "use_spear_pose_2", "use_spear_pose_2_left", "use_spear_pose_2_right", "use_spear_pose_3", "use_spear_pose_3_left", "use_spear_pose_3_right"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("inspect"), List.of("inspect"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("hand_in"), List.of("hand_in", "handin"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("hand_out"), List.of("hand_out", "handout"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("falling"), List.of("falling"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("triggerable"), List.of("triggerable"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("use_shield"), List.of("use_shield"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("use_bow"), List.of("use_bow"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("use_crossbow"), List.of("use_crossbow"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("use_crossbow_shot"), List.of("use_crossbow_shot"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("useEat"), List.of("use_eat", "useeat"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("useDrink"), List.of("use_drink", "usedrink"));
        aliases.put(PunchyAnimationManager.normalizeTypeKey("idle"), List.of("idle"));
        return aliases;
    }

    public static void loadAnimations(class_3300 manager) {
        ALL_CLIPS.clear();
        ALL_CLIP_VARIANTS.clear();
        CLIP_RESOLUTION_CACHE.clear();
        SOUND_ENABLED = false;
        lastSoundClip = null;
        lastSoundTime = 0.0f;
        POSE_HANDLER.stopImmediateWithBlendOut();
        SWIM_HANDLER.stopImmediateWithBlendOut();
        Map<String, LinkedHashMap<String, AnimationClip>> loadedVariants = PunchyAnimationLoader.loadAllWithPackVariants(manager);
        for (Map.Entry<String, LinkedHashMap<String, AnimationClip>> entry : loadedVariants.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) continue;
            LinkedHashMap<String, AnimationClip> variants = new LinkedHashMap<String, AnimationClip>();
            for (Map.Entry<String, AnimationClip> variant : entry.getValue().entrySet()) {
                if (variant == null || variant.getKey() == null || variant.getValue() == null) continue;
                if (!variant.getValue().getSoundEffects().isEmpty()) {
                    SOUND_ENABLED = true;
                }
                variants.put(variant.getKey(), AnimationClip.copyOf(variant.getValue()));
            }
            if (variants.isEmpty()) continue;
            ALL_CLIP_VARIANTS.put(entry.getKey(), variants);
            AnimationClip merged = (AnimationClip)variants.get("punchy_default");
            if (merged == null) {
                merged = (AnimationClip)variants.values().iterator().next();
            }
            if (merged == null) continue;
            ALL_CLIPS.put(entry.getKey(), AnimationClip.copyOf(merged));
        }
        PunchyAnimationManager.assignKnownClips();
    }

    public static List<String> getMixPackAnimationNames() {
        return PunchyTuningConfig.getMixPackSlotIds();
    }

    public static String getMixPackEntryLabel(String entryId) {
        return PunchyTuningConfig.getMixPackSlotLabel(entryId);
    }

    public static List<PackOption> getMixPackOptions(String animationName) {
        List<String> optionIds = PunchyTuningConfig.getMixPackSlotOptionIds(animationName);
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        ArrayList<PackOption> options = new ArrayList<PackOption>();
        for (String optionId : optionIds) {
            if ("punchy_default".equals(optionId)) continue;
            options.add(new PackOption(optionId, PunchyAnimationManager.packOptionLabel(optionId)));
        }
        return options;
    }

    public static String getSelectedMixPackOption(String animationName) {
        return PunchyTuningConfig.getMixPackSelectedOption(animationName);
    }

    public static boolean selectMixPackOption(String animationName, String optionId) {
        String before = PunchyTuningConfig.getMixPackSelectedOption(animationName);
        boolean changed = PunchyTuningConfig.setMixPackSelection(animationName, optionId);
        if (!changed) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client != null && client.method_1478() != null) {
            PunchyTuningConfig.reloadActiveProfileFast(client.method_1478());
        } else {
            PunchyTuningConfig.reloadActiveProfileFast(null);
        }
        PunchyAnimationManager.rebuildActiveClipsFromSelections();
        String after = PunchyTuningConfig.getMixPackSelectedOption(animationName);
        return true;
    }

    public static List<String> getMixPackEntryTypes(String animationName) {
        return PunchyTuningConfig.getMixPackSlotEntryTypes(animationName);
    }

    public static List<String> getMixPackExpandableEntryTypes(String animationName) {
        return PunchyTuningConfig.getMixPackExpandableEntryTypes(animationName);
    }

    public static List<PackOption> getMixPackTypeOptions(String animationName, String entryType) {
        List<String> optionIds = PunchyTuningConfig.getMixPackSlotEntryTypeOptionIds(animationName, entryType);
        if (optionIds == null) {
            return List.of();
        }
        ArrayList<String> normalizedOptionIds = new ArrayList<String>(optionIds);
        String selectedMain = PunchyAnimationManager.getSelectedMixPackOption(animationName);
        if (selectedMain != null && !selectedMain.isBlank() && !normalizedOptionIds.contains(selectedMain)) {
            normalizedOptionIds.add(0, selectedMain);
        }
        if (normalizedOptionIds.isEmpty()) {
            return List.of();
        }
        ArrayList<PackOption> options = new ArrayList<PackOption>();
        for (String optionId : normalizedOptionIds) {
            if ("punchy_default".equals(optionId)) continue;
            options.add(new PackOption(optionId, PunchyAnimationManager.packOptionLabel(optionId)));
        }
        return options;
    }

    public static String getSelectedMixPackTypeOption(String animationName, String entryType) {
        return PunchyTuningConfig.getMixPackSelectedEntryTypeOption(animationName, entryType);
    }

    public static boolean hasExplicitMixPackTypeOption(String animationName, String entryType) {
        return PunchyTuningConfig.hasMixPackEntryTypeOverride(animationName, entryType);
    }

    public static boolean selectMixPackTypeOption(String animationName, String entryType, String optionId) {
        String before = PunchyTuningConfig.getMixPackSelectedEntryTypeOption(animationName, entryType);
        boolean changed = PunchyTuningConfig.setMixPackEntryTypeSelection(animationName, entryType, optionId);
        if (!changed) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client != null && client.method_1478() != null) {
            PunchyTuningConfig.reloadActiveProfileFast(client.method_1478());
        } else {
            PunchyTuningConfig.reloadActiveProfileFast(null);
        }
        PunchyAnimationManager.rebuildActiveClipsFromSelections();
        String after = PunchyTuningConfig.getMixPackSelectedEntryTypeOption(animationName, entryType);
        return true;
    }

    public static void resetMixPacksToResourcePackOrder() {
        PunchyTuningConfig.resetMixPackSelectionsToResourcepackOrder();
        class_310 client = class_310.method_1551();
        if (client != null && client.method_1478() != null) {
            PunchyTuningConfig.reloadActiveProfileFast(client.method_1478());
        } else {
            PunchyTuningConfig.reloadActiveProfileFast(null);
        }
        PunchyAnimationManager.rebuildActiveClipsFromSelections();
    }

    private static void rebuildActiveClipsFromSelections() {
        CLIP_RESOLUTION_CACHE.clear();
        PunchyAnimationManager.assignKnownClips();
    }

    private static void assignKnownClips() {
        PUNCH_CLIP = ALL_CLIPS.get("punch");
        INTERACT_CLIP = ALL_CLIPS.get("interact");
        USE_BOW_RIGHT_CLIP = ALL_CLIPS.get("use_bow_right");
        USE_BOW_LEFT_CLIP = ALL_CLIPS.get("use_bow_left");
        ATTACK_CLIP = ALL_CLIPS.get("attack");
        ATTACK_SPEAR_CLIP = ALL_CLIPS.get("attack_spear");
        ATTACK_TRIDENT_CLIP = ALL_CLIPS.get("attack_trident");
        ATTACK_MACE_CLIP = ALL_CLIPS.get("attack_mace");
        USE_SPEAR_POSE_1 = ALL_CLIPS.get("use_spear_pose_1");
        USE_SPEAR_POSE_2 = ALL_CLIPS.get("use_spear_pose_2");
        USE_SPEAR_POSE_3_RIGHT = ALL_CLIPS.get("use_spear_pose_3_right");
        USE_SPEAR_POSE_3_LEFT = ALL_CLIPS.get("use_spear_pose_3_left");
        USE_SHIELD_RIGHT_CLIP = ALL_CLIPS.get("use_shield_right");
        USE_SHIELD_LEFT_CLIP = ALL_CLIPS.get("use_shield_left");
        USE_CROSSBOW_RIGHT_CLIP = ALL_CLIPS.get("use_crossbow_right");
        USE_CROSSBOW_LEFT_CLIP = ALL_CLIPS.get("use_crossbow_left");
        USE_CROSSBOW_SHOT_CLIP = ALL_CLIPS.get("use_crossbow_shot");
        USE_TRIDENT_CLIP = ALL_CLIPS.get("use_trident");
        USE_TRIDENT_THROW_CLIP = ALL_CLIPS.get("use_trident_throw");
        USE_EAT_CLIP = ALL_CLIPS.get("use_eat");
        USE_DRINK_CLIP = ALL_CLIPS.get("use_drink");
        SWIMING_CLIP = ALL_CLIPS.get("swiming");
        SWIMING_ABOVE_CLIP = ALL_CLIPS.get("swiming_above");
        ROWING_CLIP = ALL_CLIPS.get("rowing");
        ROWING_BACK_CLIP = ALL_CLIPS.get("rowing_back");
        FALLING_IN_WATER_CLIP = ALL_CLIPS.get("falling_in_water");
        USE_TRIDENT_THROW_LOYALTY_CLIP = ALL_CLIPS.get("use_trident_throw_loyalty");
        USE_TRIDENT_THROW_RIPTIDE_CLIP = ALL_CLIPS.get("use_trident_throw_riptide");
        MINING_FORWARD_CLIP = ALL_CLIPS.get("mining_foward");
        MINING_SIDEWAYS_CLIP = ALL_CLIPS.get("mining_sideways");
        MINING_DIGGING_CLIP = ALL_CLIPS.get("mining_digging");
        MINING_USE_FORWARD_CLIP = ALL_CLIPS.get("mining_use_foward");
        MINING_USE_DIGGING_CLIP = ALL_CLIPS.get("mining_use_digging");
        PUT_OUT_FIRE_CLIP = ALL_CLIPS.get("put_out_fire");
        HAND_IN_CLIP = ALL_CLIPS.get("hand_in");
        HAND_OUT_CLIP = ALL_CLIPS.get("hand_out");
        WAKE_UP_CLIP = ALL_CLIPS.get("wake_up");
    }

    private static String normalizeOptionId(String optionId) {
        if (optionId == null || optionId.isBlank()) {
            return null;
        }
        return optionId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeTypeKey(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        if ("miningv2".equals(normalized) || "mining".equals(normalized)) {
            return "mining";
        }
        return normalized;
    }

    private static String packOptionLabel(String optionId) {
        if (optionId == null || optionId.isBlank()) {
            return "Unknown";
        }
        if ("pack:punchy:punchy".equals(optionId)) {
            return "\u00a7bPunchy! \u00a7eDefault";
        }
        if ("punchy_default".equals(optionId)) {
            return "\u00a77Default";
        }
        String cleaned = optionId;
        if (cleaned.startsWith("pack:")) {
            cleaned = cleaned.substring("pack:".length());
        }
        if (cleaned.startsWith("file/")) {
            cleaned = cleaned.substring("file/".length());
        }
        if (cleaned.startsWith("file\\")) {
            cleaned = cleaned.substring("file\\".length());
        }
        if (cleaned.endsWith(".zip")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }
        if (cleaned.endsWith(".jar")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }
        if (cleaned.isBlank()) {
            return "Unknown";
        }
        cleaned = cleaned.replace('_', ' ').replace('-', ' ').replace('/', ' ').replace('\\', ' ');
        String[] parts = cleaned.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank() || "file".equalsIgnoreCase(part)) continue;
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            String lowered = part.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(lowered.charAt(0)));
            if (lowered.length() <= 1) continue;
            builder.append(lowered.substring(1));
        }
        return builder.isEmpty() ? "Unknown" : builder.toString();
    }

    private static AnimationClip resolveClipByOption(String clipName, String preferredOptionId) {
        return PunchyAnimationManager.resolveClipByOptionWithSource(clipName, preferredOptionId).clip();
    }

    public static AnimationClip resolveCustomClip(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = name.trim();
        return ALL_CLIPS.get(key);
    }

    public static AnimationClip resolveHandedClip(String baseName, class_310 client, class_1268 hand) {
        return PunchyAnimationManager.resolveHandedClip(baseName, client, hand, null);
    }

    public static AnimationClip resolveHandedClip(String baseName, class_310 client, class_1268 hand, String preferredOptionId) {
        boolean hasSuffixed;
        if (baseName == null || baseName.isBlank()) {
            return null;
        }
        String preferred = preferredOptionId;
        if (preferred == null || preferred.isBlank()) {
            preferred = PunchyAnimationManager.inferPreferredOptionFromHand(client, hand);
        }
        String key = baseName.trim();
        AnimationClip base = PunchyAnimationManager.resolveClipByOption(key, preferred);
        AnimationClip right = PunchyAnimationManager.resolveClipByOption(key + "_right", preferred);
        AnimationClip left = PunchyAnimationManager.resolveClipByOption(key + "_left", preferred);
        boolean bl = hasSuffixed = right != null || left != null;
        if (hasSuffixed) {
            boolean useRight;
            if (client == null || client.field_1724 == null || hand == null) {
                return base != null ? base : (right != null ? right : left);
            }
            boolean bl2 = useRight = hand == class_1268.field_5808 == (client.field_1724.method_6068() == class_1306.field_6183);
            if (useRight) {
                if (right != null) {
                    return right;
                }
                if (base != null) {
                    return base;
                }
                return left;
            }
            if (left != null) {
                return left;
            }
            if (base != null) {
                return base;
            }
            return right;
        }
        return base;
    }

    public static AnimationClip resolveNamedClip(String name, class_310 client, class_1268 hand) {
        return PunchyAnimationManager.resolveNamedClip(name, client, hand, null);
    }

    public static AnimationClip resolveNamedClip(String name, class_310 client, class_1268 hand, String preferredOptionId) {
        String key;
        if (name == null || name.isBlank()) {
            return null;
        }
        String preferred = preferredOptionId;
        if (preferred == null || preferred.isBlank()) {
            preferred = PunchyAnimationManager.inferPreferredOptionFromHand(client, hand);
        }
        if ((key = name.trim()).endsWith("_left") || key.endsWith("_right")) {
            return PunchyAnimationManager.resolveClipByOption(key, preferred);
        }
        return PunchyAnimationManager.resolveHandedClip(key, client, hand, preferred);
    }

    public static ClipResolution resolveNamedClipWithSource(String name, class_310 client, class_1268 hand, String preferredOptionId) {
        String key;
        if (name == null || name.isBlank()) {
            return new ClipResolution(null, null, "name_blank");
        }
        String preferred = preferredOptionId;
        if (preferred == null || preferred.isBlank()) {
            preferred = PunchyAnimationManager.inferPreferredOptionFromHand(client, hand);
        }
        if ((key = name.trim()).endsWith("_left") || key.endsWith("_right")) {
            return PunchyAnimationManager.resolveClipByOptionWithSource(key, preferred);
        }
        return PunchyAnimationManager.resolveHandedClipWithSource(key, client, hand, preferred);
    }

    public static ClipResolution resolveHandedClipWithSource(String baseName, class_310 client, class_1268 hand, String preferredOptionId) {
        boolean hasSuffixed;
        if (baseName == null || baseName.isBlank()) {
            return new ClipResolution(null, null, "base_blank");
        }
        String preferred = preferredOptionId;
        if (preferred == null || preferred.isBlank()) {
            preferred = PunchyAnimationManager.inferPreferredOptionFromHand(client, hand);
        }
        String key = baseName.trim();
        ClipResolution base = PunchyAnimationManager.resolveClipByOptionWithSource(key, preferred);
        ClipResolution right = PunchyAnimationManager.resolveClipByOptionWithSource(key + "_right", preferred);
        ClipResolution left = PunchyAnimationManager.resolveClipByOptionWithSource(key + "_left", preferred);
        boolean bl = hasSuffixed = right.clip() != null || left.clip() != null;
        if (hasSuffixed) {
            boolean useRight;
            if (client == null || client.field_1724 == null || hand == null) {
                if (base.clip() != null) {
                    return new ClipResolution(base.clip(), base.optionId(), "suffixed_no_context_base:" + base.note());
                }
                if (right.clip() != null) {
                    return new ClipResolution(right.clip(), right.optionId(), "suffixed_no_context_right:" + right.note());
                }
                if (left.clip() != null) {
                    return new ClipResolution(left.clip(), left.optionId(), "suffixed_no_context_left:" + left.note());
                }
                return new ClipResolution(null, null, "suffixed_no_context_missing");
            }
            boolean bl2 = useRight = hand == class_1268.field_5808 == (client.field_1724.method_6068() == class_1306.field_6183);
            if (useRight) {
                if (right.clip() != null) {
                    return right;
                }
                if (base.clip() != null) {
                    return new ClipResolution(base.clip(), base.optionId(), "suffixed_main_fallback_base:" + base.note());
                }
                if (left.clip() != null) {
                    return new ClipResolution(left.clip(), left.optionId(), "suffixed_main_fallback_left:" + left.note());
                }
                return new ClipResolution(null, null, "suffixed_main_missing");
            }
            if (left.clip() != null) {
                return left;
            }
            if (base.clip() != null) {
                return new ClipResolution(base.clip(), base.optionId(), "suffixed_off_fallback_base:" + base.note());
            }
            if (right.clip() != null) {
                return new ClipResolution(right.clip(), right.optionId(), "suffixed_off_fallback_right:" + right.note());
            }
            return new ClipResolution(null, null, "suffixed_off_missing");
        }
        return base;
    }

    public static AnimationClip resolveTypeFallbackClip(String type, class_310 client, class_1268 hand) {
        return PunchyAnimationManager.resolveTypeFallbackClip(type, client, hand, null);
    }

    public static AnimationClip resolveTypeFallbackClip(String type, class_310 client, class_1268 hand, String preferredOptionId) {
        return PunchyAnimationManager.resolveTypeFallbackClipWithSource(type, client, hand, preferredOptionId).clip();
    }

    public static ClipResolution resolveTypeFallbackClipWithSource(String type, class_310 client, class_1268 hand, String preferredOptionId) {
        List<String> candidates = TYPE_CLIP_ALIASES.get(PunchyAnimationManager.normalizeTypeKey(type));
        if (candidates == null || candidates.isEmpty()) {
            return new ClipResolution(null, null, "type_unmapped");
        }
        for (String candidate : candidates) {
            ClipResolution resolution;
            if (candidate == null || candidate.isBlank() || (resolution = PunchyAnimationManager.resolveNamedClipWithSource(candidate, client, hand, preferredOptionId)) == null || resolution.clip() == null) continue;
            return new ClipResolution(resolution.clip(), resolution.optionId(), "type_fallback:" + candidate + ":" + resolution.note());
        }
        return new ClipResolution(null, null, "type_missing");
    }

    public static boolean hasTypeFallbackClip(String type, String preferredOptionId) {
        List<String> candidates = TYPE_CLIP_ALIASES.get(PunchyAnimationManager.normalizeTypeKey(type));
        if (candidates == null || candidates.isEmpty()) {
            return false;
        }
        for (String candidate : candidates) {
            if (!PunchyAnimationManager.hasNamedOrHandedClip(candidate, preferredOptionId)) continue;
            return true;
        }
        return false;
    }

    private static ClipResolution resolveClipByOptionWithSource(String clipName, String preferredOptionId) {
        ClipResolution resolved;
        if (clipName == null || clipName.isBlank()) {
            return new ClipResolution(null, null, "clip_blank");
        }
        String normalized = PunchyAnimationManager.normalizeOptionId(preferredOptionId);
        String cacheKey = clipName + "|" + (normalized == null ? "__auto__" : normalized);
        ClipResolution cached = CLIP_RESOLUTION_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        LinkedHashMap<String, AnimationClip> variants = ALL_CLIP_VARIANTS.get(clipName);
        if (normalized != null && !normalized.isBlank() && !"punchy_default".equals(normalized)) {
            AnimationClip fallback;
            AnimationClip preferred;
            AnimationClip animationClip = preferred = variants == null ? null : variants.get(normalized);
            resolved = preferred != null ? new ClipResolution(preferred, normalized, "preferred_option_variant") : new ClipResolution(fallback, (fallback = ALL_CLIPS.get(clipName)) == null ? null : "punchy_default", fallback == null ? "preferred_option_missing" : "preferred_option_fallback_default");
        } else {
            AnimationClip clip;
            resolved = new ClipResolution(clip, (clip = ALL_CLIPS.get(clipName)) == null ? null : "punchy_default", clip == null ? "clip_missing" : "resource_order_default");
        }
        CLIP_RESOLUTION_CACHE.put(cacheKey, resolved);
        return resolved;
    }

    private static boolean hasNamedOrHandedClip(String clipName, String preferredOptionId) {
        if (clipName == null || clipName.isBlank()) {
            return false;
        }
        if (PunchyAnimationManager.resolveClipByOption(clipName, preferredOptionId) != null) {
            return true;
        }
        return PunchyAnimationManager.resolveClipByOption(clipName + "_left", preferredOptionId) != null || PunchyAnimationManager.resolveClipByOption(clipName + "_right", preferredOptionId) != null;
    }

    private static String inferPreferredOptionFromHand(class_310 client, class_1268 hand) {
        if (client == null || client.field_1724 == null || hand == null) {
            return null;
        }
        class_1799 stack = client.field_1724.method_5998(hand);
        return PunchyTuningConfig.getMixPreferredOptionForEntry(stack, null);
    }

    public static void logMixSelectionTrace(String context, String key, String clipName, String preferredOptionId, ClipResolution resolution) {
    }

    public static boolean isClipNamed(AnimationClip clip, String baseName) {
        if (clip == null || baseName == null || baseName.isBlank()) {
            return false;
        }
        String name = clip.getName();
        String key = baseName.trim();
        return key.equals(name) || (key + "_left").equals(name) || (key + "_right").equals(name);
    }

    public static boolean isHandBlacklisted(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return false;
        }
        class_1268 hand = PunchyAnimationManager.getHandForArm(mc, arm);
        return PunchyConfig.isHandBlacklisted((class_1657)mc.field_1724, hand);
    }

    public static boolean isHandBlacklisted(class_310 client, class_1268 hand) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1268 resolved = hand == null ? class_1268.field_5808 : hand;
        return PunchyConfig.isHandBlacklisted((class_1657)client.field_1724, resolved);
    }

    public static boolean isAnyHandBlacklisted() {
        return PunchyAnimationManager.isHandBlacklisted(class_1306.field_6183) || PunchyAnimationManager.isHandBlacklisted(class_1306.field_6182);
    }

    public static boolean shouldBlockClip(AnimationClip clip, class_1306 activeArm) {
        if (clip == null) {
            return false;
        }
        if (activeArm != null && PunchyAnimationManager.isHandBlacklisted(activeArm)) {
            return true;
        }
        if (!PunchyAnimationManager.isDualHandedActive(clip, PunchyAnimationManager.resolveDualHandedEffectTime(clip)) || !PunchyAnimationManager.isAnyHandBlacklisted()) {
            return false;
        }
        class_1306 sourceArm = PunchyAnimationManager.getSourceArm();
        if (sourceArm == null) {
            return true;
        }
        return PunchyAnimationManager.isHandBlacklisted(sourceArm);
    }

    public static void enforceBlacklist(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (clip == null || !POSE_HANDLER.isPlaying()) {
            return;
        }
        class_1306 arm = POSE_HANDLER.getActiveArm();
        if (PunchyAnimationManager.shouldBlockClip(clip, arm)) {
            POSE_HANDLER.setLooping(false);
            POSE_HANDLER.stopImmediateWithBlendOut();
        }
    }

    private static class_1799 getStackForArm(class_1657 player, class_1306 arm) {
        boolean mainRight;
        if (player == null || arm == null) {
            return class_1799.field_8037;
        }
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        return arm == class_1306.field_6183 ? (mainRight ? player.method_6047() : player.method_6079()) : (mainRight ? player.method_6079() : player.method_6047());
    }

    public static void setAnimationSpeed(float speed) {
        ANIMATION_SPEED = class_3532.method_15363((float)speed, (float)0.5f, (float)7.0f);
    }

    public static boolean isDualHandedActive() {
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (clip == null) {
            return false;
        }
        float time = POSE_HANDLER.getCurrentTime();
        return PunchyAnimationManager.isDualHandedActive(clip, time);
    }

    public static boolean hasAnyDualHandedEffect(AnimationClip clip) {
        return clip != null && (clip.hasEffectAny(EFFECT_DUAL_HANDED) || clip.hasEffectAny(EFFECT_DUAL_HANDED_IF_HAS_OFF_ITEM));
    }

    public static boolean isDualHandedActive(AnimationClip clip, float time) {
        return PunchyAnimationManager.isDualHandedActive(class_310.method_1551(), clip, time);
    }

    public static boolean isDualHandedActive(class_310 client, AnimationClip clip, float time) {
        if (clip == null) {
            return false;
        }
        if (clip.isEffectActive(EFFECT_DUAL_HANDED, time)) {
            return PunchyAnimationManager.isConditionalOffhandWindowActive(client, clip, time);
        }
        return PunchyAnimationManager.hasDualHandedOffhandContext(client) && clip.isEffectActive(EFFECT_DUAL_HANDED_IF_HAS_OFF_ITEM, time);
    }

    private static boolean isConditionalOffhandWindowActive(class_310 client, AnimationClip clip, float time) {
        boolean hasStop;
        if (clip == null) {
            return false;
        }
        if (!PunchyAnimationManager.hasOffhandItem(client)) {
            return true;
        }
        float lastStart = PunchyAnimationManager.resolveLastOffhandStartTime(clip, time);
        float lastStop = clip.getLastEffectTime(EFFECT_STOP_WITH_OFFHAND, time);
        boolean hasStart = !Float.isNaN(lastStart);
        boolean bl = hasStop = !Float.isNaN(lastStop);
        if (!hasStart && !hasStop) {
            return true;
        }
        if (!hasStart) {
            return !hasStop;
        }
        return !hasStop || lastStart > lastStop;
    }

    private static float resolveLastOffhandStartTime(AnimationClip clip, float time) {
        if (clip == null) {
            return Float.NaN;
        }
        float canonical = clip.getLastEffectTime(EFFECT_START_WITH_OFFHAND, time);
        float alias = clip.getLastEffectTime(EFFECT_START_WITH_OFFHAND_ALIAS, time);
        if (Float.isNaN(canonical)) {
            return alias;
        }
        if (Float.isNaN(alias)) {
            return canonical;
        }
        return Math.max(canonical, alias);
    }

    public static boolean isSwimDualHandedActive() {
        if (!SWIM_HANDLER.isPlaying()) {
            return false;
        }
        if (SWIM_HANDLER.getActiveArm() != null) {
            return false;
        }
        AnimationClip clip = SWIM_HANDLER.getCurrentClip();
        if (clip == null) {
            return false;
        }
        return PunchyAnimationManager.isDualHandedActive(clip, SWIM_HANDLER.getCurrentTime());
    }

    public static boolean isOffhandHiddenIfEmptyActive() {
        return POSE_HANDLER.isOffhandHiddenIfEmptyActive() || SWIM_HANDLER.isOffhandHiddenIfEmptyActive();
    }

    public static boolean isOffhandHiddenIfEmptyActive(float partialTicks) {
        return POSE_HANDLER.isOffhandHiddenIfEmptyActive(partialTicks) || SWIM_HANDLER.isOffhandHiddenIfEmptyActive(partialTicks);
    }

    public static boolean shouldKeepBlacklistedOppositeHandForDualHanded(class_310 client, class_1268 hand) {
        if (client == null || client.field_1724 == null || hand == null) {
            return false;
        }
        if (!PunchyConfig.isHandBlacklisted((class_1657)client.field_1724, hand)) {
            return false;
        }
        if (!POSE_HANDLER.isPlaying()) {
            return false;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (!PunchyAnimationManager.isDualHandedActive(clip, POSE_HANDLER.getCurrentTime())) {
            return false;
        }
        class_1306 sourceArm = PunchyAnimationManager.getSourceArm();
        if (sourceArm == null) {
            return false;
        }
        return PunchyAnimationManager.getHandForArm(client, sourceArm) != hand;
    }

    public static void updateUseMeshSuppression(class_310 client) {
        boolean started;
        float lastStart;
        boolean hasTrigger;
        if (client == null || client.field_1724 == null) {
            UseAnimationSuppressor.clear();
            return;
        }
        if (client.field_1724.method_6115() && PunchyConfig.isHandBlacklisted((class_1657)client.field_1724, client.field_1724.method_6058())) {
            UseAnimationSuppressor.clear();
            UseAnimationSuppressor.force(false);
            return;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (clip == null) {
            UseAnimationSuppressor.clear();
            return;
        }
        class_1268 activeHand = PunchyAnimationManager.resolveMeshSuppressorHand(client);
        if (activeHand == null) {
            UseAnimationSuppressor.clear();
            return;
        }
        class_1268 oppositeHand = activeHand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
        UseAnimationSuppressor.clear(oppositeHand);
        if (PunchyAnimationManager.isClipNamed(clip, "use_shield") || PunchyAnimationManager.isClipNamed(clip, "use_trident")) {
            UseAnimationSuppressor.suppress(activeHand);
            return;
        }
        float time = POSE_HANDLER.getCurrentTime();
        boolean bl = hasTrigger = clip.hasEffectAny("start_mesh_animation") || clip.hasEffectAny("loadstart") || clip.hasEffectAny("load_start") || clip.hasEffectAny("suppress_mesh_animation");
        if (!hasTrigger) {
            UseAnimationSuppressor.clear();
            return;
        }
        float lastSuppress = PunchyAnimationManager.getLastEffectTime(clip, "suppress_mesh_animation", time);
        if (lastSuppress > (lastStart = Math.max(PunchyAnimationManager.getLastEffectTime(clip, "start_mesh_animation", time), Math.max(PunchyAnimationManager.getLastEffectTime(clip, "loadstart", time), PunchyAnimationManager.getLastEffectTime(clip, "load_start", time))))) {
            UseAnimationSuppressor.suppress(activeHand);
            return;
        }
        boolean bl2 = started = lastStart >= 0.0f;
        if (!started) {
            UseAnimationSuppressor.suppress(activeHand);
        } else {
            class_1799 useStack;
            int remaining;
            int n = remaining = client.field_1724.method_6115() ? client.field_1724.method_6014() : 0;
            if (client.field_1724.method_6115() && !(useStack = client.field_1724.method_6030()).method_7960()) {
                remaining = useStack.method_7935((class_1309)client.field_1724);
            }
            UseAnimationSuppressor.release(activeHand, client.field_1687.method_8510(), remaining);
        }
    }

    private static class_1268 resolveMeshSuppressorHand(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return null;
        }
        if (client.field_1724.method_6115()) {
            return client.field_1724.method_6058();
        }
        if (activeSourceArm == null) {
            return null;
        }
        return PunchyAnimationManager.getHandForArm(client, activeSourceArm);
    }

    private static float getLastEffectTime(AnimationClip clip, String effect, float time) {
        if (clip == null || effect == null || effect.isEmpty()) {
            return Float.NEGATIVE_INFINITY;
        }
        if (clip.hasEffect(effect)) {
            return Float.POSITIVE_INFINITY;
        }
        float lastTime = Float.NEGATIVE_INFINITY;
        for (Map.Entry<Float, Set<String>> entry : clip.getTimelineEffectsUpTo(time)) {
            if (!entry.getValue().contains(effect)) continue;
            lastTime = entry.getKey().floatValue();
        }
        return lastTime;
    }

    public static void updateItemOverrides(class_310 client) {
        if (client == null || client.field_1724 == null) {
            PunchyAnimationManager.clearItemOverrides();
            return;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (clip == null || !POSE_HANDLER.isPlaying()) {
            PunchyAnimationManager.clearItemOverrides();
            return;
        }
        float time = POSE_HANDLER.getCurrentTime();
        ItemOverrideState left = new ItemOverrideState();
        ItemOverrideState right = new ItemOverrideState();
        PunchyAnimationManager.applyItemEffectsOrdered(clip.getEffects(), left, right);
        for (Map.Entry<Float, Set<String>> entry : clip.getTimelineEffectsUpTo(time)) {
            PunchyAnimationManager.applyItemEffectsOrdered((Iterable<String>)entry.getValue(), left, right);
        }
        ITEM_OVERRIDES.put(class_1306.field_6183, right);
        ITEM_OVERRIDES.put(class_1306.field_6182, left);
    }

    public static void updateSoundEffects(class_310 client) {
        if (!SOUND_ENABLED) {
            lastSoundClip = null;
            lastSoundTime = 0.0f;
            return;
        }
        if (client == null) {
            lastSoundClip = null;
            lastSoundTime = 0.0f;
            return;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (clip == null || !POSE_HANDLER.isPlaying()) {
            lastSoundClip = null;
            lastSoundTime = 0.0f;
            return;
        }
        float currentTime = POSE_HANDLER.getCurrentTime();
        if (clip != lastSoundClip) {
            lastSoundClip = clip;
            lastSoundTime = 0.0f;
            PunchyAnimationManager.playSoundsAtTime(clip, 0.0f, client);
        }
        if (currentTime < lastSoundTime) {
            PunchyAnimationManager.playSoundsBetween(clip, 0.0f, currentTime, client);
        } else {
            PunchyAnimationManager.playSoundsBetween(clip, lastSoundTime, currentTime, client);
        }
        lastSoundTime = currentTime;
    }

    public static class_1799 resolveRenderStack(class_1657 player, class_1306 arm, class_1799 baseStack) {
        class_1799 visualOffhand;
        class_1306 offArm;
        class_1799 forcedConsumeStack;
        boolean forceShow;
        if (player == null || arm == null) {
            return baseStack;
        }
        ItemOverrideState override = ITEM_OVERRIDES.get(arm);
        boolean bl = forceShow = override != null && override.forceShow;
        if (player.method_6115()) {
            class_1306 usedArm;
            class_1268 usedHand = player.method_6058();
            class_1306 class_13062 = usedArm = usedHand == class_1268.field_5808 ? player.method_6068() : PunchyAnimationManager.opposite(player.method_6068());
            if (usedArm == arm) {
                return baseStack;
            }
        }
        if ((forcedConsumeStack = ConsumeStateMachine.getForcedRenderStack(arm)) != null && !forcedConsumeStack.method_7960()) {
            return forcedConsumeStack;
        }
        if (!forceShow && PunchyClient.shouldSuppressOffhandRender(player)) {
            class_1306 class_13063 = offArm = player.method_6068() == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
            if (arm == offArm) {
                return class_1799.field_8037;
            }
        }
        class_1306 class_13064 = offArm = player.method_6068() == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
        if (arm == offArm && HandEquipStateMachine.shouldHideOffhandTridentThrowCacheRender()) {
            return class_1799.field_8037;
        }
        if (arm == offArm && HandEquipStateMachine.isOffhandEquipTransitionActive() && (visualOffhand = HandEquipStateMachine.getVisualOffhandStack((class_746)player)) != null && !visualOffhand.method_7960()) {
            return visualOffhand;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if ((PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide")) && activeSourceArm != null && arm == activeSourceArm) {
            return class_1799.field_8037;
        }
        if (override != null && override.hidden) {
            return class_1799.field_8037;
        }
        if (override != null && !override.stack.method_7960()) {
            return override.stack;
        }
        EquipOverrideState equipOverride = EQUIP_OVERRIDES.get(arm);
        if (equipOverride != null && equipOverride.active) {
            return equipOverride.stack;
        }
        if (PunchyAnimationManager.shouldHideBaseItem(player, arm)) {
            return class_1799.field_8037;
        }
        return baseStack;
    }

    private static boolean shouldHideBaseItem(class_1657 player, class_1306 arm) {
        if (player == null || arm == null) {
            return false;
        }
        ItemOverrideState override = ITEM_OVERRIDES.get(arm);
        if (override != null && override.forceShow) {
            return false;
        }
        if (!PunchyAnimationManager.isDualHandedActive()) {
            return false;
        }
        if (activeSourceArm == null) {
            return false;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (PunchyAnimationManager.isClipNamed(clip, "use_shield")) {
            return false;
        }
        return arm != activeSourceArm;
    }

    public static void setSourceHand(class_310 client, class_1268 hand) {
        activeSourceArm = PunchyAnimationManager.getUseArm(client, hand);
    }

    public static void clearSourceHand() {
        activeSourceArm = null;
    }

    public static class_1306 getSourceArm() {
        return activeSourceArm;
    }

    public static void syncSourceHandToActiveClip() {
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (!POSE_HANDLER.isPlaying() || !PunchyAnimationManager.isDualHandedActive(clip, POSE_HANDLER.getCurrentTime())) {
            activeSourceArm = null;
        }
    }

    private static boolean hasOffhandItem(class_310 client) {
        return client != null && client.field_1724 != null && client.field_1724.method_6079() != null && !client.field_1724.method_6079().method_7960();
    }

    private static boolean hasDualHandedOffhandContext(class_310 client) {
        class_746 class_7462;
        if (PunchyAnimationManager.hasOffhandItem(client)) {
            return true;
        }
        if (client == null || !((class_7462 = client.field_1724) instanceof class_746)) {
            return false;
        }
        class_746 localPlayer = class_7462;
        return PunchyClient.shouldSuppressOffhandRender((class_1657)localPlayer) || PunchyClient.shouldLowerOffhand(localPlayer) || HandEquipStateMachine.shouldKeepOffhandVisibleUntilHandOutEnds() || HandEquipStateMachine.isOffhandEquipTransitionActive();
    }

    private static float resolveDualHandedEffectTime(AnimationClip clip) {
        if (clip == null) {
            return 0.0f;
        }
        if (POSE_HANDLER.getCurrentClip() == clip && POSE_HANDLER.isPlaying()) {
            return POSE_HANDLER.getCurrentTime();
        }
        if (SWIM_HANDLER.getCurrentClip() == clip && SWIM_HANDLER.isPlaying()) {
            return SWIM_HANDLER.getCurrentTime();
        }
        return 0.0f;
    }

    public static void setSuppressOffhandForAttack(AnimationClip clip, boolean enable) {
        if (enable && clip != null) {
            suppressOffhandForAttack = true;
            suppressOffhandClip = clip;
        } else {
            suppressOffhandForAttack = false;
            suppressOffhandClip = null;
        }
    }

    public static boolean shouldSuppressOffhandForAttack() {
        if (!suppressOffhandForAttack) {
            return false;
        }
        if (!POSE_HANDLER.isPlaying()) {
            suppressOffhandForAttack = false;
            suppressOffhandClip = null;
            return false;
        }
        AnimationClip clip = POSE_HANDLER.getCurrentClip();
        if (clip == null || clip != suppressOffhandClip) {
            suppressOffhandForAttack = false;
            suppressOffhandClip = null;
            return false;
        }
        return true;
    }

    public static boolean isItemForceShown(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        ItemOverrideState state = ITEM_OVERRIDES.get(arm);
        return state != null && state.forceShow;
    }

    private static void clearItemOverrides() {
        ITEM_OVERRIDES.clear();
    }

    private static void playSoundsBetween(AnimationClip clip, float start, float end, class_310 client) {
        if (clip == null || client == null) {
            return;
        }
        for (Map.Entry entry : clip.getSoundEffectsBetween(start, end).entrySet()) {
            for (String name : (Set)entry.getValue()) {
                PunchyAnimationManager.playAnimationSound(name, client);
            }
        }
    }

    private static void playSoundsAtTime(AnimationClip clip, float time, class_310 client) {
        if (clip == null || client == null) {
            return;
        }
        for (String name : clip.getSoundEffectsAt(time)) {
            PunchyAnimationManager.playAnimationSound(name, client);
        }
    }

    private static void playAnimationSound(String name, class_310 client) {
        if (name == null || name.isBlank() || client == null) {
            return;
        }
        String clean = name.trim();
        if (clean.isEmpty()) {
            return;
        }
        class_2960 explicitId = class_2960.method_12829((String)clean);
        if (explicitId != null) {
            class_2960 fileId = class_2960.method_60655((String)explicitId.method_12836(), (String)("sounds/" + explicitId.method_12832() + ".ogg"));
            try {
                if (client.method_1478().method_14486(fileId).isPresent()) {
                    class_3414 event = class_3414.method_47908((class_2960)explicitId);
                    client.method_1483().method_4873((class_1113)class_1109.method_4758((class_3414)event, (float)1.0f));
                    return;
                }
            }
            catch (Exception event) {
                // empty catch block
            }
        }
        class_2960 punchyLegacy = class_2960.method_60655((String)"punchy", (String)("animationssounds/" + clean));
        class_2960 punchyLegacyFile = class_2960.method_60655((String)"punchy", (String)("sounds/animationssounds/" + clean + ".ogg"));
        try {
            if (client.method_1478().method_14486(punchyLegacyFile).isPresent()) {
                class_3414 event = class_3414.method_47908((class_2960)punchyLegacy);
                client.method_1483().method_4873((class_1113)class_1109.method_4758((class_3414)event, (float)1.0f));
                return;
            }
        }
        catch (Exception event) {
            // empty catch block
        }
        class_2960 ahpId = class_2960.method_60655((String)"adorablehamsterpets", (String)clean);
        class_2960 ahpFile = class_2960.method_60655((String)"adorablehamsterpets", (String)("sounds/" + clean + ".ogg"));
        try {
            if (client.method_1478().method_14486(ahpFile).isPresent()) {
                class_3414 event = class_3414.method_47908((class_2960)ahpId);
                client.method_1483().method_4873((class_1113)class_1109.method_4758((class_3414)event, (float)1.0f));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void setEquipOverride(class_1306 arm, class_1799 stack) {
        if (arm == null) {
            return;
        }
        EquipOverrideState state = EQUIP_OVERRIDES.computeIfAbsent(arm, key -> new EquipOverrideState());
        state.active = true;
        state.stack = stack == null ? class_1799.field_8037 : stack;
    }

    public static void clearEquipOverride(class_1306 arm) {
        if (arm == null) {
            return;
        }
        EquipOverrideState state = EQUIP_OVERRIDES.get(arm);
        if (state == null) {
            return;
        }
        state.active = false;
        state.stack = class_1799.field_8037;
    }

    public static void clearAllEquipOverrides() {
        for (class_1306 arm : class_1306.values()) {
            PunchyAnimationManager.clearEquipOverride(arm);
        }
    }

    public static boolean isEquipOverrideActive(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        EquipOverrideState state = EQUIP_OVERRIDES.get(arm);
        return state != null && state.active;
    }

    public static boolean hasRenderableEquipOverride(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        EquipOverrideState state = EQUIP_OVERRIDES.get(arm);
        return state != null && state.active && state.stack != null && !state.stack.method_7960();
    }

    public static class_1799 getEquipOverrideStack(class_1306 arm) {
        if (arm == null) {
            return class_1799.field_8037;
        }
        EquipOverrideState state = EQUIP_OVERRIDES.get(arm);
        if (state == null || !state.active || state.stack == null) {
            return class_1799.field_8037;
        }
        return state.stack;
    }

    private static void applyItemEffectsOrdered(Iterable<String> effects, ItemOverrideState left, ItemOverrideState right) {
        String normalized;
        if (effects == null) {
            return;
        }
        for (String effect : effects) {
            if (effect == null || effect.isBlank() || !(normalized = effect.toLowerCase(Locale.ROOT).trim()).startsWith(LEFT_SWITCH_PREFIX) && !normalized.startsWith(RIGHT_SWITCH_PREFIX)) continue;
            PunchyAnimationManager.applyItemEffect(normalized, left, right);
        }
        for (String effect : effects) {
            if (effect == null || effect.isBlank()) continue;
            normalized = effect.toLowerCase(Locale.ROOT).trim();
            if (normalized.startsWith(HIDE_LEFT_SWITCH_PREFIX) || normalized.startsWith(HIDE_RIGHT_SWITCH_PREFIX)) {
                PunchyAnimationManager.applyItemEffect(normalized, left, right);
            }
            if (normalized.equals(SHOW_LEFT_SWITCH) || normalized.equals(SHOW_RIGHT_SWITCH) || normalized.startsWith(SHOW_LEFT_SWITCH_PREFIX) || normalized.startsWith(SHOW_RIGHT_SWITCH_PREFIX)) {
                PunchyAnimationManager.applyItemEffect(normalized, left, right);
            }
            if (!HIDE_ITEM_EFFECT.equals(normalized)) continue;
            PunchyAnimationManager.applyHideItemEffect(left, right);
        }
    }

    private static void applyItemEffect(String effect, ItemOverrideState left, ItemOverrideState right) {
        if (effect == null || effect.isBlank()) {
            return;
        }
        String normalized = effect.toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith(LEFT_SWITCH_PREFIX)) {
            left.stack = PunchyAnimationManager.resolveItemStack(normalized.substring(LEFT_SWITCH_PREFIX.length()));
            left.hidden = false;
            left.forceShow = true;
        } else if (normalized.startsWith(RIGHT_SWITCH_PREFIX)) {
            right.stack = PunchyAnimationManager.resolveItemStack(normalized.substring(RIGHT_SWITCH_PREFIX.length()));
            right.hidden = false;
            right.forceShow = true;
        } else if (normalized.startsWith(HIDE_LEFT_SWITCH_PREFIX)) {
            left.stack = class_1799.field_8037;
            left.hidden = true;
            left.forceShow = false;
        } else if (normalized.startsWith(HIDE_RIGHT_SWITCH_PREFIX)) {
            right.stack = class_1799.field_8037;
            right.hidden = true;
            right.forceShow = false;
        } else if (normalized.equals(SHOW_LEFT_SWITCH)) {
            left.stack = class_1799.field_8037;
            left.hidden = false;
            left.forceShow = true;
        } else if (normalized.equals(SHOW_RIGHT_SWITCH)) {
            right.stack = class_1799.field_8037;
            right.hidden = false;
            right.forceShow = true;
        } else if (normalized.startsWith(SHOW_LEFT_SWITCH_PREFIX)) {
            String suffix = normalized.substring(SHOW_LEFT_SWITCH_PREFIX.length()).trim();
            left.stack = !suffix.isBlank() && !"*".equals(suffix) ? PunchyAnimationManager.resolveItemStack(suffix) : class_1799.field_8037;
            left.hidden = false;
            left.forceShow = true;
        } else if (normalized.startsWith(SHOW_RIGHT_SWITCH_PREFIX)) {
            String suffix = normalized.substring(SHOW_RIGHT_SWITCH_PREFIX.length()).trim();
            right.stack = !suffix.isBlank() && !"*".equals(suffix) ? PunchyAnimationManager.resolveItemStack(suffix) : class_1799.field_8037;
            right.hidden = false;
            right.forceShow = true;
        }
    }

    private static void applyHideItemEffect(ItemOverrideState left, ItemOverrideState right) {
        if (activeSourceArm == null) {
            return;
        }
        if (activeSourceArm == class_1306.field_6183) {
            left.stack = class_1799.field_8037;
            left.hidden = true;
        } else {
            right.stack = class_1799.field_8037;
            right.hidden = true;
        }
    }

    private static class_1799 resolveItemStack(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return class_1799.field_8037;
        }
        class_2960 id = class_2960.method_12829((String)itemId.trim());
        if (id == null) {
            return class_1799.field_8037;
        }
        class_1792 item = class_7923.field_41178.method_17966(id).orElse(class_1802.field_8162);
        return item == class_1802.field_8162 ? class_1799.field_8037 : new class_1799((class_1935)item);
    }

    public static float clipSpeedMultiplier(AnimationClip clip) {
        if (clip == null) {
            return 1.0f;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "use_bow") || PunchyAnimationManager.isClipNamed(clip, "use_shield") || PunchyAnimationManager.isClipNamed(clip, "use_crossbow") || PunchyAnimationManager.isClipNamed(clip, "use_crossbow_shot")) {
            return 1.0f / Math.max(0.01f, ANIMATION_SPEED);
        }
        if (PunchyAnimationManager.isClipNamed(clip, "use_trident") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide") || PunchyAnimationManager.isClipNamed(clip, "attack_mace")) {
            return 1.0f / Math.max(0.01f, ANIMATION_SPEED);
        }
        if (PunchyAnimationManager.isClipNamed(clip, "use_eat") || PunchyAnimationManager.isClipNamed(clip, "use_drink")) {
            return 1.0f;
        }
        if (clip == SWIMING_CLIP) {
            return 1.0f / Math.max(0.01f, ANIMATION_SPEED);
        }
        if (PunchyAnimationManager.isClipNamed(clip, "falling_in_water")) {
            return 1.0f;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "dry_out")) {
            return 1.0f / Math.max(0.01f, ANIMATION_SPEED);
        }
        if (PunchyAnimationManager.isClipNamed(clip, "hand_in") || PunchyAnimationManager.isClipNamed(clip, "hand_out")) {
            return 1.0f / Math.max(0.01f, ANIMATION_SPEED);
        }
        if (PunchyAnimationManager.isClipNamed(clip, "interact")) {
            return 5.4f / Math.max(0.01f, ANIMATION_SPEED);
        }
        return 1.0f;
    }

    public static class_1306 getMiningArm() {
        class_310 mc = class_310.method_1551();
        return mc.field_1724 == null ? class_1306.field_6183 : mc.field_1724.method_6068();
    }

    public static class_1306 getUseArm(class_310 mc, class_1268 hand) {
        if (mc == null || mc.field_1724 == null) {
            return class_1306.field_6183;
        }
        return hand == class_1268.field_5810 ? PunchyAnimationManager.opposite(mc.field_1724.method_6068()) : mc.field_1724.method_6068();
    }

    public static class_1268 getHandForArm(class_310 mc, class_1306 arm) {
        boolean mainRight;
        if (mc == null || mc.field_1724 == null || arm == null) {
            return class_1268.field_5808;
        }
        boolean bl = mainRight = mc.field_1724.method_6068() == class_1306.field_6183;
        if (arm == class_1306.field_6183) {
            return mainRight ? class_1268.field_5808 : class_1268.field_5810;
        }
        return mainRight ? class_1268.field_5810 : class_1268.field_5808;
    }

    public static float resolveMovementForceMultiplier() {
        float pose = PunchyAnimationManager.resolveMovementForceMultiplierForClip(POSE_HANDLER.getCurrentClip(), POSE_HANDLER.isPlaying(), POSE_HANDLER.getCurrentTime());
        float secondary = PunchyAnimationManager.resolveMovementForceMultiplierForClip(POSE_HANDLER.getSecondaryClip(), POSE_HANDLER.isSecondaryCompatIdle(), POSE_HANDLER.getSecondaryTime());
        float swim = PunchyAnimationManager.resolveMovementForceMultiplierForClip(SWIM_HANDLER.getCurrentClip(), SWIM_HANDLER.isPlaying(), SWIM_HANDLER.getCurrentTime());
        return Math.max(0.0f, Math.min(1.0f, Math.min(pose, Math.min(secondary, swim))));
    }

    private static float resolveMovementForceMultiplierForClip(AnimationClip clip, boolean active, float time) {
        if (clip == null || !active) {
            return 1.0f;
        }
        String latest = null;
        Float latestTime = null;
        for (Map.Entry entry : clip.getTimelineEffects().headMap(Float.valueOf(time), true).entrySet()) {
            if (entry == null || entry.getValue() == null || ((Set)entry.getValue()).isEmpty()) continue;
            for (String effect : (Set)entry.getValue()) {
                String normalized = PunchyAnimationManager.normalizeEffect(effect);
                if (normalized == null || !normalized.startsWith(REDUCE_MOVEMENT_FORCE_PREFIX)) continue;
                latest = normalized;
                latestTime = (Float)entry.getKey();
            }
        }
        if (latest != null && latestTime != null && clip.isEffectActive(latest, time)) {
            return PunchyAnimationManager.parseReduceMovementMultiplier(latest);
        }
        for (String effect : clip.getEffects()) {
            String normalized = PunchyAnimationManager.normalizeEffect(effect);
            if (normalized == null || !normalized.startsWith(REDUCE_MOVEMENT_FORCE_PREFIX)) continue;
            return PunchyAnimationManager.parseReduceMovementMultiplier(normalized);
        }
        return 1.0f;
    }

    private static String normalizeEffect(String effect) {
        if (effect == null || effect.isBlank()) {
            return null;
        }
        return effect.toLowerCase(Locale.ROOT).trim();
    }

    private static float parseReduceMovementMultiplier(String normalizedEffect) {
        if (normalizedEffect == null || !normalizedEffect.startsWith(REDUCE_MOVEMENT_FORCE_PREFIX)) {
            return 1.0f;
        }
        String value = normalizedEffect.substring(REDUCE_MOVEMENT_FORCE_PREFIX.length()).trim();
        if (value.isEmpty()) {
            return 1.0f;
        }
        try {
            int percent = Integer.parseInt(value);
            if (percent < 1 || percent > 100) {
                return 1.0f;
            }
            return 1.0f - (float)percent / 100.0f;
        }
        catch (NumberFormatException ignored) {
            return 1.0f;
        }
    }

    private static class_1306 opposite(class_1306 arm) {
        return arm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
    }

    static {
        ANIMATION_SPEED = 5.5f;
        ITEM_OVERRIDES = new EnumMap(class_1306.class);
        EQUIP_OVERRIDES = new EnumMap(class_1306.class);
        SOUND_ENABLED = false;
        ALL_CLIPS = new HashMap<String, AnimationClip>();
        ALL_CLIP_VARIANTS = new HashMap<String, LinkedHashMap<String, AnimationClip>>();
        TYPE_CLIP_ALIASES = PunchyAnimationManager.buildTypeClipAliases();
        CLIP_RESOLUTION_CACHE = new HashMap<String, ClipResolution>();
    }

    public record PackOption(String optionId, String label) {
    }

    public record ClipResolution(AnimationClip clip, String optionId, String note) {
    }

    private static final class ItemOverrideState {
        class_1799 stack = class_1799.field_8037;
        boolean hidden;
        boolean forceShow;

        private ItemOverrideState() {
        }
    }

    private static final class EquipOverrideState {
        class_1799 stack = class_1799.field_8037;
        boolean active;

        private EquipOverrideState() {
        }
    }
}

