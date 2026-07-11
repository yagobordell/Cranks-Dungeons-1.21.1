/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_1839
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2561
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
 *  net.minecraft.class_9334
 */
package punchy.client.bedrockitems;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_1839;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
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
import net.minecraft.class_9334;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.bedrockitems.BedrockItemDefinition;
import punchy.client.bedrockitems.BedrockItemSystem;
import punchy.client.modelparts.ModelPartsParticleSystem;
import punchy.client.state.AttackActionTracker;
import punchy.client.state.ConsumeStateMachine;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.MovementStateMachine;
import punchy.client.state.SpearStateMachine;
import punchy.client.state.UseItemStateMachine;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public final class BedrockItemAnimationManager {
    private static final EnumMap<class_1306, PoseHandler> HANDLERS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, AnimationClip> ACTIVE_CLIPS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, String> ACTIVE_TYPES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, PunchyTuningConfig.CustomAnimationEntry> ACTIVE_ENTRIES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, PunchyTuningConfig.CustomAnimationEntry> CLIP_ENTRIES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, PunchyTuningConfig.CustomAnimationEntry> KEEP_LOOP_ACTIVE_ENTRY = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, String> KEEP_LOOP_ACTIVE_NAME = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Boolean> LAST_PLAYING = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Boolean> SWIM_SUPPRESSION = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Integer> RAIN_GRACE_TICKS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Integer> INSIDE_WATER_GRACE_TICKS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Integer> KEEP_LOOP_FALSE_GRACE_TICKS_MAP = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Boolean> ANIM_MIRROR = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, String> LAST_STACK_KEY = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, AnimationClip> IDLE_CLIPS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Float> IDLE_TIMES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_ATTACK_TRIGGER_EPOCH = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_VISUAL_USE_ITEM_EPOCH = new EnumMap(class_1306.class);
    private static final EnumMap<class_1268, ForcedAction> FORCED_TYPES = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, PersistentAction> PERSISTENT_ACTIONS = new EnumMap(class_1268.class);
    private static final float ITEM_BLEND_IN_SECONDS = 0.0f;
    private static final float ITEM_BLEND_TRANSITION_SECONDS = 0.4f;
    private static final float ITEM_KEEP_LOOP_BLEND_SECONDS = 0.55f;
    private static final float ITEM_IDLE_BLEND_SECONDS = 0.6f;
    private static final float ITEM_TRIGGER_BLEND_OUT_SECONDS = 1.0f;
    private static final float ITEM_INSPECT_BLEND_IN_SECONDS = 0.0f;
    private static final float ITEM_INSPECT_BLEND_OUT_SECONDS = 0.18f;
    private static final float ITEM_INSPECT_BLEND_OUT_SLOW = 1.0f;
    private static final int RAIN_FALSE_GRACE_TICKS = 100;
    private static final float ITEM_BLEND_OUT_SECONDS = 3.0f;
    private static final float ITEM_BLEND_OUT_SLOW = 3.6f;
    private static final float ITEM_BASE_DELTA_SECONDS = 0.05f;
    private static final int INSIDE_WATER_FALSE_GRACE_TICKS = 4;
    private static final int KEEP_LOOP_FALSE_GRACE_TICKS = 6;
    private static final String TYPE_USE_DRINK = "useDrink";
    private static final String TYPE_USE_EAT = "useEat";

    private BedrockItemAnimationManager() {
    }

    public static void forceTypeOnce(class_1268 hand, String type, boolean immediate) {
        if (type == null || type.isBlank()) {
            return;
        }
        if (hand == null) {
            BedrockItemAnimationManager.putForcedType(class_1268.field_5808, type, immediate);
            BedrockItemAnimationManager.putForcedType(class_1268.field_5810, type, immediate);
            return;
        }
        BedrockItemAnimationManager.putForcedType(hand, type, immediate);
    }

    public static void setPersistentType(class_1268 hand, String type, Boolean loopOverride, boolean immediate) {
        if (hand == null || type == null || type.isBlank()) {
            return;
        }
        PERSISTENT_ACTIONS.put(hand, new PersistentAction(type, loopOverride, immediate));
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
        }
    }

    public static void clearAllPersistentTypes() {
        PERSISTENT_ACTIONS.clear();
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
        for (class_1306 arm : class_1306.values()) {
            float remaining;
            boolean typeChanged;
            boolean restartVisualForcedUseItem;
            boolean repeatConsumeWhileHolding;
            PunchyTuningConfig.CustomAnimationEntry activeEntry;
            boolean preserveHoldOnLastFrameDuringFalling;
            boolean triggerInvalid;
            String name;
            SWIM_SUPPRESSION.put(arm, false);
            RAIN_GRACE_TICKS.putIfAbsent(arm, 0);
            INSIDE_WATER_GRACE_TICKS.putIfAbsent(arm, 0);
            KEEP_LOOP_FALSE_GRACE_TICKS_MAP.putIfAbsent(arm, 0);
            ANIM_MIRROR.putIfAbsent(arm, false);
            PoseHandler handler = HANDLERS.computeIfAbsent(arm, key -> new PoseHandler());
            boolean swapTranslationYZ = false;
            boolean javaBlock = false;
            handler.setSwapTranslationYZ(swapTranslationYZ);
            handler.setJavaBlock(javaBlock);
            boolean wasPlaying = Boolean.TRUE.equals(LAST_PLAYING.get(arm));
            class_1799 stack = BedrockItemAnimationManager.resolveStack(client, arm);
            class_1268 hand = PunchyAnimationManager.getHandForArm(client, arm);
            if (stack.method_7960() || PunchyConfig.isHandBlacklisted((class_1657)client.field_1724, hand)) {
                BedrockItemAnimationManager.stopHandler(handler, arm);
                CLIP_ENTRIES.remove(arm);
                KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                KEEP_LOOP_ACTIVE_NAME.remove(arm);
                LAST_STACK_KEY.remove(arm);
                LAST_ATTACK_TRIGGER_EPOCH.remove(arm);
                ModelPartsParticleSystem.resetArm(arm);
                continue;
            }
            if (BedrockItemSystem.get().isBlockedByCustomName(stack)) {
                BedrockItemAnimationManager.stopHandler(handler, arm);
                CLIP_ENTRIES.remove(arm);
                KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                KEEP_LOOP_ACTIVE_NAME.remove(arm);
                LAST_STACK_KEY.remove(arm);
                LAST_ATTACK_TRIGGER_EPOCH.remove(arm);
                ModelPartsParticleSystem.clearArmAnimationState(arm);
                continue;
            }
            BedrockItemDefinition def = BedrockItemSystem.get().resolveDefinition(stack);
            String currentKey = BedrockItemAnimationManager.getStackKey(stack, def);
            String lastKey = LAST_STACK_KEY.get(arm);
            if (currentKey != null && !currentKey.equals(lastKey)) {
                BedrockItemAnimationManager.stopHandler(handler, arm);
                ACTIVE_CLIPS.put(arm, null);
                ACTIVE_TYPES.remove(arm);
                ACTIVE_ENTRIES.remove(arm);
                CLIP_ENTRIES.remove(arm);
                KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                KEEP_LOOP_ACTIVE_NAME.remove(arm);
                ModelPartsParticleSystem.resetArm(arm);
            }
            LAST_STACK_KEY.put(arm, currentKey);
            if (def == null) {
                BedrockItemAnimationManager.stopHandler(handler, arm);
                CLIP_ENTRIES.remove(arm);
                KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                KEEP_LOOP_ACTIVE_NAME.remove(arm);
                ModelPartsParticleSystem.clearArmAnimationState(arm);
                continue;
            }
            if (BedrockItemAnimationManager.hasItemSuppression(def, stack, true)) {
                SWIM_SUPPRESSION.put(arm, true);
            }
            ActionState desiredState = BedrockItemAnimationManager.resolveDesiredState(client, arm, stack);
            String type = desiredState.type;
            PunchyTuningConfig.CustomAnimationEntry entry = BedrockItemAnimationManager.resolveEntry(def, stack, type, client, arm);
            PunchyTuningConfig.CustomAnimationEntry idleEntry = BedrockItemAnimationManager.resolveEntry(def, stack, "idle", client, arm);
            AnimationClip idleClip = BedrockItemAnimationManager.resolveDesiredClip(def, idleEntry, arm, null);
            if (entry == null && idleEntry != null && !"idle".equals(type)) {
                entry = idleEntry;
                type = "idle";
            }
            String keepLoopOverride = null;
            if (entry != null) {
                PunchyTuningConfig.CustomAnimationEntry activeKeepEntry = KEEP_LOOP_ACTIVE_ENTRY.get(arm);
                String activeKeepName = KEEP_LOOP_ACTIVE_NAME.get(arm);
                if (activeKeepEntry == entry && activeKeepName != null && !activeKeepName.isBlank() && BedrockItemAnimationManager.matchesWorldTriggersStable(entry, client, arm)) {
                    keepLoopOverride = activeKeepName;
                } else {
                    KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                    KEEP_LOOP_ACTIVE_NAME.remove(arm);
                }
            } else {
                KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                KEEP_LOOP_ACTIVE_NAME.remove(arm);
            }
            AnimationClip desired = BedrockItemAnimationManager.resolveDesiredClip(def, entry, arm, keepLoopOverride);
            if (desired == null && idleClip != null && !"idle".equals(type)) {
                entry = idleEntry;
                type = "idle";
                desired = idleClip;
            }
            if (desired == null && entry != null) {
                if (idleClip != null) {
                    entry = idleEntry;
                    type = "idle";
                    desired = idleClip;
                } else if (handler.isPlaying() && handler.getCurrentClip() != null && ACTIVE_TYPES.get(arm) != null && ACTIVE_TYPES.get(arm).equals(type)) {
                    desired = handler.getCurrentClip();
                    type = ACTIVE_TYPES.getOrDefault(arm, type);
                    PunchyTuningConfig.CustomAnimationEntry activeEntry2 = CLIP_ENTRIES.get(arm);
                    if (activeEntry2 != null) {
                        entry = activeEntry2;
                    }
                }
            }
            boolean mirrorAnim = false;
            if (arm == class_1306.field_6182 && desired != null && (name = desired.getName()) != null && name.endsWith("_left")) {
                mirrorAnim = true;
            }
            ANIM_MIRROR.put(arm, mirrorAnim);
            AnimationClip active = ACTIVE_CLIPS.get(arm);
            String activeType = ACTIVE_TYPES.get(arm);
            PunchyTuningConfig.CustomAnimationEntry previousActiveEntry = ACTIVE_ENTRIES.get(arm);
            PunchyTuningConfig.CustomAnimationEntry clipEntry = CLIP_ENTRIES.get(arm);
            if (active != null && clipEntry != null && clipEntry.keep_looping != null && !clipEntry.keep_looping.isBlank() && !handler.isPlaying() && BedrockItemAnimationManager.matchesWorldTriggersStable(clipEntry, client, arm)) {
                boolean introEnded;
                AnimationClip introClip = BedrockItemAnimationManager.resolveDesiredClip(def, clipEntry, arm, null);
                boolean bl = introEnded = BedrockItemAnimationManager.isEndedClipForEntry(def, active, clipEntry, arm) || introClip != null && active == introClip;
                if (introEnded) {
                    String transitionType;
                    String string = transitionType = activeType != null ? activeType : type;
                    if (BedrockItemAnimationManager.startKeepLoopTransition(def, handler, arm, transitionType, clipEntry, clipEntry.keep_looping.trim(), handler.getCurrentTime())) {
                        handler.tick(0.05f, 1.0f);
                        ModelPartsParticleSystem.onHandlerTick(client, arm, handler);
                        LAST_PLAYING.put(arm, handler.isPlaying());
                        continue;
                    }
                }
            }
            float idleTime = BedrockItemAnimationManager.updateIdleTime(arm, handler, idleClip, 0.05f);
            boolean bl = triggerInvalid = active != null && handler.isPlaying() && handler.getCurrentClip() == active && clipEntry != null && !BedrockItemAnimationManager.matchesWorldTriggersStable(clipEntry, client, arm);
            if (triggerInvalid) {
                KEEP_LOOP_ACTIVE_ENTRY.remove(arm);
                KEEP_LOOP_ACTIVE_NAME.remove(arm);
                if (idleClip != null) {
                    desired = idleClip;
                    entry = idleEntry;
                    type = "idle";
                } else {
                    handler.setLooping(false);
                    boolean skipBlendOut = Boolean.TRUE.equals(clipEntry.skip_blend_out);
                    handler.setBlendOutDurationNext(skipBlendOut ? 0.0f : 1.0f);
                    handler.stopImmediateWithBlendOut();
                    ACTIVE_CLIPS.put(arm, null);
                    ACTIVE_TYPES.remove(arm);
                    ACTIVE_ENTRIES.remove(arm);
                    CLIP_ENTRIES.remove(arm);
                    active = null;
                    activeType = null;
                    ModelPartsParticleSystem.clearArmAnimationState(arm);
                    LAST_PLAYING.put(arm, handler.isPlaying());
                    continue;
                }
            }
            boolean consumeFinishLock = ConsumeStateMachine.isEquipBlockedByConsumeAnimation();
            boolean transitioningToWaterState = entry != null && (Boolean.TRUE.equals(entry.inside_water) || Boolean.TRUE.equals(entry.in_water)) && desired != null && desired != active;
            boolean allowActiveToFinish = !(triggerInvalid || active == null || !handler.isPlaying() || handler.getCurrentClip() != active || active.isLoop() || active.isHoldOnLastFrame() || handler.isForceLooping() || desired != null && !"idle".equals(type) || "falling".equals(activeType) || transitioningToWaterState || BedrockItemAnimationManager.isConsumeType(activeType) && !consumeFinishLock);
            boolean bl2 = preserveHoldOnLastFrameDuringFalling = !triggerInvalid && active != null && handler.getCurrentClip() == active && active.isHoldOnLastFrame() && "falling".equals(type) && !"falling".equals(activeType) && !transitioningToWaterState;
            if (allowActiveToFinish) {
                desired = active;
                if (activeType != null) {
                    type = activeType;
                }
                if ((activeEntry = CLIP_ENTRIES.get(arm)) != null) {
                    entry = activeEntry;
                }
            }
            if (preserveHoldOnLastFrameDuringFalling) {
                desired = active;
                if (activeType != null) {
                    type = activeType;
                }
                if ((activeEntry = CLIP_ENTRIES.get(arm)) != null) {
                    entry = activeEntry;
                }
            }
            boolean bl3 = repeatConsumeWhileHolding = desired != null && desired == active && BedrockItemAnimationManager.isConsumeType(type) && !handler.isPlaying() && !handler.isBlendOutActive();
            if (repeatConsumeWhileHolding) {
                active = null;
                ACTIVE_CLIPS.put(arm, null);
            }
            if (active == null && desired != null && !desiredState.immediate && handler.isBlendOutActive() && handler.getCurrentClip() != null) {
                handler.tick(0.05f, 1.0f);
                ModelPartsParticleSystem.onHandlerTick(client, arm, handler);
                LAST_PLAYING.put(arm, handler.isPlaying());
                continue;
            }
            ACTIVE_ENTRIES.put(arm, entry);
            if (entry != null) {
                if (entry.isJavaBlock != null) {
                    javaBlock = entry.isJavaBlock;
                }
                handler.setLoopSpeedParams(entry.loop_speed_max, entry.loop_speed_ramp);
            } else {
                handler.resetLoopSpeedParams();
            }
            handler.setSwapTranslationYZ(swapTranslationYZ);
            handler.setJavaBlock(javaBlock);
            if (entry != null && Boolean.TRUE.equals(entry.disable_swim_animation)) {
                SWIM_SUPPRESSION.put(arm, true);
            }
            if (restartVisualForcedUseItem = BedrockItemAnimationManager.shouldRestartVisualForcedUseItem(arm, type, desired)) {
                active = null;
                ACTIVE_CLIPS.put(arm, null);
            }
            boolean bl4 = typeChanged = !Objects.equals(activeType, type);
            if (desired == active && typeChanged) {
                ACTIVE_TYPES.put(arm, type);
                CLIP_ENTRIES.put(arm, entry);
                ACTIVE_CLIPS.put(arm, desired);
            } else if (desired != active || typeChanged) {
                boolean hardCut;
                PunchyTuningConfig.CustomAnimationEntry activeEntry3 = previousActiveEntry != null ? previousActiveEntry : CLIP_ENTRIES.get(arm);
                boolean forceCutFromActive = activeEntry3 != null && Boolean.TRUE.equals(activeEntry3.skip_blend_out);
                boolean forceCutToDesired = entry != null && Boolean.TRUE.equals(entry.skip_blend_in);
                boolean bl5 = hardCut = forceCutFromActive || forceCutToDesired;
                if (desired != null) {
                    boolean skipBlendIn;
                    boolean loop;
                    boolean inspectTransition = "inspect".equals(type);
                    if (hardCut && (handler.isPlaying() || handler.isBlendOutActive())) {
                        handler.resetImmediate();
                    }
                    handler.setActiveArm(null);
                    boolean bl6 = loop = desiredState.loopOverride != null ? desiredState.loopOverride : BedrockItemAnimationManager.shouldLoop(type);
                    if (entry != null && entry.keep_looping != null && !entry.keep_looping.isBlank()) {
                        loop = false;
                    }
                    if (keepLoopOverride != null) {
                        loop = true;
                    }
                    handler.setLooping(loop);
                    boolean hasPrevious = handler.isPlaying() && handler.getCurrentClip() != null;
                    boolean isIdleDesired = desired == idleClip;
                    float speedMultiplier = 1.0f;
                    boolean blendFromCurrentOrigin = BedrockItemAnimationManager.shouldBlendFromCurrentOrigin(entry);
                    boolean bl7 = skipBlendIn = entry != null && Boolean.TRUE.equals(entry.skip_blend_in);
                    if (forceCutFromActive || forceCutToDesired) {
                        skipBlendIn = true;
                    }
                    if (skipBlendIn) {
                        handler.setBlendInDurationNext(0.0f);
                    }
                    if (desiredState.immediate) {
                        if (hasPrevious) {
                            if (!skipBlendIn) {
                                handler.setBlendInDurationNext(inspectTransition ? 0.0f : (isIdleDesired && "idle".equals(activeType) ? 0.6f : 0.4f));
                            }
                            if (isIdleDesired) {
                                if (skipBlendIn) {
                                    handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                                } else if (blendFromCurrentOrigin) {
                                    handler.setBlendInEasingNext(PoseHandler.BlendInEasing.STRONG_OUT_BACK);
                                    handler.playAtTimeRebasedFromCurrentReturnToOrigin(desired, speedMultiplier, desired.isHoldOnLastFrame(), idleTime);
                                } else {
                                    handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                                }
                            } else if (skipBlendIn) {
                                handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                            } else if (blendFromCurrentOrigin) {
                                handler.setBlendInEasingNext(PoseHandler.BlendInEasing.STRONG_OUT_BACK);
                                handler.playAtTimeRebasedFromCurrentReturnToOrigin(desired, speedMultiplier, desired.isHoldOnLastFrame(), 0.0f);
                            } else {
                                handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                            }
                        } else {
                            if (!skipBlendIn) {
                                float f = blend = inspectTransition ? 0.0f : 0.0f;
                                if (isIdleDesired && wasPlaying) {
                                    blend = 0.6f;
                                }
                                handler.setBlendInDurationNext(blend);
                            }
                            if (isIdleDesired) {
                                handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                            } else if (blendFromCurrentOrigin && wasPlaying) {
                                handler.setBlendInEasingNext(PoseHandler.BlendInEasing.STRONG_OUT_BACK);
                                handler.playAtTimeRebasedFromCurrentReturnToOrigin(desired, speedMultiplier, desired.isHoldOnLastFrame(), 0.0f);
                            } else {
                                handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                            }
                        }
                    } else {
                        if (!skipBlendIn) {
                            float f = inspectTransition ? 0.0f : (blend = hasPrevious ? 0.4f : 0.0f);
                            if (hasPrevious && isIdleDesired && "idle".equals(activeType)) {
                                blend = 0.6f;
                            } else if (!hasPrevious && isIdleDesired && wasPlaying) {
                                blend = 0.6f;
                            }
                            handler.setBlendInDurationNext(blend);
                        }
                        if (isIdleDesired) {
                            if (skipBlendIn) {
                                handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                            } else if (blendFromCurrentOrigin) {
                                handler.setBlendInEasingNext(PoseHandler.BlendInEasing.STRONG_OUT_BACK);
                                handler.playAtTimeRebasedFromCurrentReturnToOrigin(desired, speedMultiplier, desired.isHoldOnLastFrame(), idleTime);
                            } else {
                                handler.playAtTime(desired, speedMultiplier, true, desired.isHoldOnLastFrame(), idleTime);
                            }
                        } else if (skipBlendIn) {
                            handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                        } else if (blendFromCurrentOrigin) {
                            handler.setBlendInEasingNext(PoseHandler.BlendInEasing.STRONG_OUT_BACK);
                            handler.playAtTimeRebasedFromCurrentReturnToOrigin(desired, speedMultiplier, desired.isHoldOnLastFrame(), 0.0f);
                        } else {
                            handler.play(desired, speedMultiplier, true, desired.isHoldOnLastFrame());
                        }
                    }
                    if (!Objects.equals(type, activeType)) {
                        MovementStateMachine.get().triggerActionAnimation(arm);
                    }
                    ACTIVE_TYPES.put(arm, type);
                    CLIP_ENTRIES.put(arm, entry);
                } else if (active != null) {
                    boolean skipBlendOut;
                    boolean bl8 = skipBlendOut = activeEntry3 != null && Boolean.TRUE.equals(activeEntry3.skip_blend_out);
                    if (skipBlendOut) {
                        handler.resetImmediate();
                    } else {
                        boolean inspectBlendOut = "inspect".equals(activeType);
                        handler.setBlendOutDurationNext(inspectBlendOut ? 0.18f : 3.0f);
                        handler.setBlendOutSlowNext(inspectBlendOut ? 1.0f : 3.6f);
                        handler.setLooping(false);
                        handler.stopImmediateWithBlendOut();
                    }
                    ACTIVE_TYPES.remove(arm);
                    ACTIVE_ENTRIES.remove(arm);
                    CLIP_ENTRIES.remove(arm);
                }
                ACTIVE_CLIPS.put(arm, desired);
            }
            BedrockItemAnimationManager.syncIdleOverlay(handler, arm, idleClip, idleTime, ACTIVE_CLIPS.get(arm));
            PunchyTuningConfig.CustomAnimationEntry liveEntry = CLIP_ENTRIES.get(arm);
            String liveKeepName = liveEntry != null ? liveEntry.keep_looping : null;
            AnimationClip liveClip = ACTIVE_CLIPS.get(arm);
            if (liveEntry != null && liveKeepName != null && !liveKeepName.isBlank() && liveClip != null && handler.isPlaying() && handler.getCurrentClip() == liveClip && !liveClip.isLoop() && !liveClip.isHoldOnLastFrame() && !handler.isForceLooping() && BedrockItemAnimationManager.isEndedClipForEntry(def, liveClip, liveEntry, arm) && BedrockItemAnimationManager.matchesWorldTriggersStable(liveEntry, client, arm) && (remaining = Math.max(0.0f, liveClip.getLength() - handler.getCurrentTime())) <= 0.3f && BedrockItemAnimationManager.startKeepLoopTransition(def, handler, arm, type, liveEntry, liveKeepName.trim(), handler.getCurrentTime())) {
                handler.tick(0.05f, 1.0f);
                ModelPartsParticleSystem.onHandlerTick(client, arm, handler);
                LAST_PLAYING.put(arm, handler.isPlaying());
                continue;
            }
            handler.tick(0.05f, 1.0f);
            ModelPartsParticleSystem.onHandlerTick(client, arm, handler);
            if (!handler.isPlaying()) {
                String keepName;
                clipEntry = CLIP_ENTRIES.get(arm);
                String string = keepName = clipEntry != null ? clipEntry.keep_looping : null;
                if (keepName != null && !keepName.isBlank() && BedrockItemAnimationManager.matchesWorldTriggersStable(clipEntry, client, arm)) {
                    boolean introEnded;
                    AnimationClip introClip = BedrockItemAnimationManager.resolveDesiredClip(def, clipEntry, arm, null);
                    AnimationClip endedClip = handler.getCurrentClip();
                    AnimationClip activeClip = ACTIVE_CLIPS.get(arm);
                    boolean bl9 = introEnded = BedrockItemAnimationManager.isEndedClipForEntry(def, endedClip, clipEntry, arm) || endedClip == null && introClip != null && activeClip == introClip;
                    if (!introEnded) {
                        ModelPartsParticleSystem.onHandlerTick(client, arm, handler);
                        LAST_PLAYING.put(arm, handler.isPlaying());
                        continue;
                    }
                    if (BedrockItemAnimationManager.startKeepLoopTransition(def, handler, arm, type, clipEntry, keepName.trim(), handler.getCurrentTime())) {
                        // empty if block
                    }
                }
            }
            LAST_PLAYING.put(arm, handler.isPlaying());
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
        boolean mirrorX = Boolean.TRUE.equals(ANIM_MIRROR.get(arm));
        handler.setMirrorX(mirrorX);
        handler.applyTransformations(parts, partialTicks, true);
    }

    public static boolean isSwimSuppressed(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        return Boolean.TRUE.equals(SWIM_SUPPRESSION.get(arm));
    }

    public static boolean isAnimMirrorActive(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        return Boolean.TRUE.equals(ANIM_MIRROR.get(arm));
    }

    public static PunchyTuningConfig.CustomAnimationEntry getActiveEntry(class_1306 arm) {
        if (arm == null) {
            return null;
        }
        return ACTIVE_ENTRIES.get(arm);
    }

    private static boolean hasItemSuppression(BedrockItemDefinition def, class_1799 stack, boolean swim) {
        if (def == null || stack == null || stack.method_7960()) {
            return false;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = def.getModelPartsAnimation();
        if (entries == null || entries.isEmpty()) {
            return false;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.items != null && !entry.items.isEmpty() && !BedrockItemSystem.matchesItemList(stack, entry.items) || !swim || !Boolean.TRUE.equals(entry.disable_swim_animation)) continue;
            return true;
        }
        return false;
    }

    private static String getStackKey(class_1799 stack, BedrockItemDefinition def) {
        int cmd;
        class_2960 itemId;
        block18: {
            if (stack == null || stack.method_7960()) {
                return null;
            }
            itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
            if (itemId == null) {
                return null;
            }
            cmd = -1;
            try {
                Integer i2;
                Object e;
                List list;
                Integer i;
                Object value;
                Method method2;
                Object cmdObj = stack.method_57824(class_9334.field_49637);
                if (cmdObj instanceof Integer) {
                    Integer i3 = (Integer)cmdObj;
                    cmd = i3;
                    break block18;
                }
                if (cmdObj == null) break block18;
                try {
                    method2 = cmdObj.getClass().getMethod("value", new Class[0]);
                    value = method2.invoke(cmdObj, new Object[0]);
                    if (value instanceof Integer) {
                        i = (Integer)value;
                        cmd = i;
                    } else if (value instanceof List && !(list = (List)value).isEmpty() && (e = list.get(0)) instanceof Integer) {
                        i2 = (Integer)e;
                        cmd = i2;
                    }
                }
                catch (Throwable method2) {
                    // empty catch block
                }
                if (cmd != -1) break block18;
                try {
                    method2 = cmdObj.getClass().getMethod("getValue", new Class[0]);
                    value = method2.invoke(cmdObj, new Object[0]);
                    if (value instanceof Integer) {
                        i = (Integer)value;
                        cmd = i;
                    } else if (value instanceof List && !(list = (List)value).isEmpty() && (e = list.get(0)) instanceof Integer) {
                        i2 = (Integer)e;
                        cmd = i2;
                    }
                }
                catch (Throwable method3) {}
            }
            catch (Throwable cmdObj) {
                // empty catch block
            }
        }
        String customName = "";
        try {
            class_2561 hover = stack.method_7964();
            if (hover != null) {
                String raw = hover.getString();
                customName = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            }
        }
        catch (Throwable hover) {
            // empty catch block
        }
        String defKey = def != null && def.getId() != null ? def.getId().toString() : "null";
        return String.valueOf(itemId) + ":" + cmd + "|name=" + customName + "|def=" + defKey;
    }

    private static void stopHandler(PoseHandler handler, class_1306 arm) {
        handler.setLooping(false);
        handler.resetImmediate();
        ACTIVE_CLIPS.put(arm, null);
        ACTIVE_TYPES.remove(arm);
        ACTIVE_ENTRIES.remove(arm);
    }

    private static ActionState resolveDesiredState(class_310 client, class_1306 arm, class_1799 stack) {
        if (client == null || client.field_1724 == null) {
            return ActionState.EMPTY;
        }
        ActionState forcedVisualUseItem = BedrockItemAnimationManager.resolveVisualForcedUseItemState(client, arm, stack);
        if (forcedVisualUseItem != null) {
            return forcedVisualUseItem;
        }
        ForcedAction forced = BedrockItemAnimationManager.consumeForcedTypeForArm(client, arm);
        if (forced != null) {
            return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, forced.type), null, forced.immediate);
        }
        PersistentAction persistent = BedrockItemAnimationManager.getPersistentForArm(client, arm);
        if (persistent != null) {
            return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, persistent.type), persistent.loopOverride, persistent.immediate);
        }
        String consumeTypeFinishing = BedrockItemAnimationManager.resolveConsumeTypeForArmDuringFinish(client, arm);
        if (consumeTypeFinishing != null) {
            return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, consumeTypeFinishing), null, true);
        }
        if (BedrockItemAnimationManager.consumeAttackPulseForArm(client, arm)) {
            return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, "attack"), null, true);
        }
        String consumeType = BedrockItemAnimationManager.resolveConsumeTypeForArm(client, arm);
        if (consumeType != null) {
            return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, consumeType), null, true);
        }
        if (BedrockItemAnimationManager.isChargeUseActiveForArm(client, arm)) {
            return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, "useItem"), true, true);
        }
        return new ActionState(BedrockItemAnimationManager.resolveTypeWithFallback(stack, "idle"), null, false);
    }

    private static ActionState resolveVisualForcedUseItemState(class_310 client, class_1306 arm, class_1799 stack) {
        if (client == null || client.field_1724 == null || arm == null || stack == null || stack.method_7960()) {
            return null;
        }
        if (!PunchyAnimationManager.isItemForceShown(arm)) {
            return null;
        }
        AnimationClip poseClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (poseClip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return null;
        }
        String poseName = poseClip.getName();
        if (poseName == null || poseName.isBlank()) {
            return null;
        }
        if (!BedrockItemAnimationManager.isVisualUseItemPose(poseName)) {
            return null;
        }
        String resolved = BedrockItemAnimationManager.resolveTypeWithFallback(stack, "useItem");
        if (resolved == null) {
            return null;
        }
        return new ActionState(resolved, false, true);
    }

    private static boolean isVisualUseItemPose(String poseName) {
        String normalized = BedrockItemAnimationManager.normalizeType(poseName);
        if (normalized.isEmpty()) {
            return false;
        }
        if (normalized.contains("strikefire")) {
            return true;
        }
        if (normalized.contains("interact") || normalized.contains("interaction")) {
            return true;
        }
        return normalized.startsWith("use") || normalized.contains("flintsteel");
    }

    private static boolean shouldRestartVisualForcedUseItem(class_1306 arm, String type, AnimationClip desired) {
        if (arm == null || desired == null || !"useItem".equals(type)) {
            return false;
        }
        if (!PunchyAnimationManager.isItemForceShown(arm)) {
            return false;
        }
        long currentEpoch = UseItemStateMachine.getVisualUseItemEpoch();
        if (currentEpoch <= 0L) {
            return false;
        }
        long lastEpoch = LAST_VISUAL_USE_ITEM_EPOCH.getOrDefault(arm, 0L);
        LAST_VISUAL_USE_ITEM_EPOCH.put(arm, currentEpoch);
        return currentEpoch > lastEpoch;
    }

    private static String resolveConsumeTypeForArm(class_310 client, class_1306 arm) {
        class_1268 expected;
        if (client == null || client.field_1724 == null || arm == null) {
            return null;
        }
        if (!client.field_1724.method_6115() || client.field_1690 == null || !client.field_1690.field_1904.method_1434()) {
            return null;
        }
        class_1268 usedHand = client.field_1724.method_6058();
        if (usedHand != (expected = BedrockItemAnimationManager.resolveHand(client, arm))) {
            return null;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        if (usingStack == null || usingStack.method_7960()) {
            return null;
        }
        class_1839 useAnim = usingStack.method_7976();
        if (useAnim == class_1839.field_8946) {
            return TYPE_USE_DRINK;
        }
        if (useAnim == class_1839.field_8950) {
            return TYPE_USE_EAT;
        }
        return null;
    }

    private static String resolveConsumeTypeForArmDuringFinish(class_310 client, class_1306 arm) {
        if (!ConsumeStateMachine.isEquipBlockedByConsumeAnimation()) {
            return null;
        }
        if (client == null || client.field_1724 == null || arm == null) {
            return null;
        }
        String forcedType = ConsumeStateMachine.getForcedConsumeTypeForArm(arm);
        if (forcedType != null) {
            return forcedType;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return null;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null) {
            return null;
        }
        class_1306 clipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (clipArm != null && clipArm != arm) {
            return null;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "use_drink")) {
            return TYPE_USE_DRINK;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "use_eat")) {
            return TYPE_USE_EAT;
        }
        return null;
    }

    private static boolean isChargeUseActiveForArm(class_310 client, class_1306 arm) {
        class_1268 expected;
        if (client == null || client.field_1724 == null || arm == null) {
            return false;
        }
        if (!client.field_1724.method_6115() || client.field_1690 == null || !client.field_1690.field_1904.method_1434()) {
            return false;
        }
        class_1268 usedHand = client.field_1724.method_6058();
        if (usedHand != (expected = BedrockItemAnimationManager.resolveHand(client, arm))) {
            return false;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        if (usingStack == null || usingStack.method_7960()) {
            return false;
        }
        return usingStack.method_7909() instanceof class_1753 || usingStack.method_7909() instanceof class_1764 || usingStack.method_7909() instanceof class_1835 || SpearStateMachine.isSpearStack(usingStack);
    }

    private static ForcedAction consumeForcedTypeForArm(class_310 client, class_1306 arm) {
        class_1268 hand = BedrockItemAnimationManager.resolveHand(client, arm);
        return FORCED_TYPES.remove(hand);
    }

    private static PersistentAction getPersistentForArm(class_310 client, class_1306 arm) {
        class_1268 hand = BedrockItemAnimationManager.resolveHand(client, arm);
        PersistentAction current = PERSISTENT_ACTIONS.get(hand);
        if (current != null) {
            return current;
        }
        if (client == null || client.field_1724 == null || arm == null) {
            return null;
        }
        if (!PunchyAnimationManager.isItemForceShown(arm)) {
            return null;
        }
        class_1306 sourceArm = PunchyAnimationManager.getSourceArm();
        if (sourceArm == null || sourceArm == arm) {
            return null;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return null;
        }
        float time = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
        if (!PunchyAnimationManager.isDualHandedActive(clip, time)) {
            return null;
        }
        class_1268 sourceHand = BedrockItemAnimationManager.resolveHand(client, sourceArm);
        return PERSISTENT_ACTIONS.get(sourceHand);
    }

    private static boolean consumeAttackPulseForArm(class_310 client, class_1306 arm) {
        long seen;
        if (client == null || client.field_1724 == null || arm == null) {
            return false;
        }
        class_1268 armHand = BedrockItemAnimationManager.resolveHand(client, arm);
        class_1268 attackHand = AttackActionTracker.peekLastRecordedHand();
        if (attackHand != armHand) {
            return false;
        }
        long epoch = AttackActionTracker.peekEpoch();
        if (epoch <= (seen = LAST_ATTACK_TRIGGER_EPOCH.getOrDefault(arm, 0L).longValue())) {
            return false;
        }
        LAST_ATTACK_TRIGGER_EPOCH.put(arm, epoch);
        return true;
    }

    private static class_1799 resolveStack(class_310 client, class_1306 arm) {
        boolean mainRight;
        class_1799 forcedConsumeStack = ConsumeStateMachine.getForcedRenderStack(arm);
        if (forcedConsumeStack != null && !forcedConsumeStack.method_7960()) {
            return forcedConsumeStack;
        }
        if (client == null || client.field_1724 == null || arm == null) {
            return class_1799.field_8037;
        }
        boolean bl = mainRight = client.field_1724.method_6068() == class_1306.field_6183;
        class_1268 hand = arm == class_1306.field_6183 ? (mainRight ? class_1268.field_5808 : class_1268.field_5810) : (mainRight ? class_1268.field_5810 : class_1268.field_5808);
        class_1799 baseStack = hand == class_1268.field_5810 ? HandEquipStateMachine.getVisualOffhandStack(client.field_1724) : client.field_1724.method_6047();
        return PunchyAnimationManager.resolveRenderStack((class_1657)client.field_1724, arm, baseStack);
    }

    private static class_1268 resolveHand(class_310 client, class_1306 arm) {
        boolean mainRight;
        boolean bl = mainRight = client.field_1724.method_6068() == class_1306.field_6183;
        if (arm == class_1306.field_6183) {
            return mainRight ? class_1268.field_5808 : class_1268.field_5810;
        }
        return mainRight ? class_1268.field_5810 : class_1268.field_5808;
    }

    private static AnimationClip resolveDesiredClip(BedrockItemDefinition def, class_1799 stack, String type, class_310 client, class_1306 arm) {
        if (def == null || type == null || type.isBlank()) {
            return null;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = BedrockItemAnimationManager.resolveEntry(def, stack, type, client, arm);
        return BedrockItemAnimationManager.resolveDesiredClip(def, entry, arm, null);
    }

    private static AnimationClip resolveDesiredClip(BedrockItemDefinition def, PunchyTuningConfig.CustomAnimationEntry entry, class_1306 arm, String overrideName) {
        String suffix;
        AnimationClip sided;
        AnimationClip overrideClip;
        if (def == null) {
            return null;
        }
        if (overrideName != null && !overrideName.isBlank() && (overrideClip = BedrockItemAnimationManager.resolveClipByName(def, overrideName.trim())) != null) {
            if (arm != null) {
                String suffix2 = arm == class_1306.field_6182 ? "_left" : "_right";
                AnimationClip sided2 = BedrockItemAnimationManager.resolveClipByName(def, overrideName.trim() + suffix2);
                if (sided2 != null) {
                    return sided2;
                }
            }
            return overrideClip;
        }
        String name = BedrockItemAnimationManager.resolveEntryName(entry);
        if (name == null || name.isBlank()) {
            return null;
        }
        if (arm != null && (sided = BedrockItemAnimationManager.resolveClipByName(def, name + (suffix = arm == class_1306.field_6182 ? "_left" : "_right"))) != null) {
            return sided;
        }
        return BedrockItemAnimationManager.resolveClipByName(def, name);
    }

    private static AnimationClip resolveClipByName(BedrockItemDefinition def, String name) {
        if (def == null || name == null || name.isBlank()) {
            return null;
        }
        AnimationClip clip = def.getAnimations().get(name);
        if (clip != null) {
            return clip;
        }
        return def.getAnimations().get(def.getId().method_12836() + ":" + name);
    }

    private static AnimationClip resolveKeepLoopClip(BedrockItemDefinition def, String name, class_1306 arm) {
        String suffix;
        AnimationClip sided;
        if (def == null || name == null || name.isBlank()) {
            return null;
        }
        if (arm != null && (sided = BedrockItemAnimationManager.resolveClipByName(def, name + (suffix = arm == class_1306.field_6182 ? "_left" : "_right"))) != null) {
            return sided;
        }
        return BedrockItemAnimationManager.resolveClipByName(def, name);
    }

    private static boolean isEndedClipForEntry(BedrockItemDefinition def, AnimationClip ended, PunchyTuningConfig.CustomAnimationEntry entry, class_1306 arm) {
        if (def == null || ended == null || entry == null) {
            return false;
        }
        String baseName = BedrockItemAnimationManager.resolveEntryName(entry);
        if (baseName == null || baseName.isBlank()) {
            return false;
        }
        AnimationClip base = BedrockItemAnimationManager.resolveClipByName(def, baseName);
        if (base != null && ended == base) {
            return true;
        }
        if (arm != null) {
            String suffix = arm == class_1306.field_6182 ? "_left" : "_right";
            AnimationClip sided = BedrockItemAnimationManager.resolveClipByName(def, baseName + suffix);
            return ended == sided;
        }
        return false;
    }

    private static boolean startKeepLoopTransition(BedrockItemDefinition def, PoseHandler handler, class_1306 arm, String type, PunchyTuningConfig.CustomAnimationEntry clipEntry, String keepName, float keepStartTime) {
        if (def == null || handler == null || clipEntry == null || keepName == null || keepName.isBlank()) {
            return false;
        }
        AnimationClip keepClip = BedrockItemAnimationManager.resolveKeepLoopClip(def, keepName, arm);
        if (keepClip == null) {
            return false;
        }
        boolean skipBlendIn = Boolean.TRUE.equals(clipEntry.skip_blend_in);
        boolean blendFromCurrentOrigin = BedrockItemAnimationManager.shouldBlendFromCurrentOrigin(clipEntry);
        boolean noActiveClipNow = !handler.isPlaying() || handler.getCurrentClip() == null;
        handler.setLooping(true);
        if (skipBlendIn || noActiveClipNow) {
            handler.playAtTime(keepClip, 1.0f, true, false, keepStartTime);
        } else {
            handler.setBlendInDurationNext(0.55f);
            if (blendFromCurrentOrigin) {
                handler.setBlendInEasingNext(PoseHandler.BlendInEasing.STRONG_OUT_BACK);
                handler.playAtTimeRebasedFromCurrentReturnToOrigin(keepClip, 1.0f, false, keepStartTime);
            } else {
                handler.playAtTime(keepClip, 1.0f, true, false, keepStartTime);
            }
        }
        ACTIVE_CLIPS.put(arm, keepClip);
        ACTIVE_TYPES.put(arm, type);
        KEEP_LOOP_ACTIVE_ENTRY.put(arm, clipEntry);
        KEEP_LOOP_ACTIVE_NAME.put(arm, keepName);
        return true;
    }

    private static PunchyTuningConfig.CustomAnimationEntry resolveEntry(BedrockItemDefinition def, class_1799 stack, String type, class_310 client, class_1306 arm) {
        if (def == null) {
            return null;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = def.getModelPartsAnimation();
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        String normalized = BedrockItemAnimationManager.normalizeType(type);
        PunchyTuningConfig.CustomAnimationEntry bestTriggered = null;
        int bestTriggeredScore = -1;
        PunchyTuningConfig.CustomAnimationEntry bestFallback = null;
        int bestFallbackScore = -1;
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.type == null || !BedrockItemAnimationManager.matchesRequestedType(normalized, BedrockItemAnimationManager.normalizeType(entry.type)) || entry.items != null && !entry.items.isEmpty() && !BedrockItemSystem.matchesItemList(stack, entry.items) || !BedrockItemAnimationManager.matchesWorldTriggersStable(entry, client, arm)) continue;
            int score = BedrockItemAnimationManager.triggerSpecificityScore(entry);
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
        return bestTriggered != null ? bestTriggered : bestFallback;
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

    private static float updateIdleTime(class_1306 arm, PoseHandler handler, AnimationClip idleClip, float baseDeltaSeconds) {
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
            float delta = baseDeltaSeconds;
            idleTime = BedrockItemAnimationManager.advanceLoopTime(idleClip, idleTime, delta);
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
            handler.setSecondaryClipAtTime(idleClip, arm, 1.0f, idleTime);
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

    private static boolean shouldLoop(String type) {
        if (type == null) {
            return false;
        }
        return "mining".equals(type) || "useItem".equals(type) || "idle".equals(type);
    }

    private static boolean isConsumeType(String type) {
        return TYPE_USE_DRINK.equals(type) || TYPE_USE_EAT.equals(type);
    }

    private static boolean shouldBlendFromCurrentOrigin(PunchyTuningConfig.CustomAnimationEntry entry) {
        return entry != null && Boolean.TRUE.equals(entry.blend_from_current_origin);
    }

    private static String resolveTypeWithFallback(class_1799 stack, String type) {
        return type;
    }

    private static String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        return type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
    }

    private static void putForcedType(class_1268 hand, String type, boolean immediate) {
        if (hand == null || type == null || type.isBlank()) {
            return;
        }
        ForcedAction incoming = new ForcedAction(type, immediate);
        ForcedAction current = FORCED_TYPES.get(hand);
        if (current == null) {
            FORCED_TYPES.put(hand, incoming);
            return;
        }
        int currentPriority = BedrockItemAnimationManager.forcedTypePriority(current.type);
        int incomingPriority = BedrockItemAnimationManager.forcedTypePriority(incoming.type);
        if (incomingPriority > currentPriority) {
            FORCED_TYPES.put(hand, incoming);
            return;
        }
        if (incomingPriority == currentPriority) {
            FORCED_TYPES.put(hand, incoming);
        }
    }

    private static int forcedTypePriority(String type) {
        String normalized = BedrockItemAnimationManager.normalizeType(type);
        if ("attack".equals(normalized)) {
            return 100;
        }
        if ("triggerable".equals(normalized)) {
            return 90;
        }
        if ("inspect".equals(normalized)) {
            return 80;
        }
        if ("handout".equals(normalized)) {
            return 60;
        }
        if ("handin".equals(normalized)) {
            return 55;
        }
        if ("mining".equals(normalized)) {
            return 50;
        }
        if ("useitem".equals(normalized)) {
            return 45;
        }
        if ("usedrink".equals(normalized) || "useeat".equals(normalized)) {
            return 44;
        }
        if ("idle".equals(normalized)) {
            return 0;
        }
        return 40;
    }

    private static boolean matchesRequestedType(String requestedType, String entryType) {
        if (requestedType == null || entryType == null) {
            return false;
        }
        if (requestedType.equals(entryType)) {
            return true;
        }
        return "idle".equals(requestedType) && "inwater".equals(entryType);
    }

    private static int triggerSpecificityScore(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return 0;
        }
        int score = 0;
        if (entry.in_water != null) {
            score += 2;
        }
        if (entry.inside_water != null) {
            score += 3;
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
        String normalizedType = BedrockItemAnimationManager.normalizeType(entry.type);
        if ("inwater".equals(normalizedType) && entry.in_water == null && !BedrockItemAnimationManager.isInWaterContact((class_1937)level, (class_1657)player)) {
            return false;
        }
        if (entry.in_water != null) {
            boolean inWater = BedrockItemAnimationManager.isInWaterContact((class_1937)level, (class_1657)player);
            if (entry.in_water != inWater) {
                return false;
            }
        }
        if (entry.inside_water != null) {
            boolean inside = player.method_5777(class_3486.field_15517);
            if (Boolean.TRUE.equals(entry.inside_water) && arm != null) {
                int grace = INSIDE_WATER_GRACE_TICKS.getOrDefault(arm, 0);
                if (inside) {
                    grace = 4;
                } else if (grace > 0) {
                    --grace;
                    inside = true;
                }
                INSIDE_WATER_GRACE_TICKS.put(arm, grace);
            }
            if (entry.inside_water != inside) {
                return false;
            }
        }
        if (entry.weather_detection != null && !BedrockItemAnimationManager.matchesWeather(entry.weather_detection, (class_1937)level, (class_1657)player, arm)) {
            return false;
        }
        if (entry.time_detection != null && !BedrockItemAnimationManager.matchesTime(entry.time_detection, (class_1937)level)) {
            return false;
        }
        if (entry.block_detection != null && !BedrockItemAnimationManager.matchesBlock(entry.block_detection, (class_1937)level, player.method_24515())) {
            return false;
        }
        return entry.biome_detection == null || BedrockItemAnimationManager.matchesBiome(entry.biome_detection, (class_1937)level, player.method_24515());
    }

    private static boolean matchesWorldTriggersStable(PunchyTuningConfig.CustomAnimationEntry entry, class_310 client, class_1306 arm) {
        boolean matched = BedrockItemAnimationManager.matchesWorldTriggers(entry, client, arm);
        if (entry != null && entry.weather_detection != null) {
            return matched;
        }
        if (arm == null || entry == null || entry.keep_looping == null || entry.keep_looping.isBlank()) {
            return matched;
        }
        int grace = KEEP_LOOP_FALSE_GRACE_TICKS_MAP.getOrDefault(arm, 0);
        if (matched) {
            KEEP_LOOP_FALSE_GRACE_TICKS_MAP.put(arm, 6);
            return true;
        }
        if (grace > 0) {
            KEEP_LOOP_FALSE_GRACE_TICKS_MAP.put(arm, grace - 1);
            return true;
        }
        KEEP_LOOP_FALSE_GRACE_TICKS_MAP.put(arm, 0);
        return false;
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

    private static boolean matchesWeather(PunchyTuningConfig.WeatherDetection weather, class_1937 level, class_1657 player, class_1306 arm) {
        if (weather == null || level == null) {
            return true;
        }
        boolean isRainingAt = level.method_8419();
        boolean isThundering = level.method_8546();
        if (player != null) {
            class_2338 pos = class_2338.method_49637((double)player.method_23317(), (double)(player.method_23318() + 1.0), (double)player.method_23321());
            isRainingAt = level.method_8419() || level.method_8520(pos) || level.method_8520(pos.method_10084());
            boolean bl = isThundering = level.method_8546() && isRainingAt;
        }
        if (weather.raining != null) {
            boolean desired = weather.raining;
            boolean effectiveRaining = isRainingAt;
            if (desired && arm != null) {
                int grace = RAIN_GRACE_TICKS.getOrDefault(arm, 0);
                if (isRainingAt) {
                    grace = 100;
                } else if (grace > 0) {
                    --grace;
                    effectiveRaining = true;
                }
                RAIN_GRACE_TICKS.put(arm, grace);
            }
            if (desired != effectiveRaining) {
                return false;
            }
        }
        if (weather.thundering != null && weather.thundering != isThundering) {
            return false;
        }
        if (weather.clear != null) {
            boolean clear;
            boolean bl = clear = !isRainingAt && !isThundering;
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
            return BedrockItemAnimationManager.matchesBlockList(level.method_8320(center), targets);
        }
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    cursor.method_10103(center.method_10263() + dx, center.method_10264() + dy, center.method_10260() + dz);
                    if (!BedrockItemAnimationManager.matchesBlockList(level.method_8320((class_2338)cursor), targets)) continue;
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
            return BedrockItemAnimationManager.matchesBiomeList(level, center, targets);
        }
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    cursor.method_10103(center.method_10263() + dx, center.method_10264() + dy, center.method_10260() + dz);
                    if (!BedrockItemAnimationManager.matchesBiomeList(level, (class_2338)cursor, targets)) continue;
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

    private record PersistentAction(String type, Boolean loopOverride, boolean immediate) {
    }

    private record ActionState(String type, Boolean loopOverride, boolean immediate) {
        private static final ActionState EMPTY = new ActionState(null, null, false);
    }

    private record ForcedAction(String type, boolean immediate) {
    }
}

