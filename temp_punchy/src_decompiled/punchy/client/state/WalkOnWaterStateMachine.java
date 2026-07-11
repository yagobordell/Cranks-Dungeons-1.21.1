/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 *  net.minecraft.class_3610
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 */
package punchy.client.state;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.config.PunchyTuningConfig;

public class WalkOnWaterStateMachine {
    private static final boolean LOG_WALK_ON_WATER_DEBUG = true;
    private static final int IN_WATER_TRIGGER_GAP_TICKS = 20;
    private static final float REBASE_RETURN_SPEED_DEFAULT = 1.7f;
    private static final float REBASE_RETURN_SPEED_END = 4.5f;
    private static final float REBASE_RETURN_SPEED_RESUME = 1.15f;
    private static final String START_WITH_OFFHAND_EFFECT = "start_here_if_off_hand_item";
    private static final String START_WITH_OFFHAND_EFFECT_ALIAS = "start_if_offhand_item";
    private static final String END_STOP_WITH_OFFHAND_EFFECT = "stop_if_offhand_item";
    private Phase phase = Phase.NONE;
    private PunchyTuningConfig.CustomAnimationEntry activeEntry;
    private AnimationClip startClip;
    private AnimationClip loopClip;
    private AnimationClip endClip;
    private AnimationClip activeClip;
    private class_1799 lastStack = class_1799.field_8037;
    private Boolean lastTriggerActive;
    private String lastTriggerReason;
    private String lastResolvedSummary;
    private Phase lastLoggedPhase = Phase.NONE;
    private boolean suspendedByForeignClip;
    private Phase suspendedPhase = Phase.NONE;
    private float suspendedTime;
    private boolean pauseLoopAfterResumeBlend;
    private int inWaterTriggerGapTicks;
    private int unsupportedWaterGraceTicks;
    private AnimationClip handoffClip;

    public void tick(class_310 client) {
        boolean needsRestart;
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            this.stopAndReset(false);
            return;
        }
        if (!client.field_1690.method_31044().method_31034()) {
            this.stopAndReset(false);
            return;
        }
        class_1799 stack = client.field_1724.method_6047();
        if (this.isWalkOnWaterSuppressedByFishingRod(client)) {
            this.forceDisableReset();
            return;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "walk_on_water_v2");
        WalkOnWaterSelection selection = entry != null ? this.resolveSelection(client, stack, entry) : WalkOnWaterSelection.INVALID;
        TriggerState triggerState = this.applyUnsupportedWaterGrace(client, this.applyInWaterTriggerGap(client, this.evaluateTrigger(client)));
        boolean triggerActive = triggerState.active();
        if (triggerActive) {
            ItemAnimationManager.setPersistentType(class_1268.field_5808, "walk_on_water_v2", true, true);
        } else {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "walk_on_water_v2");
        }
        this.logTriggerState(client, entry, selection, triggerState);
        this.captureOwnedPhaseTime();
        if (this.shouldYieldToForeignClip()) {
            this.suspendForForeignClip();
            return;
        }
        if (this.suspendedByForeignClip) {
            if (HandEquipStateMachine.isEquipInProgress()) {
                this.keepOppositeArmWalkingOnWater();
                return;
            }
            if (!selection.isValid()) {
                this.stopAndReset(false);
                return;
            }
            if (this.activeEntry != entry || !this.matchesActiveSelection(selection)) {
                this.activeEntry = entry;
                this.startClip = selection.start();
                this.loopClip = selection.loop();
                this.endClip = selection.end();
                this.logResolvedSelection(entry, selection);
            }
            this.resumeFromSuspendedState(triggerActive);
            return;
        }
        if (!selection.isValid()) {
            if (this.phase != Phase.NONE) {
                this.stopAndReset(true);
            }
            return;
        }
        boolean bl = needsRestart = this.phase == Phase.NONE || this.activeEntry != entry || !this.matchesActiveSelection(selection);
        if (needsRestart) {
            this.activeEntry = entry;
            this.startClip = selection.start();
            this.loopClip = selection.loop();
            this.endClip = selection.end();
            this.lastStack = this.copyStack(stack);
            this.logResolvedSelection(entry, selection);
            if (triggerActive) {
                this.startStartPhase();
            } else {
                this.phase = Phase.NONE;
                this.activeClip = null;
            }
            return;
        }
        this.lastStack = this.copyStack(stack);
        boolean pauseForGapWithoutSupport = this.shouldPauseForInWaterGapWithoutSupport(client, triggerState);
        switch (this.phase.ordinal()) {
            case 0: {
                if (!triggerActive) break;
                this.startStartPhase();
                break;
            }
            case 1: {
                if (triggerActive) {
                    if (pauseForGapWithoutSupport) {
                        PunchyAnimationManager.POSE_HANDLER.setPaused(true);
                        break;
                    }
                    if (!this.isPlaying(this.startClip)) {
                        this.startLoopPhase();
                        break;
                    }
                    PunchyAnimationManager.POSE_HANDLER.setPaused(false);
                    break;
                }
                if (this.shouldHandOffToSwimAbove(client)) {
                    this.stopAndReset(false);
                    break;
                }
                this.startEndPhase();
                break;
            }
            case 2: {
                if (!triggerActive) {
                    if (this.shouldHandOffToSwimAbove(client)) {
                        this.stopAndReset(false);
                        break;
                    }
                    this.startEndPhase();
                    break;
                }
                if (!this.isPlaying(this.loopClip)) {
                    this.startLoopPhase();
                    break;
                }
                PunchyAnimationManager.POSE_HANDLER.setLooping(true);
                boolean movementActive = this.isMovementKeyActive(client);
                PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
                if (pauseForGapWithoutSupport) {
                    handler.setPaused(true);
                    handler.setSecondaryPaused(true);
                    break;
                }
                class_1306 offArm = this.resolveArm(client.field_1724, class_1268.field_5810);
                if (handler.getSecondaryClip() == this.loopClip && offArm != null && handler.getSecondaryArm() == offArm) {
                    handler.setSecondaryPaused(!movementActive);
                }
                if (this.pauseLoopAfterResumeBlend) {
                    if (movementActive) {
                        this.pauseLoopAfterResumeBlend = false;
                        handler.setPaused(false);
                        break;
                    }
                    if (!PunchyAnimationManager.POSE_HANDLER.isRebaseReturnToOriginActive()) {
                        this.pauseLoopAfterResumeBlend = false;
                        handler.setPaused(true);
                        break;
                    }
                    handler.setPaused(false);
                    break;
                }
                handler.setPaused(!movementActive);
                break;
            }
            case 3: {
                if (triggerActive) {
                    this.startStartPhase();
                    break;
                }
                if (this.shouldStopEndClipEarly(client)) {
                    this.stopAndReset(false);
                    break;
                }
                if (this.isCurrentOrBlending(this.endClip)) break;
                this.resetState();
            }
        }
    }

    private void startStartPhase() {
        float resolvedStartTime;
        if (this.startClip == null) {
            return;
        }
        class_310 client = class_310.method_1551();
        HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client != null ? client.field_1724 : null);
        PunchyAnimationManager.setSourceHand(client, class_1268.field_5808);
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        handler.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        handler.setIgnoreDualHanded(this.isAnyWalkOnWaterArmBlocked(client));
        handler.setActiveArm(this.resolveActiveArmForClip(this.startClip));
        handler.setLooping(false);
        handler.setFrozen(false);
        handler.setPaused(false);
        handler.setBlendInDurationNext(0.0f);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(this.startClip);
        Float startTime = this.resolveStartClipTime(client);
        float f = resolvedStartTime = startTime != null ? startTime.floatValue() : 0.0f;
        if (this.hasPoseToRebase(handler)) {
            handler.setRebaseReturnSpeedNext(1.7f);
            handler.setBlendInDurationNext(0.0f);
            handler.playAtTimeRebasedFromCurrentReturnToOrigin(this.startClip, speedMultiplier, false, resolvedStartTime);
        } else if (resolvedStartTime > 0.0f) {
            handler.playAtTime(this.startClip, speedMultiplier, true, false, resolvedStartTime);
        } else {
            handler.play(this.startClip, speedMultiplier, true, false);
        }
        this.phase = Phase.START;
        this.activeClip = this.startClip;
        this.logPhaseTransition("start", this.startClip, startTime);
    }

    private void startLoopPhase() {
        if (this.loopClip == null) {
            return;
        }
        class_310 client = class_310.method_1551();
        HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client != null ? client.field_1724 : null);
        PunchyAnimationManager.setSourceHand(client, class_1268.field_5808);
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        handler.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        handler.setIgnoreDualHanded(this.isAnyWalkOnWaterArmBlocked(client));
        handler.setActiveArm(this.resolveActiveArmForClip(this.loopClip));
        handler.setLooping(true);
        handler.setFrozen(false);
        handler.setPaused(!this.isMovementKeyActive(client));
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(this.loopClip);
        if (this.hasPoseToRebase(handler)) {
            handler.setRebaseReturnSpeedNext(1.7f);
            handler.setBlendInDurationNext(0.0f);
            handler.playAtTimeRebasedFromCurrentReturnToOrigin(this.loopClip, speedMultiplier, false, 0.0f);
        } else {
            handler.play(this.loopClip, speedMultiplier, true, false);
        }
        this.phase = Phase.LOOP;
        this.activeClip = this.loopClip;
        this.logPhaseTransition("loop", this.loopClip);
    }

    private void startEndPhase() {
        if (this.endClip == null) {
            this.resetState();
            return;
        }
        PunchyAnimationManager.setSourceHand(class_310.method_1551(), class_1268.field_5808);
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        AnimationClip secondaryBeforeEnd = handler.getSecondaryClip();
        class_1306 secondaryArmBeforeEnd = handler.getSecondaryArm();
        handler.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        handler.setIgnoreDualHanded(this.isAnyWalkOnWaterArmBlocked(class_310.method_1551()));
        handler.setActiveArm(this.resolveActiveArmForClip(this.endClip));
        handler.setLooping(false);
        handler.setFrozen(false);
        handler.setPaused(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(this.endClip);
        if (this.hasPoseToRebase(handler)) {
            handler.setRebaseReturnSpeedNext(4.5f);
            handler.setBlendInDurationNext(0.0f);
            handler.playAtTimeRebasedFromCurrentReturnToOrigin(this.endClip, speedMultiplier, false, 0.0f);
        } else {
            handler.play(this.endClip, speedMultiplier, true, false);
        }
        if (this.isWalkOnWaterPhaseClip(secondaryBeforeEnd) && secondaryArmBeforeEnd != null) {
            if (!this.isArmBlockedByWalkOnWaterFlag(class_310.method_1551(), secondaryArmBeforeEnd)) {
                handler.setSecondaryClipAtTime(this.endClip, secondaryArmBeforeEnd, speedMultiplier, 0.0f);
                handler.setSecondaryPaused(false);
            } else {
                handler.clearSecondaryClip();
            }
        }
        this.phase = Phase.END;
        this.activeClip = this.endClip;
        this.logPhaseTransition("end", this.endClip);
    }

    private void stopAndReset(boolean immediate) {
        if (this.phase == Phase.NONE) {
            this.resetState();
            return;
        }
        this.handoffClip = this.activeClip;
        if (this.ownsCurrentClip()) {
            PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            if (immediate) {
                PunchyAnimationManager.POSE_HANDLER.stopWithoutClear();
            } else {
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            }
        }
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "walk_on_water_v2");
        PunchyAnimationManager.clearSourceHand();
        this.resetState();
    }

    private void resetState() {
        PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        this.phase = Phase.NONE;
        this.activeEntry = null;
        this.startClip = null;
        this.loopClip = null;
        this.endClip = null;
        this.activeClip = null;
        this.lastStack = class_1799.field_8037;
        this.lastLoggedPhase = Phase.NONE;
        this.suspendedByForeignClip = false;
        this.suspendedPhase = Phase.NONE;
        this.suspendedTime = 0.0f;
        this.pauseLoopAfterResumeBlend = false;
        this.inWaterTriggerGapTicks = 0;
        this.unsupportedWaterGraceTicks = 0;
        this.handoffClip = null;
    }

    private void forceDisableReset() {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler != null) {
            handler.clearSecondaryClip();
            handler.setLooping(false);
            if (this.ownsCurrentClip()) {
                handler.stopWithoutClear();
            }
        }
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "walk_on_water_v2");
        PunchyAnimationManager.clearSourceHand();
        this.handoffClip = null;
        this.resetState();
    }

    private boolean isWalkOnWaterSuppressedByFishingRod(class_310 client) {
        boolean suppressed;
        class_1306 arm = this.resolveWalkOnWaterActiveArm(client);
        boolean bl = suppressed = arm == null;
        if (client != null && client.field_1724 != null) {
            this.debugWalkOnWater("suppressCheck", "suppressed=" + suppressed + " main=" + this.stackId(client.field_1724.method_6047()) + " off=" + this.stackId(client.field_1724.method_6079()) + " mainDisabled=" + this.hasFishingRodWalkDisable(client.field_1724.method_6047()) + " offDisabled=" + this.hasFishingRodWalkDisable(client.field_1724.method_6079()) + " resolvedArm=" + String.valueOf(arm));
        }
        return suppressed;
    }

    private boolean hasFishingRodWalkDisable(class_1799 stack) {
        return PunchyTuningConfig.isWalkOnWaterDisabledForStack(stack);
    }

    private WalkOnWaterSelection resolveSelection(class_310 client, class_1799 stack, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (client == null || entry == null) {
            return WalkOnWaterSelection.INVALID;
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForEntry(stack, entry);
        AnimationClip start = this.resolveNamedClip(CustomAnimationResolver.resolveStartName(entry), client, preferredOption);
        AnimationClip loop = this.resolveNamedClip(CustomAnimationResolver.resolveLoopingName(entry), client, preferredOption);
        AnimationClip end = this.resolveNamedClip(CustomAnimationResolver.resolveEndName(entry), client, preferredOption);
        if (start == null || loop == null || end == null) {
            return WalkOnWaterSelection.INVALID;
        }
        return new WalkOnWaterSelection(start, loop, end);
    }

    private AnimationClip resolveNamedClip(String name, class_310 client, String preferredOption) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return PunchyAnimationManager.resolveNamedClip(name, client, class_1268.field_5808, preferredOption);
    }

    private TriggerState evaluateTrigger(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return new TriggerState(false, "client_or_player_missing");
        }
        if (client.field_1724.method_7325() || !client.field_1724.method_5805()) {
            return new TriggerState(false, "spectator_or_dead");
        }
        if (client.field_1724.method_5681()) {
            return new TriggerState(false, "swimming_or_submerged");
        }
        if (client.field_1724.method_31549().field_7479 || client.field_1724.method_6128() || client.field_1724.method_5854() != null) {
            return new TriggerState(false, "flying_or_vehicle");
        }
        if (this.shouldTriggerOnSolidUnderwaterFloor(client)) {
            return new TriggerState(true, "supported_underwater_floor");
        }
        if (client.field_1724.method_5869() || client.field_1724.method_5777(class_3486.field_15517)) {
            return new TriggerState(false, "swimming_or_submerged");
        }
        class_638 level = client.field_1687;
        class_238 bounds = client.field_1724.method_5829();
        double[] sampleYs = new double[]{bounds.field_1322 + 0.01, bounds.field_1322 + 0.08, bounds.field_1322 + 0.16};
        double insetX = Math.min(0.2, bounds.method_17939() * 0.25);
        double insetZ = Math.min(0.2, bounds.method_17941() * 0.25);
        double[] sampleXs = new double[]{bounds.field_1323 + insetX, bounds.field_1320 - insetX, (bounds.field_1323 + bounds.field_1320) * 0.5};
        double[] sampleZs = new double[]{bounds.field_1321 + insetZ, bounds.field_1324 - insetZ, (bounds.field_1321 + bounds.field_1324) * 0.5};
        for (double sampleY : sampleYs) {
            for (double sampleX : sampleXs) {
                for (double sampleZ : sampleZs) {
                    class_2338 waterPos = class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ);
                    class_3610 fluidState = level.method_8316(waterPos);
                    if (!fluidState.method_15767(class_3486.field_15517) || level.method_8316(waterPos.method_10084()).method_15767(class_3486.field_15517) || level.method_8316(waterPos.method_10074()).method_15767(class_3486.field_15517) || !level.method_8320(waterPos.method_10074()).method_51366()) continue;
                    String reason = "water@" + waterPos.method_10263() + "," + waterPos.method_10264() + "," + waterPos.method_10260() + " below=" + waterPos.method_10074().method_10263() + "," + waterPos.method_10074().method_10264() + "," + waterPos.method_10074().method_10260();
                    return new TriggerState(true, reason);
                }
            }
        }
        return new TriggerState(false, "no_shallow_water_sample");
    }

    private boolean shouldTriggerOnSolidUnderwaterFloor(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        if (!client.field_1724.method_5869() && !client.field_1724.method_5777(class_3486.field_15517)) {
            return false;
        }
        if (!this.hasSolidSupportAtFeet(client)) {
            return false;
        }
        return this.hasWaterContactAnyDepth(client);
    }

    private TriggerState applyInWaterTriggerGap(class_310 client, TriggerState triggerState) {
        if (triggerState == null) {
            this.inWaterTriggerGapTicks = 0;
            return new TriggerState(false, "null_trigger_state");
        }
        if (triggerState.active()) {
            this.inWaterTriggerGapTicks = 0;
            return triggerState;
        }
        if (!"no_shallow_water_sample".equals(triggerState.reason())) {
            if (this.shouldHoldTriggerAfterLosingSupport(client, triggerState) && this.inWaterTriggerGapTicks < 20) {
                ++this.inWaterTriggerGapTicks;
                return new TriggerState(true, "gap_in_water_" + this.inWaterTriggerGapTicks);
            }
            this.inWaterTriggerGapTicks = 0;
            return triggerState;
        }
        if (this.phase == Phase.NONE && !this.suspendedByForeignClip) {
            this.inWaterTriggerGapTicks = 0;
            return triggerState;
        }
        if (!this.hasWaterContact(client)) {
            this.inWaterTriggerGapTicks = 0;
            return triggerState;
        }
        if (this.inWaterTriggerGapTicks < 20) {
            ++this.inWaterTriggerGapTicks;
            return new TriggerState(true, "gap_in_water_" + this.inWaterTriggerGapTicks);
        }
        return triggerState;
    }

    private TriggerState applyUnsupportedWaterGrace(class_310 client, TriggerState triggerState) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            this.unsupportedWaterGraceTicks = 0;
            return triggerState == null ? new TriggerState(false, "null_trigger_state") : triggerState;
        }
        if (triggerState != null && triggerState.active()) {
            this.unsupportedWaterGraceTicks = 0;
            return triggerState;
        }
        if (this.phase == Phase.NONE && !this.suspendedByForeignClip) {
            this.unsupportedWaterGraceTicks = 0;
            return triggerState;
        }
        if (client.field_1724.method_5681() || client.field_1724.method_5869() || client.field_1724.method_5777(class_3486.field_15517)) {
            this.unsupportedWaterGraceTicks = 0;
            return triggerState;
        }
        if (this.hasSolidSupportAtFeet(client) || !this.hasWaterContactAnyDepth(client)) {
            this.unsupportedWaterGraceTicks = 0;
            return triggerState;
        }
        if (this.unsupportedWaterGraceTicks < 20) {
            ++this.unsupportedWaterGraceTicks;
            return new TriggerState(true, "unsupported_water_grace_" + this.unsupportedWaterGraceTicks);
        }
        return triggerState;
    }

    private boolean shouldHoldTriggerAfterLosingSupport(class_310 client, TriggerState triggerState) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        if (this.phase == Phase.NONE && !this.suspendedByForeignClip) {
            return false;
        }
        if (triggerState == null || triggerState.active()) {
            return false;
        }
        if (client.field_1724.method_5681() || client.field_1724.method_5869() || client.field_1724.method_5777(class_3486.field_15517)) {
            return false;
        }
        if (this.hasSolidSupportAtFeet(client)) {
            return false;
        }
        return this.hasWaterContactAnyDepth(client);
    }

    private boolean shouldPauseForInWaterGapWithoutSupport(class_310 client, TriggerState triggerState) {
        return triggerState != null && triggerState.active() && triggerState.reason() != null && triggerState.reason().startsWith("gap_in_water_") && !this.hasSolidSupportAtFeet(client);
    }

    private boolean hasWaterContact(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        if (client.field_1724.method_5681() || client.field_1724.method_5869() || client.field_1724.method_5777(class_3486.field_15517)) {
            return false;
        }
        return this.hasWaterContactAnyDepth(client);
    }

    private boolean hasWaterContactAnyDepth(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        class_638 level = client.field_1687;
        class_238 bounds = client.field_1724.method_5829();
        double[] sampleYs = new double[]{bounds.field_1322 + 0.01, bounds.field_1322 + 0.08, bounds.field_1322 + 0.16, Math.min(bounds.field_1325 - 0.05, bounds.field_1322 + 0.45)};
        double insetX = Math.min(0.2, bounds.method_17939() * 0.25);
        double insetZ = Math.min(0.2, bounds.method_17941() * 0.25);
        double[] sampleXs = new double[]{bounds.field_1323 + insetX, bounds.field_1320 - insetX, (bounds.field_1323 + bounds.field_1320) * 0.5};
        double[] sampleZs = new double[]{bounds.field_1321 + insetZ, bounds.field_1324 - insetZ, (bounds.field_1321 + bounds.field_1324) * 0.5};
        for (double sampleY : sampleYs) {
            for (double sampleX : sampleXs) {
                for (double sampleZ : sampleZs) {
                    if (!level.method_8316(class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ)).method_15767(class_3486.field_15517)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldHandOffToSwimAbove(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        if (this.phase == Phase.NONE) {
            return false;
        }
        if (client.field_1724.method_5681() || client.field_1724.method_5869() || client.field_1724.method_5777(class_3486.field_15517)) {
            return false;
        }
        if (this.hasSolidSupportAtFeet(client)) {
            return false;
        }
        return this.hasWaterContactAnyDepth(client);
    }

    private boolean hasSolidSupportAtFeet(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1687 == null) {
            return false;
        }
        class_638 level = client.field_1687;
        class_238 bounds = client.field_1724.method_5829();
        double sampleY = bounds.field_1322 - 0.05;
        double insetX = Math.min(0.2, bounds.method_17939() * 0.25);
        double insetZ = Math.min(0.2, bounds.method_17941() * 0.25);
        double[] sampleXs = new double[]{bounds.field_1323 + insetX, bounds.field_1320 - insetX, (bounds.field_1323 + bounds.field_1320) * 0.5};
        double[] sampleZs = new double[]{bounds.field_1321 + insetZ, bounds.field_1324 - insetZ, (bounds.field_1321 + bounds.field_1324) * 0.5};
        for (double sampleX : sampleXs) {
            for (double sampleZ : sampleZs) {
                class_2338 supportPos = class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ);
                if (!level.method_8320(supportPos).method_51366()) continue;
                return true;
            }
        }
        return false;
    }

    private boolean ownsCurrentClip() {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        return current != null && (current == this.activeClip || current == this.startClip || current == this.loopClip || current == this.endClip);
    }

    private boolean isPlaying(AnimationClip clip) {
        return clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() == clip;
    }

    private boolean isCurrentOrBlending(AnimationClip clip) {
        return clip != null && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() == clip;
    }

    private boolean hasPoseToRebase(PoseHandler handler) {
        return handler != null && handler.getCurrentClip() != null && (handler.isPlaying() || handler.isBlendOutActive());
    }

    private void captureOwnedPhaseTime() {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null || this.activeClip == null) {
            return;
        }
        if (handler.getCurrentClip() != this.activeClip) {
            return;
        }
        if (!handler.isPlaying() && !handler.isBlendOutActive()) {
            return;
        }
        this.suspendedPhase = this.phase;
        this.suspendedTime = handler.getCurrentTime();
    }

    private boolean shouldYieldToForeignClip() {
        if (this.phase == Phase.NONE) {
            return false;
        }
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null) {
            return false;
        }
        AnimationClip current = handler.getCurrentClip();
        if (current == null) {
            return false;
        }
        if (current == this.startClip || current == this.loopClip || current == this.endClip) {
            return false;
        }
        return handler.isPlaying();
    }

    private void suspendForForeignClip() {
        AnimationClip foreign;
        PoseHandler handler;
        if (!this.suspendedByForeignClip) {
            this.suspendedByForeignClip = true;
            this.suspendedPhase = this.phase;
        }
        if ((handler = PunchyAnimationManager.POSE_HANDLER) != null && handler.getSecondaryClip() == this.activeClip && PunchyAnimationManager.hasAnyDualHandedEffect(foreign = handler.getCurrentClip())) {
            handler.clearSecondaryClip();
            this.debugWalkOnWater("clearSecondaryForDualForeign", "foreign=" + WalkOnWaterStateMachine.clipName(foreign) + " active=" + WalkOnWaterStateMachine.clipName(this.activeClip));
            return;
        }
        if (!HandEquipStateMachine.isOffhandEquipTransitionActive()) {
            this.keepOppositeArmWalkingOnWater();
        }
    }

    private void resumeFromSuspendedState(boolean triggerActive) {
        boolean resumeAllowed;
        if (this.shouldYieldToForeignClip()) {
            return;
        }
        this.syncSuspendedTimeFromBackgroundClip();
        Phase resumePhase = this.resolveResumePhaseFromBackgroundClip();
        this.debugWalkOnWater("resume", "trigger=" + triggerActive + " suspendedPhase=" + String.valueOf((Object)this.suspendedPhase) + " resumePhase=" + String.valueOf((Object)resumePhase) + " suspendedTime=" + this.suspendedTime + " active=" + WalkOnWaterStateMachine.clipName(this.activeClip) + " secondary=" + WalkOnWaterStateMachine.clipName(PunchyAnimationManager.POSE_HANDLER.getSecondaryClip()));
        boolean bl = resumeAllowed = triggerActive && (resumePhase == Phase.START || resumePhase == Phase.LOOP);
        if (resumeAllowed) {
            this.resumePhaseClip(resumePhase, this.suspendedTime);
        } else if (!triggerActive && this.phase != Phase.END) {
            this.startEndPhase();
        }
        this.suspendedByForeignClip = false;
        this.suspendedPhase = Phase.NONE;
        this.suspendedTime = 0.0f;
        if (!resumeAllowed) {
            this.pauseLoopAfterResumeBlend = false;
        }
    }

    public void reattachMainhandAfterEquip(class_746 player) {
        Phase resumePhase;
        if (player == null || !this.suspendedByForeignClip) {
            return;
        }
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null) {
            return;
        }
        boolean hasOffhandBackground = false;
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        if (offArm != null && handler.getSecondaryArm() == offArm && handler.getSecondaryClip() != null) {
            hasOffhandBackground = true;
            this.syncSuspendedTimeFromBackgroundClip();
        }
        Phase phase = resumePhase = hasOffhandBackground ? this.resolveResumePhaseFromBackgroundClip() : this.suspendedPhase;
        if (!this.triggerActiveNow() || resumePhase != Phase.START && resumePhase != Phase.LOOP) {
            return;
        }
        if (!hasOffhandBackground && offArm != null) {
            this.resumePhaseClipWithExplicitOffhand(resumePhase, this.suspendedTime, offArm);
        } else {
            this.resumePhaseClip(resumePhase, this.suspendedTime);
        }
        this.suspendedByForeignClip = false;
        this.suspendedPhase = Phase.NONE;
        this.suspendedTime = 0.0f;
    }

    private void syncSuspendedTimeFromBackgroundClip() {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null || this.activeClip == null) {
            return;
        }
        if (handler.getSecondaryClip() != this.activeClip) {
            return;
        }
        this.suspendedTime = Math.max(0.0f, handler.getSecondaryTime());
    }

    private Phase resolveResumePhaseFromBackgroundClip() {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null) {
            return this.suspendedPhase;
        }
        AnimationClip secondary = handler.getSecondaryClip();
        if (secondary == null) {
            return this.suspendedPhase;
        }
        if (secondary == this.loopClip) {
            return Phase.LOOP;
        }
        if (secondary == this.startClip) {
            return Phase.START;
        }
        return this.suspendedPhase;
    }

    private void resumePhaseClip(Phase phaseToResume, float time) {
        AnimationClip clip;
        switch (phaseToResume.ordinal()) {
            case 1: {
                AnimationClip animationClip = this.startClip;
                break;
            }
            case 2: {
                AnimationClip animationClip = this.loopClip;
                break;
            }
            default: {
                AnimationClip animationClip = clip = null;
            }
        }
        if (clip == null) {
            if (this.triggerActiveNow()) {
                this.startStartPhase();
            } else {
                this.resetState();
            }
            return;
        }
        class_310 client = class_310.method_1551();
        HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client != null ? client.field_1724 : null);
        PunchyAnimationManager.setSourceHand(client, class_1268.field_5808);
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        boolean splitResumeFromOffhandBackground = this.shouldSplitResumeFromOffhandBackground(handler, clip, client);
        float preservedSecondaryTime = splitResumeFromOffhandBackground ? handler.getSecondaryTime() : 0.0f;
        class_1306 preservedSecondaryArm = splitResumeFromOffhandBackground ? handler.getSecondaryArm() : null;
        handler.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        handler.setIgnoreDualHanded(splitResumeFromOffhandBackground || this.isAnyWalkOnWaterArmBlocked(client));
        handler.setActiveArm(splitResumeFromOffhandBackground ? this.resolveActiveArm() : this.resolveActiveArmForClip(clip));
        handler.setLooping(phaseToResume == Phase.LOOP);
        handler.setFrozen(false);
        boolean pausedLoop = phaseToResume == Phase.LOOP && !this.isMovementKeyActive(client);
        handler.setPaused(false);
        handler.setBlendInDurationNext(0.0f);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        float startAt = Math.max(0.0f, time);
        handler.setRebaseReturnSpeedNext(1.15f);
        handler.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, false, startAt);
        if (splitResumeFromOffhandBackground && preservedSecondaryArm != null) {
            if (handler.getSecondaryClip() != clip || handler.getSecondaryArm() != preservedSecondaryArm) {
                handler.setSecondaryClipAtTime(clip, preservedSecondaryArm, speedMultiplier, preservedSecondaryTime);
            }
            handler.setSecondaryPaused(phaseToResume == Phase.LOOP && !this.isMovementKeyActive(client));
        }
        this.pauseLoopAfterResumeBlend = pausedLoop;
        this.phase = phaseToResume;
        this.activeClip = clip;
        this.logPhaseTransition(phaseToResume == Phase.START ? "resume_start" : "resume_loop", clip, Float.valueOf(startAt));
    }

    private void resumePhaseClipWithExplicitOffhand(Phase phaseToResume, float time, class_1306 offArm) {
        boolean pausedLoop;
        AnimationClip clip;
        switch (phaseToResume.ordinal()) {
            case 1: {
                AnimationClip animationClip = this.startClip;
                break;
            }
            case 2: {
                AnimationClip animationClip = this.loopClip;
                break;
            }
            default: {
                AnimationClip animationClip = clip = null;
            }
        }
        if (clip == null) {
            if (this.triggerActiveNow()) {
                this.startStartPhase();
            } else {
                this.resetState();
            }
            return;
        }
        class_310 client = class_310.method_1551();
        PunchyAnimationManager.setSourceHand(client, class_1268.field_5808);
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        handler.clearRequestedArmFilters();
        handler.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        boolean dualHanded = !this.isAnyWalkOnWaterArmBlocked(client) && PunchyAnimationManager.isDualHandedActive(client, clip, 0.0f);
        this.debugWalkOnWater("resumeExplicitOffhand.begin", "phaseToResume=" + String.valueOf((Object)phaseToResume) + " time=" + time + " offArm=" + String.valueOf(offArm) + " dualHanded=" + dualHanded + " current=" + WalkOnWaterStateMachine.clipName(handler.getCurrentClip()) + " secondary=" + WalkOnWaterStateMachine.clipName(handler.getSecondaryClip()) + " activeArm=" + String.valueOf(handler.getActiveArm()) + " secondaryArm=" + String.valueOf(handler.getSecondaryArm()));
        handler.setIgnoreDualHanded(false);
        handler.setActiveArm(dualHanded ? null : this.resolveActiveArm());
        handler.setLooping(phaseToResume == Phase.LOOP);
        handler.setFrozen(false);
        handler.setPaused(false);
        handler.setBlendInDurationNext(0.0f);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        float startAt = Math.max(0.0f, time);
        handler.setRebaseReturnSpeedNext(1.15f);
        handler.setRebaseUseBothArmsForNextPlay(dualHanded);
        handler.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, false, startAt);
        this.debugWalkOnWater("resumeExplicitOffhand.afterPlay", "current=" + WalkOnWaterStateMachine.clipName(handler.getCurrentClip()) + " currentTime=" + handler.getCurrentTime() + " activeArm=" + String.valueOf(handler.getActiveArm()) + " secondary=" + WalkOnWaterStateMachine.clipName(handler.getSecondaryClip()) + " secondaryArm=" + String.valueOf(handler.getSecondaryArm()));
        boolean bl = pausedLoop = phaseToResume == Phase.LOOP && !this.isMovementKeyActive(client);
        if (!dualHanded) {
            float syncedTime = Math.max(0.0f, handler.getCurrentTime());
            if (!this.isArmBlockedByWalkOnWaterFlag(client, offArm)) {
                handler.setSecondaryClipAtTimeWithBlendFromCurrent(clip, offArm, speedMultiplier, syncedTime);
                handler.requestBlendInArm(offArm);
                handler.setSecondaryPaused(pausedLoop);
            } else {
                handler.clearSecondaryClip();
            }
            this.debugWalkOnWater("resumeExplicitOffhand.secondary", "syncedTime=" + syncedTime + " secondary=" + WalkOnWaterStateMachine.clipName(handler.getSecondaryClip()) + " secondaryArm=" + String.valueOf(handler.getSecondaryArm()) + " secondaryTime=" + handler.getSecondaryTime() + " paused=" + pausedLoop);
        } else {
            handler.clearSecondaryClip();
        }
        HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client != null ? client.field_1724 : null);
        this.debugWalkOnWater("resumeExplicitOffhand.end", "current=" + WalkOnWaterStateMachine.clipName(handler.getCurrentClip()) + " currentTime=" + handler.getCurrentTime() + " activeArm=" + String.valueOf(handler.getActiveArm()) + " secondary=" + WalkOnWaterStateMachine.clipName(handler.getSecondaryClip()) + " secondaryArm=" + String.valueOf(handler.getSecondaryArm()) + " secondaryTime=" + handler.getSecondaryTime());
        this.pauseLoopAfterResumeBlend = pausedLoop;
        this.phase = phaseToResume;
        this.activeClip = clip;
        this.logPhaseTransition(phaseToResume == Phase.START ? "resume_start_offhand" : "resume_loop_offhand", clip, Float.valueOf(Math.max(0.0f, handler.getCurrentTime())));
    }

    private boolean shouldSplitResumeFromOffhandBackground(PoseHandler handler, AnimationClip clip, class_310 client) {
        if (handler == null || clip == null || client == null || client.field_1724 == null) {
            return false;
        }
        AnimationClip secondary = handler.getSecondaryClip();
        if (secondary != clip) {
            return false;
        }
        class_1306 offArm = this.resolveArm(client.field_1724, class_1268.field_5810);
        return offArm != null && handler.getSecondaryArm() == offArm;
    }

    private boolean triggerActiveNow() {
        class_310 client = class_310.method_1551();
        return this.evaluateTrigger(client).active();
    }

    public boolean isTriggerActiveOrInGap(class_310 client) {
        if (client == null) {
            return false;
        }
        return this.applyInWaterTriggerGap(client, this.evaluateTrigger(client)).active();
    }

    private void keepOppositeArmWalkingOnWater() {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null || this.activeClip == null || this.suspendedPhase == Phase.END) {
            return;
        }
        if (this.isOffhandEquipCurrentlyVisible(handler)) {
            return;
        }
        boolean movementActive = this.isMovementKeyActive(class_310.method_1551());
        class_1306 foreignArm = handler.getActiveArm();
        if (foreignArm == null) {
            return;
        }
        class_1306 overlapArm = foreignArm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
        class_1306 walkArm = this.resolveActiveArmForClip(this.activeClip);
        if (walkArm != null && overlapArm != walkArm) {
            return;
        }
        if (this.isArmBlockedByWalkOnWaterFlag(class_310.method_1551(), overlapArm)) {
            return;
        }
        AnimationClip secondary = handler.getSecondaryClip();
        if (secondary == this.activeClip && handler.getSecondaryArm() == overlapArm) {
            handler.setSecondaryPaused(!movementActive && this.activeClip == this.loopClip);
            return;
        }
        float clipLength = this.activeClip.getLength();
        float startAt = Math.max(0.0f, this.suspendedTime);
        if (clipLength > 0.0f) {
            startAt = this.activeClip.isLoop() ? startAt % clipLength : Math.min(startAt, clipLength);
        }
        handler.setSecondaryClipAtTime(this.activeClip, overlapArm, PunchyAnimationManager.clipSpeedMultiplier(this.activeClip), startAt);
        handler.setSecondaryPaused(!movementActive && this.activeClip == this.loopClip);
        this.debugWalkOnWater("keepOpposite", "active=" + WalkOnWaterStateMachine.clipName(this.activeClip) + " overlapArm=" + String.valueOf(overlapArm) + " startAt=" + startAt + " movement=" + movementActive);
    }

    private boolean isOffhandEquipCurrentlyVisible(PoseHandler handler) {
        class_310 client = class_310.method_1551();
        if (handler == null || client == null || client.field_1724 == null) {
            return false;
        }
        class_1306 offArm = this.resolveArm(client.field_1724, class_1268.field_5810);
        if (offArm == null) {
            return false;
        }
        AnimationClip current = handler.getCurrentClip();
        if (current != null && this.isEquipClip(current) && (handler.getActiveArm() == null || handler.getActiveArm() == offArm)) {
            return true;
        }
        AnimationClip secondary = handler.getSecondaryClip();
        return secondary != null && this.isEquipClip(secondary) && handler.getSecondaryArm() == offArm;
    }

    private boolean isEquipClip(AnimationClip clip) {
        String normalized;
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name != null && ((normalized = name.trim().toLowerCase(Locale.ROOT)).equals("hand_in") || normalized.equals("hand_out") || normalized.startsWith("hand_in_") || normalized.startsWith("hand_out_"))) {
            return true;
        }
        return PunchyAnimationManager.isClipNamed(clip, "hand_in") || PunchyAnimationManager.isClipNamed(clip, "hand_out");
    }

    private void debugWalkOnWater(String event, String details) {
    }

    private String stackId(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return "empty";
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        return id == null ? "unknown" : id.toString();
    }

    private boolean shouldStopEndClipEarly(class_310 client) {
        if (client == null || client.field_1724 == null || this.endClip == null) {
            return false;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying() || PunchyAnimationManager.POSE_HANDLER.getCurrentClip() != this.endClip) {
            return false;
        }
        if (client.field_1724.method_6079() == null || client.field_1724.method_6079().method_7960()) {
            return false;
        }
        Float stopTime = this.findTimelineEffectTime(this.endClip, END_STOP_WITH_OFFHAND_EFFECT);
        if (stopTime == null) {
            return false;
        }
        return PunchyAnimationManager.POSE_HANDLER.getCurrentTime() >= stopTime.floatValue();
    }

    private Float resolveStartClipTime(class_310 client) {
        if (client == null || client.field_1724 == null || this.startClip == null) {
            return null;
        }
        if (client.field_1724.method_6079() == null || client.field_1724.method_6079().method_7960()) {
            return null;
        }
        Float startTime = this.findTimelineEffectTime(this.startClip, START_WITH_OFFHAND_EFFECT);
        if (startTime != null) {
            return startTime;
        }
        return this.findTimelineEffectTime(this.startClip, START_WITH_OFFHAND_EFFECT_ALIAS);
    }

    private Float findTimelineEffectTime(AnimationClip clip, String effectName) {
        if (clip == null || effectName == null || effectName.isBlank()) {
            return null;
        }
        for (Map.Entry entry : clip.getTimelineEffects().entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            for (String effect : (Set)entry.getValue()) {
                if (effect == null || !effectName.equalsIgnoreCase(effect.trim())) continue;
                return (Float)entry.getKey();
            }
        }
        return null;
    }

    private boolean isMovementKeyActive(class_310 client) {
        if (client == null || client.field_1690 == null) {
            return false;
        }
        return client.field_1690.field_1894.method_1434() || client.field_1690.field_1881.method_1434() || client.field_1690.field_1913.method_1434() || client.field_1690.field_1849.method_1434();
    }

    private class_1306 resolveActiveArm() {
        return this.resolveWalkOnWaterActiveArm(class_310.method_1551());
    }

    private class_1306 resolveWalkOnWaterActiveArm(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return null;
        }
        class_1306 mainArm = this.resolveArm(client.field_1724, class_1268.field_5808);
        class_1306 offArm = this.resolveArm(client.field_1724, class_1268.field_5810);
        boolean mainBlocked = this.hasFishingRodWalkDisable(client.field_1724.method_6047());
        boolean offBlocked = this.hasFishingRodWalkDisable(client.field_1724.method_6079());
        if (mainBlocked && offBlocked) {
            this.debugWalkOnWater("resolveArm", "both_arms_blocked=true");
            return null;
        }
        if (mainBlocked) {
            return offArm;
        }
        if (offBlocked) {
            return mainArm;
        }
        class_1306 resolved = PunchyAnimationManager.getUseArm(client, class_1268.field_5808);
        this.debugWalkOnWater("resolveArm", "mainBlocked=" + mainBlocked + " offBlocked=" + offBlocked + " resolved=" + String.valueOf(resolved));
        return resolved;
    }

    private boolean isArmBlockedByWalkOnWaterFlag(class_310 client, class_1306 arm) {
        if (client == null || client.field_1724 == null || arm == null) {
            return false;
        }
        class_1306 mainArm = this.resolveArm(client.field_1724, class_1268.field_5808);
        class_1306 offArm = this.resolveArm(client.field_1724, class_1268.field_5810);
        if (arm == mainArm) {
            return this.hasFishingRodWalkDisable(client.field_1724.method_6047());
        }
        if (arm == offArm) {
            return this.hasFishingRodWalkDisable(client.field_1724.method_6079());
        }
        return false;
    }

    private class_1306 resolveArm(class_746 player, class_1268 hand) {
        boolean mainRight;
        if (player == null || hand == null) {
            return null;
        }
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        if (hand == class_1268.field_5808) {
            return mainRight ? class_1306.field_6183 : class_1306.field_6182;
        }
        return mainRight ? class_1306.field_6182 : class_1306.field_6183;
    }

    private class_1306 resolveActiveArmForClip(AnimationClip clip) {
        if (this.isAnyWalkOnWaterArmBlocked(class_310.method_1551())) {
            return this.resolveActiveArm();
        }
        if (PunchyAnimationManager.isDualHandedActive(class_310.method_1551(), clip, 0.0f)) {
            return null;
        }
        return this.resolveActiveArm();
    }

    private boolean isAnyWalkOnWaterArmBlocked(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        return this.hasFishingRodWalkDisable(client.field_1724.method_6047()) || this.hasFishingRodWalkDisable(client.field_1724.method_6079());
    }

    private boolean hasStackChanged(class_1799 previous, class_1799 current) {
        class_1799 prev = previous == null ? class_1799.field_8037 : previous;
        class_1799 now = current == null ? class_1799.field_8037 : current;
        return !class_1799.method_31577((class_1799)prev, (class_1799)now);
    }

    private class_1799 copyStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return class_1799.field_8037;
        }
        return stack.method_7972();
    }

    private boolean matchesActiveSelection(WalkOnWaterSelection selection) {
        if (selection == null || !selection.isValid()) {
            return false;
        }
        return selection.start() == this.startClip && selection.loop() == this.loopClip && selection.end() == this.endClip;
    }

    private boolean isWalkOnWaterPhaseClip(AnimationClip clip) {
        return clip != null && (clip == this.startClip || clip == this.loopClip || clip == this.endClip);
    }

    public boolean isCurrentPoseOwnedByWalkOnWater() {
        if (this.phase != Phase.NONE && this.ownsCurrentClip()) {
            return true;
        }
        if (this.handoffClip == null) {
            return false;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == this.handoffClip && (PunchyAnimationManager.POSE_HANDLER.isPlaying() || PunchyAnimationManager.POSE_HANDLER.isBlendOutActive())) {
            return true;
        }
        this.handoffClip = null;
        return false;
    }

    public boolean isActiveOrHandingOff() {
        if (this.phase != Phase.NONE || this.suspendedByForeignClip) {
            return true;
        }
        if (this.handoffClip == null) {
            return false;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == this.handoffClip && (PunchyAnimationManager.POSE_HANDLER.isPlaying() || PunchyAnimationManager.POSE_HANDLER.isBlendOutActive())) {
            return true;
        }
        this.handoffClip = null;
        return false;
    }

    private void logTriggerState(class_310 client, PunchyTuningConfig.CustomAnimationEntry entry, WalkOnWaterSelection selection, TriggerState triggerState) {
        this.lastTriggerActive = triggerState.active();
        this.lastTriggerReason = triggerState.reason();
    }

    private void logResolvedSelection(PunchyTuningConfig.CustomAnimationEntry entry, WalkOnWaterSelection selection) {
        String summary = "entry=" + (entry == null ? "null" : entry.type) + " start=" + WalkOnWaterStateMachine.clipName(selection.start()) + " loop=" + WalkOnWaterStateMachine.clipName(selection.loop()) + " end=" + WalkOnWaterStateMachine.clipName(selection.end()) + " valid=" + selection.isValid();
        if (WalkOnWaterStateMachine.safeEquals(this.lastResolvedSummary, summary)) {
            return;
        }
        this.lastResolvedSummary = summary;
    }

    private void logPhaseTransition(String targetPhase, AnimationClip clip) {
        this.logPhaseTransition(targetPhase, clip, null);
    }

    private void logPhaseTransition(String targetPhase, AnimationClip clip, Float startTime) {
        if (this.phase == this.lastLoggedPhase && this.activeClip == clip) {
            return;
        }
        this.lastLoggedPhase = this.phase;
    }

    private static String clipName(AnimationClip clip) {
        return clip == null ? "null" : clip.getName();
    }

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static enum Phase {
        NONE,
        START,
        LOOP,
        END;

    }

    private record WalkOnWaterSelection(AnimationClip start, AnimationClip loop, AnimationClip end) {
        private static final WalkOnWaterSelection INVALID = new WalkOnWaterSelection(null, null, null);

        private boolean isValid() {
            return this.start != null && this.loop != null && this.end != null;
        }
    }

    private record TriggerState(boolean active, String reason) {
    }
}

