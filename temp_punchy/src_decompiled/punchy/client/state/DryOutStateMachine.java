/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 *  net.minecraft.class_3610
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package punchy.client.state;

import java.util.Map;
import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_638;
import net.minecraft.class_746;
import punchy.client.PunchyClient;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.WalkOnWaterStateMachine;
import punchy.config.PunchyTuningConfig;

public final class DryOutStateMachine {
    private static final int MIN_WET_TICKS = 40;
    private static final float DRY_OUT_REBASE_SPEED = 3.4f;
    private static final String START_WITH_OFFHAND_EFFECT = "start_here_if_off_hand_item";
    private static final String START_WITH_OFFHAND_EFFECT_ALIAS = "start_if_offhand_item";
    private static final String STOP_WITH_OFFHAND_EFFECT = "stop_if_offhand_item";
    private int wetTicks;
    private boolean wasTouchingWater;
    private boolean pendingDryOut;
    private AnimationClip activeClip;

    public void tick(class_310 client) {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (client == null || client.field_1724 == null || client.field_1687 == null || handler == null) {
            this.resetTracking();
            return;
        }
        if (!client.field_1690.method_31044().method_31034()) {
            this.resetTracking();
            return;
        }
        if (client.field_1724.method_7325() || !client.field_1724.method_5805()) {
            this.stopIfOwned(handler);
            this.resetTracking();
            return;
        }
        if (this.resolveDryOutActiveArm(client) == null) {
            this.logDryOutState("suppressed", client, null);
            this.stopIfOwned(handler);
            this.resetTracking();
            return;
        }
        AnimationClip clip = PunchyAnimationManager.resolveNamedClip("dry_out", client, class_1268.field_5808);
        if (clip == null) {
            this.stopIfOwned(handler);
            this.resetTracking();
            return;
        }
        if (this.shouldStopForOffhand(handler)) {
            this.stopIfOwned(handler);
            this.pendingDryOut = false;
            this.wetTicks = 0;
            return;
        }
        boolean touchingWater = this.hasWaterContactAnyDepth(client);
        boolean leftWaterThisTick = this.wasTouchingWater && !touchingWater;
        this.wasTouchingWater = touchingWater;
        if (touchingWater) {
            ++this.wetTicks;
            this.pendingDryOut = false;
            this.stopIfOwned(handler);
            return;
        }
        if (leftWaterThisTick) {
            this.pendingDryOut = this.wetTicks >= 40;
            this.wetTicks = 0;
        } else if (!this.pendingDryOut) {
            this.wetTicks = 0;
        }
        if (!this.pendingDryOut) {
            this.clearFinishedClip(handler);
            return;
        }
        if (!this.isReadyToDryOut(client)) {
            this.clearFinishedClip(handler);
            return;
        }
        WalkOnWaterStateMachine walkOnWater = PunchyClient.getWalkOnWaterStateMachine();
        if (walkOnWater != null && walkOnWater.isActiveOrHandingOff()) {
            this.clearFinishedClip(handler);
            return;
        }
        this.playDryOut(client, handler, clip);
        this.pendingDryOut = false;
        this.wetTicks = 0;
    }

    private void playDryOut(class_310 client, PoseHandler handler, AnimationClip clip) {
        if (handler.getCurrentClip() == clip && (handler.isPlaying() || handler.isBlendOutActive())) {
            this.activeClip = clip;
            return;
        }
        float startTime = this.resolveStartTime(client, clip);
        HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client.field_1724);
        PunchyAnimationManager.setSourceHand(client, class_1268.field_5808);
        handler.setIgnoreDualHanded(this.isAnyDryOutArmBlocked(client));
        handler.setLooping(false);
        handler.setFrozen(false);
        handler.setPaused(false);
        class_1306 activeArm = this.resolveActiveArm(client, clip, startTime);
        handler.setActiveArm(activeArm);
        handler.setBlendInDurationNext(0.0f);
        handler.setRebaseReturnSpeedNext(3.4f);
        handler.playAtTimeRebasedFromCurrentReturnToOrigin(clip, PunchyAnimationManager.clipSpeedMultiplier(clip), false, startTime);
        this.activeClip = clip;
        this.logDryOutState("play", client, activeArm);
    }

    private class_1306 resolveActiveArm(class_310 client, AnimationClip clip, float time) {
        class_1306 filteredArm = this.resolveDryOutActiveArm(client);
        if (filteredArm == null) {
            return null;
        }
        if (this.isAnyDryOutArmBlocked(client)) {
            return filteredArm;
        }
        if (PunchyAnimationManager.isDualHandedActive(client, clip, time)) {
            return null;
        }
        return filteredArm;
    }

    private boolean isAnyDryOutArmBlocked(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        return PunchyTuningConfig.isDryOutDisabledForStack(client.field_1724.method_6047()) || PunchyTuningConfig.isDryOutDisabledForStack(client.field_1724.method_6079());
    }

    private class_1306 resolveDryOutActiveArm(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return null;
        }
        boolean mainBlocked = PunchyTuningConfig.isDryOutDisabledForStack(client.field_1724.method_6047());
        boolean offBlocked = PunchyTuningConfig.isDryOutDisabledForStack(client.field_1724.method_6079());
        class_1306 mainArm = this.resolveArm(client.field_1724, class_1268.field_5808);
        class_1306 offArm = this.resolveArm(client.field_1724, class_1268.field_5810);
        if (mainBlocked && offBlocked) {
            this.logDryOutState("resolveArm", client, null);
            return null;
        }
        if (mainBlocked) {
            return offArm;
        }
        if (offBlocked) {
            return mainArm;
        }
        class_1306 resolved = PunchyAnimationManager.getUseArm(client, class_1268.field_5808);
        this.logDryOutState("resolveArm", client, resolved);
        return resolved;
    }

    private void logDryOutState(String event, class_310 client, class_1306 resolvedArm) {
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

    private float resolveStartTime(class_310 client, AnimationClip clip) {
        if (client == null || client.field_1724 == null || clip == null) {
            return 0.0f;
        }
        if (client.field_1724.method_6079() == null || client.field_1724.method_6079().method_7960()) {
            return 0.0f;
        }
        Float startTime = this.findTimelineEffectTime(clip, START_WITH_OFFHAND_EFFECT);
        if (startTime != null) {
            return startTime.floatValue();
        }
        Float alias = this.findTimelineEffectTime(clip, START_WITH_OFFHAND_EFFECT_ALIAS);
        return alias == null ? 0.0f : alias.floatValue();
    }

    private boolean shouldStopForOffhand(PoseHandler handler) {
        if (handler == null || this.activeClip == null) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (!handler.isPlaying() || handler.getCurrentClip() != this.activeClip) {
            return false;
        }
        if (client.field_1724.method_6079() == null || client.field_1724.method_6079().method_7960()) {
            return false;
        }
        Float stopTime = this.findTimelineEffectTime(this.activeClip, STOP_WITH_OFFHAND_EFFECT);
        return stopTime != null && handler.getCurrentTime() >= stopTime.floatValue();
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

    private boolean isReadyToDryOut(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (client.field_1724.method_31549().field_7479 || client.field_1724.method_6128() || client.field_1724.method_5854() != null) {
            return false;
        }
        return this.hasDryGroundSupport(client);
    }

    private boolean hasDryGroundSupport(class_310 client) {
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
        boolean solidSupport = false;
        for (double sampleX : sampleXs) {
            for (double sampleZ : sampleZs) {
                class_2338 supportPos = class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ);
                if (level.method_8316(supportPos).method_15767(class_3486.field_15517)) {
                    return false;
                }
                if (!level.method_8320(supportPos).method_51366()) continue;
                solidSupport = true;
            }
        }
        return solidSupport;
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
                    class_3610 fluidState = level.method_8316(class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ));
                    if (!fluidState.method_15767(class_3486.field_15517)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private void stopIfOwned(PoseHandler handler) {
        if (handler == null || this.activeClip == null) {
            return;
        }
        if (handler.getCurrentClip() == this.activeClip && (handler.isPlaying() || handler.isBlendOutActive())) {
            handler.stopImmediateWithBlendOut();
        }
        this.activeClip = null;
    }

    private void clearFinishedClip(PoseHandler handler) {
        if (handler == null || this.activeClip == null) {
            return;
        }
        if (handler.getCurrentClip() != this.activeClip || !handler.isPlaying() && !handler.isBlendOutActive()) {
            this.activeClip = null;
        }
    }

    private void resetTracking() {
        this.wetTicks = 0;
        this.wasTouchingWater = false;
        this.pendingDryOut = false;
        this.activeClip = null;
    }
}

