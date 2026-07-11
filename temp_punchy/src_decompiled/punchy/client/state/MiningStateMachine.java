/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3965
 *  net.minecraft.class_9334
 *  net.minecraft.class_9336
 */
package punchy.client.state;

import java.util.Objects;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_9334;
import net.minecraft.class_9336;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.ToolKindResolver;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.MiningActionTracker;
import punchy.config.PunchyTuningConfig;

public class MiningStateMachine {
    private static final long MINING_TARGET_LOCK_NANOS = 250000000L;
    private static final long RECENT_TARGET_GRACE_NANOS = 90000000L;
    private static final long RECENT_MINING_CHAIN_NANOS = 180000000L;
    private static final long MIN_MINING_DURATION_TICKS = 2L;
    private static final long MINING_V2_FASTEST_TRIGGER_TICKS = 1L;
    private static final long MINING_V2_SLOWEST_TRIGGER_TICKS = 6L;
    private static final float MINING_V2_START_SPEED_MIN = 1.0f;
    private static final float MINING_V2_START_SPEED_MAX = 2.35f;
    private static final float MINING_V2_START_SPEED_RESPONSE = 0.45f;
    private static final float STOP_IMMEDIATE_BLEND_OUT_SECONDS = 0.3f;
    private static final int TARGETED_GRACE_TICKS = 2;
    private static final int RECENT_TARGET_GRACE_TICKS = 1;
    private static final int RECENT_CHAIN_GRACE_TICKS = 2;
    private boolean wasMining;
    private long lastTriggerTick = -1L;
    private static final long FAST_TRIGGER_WINDOW_TICKS = 4L;
    private int stopGraceTicks = 0;
    private boolean stopImmediateOnRelease;
    private boolean skipBlendOut;
    private AnimationClip activeClip;
    private class_1306 activeArm;
    private class_1799 lastMiningStack = class_1799.field_8037;
    private long minStopTick = -1L;
    private MiningV2Phase miningV2Phase = MiningV2Phase.NONE;
    private PunchyTuningConfig.CustomAnimationEntry miningV2Entry;
    private AnimationClip miningV2StartClip;
    private AnimationClip miningV2LoopClip;
    private AnimationClip miningV2EndClip;
    private class_1799 lastMiningV2Stack = class_1799.field_8037;
    private long miningV2LastTriggerTick = -1L;
    private float miningV2StartSpeed = 1.0f;

    public void tick(class_310 client) {
        boolean hasMiningV2;
        if (client == null || client.field_1724 == null || client.field_1761 == null) {
            return;
        }
        class_1799 stack = client.field_1724.method_6047();
        if (PunchyAnimationManager.isHandBlacklisted(client, class_1268.field_5808)) {
            if (this.wasMining) {
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                this.stopMiningAnimation(client, true);
                this.clearMiningState();
                this.resetMiningV2();
            } else {
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
            }
            this.wasMining = false;
            return;
        }
        boolean rawMining = MiningActionTracker.isMiningActive() && client.field_1724 != null && !client.field_1724.method_7325();
        long now = this.currentTick();
        boolean mining = this.shouldKeepMiningActive(rawMining, client, now);
        boolean triggered = MiningActionTracker.consumeTrigger();
        if (triggered && !mining && !client.field_1724.method_7325()) {
            mining = true;
        }
        if (HandEquipStateMachine.shouldTreatHandOutActive()) {
            this.clearMiningForEquipTransition();
            return;
        }
        boolean bl = hasMiningV2 = this.hasMiningV2Entry(client, stack) || this.miningV2Phase != MiningV2Phase.NONE;
        if (this.isFunctionalSwapFromMainToOffhand(client, stack)) {
            return;
        }
        if (mining && HandEquipStateMachine.isEquipInProgress() && this.shouldAbortEquipForMainHand(client) && !HandEquipStateMachine.abortEquipForAction(client, class_1268.field_5808)) {
            return;
        }
        if (triggered && hasMiningV2 && HandEquipStateMachine.isEquipInProgress() && this.shouldAbortEquipForMainHand(client) && !HandEquipStateMachine.abortEquipForAction(client, class_1268.field_5808)) {
            return;
        }
        if (hasMiningV2) {
            if (!mining && !triggered && this.miningV2Phase != MiningV2Phase.NONE && this.stopImmediateOnRelease && !this.isAttackDown(client)) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                if (this.skipBlendOut) {
                    PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
                } else {
                    PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.3f);
                }
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
                this.clearMiningState();
                this.resetMiningV2();
                this.wasMining = false;
                return;
            }
            if (mining || triggered || this.miningV2Phase != MiningV2Phase.NONE) {
                ItemAnimationManager.setPersistentType(class_1268.field_5808, "mining", true, true);
                this.tickMiningV2(client, stack, rawMining, mining, triggered);
            } else {
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
            }
            this.wasMining = mining;
            return;
        }
        if (mining) {
            ItemAnimationManager.setPersistentType(class_1268.field_5808, "mining", true, true);
            PunchyAnimationManager.POSE_HANDLER.setLooping(true);
            boolean itemChanged = this.hasItemChanged(this.lastMiningStack, stack);
            if (itemChanged && this.isFunctionalSwapFromMainToOffhand(client, stack)) {
                this.wasMining = true;
                return;
            }
            if (!this.wasMining || !PunchyAnimationManager.POSE_HANDLER.isPlaying() || itemChanged) {
                this.playPunch();
            }
        } else if (this.wasMining) {
            if (this.isFunctionalSwapFromMainToOffhand(client, stack)) {
                this.wasMining = true;
                return;
            }
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            this.stopMiningAnimation(client, false);
            this.clearMiningState();
        } else {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
        }
        this.wasMining = mining;
    }

    private void playPunch() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        class_1799 stack = mc.field_1724.method_6047();
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "mining");
        AnimationClip clip = this.resolveCustomMiningClip(mc, stack, entry);
        String resolutionPath = "custom_entry";
        if (clip == null) {
            CustomAnimationResolver.applyLoopSpeed(null);
            String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
            String preferredTypeOption = PunchyTuningConfig.getMixPreferredOptionForType(stack, "mining");
            clip = PunchyAnimationManager.resolveHandedClip("punch", mc, class_1268.field_5808, preferredOption);
            resolutionPath = "fallback_punch";
            if (stack.method_7909() instanceof class_1835) {
                AnimationClip tridentClip = PunchyAnimationManager.resolveHandedClip("attack_trident", mc, class_1268.field_5808, preferredOption);
                if (tridentClip != null) {
                    clip = tridentClip;
                    resolutionPath = "fallback_trident";
                }
            } else if (kind == VanillaFirstPersonItemLayer.ToolKind.PICKAXE || kind == VanillaFirstPersonItemLayer.ToolKind.HOE || kind == VanillaFirstPersonItemLayer.ToolKind.AXE || kind == VanillaFirstPersonItemLayer.ToolKind.SHOVEL) {
                if (kind == VanillaFirstPersonItemLayer.ToolKind.AXE && (clip = PunchyAnimationManager.resolveHandedClip("mining_sideways", mc, class_1268.field_5808, preferredOption)) != null) {
                    resolutionPath = "fallback_kind_sideways";
                }
                if (clip == null && (clip = PunchyAnimationManager.resolveTypeFallbackClip("mining", mc, class_1268.field_5808, preferredOption)) != null) {
                    resolutionPath = "fallback_type_alias";
                }
                if (clip == null) {
                    if (kind == VanillaFirstPersonItemLayer.ToolKind.PICKAXE || kind == VanillaFirstPersonItemLayer.ToolKind.HOE) {
                        clip = PunchyAnimationManager.resolveHandedClip("mining_foward", mc, class_1268.field_5808, preferredOption);
                        resolutionPath = "fallback_kind_forward";
                    } else if (kind == VanillaFirstPersonItemLayer.ToolKind.AXE) {
                        clip = PunchyAnimationManager.resolveHandedClip("mining_foward", mc, class_1268.field_5808, preferredOption);
                        resolutionPath = "fallback_kind_axe_forward";
                    } else {
                        clip = PunchyAnimationManager.resolveHandedClip("mining_digging", mc, class_1268.field_5808, preferredOption);
                        resolutionPath = "fallback_kind_digging";
                    }
                }
            }
        } else {
            CustomAnimationResolver.applyLoopSpeed(entry);
        }
        if (clip != null) {
            class_1306 arm;
            this.stopImmediateOnRelease = CustomAnimationResolver.shouldStopImmediately(entry);
            this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(entry);
            this.activeArm = arm = PunchyAnimationManager.getMiningArm();
            this.activeClip = clip;
            this.primeOppositeArmOverlapDuringEquip(mc, class_1268.field_5808, arm);
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
            float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
            boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose() || this.shouldSkipBlendIn() || CustomAnimationResolver.shouldSkipBlendIn(entry);
            CustomAnimationResolver.playConfiguredClip(clip, entry, speedMultiplier, skipBlendIn, false);
            this.lastTriggerTick = this.currentTick();
            this.minStopTick = this.lastTriggerTick + 2L;
            this.lastMiningStack = this.copyStack(stack);
        }
    }

    private class_1799 copyStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return class_1799.field_8037;
        }
        return stack.method_7972();
    }

    private boolean hasItemChanged(class_1799 previous, class_1799 current) {
        class_1799 now;
        class_1799 prev = previous == null ? class_1799.field_8037 : previous;
        class_1799 class_17992 = now = current == null ? class_1799.field_8037 : current;
        if (!prev.method_31574(now.method_7909())) {
            return true;
        }
        for (class_9336 dataComponent : now.method_57353()) {
            if (dataComponent.comp_2443() == class_9334.field_49629) continue;
            if (!prev.method_57353().method_57832(dataComponent.comp_2443())) {
                return true;
            }
            if (Objects.equals(prev.method_57824(dataComponent.comp_2443()), dataComponent.comp_2444())) continue;
            return true;
        }
        for (class_9336 dataComponent : prev.method_57353()) {
            if (dataComponent.comp_2443() == class_9334.field_49629 || now.method_57353().method_57832(dataComponent.comp_2443())) continue;
            return true;
        }
        return false;
    }

    private boolean isFunctionalSwapFromMainToOffhand(class_310 client, class_1799 currentMain) {
        if (client == null || client.field_1724 == null || this.lastMiningStack == null || this.lastMiningStack.method_7960()) {
            return false;
        }
        class_1799 currentOff = client.field_1724.method_6079();
        if (!class_1799.method_31577((class_1799)this.lastMiningStack, (class_1799)currentOff)) {
            return false;
        }
        return this.hasItemChanged(this.lastMiningStack, currentMain);
    }

    private AnimationClip resolveCustomMiningClip(class_310 client, class_1799 stack, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (stack == null || entry == null) {
            return null;
        }
        return CustomAnimationResolver.resolveNamedOrTypeFallback(stack, "mining", entry, client, class_1268.field_5808).clip();
    }

    private boolean hasMiningV2Entry(class_310 client, class_1799 stack) {
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "mining_v2");
        if (entry == null) {
            return false;
        }
        MiningV2Selection selection = this.resolveMiningV2Selection(client, stack, entry);
        return selection.isValid();
    }

    private void tickMiningV2(class_310 client, class_1799 stack, boolean rawMining, boolean mining, boolean triggered) {
        PunchyTuningConfig.CustomAnimationEntry activeEntry;
        boolean needsRestart;
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "mining_v2");
        MiningV2Selection selection = entry != null ? this.resolveMiningV2Selection(client, stack, entry) : MiningV2Selection.INVALID;
        boolean hasTrackableTarget = this.hasTrackableMiningTarget(client);
        if (triggered) {
            this.registerMiningV2Trigger();
        }
        if (this.miningV2Phase == MiningV2Phase.NONE) {
            if (!selection.isValid()) {
                if (mining) {
                    PunchyAnimationManager.POSE_HANDLER.setLooping(true);
                    if (!this.wasMining || !PunchyAnimationManager.POSE_HANDLER.isPlaying() || this.hasItemChanged(this.lastMiningStack, stack)) {
                        this.playPunch();
                    }
                }
                return;
            }
            if (!mining && !triggered) {
                return;
            }
        }
        boolean bl = needsRestart = this.miningV2Phase == MiningV2Phase.NONE || this.hasItemChanged(this.lastMiningV2Stack, stack) || this.miningV2Entry != entry;
        if (needsRestart) {
            if (!selection.isValid()) {
                this.resetMiningV2();
                return;
            }
            this.miningV2Entry = entry;
            this.stopImmediateOnRelease = CustomAnimationResolver.shouldStopImmediately(entry);
            this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(entry);
            this.miningV2StartClip = selection.start();
            this.miningV2LoopClip = selection.loop();
            this.miningV2EndClip = selection.end();
            this.startMiningV2(selection.start());
            this.lastMiningV2Stack = this.copyStack(stack);
            this.lastMiningStack = this.copyStack(stack);
            return;
        }
        PunchyTuningConfig.CustomAnimationEntry customAnimationEntry = activeEntry = entry != null ? entry : this.miningV2Entry;
        if (activeEntry != null) {
            CustomAnimationResolver.applyLoopSpeed(activeEntry);
            this.stopImmediateOnRelease = CustomAnimationResolver.shouldStopImmediately(activeEntry);
            this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(activeEntry);
        }
        if (mining && this.miningV2Phase == MiningV2Phase.END && selection.isValid()) {
            this.restartMiningV2FromTrigger(selection.start());
            this.lastMiningV2Stack = this.copyStack(stack);
            this.lastMiningStack = this.copyStack(stack);
            return;
        }
        if (triggered && this.miningV2Phase == MiningV2Phase.END) {
            this.restartMiningV2FromTrigger(selection.start());
            this.lastMiningV2Stack = this.copyStack(stack);
            this.lastMiningStack = this.copyStack(stack);
            return;
        }
        this.lastMiningV2Stack = this.copyStack(stack);
        this.lastMiningStack = this.copyStack(stack);
        PoseHandlerWrapper handler = new PoseHandlerWrapper();
        switch (this.miningV2Phase.ordinal()) {
            case 1: {
                this.applyMiningV2StartSpeed(selection.start());
                if (handler.isPlaying(selection.start())) break;
                if (this.isAttackDown(client) && mining) {
                    this.transitionToMiningV2Loop(selection.loop());
                    break;
                }
                this.transitionToMiningV2End(this.miningV2EndClip);
                break;
            }
            case 2: {
                if (!this.isAttackDown(client) || !mining) {
                    this.beginMiningV2LoopEnd();
                    break;
                }
                if (!handler.isPlaying(selection.loop())) {
                    this.transitionToMiningV2Loop(selection.loop());
                    break;
                }
                PunchyAnimationManager.POSE_HANDLER.setLooping(true);
                break;
            }
            case 3: {
                if (triggered) {
                    if (!mining) {
                        return;
                    }
                    this.restartMiningV2FromTrigger(selection.start());
                    break;
                }
                if (handler.isPlaying(selection.loop())) {
                    PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                    break;
                }
                this.transitionToMiningV2End(this.miningV2EndClip);
                break;
            }
            case 4: {
                if (handler.isPlaying(this.miningV2EndClip) || handler.isCurrentOrBlending(this.miningV2EndClip)) break;
                this.resetMiningV2();
                break;
            }
        }
    }

    private void startMiningV2(AnimationClip startClip) {
        class_1306 arm;
        if (startClip == null) {
            return;
        }
        this.activeArm = arm = PunchyAnimationManager.getMiningArm();
        this.activeClip = startClip;
        this.primeOppositeArmOverlapDuringEquip(class_310.method_1551(), class_1268.field_5808, arm);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = this.resolveMiningV2StartSpeed(startClip);
        boolean skipBlendIn = true;
        PunchyAnimationManager.POSE_HANDLER.setBlendInDurationNext(0.0f);
        PunchyAnimationManager.POSE_HANDLER.play(startClip, speedMultiplier, skipBlendIn);
        this.miningV2Phase = MiningV2Phase.START;
        this.lastTriggerTick = this.currentTick();
        this.minStopTick = this.lastTriggerTick + 2L;
    }

    private void restartMiningV2FromTrigger(AnimationClip startClip) {
        if (startClip == null) {
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.resetImmediate();
        this.startMiningV2(startClip);
    }

    private void transitionToMiningV2Loop(AnimationClip loopClip) {
        class_1306 arm;
        if (loopClip == null) {
            return;
        }
        this.activeArm = arm = PunchyAnimationManager.getMiningArm();
        this.activeClip = loopClip;
        this.primeOppositeArmOverlapDuringEquip(class_310.method_1551(), class_1268.field_5808, arm);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(true);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(loopClip);
        if (PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            PunchyAnimationManager.POSE_HANDLER.setBlendInDurationNext(0.06f);
            PunchyAnimationManager.POSE_HANDLER.setBlendInEasingNext(PoseHandler.BlendInEasing.SMOOTH);
            PunchyAnimationManager.POSE_HANDLER.playWithBlendFromCurrentDynamic(loopClip, speedMultiplier, false);
        } else {
            PunchyAnimationManager.POSE_HANDLER.play(loopClip, speedMultiplier, true);
        }
        this.miningV2Phase = MiningV2Phase.LOOP;
    }

    private void transitionToMiningV2End(AnimationClip endClip) {
        this.transitionToMiningV2End(endClip, PoseHandler.BlendInEasing.SMOOTH, 0.08f);
    }

    private void transitionToMiningV2End(AnimationClip endClip, PoseHandler.BlendInEasing easing, float blendDuration) {
        class_1306 arm;
        if (endClip == null) {
            this.resetMiningV2();
            return;
        }
        this.activeArm = arm = PunchyAnimationManager.getMiningArm();
        this.activeClip = endClip;
        this.primeOppositeArmOverlapDuringEquip(class_310.method_1551(), class_1268.field_5808, arm);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(endClip);
        if (PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            PunchyAnimationManager.POSE_HANDLER.setRebaseReturnSpeedNext(4.5f);
            PunchyAnimationManager.POSE_HANDLER.playAtTimeRebasedFromCurrentReturnToOrigin(endClip, speedMultiplier, false, 0.0f);
        } else {
            PunchyAnimationManager.POSE_HANDLER.play(endClip, speedMultiplier, true);
        }
        this.miningV2Phase = MiningV2Phase.END;
    }

    private void beginMiningV2LoopEnd() {
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        this.transitionToMiningV2End(this.miningV2EndClip, PoseHandler.BlendInEasing.BACK, 0.12f);
    }

    private void registerMiningV2Trigger() {
        long now = this.currentTick();
        float targetSpeed = 1.0f;
        if (this.miningV2LastTriggerTick >= 0L) {
            long intervalTicks = Math.max(1L, now - this.miningV2LastTriggerTick);
            float normalized = 1.0f - (float)(intervalTicks - 1L) / 5.0f;
            normalized = class_3532.method_15363((float)normalized, (float)0.0f, (float)1.0f);
            normalized *= normalized;
            targetSpeed = class_3532.method_16439((float)normalized, (float)1.0f, (float)2.35f);
        }
        this.miningV2StartSpeed = class_3532.method_16439((float)0.45f, (float)this.miningV2StartSpeed, (float)targetSpeed);
        this.miningV2StartSpeed = class_3532.method_15363((float)this.miningV2StartSpeed, (float)1.0f, (float)2.35f);
        this.miningV2LastTriggerTick = now;
    }

    private float resolveMiningV2StartSpeed(AnimationClip startClip) {
        return PunchyAnimationManager.clipSpeedMultiplier(startClip) * this.miningV2StartSpeed;
    }

    private void applyMiningV2StartSpeed(AnimationClip startClip) {
        if (startClip == null) {
            return;
        }
        if (PunchyAnimationManager.POSE_HANDLER.getCurrentClip() != startClip) {
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setSpeedMultiplier(this.resolveMiningV2StartSpeed(startClip));
    }

    private boolean shouldSkipBlendIn() {
        long now = this.currentTick();
        return this.lastTriggerTick > 0L && now - this.lastTriggerTick <= 4L;
    }

    private boolean shouldKeepMiningActive(boolean rawMining, class_310 client, long now) {
        if (rawMining) {
            this.stopGraceTicks = 0;
            return true;
        }
        if (this.wasMining && this.minStopTick >= 0L && now < this.minStopTick) {
            return true;
        }
        if (this.wasMining && this.isAttackDown(client) && this.isAimingTrackableBlockNow(client) && this.stopGraceTicks < 2) {
            ++this.stopGraceTicks;
            return true;
        }
        if (this.wasMining && this.isAttackDown(client) && this.hasVeryRecentMiningTarget() && this.stopGraceTicks < 1) {
            ++this.stopGraceTicks;
            return true;
        }
        if (this.wasMining && this.isAttackDown(client) && this.hasRecentMiningChain() && this.stopGraceTicks < 2) {
            ++this.stopGraceTicks;
            return true;
        }
        return false;
    }

    private boolean isAttackDown(class_310 client) {
        return client.field_1690 != null && client.field_1690.field_1886.method_1434();
    }

    private boolean isAimingTrackableBlockNow(class_310 client) {
        if (client == null || client.field_1687 == null || client.field_1765 == null) {
            return false;
        }
        class_239 hit = client.field_1765;
        if (hit.method_17783() != class_239.class_240.field_1332 || !(hit instanceof class_3965)) {
            return false;
        }
        class_3965 blockHit = (class_3965)hit;
        class_2680 state = client.field_1687.method_8320(blockHit.method_17777());
        return state != null && !state.method_26215();
    }

    private boolean hasVeryRecentMiningTarget() {
        return MiningActionTracker.peekRecentTarget(90000000L) != null;
    }

    private boolean hasRecentMiningChain() {
        return MiningActionTracker.wasRecentMining(180000000L);
    }

    private void stopMiningAnimation(class_310 client, boolean forceReset) {
        if (!forceReset && this.miningV2Phase != MiningV2Phase.NONE) {
            this.transitionToMiningV2End(this.miningV2EndClip);
            return;
        }
        if (this.ownsActiveClip()) {
            if (this.skipBlendOut) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
            }
            if (this.stopImmediateOnRelease && !this.isAttackDown(client)) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.3f);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                return;
            }
            PunchyAnimationManager.POSE_HANDLER.requestStopAfterCurrent();
        }
    }

    private void clearMiningState() {
        this.stopImmediateOnRelease = false;
        this.skipBlendOut = false;
        this.activeClip = null;
        this.activeArm = null;
        this.lastMiningStack = class_1799.field_8037;
        this.minStopTick = -1L;
        this.stopGraceTicks = 0;
        this.lastTriggerTick = -1L;
    }

    private long currentTick() {
        class_310 mc = class_310.method_1551();
        return mc.field_1687 == null ? 0L : mc.field_1687.method_8510();
    }

    private void resetMiningV2() {
        this.miningV2Phase = MiningV2Phase.NONE;
        this.miningV2Entry = null;
        this.miningV2StartClip = null;
        this.miningV2LoopClip = null;
        this.miningV2EndClip = null;
        this.activeClip = null;
        this.lastMiningV2Stack = class_1799.field_8037;
        this.miningV2LastTriggerTick = -1L;
        this.miningV2StartSpeed = 1.0f;
    }

    private boolean ownsActiveClip() {
        return PunchyAnimationManager.POSE_HANDLER.isCurrentClipOwnedBy(this.activeClip, this.activeArm);
    }

    private boolean shouldAbortEquipForMainHand(class_310 client) {
        if (client == null) {
            return true;
        }
        class_1306 equipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (equipArm == null) {
            return false;
        }
        class_1306 actionArm = PunchyAnimationManager.getUseArm(client, class_1268.field_5808);
        return actionArm == null || actionArm == equipArm;
    }

    private void primeOppositeArmOverlapDuringEquip(class_310 client, class_1268 hand, class_1306 actionArm) {
        if (client == null || hand == null || actionArm == null) {
            return;
        }
        if (!HandEquipStateMachine.isEquipInProgress()) {
            return;
        }
        if (hand == class_1268.field_5808) {
            HandEquipStateMachine.prepareOffhandForMainhandAction(client);
        }
        if (PunchyAnimationManager.POSE_HANDLER.getActiveArm() != null) {
            return;
        }
        class_1306 opposite = actionArm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(opposite);
    }

    private void clearMiningForEquipTransition() {
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "mining");
        this.wasMining = false;
        this.clearMiningState();
        this.resetMiningV2();
    }

    private MiningV2Selection resolveMiningV2Selection(class_310 client, class_1799 stack, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (client == null || entry == null) {
            return MiningV2Selection.INVALID;
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForEntry(stack, entry);
        AnimationClip start = this.resolveNamedClip(CustomAnimationResolver.resolveStartName(entry), client, preferredOption);
        AnimationClip loop = this.resolveNamedClip(CustomAnimationResolver.resolveLoopingName(entry), client, preferredOption);
        AnimationClip end = this.resolveNamedClip(CustomAnimationResolver.resolveEndName(entry), client, preferredOption);
        if (start == null || loop == null || end == null) {
            return MiningV2Selection.INVALID;
        }
        return new MiningV2Selection(start, loop, end);
    }

    private AnimationClip resolveNamedClip(String name, class_310 client, String preferredOption) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return PunchyAnimationManager.resolveNamedClip(name, client, class_1268.field_5808, preferredOption);
    }

    private boolean hasTrackableMiningTarget(class_310 client) {
        if (client == null || client.field_1687 == null || client.field_1765 == null) {
            return MiningActionTracker.peekRecentTarget(250000000L) != null;
        }
        class_239 hit = client.field_1765;
        if (hit.method_17783() != class_239.class_240.field_1332 || !(hit instanceof class_3965)) {
            return MiningActionTracker.peekRecentTarget(250000000L) != null;
        }
        class_3965 blockHit = (class_3965)hit;
        class_2680 state = client.field_1687.method_8320(blockHit.method_17777());
        if (state != null && !state.method_26215()) {
            return true;
        }
        return MiningActionTracker.peekRecentTarget(250000000L) != null;
    }

    private static enum MiningV2Phase {
        NONE,
        START,
        LOOP,
        LOOP_END_PENDING,
        END;

    }

    private record MiningV2Selection(AnimationClip start, AnimationClip loop, AnimationClip end) {
        private static final MiningV2Selection INVALID = new MiningV2Selection(null, null, null);

        private boolean isValid() {
            return this.start != null && this.loop != null && this.end != null;
        }
    }

    private static final class PoseHandlerWrapper {
        private PoseHandlerWrapper() {
        }

        private boolean isPlaying(AnimationClip clip) {
            return clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() == clip;
        }

        private boolean isCurrentOrBlending(AnimationClip clip) {
            return clip != null && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() == clip;
        }
    }
}

