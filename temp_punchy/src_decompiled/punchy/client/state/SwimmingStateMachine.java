/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1690
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_238
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 */
package punchy.client.state;

import java.util.Locale;
import net.minecraft.class_1306;
import net.minecraft.class_1690;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import punchy.client.PunchyClient;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.bedrockitems.BedrockItemAnimationManager;
import punchy.client.state.WalkOnWaterStateMachine;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public class SwimmingStateMachine {
    private static final int MIN_FALL_HEIGHT_BLOCKS = 5;
    private static final int MAX_WATER_SCAN_BLOCKS = 64;
    private static final float FALLING_IN_WATER_BLEND_IN_SECONDS = 0.64f;
    private static final float SWIM_NO_SHIFT_SPEED_MULTIPLIER = 0.6f;
    private boolean wasSwimming;
    private boolean wasFallingInWater;
    private boolean suppressSwimThisTick;
    private class_1306 lastSwimArm;
    private class_1306 suppressedMainArm;

    public void tick(class_310 client) {
        AnimationClip swimClip;
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (!PunchyConfig.enableSwimAnimation()) {
            this.stopAllSwimOverlays();
            return;
        }
        if (client.field_1724.method_5854() instanceof class_1690) {
            this.stopAllSwimOverlays();
            return;
        }
        this.suppressSwimArmForMainAnimation();
        this.suppressSwimThisTick = false;
        boolean submerged = client.field_1724.method_5869();
        boolean aboveWater = !submerged && this.isAboveWaterSwimming(client);
        WalkOnWaterStateMachine walkOnWater = PunchyClient.getWalkOnWaterStateMachine();
        if (aboveWater && walkOnWater != null && walkOnWater.isCurrentPoseOwnedByWalkOnWater()) {
            aboveWater = false;
        }
        boolean inWater = client.field_1724.method_5799();
        boolean onGround = client.field_1724.method_24828();
        boolean solidSupportAtFeet = this.hasSolidSupportAtFeet(client);
        boolean forward = client.field_1690 != null && client.field_1690.field_1894.method_1434();
        boolean left = client.field_1690 != null && client.field_1690.field_1913.method_1434();
        boolean right = client.field_1690 != null && client.field_1690.field_1849.method_1434();
        float strafe = client.field_1724.field_6212;
        if (Math.abs(strafe) > 0.001f) {
            left = strafe > 0.0f;
            right = strafe < 0.0f;
        }
        boolean moving = forward || left || right;
        this.handleFallingInWater(client, inWater, onGround);
        if (!submerged && !aboveWater) {
            if (this.wasSwimming) {
                this.stopSwim();
                this.wasSwimming = false;
                this.lastSwimArm = null;
            }
            return;
        }
        boolean activeSwimPose = client.field_1724.method_5681();
        if (!activeSwimPose && solidSupportAtFeet || !moving || this.suppressSwimThisTick) {
            if (this.wasSwimming) {
                this.stopSwim();
                this.wasSwimming = false;
                this.lastSwimArm = null;
            }
            return;
        }
        AnimationClip animationClip = swimClip = aboveWater ? this.resolveAboveWaterClip() : PunchyAnimationManager.SWIMING_CLIP;
        if (this.playOverlay(swimClip, forward, left, right, false)) {
            this.wasSwimming = true;
        }
    }

    private boolean playOverlay(AnimationClip clip, boolean forward, boolean left, boolean right, boolean freezeAtStart) {
        class_1306 arm;
        boolean dualHanded;
        boolean allowDual;
        boolean rightSuppressed;
        if (clip == null) {
            return false;
        }
        PunchyAnimationManager.SWIM_HANDLER.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        class_1306 mainArm = this.resolveMainArm();
        boolean leftSuppressed = this.isArmBlockedBySwimFlag(class_310.method_1551(), class_1306.field_6182) || BedrockItemAnimationManager.isSwimSuppressed(class_1306.field_6182) || ItemAnimationManager.isSwimSuppressed(class_1306.field_6182);
        boolean bl = rightSuppressed = this.isArmBlockedBySwimFlag(class_310.method_1551(), class_1306.field_6183) || BedrockItemAnimationManager.isSwimSuppressed(class_1306.field_6183) || ItemAnimationManager.isSwimSuppressed(class_1306.field_6183);
        if (leftSuppressed && rightSuppressed) {
            return false;
        }
        boolean bl2 = allowDual = forward || left && right;
        if (leftSuppressed || rightSuppressed) {
            allowDual = false;
        }
        PunchyAnimationManager.SWIM_HANDLER.setIgnoreDualHanded(!allowDual || mainArm != null);
        if (mainArm == null && this.isMainDualHanded() && !this.isDualHandedEquipPoseActive()) {
            if (this.wasSwimming) {
                this.stopSwimWithoutClear();
                this.wasSwimming = false;
                this.lastSwimArm = null;
            }
            return false;
        }
        float effectTime = 0.0f;
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying() && PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() == clip) {
            effectTime = PunchyAnimationManager.SWIM_HANDLER.getCurrentTime();
        }
        boolean bl3 = dualHanded = allowDual && PunchyAnimationManager.isDualHandedActive(class_310.method_1551(), clip, effectTime);
        if (mainArm != null) {
            dualHanded = false;
            arm = this.opposite(mainArm);
            if (arm == class_1306.field_6182 && leftSuppressed && !rightSuppressed) {
                arm = class_1306.field_6183;
            } else if (arm == class_1306.field_6183 && rightSuppressed && !leftSuppressed) {
                arm = class_1306.field_6182;
            } else if (arm == class_1306.field_6182 && leftSuppressed || arm == class_1306.field_6183 && rightSuppressed) {
                return false;
            }
        } else {
            class_1306 class_13062 = arm = dualHanded ? null : this.resolveSwimArm(forward, left, right, mainArm);
            if (arm == null && leftSuppressed ^ rightSuppressed) {
                class_1306 class_13063 = arm = leftSuppressed ? class_1306.field_6183 : class_1306.field_6182;
            }
            if (!dualHanded) {
                if (this.lastSwimArm == null && arm != null) {
                    class_1306 clearArm = this.opposite(arm);
                    if (clearArm == mainArm) {
                        PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(clearArm);
                    } else {
                        PunchyAnimationManager.SWIM_HANDLER.requestClearArm(clearArm);
                    }
                } else if (this.lastSwimArm != null && arm != null && this.lastSwimArm != arm) {
                    if (this.lastSwimArm == mainArm) {
                        PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(this.lastSwimArm);
                    } else {
                        PunchyAnimationManager.SWIM_HANDLER.requestClearArm(this.lastSwimArm);
                    }
                }
            }
        }
        if (arm == null) {
            if (leftSuppressed) {
                PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(class_1306.field_6182);
            }
            if (rightSuppressed) {
                PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(class_1306.field_6183);
            }
        } else if (arm == class_1306.field_6182 && leftSuppressed) {
            arm = rightSuppressed ? null : class_1306.field_6183;
        } else if (arm == class_1306.field_6183 && rightSuppressed) {
            class_1306 class_13064 = arm = leftSuppressed ? null : class_1306.field_6182;
        }
        if (arm == null && leftSuppressed && rightSuppressed) {
            return false;
        }
        this.lastSwimArm = arm;
        PunchyAnimationManager.SWIM_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.SWIM_HANDLER.setLooping(true);
        PunchyAnimationManager.SWIM_HANDLER.setFrozen(freezeAtStart);
        PunchyAnimationManager.SWIM_HANDLER.setPaused(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        float timelineSpeedScale = 1.0f;
        if (!(clip != PunchyAnimationManager.SWIMING_CLIP && clip != PunchyAnimationManager.SWIMING_ABOVE_CLIP || class_310.method_1551().field_1724 != null && class_310.method_1551().field_1724.method_5624())) {
            timelineSpeedScale = 0.6f;
        }
        PunchyAnimationManager.SWIM_HANDLER.setSpeedMultiplier(speedMultiplier);
        PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(timelineSpeedScale);
        if (!PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            if (clip == this.resolveAboveWaterClip() && this.canRebaseFromWalkOnWater()) {
                PunchyAnimationManager.SWIM_HANDLER.playAtTimeRebasedFromHandlerReturnToOrigin(PunchyAnimationManager.POSE_HANDLER, clip, speedMultiplier, false, 0.0f);
            } else {
                PunchyAnimationManager.SWIM_HANDLER.play(clip, speedMultiplier, false, false);
            }
        } else if (PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() != clip) {
            AnimationClip previousClip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
            float startAt = this.remapSwimTransitionStartTime(previousClip, clip, PunchyAnimationManager.SWIM_HANDLER.getCurrentTime());
            if (previousClip == this.resolveAboveWaterClip() && clip == PunchyAnimationManager.SWIMING_CLIP) {
                PunchyAnimationManager.SWIM_HANDLER.playWithBlendFromCurrentDynamic(clip, speedMultiplier, false);
            } else {
                PunchyAnimationManager.SWIM_HANDLER.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, false, startAt);
            }
        }
        return true;
    }

    private void handleFallingInWater(class_310 client, boolean inWater, boolean onGround) {
        AnimationClip clip = PunchyAnimationManager.FALLING_IN_WATER_CLIP;
        if (clip == null) {
            this.wasFallingInWater = false;
            return;
        }
        if (this.isMainAnimationActive()) {
            if (this.wasFallingInWater) {
                this.stopFallingInWaterWithoutClear();
                this.wasFallingInWater = false;
                this.lastSwimArm = null;
            }
            return;
        }
        if (this.wasFallingInWater) {
            if (inWater || onGround || !this.isActuallyFallingTowardWater(client)) {
                this.stopFallingInWater();
                this.suppressSwimThisTick = true;
                this.wasFallingInWater = false;
                this.lastSwimArm = null;
            }
            return;
        }
        if (inWater || !this.shouldStartFallingInWater(client)) {
            return;
        }
        boolean leftBlockedByFlag = this.isArmBlockedBySwimFlag(client, class_1306.field_6182);
        boolean rightBlockedByFlag = this.isArmBlockedBySwimFlag(client, class_1306.field_6183);
        if (leftBlockedByFlag && rightBlockedByFlag) {
            return;
        }
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying() && PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() != clip) {
            return;
        }
        class_1306 forcedArm = null;
        if (leftBlockedByFlag && !rightBlockedByFlag) {
            forcedArm = class_1306.field_6183;
        } else if (rightBlockedByFlag && !leftBlockedByFlag) {
            forcedArm = class_1306.field_6182;
        }
        PunchyAnimationManager.SWIM_HANDLER.setIgnoreDualHanded(forcedArm != null);
        PunchyAnimationManager.SWIM_HANDLER.setActiveArm(forcedArm);
        if (leftBlockedByFlag) {
            PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(class_1306.field_6182);
        }
        if (rightBlockedByFlag) {
            PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(class_1306.field_6183);
        }
        PunchyAnimationManager.SWIM_HANDLER.setLooping(true);
        PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(1.0f);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        if (!PunchyAnimationManager.SWIM_HANDLER.isPlaying() || PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() != clip) {
            PunchyAnimationManager.SWIM_HANDLER.setBlendInDurationNext(0.64f);
            PunchyAnimationManager.SWIM_HANDLER.play(clip, speedMultiplier, false, false);
        }
        this.wasFallingInWater = true;
    }

    private void stopFallingInWater() {
        PunchyAnimationManager.SWIM_HANDLER.setLooping(false);
        PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(1.0f);
        PunchyAnimationManager.SWIM_HANDLER.stopImmediateWithBlendOut();
    }

    private void stopFallingInWaterWithoutClear() {
        PunchyAnimationManager.SWIM_HANDLER.setLooping(false);
        PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(1.0f);
        PunchyAnimationManager.SWIM_HANDLER.stopWithoutClear();
    }

    private boolean shouldStartFallingInWater(class_310 client) {
        if (client.field_1724 == null) {
            return false;
        }
        if (!this.isActuallyFallingTowardWater(client)) {
            return false;
        }
        int distanceToWater = this.findWaterDistanceBelow(client.field_1724.method_37908(), client.field_1724.method_24515());
        return distanceToWater >= 5;
    }

    private boolean isActuallyFallingTowardWater(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (client.field_1724.method_24828() || client.field_1724.method_5799() || client.field_1724.method_6128()) {
            return false;
        }
        if (client.field_1724.method_31549().field_7479) {
            return false;
        }
        if (client.field_1724.method_18798().field_1351 >= -0.08) {
            return false;
        }
        return client.field_1724.field_6017 > 1.5f;
    }

    private int findWaterDistanceBelow(class_1937 level, class_2338 start) {
        if (level == null || start == null) {
            return -1;
        }
        int startY = start.method_10264();
        class_2338.class_2339 pos = new class_2338.class_2339(start.method_10263(), startY, start.method_10260());
        for (int i = 0; i <= 64; ++i) {
            int y = startY - i;
            pos.method_33098(y);
            if (level.method_8316((class_2338)pos).method_15767(class_3486.field_15517)) {
                int waterSurfaceY = y + 1;
                return Math.max(0, startY - waterSurfaceY);
            }
            if (!level.method_8320((class_2338)pos).method_51366()) continue;
            return -1;
        }
        return -1;
    }

    private void stopSwim() {
        PunchyAnimationManager.SWIM_HANDLER.setLooping(false);
        PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(1.0f);
        PunchyAnimationManager.SWIM_HANDLER.stopImmediateWithBlendOut();
    }

    private void stopSwimWithoutClear() {
        PunchyAnimationManager.SWIM_HANDLER.setLooping(false);
        PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(1.0f);
        PunchyAnimationManager.SWIM_HANDLER.stopWithoutClear();
    }

    private void stopAllSwimOverlays() {
        AnimationClip clip;
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying() && ((clip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip()) == PunchyAnimationManager.SWIMING_CLIP || clip == PunchyAnimationManager.SWIMING_ABOVE_CLIP || clip == PunchyAnimationManager.FALLING_IN_WATER_CLIP)) {
            PunchyAnimationManager.SWIM_HANDLER.setLooping(false);
            PunchyAnimationManager.SWIM_HANDLER.setTimelineSpeedScale(1.0f);
            PunchyAnimationManager.SWIM_HANDLER.stopWithoutClear();
        }
        this.wasSwimming = false;
        this.wasFallingInWater = false;
        this.lastSwimArm = null;
    }

    private class_1306 resolveSwimArm(boolean forward, boolean left, boolean right, class_1306 mainArm) {
        if (mainArm != null) {
            return this.opposite(mainArm);
        }
        if (forward || left && right) {
            return null;
        }
        if (left) {
            return class_1306.field_6183;
        }
        if (right) {
            return class_1306.field_6182;
        }
        return null;
    }

    private boolean isMainAnimationActive() {
        return PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() != null;
    }

    private boolean isMainDualHanded() {
        if (!this.isMainAnimationActive()) {
            return false;
        }
        if (PunchyAnimationManager.isDualHandedActive()) {
            return true;
        }
        if (PunchyAnimationManager.POSE_HANDLER.getActiveArm() != null) {
            return false;
        }
        if (PunchyAnimationManager.getSourceArm() != null) {
            return false;
        }
        return PunchyAnimationManager.POSE_HANDLER.getActiveArm() == null;
    }

    private boolean isDualHandedEquipPoseActive() {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String key = name.toLowerCase(Locale.ROOT);
        if (!(key.equals("hand_in") || key.equals("hand_out") || key.startsWith("hand_in_") || key.startsWith("hand_out_"))) {
            return false;
        }
        if (PunchyAnimationManager.POSE_HANDLER.getActiveArm() != null) {
            return false;
        }
        return PunchyAnimationManager.isDualHandedActive(clip, PunchyAnimationManager.POSE_HANDLER.getCurrentTime());
    }

    private class_1306 resolveMainArm() {
        if (!this.isMainAnimationActive()) {
            return null;
        }
        if (this.isMainDualHanded()) {
            return null;
        }
        class_1306 active = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (active != null) {
            return active;
        }
        return PunchyAnimationManager.getSourceArm();
    }

    private void suppressSwimArmForMainAnimation() {
        if (!PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            this.suppressedMainArm = null;
            return;
        }
        AnimationClip clip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
        if (clip != PunchyAnimationManager.SWIMING_CLIP && clip != PunchyAnimationManager.SWIMING_ABOVE_CLIP) {
            this.suppressedMainArm = null;
            return;
        }
        class_1306 mainArm = this.resolveMainArm();
        if (mainArm != null) {
            PunchyAnimationManager.SWIM_HANDLER.requestSkipArm(mainArm);
            this.suppressedMainArm = mainArm;
            return;
        }
        if (this.suppressedMainArm != null) {
            PunchyAnimationManager.SWIM_HANDLER.requestBlendInArm(this.suppressedMainArm);
            this.suppressedMainArm = null;
        }
    }

    private class_1306 opposite(class_1306 arm) {
        return arm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
    }

    private boolean isArmBlockedBySwimFlag(class_310 client, class_1306 arm) {
        class_1306 mainArm;
        if (client == null || client.field_1724 == null || arm == null) {
            return false;
        }
        boolean mainRight = client.field_1724.method_6068() == class_1306.field_6183;
        class_1306 class_13062 = mainArm = mainRight ? class_1306.field_6183 : class_1306.field_6182;
        if (arm == mainArm) {
            return PunchyTuningConfig.isSwimingDisabledForStack(client.field_1724.method_6047());
        }
        return PunchyTuningConfig.isSwimingDisabledForStack(client.field_1724.method_6079());
    }

    private boolean isDeepWaterForSwim(class_310 client) {
        class_2338 feet;
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1937 level = client.field_1724.method_37908();
        if (!level.method_8316(feet = client.field_1724.method_24515()).method_15767(class_3486.field_15517)) {
            return false;
        }
        class_2338.class_2339 pos = new class_2338.class_2339(feet.method_10263(), feet.method_10264(), feet.method_10260());
        int waterAbove = 0;
        for (int i = 0; i < 4; ++i) {
            pos.method_33098(feet.method_10264() + i);
            if (!level.method_8316((class_2338)pos).method_15767(class_3486.field_15517)) break;
            ++waterAbove;
        }
        return waterAbove >= 2;
    }

    private boolean isAboveWaterSwimming(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (client.field_1724.method_5869() || client.field_1724.method_5777(class_3486.field_15517)) {
            return false;
        }
        if (this.hasSolidSupportAtFeet(client)) {
            return false;
        }
        return this.hasWaterContactAnyDepth(client);
    }

    private AnimationClip resolveAboveWaterClip() {
        AnimationClip clip = PunchyAnimationManager.SWIMING_ABOVE_CLIP;
        if (clip != null) {
            return clip;
        }
        return PunchyAnimationManager.resolveCustomClip("swiming_above");
    }

    private boolean canRebaseFromWalkOnWater() {
        PoseHandler source = PunchyAnimationManager.POSE_HANDLER;
        if (source == null || source.getCurrentClip() == null || !source.isPlaying() && !source.isBlendOutActive()) {
            return false;
        }
        WalkOnWaterStateMachine walkOnWater = PunchyClient.getWalkOnWaterStateMachine();
        return walkOnWater != null && walkOnWater.isCurrentPoseOwnedByWalkOnWater();
    }

    private float remapSwimTransitionStartTime(AnimationClip fromClip, AnimationClip toClip, float fromTime) {
        if (fromClip == null || toClip == null) {
            return 0.0f;
        }
        float fromLength = fromClip.getLength();
        float toLength = toClip.getLength();
        if (fromLength <= 0.0f || toLength <= 0.0f) {
            return 0.0f;
        }
        float normalized = fromTime % fromLength;
        if (normalized < 0.0f) {
            normalized += fromLength;
        }
        return normalized / fromLength * toLength;
    }

    private boolean isShallowWater(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1937 level = client.field_1724.method_37908();
        class_238 bounds = client.field_1724.method_5829();
        double sampleY = bounds.field_1322 + 0.05;
        double insetX = Math.min(0.2, bounds.method_17939() * 0.25);
        double insetZ = Math.min(0.2, bounds.method_17941() * 0.25);
        double[] sampleXs = new double[]{bounds.field_1323 + insetX, bounds.field_1320 - insetX, (bounds.field_1323 + bounds.field_1320) * 0.5};
        double[] sampleZs = new double[]{bounds.field_1321 + insetZ, bounds.field_1324 - insetZ, (bounds.field_1321 + bounds.field_1324) * 0.5};
        for (double sampleX : sampleXs) {
            for (double sampleZ : sampleZs) {
                class_2338 below;
                class_2338 waterPos = class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ);
                if (!level.method_8316(waterPos).method_15767(class_3486.field_15517) || level.method_8316(below = waterPos.method_10074()).method_15767(class_3486.field_15517) || !level.method_8320(below).method_51366()) continue;
                return true;
            }
        }
        return false;
    }

    private boolean hasSolidSupportAtFeet(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1937 level = client.field_1724.method_37908();
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

    private boolean hasWaterContactAnyDepth(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1937 level = client.field_1724.method_37908();
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
}

