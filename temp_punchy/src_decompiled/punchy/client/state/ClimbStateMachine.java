/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package punchy.client.state;

import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_746;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.HandEquipStateMachine;
import punchy.config.PunchyConfig;

public class ClimbStateMachine {
    private static final String CLIMB_CLIP_NAME = "climb";
    private static final float RESUME_BLEND_IN_SECONDS = 0.25f;
    private static final int EQUIP_RELEASE_TICKS = 6;
    private static final int SWAP_HOLD_TICKS = 12;
    private boolean wasClimbing;
    private boolean suspendedByMain;
    private class_1306 suppressedMainArm;
    private boolean equipSequenceActive;
    private int equipReleaseTicks;
    private boolean equipTransitionActiveThisTick;
    private boolean swapHoldActiveThisTick;
    private int swapHoldTicks;
    private boolean swapKeyDown;
    private class_1799 lastMain = class_1799.field_8037;
    private class_1799 lastOff = class_1799.field_8037;
    private boolean swapSuspendActive;
    private static boolean suppressSwimForSwap;

    public void tick(class_310 client) {
        boolean equipTransition;
        if (client == null || client.field_1724 == null) {
            this.stopClimbIfNeeded();
            return;
        }
        if (!PunchyConfig.enableClimbAnimation()) {
            this.stopClimbIfNeeded();
            return;
        }
        if (!client.field_1690.method_31044().method_31034()) {
            this.stopClimbIfNeeded();
            return;
        }
        class_746 player = client.field_1724;
        if (!this.isClimbingUp(client, player)) {
            this.stopClimbIfNeeded();
            return;
        }
        boolean mainActive = this.isMainAnimationActive();
        if (mainActive && this.isMainDualHanded()) {
            this.suspendForMainAnimation();
            return;
        }
        AnimationClip clip = PunchyAnimationManager.resolveCustomClip(CLIMB_CLIP_NAME);
        if (clip == null) {
            this.stopClimbIfNeeded();
            return;
        }
        if (!this.wasClimbing) {
            this.lastMain = this.copyStack(player.method_6047());
            this.lastOff = this.copyStack(player.method_6079());
            this.swapHoldTicks = 0;
            this.swapSuspendActive = false;
        }
        PoseHandler handler = PunchyAnimationManager.SWIM_HANDLER;
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        this.swapHoldActiveThisTick = this.updateSwapHold(client, player);
        suppressSwimForSwap = false;
        this.equipTransitionActiveThisTick = equipTransition = this.updateEquipSequenceState();
        if (equipTransition) {
            class_1306 equipArm = this.resolveMainArm();
            if (equipArm != null) {
                this.suppressedMainArm = equipArm;
                handler.requestSkipArm(equipArm);
            } else if (this.suppressedMainArm != null) {
                handler.requestSkipArm(this.suppressedMainArm);
            }
        }
        if (mainActive) {
            this.suppressClimbArmForMainAnimation();
        } else if (!equipTransition && this.suppressedMainArm != null) {
            handler.requestBlendInArm(this.suppressedMainArm);
            this.suppressedMainArm = null;
        }
        if (!equipTransition && this.suppressedMainArm != null) {
            handler.setBlendInDurationNext(0.25f);
        }
        if (!mainActive && this.suspendedByMain) {
            handler.setBlendInDurationNext(0.25f);
            this.suspendedByMain = false;
        }
        handler.setLoopSpeedParams(Float.valueOf(1.0f), Float.valueOf(0.0f));
        handler.setIgnoreDualHanded(false);
        handler.setActiveArm(null);
        handler.setLooping(true);
        handler.setFrozen(false);
        handler.setPaused(!this.isMoving(client));
        if (!handler.isPlaying() || handler.getCurrentClip() != clip) {
            handler.play(clip, speedMultiplier, false, false);
        }
        this.lastMain = this.copyStack(player.method_6047());
        this.lastOff = this.copyStack(player.method_6079());
        this.wasClimbing = true;
    }

    private boolean isClimbingUp(class_310 client, class_746 player) {
        if (client == null || player == null) {
            return false;
        }
        if (!player.method_6101()) {
            return false;
        }
        double dy = player.method_18798().field_1351;
        if (dy > 0.02) {
            return true;
        }
        if (client.field_1690 == null) {
            return false;
        }
        return client.field_1690.field_1894.method_1434() || client.field_1690.field_1903.method_1434();
    }

    private void suspendForMainAnimation() {
        PoseHandler handler = PunchyAnimationManager.SWIM_HANDLER;
        AnimationClip clip = handler.getCurrentClip();
        if (clip != null && PunchyAnimationManager.isClipNamed(clip, CLIMB_CLIP_NAME)) {
            handler.setLooping(false);
            handler.setPaused(false);
            handler.setFrozen(false);
            handler.stopImmediateWithBlendOut();
            this.suspendedByMain = true;
            this.suppressedMainArm = null;
        }
    }

    private void stopClimbIfNeeded() {
        if (!this.wasClimbing) {
            return;
        }
        PoseHandler handler = PunchyAnimationManager.SWIM_HANDLER;
        AnimationClip clip = handler.getCurrentClip();
        if (clip != null && PunchyAnimationManager.isClipNamed(clip, CLIMB_CLIP_NAME)) {
            handler.setLooping(false);
            handler.setPaused(false);
            handler.setFrozen(false);
            handler.stopImmediateWithBlendOut();
        }
        this.wasClimbing = false;
        this.suspendedByMain = false;
        this.suppressedMainArm = null;
        this.equipSequenceActive = false;
        this.equipReleaseTicks = 0;
        this.equipTransitionActiveThisTick = false;
        this.swapHoldActiveThisTick = false;
        this.swapHoldTicks = 0;
        this.swapKeyDown = false;
        this.lastMain = class_1799.field_8037;
        this.lastOff = class_1799.field_8037;
        this.swapSuspendActive = false;
        suppressSwimForSwap = false;
    }

    private boolean isMoving(class_310 client) {
        if (client == null || client.field_1690 == null || client.field_1724 == null) {
            return false;
        }
        boolean forward = client.field_1690.field_1894.method_1434();
        boolean back = client.field_1690.field_1881.method_1434();
        boolean left = client.field_1690.field_1913.method_1434();
        boolean right = client.field_1690.field_1849.method_1434();
        boolean jump = client.field_1690.field_1903.method_1434();
        boolean sneak = client.field_1690.field_1832.method_1434();
        if (forward || back || left || right || jump || sneak) {
            return true;
        }
        if (client.field_1724.method_6101()) {
            return Math.abs(client.field_1724.method_18798().field_1351) > 0.001;
        }
        return Math.abs(client.field_1724.field_6212) > 0.001f || Math.abs(client.field_1724.field_6250) > 0.001f;
    }

    private boolean isMainAnimationActive() {
        return PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() != null;
    }

    private boolean isEquipClipActive() {
        if (!this.isMainAnimationActive()) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        return "hand_in".equals(name) || "hand_out".equals(name);
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

    private void suppressClimbArmForMainAnimation() {
        class_1306 equipArm;
        PoseHandler handler = PunchyAnimationManager.SWIM_HANDLER;
        if (!handler.isPlaying()) {
            this.suppressedMainArm = null;
            return;
        }
        AnimationClip clip = handler.getCurrentClip();
        if (!PunchyAnimationManager.isClipNamed(clip, CLIMB_CLIP_NAME)) {
            this.suppressedMainArm = null;
            return;
        }
        class_1306 mainArm = this.resolveMainArm();
        boolean equipTransition = this.equipTransitionActiveThisTick;
        if (this.isEquipClipActive() && equipTransition && (equipArm = this.resolveMainArm()) != null) {
            mainArm = equipArm;
        }
        if (mainArm != null) {
            handler.requestSkipArm(mainArm);
            this.suppressedMainArm = mainArm;
            return;
        }
        if (this.suppressedMainArm != null && !equipTransition) {
            handler.requestBlendInArm(this.suppressedMainArm);
            this.suppressedMainArm = null;
        }
    }

    private boolean isEquipTransitionActive() {
        if (HandEquipStateMachine.isEquipInProgress()) {
            return true;
        }
        return HandEquipStateMachine.getActiveType() != null;
    }

    private boolean isEquipActiveRaw() {
        if (HandEquipStateMachine.isEquipInProgress()) {
            return true;
        }
        if (HandEquipStateMachine.getActiveType() != null) {
            return true;
        }
        return this.isEquipClipActive();
    }

    private boolean updateEquipSequenceState() {
        boolean activeNow;
        boolean bl = activeNow = this.isEquipTransitionActive() || this.isEquipClipActive();
        if (activeNow) {
            this.equipSequenceActive = true;
            this.equipReleaseTicks = 6;
            return true;
        }
        if (!this.equipSequenceActive) {
            return false;
        }
        if (this.equipReleaseTicks > 0) {
            --this.equipReleaseTicks;
            return true;
        }
        this.equipSequenceActive = false;
        return false;
    }

    private boolean updateSwapHold(class_310 client, class_746 player) {
        boolean active;
        if (player == null || client == null || client.field_1690 == null) {
            this.swapHoldTicks = 0;
            return false;
        }
        boolean down = client.field_1690.field_1831.method_1434();
        boolean pressed = down && !this.swapKeyDown;
        this.swapKeyDown = down;
        if (pressed) {
            this.swapHoldTicks = 12;
            return true;
        }
        class_1799 currentMain = player.method_6047();
        class_1799 currentOff = player.method_6079();
        boolean mainChanged = !this.isSameStack(this.lastMain, currentMain);
        boolean offChanged = !this.isSameStack(this.lastOff, currentOff);
        boolean swapDetected = false;
        if (mainChanged || offChanged) {
            boolean lastMainEmpty = this.isEmpty(this.lastMain);
            boolean lastOffEmpty = this.isEmpty(this.lastOff);
            boolean currentMainEmpty = this.isEmpty(currentMain);
            boolean currentOffEmpty = this.isEmpty(currentOff);
            if (!lastMainEmpty && !lastOffEmpty && this.isSameStack(currentMain, this.lastOff) && this.isSameStack(currentOff, this.lastMain)) {
                swapDetected = true;
            } else if (!lastMainEmpty && lastOffEmpty && currentMainEmpty && this.isSameStack(currentOff, this.lastMain)) {
                swapDetected = true;
            } else if (lastMainEmpty && !lastOffEmpty && currentOffEmpty && this.isSameStack(currentMain, this.lastOff)) {
                swapDetected = true;
            }
        }
        if (swapDetected) {
            this.swapHoldTicks = 6;
        }
        boolean bl = active = this.swapHoldTicks > 0;
        if (this.swapHoldTicks > 0 && !swapDetected) {
            --this.swapHoldTicks;
        }
        return active;
    }

    private boolean isSameStack(class_1799 a, class_1799 b) {
        if (this.isEmpty(a) && this.isEmpty(b)) {
            return true;
        }
        if (this.isEmpty(a) || this.isEmpty(b)) {
            return false;
        }
        return class_1799.method_31577((class_1799)a, (class_1799)b);
    }

    private boolean isEmpty(class_1799 stack) {
        return stack == null || stack.method_7960();
    }

    private class_1799 copyStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return class_1799.field_8037;
        }
        return stack.method_7972();
    }

    public static boolean shouldSuppressSwimForSwap() {
        return suppressSwimForSwap;
    }
}

