/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1839
 *  net.minecraft.class_310
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_310;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.UseActionTracker;
import punchy.config.PunchyTuningConfig;

public class ConsumeStateMachine {
    private static volatile boolean equipBlockedByConsumeAnimation;
    private static volatile boolean suppressUseAfterConsume;
    private static volatile class_1306 forcedRenderArm;
    private static volatile class_1799 forcedRenderStack;
    private boolean wasConsuming;
    private boolean finishingConsume;
    private boolean swapOnLastFramePending;
    private boolean swappedOnLastFrame;
    private AnimationClip activeClip;
    private class_1268 activeHand;
    private boolean skipBlendOut;
    private ConsumeV2Phase consumeV2Phase = ConsumeV2Phase.NONE;
    private AnimationClip consumeV2StartClip;
    private AnimationClip consumeV2LoopClip;
    private AnimationClip consumeV2EndClip;
    private PunchyTuningConfig.CustomAnimationEntry consumeV2Entry;
    private class_1799 lastConsumeV2Stack = class_1799.field_8037;

    public void tick(class_310 client) {
        class_1268 consumeHand;
        boolean consuming;
        boolean useDown;
        if (client == null || client.field_1724 == null) {
            equipBlockedByConsumeAnimation = false;
            ConsumeStateMachine.clearForcedRenderStack();
            return;
        }
        boolean bl = useDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
        if (!useDown) {
            suppressUseAfterConsume = false;
        }
        boolean usingItem = client.field_1724.method_6115();
        class_1799 usingStack = client.field_1724.method_6030();
        boolean bl2 = consuming = usingItem && useDown && ConsumeStateMachine.isConsumeStack(usingStack);
        if (this.finishingConsume) {
            boolean stillFinishing;
            if (consuming) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                this.finishingConsume = false;
                this.swapOnLastFramePending = false;
                this.swappedOnLastFrame = false;
                this.activeClip = null;
                this.skipBlendOut = false;
                this.resetConsumeV2();
                PunchyAnimationManager.clearSourceHand();
            }
            if (this.shouldReleaseFinishingForEquip(client)) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                this.finishingConsume = false;
                this.swapOnLastFramePending = false;
                this.swappedOnLastFrame = false;
                this.activeClip = null;
                this.skipBlendOut = false;
                this.resetConsumeV2();
                PunchyAnimationManager.clearSourceHand();
                ConsumeStateMachine.clearForcedRenderStack();
                equipBlockedByConsumeAnimation = false;
            }
            UseActionTracker.clear();
            AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
            boolean bl3 = stillFinishing = current != null && this.activeClip != null && current == this.activeClip && (PunchyAnimationManager.POSE_HANDLER.isPlaying() || PunchyAnimationManager.POSE_HANDLER.isBlendOutActive());
            if (stillFinishing) {
                if (this.swapOnLastFramePending && !this.swappedOnLastFrame) {
                    float time = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
                    float length = this.activeClip.getLength();
                    if (length <= 0.0f || time >= length - 1.0E-4f) {
                        ConsumeStateMachine.clearForcedRenderStack();
                        HandEquipStateMachine.requestRenderedSync();
                        this.swappedOnLastFrame = true;
                    }
                }
                equipBlockedByConsumeAnimation = true;
                return;
            }
            this.finishingConsume = false;
            this.swapOnLastFramePending = false;
            this.swappedOnLastFrame = false;
            this.activeClip = null;
            this.activeHand = null;
            this.skipBlendOut = false;
            this.resetConsumeV2();
            PunchyAnimationManager.clearSourceHand();
            ConsumeStateMachine.clearForcedRenderStack();
            suppressUseAfterConsume = true;
        }
        if ((consumeHand = client.field_1724.method_6058()) == null) {
            consumeHand = class_1268.field_5808;
        }
        if (consuming && this.wasConsuming && this.activeHand != null && consumeHand != this.activeHand) {
            this.interruptConsumeForHandSwap(client, consumeHand);
            return;
        }
        if (consuming && HandEquipStateMachine.isEquipInProgress() && !HandEquipStateMachine.abortEquipForAction(client, consumeHand)) {
            return;
        }
        if (consuming) {
            UseActionTracker.clear();
            suppressUseAfterConsume = true;
            equipBlockedByConsumeAnimation = true;
            class_1268 hand = consumeHand;
            if (PunchyAnimationManager.isHandBlacklisted(client, hand)) {
                if (this.wasConsuming) {
                    PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                    PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                    PunchyAnimationManager.clearSourceHand();
                }
                this.wasConsuming = false;
                this.finishingConsume = false;
                this.activeClip = null;
                this.activeHand = null;
                equipBlockedByConsumeAnimation = false;
                this.resetConsumeV2();
                ConsumeStateMachine.clearForcedRenderStack();
                return;
            }
            ConsumeSelection selection = this.resolveConsumeClip(usingStack);
            if (selection.isV2()) {
                this.tickConsumeV2(client, hand, usingStack, selection);
            } else {
                AnimationClip clip = selection.clip;
                if (!(clip == null || this.wasConsuming && this.activeClip == clip && PunchyAnimationManager.POSE_HANDLER.isPlaying())) {
                    this.playConsume(client, hand, usingStack, clip, selection.entry);
                }
                if (this.consumeV2Phase != ConsumeV2Phase.NONE) {
                    this.resetConsumeV2();
                }
            }
            this.wasConsuming = true;
            this.finishingConsume = false;
            this.swapOnLastFramePending = false;
            this.swappedOnLastFrame = false;
            this.activeHand = hand;
            return;
        }
        if (this.wasConsuming) {
            boolean consumeCompleted;
            boolean functionalSwapDuringConsume = this.isFunctionalSwapDuringConsume(client);
            boolean bl4 = consumeCompleted = !usingItem && useDown && !functionalSwapDuringConsume;
            if (this.consumeV2Phase != ConsumeV2Phase.NONE && this.consumeV2EndClip != null) {
                this.transitionToConsumeV2End(this.activeHand);
                this.finishingConsume = true;
                this.swapOnLastFramePending = consumeCompleted;
                this.swappedOnLastFrame = false;
                equipBlockedByConsumeAnimation = true;
                suppressUseAfterConsume = true;
            } else if (consumeCompleted) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                if (this.skipBlendOut) {
                    PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
                }
                PunchyAnimationManager.POSE_HANDLER.releaseHold();
                PunchyAnimationManager.POSE_HANDLER.requestStopAfterCurrent();
                this.finishingConsume = true;
                this.swapOnLastFramePending = true;
                this.swappedOnLastFrame = false;
                equipBlockedByConsumeAnimation = true;
                suppressUseAfterConsume = true;
            } else {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                if (this.skipBlendOut) {
                    PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
                }
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                this.finishingConsume = false;
                this.swapOnLastFramePending = false;
                this.swappedOnLastFrame = false;
                equipBlockedByConsumeAnimation = false;
                this.resetConsumeV2();
                ConsumeStateMachine.clearForcedRenderStack();
                if (functionalSwapDuringConsume) {
                    HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client.field_1724);
                    HandEquipStateMachine.requestRenderedSync();
                }
            }
        }
        this.wasConsuming = false;
        if (!this.finishingConsume) {
            this.swapOnLastFramePending = false;
            this.swappedOnLastFrame = false;
            equipBlockedByConsumeAnimation = false;
            this.resetConsumeV2();
            ConsumeStateMachine.clearForcedRenderStack();
        }
    }

    public static boolean isEquipBlockedByConsumeAnimation() {
        return equipBlockedByConsumeAnimation;
    }

    public static boolean isUseSuppressedAfterConsume() {
        return suppressUseAfterConsume;
    }

    public static class_1799 getForcedRenderStack(class_1306 arm) {
        if (arm == null) {
            return class_1799.field_8037;
        }
        if (forcedRenderArm != arm || forcedRenderStack == null || forcedRenderStack.method_7960()) {
            return class_1799.field_8037;
        }
        return forcedRenderStack;
    }

    public static String getForcedConsumeTypeForArm(class_1306 arm) {
        if (arm == null) {
            return null;
        }
        if (forcedRenderArm != arm || forcedRenderStack == null || forcedRenderStack.method_7960()) {
            return null;
        }
        class_1839 useAnim = forcedRenderStack.method_7976();
        if (useAnim == class_1839.field_8946) {
            return "useDrink";
        }
        if (useAnim == class_1839.field_8950) {
            return "useEat";
        }
        return null;
    }

    private void updateForcedRenderStack(class_1268 hand, class_1799 sourceStack) {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null || hand == null) {
            return;
        }
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        class_1799 stack = sourceStack;
        if (stack == null || stack.method_7960()) {
            return;
        }
        forcedRenderArm = arm;
        forcedRenderStack = stack.method_7972();
    }

    private static void clearForcedRenderStack() {
        forcedRenderArm = null;
        forcedRenderStack = class_1799.field_8037;
    }

    private void playConsume(class_310 client, class_1268 hand, class_1799 sourceStack, AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry) {
        PunchyAnimationManager.setSourceHand(client, hand);
        this.updateForcedRenderStack(hand, sourceStack);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose() || entry == null || CustomAnimationResolver.shouldSkipBlendIn(entry);
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn, clip.isHoldOnLastFrame());
        this.activeClip = clip;
        this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(entry);
    }

    private void tickConsumeV2(class_310 client, class_1268 hand, class_1799 stack, ConsumeSelection selection) {
        boolean needsRestart;
        if (!selection.isV2Valid()) {
            this.resetConsumeV2();
            return;
        }
        boolean bl = needsRestart = this.consumeV2Phase == ConsumeV2Phase.NONE || this.consumeV2Entry != selection.entry || this.hasConsumeStackChanged(this.lastConsumeV2Stack, stack);
        if (needsRestart) {
            this.consumeV2Entry = selection.entry;
            this.consumeV2StartClip = selection.start;
            this.consumeV2LoopClip = selection.loop;
            this.consumeV2EndClip = selection.end;
            this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(selection.entry);
            this.startConsumeV2(client, hand, stack, this.consumeV2StartClip, selection.entry);
            this.lastConsumeV2Stack = this.copyStack(stack);
            return;
        }
        this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(selection.entry);
        PoseHandlerWrapper handler = new PoseHandlerWrapper();
        switch (this.consumeV2Phase.ordinal()) {
            case 1: {
                if (handler.isPlaying(this.consumeV2StartClip)) break;
                this.transitionToConsumeV2Loop(client, hand, this.consumeV2LoopClip, selection.entry);
                break;
            }
            case 2: {
                if (!handler.isPlaying(this.consumeV2LoopClip)) {
                    this.transitionToConsumeV2Loop(client, hand, this.consumeV2LoopClip, selection.entry);
                    break;
                }
                PunchyAnimationManager.POSE_HANDLER.setLooping(true);
                break;
            }
            case 3: {
                if (handler.isPlaying(this.consumeV2EndClip) || handler.isCurrentOrBlending(this.consumeV2EndClip)) break;
                this.resetConsumeV2();
                break;
            }
        }
        this.lastConsumeV2Stack = this.copyStack(stack);
    }

    private void startConsumeV2(class_310 client, class_1268 hand, class_1799 sourceStack, AnimationClip startClip, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (client == null || startClip == null) {
            return;
        }
        PunchyAnimationManager.setSourceHand(client, hand);
        this.updateForcedRenderStack(hand, sourceStack);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(startClip);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose() || entry == null || CustomAnimationResolver.shouldSkipBlendIn(entry);
        PunchyAnimationManager.POSE_HANDLER.play(startClip, speedMultiplier, skipBlendIn, false);
        this.activeClip = startClip;
        this.consumeV2Phase = ConsumeV2Phase.START;
    }

    private void transitionToConsumeV2Loop(class_310 client, class_1268 hand, AnimationClip loopClip, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (client == null || loopClip == null) {
            return;
        }
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(true);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(loopClip);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose() || entry == null || CustomAnimationResolver.shouldSkipBlendIn(entry);
        PunchyAnimationManager.POSE_HANDLER.setBlendInDurationNext(0.06f);
        PunchyAnimationManager.POSE_HANDLER.play(loopClip, speedMultiplier, skipBlendIn, false);
        this.activeClip = loopClip;
        this.consumeV2Phase = ConsumeV2Phase.LOOP;
    }

    private void transitionToConsumeV2End(class_1268 hand) {
        if (this.consumeV2EndClip == null) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client != null) {
            PunchyAnimationManager.setSourceHand(client, hand == null ? class_1268.field_5808 : hand);
            class_1306 arm = PunchyAnimationManager.getUseArm(client, hand == null ? class_1268.field_5808 : hand);
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        }
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(this.consumeV2EndClip);
        PunchyAnimationManager.POSE_HANDLER.setBlendInDurationNext(0.0f);
        PunchyAnimationManager.POSE_HANDLER.play(this.consumeV2EndClip, speedMultiplier, true, false);
        this.activeClip = this.consumeV2EndClip;
        this.consumeV2Phase = ConsumeV2Phase.END;
    }

    private boolean hasConsumeStackChanged(class_1799 previous, class_1799 current) {
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

    private void resetConsumeV2() {
        this.consumeV2Phase = ConsumeV2Phase.NONE;
        this.consumeV2StartClip = null;
        this.consumeV2LoopClip = null;
        this.consumeV2EndClip = null;
        this.consumeV2Entry = null;
        this.lastConsumeV2Stack = class_1799.field_8037;
    }

    public static boolean isConsumeStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (ConsumeStateMachine.isThrowableDrinkLikeStack(stack)) {
            return false;
        }
        class_1839 useAnim = stack.method_7976();
        return useAnim == class_1839.field_8950 || useAnim == class_1839.field_8946;
    }

    private static boolean isThrowableDrinkLikeStack(class_1799 stack) {
        return stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150) || stack.method_31574(class_1802.field_8287) || stack.method_31574(class_1802.field_8543) || stack.method_31574(class_1802.field_8803) || stack.method_31574(class_1802.field_8634) || stack.method_31574(class_1802.field_8449);
    }

    private ConsumeSelection resolveConsumeClip(class_1799 stack) {
        ConsumeSelection v2Selection;
        String targetType;
        CustomAnimationResolver.ClipLookup lookup;
        String targetType2;
        CustomAnimationResolver.ClipLookup lookup2;
        ConsumeSelection v2Selection2;
        PunchyTuningConfig.CustomAnimationEntry v2EntryExplicit;
        class_1268 hand;
        if (stack == null || stack.method_7960()) {
            return ConsumeSelection.single(null, null);
        }
        class_1839 useAnim = stack.method_7976();
        class_310 client = class_310.method_1551();
        class_1268 class_12682 = hand = client == null || client.field_1724 == null ? class_1268.field_5808 : client.field_1724.method_6058();
        if (hand == null) {
            hand = class_1268.field_5808;
        }
        if ((v2EntryExplicit = this.findConsumeV2Entry(stack, useAnim, false)) != null && (v2Selection2 = this.resolveConsumeV2Selection(client, hand, stack, v2EntryExplicit)).isV2Valid()) {
            return v2Selection2;
        }
        PunchyTuningConfig.CustomAnimationEntry entryExplicit = this.findConsumeLegacyEntry(stack, useAnim, false);
        if (entryExplicit != null && (lookup2 = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, targetType2 = useAnim == class_1839.field_8950 ? "useEat" : "useDrink", entryExplicit, client, hand)).clip() != null) {
            return ConsumeSelection.single(lookup2.clip(), entryExplicit);
        }
        PunchyTuningConfig.CustomAnimationEntry entryMerged = this.findConsumeLegacyEntry(stack, useAnim, true);
        if (entryMerged != null && (lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, targetType = useAnim == class_1839.field_8950 ? "useEat" : "useDrink", entryMerged, client, hand)).clip() != null) {
            return ConsumeSelection.single(lookup.clip(), entryMerged);
        }
        PunchyTuningConfig.CustomAnimationEntry v2EntryMerged = this.findConsumeV2Entry(stack, useAnim, true);
        if (v2EntryMerged != null && (v2Selection = this.resolveConsumeV2Selection(client, hand, stack, v2EntryMerged)).isV2Valid()) {
            return v2Selection;
        }
        if (useAnim == class_1839.field_8950) {
            return ConsumeSelection.single(PunchyAnimationManager.resolveHandedClip("use_eat", client, hand), null);
        }
        if (useAnim == class_1839.field_8946) {
            return ConsumeSelection.single(PunchyAnimationManager.resolveHandedClip("use_drink", client, hand), null);
        }
        return ConsumeSelection.single(null, null);
    }

    private PunchyTuningConfig.CustomAnimationEntry findConsumeV2Entry(class_1799 stack, class_1839 useAnim, boolean includeDefaults) {
        if (useAnim == class_1839.field_8950) {
            PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "eating", includeDefaults);
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "eating_v2", includeDefaults);
            }
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "useitem_v2", includeDefaults);
            }
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "useeat_v2", includeDefaults);
            }
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "use_eat_v2", includeDefaults);
            }
            return entry;
        }
        if (useAnim == class_1839.field_8946) {
            PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "drinking", includeDefaults);
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "drinking_v2", includeDefaults);
            }
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "useitem_v2", includeDefaults);
            }
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "usedrink_v2", includeDefaults);
            }
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "use_drink_v2", includeDefaults);
            }
            return entry;
        }
        return null;
    }

    private PunchyTuningConfig.CustomAnimationEntry findConsumeLegacyEntry(class_1799 stack, class_1839 useAnim, boolean includeDefaults) {
        if (useAnim == class_1839.field_8950) {
            PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "useEat", includeDefaults);
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "use_eat", includeDefaults);
            }
            return entry;
        }
        if (useAnim == class_1839.field_8946) {
            PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "useDrink", includeDefaults);
            if (entry == null) {
                entry = CustomAnimationResolver.findEntry(stack, "use_drink", includeDefaults);
            }
            return entry;
        }
        return null;
    }

    public boolean isActive() {
        return this.wasConsuming && this.activeClip != null;
    }

    private boolean shouldReleaseFinishingForEquip(class_310 client) {
        class_1799 reference;
        if (!this.finishingConsume || client == null || client.field_1724 == null || this.activeHand == null) {
            return false;
        }
        class_1799 class_17992 = reference = forcedRenderStack == null ? class_1799.field_8037 : forcedRenderStack;
        if (reference.method_7960()) {
            return false;
        }
        class_1799 current = client.field_1724.method_5998(this.activeHand);
        class_1799 now = current == null ? class_1799.field_8037 : current;
        return !class_1799.method_31577((class_1799)reference, (class_1799)now);
    }

    private void interruptConsumeForHandSwap(class_310 client, class_1268 newHand) {
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        PunchyAnimationManager.clearSourceHand();
        this.wasConsuming = false;
        this.finishingConsume = false;
        this.swapOnLastFramePending = false;
        this.swappedOnLastFrame = false;
        this.activeClip = null;
        this.activeHand = null;
        this.skipBlendOut = false;
        suppressUseAfterConsume = false;
        equipBlockedByConsumeAnimation = false;
        this.resetConsumeV2();
        ConsumeStateMachine.clearForcedRenderStack();
        HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client.field_1724);
        HandEquipStateMachine.requestRenderedSync();
        if (newHand == class_1268.field_5810) {
            HandEquipStateMachine.triggerPickupEquipCycle(client, class_1268.field_5810);
        }
    }

    private boolean isFunctionalSwapDuringConsume(class_310 client) {
        class_1799 reference;
        if (client == null || client.field_1724 == null || this.activeHand == null) {
            return false;
        }
        class_1799 class_17992 = reference = forcedRenderStack == null ? class_1799.field_8037 : forcedRenderStack;
        if (reference.method_7960()) {
            return false;
        }
        class_1799 activeNow = client.field_1724.method_5998(this.activeHand);
        class_1268 oppositeHand = this.activeHand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
        class_1799 oppositeNow = client.field_1724.method_5998(oppositeHand);
        return !class_1799.method_31577((class_1799)reference, (class_1799)activeNow) && class_1799.method_31577((class_1799)reference, (class_1799)oppositeNow);
    }

    private ConsumeSelection resolveConsumeV2Selection(class_310 client, class_1268 hand, class_1799 stack, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (client == null || entry == null) {
            return ConsumeSelection.single(null, null);
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForEntry(stack, entry);
        AnimationClip start = this.resolveNamedClip(CustomAnimationResolver.resolveStartName(entry), client, hand, preferredOption);
        AnimationClip loop = this.resolveNamedClip(CustomAnimationResolver.resolveLoopingName(entry), client, hand, preferredOption);
        AnimationClip end = this.resolveNamedClip(CustomAnimationResolver.resolveEndName(entry), client, hand, preferredOption);
        return ConsumeSelection.v2(start, loop, end, entry);
    }

    private AnimationClip resolveNamedClip(String name, class_310 client, class_1268 hand, String preferredOption) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return PunchyAnimationManager.resolveNamedClip(name, client, hand, preferredOption);
    }

    static {
        forcedRenderStack = class_1799.field_8037;
    }

    private static enum ConsumeV2Phase {
        NONE,
        START,
        LOOP,
        END;

    }

    private record ConsumeSelection(AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry, AnimationClip start, AnimationClip loop, AnimationClip end) {
        private static ConsumeSelection single(AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry) {
            return new ConsumeSelection(clip, entry, null, null, null);
        }

        private static ConsumeSelection v2(AnimationClip start, AnimationClip loop, AnimationClip end, PunchyTuningConfig.CustomAnimationEntry entry) {
            return new ConsumeSelection(start, entry, start, loop, end);
        }

        private boolean isV2() {
            return this.start != null || this.loop != null || this.end != null;
        }

        private boolean isV2Valid() {
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

