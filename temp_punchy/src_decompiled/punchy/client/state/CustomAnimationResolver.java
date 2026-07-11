/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 *  net.minecraft.class_5321
 *  net.minecraft.class_638
 *  net.minecraft.class_6862
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 *  net.minecraft.class_7924
 *  org.joml.Vector3f
 */
package punchy.client.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.joml.Vector3f;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;
import punchy.client.render.ToolTuning;
import punchy.config.PunchyTuningConfig;

final class CustomAnimationResolver {
    private static final String EFFECT_BLEND_FROM_CURRENT_ORIGIN = "blend_from_current_origin";
    private static final String EFFECT_REBASE_BLEND_IN = "rebase_blend_in";
    static final float WALK_ON_WATER_ACTION_REBASE_SPEED = 0.95f;

    private CustomAnimationResolver() {
    }

    static PunchyTuningConfig.CustomAnimationEntry findEntry(class_1799 stack, String targetType) {
        List<PunchyTuningConfig.CustomAnimationEntry> matches = CustomAnimationResolver.findEntries(stack, targetType, true);
        return matches.isEmpty() ? null : matches.get(0);
    }

    static PunchyTuningConfig.CustomAnimationEntry findEntry(class_1799 stack, String targetType, boolean includeDefaults) {
        List<PunchyTuningConfig.CustomAnimationEntry> matches = CustomAnimationResolver.findEntries(stack, targetType, includeDefaults);
        return matches.isEmpty() ? null : matches.get(0);
    }

    static PunchyTuningConfig.CustomAnimationEntry findInteractionEntry(class_1799 stack, class_2680 blockState) {
        if (stack == null) {
            return null;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        String normalizedTarget = CustomAnimationResolver.normalizeType("interaction");
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.type == null || !normalizedTarget.equals(CustomAnimationResolver.normalizeType(entry.type)) || !CustomAnimationResolver.matchesWorldTriggers(entry) || !CustomAnimationResolver.matchesInteractionBlocks(entry.items, blockState, entry.blockState)) continue;
            return entry;
        }
        return null;
    }

    static List<PunchyTuningConfig.CustomAnimationEntry> findEntries(class_1799 stack, String targetType) {
        return CustomAnimationResolver.findEntries(stack, targetType, true);
    }

    static List<PunchyTuningConfig.CustomAnimationEntry> findEntries(class_1799 stack, String targetType, boolean includeDefaults) {
        List<PunchyTuningConfig.CustomAnimationEntry> entries;
        if (stack == null || targetType == null || targetType.isBlank()) {
            return List.of();
        }
        List<PunchyTuningConfig.CustomAnimationEntry> list = entries = includeDefaults ? ToolTuning.getCustomAnimation(stack) : ToolTuning.getCustomAnimationWithoutDefaults(stack);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        String normalizedTarget = CustomAnimationResolver.normalizeType(targetType);
        ArrayList<PunchyTuningConfig.CustomAnimationEntry> matches = new ArrayList<PunchyTuningConfig.CustomAnimationEntry>();
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.type == null || !normalizedTarget.equals(CustomAnimationResolver.normalizeType(entry.type)) || entry.items != null && !entry.items.isEmpty() && !CustomAnimationResolver.matchesItemList(stack, entry.items) || !CustomAnimationResolver.matchesWorldTriggers(entry)) continue;
            matches.add(entry);
        }
        if (matches.size() > 1) {
            matches.sort((a, b) -> Integer.compare(CustomAnimationResolver.entrySpecificityScore(b), CustomAnimationResolver.entrySpecificityScore(a)));
        }
        return matches;
    }

    static PunchyTuningConfig.BetterCombatEntry findBetterCombatEntry(class_1799 stack, String targetType) {
        List<PunchyTuningConfig.BetterCombatEntry> matches = CustomAnimationResolver.findBetterCombatEntries(stack, targetType);
        return matches.isEmpty() ? null : matches.get(0);
    }

    static List<PunchyTuningConfig.BetterCombatEntry> findBetterCombatEntries(class_1799 stack, String targetType) {
        if (stack == null || targetType == null || targetType.isBlank()) {
            return List.of();
        }
        List<PunchyTuningConfig.BetterCombatEntry> entries = ToolTuning.getBetterCombat(stack);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        String normalizedTarget = CustomAnimationResolver.normalizeType(targetType);
        ArrayList<PunchyTuningConfig.BetterCombatEntry> matches = new ArrayList<PunchyTuningConfig.BetterCombatEntry>();
        for (PunchyTuningConfig.BetterCombatEntry entry : entries) {
            if (entry == null || entry.type == null || !normalizedTarget.equals(CustomAnimationResolver.normalizeType(entry.type))) continue;
            matches.add(entry);
        }
        return matches;
    }

    static List<String> resolveBetterCombatComboNames(PunchyTuningConfig.BetterCombatEntry entry) {
        ArrayList<String> names = new ArrayList<String>(6);
        if (entry == null) {
            return names;
        }
        CustomAnimationResolver.addIfPresent(names, entry.attack_1);
        CustomAnimationResolver.addIfPresent(names, entry.attack_2);
        CustomAnimationResolver.addIfPresent(names, entry.attack_3);
        CustomAnimationResolver.addIfPresent(names, entry.attack_4);
        CustomAnimationResolver.addIfPresent(names, entry.attack_5);
        CustomAnimationResolver.addIfPresent(names, entry.attack_6);
        return names;
    }

    static List<String> resolveComboNames(PunchyTuningConfig.CustomAnimationEntry entry) {
        ArrayList<String> names = new ArrayList<String>(3);
        if (entry == null) {
            return names;
        }
        CustomAnimationResolver.addIfPresent(names, entry.var_1);
        CustomAnimationResolver.addIfPresent(names, entry.var_2);
        CustomAnimationResolver.addIfPresent(names, entry.var_3);
        if (names.isEmpty()) {
            CustomAnimationResolver.addIfPresent(names, entry.name);
        }
        return names;
    }

    static String resolveSingleName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.animation != null && !entry.animation.isBlank()) {
            return entry.animation.trim();
        }
        if (entry.name != null && !entry.name.isBlank()) {
            return entry.name.trim();
        }
        if (entry.var_1 != null && !entry.var_1.isBlank()) {
            return entry.var_1.trim();
        }
        return null;
    }

    static String resolveInteractionName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.animation != null && !entry.animation.isBlank()) {
            return entry.animation.trim();
        }
        return CustomAnimationResolver.resolveSingleName(entry);
    }

    static String resolveStartName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.start != null && !entry.start.isBlank()) {
            return entry.start.trim();
        }
        return CustomAnimationResolver.resolveSingleName(entry);
    }

    static String resolveLoopingName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.looping != null && !entry.looping.isBlank()) {
            return entry.looping.trim();
        }
        if (entry.keep_looping != null && !entry.keep_looping.isBlank()) {
            return entry.keep_looping.trim();
        }
        return CustomAnimationResolver.resolveSingleName(entry);
    }

    static String resolveEndName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.end != null && !entry.end.isBlank()) {
            return entry.end.trim();
        }
        return CustomAnimationResolver.resolveSingleName(entry);
    }

    static boolean isInteractionEntry(PunchyTuningConfig.CustomAnimationEntry entry) {
        return entry != null && entry.type != null && "interaction".equalsIgnoreCase(entry.type.trim());
    }

    static String resolveCriticalName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.critical_hit != null && !entry.critical_hit.isBlank()) {
            return entry.critical_hit.trim();
        }
        return null;
    }

    static void applyLoopSpeed(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            PunchyAnimationManager.POSE_HANDLER.resetLoopSpeedParams();
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setLoopSpeedParams(entry.loop_speed_max, entry.loop_speed_ramp);
    }

    static boolean shouldStopImmediately(PunchyTuningConfig.CustomAnimationEntry entry) {
        return entry != null && Boolean.TRUE.equals(entry.stop_immediate);
    }

    static boolean shouldSkipBlendIn(PunchyTuningConfig.CustomAnimationEntry entry) {
        return entry != null && Boolean.TRUE.equals(entry.skip_blend_in);
    }

    static boolean shouldSkipBlendOut(PunchyTuningConfig.CustomAnimationEntry entry) {
        return entry != null && Boolean.TRUE.equals(entry.skip_blend_out);
    }

    static boolean shouldSkipHandEquip(PunchyTuningConfig.CustomAnimationEntry entry) {
        return entry != null && Boolean.TRUE.equals(entry.skip_hand_equip);
    }

    static boolean shouldSkipBlendInOnIdlePose() {
        return !PunchyAnimationManager.POSE_HANDLER.isPlaying() && !PunchyAnimationManager.POSE_HANDLER.isBlendOutActive();
    }

    static boolean shouldBlendFromCurrentOrigin(PunchyTuningConfig.CustomAnimationEntry entry, AnimationClip clip) {
        if (entry != null && Boolean.TRUE.equals(entry.blend_from_current_origin)) {
            return true;
        }
        return clip != null && (clip.hasEffectAny(EFFECT_BLEND_FROM_CURRENT_ORIGIN) || clip.hasEffectAny(EFFECT_REBASE_BLEND_IN) || CustomAnimationResolver.shouldAutoBlendFromWalkOnWater());
    }

    static boolean shouldAutoBlendFromWalkOnWater() {
        return ItemAnimationManager.isTypeActive("walk_on_water_v2") && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() != null && (PunchyAnimationManager.POSE_HANDLER.isPlaying() || PunchyAnimationManager.POSE_HANDLER.isBlendOutActive());
    }

    static void playConfiguredClip(AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry, float speedMultiplier, boolean skipBlendIn, boolean holdOnLastFrame) {
        if (clip == null) {
            return;
        }
        if (CustomAnimationResolver.shouldBlendFromCurrentOrigin(entry, clip)) {
            float startTime = CustomAnimationResolver.resolveBlendFromCurrentOriginStartTime(clip);
            if (CustomAnimationResolver.shouldAutoBlendFromWalkOnWater()) {
                PunchyAnimationManager.POSE_HANDLER.setRebaseReturnSpeedNext(0.95f);
            }
            PunchyAnimationManager.POSE_HANDLER.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, holdOnLastFrame, startTime);
            return;
        }
        boolean effectiveSkipBlendIn = skipBlendIn || CustomAnimationResolver.shouldSkipBlendInOnIdlePose();
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, effectiveSkipBlendIn, holdOnLastFrame);
    }

    static ClipLookup resolveNamedOrTypeFallback(class_1799 stack, String targetType, PunchyTuningConfig.CustomAnimationEntry entry, class_310 client, class_1268 hand) {
        String entryOption;
        if (targetType == null || targetType.isBlank()) {
            return new ClipLookup(null, entry, null, false);
        }
        boolean globalHandEquipType = CustomAnimationResolver.isGlobalHandEquipType(targetType);
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForType(stack, targetType);
        if (globalHandEquipType && (preferredOption == null || preferredOption.isBlank())) {
            preferredOption = PunchyTuningConfig.getGlobalMixPriorityOptionId();
        }
        if (globalHandEquipType) {
            PunchyAnimationManager.ClipResolution explicitResolution;
            String explicitName = CustomAnimationResolver.resolveSingleName(entry);
            if (explicitName != null && !explicitName.isBlank() && (explicitResolution = PunchyAnimationManager.resolveNamedClipWithSource(explicitName, client, hand, preferredOption)) != null && explicitResolution.clip() != null && CustomAnimationResolver.optionMatchesPreferred(explicitResolution.optionId(), preferredOption)) {
                return new ClipLookup(explicitResolution.clip(), entry, explicitName, false);
            }
            PunchyAnimationManager.ClipResolution fallbackResolution = PunchyAnimationManager.resolveTypeFallbackClipWithSource(targetType, client, hand, preferredOption);
            if (fallbackResolution != null && fallbackResolution.clip() != null) {
                return new ClipLookup(fallbackResolution.clip(), null, fallbackResolution.clip().getName(), true);
            }
            return new ClipLookup(null, null, explicitName, false);
        }
        PunchyTuningConfig.CustomAnimationEntry effectiveEntry = entry;
        if (effectiveEntry != null && preferredOption != null && !preferredOption.isBlank() && (entryOption = effectiveEntry.mix_pack_option) != null && !entryOption.isBlank()) {
            String normalizedEntryOption = entryOption.trim().toLowerCase(Locale.ROOT);
            String normalizedPreferred = preferredOption.trim().toLowerCase(Locale.ROOT);
            if (!normalizedPreferred.equals(normalizedEntryOption) && PunchyTuningConfig.isMixCompatOption(normalizedEntryOption)) {
                effectiveEntry = null;
            }
        }
        if (effectiveEntry != null && (entryOption = effectiveEntry.mix_pack_option) != null && !entryOption.isBlank()) {
            preferredOption = entryOption.trim().toLowerCase(Locale.ROOT);
        }
        if (preferredOption == null || preferredOption.isBlank()) {
            preferredOption = PunchyTuningConfig.getMixPreferredOptionForEntry(stack, effectiveEntry);
        }
        String resolvedName = CustomAnimationResolver.resolveSingleName(effectiveEntry);
        AnimationClip clip = null;
        if (resolvedName != null && !resolvedName.isBlank()) {
            clip = PunchyAnimationManager.resolveNamedClip(resolvedName, client, hand, preferredOption);
        }
        if (clip != null) {
            return new ClipLookup(clip, effectiveEntry, resolvedName, false);
        }
        PunchyAnimationManager.ClipResolution resolution = PunchyAnimationManager.resolveTypeFallbackClipWithSource(targetType, client, hand, preferredOption);
        if (resolution != null && resolution.clip() != null) {
            return new ClipLookup(resolution.clip(), effectiveEntry, resolution.clip().getName(), true);
        }
        return new ClipLookup(null, effectiveEntry, resolvedName, false);
    }

    static ClipLookup resolveNamedOrTypeFallback(class_1799 stack, String targetType, class_310 client, class_1268 hand) {
        return CustomAnimationResolver.resolveNamedOrTypeFallback(stack, targetType, CustomAnimationResolver.findEntry(stack, targetType), client, hand);
    }

    static void applySkipBlendOut(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (CustomAnimationResolver.shouldSkipBlendOut(entry)) {
            PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
        }
    }

    private static float resolveBlendFromCurrentOriginStartTime(AnimationClip clip) {
        if (clip == null) {
            return 0.0f;
        }
        float firstActiveTime = Float.POSITIVE_INFINITY;
        block0: for (BoneTrack track : clip.getTracks().values()) {
            if (track == null) continue;
            for (Keyframe keyframe : track.getKeyframes()) {
                float time;
                if (keyframe == null || (time = keyframe.timeStamp()) <= 0.0f || CustomAnimationResolver.isNeutralKeyframe(keyframe)) continue;
                firstActiveTime = Math.min(firstActiveTime, time);
                continue block0;
            }
        }
        if (!Float.isFinite(firstActiveTime)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(firstActiveTime, clip.getLength()));
    }

    private static boolean isNeutralKeyframe(Keyframe keyframe) {
        return CustomAnimationResolver.isZeroVector(keyframe.position()) && CustomAnimationResolver.isZeroVector(keyframe.rotationEuler()) && CustomAnimationResolver.isIdentityScale(keyframe.scale());
    }

    private static boolean isZeroVector(Vector3f vector) {
        return vector == null || Math.abs(vector.x) < 1.0E-4f && Math.abs(vector.y) < 1.0E-4f && Math.abs(vector.z) < 1.0E-4f;
    }

    private static boolean isIdentityScale(Vector3f vector) {
        return vector == null || Math.abs(vector.x - 1.0f) < 1.0E-4f && Math.abs(vector.y - 1.0f) < 1.0E-4f && Math.abs(vector.z - 1.0f) < 1.0E-4f;
    }

    private static void addIfPresent(List<String> names, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (!trimmed.isEmpty()) {
            names.add(trimmed);
        }
    }

    private static String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        if ("miningv2".equals(normalized) || "mining".equals(normalized)) {
            return "mining";
        }
        return normalized;
    }

    private static boolean isGlobalHandEquipType(String type) {
        return false;
    }

    private static boolean optionMatchesPreferred(String resolvedOption, String preferredOption) {
        if (resolvedOption == null || resolvedOption.isBlank() || preferredOption == null || preferredOption.isBlank()) {
            return false;
        }
        return resolvedOption.trim().equalsIgnoreCase(preferredOption.trim());
    }

    private static boolean matchesInteractionBlocks(List<String> blocks, class_2680 state, JsonObject requiredState) {
        if (blocks == null || blocks.isEmpty()) {
            return CustomAnimationResolver.matchesBlockState(state, requiredState);
        }
        if (state == null) {
            return false;
        }
        class_2960 blockId = class_7923.field_41175.method_10221((Object)state.method_26204());
        if (blockId == null) {
            return false;
        }
        String current = blockId.toString();
        for (String entry : blocks) {
            class_2960 parsed;
            String trimmed;
            if (entry == null || (trimmed = entry.trim()).isEmpty() || (parsed = class_2960.method_12829((String)trimmed)) == null || !current.equals(parsed.toString())) continue;
            return CustomAnimationResolver.matchesBlockState(state, requiredState);
        }
        return false;
    }

    private static boolean matchesBlockState(class_2680 state, JsonObject required) {
        if (required == null || required.isEmpty()) {
            return true;
        }
        if (state == null) {
            return false;
        }
        for (Map.Entry entry : required.entrySet()) {
            String key = (String)entry.getKey();
            if (key == null || key.isBlank()) continue;
            class_2769<?> property = CustomAnimationResolver.findProperty(state, key.trim());
            if (property == null) {
                return false;
            }
            JsonElement desiredElement = (JsonElement)entry.getValue();
            if (desiredElement == null || !desiredElement.isJsonPrimitive()) {
                return false;
            }
            String desired = desiredElement.getAsString();
            if (desired == null) {
                return false;
            }
            String actual = String.valueOf(state.method_11654(property));
            if (desired.trim().equalsIgnoreCase(actual)) continue;
            return false;
        }
        return true;
    }

    private static class_2769<?> findProperty(class_2680 state, String name) {
        for (class_2769 property : state.method_28501()) {
            if (!property.method_11899().equalsIgnoreCase(name)) continue;
            return property;
        }
        return null;
    }

    private static int triggerSpecificityScore(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return 0;
        }
        int score = 0;
        if (entry.items != null && !entry.items.isEmpty()) {
            score += 100;
        }
        if (entry.in_water != null) {
            ++score;
        }
        if (entry.inside_water != null) {
            ++score;
        }
        if (entry.block_detection != null) {
            ++score;
        }
        if (entry.weather_detection != null) {
            ++score;
        }
        if (entry.time_detection != null) {
            ++score;
        }
        if (entry.biome_detection != null) {
            ++score;
        }
        return score;
    }

    private static int entrySpecificityScore(PunchyTuningConfig.CustomAnimationEntry entry) {
        return CustomAnimationResolver.triggerSpecificityScore(entry);
    }

    private static boolean matchesWorldTriggers(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return false;
        }
        if (entry.in_water == null && entry.inside_water == null && entry.block_detection == null && entry.weather_detection == null && entry.time_detection == null && entry.biome_detection == null) {
            return true;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_746 player = client.field_1724;
        class_638 level = client.field_1687;
        if (level == null) {
            return false;
        }
        if (entry.in_water != null) {
            boolean inWater = CustomAnimationResolver.isInWaterContact((class_1937)level, (class_1657)player);
            if (entry.in_water != inWater) {
                return false;
            }
        }
        if (entry.inside_water != null) {
            boolean inside = player.method_5777(class_3486.field_15517);
            if (entry.inside_water != inside) {
                return false;
            }
        }
        if (entry.weather_detection != null && !CustomAnimationResolver.matchesWeather(entry.weather_detection, (class_1937)level)) {
            return false;
        }
        if (entry.time_detection != null && !CustomAnimationResolver.matchesTime(entry.time_detection, (class_1937)level)) {
            return false;
        }
        if (entry.block_detection != null && !CustomAnimationResolver.matchesBlock(entry.block_detection, (class_1937)level, player.method_24515())) {
            return false;
        }
        return entry.biome_detection == null || CustomAnimationResolver.matchesBiome(entry.biome_detection, (class_1937)level, player.method_24515());
    }

    private static boolean isInWaterContact(class_1937 level, class_1657 player) {
        if (level == null || player == null) {
            return false;
        }
        if (player.method_5799() || player.method_5777(class_3486.field_15517)) {
            return true;
        }
        double x = player.method_23317();
        double y = player.method_23318();
        double z = player.method_23321();
        double[] ys = new double[]{y + 0.05, y + 0.6, y + 1.1};
        double[] xs = new double[]{x, x - 0.3, x + 0.3};
        double[] zs = new double[]{z, z - 0.3, z + 0.3};
        for (double sampleY : ys) {
            for (double sampleX : xs) {
                for (double sampleZ : zs) {
                    if (!level.method_8316(class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ)).method_15767(class_3486.field_15517)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesWeather(PunchyTuningConfig.WeatherDetection weather, class_1937 level) {
        if (weather == null || level == null) {
            return true;
        }
        boolean raining = level.method_8419();
        boolean thundering = level.method_8546();
        if (weather.raining != null && weather.raining != raining) {
            return false;
        }
        if (weather.thundering != null && weather.thundering != thundering) {
            return false;
        }
        if (weather.clear != null) {
            boolean clear;
            boolean bl = clear = !raining && !thundering;
            if (weather.clear != clear) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesTime(PunchyTuningConfig.TimeDetection time, class_1937 level) {
        int max;
        if (time == null || level == null) {
            return true;
        }
        int min = time.min != null ? time.min : -1;
        int n = max = time.max != null ? time.max : -1;
        if (time.range != null && time.range.size() >= 2) {
            min = time.range.get(0);
            max = time.range.get(1);
        }
        if (min < 0 && max < 0) {
            return true;
        }
        int dayTime = (int)(level.method_8532() % 24000L);
        if (min < 0) {
            return dayTime <= max;
        }
        if (max < 0) {
            return dayTime >= min;
        }
        if (min <= max) {
            return dayTime >= min && dayTime <= max;
        }
        return dayTime >= min || dayTime <= max;
    }

    private static boolean matchesBlock(PunchyTuningConfig.BlockDetection block, class_1937 level, class_2338 origin) {
        if (block == null || level == null || origin == null) {
            return true;
        }
        List<String> targets = block.blocks;
        if ((targets == null || targets.isEmpty()) && block.block != null) {
            targets = List.of(block.block);
        }
        if (targets == null || targets.isEmpty()) {
            return true;
        }
        int radius = block.radius != null ? Math.max(0, block.radius) : 0;
        int offX = block.offsetX != null ? block.offsetX : 0;
        int offY = block.offsetY != null ? block.offsetY : 0;
        int offZ = block.offsetZ != null ? block.offsetZ : 0;
        class_2338 center = origin.method_10069(offX, offY, offZ);
        if (radius == 0) {
            return CustomAnimationResolver.matchesBlockList(level.method_8320(center), targets);
        }
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    cursor.method_10103(center.method_10263() + dx, center.method_10264() + dy, center.method_10260() + dz);
                    if (!CustomAnimationResolver.matchesBlockList(level.method_8320((class_2338)cursor), targets)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesBiome(PunchyTuningConfig.BiomeDetection biome, class_1937 level, class_2338 origin) {
        if (biome == null || level == null || origin == null) {
            return true;
        }
        List<String> targets = biome.biomes;
        if ((targets == null || targets.isEmpty()) && biome.biome != null) {
            targets = List.of(biome.biome);
        }
        if (targets == null || targets.isEmpty()) {
            return true;
        }
        int radius = biome.radius != null ? Math.max(0, biome.radius) : 0;
        int offX = biome.offsetX != null ? biome.offsetX : 0;
        int offY = biome.offsetY != null ? biome.offsetY : 0;
        int offZ = biome.offsetZ != null ? biome.offsetZ : 0;
        class_2338 center = origin.method_10069(offX, offY, offZ);
        if (radius == 0) {
            return CustomAnimationResolver.matchesBiomeList(level, center, targets);
        }
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    cursor.method_10103(center.method_10263() + dx, center.method_10264() + dy, center.method_10260() + dz);
                    if (!CustomAnimationResolver.matchesBiomeList(level, (class_2338)cursor, targets)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesBiomeList(class_1937 level, class_2338 pos, List<String> targets) {
        if (level == null || pos == null || targets == null || targets.isEmpty()) {
            return false;
        }
        class_6880 holder = level.method_23753(pos);
        for (String entry : targets) {
            class_5321 key;
            class_2960 parsed;
            class_6862 tag;
            class_2960 tagId;
            String trimmed;
            if (entry == null || entry.isBlank() || !((trimmed = entry.trim()).startsWith("#") ? (tagId = class_2960.method_12829((String)trimmed.substring(1))) != null && holder.method_40220(tag = class_6862.method_40092((class_5321)class_7924.field_41236, (class_2960)tagId)) : (parsed = class_2960.method_12829((String)trimmed)) != null && holder.method_40225(key = class_5321.method_29179((class_5321)class_7924.field_41236, (class_2960)parsed)))) continue;
            return true;
        }
        return false;
    }

    private static boolean matchesBlockList(class_2680 state, List<String> targets) {
        if (state == null || targets == null || targets.isEmpty()) {
            return false;
        }
        class_2960 id = class_7923.field_41175.method_10221((Object)state.method_26204());
        String current = id == null ? null : id.toString();
        for (String entry : targets) {
            class_2960 parsed;
            class_6862 tag;
            class_2960 tagId;
            String trimmed;
            if (entry == null || entry.isBlank() || !((trimmed = entry.trim()).startsWith("#") ? (tagId = class_2960.method_12829((String)trimmed.substring(1))) != null && state.method_26164(tag = class_6862.method_40092((class_5321)class_7924.field_41254, (class_2960)tagId)) : (parsed = class_2960.method_12829((String)trimmed)) != null && current != null && current.equals(parsed.toString()))) continue;
            return true;
        }
        return false;
    }

    private static boolean matchesItemList(class_1799 stack, List<String> items) {
        if (stack == null || stack.method_7960() || items == null || items.isEmpty()) {
            return false;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId == null) {
            return false;
        }
        String current = itemId.toString();
        for (String entry : items) {
            class_2960 parsed;
            class_6862 tag;
            class_2960 tagId;
            String trimmed;
            if (entry == null || entry.isBlank() || !((trimmed = entry.trim()).startsWith("#") ? (tagId = class_2960.method_12829((String)trimmed.substring(1))) != null && stack.method_31573(tag = class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)tagId)) : (parsed = class_2960.method_12829((String)trimmed)) != null && current.equals(parsed.toString()))) continue;
            return true;
        }
        return false;
    }

    record ClipLookup(AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry, String resolvedName, boolean usedTypeFallback) {
    }
}

