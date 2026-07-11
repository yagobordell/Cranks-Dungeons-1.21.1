/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_630
 *  net.minecraft.class_746
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package punchy.client.animation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_630;
import net.minecraft.class_746;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import punchy.client.access.TransformablePart;
import punchy.client.animation.Easing;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.MovementStateMachine;
import punchy.config.PunchyTuningConfig;

public class PoseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Punchy/PoseHandler");
    private static final boolean DEBUG_COMPAT_IDLE = false;
    private static final float BLEND_OUT_SECONDS = 0.28f;
    private static final float BLEND_IN_SECONDS = 0.2f;
    private static final float BLEND_OUT_SLOW = 1.8f;
    private static final float BLEND_IN_SLOW = 1.0f;
    private static final float ANIM_POS_X_OFFSET = 0.0f;
    private static final float ANIM_POS_Z_OFFSET = 0.0f;
    private static final String ANIM_SPEED_PREFIX = "anim_speed_";
    private static final Pattern ANIM_SPEED_VALUE_PATTERN = Pattern.compile("^[-+]?\\d+(?:[\\.,]\\d+)?");
    private static final float MIN_TIMELINE_SPEED = 0.5f;
    private static final float MAX_TIMELINE_SPEED = 5.5f;
    private static final float DEFAULT_LOOP_SPEED_MAX = 2.0f;
    private static final float DEFAULT_LOOP_SPEED_RAMP = 0.6f;
    private static final float LOOP_SPEED_MAX_LIMIT = 1.0f;
    private static final float LOOP_SPEED_RAMP_LIMIT = 1.0f;
    private static final float ARM_TUNING_SUPPRESS_IN_SECONDS = 0.27f;
    private static final float ARM_TUNING_SUPPRESS_OUT_SECONDS = 0.48f;
    private static final float REBASE_RETURN_SPEED_MULTIPLIER = 1.7f;
    private static final String HIDE_OFFHAND_IF_EMPTY_EFFECT = "hide_offhand_if_empty";
    private static final String SHOW_OFFHAND_IF_EMPTY_EFFECT = "show_offhand_if_empty";
    private static final String START_WITH_OFFHAND_EFFECT = "start_here_if_off_hand_item";
    private static final String START_WITH_OFFHAND_EFFECT_ALIAS = "start_if_offhand_item";
    private static final String START_WITH_NO_OFFHAND_EFFECT = "start_here_if_no_off_hand_item";
    private static final String START_WITH_NO_OFFHAND_EFFECT_ALIAS = "start_if_no_offhand_item";
    private static final String END_WITH_NO_OFFHAND_EFFECT = "end_here_if_no_off_hand_item";
    private static final String END_WITH_NO_OFFHAND_EFFECT_ALIAS = "end_if_no_offhand_item";
    private static final String STOP_WITH_OFFHAND_EFFECT = "stop_if_offhand_item";
    private AnimationClip currentClip;
    private float currentTime;
    private float prevTime;
    private float expressionTime;
    private float prevExpressionTime;
    private boolean isPlaying;
    private float blendRemaining;
    private float prevBlendRemaining;
    private float blendInRemaining;
    private float prevBlendInRemaining;
    private final Map<String, Sample> blendFrom = new HashMap<String, Sample>();
    private final Map<String, Sample> blendInFrom = new HashMap<String, Sample>();
    private final Map<String, Sample> rebaseFrom = new HashMap<String, Sample>();
    private final Map<String, Sample> rebaseOrigin = new HashMap<String, Sample>();
    private boolean rebaseActive;
    private boolean rebaseReturnToOrigin;
    private float rebaseStartTime;
    private float rebaseReturnSpeedMultiplier = 1.7f;
    private Float nextRebaseReturnSpeedMultiplier;
    private class_1306 nextRebaseSourceArm;
    private boolean nextRebaseUseBothArms;
    private boolean blendInFromActive;
    private boolean blendInDynamic;
    private AnimationClip blendInClip;
    private float blendInTime;
    private float blendInPrevTime;
    private float blendInSpeedMultiplier = 1.0f;
    private class_1306 activeArm;
    private class_1306 clipArm;
    private boolean clipArmFilter;
    private class_1306 blendArm;
    private boolean blendArmFilter;
    private float blendOutDuration = 0.28f;
    private float blendInDuration = 0.2f;
    private float blendOutSlow = 1.8f;
    private BlendInEasing blendInEasing = BlendInEasing.BACK;
    private Float nextBlendInDuration;
    private Float nextBlendOutDuration;
    private Float nextBlendOutSlow;
    private BlendInEasing nextBlendInEasing;
    private boolean stopAfterCurrent;
    private boolean forceLoop;
    private float speedMultiplier = 1.0f;
    private float timelineSpeedScale = 1.0f;
    private boolean preBlendInActive;
    private boolean holdOnLastFrame;
    private boolean holdReleasePending;
    private boolean hasLooped;
    private boolean skipClearOnStop;
    private class_1306 pendingClearArm;
    private int pendingClearArmTick = -1;
    private class_1306 pendingSkipArm;
    private int pendingSkipArmTick = -1;
    private class_1306 forcedSkipArm;
    private class_1306 nextForcedSkipArm;
    private boolean nextForcedSkipArmSet;
    private float leftArmBlendRemaining;
    private float leftArmBlendPrevRemaining;
    private float rightArmBlendRemaining;
    private float rightArmBlendPrevRemaining;
    private int tickCounter;
    private boolean frozen;
    private boolean swapTranslationYZ;
    private boolean javaBlock;
    private boolean mirrorX;
    private float loopSpeedBoost = 1.0f;
    private float loopSpeedMax = 2.0f;
    private float loopSpeedRamp = 0.6f;
    private boolean ignoreDualHanded;
    private boolean paused;
    private class_1306 pendingOverlapSourceArm;
    private AnimationClip overlapClip;
    private float overlapTime;
    private float overlapPrevTime;
    private float overlapSpeedMultiplier = 1.0f;
    private float overlapBlendRemaining;
    private float overlapPrevBlendRemaining;
    private class_1306 overlapArm;
    private boolean overlapPaused;
    private boolean overlapCompatIdle;
    private final Map<String, Sample> overlapBlendFrom = new HashMap<String, Sample>();
    private float overlapBlendInRemaining;
    private float overlapPrevBlendInRemaining;
    private float overlapBlendInDuration = 0.2f;
    private float armTuningSuppressAlpha;
    private float prevArmTuningSuppressAlpha;
    private boolean latchedKeepTriggerArmTuning;
    private PunchyTuningConfig.CustomFlags overlapCustomFlags;
    private class_1306 latchedTriggerArm;
    private String lastCompatIdleSnapshot = "";

    public PunchyTuningConfig.CustomFlags getOverlapCustomFlags() {
        return this.overlapCustomFlags;
    }

    public void setSecondaryClipAtTimeWithFlags(AnimationClip clip, class_1306 arm, float speedMultiplier, float startTime, PunchyTuningConfig.CustomFlags flags) {
        this.setSecondaryClipAtTime(clip, arm, speedMultiplier, startTime);
        this.overlapCustomFlags = flags;
    }

    public void setSecondaryClipAtTimeWithBlendFromCurrentAndFlags(AnimationClip clip, class_1306 arm, float speedMultiplier, float startTime, boolean armPoseOnlyBlend, float blendInDuration, PunchyTuningConfig.CustomFlags flags) {
        this.setSecondaryClipAtTimeWithBlendFromCurrent(clip, arm, speedMultiplier, startTime, armPoseOnlyBlend, blendInDuration);
        this.overlapCustomFlags = flags;
    }

    public void play(AnimationClip clip) {
        this.play(clip, 1.0f);
    }

    public void play(AnimationClip clip, float speedMultiplier) {
        this.play(clip, speedMultiplier, false);
    }

    public void play(AnimationClip clip, float speedMultiplier, boolean skipBlendIn) {
        this.play(clip, speedMultiplier, skipBlendIn, false);
    }

    public void play(AnimationClip clip, float speedMultiplier, boolean skipBlendIn, boolean holdOnLastFrame) {
        this.playInternal(clip, speedMultiplier, skipBlendIn, holdOnLastFrame, false);
    }

    public void playAtTime(AnimationClip clip, float speedMultiplier, boolean skipBlendIn, boolean holdOnLastFrame, float startTime) {
        this.playInternal(clip, speedMultiplier, skipBlendIn, holdOnLastFrame, false);
        if (clip == null) {
            return;
        }
        float rawStartTime = startTime;
        float length = clip.getLength();
        float time = startTime;
        if (length > 0.0f) {
            if (clip.isLoop()) {
                if ((time %= length) < 0.0f) {
                    time += length;
                }
            } else {
                time = class_3532.method_15363((float)time, (float)0.0f, (float)length);
            }
        } else {
            time = 0.0f;
        }
        this.currentTime = time;
        this.prevTime = time;
        this.expressionTime = rawStartTime;
        this.prevExpressionTime = rawStartTime;
        this.preBlendInActive = false;
    }

    public void playAtTimeWithBlendFromCurrent(AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame, float startTime) {
        this.playWithBlendFromCurrentDynamic(clip, speedMultiplier, holdOnLastFrame);
        if (clip == null) {
            return;
        }
        float rawStartTime = startTime;
        float length = clip.getLength();
        float time = startTime;
        if (length > 0.0f) {
            if (clip.isLoop()) {
                if ((time %= length) < 0.0f) {
                    time += length;
                }
            } else {
                time = class_3532.method_15363((float)time, (float)0.0f, (float)length);
            }
        } else {
            time = 0.0f;
        }
        this.currentTime = time;
        this.prevTime = time;
        this.expressionTime = rawStartTime;
        this.prevExpressionTime = rawStartTime;
        this.preBlendInActive = false;
    }

    public void playAtTimeRebasedFromCurrent(AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame, float startTime) {
        this.playAtTimeRebasedFromCurrentInternal(clip, speedMultiplier, holdOnLastFrame, startTime, false);
    }

    public void playAtTimeRebasedFromCurrentReturnToOrigin(AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame, float startTime) {
        this.playAtTimeRebasedFromCurrentInternal(clip, speedMultiplier, holdOnLastFrame, startTime, true);
    }

    public void playAtTimeRebasedFromHandlerReturnToOrigin(PoseHandler sourceHandler, AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame, float startTime) {
        if (sourceHandler == null || sourceHandler == this) {
            this.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, holdOnLastFrame, startTime);
            return;
        }
        AnimationClip previousClip = sourceHandler.currentClip;
        HashMap<String, Sample> previousRebaseFrom = new HashMap<String, Sample>(sourceHandler.rebaseFrom);
        HashMap<String, Sample> previousRebaseOrigin = new HashMap<String, Sample>(sourceHandler.rebaseOrigin);
        boolean previousRebaseActive = sourceHandler.rebaseActive;
        boolean previousRebaseReturnToOrigin = sourceHandler.rebaseReturnToOrigin;
        float previousRebaseStartTime = sourceHandler.rebaseStartTime;
        float previousRebaseReturnSpeedMultiplier = sourceHandler.rebaseReturnSpeedMultiplier;
        class_1306 previousArm = sourceHandler.clipArmFilter ? sourceHandler.clipArm : sourceHandler.activeArm;
        float previousTime = sourceHandler.currentTime;
        float previousExpressionTime = sourceHandler.expressionTime;
        boolean previousActive = previousClip != null && (sourceHandler.isPlaying || sourceHandler.blendRemaining > 0.0f);
        this.playAtTime(clip, speedMultiplier, true, holdOnLastFrame, startTime);
        this.rebaseFrom.clear();
        this.rebaseOrigin.clear();
        this.rebaseActive = false;
        this.rebaseReturnToOrigin = false;
        this.rebaseStartTime = startTime;
        this.rebaseReturnSpeedMultiplier = this.nextRebaseReturnSpeedMultiplier != null ? Math.max(0.01f, this.nextRebaseReturnSpeedMultiplier.floatValue()) : 1.7f;
        this.nextRebaseReturnSpeedMultiplier = null;
        if (!previousActive || clip == null) {
            return;
        }
        for (Map.Entry<String, BoneTrack> entry : clip.getTracks().entrySet()) {
            String boneName = entry.getKey();
            BoneTrack targetTrack = entry.getValue();
            if (targetTrack == null) continue;
            class_1306 boneArm = this.resolveBoneArm(boneName);
            if (previousArm != null && boneArm != null && boneArm != previousArm) continue;
            Sample from = sourceHandler.sampleVisibleSourceForRebase(boneName, boneArm, previousClip, previousTime, previousExpressionTime, previousRebaseFrom, previousRebaseOrigin, previousRebaseActive, previousRebaseReturnToOrigin, previousRebaseStartTime, previousRebaseReturnSpeedMultiplier);
            Sample origin = this.sampleTrack(targetTrack, startTime, startTime, clip);
            this.rebaseFrom.put(boneName, from);
            this.rebaseOrigin.put(boneName, origin);
        }
        this.rebaseReturnToOrigin = this.rebaseActive = !this.rebaseFrom.isEmpty();
    }

    private void playAtTimeRebasedFromCurrentInternal(AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame, float startTime, boolean returnToOrigin) {
        boolean previousActive;
        AnimationClip previousClip = this.currentClip;
        HashMap<String, Sample> previousRebaseFrom = new HashMap<String, Sample>(this.rebaseFrom);
        HashMap<String, Sample> previousRebaseOrigin = new HashMap<String, Sample>(this.rebaseOrigin);
        boolean previousRebaseActive = this.rebaseActive;
        boolean previousRebaseReturnToOrigin = this.rebaseReturnToOrigin;
        float previousRebaseStartTime = this.rebaseStartTime;
        float previousRebaseReturnSpeedMultiplier = this.rebaseReturnSpeedMultiplier;
        class_1306 previousArm = this.nextRebaseSourceArm;
        boolean forceBothArms = this.nextRebaseUseBothArms;
        this.nextRebaseSourceArm = null;
        this.nextRebaseUseBothArms = false;
        if (forceBothArms) {
            previousArm = null;
        }
        if (previousArm == null) {
            if (previousClip != null) {
                previousArm = this.clipArmFilter ? this.clipArm : this.activeArm;
            } else {
                Object object = this.activeArm != null ? this.activeArm : (previousArm = this.clipArmFilter ? this.clipArm : null);
            }
            if (forceBothArms) {
                previousArm = null;
            }
        }
        float previousTime = this.currentTime;
        float previousExpressionTime = this.expressionTime;
        boolean bl = previousActive = previousClip != null && (this.isPlaying || this.blendRemaining > 0.0f);
        if (!previousActive && this.overlapCompatIdle && this.overlapClip != null && this.overlapArm != null) {
            previousClip = this.overlapClip;
            previousArm = this.overlapArm;
            previousTime = this.overlapTime;
            previousExpressionTime = this.overlapTime;
            previousRebaseFrom.clear();
            previousRebaseOrigin.clear();
            previousRebaseActive = false;
            previousRebaseReturnToOrigin = false;
            previousRebaseStartTime = 0.0f;
            previousRebaseReturnSpeedMultiplier = 1.7f;
            previousActive = true;
        }
        this.playAtTime(clip, speedMultiplier, true, holdOnLastFrame, startTime);
        this.rebaseFrom.clear();
        this.rebaseOrigin.clear();
        this.rebaseActive = false;
        this.rebaseReturnToOrigin = false;
        this.rebaseStartTime = startTime;
        this.rebaseReturnSpeedMultiplier = this.nextRebaseReturnSpeedMultiplier != null ? Math.max(0.01f, this.nextRebaseReturnSpeedMultiplier.floatValue()) : 1.7f;
        this.nextRebaseReturnSpeedMultiplier = null;
        if (!previousActive || clip == null || previousClip == clip) {
            return;
        }
        for (Map.Entry<String, BoneTrack> entry : clip.getTracks().entrySet()) {
            String boneName = entry.getKey();
            BoneTrack targetTrack = entry.getValue();
            if (targetTrack == null) continue;
            class_1306 boneArm = this.resolveBoneArm(boneName);
            if (previousArm != null && boneArm != null && boneArm != previousArm) continue;
            Sample from = this.sampleVisibleSourceForRebase(boneName, boneArm, previousClip, previousTime, previousExpressionTime, previousRebaseFrom, previousRebaseOrigin, previousRebaseActive, previousRebaseReturnToOrigin, previousRebaseStartTime, previousRebaseReturnSpeedMultiplier);
            Sample origin = this.sampleTrack(targetTrack, startTime, startTime, clip);
            this.rebaseFrom.put(boneName, from);
            this.rebaseOrigin.put(boneName, origin);
        }
        this.rebaseActive = !this.rebaseFrom.isEmpty();
        this.rebaseReturnToOrigin = this.rebaseActive && returnToOrigin;
    }

    private Sample sampleVisibleSourceForRebase(String boneName, class_1306 boneArm, AnimationClip previousClip, float previousTime, float previousExpressionTime, Map<String, Sample> previousRebaseFrom, Map<String, Sample> previousRebaseOrigin, boolean previousRebaseActive, boolean previousRebaseReturnToOrigin, float previousRebaseStartTime, float previousRebaseReturnSpeedMultiplier) {
        BoneTrack overlapTrack;
        if (this.overlapCompatIdle && this.overlapClip != null && this.overlapArm != null && boneArm != null && boneArm == this.overlapArm && !this.isItemGripOrHeldItemBone(boneName) && (overlapTrack = this.overlapClip.getTracks().get(boneName)) != null) {
            return this.sampleTrack(overlapTrack, this.overlapTime, this.overlapTime, this.overlapClip);
        }
        if (previousClip == null) {
            return Sample.identity();
        }
        BoneTrack previousTrack = previousClip.getTracks().get(boneName);
        if (previousTrack == null) {
            return Sample.identity();
        }
        return this.samplePreviousVisibleTrack(previousTrack, boneName, previousTime, previousExpressionTime, previousClip, previousRebaseFrom, previousRebaseOrigin, previousRebaseActive, previousRebaseReturnToOrigin, previousRebaseStartTime, previousRebaseReturnSpeedMultiplier);
    }

    private Sample samplePreviousVisibleTrack(BoneTrack track, String boneName, float time, float evalTime, AnimationClip clip, Map<String, Sample> capturedRebaseFrom, Map<String, Sample> capturedRebaseOrigin, boolean capturedRebaseActive, boolean capturedRebaseReturnToOrigin, float capturedRebaseStartTime, float capturedRebaseReturnSpeedMultiplier) {
        Sample sample = this.sampleTrack(track, time, evalTime, clip);
        if (!capturedRebaseActive) {
            return sample;
        }
        Sample from = capturedRebaseFrom.get(boneName);
        Sample origin = capturedRebaseOrigin.get(boneName);
        if (from == null || origin == null) {
            return sample;
        }
        Sample rebased = this.rebaseSample(from, origin, sample);
        if (!capturedRebaseReturnToOrigin || clip == null) {
            return rebased;
        }
        float progress = this.resolveCapturedRebaseReturnProgress(clip, evalTime, capturedRebaseStartTime, capturedRebaseReturnSpeedMultiplier);
        if (progress <= 0.0f) {
            return rebased;
        }
        if (progress >= 1.0f) {
            return sample;
        }
        return this.blendBetweenSamples(rebased, sample, progress);
    }

    private float resolveCapturedRebaseReturnProgress(AnimationClip clip, float evalTime, float startTime, float returnSpeedMultiplier) {
        if (clip == null) {
            return 0.0f;
        }
        float length = clip.getLength();
        if (length <= 0.0f) {
            return 1.0f;
        }
        float start = Math.max(0.0f, Math.min(startTime, length));
        if (evalTime <= start) {
            return 0.0f;
        }
        float duration = Math.max(1.0E-4f, length - start);
        float rawProgress = class_3532.method_15363((float)((evalTime - start) / duration * Math.max(0.01f, returnSpeedMultiplier)), (float)0.0f, (float)1.0f);
        return this.easeOutCubic(rawProgress);
    }

    public void playWithBlendFromCurrentDynamic(AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame) {
        AnimationClip previousClip = this.currentClip;
        float previousTime = this.currentTime;
        float previousPrevTime = this.prevTime;
        float previousSpeedMultiplier = this.speedMultiplier;
        boolean previousPlaying = previousClip != null && (this.isPlaying || this.blendRemaining > 0.0f);
        this.playInternal(clip, speedMultiplier, false, holdOnLastFrame, true);
        this.blendInFrom.clear();
        this.blendInFromActive = false;
        if (!previousPlaying || this.currentClip != clip) {
            this.blendInDynamic = false;
            this.blendInClip = null;
            this.blendInTime = 0.0f;
            this.blendInPrevTime = 0.0f;
            this.blendInSpeedMultiplier = 1.0f;
            return;
        }
        this.blendInDynamic = true;
        this.blendInClip = previousClip;
        this.blendInTime = previousTime;
        this.blendInPrevTime = previousPrevTime;
        this.blendInSpeedMultiplier = previousSpeedMultiplier;
        if (this.blendInRemaining <= 0.0f) {
            this.blendInDynamic = false;
            this.blendInClip = null;
            this.blendInTime = 0.0f;
            this.blendInPrevTime = 0.0f;
            this.blendInSpeedMultiplier = 1.0f;
        }
    }

    public void playWithBlendFromCurrent(AnimationClip clip, float speedMultiplier, boolean holdOnLastFrame) {
        if (this.shouldForceBlendInFromVisibleSource()) {
            this.captureVisibleArmSamplesForBlendIn(this.resolveVisibleSourceArm());
        } else if (this.currentClip != null && (this.isPlaying || this.blendRemaining > 0.0f)) {
            this.blendInFrom.clear();
            for (Map.Entry<String, BoneTrack> entry : this.currentClip.getTracks().entrySet()) {
                Sample sample = this.sampleTrack(entry.getValue(), this.currentTime, this.currentClip);
                this.blendInFrom.put(entry.getKey(), sample);
            }
            this.blendInFromActive = !this.blendInFrom.isEmpty();
        } else {
            this.blendInFrom.clear();
            this.blendInFromActive = false;
        }
        this.playInternal(clip, speedMultiplier, false, holdOnLastFrame, true);
    }

    private void playInternal(AnimationClip clip, float speedMultiplier, boolean skipBlendIn, boolean holdOnLastFrame, boolean forceBlendIn) {
        float initialTime;
        if (PunchyAnimationManager.shouldBlockClip(clip, this.activeArm)) {
            return;
        }
        if (!forceBlendIn && this.shouldForceBlendInFromVisibleSource()) {
            this.captureVisibleArmSamplesForBlendIn(this.resolveVisibleSourceArm());
            forceBlendIn = this.blendInFromActive;
        }
        boolean preserveOverlap = false;
        class_1306 preserveOverlapArm = null;
        if (!preserveOverlap) {
            boolean incomingDual;
            class_1306 previousArm = this.pendingOverlapSourceArm != null ? this.pendingOverlapSourceArm : (this.clipArmFilter ? this.clipArm : this.activeArm);
            class_1306 targetArm = this.activeArm;
            boolean previousDual = this.isPlaying && this.currentClip != null && this.isDualHandedActive(this.currentClip, this.currentTime);
            boolean bl = incomingDual = clip != null && this.isDualHandedActive(clip, 0.0f);
            if (previousDual && !incomingDual && targetArm != null) {
                preserveOverlap = true;
                class_1306 class_13062 = preserveOverlapArm = targetArm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
            }
            if (!preserveOverlap) {
                boolean bl2 = preserveOverlap = this.isPlaying && this.currentClip != null && previousArm != null && targetArm != null && clip != null && previousArm != targetArm && !this.isDualHandedActive(this.currentClip, this.currentTime);
                if (preserveOverlap) {
                    preserveOverlapArm = previousArm;
                }
            }
            if (preserveOverlap && preserveOverlapArm != null) {
                this.preserveOverlapClip(preserveOverlapArm);
            }
        }
        this.pendingOverlapSourceArm = null;
        boolean effectiveSkip = skipBlendIn || !forceBlendIn && (this.isPlaying || this.blendRemaining > 0.0f);
        this.currentClip = clip;
        if (this.nextForcedSkipArmSet) {
            this.forcedSkipArm = this.nextForcedSkipArm;
            this.nextForcedSkipArm = null;
            this.nextForcedSkipArmSet = false;
        } else {
            this.forcedSkipArm = null;
        }
        this.currentTime = initialTime = this.resolveConditionalDualStartTime(clip);
        this.prevTime = initialTime;
        this.expressionTime = initialTime;
        this.prevExpressionTime = initialTime;
        this.isPlaying = true;
        this.hasLooped = false;
        this.blendRemaining = 0.0f;
        this.prevBlendRemaining = 0.0f;
        if (this.nextBlendInDuration != null) {
            this.blendInDuration = this.nextBlendInDuration.floatValue();
            this.nextBlendInDuration = null;
        } else {
            this.blendInDuration = 0.2f;
        }
        if (this.nextBlendInEasing != null) {
            this.blendInEasing = this.nextBlendInEasing;
            this.nextBlendInEasing = null;
        } else {
            this.blendInEasing = BlendInEasing.BACK;
        }
        this.blendInRemaining = effectiveSkip ? 0.0f : this.blendInDuration;
        this.prevBlendInRemaining = effectiveSkip ? 0.0f : this.blendInDuration;
        this.blendFrom.clear();
        this.blendArm = null;
        this.blendArmFilter = false;
        if (!forceBlendIn) {
            this.blendInFrom.clear();
            this.blendInFromActive = false;
        }
        this.rebaseFrom.clear();
        this.rebaseOrigin.clear();
        this.rebaseActive = false;
        this.rebaseReturnToOrigin = false;
        this.rebaseStartTime = 0.0f;
        this.blendInDynamic = false;
        this.blendInClip = null;
        this.blendInTime = 0.0f;
        this.blendInPrevTime = 0.0f;
        this.blendInSpeedMultiplier = 1.0f;
        this.stopAfterCurrent = false;
        this.forceLoop = false;
        this.speedMultiplier = Math.max(0.01f, speedMultiplier);
        this.preBlendInActive = !effectiveSkip && this.blendInRemaining > 0.0f;
        this.holdOnLastFrame = holdOnLastFrame;
        this.holdReleasePending = false;
        this.paused = false;
        this.clipArm = this.activeArm;
        this.clipArmFilter = this.activeArm != null;
        this.latchCompatKeepTriggerArmState();
        this.clearArmBlend();
        if (clip != null && this.activeArm != null && !this.isDualHandedActive(clip, 0.0f) && !this.isEquipClip(clip)) {
            MovementStateMachine.get().triggerIdleBalance(this.activeArm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182);
        }
        if (clip != null && this.activeArm != null && !this.isEquipClip(clip) && !this.isIdleLikeClip(clip)) {
            MovementStateMachine.get().triggerActionAnimation(this.activeArm);
        }
    }

    public void setActiveArm(class_1306 arm) {
        if (this.isPlaying && this.currentClip != null && arm != null && this.activeArm != null && arm != this.activeArm && !this.isDualHandedActive(this.currentClip, this.currentTime)) {
            this.pendingOverlapSourceArm = this.clipArmFilter ? this.clipArm : this.activeArm;
        }
        this.activeArm = arm;
        if (!this.isPlaying) {
            this.clipArm = arm;
            this.clipArmFilter = arm != null;
        }
    }

    public void prepareOverlapBlendOut(class_1306 newArm, AnimationClip newClip) {
        class_1306 previousArm;
        if (!this.isPlaying || this.currentClip == null || newArm == null || newClip == null) {
            return;
        }
        if (this.isDualHandedActive(this.currentClip, this.currentTime)) {
            return;
        }
        class_1306 class_13062 = previousArm = this.clipArmFilter ? this.clipArm : this.activeArm;
        if (previousArm == null || previousArm == newArm) {
            return;
        }
        this.pendingOverlapSourceArm = previousArm;
    }

    public void requestClearArm(class_1306 arm) {
        this.pendingClearArm = arm;
        this.pendingClearArmTick = this.tickCounter;
    }

    public void requestSkipArm(class_1306 arm) {
        this.pendingSkipArm = arm;
        this.pendingSkipArmTick = this.tickCounter;
    }

    public void clearRequestedArmFilters() {
        this.pendingClearArm = null;
        this.pendingClearArmTick = -1;
        this.pendingSkipArm = null;
        this.pendingSkipArmTick = -1;
        this.forcedSkipArm = null;
    }

    public void setForcedSkipArmForNextPlay(class_1306 arm) {
        this.nextForcedSkipArm = arm;
        this.nextForcedSkipArmSet = true;
    }

    public void requestBlendInArm(class_1306 arm) {
        if (arm == null || this.currentClip == null || !this.isPlaying) {
            return;
        }
        this.setArmBlendRemaining(arm, 0.2f);
        this.setArmBlendPrevRemaining(arm, 0.2f);
    }

    public void setSecondaryClip(AnimationClip clip, class_1306 arm, float speedMultiplier) {
        this.setSecondaryClipAtTime(clip, arm, speedMultiplier, 0.0f);
    }

    public void setSecondaryClipAtTime(AnimationClip clip, class_1306 arm, float speedMultiplier, float startTime) {
        float clampedStartTime;
        if (clip == null || arm == null) {
            this.clearSecondaryClip();
            return;
        }
        this.overlapClip = clip;
        this.overlapArm = arm;
        this.overlapTime = clampedStartTime = class_3532.method_15363((float)startTime, (float)0.0f, (float)clip.getLength());
        this.overlapPrevTime = clampedStartTime;
        this.overlapSpeedMultiplier = Math.max(0.01f, speedMultiplier);
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapPaused = false;
        this.overlapCompatIdle = false;
        this.overlapBlendFrom.clear();
        this.overlapBlendInRemaining = 0.0f;
        this.overlapPrevBlendInRemaining = 0.0f;
        this.overlapBlendInDuration = 0.2f;
        this.debugCompatIdle("setSecondary", "clip=" + this.clipName(clip) + " arm=" + String.valueOf(arm) + " start=" + clampedStartTime + " compat=" + this.overlapCompatIdle);
    }

    public void setSecondaryClipAtTimeWithBlendFromCurrent(AnimationClip clip, class_1306 arm, float speedMultiplier, float startTime) {
        this.setSecondaryClipAtTimeWithBlendFromCurrent(clip, arm, speedMultiplier, startTime, false, 0.2f);
    }

    public void setSecondaryClipAtTimeWithBlendFromCurrent(AnimationClip clip, class_1306 arm, float speedMultiplier, float startTime, boolean armPoseOnlyBlend) {
        this.setSecondaryClipAtTimeWithBlendFromCurrent(clip, arm, speedMultiplier, startTime, armPoseOnlyBlend, 0.2f);
    }

    public void setSecondaryClipAtTimeWithBlendFromCurrent(AnimationClip clip, class_1306 arm, float speedMultiplier, float startTime, boolean armPoseOnlyBlend, float blendInDuration) {
        this.setSecondaryClipAtTime(clip, arm, speedMultiplier, startTime);
        if (clip == null || arm == null) {
            return;
        }
        this.captureVisibleSamplesForTargetClip(this.overlapBlendFrom, arm, clip, armPoseOnlyBlend);
        if (!this.overlapBlendFrom.isEmpty()) {
            float duration;
            this.overlapBlendInDuration = duration = Math.max(1.0E-4f, blendInDuration);
            this.overlapBlendInRemaining = duration;
            this.overlapPrevBlendInRemaining = duration;
        } else {
            this.overlapBlendInDuration = 0.2f;
        }
        this.debugCompatIdle("setSecondaryBlendFromCurrent", "clip=" + this.clipName(clip) + " arm=" + String.valueOf(arm) + " armOnly=" + armPoseOnlyBlend + " blendFromSize=" + this.overlapBlendFrom.size());
    }

    public void clearSecondaryClipWithBlendOut() {
        if (this.overlapClip == null || this.overlapArm == null) {
            this.clearSecondaryClip();
            return;
        }
        this.overlapPaused = true;
        this.overlapPrevTime = this.overlapTime;
        this.overlapBlendFrom.clear();
        this.overlapBlendInRemaining = 0.0f;
        this.overlapPrevBlendInRemaining = 0.0f;
        this.overlapBlendInDuration = 0.2f;
        this.overlapPrevBlendRemaining = 0.28f;
        this.overlapBlendRemaining = 0.28f;
        this.debugCompatIdle("clearSecondaryBlendOut", "clip=" + this.clipName(this.overlapClip) + " arm=" + String.valueOf(this.overlapArm) + " compat=" + this.overlapCompatIdle);
    }

    public void setSecondaryPaused(boolean paused) {
        this.overlapPaused = paused;
    }

    public void setSecondaryCompatIdle(boolean compatIdle) {
        this.overlapCompatIdle = compatIdle && this.overlapClip != null && this.overlapArm != null;
        this.debugCompatIdle("setCompatIdle", "clip=" + this.clipName(this.overlapClip) + " arm=" + String.valueOf(this.overlapArm) + " compat=" + this.overlapCompatIdle);
    }

    public boolean isSecondaryCompatIdle() {
        return this.overlapCompatIdle && this.overlapClip != null && this.overlapArm != null;
    }

    public void clearSecondaryClip() {
        this.debugCompatIdle("clearSecondary", "clip=" + this.clipName(this.overlapClip) + " arm=" + String.valueOf(this.overlapArm) + " compat=" + this.overlapCompatIdle);
        this.overlapClip = null;
        this.overlapTime = 0.0f;
        this.overlapPrevTime = 0.0f;
        this.overlapSpeedMultiplier = 1.0f;
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapArm = null;
        this.overlapPaused = false;
        this.overlapCompatIdle = false;
        this.overlapBlendFrom.clear();
        this.overlapBlendInRemaining = 0.0f;
        this.overlapPrevBlendInRemaining = 0.0f;
        this.overlapBlendInDuration = 0.2f;
        this.pendingOverlapSourceArm = null;
        this.latchedKeepTriggerArmTuning = false;
        this.latchedTriggerArm = null;
        this.overlapCustomFlags = null;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        if (frozen) {
            this.currentTime = 0.0f;
            this.prevTime = 0.0f;
            this.expressionTime = 0.0f;
            this.prevExpressionTime = 0.0f;
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isBlendOutActive() {
        return this.blendRemaining > 0.0f;
    }

    public float getBlendOutProgress(float partialTicks) {
        if (this.blendRemaining <= 0.0f) {
            return 1.0f;
        }
        float blendRem = class_3532.method_16439((float)partialTicks, (float)this.prevBlendRemaining, (float)this.blendRemaining);
        float t = 1.0f - blendRem / Math.max(1.0E-4f, this.blendOutDuration);
        return class_3532.method_15363((float)Easing.easeOutBack(t), (float)0.0f, (float)1.0f);
    }

    public void setRebaseSourceArmForNextPlay(class_1306 arm) {
        this.nextRebaseSourceArm = arm;
    }

    public void setRebaseUseBothArmsForNextPlay(boolean useBothArms) {
        this.nextRebaseUseBothArms = useBothArms;
    }

    public boolean isRebaseReturnToOriginActive() {
        return this.rebaseActive && this.rebaseReturnToOrigin;
    }

    public float getRebaseReturnToOriginProgress(float partialTicks) {
        if (!this.rebaseActive || !this.rebaseReturnToOrigin || this.currentClip == null) {
            return 0.0f;
        }
        float evalTime = class_3532.method_16439((float)partialTicks, (float)this.prevExpressionTime, (float)this.expressionTime);
        return this.resolveRebaseReturnProgress(this.currentClip, evalTime);
    }

    public void resetImmediate() {
        this.stop();
        this.currentClip = null;
        this.blendRemaining = 0.0f;
        this.prevBlendRemaining = 0.0f;
        this.blendInRemaining = 0.0f;
        this.prevBlendInRemaining = 0.0f;
        this.blendFrom.clear();
        this.blendInFrom.clear();
        this.blendInFromActive = false;
        this.rebaseReturnToOrigin = false;
        this.rebaseStartTime = 0.0f;
        this.blendInDynamic = false;
        this.blendInClip = null;
        this.blendInTime = 0.0f;
        this.blendInPrevTime = 0.0f;
        this.blendInSpeedMultiplier = 1.0f;
        this.overlapClip = null;
        this.overlapTime = 0.0f;
        this.overlapPrevTime = 0.0f;
        this.overlapSpeedMultiplier = 1.0f;
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapArm = null;
        this.overlapPaused = false;
        this.pendingOverlapSourceArm = null;
    }

    public void setSwapTranslationYZ(boolean swapTranslationYZ) {
        this.swapTranslationYZ = swapTranslationYZ;
    }

    public void setJavaBlock(boolean javaBlock) {
        this.javaBlock = javaBlock;
    }

    public void setMirrorX(boolean mirrorX) {
        this.mirrorX = mirrorX;
    }

    public void setLooping(boolean loop) {
        this.forceLoop = loop;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = Math.max(0.01f, speedMultiplier);
    }

    public void setTimelineSpeedScale(float timelineSpeedScale) {
        this.timelineSpeedScale = Math.max(0.01f, timelineSpeedScale);
    }

    public void setIgnoreDualHanded(boolean ignore) {
        this.ignoreDualHanded = ignore;
    }

    public boolean isCurrentDualHandedActive() {
        if (!this.isPlaying || this.currentClip == null) {
            return false;
        }
        return this.isDualHandedActive(this.currentTime);
    }

    public void setLoopSpeedParams(Float max, Float ramp) {
        float resolvedMax = max == null ? 2.0f : max.floatValue();
        float resolvedRamp = ramp == null ? 0.6f : ramp.floatValue();
        this.loopSpeedMax = class_3532.method_15363((float)resolvedMax, (float)1.0f, (float)1.0f);
        this.loopSpeedRamp = class_3532.method_15363((float)resolvedRamp, (float)0.0f, (float)1.0f);
    }

    public void setBlendInDurationNext(float seconds) {
        this.nextBlendInDuration = Float.valueOf(Math.max(0.0f, seconds));
    }

    public void setBlendInEasingNext(BlendInEasing easing) {
        this.nextBlendInEasing = easing == null ? BlendInEasing.BACK : easing;
    }

    public void setBlendOutDurationNext(float seconds) {
        this.nextBlendOutDuration = Float.valueOf(Math.max(0.0f, seconds));
    }

    public void setBlendOutSlowNext(float slow) {
        this.nextBlendOutSlow = Float.valueOf(Math.max(0.01f, slow));
    }

    public void setRebaseReturnSpeedNext(float multiplier) {
        this.nextRebaseReturnSpeedMultiplier = Float.valueOf(Math.max(0.01f, multiplier));
    }

    public void resetLoopSpeedParams() {
        this.setLoopSpeedParams(null, null);
    }

    public void requestStopAfterCurrent() {
        this.stopAfterCurrent = true;
        this.forceLoop = false;
    }

    public boolean isLooping(AnimationClip clip) {
        return this.isPlaying && this.currentClip == clip;
    }

    public boolean isForceLooping() {
        return this.forceLoop;
    }

    public boolean hasLooped() {
        return this.hasLooped;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public AnimationClip getCurrentClip() {
        return this.currentClip;
    }

    public boolean isCurrentClipOwnedBy(AnimationClip clip, class_1306 arm) {
        class_1306 ownerArm;
        if (clip == null || this.currentClip != clip) {
            return false;
        }
        if (arm == null) {
            return true;
        }
        class_1306 class_13062 = ownerArm = this.clipArmFilter ? this.clipArm : this.activeArm;
        if (ownerArm == null) {
            return this.isDualHandedActive(this.currentClip, this.currentTime);
        }
        return ownerArm == arm;
    }

    public float getCurrentTime() {
        return this.currentTime;
    }

    public AnimationClip getSecondaryClip() {
        return this.overlapClip;
    }

    public float getSecondaryTime() {
        return this.overlapTime;
    }

    public class_1306 getSecondaryArm() {
        return this.overlapArm;
    }

    public float getTuningFactor(float partialTicks, class_1306 arm) {
        boolean dualHanded;
        boolean disableTuning;
        boolean useKeyframesV2;
        if (this.currentClip == null) {
            return 1.0f;
        }
        float renderTime = class_3532.method_16439((float)partialTicks, (float)this.prevTime, (float)this.currentTime);
        float renderEvalTime = class_3532.method_16439((float)partialTicks, (float)this.prevExpressionTime, (float)this.expressionTime);
        if (this.preBlendInActive) {
            renderTime = 0.0f;
        }
        if (useKeyframesV2 = this.currentClip.isEffectActive("use_keyframes_v2", renderTime)) {
            return 0.0f;
        }
        boolean bl = disableTuning = this.currentClip.hasEffectAny("disable_tuning") && this.isPlaying && this.currentClip.isEffectActive("disable_tuning", renderTime);
        if (disableTuning && this.activeArm != null && arm != null && arm != this.activeArm && !(dualHanded = this.isDualHandedActive(renderTime))) {
            disableTuning = false;
        }
        if (disableTuning) {
            return 0.0f;
        }
        if (this.isPlaying) {
            return 1.0f;
        }
        if (this.blendRemaining > 0.0f) {
            return 1.0f;
        }
        return 1.0f;
    }

    public float getArmTuningFactor(float partialTicks, class_1306 arm) {
        boolean equipClipActive;
        boolean dualHanded;
        boolean disableArmTuning;
        AnimationClip armTuningClip = this.currentClip;
        float renderTime = class_3532.method_16439((float)partialTicks, (float)this.prevTime, (float)this.currentTime);
        class_1306 tuningArm = this.activeArm;
        if (armTuningClip == null && this.overlapCompatIdle && this.overlapClip != null) {
            armTuningClip = this.overlapClip;
            renderTime = class_3532.method_16439((float)partialTicks, (float)this.overlapPrevTime, (float)this.overlapTime);
            tuningArm = this.overlapArm;
        }
        if (armTuningClip == null && this.armTuningSuppressAlpha <= 1.0E-4f && this.prevArmTuningSuppressAlpha <= 1.0E-4f) {
            return 1.0f;
        }
        if (armTuningClip == null) {
            return 1.0f;
        }
        float renderEvalTime = class_3532.method_16439((float)partialTicks, (float)this.prevExpressionTime, (float)this.expressionTime);
        if (this.preBlendInActive) {
            renderTime = 0.0f;
        }
        boolean keepArmTuning = this.isArmTuningKeepActive(renderTime);
        boolean useKeyframesV2 = armTuningClip.isEffectActive("use_keyframes_v2", renderTime);
        boolean keepTriggerArmTuning = this.isCompatKeepTriggerArmTuningActive(renderTime, arm);
        boolean keepLatchedTriggerDuringBlend = this.latchedKeepTriggerArmTuning && this.latchedTriggerArm != null && arm != null && arm == this.latchedTriggerArm && (this.blendRemaining > 0.0f || this.prevBlendRemaining > 0.0f);
        boolean primaryClipActive = armTuningClip == this.currentClip && this.isPlaying;
        boolean bl = disableArmTuning = armTuningClip.hasEffectAny("disable_arm_tuning") && (armTuningClip == this.overlapClip && this.overlapCompatIdle || primaryClipActive) && armTuningClip.isEffectActive("disable_arm_tuning", renderTime);
        if (this.isWalkOnWaterClip(armTuningClip)) {
            disableArmTuning = false;
        }
        if (keepArmTuning && !disableArmTuning) {
            disableArmTuning = false;
        }
        if (disableArmTuning && tuningArm != null && arm != null && arm != tuningArm && !(dualHanded = this.isDualHandedActive(armTuningClip, renderTime))) {
            disableArmTuning = false;
        }
        boolean bl2 = equipClipActive = primaryClipActive && this.isEquipClip(armTuningClip);
        if (!(!useKeyframesV2 || keepArmTuning || disableArmTuning && equipClipActive)) {
            disableArmTuning = false;
        }
        float suppressAlpha = class_3532.method_16439((float)partialTicks, (float)this.prevArmTuningSuppressAlpha, (float)this.armTuningSuppressAlpha);
        suppressAlpha = class_3532.method_15363((float)suppressAlpha, (float)0.0f, (float)1.0f);
        boolean dualHanded2 = this.isDualHandedActive(armTuningClip, renderTime);
        if (!dualHanded2 && tuningArm != null && arm != null && arm != tuningArm) {
            suppressAlpha = 0.0f;
        }
        if (keepTriggerArmTuning) {
            disableArmTuning = false;
            suppressAlpha = 0.0f;
        }
        if (keepLatchedTriggerDuringBlend) {
            disableArmTuning = false;
            suppressAlpha = 0.0f;
        }
        float suppressEased = primaryClipActive ? class_3532.method_15363((float)Easing.easeOutBack(suppressAlpha), (float)0.0f, (float)1.0f) : 1.0f - class_3532.method_15363((float)Easing.easeOutBack(1.0f - suppressAlpha), (float)0.0f, (float)1.0f);
        if (disableArmTuning) {
            return 0.0f;
        }
        return 1.0f - suppressEased;
    }

    public boolean isPendulumDisabled(float partialTicks, class_1306 arm) {
        boolean dualHanded;
        boolean disablePendulum;
        if (this.currentClip == null || !this.isPlaying) {
            return false;
        }
        float renderTime = class_3532.method_16439((float)partialTicks, (float)this.prevTime, (float)this.currentTime);
        if (this.preBlendInActive) {
            renderTime = 0.0f;
        }
        if (this.currentClip.isEffectActive("use_keyframes_v2", renderTime)) {
            return true;
        }
        boolean bl = disablePendulum = this.currentClip.hasEffectAny("disable_pendulum") && this.currentClip.isEffectActive("disable_pendulum", renderTime) || this.currentClip.hasEffectAny("disable_pendulum_physics") && this.currentClip.isEffectActive("disable_pendulum_physics", renderTime);
        if (disablePendulum && this.activeArm != null && arm != null && arm != this.activeArm && !(dualHanded = this.isDualHandedActive(renderTime))) {
            disablePendulum = false;
        }
        return disablePendulum;
    }

    public void stop() {
        this.isPlaying = false;
        this.blendRemaining = 0.0f;
        this.prevBlendRemaining = 0.0f;
        this.blendInRemaining = 0.0f;
        this.prevBlendInRemaining = 0.0f;
        this.blendFrom.clear();
        this.blendInFrom.clear();
        this.blendInFromActive = false;
        this.blendInDynamic = false;
        this.blendInClip = null;
        this.blendInTime = 0.0f;
        this.blendInPrevTime = 0.0f;
        this.blendInSpeedMultiplier = 1.0f;
        this.blendOutSlow = 1.8f;
        this.activeArm = null;
        this.clipArm = null;
        this.clipArmFilter = false;
        this.blendArm = null;
        this.blendArmFilter = false;
        this.blendOutDuration = 0.28f;
        if (this.nextBlendInDuration != null) {
            this.blendInDuration = this.nextBlendInDuration.floatValue();
            this.nextBlendInDuration = null;
        } else {
            this.blendInDuration = 0.2f;
        }
        this.stopAfterCurrent = false;
        this.forceLoop = false;
        this.speedMultiplier = 1.0f;
        this.preBlendInActive = false;
        this.holdOnLastFrame = false;
        this.holdReleasePending = false;
        this.expressionTime = this.currentTime;
        this.prevExpressionTime = this.prevTime;
        this.skipClearOnStop = false;
        this.pendingClearArm = null;
        this.pendingClearArmTick = -1;
        this.pendingSkipArm = null;
        this.pendingSkipArmTick = -1;
        this.forcedSkipArm = null;
        this.nextForcedSkipArm = null;
        this.nextForcedSkipArmSet = false;
        this.clearArmBlend();
        this.frozen = false;
        this.paused = false;
        this.loopSpeedBoost = 1.0f;
        this.resetLoopSpeedParams();
        this.overlapClip = null;
        this.overlapTime = 0.0f;
        this.overlapPrevTime = 0.0f;
        this.overlapSpeedMultiplier = 1.0f;
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapArm = null;
        this.overlapPaused = false;
        this.overlapCompatIdle = false;
        this.pendingOverlapSourceArm = null;
        this.overlapBlendInDuration = 0.2f;
        this.latchedKeepTriggerArmTuning = false;
        this.latchedTriggerArm = null;
        this.clearArmTuningSuppression();
    }

    public void clearCurrentClipState() {
        this.stop();
        this.currentClip = null;
        this.currentTime = 0.0f;
        this.prevTime = 0.0f;
        this.expressionTime = 0.0f;
        this.prevExpressionTime = 0.0f;
    }

    public void stopWithBlendOut() {
        if (this.currentClip == null) {
            this.stop();
            return;
        }
        this.holdOnLastFrame = false;
        if (this.isPlaying) {
            this.requestStopAfterCurrent();
            return;
        }
        this.beginBlendOut();
    }

    public void stopImmediateWithBlendOut() {
        if (this.currentClip == null) {
            this.stop();
            return;
        }
        this.holdOnLastFrame = false;
        this.holdReleasePending = false;
        this.skipClearOnStop = false;
        this.pendingClearArm = null;
        this.pendingClearArmTick = -1;
        this.pendingSkipArm = null;
        this.pendingSkipArmTick = -1;
        this.clearArmBlend();
        this.frozen = false;
        this.paused = false;
        this.blendInFrom.clear();
        this.blendInFromActive = false;
        this.blendInDynamic = false;
        this.blendInClip = null;
        this.blendInTime = 0.0f;
        this.blendInPrevTime = 0.0f;
        this.blendInSpeedMultiplier = 1.0f;
        this.blendOutSlow = 1.8f;
        this.beginBlendOut();
        this.overlapClip = null;
        this.overlapTime = 0.0f;
        this.overlapPrevTime = 0.0f;
        this.overlapSpeedMultiplier = 1.0f;
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapArm = null;
        this.overlapPaused = false;
    }

    public void stopWithoutClear() {
        this.isPlaying = false;
        this.blendRemaining = 0.0f;
        this.prevBlendRemaining = 0.0f;
        this.blendInRemaining = 0.0f;
        this.prevBlendInRemaining = 0.0f;
        this.blendFrom.clear();
        this.blendInFrom.clear();
        this.blendInFromActive = false;
        this.blendInDynamic = false;
        this.blendInClip = null;
        this.blendInTime = 0.0f;
        this.blendInPrevTime = 0.0f;
        this.blendInSpeedMultiplier = 1.0f;
        this.blendOutSlow = 1.8f;
        this.stopAfterCurrent = false;
        this.forceLoop = false;
        this.speedMultiplier = 1.0f;
        this.preBlendInActive = false;
        this.holdOnLastFrame = false;
        this.holdReleasePending = false;
        this.expressionTime = this.currentTime;
        this.prevExpressionTime = this.prevTime;
        this.skipClearOnStop = true;
        this.pendingClearArm = null;
        this.pendingClearArmTick = -1;
        this.pendingSkipArm = null;
        this.pendingSkipArmTick = -1;
        this.clearArmBlend();
        this.frozen = false;
        this.paused = false;
        this.loopSpeedBoost = 1.0f;
        this.resetLoopSpeedParams();
        this.overlapClip = null;
        this.overlapTime = 0.0f;
        this.overlapPrevTime = 0.0f;
        this.overlapSpeedMultiplier = 1.0f;
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapArm = null;
        this.overlapPaused = false;
        this.overlapCompatIdle = false;
        this.overlapBlendInDuration = 0.2f;
        this.latchedKeepTriggerArmTuning = false;
        this.latchedTriggerArm = null;
        this.clearArmTuningSuppression();
    }

    public void tick(float baseDeltaSeconds, float animationSpeed) {
        ++this.tickCounter;
        this.tickArmTuningSuppression(baseDeltaSeconds);
        float effectiveSpeed = animationSpeed;
        float effectiveMultiplier = this.speedMultiplier;
        if (this.isEquipClip(this.currentClip)) {
            effectiveSpeed = 1.0f;
            effectiveMultiplier = 1.0f;
        } else if (this.isConsumeClip(this.currentClip)) {
            effectiveSpeed = 1.0f;
        } else if (this.isFixedSpeedClip(this.currentClip)) {
            effectiveSpeed = 1.0f;
        }
        float timelineSpeed = this.resolveTimelineSpeed(this.currentClip, this.currentTime);
        if (!Float.isNaN(timelineSpeed)) {
            effectiveSpeed = timelineSpeed * this.timelineSpeedScale;
            effectiveMultiplier = 1.0f;
        }
        boolean loopingActive = this.isPlaying && this.currentClip != null && (this.currentClip.isLoop() || this.forceLoop) && !this.stopAfterCurrent;
        this.loopSpeedBoost = 1.0f;
        float animDelta = baseDeltaSeconds * effectiveSpeed * effectiveMultiplier * this.loopSpeedBoost;
        if (this.blendInDynamic && this.blendInClip != null) {
            float blendDelta = this.computeAnimDelta(this.blendInClip, this.blendInTime, baseDeltaSeconds, animationSpeed, this.blendInSpeedMultiplier);
            this.blendInPrevTime = this.blendInTime;
            this.blendInTime = this.advanceClipTime(this.blendInClip, this.blendInTime, blendDelta);
        }
        if (this.overlapClip != null) {
            float overlapSpeed = animationSpeed;
            float overlapMultiplier = this.overlapSpeedMultiplier;
            if (this.isEquipClip(this.overlapClip)) {
                overlapSpeed = 1.0f;
                overlapMultiplier = 1.0f;
            } else if (this.isConsumeClip(this.overlapClip)) {
                overlapSpeed = 1.0f;
            } else if (this.isFixedSpeedClip(this.overlapClip)) {
                overlapSpeed = 1.0f;
            }
            float overlapTimelineSpeed = this.resolveTimelineSpeed(this.overlapClip, this.overlapTime);
            if (!Float.isNaN(overlapTimelineSpeed)) {
                overlapSpeed = overlapTimelineSpeed * this.timelineSpeedScale;
                overlapMultiplier = 1.0f;
            }
            float overlapDelta = this.overlapPaused ? 0.0f : baseDeltaSeconds * overlapSpeed * overlapMultiplier;
            this.overlapPrevTime = this.overlapTime;
            this.overlapTime += overlapDelta;
            if (this.overlapTime >= this.overlapClip.getLength()) {
                if (this.overlapClip.isLoop() && this.overlapClip.getLength() > 0.0f) {
                    this.overlapTime %= this.overlapClip.getLength();
                    this.overlapPrevTime = this.overlapTime;
                } else {
                    this.overlapTime = this.overlapClip.getLength();
                    if (this.overlapBlendRemaining <= 0.0f) {
                        this.overlapPrevBlendRemaining = this.overlapBlendRemaining = 0.28f;
                    }
                }
            }
            if (this.overlapBlendRemaining > 0.0f) {
                this.overlapPrevBlendRemaining = this.overlapBlendRemaining;
                this.overlapBlendRemaining = Math.max(0.0f, this.overlapBlendRemaining - baseDeltaSeconds / 1.8f);
                if (this.overlapBlendRemaining == 0.0f) {
                    this.overlapClip = null;
                    this.overlapArm = null;
                    this.overlapBlendFrom.clear();
                    this.overlapBlendInRemaining = 0.0f;
                    this.overlapPrevBlendInRemaining = 0.0f;
                }
            }
            if (this.overlapBlendInRemaining > 0.0f) {
                this.overlapPrevBlendInRemaining = this.overlapBlendInRemaining;
                this.overlapBlendInRemaining = Math.max(0.0f, this.overlapBlendInRemaining - baseDeltaSeconds / 1.0f);
            }
        }
        if (!this.isPlaying || this.currentClip == null) {
            if (this.blendRemaining > 0.0f) {
                this.prevBlendRemaining = this.blendRemaining;
                this.blendRemaining = Math.max(0.0f, this.blendRemaining - baseDeltaSeconds / this.blendOutSlow);
            }
            return;
        }
        if (this.blendInRemaining > 0.0f) {
            this.prevBlendInRemaining = this.blendInRemaining;
            this.blendInRemaining = Math.max(0.0f, this.blendInRemaining - animDelta / 1.0f);
            if (this.blendInRemaining == 0.0f) {
                this.preBlendInActive = false;
                this.blendInFrom.clear();
                this.blendInFromActive = false;
                this.blendInDynamic = false;
                this.blendInClip = null;
                this.blendInTime = 0.0f;
                this.blendInPrevTime = 0.0f;
                this.blendInSpeedMultiplier = 1.0f;
            }
        }
        this.tickArmBlend(animDelta);
        if (!this.preBlendInActive) {
            if (this.frozen) {
                this.prevTime = 0.0f;
                this.currentTime = 0.0f;
                this.prevExpressionTime = 0.0f;
                this.expressionTime = 0.0f;
            } else {
                float timeDelta = this.paused ? 0.0f : animDelta;
                this.prevTime = this.currentTime;
                this.currentTime += timeDelta;
                this.prevExpressionTime = this.expressionTime;
                this.expressionTime += timeDelta;
            }
        }
        if (this.currentTime >= this.currentClip.getLength()) {
            if (this.holdOnLastFrame) {
                if (!this.holdReleasePending) {
                    this.prevTime = this.currentTime = this.currentClip.getLength();
                    return;
                }
                this.holdOnLastFrame = false;
                this.holdReleasePending = false;
            }
            if ((this.currentClip.isLoop() || this.forceLoop) && !this.stopAfterCurrent) {
                this.currentTime %= this.currentClip.getLength();
                this.prevTime = this.currentTime - animDelta;
                this.hasLooped = true;
            } else {
                this.beginBlendOut();
                this.stopAfterCurrent = false;
                this.forceLoop = false;
                this.prevBlendRemaining = this.blendRemaining;
                this.blendRemaining = Math.max(0.0f, this.blendRemaining - baseDeltaSeconds / 1.8f);
            }
        }
        if (this.rebaseActive && this.rebaseReturnToOrigin && this.currentClip != null && this.resolveRebaseReturnProgress(this.currentClip, this.expressionTime) >= 1.0f) {
            this.rebaseFrom.clear();
            this.rebaseOrigin.clear();
            this.rebaseActive = false;
            this.rebaseReturnToOrigin = false;
            this.rebaseStartTime = 0.0f;
        }
        if (this.hasLooped && this.currentClip != null && this.activeArm != null) {
            this.hasLooped = false;
            if (!this.isEquipClip(this.currentClip) && !this.isDualHandedActive(0.0f)) {
                MovementStateMachine.get().triggerIdleBalance(this.activeArm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182);
            }
        }
    }

    public boolean isOffhandHiddenIfEmptyActive() {
        if (this.currentClip == null) {
            return false;
        }
        return this.isOffhandHiddenIfEmptyActiveAtTime(this.currentTime);
    }

    public boolean isOffhandHiddenIfEmptyActive(float partialTicks) {
        if (this.currentClip == null) {
            return false;
        }
        float renderTime = class_3532.method_16439((float)partialTicks, (float)this.prevTime, (float)this.currentTime);
        if (this.preBlendInActive) {
            renderTime = 0.0f;
        }
        return this.isOffhandHiddenIfEmptyActiveAtTime(renderTime);
    }

    private boolean isOffhandHiddenIfEmptyActiveAtTime(float time) {
        if (this.currentClip == null || !this.isRealOffhandEmpty()) {
            return false;
        }
        float lastHide = this.currentClip.getLastEffectTime(HIDE_OFFHAND_IF_EMPTY_EFFECT, time);
        float lastShow = this.currentClip.getLastEffectTime(SHOW_OFFHAND_IF_EMPTY_EFFECT, time);
        boolean hasHide = !Float.isNaN(lastHide);
        boolean hasShow = !Float.isNaN(lastShow);
        return hasHide && (!hasShow || lastHide > lastShow);
    }

    private class_1306 resolveTimelineForcedSkipArm(float time, class_1306 currentForcedArm) {
        if (!this.isOffhandHiddenIfEmptyActiveAtTime(time)) {
            return currentForcedArm;
        }
        class_1306 offhandArm = this.resolvePlayerOffhandArm();
        return offhandArm == null ? currentForcedArm : offhandArm;
    }

    private boolean isRealOffhandEmpty() {
        class_310 client = class_310.method_1551();
        return client == null || client.field_1724 == null || client.field_1724.method_6079().method_7960();
    }

    private class_1306 resolvePlayerOffhandArm() {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null) {
            return null;
        }
        class_1306 mainArm = client.field_1724.method_6068();
        return mainArm == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
    }

    private boolean isPlayerOffhandArm(class_1306 arm) {
        return arm != null && arm == this.resolvePlayerOffhandArm();
    }

    private float computeAnimDelta(AnimationClip clip, float time, float baseDeltaSeconds, float animationSpeed, float clipSpeedMultiplier) {
        if (clip == null) {
            return 0.0f;
        }
        float effectiveSpeed = animationSpeed;
        float effectiveMultiplier = clipSpeedMultiplier;
        if (this.isEquipClip(clip)) {
            effectiveSpeed = 1.0f;
            effectiveMultiplier = 1.0f;
        } else if (this.isConsumeClip(clip)) {
            effectiveSpeed = 1.0f;
        } else if (this.isFixedSpeedClip(clip)) {
            effectiveSpeed = 1.0f;
        }
        float timelineSpeed = this.resolveTimelineSpeed(clip, time);
        if (!Float.isNaN(timelineSpeed)) {
            effectiveSpeed = timelineSpeed;
            effectiveMultiplier = 1.0f;
        }
        return baseDeltaSeconds * effectiveSpeed * effectiveMultiplier;
    }

    private float advanceClipTime(AnimationClip clip, float time, float delta) {
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

    private boolean isEquipClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        return name != null && (name.equals("hand_in") || name.equals("hand_out") || name.startsWith("hand_in_") || name.startsWith("hand_out_"));
    }

    private boolean isWalkOnWaterClip(AnimationClip clip) {
        if (clip == null || clip.getName() == null) {
            return false;
        }
        return clip.getName().contains("walk_on_water");
    }

    private boolean isConsumeClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        return "use_eat".equals(name) || "use_drink".equals(name);
    }

    private boolean isIdleLikeClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name == null) {
            return false;
        }
        return "idle".equals(name) || name.startsWith("idle_");
    }

    private boolean isFixedSpeedClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        return "falling_in_water".equals(clip.getName());
    }

    private float resolveTimelineSpeed(AnimationClip clip, float time) {
        if (clip == null) {
            return Float.NaN;
        }
        float resolved = Float.NaN;
        for (Map.Entry<Float, Set<String>> entry : clip.getTimelineEffectsUpTo(time)) {
            for (String effect : entry.getValue()) {
                String value;
                String normalized;
                if (effect == null || effect.isBlank() || !(normalized = effect.toLowerCase(Locale.ROOT).trim()).startsWith(ANIM_SPEED_PREFIX) || (value = normalized.substring(ANIM_SPEED_PREFIX.length())).isEmpty()) continue;
                try {
                    String numericToken;
                    float parsed;
                    Matcher matcher = ANIM_SPEED_VALUE_PATTERN.matcher(value);
                    if (!matcher.find() || !Float.isFinite(parsed = Float.parseFloat(numericToken = matcher.group().replace(',', '.')))) continue;
                    resolved = class_3532.method_15363((float)parsed, (float)0.5f, (float)5.5f);
                }
                catch (NumberFormatException numberFormatException) {}
            }
        }
        return resolved;
    }

    public void releaseHold() {
        if (!this.holdOnLastFrame || this.currentClip == null) {
            return;
        }
        if (this.currentTime >= this.currentClip.getLength()) {
            this.holdOnLastFrame = false;
            return;
        }
        this.holdReleasePending = true;
    }

    public void applyTransformations(Map<String, class_630> partsMap, float partialTicks) {
        this.applyTransformations(partsMap, partialTicks, true);
    }

    public void applyTransformations(Map<String, class_630> partsMap, float partialTicks, boolean clearUnused) {
        block50: {
            String boneName;
            float overlapRenderTime;
            class_1306 forcedArm;
            class_1306 skipArm;
            class_1306 clearArm;
            block51: {
                boolean dualHandedActive;
                block49: {
                    class_630 part;
                    Matrix4f base;
                    TransformablePart transformable;
                    if (this.pendingClearArm != null && this.pendingClearArmTick != this.tickCounter) {
                        this.pendingClearArm = null;
                        this.pendingClearArmTick = -1;
                    }
                    if (this.pendingSkipArm != null && this.pendingSkipArmTick != this.tickCounter) {
                        this.pendingSkipArm = null;
                        this.pendingSkipArmTick = -1;
                    }
                    clearArm = this.pendingClearArm;
                    skipArm = this.pendingSkipArm;
                    forcedArm = this.forcedSkipArm;
                    if (!(this.currentClip != null || this.overlapCompatIdle && this.overlapClip != null)) {
                        if (clearUnused) {
                            this.clearTransforms(partsMap);
                        }
                        return;
                    }
                    if (this.currentClip != null && !this.isPlaying && this.blendRemaining <= 0.0f) {
                        boolean hasOverlapClip;
                        boolean bl = hasOverlapClip = this.overlapCompatIdle && this.overlapClip != null && this.overlapArm != null;
                        if (!this.skipClearOnStop && !hasOverlapClip) {
                            if (clearUnused) {
                                this.clearTransforms(partsMap);
                            } else {
                                this.clearTransformsForClipArm(partsMap);
                            }
                        }
                        this.skipClearOnStop = false;
                        this.currentClip = null;
                        this.activeArm = null;
                        this.clipArm = null;
                        this.clipArmFilter = false;
                        this.blendArm = null;
                        this.blendArmFilter = false;
                        if (!this.overlapCompatIdle || this.overlapClip == null) {
                            return;
                        }
                    }
                    if (clearArm != null) {
                        this.clearTransformsForArm(partsMap, clearArm);
                    }
                    float renderTime = class_3532.method_16439((float)partialTicks, (float)this.prevTime, (float)this.currentTime);
                    float renderEvalTime = class_3532.method_16439((float)partialTicks, (float)this.prevExpressionTime, (float)this.expressionTime);
                    if (this.preBlendInActive) {
                        renderTime = 0.0f;
                    }
                    if ((forcedArm = this.resolveTimelineForcedSkipArm(renderTime, forcedArm)) != null && forcedArm != clearArm) {
                        this.clearTransformsForArm(partsMap, forcedArm);
                    }
                    float blendT = 0.0f;
                    if (this.blendRemaining > 0.0f) {
                        float blendRem = class_3532.method_16439((float)partialTicks, (float)this.prevBlendRemaining, (float)this.blendRemaining);
                        blendT = 1.0f - blendRem / Math.max(1.0E-4f, this.blendOutDuration);
                        blendT = Easing.easeOutBack(blendT);
                    }
                    float blendInT = 1.0f;
                    if (this.isPlaying && this.blendInRemaining > 0.0f) {
                        float blendInRem = class_3532.method_16439((float)partialTicks, (float)this.prevBlendInRemaining, (float)this.blendInRemaining);
                        blendInT = 1.0f - blendInRem / Math.max(1.0E-4f, this.blendInDuration);
                        blendInT = this.applyBlendInEasing(blendInT);
                    }
                    float blendInRenderTime = class_3532.method_16439((float)partialTicks, (float)this.blendInPrevTime, (float)this.blendInTime);
                    dualHandedActive = this.isDualHandedActive(renderTime);
                    overlapRenderTime = class_3532.method_16439((float)partialTicks, (float)this.overlapPrevTime, (float)this.overlapTime);
                    float overlapBlendT = 0.0f;
                    if (this.overlapBlendRemaining > 0.0f) {
                        float overlapRem = class_3532.method_16439((float)partialTicks, (float)this.overlapPrevBlendRemaining, (float)this.overlapBlendRemaining);
                        overlapBlendT = 1.0f - overlapRem / Math.max(1.0E-4f, 0.28f);
                        overlapBlendT = Easing.easeOutBack(overlapBlendT);
                    }
                    float overlapBlendInT = 1.0f;
                    if (this.overlapBlendInRemaining > 0.0f) {
                        float overlapBlendInRem = class_3532.method_16439((float)partialTicks, (float)this.overlapPrevBlendInRemaining, (float)this.overlapBlendInRemaining);
                        overlapBlendInT = 1.0f - overlapBlendInRem / Math.max(1.0E-4f, this.overlapBlendInDuration);
                        overlapBlendInT = this.applyBlendInEasing(overlapBlendInT);
                    }
                    Map<String, PartPoseSnapshot> baseSnapshots = this.capturePartSnapshots(partsMap);
                    if (this.currentClip != null) {
                        for (Map.Entry<String, BoneTrack> entry : this.currentClip.getTracks().entrySet()) {
                            Sample sample;
                            class_630 part2;
                            boolean allowBlendArm;
                            boneName = entry.getKey();
                            BoneTrack track = entry.getValue();
                            if (clearArm != null && this.isBoneForArm(boneName, clearArm) || skipArm != null && this.isBoneForArm(boneName, skipArm) || forcedArm != null && this.isBoneForArm(boneName, forcedArm)) continue;
                            boolean bl = allowBlendArm = this.blendRemaining > 0.0f && this.blendArmFilter && this.isBoneForArm(boneName, this.blendArm) && (this.activeArm == null || !this.isBoneForArm(boneName, this.activeArm));
                            if (!dualHandedActive && this.isPlaying && this.activeArm != null && !this.isBoneForArm(boneName, this.activeArm) && !allowBlendArm || !dualHandedActive && !this.isPlaying && this.blendRemaining > 0.0f && this.blendArmFilter && !this.isBoneForArm(boneName, this.blendArm) || (part2 = partsMap.get(boneName)) == null) continue;
                            if (this.blendRemaining > 0.0f && (!this.isPlaying || allowBlendArm)) {
                                from = this.blendFrom.get(boneName);
                                if (from == null) continue;
                                sample = this.blendToIdentity(from, blendT);
                            } else {
                                class_1306 boneArm;
                                sample = this.sampleTrack(track, renderTime, renderEvalTime, this.currentClip);
                                if (this.rebaseActive) {
                                    from = this.rebaseFrom.get(boneName);
                                    Sample origin = this.rebaseOrigin.get(boneName);
                                    if (from != null && origin != null) {
                                        sample = this.rebaseSampleForCurrentClip(boneName, from, origin, sample, renderEvalTime);
                                    }
                                }
                                float combinedBlendInT = blendInT;
                                if (this.isPlaying && (boneArm = this.resolveBoneArm(boneName)) != null) {
                                    float armBlendT = this.resolveArmBlendInT(boneArm, partialTicks);
                                    combinedBlendInT *= armBlendT;
                                }
                                if (combinedBlendInT < 1.0f) {
                                    if (this.blendInDynamic && this.blendInClip != null) {
                                        Sample from;
                                        BoneTrack fromTrack = this.blendInClip.getTracks().get(boneName);
                                        Sample sample2 = from = fromTrack != null ? this.sampleTrack(fromTrack, blendInRenderTime, blendInRenderTime, this.blendInClip) : null;
                                        sample = from != null ? this.blendBetweenSamples(from, sample, combinedBlendInT) : this.blendFromIdentity(sample, combinedBlendInT);
                                    } else {
                                        Sample from = this.blendInFromActive ? this.blendInFrom.get(boneName) : null;
                                        sample = from != null ? this.blendBetweenSamples(from, sample, combinedBlendInT) : this.blendFromIdentity(sample, combinedBlendInT);
                                    }
                                }
                            }
                            boolean useKeyframesV2 = this.currentClip.isEffectActive("use_keyframes_v2", renderTime);
                            if (useKeyframesV2) {
                                this.applyV2ToPart(part2, baseSnapshots.get(boneName), sample);
                                continue;
                            }
                            class_630 from = part2;
                            if (!(from instanceof TransformablePart)) continue;
                            transformable = (TransformablePart)from;
                            Matrix4f transform = this.matrixFromSample(sample, false);
                            base = new Matrix4f().translate(part2.field_3657, part2.field_3656, part2.field_3655).rotateZ(part2.field_3674).rotateY(part2.field_3675).rotateX(part2.field_3654).scale(part2.field_37938, part2.field_37939, part2.field_37940);
                            transform = base.mul((Matrix4fc)transform);
                            transformable.punchy$setExplicitTransform(transform);
                        }
                    }
                    if (this.overlapClip != null && this.overlapArm != null) {
                        for (Map.Entry<String, BoneTrack> entry : this.overlapClip.getTracks().entrySet()) {
                            boolean useKeyframesV22;
                            float overlapArmBlendT;
                            boneName = entry.getKey();
                            if (!this.isSecondaryBoneActive(boneName, overlapRenderTime) || skipArm != null && this.isBoneForArm(boneName, skipArm) || forcedArm != null && this.isBoneForArm(boneName, forcedArm) || (part = partsMap.get(boneName)) == null) continue;
                            if (this.currentClip != null && this.currentClip.getTracks().containsKey(boneName)) {
                                boolean primaryCoversBone = false;
                                if (this.isPlaying) {
                                    primaryCoversBone = dualHandedActive || this.activeArm == null || this.isBoneForArm(boneName, this.activeArm);
                                } else if (this.blendRemaining > 0.0f) {
                                    primaryCoversBone = !this.overlapCompatIdle && !this.isEquipClip(this.currentClip) && (!this.blendArmFilter || this.blendArm == null || this.isBoneForArm(boneName, this.blendArm));
                                } else {
                                    boolean bl = primaryCoversBone = !this.isEquipClip(this.currentClip);
                                }
                                if (primaryCoversBone) continue;
                            }
                            Sample sample = this.sampleTrack(entry.getValue(), overlapRenderTime, overlapRenderTime, this.overlapClip);
                            Sample overlapFrom = this.overlapBlendFrom.get(boneName);
                            if (overlapFrom != null && overlapBlendInT < 1.0f) {
                                sample = this.blendBetweenSamples(overlapFrom, sample, overlapBlendInT);
                            }
                            if ((overlapArmBlendT = this.resolveSecondaryArmBlendInT(boneName, partialTicks, overlapRenderTime)) < 1.0f) {
                                sample = this.blendFromIdentity(sample, overlapArmBlendT);
                            }
                            if (this.overlapBlendRemaining > 0.0f) {
                                sample = this.blendToIdentity(sample, overlapBlendT);
                            }
                            boolean bl = useKeyframesV22 = this.overlapClip != null && this.overlapClip.isEffectActive("use_keyframes_v2", overlapRenderTime);
                            if (useKeyframesV22) {
                                this.applyV2ToPart(part, baseSnapshots.get(boneName), sample);
                                continue;
                            }
                            class_630 transform = part;
                            if (!(transform instanceof TransformablePart)) continue;
                            transformable = (TransformablePart)transform;
                            transform = this.matrixFromSample(sample, false);
                            base = new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
                            transform = base.mul((Matrix4fc)transform);
                            transformable.punchy$setExplicitTransform((Matrix4f)transform);
                        }
                    }
                    if (this.isPlaying && this.blendRemaining > 0.0f && this.blendArmFilter && this.blendArm != null) {
                        for (Map.Entry<String, Object> entry : this.blendFrom.entrySet()) {
                            boolean useKeyframesV2;
                            boneName = entry.getKey();
                            if (!this.isBoneForArm(boneName, this.blendArm) || skipArm != null && this.isBoneForArm(boneName, skipArm) || forcedArm != null && this.isBoneForArm(boneName, forcedArm) || this.currentClip.getTracks().containsKey(boneName) || (part = partsMap.get(boneName)) == null) continue;
                            Sample sample = this.blendToIdentity((Sample)entry.getValue(), blendT);
                            boolean bl = useKeyframesV2 = this.currentClip != null && this.currentClip.isEffectActive("use_keyframes_v2", renderTime);
                            if (useKeyframesV2) {
                                this.applyV2ToPart(part, baseSnapshots.get(boneName), sample);
                                continue;
                            }
                            class_630 useKeyframesV22 = part;
                            if (!(useKeyframesV22 instanceof TransformablePart)) continue;
                            TransformablePart transformable2 = (TransformablePart)useKeyframesV22;
                            Matrix4f transform = this.matrixFromSample(sample, false);
                            Matrix4f base2 = new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
                            transform = base2.mul((Matrix4fc)transform);
                            transformable2.punchy$setExplicitTransform(transform);
                        }
                    }
                    if (!this.isPlaying) break block49;
                    if (!clearUnused) {
                        return;
                    }
                    for (Map.Entry<String, BoneTrack> entry : partsMap.entrySet()) {
                        boneName = entry.getKey();
                        if (boneName == null || this.isSecondaryBoneActive(boneName, overlapRenderTime) || clearArm != null && this.isBoneForArm(boneName, clearArm) || skipArm != null && this.isBoneForArm(boneName, skipArm) || forcedArm != null && this.isBoneForArm(boneName, forcedArm) || this.blendRemaining > 0.0f && this.blendArmFilter && this.isBoneForArm(boneName, this.blendArm)) continue;
                        if (!dualHandedActive && this.activeArm != null && !this.isBoneForArm(boneName, this.activeArm)) {
                            this.clearPartTransform((class_630)entry.getValue());
                            continue;
                        }
                        if (this.currentClip.getTracks().containsKey(boneName)) continue;
                        this.clearPartTransform((class_630)entry.getValue());
                    }
                    break block50;
                }
                if (!(this.blendRemaining > 0.0f)) break block51;
                if (!clearUnused) {
                    return;
                }
                for (Map.Entry<String, Object> entry : partsMap.entrySet()) {
                    boneName = entry.getKey();
                    if (boneName == null || this.isSecondaryBoneActive(boneName, overlapRenderTime) || clearArm != null && this.isBoneForArm(boneName, clearArm) || skipArm != null && this.isBoneForArm(boneName, skipArm) || forcedArm != null && this.isBoneForArm(boneName, forcedArm)) continue;
                    if (!dualHandedActive && this.blendArmFilter && !this.isBoneForArm(boneName, this.blendArm)) {
                        this.clearPartTransform((class_630)entry.getValue());
                        continue;
                    }
                    if (this.blendFrom.containsKey(boneName)) continue;
                    this.clearPartTransform((class_630)entry.getValue());
                }
                break block50;
            }
            if (!this.overlapCompatIdle || this.overlapClip == null || this.overlapArm == null) break block50;
            if (!clearUnused) {
                return;
            }
            for (Map.Entry<String, Object> entry : partsMap.entrySet()) {
                boneName = entry.getKey();
                if (boneName == null || clearArm != null && this.isBoneForArm(boneName, clearArm) || skipArm != null && this.isBoneForArm(boneName, skipArm) || forcedArm != null && this.isBoneForArm(boneName, forcedArm) || this.isSecondaryBoneActive(boneName, overlapRenderTime) && this.overlapClip.getTracks().containsKey(boneName)) continue;
                this.clearPartTransform((class_630)entry.getValue());
            }
        }
    }

    private void beginBlendOut() {
        if (this.currentClip == null) {
            return;
        }
        this.isPlaying = false;
        this.blendInRemaining = 0.0f;
        this.prevBlendInRemaining = 0.0f;
        if (this.nextBlendOutDuration != null) {
            this.blendOutDuration = this.nextBlendOutDuration.floatValue();
            this.nextBlendOutDuration = null;
        } else {
            this.blendOutDuration = 0.28f;
        }
        if (this.nextBlendOutSlow != null) {
            this.blendOutSlow = this.nextBlendOutSlow.floatValue();
            this.nextBlendOutSlow = null;
        } else {
            this.blendOutSlow = 1.8f;
        }
        this.prevBlendRemaining = this.blendOutDuration;
        this.blendRemaining = this.blendOutDuration;
        this.blendFrom.clear();
        class_1306 arm = this.activeArm != null ? this.activeArm : (this.clipArmFilter ? this.clipArm : null);
        boolean dualHandedActive = this.isDualHandedActive(this.currentTime);
        this.blendArm = arm;
        this.blendArmFilter = arm != null && !dualHandedActive;
        for (Map.Entry<String, BoneTrack> entry : this.currentClip.getTracks().entrySet()) {
            if (this.blendArmFilter && !this.isBoneForArm(entry.getKey(), this.blendArm)) continue;
            Sample sample = this.sampleTrack(entry.getValue(), this.currentTime, this.expressionTime, this.currentClip);
            if (this.rebaseActive) {
                Sample from = this.rebaseFrom.get(entry.getKey());
                Sample origin = this.rebaseOrigin.get(entry.getKey());
                if (from != null && origin != null) {
                    sample = this.rebaseSampleForCurrentClip(entry.getKey(), from, origin, sample, this.expressionTime);
                }
            }
            this.blendFrom.put(entry.getKey(), sample);
        }
        this.speedMultiplier = 1.0f;
        this.timelineSpeedScale = 1.0f;
        this.preBlendInActive = false;
        this.holdOnLastFrame = false;
        this.holdReleasePending = false;
        this.clearArmBlend();
    }

    private boolean isSecondaryBoneActive(String boneName, float time) {
        if (this.overlapClip == null) {
            return false;
        }
        if (this.isDualHandedActive(this.overlapClip, time)) {
            return true;
        }
        return this.overlapArm != null && this.isBoneForArm(boneName, this.overlapArm);
    }

    private float resolveSecondaryArmBlendInT(String boneName, float partialTicks, float time) {
        if (this.overlapArm == null) {
            return 1.0f;
        }
        if (!this.isDualHandedActive(this.overlapClip, time)) {
            return this.resolveArmBlendInT(this.overlapArm, partialTicks);
        }
        class_1306 boneArm = this.resolveBoneArm(boneName);
        if (boneArm == null) {
            return 1.0f;
        }
        return this.resolveArmBlendInT(boneArm, partialTicks);
    }

    private void beginOverlapBlendOut(class_1306 arm) {
        if (this.currentClip == null || arm == null) {
            return;
        }
        this.prevBlendRemaining = this.blendOutDuration = 0.28f;
        this.blendRemaining = this.blendOutDuration;
        this.blendFrom.clear();
        boolean dualHandedActive = this.isDualHandedActive(this.currentTime);
        this.blendArm = arm;
        this.blendArmFilter = arm != null && !dualHandedActive;
        for (Map.Entry<String, BoneTrack> entry : this.currentClip.getTracks().entrySet()) {
            if (this.blendArmFilter && !this.isBoneForArm(entry.getKey(), this.blendArm)) continue;
            Sample sample = this.sampleTrack(entry.getValue(), this.currentTime, this.expressionTime, this.currentClip);
            if (this.rebaseActive) {
                Sample from = this.rebaseFrom.get(entry.getKey());
                Sample origin = this.rebaseOrigin.get(entry.getKey());
                if (from != null && origin != null) {
                    sample = this.rebaseSampleForCurrentClip(entry.getKey(), from, origin, sample, this.expressionTime);
                }
            }
            this.blendFrom.put(entry.getKey(), sample);
        }
    }

    private void preserveOverlapClip(class_1306 arm) {
        if (this.currentClip == null || arm == null) {
            return;
        }
        this.overlapClip = this.currentClip;
        this.overlapTime = this.currentTime;
        this.overlapPrevTime = this.prevTime;
        this.overlapSpeedMultiplier = this.speedMultiplier;
        this.overlapBlendRemaining = 0.0f;
        this.overlapPrevBlendRemaining = 0.0f;
        this.overlapArm = arm;
    }

    private void captureCurrentArmSamplesInto(Map<String, Sample> target, class_1306 arm) {
        if (target == null) {
            return;
        }
        target.clear();
        if (arm == null) {
            return;
        }
        AnimationClip sourceClip = this.currentClip;
        float sourceTime = this.currentTime;
        float sourceEvalTime = this.expressionTime;
        boolean sourceIsOverlap = false;
        if (sourceClip == null && this.overlapCompatIdle && this.overlapClip != null && this.overlapArm == arm) {
            sourceClip = this.overlapClip;
            sourceTime = this.overlapTime;
            sourceEvalTime = this.overlapTime;
            sourceIsOverlap = true;
        }
        if (sourceClip == null) {
            return;
        }
        for (Map.Entry<String, BoneTrack> entry : sourceClip.getTracks().entrySet()) {
            String boneName = entry.getKey();
            if (!this.isBoneForArm(boneName, arm)) continue;
            Sample sample = this.sampleTrack(entry.getValue(), sourceTime, sourceEvalTime, sourceClip);
            if (!sourceIsOverlap && this.rebaseActive) {
                Sample from = this.rebaseFrom.get(boneName);
                Sample origin = this.rebaseOrigin.get(boneName);
                if (from != null && origin != null) {
                    sample = this.rebaseSampleForCurrentClip(boneName, from, origin, sample, this.expressionTime);
                }
            }
            target.put(boneName, sample);
        }
    }

    private void captureVisibleArmSamplesForBlendIn(class_1306 arm) {
        this.blendInFrom.clear();
        this.blendInFromActive = false;
        if (arm == null) {
            return;
        }
        this.captureVisibleArmSamplesInto(this.blendInFrom, arm);
        this.blendInFromActive = !this.blendInFrom.isEmpty();
    }

    private void captureVisibleArmSamplesInto(Map<String, Sample> target, class_1306 arm) {
        if (target == null) {
            return;
        }
        target.clear();
        if (arm == null) {
            return;
        }
        if (this.overlapCompatIdle && this.overlapClip != null && this.overlapArm == arm) {
            for (Map.Entry<String, BoneTrack> entry : this.overlapClip.getTracks().entrySet()) {
                String boneName = entry.getKey();
                if (!this.isBoneForArm(boneName, arm)) continue;
                target.put(boneName, this.sampleTrack(entry.getValue(), this.overlapTime, this.overlapTime, this.overlapClip));
            }
            if (!target.isEmpty()) {
                return;
            }
        }
        this.captureCurrentArmSamplesInto(target, arm);
    }

    private void captureVisibleSamplesForTargetClip(Map<String, Sample> target, class_1306 arm, AnimationClip targetClip) {
        this.captureVisibleSamplesForTargetClip(target, arm, targetClip, false);
    }

    private void captureVisibleSamplesForTargetClip(Map<String, Sample> target, class_1306 arm, AnimationClip targetClip, boolean armPoseOnlyBlend) {
        if (target == null) {
            return;
        }
        target.clear();
        if (arm == null || targetClip == null) {
            return;
        }
        AnimationClip previousClip = this.currentClip;
        float previousTime = this.currentTime;
        float previousExpressionTime = this.expressionTime;
        Map<String, Sample> previousRebaseFrom = this.rebaseFrom;
        Map<String, Sample> previousRebaseOrigin = this.rebaseOrigin;
        boolean previousRebaseActive = this.rebaseActive;
        boolean previousRebaseReturnToOrigin = this.rebaseReturnToOrigin;
        float previousRebaseStartTime = this.rebaseStartTime;
        float previousRebaseReturnSpeedMultiplier = this.rebaseReturnSpeedMultiplier;
        if (previousClip == null && this.overlapCompatIdle && this.overlapClip != null && this.overlapArm == arm) {
            previousClip = this.overlapClip;
            previousTime = this.overlapTime;
            previousExpressionTime = this.overlapTime;
            previousRebaseFrom = Map.of();
            previousRebaseOrigin = Map.of();
            previousRebaseActive = false;
            previousRebaseReturnToOrigin = false;
            previousRebaseStartTime = 0.0f;
            previousRebaseReturnSpeedMultiplier = 1.7f;
        }
        for (Map.Entry<String, BoneTrack> entry : targetClip.getTracks().entrySet()) {
            class_1306 boneArm;
            String boneName = entry.getKey();
            if (armPoseOnlyBlend && this.isItemGripOrHeldItemBone(boneName) || (boneArm = this.resolveBoneArm(boneName)) != null && boneArm != arm) continue;
            Sample sample = this.sampleVisibleSourceForRebase(boneName, boneArm, previousClip, previousTime, previousExpressionTime, previousRebaseFrom, previousRebaseOrigin, previousRebaseActive, previousRebaseReturnToOrigin, previousRebaseStartTime, previousRebaseReturnSpeedMultiplier);
            target.put(boneName, sample);
        }
    }

    private boolean isItemGripOrHeldItemBone(String boneName) {
        if (boneName == null) {
            return false;
        }
        String normalized = boneName.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("item_") || normalized.endsWith("_item") || normalized.startsWith("itemgrip_") || normalized.endsWith("_itemgrip") || normalized.contains("itemgrip");
    }

    private boolean shouldForceBlendInFromVisibleSource() {
        return !this.isPlaying && this.blendRemaining <= 0.0f && this.overlapCompatIdle && this.overlapClip != null && this.overlapArm != null;
    }

    private void debugCompatIdle(String phase, String extra) {
    }

    private String clipName(AnimationClip clip) {
        return clip == null ? "null" : clip.getName();
    }

    private class_1306 resolveVisibleSourceArm() {
        if (this.activeArm != null) {
            return this.activeArm;
        }
        if (this.clipArmFilter && this.clipArm != null) {
            return this.clipArm;
        }
        if (this.overlapCompatIdle && this.overlapArm != null) {
            return this.overlapArm;
        }
        return null;
    }

    private Matrix4f calculateMatrix(BoneTrack track, float time) {
        Sample sample = this.sampleTrack(track, time, time, this.currentClip);
        return this.matrixFromSample(sample, this.currentClip != null && this.currentClip.isEffectActive("use_keyframes_v2", time));
    }

    private Matrix4f matrixFromKeyframe(Keyframe kf) {
        Vector3f pos = kf.sampledPosition(this.expressionTime);
        Vector3f euler = kf.sampledRotationEuler(this.expressionTime);
        Vector3f scl = kf.sampledScale(this.expressionTime);
        Quaternionf rot = this.quatFromEuler(euler, this.currentClip, this.currentTime);
        return new Matrix4f().translate((Vector3fc)pos).rotate((Quaternionfc)rot).scale((Vector3fc)scl);
    }

    private Sample sampleTrack(BoneTrack track, float time, AnimationClip clip) {
        return this.sampleTrack(track, time, time, clip);
    }

    private Sample sampleTrack(BoneTrack track, float time, float evalTime, AnimationClip clip) {
        TreeMap<Float, Keyframe> index = track.getTimeIndex();
        boolean useKeyframesV2 = clip != null && clip.isEffectActive("use_keyframes_v2", time);
        Keyframe prevPos = this.findPrevWithPosition(index, time);
        Keyframe nextPos = this.findNextWithPosition(index, time);
        Vector3f pos = this.sampleVectorChannel(prevPos, nextPos, time, evalTime, new Vector3f(0.0f, 0.0f, 0.0f), useKeyframesV2 ? (kf, t) -> kf.sampledPositionV2(t.floatValue()) : (kf, t) -> kf.sampledPosition(t.floatValue()));
        Keyframe prevRot = this.findPrevWithRotation(index, time);
        Keyframe nextRot = this.findNextWithRotation(index, time);
        Quaternionf rot = this.sampleRotationChannel(index, prevRot, nextRot, time, evalTime, clip);
        Vector3f rotEulerV2 = useKeyframesV2 ? this.sampleRotationEulerChannelV2(index, prevRot, nextRot, time, evalTime) : null;
        Keyframe prevScale = this.findPrevWithScale(index, time);
        Keyframe nextScale = this.findNextWithScale(index, time);
        Vector3f scl = this.sampleVectorChannel(prevScale, nextScale, time, evalTime, new Vector3f(1.0f, 1.0f, 1.0f), (kf, t) -> kf.sampledScale(t.floatValue()));
        return new Sample(pos, rot, scl, rotEulerV2);
    }

    private static String fmt(Vector3f v) {
        if (v == null) {
            return "null";
        }
        return String.format(Locale.ROOT, "[%.3f, %.3f, %.3f]", Float.valueOf(v.x), Float.valueOf(v.y), Float.valueOf(v.z));
    }

    private static String fmt(Quaternionf q) {
        if (q == null) {
            return "null";
        }
        return String.format(Locale.ROOT, "[%.5f, %.5f, %.5f, %.5f]", Float.valueOf(q.x), Float.valueOf(q.y), Float.valueOf(q.z), Float.valueOf(q.w));
    }

    private static String fmt(float value) {
        return String.format(Locale.ROOT, "%.3f", Float.valueOf(value));
    }

    private Keyframe findPrevWithPosition(TreeMap<Float, Keyframe> index, float time) {
        Float key = index.floorKey(Float.valueOf(time));
        while (key != null) {
            Keyframe kf = index.get(key);
            if (kf != null && (kf.position() != null || kf.positionExpression() != null)) {
                return kf;
            }
            key = index.lowerKey(key);
        }
        return null;
    }

    private Keyframe findNextWithPosition(TreeMap<Float, Keyframe> index, float time) {
        Float key = index.ceilingKey(Float.valueOf(time));
        while (key != null) {
            Keyframe kf = index.get(key);
            if (kf != null && (kf.position() != null || kf.positionExpression() != null)) {
                return kf;
            }
            key = index.higherKey(key);
        }
        return null;
    }

    private Keyframe findPrevWithRotation(TreeMap<Float, Keyframe> index, float time) {
        Float key = index.floorKey(Float.valueOf(time));
        while (key != null) {
            Keyframe kf = index.get(key);
            if (kf != null && (kf.rotationEuler() != null || kf.rotationExpression() != null)) {
                return kf;
            }
            key = index.lowerKey(key);
        }
        return null;
    }

    private Keyframe findNextWithRotation(TreeMap<Float, Keyframe> index, float time) {
        Float key = index.ceilingKey(Float.valueOf(time));
        while (key != null) {
            Keyframe kf = index.get(key);
            if (kf != null && (kf.rotationEuler() != null || kf.rotationExpression() != null)) {
                return kf;
            }
            key = index.higherKey(key);
        }
        return null;
    }

    private Keyframe findPrevWithScale(TreeMap<Float, Keyframe> index, float time) {
        Float key = index.floorKey(Float.valueOf(time));
        while (key != null) {
            Keyframe kf = index.get(key);
            if (kf != null && (kf.scale() != null || kf.scaleExpression() != null)) {
                return kf;
            }
            key = index.lowerKey(key);
        }
        return null;
    }

    private Keyframe findNextWithScale(TreeMap<Float, Keyframe> index, float time) {
        Float key = index.ceilingKey(Float.valueOf(time));
        while (key != null) {
            Keyframe kf = index.get(key);
            if (kf != null && (kf.scale() != null || kf.scaleExpression() != null)) {
                return kf;
            }
            key = index.higherKey(key);
        }
        return null;
    }

    private Vector3f sampleVectorChannel(Keyframe prev, Keyframe next, float time, float evalTime, Vector3f fallback, BiFunction<Keyframe, Float, Vector3f> getter) {
        if (prev == null && next == null) {
            return new Vector3f((Vector3fc)fallback);
        }
        if (prev == null) {
            Vector3f v = getter.apply(next, Float.valueOf(evalTime));
            return v == null ? new Vector3f((Vector3fc)fallback) : new Vector3f((Vector3fc)v);
        }
        if (next == null) {
            Vector3f v = getter.apply(prev, Float.valueOf(evalTime));
            return v == null ? new Vector3f((Vector3fc)fallback) : new Vector3f((Vector3fc)v);
        }
        Vector3f v1 = getter.apply(prev, Float.valueOf(evalTime));
        Vector3f v2 = getter.apply(next, Float.valueOf(evalTime));
        if (v1 == null && v2 == null) {
            return new Vector3f((Vector3fc)fallback);
        }
        if (v1 == null) {
            return new Vector3f((Vector3fc)v2);
        }
        if (v2 == null) {
            return new Vector3f((Vector3fc)v1);
        }
        float t1 = prev.timeStamp();
        float t2 = next.timeStamp();
        float delta = t2 - t1;
        float progress = delta == 0.0f ? 0.0f : (time - t1) / delta;
        return new Vector3f((Vector3fc)v1).lerp((Vector3fc)v2, progress);
    }

    private Quaternionf sampleRotationChannel(TreeMap<Float, Keyframe> index, Keyframe prev, Keyframe next, float time, float evalTime, AnimationClip clip) {
        boolean useKeyframesV2;
        boolean bl = useKeyframesV2 = clip != null && clip.isEffectActive("use_keyframes_v2", time);
        if (useKeyframesV2) {
            return this.sampleRotationChannelV2(index, prev, next, time, evalTime);
        }
        if (prev == null && next == null) {
            return new Quaternionf();
        }
        if (prev == null) {
            Vector3f euler = next.sampledRotationEuler(evalTime);
            return euler == null ? new Quaternionf() : this.quatFromEuler(euler, clip, time);
        }
        if (next == null) {
            Vector3f euler = prev.sampledRotationEuler(evalTime);
            return euler == null ? new Quaternionf() : this.quatFromEuler(euler, clip, time);
        }
        Vector3f eulerPrev = prev.sampledRotationEuler(evalTime);
        Vector3f eulerNext = next.sampledRotationEuler(evalTime);
        if (eulerPrev == null && eulerNext == null) {
            return new Quaternionf();
        }
        if (eulerPrev == null) {
            return this.quatFromEuler(eulerNext, clip, time);
        }
        if (eulerNext == null) {
            return this.quatFromEuler(eulerPrev, clip, time);
        }
        float t1 = prev.timeStamp();
        float t2 = next.timeStamp();
        float delta = t2 - t1;
        float progress = delta == 0.0f ? 0.0f : (time - t1) / delta;
        progress = class_3532.method_15363((float)progress, (float)0.0f, (float)1.0f);
        Vector3f out = new Vector3f(PoseHandler.lerpAngleDegrees(eulerPrev.x, eulerNext.x, progress), PoseHandler.lerpAngleDegrees(eulerPrev.y, eulerNext.y, progress), PoseHandler.lerpAngleDegrees(eulerPrev.z, eulerNext.z, progress));
        return this.quatFromEuler(out, clip, time);
    }

    private Quaternionf sampleRotationChannelV2(TreeMap<Float, Keyframe> index, Keyframe prev, Keyframe next, float time, float evalTime) {
        Vector3f euler = this.sampleRotationEulerChannelV2(index, prev, next, time, evalTime);
        return euler == null ? new Quaternionf() : PoseHandler.quatFromEulerV2(euler);
    }

    private Vector3f sampleRotationEulerChannelV2(TreeMap<Float, Keyframe> index, Keyframe prev, Keyframe next, float time, float evalTime) {
        if (prev == null && next == null) {
            return null;
        }
        if (prev == null) {
            return next.sampledRotationPreEulerV2(evalTime);
        }
        if (next == null) {
            return prev.sampledRotationPostEulerV2(evalTime);
        }
        if (prev == next || Math.abs(prev.timeStamp() - next.timeStamp()) <= 1.0E-4f) {
            return prev.sampledRotationPreEulerV2(evalTime);
        }
        float t1 = prev.timeStamp();
        float t2 = next.timeStamp();
        float delta = t2 - t1;
        float progress = delta == 0.0f ? 0.0f : (time - t1) / delta;
        progress = class_3532.method_15363((float)progress, (float)0.0f, (float)1.0f);
        String fromInterpolation = PoseHandler.normalizeInterpolation(prev.interpolation());
        String toInterpolation = PoseHandler.normalizeInterpolation(next.interpolation());
        if ("spherical_squad".equals(fromInterpolation) || "spherical_squad".equals(toInterpolation)) {
            Quaternionf rot = this.sampleRotationSphericalSquadV2(index, prev, next, evalTime, progress);
            return rot.getEulerAnglesXYZ(new Vector3f()).mul(57.29578f);
        }
        if ("spherical_linear".equals(fromInterpolation) || "spherical_linear".equals(toInterpolation)) {
            Quaternionf from = PoseHandler.quatFromEulerV2(prev.sampledRotationPostEulerV2(evalTime));
            Quaternionf to = PoseHandler.quatFromEulerV2(next.sampledRotationPreEulerV2(evalTime));
            return from.slerp((Quaternionfc)to, progress).getEulerAnglesXYZ(new Vector3f()).mul(57.29578f);
        }
        if ("catmullrom".equals(fromInterpolation) || "catmullrom".equals(toInterpolation)) {
            return this.sampleRotationCatmullRomEulerV2(index, prev, next, evalTime, progress);
        }
        Vector3f from = prev.sampledRotationPostEulerV2(evalTime);
        Vector3f to = next.sampledRotationPreEulerV2(evalTime);
        if (from == null && to == null) {
            return null;
        }
        if (from == null) {
            return to;
        }
        if (to == null) {
            return from;
        }
        return new Vector3f((Vector3fc)from).lerp((Vector3fc)to, progress);
    }

    private Quaternionf sampleRotationCatmullRomV2(TreeMap<Float, Keyframe> index, Keyframe prev, Keyframe next, float evalTime, float progress) {
        Vector3f euler = this.sampleRotationCatmullRomEulerV2(index, prev, next, evalTime, progress);
        return euler == null ? new Quaternionf() : PoseHandler.quatFromEulerV2(euler);
    }

    private Vector3f sampleRotationCatmullRomEulerV2(TreeMap<Float, Keyframe> index, Keyframe prev, Keyframe next, float evalTime, float progress) {
        Keyframe beforePrev = this.findPrevWithRotation(index, prev.timeStamp() - 1.0E-4f);
        Keyframe afterNext = this.findNextWithRotation(index, next.timeStamp() + 1.0E-4f);
        Vector3f p0 = this.getRotationValueForSpline(beforePrev != null ? beforePrev : prev, true, evalTime);
        Vector3f p1 = this.getRotationValueForSpline(prev, false, evalTime);
        Vector3f p2 = this.getRotationValueForSpline(next, false, evalTime);
        Vector3f p3 = this.getRotationValueForSpline(afterNext != null ? afterNext : next, false, evalTime);
        Vector3f euler = new Vector3f(PoseHandler.splineCurve(p0.x, p1.x, p2.x, p3.x, progress), PoseHandler.splineCurve(p0.y, p1.y, p2.y, p3.y, progress), PoseHandler.splineCurve(p0.z, p1.z, p2.z, p3.z, progress));
        return euler;
    }

    private Quaternionf sampleRotationSphericalSquadV2(TreeMap<Float, Keyframe> index, Keyframe prev, Keyframe next, float evalTime, float progress) {
        Keyframe beforePrev = this.findPrevWithRotation(index, prev.timeStamp() - 1.0E-4f);
        Keyframe afterNext = this.findNextWithRotation(index, next.timeStamp() + 1.0E-4f);
        Quaternionf q0 = PoseHandler.quatFromEulerV2(this.getRotationValueForSpline(beforePrev != null ? beforePrev : prev, true, evalTime));
        Quaternionf q1 = PoseHandler.quatFromEulerV2(this.getRotationValueForSpline(prev, false, evalTime));
        Quaternionf q2 = PoseHandler.quatFromEulerV2(this.getRotationValueForSpline(next, false, evalTime));
        Quaternionf q3 = PoseHandler.quatFromEulerV2(this.getRotationValueForSpline(afterNext != null ? afterNext : next, false, evalTime));
        return PoseHandler.squad(q0, q1, q2, q3, progress);
    }

    private Vector3f getRotationValueForSpline(Keyframe keyframe, boolean useOffset, float evalTime) {
        if (keyframe == null) {
            return new Vector3f();
        }
        Vector3f value = useOffset ? keyframe.sampledRotationPostEulerV2(evalTime) : keyframe.sampledRotationPreEulerV2(evalTime);
        return value == null ? new Vector3f() : value;
    }

    private static String normalizeInterpolation(String interpolation) {
        if (interpolation == null || interpolation.isBlank()) {
            return "linear";
        }
        return interpolation.trim().toLowerCase(Locale.ROOT);
    }

    private static float splineCurve(float y0, float y1, float y2, float y3, float alpha) {
        float v0 = (y2 - y0) * 0.5f;
        float v1 = (y3 - y1) * 0.5f;
        float t2 = alpha * alpha;
        float t3 = alpha * t2;
        float h1 = 2.0f * t3 - 3.0f * t2 + 1.0f;
        float h2 = -2.0f * t3 + 3.0f * t2;
        float h3 = t3 - 2.0f * t2 + alpha;
        float h4 = t3 - t2;
        return h1 * y1 + h2 * y2 + h3 * v0 + h4 * v1;
    }

    private static float lerpAngleDegrees(float a, float b, float t) {
        float delta = b - a;
        float abs = Math.abs(delta);
        if (abs > 180.0f && abs < 359.9999f) {
            delta -= Math.signum(delta) * 360.0f;
        }
        return a + delta * t;
    }

    private Sample blendToIdentity(Sample from, float t) {
        Vector3f pos = new Vector3f((Vector3fc)from.pos).lerp((Vector3fc)new Vector3f(0.0f, 0.0f, 0.0f), t);
        Quaternionf rot = new Quaternionf((Quaternionfc)from.rot).slerp((Quaternionfc)new Quaternionf(), t);
        Vector3f scl = new Vector3f((Vector3fc)from.scl).lerp((Vector3fc)new Vector3f(1.0f, 1.0f, 1.0f), t);
        Vector3f rotEulerV2 = from.rotEulerV2 == null ? null : new Vector3f((Vector3fc)from.rotEulerV2).lerp((Vector3fc)new Vector3f(), t);
        return new Sample(pos, rot, scl, rotEulerV2);
    }

    private Quaternionf quatFromEuler(Vector3f eulerDeg, AnimationClip clip, float sampleTime) {
        boolean useXYZ;
        float rx = (float)Math.toRadians(eulerDeg.x);
        float ry = (float)Math.toRadians(eulerDeg.y);
        float rz = (float)Math.toRadians(eulerDeg.z);
        if (this.javaBlock) {
            float rawX = (float)Math.toRadians(-eulerDeg.x);
            float rawY = (float)Math.toRadians(-eulerDeg.z);
            float rawZ = (float)Math.toRadians(-eulerDeg.y);
            return new Quaternionf().rotationXYZ(rawX, rawY, rawZ);
        }
        boolean forceXYZ = clip != null && (clip.isEffectActive("rotation_xyz", sampleTime) || clip.isEffectActive("rot_xyz", sampleTime));
        boolean forceZYX = clip != null && (clip.isEffectActive("rotation_zyx", sampleTime) || clip.isEffectActive("rot_zyx", sampleTime));
        String clipName = clip != null && clip.getName() != null ? clip.getName() : "";
        boolean bl = useXYZ = clip != null && (clipName.contains("mace") || clipName.contains("trident") || clipName.contains("crossbow"));
        if (forceZYX) {
            useXYZ = false;
        } else if (forceXYZ) {
            useXYZ = true;
        }
        if (useXYZ) {
            return new Quaternionf().rotationXYZ(rx, ry, rz);
        }
        return new Quaternionf().rotationZYX(rz, ry, rx);
    }

    private static Quaternionf quatFromEulerV2(Vector3f eulerDeg) {
        if (eulerDeg == null) {
            return new Quaternionf();
        }
        float rx = (float)Math.toRadians(eulerDeg.x);
        float ry = (float)Math.toRadians(eulerDeg.y);
        float rz = (float)Math.toRadians(eulerDeg.z);
        return new Quaternionf().rotationXYZ(rx, ry, rz);
    }

    private static Quaternionf squad(Quaternionf q0, Quaternionf q1, Quaternionf q2, Quaternionf q3, float t) {
        Quaternionf a = PoseHandler.intermediate(q0, q1, q2);
        Quaternionf b = PoseHandler.intermediate(q1, q2, q3);
        Quaternionf slerp1 = new Quaternionf((Quaternionfc)q1).slerp((Quaternionfc)q2, t);
        Quaternionf slerp2 = new Quaternionf((Quaternionfc)a).slerp((Quaternionfc)b, t);
        return slerp1.slerp((Quaternionfc)slerp2, 2.0f * t * (1.0f - t));
    }

    private static Quaternionf intermediate(Quaternionf q0, Quaternionf q1, Quaternionf q2) {
        Quaternionf a = new Quaternionf((Quaternionfc)q0);
        Quaternionf b = new Quaternionf((Quaternionfc)q2);
        if (q1.dot(a) < 0.0f) {
            a.mul(-1.0f);
        }
        if (q1.dot(b) < 0.0f) {
            b.mul(-1.0f);
        }
        Quaternionf q0Conj = new Quaternionf((Quaternionfc)a).conjugate();
        Quaternionf q1Conj = new Quaternionf((Quaternionfc)q1).conjugate();
        Quaternionf m0 = q0Conj.mul((Quaternionfc)new Quaternionf((Quaternionfc)q1));
        Quaternionf m1 = q1Conj.mul((Quaternionfc)new Quaternionf((Quaternionfc)b));
        Quaternionf m0Log = PoseHandler.quaternionLog(m0);
        Quaternionf m1Log = PoseHandler.quaternionLog(m1);
        Quaternionf sum = new Quaternionf(m0Log.x - m1Log.x, m0Log.y - m1Log.y, m0Log.z - m1Log.z, m0Log.w - m1Log.w).mul(0.25f);
        Quaternionf exp = PoseHandler.quaternionExp(sum);
        return new Quaternionf((Quaternionfc)q1).mul((Quaternionfc)exp);
    }

    private static Quaternionf quaternionLog(Quaternionf q) {
        double sin = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
        double theta = Math.atan2(sin, q.w);
        Quaternionf result = new Quaternionf((Quaternionfc)q);
        if (sin > 5.0E-4) {
            result.mul((float)(theta / sin));
        }
        result.w = 0.0f;
        return result;
    }

    private static Quaternionf quaternionExp(Quaternionf q) {
        double theta = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
        double cos = Math.cos(theta);
        Quaternionf result = new Quaternionf((Quaternionfc)q);
        if (cos < 0.9995 && theta > 1.0E-7) {
            result.mul((float)(Math.sin(theta) / theta));
        }
        result.w = (float)cos;
        return result;
    }

    private Sample blendFromIdentity(Sample to, float t) {
        Vector3f pos = new Vector3f(0.0f, 0.0f, 0.0f).lerp((Vector3fc)to.pos, t);
        Quaternionf rot = new Quaternionf().slerp((Quaternionfc)to.rot, t);
        Vector3f scl = new Vector3f(1.0f, 1.0f, 1.0f).lerp((Vector3fc)to.scl, t);
        Vector3f rotEulerV2 = to.rotEulerV2 == null ? null : new Vector3f((Vector3fc)to.rotEulerV2).mul(t);
        return new Sample(pos, rot, scl, rotEulerV2);
    }

    private Sample blendBetweenSamples(Sample from, Sample to, float t) {
        Vector3f pos = new Vector3f((Vector3fc)from.pos).lerp((Vector3fc)to.pos, t);
        Quaternionf rot = new Quaternionf((Quaternionfc)from.rot).slerp((Quaternionfc)to.rot, t);
        Vector3f scl = new Vector3f((Vector3fc)from.scl).lerp((Vector3fc)to.scl, t);
        Vector3f rotEulerV2 = null;
        if (from.rotEulerV2 != null || to.rotEulerV2 != null) {
            Vector3f start = from.rotEulerV2 != null ? new Vector3f((Vector3fc)from.rotEulerV2) : new Vector3f();
            Vector3f end = to.rotEulerV2 != null ? new Vector3f((Vector3fc)to.rotEulerV2) : new Vector3f();
            rotEulerV2 = start.lerp((Vector3fc)end, t);
        }
        return new Sample(pos, rot, scl, rotEulerV2);
    }

    private Sample rebaseSample(Sample from, Sample origin, Sample target) {
        Vector3f posDelta = new Vector3f((Vector3fc)target.pos).sub((Vector3fc)origin.pos);
        Vector3f pos = new Vector3f((Vector3fc)from.pos).add((Vector3fc)posDelta);
        Quaternionf invOrigin = new Quaternionf((Quaternionfc)origin.rot).invert();
        Quaternionf rotDelta = invOrigin.mul((Quaternionfc)new Quaternionf((Quaternionfc)target.rot), new Quaternionf());
        Quaternionf rot = new Quaternionf((Quaternionfc)from.rot).mul((Quaternionfc)rotDelta).normalize();
        Vector3f scl = new Vector3f((Vector3fc)target.scl);
        return new Sample(pos, rot, scl, target.rotEulerV2);
    }

    private Sample rebaseSampleForCurrentClip(String boneName, Sample from, Sample origin, Sample target, float evalTime) {
        if (this.shouldBypassRebaseForBone(boneName)) {
            return target;
        }
        Sample rebased = this.rebaseSample(from, origin, target);
        if (!this.rebaseReturnToOrigin || this.currentClip == null) {
            return rebased;
        }
        float progress = this.resolveRebaseReturnProgress(this.currentClip, evalTime);
        if (progress <= 0.0f) {
            return rebased;
        }
        if (progress >= 1.0f) {
            return target;
        }
        return this.blendBetweenSamples(rebased, target, progress);
    }

    private boolean shouldBypassRebaseForBone(String boneName) {
        if (boneName == null || boneName.isBlank()) {
            return false;
        }
        String normalized = boneName.toLowerCase(Locale.ROOT);
        return normalized.contains("eyelid") || normalized.contains("pupil") || normalized.startsWith("eye_") || normalized.endsWith("_eye") || normalized.equals("eye") || normalized.contains("fin_") || normalized.endsWith("_fin");
    }

    private float resolveRebaseReturnProgress(AnimationClip clip, float evalTime) {
        if (clip == null) {
            return 0.0f;
        }
        float length = clip.getLength();
        if (length <= 0.0f) {
            return 1.0f;
        }
        float start = Math.max(0.0f, Math.min(this.rebaseStartTime, length));
        if (evalTime <= start) {
            return 0.0f;
        }
        float duration = Math.max(1.0E-4f, length - start);
        float rawProgress = class_3532.method_15363((float)((evalTime - start) / duration * this.rebaseReturnSpeedMultiplier), (float)0.0f, (float)1.0f);
        return this.easeOutCubic(rawProgress);
    }

    private float easeOutCubic(float value) {
        float t = class_3532.method_15363((float)value, (float)0.0f, (float)1.0f);
        float inv = 1.0f - t;
        return 1.0f - inv * inv * inv;
    }

    private Matrix4f matrixFromSample(Sample sample, boolean useKeyframesV2) {
        Vector3f pos = new Vector3f((Vector3fc)sample.pos);
        if (!useKeyframesV2 && this.javaBlock) {
            pos.set(-pos.x, -pos.z, -pos.y);
        } else if (!useKeyframesV2 && this.swapTranslationYZ) {
            pos.set(pos.x, pos.z, pos.y);
        }
        pos.x += 0.0f;
        pos.z += 0.0f;
        Matrix4f transform = new Matrix4f().translate((Vector3fc)pos);
        if (useKeyframesV2 && sample.rotEulerV2 != null) {
            float rx = (float)Math.toRadians(sample.rotEulerV2.x);
            float ry = (float)Math.toRadians(sample.rotEulerV2.y);
            float rz = (float)Math.toRadians(sample.rotEulerV2.z);
            transform.rotateXYZ(rx, ry, rz);
        } else {
            transform.rotate((Quaternionfc)sample.rot);
        }
        transform.scale((Vector3fc)sample.scl);
        if (!useKeyframesV2 && this.mirrorX) {
            Matrix4f mirror = new Matrix4f().scale(-1.0f, 1.0f, 1.0f);
            transform = new Matrix4f((Matrix4fc)mirror).mul((Matrix4fc)transform).mul((Matrix4fc)mirror);
        }
        return transform;
    }

    private void clearTransforms(Map<String, class_630> partsMap) {
        if (partsMap == null || partsMap.isEmpty()) {
            return;
        }
        if (this.currentClip == null) {
            for (class_630 part : partsMap.values()) {
                this.clearPartTransform(part);
            }
            return;
        }
        for (String boneName : this.currentClip.getTracks().keySet()) {
            if (this.isPlaying && this.activeArm != null && !this.isBoneForArm(boneName, this.activeArm)) continue;
            class_630 part = partsMap.get(boneName);
            this.clearPartTransform(part);
        }
    }

    private void clearTransformsForClipArm(Map<String, class_630> partsMap) {
        if (this.currentClip == null) {
            return;
        }
        for (String boneName : this.currentClip.getTracks().keySet()) {
            if (this.clipArmFilter && !this.isBoneForArm(boneName, this.clipArm)) continue;
            class_630 part = partsMap.get(boneName);
            this.clearPartTransform(part);
        }
    }

    private void clearTransformsForArm(Map<String, class_630> partsMap, class_1306 arm) {
        if (this.currentClip == null || arm == null) {
            return;
        }
        for (String boneName : this.currentClip.getTracks().keySet()) {
            if (!this.isBoneForArm(boneName, arm)) continue;
            class_630 part = partsMap.get(boneName);
            this.clearPartTransform(part);
        }
    }

    private Map<String, PartPoseSnapshot> capturePartSnapshots(Map<String, class_630> partsMap) {
        HashMap<String, PartPoseSnapshot> snapshots = new HashMap<String, PartPoseSnapshot>();
        if (partsMap == null || partsMap.isEmpty()) {
            return snapshots;
        }
        for (Map.Entry<String, class_630> entry : partsMap.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            snapshots.put(entry.getKey(), PartPoseSnapshot.of(entry.getValue()));
        }
        return snapshots;
    }

    private void applyV2ToPart(class_630 part, PartPoseSnapshot base, Sample sample) {
        if (part == null || sample == null) {
            return;
        }
        PartPoseSnapshot snapshot = base != null ? base : PartPoseSnapshot.of(part);
        Vector3f pos = sample.pos != null ? sample.pos : new Vector3f();
        Vector3f rotEuler = sample.rotEulerV2 != null ? sample.rotEulerV2 : new Vector3f();
        part.field_3657 = snapshot.x + pos.x;
        part.field_3656 = snapshot.y + pos.y;
        part.field_3655 = snapshot.z + pos.z;
        part.field_3654 = snapshot.xRot + (float)Math.toRadians(rotEuler.x);
        part.field_3675 = snapshot.yRot + (float)Math.toRadians(rotEuler.y);
        part.field_3674 = snapshot.zRot + (float)Math.toRadians(rotEuler.z);
        part.field_37938 = snapshot.xScale * sample.scl.x;
        part.field_37939 = snapshot.yScale * sample.scl.y;
        part.field_37940 = snapshot.zScale * sample.scl.z;
        this.clearPartTransform(part);
        class_630 class_6302 = part;
        if (class_6302 instanceof TransformablePart) {
            TransformablePart transformable = (TransformablePart)class_6302;
            transformable.punchy$setDirectPoseBase(snapshot.x, snapshot.y, snapshot.z, snapshot.xRot, snapshot.yRot, snapshot.zRot);
        }
    }

    public class_1306 getActiveArm() {
        return this.activeArm;
    }

    private void clearPartTransform(class_630 part) {
        class_630 class_6302;
        if (part != null && (class_6302 = part) instanceof TransformablePart) {
            TransformablePart transformable = (TransformablePart)class_6302;
            transformable.punchy$setExplicitTransform(null);
            transformable.punchy$clearDirectPoseBase();
        }
    }

    private void clearArmBlend() {
        this.leftArmBlendRemaining = 0.0f;
        this.leftArmBlendPrevRemaining = 0.0f;
        this.rightArmBlendRemaining = 0.0f;
        this.rightArmBlendPrevRemaining = 0.0f;
    }

    private void tickArmBlend(float animDelta) {
        if (!this.isPlaying || this.currentClip == null) {
            return;
        }
        if (this.leftArmBlendRemaining > 0.0f) {
            this.leftArmBlendPrevRemaining = this.leftArmBlendRemaining;
            this.leftArmBlendRemaining = Math.max(0.0f, this.leftArmBlendRemaining - animDelta / 1.0f);
        }
        if (this.rightArmBlendRemaining > 0.0f) {
            this.rightArmBlendPrevRemaining = this.rightArmBlendRemaining;
            this.rightArmBlendRemaining = Math.max(0.0f, this.rightArmBlendRemaining - animDelta / 1.0f);
        }
    }

    private float resolveArmBlendInT(class_1306 arm, float partialTicks) {
        float remaining = this.getArmBlendRemaining(arm);
        if (remaining <= 0.0f) {
            return 1.0f;
        }
        float prevRemaining = this.getArmBlendPrevRemaining(arm);
        float blendRem = class_3532.method_16439((float)partialTicks, (float)prevRemaining, (float)remaining);
        float t = 1.0f - blendRem / Math.max(1.0E-4f, 0.2f);
        return this.applyBlendInEasing(t);
    }

    private float applyBlendInEasing(float t) {
        return switch (this.blendInEasing.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> Easing.smoothStep(t);
            case 0 -> Easing.easeInBack(t);
            case 2 -> Easing.easeOutBackStrong(t);
        };
    }

    private void tickArmTuningSuppression(float deltaSeconds) {
        boolean overlapDisableArmTuning;
        boolean disableArmTuning;
        this.prevArmTuningSuppressAlpha = this.armTuningSuppressAlpha;
        boolean equipClip = this.isPlaying && this.isEquipClip(this.currentClip);
        boolean bl = disableArmTuning = this.isPlaying && this.currentClip != null && this.currentClip.hasEffectAny("disable_arm_tuning") && this.currentClip.isEffectActive("disable_arm_tuning", this.currentTime);
        if (this.isWalkOnWaterClip(this.currentClip)) {
            disableArmTuning = false;
        }
        if (equipClip) {
            String key;
            String name = this.currentClip != null ? this.currentClip.getName() : "";
            String string = key = name == null ? "" : name.toLowerCase(Locale.ROOT);
            if (key.equals("hand_in") || key.startsWith("hand_in_")) {
                if (disableArmTuning) {
                    this.armTuningSuppressAlpha = 1.0f;
                    this.prevArmTuningSuppressAlpha = 1.0f;
                } else {
                    this.clearArmTuningSuppression();
                }
                return;
            }
        }
        boolean bl2 = overlapDisableArmTuning = this.overlapCompatIdle && this.overlapClip != null && this.overlapClip.hasEffectAny("disable_arm_tuning") && this.overlapClip.isEffectActive("disable_arm_tuning", this.overlapTime);
        if (this.isWalkOnWaterClip(this.overlapClip)) {
            overlapDisableArmTuning = false;
        }
        disableArmTuning = disableArmTuning || overlapDisableArmTuning;
        boolean keepArmTuning = this.isArmTuningKeepActive(this.currentTime);
        if (keepArmTuning && !disableArmTuning) {
            this.clearArmTuningSuppression();
            return;
        }
        boolean shouldSuppress = this.isPlaying && this.currentClip != null && (!equipClip || disableArmTuning) || overlapDisableArmTuning;
        float target = shouldSuppress ? 1.0f : 0.0f;
        float duration = shouldSuppress ? 0.27f : 0.48f;
        float step = duration <= 0.0f ? 1.0f : deltaSeconds / duration;
        step = class_3532.method_15363((float)step, (float)0.0f, (float)1.0f);
        this.armTuningSuppressAlpha = class_3532.method_16439((float)step, (float)this.armTuningSuppressAlpha, (float)target);
    }

    private void clearArmTuningSuppression() {
        this.armTuningSuppressAlpha = 0.0f;
        this.prevArmTuningSuppressAlpha = 0.0f;
    }

    private boolean isArmTuningKeepActive(float time) {
        boolean timelineKeep;
        if (!this.isPlaying || this.currentClip == null) {
            return this.overlapCompatIdle && this.overlapClip != null && this.overlapClip.hasEffectAny("keep_arm_tuning") && this.overlapClip.isEffectActive("keep_arm_tuning", this.overlapTime);
        }
        boolean bl = timelineKeep = this.currentClip.hasEffectAny("keep_arm_tuning") && this.currentClip.isEffectActive("keep_arm_tuning", time);
        if (timelineKeep) {
            return true;
        }
        if (this.overlapCompatIdle && this.overlapClip != null && this.overlapClip.hasEffectAny("keep_arm_tuning") && this.overlapClip.isEffectActive("keep_arm_tuning", this.overlapTime)) {
            return true;
        }
        return this.isCompatKeepArmTuningActive(time);
    }

    private boolean isCompatKeepArmTuningActive(float time) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return false;
        }
        class_746 player = mc.field_1724;
        if (this.activeArm == null) {
            return this.hasCompatKeepArmTuning(player, class_1306.field_6182) || this.hasCompatKeepArmTuning(player, class_1306.field_6183);
        }
        if (this.hasCompatKeepArmTuning(player, this.activeArm)) {
            return true;
        }
        if (this.isDualHandedActive(time)) {
            class_1306 other = this.activeArm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
            return this.hasCompatKeepArmTuning(player, other);
        }
        return false;
    }

    private boolean hasCompatKeepArmTuning(class_746 player, class_1306 arm) {
        ToolTuning.Profile profile = this.resolveCompatProfileForArm(player, arm);
        return profile != null && Boolean.TRUE.equals(profile.keepArmTuning);
    }

    private boolean isCompatKeepTriggerArmTuningActive(float time, class_1306 arm) {
        if (!this.isPlaying || this.currentClip == null || arm == null) {
            return false;
        }
        if (this.latchedKeepTriggerArmTuning && this.latchedTriggerArm != null) {
            return arm == this.latchedTriggerArm;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return false;
        }
        class_746 player = mc.field_1724;
        ToolTuning.Profile profile = this.resolveCompatProfileForArm(player, arm);
        class_1306 triggerArm = this.activeArm != null ? this.activeArm : PunchyAnimationManager.getSourceArm();
        return profile != null && Boolean.TRUE.equals(profile.keepTriggerArmTuning) && triggerArm != null && arm == triggerArm;
    }

    private void latchCompatKeepTriggerArmState() {
        class_1306 triggerArm;
        this.latchedKeepTriggerArmTuning = false;
        this.latchedTriggerArm = null;
        if (!this.isPlaying || this.currentClip == null) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return;
        }
        class_1306 class_13062 = triggerArm = this.activeArm != null ? this.activeArm : PunchyAnimationManager.getSourceArm();
        if (triggerArm == null) {
            return;
        }
        ToolTuning.Profile profile = this.resolveCompatProfileForArm(mc.field_1724, triggerArm);
        if (profile != null && Boolean.TRUE.equals(profile.keepTriggerArmTuning)) {
            this.latchedKeepTriggerArmTuning = true;
            this.latchedTriggerArm = triggerArm;
        }
    }

    private ToolTuning.Profile resolveCompatProfileForArm(class_746 player, class_1306 arm) {
        boolean mainRight;
        if (player == null || arm == null) {
            return null;
        }
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        class_1799 baseStack = arm == class_1306.field_6183 ? (mainRight ? player.method_6047() : player.method_6079()) : (mainRight ? player.method_6079() : player.method_6047());
        class_1799 stack = PunchyAnimationManager.resolveRenderStack((class_1657)player, arm, baseStack);
        if (stack == null || stack.method_7960()) {
            return null;
        }
        boolean left = arm == class_1306.field_6182;
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        return ToolTuning.resolveProfile(stack, kind, left);
    }

    private float getArmBlendRemaining(class_1306 arm) {
        return arm == class_1306.field_6182 ? this.leftArmBlendRemaining : this.rightArmBlendRemaining;
    }

    private float getArmBlendPrevRemaining(class_1306 arm) {
        return arm == class_1306.field_6182 ? this.leftArmBlendPrevRemaining : this.rightArmBlendPrevRemaining;
    }

    private void setArmBlendRemaining(class_1306 arm, float value) {
        if (arm == class_1306.field_6182) {
            this.leftArmBlendRemaining = value;
        } else {
            this.rightArmBlendRemaining = value;
        }
    }

    private void setArmBlendPrevRemaining(class_1306 arm, float value) {
        if (arm == class_1306.field_6182) {
            this.leftArmBlendPrevRemaining = value;
        } else {
            this.rightArmBlendPrevRemaining = value;
        }
    }

    private boolean isBoneForArm(String boneName, class_1306 arm) {
        if (boneName == null) {
            return true;
        }
        boolean isLeft = this.isLeftBone(boneName);
        boolean isRight = this.isRightBone(boneName);
        if (!isLeft && !isRight) {
            return true;
        }
        return arm == class_1306.field_6182 ? isLeft : isRight;
    }

    private class_1306 resolveBoneArm(String boneName) {
        if (boneName == null) {
            return null;
        }
        boolean isLeft = this.isLeftBone(boneName);
        boolean isRight = this.isRightBone(boneName);
        if (isLeft && !isRight) {
            return class_1306.field_6182;
        }
        if (isRight && !isLeft) {
            return class_1306.field_6183;
        }
        return null;
    }

    private boolean isLeftBone(String name) {
        return name.startsWith("left_") || name.endsWith("_left");
    }

    private boolean isRightBone(String name) {
        return name.startsWith("right_") || name.endsWith("_right");
    }

    private String oppositeBoneName(String name) {
        if (name.startsWith("left_")) {
            return "right_" + name.substring("left_".length());
        }
        if (name.startsWith("right_")) {
            return "left_" + name.substring("right_".length());
        }
        if (name.endsWith("_left")) {
            return name.substring(0, name.length() - "_left".length()) + "_right";
        }
        if (name.endsWith("_right")) {
            return name.substring(0, name.length() - "_right".length()) + "_left";
        }
        return name;
    }

    private boolean isDualHandedActive(float time) {
        return this.isDualHandedActive(this.currentClip, time);
    }

    private boolean isDualHandedActive(AnimationClip clip, float time) {
        if (this.ignoreDualHanded || clip == null) {
            return false;
        }
        if (!PunchyAnimationManager.isDualHandedActive(clip, time)) {
            return false;
        }
        return this.isConditionalOffhandWindowActive(clip, time);
    }

    private boolean isConditionalOffhandWindowActive(AnimationClip clip, float time) {
        if (clip == null) {
            return false;
        }
        if (this.isRealOffhandEmpty()) {
            float lastStart = this.resolveLastOffhandNoItemStartTime(clip, time);
            float lastEnd = this.resolveLastOffhandNoItemEndTime(clip, time);
            return this.isConditionalWindowActive(lastStart, lastEnd);
        }
        float lastStart = this.resolveLastOffhandStartTime(clip, time);
        float lastStop = clip.getLastEffectTime(STOP_WITH_OFFHAND_EFFECT, time);
        return this.isConditionalWindowActive(lastStart, lastStop);
    }

    private boolean isConditionalWindowActive(float lastStart, float lastEnd) {
        boolean hasEnd;
        boolean hasStart = !Float.isNaN(lastStart);
        boolean bl = hasEnd = !Float.isNaN(lastEnd);
        if (!hasStart && !hasEnd) {
            return true;
        }
        if (!hasStart) {
            return !hasEnd;
        }
        return !hasEnd || lastStart > lastEnd;
    }

    private float resolveLastOffhandStartTime(AnimationClip clip, float time) {
        if (clip == null) {
            return Float.NaN;
        }
        float canonical = clip.getLastEffectTime(START_WITH_OFFHAND_EFFECT, time);
        float alias = clip.getLastEffectTime(START_WITH_OFFHAND_EFFECT_ALIAS, time);
        if (Float.isNaN(canonical)) {
            return alias;
        }
        if (Float.isNaN(alias)) {
            return canonical;
        }
        return Math.max(canonical, alias);
    }

    private float resolveLastOffhandNoItemStartTime(AnimationClip clip, float time) {
        if (clip == null) {
            return Float.NaN;
        }
        float canonical = clip.getLastEffectTime(START_WITH_NO_OFFHAND_EFFECT, time);
        float alias = clip.getLastEffectTime(START_WITH_NO_OFFHAND_EFFECT_ALIAS, time);
        if (Float.isNaN(canonical)) {
            return alias;
        }
        if (Float.isNaN(alias)) {
            return canonical;
        }
        return Math.max(canonical, alias);
    }

    private float resolveLastOffhandNoItemEndTime(AnimationClip clip, float time) {
        if (clip == null) {
            return Float.NaN;
        }
        float canonical = clip.getLastEffectTime(END_WITH_NO_OFFHAND_EFFECT, time);
        float alias = clip.getLastEffectTime(END_WITH_NO_OFFHAND_EFFECT_ALIAS, time);
        if (Float.isNaN(canonical)) {
            return alias;
        }
        if (Float.isNaN(alias)) {
            return canonical;
        }
        return Math.max(canonical, alias);
    }

    private float resolveFirstConditionalOffhandStartTime(AnimationClip clip, boolean offhandEmpty) {
        if (clip == null) {
            return Float.NaN;
        }
        float first = Float.NaN;
        for (Map.Entry entry : clip.getTimelineEffects().entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            for (String effect : (Set)entry.getValue()) {
                boolean matchesWithoutOffhand;
                if (effect == null) continue;
                String normalized = effect.trim().toLowerCase(Locale.ROOT);
                boolean matchesWithOffhand = START_WITH_OFFHAND_EFFECT.equals(normalized) || START_WITH_OFFHAND_EFFECT_ALIAS.equals(normalized);
                boolean bl = matchesWithoutOffhand = START_WITH_NO_OFFHAND_EFFECT.equals(normalized) || START_WITH_NO_OFFHAND_EFFECT_ALIAS.equals(normalized);
                if ((offhandEmpty || !matchesWithOffhand) && (!offhandEmpty || !matchesWithoutOffhand)) continue;
                return ((Float)entry.getKey()).floatValue();
            }
        }
        return first;
    }

    private float resolveConditionalDualStartTime(AnimationClip clip) {
        if (clip == null) {
            return 0.0f;
        }
        if (!PunchyAnimationManager.hasAnyDualHandedEffect(clip)) {
            return 0.0f;
        }
        boolean offhandEmpty = this.isRealOffhandEmpty();
        float start = this.resolveFirstConditionalOffhandStartTime(clip, offhandEmpty);
        if (Float.isNaN(start) || start <= 0.0f) {
            return 0.0f;
        }
        float length = clip.getLength();
        if (length <= 0.0f) {
            return 0.0f;
        }
        return clip.isLoop() ? start % length : Math.min(start, length);
    }

    public static enum BlendInEasing {
        BACK,
        SMOOTH,
        STRONG_OUT_BACK;

    }

    private static class Sample {
        final Vector3f pos;
        final Quaternionf rot;
        final Vector3f scl;
        final Vector3f rotEulerV2;

        private Sample(Vector3f pos, Quaternionf rot, Vector3f scl, Vector3f rotEulerV2) {
            this.pos = pos;
            this.rot = rot;
            this.scl = scl;
            this.rotEulerV2 = rotEulerV2 != null ? new Vector3f((Vector3fc)rotEulerV2) : null;
        }

        private static Sample identity() {
            return new Sample(new Vector3f(), new Quaternionf(), new Vector3f(1.0f, 1.0f, 1.0f), null);
        }
    }

    private static class PartPoseSnapshot {
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final float xScale;
        final float yScale;
        final float zScale;

        private PartPoseSnapshot(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.xScale = xScale;
            this.yScale = yScale;
            this.zScale = zScale;
        }

        private static PartPoseSnapshot of(class_630 part) {
            return new PartPoseSnapshot(part.field_3657, part.field_3656, part.field_3655, part.field_3654, part.field_3675, part.field_3674, part.field_37938, part.field_37939, part.field_37940);
        }
    }
}

