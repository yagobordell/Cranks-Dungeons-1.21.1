/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.annotations.SerializedName
 *  net.minecraft.class_156
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3283
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_7923
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package punchy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import net.minecraft.class_156;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3283;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_7923;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import punchy.Punchy;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.io.PunchyAnimationLoader;
import punchy.client.render.ChestProxyTuning;
import punchy.client.render.EnchantParticleProxyTuning;
import punchy.client.render.FishingLineProxyTuning;
import punchy.client.render.FluidProxyTuning;
import punchy.client.render.PaddleProxyTuning;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.ArmMeshTuning;
import punchy.client.renderer.layer.ArmToolTuning;
import punchy.client.renderer.layer.PlayerArmModelLayer;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.SpearStateMachine;
import punchy.client.tuning.TuningProfileManager;
import punchy.config.PunchyConfig;

public final class PunchyTuningConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Punchy/MixPacks");
    private static final boolean PERF_DEBUG = Boolean.getBoolean("punchy.perfDebug");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "punchy_tuning.json";
    private static final String MERGED_ANIMATION_SELECTION_FILE = "merged_animation_selection.json";
    private static final class_2960 DEFAULTS_ID = class_2960.method_60655((String)"punchy", (String)"default_tuning.json");
    private static final String COMPAT_DIR_NAME = "minecraft/punchy/compat";
    private static final float EPSILON = 1.0E-6f;
    private static final String[] BUNDLED_COMPAT_PROFILES = new String[]{"items_compat"};
    private static final String ARM_MESH_SIDE_KEY = "global";
    private static final String MIX_DEFAULT_OPTION_ID = "punchy_default";
    private static final String BUILTIN_PACK_OPTION_ID = "pack:punchy:punchy";
    private static final Set<String> MIX_HIDDEN_SPECIFIC_IDS = Set.of("adorablehamsterpets:cheese");
    private static List<String> cachedSelectedResourcePackIdsHighToLow;
    private static class_3300 cachedSelectedResourcePackResourceManager;
    private static String cachedActiveBuiltInPackId;
    private static class_3300 cachedActiveBuiltInPackResourceManager;
    private static long selectedPackLookupWindowStartedNanos;
    private static int selectedPackLookupCount;
    private static int selectedPackCacheMissCount;
    private static int builtInPackLookupCount;
    private static Data BASELINE_STATE;
    private static Data INITIAL_STATE;
    private static Data DEFAULTS_RESOURCE_STATE;
    private static boolean APPLYING_COMPAT_OVERLAY;
    private static final Map<String, List<String>> REGISTRY_CUSTOM_GROUPS;
    private static MixCatalog MIX_CATALOG;
    private static Set<String> MIX_COMPAT_OPTION_IDS;
    private static Set<String> MIX_PUNCHY_OPTION_IDS;
    private static final EnumMap<VanillaFirstPersonItemLayer.ToolKind, ItemRuntimeFlags> GROUP_RUNTIME_FLAGS;
    private static final Map<class_2960, ItemRuntimeFlags> SPECIFIC_RUNTIME_FLAGS;
    private static final Set<String> SPEAR_MIX_CLIP_NAMES;
    private static final Set<String> SWORD_PASSIVE_CLIP_NAMES;
    private static final Set<String> GENERIC_PASSIVE_CLIP_NAMES;
    private static final Map<String, List<String>> MIX_TYPE_CLIP_ALIASES;

    private PunchyTuningConfig() {
    }

    private static void diag(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
    }

    private static Map<String, List<String>> createMixTypeClipAliases() {
        HashMap<String, List<String>> aliases = new HashMap<String, List<String>>();
        aliases.put("attack", List.of("attack", "attack_sword", "attack_spear"));
        aliases.put("mining", List.of("mining", "mining_foward", "mining_forward", "mining_sideways", "mining_digging"));
        aliases.put("mining_v2", List.of("mining", "mining_foward", "mining_forward", "mining_sideways", "mining_digging"));
        aliases.put("useitem", List.of("use_item", "useitem", "interaction", "interact", "mining_use_foward", "mining_use_forward", "mining_use_digging"));
        aliases.put("interaction", List.of("interaction", "interact", "use_item", "useitem"));
        aliases.put("spear_charge", List.of("use_spear_pose_1", "use_spear_pose_1_left", "use_spear_pose_1_right", "use_spear_pose_2", "use_spear_pose_2_left", "use_spear_pose_2_right", "use_spear_pose_3", "use_spear_pose_3_left", "use_spear_pose_3_right"));
        aliases.put("inspect", List.of("inspect"));
        aliases.put("hand_in", List.of("hand_in", "handin"));
        aliases.put("hand_out", List.of("hand_out", "handout"));
        aliases.put("falling", List.of("falling"));
        aliases.put("triggerable", List.of("triggerable"));
        aliases.put("use_shield", List.of("use_shield"));
        aliases.put("use_bow", List.of("use_bow"));
        aliases.put("use_crossbow", List.of("use_crossbow"));
        aliases.put("use_crossbow_shot", List.of("use_crossbow_shot"));
        aliases.put("useeat", List.of("use_eat", "useeat"));
        aliases.put("usedrink", List.of("use_drink", "usedrink"));
        aliases.put("idle", List.of("idle"));
        return aliases;
    }

    private static boolean isMixThePacksEnabled() {
        return true;
    }

    public static List<String> getMixPackSlotIds() {
        if (!PunchyTuningConfig.isMixThePacksEnabled()) {
            return List.of();
        }
        if (PunchyTuningConfig.MIX_CATALOG.slots.isEmpty()) {
            return List.of();
        }
        return new ArrayList<String>(PunchyTuningConfig.MIX_CATALOG.slots.keySet());
    }

    public static String getMixPackSlotLabel(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return "";
        }
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        if (slot == null) {
            return slotId;
        }
        if (slot.type == MixSlotType.SPECIFIC && !PunchyTuningConfig.isSpecificSlotForced(slot) && !PunchyTuningConfig.shouldAutoUseSpecificSlot(slot)) {
            return slot.label + " (Inherit Kind)";
        }
        return slot.label;
    }

    public static List<String> getMixPackSlotOptionIds(String slotId) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        if (slot == null) {
            return List.of();
        }
        Set<String> activeOptions = PunchyTuningConfig.getActiveMixPackOptionIds();
        LinkedHashSet<String> unique = new LinkedHashSet<String>();
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(MIX_DEFAULT_OPTION_ID);
        unique.add(MIX_DEFAULT_OPTION_ID);
        if (slot.optionOrder != null && !slot.optionOrder.isEmpty()) {
            for (String optionId : slot.optionOrder) {
                if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId) || !activeOptions.contains(optionId) || !PunchyTuningConfig.slotHasOptionPayload(slot, optionId) || !unique.add(optionId)) continue;
                ids.add(optionId);
            }
        }
        return ids;
    }

    private static boolean slotHasOptionPayload(MixSlotDef slot, String optionId) {
        if (slot == null || optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) {
            return false;
        }
        MixSlotPayload payload = slot.optionPayloads.get(optionId);
        if (payload != null) {
            return true;
        }
        Map specificPayloads = slot.optionSpecificPayloads.get(optionId);
        return specificPayloads != null && !specificPayloads.isEmpty();
    }

    public static boolean isMixCompatOption(String optionId) {
        String normalized = PunchyTuningConfig.normalizeMixOption(optionId);
        if (normalized == null || normalized.isBlank() || MIX_DEFAULT_OPTION_ID.equals(normalized)) {
            return false;
        }
        return MIX_COMPAT_OPTION_IDS.contains(normalized);
    }

    private static boolean isUserResourcePackId(String packId) {
        String normalized = PunchyTuningConfig.normalizeMixOption(packId);
        return normalized != null && !normalized.isBlank() && !MIX_DEFAULT_OPTION_ID.equals(normalized);
    }

    public static String getMixPackSelectedOption(String slotId) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        if (slot == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        if (slot.type == MixSlotType.SPECIFIC && !PunchyTuningConfig.isSpecificSlotForced(slot) && !PunchyTuningConfig.shouldAutoUseSpecificSlot(slot)) {
            String inherited = PunchyTuningConfig.resolveInheritedKindOption(slot);
            return inherited == null ? MIX_DEFAULT_OPTION_ID : inherited;
        }
        return PunchyTuningConfig.resolveSelectedOption(slot);
    }

    public static boolean setMixPackSelection(String slotId, String optionId) {
        if (!PunchyTuningConfig.isMixThePacksEnabled()) {
            return false;
        }
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        if (slot == null) {
            return false;
        }
        String normalized = PunchyTuningConfig.normalizeMixOption(optionId);
        if (normalized == null) {
            return false;
        }
        List<String> allowedOptions = PunchyTuningConfig.getMixPackSlotOptionIds(slotId);
        if (!MIX_DEFAULT_OPTION_ID.equals(normalized) && !allowedOptions.contains(normalized)) {
            return false;
        }
        String previous = PunchyConfig.getAnimationPackSelection(slot.slotId);
        PunchyConfig.setAnimationPackSelection(slot.slotId, normalized);
        PunchyConfig.clearAnimationPackTypeSelections(slot.slotId);
        if (slot.type == MixSlotType.SPECIFIC) {
            boolean force = !MIX_DEFAULT_OPTION_ID.equals(normalized);
            PunchyConfig.setMixSpecificForceOverride(slot.slotId, force);
        }
        PunchyTuningConfig.persistMixSelectionsToActiveProfile();
        return true;
    }

    public static List<String> getMixPackSlotEntryTypes(String slotId) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        if (slot == null) {
            return List.of();
        }
        LinkedHashSet<String> types = new LinkedHashSet<String>();
        MixSlotPayload baseline = PunchyTuningConfig.baselinePayloadForSlot(slot);
        PunchyTuningConfig.collectEntryTypes(types, PunchyTuningConfig.payloadCustomAnimation(baseline));
        PunchyTuningConfig.collectEntryTypes(types, PunchyTuningConfig.payloadCustomItemAnimations(baseline));
        for (MixSlotPayload payload : slot.optionPayloads.values()) {
            PunchyTuningConfig.collectEntryTypes(types, PunchyTuningConfig.payloadCustomAnimation(payload));
            PunchyTuningConfig.collectEntryTypes(types, PunchyTuningConfig.payloadCustomItemAnimations(payload));
        }
        return new ArrayList<String>(types);
    }

    public static List<String> getMixPackExpandableEntryTypes(String slotId) {
        List<String> types = PunchyTuningConfig.getMixPackSlotEntryTypes(slotId);
        if (types.isEmpty()) {
            return List.of();
        }
        ArrayList<String> expandable = new ArrayList<String>();
        for (String type : types) {
            if (PunchyTuningConfig.getMixPackSlotEntryTypeOptionIds(slotId, type).size() <= 1) continue;
            expandable.add(type);
        }
        return expandable;
    }

    public static List<String> getMixPackSlotEntryTypeOptionIds(String slotId, String entryType) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        String normalizedType = PunchyTuningConfig.normalizeEntryType(entryType);
        if (slot == null || normalizedType == null) {
            return List.of();
        }
        Set<String> activeOptions = PunchyTuningConfig.getActiveMixPackOptionIds();
        LinkedHashSet<String> ids = new LinkedHashSet<String>();
        ids.add(MIX_DEFAULT_OPTION_ID);
        for (String optionId : slot.optionOrder) {
            if (!activeOptions.contains(optionId) || !PunchyTuningConfig.optionSupportsType(slot, optionId, normalizedType)) continue;
            ids.add(optionId);
        }
        return new ArrayList<String>(ids);
    }

    public static String getMixPackSelectedEntryTypeOption(String slotId, String entryType) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        String normalizedType = PunchyTuningConfig.normalizeEntryType(entryType);
        if (slot == null || normalizedType == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        String main = PunchyTuningConfig.getMixPackSelectedOption(slotId);
        return PunchyTuningConfig.resolveSelectedTypeOption(slot, normalizedType, main);
    }

    public static boolean hasMixPackEntryTypeOverride(String slotId, String entryType) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        String normalizedType = PunchyTuningConfig.normalizeEntryType(entryType);
        if (slot == null || normalizedType == null) {
            return false;
        }
        String selected = PunchyConfig.getAnimationPackTypeSelection(slot.slotId, normalizedType);
        return selected != null && !selected.isBlank();
    }

    public static boolean setMixPackEntryTypeSelection(String slotId, String entryType, String optionId) {
        MixSlotDef slot = PunchyTuningConfig.MIX_CATALOG.slots.get(slotId);
        String normalizedType = PunchyTuningConfig.normalizeEntryType(entryType);
        if (slot == null || normalizedType == null) {
            return false;
        }
        String normalizedOption = PunchyTuningConfig.normalizeMixOption(optionId);
        if (normalizedOption == null) {
            return false;
        }
        List<String> allowed = PunchyTuningConfig.getMixPackSlotEntryTypeOptionIds(slotId, normalizedType);
        String mainOption = PunchyTuningConfig.getMixPackSelectedOption(slotId);
        if (!(allowed.contains(normalizedOption) || mainOption != null && !mainOption.isBlank() && mainOption.equals(normalizedOption))) {
            return false;
        }
        String previous = PunchyConfig.getAnimationPackTypeSelection(slot.slotId, normalizedType);
        if (normalizedOption.equals(mainOption)) {
            PunchyConfig.setAnimationPackTypeSelection(slot.slotId, normalizedType, null);
        } else if (MIX_DEFAULT_OPTION_ID.equals(normalizedOption)) {
            PunchyConfig.setAnimationPackTypeSelection(slot.slotId, normalizedType, null);
        } else {
            PunchyConfig.setAnimationPackTypeSelection(slot.slotId, normalizedType, normalizedOption);
        }
        PunchyTuningConfig.persistMixSelectionsToActiveProfile();
        String effective = PunchyConfig.getAnimationPackTypeSelection(slot.slotId, normalizedType);
        return true;
    }

    public static String getMixPreferredOptionForStack(class_1799 stack) {
        MixSlotDef slot = PunchyTuningConfig.resolveMixSlotForStack(stack);
        if (slot == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        return PunchyTuningConfig.resolveSelectedOption(slot);
    }

    public static String getGlobalMixPriorityOptionId() {
        for (String packId : PunchyTuningConfig.getSelectedResourcePackIdsHighToLow()) {
            String optionId = PunchyTuningConfig.normalizeMixOption(packId);
            if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
            return optionId;
        }
        return MIX_DEFAULT_OPTION_ID;
    }

    public static String getMixPreferredOptionForType(class_1799 stack, String entryType) {
        MixSlotDef slot = PunchyTuningConfig.resolveMixSlotForStack(stack);
        String normalizedType = PunchyTuningConfig.normalizeEntryType(entryType);
        if (slot == null || normalizedType == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        return PunchyTuningConfig.resolveSelectedTypeOption(slot, normalizedType, PunchyTuningConfig.resolveSelectedOption(slot));
    }

    public static String getMixPreferredOptionForEntry(class_1799 stack, CustomAnimationEntry entry) {
        String normalized;
        if (entry != null && entry.mix_pack_option != null && !entry.mix_pack_option.isBlank() && (normalized = PunchyTuningConfig.normalizeMixOption(entry.mix_pack_option)) != null && !normalized.isBlank()) {
            return normalized;
        }
        MixSlotDef slot = PunchyTuningConfig.resolveMixSlotForStack(stack);
        if (slot == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        return PunchyTuningConfig.resolveSelectedOption(slot);
    }

    private static MixSlotDef resolveMixSlotForStack(class_1799 stack) {
        if (MIX_CATALOG == null || PunchyTuningConfig.MIX_CATALOG.slots.isEmpty()) {
            return null;
        }
        if (stack == null || stack.method_7960()) {
            return PunchyTuningConfig.MIX_CATALOG.slots.get("kind:empty_hand");
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId == null) {
            return null;
        }
        String specificSlotId = "specific:" + String.valueOf(itemId);
        MixSlotDef specificSlot = PunchyTuningConfig.MIX_CATALOG.slots.get(specificSlotId);
        if (specificSlot != null && (PunchyTuningConfig.isSpecificSlotForced(specificSlot) || PunchyTuningConfig.shouldAutoUseSpecificSlot(specificSlot))) {
            return specificSlot;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.resolveMixKindForStack(stack);
        if (kind == null) {
            return null;
        }
        String kindSlotId = "kind:" + kind.name().toLowerCase(Locale.ROOT);
        return PunchyTuningConfig.MIX_CATALOG.slots.get(kindSlotId);
    }

    public static void resetMixPackSelectionsToResourcepackOrder() {
        PunchyConfig.clearAnimationPackSelections();
        PunchyTuningConfig.persistMixSelectionsToActiveProfile();
    }

    public static void clearGroupsRegistry() {
        REGISTRY_CUSTOM_GROUPS.clear();
    }

    public static boolean matchesItemList(class_1799 stack, List<String> items) {
        String current;
        if (stack == null || items == null || items.isEmpty()) {
            return false;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        String string = current = itemId == null ? null : itemId.toString();
        if (current == null || current.isBlank()) {
            return false;
        }
        for (String entry : items) {
            String trimmed;
            if (entry == null || (trimmed = entry.trim()).isEmpty()) continue;
            List<String> groupItems = PunchyTuningConfig.resolveCustomGroupItems(trimmed);
            if (groupItems != null && groupItems.contains(current)) {
                return true;
            }
            class_2960 parsed = class_2960.method_12829((String)trimmed);
            if (parsed == null || !current.equals(parsed.toString())) continue;
            return true;
        }
        return false;
    }

    private static List<String> resolveCustomGroupItems(String name) {
        if (name == null) {
            return null;
        }
        List<String> items = REGISTRY_CUSTOM_GROUPS.get(name);
        if (items != null) {
            return items;
        }
        for (Map.Entry<String, List<String>> entry : REGISTRY_CUSTOM_GROUPS.entrySet()) {
            if (entry.getKey() == null || !entry.getKey().equalsIgnoreCase(name)) continue;
            return entry.getValue();
        }
        return null;
    }

    private static void applyItemLevelFishingRodFlags(List<CustomAnimationEntry> entries, Boolean disableWalkOnWater, Boolean disableDryOut, Boolean disableSwiming) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (CustomAnimationEntry entry : entries) {
            String normalized;
            if (entry == null || entry.type == null || !"fishingrod".equals(normalized = entry.type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", ""))) continue;
            if (entry.disable_walk_on_water == null && disableWalkOnWater != null) {
                entry.disable_walk_on_water = disableWalkOnWater;
            }
            if (entry.disable_dry_out == null && disableDryOut != null) {
                entry.disable_dry_out = disableDryOut;
            }
            if (entry.disable_swim_animation != null || disableSwiming == null) continue;
            entry.disable_swim_animation = disableSwiming;
        }
    }

    private static void applyGroupRuntimeFlags(VanillaFirstPersonItemLayer.ToolKind kind, Boolean disableWalkOnWater, Boolean disableDryOut, Boolean disableSwiming) {
        if (kind == null) {
            return;
        }
        if (disableWalkOnWater == null && disableDryOut == null && disableSwiming == null) {
            return;
        }
        ItemRuntimeFlags flags = GROUP_RUNTIME_FLAGS.computeIfAbsent(kind, ignored -> new ItemRuntimeFlags());
        if (disableWalkOnWater != null) {
            flags.disableWalkOnWater = disableWalkOnWater;
        }
        if (disableDryOut != null) {
            flags.disableDryOut = disableDryOut;
        }
        if (disableSwiming != null) {
            flags.disableSwiming = disableSwiming;
        }
    }

    private static void applySpecificRuntimeFlags(class_2960 itemId, Boolean disableWalkOnWater, Boolean disableDryOut, Boolean disableSwiming) {
        if (itemId == null) {
            return;
        }
        if (disableWalkOnWater == null && disableDryOut == null && disableSwiming == null) {
            return;
        }
        ItemRuntimeFlags flags = SPECIFIC_RUNTIME_FLAGS.computeIfAbsent(itemId, ignored -> new ItemRuntimeFlags());
        if (disableWalkOnWater != null) {
            flags.disableWalkOnWater = disableWalkOnWater;
        }
        if (disableDryOut != null) {
            flags.disableDryOut = disableDryOut;
        }
        if (disableSwiming != null) {
            flags.disableSwiming = disableSwiming;
        }
    }

    public static boolean isWalkOnWaterDisabledForStack(class_1799 stack) {
        ItemRuntimeFlags specific;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId != null && (specific = SPECIFIC_RUNTIME_FLAGS.get(itemId)) != null && Boolean.TRUE.equals(specific.disableWalkOnWater)) {
            return true;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.resolveMixKindForStack(stack);
        if (kind == null) {
            return false;
        }
        ItemRuntimeFlags group = GROUP_RUNTIME_FLAGS.get((Object)kind);
        return group != null && Boolean.TRUE.equals(group.disableWalkOnWater);
    }

    public static boolean isDryOutDisabledForStack(class_1799 stack) {
        ItemRuntimeFlags specific;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId != null && (specific = SPECIFIC_RUNTIME_FLAGS.get(itemId)) != null && Boolean.TRUE.equals(specific.disableDryOut)) {
            return true;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.resolveMixKindForStack(stack);
        if (kind == null) {
            return false;
        }
        ItemRuntimeFlags group = GROUP_RUNTIME_FLAGS.get((Object)kind);
        return group != null && Boolean.TRUE.equals(group.disableDryOut);
    }

    public static boolean isSwimingDisabledForStack(class_1799 stack) {
        ItemRuntimeFlags specific;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId != null && (specific = SPECIFIC_RUNTIME_FLAGS.get(itemId)) != null && Boolean.TRUE.equals(specific.disableSwiming)) {
            return true;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.resolveMixKindForStack(stack);
        if (kind == null) {
            return false;
        }
        ItemRuntimeFlags group = GROUP_RUNTIME_FLAGS.get((Object)kind);
        return group != null && Boolean.TRUE.equals(group.disableSwiming);
    }

    private static List<String> readCompatParticleEffects(SpecificEntry entry) {
        if (entry == null) {
            return null;
        }
        ArrayList<String> out = new ArrayList<String>();
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particleEmitters);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particle);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particles);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particleEffects);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particleEmission);
        return out.isEmpty() ? null : out;
    }

    private static List<String> readCompatParticleEffects(GroupEntry entry) {
        if (entry == null) {
            return null;
        }
        ArrayList<String> out = new ArrayList<String>();
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particleEmitters);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particle);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particles);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particleEffects);
        PunchyTuningConfig.appendCompatParticleEffects(out, entry.particleEmission);
        return out.isEmpty() ? null : out;
    }

    private static void appendCompatParticleEffects(List<String> out, JsonElement element) {
        String normalized;
        if (out == null || element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                PunchyTuningConfig.appendCompatParticleEffects(out, child);
            }
            return;
        }
        if (element.isJsonObject()) {
            String script = PunchyTuningConfig.compatParticleObjectToScript(element.getAsJsonObject());
            if (script != null && !script.isBlank()) {
                out.add(script);
            }
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() && (normalized = PunchyTuningConfig.normalizeCompatParticleEffect(element.getAsString())) != null && !normalized.isBlank()) {
            out.add(normalized);
        }
    }

    private static String normalizeCompatParticleEffect(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("particle(") && trimmed.endsWith(")")) {
            return trimmed;
        }
        if (trimmed.contains("=")) {
            return "particle(" + trimmed + ")";
        }
        class_2960 parsed = class_2960.method_12829((String)trimmed);
        if (parsed != null) {
            return "particle(type=" + String.valueOf(parsed) + ")";
        }
        return null;
    }

    private static String compatParticleObjectToScript(JsonObject object) {
        if (object == null) {
            return null;
        }
        if (object.has("enabled") && object.get("enabled").isJsonPrimitive() && !PunchyTuningConfig.readCompatBoolean(object.get("enabled"), true)) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Map.Entry entry : object.entrySet()) {
            String key = (String)entry.getKey();
            if (key == null || key.isBlank() || "enabled".equalsIgnoreCase(key) || PunchyTuningConfig.isIgnoredCompatParticleRenderHint(key)) continue;
            if ("triggers".equalsIgnoreCase(key) && entry.getValue() != null && !((JsonElement)entry.getValue()).isJsonNull()) {
                PunchyTuningConfig.appendCompatTriggerTokens(joiner, (JsonElement)entry.getValue());
                continue;
            }
            if ("on_animation".equalsIgnoreCase(key) && entry.getValue() != null && ((JsonElement)entry.getValue()).isJsonObject()) {
                PunchyTuningConfig.appendCompatOnAnimationTokens(joiner, ((JsonElement)entry.getValue()).getAsJsonObject());
                continue;
            }
            String value = PunchyTuningConfig.compatParticleValueToToken((JsonElement)entry.getValue());
            if (value == null || value.isBlank()) continue;
            joiner.add(key.trim() + "=" + value);
        }
        String body = joiner.toString();
        return body.isBlank() ? null : "particle(" + body + ")";
    }

    private static boolean isIgnoredCompatParticleRenderHint(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return "emissive_strength".equals(normalized) || "emission_strength".equals(normalized) || "emissive_blur".equals(normalized) || "emission_blur".equals(normalized) || "emissive_clamp".equals(normalized) || "emission_clamp".equals(normalized);
    }

    private static String compatParticleValueToToken(JsonElement value) {
        JsonObject obj;
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        if (value.isJsonArray()) {
            JsonArray arr = value.getAsJsonArray();
            boolean allPrimitive = true;
            for (JsonElement part : arr) {
                if (part != null && part.isJsonPrimitive()) continue;
                allPrimitive = false;
                break;
            }
            if (!allPrimitive) {
                return null;
            }
            if (arr.size() == 3) {
                StringJoiner vecJoiner = new StringJoiner(":");
                for (JsonElement part : arr) {
                    vecJoiner.add(part.getAsString());
                }
                return vecJoiner.toString();
            }
            StringJoiner listJoiner = new StringJoiner("|");
            for (JsonElement part : arr) {
                listJoiner.add(part.getAsString());
            }
            return listJoiner.toString();
        }
        if (value.isJsonObject() && (obj = value.getAsJsonObject()).has("x") && obj.has("y") && obj.has("z") && obj.get("x").isJsonPrimitive() && obj.get("y").isJsonPrimitive() && obj.get("z").isJsonPrimitive()) {
            return obj.get("x").getAsString() + ":" + obj.get("y").getAsString() + ":" + obj.get("z").getAsString();
        }
        return null;
    }

    private static void appendCompatOnAnimationTokens(StringJoiner joiner, JsonObject onAnimation) {
        String n;
        if (joiner == null || onAnimation == null) {
            return;
        }
        JsonElement name = onAnimation.get("name");
        JsonElement timecode = onAnimation.get("timecode");
        if (name != null && name.isJsonPrimitive() && (n = name.getAsString()) != null && !n.isBlank()) {
            joiner.add("on_animation_name=" + n.trim());
        }
        if (timecode != null && timecode.isJsonPrimitive()) {
            joiner.add("on_animation_timecode=" + timecode.getAsString());
        }
    }

    private static void appendCompatTriggerTokens(StringJoiner joiner, JsonElement triggersElement) {
        if (joiner == null || triggersElement == null || triggersElement.isJsonNull()) {
            return;
        }
        if (triggersElement.isJsonObject()) {
            PunchyTuningConfig.appendCompatTriggerTokens(joiner, triggersElement.getAsJsonObject());
            return;
        }
        if (triggersElement.isJsonArray()) {
            PunchyTuningConfig.appendCompatTriggerList(joiner, triggersElement.getAsJsonArray());
            return;
        }
        if (triggersElement.isJsonPrimitive()) {
            PunchyTuningConfig.appendCompatNamedTrigger(joiner, triggersElement.getAsString());
        }
    }

    private static void appendCompatTriggerTokens(StringJoiner joiner, JsonObject triggers) {
        List<String> biomes;
        if (joiner == null || triggers == null) {
            return;
        }
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_jump", "jump", "trigger_jump", "triggerJump");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_landing", "landing", "trigger_landing", "triggerLanding");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_walking", "walking", "walkgin", "trigger_walking", "triggerWalking");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_sprinting", "sprinting", "trigger_sprinting", "triggerSprinting");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_swimming", "swimming", "swiming", "trigger_swimming", "triggerSwimming");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_attacking", "attacking", "attack", "trigger_attacking", "triggerAttacking");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_use_item", "use_item", "useitem", "useItem", "trigger_use_item", "triggerUseItem", "trigger_useitem", "triggerUseitem");
        PunchyTuningConfig.appendCompatTrueTrigger(joiner, triggers, "trigger_mining", "mining", "trigger_mining", "triggerMining");
        JsonElement falling = triggers.get("falling");
        if (falling != null && !falling.isJsonNull()) {
            if (falling.isJsonPrimitive()) {
                if (PunchyTuningConfig.readCompatBoolean(falling, false)) {
                    joiner.add("trigger_falling=true");
                }
            } else if (falling.isJsonObject()) {
                joiner.add("trigger_falling=true");
                Float minBlocks = PunchyTuningConfig.readCompatFallingMinBlocks(falling.getAsJsonObject());
                if (minBlocks != null) {
                    joiner.add("trigger_falling_min_blocks=" + minBlocks);
                }
            }
        }
        if (!(biomes = PunchyTuningConfig.readCompatTriggerBiomeNames(triggers.get("biome"))).isEmpty()) {
            joiner.add("trigger_biomes=" + String.join((CharSequence)"|", biomes));
        }
    }

    private static void appendCompatTriggerList(StringJoiner joiner, JsonArray triggers) {
        if (joiner == null || triggers == null) {
            return;
        }
        joiner.add("trigger_mode=any");
        for (JsonElement element : triggers) {
            if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) continue;
            PunchyTuningConfig.appendCompatNamedTrigger(joiner, element.getAsString());
        }
    }

    private static void appendCompatNamedTrigger(StringJoiner joiner, String name) {
        String normalized;
        if (joiner == null || name == null || name.isBlank()) {
            return;
        }
        switch (normalized = name.trim().toLowerCase(Locale.ROOT)) {
            case "jump": 
            case "trigger_jump": 
            case "triggerjump": {
                joiner.add("trigger_jump=true");
                break;
            }
            case "landing": 
            case "trigger_landing": 
            case "triggerlanding": {
                joiner.add("trigger_landing=true");
                break;
            }
            case "walking": 
            case "walkgin": 
            case "trigger_walking": 
            case "triggerwalking": {
                joiner.add("trigger_walking=true");
                break;
            }
            case "sprinting": 
            case "trigger_sprinting": 
            case "triggersprinting": {
                joiner.add("trigger_sprinting=true");
                break;
            }
            case "swimming": 
            case "swiming": 
            case "trigger_swimming": 
            case "triggerswimming": {
                joiner.add("trigger_swimming=true");
                break;
            }
            case "attacking": 
            case "attack": 
            case "trigger_attacking": 
            case "triggerattacking": {
                joiner.add("trigger_attacking=true");
                break;
            }
            case "use_item": 
            case "useitem": 
            case "trigger_use_item": 
            case "triggeruseitem": {
                joiner.add("trigger_use_item=true");
                break;
            }
            case "mining": 
            case "trigger_mining": 
            case "triggermining": {
                joiner.add("trigger_mining=true");
                break;
            }
            case "falling": 
            case "trigger_falling": 
            case "triggerfalling": {
                joiner.add("trigger_falling=true");
                break;
            }
        }
    }

    private static void appendCompatTrueTrigger(StringJoiner joiner, JsonObject triggers, String tokenKey, String ... keys) {
        if (joiner == null || triggers == null || tokenKey == null || keys == null) {
            return;
        }
        for (String key : keys) {
            if (key == null || key.isBlank() || !PunchyTuningConfig.readCompatBoolean(triggers.get(key), false)) continue;
            joiner.add(tokenKey + "=true");
            return;
        }
    }

    private static boolean readCompatBoolean(JsonElement element, boolean fallback) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt() != 0;
            }
            String value = element.getAsString();
            if (value == null) {
                return fallback;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
                return true;
            }
            if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
                return false;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return fallback;
    }

    private static Float readCompatFallingMinBlocks(JsonObject obj) {
        String[] keys;
        if (obj == null) {
            return null;
        }
        for (String key : keys = new String[]{"min_blocks", "minBlocks", "min_height", "minHeight", "min"}) {
            JsonElement element = obj.get(key);
            if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) continue;
            float value = element.getAsFloat();
            return Float.valueOf(Math.max(0.0f, value));
        }
        return null;
    }

    private static List<String> readCompatTriggerBiomeNames(JsonElement biome) {
        JsonElement one;
        JsonElement names;
        if (biome == null || biome.isJsonNull()) {
            return List.of();
        }
        ArrayList<String> out = new ArrayList<String>();
        if (biome.isJsonPrimitive() && biome.getAsJsonPrimitive().isString()) {
            String value = biome.getAsString();
            if (value != null && !value.isBlank()) {
                out.add(value.trim());
            }
            return out;
        }
        if (biome.isJsonArray()) {
            for (JsonElement el : biome.getAsJsonArray()) {
                String value;
                if (el == null || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString() || (value = el.getAsString()) == null || value.isBlank()) continue;
                out.add(value.trim());
            }
            return out;
        }
        if (!biome.isJsonObject()) {
            return out;
        }
        JsonObject obj = biome.getAsJsonObject();
        JsonElement jsonElement = names = obj.has("names") ? obj.get("names") : obj.get("biomes");
        if (names != null) {
            out.addAll(PunchyTuningConfig.readCompatTriggerBiomeNames(names));
            return out;
        }
        JsonElement jsonElement2 = one = obj.has("name") ? obj.get("name") : obj.get("biome");
        if (one != null) {
            out.addAll(PunchyTuningConfig.readCompatTriggerBiomeNames(one));
        }
        return out;
    }

    private static File getFile() {
        Path configDir = PunchyTuningConfig.getConfigDirectory();
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return configDir.resolve("punchy").resolve(FILE_NAME).toFile();
    }

    public static void loadAndApply() {
        PunchyTuningConfig.loadAndApply(class_310.method_1551() != null ? class_310.method_1551().method_1478() : null);
    }

    public static void loadAndApply(class_3300 resourceManager) {
        try {
            Data activeProfileData;
            PunchyTuningConfig.invalidateSelectedResourcePackCache();
            PunchyTuningConfig.clearGroupsRegistry();
            PunchyTuningConfig.resetAllTunings();
            DEFAULTS_RESOURCE_STATE = null;
            JsonObject activeProfileJson = null;
            Path activeProfile = TuningProfileManager.getActiveProfileFilePath();
            if (activeProfile != null && Files.isRegularFile(activeProfile, new LinkOption[0])) {
                try (FileReader reader = new FileReader(activeProfile.toFile());){
                    JsonElement profileElement = (JsonElement)GSON.fromJson((Reader)reader, JsonElement.class);
                    if (profileElement != null && profileElement.isJsonObject()) {
                        activeProfileJson = profileElement.getAsJsonObject();
                    }
                }
            }
            Data baseData = PunchyTuningConfig.buildBaseDefaults(resourceManager);
            DEFAULTS_RESOURCE_STATE = PunchyTuningConfig.copyData(baseData);
            if (baseData != null) {
                if (baseData.customGroups != null) {
                    for (CustomGroupDefinition customGroupDefinition : baseData.customGroups) {
                        if (customGroupDefinition == null || customGroupDefinition.name == null || customGroupDefinition.items == null) continue;
                        REGISTRY_CUSTOM_GROUPS.put(customGroupDefinition.name, customGroupDefinition.items);
                    }
                }
                PunchyTuningConfig.applyData(PunchyTuningConfig.copyData(baseData));
            }
            if ((activeProfileData = PunchyTuningConfig.parseData(activeProfileJson)) != null) {
                if (activeProfileData.customGroups != null) {
                    for (CustomGroupDefinition g : activeProfileData.customGroups) {
                        if (g == null || g.name == null || g.items == null) continue;
                        REGISTRY_CUSTOM_GROUPS.put(g.name, g.items);
                    }
                }
                PunchyTuningConfig.applyData(PunchyTuningConfig.copyData(activeProfileData));
                PunchyTuningConfig.saveData(activeProfileData);
            }
            Set<class_2960> set = PunchyTuningConfig.discoverCompatResourceIds(resourceManager).keySet();
            MIX_COMPAT_OPTION_IDS = PunchyTuningConfig.resolveMixCompatOptionIds(resourceManager, set);
            MIX_PUNCHY_OPTION_IDS = PunchyTuningConfig.discoverPunchyOptionIds(resourceManager, set);
            MIX_CATALOG = PunchyTuningConfig.buildMixCatalog(resourceManager, PunchyTuningConfig.discoverCompatProfilesFromResourcePacks(resourceManager));
            PunchyTuningConfig.applyMixSelections();
            PunchyTuningConfig.reapplyActiveProfileSpecificTransforms(activeProfileData);
            PunchyTuningConfig.rememberBaseline();
            INITIAL_STATE = PunchyTuningConfig.captureCurrent();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void reloadActiveProfileFast(class_3300 resourceManager) {
        try {
            Data activeProfileData;
            Data baseData;
            PunchyTuningConfig.invalidateSelectedResourcePackCache();
            if (DEFAULTS_RESOURCE_STATE == null) {
                PunchyTuningConfig.loadAndApply(resourceManager);
                return;
            }
            PunchyTuningConfig.clearGroupsRegistry();
            PunchyTuningConfig.resetAllTunings();
            Data freshBaseData = PunchyTuningConfig.buildBaseDefaults(resourceManager);
            if (freshBaseData != null) {
                DEFAULTS_RESOURCE_STATE = PunchyTuningConfig.copyData(freshBaseData);
            }
            if ((baseData = PunchyTuningConfig.copyData(DEFAULTS_RESOURCE_STATE)) != null) {
                if (baseData.customGroups != null) {
                    for (CustomGroupDefinition g : baseData.customGroups) {
                        if (g == null || g.name == null || g.items == null) continue;
                        REGISTRY_CUSTOM_GROUPS.put(g.name, g.items);
                    }
                }
                PunchyTuningConfig.applyData(PunchyTuningConfig.copyData(baseData));
            }
            JsonObject activeProfileJson = null;
            Path activeProfile = TuningProfileManager.getActiveProfileFilePath();
            if (activeProfile != null && Files.isRegularFile(activeProfile, new LinkOption[0])) {
                try (FileReader reader = new FileReader(activeProfile.toFile());){
                    JsonElement profileElement = (JsonElement)GSON.fromJson((Reader)reader, JsonElement.class);
                    if (profileElement != null && profileElement.isJsonObject()) {
                        activeProfileJson = profileElement.getAsJsonObject();
                    }
                }
            }
            if ((activeProfileData = PunchyTuningConfig.parseData(activeProfileJson)) != null) {
                if (activeProfileData.customGroups != null) {
                    for (CustomGroupDefinition g : activeProfileData.customGroups) {
                        if (g == null || g.name == null || g.items == null) continue;
                        REGISTRY_CUSTOM_GROUPS.put(g.name, g.items);
                    }
                }
                PunchyTuningConfig.applyData(PunchyTuningConfig.copyData(activeProfileData));
                PunchyTuningConfig.saveData(activeProfileData);
            }
            PunchyTuningConfig.applyMixSelections();
            PunchyTuningConfig.reapplyActiveProfileSpecificTransforms(activeProfileData);
            PunchyTuningConfig.rememberBaseline();
            INITIAL_STATE = PunchyTuningConfig.captureCurrent();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static JsonObject loadDefaultsJsonFromResource(class_3300 resourceManager) {
        if (resourceManager == null) {
            return null;
        }
        try {
            class_3298 resource = resourceManager.getResourceOrThrow(DEFAULTS_ID);
            try (InputStream stream = resource.method_14482();
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);){
                JsonElement element = (JsonElement)GSON.fromJson((Reader)reader, JsonElement.class);
                if (element == null) return null;
                if (!element.isJsonObject()) return null;
                JsonObject jsonObject = element.getAsJsonObject();
                return jsonObject;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static Data buildBaseDefaults(class_3300 resourceManager) {
        Data resourceDefaults = PunchyTuningConfig.parseData(PunchyTuningConfig.loadDefaultsJsonFromResource(resourceManager));
        Data mergedCompat = PunchyTuningConfig.buildMergedCompatOverlay(resourceManager);
        return PunchyTuningConfig.mergeData(PunchyTuningConfig.copyData(resourceDefaults), PunchyTuningConfig.copyData(mergedCompat));
    }

    private static boolean isPunchyDefaultPackId(String packId) {
        if (packId == null || packId.isBlank()) {
            return false;
        }
        String normalized = packId.trim().toLowerCase(Locale.ROOT);
        return "punchy".equals(normalized) || normalized.startsWith("punchy:") || normalized.endsWith(":punchy") || normalized.endsWith("/punchy") || normalized.endsWith("\\punchy") || "builtin/punchy".equals(normalized) || "builtin\\punchy".equals(normalized);
    }

    private static Data parseData(JsonObject json) {
        return json == null ? null : (Data)GSON.fromJson((JsonElement)json, Data.class);
    }

    private static JsonObject deepMergeJson(JsonObject base, JsonObject overlay) {
        if (base == null) {
            return overlay == null ? null : overlay.deepCopy();
        }
        JsonObject merged = base.deepCopy();
        if (overlay == null) {
            return merged;
        }
        for (Map.Entry entry : overlay.entrySet()) {
            JsonElement overlayValue = (JsonElement)entry.getValue();
            if (overlayValue == null || overlayValue.isJsonNull()) {
                merged.add((String)entry.getKey(), (JsonElement)JsonNull.INSTANCE);
                continue;
            }
            JsonElement baseValue = merged.get((String)entry.getKey());
            if (baseValue != null && baseValue.isJsonObject() && overlayValue.isJsonObject()) {
                merged.add((String)entry.getKey(), (JsonElement)PunchyTuningConfig.deepMergeJson(baseValue.getAsJsonObject(), overlayValue.getAsJsonObject()));
                continue;
            }
            merged.add((String)entry.getKey(), overlayValue.deepCopy());
        }
        return merged;
    }

    private static PhysicsEntry resolvePhysicsEntry(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String name = element.getAsString();
                if (name == null || name.isBlank()) {
                    return null;
                }
                PhysicsEntry entry = new PhysicsEntry();
                entry.name = name;
                return entry;
            }
            if (element.isJsonObject()) {
                PhysicsEntry entry = (PhysicsEntry)GSON.fromJson(element, PhysicsEntry.class);
                if (entry != null && entry.name != null && entry.name.isBlank()) {
                    entry.name = null;
                }
                return entry;
            }
            if (element.isJsonArray()) {
                for (JsonElement entryElement : element.getAsJsonArray()) {
                    PhysicsEntry entry = PunchyTuningConfig.resolvePhysicsEntry(entryElement);
                    if (entry == null || entry.name == null || entry.name.isBlank()) continue;
                    return entry;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static ToolTuning.PhysicsMode resolvePhysicsMode(PhysicsEntry entry) {
        if (entry == null || entry.name == null || entry.name.isBlank()) {
            return null;
        }
        try {
            return ToolTuning.PhysicsMode.valueOf(entry.name.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    private static ToolTuning.PhysicsSettings resolvePhysicsSettings(PhysicsEntry entry) {
        if (entry == null) {
            return null;
        }
        ToolTuning.PhysicsSettings settings = new ToolTuning.PhysicsSettings();
        boolean hasValue = false;
        if (entry.limitDeg != null) {
            settings.limitDeg = entry.limitDeg;
            hasValue = true;
        }
        if (entry.limitDegUp != null) {
            settings.limitDegUp = entry.limitDegUp;
            hasValue = true;
        }
        if (entry.limitDegDown != null) {
            settings.limitDegDown = entry.limitDegDown;
            hasValue = true;
        }
        if (entry.force != null) {
            settings.force = entry.force;
            hasValue = true;
        }
        if (entry.speed != null) {
            settings.speed = entry.speed;
            hasValue = true;
        }
        if (entry.cameraSens != null) {
            settings.cameraSens = entry.cameraSens;
            hasValue = true;
        }
        if (entry.gravity != null) {
            settings.gravity = entry.gravity;
            hasValue = true;
        }
        if (entry.motionSens != null) {
            settings.motionSens = entry.motionSens;
            hasValue = true;
        }
        if (entry.invert != null) {
            settings.invert = entry.invert;
            hasValue = true;
        }
        if (entry.handSpace != null) {
            settings.handSpace = entry.handSpace;
            hasValue = true;
        }
        if (entry.enableCreaturePhysics != null) {
            settings.enableCreaturePhysics = entry.enableCreaturePhysics;
            hasValue = true;
        }
        return hasValue ? settings : null;
    }

    private static void applyData(Data data) {
        PunchyTuningConfig.applyData(data, true);
    }

    private static void applyData(Data data, boolean resetRuntimeFlags) {
        SideSet meshSet;
        SideSet sideSet;
        PlayerArmModelLayer.ToolKind armKind;
        ToolTuning.Profile profile;
        PhysicsEntry physicsEntry;
        if (data == null) {
            return;
        }
        if (data.item == null) {
            data.item = new HashMap<String, GroupEntry>();
        }
        if (resetRuntimeFlags) {
            GROUP_RUNTIME_FLAGS.clear();
            SPECIFIC_RUNTIME_FLAGS.clear();
        }
        for (Map.Entry<String, GroupEntry> entry : data.item.entrySet()) {
            List<String> items;
            String key = entry.getKey();
            GroupEntry groupConfig = entry.getValue();
            if (groupConfig == null || (items = REGISTRY_CUSTOM_GROUPS.get(key)) == null) continue;
            if (groupConfig.transforms == null && (groupConfig.right != null || groupConfig.left != null)) {
                groupConfig.transforms = new SideSet();
                groupConfig.transforms.right = groupConfig.right;
                groupConfig.transforms.left = groupConfig.left;
            }
            PunchyTuningConfig.applyItemLevelFishingRodFlags(groupConfig.customAnimation, groupConfig.disableWalkOnWater, groupConfig.disableDryOut, groupConfig.disableSwiming);
            for (String itemId : items) {
                class_1792 item;
                class_2960 id = class_2960.method_12829((String)itemId);
                if (id == null || (item = class_7923.field_41178.method_17966(id).orElse(class_1802.field_8162)) == class_1802.field_8162) continue;
                class_1799 dummyStack = new class_1799((class_1935)item);
                VanillaFirstPersonItemLayer.ToolKind resolvedKind = ToolKindResolver.resolveItemKind(dummyStack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
                PunchyTuningConfig.applySpecificRuntimeFlags(id, groupConfig.disableWalkOnWater, groupConfig.disableDryOut, groupConfig.disableSwiming);
                PunchyTuningConfig.applyGroupRuntimeFlags(resolvedKind, groupConfig.disableWalkOnWater, groupConfig.disableDryOut, groupConfig.disableSwiming);
                ToolTuning.Profile right = groupConfig.transforms != null && groupConfig.transforms.right != null ? groupConfig.transforms.right.toTool() : null;
                ToolTuning.Profile left = groupConfig.transforms != null && groupConfig.transforms.left != null ? groupConfig.transforms.left.toTool() : null;
                physicsEntry = PunchyTuningConfig.resolvePhysicsEntry(groupConfig.physics);
                ToolTuning.PhysicsMode mode = PunchyTuningConfig.resolvePhysicsMode(physicsEntry);
                ToolTuning.PhysicsSettings settings = PunchyTuningConfig.resolvePhysicsSettings(physicsEntry);
                if (mode != null) {
                    if (right != null) {
                        right.physicsMode = mode;
                        if (settings != null) {
                            right.physicsSettings = settings.copy();
                        }
                    }
                    if (left != null) {
                        left.physicsMode = mode;
                        if (settings != null) {
                            left.physicsSettings = settings.copy();
                        }
                    }
                }
                ToolTuning.applySpecific(id, resolvedKind, null, groupConfig.customAnimation, groupConfig.customItemAnimations, PunchyTuningConfig.readCompatParticleEffects(groupConfig), groupConfig.betterCombat, null, right, left);
                if (mode != null) {
                    if (right == null && (profile = ToolTuning.getSpecificProfile(id, resolvedKind, false)) != null) {
                        profile.physicsMode = mode;
                        if (settings != null) {
                            profile.physicsSettings = settings.copy();
                        }
                    }
                    if (left == null && (profile = ToolTuning.getSpecificProfile(id, resolvedKind, true)) != null) {
                        profile.physicsMode = mode;
                        if (settings != null) {
                            profile.physicsSettings = settings.copy();
                        }
                    }
                }
                if (groupConfig.armTransforms == null) continue;
                armKind = ToolKindResolver.resolveArmKind(dummyStack, PlayerArmModelLayer.ToolKind.AXE);
                ArmToolTuning.Profile armRight = groupConfig.armTransforms.right != null ? PunchyTuningConfig.deserializeSpecificArmProfileForRuntime(armKind, false, groupConfig.armTransforms.right.toArm()) : null;
                ArmToolTuning.Profile armLeft = groupConfig.armTransforms.left != null ? PunchyTuningConfig.deserializeSpecificArmProfileForRuntime(armKind, true, groupConfig.armTransforms.left.toArm()) : null;
                ArmToolTuning.applySpecific(id, armKind, armRight, armLeft);
            }
        }
        SideSet globalItem = PunchyTuningConfig.resolveGlobalItem(data);
        if (globalItem != null) {
            if (globalItem.right != null) {
                ToolTuning.applyGlobal(false, globalItem.right.toTool());
            }
            if (globalItem.left != null) {
                ToolTuning.applyGlobal(true, globalItem.left.toTool());
            }
        }
        if (data.default_animations != null && !data.default_animations.isEmpty()) {
            List<BetterCombatEntry> list = PunchyTuningConfig.extractDefaultBetterCombatFromDefaultAnimations(data.default_animations);
            List<CustomAnimationEntry> filteredDefaults = PunchyTuningConfig.stripBetterCombatFromDefaultAnimations(data.default_animations);
            ToolTuning.setDefaultAnimations(filteredDefaults);
            ToolTuning.setDefaultBetterCombat(list);
        }
        for (VanillaFirstPersonItemLayer.ToolKind kind : VanillaFirstPersonItemLayer.ToolKind.values()) {
            SideSet armSet;
            ToolTuning.Profile profile2;
            List<String> groupParticles;
            GroupEntry entry = data.item.get(kind.name());
            if (entry == null) continue;
            PunchyTuningConfig.applyGroupRuntimeFlags(kind, entry.disableWalkOnWater, entry.disableDryOut, entry.disableSwiming);
            if (entry.transforms == null && (entry.right != null || entry.left != null)) {
                entry.transforms = new SideSet();
                entry.transforms.right = entry.right;
                entry.transforms.left = entry.left;
            }
            if (entry.customAnimation != null) {
                PunchyTuningConfig.applyItemLevelFishingRodFlags(entry.customAnimation, entry.disableWalkOnWater, entry.disableDryOut, entry.disableSwiming);
                ToolTuning.applyGroupAnimation(kind, entry.customAnimation);
            }
            if (entry.customItemAnimations != null) {
                ToolTuning.applyGroupItemAnimation(kind, entry.customItemAnimations);
            }
            if ((groupParticles = PunchyTuningConfig.readCompatParticleEffects(entry)) != null) {
                ToolTuning.applyGroupParticleEmitters(kind, groupParticles);
            }
            if (entry.betterCombat != null) {
                ToolTuning.applyGroupBetterCombat(kind, entry.betterCombat);
            }
            SideSet set = entry.transforms;
            PhysicsEntry physicsEntry2 = PunchyTuningConfig.resolvePhysicsEntry(entry.physics);
            ToolTuning.PhysicsMode groupPhysics = PunchyTuningConfig.resolvePhysicsMode(physicsEntry2);
            ToolTuning.PhysicsSettings groupSettings = PunchyTuningConfig.resolvePhysicsSettings(physicsEntry2);
            Boolean groupDisableSprintLateralTilt = entry.disableSprintLateralTilt;
            Boolean groupKeepMainArmTuning = entry.keepTriggerArmTuning;
            Boolean groupKeepArmTuningRight = PunchyTuningConfig.resolveSideKeepArmTuning(entry.keepArmTuning, entry.keepArmTuningR);
            Boolean groupKeepArmTuningLeft = PunchyTuningConfig.resolveSideKeepArmTuning(entry.keepArmTuning, entry.keepArmTuningL);
            if (groupPhysics != null) {
                ToolTuning.Profile profile22;
                if (set != null) {
                    if (set.right != null && (set.right.physics == null || set.right.physics.isBlank())) {
                        set.right.physics = groupPhysics.name();
                    }
                    if (set.left != null && (set.left.physics == null || set.left.physics.isBlank())) {
                        set.left.physics = groupPhysics.name();
                    }
                }
                if ((set == null || set.right == null) && (profile22 = ToolTuning.get(kind, false)) != null) {
                    profile22.physicsMode = groupPhysics;
                    if (groupSettings != null) {
                        profile22.physicsSettings = groupSettings.copy();
                    }
                    ToolTuning.commitKind(kind, false);
                }
                if ((set == null || set.left == null) && (profile2 = ToolTuning.get(kind, true)) != null) {
                    profile2.physicsMode = groupPhysics;
                    if (groupSettings != null) {
                        profile2.physicsSettings = groupSettings.copy();
                    }
                    ToolTuning.commitKind(kind, true);
                }
            }
            if (groupDisableSprintLateralTilt != null) {
                if (set != null) {
                    if (set.right != null && set.right.disableSprintLateralTilt == null) {
                        set.right.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                    }
                    if (set.left != null && set.left.disableSprintLateralTilt == null) {
                        set.left.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                    }
                }
                if ((set == null || set.right == null) && (profile2 = ToolTuning.get(kind, false)) != null) {
                    profile2.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                    ToolTuning.commitKind(kind, false);
                }
                if ((set == null || set.left == null) && (profile2 = ToolTuning.get(kind, true)) != null) {
                    profile2.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                    ToolTuning.commitKind(kind, true);
                }
            }
            if (groupKeepArmTuningRight != null || groupKeepArmTuningLeft != null) {
                if (set != null) {
                    if (set.right != null && set.right.keepArmTuning == null && groupKeepArmTuningRight != null) {
                        set.right.keepArmTuning = groupKeepArmTuningRight;
                    }
                    if (set.left != null && set.left.keepArmTuning == null && groupKeepArmTuningLeft != null) {
                        set.left.keepArmTuning = groupKeepArmTuningLeft;
                    }
                }
                if ((set == null || set.right == null) && groupKeepArmTuningRight != null && (profile2 = ToolTuning.get(kind, false)) != null) {
                    profile2.keepArmTuning = groupKeepArmTuningRight;
                    ToolTuning.commitKind(kind, false);
                }
                if ((set == null || set.left == null) && groupKeepArmTuningLeft != null && (profile2 = ToolTuning.get(kind, true)) != null) {
                    profile2.keepArmTuning = groupKeepArmTuningLeft;
                    ToolTuning.commitKind(kind, true);
                }
            }
            if (groupKeepMainArmTuning != null) {
                if (set != null) {
                    if (set.right != null && set.right.keepTriggerArmTuning == null) {
                        set.right.keepTriggerArmTuning = groupKeepMainArmTuning;
                    }
                    if (set.left != null && set.left.keepTriggerArmTuning == null) {
                        set.left.keepTriggerArmTuning = groupKeepMainArmTuning;
                    }
                }
                if ((set == null || set.right == null) && (profile2 = ToolTuning.get(kind, false)) != null) {
                    profile2.keepTriggerArmTuning = groupKeepMainArmTuning;
                    ToolTuning.commitKind(kind, false);
                }
                if ((set == null || set.left == null) && (profile2 = ToolTuning.get(kind, true)) != null) {
                    profile2.keepTriggerArmTuning = groupKeepMainArmTuning;
                    ToolTuning.commitKind(kind, true);
                }
            }
            if (set != null) {
                if (set.right != null) {
                    profile2 = set.right.toTool();
                    if (groupDisableSprintLateralTilt != null && set.right.disableSprintLateralTilt == null) {
                        profile2.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                    }
                    if (groupKeepArmTuningRight != null && set.right.keepArmTuning == null) {
                        profile2.keepArmTuning = groupKeepArmTuningRight;
                    }
                    if (groupKeepMainArmTuning != null && set.right.keepTriggerArmTuning == null) {
                        profile2.keepTriggerArmTuning = groupKeepMainArmTuning;
                    }
                    if (groupPhysics != null && (set.right.physics == null || set.right.physics.isBlank())) {
                        profile2.physicsMode = groupPhysics;
                    }
                    if (groupSettings != null && groupPhysics != null && profile2.physicsMode == groupPhysics) {
                        profile2.physicsSettings = groupSettings.copy();
                    }
                    ToolTuning.apply(kind, false, profile2);
                    ToolTuning.markKindDetached(kind, false);
                }
                if (set.left != null) {
                    profile2 = set.left.toTool();
                    if (groupDisableSprintLateralTilt != null && set.left.disableSprintLateralTilt == null) {
                        profile2.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                    }
                    if (groupKeepArmTuningLeft != null && set.left.keepArmTuning == null) {
                        profile2.keepArmTuning = groupKeepArmTuningLeft;
                    }
                    if (groupKeepMainArmTuning != null && set.left.keepTriggerArmTuning == null) {
                        profile2.keepTriggerArmTuning = groupKeepMainArmTuning;
                    }
                    if (groupPhysics != null && (set.left.physics == null || set.left.physics.isBlank())) {
                        profile2.physicsMode = groupPhysics;
                    }
                    if (groupSettings != null && groupPhysics != null && profile2.physicsMode == groupPhysics) {
                        profile2.physicsSettings = groupSettings.copy();
                    }
                    ToolTuning.apply(kind, true, profile2);
                    ToolTuning.markKindDetached(kind, true);
                }
            }
            if ((armSet = entry.armTransforms) == null) continue;
            armKind = null;
            try {
                armKind = PlayerArmModelLayer.ToolKind.valueOf(kind.name());
            }
            catch (IllegalArgumentException armRight) {
                // empty catch block
            }
            if (armKind == null) continue;
            if (armSet.right != null) {
                ArmToolTuning.apply(armKind, false, PunchyTuningConfig.deserializeKindArmProfileForRuntime(false, armSet.right.toArm()));
                ArmToolTuning.markKindDetached(armKind, false);
            }
            if (armSet.left == null) continue;
            ArmToolTuning.apply(armKind, true, PunchyTuningConfig.deserializeKindArmProfileForRuntime(true, armSet.left.toArm()));
            ArmToolTuning.markKindDetached(armKind, true);
        }
        if (data.emptyHand != null) {
            if (data.emptyHand.customAnimation != null) {
                ToolTuning.applyGroupAnimation(VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND, data.emptyHand.customAnimation);
            }
            if (data.emptyHand.customItemAnimations != null) {
                ToolTuning.applyGroupItemAnimation(VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND, data.emptyHand.customItemAnimations);
            }
            if (data.emptyHand.betterCombat != null) {
                ToolTuning.applyGroupBetterCombat(VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND, data.emptyHand.betterCombat);
            }
            if (data.emptyHand.transforms != null || data.emptyHand.physics != null) {
                ToolTuning.Profile profile3;
                VanillaFirstPersonItemLayer.ToolKind toolKind = VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND;
                SideSet set = data.emptyHand.transforms;
                PhysicsEntry physicsEntry3 = PunchyTuningConfig.resolvePhysicsEntry(data.emptyHand.physics);
                ToolTuning.PhysicsMode groupPhysics = PunchyTuningConfig.resolvePhysicsMode(physicsEntry3);
                ToolTuning.PhysicsSettings groupSettings = PunchyTuningConfig.resolvePhysicsSettings(physicsEntry3);
                Boolean groupDisableSprintLateralTilt = data.emptyHand.disableSprintLateralTilt;
                Boolean groupKeepMainArmTuning = data.emptyHand.keepTriggerArmTuning;
                Boolean groupKeepArmTuningRight = PunchyTuningConfig.resolveSideKeepArmTuning(data.emptyHand.keepArmTuning, data.emptyHand.keepArmTuningR);
                Boolean groupKeepArmTuningLeft = PunchyTuningConfig.resolveSideKeepArmTuning(data.emptyHand.keepArmTuning, data.emptyHand.keepArmTuningL);
                if (groupPhysics != null) {
                    ToolTuning.Profile profile32;
                    if (set != null) {
                        if (set.right != null && (set.right.physics == null || set.right.physics.isBlank())) {
                            set.right.physics = groupPhysics.name();
                        }
                        if (set.left != null && (set.left.physics == null || set.left.physics.isBlank())) {
                            set.left.physics = groupPhysics.name();
                        }
                    }
                    if ((set == null || set.right == null) && (profile32 = ToolTuning.get(toolKind, false)) != null) {
                        profile32.physicsMode = groupPhysics;
                        if (groupSettings != null) {
                            profile32.physicsSettings = groupSettings.copy();
                        }
                        ToolTuning.commitKind(toolKind, false);
                    }
                    if ((set == null || set.left == null) && (profile3 = ToolTuning.get(toolKind, true)) != null) {
                        profile3.physicsMode = groupPhysics;
                        if (groupSettings != null) {
                            profile3.physicsSettings = groupSettings.copy();
                        }
                        ToolTuning.commitKind(toolKind, true);
                    }
                }
                if (groupDisableSprintLateralTilt != null) {
                    if (set != null) {
                        if (set.right != null && set.right.disableSprintLateralTilt == null) {
                            set.right.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                        }
                        if (set.left != null && set.left.disableSprintLateralTilt == null) {
                            set.left.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                        }
                    }
                    if ((set == null || set.right == null) && (profile3 = ToolTuning.get(toolKind, false)) != null) {
                        profile3.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                        ToolTuning.commitKind(toolKind, false);
                    }
                    if ((set == null || set.left == null) && (profile3 = ToolTuning.get(toolKind, true)) != null) {
                        profile3.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                        ToolTuning.commitKind(toolKind, true);
                    }
                }
                if (groupKeepArmTuningRight != null || groupKeepArmTuningLeft != null) {
                    if (set != null) {
                        if (set.right != null && set.right.keepArmTuning == null && groupKeepArmTuningRight != null) {
                            set.right.keepArmTuning = groupKeepArmTuningRight;
                        }
                        if (set.left != null && set.left.keepArmTuning == null && groupKeepArmTuningLeft != null) {
                            set.left.keepArmTuning = groupKeepArmTuningLeft;
                        }
                    }
                    if ((set == null || set.right == null) && groupKeepArmTuningRight != null && (profile3 = ToolTuning.get(toolKind, false)) != null) {
                        profile3.keepArmTuning = groupKeepArmTuningRight;
                        ToolTuning.commitKind(toolKind, false);
                    }
                    if ((set == null || set.left == null) && groupKeepArmTuningLeft != null && (profile3 = ToolTuning.get(toolKind, true)) != null) {
                        profile3.keepArmTuning = groupKeepArmTuningLeft;
                        ToolTuning.commitKind(toolKind, true);
                    }
                }
                if (groupKeepMainArmTuning != null) {
                    if (set != null) {
                        if (set.right != null && set.right.keepTriggerArmTuning == null) {
                            set.right.keepTriggerArmTuning = groupKeepMainArmTuning;
                        }
                        if (set.left != null && set.left.keepTriggerArmTuning == null) {
                            set.left.keepTriggerArmTuning = groupKeepMainArmTuning;
                        }
                    }
                    if ((set == null || set.right == null) && (profile3 = ToolTuning.get(toolKind, false)) != null) {
                        profile3.keepTriggerArmTuning = groupKeepMainArmTuning;
                        ToolTuning.commitKind(toolKind, false);
                    }
                    if ((set == null || set.left == null) && (profile3 = ToolTuning.get(toolKind, true)) != null) {
                        profile3.keepTriggerArmTuning = groupKeepMainArmTuning;
                        ToolTuning.commitKind(toolKind, true);
                    }
                }
                if (set != null) {
                    if (set.right != null) {
                        profile3 = set.right.toTool();
                        if (groupDisableSprintLateralTilt != null && set.right.disableSprintLateralTilt == null) {
                            profile3.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                        }
                        if (groupKeepArmTuningRight != null && set.right.keepArmTuning == null) {
                            profile3.keepArmTuning = groupKeepArmTuningRight;
                        }
                        if (groupKeepMainArmTuning != null && set.right.keepTriggerArmTuning == null) {
                            profile3.keepTriggerArmTuning = groupKeepMainArmTuning;
                        }
                        if (groupPhysics != null && (set.right.physics == null || set.right.physics.isBlank())) {
                            profile3.physicsMode = groupPhysics;
                        }
                        if (groupSettings != null && groupPhysics != null && profile3.physicsMode == groupPhysics) {
                            profile3.physicsSettings = groupSettings.copy();
                        }
                        ToolTuning.apply(toolKind, false, profile3);
                        ToolTuning.markKindDetached(toolKind, false);
                    }
                    if (set.left != null) {
                        profile3 = set.left.toTool();
                        if (groupDisableSprintLateralTilt != null && set.left.disableSprintLateralTilt == null) {
                            profile3.disableSprintLateralTilt = groupDisableSprintLateralTilt;
                        }
                        if (groupKeepArmTuningLeft != null && set.left.keepArmTuning == null) {
                            profile3.keepArmTuning = groupKeepArmTuningLeft;
                        }
                        if (groupKeepMainArmTuning != null && set.left.keepTriggerArmTuning == null) {
                            profile3.keepTriggerArmTuning = groupKeepMainArmTuning;
                        }
                        if (groupPhysics != null && (set.left.physics == null || set.left.physics.isBlank())) {
                            profile3.physicsMode = groupPhysics;
                        }
                        if (groupSettings != null && groupPhysics != null && profile3.physicsMode == groupPhysics) {
                            profile3.physicsSettings = groupSettings.copy();
                        }
                        ToolTuning.apply(toolKind, true, profile3);
                        ToolTuning.markKindDetached(toolKind, true);
                    }
                }
            }
            if (data.emptyHand.armTransforms != null) {
                SideSet sideSet2 = data.emptyHand.armTransforms;
                PlayerArmModelLayer.ToolKind armKind2 = PlayerArmModelLayer.ToolKind.EMPTY_HAND;
                if (sideSet2.right != null) {
                    ArmToolTuning.apply(armKind2, false, PunchyTuningConfig.deserializeKindArmProfileForRuntime(false, sideSet2.right.toArm()));
                    ArmToolTuning.markKindDetached(armKind2, false);
                }
                if (sideSet2.left != null) {
                    ArmToolTuning.apply(armKind2, true, PunchyTuningConfig.deserializeKindArmProfileForRuntime(true, sideSet2.left.toArm()));
                    ArmToolTuning.markKindDetached(armKind2, true);
                }
            }
        }
        if ((sideSet = PunchyTuningConfig.resolveGlobalArm(data)) != null) {
            if (sideSet.right != null) {
                ArmToolTuning.applyGlobal(false, sideSet.right.toArm());
            }
            if (sideSet.left != null) {
                ArmToolTuning.applyGlobal(true, sideSet.left.toArm());
            }
            ArmMeshTuning.resetGlobal(false);
            ArmMeshTuning.resetGlobal(true);
        }
        if (data.armMesh != null && (meshSet = data.armMesh.get(ARM_MESH_SIDE_KEY)) != null) {
            if (meshSet.right != null) {
                ArmMeshTuning.applyGlobal(false, meshSet.right.toArmMesh());
            }
            if (meshSet.left != null) {
                ArmMeshTuning.applyGlobal(true, meshSet.left.toArmMesh());
            }
        }
        if (data.chestProxy != null) {
            PunchyTuningConfig.applyChestProxy(data.chestProxy);
        }
        if (data.fluidProxy != null) {
            PunchyTuningConfig.applyFluidProxy(data.fluidProxy);
        }
        if (data.enchantParticleProxy != null) {
            PunchyTuningConfig.applyEnchantParticleProxy(data.enchantParticleProxy);
        }
        if (data.paddleProxy != null) {
            PunchyTuningConfig.applyPaddleProxy(data.paddleProxy);
        }
        if (data.fishingLineProxy != null) {
            PunchyTuningConfig.applyFishingLineProxy(data.fishingLineProxy);
        }
        if (data.itemSpecific != null) {
            for (Map.Entry<String, SpecificEntry> entry : data.itemSpecific.entrySet()) {
                SideSet armTransforms;
                ToolTuning.Profile profile4;
                SideSet transforms;
                class_1792 item;
                class_2960 id;
                if (entry == null || entry.getValue() == null || (id = PunchyTuningConfig.resolveSpecificItemId(entry.getKey())) == null) continue;
                VanillaFirstPersonItemLayer.ToolKind kind = null;
                if (entry.getValue().kind != null) {
                    try {
                        kind = VanillaFirstPersonItemLayer.ToolKind.valueOf(entry.getValue().kind);
                    }
                    catch (IllegalArgumentException groupDisableSprintLateralTilt) {
                        // empty catch block
                    }
                }
                if (kind == null && (item = class_7923.field_41178.method_17966(id).orElse(class_1802.field_8162)) != class_1802.field_8162) {
                    class_1799 dummyStack = new class_1799((class_1935)item);
                    kind = ToolKindResolver.resolveItemKind(dummyStack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
                }
                ToolTuning.Profile right = (transforms = entry.getValue().transforms) != null && transforms.right != null ? transforms.right.toTool() : null;
                ToolTuning.Profile left = transforms != null && transforms.left != null ? transforms.left.toTool() : null;
                Boolean specificDisableSprintLateralTilt = entry.getValue().disableSprintLateralTilt;
                Boolean specificKeepMainArmTuning = entry.getValue().keepTriggerArmTuning;
                Boolean specificKeepArmTuningRight = PunchyTuningConfig.resolveSideKeepArmTuning(entry.getValue().keepArmTuning, entry.getValue().keepArmTuningR);
                Boolean specificKeepArmTuningLeft = PunchyTuningConfig.resolveSideKeepArmTuning(entry.getValue().keepArmTuning, entry.getValue().keepArmTuningL);
                if (specificDisableSprintLateralTilt != null) {
                    ToolTuning.Profile profile42;
                    if (transforms != null) {
                        if (transforms.right != null && transforms.right.disableSprintLateralTilt == null && right != null) {
                            right.disableSprintLateralTilt = specificDisableSprintLateralTilt;
                        }
                        if (transforms.left != null && transforms.left.disableSprintLateralTilt == null && left != null) {
                            left.disableSprintLateralTilt = specificDisableSprintLateralTilt;
                        }
                    }
                    if ((transforms == null || transforms.right == null) && kind != null && (profile42 = ToolTuning.getSpecificProfile(id, kind, false)) != null) {
                        profile42.disableSprintLateralTilt = specificDisableSprintLateralTilt;
                    }
                    if ((transforms == null || transforms.left == null) && kind != null && (profile4 = ToolTuning.getSpecificProfile(id, kind, true)) != null) {
                        profile4.disableSprintLateralTilt = specificDisableSprintLateralTilt;
                    }
                }
                if (specificKeepArmTuningRight != null || specificKeepArmTuningLeft != null) {
                    if (transforms != null) {
                        if (transforms.right != null && transforms.right.keepArmTuning == null && right != null && specificKeepArmTuningRight != null) {
                            right.keepArmTuning = specificKeepArmTuningRight;
                        }
                        if (transforms.left != null && transforms.left.keepArmTuning == null && left != null && specificKeepArmTuningLeft != null) {
                            left.keepArmTuning = specificKeepArmTuningLeft;
                        }
                    }
                    if ((transforms == null || transforms.right == null) && specificKeepArmTuningRight != null && kind != null && (profile4 = ToolTuning.getSpecificProfile(id, kind, false)) != null) {
                        profile4.keepArmTuning = specificKeepArmTuningRight;
                    }
                    if ((transforms == null || transforms.left == null) && specificKeepArmTuningLeft != null && kind != null && (profile4 = ToolTuning.getSpecificProfile(id, kind, true)) != null) {
                        profile4.keepArmTuning = specificKeepArmTuningLeft;
                    }
                }
                if (specificKeepMainArmTuning != null) {
                    if (transforms != null) {
                        if (transforms.right != null && transforms.right.keepTriggerArmTuning == null && right != null) {
                            right.keepTriggerArmTuning = specificKeepMainArmTuning;
                        }
                        if (transforms.left != null && transforms.left.keepTriggerArmTuning == null && left != null) {
                            left.keepTriggerArmTuning = specificKeepMainArmTuning;
                        }
                    }
                    if ((transforms == null || transforms.right == null) && kind != null && (profile4 = ToolTuning.getSpecificProfile(id, kind, false)) != null) {
                        profile4.keepTriggerArmTuning = specificKeepMainArmTuning;
                    }
                    if ((transforms == null || transforms.left == null) && kind != null && (profile4 = ToolTuning.getSpecificProfile(id, kind, true)) != null) {
                        profile4.keepTriggerArmTuning = specificKeepMainArmTuning;
                    }
                }
                physicsEntry = PunchyTuningConfig.resolvePhysicsEntry(entry.getValue().physics);
                ToolTuning.PhysicsMode specificPhysics = PunchyTuningConfig.resolvePhysicsMode(physicsEntry);
                ToolTuning.PhysicsSettings specificSettings = PunchyTuningConfig.resolvePhysicsSettings(physicsEntry);
                if (specificPhysics != null) {
                    if (right != null && (transforms.right == null || transforms.right.physics == null)) {
                        right.physicsMode = specificPhysics;
                    }
                    if (left != null && (transforms.left == null || transforms.left.physics == null)) {
                        left.physicsMode = specificPhysics;
                    }
                    if (kind != null) {
                        if ((transforms == null || transforms.right == null) && (profile = ToolTuning.getSpecificProfile(id, kind, false)) != null) {
                            profile.physicsMode = specificPhysics;
                            if (specificSettings != null) {
                                profile.physicsSettings = specificSettings.copy();
                            }
                        }
                        if ((transforms == null || transforms.left == null) && (profile = ToolTuning.getSpecificProfile(id, kind, true)) != null) {
                            profile.physicsMode = specificPhysics;
                            if (specificSettings != null) {
                                profile.physicsSettings = specificSettings.copy();
                            }
                        }
                    }
                }
                if (specificSettings != null && specificPhysics != null) {
                    if (right != null && right.physicsMode == specificPhysics) {
                        right.physicsSettings = specificSettings.copy();
                    }
                    if (left != null && left.physicsMode == specificPhysics) {
                        left.physicsSettings = specificSettings.copy();
                    }
                }
                if (kind != null) {
                    PunchyTuningConfig.applySpecificRuntimeFlags(id, entry.getValue().disableWalkOnWater, entry.getValue().disableDryOut, entry.getValue().disableSwiming);
                    PunchyTuningConfig.applyGroupRuntimeFlags(kind, entry.getValue().disableWalkOnWater, entry.getValue().disableDryOut, entry.getValue().disableSwiming);
                    PunchyTuningConfig.applyItemLevelFishingRodFlags(entry.getValue().customAnimation, entry.getValue().disableWalkOnWater, entry.getValue().disableDryOut, entry.getValue().disableSwiming);
                    ToolTuning.applySpecific(id, entry.getValue().customModelData, kind, entry.getValue().renamed, entry.getValue().model, entry.getValue().customAnimation, entry.getValue().customItemAnimations, PunchyTuningConfig.readCompatParticleEffects(entry.getValue()), entry.getValue().betterCombat, null, entry.getValue().bonePhysics, right, left);
                }
                if ((armTransforms = entry.getValue().armTransforms) == null) continue;
                PlayerArmModelLayer.ToolKind armKind3 = null;
                if (entry.getValue().armKind != null) {
                    try {
                        armKind3 = PlayerArmModelLayer.ToolKind.valueOf(entry.getValue().armKind);
                    }
                    catch (IllegalArgumentException armLeft) {
                        // empty catch block
                    }
                }
                if (armKind3 == null && entry.getValue().kind != null) {
                    try {
                        armKind3 = PlayerArmModelLayer.ToolKind.valueOf(entry.getValue().kind);
                    }
                    catch (IllegalArgumentException ignored) {
                        armKind3 = PlayerArmModelLayer.ToolKind.AXE;
                    }
                }
                if (armKind3 == null) {
                    armKind3 = PlayerArmModelLayer.ToolKind.AXE;
                }
                ArmToolTuning.Profile armRight = armTransforms.right != null ? PunchyTuningConfig.deserializeSpecificArmProfileFromSource(armKind3, false, armTransforms.right.toArm()) : null;
                ArmToolTuning.Profile armLeft = armTransforms.left != null ? PunchyTuningConfig.deserializeSpecificArmProfileFromSource(armKind3, true, armTransforms.left.toArm()) : null;
                ArmToolTuning.applySpecific(id, armKind3, armRight, armLeft);
            }
        }
        ToolTuning.refreshAll();
    }

    private static void applyChestProxy(Map<String, Pose> proxyData) {
        if (proxyData == null) {
            return;
        }
        for (Map.Entry<String, Pose> entry : proxyData.entrySet()) {
            ChestProxyTuning.Kind kind;
            if (entry == null || entry.getKey() == null || entry.getValue() == null || (kind = PunchyTuningConfig.parseChestProxyKind(entry.getKey())) == null) continue;
            ChestProxyTuning.Profile profile = entry.getValue().toChestProxy();
            ChestProxyTuning.apply(kind, profile);
        }
    }

    private static void applyFluidProxy(Map<String, Pose> proxyData) {
        if (proxyData == null) {
            return;
        }
        for (Map.Entry<String, Pose> entry : proxyData.entrySet()) {
            FluidProxyTuning.Kind kind;
            if (entry == null || entry.getKey() == null || entry.getValue() == null || (kind = PunchyTuningConfig.parseFluidProxyKind(entry.getKey())) == null) continue;
            FluidProxyTuning.Profile profile = entry.getValue().toFluidProxy();
            FluidProxyTuning.apply(kind, profile);
        }
    }

    private static void applyEnchantParticleProxy(Map<String, Pose> proxyData) {
        if (proxyData == null) {
            return;
        }
        for (Map.Entry<String, Pose> entry : proxyData.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            EnchantParticleProxySide side = PunchyTuningConfig.parseEnchantParticleProxySide(entry.getKey());
            EnchantParticleProxyTuning.Kind kind = PunchyTuningConfig.parseEnchantParticleProxyKind(entry.getKey());
            if (kind == null) continue;
            EnchantParticleProxyTuning.Profile profile = entry.getValue().toEnchantParticleProxy();
            if (side == EnchantParticleProxySide.LEFT) {
                EnchantParticleProxyTuning.apply(kind, true, profile);
                continue;
            }
            if (side == EnchantParticleProxySide.RIGHT) {
                EnchantParticleProxyTuning.apply(kind, false, profile);
                continue;
            }
            EnchantParticleProxyTuning.apply(kind, false, profile);
            EnchantParticleProxyTuning.apply(kind, true, profile);
        }
    }

    private static void applyPaddleProxy(Map<String, Pose> proxyData) {
        if (proxyData == null) {
            return;
        }
        for (Map.Entry<String, Pose> entry : proxyData.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            PaddleProxySide side = PunchyTuningConfig.parsePaddleProxySide(entry.getKey());
            PaddleProxyTuning.Kind kind = PunchyTuningConfig.parsePaddleProxyKind(entry.getKey());
            if (kind == null) continue;
            PaddleProxyTuning.Profile profile = entry.getValue().toPaddleProxy();
            if (side == PaddleProxySide.LEFT) {
                PaddleProxyTuning.apply(kind, true, profile);
                continue;
            }
            if (side == PaddleProxySide.RIGHT) {
                PaddleProxyTuning.apply(kind, false, profile);
                continue;
            }
            PaddleProxyTuning.apply(kind, false, profile);
            PaddleProxyTuning.apply(kind, true, profile);
        }
    }

    private static void applyFishingLineProxy(Map<String, Pose> proxyData) {
        if (proxyData == null) {
            return;
        }
        for (Map.Entry<String, Pose> entry : proxyData.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            FishingLineProxySide side = PunchyTuningConfig.parseFishingLineProxySide(entry.getKey());
            FishingLineProxyTuning.Kind kind = PunchyTuningConfig.parseFishingLineProxyKind(entry.getKey());
            if (kind == null) continue;
            FishingLineProxyTuning.Profile profile = entry.getValue().toFishingLineProxy();
            if (side == FishingLineProxySide.LEFT) {
                FishingLineProxyTuning.apply(kind, true, profile);
                continue;
            }
            if (side == FishingLineProxySide.RIGHT) {
                FishingLineProxyTuning.apply(kind, false, profile);
                continue;
            }
            FishingLineProxyTuning.apply(kind, false, profile);
            FishingLineProxyTuning.apply(kind, true, profile);
        }
    }

    private static Map<String, Pose> captureChestProxy() {
        HashMap<String, Pose> out = new HashMap<String, Pose>();
        for (ChestProxyTuning.Kind kind : ChestProxyTuning.Kind.values()) {
            ChestProxyTuning.Profile profile = ChestProxyTuning.get(kind);
            if (profile == null) continue;
            out.put(kind.name().toLowerCase(Locale.ROOT), Pose.fromChestProxy(profile));
        }
        return out;
    }

    private static Map<String, Pose> captureFluidProxy() {
        HashMap<String, Pose> out = new HashMap<String, Pose>();
        for (FluidProxyTuning.Kind kind : FluidProxyTuning.Kind.values()) {
            FluidProxyTuning.Profile profile = FluidProxyTuning.get(kind);
            if (profile == null) continue;
            out.put(kind.name().toLowerCase(Locale.ROOT), Pose.fromFluidProxy(profile));
        }
        return out;
    }

    private static Map<String, Pose> captureEnchantParticleProxy() {
        HashMap<String, Pose> out = new HashMap<String, Pose>();
        for (EnchantParticleProxyTuning.Kind kind : EnchantParticleProxyTuning.Kind.values()) {
            EnchantParticleProxyTuning.Profile right = EnchantParticleProxyTuning.get(kind, false);
            EnchantParticleProxyTuning.Profile left = EnchantParticleProxyTuning.get(kind, true);
            if (right != null) {
                out.put(kind.name().toLowerCase(Locale.ROOT) + "_right", Pose.fromEnchantParticleProxy(right));
            }
            if (left == null) continue;
            out.put(kind.name().toLowerCase(Locale.ROOT) + "_left", Pose.fromEnchantParticleProxy(left));
        }
        return out;
    }

    private static Map<String, Pose> capturePaddleProxy() {
        HashMap<String, Pose> out = new HashMap<String, Pose>();
        for (PaddleProxyTuning.Kind kind : PaddleProxyTuning.Kind.values()) {
            PaddleProxyTuning.Profile right = PaddleProxyTuning.get(kind, false);
            PaddleProxyTuning.Profile left = PaddleProxyTuning.get(kind, true);
            if (right != null) {
                out.put(kind.name().toLowerCase(Locale.ROOT) + "_right", Pose.fromPaddleProxy(right));
            }
            if (left == null) continue;
            out.put(kind.name().toLowerCase(Locale.ROOT) + "_left", Pose.fromPaddleProxy(left));
        }
        return out;
    }

    private static Map<String, Pose> captureFishingLineProxy() {
        HashMap<String, Pose> out = new HashMap<String, Pose>();
        for (FishingLineProxyTuning.Kind kind : FishingLineProxyTuning.Kind.values()) {
            FishingLineProxyTuning.Profile right = FishingLineProxyTuning.get(kind, false);
            FishingLineProxyTuning.Profile left = FishingLineProxyTuning.get(kind, true);
            if (right != null) {
                out.put(kind.name().toLowerCase(Locale.ROOT) + "_right", Pose.fromFishingLineProxy(right));
            }
            if (left == null) continue;
            out.put(kind.name().toLowerCase(Locale.ROOT) + "_left", Pose.fromFishingLineProxy(left));
        }
        return out;
    }

    private static ChestProxyTuning.Kind parseChestProxyKind(String raw) {
        String key;
        if (raw == null) {
            return null;
        }
        return switch (key = raw.toLowerCase(Locale.ROOT)) {
            case "boat" -> ChestProxyTuning.Kind.BOAT;
            case "raft" -> ChestProxyTuning.Kind.RAFT;
            case "minecart" -> ChestProxyTuning.Kind.MINECART;
            default -> null;
        };
    }

    private static FluidProxyTuning.Kind parseFluidProxyKind(String raw) {
        String key;
        if (raw == null) {
            return null;
        }
        return switch (key = raw.toLowerCase(Locale.ROOT)) {
            case "water" -> FluidProxyTuning.Kind.WATER;
            case "lava" -> FluidProxyTuning.Kind.LAVA;
            case "milk" -> FluidProxyTuning.Kind.MILK;
            default -> null;
        };
    }

    private static EnchantParticleProxyTuning.Kind parseEnchantParticleProxyKind(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.endsWith("_right")) {
            key = key.substring(0, key.length() - "_right".length());
        } else if (key.endsWith("_left")) {
            key = key.substring(0, key.length() - "_left".length());
        }
        return switch (key) {
            case "book" -> EnchantParticleProxyTuning.Kind.BOOK;
            default -> null;
        };
    }

    private static PaddleProxyTuning.Kind parsePaddleProxyKind(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.endsWith("_right")) {
            key = key.substring(0, key.length() - "_right".length());
        } else if (key.endsWith("_left")) {
            key = key.substring(0, key.length() - "_left".length());
        }
        return switch (key) {
            case "boat" -> PaddleProxyTuning.Kind.BOAT;
            case "raft" -> PaddleProxyTuning.Kind.RAFT;
            default -> null;
        };
    }

    private static FishingLineProxyTuning.Kind parseFishingLineProxyKind(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.endsWith("_right")) {
            key = key.substring(0, key.length() - "_right".length());
        } else if (key.endsWith("_left")) {
            key = key.substring(0, key.length() - "_left".length());
        }
        return switch (key) {
            case "rod", "fishing_rod" -> FishingLineProxyTuning.Kind.ROD;
            default -> null;
        };
    }

    private static PaddleProxySide parsePaddleProxySide(String raw) {
        if (raw == null) {
            return PaddleProxySide.BOTH;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.endsWith("_left")) {
            return PaddleProxySide.LEFT;
        }
        if (key.endsWith("_right")) {
            return PaddleProxySide.RIGHT;
        }
        return PaddleProxySide.BOTH;
    }

    private static FishingLineProxySide parseFishingLineProxySide(String raw) {
        if (raw == null) {
            return FishingLineProxySide.BOTH;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.endsWith("_left")) {
            return FishingLineProxySide.LEFT;
        }
        if (key.endsWith("_right")) {
            return FishingLineProxySide.RIGHT;
        }
        return FishingLineProxySide.BOTH;
    }

    private static EnchantParticleProxySide parseEnchantParticleProxySide(String raw) {
        if (raw == null) {
            return EnchantParticleProxySide.BOTH;
        }
        String key = raw.toLowerCase(Locale.ROOT);
        if (key.endsWith("_left")) {
            return EnchantParticleProxySide.LEFT;
        }
        if (key.endsWith("_right")) {
            return EnchantParticleProxySide.RIGHT;
        }
        return EnchantParticleProxySide.BOTH;
    }

    public static void saveCurrent() {
        try {
            String compatTarget;
            Data data = PunchyTuningConfig.captureCurrent();
            if (INITIAL_STATE != null) {
                Data sessionDelta = PunchyTuningConfig.computeDelta(INITIAL_STATE, data);
                sessionDelta.globalItem = null;
                sessionDelta.globalArm = null;
                sessionDelta.armMesh = null;
                PunchyTuningConfig.saveHelperData(sessionDelta);
            }
            if ((compatTarget = Punchy.ACTIVE_COMPAT_EDIT_TARGET) != null && !compatTarget.isBlank()) {
                Data toSave = BASELINE_STATE != null ? PunchyTuningConfig.computeDelta(BASELINE_STATE, data) : data;
                PunchyTuningConfig.saveCompatData(compatTarget, toSave);
            } else {
                Path activeProfile = TuningProfileManager.getActiveProfileFilePath();
                if (activeProfile != null) {
                    PunchyTuningConfig.saveCurrentProfileDeltaToFile(activeProfile);
                } else {
                    Data toSave = INITIAL_STATE != null ? PunchyTuningConfig.computeDelta(INITIAL_STATE, data) : data;
                    PunchyTuningConfig.saveData(toSave);
                }
                BASELINE_STATE = data;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static boolean saveCurrentDeltaToFile(Path targetFile) {
        if (targetFile == null) {
            return false;
        }
        try {
            Data toSave;
            Data current = PunchyTuningConfig.captureCurrent();
            Data data = toSave = BASELINE_STATE != null ? PunchyTuningConfig.computeDelta(BASELINE_STATE, current) : current;
            if (toSave == null) {
                toSave = new Data();
            }
            PunchyTuningConfig.pruneEmptyCollections(toSave);
            Path parent = targetFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            try (FileWriter writer = new FileWriter(targetFile.toFile());){
                GSON.toJson((Object)toSave, (Appendable)writer);
            }
            INITIAL_STATE = PunchyTuningConfig.copyData(current);
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public static boolean saveCurrentProfileDeltaToFile(Path targetFile) {
        if (targetFile == null) {
            return false;
        }
        try {
            Data current = PunchyTuningConfig.captureCurrent();
            Data toSave = PunchyTuningConfig.buildProfileSaveData(targetFile, current);
            if (toSave == null) {
                toSave = new Data();
            }
            PunchyTuningConfig.pruneEmptyCollections(toSave);
            Path parent = targetFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            try (FileWriter writer = new FileWriter(targetFile.toFile());){
                GSON.toJson((Object)toSave, (Appendable)writer);
            }
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private static Data buildProfileSaveData(Path targetFile, Data current) {
        Data mergedProfile;
        Data sessionDelta;
        Data data = sessionDelta = INITIAL_STATE != null ? PunchyTuningConfig.computeDelta(INITIAL_STATE, current) : PunchyTuningConfig.copyData(current);
        if (sessionDelta == null) {
            sessionDelta = new Data();
        }
        if ((mergedProfile = PunchyTuningConfig.mergeData(PunchyTuningConfig.readDataFile(targetFile), PunchyTuningConfig.copyData(sessionDelta))) == null) {
            mergedProfile = new Data();
        }
        if (DEFAULTS_RESOURCE_STATE == null) {
            PunchyTuningConfig.pruneEmptyCollections(mergedProfile);
            return mergedProfile;
        }
        Data effective = PunchyTuningConfig.mergeData(PunchyTuningConfig.copyData(DEFAULTS_RESOURCE_STATE), PunchyTuningConfig.copyData(mergedProfile));
        Data canonical = PunchyTuningConfig.computeDelta(DEFAULTS_RESOURCE_STATE, effective);
        if (canonical == null) {
            canonical = new Data();
        }
        if (mergedProfile.itemSpecific != null && !mergedProfile.itemSpecific.isEmpty()) {
            canonical.itemSpecific = PunchyTuningConfig.copySpecificMapForProfile(mergedProfile.itemSpecific);
        }
        PunchyTuningConfig.pruneEmptyCollections(canonical);
        return canonical;
    }

    private static Map<String, SpecificEntry> copySpecificMapForProfile(Map<String, SpecificEntry> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        HashMap<String, SpecificEntry> copy = new HashMap<String, SpecificEntry>();
        for (Map.Entry<String, SpecificEntry> entry : source.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            copy.put(entry.getKey(), PunchyTuningConfig.copySpecificEntryForProfile(entry.getValue()));
        }
        return copy.isEmpty() ? null : copy;
    }

    private static Data readDataFile(Path file) {
        Data data;
        if (file == null || !Files.isRegularFile(file, new LinkOption[0])) {
            return null;
        }
        FileReader reader = new FileReader(file.toFile());
        try {
            data = (Data)GSON.fromJson((Reader)reader, Data.class);
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception ignored) {
                return null;
            }
        }
        reader.close();
        return data;
    }

    private static void applyMixSelectionsFromActiveProfile(JsonObject activeProfileJson) {
    }

    private static void persistMixSelectionsToActiveProfile() {
    }

    private static JsonObject readJsonObjectFile(Path file) {
        JsonObject jsonObject;
        if (file == null || !Files.isRegularFile(file, new LinkOption[0])) {
            return null;
        }
        FileReader reader = new FileReader(file.toFile());
        try {
            JsonElement element = (JsonElement)GSON.fromJson((Reader)reader, JsonElement.class);
            jsonObject = element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception ignored) {
                return null;
            }
        }
        reader.close();
        return jsonObject;
    }

    private static void injectMixSelectionsIntoProfileJson(JsonObject root, PunchyConfig.Data snapshot) {
        if (root == null) {
            return;
        }
        if (snapshot == null) {
            root.remove("animationPackSelections");
            root.remove("animationPackTypeSelections");
            root.remove("mixSpecificForceOverride");
            return;
        }
        PunchyTuningConfig.writeStringMap(root, "animationPackSelections", snapshot.animationPackSelections);
        PunchyTuningConfig.writeStringMap(root, "animationPackTypeSelections", snapshot.animationPackTypeSelections);
        PunchyTuningConfig.writeBooleanMap(root, "mixSpecificForceOverride", snapshot.mixSpecificForceOverride);
    }

    private static LinkedHashMap<String, String> readStringMap(JsonObject root, String key) {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        if (root == null || key == null || !root.has(key) || !root.get(key).isJsonObject()) {
            return result;
        }
        JsonObject obj = root.getAsJsonObject(key);
        for (Map.Entry entry : obj.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || ((JsonElement)entry.getValue()).isJsonNull()) continue;
            try {
                String value = ((JsonElement)entry.getValue()).getAsString();
                if (value == null || value.isBlank()) continue;
                result.put((String)entry.getKey(), value);
            }
            catch (Exception exception) {}
        }
        return result;
    }

    private static LinkedHashMap<String, Boolean> readBooleanMap(JsonObject root, String key) {
        LinkedHashMap<String, Boolean> result = new LinkedHashMap<String, Boolean>();
        if (root == null || key == null || !root.has(key) || !root.get(key).isJsonObject()) {
            return result;
        }
        JsonObject obj = root.getAsJsonObject(key);
        for (Map.Entry entry : obj.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || ((JsonElement)entry.getValue()).isJsonNull()) continue;
            try {
                result.put((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsBoolean());
            }
            catch (Exception exception) {}
        }
        return result;
    }

    private static void writeStringMap(JsonObject root, String key, Map<String, String> values) {
        if (root == null || key == null) {
            return;
        }
        if (values == null || values.isEmpty()) {
            root.remove(key);
            return;
        }
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || entry.getValue().isBlank()) continue;
            obj.addProperty(entry.getKey(), entry.getValue());
        }
        if (obj.size() == 0) {
            root.remove(key);
        } else {
            root.add(key, (JsonElement)obj);
        }
    }

    private static void writeBooleanMap(JsonObject root, String key, Map<String, Boolean> values) {
        if (root == null || key == null) {
            return;
        }
        if (values == null || values.isEmpty()) {
            root.remove(key);
            return;
        }
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            obj.addProperty(entry.getKey(), entry.getValue());
        }
        if (obj.size() == 0) {
            root.remove(key);
        } else {
            root.add(key, (JsonElement)obj);
        }
    }

    public static boolean exportTuningHelperDevAndOpen() {
        try {
            Path exportDir = PunchyTuningConfig.getConfigDirectory().resolve("punchy").resolve("dev_tuning_helper");
            Files.createDirectories(exportDir, new FileAttribute[0]);
            Path target = exportDir.resolve("tuning_helper_dev.json");
            if (!PunchyTuningConfig.saveCurrentDeltaToFile(target)) {
                return false;
            }
            class_156.method_668().method_60932(exportDir);
            return true;
        }
        catch (Exception ignored) {
            try {
                Path exportDir = PunchyTuningConfig.getConfigDirectory().resolve("punchy").resolve("dev_tuning_helper");
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(exportDir.toFile());
                    return true;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return false;
        }
    }

    private static void saveHelperData(Data data) {
        Path configDir = PunchyTuningConfig.getConfigDirectory();
        try {
            Files.createDirectories(configDir.resolve("punchy"), new FileAttribute[0]);
            File file = configDir.resolve("punchy").resolve("creator_tuning_helper.json").toFile();
            try (FileWriter writer = new FileWriter(file);){
                GSON.toJson((Object)data, (Appendable)writer);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static Data captureCurrent() {
        Data d = new Data();
        for (VanillaFirstPersonItemLayer.ToolKind kind : VanillaFirstPersonItemLayer.ToolKind.values()) {
            GroupEntry entry = new GroupEntry();
            boolean hasData = false;
            SideSet ss = null;
            if (!ToolTuning.isKindAttached(kind, false)) {
                ss = new SideSet();
                ss.right = Pose.fromTool(ToolTuning.snapshot(kind, false));
            }
            if (!ToolTuning.isKindAttached(kind, true)) {
                if (ss == null) {
                    ss = new SideSet();
                }
                ss.left = Pose.fromTool(ToolTuning.snapshot(kind, true));
            }
            if (ss != null) {
                entry.transforms = ss;
                hasData = true;
            }
            PlayerArmModelLayer.ToolKind armKind = null;
            try {
                armKind = PlayerArmModelLayer.ToolKind.valueOf(kind.name());
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            if (armKind != null) {
                ArmToolTuning.Profile leftProfile;
                ArmToolTuning.Profile rightProfile;
                SideSet armSS = null;
                if (!ArmToolTuning.isKindAttached(armKind, false) && !PunchyTuningConfig.isImplicitEmptyHandArmProfile(armKind, false, rightProfile = ArmToolTuning.snapshot(armKind, false))) {
                    armSS = new SideSet();
                    armSS.right = Pose.fromArm(PunchyTuningConfig.normalizeKindArmProfileForSerialization(armKind, false, rightProfile));
                }
                if (!ArmToolTuning.isKindAttached(armKind, true) && !PunchyTuningConfig.isImplicitEmptyHandArmProfile(armKind, true, leftProfile = ArmToolTuning.snapshot(armKind, true))) {
                    if (armSS == null) {
                        armSS = new SideSet();
                    }
                    armSS.left = Pose.fromArm(PunchyTuningConfig.normalizeKindArmProfileForSerialization(armKind, true, leftProfile));
                }
                if (armSS != null) {
                    entry.armTransforms = armSS;
                    hasData = true;
                }
            }
            if (!hasData) continue;
            d.item.put(kind.name(), entry);
        }
        SideSet globalItem = new SideSet();
        globalItem.right = Pose.fromTool(ToolTuning.snapshotGlobal(false));
        globalItem.left = Pose.fromTool(ToolTuning.snapshotGlobal(true));
        d.globalItem = globalItem;
        SideSet globalArm = new SideSet();
        globalArm.right = Pose.fromArm(ArmToolTuning.snapshotGlobal(false));
        globalArm.left = Pose.fromArm(ArmToolTuning.snapshotGlobal(true));
        d.globalArm = globalArm;
        SideSet meshSet = new SideSet();
        meshSet.right = Pose.fromArmMesh(ArmMeshTuning.snapshotGlobal(false));
        meshSet.left = Pose.fromArmMesh(ArmMeshTuning.snapshotGlobal(true));
        d.armMesh.put(ARM_MESH_SIDE_KEY, meshSet);
        d.itemSpecific = PunchyTuningConfig.captureSpecificCurrent();
        d.chestProxy = PunchyTuningConfig.captureChestProxy();
        d.fluidProxy = PunchyTuningConfig.captureFluidProxy();
        d.enchantParticleProxy = PunchyTuningConfig.captureEnchantParticleProxy();
        d.paddleProxy = PunchyTuningConfig.capturePaddleProxy();
        d.fishingLineProxy = PunchyTuningConfig.captureFishingLineProxy();
        return d;
    }

    private static void rememberBaseline() {
        BASELINE_STATE = PunchyTuningConfig.captureCurrent();
    }

    private static boolean isImplicitEmptyHandArmProfile(PlayerArmModelLayer.ToolKind kind, boolean left, ArmToolTuning.Profile current) {
        if (kind != PlayerArmModelLayer.ToolKind.EMPTY_HAND || current == null) {
            return false;
        }
        ArmToolTuning.Profile defaultEmpty = PunchyTuningConfig.getDefaultArmEditorBase(kind, left);
        ArmToolTuning.Profile defaultGlobal = PunchyTuningConfig.getDefaultGlobalArmBase(left);
        ArmToolTuning.Profile currentGlobal = ArmToolTuning.snapshotGlobal(left);
        if (defaultEmpty == null || defaultGlobal == null || currentGlobal == null) {
            return false;
        }
        return PunchyTuningConfig.nearlyEqual(current.scale, currentGlobal.scale + (defaultEmpty.scale - defaultGlobal.scale)) && PunchyTuningConfig.nearlyEqual(current.offX, currentGlobal.offX + (defaultEmpty.offX - defaultGlobal.offX)) && PunchyTuningConfig.nearlyEqual(current.offY, currentGlobal.offY + (defaultEmpty.offY - defaultGlobal.offY)) && PunchyTuningConfig.nearlyEqual(current.offZ, currentGlobal.offZ + (defaultEmpty.offZ - defaultGlobal.offZ)) && PunchyTuningConfig.nearlyEqual(current.rotX, defaultEmpty.rotX) && PunchyTuningConfig.nearlyEqual(current.rotY, defaultEmpty.rotY) && PunchyTuningConfig.nearlyEqual(current.rotZ, defaultEmpty.rotZ);
    }

    public static ArmToolTuning.Profile getDefaultArmEditorBase(PlayerArmModelLayer.ToolKind kind, boolean left) {
        Pose pose;
        GroupEntry group;
        Data defaults = DEFAULTS_RESOURCE_STATE;
        if (defaults == null || kind == null) {
            return new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        SideSet direct = null;
        if (kind == PlayerArmModelLayer.ToolKind.EMPTY_HAND && defaults.emptyHand != null) {
            direct = defaults.emptyHand.armTransforms;
        }
        if (direct == null && defaults.item != null && (group = defaults.item.get(kind.name())) != null) {
            direct = group.armTransforms;
        }
        Pose pose2 = direct != null ? (left ? direct.left : direct.right) : (pose = null);
        if (pose != null) {
            return pose.toArm();
        }
        SideSet global = defaults.globalArm;
        Pose globalPose = global != null ? (left ? global.left : global.right) : null;
        return globalPose != null ? globalPose.toArm() : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public static ArmToolTuning.Profile getDefaultGlobalArmBase(boolean left) {
        Data defaults = DEFAULTS_RESOURCE_STATE;
        if (defaults == null || defaults.globalArm == null) {
            return new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        Pose pose = left ? defaults.globalArm.left : defaults.globalArm.right;
        return pose != null ? pose.toArm() : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    private static Map<String, SpecificEntry> captureSpecificCurrent() {
        class_2960 id;
        HashMap<String, SpecificEntry> out = new HashMap<String, SpecificEntry>();
        List<ToolTuning.SpecificSnapshotDetailed> itemSnapshotDetailed = ToolTuning.snapshotSpecificsDetailed();
        Map<class_2960, ArmToolTuning.SpecificSnapshot> armSnapshot = ArmToolTuning.snapshotSpecifics();
        HashSet<class_2960> capturedItemIds = new HashSet<class_2960>();
        for (ToolTuning.SpecificSnapshotDetailed specificSnapshotDetailed : itemSnapshotDetailed) {
            String key;
            ArmToolTuning.SpecificSnapshot armSpec;
            PlayerArmModelLayer.ToolKind resolvedArmKind;
            VanillaFirstPersonItemLayer.ToolKind resolvedItemKind;
            if (specificSnapshotDetailed == null || specificSnapshotDetailed.itemId() == null) continue;
            id = specificSnapshotDetailed.itemId();
            SpecificEntry entry = new SpecificEntry();
            VanillaFirstPersonItemLayer.ToolKind toolKind = resolvedItemKind = specificSnapshotDetailed != null && specificSnapshotDetailed.kind() != null ? specificSnapshotDetailed.kind() : PunchyTuningConfig.resolveCurrentItemKind(id);
            if (resolvedItemKind != null) {
                entry.kind = resolvedItemKind.name();
            }
            ToolTuning.Profile itemRight = PunchyTuningConfig.resolveEffectiveSpecificItemProfile(specificSnapshotDetailed.right(), resolvedItemKind, false);
            ToolTuning.Profile itemLeft = PunchyTuningConfig.resolveEffectiveSpecificItemProfile(specificSnapshotDetailed.left(), resolvedItemKind, true);
            entry.transforms = PunchyTuningConfig.captureSpecificItemTransforms(itemRight, itemLeft);
            PunchyTuningConfig.applySpecificToolMetadata(entry, entry.transforms, itemRight, itemLeft);
            entry.customModelData = specificSnapshotDetailed.customModelData();
            entry.renamed = specificSnapshotDetailed.renamed();
            if (specificSnapshotDetailed.model() != null && !specificSnapshotDetailed.model().isBlank()) {
                entry.model = specificSnapshotDetailed.model();
            }
            PlayerArmModelLayer.ToolKind toolKind2 = resolvedArmKind = (armSpec = armSnapshot.get(id)) != null && armSpec.kind() != null ? armSpec.kind() : PunchyTuningConfig.resolveCurrentArmKind(id);
            if (resolvedArmKind != null) {
                entry.armKind = resolvedArmKind.name();
            }
            ArmToolTuning.Profile armRight = PunchyTuningConfig.resolveEffectiveSpecificArmProfile(armSpec != null ? armSpec.right() : null, resolvedArmKind, false);
            ArmToolTuning.Profile armLeft = PunchyTuningConfig.resolveEffectiveSpecificArmProfile(armSpec != null ? armSpec.left() : null, resolvedArmKind, true);
            entry.armTransforms = PunchyTuningConfig.captureSpecificArmTransforms(resolvedArmKind, armRight, armLeft);
            if (entry.transforms == null && entry.armTransforms == null && entry.kind == null && entry.armKind == null || (key = PunchyTuningConfig.specificStorageKey(id, entry.customModelData, entry.renamed)) == null || key.isBlank()) continue;
            out.put(key, entry);
            capturedItemIds.add(id);
        }
        for (Map.Entry entry : armSnapshot.entrySet()) {
            PlayerArmModelLayer.ToolKind resolvedArmKind;
            id = (class_2960)entry.getKey();
            ArmToolTuning.SpecificSnapshot armSpec = (ArmToolTuning.SpecificSnapshot)entry.getValue();
            if (id == null || armSpec == null || capturedItemIds.contains(id)) continue;
            String armKey = id.toString();
            SpecificEntry entry2 = out.getOrDefault(armKey, new SpecificEntry());
            PlayerArmModelLayer.ToolKind toolKind = resolvedArmKind = armSpec.kind() != null ? armSpec.kind() : PunchyTuningConfig.resolveCurrentArmKind(id);
            if (resolvedArmKind != null) {
                entry2.armKind = resolvedArmKind.name();
            }
            ArmToolTuning.Profile armRight = PunchyTuningConfig.resolveEffectiveSpecificArmProfile(armSpec.right(), resolvedArmKind, false);
            ArmToolTuning.Profile armLeft = PunchyTuningConfig.resolveEffectiveSpecificArmProfile(armSpec.left(), resolvedArmKind, true);
            entry2.armTransforms = PunchyTuningConfig.captureSpecificArmTransforms(resolvedArmKind, armRight, armLeft);
            if (entry2.transforms == null && entry2.armTransforms == null && entry2.kind == null && entry2.armKind == null) continue;
            out.put(armKey, entry2);
        }
        return out;
    }

    private static String specificStorageKey(class_2960 itemId, Integer customModelData, String renamed) {
        if (itemId == null) {
            return null;
        }
        StringBuilder key = new StringBuilder(itemId.toString());
        if (customModelData != null) {
            key.append('#').append(customModelData);
        }
        if (renamed != null && !renamed.isBlank()) {
            key.append('|').append(renamed);
        }
        return key.toString();
    }

    private static VanillaFirstPersonItemLayer.ToolKind resolveCurrentItemKind(class_2960 id) {
        if (id == null) {
            return null;
        }
        class_1792 item = class_7923.field_41178.method_17966(id).orElse(class_1802.field_8162);
        if (item == class_1802.field_8162) {
            return null;
        }
        class_1799 stack = new class_1799((class_1935)item);
        return ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
    }

    private static PlayerArmModelLayer.ToolKind resolveCurrentArmKind(class_2960 id) {
        if (id == null) {
            return null;
        }
        class_1792 item = class_7923.field_41178.method_17966(id).orElse(class_1802.field_8162);
        if (item == class_1802.field_8162) {
            return null;
        }
        class_1799 stack = new class_1799((class_1935)item);
        return ToolKindResolver.resolveArmKind(stack, PlayerArmModelLayer.ToolKind.AXE);
    }

    private static SideSet captureCurrentItemKindTransforms(VanillaFirstPersonItemLayer.ToolKind kind) {
        if (kind == null) {
            return null;
        }
        SideSet set = new SideSet();
        if (PunchyTuningConfig.shouldCaptureCurrentItemKindSide(kind, false)) {
            set.right = Pose.fromTool(ToolTuning.snapshot(kind, false));
        }
        if (PunchyTuningConfig.shouldCaptureCurrentItemKindSide(kind, true)) {
            set.left = Pose.fromTool(ToolTuning.snapshot(kind, true));
        }
        return set.right == null && set.left == null ? null : set;
    }

    private static SideSet captureCurrentArmKindTransforms(PlayerArmModelLayer.ToolKind kind) {
        if (kind == null) {
            return null;
        }
        SideSet set = new SideSet();
        if (!ArmToolTuning.isKindAttached(kind, false)) {
            set.right = Pose.fromArm(ArmToolTuning.snapshot(kind, false));
        }
        if (!ArmToolTuning.isKindAttached(kind, true)) {
            set.left = Pose.fromArm(ArmToolTuning.snapshot(kind, true));
        }
        return set.right == null && set.left == null ? null : set;
    }

    private static boolean shouldCaptureCurrentItemKindSide(VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        if (kind == null) {
            return false;
        }
        if (!ToolTuning.isKindAttached(kind, left)) {
            return true;
        }
        return PunchyTuningConfig.hasSerializableToolMetadata(ToolTuning.get(kind, left));
    }

    private static boolean hasSerializableToolMetadata(ToolTuning.Profile profile) {
        if (profile == null) {
            return false;
        }
        return profile.disableSprintLateralTilt != null || profile.keepArmTuning != null || profile.keepTriggerArmTuning != null || profile.physicsMode != null && profile.physicsMode != ToolTuning.PhysicsMode.NONE || profile.physicsSettings != null;
    }

    private static ToolTuning.Profile resolveEffectiveSpecificItemProfile(ToolTuning.Profile specific, VanillaFirstPersonItemLayer.ToolKind kind, boolean left) {
        ToolTuning.Profile fallback = null;
        if (kind != null && PunchyTuningConfig.shouldCaptureCurrentItemKindSide(kind, left)) {
            fallback = ToolTuning.snapshot(kind, left);
        }
        if (specific == null) {
            return fallback;
        }
        ToolTuning.Profile merged = PunchyTuningConfig.copyToolProfile(specific);
        if (fallback == null) {
            return merged;
        }
        if (merged.disableSprintLateralTilt == null) {
            merged.disableSprintLateralTilt = fallback.disableSprintLateralTilt;
        }
        if (merged.keepArmTuning == null) {
            merged.keepArmTuning = fallback.keepArmTuning;
        }
        if (merged.keepTriggerArmTuning == null) {
            merged.keepTriggerArmTuning = fallback.keepTriggerArmTuning;
        }
        return merged;
    }

    private static ArmToolTuning.Profile resolveEffectiveSpecificArmProfile(ArmToolTuning.Profile specific, PlayerArmModelLayer.ToolKind kind, boolean left) {
        ArmToolTuning.Profile fallback;
        ArmToolTuning.Profile profile = fallback = kind != null ? ArmToolTuning.snapshot(kind, left) : null;
        if (specific == null) {
            return fallback;
        }
        return specific;
    }

    private static SideSet captureSpecificItemTransforms(ToolTuning.Profile right, ToolTuning.Profile left) {
        if (right == null && left == null) {
            return null;
        }
        SideSet set = new SideSet();
        if (right != null) {
            set.right = Pose.fromTool(right);
        }
        if (left != null) {
            set.left = Pose.fromTool(left);
        }
        return set;
    }

    private static SideSet captureSpecificArmTransforms(PlayerArmModelLayer.ToolKind kind, ArmToolTuning.Profile right, ArmToolTuning.Profile left) {
        if (right == null && left == null) {
            return null;
        }
        SideSet set = new SideSet();
        if (right != null) {
            set.right = Pose.fromArm(PunchyTuningConfig.normalizeSpecificArmProfileForSerialization(kind, false, right));
        }
        if (left != null) {
            set.left = Pose.fromArm(PunchyTuningConfig.normalizeSpecificArmProfileForSerialization(kind, true, left));
        }
        return set;
    }

    private static ArmToolTuning.Profile normalizeKindArmProfileForSerialization(PlayerArmModelLayer.ToolKind kind, boolean left, ArmToolTuning.Profile current) {
        return PunchyTuningConfig.rebaseArmProfileWithGlobalOffsetsOnly(current, ArmToolTuning.getGlobal(left), PunchyTuningConfig.getDefaultGlobalArmBase(left));
    }

    private static ArmToolTuning.Profile normalizeSpecificArmProfileForSerialization(PlayerArmModelLayer.ToolKind kind, boolean left, ArmToolTuning.Profile current) {
        return PunchyTuningConfig.stripGlobalOffsetsFromSpecificArmProfile(current, ArmToolTuning.getGlobal(left));
    }

    private static ArmToolTuning.Profile deserializeKindArmProfileForRuntime(boolean left, ArmToolTuning.Profile stored) {
        return PunchyTuningConfig.rebaseArmProfileWithGlobalOffsetsOnly(stored, PunchyTuningConfig.getDefaultGlobalArmBase(left), ArmToolTuning.getGlobal(left));
    }

    private static ArmToolTuning.Profile deserializeSpecificArmProfileForRuntime(PlayerArmModelLayer.ToolKind kind, boolean left, ArmToolTuning.Profile stored) {
        return PunchyTuningConfig.applyGlobalOffsetsToSpecificArmProfile(stored, ArmToolTuning.getGlobal(left));
    }

    private static ArmToolTuning.Profile deserializeSpecificArmProfileFromSource(PlayerArmModelLayer.ToolKind kind, boolean left, ArmToolTuning.Profile stored) {
        if (stored == null) {
            return null;
        }
        return PunchyTuningConfig.deserializeSpecificArmProfileForRuntime(kind, left, stored);
    }

    private static ArmToolTuning.Profile rebaseArmProfileWithGlobalOffsetsOnly(ArmToolTuning.Profile current, ArmToolTuning.Profile currentBase, ArmToolTuning.Profile targetBase) {
        if (current == null) {
            return null;
        }
        ArmToolTuning.Profile sourceBase = currentBase != null ? currentBase : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        ArmToolTuning.Profile destBase = targetBase != null ? targetBase : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        return new ArmToolTuning.Profile(destBase.scale + (current.scale - sourceBase.scale), destBase.offX + (current.offX - sourceBase.offX), destBase.offY + (current.offY - sourceBase.offY), destBase.offZ + (current.offZ - sourceBase.offZ), current.rotX, current.rotY, current.rotZ);
    }

    private static ArmToolTuning.Profile stripGlobalOffsetsFromSpecificArmProfile(ArmToolTuning.Profile current, ArmToolTuning.Profile globalBase) {
        if (current == null) {
            return null;
        }
        ArmToolTuning.Profile global = globalBase != null ? globalBase : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        return new ArmToolTuning.Profile(1.0f + (current.scale - global.scale), current.offX - global.offX, current.offY - global.offY, current.offZ - global.offZ, current.rotX, current.rotY, current.rotZ);
    }

    private static ArmToolTuning.Profile applyGlobalOffsetsToSpecificArmProfile(ArmToolTuning.Profile stored, ArmToolTuning.Profile globalBase) {
        if (stored == null) {
            return null;
        }
        ArmToolTuning.Profile global = globalBase != null ? globalBase : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        return new ArmToolTuning.Profile(global.scale + (stored.scale - 1.0f), global.offX + stored.offX, global.offY + stored.offY, global.offZ + stored.offZ, stored.rotX, stored.rotY, stored.rotZ);
    }

    private static ArmToolTuning.Profile rebaseArmProfile(ArmToolTuning.Profile current, ArmToolTuning.Profile currentBase, ArmToolTuning.Profile targetBase) {
        if (current == null) {
            return null;
        }
        ArmToolTuning.Profile sourceBase = currentBase != null ? currentBase : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        ArmToolTuning.Profile destBase = targetBase != null ? targetBase : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        return new ArmToolTuning.Profile(destBase.scale + (current.scale - sourceBase.scale), destBase.offX + (current.offX - sourceBase.offX), destBase.offY + (current.offY - sourceBase.offY), destBase.offZ + (current.offZ - sourceBase.offZ), destBase.rotX + (current.rotX - sourceBase.rotX), destBase.rotY + (current.rotY - sourceBase.rotY), destBase.rotZ + (current.rotZ - sourceBase.rotZ));
    }

    private static void applySpecificToolMetadata(SpecificEntry entry, SideSet transforms, ToolTuning.Profile right, ToolTuning.Profile left) {
        JsonElement physics;
        Boolean leftTrigger;
        Boolean leftKeep;
        Boolean leftDisable;
        if (entry == null) {
            return;
        }
        Boolean rightDisable = right != null ? right.disableSprintLateralTilt : null;
        Boolean bl = leftDisable = left != null ? left.disableSprintLateralTilt : null;
        if (right != null && left != null && Objects.equals(rightDisable, leftDisable) && rightDisable != null) {
            entry.disableSprintLateralTilt = rightDisable;
            PunchyTuningConfig.clearPoseDisableSprint(transforms);
        }
        Boolean rightKeep = right != null ? right.keepArmTuning : null;
        Boolean bl2 = leftKeep = left != null ? left.keepArmTuning : null;
        if (right != null && left != null) {
            if (Objects.equals(rightKeep, leftKeep) && rightKeep != null) {
                entry.keepArmTuning = rightKeep;
                PunchyTuningConfig.clearPoseKeepArmTuning(transforms);
            } else {
                if (rightKeep != null) {
                    entry.keepArmTuningR = rightKeep;
                    if (transforms != null && transforms.right != null) {
                        transforms.right.keepArmTuning = null;
                    }
                }
                if (leftKeep != null) {
                    entry.keepArmTuningL = leftKeep;
                    if (transforms != null && transforms.left != null) {
                        transforms.left.keepArmTuning = null;
                    }
                }
            }
        }
        Boolean rightTrigger = right != null ? right.keepTriggerArmTuning : null;
        Boolean bl3 = leftTrigger = left != null ? left.keepTriggerArmTuning : null;
        if (right != null && left != null && Objects.equals(rightTrigger, leftTrigger) && rightTrigger != null) {
            entry.keepTriggerArmTuning = rightTrigger;
            PunchyTuningConfig.clearPoseKeepTriggerArmTuning(transforms);
        }
        if ((physics = PunchyTuningConfig.buildSharedPhysicsDefinition(right, left)) != null) {
            entry.physics = physics;
        }
    }

    private static void clearPoseDisableSprint(SideSet transforms) {
        if (transforms == null) {
            return;
        }
        if (transforms.right != null) {
            transforms.right.disableSprintLateralTilt = null;
        }
        if (transforms.left != null) {
            transforms.left.disableSprintLateralTilt = null;
        }
    }

    private static void clearPoseKeepArmTuning(SideSet transforms) {
        if (transforms == null) {
            return;
        }
        if (transforms.right != null) {
            transforms.right.keepArmTuning = null;
        }
        if (transforms.left != null) {
            transforms.left.keepArmTuning = null;
        }
    }

    private static void clearPoseKeepTriggerArmTuning(SideSet transforms) {
        if (transforms == null) {
            return;
        }
        if (transforms.right != null) {
            transforms.right.keepTriggerArmTuning = null;
        }
        if (transforms.left != null) {
            transforms.left.keepTriggerArmTuning = null;
        }
    }

    private static JsonElement buildSharedPhysicsDefinition(ToolTuning.Profile right, ToolTuning.Profile left) {
        ToolTuning.PhysicsSettings settings;
        boolean hasMode;
        ToolTuning.Profile source;
        ToolTuning.Profile profile = source = right != null ? right : left;
        if (source == null) {
            return null;
        }
        if (right != null && left != null && !PunchyTuningConfig.samePhysicsDefinition(right, left)) {
            return null;
        }
        boolean bl = hasMode = source.physicsMode != null && source.physicsMode != ToolTuning.PhysicsMode.NONE;
        if (!hasMode && source.physicsSettings == null) {
            return null;
        }
        JsonObject obj = new JsonObject();
        if (hasMode) {
            obj.addProperty("name", source.physicsMode.name());
        }
        if ((settings = source.physicsSettings) != null) {
            if (settings.limitDeg != null) {
                obj.addProperty("limitDeg", (Number)settings.limitDeg);
            }
            if (settings.limitDegUp != null) {
                obj.addProperty("limitDegUp", (Number)settings.limitDegUp);
            }
            if (settings.limitDegDown != null) {
                obj.addProperty("limitDegDown", (Number)settings.limitDegDown);
            }
            if (settings.force != null) {
                obj.addProperty("force", (Number)settings.force);
            }
            if (settings.speed != null) {
                obj.addProperty("speed", (Number)settings.speed);
            }
            if (settings.cameraSens != null) {
                obj.addProperty("cameraSens", (Number)settings.cameraSens);
            }
            if (settings.gravity != null) {
                obj.addProperty("gravity", (Number)settings.gravity);
            }
            if (settings.motionSens != null) {
                obj.addProperty("motionSens", (Number)settings.motionSens);
            }
            if (settings.invert != null) {
                obj.addProperty("invert", settings.invert);
            }
            if (settings.handSpace != null) {
                obj.addProperty("handSpace", settings.handSpace);
            }
            if (settings.enableCreaturePhysics != null) {
                obj.addProperty("enableCreaturePhysics", settings.enableCreaturePhysics);
            }
        }
        if (obj.entrySet().isEmpty()) {
            return null;
        }
        JsonArray array = new JsonArray();
        array.add((JsonElement)obj);
        return array;
    }

    private static boolean samePhysicsDefinition(ToolTuning.Profile a, ToolTuning.Profile b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.physicsMode != b.physicsMode) {
            return false;
        }
        ToolTuning.PhysicsSettings as = a.physicsSettings;
        ToolTuning.PhysicsSettings bs = b.physicsSettings;
        if (as == bs) {
            return true;
        }
        if (as == null || bs == null) {
            return false;
        }
        return Objects.equals(as.limitDeg, bs.limitDeg) && Objects.equals(as.limitDegUp, bs.limitDegUp) && Objects.equals(as.limitDegDown, bs.limitDegDown) && Objects.equals(as.force, bs.force) && Objects.equals(as.speed, bs.speed) && Objects.equals(as.cameraSens, bs.cameraSens) && Objects.equals(as.gravity, bs.gravity) && Objects.equals(as.motionSens, bs.motionSens) && Objects.equals(as.invert, bs.invert) && Objects.equals(as.handSpace, bs.handSpace) && Objects.equals(as.enableCreaturePhysics, bs.enableCreaturePhysics);
    }

    private static Data computeDelta(Data base, Data current) {
        SideSet baseSet;
        SideSet currentSet;
        SideSet diff;
        if (base == null) {
            return current;
        }
        Data delta = new Data();
        boolean hasData = false;
        delta.globalItem = PunchyTuningConfig.diffSideSet(current.globalItem, base.globalItem);
        if (delta.globalItem != null) {
            hasData = true;
        }
        delta.globalArm = PunchyTuningConfig.diffSideSet(current.globalArm, base.globalArm);
        if (delta.globalArm != null) {
            hasData = true;
        }
        HashMap<String, GroupEntry> itemDiff = new HashMap<String, GroupEntry>();
        if (current.item != null) {
            for (VanillaFirstPersonItemLayer.ToolKind kind : VanillaFirstPersonItemLayer.ToolKind.values()) {
                GroupEntry currentEntry = current.item.get(kind.name());
                GroupEntry baseEntry = base.item != null ? base.item.get(kind.name()) : null;
                SideSet diffTransforms = null;
                SideSet diffArmTransforms = null;
                if (currentEntry != null) {
                    diffTransforms = PunchyTuningConfig.diffSideSet(currentEntry.transforms, baseEntry != null ? baseEntry.transforms : null);
                    diffArmTransforms = PunchyTuningConfig.diffSideSet(currentEntry.armTransforms, baseEntry != null ? baseEntry.armTransforms : null);
                }
                if (diffTransforms == null && diffArmTransforms == null) continue;
                GroupEntry diffEntry = new GroupEntry();
                diffEntry.transforms = diffTransforms;
                diffEntry.armTransforms = diffArmTransforms;
                itemDiff.put(kind.name(), diffEntry);
                hasData = true;
            }
        }
        delta.item = itemDiff.isEmpty() ? null : itemDiff;
        HashMap<String, SideSet> meshDiff = new HashMap<String, SideSet>();
        if (current.armMesh != null && (diff = PunchyTuningConfig.diffSideSet(currentSet = current.armMesh.get(ARM_MESH_SIDE_KEY), baseSet = base.armMesh != null ? base.armMesh.get(ARM_MESH_SIDE_KEY) : null)) != null) {
            meshDiff.put(ARM_MESH_SIDE_KEY, diff);
            hasData = true;
        }
        delta.armMesh = meshDiff.isEmpty() ? null : meshDiff;
        Map<String, Pose> chestProxyDiff = PunchyTuningConfig.diffPoseMap(current.chestProxy, base.chestProxy);
        delta.chestProxy = chestProxyDiff == null || chestProxyDiff.isEmpty() ? null : chestProxyDiff;
        Map<String, Pose> fluidProxyDiff = PunchyTuningConfig.diffPoseMap(current.fluidProxy, base.fluidProxy);
        delta.fluidProxy = fluidProxyDiff == null || fluidProxyDiff.isEmpty() ? null : fluidProxyDiff;
        Map<String, Pose> enchantParticleProxyDiff = PunchyTuningConfig.diffPoseMap(current.enchantParticleProxy, base.enchantParticleProxy);
        delta.enchantParticleProxy = enchantParticleProxyDiff == null || enchantParticleProxyDiff.isEmpty() ? null : enchantParticleProxyDiff;
        Map<String, Pose> paddleProxyDiff = PunchyTuningConfig.diffPoseMap(current.paddleProxy, base.paddleProxy);
        delta.paddleProxy = paddleProxyDiff == null || paddleProxyDiff.isEmpty() ? null : paddleProxyDiff;
        Map<String, Pose> fishingLineProxyDiff = PunchyTuningConfig.diffPoseMap(current.fishingLineProxy, base.fishingLineProxy);
        Map<String, Pose> map = delta.fishingLineProxy = fishingLineProxyDiff == null || fishingLineProxyDiff.isEmpty() ? null : fishingLineProxyDiff;
        if (delta.chestProxy != null || delta.fluidProxy != null || delta.enchantParticleProxy != null || delta.paddleProxy != null || delta.fishingLineProxy != null) {
            hasData = true;
        }
        HashMap<String, SpecificEntry> specificDiff = new HashMap<String, SpecificEntry>();
        Map<Object, Object> currentSpecific = current.itemSpecific != null ? current.itemSpecific : Collections.emptyMap();
        Map<Object, Object> baseSpecific = base.itemSpecific != null ? base.itemSpecific : Collections.emptyMap();
        for (Map.Entry<Object, Object> entry : currentSpecific.entrySet()) {
            boolean armKindDiffers;
            SpecificEntry baseEntry;
            SpecificEntry currentEntry = (SpecificEntry)entry.getValue();
            if (currentEntry == null || (baseEntry = (SpecificEntry)baseSpecific.get(entry.getKey())) == null && PunchyTuningConfig.isRedundantResolvedSpecificEntry((String)entry.getKey(), currentEntry)) continue;
            JsonElement physics = baseEntry == null ? PunchyTuningConfig.copyJson(currentEntry.physics) : PunchyTuningConfig.diffJson(currentEntry.physics, baseEntry.physics);
            boolean physicsDiffers = physics != null;
            Boolean disableSprintLateralTilt = baseEntry == null ? currentEntry.disableSprintLateralTilt : PunchyTuningConfig.diffNullable(currentEntry.disableSprintLateralTilt, baseEntry.disableSprintLateralTilt);
            Boolean keepArmTuning = baseEntry == null ? currentEntry.keepArmTuning : PunchyTuningConfig.diffNullable(currentEntry.keepArmTuning, baseEntry.keepArmTuning);
            Boolean keepTriggerArmTuning = baseEntry == null ? currentEntry.keepTriggerArmTuning : PunchyTuningConfig.diffNullable(currentEntry.keepTriggerArmTuning, baseEntry.keepTriggerArmTuning);
            Boolean keepArmTuningL = baseEntry == null ? currentEntry.keepArmTuningL : PunchyTuningConfig.diffNullable(currentEntry.keepArmTuningL, baseEntry.keepArmTuningL);
            Boolean keepArmTuningR = baseEntry == null ? currentEntry.keepArmTuningR : PunchyTuningConfig.diffNullable(currentEntry.keepArmTuningR, baseEntry.keepArmTuningR);
            SideSet transforms = baseEntry == null ? PunchyTuningConfig.copySideSet(currentEntry.transforms) : PunchyTuningConfig.diffSideSet(currentEntry.transforms, baseEntry.transforms);
            boolean kindDiffers = baseEntry == null ? currentEntry.kind != null : !Objects.equals(currentEntry.kind, baseEntry.kind);
            SideSet armTransforms = baseEntry == null ? PunchyTuningConfig.copySideSet(currentEntry.armTransforms) : PunchyTuningConfig.diffSideSet(currentEntry.armTransforms, baseEntry.armTransforms);
            boolean bl = baseEntry == null ? currentEntry.armKind != null : (armKindDiffers = !Objects.equals(currentEntry.armKind, baseEntry.armKind));
            if (transforms == null && armTransforms == null && !kindDiffers && !armKindDiffers && !physicsDiffers && disableSprintLateralTilt == null && keepArmTuning == null && keepTriggerArmTuning == null && keepArmTuningL == null && keepArmTuningR == null) continue;
            SpecificEntry diffEntry = new SpecificEntry();
            if (kindDiffers) {
                diffEntry.kind = currentEntry.kind;
            }
            if (armKindDiffers) {
                diffEntry.armKind = currentEntry.armKind;
            }
            if (physicsDiffers) {
                diffEntry.physics = physics;
            }
            diffEntry.disableSprintLateralTilt = disableSprintLateralTilt;
            diffEntry.keepArmTuning = keepArmTuning;
            diffEntry.keepTriggerArmTuning = keepTriggerArmTuning;
            diffEntry.keepArmTuningL = keepArmTuningL;
            diffEntry.keepArmTuningR = keepArmTuningR;
            diffEntry.transforms = transforms;
            diffEntry.armTransforms = armTransforms;
            specificDiff.put((String)entry.getKey(), diffEntry);
            hasData = true;
        }
        delta.itemSpecific = specificDiff.isEmpty() ? null : specificDiff;
        PunchyTuningConfig.pruneEmptyCollections(delta);
        if (!hasData && delta.itemSpecific == null && delta.item == null && delta.globalItem == null && delta.globalArm == null) {
            return new Data();
        }
        return delta;
    }

    private static boolean isRedundantResolvedSpecificEntry(String itemKey, SpecificEntry entry) {
        String resolvedArmKindName;
        if (entry == null || PunchyTuningConfig.hasExplicitSpecificMetadata(entry)) {
            return false;
        }
        class_2960 itemId = class_2960.method_12829((String)itemKey);
        VanillaFirstPersonItemLayer.ToolKind resolvedItemKind = entry.kind != null ? PunchyTuningConfig.parseItemKind(entry.kind) : PunchyTuningConfig.resolveCurrentItemKind(itemId);
        PlayerArmModelLayer.ToolKind resolvedArmKind = entry.armKind != null ? PunchyTuningConfig.parseArmKind(entry.armKind) : PunchyTuningConfig.resolveCurrentArmKind(itemId);
        String resolvedItemKindName = resolvedItemKind != null ? resolvedItemKind.name() : null;
        String string = resolvedArmKindName = resolvedArmKind != null ? resolvedArmKind.name() : null;
        if (!Objects.equals(entry.kind, resolvedItemKindName)) {
            return false;
        }
        if (!Objects.equals(entry.armKind, resolvedArmKindName)) {
            return false;
        }
        SideSet itemBase = PunchyTuningConfig.captureResolvedSpecificItemBase(resolvedItemKind);
        if (!PunchyTuningConfig.isSideSetEqual(entry.transforms, itemBase)) {
            return false;
        }
        SideSet armBase = PunchyTuningConfig.captureResolvedSpecificArmBase(resolvedArmKind);
        return PunchyTuningConfig.isSideSetEqual(entry.armTransforms, armBase);
    }

    private static VanillaFirstPersonItemLayer.ToolKind parseItemKind(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return VanillaFirstPersonItemLayer.ToolKind.valueOf(raw);
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static PlayerArmModelLayer.ToolKind parseArmKind(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return PlayerArmModelLayer.ToolKind.valueOf(raw);
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static SideSet captureResolvedSpecificItemBase(VanillaFirstPersonItemLayer.ToolKind kind) {
        if (kind == null) {
            return null;
        }
        SideSet set = new SideSet();
        set.right = Pose.fromTool(ToolTuning.snapshot(kind, false));
        set.left = Pose.fromTool(ToolTuning.snapshot(kind, true));
        return set.right == null && set.left == null ? null : set;
    }

    private static SideSet captureResolvedSpecificArmBase(PlayerArmModelLayer.ToolKind kind) {
        if (kind == null) {
            return null;
        }
        SideSet set = new SideSet();
        ArmToolTuning.Profile right = ArmToolTuning.get(kind, false);
        ArmToolTuning.Profile left = ArmToolTuning.get(kind, true);
        if (right != null) {
            set.right = Pose.fromArm(PunchyTuningConfig.normalizeSpecificArmProfileForSerialization(kind, false, right));
        }
        if (left != null) {
            set.left = Pose.fromArm(PunchyTuningConfig.normalizeSpecificArmProfileForSerialization(kind, true, left));
        }
        return set.right == null && set.left == null ? null : set;
    }

    private static boolean hasExplicitSpecificMetadata(SpecificEntry entry) {
        return entry.renamed != null || entry.model != null || entry.customModelData != null || entry.animation != null || entry.customAnimation != null && !entry.customAnimation.isEmpty() || entry.customItemAnimations != null && !entry.customItemAnimations.isEmpty() || entry.betterCombat != null && !entry.betterCombat.isEmpty() || entry.physics != null && !entry.physics.isJsonNull() || entry.particleEmitters != null && !entry.particleEmitters.isJsonNull() || entry.particle != null && !entry.particle.isJsonNull() || entry.particles != null && !entry.particles.isJsonNull() || entry.particleEffects != null && !entry.particleEffects.isJsonNull() || entry.particleEmission != null && !entry.particleEmission.isJsonNull() || entry.disableSprintLateralTilt != null || entry.keepArmTuning != null || entry.keepTriggerArmTuning != null || entry.keepArmTuningL != null || entry.keepArmTuningR != null;
    }

    private static SideSet diffSideSet(SideSet current, SideSet base) {
        if (current == null) {
            return null;
        }
        SideSet diff = new SideSet();
        if (current.right != null) {
            diff.right = PunchyTuningConfig.diffPose(current.right, base != null ? base.right : null);
        }
        if (current.left != null) {
            diff.left = PunchyTuningConfig.diffPose(current.left, base != null ? base.left : null);
        }
        if (diff.right == null && diff.left == null) {
            return null;
        }
        return diff;
    }

    private static Map<String, Pose> diffPoseMap(Map<String, Pose> current, Map<String, Pose> base) {
        if (current == null) {
            return null;
        }
        HashMap<String, Pose> diff = new HashMap<String, Pose>();
        for (Map.Entry<String, Pose> entry : current.entrySet()) {
            Pose baseValue;
            Pose delta;
            Pose value;
            if (entry == null || (value = entry.getValue()) == null || (delta = PunchyTuningConfig.diffPose(value, baseValue = base != null ? base.get(entry.getKey()) : null)) == null) continue;
            diff.put(entry.getKey(), delta);
        }
        return diff.isEmpty() ? null : diff;
    }

    private static JsonElement diffJson(JsonElement current, JsonElement base) {
        if (current == null || current.isJsonNull()) {
            return null;
        }
        if (base == null || base.isJsonNull() || !current.equals(base)) {
            return current.deepCopy();
        }
        return null;
    }

    private static JsonElement copyJson(JsonElement src) {
        if (src == null || src.isJsonNull()) {
            return null;
        }
        return src.deepCopy();
    }

    private static <T> T diffNullable(T current, T base) {
        if (!Objects.equals(current, base)) {
            return current;
        }
        return null;
    }

    private static SpecificEntry copySpecificEntryForProfile(SpecificEntry src) {
        if (src == null) {
            return null;
        }
        SpecificEntry copy = new SpecificEntry();
        copy.kind = src.kind;
        copy.armKind = src.armKind;
        copy.renamed = src.renamed;
        copy.model = src.model;
        copy.customModelData = src.customModelData;
        copy.animation = src.animation;
        copy.customAnimation = src.customAnimation;
        copy.customItemAnimations = src.customItemAnimations;
        copy.particleEmitters = PunchyTuningConfig.copyJson(src.particleEmitters);
        copy.particle = PunchyTuningConfig.copyJson(src.particle);
        copy.particles = PunchyTuningConfig.copyJson(src.particles);
        copy.particleEffects = PunchyTuningConfig.copyJson(src.particleEffects);
        copy.particleEmission = PunchyTuningConfig.copyJson(src.particleEmission);
        copy.betterCombat = src.betterCombat;
        copy.physics = PunchyTuningConfig.copyJson(src.physics);
        copy.disableSprintLateralTilt = src.disableSprintLateralTilt;
        copy.keepArmTuning = src.keepArmTuning;
        copy.keepTriggerArmTuning = src.keepTriggerArmTuning;
        copy.keepArmTuningL = src.keepArmTuningL;
        copy.keepArmTuningR = src.keepArmTuningR;
        copy.transforms = PunchyTuningConfig.copySideSet(src.transforms);
        copy.armTransforms = PunchyTuningConfig.copySideSet(src.armTransforms);
        return copy;
    }

    private static ToolTuning.Profile copyToolProfile(ToolTuning.Profile src) {
        if (src == null) {
            return null;
        }
        ToolTuning.Profile copy = new ToolTuning.Profile(src.scale, src.offX, src.offY, src.offZ, src.rotX, src.rotY, src.rotZ, src.pivotX, src.pivotY, src.pivotZ, src.physicsMode);
        copy.disableSprintLateralTilt = src.disableSprintLateralTilt;
        copy.keepArmTuning = src.keepArmTuning;
        copy.keepTriggerArmTuning = src.keepTriggerArmTuning;
        if (src.physicsSettings != null) {
            copy.physicsSettings = src.physicsSettings.copy();
        }
        return copy;
    }

    private static Pose diffPose(Pose current, Pose base) {
        if (current == null) {
            return null;
        }
        if (base == null || !PunchyTuningConfig.poseEquals(current, base)) {
            return PunchyTuningConfig.copyPose(current);
        }
        return null;
    }

    private static Pose copyPose(Pose src) {
        if (src == null) {
            return null;
        }
        Pose copy = new Pose(src.scale, src.offX, src.offY, src.offZ, src.rotX, src.rotY, src.rotZ, src.pivotX, src.pivotY, src.pivotZ, src.physics);
        copy.disableSprintLateralTilt = src.disableSprintLateralTilt;
        copy.keepArmTuning = src.keepArmTuning;
        copy.keepTriggerArmTuning = src.keepTriggerArmTuning;
        copy.keepArmTuningL = src.keepArmTuningL;
        copy.keepArmTuningR = src.keepArmTuningR;
        return copy;
    }

    private static SideSet copySideSet(SideSet src) {
        if (src == null) {
            return null;
        }
        SideSet copy = new SideSet();
        copy.right = PunchyTuningConfig.copyPose(src.right);
        copy.left = PunchyTuningConfig.copyPose(src.left);
        if (copy.right == null && copy.left == null) {
            return null;
        }
        return copy;
    }

    private static boolean poseEquals(Pose a, Pose b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return PunchyTuningConfig.nearlyEqual(a.scale, b.scale) && PunchyTuningConfig.nearlyEqual(a.offX, b.offX) && PunchyTuningConfig.nearlyEqual(a.offY, b.offY) && PunchyTuningConfig.nearlyEqual(a.offZ, b.offZ) && PunchyTuningConfig.nearlyEqual(a.rotX, b.rotX) && PunchyTuningConfig.nearlyEqual(a.rotY, b.rotY) && PunchyTuningConfig.nearlyEqual(a.rotZ, b.rotZ) && PunchyTuningConfig.nearlyEqual(a.pivotX, b.pivotX) && PunchyTuningConfig.nearlyEqual(a.pivotY, b.pivotY) && PunchyTuningConfig.nearlyEqual(a.pivotZ, b.pivotZ) && Objects.equals(a.physics, b.physics) && Objects.equals(a.disableSprintLateralTilt, b.disableSprintLateralTilt) && Objects.equals(a.keepArmTuning, b.keepArmTuning) && Objects.equals(a.keepTriggerArmTuning, b.keepTriggerArmTuning) && Objects.equals(a.keepArmTuningL, b.keepArmTuningL) && Objects.equals(a.keepArmTuningR, b.keepArmTuningR);
    }

    private static boolean nearlyEqual(float a, float b) {
        return Math.abs(a - b) <= 1.0E-6f;
    }

    private static Boolean resolveSideKeepArmTuning(Boolean common, Boolean sideSpecific) {
        return sideSpecific != null ? sideSpecific : common;
    }

    private static void pruneEmptyCollections(Data data) {
        if (data.item != null && data.item.isEmpty()) {
            data.item = null;
        }
        if (data.armMesh != null && data.armMesh.isEmpty()) {
            data.armMesh = null;
        }
        if (data.chestProxy != null && data.chestProxy.isEmpty()) {
            data.chestProxy = null;
        }
        if (data.fluidProxy != null && data.fluidProxy.isEmpty()) {
            data.fluidProxy = null;
        }
        if (data.enchantParticleProxy != null && data.enchantParticleProxy.isEmpty()) {
            data.enchantParticleProxy = null;
        }
        if (data.paddleProxy != null && data.paddleProxy.isEmpty()) {
            data.paddleProxy = null;
        }
        if (data.fishingLineProxy != null && data.fishingLineProxy.isEmpty()) {
            data.fishingLineProxy = null;
        }
        if (data.itemSpecific != null && data.itemSpecific.isEmpty()) {
            data.itemSpecific = null;
        }
    }

    private static boolean isSideSetEqual(SideSet a, SideSet b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return PunchyTuningConfig.poseEquals(a.right, b.right) && PunchyTuningConfig.poseEquals(a.left, b.left);
    }

    private static boolean isGroupEntryEqual(GroupEntry a, GroupEntry b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return PunchyTuningConfig.isSideSetEqual(a.transforms, b.transforms) && PunchyTuningConfig.isSideSetEqual(a.armTransforms, b.armTransforms) && Objects.equals(a.disableSprintLateralTilt, b.disableSprintLateralTilt) && Objects.equals(a.keepArmTuning, b.keepArmTuning) && Objects.equals(a.keepTriggerArmTuning, b.keepTriggerArmTuning) && Objects.equals(a.keepArmTuningL, b.keepArmTuningL) && Objects.equals(a.keepArmTuningR, b.keepArmTuningR);
    }

    private static boolean isDataEmpty(Data data) {
        if (data == null) {
            return true;
        }
        boolean globalsEmpty = (data.globalItem == null || data.globalItem.right == null && data.globalItem.left == null) && (data.globalArm == null || data.globalArm.right == null && data.globalArm.left == null);
        boolean mapsEmpty = !(data.item != null && !data.item.isEmpty() || data.itemSpecific != null && !data.itemSpecific.isEmpty() || data.chestProxy != null && !data.chestProxy.isEmpty() || data.fluidProxy != null && !data.fluidProxy.isEmpty() || data.enchantParticleProxy != null && !data.enchantParticleProxy.isEmpty() || data.paddleProxy != null && !data.paddleProxy.isEmpty() || data.fishingLineProxy != null && !data.fishingLineProxy.isEmpty());
        return globalsEmpty && mapsEmpty;
    }

    private static void stripRedundantEntries(Data data) {
        Map<String, Object> base;
        if (data == null || BASELINE_STATE == null) {
            return;
        }
        if (PunchyTuningConfig.isSideSetEqual(data.globalItem, PunchyTuningConfig.BASELINE_STATE.globalItem)) {
            data.globalItem = null;
        }
        if (PunchyTuningConfig.isSideSetEqual(data.globalArm, PunchyTuningConfig.BASELINE_STATE.globalArm)) {
            data.globalArm = null;
        }
        if (data.item != null) {
            base = PunchyTuningConfig.BASELINE_STATE.item;
            data.item.entrySet().removeIf(entry -> PunchyTuningConfig.isGroupEntryEqual((GroupEntry)entry.getValue(), base != null ? (GroupEntry)base.get(entry.getKey()) : null));
            if (data.item.isEmpty()) {
                data.item = null;
            }
        }
        if (data.armMesh != null) {
            base = PunchyTuningConfig.BASELINE_STATE.armMesh;
            data.armMesh.entrySet().removeIf(entry -> PunchyTuningConfig.isSideSetEqual((SideSet)entry.getValue(), base != null ? (SideSet)base.get(entry.getKey()) : null));
            if (data.armMesh.isEmpty()) {
                data.armMesh = null;
            }
        }
        if (data.chestProxy != null) {
            base = PunchyTuningConfig.BASELINE_STATE.chestProxy;
            data.chestProxy.entrySet().removeIf(entry -> PunchyTuningConfig.poseEquals((Pose)entry.getValue(), base != null ? (Pose)base.get(entry.getKey()) : null));
            if (data.chestProxy.isEmpty()) {
                data.chestProxy = null;
            }
        }
        if (data.fluidProxy != null) {
            base = PunchyTuningConfig.BASELINE_STATE.fluidProxy;
            data.fluidProxy.entrySet().removeIf(entry -> PunchyTuningConfig.poseEquals((Pose)entry.getValue(), base != null ? (Pose)base.get(entry.getKey()) : null));
            if (data.fluidProxy.isEmpty()) {
                data.fluidProxy = null;
            }
        }
        if (data.enchantParticleProxy != null) {
            base = PunchyTuningConfig.BASELINE_STATE.enchantParticleProxy;
            data.enchantParticleProxy.entrySet().removeIf(entry -> PunchyTuningConfig.poseEquals((Pose)entry.getValue(), base != null ? (Pose)base.get(entry.getKey()) : null));
            if (data.enchantParticleProxy.isEmpty()) {
                data.enchantParticleProxy = null;
            }
        }
        if (data.paddleProxy != null) {
            base = PunchyTuningConfig.BASELINE_STATE.paddleProxy;
            data.paddleProxy.entrySet().removeIf(entry -> PunchyTuningConfig.poseEquals((Pose)entry.getValue(), base != null ? (Pose)base.get(entry.getKey()) : null));
            if (data.paddleProxy.isEmpty()) {
                data.paddleProxy = null;
            }
        }
        if (data.fishingLineProxy != null) {
            base = PunchyTuningConfig.BASELINE_STATE.fishingLineProxy;
            data.fishingLineProxy.entrySet().removeIf(entry -> PunchyTuningConfig.poseEquals((Pose)entry.getValue(), base != null ? (Pose)base.get(entry.getKey()) : null));
            if (data.fishingLineProxy.isEmpty()) {
                data.fishingLineProxy = null;
            }
        }
        if (data.itemSpecific != null) {
            Map<String, SpecificEntry> baseSpecific = PunchyTuningConfig.BASELINE_STATE.itemSpecific;
            data.itemSpecific.entrySet().removeIf(entry -> {
                SpecificEntry base = baseSpecific != null ? (SpecificEntry)baseSpecific.get(entry.getKey()) : null;
                boolean kindMatches = Objects.equals(((SpecificEntry)entry.getValue()).kind, base != null ? base.kind : null);
                boolean armKindMatches = Objects.equals(((SpecificEntry)entry.getValue()).armKind, base != null ? base.armKind : null);
                boolean renamedMatches = Objects.equals(((SpecificEntry)entry.getValue()).renamed, base != null ? base.renamed : null);
                boolean disableSprintTiltMatches = Objects.equals(((SpecificEntry)entry.getValue()).disableSprintLateralTilt, base != null ? base.disableSprintLateralTilt : null);
                boolean keepArmTuningMatches = Objects.equals(((SpecificEntry)entry.getValue()).keepArmTuning, base != null ? base.keepArmTuning : null);
                boolean keepTriggerArmTuningMatches = Objects.equals(((SpecificEntry)entry.getValue()).keepTriggerArmTuning, base != null ? base.keepTriggerArmTuning : null);
                boolean keepArmTuningLeftMatches = Objects.equals(((SpecificEntry)entry.getValue()).keepArmTuningL, base != null ? base.keepArmTuningL : null);
                boolean keepArmTuningRightMatches = Objects.equals(((SpecificEntry)entry.getValue()).keepArmTuningR, base != null ? base.keepArmTuningR : null);
                boolean transformsMatch = PunchyTuningConfig.isSideSetEqual(((SpecificEntry)entry.getValue()).transforms, base != null ? base.transforms : null);
                boolean armTransformsMatch = PunchyTuningConfig.isSideSetEqual(((SpecificEntry)entry.getValue()).armTransforms, base != null ? base.armTransforms : null);
                return kindMatches && armKindMatches && renamedMatches && disableSprintTiltMatches && keepArmTuningMatches && keepTriggerArmTuningMatches && keepArmTuningLeftMatches && keepArmTuningRightMatches && transformsMatch && armTransformsMatch;
            });
            if (data.itemSpecific.isEmpty()) {
                data.itemSpecific = null;
            }
        }
        PunchyTuningConfig.pruneEmptyCollections(data);
    }

    private static void saveData(Data data) {
        File file = PunchyTuningConfig.getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file);){
            GSON.toJson((Object)data, (Appendable)writer);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void saveCompatData(String profile, Data data) {
        Path compatDir = PunchyTuningConfig.getCompatDirectory();
        try {
            Files.createDirectories(compatDir, new FileAttribute[0]);
            Path target = compatDir.resolve(profile + ".json");
            try (FileWriter writer = new FileWriter(target.toFile());){
                GSON.toJson((Object)data, (Appendable)writer);
            }
            PunchyTuningConfig.mirrorCompatResource(profile, target);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void mirrorCompatResource(String profile, Path source) {
        if (!"items_compat".equals(profile) || source == null) {
            return;
        }
        try {
            Path workspace = Path.of("", new String[0]).toAbsolutePath();
            Path srcRoot = workspace.resolve("src");
            if (!Files.exists(srcRoot, new LinkOption[0])) {
                return;
            }
            Path compatDir = srcRoot.resolve("main").resolve("resources").resolve("assets").resolve("minecraft").resolve("punchy").resolve("compat");
            Files.createDirectories(compatDir, new FileAttribute[0]);
            Path dest = compatDir.resolve(source.getFileName());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void applyCompatOverlays(class_3300 resourceManager) {
        Data baseData = PunchyTuningConfig.buildBaseDefaults(resourceManager);
        DEFAULTS_RESOURCE_STATE = PunchyTuningConfig.copyData(baseData);
        if (baseData != null) {
            PunchyTuningConfig.applyData(PunchyTuningConfig.copyData(baseData));
        }
        Set<class_2960> compatIds = PunchyTuningConfig.discoverCompatResourceIds(resourceManager).keySet();
        MIX_COMPAT_OPTION_IDS = PunchyTuningConfig.resolveMixCompatOptionIds(resourceManager, compatIds);
        MIX_PUNCHY_OPTION_IDS = PunchyTuningConfig.discoverPunchyOptionIds(resourceManager, compatIds);
        MIX_CATALOG = PunchyTuningConfig.buildMixCatalog(resourceManager, PunchyTuningConfig.discoverCompatProfilesFromResourcePacks(resourceManager));
        PunchyTuningConfig.applyMixSelections();
    }

    private static void reapplyActiveProfilePostCompatOverrides() {
        Data activeOverrides = PunchyTuningConfig.readActiveProfilePostCompatOverrides();
        if (activeOverrides == null || PunchyTuningConfig.isDataEmpty(activeOverrides)) {
            return;
        }
        try {
            PunchyTuningConfig.applyData(activeOverrides, false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static Data readActiveProfilePostCompatOverrides() {
        GroupEntry emptyGroup;
        Data active;
        Path activeProfile = TuningProfileManager.getActiveProfileFilePath();
        if (activeProfile == null || !Files.isRegularFile(activeProfile, new LinkOption[0])) {
            return null;
        }
        try (FileReader reader = new FileReader(activeProfile.toFile());){
            active = (Data)GSON.fromJson((Reader)reader, Data.class);
        }
        catch (Exception ignored) {
            return null;
        }
        if (active == null) {
            return null;
        }
        Data overrides = new Data();
        if (PunchyTuningConfig.hasEmptyHandOverrideData(active.emptyHand)) {
            overrides.emptyHand = PunchyTuningConfig.copyEmptyHandEntry(active.emptyHand);
        }
        if (active.item != null && PunchyTuningConfig.hasGroupOverrideData(emptyGroup = active.item.get(VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND.name()))) {
            overrides.item = new HashMap<String, GroupEntry>();
            overrides.item.put(VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND.name(), PunchyTuningConfig.copyGroupEntry(emptyGroup));
        }
        if (active.itemSpecific != null && !active.itemSpecific.isEmpty()) {
            for (Map.Entry<String, SpecificEntry> entry : active.itemSpecific.entrySet()) {
                if (entry == null || entry.getKey() == null || entry.getValue() == null || !PunchyTuningConfig.hasMixPayload(entry.getValue())) continue;
                if (overrides.itemSpecific == null) {
                    overrides.itemSpecific = new HashMap<String, SpecificEntry>();
                }
                overrides.itemSpecific.put(entry.getKey(), PunchyTuningConfig.copySpecificEntry(entry.getValue()));
            }
        }
        PunchyTuningConfig.pruneEmptyCollections(overrides);
        return PunchyTuningConfig.isDataEmpty(overrides) ? null : overrides;
    }

    private static boolean hasEmptyHandOverrideData(EmptyHandEntry entry) {
        return entry != null && (entry.transforms != null || entry.armTransforms != null || PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations) || entry.physics != null && !entry.physics.isJsonNull() || entry.disableSprintLateralTilt != null || entry.keepArmTuning != null || entry.keepTriggerArmTuning != null || entry.keepArmTuningL != null || entry.keepArmTuningR != null || entry.betterCombat != null && !entry.betterCombat.isEmpty());
    }

    private static boolean hasGroupOverrideData(GroupEntry entry) {
        return entry != null && (entry.transforms != null || entry.armTransforms != null || PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations) || entry.physics != null && !entry.physics.isJsonNull() || entry.disableSprintLateralTilt != null || entry.keepArmTuning != null || entry.keepTriggerArmTuning != null || entry.keepArmTuningL != null || entry.keepArmTuningR != null || entry.betterCombat != null && !entry.betterCombat.isEmpty());
    }

    private static Data buildMergedCompatOverlay(class_3300 resourceManager) {
        List<CompatResourceEntry> entries = PunchyTuningConfig.collectCompatResourceEntries(resourceManager);
        if (entries.isEmpty()) {
            return null;
        }
        Data merged = null;
        for (CompatResourceEntry entry : entries) {
            Data data;
            if (entry == null || entry.resource == null || (data = PunchyTuningConfig.readCompatResource(entry.resource)) == null) continue;
            merged = PunchyTuningConfig.mergeDataPreferBase(merged, PunchyTuningConfig.copyData(data));
        }
        return PunchyTuningConfig.finalizeCompatData("selected", merged);
    }

    private static List<CompatResourceEntry> collectCompatResourceEntries(class_3300 resourceManager) {
        Map<class_2960, String> compatResources = PunchyTuningConfig.discoverCompatResourceIds(resourceManager);
        if (compatResources.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Integer> packPriorities = PunchyTuningConfig.resolveCompatPackPriorities(resourceManager, compatResources.keySet());
        ArrayList<CompatResourceEntry> entries = new ArrayList<CompatResourceEntry>();
        AtomicInteger nextFallbackPriority = new AtomicInteger(packPriorities.values().stream().mapToInt(Integer::intValue).min().orElse(0) - 1);
        int discoveryOrder = 0;
        for (Map.Entry<class_2960, String> compatEntry : compatResources.entrySet()) {
            String searchPath;
            Map<class_2960, List<class_3298>> stacks;
            class_2960 id = compatEntry.getKey();
            String profile = compatEntry.getValue();
            List<class_3298> stack = PunchyTuningConfig.tryGetResourceStack(resourceManager, id);
            if ((stack == null || stack.isEmpty()) && resourceManager != null && (stacks = PunchyTuningConfig.tryListResourceStacks(resourceManager, searchPath = id.method_12832().contains("/") ? id.method_12832().substring(0, id.method_12832().lastIndexOf(47)) : id.method_12832())) != null && !stacks.isEmpty()) {
                stack = stacks.get(id);
            }
            if (stack != null && !stack.isEmpty()) {
                int stackOrder = 0;
                for (int i = stack.size() - 1; i >= 0; --i) {
                    class_3298 resource = stack.get(i);
                    if (resource == null) continue;
                    int packPriority = PunchyTuningConfig.getOrAllocatePackPriority(packPriorities, resource.method_14480(), nextFallbackPriority);
                    entries.add(new CompatResourceEntry(id, profile, resource, packPriority, discoveryOrder, stackOrder++));
                }
                ++discoveryOrder;
                continue;
            }
            try {
                Optional resource = resourceManager.method_14486(id);
                if (resource.isPresent()) {
                    class_3298 resolved = (class_3298)resource.get();
                    int packPriority = PunchyTuningConfig.getOrAllocatePackPriority(packPriorities, resolved.method_14480(), nextFallbackPriority);
                    entries.add(new CompatResourceEntry(id, profile, resolved, packPriority, discoveryOrder, 0));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++discoveryOrder;
        }
        entries.sort(Comparator.comparingInt(entry -> entry.packPriority).reversed().thenComparingInt(entry -> entry.discoveryOrder).thenComparingInt(entry -> entry.stackOrder).thenComparing(entry -> entry.id.toString()));
        if (!entries.isEmpty()) {
            StringJoiner joiner = new StringJoiner(" | ");
            for (CompatResourceEntry entry2 : entries) {
                if (entry2 == null || entry2.resource == null) continue;
                joiner.add(entry2.resource.method_14480() + "#" + entry2.id.method_12832() + "(p=" + entry2.packPriority + ",d=" + entry2.discoveryOrder + ",s=" + entry2.stackOrder + ")");
            }
        }
        return entries;
    }

    private static int getOrAllocatePackPriority(Map<String, Integer> packPriorities, String packId, AtomicInteger nextFallbackPriority) {
        if (packId == null || packId.isBlank()) {
            return nextFallbackPriority.getAndDecrement();
        }
        Integer existing = packPriorities.get(packId);
        if (existing != null) {
            return existing;
        }
        int allocated = nextFallbackPriority.getAndDecrement();
        packPriorities.put(packId, allocated);
        return allocated;
    }

    private static Map<class_2960, String> discoverCompatResourceIds(class_3300 resourceManager) {
        LinkedHashMap<class_2960, String> result = new LinkedHashMap<class_2960, String>();
        if (resourceManager == null) {
            return result;
        }
        PunchyTuningConfig.collectCompatResourceIds(resourceManager, "punchy/compat", "minecraft", "punchy/compat/", result);
        PunchyTuningConfig.collectCompatResourceIds(resourceManager, "compat", "punchy", "compat/", result);
        return result;
    }

    private static void collectCompatResourceIds(class_3300 manager, String searchPath, String namespace, String prefix, Map<class_2960, String> result) {
        Map found = manager.method_14488(searchPath, loc -> loc.method_12836().equals(namespace) && loc.method_12832().startsWith(prefix) && loc.method_12832().endsWith(".json"));
        for (class_2960 id : found.keySet()) {
            String path = id.method_12832();
            String file = path.substring(prefix.length());
            if (file.endsWith(".json")) {
                file = file.substring(0, file.length() - 5);
            }
            if (file.isBlank()) continue;
            result.putIfAbsent(id, file.toLowerCase(Locale.ROOT));
        }
    }

    private static Map<String, Integer> resolveCompatPackPriorities(class_3300 resourceManager, Set<class_2960> compatIds) {
        LinkedHashMap<String, Integer> priorities = new LinkedHashMap<String, Integer>();
        List<String> selected = PunchyTuningConfig.getSelectedResourcePackIdsHighToLow();
        int weight = selected.size();
        for (String packId : selected) {
            if (packId == null || packId.isBlank()) continue;
            priorities.putIfAbsent(packId, weight--);
        }
        return priorities;
    }

    private static Set<String> resolveMixCompatOptionIds(class_3300 resourceManager, Set<class_2960> compatIds) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (CompatResourceEntry entry : PunchyTuningConfig.collectCompatResourceEntries(resourceManager)) {
            String optionId;
            if (entry == null || entry.resource == null || (optionId = PunchyTuningConfig.normalizeMixOption(entry.resource.method_14480())) == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
            result.add(optionId);
        }
        return result;
    }

    private static Set<String> discoverPunchyOptionIds(class_3300 resourceManager, Set<class_2960> compatIds) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (String packId : PunchyTuningConfig.getSelectedResourcePackIdsHighToLow()) {
            String optionId = PunchyTuningConfig.normalizeMixOption(packId);
            if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
            result.add(optionId);
        }
        result.addAll(PunchyTuningConfig.resolveMixCompatOptionIds(resourceManager, compatIds));
        return result;
    }

    private static List<String> getSelectedResourcePackIds() {
        List<String> highToLow = PunchyTuningConfig.getSelectedResourcePackIdsHighToLow();
        if (highToLow.isEmpty()) {
            return List.of();
        }
        ArrayList<String> lowToHigh = new ArrayList<String>(highToLow);
        Collections.reverse(lowToHigh);
        return lowToHigh;
    }

    private static List<String> getSelectedResourcePackIdsHighToLow() {
        class_310 client = class_310.method_1551();
        if (client == null) {
            return List.of();
        }
        class_3300 resourceManager = client.method_1478();
        if (cachedSelectedResourcePackIdsHighToLow != null && cachedSelectedResourcePackResourceManager == resourceManager) {
            PunchyTuningConfig.recordSelectedPackLookup(false);
            return cachedSelectedResourcePackIdsHighToLow;
        }
        PunchyTuningConfig.recordSelectedPackLookup(true);
        ArrayList<String> result = new ArrayList<String>();
        try {
            class_3283 repository = client.method_1520();
            if (repository != null) {
                for (String packId : repository.method_29210()) {
                    if (packId == null || packId.isBlank()) continue;
                    result.add(packId);
                }
            }
        }
        catch (Exception repository) {
            // empty catch block
        }
        Collections.reverse(result);
        String builtinPackId = PunchyTuningConfig.resolveActiveBuiltInPackId(client);
        if (builtinPackId != null && !builtinPackId.isBlank() && !result.contains(builtinPackId)) {
            result.add(0, builtinPackId);
        }
        cachedSelectedResourcePackResourceManager = resourceManager;
        cachedSelectedResourcePackIdsHighToLow = List.copyOf(result);
        return cachedSelectedResourcePackIdsHighToLow;
    }

    private static Set<String> getActiveMixPackOptionIds() {
        LinkedHashSet<String> active = new LinkedHashSet<String>();
        for (String packId : PunchyTuningConfig.getSelectedResourcePackIdsHighToLow()) {
            String optionId = PunchyTuningConfig.normalizeMixOption(packId);
            if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
            active.add(optionId);
        }
        return active;
    }

    private static String resolveActiveBuiltInPackId(class_310 client) {
        if (client == null || client.method_1478() == null) {
            return null;
        }
        class_3300 resourceManager = client.method_1478();
        if (cachedActiveBuiltInPackResourceManager == resourceManager) {
            return cachedActiveBuiltInPackId;
        }
        ++builtInPackLookupCount;
        try {
            class_2960 id = class_2960.method_60655((String)"minecraft", (String)"punchy/compat/default_tuning.json");
            cachedActiveBuiltInPackResourceManager = resourceManager;
            cachedActiveBuiltInPackId = resourceManager.method_14486(id).map(class_3298::method_14480).filter(packId -> packId != null && !packId.isBlank()).orElse(null);
            return cachedActiveBuiltInPackId;
        }
        catch (Exception exception) {
            cachedActiveBuiltInPackResourceManager = resourceManager;
            cachedActiveBuiltInPackId = null;
            return null;
        }
    }

    private static void invalidateSelectedResourcePackCache() {
        if (cachedSelectedResourcePackIdsHighToLow != null || cachedActiveBuiltInPackResourceManager != null) {
            // empty if block
        }
        cachedSelectedResourcePackIdsHighToLow = null;
        cachedSelectedResourcePackResourceManager = null;
        cachedActiveBuiltInPackId = null;
        cachedActiveBuiltInPackResourceManager = null;
    }

    private static void recordSelectedPackLookup(boolean cacheMiss) {
        long now = System.nanoTime();
        if (selectedPackLookupWindowStartedNanos == 0L) {
            selectedPackLookupWindowStartedNanos = now;
        }
        ++selectedPackLookupCount;
        if (cacheMiss) {
            ++selectedPackCacheMissCount;
        }
        if (now - selectedPackLookupWindowStartedNanos < 1000000000L) {
            return;
        }
        if (selectedPackLookupCount > 120 || selectedPackCacheMissCount > 1 || builtInPackLookupCount > 0) {
            // empty if block
        }
        selectedPackLookupWindowStartedNanos = now;
        selectedPackLookupCount = 0;
        selectedPackCacheMissCount = 0;
        builtInPackLookupCount = 0;
    }

    private static String describeSelectedPackLookupCaller() {
        StackTraceElement[] stack;
        for (StackTraceElement element : stack = Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (className.equals(PunchyTuningConfig.class.getName()) || className.equals(Thread.class.getName())) continue;
            return element.toString();
        }
        return "unknown";
    }

    private static Set<String> discoverCompatProfilesFromResourcePacks(class_3300 resourceManager) {
        return new LinkedHashSet<String>(PunchyTuningConfig.discoverCompatResourceIds(resourceManager).values());
    }

    private static MixCatalog buildMixCatalog(class_3300 resourceManager, Set<String> profiles) {
        MixCatalog catalog = new MixCatalog();
        if (DEFAULTS_RESOURCE_STATE != null) {
            PunchyTuningConfig.collectMixSlotsFromData(catalog, DEFAULTS_RESOURCE_STATE, MIX_DEFAULT_OPTION_ID);
            PunchyTuningConfig.collectMixSlotsFromData(catalog, PunchyTuningConfig.copyData(DEFAULTS_RESOURCE_STATE), BUILTIN_PACK_OPTION_ID);
        }
        if (profiles != null) {
            for (String profile : profiles) {
                if (profile == null || profile.isBlank()) continue;
                PunchyTuningConfig.collectMixCatalogProfile(resourceManager, catalog, profile, "minecraft", "punchy/compat/" + profile + ".json");
                PunchyTuningConfig.collectMixCatalogProfile(resourceManager, catalog, profile, "punchy", "compat/" + profile + ".json");
            }
        }
        PunchyTuningConfig.finalizeMixCatalog(resourceManager, catalog);
        return catalog;
    }

    private static void collectMixCatalogProfile(class_3300 resourceManager, MixCatalog catalog, String profile, String namespace, String path) {
        String searchPath;
        Map<class_2960, List<class_3298>> stacks;
        class_2960 id = class_2960.method_60655((String)namespace, (String)path);
        List<class_3298> stack = PunchyTuningConfig.tryGetResourceStack(resourceManager, id);
        if ((stack == null || stack.isEmpty()) && resourceManager != null && (stacks = PunchyTuningConfig.tryListResourceStacks(resourceManager, searchPath = path.contains("/") ? path.substring(0, path.lastIndexOf(47)) : path)) != null && !stacks.isEmpty()) {
            stack = stacks.get(id);
        }
        if (stack == null || stack.isEmpty()) {
            try {
                Data data;
                Optional resource = resourceManager.method_14486(id);
                if (resource.isPresent() && (data = PunchyTuningConfig.readCompatResource((class_3298)resource.get())) != null) {
                    String optionId = PunchyTuningConfig.normalizeMixOption(((class_3298)resource.get()).method_14480());
                    PunchyTuningConfig.collectMixSlotsFromData(catalog, data, optionId);
                    return;
                }
            }
            catch (Exception resource) {
                // empty catch block
            }
            return;
        }
        for (int i = 0; i < stack.size(); ++i) {
            Data data;
            class_3298 resource = stack.get(i);
            if (resource == null || (data = PunchyTuningConfig.readCompatResource(resource)) == null) continue;
            String optionId = PunchyTuningConfig.normalizeMixOption(resource.method_14480());
            PunchyTuningConfig.collectMixSlotsFromData(catalog, PunchyTuningConfig.copyData(data), optionId);
        }
    }

    private static void collectMixSlotsFromData(MixCatalog catalog, Data data, String optionId) {
        if (catalog == null || data == null || optionId == null || optionId.isBlank()) {
            return;
        }
        if (data.item != null) {
            for (Map.Entry<String, GroupEntry> entry : data.item.entrySet()) {
                VanillaFirstPersonItemLayer.ToolKind kind;
                GroupEntry group;
                if (entry == null || entry.getKey() == null || entry.getValue() == null || !PunchyTuningConfig.hasAnimationMixPayload(group = entry.getValue()) || (kind = PunchyTuningConfig.parseToolKind(entry.getKey())) == null) continue;
                String slotId = "kind:" + kind.name().toLowerCase(Locale.ROOT);
                MixSlotDef slot = catalog.slots.computeIfAbsent(slotId, key -> {
                    MixSlotDef def = new MixSlotDef();
                    def.slotId = key;
                    def.type = MixSlotType.KIND;
                    def.kind = kind;
                    def.label = PunchyTuningConfig.prettySlotLabel(kind.name());
                    return def;
                });
                if (MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
                PunchyTuningConfig.registerMixSlotOption(slot, optionId);
                PunchyTuningConfig.registerMixSlotPayload(slot, optionId, PunchyTuningConfig.payloadFromGroup(group, kind, optionId));
            }
        }
        if (data.emptyHand != null && PunchyTuningConfig.hasAnimationMixPayload(data.emptyHand)) {
            VanillaFirstPersonItemLayer.ToolKind kind = VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND;
            String string = "kind:" + kind.name().toLowerCase(Locale.ROOT);
            MixSlotDef slot = catalog.slots.computeIfAbsent(string, key -> {
                MixSlotDef def = new MixSlotDef();
                def.slotId = key;
                def.type = MixSlotType.KIND;
                def.kind = kind;
                def.label = PunchyTuningConfig.prettySlotLabel(kind.name());
                return def;
            });
            if (!MIX_DEFAULT_OPTION_ID.equals(optionId)) {
                PunchyTuningConfig.registerMixSlotOption(slot, optionId);
                PunchyTuningConfig.registerMixSlotPayload(slot, optionId, PunchyTuningConfig.payloadFromEmptyHand(data.emptyHand, kind, optionId));
            }
        }
        if (data.itemSpecific != null) {
            for (Map.Entry<String, Object> entry : data.itemSpecific.entrySet()) {
                class_2960 itemId;
                SpecificEntry specific;
                if (entry == null || entry.getKey() == null || entry.getValue() == null || !PunchyTuningConfig.hasAnimationMixPayload(specific = (SpecificEntry)entry.getValue()) || (itemId = PunchyTuningConfig.resolveSpecificItemId(entry.getKey())) == null || PunchyTuningConfig.isMixSpecificHidden(itemId)) continue;
                VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.parseToolKind(specific.kind);
                if (kind == null) {
                    kind = PunchyTuningConfig.resolveKindForItem(itemId);
                }
                if (kind != null) {
                    VanillaFirstPersonItemLayer.ToolKind resolvedKind = kind;
                    String kindSlotId = "kind:" + kind.name().toLowerCase(Locale.ROOT);
                    MixSlotDef slot = catalog.slots.computeIfAbsent(kindSlotId, key -> {
                        MixSlotDef def = new MixSlotDef();
                        def.slotId = key;
                        def.type = MixSlotType.KIND;
                        def.kind = resolvedKind;
                        def.label = PunchyTuningConfig.prettySlotLabel(resolvedKind.name());
                        return def;
                    });
                    slot.mergedSpecificItems.add(itemId);
                    if (MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
                    PunchyTuningConfig.registerMixSlotOption(slot, optionId);
                    PunchyTuningConfig.registerMixSlotSpecificPayload(slot, optionId, itemId, PunchyTuningConfig.payloadFromSpecific(specific, resolvedKind, optionId));
                    continue;
                }
                String slotId = "specific:" + String.valueOf(itemId);
                MixSlotDef slot = catalog.slots.computeIfAbsent(slotId, key -> {
                    MixSlotDef def = new MixSlotDef();
                    def.slotId = key;
                    def.type = MixSlotType.SPECIFIC;
                    def.itemId = itemId;
                    def.label = "Item Specific: " + String.valueOf(itemId);
                    return def;
                });
                if (MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
                PunchyTuningConfig.registerMixSlotOption(slot, optionId);
                PunchyTuningConfig.registerMixSlotPayload(slot, optionId, PunchyTuningConfig.payloadFromSpecific(specific, null, optionId));
            }
        }
    }

    private static boolean isMixSpecificHidden(class_2960 itemId) {
        if (itemId == null) {
            return false;
        }
        return MIX_HIDDEN_SPECIFIC_IDS.contains(itemId.toString().toLowerCase(Locale.ROOT));
    }

    private static void registerMixSlotOption(MixSlotDef slot, String optionId) {
        if (slot == null || optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) {
            return;
        }
        if (!slot.optionOrder.contains(optionId)) {
            slot.optionOrder.add(optionId);
        }
    }

    private static void finalizeMixCatalog(class_3300 resourceManager, MixCatalog catalog) {
        if (catalog == null || catalog.slots.isEmpty()) {
            return;
        }
        MixCatalogOrdering ordering = PunchyTuningConfig.resolveMixCatalogOrdering(resourceManager);
        PunchyTuningConfig.registerAnimationOnlyOptions(catalog, ordering, resourceManager);
        for (MixSlotDef slot : catalog.slots.values()) {
            if (slot == null || slot.optionOrder.isEmpty()) continue;
            ArrayList<String> sortedOptions = new ArrayList<String>(slot.optionOrder);
            sortedOptions.sort((left, right) -> {
                int rightPriority;
                int leftPriority = ordering.optionPriorities.getOrDefault(left, Integer.MIN_VALUE);
                if (leftPriority != (rightPriority = ordering.optionPriorities.getOrDefault(right, Integer.MIN_VALUE).intValue())) {
                    return Integer.compare(rightPriority, leftPriority);
                }
                return left.compareTo((String)right);
            });
            slot.optionOrder.clear();
            slot.optionOrder.addAll(sortedOptions);
            slot.resourceOrderOption = PunchyTuningConfig.resolveResourceOrderOption(sortedOptions, ordering);
        }
    }

    private static String resolveResourceOrderOption(List<String> sortedOptions, MixCatalogOrdering ordering) {
        if (sortedOptions == null || sortedOptions.isEmpty()) {
            return null;
        }
        if (ordering != null && !ordering.selectedOptionIdsByPriority.isEmpty()) {
            for (String optionId : ordering.selectedOptionIdsByPriority) {
                if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId) || !sortedOptions.contains(optionId)) continue;
                return optionId;
            }
        }
        return sortedOptions.getFirst();
    }

    private static MixCatalogOrdering resolveMixCatalogOrdering(class_3300 resourceManager) {
        MixCatalogOrdering ordering = new MixCatalogOrdering();
        List<String> selected = PunchyTuningConfig.getSelectedResourcePackIdsHighToLow();
        int priority = selected.size();
        for (String packId : selected) {
            String optionId = PunchyTuningConfig.normalizeMixOption(packId);
            if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
            ordering.selectedOptionIdsByPriority.add(optionId);
            ordering.optionPriorities.putIfAbsent(optionId, priority--);
        }
        return ordering;
    }

    private static void registerAnimationOnlyOptions(MixCatalog catalog, MixCatalogOrdering ordering, class_3300 resourceManager) {
        if (catalog == null || ordering == null || resourceManager == null) {
            return;
        }
        Map<String, LinkedHashMap<String, AnimationClip>> variants = PunchyAnimationLoader.loadAllWithPackVariants(resourceManager);
        if (variants.isEmpty()) {
            return;
        }
        for (MixSlotDef slot : catalog.slots.values()) {
            Set<String> clipNames;
            if (slot == null || (clipNames = PunchyTuningConfig.collectReferencedClipNamesForSlot(slot)).isEmpty()) continue;
            for (String clipName : clipNames) {
                LinkedHashMap<String, AnimationClip> clipVariants = variants.get(clipName);
                if (clipVariants == null || clipVariants.isEmpty()) continue;
                for (String optionId : clipVariants.keySet()) {
                    String normalizedOption = PunchyTuningConfig.normalizeMixOption(optionId);
                    if (normalizedOption == null || normalizedOption.isBlank()) continue;
                    PunchyTuningConfig.registerMixSlotClipVariant(slot, normalizedOption, clipName);
                    if (MIX_DEFAULT_OPTION_ID.equals(normalizedOption) || !MIX_PUNCHY_OPTION_IDS.isEmpty() && !MIX_PUNCHY_OPTION_IDS.contains(normalizedOption)) continue;
                    PunchyTuningConfig.registerMixSlotOption(slot, normalizedOption);
                    ordering.optionPriorities.putIfAbsent(normalizedOption, ordering.optionPriorities.size());
                }
            }
        }
    }

    private static void registerMixSlotClipVariant(MixSlotDef slot, String optionId, String clipName) {
        if (slot == null || optionId == null || optionId.isBlank() || clipName == null) {
            return;
        }
        String normalizedClip = clipName.trim().toLowerCase(Locale.ROOT);
        if (normalizedClip.isBlank()) {
            return;
        }
        LinkedHashSet names = slot.optionClipNames.computeIfAbsent(optionId, ignored -> new LinkedHashSet());
        names.add(normalizedClip);
    }

    private static Set<String> collectReferencedClipNamesForSlot(MixSlotDef slot) {
        if (slot == null) {
            return Set.of();
        }
        LinkedHashSet<String> names = new LinkedHashSet<String>();
        PunchyTuningConfig.collectPayloadClipNames(names, PunchyTuningConfig.baselinePayloadForSlot(slot));
        for (MixSlotPayload mixSlotPayload : slot.optionPayloads.values()) {
            PunchyTuningConfig.collectPayloadClipNames(names, mixSlotPayload);
        }
        if (slot.optionSpecificPayloads != null && !slot.optionSpecificPayloads.isEmpty()) {
            for (Map map : slot.optionSpecificPayloads.values()) {
                if (map == null || map.isEmpty()) continue;
                for (MixSlotPayload payload : map.values()) {
                    PunchyTuningConfig.collectPayloadClipNames(names, payload);
                }
            }
        }
        PunchyTuningConfig.collectPassiveDefaultClipNamesForSlot(slot, names);
        return names;
    }

    private static void collectPassiveDefaultClipNamesForSlot(MixSlotDef slot, Set<String> names) {
        if (slot == null || names == null) {
            return;
        }
        names.addAll(GENERIC_PASSIVE_CLIP_NAMES);
        if (slot.kind == VanillaFirstPersonItemLayer.ToolKind.SPEAR) {
            names.addAll(SPEAR_MIX_CLIP_NAMES);
            return;
        }
        if (slot.kind == VanillaFirstPersonItemLayer.ToolKind.SWORD) {
            names.addAll(SWORD_PASSIVE_CLIP_NAMES);
        }
    }

    private static void collectPayloadClipNames(Set<String> out, MixSlotPayload payload) {
        if (out == null || payload == null) {
            return;
        }
        PunchyTuningConfig.collectEntryClipNames(out, PunchyTuningConfig.payloadCustomAnimation(payload));
        PunchyTuningConfig.collectEntryClipNames(out, PunchyTuningConfig.payloadCustomItemAnimations(payload));
    }

    private static void collectEntryClipNames(Set<String> out, List<CustomAnimationEntry> entries) {
        if (out == null || entries == null || entries.isEmpty()) {
            return;
        }
        for (CustomAnimationEntry entry : entries) {
            if (entry == null) continue;
            PunchyTuningConfig.addClipName(out, entry.name);
            PunchyTuningConfig.addClipName(out, entry.animation);
            PunchyTuningConfig.addClipName(out, entry.start);
            PunchyTuningConfig.addClipName(out, entry.looping);
            PunchyTuningConfig.addClipName(out, entry.end);
            PunchyTuningConfig.addClipName(out, entry.keep_looping);
            PunchyTuningConfig.addClipName(out, entry.stage_1);
            PunchyTuningConfig.addClipName(out, entry.stage_2);
            PunchyTuningConfig.addClipName(out, entry.stage_3);
            PunchyTuningConfig.addClipName(out, entry.var_1);
            PunchyTuningConfig.addClipName(out, entry.var_2);
            PunchyTuningConfig.addClipName(out, entry.var_3);
            PunchyTuningConfig.addClipName(out, entry.critical_hit);
        }
    }

    private static void addClipName(Set<String> out, String clipName) {
        if (out == null || clipName == null) {
            return;
        }
        String normalized = clipName.trim();
        if (!normalized.isBlank()) {
            out.add(normalized);
        }
    }

    private static void registerMixSlotPayload(MixSlotDef slot, String optionId, MixSlotPayload payload) {
        if (slot == null || optionId == null || optionId.isBlank() || payload == null) {
            return;
        }
        MixSlotPayload existing = slot.optionPayloads.get(optionId);
        slot.optionPayloads.put(optionId, PunchyTuningConfig.mergeMixSlotPayload(existing, payload));
    }

    private static void registerMixSlotSpecificPayload(MixSlotDef slot, String optionId, class_2960 itemId, MixSlotPayload payload) {
        if (slot == null || optionId == null || optionId.isBlank() || itemId == null || payload == null) {
            return;
        }
        LinkedHashMap items = slot.optionSpecificPayloads.computeIfAbsent(optionId, key -> new LinkedHashMap());
        MixSlotPayload existing = (MixSlotPayload)items.get(itemId);
        items.put(itemId, PunchyTuningConfig.mergeMixSlotPayload(existing, payload));
    }

    private static MixSlotPayload mergeMixSlotPayload(MixSlotPayload base, MixSlotPayload overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            MixSlotPayload copy = new MixSlotPayload();
            copy.kind = overlay.kind;
            copy.groupEntry = PunchyTuningConfig.copyGroupEntry(overlay.groupEntry);
            copy.emptyHandEntry = PunchyTuningConfig.copyEmptyHandEntry(overlay.emptyHandEntry);
            copy.specificEntry = PunchyTuningConfig.copySpecificEntry(overlay.specificEntry);
            return copy;
        }
        MixSlotPayload merged = new MixSlotPayload();
        merged.kind = overlay.kind != null ? overlay.kind : base.kind;
        merged.groupEntry = PunchyTuningConfig.mergeGroupEntry(PunchyTuningConfig.copyGroupEntry(base.groupEntry), PunchyTuningConfig.copyGroupEntry(overlay.groupEntry));
        merged.emptyHandEntry = PunchyTuningConfig.mergeEmptyHand(PunchyTuningConfig.copyEmptyHandEntry(base.emptyHandEntry), PunchyTuningConfig.copyEmptyHandEntry(overlay.emptyHandEntry));
        merged.specificEntry = PunchyTuningConfig.mergeSpecificEntry(PunchyTuningConfig.copySpecificEntry(base.specificEntry), PunchyTuningConfig.copySpecificEntry(overlay.specificEntry));
        return merged;
    }

    private static boolean hasAnimationPayload(List<CustomAnimationEntry> customAnimation, List<CustomAnimationEntry> customItemAnimations) {
        return customAnimation != null && !customAnimation.isEmpty() || customItemAnimations != null && !customItemAnimations.isEmpty();
    }

    private static boolean hasMixPayload(GroupEntry entry) {
        return entry != null && (PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations) || entry.transforms != null || entry.armTransforms != null || entry.physics != null && !entry.physics.isJsonNull() || entry.disableSprintLateralTilt != null || entry.keepArmTuning != null || entry.keepTriggerArmTuning != null || entry.keepArmTuningL != null || entry.keepArmTuningR != null || PunchyTuningConfig.readCompatParticleEffects(entry) != null || entry.betterCombat != null && !entry.betterCombat.isEmpty());
    }

    private static boolean hasAnimationMixPayload(GroupEntry entry) {
        return entry != null && PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations);
    }

    private static boolean hasMixPayload(EmptyHandEntry entry) {
        return entry != null && (PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations) || entry.transforms != null || entry.armTransforms != null || entry.physics != null && !entry.physics.isJsonNull() || entry.disableSprintLateralTilt != null || entry.keepArmTuning != null || entry.keepTriggerArmTuning != null || entry.keepArmTuningL != null || entry.keepArmTuningR != null || entry.betterCombat != null && !entry.betterCombat.isEmpty());
    }

    private static boolean hasAnimationMixPayload(EmptyHandEntry entry) {
        return entry != null && PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations);
    }

    private static boolean hasMixPayload(SpecificEntry entry) {
        return entry != null && (PunchyTuningConfig.hasAnimationPayload(entry.customAnimation, entry.customItemAnimations) || entry.kind != null || entry.armKind != null || entry.renamed != null || entry.model != null || entry.customModelData != null || entry.physics != null && !entry.physics.isJsonNull() || entry.disableSprintLateralTilt != null || entry.keepArmTuning != null || entry.keepTriggerArmTuning != null || entry.keepArmTuningL != null || entry.keepArmTuningR != null || entry.animation != null && !entry.animation.isEmpty() || entry.transforms != null || entry.armTransforms != null || PunchyTuningConfig.readCompatParticleEffects(entry) != null || entry.betterCombat != null && !entry.betterCombat.isEmpty());
    }

    private static boolean hasAnimationMixPayload(SpecificEntry entry) {
        return PunchyTuningConfig.hasMixPayload(entry);
    }

    private static List<CustomAnimationEntry> payloadCustomAnimation(MixSlotPayload payload) {
        if (payload == null) {
            return null;
        }
        if (payload.groupEntry != null) {
            return payload.groupEntry.customAnimation;
        }
        if (payload.emptyHandEntry != null) {
            return payload.emptyHandEntry.customAnimation;
        }
        if (payload.specificEntry != null) {
            return payload.specificEntry.customAnimation;
        }
        return null;
    }

    private static List<CustomAnimationEntry> payloadCustomItemAnimations(MixSlotPayload payload) {
        if (payload == null) {
            return null;
        }
        if (payload.groupEntry != null) {
            return payload.groupEntry.customItemAnimations;
        }
        if (payload.emptyHandEntry != null) {
            return payload.emptyHandEntry.customItemAnimations;
        }
        if (payload.specificEntry != null) {
            return payload.specificEntry.customItemAnimations;
        }
        return null;
    }

    private static void setPayloadCustomAnimation(MixSlotPayload payload, List<CustomAnimationEntry> entries) {
        if (payload == null) {
            return;
        }
        if (payload.groupEntry != null) {
            payload.groupEntry.customAnimation = entries;
        } else if (payload.emptyHandEntry != null) {
            payload.emptyHandEntry.customAnimation = entries;
        } else if (payload.specificEntry != null) {
            payload.specificEntry.customAnimation = entries;
        }
    }

    private static void setPayloadCustomItemAnimations(MixSlotPayload payload, List<CustomAnimationEntry> entries) {
        if (payload == null) {
            return;
        }
        if (payload.groupEntry != null) {
            payload.groupEntry.customItemAnimations = entries;
        } else if (payload.emptyHandEntry != null) {
            payload.emptyHandEntry.customItemAnimations = entries;
        } else if (payload.specificEntry != null) {
            payload.specificEntry.customItemAnimations = entries;
        }
    }

    private static MixSlotPayload payloadFromGroup(GroupEntry entry, VanillaFirstPersonItemLayer.ToolKind kind, String mixPackOption) {
        MixSlotPayload payload = new MixSlotPayload();
        payload.kind = kind;
        payload.groupEntry = PunchyTuningConfig.copyGroupEntry(entry);
        if (payload.groupEntry != null) {
            payload.groupEntry.customAnimation = PunchyTuningConfig.copyCustomAnimationList(entry.customAnimation, mixPackOption);
            payload.groupEntry.customItemAnimations = PunchyTuningConfig.copyCustomAnimationList(entry.customItemAnimations, mixPackOption);
        }
        return payload;
    }

    private static MixSlotPayload payloadFromEmptyHand(EmptyHandEntry entry, VanillaFirstPersonItemLayer.ToolKind kind, String mixPackOption) {
        MixSlotPayload payload = new MixSlotPayload();
        payload.kind = kind;
        payload.emptyHandEntry = PunchyTuningConfig.copyEmptyHandEntry(entry);
        if (payload.emptyHandEntry != null) {
            payload.emptyHandEntry.customAnimation = PunchyTuningConfig.copyCustomAnimationList(entry.customAnimation, mixPackOption);
            payload.emptyHandEntry.customItemAnimations = PunchyTuningConfig.copyCustomAnimationList(entry.customItemAnimations, mixPackOption);
        }
        return payload;
    }

    private static MixSlotPayload payloadFromSpecific(SpecificEntry entry, VanillaFirstPersonItemLayer.ToolKind kind, String mixPackOption) {
        MixSlotPayload payload = new MixSlotPayload();
        payload.kind = kind;
        payload.specificEntry = PunchyTuningConfig.copySpecificEntry(entry);
        if (payload.specificEntry != null) {
            payload.specificEntry.customAnimation = PunchyTuningConfig.copyCustomAnimationList(entry.customAnimation, mixPackOption);
            payload.specificEntry.customItemAnimations = PunchyTuningConfig.copyCustomAnimationList(entry.customItemAnimations, mixPackOption);
        }
        return payload;
    }

    private static List<CustomAnimationEntry> copyCustomAnimationList(List<CustomAnimationEntry> input, String mixPackOption) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        ArrayList<CustomAnimationEntry> copy = new ArrayList<CustomAnimationEntry>(input.size());
        String normalizedOption = PunchyTuningConfig.normalizeMixOption(mixPackOption);
        for (CustomAnimationEntry entry : input) {
            if (entry == null) continue;
            CustomAnimationEntry cloned = (CustomAnimationEntry)GSON.fromJson(GSON.toJsonTree((Object)entry), CustomAnimationEntry.class);
            cloned.mix_pack_option = normalizedOption;
            copy.add(cloned);
        }
        return copy.isEmpty() ? null : copy;
    }

    private static String summarizeEntryTypes(List<CustomAnimationEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "[]";
        }
        LinkedHashSet<String> types = new LinkedHashSet<String>();
        for (CustomAnimationEntry entry : entries) {
            String type = PunchyTuningConfig.normalizeEntryType(entry == null ? null : entry.type);
            if (type == null) continue;
            types.add(type);
        }
        return types.toString();
    }

    private static String summarizeSideSet(SideSet set) {
        if (set == null) {
            return "none";
        }
        return "R=" + (set.right != null) + ",L=" + (set.left != null);
    }

    private static String summarizeGroupEntry(GroupEntry entry) {
        if (entry == null) {
            return "null";
        }
        List<String> particles = PunchyTuningConfig.readCompatParticleEffects(entry);
        return "transforms{" + PunchyTuningConfig.summarizeSideSet(entry.transforms) + "} arm{" + PunchyTuningConfig.summarizeSideSet(entry.armTransforms) + "} physics=" + (entry.physics != null && !entry.physics.isJsonNull()) + " anim=" + (entry.customAnimation == null ? 0 : entry.customAnimation.size()) + PunchyTuningConfig.summarizeEntryTypes(entry.customAnimation) + " itemAnim=" + (entry.customItemAnimations == null ? 0 : entry.customItemAnimations.size()) + PunchyTuningConfig.summarizeEntryTypes(entry.customItemAnimations) + " particles=" + (particles == null ? 0 : particles.size()) + " betterCombat=" + (entry.betterCombat == null ? 0 : entry.betterCombat.size()) + " flags={disable=" + entry.disableSprintLateralTilt + ",keep=" + entry.keepArmTuning + ",keepMain=" + entry.keepTriggerArmTuning + ",keepL=" + entry.keepArmTuningL + ",keepR=" + entry.keepArmTuningR + "}";
    }

    private static String summarizeEmptyHandEntry(EmptyHandEntry entry) {
        if (entry == null) {
            return "null";
        }
        return "transforms{" + PunchyTuningConfig.summarizeSideSet(entry.transforms) + "} arm{" + PunchyTuningConfig.summarizeSideSet(entry.armTransforms) + "} physics=" + (entry.physics != null && !entry.physics.isJsonNull()) + " anim=" + (entry.customAnimation == null ? 0 : entry.customAnimation.size()) + PunchyTuningConfig.summarizeEntryTypes(entry.customAnimation) + " itemAnim=" + (entry.customItemAnimations == null ? 0 : entry.customItemAnimations.size()) + PunchyTuningConfig.summarizeEntryTypes(entry.customItemAnimations) + " betterCombat=" + (entry.betterCombat == null ? 0 : entry.betterCombat.size()) + " flags={disable=" + entry.disableSprintLateralTilt + ",keep=" + entry.keepArmTuning + ",keepMain=" + entry.keepTriggerArmTuning + ",keepL=" + entry.keepArmTuningL + ",keepR=" + entry.keepArmTuningR + "}";
    }

    private static String summarizeSpecificEntry(SpecificEntry entry) {
        if (entry == null) {
            return "null";
        }
        List<String> particles = PunchyTuningConfig.readCompatParticleEffects(entry);
        return "kind=" + entry.kind + " armKind=" + entry.armKind + " renamed=" + entry.renamed + " model=" + entry.model + " cmd=" + entry.customModelData + " transforms{" + PunchyTuningConfig.summarizeSideSet(entry.transforms) + "} arm{" + PunchyTuningConfig.summarizeSideSet(entry.armTransforms) + "} physics=" + (entry.physics != null && !entry.physics.isJsonNull()) + " anim=" + (entry.customAnimation == null ? 0 : entry.customAnimation.size()) + PunchyTuningConfig.summarizeEntryTypes(entry.customAnimation) + " itemAnim=" + (entry.customItemAnimations == null ? 0 : entry.customItemAnimations.size()) + PunchyTuningConfig.summarizeEntryTypes(entry.customItemAnimations) + " particles=" + (particles == null ? 0 : particles.size()) + " betterCombat=" + (entry.betterCombat == null ? 0 : entry.betterCombat.size()) + " flags={disable=" + entry.disableSprintLateralTilt + ",keep=" + entry.keepArmTuning + ",keepMain=" + entry.keepTriggerArmTuning + ",keepL=" + entry.keepArmTuningL + ",keepR=" + entry.keepArmTuningR + "}";
    }

    private static String summarizePayload(MixSlotPayload payload) {
        if (payload == null) {
            return "null";
        }
        if (payload.groupEntry != null) {
            return "kind=" + String.valueOf((Object)payload.kind) + " group{" + PunchyTuningConfig.summarizeGroupEntry(payload.groupEntry) + "}";
        }
        if (payload.emptyHandEntry != null) {
            return "kind=" + String.valueOf((Object)payload.kind) + " empty{" + PunchyTuningConfig.summarizeEmptyHandEntry(payload.emptyHandEntry) + "}";
        }
        if (payload.specificEntry != null) {
            return "kind=" + String.valueOf((Object)payload.kind) + " specific{" + PunchyTuningConfig.summarizeSpecificEntry(payload.specificEntry) + "}";
        }
        return "kind=" + String.valueOf((Object)payload.kind) + " emptyPayload=true";
    }

    private static String summarizeData(Data data) {
        int count;
        if (data == null) {
            return "null";
        }
        StringJoiner joiner = new StringJoiner("; ");
        if (data.emptyHand != null) {
            joiner.add("emptyHand{" + PunchyTuningConfig.summarizeEmptyHandEntry(data.emptyHand) + "}");
        }
        if (data.item != null && !data.item.isEmpty()) {
            count = 0;
            StringJoiner items = new StringJoiner(", ", "items[", "]");
            for (Map.Entry<String, Object> entry : data.item.entrySet()) {
                if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
                items.add(entry.getKey() + "{" + PunchyTuningConfig.summarizeGroupEntry((GroupEntry)entry.getValue()) + "}");
                if (++count < 6) continue;
                break;
            }
            joiner.add(items.toString());
        }
        if (data.itemSpecific != null && !data.itemSpecific.isEmpty()) {
            count = 0;
            StringJoiner specifics = new StringJoiner(", ", "specific[", "]");
            for (Map.Entry<String, Object> entry : data.itemSpecific.entrySet()) {
                if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
                specifics.add(entry.getKey() + "{" + PunchyTuningConfig.summarizeSpecificEntry((SpecificEntry)entry.getValue()) + "}");
                if (++count < 6) continue;
                break;
            }
            joiner.add(specifics.toString());
        }
        if (joiner.length() == 0) {
            return "empty";
        }
        return joiner.toString();
    }

    private static String prettySlotLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] parts = raw.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) continue;
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() <= 1) continue;
            builder.append(part.substring(1));
        }
        return builder.toString();
    }

    private static VanillaFirstPersonItemLayer.ToolKind parseToolKind(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return VanillaFirstPersonItemLayer.ToolKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static VanillaFirstPersonItemLayer.ToolKind resolveKindForItem(class_2960 itemId) {
        if (itemId == null) {
            return null;
        }
        class_1792 item = class_7923.field_41178.method_17966(itemId).orElse(null);
        if (item == null) {
            return null;
        }
        return PunchyTuningConfig.resolveMixKindForStack(new class_1799((class_1935)item));
    }

    private static VanillaFirstPersonItemLayer.ToolKind resolveMixKindForStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return VanillaFirstPersonItemLayer.ToolKind.INGREDIENT;
        }
        if (SpearStateMachine.isSpearStack(stack)) {
            return VanillaFirstPersonItemLayer.ToolKind.SPEAR;
        }
        return ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
    }

    private static String normalizeMixOption(String packId) {
        if (packId == null || packId.isBlank()) {
            return MIX_DEFAULT_OPTION_ID;
        }
        String normalized = packId.trim().toLowerCase(Locale.ROOT);
        if (MIX_DEFAULT_OPTION_ID.equals(normalized)) {
            return MIX_DEFAULT_OPTION_ID;
        }
        if (normalized.startsWith("pack:")) {
            normalized = normalized.substring("pack:".length());
        }
        if (PunchyTuningConfig.isPunchyDefaultPackId(normalized)) {
            return BUILTIN_PACK_OPTION_ID;
        }
        if ("vanilla".equals(normalized) || "mod_resources".equals(normalized)) {
            return MIX_DEFAULT_OPTION_ID;
        }
        return "pack:" + normalized;
    }

    private static String resolveSelectedOption(MixSlotDef slot) {
        if (slot == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        List<String> allowedOptions = PunchyTuningConfig.getMixPackSlotOptionIds(slot.slotId);
        String rawSelected = PunchyConfig.getAnimationPackSelection(slot.slotId);
        if (rawSelected == null || rawSelected.isBlank()) {
            if (slot.resourceOrderOption != null && allowedOptions.contains(slot.resourceOrderOption)) {
                return slot.resourceOrderOption;
            }
            for (String optionId : allowedOptions) {
                if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
                return optionId;
            }
            return MIX_DEFAULT_OPTION_ID;
        }
        String selected = PunchyTuningConfig.normalizeMixOption(rawSelected);
        if (MIX_DEFAULT_OPTION_ID.equals(selected)) {
            if (slot.resourceOrderOption != null && allowedOptions.contains(slot.resourceOrderOption)) {
                return slot.resourceOrderOption;
            }
            for (String optionId : allowedOptions) {
                if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
                return optionId;
            }
            return MIX_DEFAULT_OPTION_ID;
        }
        if (selected != null && allowedOptions.contains(selected)) {
            return selected;
        }
        if (slot.resourceOrderOption != null && allowedOptions.contains(slot.resourceOrderOption)) {
            return slot.resourceOrderOption;
        }
        for (String optionId : allowedOptions) {
            if (optionId == null || optionId.isBlank() || MIX_DEFAULT_OPTION_ID.equals(optionId)) continue;
            return optionId;
        }
        return MIX_DEFAULT_OPTION_ID;
    }

    private static String resolveSelectedTypeOption(MixSlotDef slot, String normalizedType, String selectedMain) {
        String selected;
        if (slot == null || normalizedType == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        MixSlotPayload baselinePayload = PunchyTuningConfig.baselinePayloadForSlot(slot);
        boolean baselineHasType = PunchyTuningConfig.payloadHasType(baselinePayload, normalizedType);
        boolean enforceGlobalMain = PunchyTuningConfig.isGlobalHandType(normalizedType);
        String rawSelected = PunchyConfig.getAnimationPackTypeSelection(slot.slotId, normalizedType);
        String string = selected = rawSelected == null || rawSelected.isBlank() ? null : PunchyTuningConfig.normalizeMixOption(rawSelected);
        if (enforceGlobalMain) {
            if (selected != null && !selected.isBlank() && !MIX_DEFAULT_OPTION_ID.equals(selected) && slot.optionOrder.contains(selected) && PunchyTuningConfig.optionDefinesTypeExactly(slot, selected, normalizedType)) {
                return selected;
            }
            if (selectedMain != null && !selectedMain.isBlank() && PunchyTuningConfig.optionDefinesTypeExactly(slot, selectedMain, normalizedType)) {
                return selectedMain;
            }
            String globalOption = PunchyTuningConfig.getGlobalMixPriorityOptionId();
            if (globalOption != null && !globalOption.isBlank()) {
                return globalOption;
            }
            return MIX_DEFAULT_OPTION_ID;
        }
        if (selected != null && !selected.isBlank()) {
            if (MIX_DEFAULT_OPTION_ID.equals(selected)) {
                if (baselineHasType) {
                    return MIX_DEFAULT_OPTION_ID;
                }
                return MIX_DEFAULT_OPTION_ID;
            }
            if (slot.optionOrder.contains(selected) && PunchyTuningConfig.optionSupportsType(slot, selected, normalizedType)) {
                return selected;
            }
            if (selectedMain != null && !selectedMain.isBlank() && !MIX_DEFAULT_OPTION_ID.equals(selectedMain)) {
                return selectedMain;
            }
            return baselineHasType ? MIX_DEFAULT_OPTION_ID : MIX_DEFAULT_OPTION_ID;
        }
        if (selectedMain != null && !selectedMain.isBlank() && !MIX_DEFAULT_OPTION_ID.equals(selectedMain) && PunchyTuningConfig.optionSupportsType(slot, selectedMain, normalizedType)) {
            return selectedMain;
        }
        String fallback = PunchyTuningConfig.resolveAnimationTypeFallbackOption(slot, normalizedType, selectedMain, baselineHasType, optionId -> null);
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (selectedMain != null && !selectedMain.isBlank() && !MIX_DEFAULT_OPTION_ID.equals(selectedMain)) {
            return selectedMain;
        }
        return MIX_DEFAULT_OPTION_ID;
    }

    private static String resolveSelectedAnimationTypeOption(MixSlotDef slot, String normalizedType, String selectedMain, List<CustomAnimationEntry> baselineEntries, Function<String, List<CustomAnimationEntry>> entriesForOption) {
        String selected;
        if (slot == null || normalizedType == null || entriesForOption == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        boolean baselineHasType = PunchyTuningConfig.listHasType(baselineEntries, normalizedType);
        boolean enforceGlobalMain = PunchyTuningConfig.isGlobalHandType(normalizedType);
        boolean mainHasType = selectedMain != null && !selectedMain.isBlank() && (PunchyTuningConfig.listHasType(entriesForOption.apply(selectedMain), normalizedType) || PunchyTuningConfig.optionSupportsType(slot, selectedMain, normalizedType));
        String rawSelected = PunchyConfig.getAnimationPackTypeSelection(slot.slotId, normalizedType);
        String string = selected = rawSelected == null || rawSelected.isBlank() ? null : PunchyTuningConfig.normalizeMixOption(rawSelected);
        if (enforceGlobalMain) {
            if (selected != null && !selected.isBlank() && !MIX_DEFAULT_OPTION_ID.equals(selected) && slot.optionOrder.contains(selected) && PunchyTuningConfig.listHasExactType(entriesForOption.apply(selected), normalizedType)) {
                return selected;
            }
            if (selectedMain != null && !selectedMain.isBlank() && PunchyTuningConfig.listHasExactType(entriesForOption.apply(selectedMain), normalizedType)) {
                return selectedMain;
            }
            String globalOption = PunchyTuningConfig.getGlobalMixPriorityOptionId();
            if (globalOption != null && !globalOption.isBlank()) {
                return globalOption;
            }
            return MIX_DEFAULT_OPTION_ID;
        }
        if (selected != null && !selected.isBlank()) {
            if (MIX_DEFAULT_OPTION_ID.equals(selected)) {
                if (baselineHasType) {
                    return MIX_DEFAULT_OPTION_ID;
                }
                return MIX_DEFAULT_OPTION_ID;
            }
            if (slot.optionOrder.contains(selected) && (PunchyTuningConfig.listHasType(entriesForOption.apply(selected), normalizedType) || PunchyTuningConfig.optionSupportsType(slot, selected, normalizedType))) {
                return selected;
            }
            if (mainHasType && selectedMain != null && !selectedMain.isBlank()) {
                return selectedMain;
            }
            return baselineHasType ? MIX_DEFAULT_OPTION_ID : MIX_DEFAULT_OPTION_ID;
        }
        if (mainHasType && selectedMain != null && !selectedMain.isBlank()) {
            return selectedMain;
        }
        return PunchyTuningConfig.resolveAnimationTypeFallbackOption(slot, normalizedType, selectedMain, baselineHasType, entriesForOption);
    }

    private static String resolveAnimationTypeFallbackOption(MixSlotDef slot, String normalizedType, String selectedMain, boolean baselineHasType, Function<String, List<CustomAnimationEntry>> entriesForOption) {
        if (slot != null && normalizedType != null && entriesForOption != null) {
            String optionId;
            int i;
            ArrayList<String> orderedOptions = new ArrayList<String>(slot.optionOrder);
            for (i = 0; i < orderedOptions.size(); ++i) {
                optionId = (String)orderedOptions.get(i);
                if (optionId == null || optionId.isBlank() || optionId.equals(selectedMain) || !PunchyTuningConfig.listHasType(entriesForOption.apply(optionId), normalizedType)) continue;
                return optionId;
            }
            for (i = 0; i < orderedOptions.size(); ++i) {
                optionId = (String)orderedOptions.get(i);
                if (optionId == null || optionId.isBlank() || optionId.equals(selectedMain) || !PunchyTuningConfig.optionSupportsType(slot, optionId, normalizedType)) continue;
                return optionId;
            }
        }
        return baselineHasType ? MIX_DEFAULT_OPTION_ID : MIX_DEFAULT_OPTION_ID;
    }

    private static boolean optionSupportsType(MixSlotDef slot, String optionId, String normalizedType) {
        MixSlotPayload payload;
        MixSlotPayload baseline;
        if (slot == null || optionId == null || optionId.isBlank() || normalizedType == null || normalizedType.isBlank()) {
            return false;
        }
        return MIX_DEFAULT_OPTION_ID.equals(optionId) ? PunchyTuningConfig.payloadHasType(baseline = PunchyTuningConfig.baselinePayloadForSlot(slot), normalizedType) : PunchyTuningConfig.payloadHasType(payload = slot.optionPayloads.get(optionId), normalizedType);
    }

    private static boolean slotOptionHasTypeFallbackClip(MixSlotDef slot, String optionId, String normalizedType) {
        if (slot == null || optionId == null || optionId.isBlank() || normalizedType == null || normalizedType.isBlank()) {
            return false;
        }
        List<String> aliases = MIX_TYPE_CLIP_ALIASES.get(normalizedType);
        if (aliases == null || aliases.isEmpty()) {
            return false;
        }
        Set names = slot.optionClipNames.get(optionId);
        if (names == null || names.isEmpty()) {
            return false;
        }
        for (String alias : aliases) {
            if (alias == null || alias.isBlank() || !names.contains(alias.toLowerCase(Locale.ROOT))) continue;
            return true;
        }
        return false;
    }

    private static String normalizeEntryType(String entryType) {
        if (entryType == null || entryType.isBlank()) {
            return null;
        }
        return entryType.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isGlobalHandType(String normalizedType) {
        return false;
    }

    private static String normalizeEntryTypeFamily(String entryType) {
        String normalized = PunchyTuningConfig.normalizeEntryType(entryType);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        String compact = normalized.replace("_", "").replace("-", "").replace(" ", "");
        if ("mining".equals(compact) || "miningv2".equals(compact)) {
            return "mining";
        }
        return compact;
    }

    private static boolean isSameEntryTypeFamily(String leftType, String rightType) {
        String leftFamily = PunchyTuningConfig.normalizeEntryTypeFamily(leftType);
        String rightFamily = PunchyTuningConfig.normalizeEntryTypeFamily(rightType);
        return leftFamily != null && leftFamily.equals(rightFamily);
    }

    private static void collectEntryTypes(Set<String> out, List<CustomAnimationEntry> entries) {
        if (out == null || entries == null || entries.isEmpty()) {
            return;
        }
        for (CustomAnimationEntry entry : entries) {
            String type = PunchyTuningConfig.normalizeEntryType(entry == null ? null : entry.type);
            if (type == null) continue;
            out.add(type);
        }
    }

    private static boolean payloadHasType(MixSlotPayload payload, String normalizedType) {
        if (payload == null || normalizedType == null) {
            return false;
        }
        return PunchyTuningConfig.listHasType(PunchyTuningConfig.payloadCustomAnimation(payload), normalizedType) || PunchyTuningConfig.listHasType(PunchyTuningConfig.payloadCustomItemAnimations(payload), normalizedType);
    }

    private static boolean listHasType(List<CustomAnimationEntry> entries, String normalizedType) {
        if (entries == null || entries.isEmpty() || normalizedType == null) {
            return false;
        }
        for (CustomAnimationEntry entry : entries) {
            if (!PunchyTuningConfig.isSameEntryTypeFamily(normalizedType, entry == null ? null : entry.type)) continue;
            return true;
        }
        return false;
    }

    private static List<CustomAnimationEntry> composeCustomAnimationByType(MixSlotDef slot, String selectedMain, List<CustomAnimationEntry> base, Function<String, List<CustomAnimationEntry>> entriesForOption) {
        if (slot == null || entriesForOption == null) {
            return null;
        }
        List<Object> safeBase = base == null ? List.of() : base;
        List<String> entryTypes = PunchyTuningConfig.getMixPackSlotEntryTypes(slot.slotId);
        if (entryTypes.isEmpty()) {
            return safeBase.isEmpty() ? null : new ArrayList<Object>(safeBase);
        }
        LinkedHashMap<String, List<CustomAnimationEntry>> overrides = new LinkedHashMap<String, List<CustomAnimationEntry>>();
        for (String type : entryTypes) {
            List<CustomAnimationEntry> source;
            List<CustomAnimationEntry> entriesForType;
            String selectedForType;
            String normalizedType = PunchyTuningConfig.normalizeEntryType(type);
            if (normalizedType == null || (selectedForType = PunchyTuningConfig.resolveSelectedAnimationTypeOption(slot, normalizedType, selectedMain, safeBase, entriesForOption)) == null || selectedForType.isBlank() || selectedForType.equals(selectedMain) || (entriesForType = PunchyTuningConfig.filterCustomAnimationByType(source = entriesForOption.apply(selectedForType), normalizedType)).isEmpty()) continue;
            overrides.put(normalizedType, entriesForType);
        }
        return PunchyTuningConfig.mergeCustomAnimationByType(safeBase, overrides);
    }

    private static MixSlotPayload composePayloadByType(MixSlotDef slot, String selectedMain, MixSlotPayload payload) {
        if (slot == null || payload == null) {
            return payload;
        }
        MixSlotPayload composed = PunchyTuningConfig.mergeMixSlotPayload(null, payload);
        PunchyTuningConfig.setPayloadCustomAnimation(composed, PunchyTuningConfig.composeCustomAnimationByType(slot, selectedMain, PunchyTuningConfig.payloadCustomAnimation(payload), optionId -> {
            MixSlotPayload source = MIX_DEFAULT_OPTION_ID.equals(optionId) ? PunchyTuningConfig.baselinePayloadForSlot(slot) : slot.optionPayloads.get(optionId);
            return PunchyTuningConfig.payloadCustomAnimation(source);
        }));
        PunchyTuningConfig.setPayloadCustomItemAnimations(composed, PunchyTuningConfig.composeCustomAnimationByType(slot, selectedMain, PunchyTuningConfig.payloadCustomItemAnimations(payload), optionId -> {
            MixSlotPayload source = MIX_DEFAULT_OPTION_ID.equals(optionId) ? PunchyTuningConfig.baselinePayloadForSlot(slot) : slot.optionPayloads.get(optionId);
            return PunchyTuningConfig.payloadCustomItemAnimations(source);
        }));
        PunchyTuningConfig.logTypeComposition(slot, selectedMain, PunchyTuningConfig.payloadCustomAnimation(composed), PunchyTuningConfig.payloadCustomItemAnimations(composed));
        return composed;
    }

    private static MixSlotPayload composeMergedSpecificPayloadByType(MixSlotDef hostSlot, String selectedMain, class_2960 itemId, MixSlotPayload payload) {
        if (hostSlot == null || itemId == null || payload == null) {
            return payload;
        }
        MixSlotPayload composed = PunchyTuningConfig.mergeMixSlotPayload(null, payload);
        PunchyTuningConfig.setPayloadCustomAnimation(composed, PunchyTuningConfig.composeCustomAnimationByType(hostSlot, selectedMain, PunchyTuningConfig.payloadCustomAnimation(payload), optionId -> {
            Map optionPayloads = hostSlot.optionSpecificPayloads.get(optionId);
            MixSlotPayload source = MIX_DEFAULT_OPTION_ID.equals(optionId) ? PunchyTuningConfig.baselineSpecificPayloadForItem(itemId, hostSlot.kind) : (optionPayloads == null ? null : (MixSlotPayload)optionPayloads.get(itemId));
            return PunchyTuningConfig.payloadCustomAnimation(source);
        }));
        PunchyTuningConfig.setPayloadCustomItemAnimations(composed, PunchyTuningConfig.composeCustomAnimationByType(hostSlot, selectedMain, PunchyTuningConfig.payloadCustomItemAnimations(payload), optionId -> {
            Map optionPayloads = hostSlot.optionSpecificPayloads.get(optionId);
            MixSlotPayload source = MIX_DEFAULT_OPTION_ID.equals(optionId) ? PunchyTuningConfig.baselineSpecificPayloadForItem(itemId, hostSlot.kind) : (optionPayloads == null ? null : (MixSlotPayload)optionPayloads.get(itemId));
            return PunchyTuningConfig.payloadCustomItemAnimations(source);
        }));
        PunchyTuningConfig.logTypeComposition(hostSlot, selectedMain, PunchyTuningConfig.payloadCustomAnimation(composed), PunchyTuningConfig.payloadCustomItemAnimations(composed));
        return composed;
    }

    private static void logTypeComposition(MixSlotDef slot, String selectedMain, List<CustomAnimationEntry> customAnimation, List<CustomAnimationEntry> customItemAnimations) {
        if (slot == null) {
            return;
        }
        for (String type : PunchyTuningConfig.getMixPackSlotEntryTypes(slot.slotId)) {
            String selectedType;
            String normalizedType = PunchyTuningConfig.normalizeEntryType(type);
            if (normalizedType == null || (selectedType = PunchyTuningConfig.resolveSelectedTypeOption(slot, normalizedType, selectedMain)) == null || selectedType.isBlank() || selectedType.equals(selectedMain)) continue;
            int customAnimationCount = PunchyTuningConfig.filterCustomAnimationByType(customAnimation, normalizedType).size();
            int n = PunchyTuningConfig.filterCustomAnimationByType(customItemAnimations, normalizedType).size();
        }
    }

    private static List<CustomAnimationEntry> filterCustomAnimationByType(List<CustomAnimationEntry> entries, String normalizedType) {
        if (entries == null || entries.isEmpty() || normalizedType == null) {
            return List.of();
        }
        ArrayList<CustomAnimationEntry> result = new ArrayList<CustomAnimationEntry>();
        for (CustomAnimationEntry entry : entries) {
            if (!PunchyTuningConfig.isSameEntryTypeFamily(normalizedType, entry == null ? null : entry.type)) continue;
            result.add(entry);
        }
        return result;
    }

    private static List<CustomAnimationEntry> mergeCustomAnimationByType(List<CustomAnimationEntry> base, Map<String, List<CustomAnimationEntry>> overrides) {
        List<Object> safeBase;
        List<Object> list = safeBase = base == null ? List.of() : base;
        if (overrides == null || overrides.isEmpty()) {
            return safeBase.isEmpty() ? null : new ArrayList<Object>(safeBase);
        }
        ArrayList<CustomAnimationEntry> result = new ArrayList<CustomAnimationEntry>();
        HashSet<String> appliedTypes = new HashSet<String>();
        for (CustomAnimationEntry customAnimationEntry : safeBase) {
            String type = PunchyTuningConfig.normalizeEntryType(customAnimationEntry == null ? null : customAnimationEntry.type);
            if (type != null && overrides.containsKey(type)) {
                if (appliedTypes.contains(type)) continue;
                List<CustomAnimationEntry> replacement = overrides.get(type);
                if (replacement != null && !replacement.isEmpty()) {
                    result.addAll(replacement);
                }
                appliedTypes.add(type);
                continue;
            }
            result.add(customAnimationEntry);
        }
        for (Map.Entry entry : overrides.entrySet()) {
            List replacement;
            if (entry == null || entry.getKey() == null || appliedTypes.contains(entry.getKey()) || (replacement = (List)entry.getValue()) == null || replacement.isEmpty()) continue;
            result.addAll(replacement);
        }
        return result.isEmpty() ? null : result;
    }

    private static String customAnimationMergeKey(CustomAnimationEntry entry) {
        String familyType = PunchyTuningConfig.normalizeEntryTypeFamily(entry == null ? null : entry.type);
        return familyType != null ? familyType : "__untyped__";
    }

    private static List<CustomAnimationEntry> mergeCustomAnimationEntryLists(List<CustomAnimationEntry> base, List<CustomAnimationEntry> overlay) {
        List<Object> safeBase;
        List<Object> list = safeBase = base == null ? List.of() : base;
        if (overlay == null || overlay.isEmpty()) {
            return safeBase.isEmpty() ? null : new ArrayList<Object>(safeBase);
        }
        LinkedHashMap<String, List> overlayGroups = new LinkedHashMap<String, List>();
        for (CustomAnimationEntry entry : overlay) {
            String key = PunchyTuningConfig.customAnimationMergeKey(entry);
            overlayGroups.computeIfAbsent(key, ignored -> new ArrayList()).add(entry);
        }
        ArrayList<CustomAnimationEntry> result = new ArrayList<CustomAnimationEntry>();
        HashSet<String> applied = new HashSet<String>();
        for (CustomAnimationEntry customAnimationEntry : safeBase) {
            String key = PunchyTuningConfig.customAnimationMergeKey(customAnimationEntry);
            if (overlayGroups.containsKey(key)) {
                if (!applied.add(key)) continue;
                result.addAll((Collection)overlayGroups.get(key));
                continue;
            }
            result.add(customAnimationEntry);
        }
        for (Map.Entry entry : overlayGroups.entrySet()) {
            if (entry == null || entry.getKey() == null || !applied.add((String)entry.getKey())) continue;
            result.addAll((Collection)entry.getValue());
        }
        return result.isEmpty() ? null : result;
    }

    private static List<CustomAnimationEntry> mergeCustomAnimationEntryListsPreferBase(List<CustomAnimationEntry> base, List<CustomAnimationEntry> overlay) {
        List<Object> safeBase;
        List<Object> list = safeBase = base == null ? List.of() : base;
        if (overlay == null || overlay.isEmpty()) {
            return safeBase.isEmpty() ? null : new ArrayList<Object>(safeBase);
        }
        if (safeBase.isEmpty()) {
            return new ArrayList<CustomAnimationEntry>(overlay);
        }
        LinkedHashSet<String> existing = new LinkedHashSet<String>();
        for (CustomAnimationEntry customAnimationEntry : safeBase) {
            existing.add(PunchyTuningConfig.customAnimationMergeKey(customAnimationEntry));
        }
        ArrayList<Object> result = new ArrayList<Object>(safeBase);
        for (CustomAnimationEntry entry : overlay) {
            String key;
            if (entry == null || existing.contains(key = PunchyTuningConfig.customAnimationMergeKey(entry))) continue;
            result.add(entry);
            existing.add(key);
        }
        return result.isEmpty() ? null : result;
    }

    private static boolean isSpecificSlotForced(MixSlotDef slot) {
        if (slot == null || slot.type != MixSlotType.SPECIFIC) {
            return false;
        }
        return PunchyConfig.isMixSpecificForceOverride(slot.slotId);
    }

    private static boolean shouldAutoUseSpecificSlot(MixSlotDef slot) {
        return slot != null && slot.type == MixSlotType.SPECIFIC && !PunchyTuningConfig.isSpecificSlotForced(slot) && slot.optionOrder.size() == 1 && slot.resourceOrderOption != null && slot.optionOrder.contains(slot.resourceOrderOption);
    }

    private static String resolveInheritedKindOption(MixSlotDef slot) {
        if (slot == null) {
            return null;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = slot.kind;
        if (kind == null && slot.itemId != null) {
            kind = PunchyTuningConfig.resolveKindForItem(slot.itemId);
        }
        if (kind == null) {
            return null;
        }
        String kindSlotId = "kind:" + kind.name().toLowerCase(Locale.ROOT);
        MixSlotDef kindSlot = PunchyTuningConfig.MIX_CATALOG.slots.get(kindSlotId);
        if (kindSlot == null) {
            return MIX_DEFAULT_OPTION_ID;
        }
        return PunchyTuningConfig.resolveSelectedOption(kindSlot);
    }

    private static void applyMixSelections() {
        MixSlotPayload payload;
        String selected;
        if (MIX_CATALOG == null || PunchyTuningConfig.MIX_CATALOG.slots.isEmpty()) {
            PunchyTuningConfig.writeMergedAnimationSelectionSnapshot();
            return;
        }
        for (MixSlotDef slot : PunchyTuningConfig.MIX_CATALOG.slots.values()) {
            if (slot == null || slot.type != MixSlotType.KIND || slot.mergedSpecificItems.isEmpty()) continue;
            for (class_2960 itemId : slot.mergedSpecificItems) {
                if (itemId == null) continue;
                PunchyTuningConfig.clearSpecificMixState(itemId);
            }
        }
        for (MixSlotDef slot : PunchyTuningConfig.MIX_CATALOG.slots.values()) {
            if (slot == null || slot.type != MixSlotType.SPECIFIC || slot.itemId == null || PunchyTuningConfig.isSpecificSlotForced(slot) || PunchyTuningConfig.shouldAutoUseSpecificSlot(slot)) continue;
            PunchyTuningConfig.clearSpecificMixState(slot.itemId);
        }
        for (MixSlotDef slot : PunchyTuningConfig.MIX_CATALOG.slots.values()) {
            if (slot == null || slot.type != MixSlotType.KIND) continue;
            selected = PunchyTuningConfig.resolveSelectedOption(slot);
            payload = PunchyTuningConfig.resolveSelectedPayload(slot, selected);
            MixSlotPayload composedPayload = PunchyTuningConfig.composePayloadByType(slot, selected, payload);
            PunchyTuningConfig.applyMixSlotPayload(slot, composedPayload);
            if (slot.mergedSpecificItems.isEmpty()) continue;
            for (class_2960 itemId : slot.mergedSpecificItems) {
                if (itemId == null) continue;
                MixSlotPayload specificPayload = PunchyTuningConfig.resolveSelectedSpecificPayload(slot, selected, itemId, slot.kind);
                specificPayload = PunchyTuningConfig.composeMergedSpecificPayloadByType(slot, selected, itemId, specificPayload);
                PunchyTuningConfig.applySpecificPayload(itemId, slot.kind, specificPayload);
            }
        }
        for (MixSlotDef slot : PunchyTuningConfig.MIX_CATALOG.slots.values()) {
            if (slot == null || slot.type != MixSlotType.SPECIFIC || !PunchyTuningConfig.isSpecificSlotForced(slot) && !PunchyTuningConfig.shouldAutoUseSpecificSlot(slot)) continue;
            selected = PunchyTuningConfig.resolveSelectedOption(slot);
            payload = PunchyTuningConfig.resolveSelectedPayload(slot, selected);
            payload = PunchyTuningConfig.composePayloadByType(slot, selected, payload);
            PunchyTuningConfig.applyMixSlotPayload(slot, payload);
        }
        PunchyTuningConfig.writeMergedAnimationSelectionSnapshot();
    }

    private static void writeMergedAnimationSelectionSnapshot() {
        try {
            Path outputDir = PunchyTuningConfig.getConfigDirectory().resolve("punchy");
            Files.createDirectories(outputDir, new FileAttribute[0]);
            Path output = outputDir.resolve(MERGED_ANIMATION_SELECTION_FILE);
            JsonObject root = new JsonObject();
            JsonArray selectedAnimations = new JsonArray();
            root.add("selected_animations", (JsonElement)selectedAnimations);
            LinkedHashMap exportedItemTypesByKindSlot = new LinkedHashMap();
            if (MIX_CATALOG != null && PunchyTuningConfig.MIX_CATALOG.slots != null && !PunchyTuningConfig.MIX_CATALOG.slots.isEmpty()) {
                JsonObject entry;
                JsonObject animations;
                for (MixSlotDef slot : PunchyTuningConfig.MIX_CATALOG.slots.values()) {
                    if (slot == null || slot.type != MixSlotType.KIND || slot.kind == null || (animations = PunchyTuningConfig.collectSelectedAnimationTypesForSlot(slot, null)).size() == 0) continue;
                    entry = new JsonObject();
                    entry.addProperty("kind", "item");
                    entry.addProperty("definition", slot.kind.name());
                    entry.add("animations", (JsonElement)animations);
                    selectedAnimations.add((JsonElement)entry);
                    LinkedHashSet<String> keys = new LinkedHashSet<String>();
                    for (Map.Entry animationEntry : animations.entrySet()) {
                        String normalized;
                        if (animationEntry == null || animationEntry.getKey() == null || (normalized = PunchyTuningConfig.normalizeEntryType((String)animationEntry.getKey())) == null || normalized.isBlank()) continue;
                        keys.add(normalized);
                    }
                    exportedItemTypesByKindSlot.put(slot.slotId, keys);
                }
                for (MixSlotDef slot : PunchyTuningConfig.MIX_CATALOG.slots.values()) {
                    if (slot == null || slot.type != MixSlotType.SPECIFIC || slot.itemId == null || (animations = PunchyTuningConfig.collectSelectedAnimationTypesForSlot(slot, null)).size() == 0) continue;
                    entry = new JsonObject();
                    entry.addProperty("kind", "itemSpecific");
                    entry.addProperty("definition", slot.itemId.toString());
                    entry.add("animations", (JsonElement)animations);
                    selectedAnimations.add((JsonElement)entry);
                }
            }
            try (FileWriter writer = new FileWriter(output.toFile());){
                GSON.toJson((JsonElement)root, (Appendable)writer);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void reapplyActiveProfileSpecificTransforms(Data activeProfileData) {
        if (activeProfileData == null || activeProfileData.itemSpecific == null || activeProfileData.itemSpecific.isEmpty()) {
            return;
        }
        Data overrides = new Data();
        overrides.itemSpecific = new HashMap<String, SpecificEntry>();
        for (Map.Entry<String, SpecificEntry> entry : activeProfileData.itemSpecific.entrySet()) {
            boolean hasMetadata;
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            SpecificEntry src = entry.getValue();
            boolean hasTransforms = PunchyTuningConfig.hasSideSetData(src.transforms);
            boolean hasArmTransforms = PunchyTuningConfig.hasSideSetData(src.armTransforms);
            boolean hasPhysics = src.physics != null && !src.physics.isJsonNull();
            boolean bl = hasMetadata = src.disableSprintLateralTilt != null || src.keepArmTuning != null || src.keepTriggerArmTuning != null || src.keepArmTuningL != null || src.keepArmTuningR != null;
            if (!hasTransforms && !hasArmTransforms && !hasPhysics && !hasMetadata) continue;
            SpecificEntry copy = new SpecificEntry();
            copy.kind = src.kind;
            copy.armKind = src.armKind;
            copy.customModelData = src.customModelData;
            copy.renamed = src.renamed;
            copy.model = src.model;
            copy.transforms = PunchyTuningConfig.copySideSet(src.transforms);
            copy.armTransforms = PunchyTuningConfig.copySideSet(src.armTransforms);
            copy.physics = PunchyTuningConfig.copyJson(src.physics);
            copy.disableSprintLateralTilt = src.disableSprintLateralTilt;
            copy.keepArmTuning = src.keepArmTuning;
            copy.keepTriggerArmTuning = src.keepTriggerArmTuning;
            copy.keepArmTuningL = src.keepArmTuningL;
            copy.keepArmTuningR = src.keepArmTuningR;
            overrides.itemSpecific.put(entry.getKey(), copy);
        }
        if (overrides.itemSpecific.isEmpty()) {
            return;
        }
        try {
            PunchyTuningConfig.applyData(overrides, false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static JsonObject collectSelectedAnimationTypesForSlot(MixSlotDef slot, Set<String> suppressedTypes) {
        JsonObject animations = new JsonObject();
        if (slot == null || slot.slotId == null || slot.slotId.isBlank()) {
            return animations;
        }
        List<String> types = PunchyTuningConfig.getMixPackSlotEntryTypes(slot.slotId);
        if (types == null || types.isEmpty()) {
            return animations;
        }
        for (String type : types) {
            String selectedPack;
            String normalizedType;
            String outputType = type == null ? null : type.trim();
            if (outputType == null || outputType.isBlank() || (normalizedType = PunchyTuningConfig.normalizeEntryType(type)) == null || normalizedType.isBlank() || suppressedTypes != null && suppressedTypes.contains(normalizedType) || (selectedPack = PunchyTuningConfig.getMixPackSelectedEntryTypeOption(slot.slotId, normalizedType)) == null || selectedPack.isBlank() || !PunchyTuningConfig.isGlobalHandType(normalizedType) && !PunchyTuningConfig.optionDefinesTypeExactly(slot, selectedPack, normalizedType)) continue;
            animations.addProperty(outputType, selectedPack);
        }
        return animations;
    }

    private static boolean optionDefinesTypeExactly(MixSlotDef slot, String optionId, String normalizedType) {
        MixSlotPayload payload;
        if (slot == null || optionId == null || optionId.isBlank() || normalizedType == null || normalizedType.isBlank()) {
            return false;
        }
        MixSlotPayload mixSlotPayload = payload = MIX_DEFAULT_OPTION_ID.equals(optionId) ? PunchyTuningConfig.baselinePayloadForSlot(slot) : slot.optionPayloads.get(optionId);
        if (payload == null) {
            return false;
        }
        return PunchyTuningConfig.listHasExactType(PunchyTuningConfig.payloadCustomAnimation(payload), normalizedType) || PunchyTuningConfig.listHasExactType(PunchyTuningConfig.payloadCustomItemAnimations(payload), normalizedType);
    }

    private static boolean listHasExactType(List<CustomAnimationEntry> entries, String normalizedType) {
        if (entries == null || entries.isEmpty() || normalizedType == null || normalizedType.isBlank()) {
            return false;
        }
        for (CustomAnimationEntry entry : entries) {
            if (!normalizedType.equals(PunchyTuningConfig.normalizeEntryType(entry == null ? null : entry.type))) continue;
            return true;
        }
        return false;
    }

    private static String summarizeEntryByType(List<CustomAnimationEntry> entries, String type) {
        if (entries == null || entries.isEmpty()) {
            return "none";
        }
        String normalizedType = PunchyTuningConfig.normalizeEntryType(type);
        for (CustomAnimationEntry entry : entries) {
            if (entry == null || !Objects.equals(normalizedType, PunchyTuningConfig.normalizeEntryType(entry.type))) continue;
            StringBuilder builder = new StringBuilder();
            builder.append("type=").append(entry.type);
            if (entry.name != null) {
                builder.append(",name=").append(entry.name);
            }
            if (entry.start != null) {
                builder.append(",start=").append(entry.start);
            }
            if (entry.looping != null) {
                builder.append(",looping=").append(entry.looping);
            }
            if (entry.end != null) {
                builder.append(",end=").append(entry.end);
            }
            if (entry.var_1 != null) {
                builder.append(",var_1=").append(entry.var_1);
            }
            if (entry.mix_pack_option != null) {
                builder.append(",mix=").append(entry.mix_pack_option);
            }
            return builder.toString();
        }
        return "missing";
    }

    private static List<BetterCombatEntry> extractDefaultBetterCombatFromDefaultAnimations(List<CustomAnimationEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        ArrayList<BetterCombatEntry> out = new ArrayList<BetterCombatEntry>();
        for (CustomAnimationEntry entry : entries) {
            BetterCombatEntry combo;
            if (entry != null && entry.betterCombat != null && !entry.betterCombat.isEmpty()) {
                for (BetterCombatEntry nested : entry.betterCombat) {
                    if (nested == null || !PunchyTuningConfig.isMeleeComboType(nested.type) || PunchyTuningConfig.allBlank(nested.attack_1, nested.attack_2, nested.attack_3, nested.attack_4, nested.attack_5, nested.attack_6)) continue;
                    out.add(nested);
                }
            }
            if (!PunchyTuningConfig.isMeleeComboType(entry == null ? null : entry.type) || (combo = PunchyTuningConfig.toBetterCombatEntry(entry)) == null) continue;
            out.add(combo);
        }
        return out.isEmpty() ? null : out;
    }

    private static List<CustomAnimationEntry> stripBetterCombatFromDefaultAnimations(List<CustomAnimationEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        ArrayList<CustomAnimationEntry> filtered = new ArrayList<CustomAnimationEntry>();
        for (CustomAnimationEntry entry : entries) {
            if (entry != null && entry.betterCombat != null && !entry.betterCombat.isEmpty() || PunchyTuningConfig.isMeleeComboType(entry == null ? null : entry.type)) continue;
            filtered.add(entry);
        }
        return filtered.isEmpty() ? null : filtered;
    }

    private static boolean isMeleeComboType(String type) {
        if (type == null) {
            return false;
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        return "meleecombo".equals(normalized);
    }

    private static BetterCombatEntry toBetterCombatEntry(CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        BetterCombatEntry out = new BetterCombatEntry();
        out.type = "melee_combo";
        out.attack_1 = PunchyTuningConfig.firstNonBlank(entry.attack_1, entry.var_1);
        out.attack_2 = PunchyTuningConfig.firstNonBlank(entry.attack_2, entry.var_2);
        out.attack_3 = PunchyTuningConfig.firstNonBlank(entry.attack_3, entry.var_3);
        out.attack_4 = PunchyTuningConfig.firstNonBlank(entry.attack_4, null);
        out.attack_5 = PunchyTuningConfig.firstNonBlank(entry.attack_5, null);
        out.attack_6 = PunchyTuningConfig.firstNonBlank(entry.attack_6, null);
        if (PunchyTuningConfig.allBlank(out.attack_1, out.attack_2, out.attack_3, out.attack_4, out.attack_5, out.attack_6)) {
            return null;
        }
        return out;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }

    private static boolean allBlank(String ... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) continue;
            return false;
        }
        return true;
    }

    private static String summarizeGroupTypeEntry(Data data, VanillaFirstPersonItemLayer.ToolKind kind, String type) {
        if (data == null || kind == null || type == null || data.item == null) {
            return "none";
        }
        GroupEntry group = data.item.get(kind.name());
        if (group == null) {
            return "group-missing";
        }
        String fromAnim = PunchyTuningConfig.summarizeEntryByType(group.customAnimation, type);
        String fromItemAnim = PunchyTuningConfig.summarizeEntryByType(group.customItemAnimations, type);
        return "anim={" + fromAnim + "} itemAnim={" + fromItemAnim + "}";
    }

    private static MixSlotPayload resolveSelectedPayload(MixSlotDef slot, String selected) {
        if (slot == null || selected == null || selected.isBlank() || MIX_DEFAULT_OPTION_ID.equals(selected)) {
            return PunchyTuningConfig.baselinePayloadForSlot(slot);
        }
        int selectedIndex = new ArrayList<String>(slot.optionOrder).indexOf(selected);
        if (selectedIndex >= 0) {
            MixSlotPayload merged = PunchyTuningConfig.mergeMixSlotPayload(null, PunchyTuningConfig.baselinePayloadForSlot(slot));
            ArrayList<String> orderedOptions = new ArrayList<String>(slot.optionOrder);
            for (int index = orderedOptions.size() - 1; index >= selectedIndex; --index) {
                String optionId = (String)orderedOptions.get(index);
                MixSlotPayload optionPayload = slot.optionPayloads.get(optionId);
                merged = PunchyTuningConfig.mergeMixSlotPayload(merged, optionPayload);
            }
            return merged;
        }
        MixSlotPayload baseline = PunchyTuningConfig.baselinePayloadForSlot(slot);
        if (baseline == null) {
            return null;
        }
        return PunchyTuningConfig.mergeMixSlotPayload(null, baseline);
    }

    private static MixSlotPayload resolveSelectedSpecificPayload(MixSlotDef slot, String selected, class_2960 itemId, VanillaFirstPersonItemLayer.ToolKind fallbackKind) {
        if (slot == null || itemId == null) {
            return null;
        }
        MixSlotPayload merged = PunchyTuningConfig.mergeMixSlotPayload(null, PunchyTuningConfig.baselineSpecificPayloadForItem(itemId, fallbackKind));
        if (selected == null || selected.isBlank() || MIX_DEFAULT_OPTION_ID.equals(selected)) {
            return merged;
        }
        ArrayList<String> orderedOptions = new ArrayList<String>(slot.optionOrder);
        int selectedIndex = orderedOptions.indexOf(selected);
        if (selectedIndex < 0) {
            return merged;
        }
        for (int index = orderedOptions.size() - 1; index >= selectedIndex; --index) {
            String optionId = (String)orderedOptions.get(index);
            Map optionPayloads = slot.optionSpecificPayloads.get(optionId);
            MixSlotPayload optionPayload = optionPayloads == null ? null : (MixSlotPayload)optionPayloads.get(itemId);
            merged = PunchyTuningConfig.mergeMixSlotPayload(merged, optionPayload);
        }
        return merged;
    }

    private static MixSlotPayload baselineSpecificPayloadForItem(class_2960 itemId, VanillaFirstPersonItemLayer.ToolKind fallbackKind) {
        if (itemId == null || DEFAULTS_RESOURCE_STATE == null || PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific == null || PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific.isEmpty()) {
            return null;
        }
        SpecificEntry entry = PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific.get(itemId.toString());
        if (entry == null) {
            for (Map.Entry<String, SpecificEntry> candidate : PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific.entrySet()) {
                if (candidate == null || candidate.getKey() == null || candidate.getValue() == null || !PunchyTuningConfig.specificKeyTargetsItem(candidate.getKey(), itemId)) continue;
                entry = candidate.getValue();
                break;
            }
        }
        if (entry == null) {
            return null;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.parseToolKind(entry.kind);
        if (kind == null) {
            kind = fallbackKind != null ? fallbackKind : PunchyTuningConfig.resolveKindForItem(itemId);
        }
        return PunchyTuningConfig.payloadFromSpecific(entry, kind, MIX_DEFAULT_OPTION_ID);
    }

    private static void applySpecificPayload(class_2960 itemId, VanillaFirstPersonItemLayer.ToolKind fallbackKind, MixSlotPayload payload) {
        SpecificEntry entry;
        if (itemId == null) {
            return;
        }
        PunchyTuningConfig.clearSpecificMixState(itemId);
        if (payload == null) {
            return;
        }
        SpecificEntry specificEntry = entry = payload.specificEntry != null ? PunchyTuningConfig.copySpecificEntry(payload.specificEntry) : null;
        if (entry == null) {
            return;
        }
        if ((entry.kind == null || entry.kind.isBlank()) && fallbackKind != null) {
            entry.kind = fallbackKind.name();
        }
        Data temp = new Data();
        temp.itemSpecific.put(itemId.toString(), entry);
        PunchyTuningConfig.applyData(temp, false);
    }

    private static MixSlotPayload baselinePayloadForSlot(MixSlotDef slot) {
        if (slot == null || DEFAULTS_RESOURCE_STATE == null) {
            return null;
        }
        if (slot.type == MixSlotType.KIND) {
            if (slot.kind == VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND && PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.emptyHand != null) {
                return PunchyTuningConfig.payloadFromEmptyHand(PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.emptyHand, VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND, MIX_DEFAULT_OPTION_ID);
            }
            if (PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.item == null || slot.kind == null) {
                return null;
            }
            GroupEntry entry = PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.item.get(slot.kind.name());
            if (entry == null) {
                return null;
            }
            return PunchyTuningConfig.payloadFromGroup(entry, slot.kind, MIX_DEFAULT_OPTION_ID);
        }
        if (slot.type == MixSlotType.SPECIFIC && slot.itemId != null && PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific != null) {
            SpecificEntry entry = PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific.get(slot.itemId.toString());
            if (entry == null) {
                for (Map.Entry<String, SpecificEntry> base : PunchyTuningConfig.DEFAULTS_RESOURCE_STATE.itemSpecific.entrySet()) {
                    if (base == null || base.getKey() == null || base.getValue() == null || !PunchyTuningConfig.specificKeyTargetsItem(base.getKey(), slot.itemId)) continue;
                    entry = base.getValue();
                    break;
                }
            }
            if (entry == null) {
                return null;
            }
            VanillaFirstPersonItemLayer.ToolKind kind = PunchyTuningConfig.parseToolKind(entry.kind);
            if (kind == null) {
                kind = slot.kind != null ? slot.kind : PunchyTuningConfig.resolveKindForItem(slot.itemId);
            }
            return PunchyTuningConfig.payloadFromSpecific(entry, kind, MIX_DEFAULT_OPTION_ID);
        }
        return null;
    }

    private static void applyMixSlotPayload(MixSlotDef slot, MixSlotPayload payload) {
        if (slot == null) {
            return;
        }
        if (slot.type == MixSlotType.KIND) {
            if (slot.kind == null) {
                return;
            }
            PunchyTuningConfig.resetKindMixState(slot.kind);
            if (payload == null) {
                return;
            }
            Data temp = new Data();
            if (slot.kind == VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND) {
                EmptyHandEntry emptyPayload;
                EmptyHandEntry emptyHandEntry = emptyPayload = payload.emptyHandEntry != null ? PunchyTuningConfig.copyEmptyHandEntry(payload.emptyHandEntry) : PunchyTuningConfig.emptyHandEntryFromGroup(payload.groupEntry);
                if (emptyPayload == null) {
                    return;
                }
                temp.emptyHand = emptyPayload;
            } else {
                if (payload.groupEntry == null) {
                    return;
                }
                temp.item.put(slot.kind.name(), PunchyTuningConfig.copyGroupEntry(payload.groupEntry));
            }
            PunchyTuningConfig.applyData(temp, false);
            return;
        }
        if (slot.type == MixSlotType.SPECIFIC) {
            SpecificEntry entry;
            if (slot.itemId == null) {
                return;
            }
            PunchyTuningConfig.clearSpecificMixState(slot.itemId);
            if (payload == null) {
                return;
            }
            SpecificEntry specificEntry = entry = payload.specificEntry != null ? PunchyTuningConfig.copySpecificEntry(payload.specificEntry) : null;
            if (entry == null) {
                return;
            }
            if ((entry.kind == null || entry.kind.isBlank()) && slot.kind != null) {
                entry.kind = slot.kind.name();
            }
            Data temp = new Data();
            temp.itemSpecific.put(slot.itemId.toString(), entry);
            PunchyTuningConfig.applyData(temp, false);
        }
    }

    private static EmptyHandEntry emptyHandEntryFromGroup(GroupEntry group) {
        if (group == null) {
            return null;
        }
        try {
            return (EmptyHandEntry)GSON.fromJson(GSON.toJsonTree((Object)group), EmptyHandEntry.class);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static void clearSpecificMixState(class_2960 itemId) {
        if (itemId == null) {
            return;
        }
        ToolTuning.clearSpecificOverride(itemId);
        ArmToolTuning.clearSpecificOverride(itemId);
    }

    private static void resetKindMixState(VanillaFirstPersonItemLayer.ToolKind kind) {
        if (kind == null) {
            return;
        }
        ToolTuning.setGroupAnimation(kind, null);
        ToolTuning.setGroupItemAnimation(kind, null);
    }

    private static void collectCompatProfiles(class_3300 manager, String searchPath, String namespace, String prefix, Set<String> result) {
        Map found = manager.method_14488(searchPath, loc -> loc.method_12836().equals(namespace) && loc.method_12832().startsWith(prefix) && loc.method_12832().endsWith(".json"));
        for (class_2960 id : found.keySet()) {
            String path = id.method_12832();
            String file = path.substring(prefix.length());
            if (file.endsWith(".json")) {
                file = file.substring(0, file.length() - 5);
            }
            if (file.isBlank()) continue;
            result.add(file.toLowerCase(Locale.ROOT));
        }
    }

    private static SideSet resolveGlobalArm(Data data) {
        if (data == null) {
            return null;
        }
        return data.globalArm;
    }

    private static SideSet resolveGlobalItem(Data data) {
        if (data == null) {
            return null;
        }
        return data.globalItem;
    }

    private static Data loadCompatProfile(class_3300 resourceManager, String profile) {
        String normalized = profile.endsWith(".json") ? profile.substring(0, profile.length() - 5) : profile;
        Data merged = null;
        Path compatDir = PunchyTuningConfig.getCompatDirectory();
        try {
            Files.createDirectories(compatDir, new FileAttribute[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
        Path filePath = compatDir.resolve(normalized + ".json");
        if (Files.exists(filePath, new LinkOption[0])) {
            try (FileReader reader = new FileReader(filePath.toFile());){
                Data local = (Data)GSON.fromJson((Reader)reader, Data.class);
                merged = PunchyTuningConfig.mergeData(merged, local);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return PunchyTuningConfig.finalizeCompatData(normalized, merged);
    }

    private static Data loadCompatProfileFromResource(class_3300 resourceManager, String namespace, String path, String profile) {
        return null;
    }

    private static List<class_3298> tryGetResourceStack(class_3300 resourceManager, class_2960 id) {
        if (resourceManager == null || id == null) {
            return null;
        }
        try {
            List stack = resourceManager.method_14489(id);
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            return new ArrayList<class_3298>(stack);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static Map<class_2960, List<class_3298>> tryListResourceStacks(class_3300 resourceManager, String searchPath) {
        if (resourceManager == null || searchPath == null || searchPath.isBlank()) {
            return null;
        }
        try {
            Map value = resourceManager.method_41265(searchPath, loc -> true);
            if (value == null || value.isEmpty()) {
                return null;
            }
            LinkedHashMap<class_2960, List<class_3298>> result = new LinkedHashMap<class_2960, List<class_3298>>();
            for (Map.Entry entry : value.entrySet()) {
                class_2960 id = (class_2960)entry.getKey();
                List list = (List)entry.getValue();
                if (id == null || list == null || list.isEmpty()) continue;
                result.put(id, new ArrayList(list));
            }
            return result.isEmpty() ? null : result;
        }
        catch (Exception exception) {
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static Data readCompatResource(class_3298 resource) {
        if (resource == null) {
            return null;
        }
        try (InputStream stream = resource.method_14482();){
            Data data;
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);){
                data = (Data)GSON.fromJson((Reader)reader, Data.class);
            }
            return data;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static Data copyData(Data input) {
        if (input == null) {
            return null;
        }
        return (Data)GSON.fromJson(GSON.toJsonTree((Object)input), Data.class);
    }

    private static GroupEntry copyGroupEntry(GroupEntry input) {
        if (input == null) {
            return null;
        }
        return (GroupEntry)GSON.fromJson(GSON.toJsonTree((Object)input), GroupEntry.class);
    }

    private static EmptyHandEntry copyEmptyHandEntry(EmptyHandEntry input) {
        if (input == null) {
            return null;
        }
        return (EmptyHandEntry)GSON.fromJson(GSON.toJsonTree((Object)input), EmptyHandEntry.class);
    }

    private static SpecificEntry copySpecificEntry(SpecificEntry input) {
        if (input == null) {
            return null;
        }
        return (SpecificEntry)GSON.fromJson(GSON.toJsonTree((Object)input), SpecificEntry.class);
    }

    private static Data mergeDataPreferBase(Data base, Data overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            return overlay;
        }
        base.globalItem = PunchyTuningConfig.mergeSideSetPreferBase(base.globalItem, overlay.globalItem);
        base.globalArm = PunchyTuningConfig.mergeSideSetPreferBase(base.globalArm, overlay.globalArm);
        base.default_animations = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.default_animations, overlay.default_animations);
        base.emptyHand = PunchyTuningConfig.mergeEmptyHandPreferBase(base.emptyHand, overlay.emptyHand);
        base.item = PunchyTuningConfig.mergeGroupEntryMapPreferBase(base.item, overlay.item);
        base.armMesh = PunchyTuningConfig.mergeSideSetMapPreferBase(base.armMesh, overlay.armMesh);
        base.chestProxy = PunchyTuningConfig.mergePoseMapPreferBase(base.chestProxy, overlay.chestProxy);
        base.fluidProxy = PunchyTuningConfig.mergePoseMapPreferBase(base.fluidProxy, overlay.fluidProxy);
        base.enchantParticleProxy = PunchyTuningConfig.mergePoseMapPreferBase(base.enchantParticleProxy, overlay.enchantParticleProxy);
        base.paddleProxy = PunchyTuningConfig.mergePoseMapPreferBase(base.paddleProxy, overlay.paddleProxy);
        base.fishingLineProxy = PunchyTuningConfig.mergePoseMapPreferBase(base.fishingLineProxy, overlay.fishingLineProxy);
        base.itemSpecific = PunchyTuningConfig.mergeSpecificMapPreferBase(base, base.itemSpecific, overlay.itemSpecific);
        base.customGroups = PunchyTuningConfig.mergeCustomGroupsPreferBase(base.customGroups, overlay.customGroups);
        return base;
    }

    private static SideSet mergeSideSetPreferBase(SideSet base, SideSet overlay) {
        if (overlay == null || overlay.right == null && overlay.left == null) {
            return base;
        }
        if (base == null) {
            base = new SideSet();
        }
        if (base.right == null && overlay.right != null) {
            base.right = overlay.right;
        }
        if (base.left == null && overlay.left != null) {
            base.left = overlay.left;
        }
        return base;
    }

    private static EmptyHandEntry mergeEmptyHandPreferBase(EmptyHandEntry base, EmptyHandEntry overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            base = new EmptyHandEntry();
        }
        base.transforms = PunchyTuningConfig.mergeSideSetPreferBase(base.transforms, overlay.transforms);
        if ((base.physics == null || base.physics.isJsonNull()) && overlay.physics != null && !overlay.physics.isJsonNull()) {
            base.physics = overlay.physics;
        }
        if (base.disableSprintLateralTilt == null) {
            base.disableSprintLateralTilt = overlay.disableSprintLateralTilt;
        }
        if (base.keepArmTuning == null) {
            base.keepArmTuning = overlay.keepArmTuning;
        }
        if (base.keepTriggerArmTuning == null) {
            base.keepTriggerArmTuning = overlay.keepTriggerArmTuning;
        }
        if (base.keepArmTuningL == null) {
            base.keepArmTuningL = overlay.keepArmTuningL;
        }
        if (base.keepArmTuningR == null) {
            base.keepArmTuningR = overlay.keepArmTuningR;
        }
        base.armTransforms = PunchyTuningConfig.mergeSideSetPreferBase(base.armTransforms, overlay.armTransforms);
        base.customAnimation = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.customAnimation, overlay.customAnimation);
        base.customItemAnimations = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.customItemAnimations, overlay.customItemAnimations);
        if ((base.betterCombat == null || base.betterCombat.isEmpty()) && overlay.betterCombat != null && !overlay.betterCombat.isEmpty()) {
            base.betterCombat = overlay.betterCombat;
        }
        return base;
    }

    private static Map<String, SideSet> mergeSideSetMapPreferBase(Map<String, SideSet> base, Map<String, SideSet> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, SideSet>();
        }
        for (Map.Entry<String, SideSet> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            SideSet existing = base.get(entry.getKey());
            if (existing == null) {
                base.put(entry.getKey(), entry.getValue());
                continue;
            }
            base.put(entry.getKey(), PunchyTuningConfig.mergeSideSetPreferBase(existing, entry.getValue()));
        }
        return base;
    }

    private static Map<String, Pose> mergePoseMapPreferBase(Map<String, Pose> base, Map<String, Pose> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, Pose>();
        }
        for (Map.Entry<String, Pose> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            base.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return base;
    }

    private static Map<String, GroupEntry> mergeGroupEntryMapPreferBase(Map<String, GroupEntry> base, Map<String, GroupEntry> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, GroupEntry>();
        }
        for (Map.Entry<String, GroupEntry> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            GroupEntry merged = PunchyTuningConfig.mergeGroupEntryPreferBase(base.get(entry.getKey()), entry.getValue());
            base.put(entry.getKey(), merged);
        }
        return base;
    }

    private static GroupEntry mergeGroupEntryPreferBase(GroupEntry base, GroupEntry overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            return overlay;
        }
        if (overlay.transforms == null && (overlay.right != null || overlay.left != null)) {
            overlay.transforms = new SideSet();
            overlay.transforms.right = overlay.right;
            overlay.transforms.left = overlay.left;
        }
        base.transforms = PunchyTuningConfig.mergeSideSetPreferBase(base.transforms, overlay.transforms);
        base.armTransforms = PunchyTuningConfig.mergeSideSetPreferBase(base.armTransforms, overlay.armTransforms);
        if ((base.physics == null || base.physics.isJsonNull()) && overlay.physics != null && !overlay.physics.isJsonNull()) {
            base.physics = overlay.physics;
        }
        if (base.disableSprintLateralTilt == null) {
            base.disableSprintLateralTilt = overlay.disableSprintLateralTilt;
        }
        if (base.keepArmTuning == null) {
            base.keepArmTuning = overlay.keepArmTuning;
        }
        if (base.keepTriggerArmTuning == null) {
            base.keepTriggerArmTuning = overlay.keepTriggerArmTuning;
        }
        if (base.keepArmTuningL == null) {
            base.keepArmTuningL = overlay.keepArmTuningL;
        }
        if (base.keepArmTuningR == null) {
            base.keepArmTuningR = overlay.keepArmTuningR;
        }
        if (base.disableWalkOnWater == null) {
            base.disableWalkOnWater = overlay.disableWalkOnWater;
        }
        if (base.disableDryOut == null) {
            base.disableDryOut = overlay.disableDryOut;
        }
        if (base.disableSwiming == null) {
            base.disableSwiming = overlay.disableSwiming;
        }
        base.customAnimation = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.customAnimation, overlay.customAnimation);
        base.customItemAnimations = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.customItemAnimations, overlay.customItemAnimations);
        if ((base.particleEmitters == null || base.particleEmitters.isJsonNull()) && overlay.particleEmitters != null && !overlay.particleEmitters.isJsonNull()) {
            base.particleEmitters = overlay.particleEmitters;
        }
        if ((base.particle == null || base.particle.isJsonNull()) && overlay.particle != null && !overlay.particle.isJsonNull()) {
            base.particle = overlay.particle;
        }
        if ((base.particles == null || base.particles.isJsonNull()) && overlay.particles != null && !overlay.particles.isJsonNull()) {
            base.particles = overlay.particles;
        }
        if ((base.particleEffects == null || base.particleEffects.isJsonNull()) && overlay.particleEffects != null && !overlay.particleEffects.isJsonNull()) {
            base.particleEffects = overlay.particleEffects;
        }
        if ((base.particleEmission == null || base.particleEmission.isJsonNull()) && overlay.particleEmission != null && !overlay.particleEmission.isJsonNull()) {
            base.particleEmission = overlay.particleEmission;
        }
        if ((base.betterCombat == null || base.betterCombat.isEmpty()) && overlay.betterCombat != null && !overlay.betterCombat.isEmpty()) {
            base.betterCombat = overlay.betterCombat;
        }
        return base;
    }

    private static Map<String, SpecificEntry> mergeSpecificMapPreferBase(Data baseData, Map<String, SpecificEntry> base, Map<String, SpecificEntry> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, SpecificEntry>();
        }
        for (Map.Entry<String, SpecificEntry> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            class_2960 itemId = PunchyTuningConfig.resolveSpecificItemId(entry.getKey());
            boolean protectedTransforms = PunchyTuningConfig.hasHigherPriorityTransformsForItem(baseData, itemId);
            boolean protectedArmTransforms = PunchyTuningConfig.hasHigherPriorityArmTransformsForItem(baseData, itemId);
            SpecificEntry merged = PunchyTuningConfig.mergeSpecificEntryPreferBase(base.get(entry.getKey()), entry.getValue(), protectedTransforms, protectedArmTransforms);
            base.put(entry.getKey(), merged);
        }
        return base;
    }

    private static SpecificEntry mergeSpecificEntryPreferBase(SpecificEntry base, SpecificEntry overlay) {
        return PunchyTuningConfig.mergeSpecificEntryPreferBase(base, overlay, false, false);
    }

    private static SpecificEntry mergeSpecificEntryPreferBase(SpecificEntry base, SpecificEntry overlay, boolean protectTransforms, boolean protectArmTransforms) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            if (!protectTransforms && !protectArmTransforms) {
                return overlay;
            }
            SpecificEntry copy = PunchyTuningConfig.copySpecificEntry(overlay);
            if (protectTransforms) {
                copy.transforms = null;
            }
            if (protectArmTransforms) {
                copy.armTransforms = null;
            }
            return copy;
        }
        if (protectTransforms && protectArmTransforms) {
            return base;
        }
        if (base.kind == null) {
            base.kind = overlay.kind;
        }
        if (base.armKind == null) {
            base.armKind = overlay.armKind;
        }
        if (base.renamed == null) {
            base.renamed = overlay.renamed;
        }
        if (base.model == null) {
            base.model = overlay.model;
        }
        if (base.customModelData == null) {
            base.customModelData = overlay.customModelData;
        }
        if ((base.physics == null || base.physics.isJsonNull()) && overlay.physics != null && !overlay.physics.isJsonNull()) {
            base.physics = overlay.physics;
        }
        if (base.disableSprintLateralTilt == null) {
            base.disableSprintLateralTilt = overlay.disableSprintLateralTilt;
        }
        if (base.keepArmTuning == null) {
            base.keepArmTuning = overlay.keepArmTuning;
        }
        if (base.keepTriggerArmTuning == null) {
            base.keepTriggerArmTuning = overlay.keepTriggerArmTuning;
        }
        if (base.keepArmTuningL == null) {
            base.keepArmTuningL = overlay.keepArmTuningL;
        }
        if (base.keepArmTuningR == null) {
            base.keepArmTuningR = overlay.keepArmTuningR;
        }
        if (base.disableWalkOnWater == null) {
            base.disableWalkOnWater = overlay.disableWalkOnWater;
        }
        if (base.disableDryOut == null) {
            base.disableDryOut = overlay.disableDryOut;
        }
        if (base.disableSwiming == null) {
            base.disableSwiming = overlay.disableSwiming;
        }
        if ((base.animation == null || base.animation.isEmpty()) && overlay.animation != null && !overlay.animation.isEmpty()) {
            base.animation = overlay.animation;
        }
        base.customAnimation = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.customAnimation, overlay.customAnimation);
        base.customItemAnimations = PunchyTuningConfig.mergeCustomAnimationEntryListsPreferBase(base.customItemAnimations, overlay.customItemAnimations);
        if ((base.particleEmitters == null || base.particleEmitters.isJsonNull()) && overlay.particleEmitters != null && !overlay.particleEmitters.isJsonNull()) {
            base.particleEmitters = overlay.particleEmitters;
        }
        if ((base.particle == null || base.particle.isJsonNull()) && overlay.particle != null && !overlay.particle.isJsonNull()) {
            base.particle = overlay.particle;
        }
        if ((base.particles == null || base.particles.isJsonNull()) && overlay.particles != null && !overlay.particles.isJsonNull()) {
            base.particles = overlay.particles;
        }
        if ((base.particleEffects == null || base.particleEffects.isJsonNull()) && overlay.particleEffects != null && !overlay.particleEffects.isJsonNull()) {
            base.particleEffects = overlay.particleEffects;
        }
        if ((base.particleEmission == null || base.particleEmission.isJsonNull()) && overlay.particleEmission != null && !overlay.particleEmission.isJsonNull()) {
            base.particleEmission = overlay.particleEmission;
        }
        if ((base.betterCombat == null || base.betterCombat.isEmpty()) && overlay.betterCombat != null && !overlay.betterCombat.isEmpty()) {
            base.betterCombat = overlay.betterCombat;
        }
        if (!protectTransforms) {
            base.transforms = PunchyTuningConfig.mergeSideSetPreferBase(base.transforms, overlay.transforms);
        }
        if (!protectArmTransforms) {
            base.armTransforms = PunchyTuningConfig.mergeSideSetPreferBase(base.armTransforms, overlay.armTransforms);
        }
        return base;
    }

    private static boolean hasHigherPriorityTransformsForItem(Data data, class_2960 itemId) {
        GroupEntry group;
        if (data == null || itemId == null) {
            return false;
        }
        if (data.itemSpecific != null && !data.itemSpecific.isEmpty()) {
            for (Map.Entry<String, SpecificEntry> entry : data.itemSpecific.entrySet()) {
                if (entry == null || entry.getValue() == null || !PunchyTuningConfig.specificKeyTargetsItem(entry.getKey(), itemId) || !PunchyTuningConfig.hasSideSetData(entry.getValue().transforms)) continue;
                return true;
            }
        }
        return (group = PunchyTuningConfig.resolveHighestPriorityGroupEntryForItem(data, itemId)) != null && PunchyTuningConfig.hasSideSetData(group.transforms);
    }

    private static boolean hasHigherPriorityArmTransformsForItem(Data data, class_2960 itemId) {
        GroupEntry group;
        if (data == null || itemId == null) {
            return false;
        }
        if (data.itemSpecific != null && !data.itemSpecific.isEmpty()) {
            for (Map.Entry<String, SpecificEntry> entry : data.itemSpecific.entrySet()) {
                if (entry == null || entry.getValue() == null || !PunchyTuningConfig.specificKeyTargetsItem(entry.getKey(), itemId) || !PunchyTuningConfig.hasSideSetData(entry.getValue().armTransforms)) continue;
                return true;
            }
        }
        return (group = PunchyTuningConfig.resolveHighestPriorityGroupEntryForItem(data, itemId)) != null && PunchyTuningConfig.hasSideSetData(group.armTransforms);
    }

    private static GroupEntry resolveHighestPriorityGroupEntryForItem(Data data, class_2960 itemId) {
        if (data == null || itemId == null || data.item == null || data.item.isEmpty()) {
            return null;
        }
        String itemKey = itemId.toString();
        for (Map.Entry<String, GroupEntry> entry : data.item.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || !PunchyTuningConfig.groupEntryMatchesItem(data, entry.getKey(), itemKey, itemId)) continue;
            return entry.getValue();
        }
        return null;
    }

    private static boolean groupEntryMatchesItem(Data data, String groupKey, String itemKey, class_2960 itemId) {
        if (groupKey == null || itemKey == null || itemId == null) {
            return false;
        }
        List<String> customGroupItems = PunchyTuningConfig.resolveCustomGroupItems(data != null ? data.customGroups : null, groupKey);
        if (customGroupItems != null && customGroupItems.contains(itemKey)) {
            return true;
        }
        try {
            VanillaFirstPersonItemLayer.ToolKind kind = VanillaFirstPersonItemLayer.ToolKind.valueOf(groupKey);
            class_1792 item = class_7923.field_41178.method_17966(itemId).orElse(class_1802.field_8162);
            if (item == class_1802.field_8162) {
                return false;
            }
            class_1799 stack = new class_1799((class_1935)item);
            VanillaFirstPersonItemLayer.ToolKind resolvedKind = stack != null ? ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT) : VanillaFirstPersonItemLayer.ToolKind.INGREDIENT;
            return resolvedKind == kind;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    private static List<String> resolveCustomGroupItems(List<CustomGroupDefinition> customGroups, String name) {
        if (customGroups == null || customGroups.isEmpty() || name == null) {
            return null;
        }
        for (CustomGroupDefinition entry : customGroups) {
            if (entry == null || entry.name == null || entry.items == null || !entry.name.equalsIgnoreCase(name)) continue;
            return entry.items;
        }
        return null;
    }

    private static boolean hasSideSetData(SideSet set) {
        return set != null && (set.right != null || set.left != null);
    }

    private static List<CustomGroupDefinition> mergeCustomGroupsPreferBase(List<CustomGroupDefinition> base, List<CustomGroupDefinition> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new ArrayList<CustomGroupDefinition>();
        }
        HashSet<String> knownNames = new HashSet<String>();
        for (CustomGroupDefinition entry : base) {
            if (entry == null || entry.name == null || entry.name.isBlank()) continue;
            knownNames.add(entry.name.toLowerCase(Locale.ROOT));
        }
        for (CustomGroupDefinition entry : overlay) {
            if (entry == null) continue;
            if (entry.name == null || entry.name.isBlank()) {
                base.add(entry);
                continue;
            }
            String key = entry.name.toLowerCase(Locale.ROOT);
            if (!knownNames.add(key)) continue;
            base.add(entry);
        }
        return base;
    }

    private static Data mergeData(Data base, Data overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            return overlay;
        }
        base.globalItem = PunchyTuningConfig.mergeSideSet(base.globalItem, overlay.globalItem);
        base.globalArm = PunchyTuningConfig.mergeSideSet(base.globalArm, overlay.globalArm);
        base.default_animations = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.default_animations, overlay.default_animations);
        base.emptyHand = PunchyTuningConfig.mergeEmptyHand(base.emptyHand, overlay.emptyHand);
        base.item = PunchyTuningConfig.mergeGroupEntryMap(base.item, overlay.item);
        base.armMesh = PunchyTuningConfig.mergeSideSetMap(base.armMesh, overlay.armMesh);
        base.chestProxy = PunchyTuningConfig.mergePoseMap(base.chestProxy, overlay.chestProxy);
        base.fluidProxy = PunchyTuningConfig.mergePoseMap(base.fluidProxy, overlay.fluidProxy);
        base.enchantParticleProxy = PunchyTuningConfig.mergePoseMap(base.enchantParticleProxy, overlay.enchantParticleProxy);
        base.paddleProxy = PunchyTuningConfig.mergePoseMap(base.paddleProxy, overlay.paddleProxy);
        base.fishingLineProxy = PunchyTuningConfig.mergePoseMap(base.fishingLineProxy, overlay.fishingLineProxy);
        base.itemSpecific = PunchyTuningConfig.mergeSpecificMap(base.itemSpecific, overlay.itemSpecific);
        if (overlay.customGroups != null && !overlay.customGroups.isEmpty()) {
            if (base.customGroups == null) {
                base.customGroups = new ArrayList<CustomGroupDefinition>();
            }
            base.customGroups.addAll(overlay.customGroups);
        }
        return base;
    }

    private static SideSet mergeSideSet(SideSet base, SideSet overlay) {
        if (overlay == null || overlay.right == null && overlay.left == null) {
            return base;
        }
        if (base == null) {
            base = new SideSet();
        }
        if (overlay.right != null) {
            base.right = overlay.right;
        }
        if (overlay.left != null) {
            base.left = overlay.left;
        }
        return base;
    }

    private static EmptyHandEntry mergeEmptyHand(EmptyHandEntry base, EmptyHandEntry overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            base = new EmptyHandEntry();
        }
        base.transforms = PunchyTuningConfig.mergeSideSet(base.transforms, overlay.transforms);
        if (overlay.physics != null && !overlay.physics.isJsonNull()) {
            base.physics = overlay.physics;
        }
        if (overlay.disableSprintLateralTilt != null) {
            base.disableSprintLateralTilt = overlay.disableSprintLateralTilt;
        }
        if (overlay.keepArmTuning != null) {
            base.keepArmTuning = overlay.keepArmTuning;
        }
        if (overlay.keepTriggerArmTuning != null) {
            base.keepTriggerArmTuning = overlay.keepTriggerArmTuning;
        }
        if (overlay.keepArmTuningL != null) {
            base.keepArmTuningL = overlay.keepArmTuningL;
        }
        if (overlay.keepArmTuningR != null) {
            base.keepArmTuningR = overlay.keepArmTuningR;
        }
        base.armTransforms = PunchyTuningConfig.mergeSideSet(base.armTransforms, overlay.armTransforms);
        base.customAnimation = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.customAnimation, overlay.customAnimation);
        base.customItemAnimations = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.customItemAnimations, overlay.customItemAnimations);
        if (overlay.betterCombat != null && !overlay.betterCombat.isEmpty()) {
            base.betterCombat = overlay.betterCombat;
        }
        return base;
    }

    private static Map<String, SideSet> mergeSideSetMap(Map<String, SideSet> base, Map<String, SideSet> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, SideSet>();
        }
        for (Map.Entry<String, SideSet> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            base.put(entry.getKey(), entry.getValue());
        }
        return base;
    }

    private static Map<String, Pose> mergePoseMap(Map<String, Pose> base, Map<String, Pose> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, Pose>();
        }
        for (Map.Entry<String, Pose> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            base.put(entry.getKey(), entry.getValue());
        }
        return base;
    }

    private static Map<String, GroupEntry> mergeGroupEntryMap(Map<String, GroupEntry> base, Map<String, GroupEntry> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, GroupEntry>();
        }
        for (Map.Entry<String, GroupEntry> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            GroupEntry merged = PunchyTuningConfig.mergeGroupEntry(base.get(entry.getKey()), entry.getValue());
            base.put(entry.getKey(), merged);
        }
        return base;
    }

    private static GroupEntry mergeGroupEntry(GroupEntry base, GroupEntry overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            return overlay;
        }
        if (overlay.transforms == null && (overlay.right != null || overlay.left != null)) {
            overlay.transforms = new SideSet();
            overlay.transforms.right = overlay.right;
            overlay.transforms.left = overlay.left;
        }
        base.transforms = PunchyTuningConfig.mergeSideSet(base.transforms, overlay.transforms);
        base.armTransforms = PunchyTuningConfig.mergeSideSet(base.armTransforms, overlay.armTransforms);
        if (overlay.physics != null && !overlay.physics.isJsonNull()) {
            base.physics = overlay.physics;
        }
        if (overlay.disableSprintLateralTilt != null) {
            base.disableSprintLateralTilt = overlay.disableSprintLateralTilt;
        }
        if (overlay.keepArmTuning != null) {
            base.keepArmTuning = overlay.keepArmTuning;
        }
        if (overlay.keepTriggerArmTuning != null) {
            base.keepTriggerArmTuning = overlay.keepTriggerArmTuning;
        }
        if (overlay.keepArmTuningL != null) {
            base.keepArmTuningL = overlay.keepArmTuningL;
        }
        if (overlay.keepArmTuningR != null) {
            base.keepArmTuningR = overlay.keepArmTuningR;
        }
        if (overlay.disableWalkOnWater != null) {
            base.disableWalkOnWater = overlay.disableWalkOnWater;
        }
        if (overlay.disableDryOut != null) {
            base.disableDryOut = overlay.disableDryOut;
        }
        if (overlay.disableSwiming != null) {
            base.disableSwiming = overlay.disableSwiming;
        }
        base.customAnimation = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.customAnimation, overlay.customAnimation);
        base.customItemAnimations = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.customItemAnimations, overlay.customItemAnimations);
        if (overlay.particleEmitters != null && !overlay.particleEmitters.isJsonNull()) {
            base.particleEmitters = overlay.particleEmitters;
        }
        if (overlay.particle != null && !overlay.particle.isJsonNull()) {
            base.particle = overlay.particle;
        }
        if (overlay.particles != null && !overlay.particles.isJsonNull()) {
            base.particles = overlay.particles;
        }
        if (overlay.particleEffects != null && !overlay.particleEffects.isJsonNull()) {
            base.particleEffects = overlay.particleEffects;
        }
        if (overlay.particleEmission != null && !overlay.particleEmission.isJsonNull()) {
            base.particleEmission = overlay.particleEmission;
        }
        if (overlay.betterCombat != null && !overlay.betterCombat.isEmpty()) {
            base.betterCombat = overlay.betterCombat;
        }
        return base;
    }

    private static Map<String, SpecificEntry> mergeSpecificMap(Map<String, SpecificEntry> base, Map<String, SpecificEntry> overlay) {
        if (overlay == null || overlay.isEmpty()) {
            return base;
        }
        if (base == null) {
            base = new HashMap<String, SpecificEntry>();
        }
        for (Map.Entry<String, SpecificEntry> entry : overlay.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            SpecificEntry merged = PunchyTuningConfig.mergeSpecificEntry(base.get(entry.getKey()), entry.getValue());
            base.put(entry.getKey(), merged);
        }
        return base;
    }

    private static SpecificEntry mergeSpecificEntry(SpecificEntry base, SpecificEntry overlay) {
        if (overlay == null) {
            return base;
        }
        if (base == null) {
            return overlay;
        }
        if (overlay.kind != null) {
            base.kind = overlay.kind;
        }
        if (overlay.armKind != null) {
            base.armKind = overlay.armKind;
        }
        if (overlay.renamed != null) {
            base.renamed = overlay.renamed;
        }
        if (overlay.model != null) {
            base.model = overlay.model;
        }
        if (overlay.customModelData != null) {
            base.customModelData = overlay.customModelData;
        }
        if (overlay.physics != null && !overlay.physics.isJsonNull()) {
            base.physics = overlay.physics;
        }
        if (overlay.disableSprintLateralTilt != null) {
            base.disableSprintLateralTilt = overlay.disableSprintLateralTilt;
        }
        if (overlay.keepArmTuning != null) {
            base.keepArmTuning = overlay.keepArmTuning;
        }
        if (overlay.keepTriggerArmTuning != null) {
            base.keepTriggerArmTuning = overlay.keepTriggerArmTuning;
        }
        if (overlay.keepArmTuningL != null) {
            base.keepArmTuningL = overlay.keepArmTuningL;
        }
        if (overlay.keepArmTuningR != null) {
            base.keepArmTuningR = overlay.keepArmTuningR;
        }
        if (overlay.disableWalkOnWater != null) {
            base.disableWalkOnWater = overlay.disableWalkOnWater;
        }
        if (overlay.disableDryOut != null) {
            base.disableDryOut = overlay.disableDryOut;
        }
        if (overlay.disableSwiming != null) {
            base.disableSwiming = overlay.disableSwiming;
        }
        if (overlay.animation != null && !overlay.animation.isEmpty()) {
            base.animation = overlay.animation;
        }
        base.customAnimation = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.customAnimation, overlay.customAnimation);
        base.customItemAnimations = PunchyTuningConfig.mergeCustomAnimationEntryLists(base.customItemAnimations, overlay.customItemAnimations);
        if (overlay.particleEmitters != null && !overlay.particleEmitters.isJsonNull()) {
            base.particleEmitters = overlay.particleEmitters;
        }
        if (overlay.particle != null && !overlay.particle.isJsonNull()) {
            base.particle = overlay.particle;
        }
        if (overlay.particles != null && !overlay.particles.isJsonNull()) {
            base.particles = overlay.particles;
        }
        if (overlay.particleEffects != null && !overlay.particleEffects.isJsonNull()) {
            base.particleEffects = overlay.particleEffects;
        }
        if (overlay.particleEmission != null && !overlay.particleEmission.isJsonNull()) {
            base.particleEmission = overlay.particleEmission;
        }
        if (overlay.betterCombat != null && !overlay.betterCombat.isEmpty()) {
            base.betterCombat = overlay.betterCombat;
        }
        base.transforms = PunchyTuningConfig.mergeSideSet(base.transforms, overlay.transforms);
        base.armTransforms = PunchyTuningConfig.mergeSideSet(base.armTransforms, overlay.armTransforms);
        return base;
    }

    private static class_2960 resolveSpecificItemId(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return null;
        }
        String trimmed = rawKey.trim();
        class_2960 direct = class_2960.method_12829((String)trimmed);
        if (direct != null) {
            return direct;
        }
        int cut = -1;
        int hash = trimmed.indexOf(35);
        int pipe = trimmed.indexOf(124);
        int at = trimmed.indexOf(64);
        if (hash > 0) {
            cut = hash;
        }
        if (pipe > 0 && (cut < 0 || pipe < cut)) {
            cut = pipe;
        }
        if (at > 0 && (cut < 0 || at < cut)) {
            cut = at;
        }
        if (cut <= 0) {
            return null;
        }
        return class_2960.method_12829((String)trimmed.substring(0, cut).trim());
    }

    private static boolean specificKeyTargetsItem(String rawKey, class_2960 itemId) {
        if (itemId == null) {
            return false;
        }
        class_2960 parsed = PunchyTuningConfig.resolveSpecificItemId(rawKey);
        return parsed != null && parsed.equals((Object)itemId);
    }

    private static Data finalizeCompatData(String profile, Data data) {
        return data;
    }

    private static Path getCompatDirectory() {
        Path configDir = PunchyTuningConfig.getConfigDirectory();
        return configDir.resolve(COMPAT_DIR_NAME);
    }

    private static void resetAllTunings() {
        ToolTuning.resetDefaults();
        ArmToolTuning.resetDefaults();
        ArmMeshTuning.resetDefaults();
        for (ChestProxyTuning.Kind kind : ChestProxyTuning.Kind.values()) {
            ChestProxyTuning.reset(kind);
        }
        for (Enum enum_ : FluidProxyTuning.Kind.values()) {
            FluidProxyTuning.reset((FluidProxyTuning.Kind)enum_);
        }
        for (Enum enum_ : EnchantParticleProxyTuning.Kind.values()) {
            EnchantParticleProxyTuning.reset((EnchantParticleProxyTuning.Kind)enum_, false);
            EnchantParticleProxyTuning.reset((EnchantParticleProxyTuning.Kind)enum_, true);
        }
        for (Enum enum_ : PaddleProxyTuning.Kind.values()) {
            PaddleProxyTuning.reset((PaddleProxyTuning.Kind)enum_, false);
            PaddleProxyTuning.reset((PaddleProxyTuning.Kind)enum_, true);
        }
        for (Enum enum_ : FishingLineProxyTuning.Kind.values()) {
            FishingLineProxyTuning.reset((FishingLineProxyTuning.Kind)enum_, false);
            FishingLineProxyTuning.reset((FishingLineProxyTuning.Kind)enum_, true);
        }
    }

    private static Path getConfigDirectory() {
        class_310 client = class_310.method_1551();
        if (client != null) {
            return client.field_1697.toPath().resolve("config");
        }
        return Path.of("config", new String[0]);
    }

    private static void installBundledCompatProfiles() {
        for (String profile : BUNDLED_COMPAT_PROFILES) {
            PunchyTuningConfig.copyBundledCompatProfile(profile);
        }
    }

    private static void copyBundledCompatProfile(String profile) {
        try (InputStream stream = PunchyTuningConfig.openBundledCompatStream(profile);){
            if (stream == null) {
                return;
            }
            Path compatDir = PunchyTuningConfig.getCompatDirectory();
            Files.createDirectories(compatDir, new FileAttribute[0]);
            Path target = compatDir.resolve(profile + ".json");
            Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static InputStream openBundledCompatStream(String profile) {
        return null;
    }

    static {
        REGISTRY_CUSTOM_GROUPS = new HashMap<String, List<String>>();
        MIX_CATALOG = new MixCatalog();
        MIX_COMPAT_OPTION_IDS = new LinkedHashSet<String>();
        MIX_PUNCHY_OPTION_IDS = new LinkedHashSet<String>();
        GROUP_RUNTIME_FLAGS = new EnumMap(VanillaFirstPersonItemLayer.ToolKind.class);
        SPECIFIC_RUNTIME_FLAGS = new HashMap<class_2960, ItemRuntimeFlags>();
        SPEAR_MIX_CLIP_NAMES = Set.of("attack_spear", "use_spear_pose_1", "use_spear_pose_2", "use_spear_pose_3", "use_spear_pose_3_left", "use_spear_pose_3_right");
        SWORD_PASSIVE_CLIP_NAMES = Set.of("attack", "attack_sword", "mining", "mining_foward", "mining_forward", "mining_sideways", "mining_digging", "mining_use_foward", "mining_use_forward", "mining_use_digging", "use_item", "useitem", "interaction", "interact", "inspect", "hand_in", "hand_out");
        GENERIC_PASSIVE_CLIP_NAMES = Set.of("attack", "use_item", "useitem", "interaction", "interact", "inspect", "hand_in", "hand_out");
        MIX_TYPE_CLIP_ALIASES = PunchyTuningConfig.createMixTypeClipAliases();
    }

    private static final class MixCatalog {
        final LinkedHashMap<String, MixSlotDef> slots = new LinkedHashMap();

        private MixCatalog() {
        }
    }

    private static final class MixSlotDef {
        String slotId;
        String label;
        MixSlotType type;
        VanillaFirstPersonItemLayer.ToolKind kind;
        class_2960 itemId;
        final LinkedHashSet<String> optionOrder = new LinkedHashSet();
        final LinkedHashMap<String, MixSlotPayload> optionPayloads = new LinkedHashMap();
        final LinkedHashSet<class_2960> mergedSpecificItems = new LinkedHashSet();
        final LinkedHashMap<String, LinkedHashMap<class_2960, MixSlotPayload>> optionSpecificPayloads = new LinkedHashMap();
        final LinkedHashMap<String, LinkedHashSet<String>> optionClipNames = new LinkedHashMap();
        String resourceOrderOption;

        private MixSlotDef() {
        }
    }

    private static enum MixSlotType {
        KIND,
        SPECIFIC;

    }

    private static final class MixSlotPayload {
        VanillaFirstPersonItemLayer.ToolKind kind;
        GroupEntry groupEntry;
        EmptyHandEntry emptyHandEntry;
        SpecificEntry specificEntry;

        private MixSlotPayload() {
        }
    }

    public static final class CustomAnimationEntry {
        public String type;
        public List<BetterCombatEntry> betterCombat;
        public String key;
        @SerializedName(value="min_blocks", alternate={"minBlocks", "falling_min_blocks", "trigger_falling_min_blocks"})
        public Float min_blocks;
        public String var_1;
        public String var_2;
        public String var_3;
        public String attack_1;
        public String attack_2;
        public String attack_3;
        public String attack_4;
        public String attack_5;
        public String attack_6;
        public String critical_hit;
        public String name;
        public String animation;
        public String start;
        public String looping;
        public String end;
        @SerializedName(value="throw", alternate={"throw_animation", "cast", "start_cast"})
        public String throw_animation;
        @SerializedName(value="pull", alternate={"pull_animation", "reel", "reel_in"})
        public String pull_animation;
        @SerializedName(value="disable_walk_on_water", alternate={"disable_walkonwater", "disable_walk_on_water_v2", "disable_walk_on_water_animation"})
        public Boolean disable_walk_on_water;
        @SerializedName(value="disable_dry_out", alternate={"disable_dryout", "disable_dryout_animation"})
        public Boolean disable_dry_out;
        public String charge;
        public String release;
        public String abbort;
        @SerializedName(value="abort", alternate={"aborted"})
        public String abort;
        @SerializedName(value="stage_1", alternate={"stage1"})
        public String stage_1;
        @SerializedName(value="stage_2", alternate={"stage2"})
        public String stage_2;
        @SerializedName(value="stage_3", alternate={"stage3"})
        public String stage_3;
        public String keep_looping;
        public List<String> items;
        public JsonObject blockState;
        public Boolean in_water;
        public Boolean inside_water;
        public BlockDetection block_detection;
        public WeatherDetection weather_detection;
        public TimeDetection time_detection;
        public BiomeDetection biome_detection;
        public Boolean skip_blend_in;
        public Boolean skip_blend_out;
        @SerializedName(value="blend_from_current_origin", alternate={"blendFromCurrentOrigin", "rebase_blend_in", "rebaseBlendIn"})
        public Boolean blend_from_current_origin;
        public Boolean disable_swim_animation;
        public Boolean disable_all_particles;
        @SerializedName(value="disable_physics_bones", alternate={"disable_pendulum_bones"})
        public List<String> disable_physics_bones;
        public Float interfaceDelay;
        public Boolean stop_immediate;
        @SerializedName(value="skip_hand_equip", alternate={"disable_hand_equip", "no_hand_equip"})
        public Boolean skip_hand_equip;
        public Float loop_speed_max;
        public Float loop_speed_ramp;
        public Boolean isJavaBlock;
        public transient String mix_pack_option;
        public CustomFlags customFlags;
    }

    private static final class ItemRuntimeFlags {
        Boolean disableWalkOnWater;
        Boolean disableDryOut;
        Boolean disableSwiming;

        private ItemRuntimeFlags() {
        }
    }

    public static final class SpecificEntry {
        public String kind;
        public String armKind;
        public String renamed;
        public JsonElement physics;
        @SerializedName(value="disable_sprint_lateral_tilt", alternate={"disableSprintLateralTilt"})
        public Boolean disableSprintLateralTilt;
        @SerializedName(value="keep_arm_tuning", alternate={"keepArmTuning"})
        public Boolean keepArmTuning;
        @SerializedName(value="keep_trigger_arm_tuning", alternate={"keepTriggerArmTuning", "keep_main_arm_tuning", "keepMainArmTuning"})
        public Boolean keepTriggerArmTuning;
        @SerializedName(value="keep_arm_tuning_l", alternate={"keepArmTuningL"})
        public Boolean keepArmTuningL;
        @SerializedName(value="keep_arm_tuning_r", alternate={"keepArmTuningR"})
        public Boolean keepArmTuningR;
        public String model;
        @SerializedName(value="customModelData", alternate={"custom_model_data", "custommodeldata"})
        public Integer customModelData;
        public List<AnimationRef> animation;
        public List<CustomAnimationEntry> customAnimation;
        public List<CustomAnimationEntry> customItemAnimations;
        @SerializedName(value="disable_walk_on_water", alternate={"disable_walkonwater", "disable_walk_on_water_v2", "disable_walk_on_water_animation"})
        public Boolean disableWalkOnWater;
        @SerializedName(value="disable_dry_out", alternate={"disable_dryout", "disable_dryout_animation"})
        public Boolean disableDryOut;
        @SerializedName(value="disable_swiming", alternate={"disable_swimming", "disable_swim", "disable_swim_animation"})
        public Boolean disableSwiming;
        @SerializedName(value="particle_emitters", alternate={"particleEmitters"})
        public JsonElement particleEmitters;
        public JsonElement particle;
        public JsonElement particles;
        @SerializedName(value="particle_effects", alternate={"particleEffects"})
        public JsonElement particleEffects;
        @SerializedName(value="particle_emission", alternate={"particleEmission"})
        public JsonElement particleEmission;
        public List<BetterCombatEntry> betterCombat;
        @SerializedName(value="bone_physics", alternate={"bonePhysics"})
        public JsonElement bonePhysics;
        public SideSet transforms;
        public SideSet armTransforms;
    }

    public static final class GroupEntry {
        public SideSet transforms;
        public SideSet armTransforms;
        public JsonElement physics;
        @SerializedName(value="disable_sprint_lateral_tilt", alternate={"disableSprintLateralTilt"})
        public Boolean disableSprintLateralTilt;
        @SerializedName(value="keep_arm_tuning", alternate={"keepArmTuning"})
        public Boolean keepArmTuning;
        @SerializedName(value="keep_trigger_arm_tuning", alternate={"keepTriggerArmTuning", "keep_main_arm_tuning", "keepMainArmTuning"})
        public Boolean keepTriggerArmTuning;
        @SerializedName(value="keep_arm_tuning_l", alternate={"keepArmTuningL"})
        public Boolean keepArmTuningL;
        @SerializedName(value="keep_arm_tuning_r", alternate={"keepArmTuningR"})
        public Boolean keepArmTuningR;
        public List<CustomAnimationEntry> customAnimation;
        public List<CustomAnimationEntry> customItemAnimations;
        @SerializedName(value="disable_walk_on_water", alternate={"disable_walkonwater", "disable_walk_on_water_v2", "disable_walk_on_water_animation"})
        public Boolean disableWalkOnWater;
        @SerializedName(value="disable_dry_out", alternate={"disable_dryout", "disable_dryout_animation"})
        public Boolean disableDryOut;
        @SerializedName(value="disable_swiming", alternate={"disable_swimming", "disable_swim", "disable_swim_animation"})
        public Boolean disableSwiming;
        @SerializedName(value="particle_emitters", alternate={"particleEmitters"})
        public JsonElement particleEmitters;
        public JsonElement particle;
        public JsonElement particles;
        @SerializedName(value="particle_effects", alternate={"particleEffects"})
        public JsonElement particleEffects;
        @SerializedName(value="particle_emission", alternate={"particleEmission"})
        public JsonElement particleEmission;
        public List<BetterCombatEntry> betterCombat;
        public Pose right;
        public Pose left;
    }

    public static final class Data {
        public Map<String, GroupEntry> item = new HashMap<String, GroupEntry>();
        @SerializedName(value="default_animations", alternate={"defaultAnimations"})
        public List<CustomAnimationEntry> default_animations;
        public Map<String, SideSet> armMesh = new HashMap<String, SideSet>();
        public SideSet globalItem;
        public SideSet globalArm;
        public EmptyHandEntry emptyHand;
        public Map<String, Pose> chestProxy = new HashMap<String, Pose>();
        public Map<String, Pose> fluidProxy = new HashMap<String, Pose>();
        public Map<String, Pose> enchantParticleProxy = new HashMap<String, Pose>();
        public Map<String, Pose> paddleProxy = new HashMap<String, Pose>();
        public Map<String, Pose> fishingLineProxy = new HashMap<String, Pose>();
        public Map<String, SpecificEntry> itemSpecific = new HashMap<String, SpecificEntry>();
        @SerializedName(value="customGroups", alternate={"custom_groups"})
        public List<CustomGroupDefinition> customGroups;
    }

    public static final class CustomGroupDefinition {
        public String name;
        public List<String> items;
    }

    public static final class PhysicsEntry {
        public String name;
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
    }

    public static final class SideSet {
        public Pose right;
        public Pose left;
    }

    public static final class Pose {
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
        public String physics;
        @SerializedName(value="disable_sprint_lateral_tilt", alternate={"disableSprintLateralTilt"})
        public Boolean disableSprintLateralTilt;
        @SerializedName(value="keep_arm_tuning", alternate={"keepArmTuning"})
        public Boolean keepArmTuning;
        @SerializedName(value="keep_trigger_arm_tuning", alternate={"keepTriggerArmTuning", "keep_main_arm_tuning", "keepMainArmTuning"})
        public Boolean keepTriggerArmTuning;
        @SerializedName(value="keep_arm_tuning_l", alternate={"keepArmTuningL"})
        public Boolean keepArmTuningL;
        @SerializedName(value="keep_arm_tuning_r", alternate={"keepArmTuningR"})
        public Boolean keepArmTuningR;

        public Pose() {
        }

        public Pose(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ) {
            this(scale, offX, offY, offZ, rotX, rotY, rotZ, 0.0f, 0.0f, 0.0f, null);
        }

        public Pose(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ, float pivotX, float pivotY, float pivotZ) {
            this(scale, offX, offY, offZ, rotX, rotY, rotZ, pivotX, pivotY, pivotZ, null);
        }

        public Pose(float scale, float offX, float offY, float offZ, float rotX, float rotY, float rotZ, float pivotX, float pivotY, float pivotZ, String physics) {
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
            this.physics = physics;
        }

        public static Pose fromTool(ToolTuning.Profile p) {
            String phys = p.physicsMode != null && p.physicsMode != ToolTuning.PhysicsMode.NONE ? p.physicsMode.name() : null;
            Pose pose = new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ, p.pivotX, p.pivotY, p.pivotZ, phys);
            pose.disableSprintLateralTilt = p.disableSprintLateralTilt;
            pose.keepArmTuning = p.keepArmTuning;
            pose.keepTriggerArmTuning = p.keepTriggerArmTuning;
            return pose;
        }

        public static Pose fromArm(ArmToolTuning.Profile p) {
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public ToolTuning.Profile toTool() {
            ToolTuning.PhysicsMode mode = ToolTuning.PhysicsMode.NONE;
            if (this.physics != null && !this.physics.isBlank()) {
                try {
                    mode = ToolTuning.PhysicsMode.valueOf(this.physics.toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            ToolTuning.Profile profile = new ToolTuning.Profile(this.scale, this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ, this.pivotX, this.pivotY, this.pivotZ, mode);
            profile.disableSprintLateralTilt = this.disableSprintLateralTilt;
            profile.keepArmTuning = this.keepArmTuning != null ? this.keepArmTuning : (this.keepArmTuningL != null ? this.keepArmTuningL : this.keepArmTuningR);
            profile.keepTriggerArmTuning = this.keepTriggerArmTuning;
            return profile;
        }

        public ArmToolTuning.Profile toArm() {
            return new ArmToolTuning.Profile(this.scale, this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ);
        }

        public static Pose fromArmMesh(ArmMeshTuning.Profile p) {
            if (p == null) {
                return null;
            }
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public ArmMeshTuning.Profile toArmMesh() {
            return new ArmMeshTuning.Profile(this.scale, this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ);
        }

        public static Pose fromChestProxy(ChestProxyTuning.Profile p) {
            if (p == null) {
                return null;
            }
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public ChestProxyTuning.Profile toChestProxy() {
            return new ChestProxyTuning.Profile(this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ, this.scale);
        }

        public static Pose fromFluidProxy(FluidProxyTuning.Profile p) {
            if (p == null) {
                return null;
            }
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public FluidProxyTuning.Profile toFluidProxy() {
            return new FluidProxyTuning.Profile(this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ, this.scale);
        }

        public static Pose fromEnchantParticleProxy(EnchantParticleProxyTuning.Profile p) {
            if (p == null) {
                return null;
            }
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public EnchantParticleProxyTuning.Profile toEnchantParticleProxy() {
            return new EnchantParticleProxyTuning.Profile(this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ, this.scale);
        }

        public static Pose fromPaddleProxy(PaddleProxyTuning.Profile p) {
            if (p == null) {
                return null;
            }
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public PaddleProxyTuning.Profile toPaddleProxy() {
            return new PaddleProxyTuning.Profile(this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ, this.scale);
        }

        public static Pose fromFishingLineProxy(FishingLineProxyTuning.Profile p) {
            if (p == null) {
                return null;
            }
            return new Pose(p.scale, p.offX, p.offY, p.offZ, p.rotX, p.rotY, p.rotZ);
        }

        public FishingLineProxyTuning.Profile toFishingLineProxy() {
            return new FishingLineProxyTuning.Profile(this.offX, this.offY, this.offZ, this.rotX, this.rotY, this.rotZ, this.scale);
        }
    }

    public static final class EmptyHandEntry {
        public SideSet transforms;
        public JsonElement physics;
        @SerializedName(value="disable_sprint_lateral_tilt", alternate={"disableSprintLateralTilt"})
        public Boolean disableSprintLateralTilt;
        @SerializedName(value="keep_arm_tuning", alternate={"keepArmTuning"})
        public Boolean keepArmTuning;
        @SerializedName(value="keep_trigger_arm_tuning", alternate={"keepTriggerArmTuning", "keep_main_arm_tuning", "keepMainArmTuning"})
        public Boolean keepTriggerArmTuning;
        @SerializedName(value="keep_arm_tuning_l", alternate={"keepArmTuningL"})
        public Boolean keepArmTuningL;
        @SerializedName(value="keep_arm_tuning_r", alternate={"keepArmTuningR"})
        public Boolean keepArmTuningR;
        public SideSet armTransforms;
        public List<CustomAnimationEntry> customAnimation;
        public List<CustomAnimationEntry> customItemAnimations;
        public List<BetterCombatEntry> betterCombat;
    }

    private static enum EnchantParticleProxySide {
        LEFT,
        RIGHT,
        BOTH;

    }

    private static enum PaddleProxySide {
        LEFT,
        RIGHT,
        BOTH;

    }

    private static enum FishingLineProxySide {
        LEFT,
        RIGHT,
        BOTH;

    }

    private static final class CompatResourceEntry {
        final class_2960 id;
        final String profile;
        final class_3298 resource;
        final int packPriority;
        final int discoveryOrder;
        final int stackOrder;

        CompatResourceEntry(class_2960 id, String profile, class_3298 resource, int packPriority, int discoveryOrder, int stackOrder) {
            this.id = id;
            this.profile = profile;
            this.resource = resource;
            this.packPriority = packPriority;
            this.discoveryOrder = discoveryOrder;
            this.stackOrder = stackOrder;
        }
    }

    private static final class MixCatalogOrdering {
        final LinkedHashMap<String, Integer> optionPriorities = new LinkedHashMap();
        final ArrayList<String> selectedOptionIdsByPriority = new ArrayList();

        private MixCatalogOrdering() {
        }
    }

    public static final class BetterCombatEntry {
        public String type;
        public String attack_1;
        public String attack_2;
        public String attack_3;
        public String attack_4;
        public String attack_5;
        public String attack_6;
    }

    public static final class BiomeDetection {
        public String biome;
        public List<String> biomes;
        public Integer radius;
        public Integer offsetX;
        public Integer offsetY;
        public Integer offsetZ;
    }

    public static final class TimeDetection {
        public Integer min;
        public Integer max;
        public List<Integer> range;
    }

    public static final class WeatherDetection {
        public Boolean raining;
        public Boolean thundering;
        public Boolean clear;
    }

    public static final class BlockDetection {
        public String block;
        public List<String> blocks;
        public Integer radius;
        public Integer offsetX;
        public Integer offsetY;
        public Integer offsetZ;
    }

    public static final class CustomFlags {
        @SerializedName(value="sync_procedural_arm_animations", alternate={"syncProceduralArmAnimations"})
        public Boolean syncProceduralArmAnimations;
        @SerializedName(value="walk_and_sprint_force", alternate={"walkAndSprintForce"})
        public Integer walkAndSprintForce;
        @SerializedName(value="remaining_procedural_animations_force", alternate={"remainingProceduralAnimationsForce"})
        public Integer remainingProceduralAnimationsForce;
    }

    public static final class AnimationRef {
        public String type;
        public String name;
    }
}

