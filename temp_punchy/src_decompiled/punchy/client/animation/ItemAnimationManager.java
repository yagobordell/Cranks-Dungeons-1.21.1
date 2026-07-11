/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2680
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 *  net.minecraft.class_5321
 *  net.minecraft.class_630
 *  net.minecraft.class_638
 *  net.minecraft.class_6862
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 *  net.minecraft.class_7924
 */
package punchy.client.animation;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_5321;
import net.minecraft.class_630;
import net.minecraft.class_638;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.bedrockitems.BedrockItemAnimationManager;
import punchy.client.modelparts.ModelPartsParticleSystem;
import punchy.client.render.ToolTuning;
import punchy.client.state.MovementStateMachine;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public final class ItemAnimationManager {
    private static final EnumMap<class_1306, PoseHandler> HANDLERS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, AnimationClip> ACTIVE_CLIPS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, String> ACTIVE_TYPES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, PunchyTuningConfig.CustomAnimationEntry> ACTIVE_ENTRIES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Boolean> SWIM_SUPPRESSION = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, AnimationClip> IDLE_CLIPS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Float> IDLE_TIMES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1268, ForcedAction> FORCED_TYPES = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, PersistentAction> PERSISTENT_ACTIONS = new EnumMap(class_1268.class);
    private static final float ITEM_BLEND_IN_SECONDS = 0.0f;
    private static final float ITEM_BLEND_TRANSITION_SECONDS = 0.4f;
    private static final float ITEM_BLEND_OUT_SECONDS = 3.0f;
    private static final float ITEM_BLEND_OUT_SLOW = 3.6f;
    private static final float ITEM_BASE_DELTA_SECONDS = 0.05f;

    private ItemAnimationManager() {
    }

    public static void forceTypeOnce(class_1268 hand, String type) {
        ItemAnimationManager.forceTypeOnce(hand, type, false);
    }

    public static void forceTypeOnce(class_1268 hand, String type, boolean immediate) {
        if (type == null || type.isBlank()) {
            return;
        }
        if (hand == null) {
            FORCED_TYPES.put(class_1268.field_5808, new ForcedAction(type, immediate));
            FORCED_TYPES.put(class_1268.field_5810, new ForcedAction(type, immediate));
            BedrockItemAnimationManager.forceTypeOnce(class_1268.field_5808, type, immediate);
            BedrockItemAnimationManager.forceTypeOnce(class_1268.field_5810, type, immediate);
            return;
        }
        FORCED_TYPES.put(hand, new ForcedAction(type, immediate));
        BedrockItemAnimationManager.forceTypeOnce(hand, type, immediate);
    }

    public static void setPersistentType(class_1268 hand, String type, Boolean loopOverride) {
        ItemAnimationManager.setPersistentType(hand, type, loopOverride, false);
    }

    public static void setPersistentType(class_1268 hand, String type, Boolean loopOverride, boolean immediate) {
        if (hand == null || type == null || type.isBlank()) {
            return;
        }
        PERSISTENT_ACTIONS.put(hand, new PersistentAction(type, loopOverride, immediate));
        BedrockItemAnimationManager.setPersistentType(hand, type, loopOverride, immediate);
    }

    public static void clearPersistentType(class_1268 hand, String type) {
        if (hand == null) {
            return;
        }
        PersistentAction current = PERSISTENT_ACTIONS.get(hand);
        if (current == null) {
            return;
        }
        if (type == null || type.isBlank() || type.equals(current.type)) {
            PERSISTENT_ACTIONS.remove(hand);
            BedrockItemAnimationManager.clearPersistentType(hand, type);
        }
    }

    public static void clearAllPersistentTypes() {
        PERSISTENT_ACTIONS.clear();
        BedrockItemAnimationManager.clearAllPersistentTypes();
    }

    public static boolean isTypeActive(String type) {
        if (type == null || type.isBlank()) {
            return false;
        }
        for (class_1306 arm : class_1306.values()) {
            PoseHandler handler;
            String activeType = ACTIVE_TYPES.get(arm);
            if (!type.equals(activeType) || (handler = HANDLERS.get(arm)) == null || !handler.isPlaying()) continue;
            return true;
        }
        return false;
    }

    public static boolean isAnyDualHandedActive() {
        for (class_1306 arm : class_1306.values()) {
            PoseHandler handler = HANDLERS.get(arm);
            if (handler == null) continue;
            if (handler.isPlaying() && handler.isCurrentDualHandedActive()) {
                return true;
            }
            AnimationClip secondary = handler.getSecondaryClip();
            if (secondary == null || !PunchyAnimationManager.isDualHandedActive(secondary, handler.getSecondaryTime())) continue;
            return true;
        }
        return false;
    }

    public static void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        float speed = PunchyAnimationManager.ANIMATION_SPEED;
        for (class_1306 arm : class_1306.values()) {
            boolean allowActiveToFinish;
            SWIM_SUPPRESSION.put(arm, false);
            PoseHandler handler = HANDLERS.computeIfAbsent(arm, key -> new PoseHandler());
            boolean swapTranslationYZ = true;
            boolean javaBlock = false;
            class_1799 stack = ItemAnimationManager.resolveStack(client, arm);
            class_1268 hand = PunchyAnimationManager.getHandForArm(client, arm);
            if (PunchyConfig.isHandBlacklisted((class_1657)client.field_1724, hand)) {
                FORCED_TYPES.remove(hand);
                PERSISTENT_ACTIONS.remove(hand);
                IDLE_CLIPS.remove(arm);
                IDLE_TIMES.remove(arm);
                if (handler.isPlaying()) {
                    ItemAnimationManager.logPlaybackOnce(arm, stack, ACTIVE_TYPES.get(arm), ACTIVE_CLIPS.get(arm), ACTIVE_ENTRIES.get(arm), null, false, false, "blacklist_stop");
                    handler.setLooping(false);
                    handler.stopImmediateWithBlendOut();
                }
                ACTIVE_CLIPS.put(arm, null);
                ACTIVE_TYPES.remove(arm);
                ACTIVE_ENTRIES.remove(arm);
                ModelPartsParticleSystem.resetArm(arm);
                continue;
            }
            List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomItemAnimation(stack);
            if (ItemAnimationManager.hasItemSuppression(stack, entries, true)) {
                SWIM_SUPPRESSION.put(arm, true);
            }
            ActionState desiredState = ItemAnimationManager.resolveDesiredState(client, arm, stack, entries);
            String type = desiredState.type;
            AnimationClip desired = ItemAnimationManager.resolveDesiredClip(client, arm, stack, entries, type);
            AnimationClip idleClip = ItemAnimationManager.resolveDesiredClip(client, arm, stack, entries, "idle");
            float idleTime = ItemAnimationManager.updateIdleTime(arm, handler, idleClip, 0.05f, speed);
            PunchyTuningConfig.CustomAnimationEntry entry = ItemAnimationManager.resolveEntry(stack, entries, type, client, arm);
            if (entry != null && entry.isJavaBlock != null) {
                javaBlock = entry.isJavaBlock;
            }
            if (entry != null) {
                handler.setLoopSpeedParams(entry.loop_speed_max, entry.loop_speed_ramp);
            } else {
                handler.resetLoopSpeedParams();
            }
            if (entry != null && Boolean.TRUE.equals(entry.disable_swim_animation)) {
                SWIM_SUPPRESSION.put(arm, true);
            }
            handler.setSwapTranslationYZ(swapTranslationYZ);
            handler.setJavaBlock(javaBlock);
            AnimationClip active = ACTIVE_CLIPS.get(arm);
            String activeType = ACTIVE_TYPES.get(arm);
            boolean bl = allowActiveToFinish = active != null && handler.isPlaying() && handler.getCurrentClip() == active && !active.isLoop() && !active.isHoldOnLastFrame() && !handler.isForceLooping() && (desired == null || "idle".equals(type)) && !"falling".equals(activeType);
            if (allowActiveToFinish) {
                desired = active;
            }
            if (!stack.method_31574(class_1802.field_8378) && ItemAnimationManager.shouldSuppressMeshAnimation(desired)) {
                if (active != null) {
                    ItemAnimationManager.logPlaybackOnce(arm, stack, activeType, active, ACTIVE_ENTRIES.get(arm), null, false, false, "mesh_suppressed");
                    handler.setBlendOutDurationNext(3.0f);
                    handler.setBlendOutSlowNext(3.6f);
                    handler.setLooping(false);
                    handler.stopImmediateWithBlendOut();
                    ACTIVE_CLIPS.put(arm, null);
                    ACTIVE_TYPES.remove(arm);
                }
                ModelPartsParticleSystem.clearArmAnimationState(arm);
                continue;
            }
            if (desired != active) {
                boolean forceCutFromActive;
                PunchyTuningConfig.CustomAnimationEntry activeEntry = ACTIVE_ENTRIES.get(arm);
                boolean bl2 = forceCutFromActive = activeEntry != null && Boolean.TRUE.equals(activeEntry.skip_blend_out);
                if (desired != null) {
                    boolean skipBlendIn;
                    handler.setActiveArm(null);
                    boolean loop = desiredState.loopOverride != null ? desiredState.loopOverride : ItemAnimationManager.shouldLoop(desired, type);
                    handler.setLooping(loop);
                    float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(desired);
                    boolean hasPrevious = handler.isPlaying() && handler.getCurrentClip() != null;
                    boolean isIdleDesired = desired == idleClip;
                    boolean bl3 = skipBlendIn = entry != null && Boolean.TRUE.equals(entry.skip_blend_in);
                    if (forceCutFromActive) {
                        skipBlendIn = true;
                    }
                    if (skipBlendIn) {
                        handler.setBlendInDurationNext(0.0f);
                    }
                    if (desiredState.immediate) {
                        if (hasPrevious) {
                            if (!skipBlendIn) {
                                handler.setBlendInDurationNext(0.4f);
                            }
                            if (isIdleDesired) {
                                if (skipBlendIn) {
                                    handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                                } else {
                                    handler.playAtTimeWithBlendFromCurrent(desired, speedMultiplier, desired.isHoldOnLastFrame(), idleTime);
                                }
                            } else if (skipBlendIn) {
                                handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                            } else {
                                handler.playWithBlendFromCurrentDynamic(desired, speedMultiplier, desired.isHoldOnLastFrame());
                            }
                        } else {
                            if (!skipBlendIn) {
                                handler.setBlendInDurationNext(0.0f);
                            }
                            if (isIdleDesired) {
                                handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                            } else {
                                handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                            }
                        }
                    } else {
                        if (!skipBlendIn) {
                            handler.setBlendInDurationNext(hasPrevious ? 0.4f : 0.0f);
                        }
                        if (isIdleDesired) {
                            if (skipBlendIn) {
                                handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                            } else {
                                handler.playAtTimeWithBlendFromCurrent(desired, speedMultiplier, desired.isHoldOnLastFrame(), idleTime);
                            }
                        } else if (skipBlendIn) {
                            handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                        } else {
                            handler.playWithBlendFromCurrentDynamic(desired, speedMultiplier, desired.isHoldOnLastFrame());
                        }
                    }
                    if (!(isIdleDesired || activeType != null && activeType.equals(type))) {
                        MovementStateMachine.get().triggerActionAnimation(arm);
                    }
                    ACTIVE_TYPES.put(arm, type);
                    ACTIVE_ENTRIES.put(arm, entry);
                    ItemAnimationManager.logPlaybackOnce(arm, stack, type, desired, entry, PunchyTuningConfig.getMixPreferredOptionForEntry(stack, entry), loop, desiredState.immediate, hasPrevious ? "switch" : "start");
                } else if (active != null) {
                    boolean skipBlendOut = activeEntry != null && Boolean.TRUE.equals(activeEntry.skip_blend_out);
                    ItemAnimationManager.logPlaybackOnce(arm, stack, activeType, active, activeEntry, PunchyTuningConfig.getMixPreferredOptionForEntry(stack, activeEntry), false, false, skipBlendOut ? "stop_skip_blend" : "stop_blend_out");
                    if (skipBlendOut) {
                        handler.setBlendOutDurationNext(0.0f);
                    } else {
                        handler.setBlendOutDurationNext(3.0f);
                    }
                    handler.setBlendOutSlowNext(3.6f);
                    handler.setLooping(false);
                    handler.stopImmediateWithBlendOut();
                    ACTIVE_TYPES.remove(arm);
                    ACTIVE_ENTRIES.remove(arm);
                }
                ACTIVE_CLIPS.put(arm, desired);
            }
            handler.setMirrorX(arm == class_1306.field_6182);
            ItemAnimationManager.syncIdleOverlay(handler, arm, idleClip, idleTime, ACTIVE_CLIPS.get(arm));
            handler.tick(0.05f, speed);
            ModelPartsParticleSystem.onHandlerTick(client, arm, handler);
        }
    }

    public static void applyToParts(class_1306 arm, Map<String, class_630> parts, float partialTicks) {
        if (arm == null || parts == null || parts.isEmpty()) {
            return;
        }
        PoseHandler handler = HANDLERS.get(arm);
        if (handler == null) {
            return;
        }
        handler.setMirrorX(arm == class_1306.field_6182);
        handler.applyTransformations(parts, partialTicks, true);
    }

    private static AnimationClip resolveDesiredClip(class_310 client, class_1306 arm, class_1799 stack, String type) {
        return ItemAnimationManager.resolveDesiredClip(client, arm, stack, ToolTuning.getCustomItemAnimation(stack), type);
    }

    private static AnimationClip resolveDesiredClip(class_310 client, class_1306 arm, class_1799 stack, List<PunchyTuningConfig.CustomAnimationEntry> entries, String type) {
        String suffix;
        PunchyAnimationManager.ClipResolution resolution;
        if (type == null) {
            return null;
        }
        if (stack == null || stack.method_7960()) {
            return null;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = ItemAnimationManager.resolveEntry(stack, entries, type, client, arm);
        String name = ItemAnimationManager.resolveEntryName(entry);
        class_1268 hand = PunchyAnimationManager.getHandForArm(client, arm);
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForEntry(stack, entry);
        if (name == null || name.isBlank()) {
            PunchyAnimationManager.ClipResolution fallback = PunchyAnimationManager.resolveTypeFallbackClipWithSource(type, client, hand, preferredOption);
            if (fallback != null && fallback.clip() != null) {
                ItemAnimationManager.logResolutionOnce(arm, stack, type, type, preferredOption, entry, fallback);
                return fallback.clip();
            }
            return null;
        }
        if (arm != null && (resolution = PunchyAnimationManager.resolveNamedClipWithSource(name + (suffix = arm == class_1306.field_6182 ? "_left" : "_right"), client, hand, preferredOption)) != null && resolution.clip() != null) {
            ItemAnimationManager.logResolutionOnce(arm, stack, type, name + suffix, preferredOption, entry, resolution);
            return resolution.clip();
        }
        resolution = PunchyAnimationManager.resolveNamedClipWithSource(name, client, hand, preferredOption);
        if (resolution != null && resolution.clip() != null) {
            ItemAnimationManager.logResolutionOnce(arm, stack, type, name, preferredOption, entry, resolution);
            return resolution.clip();
        }
        return null;
    }

    private static void logResolutionOnce(class_1306 arm, class_1799 stack, String type, String clipLookupName, String preferredOption, PunchyTuningConfig.CustomAnimationEntry entry, PunchyAnimationManager.ClipResolution resolution) {
    }

    private static void logPlaybackOnce(class_1306 arm, class_1799 stack, String type, AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry, String preferredOption, boolean loop, boolean immediate, String note) {
    }

    private static float updateIdleTime(class_1306 arm, PoseHandler handler, AnimationClip idleClip, float baseDeltaSeconds, float speed) {
        float idleTime;
        if (idleClip == null) {
            IDLE_CLIPS.remove(arm);
            IDLE_TIMES.remove(arm);
            return 0.0f;
        }
        AnimationClip previous = IDLE_CLIPS.put(arm, idleClip);
        Float stored = IDLE_TIMES.get(arm);
        float f = idleTime = stored == null ? 0.0f : stored.floatValue();
        if (previous != idleClip) {
            idleTime = 0.0f;
        }
        if (handler != null && handler.isPlaying() && handler.getCurrentClip() == idleClip) {
            idleTime = handler.getCurrentTime();
        } else if (handler != null && handler.getSecondaryClip() == idleClip) {
            idleTime = handler.getSecondaryTime();
        } else {
            float delta = baseDeltaSeconds * speed * PunchyAnimationManager.clipSpeedMultiplier(idleClip);
            idleTime = ItemAnimationManager.advanceLoopTime(idleClip, idleTime, delta);
        }
        IDLE_TIMES.put(arm, Float.valueOf(idleTime));
        return idleTime;
    }

    private static void syncIdleOverlay(PoseHandler handler, class_1306 arm, AnimationClip idleClip, float idleTime, AnimationClip primaryClip) {
        if (handler == null) {
            return;
        }
        if (idleClip == null || primaryClip == null || primaryClip == idleClip) {
            if (handler.getSecondaryClip() != null) {
                handler.clearSecondaryClip();
            }
            return;
        }
        if (handler.getSecondaryClip() != idleClip) {
            handler.setSecondaryClipAtTime(idleClip, arm, PunchyAnimationManager.clipSpeedMultiplier(idleClip), idleTime);
        }
    }

    private static float advanceLoopTime(AnimationClip clip, float time, float delta) {
        if (clip == null) {
            return 0.0f;
        }
        float length = clip.getLength();
        if (length <= 0.0f) {
            return 0.0f;
        }
        float next = time + delta;
        if (clip.isLoop()) {
            if ((next %= length) < 0.0f) {
                next += length;
            }
            return next;
        }
        return Math.min(next, length);
    }

    private static ActionState resolveDesiredState(class_310 client, class_1306 arm, class_1799 stack) {
        return ItemAnimationManager.resolveDesiredState(client, arm, stack, ToolTuning.getCustomItemAnimation(stack));
    }

    private static ActionState resolveDesiredState(class_310 client, class_1306 arm, class_1799 stack, List<PunchyTuningConfig.CustomAnimationEntry> entries) {
        if (client == null || client.field_1724 == null) {
            return ActionState.EMPTY;
        }
        if (!ItemAnimationManager.shouldAnimateArm(arm)) {
            return ActionState.EMPTY;
        }
        ForcedAction forced = ItemAnimationManager.consumeForcedTypeForArm(client, arm);
        if (forced != null) {
            return new ActionState(ItemAnimationManager.resolveTypeWithFallback(stack, entries, forced.type), null, forced.immediate);
        }
        PersistentAction persistent = ItemAnimationManager.getPersistentForArm(client, arm);
        if (persistent != null) {
            return new ActionState(ItemAnimationManager.resolveTypeWithFallback(stack, entries, persistent.type), persistent.loopOverride, persistent.immediate);
        }
        return new ActionState(ItemAnimationManager.resolveTypeWithFallback(stack, entries, "idle"), null, false);
    }

    private static ForcedAction consumeForcedTypeForArm(class_310 client, class_1306 arm) {
        class_1268 hand = PunchyAnimationManager.getHandForArm(client, arm);
        return FORCED_TYPES.remove(hand);
    }

    private static PersistentAction getPersistentForArm(class_310 client, class_1306 arm) {
        class_1268 hand = PunchyAnimationManager.getHandForArm(client, arm);
        return PERSISTENT_ACTIONS.get(hand);
    }

    private static boolean shouldAnimateArm(class_1306 arm) {
        return arm != null;
    }

    private static class_1799 resolveStack(class_310 client, class_1306 arm) {
        boolean mainRight;
        if (client == null || client.field_1724 == null) {
            return class_1799.field_8037;
        }
        boolean bl = mainRight = client.field_1724.method_6068() == class_1306.field_6183;
        return arm == class_1306.field_6183 ? (mainRight ? client.field_1724.method_6047() : client.field_1724.method_6079()) : (mainRight ? client.field_1724.method_6079() : client.field_1724.method_6047());
    }

    private static boolean shouldLoop(AnimationClip clip, String type) {
        if (clip == null) {
            return false;
        }
        if (type == null) {
            return false;
        }
        return "mining".equals(type) || "useItem".equals(type) || "idle".equals(type);
    }

    private static boolean shouldSuppressMeshAnimation(AnimationClip desired) {
        float lastStart;
        if (desired == null) {
            return false;
        }
        if (!(desired.hasEffectAny("suppress_mesh_animation") || desired.hasEffectAny("start_mesh_animation") || desired.hasEffectAny("loadstart") || desired.hasEffectAny("load_start"))) {
            return false;
        }
        AnimationClip poseClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (poseClip == null || poseClip != desired) {
            return false;
        }
        float time = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
        float lastSuppress = ItemAnimationManager.getLastEffectTime(desired, "suppress_mesh_animation", time);
        if (lastSuppress > (lastStart = Math.max(ItemAnimationManager.getLastEffectTime(desired, "start_mesh_animation", time), Math.max(ItemAnimationManager.getLastEffectTime(desired, "loadstart", time), ItemAnimationManager.getLastEffectTime(desired, "load_start", time))))) {
            return true;
        }
        return lastStart < 0.0f;
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

    private static String resolveTypeWithFallback(class_1799 stack, String type) {
        return ItemAnimationManager.resolveTypeWithFallback(stack, ToolTuning.getCustomItemAnimation(stack), type);
    }

    private static String resolveTypeWithFallback(class_1799 stack, List<PunchyTuningConfig.CustomAnimationEntry> entries, String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        if (ItemAnimationManager.hasEntry(stack, entries, type) || PunchyAnimationManager.hasTypeFallbackClip(type, preferredOption)) {
            return type;
        }
        if (!"idle".equals(type) && (ItemAnimationManager.hasEntry(stack, entries, "idle") || PunchyAnimationManager.hasTypeFallbackClip("idle", preferredOption))) {
            return "idle";
        }
        return null;
    }

    private static PunchyTuningConfig.CustomAnimationEntry resolveEntry(class_1799 stack, String targetType, class_310 client, class_1306 arm) {
        return ItemAnimationManager.resolveEntry(stack, ToolTuning.getCustomItemAnimation(stack), targetType, client, arm);
    }

    private static PunchyTuningConfig.CustomAnimationEntry resolveEntry(class_1799 stack, List<PunchyTuningConfig.CustomAnimationEntry> entries, String targetType, class_310 client, class_1306 arm) {
        if (stack == null || stack.method_7960() || targetType == null || targetType.isBlank()) {
            return null;
        }
        if (entries == null || entries.isEmpty()) {
            ItemAnimationManager.logEntrySelectionOnce(arm, stack, targetType, entries, null, "no_entries");
            return null;
        }
        String normalizedTarget = ItemAnimationManager.normalizeType(targetType);
        PunchyTuningConfig.CustomAnimationEntry bestTriggered = null;
        int bestTriggeredScore = -1;
        PunchyTuningConfig.CustomAnimationEntry bestFallback = null;
        int bestFallbackScore = -1;
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.type == null || !normalizedTarget.equals(ItemAnimationManager.normalizeType(entry.type)) || entry.items != null && !entry.items.isEmpty() && !ItemAnimationManager.matchesItemList(stack, entry.items) || !ItemAnimationManager.matchesWorldTriggers(entry, client, arm)) continue;
            int score = ItemAnimationManager.triggerSpecificityScore(entry);
            if (score > 0) {
                if (score <= bestTriggeredScore) continue;
                bestTriggered = entry;
                bestTriggeredScore = score;
                continue;
            }
            if (score <= bestFallbackScore) continue;
            bestFallback = entry;
            bestFallbackScore = score;
        }
        PunchyTuningConfig.CustomAnimationEntry resolved = bestTriggered != null ? bestTriggered : bestFallback;
        ItemAnimationManager.logEntrySelectionOnce(arm, stack, targetType, entries, resolved, bestTriggered != null ? "triggered" : "fallback");
        return resolved;
    }

    private static void logEntrySelectionOnce(class_1306 arm, class_1799 stack, String targetType, List<PunchyTuningConfig.CustomAnimationEntry> entries, PunchyTuningConfig.CustomAnimationEntry resolved, String reason) {
    }

    private static String resolveEntryName(PunchyTuningConfig.CustomAnimationEntry entry) {
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

    private static boolean hasEntry(class_1799 stack, String targetType) {
        return ItemAnimationManager.hasEntry(stack, ToolTuning.getCustomItemAnimation(stack), targetType);
    }

    private static boolean hasEntry(class_1799 stack, List<PunchyTuningConfig.CustomAnimationEntry> entries, String targetType) {
        if (stack == null || stack.method_7960() || targetType == null || targetType.isBlank()) {
            return false;
        }
        if (entries == null || entries.isEmpty()) {
            return false;
        }
        String normalizedTarget = ItemAnimationManager.normalizeType(targetType);
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            String name;
            if (entry == null || entry.type == null || !normalizedTarget.equals(ItemAnimationManager.normalizeType(entry.type)) || (name = ItemAnimationManager.resolveEntryName(entry)) == null || name.isBlank()) continue;
            return true;
        }
        return false;
    }

    private static String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        return type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
    }

    public static boolean isSwimSuppressed(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        return Boolean.TRUE.equals(SWIM_SUPPRESSION.get(arm));
    }

    private static boolean hasItemSuppression(class_1799 stack, boolean swim) {
        return ItemAnimationManager.hasItemSuppression(stack, ToolTuning.getCustomItemAnimation(stack), swim);
    }

    private static boolean hasItemSuppression(class_1799 stack, List<PunchyTuningConfig.CustomAnimationEntry> entries, boolean swim) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (entries == null || entries.isEmpty()) {
            return false;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.items != null && !entry.items.isEmpty() && !ItemAnimationManager.matchesItemList(stack, entry.items) || !swim || !Boolean.TRUE.equals(entry.disable_swim_animation)) continue;
            return true;
        }
        return false;
    }

    private static int triggerSpecificityScore(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return 0;
        }
        int score = 0;
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

    private static boolean matchesWorldTriggers(PunchyTuningConfig.CustomAnimationEntry entry, class_310 client, class_1306 arm) {
        if (entry == null) {
            return false;
        }
        if (entry.in_water == null && entry.inside_water == null && entry.block_detection == null && entry.weather_detection == null && entry.time_detection == null && entry.biome_detection == null) {
            return true;
        }
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_746 player = client.field_1724;
        class_638 level = client.field_1687;
        if (level == null) {
            return false;
        }
        if (entry.in_water != null) {
            boolean inWater = ItemAnimationManager.isInWaterContact((class_1937)level, (class_1657)player);
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
        if (entry.weather_detection != null && !ItemAnimationManager.matchesWeather(entry.weather_detection, (class_1937)level)) {
            return false;
        }
        if (entry.time_detection != null && !ItemAnimationManager.matchesTime(entry.time_detection, (class_1937)level)) {
            return false;
        }
        if (entry.block_detection != null && !ItemAnimationManager.matchesBlock(entry.block_detection, (class_1937)level, player.method_24515())) {
            return false;
        }
        return entry.biome_detection == null || ItemAnimationManager.matchesBiome(entry.biome_detection, (class_1937)level, player.method_24515());
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
            return ItemAnimationManager.matchesBlockList(level.method_8320(center), targets);
        }
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    cursor.method_10103(center.method_10263() + dx, center.method_10264() + dy, center.method_10260() + dz);
                    if (!ItemAnimationManager.matchesBlockList(level.method_8320((class_2338)cursor), targets)) continue;
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
            return ItemAnimationManager.matchesBiomeList(level, center, targets);
        }
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    cursor.method_10103(center.method_10263() + dx, center.method_10264() + dy, center.method_10260() + dz);
                    if (!ItemAnimationManager.matchesBiomeList(level, (class_2338)cursor, targets)) continue;
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

    private record ForcedAction(String type, boolean immediate) {
    }

    private record PersistentAction(String type, Boolean loopOverride, boolean immediate) {
    }

    private record ActionState(String type, Boolean loopOverride, boolean immediate) {
        private static final ActionState EMPTY = new ActionState(null, null, false);
    }
}

