/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1787
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1835
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3965
 *  net.minecraft.class_9362
 */
package punchy.client.state;

import java.util.Locale;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1835;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_9362;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.ToolKindResolver;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.ConsumeStateMachine;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.InterfaceDelayState;
import punchy.client.state.SpearStateMachine;
import punchy.client.state.UseActionTracker;
import punchy.compat.BetterCombatCompat;
import punchy.compat.SwordBlockingMechanicsCompat;
import punchy.config.PunchyTuningConfig;
import punchy.mixin.client.accessor.PlayerFishingAccessor;

public class UseItemStateMachine {
    private static final long FAST_TRIGGER_WINDOW_TICKS = 4L;
    private static final float STOP_IMMEDIATE_BLEND_OUT_SECONDS = 0.3f;
    private static class_1799 pendingThrowableMain = class_1799.field_8037;
    private static class_1799 pendingThrowableOff = class_1799.field_8037;
    private static class_1799 pendingDropMain = class_1799.field_8037;
    private static class_1799 pendingDropOff = class_1799.field_8037;
    private static class_1799 pendingBoneMealUseOnMain = class_1799.field_8037;
    private static class_1799 pendingBoneMealUseOnOff = class_1799.field_8037;
    private static class_1268 pendingSwordBlockingCompatHand;
    private static long visualUseItemEpoch;
    private boolean wasUseDown;
    private boolean wasUsing;
    private long lastTriggerTick = -1L;
    private boolean holdOnLastFrame;
    private AnimationClip activeClip;
    private boolean stopImmediate;
    private boolean skipBlendOut;
    private boolean swordBlockingSuppressed;
    private class_1268 lastSwordBlockingHand;

    public void tick(class_310 client) {
        class_1268 activeHand;
        boolean using;
        class_1799 triggerStack;
        boolean usePressed;
        class_1268 usedHand;
        if (client == null || client.field_1724 == null || client.field_1761 == null) {
            return;
        }
        if (ConsumeStateMachine.isUseSuppressedAfterConsume()) {
            UseItemStateMachine.clearPendingThrowableSnapshot(null);
            UseActionTracker.clear();
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
            this.holdOnLastFrame = false;
            this.activeClip = null;
            this.stopImmediate = false;
            this.skipBlendOut = false;
            this.wasUsing = false;
            this.swordBlockingSuppressed = false;
            this.lastSwordBlockingHand = null;
            this.wasUseDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
            return;
        }
        if (ConsumeStateMachine.isEquipBlockedByConsumeAnimation()) {
            UseItemStateMachine.clearPendingThrowableSnapshot(null);
            UseActionTracker.clear();
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
            this.holdOnLastFrame = false;
            this.activeClip = null;
            this.stopImmediate = false;
            this.skipBlendOut = false;
            this.wasUsing = false;
            this.swordBlockingSuppressed = false;
            this.lastSwordBlockingHand = null;
            this.wasUseDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
            return;
        }
        if (client.field_1724.method_6115() && PunchyAnimationManager.isHandBlacklisted(client, usedHand = client.field_1724.method_6058())) {
            this.resetState();
            return;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        if (ConsumeStateMachine.isConsumeStack(usingStack)) {
            this.resetState();
            return;
        }
        if (this.isBowUseActive(client) || this.isCrossbowUseActive(client) || this.isSpearUseActive(client)) {
            this.resetPoseStateOnly();
            return;
        }
        if (this.shouldStopSwordBlockingCompat(client)) {
            this.stopSwordBlockingCompat(client);
            return;
        }
        boolean usingItem = client.field_1724.method_6115();
        boolean useDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
        boolean bl = usePressed = useDown && !this.wasUseDown;
        if (!useDown) {
            this.swordBlockingSuppressed = false;
        }
        boolean forceSwordBlockHold = useDown && this.activeClip != null && PunchyAnimationManager.isClipNamed(this.activeClip, "sword_blocking_compat");
        class_1268 swordBlockingCompatHand = UseItemStateMachine.consumePendingSwordBlockingCompatHand();
        if (!this.swordBlockingSuppressed && swordBlockingCompatHand != null && SwordBlockingMechanicsCompat.shouldUseBlockingAnimation(swordBlockingCompatHand)) {
            if (PunchyAnimationManager.isHandBlacklisted(client, swordBlockingCompatHand)) {
                this.resetState();
                this.wasUseDown = useDown;
                return;
            }
            if (HandEquipStateMachine.isEquipInProgress() && !HandEquipStateMachine.abortEquipForAction(client, swordBlockingCompatHand)) {
                pendingSwordBlockingCompatHand = swordBlockingCompatHand;
                this.wasUseDown = useDown;
                return;
            }
            if (this.playPendingSwordBlockingCompat(client, swordBlockingCompatHand)) {
                this.wasUsing = true;
            }
            this.wasUseDown = useDown;
            return;
        }
        UseActionTracker.Trigger trigger = UseActionTracker.consumeLatestTrigger();
        if (trigger != null && trigger.fallback() && !this.shouldUseFallbackTrigger(client, trigger)) {
            trigger = UseActionTracker.consumeTrigger();
        }
        class_1268 triggerHand = trigger == null ? null : trigger.hand();
        UseActionTracker.TriggerType triggerType = trigger == null ? null : trigger.type();
        class_3965 triggerBlockHit = trigger == null ? null : trigger.blockHit();
        class_2680 triggerBlockState = this.resolveBlockState(client, triggerBlockHit);
        if (this.swordBlockingSuppressed && useDown) {
            this.clearUseItemState(false);
            return;
        }
        if (triggerHand != null && PunchyAnimationManager.isHandBlacklisted(client, triggerHand)) {
            this.resetState();
            this.wasUseDown = useDown;
            return;
        }
        if (triggerHand != null && ConsumeStateMachine.isConsumeStack(triggerStack = client.field_1724.method_5998(triggerHand)) && !this.isInteractionAwareTrigger(triggerType)) {
            this.wasUsing = false;
            return;
        }
        boolean bl2 = using = triggerHand != null || usingItem && useDown || forceSwordBlockHold;
        if (this.holdOnLastFrame && this.activeClip != null && useDown) {
            using = true;
        }
        class_1268 class_12682 = triggerHand != null ? triggerHand : (activeHand = usingItem ? client.field_1724.method_6058() : class_1268.field_5808);
        if (using && HandEquipStateMachine.isEquipInProgress() && this.shouldAbortEquipForHand(client, activeHand) && !HandEquipStateMachine.abortEquipForAction(client, activeHand)) {
            if (trigger != null) {
                UseActionTracker.pushBack(trigger);
            }
            return;
        }
        if (using) {
            class_1268 usedHandNow;
            class_1268 class_12683 = usedHandNow = usingItem ? client.field_1724.method_6058() : null;
            if (this.activeClip != null && PunchyAnimationManager.isClipNamed(this.activeClip, "sword_blocking_compat") && usedHandNow != null && this.lastSwordBlockingHand != null && this.lastSwordBlockingHand != usedHandNow) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                PunchyAnimationManager.clearSourceHand();
                HandEquipStateMachine.requestRenderedSync();
                this.activeClip = null;
                this.holdOnLastFrame = false;
                this.stopImmediate = false;
            }
            if (PunchyAnimationManager.isHandBlacklisted(client, activeHand)) {
                this.resetState();
                this.wasUseDown = useDown;
                return;
            }
            if (forceSwordBlockHold) {
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
                ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
            } else {
                boolean sustainedUseFlow;
                boolean bl3 = sustainedUseFlow = usingItem && useDown;
                Boolean useItemLoopOverride = sustainedUseFlow ? !this.holdOnLastFrame : Boolean.FALSE;
                ItemAnimationManager.setPersistentType(activeHand, "useItem", useItemLoopOverride, true);
            }
            if (triggerHand != null && !this.shouldIgnoreTrigger(client, triggerHand, triggerType, triggerBlockState)) {
                UseActionTracker.discardOppositeOlderThan(triggerHand, trigger.recordNano());
                this.playInteract(client, triggerHand, triggerType, triggerBlockState, trigger.fallback());
            } else if (this.shouldForceSwordBlockingPlay(client)) {
                this.playInteract(client, activeHand, null, null, false);
            } else if (!PunchyAnimationManager.POSE_HANDLER.isPlaying() || this.shouldPlayUseDuringOffhandEquip(client, activeHand, usingItem, usedHandNow)) {
                this.playInteract(client, usingItem ? client.field_1724.method_6058() : class_1268.field_5808, null, null, false);
            }
        } else if (this.wasUsing) {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            if (this.skipBlendOut) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
            }
            if (this.holdOnLastFrame) {
                if (!useDown) {
                    PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                }
            } else if (this.stopImmediate) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.3f);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            } else {
                PunchyAnimationManager.POSE_HANDLER.requestStopAfterCurrent();
            }
            this.clearUseItemState(false);
        } else {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
        }
        this.wasUseDown = useDown;
        this.wasUsing = using;
        this.lastSwordBlockingHand = using && this.activeClip != null && PunchyAnimationManager.isClipNamed(this.activeClip, "sword_blocking_compat") ? (usingItem ? client.field_1724.method_6058() : this.lastSwordBlockingHand) : null;
    }

    private void resetState() {
        if (this.wasUsing) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            if (this.stopImmediate) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.3f);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            } else {
                PunchyAnimationManager.POSE_HANDLER.requestStopAfterCurrent();
            }
        }
        this.clearUseItemState(true);
    }

    private void resetPoseStateOnly() {
        if (this.wasUsing) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            if (this.stopImmediate) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.3f);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            } else {
                PunchyAnimationManager.POSE_HANDLER.requestStopAfterCurrent();
            }
        }
        this.holdOnLastFrame = false;
        this.activeClip = null;
        this.stopImmediate = false;
        this.skipBlendOut = false;
        this.wasUsing = false;
        this.swordBlockingSuppressed = false;
        this.lastSwordBlockingHand = null;
    }

    private boolean shouldStopSwordBlockingCompat(class_310 client) {
        if (client == null || client.field_1724 == null || this.activeClip == null) {
            return false;
        }
        if (!PunchyAnimationManager.isClipNamed(this.activeClip, "sword_blocking_compat")) {
            return false;
        }
        if (client.field_1690 == null) {
            return true;
        }
        if (!client.field_1690.field_1904.method_1434()) {
            return true;
        }
        class_1268 blockingHand = this.lastSwordBlockingHand != null ? this.lastSwordBlockingHand : (client.field_1724.method_6115() ? client.field_1724.method_6058() : class_1268.field_5808);
        return !SwordBlockingMechanicsCompat.shouldUseBlockingAnimation(blockingHand);
    }

    private void stopSwordBlockingCompat(class_310 client) {
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        this.clearUseItemState(false);
        if (client != null && client.field_1690 != null && client.field_1690.field_1904.method_1434()) {
            this.swordBlockingSuppressed = true;
        }
    }

    private void playInteract(class_310 client, class_1268 hand, UseActionTracker.TriggerType triggerType, class_2680 blockState, boolean fallbackOnly) {
        Float delay;
        UseSelection selection = this.resolveUseSelection(client, hand, triggerType, blockState, fallbackOnly);
        if (triggerType == UseActionTracker.TriggerType.DROP_ITEM) {
            UseItemStateMachine.clearPendingDropSnapshot(hand);
        } else if (triggerType == UseActionTracker.TriggerType.USE_ITEM_ON) {
            UseItemStateMachine.clearPendingBoneMealUseOnSnapshot(hand);
        }
        AnimationClip clip = selection.clip;
        if (triggerType == UseActionTracker.TriggerType.USE_ITEM_ON && CustomAnimationResolver.isInteractionEntry(selection.entry) && (delay = selection.entry.interfaceDelay) != null && delay.floatValue() > 0.0f) {
            InterfaceDelayState.schedule(delay.floatValue());
        }
        if (clip != null) {
            boolean skipBlendIn;
            boolean preserveOppositeUseFlow;
            ++visualUseItemEpoch;
            CustomAnimationResolver.applyLoopSpeed(selection.entry);
            PunchyAnimationManager.setSourceHand(client, hand);
            class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
            boolean dualHanded = PunchyAnimationManager.isDualHandedActive(client, clip, 0.0f);
            if (dualHanded && hand == class_1268.field_5808 && HandEquipStateMachine.isOffhandEquipTransitionActive()) {
                HandEquipStateMachine.cancelOffhandTransitionForPoseOverride(client.field_1724);
            }
            boolean bl = preserveOppositeUseFlow = hand == class_1268.field_5808 && HandEquipStateMachine.isOffhandEquipTransitionActive() && arm != null;
            if (!preserveOppositeUseFlow) {
                preserveOppositeUseFlow = dualHanded && arm != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.POSE_HANDLER.getActiveArm() != null && PunchyAnimationManager.POSE_HANDLER.getActiveArm() != arm;
            }
            class_1306 opposite = arm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
            PunchyAnimationManager.POSE_HANDLER.setForcedSkipArmForNextPlay((class_1306)(preserveOppositeUseFlow ? opposite : null));
            if (dualHanded && !preserveOppositeUseFlow) {
                PunchyAnimationManager.POSE_HANDLER.setActiveArm(null);
            } else {
                this.primeOppositeArmOverlapDuringEquip(client, hand, arm);
                PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
            }
            boolean hold = clip.isHoldOnLastFrame() || PunchyAnimationManager.isClipNamed(clip, "sword_blocking_compat");
            PunchyAnimationManager.POSE_HANDLER.setLooping(!hold);
            float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
            boolean bl2 = skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose() || this.shouldSkipBlendIn(client) || PunchyAnimationManager.isClipNamed(clip, "use_bow");
            if (CustomAnimationResolver.shouldSkipBlendIn(selection.entry)) {
                skipBlendIn = true;
            }
            if (CustomAnimationResolver.shouldBlendFromCurrentOrigin(selection.entry, clip)) {
                CustomAnimationResolver.playConfiguredClip(clip, selection.entry, speedMultiplier, skipBlendIn, hold);
            } else if (this.shouldBlendFromAttackToBlocking(clip)) {
                if (skipBlendIn) {
                    PunchyAnimationManager.POSE_HANDLER.setBlendInDurationNext(0.0f);
                }
                PunchyAnimationManager.POSE_HANDLER.playWithBlendFromCurrentDynamic(clip, speedMultiplier, hold);
            } else {
                PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn, hold);
            }
            this.lastTriggerTick = this.currentTick(client);
            this.holdOnLastFrame = hold;
            this.activeClip = clip;
            this.stopImmediate = CustomAnimationResolver.shouldStopImmediately(selection.entry);
            this.skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(selection.entry);
        }
    }

    private boolean playPendingSwordBlockingCompat(class_310 client, class_1268 hand) {
        if (PunchyAnimationManager.resolveNamedClip("sword_blocking_compat", client, hand) == null) {
            return false;
        }
        UseActionTracker.clear();
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
        ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
        this.playInteract(client, hand, null, null, false);
        return this.activeClip != null && PunchyAnimationManager.isClipNamed(this.activeClip, "sword_blocking_compat");
    }

    public static long getVisualUseItemEpoch() {
        return visualUseItemEpoch;
    }

    private boolean shouldSkipBlendIn(class_310 client) {
        long now = this.currentTick(client);
        return this.lastTriggerTick > 0L && now - this.lastTriggerTick <= 4L;
    }

    private long currentTick(class_310 client) {
        return client.field_1687 == null ? 0L : client.field_1687.method_8510();
    }

    private boolean shouldAbortEquipForHand(class_310 client, class_1268 hand) {
        if (client == null || hand == null) {
            return true;
        }
        if (HandEquipStateMachine.isOppositeShieldEquipInProgress(hand)) {
            return false;
        }
        class_1306 equipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (equipArm == null) {
            return false;
        }
        class_1306 actionArm = PunchyAnimationManager.getUseArm(client, hand);
        return actionArm == null || actionArm == equipArm;
    }

    private boolean shouldPlayUseDuringOffhandEquip(class_310 client, class_1268 activeHand, boolean usingItem, class_1268 usedHandNow) {
        if (client == null || activeHand != class_1268.field_5808) {
            return false;
        }
        if (!HandEquipStateMachine.isOffhandEquipTransitionActive()) {
            return false;
        }
        return !usingItem || usedHandNow == class_1268.field_5808;
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

    private AnimationClip resolveUseClip(class_310 client, class_1268 hand, UseActionTracker.TriggerType triggerType, class_2680 blockState) {
        CustomAnimationResolver.ClipLookup lookup;
        AnimationClip override;
        PunchyTuningConfig.CustomAnimationEntry interactionEntry;
        String interactionName;
        AnimationClip throwable;
        if (client == null || client.field_1724 == null) {
            return PunchyAnimationManager.INTERACT_CLIP;
        }
        class_1799 stack = this.resolveUseContextStack(client.field_1724.method_5998(hand), hand, triggerType);
        if (this.isChargeItem(stack) && this.shouldBlockChargeForTrigger(triggerType)) {
            return null;
        }
        if (this.shouldResolveThrowableUse(triggerType) && (throwable = this.resolveThrowableUseClip(client, hand, stack, triggerType)) != null) {
            return throwable;
        }
        if (this.isInteractionAwareTrigger(triggerType) && (interactionName = CustomAnimationResolver.resolveInteractionName(interactionEntry = CustomAnimationResolver.findInteractionEntry(stack, blockState))) != null && !interactionName.isBlank() && (override = PunchyAnimationManager.resolveNamedClip(interactionName, client, hand)) != null) {
            return override;
        }
        if (this.isBoneMealStack(stack) && triggerType == UseActionTracker.TriggerType.USE_ITEM_ON) {
            return PunchyAnimationManager.resolveTypeFallbackClip("useItem", client, hand, PunchyTuningConfig.getMixPreferredOptionForStack(stack));
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "useItem");
        if (entry != null && (lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, "useItem", entry, client, hand)).clip() != null) {
            return lookup.clip();
        }
        AnimationClip fishingRodClip = this.resolveFishingRodUseClip(client, hand, stack);
        if (fishingRodClip != null) {
            return fishingRodClip;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        if (kind == VanillaFirstPersonItemLayer.ToolKind.HOE || kind == VanillaFirstPersonItemLayer.ToolKind.AXE) {
            return PunchyAnimationManager.resolveHandedClip("mining_use_foward", client, hand, preferredOption);
        }
        if (kind == VanillaFirstPersonItemLayer.ToolKind.SHOVEL) {
            return PunchyAnimationManager.resolveHandedClip("mining_use_digging", client, hand, preferredOption);
        }
        return PunchyAnimationManager.resolveHandedClip("interact", client, hand, preferredOption);
    }

    private boolean shouldIgnoreTrigger(class_310 client, class_1268 hand, UseActionTracker.TriggerType triggerType, class_2680 blockState) {
        if (triggerType != null) {
            return false;
        }
        if (!this.holdOnLastFrame || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        AnimationClip clip = this.resolveUseSelection((class_310)client, (class_1268)hand, (UseActionTracker.TriggerType)triggerType, (class_2680)blockState, (boolean)false).clip;
        return clip != null && clip == this.activeClip;
    }

    private boolean shouldUseFallbackTrigger(class_310 client, UseActionTracker.Trigger trigger) {
        if (client == null || client.field_1724 == null || trigger == null) {
            return false;
        }
        if (!trigger.fallback() || trigger.type() != UseActionTracker.TriggerType.USE_ITEM_ON || trigger.blockHit() == null) {
            return false;
        }
        class_1268 hand = trigger.hand();
        if (hand == null || PunchyAnimationManager.isHandBlacklisted(client, hand)) {
            return false;
        }
        class_1799 stack = client.field_1724.method_5998(hand);
        if (stack == null || ConsumeStateMachine.isConsumeStack(stack) || this.isChargeItem(stack)) {
            return false;
        }
        if (!stack.method_7960()) {
            return true;
        }
        class_2680 blockState = this.resolveBlockState(client, trigger.blockHit());
        if (this.isFallbackAttackCandidate(client, hand, stack)) {
            return true;
        }
        PunchyTuningConfig.CustomAnimationEntry interactionEntry = CustomAnimationResolver.findInteractionEntry(stack, blockState);
        if (interactionEntry != null || CustomAnimationResolver.findEntry(stack, "useItem") != null) {
            return true;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        return kind == VanillaFirstPersonItemLayer.ToolKind.HOE || kind == VanillaFirstPersonItemLayer.ToolKind.AXE || kind == VanillaFirstPersonItemLayer.ToolKind.SHOVEL;
    }

    private UseSelection resolveUseSelection(class_310 client, class_1268 hand, UseActionTracker.TriggerType triggerType, class_2680 blockState) {
        return this.resolveUseSelection(client, hand, triggerType, blockState, false);
    }

    private UseSelection resolveUseSelection(class_310 client, class_1268 hand, UseActionTracker.TriggerType triggerType, class_2680 blockState, boolean fallbackOnly) {
        CustomAnimationResolver.ClipLookup lookup;
        AnimationClip override;
        PunchyTuningConfig.CustomAnimationEntry interactionEntry;
        String interactionName;
        AnimationClip compatClip;
        AnimationClip throwable;
        if (client == null || client.field_1724 == null) {
            return new UseSelection(PunchyAnimationManager.INTERACT_CLIP, null);
        }
        class_1799 stack = this.resolveUseContextStack(client.field_1724.method_5998(hand), hand, triggerType);
        if (this.isChargeItem(stack) && this.shouldBlockChargeForTrigger(triggerType)) {
            return new UseSelection(null, null);
        }
        if (this.shouldResolveThrowableUse(triggerType) && (throwable = this.resolveThrowableUseClip(client, hand, stack, triggerType)) != null) {
            class_1799 resolvedStack = this.resolveThrowableContextStack(stack, hand, triggerType);
            PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(resolvedStack, "useItem");
            return new UseSelection(throwable, entry);
        }
        if (!this.swordBlockingSuppressed && SwordBlockingMechanicsCompat.shouldUseBlockingAnimation(hand) && (compatClip = PunchyAnimationManager.resolveNamedClip("sword_blocking_compat", client, hand)) != null) {
            return new UseSelection(compatClip, null);
        }
        if (fallbackOnly && triggerType == UseActionTracker.TriggerType.USE_ITEM_ON && this.isFallbackAttackCandidate(client, hand, stack)) {
            return new UseSelection(this.resolveFallbackAttackClip(client, hand, stack), null);
        }
        if (this.isInteractionAwareTrigger(triggerType) && (interactionName = CustomAnimationResolver.resolveInteractionName(interactionEntry = CustomAnimationResolver.findInteractionEntry(stack, blockState))) != null && !interactionName.isBlank() && (override = PunchyAnimationManager.resolveNamedClip(interactionName, client, hand)) != null) {
            return new UseSelection(override, interactionEntry);
        }
        if (this.isBoneMealStack(stack) && triggerType == UseActionTracker.TriggerType.USE_ITEM_ON) {
            return new UseSelection(PunchyAnimationManager.resolveTypeFallbackClip("useItem", client, hand, PunchyTuningConfig.getMixPreferredOptionForStack(stack)), null);
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "useItem");
        if (entry != null && (lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, "useItem", entry, client, hand)).clip() != null) {
            return new UseSelection(lookup.clip(), entry);
        }
        AnimationClip fishingRodClip = this.resolveFishingRodUseClip(client, hand, stack);
        if (fishingRodClip != null) {
            return new UseSelection(fishingRodClip, CustomAnimationResolver.findEntry(stack, "fishingrod"));
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        if (kind == VanillaFirstPersonItemLayer.ToolKind.HOE || kind == VanillaFirstPersonItemLayer.ToolKind.AXE) {
            return new UseSelection(PunchyAnimationManager.resolveHandedClip("mining_use_foward", client, hand, preferredOption), null);
        }
        if (kind == VanillaFirstPersonItemLayer.ToolKind.SHOVEL) {
            return new UseSelection(PunchyAnimationManager.resolveHandedClip("mining_use_digging", client, hand, preferredOption), null);
        }
        return new UseSelection(PunchyAnimationManager.resolveHandedClip("interact", client, hand, preferredOption), null);
    }

    private boolean isFallbackAttackCandidate(class_310 client, class_1268 hand, class_1799 stack) {
        if (client == null || hand == null) {
            return false;
        }
        if (stack == null || stack.method_7960()) {
            return hand == class_1268.field_5808;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        if (kind == VanillaFirstPersonItemLayer.ToolKind.SWORD || kind == VanillaFirstPersonItemLayer.ToolKind.MACE || kind == VanillaFirstPersonItemLayer.ToolKind.SPEAR || kind == VanillaFirstPersonItemLayer.ToolKind.TRIDENT) {
            return true;
        }
        return BetterCombatCompat.isWeaponStack(stack);
    }

    private AnimationClip resolveFallbackAttackClip(class_310 client, class_1268 hand, class_1799 stack) {
        AnimationClip spear;
        AnimationClip trident;
        AnimationClip mace;
        if (client == null) {
            return PunchyAnimationManager.ATTACK_CLIP;
        }
        if (stack == null || stack.method_7960()) {
            AnimationClip punch = PunchyAnimationManager.resolveHandedClip("punch", client, hand);
            return punch != null ? punch : PunchyAnimationManager.PUNCH_CLIP;
        }
        if (stack.method_7909() instanceof class_9362 && (mace = PunchyAnimationManager.resolveHandedClip("attack_mace", client, hand)) != null) {
            return mace;
        }
        if (stack.method_7909() instanceof class_1835 && (trident = PunchyAnimationManager.resolveHandedClip("attack_trident", client, hand)) != null) {
            return trident;
        }
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        if (kind == VanillaFirstPersonItemLayer.ToolKind.SPEAR && (spear = PunchyAnimationManager.resolveHandedClip("attack_spear", client, hand)) != null) {
            return spear;
        }
        return PunchyAnimationManager.resolveHandedClip("attack", client, hand);
    }

    private AnimationClip resolveThrowableUseClip(class_310 client, class_1268 hand, class_1799 liveStack, UseActionTracker.TriggerType triggerType) {
        class_1799 stack = this.resolveThrowableContextStack(liveStack, hand, triggerType);
        if (client == null || hand == null || !this.isThrowableUseStack(stack)) {
            return null;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "useItem");
        if (entry != null) {
            String explicitName = null;
            explicitName = entry.throw_animation != null && !entry.throw_animation.isBlank() ? entry.throw_animation.trim() : CustomAnimationResolver.resolveSingleName(entry);
            AnimationClip explicit = PunchyAnimationManager.resolveNamedClip(explicitName, client, hand);
            if (explicit != null) {
                return explicit;
            }
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        return PunchyAnimationManager.resolveHandedClip("throw_item", client, hand, preferredOption);
    }

    private boolean isThrowableUseStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150) || stack.method_31574(class_1802.field_8287) || stack.method_31574(class_1802.field_8543) || stack.method_31574(class_1802.field_8803) || stack.method_31574(class_1802.field_8634) || stack.method_31574(class_1802.field_8449);
    }

    private class_1799 resolveThrowableContextStack(class_1799 liveStack, class_1268 hand, UseActionTracker.TriggerType triggerType) {
        if (liveStack != null && !liveStack.method_7960()) {
            return liveStack;
        }
        if (triggerType != UseActionTracker.TriggerType.USE_ITEM || hand == null) {
            return liveStack == null ? class_1799.field_8037 : liveStack;
        }
        class_1799 pending = UseItemStateMachine.consumePendingThrowableSnapshot(hand);
        return this.isThrowableUseStack(pending) ? pending : class_1799.field_8037;
    }

    public static void capturePendingThrowableSnapshot(class_1268 hand, class_1799 stack) {
        if (hand == null || !UseItemStateMachine.isThrowableStatic(stack)) {
            UseItemStateMachine.clearPendingThrowableSnapshot(hand);
            return;
        }
        class_1799 copy = stack.method_7972();
        if (hand == class_1268.field_5810) {
            pendingThrowableOff = copy;
        } else {
            pendingThrowableMain = copy;
        }
    }

    public static void capturePendingDropSnapshot(class_1268 hand, class_1799 stack) {
        if (hand == null || stack == null || stack.method_7960()) {
            UseItemStateMachine.clearPendingDropSnapshot(hand);
            return;
        }
        class_1799 copy = stack.method_7972();
        if (hand == class_1268.field_5810) {
            pendingDropOff = copy;
        } else {
            pendingDropMain = copy;
        }
    }

    public static void capturePendingBoneMealUseOnSnapshot(class_1268 hand, class_1799 stack) {
        if (hand == null || !UseItemStateMachine.isBoneMealStatic(stack)) {
            UseItemStateMachine.clearPendingBoneMealUseOnSnapshot(hand);
            return;
        }
        class_1799 copy = stack.method_7972();
        if (hand == class_1268.field_5810) {
            pendingBoneMealUseOnOff = copy;
        } else {
            pendingBoneMealUseOnMain = copy;
        }
    }

    public static boolean hasPendingBoneMealUseOnSnapshot(class_1268 hand) {
        return UseItemStateMachine.isBoneMealStatic(UseItemStateMachine.peekPendingBoneMealUseOnSnapshot(hand));
    }

    public static void requestSwordBlockingCompat(class_1268 hand) {
        pendingSwordBlockingCompatHand = hand == null ? class_1268.field_5808 : hand;
    }

    private static class_1268 consumePendingSwordBlockingCompatHand() {
        class_1268 hand = pendingSwordBlockingCompatHand;
        pendingSwordBlockingCompatHand = null;
        return hand;
    }

    public static void clearPendingThrowableSnapshot(class_1268 hand) {
        if (hand == null || hand == class_1268.field_5808) {
            pendingThrowableMain = class_1799.field_8037;
        }
        if (hand == null || hand == class_1268.field_5810) {
            pendingThrowableOff = class_1799.field_8037;
        }
    }

    private static class_1799 consumePendingThrowableSnapshot(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            class_1799 snapshot = pendingThrowableOff;
            pendingThrowableOff = class_1799.field_8037;
            return snapshot == null ? class_1799.field_8037 : snapshot;
        }
        class_1799 snapshot = pendingThrowableMain;
        pendingThrowableMain = class_1799.field_8037;
        return snapshot == null ? class_1799.field_8037 : snapshot;
    }

    private static void clearPendingDropSnapshot(class_1268 hand) {
        if (hand == null || hand == class_1268.field_5808) {
            pendingDropMain = class_1799.field_8037;
        }
        if (hand == null || hand == class_1268.field_5810) {
            pendingDropOff = class_1799.field_8037;
        }
    }

    private static class_1799 peekPendingDropSnapshot(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            return pendingDropOff == null ? class_1799.field_8037 : pendingDropOff;
        }
        return pendingDropMain == null ? class_1799.field_8037 : pendingDropMain;
    }

    private class_1799 resolveUseContextStack(class_1799 liveStack, class_1268 hand, UseActionTracker.TriggerType triggerType) {
        class_1799 stack = this.resolveDropContextStack(liveStack, hand, triggerType);
        if (triggerType != UseActionTracker.TriggerType.USE_ITEM_ON || hand == null || this.isBoneMealStack(stack)) {
            return stack;
        }
        class_1799 pending = UseItemStateMachine.peekPendingBoneMealUseOnSnapshot(hand);
        return this.isBoneMealStack(pending) ? pending : stack;
    }

    private class_1799 resolveDropContextStack(class_1799 liveStack, class_1268 hand, UseActionTracker.TriggerType triggerType) {
        if (triggerType != UseActionTracker.TriggerType.DROP_ITEM || hand == null) {
            return liveStack == null ? class_1799.field_8037 : liveStack;
        }
        class_1799 pending = UseItemStateMachine.peekPendingDropSnapshot(hand);
        return pending == null || pending.method_7960() ? (liveStack == null ? class_1799.field_8037 : liveStack) : pending;
    }

    private static void clearPendingBoneMealUseOnSnapshot(class_1268 hand) {
        if (hand == null || hand == class_1268.field_5808) {
            pendingBoneMealUseOnMain = class_1799.field_8037;
        }
        if (hand == null || hand == class_1268.field_5810) {
            pendingBoneMealUseOnOff = class_1799.field_8037;
        }
    }

    private static class_1799 peekPendingBoneMealUseOnSnapshot(class_1268 hand) {
        if (hand == class_1268.field_5810) {
            return pendingBoneMealUseOnOff == null ? class_1799.field_8037 : pendingBoneMealUseOnOff;
        }
        return pendingBoneMealUseOnMain == null ? class_1799.field_8037 : pendingBoneMealUseOnMain;
    }

    private boolean shouldResolveThrowableUse(UseActionTracker.TriggerType triggerType) {
        return !this.isInteractionAwareTrigger(triggerType) && triggerType != UseActionTracker.TriggerType.DROP_ITEM;
    }

    private boolean isBoneMealStack(class_1799 stack) {
        return UseItemStateMachine.isBoneMealStatic(stack);
    }

    private static boolean isBoneMealStatic(class_1799 stack) {
        return stack != null && stack.method_31574(class_1802.field_8324);
    }

    private static boolean isThrowableStatic(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150) || stack.method_31574(class_1802.field_8287) || stack.method_31574(class_1802.field_8543) || stack.method_31574(class_1802.field_8803) || stack.method_31574(class_1802.field_8634) || stack.method_31574(class_1802.field_8449);
    }

    private boolean shouldForceSwordBlockingPlay(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (!SwordBlockingMechanicsCompat.shouldUseBlockingAnimation(client.field_1724.method_6058())) {
            return false;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null) {
            return false;
        }
        return this.isAttackClip(current);
    }

    private boolean shouldBlendFromAttackToBlocking(AnimationClip nextClip) {
        if (nextClip == null) {
            return false;
        }
        if (!PunchyAnimationManager.isClipNamed(nextClip, "sword_blocking_compat")) {
            return false;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null) {
            return false;
        }
        return this.isAttackClip(current);
    }

    private void clearUseItemState(boolean clearSuppression) {
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "useItem");
        ItemAnimationManager.clearPersistentType(class_1268.field_5810, "useItem");
        AnimationClip currentClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        boolean keepDualHandedSource = PunchyAnimationManager.POSE_HANDLER.isPlaying() && currentClip != null && PunchyAnimationManager.hasAnyDualHandedEffect(currentClip);
        this.holdOnLastFrame = false;
        this.activeClip = null;
        this.stopImmediate = false;
        this.skipBlendOut = false;
        this.wasUsing = false;
        if (clearSuppression) {
            this.swordBlockingSuppressed = false;
        }
        if (!keepDualHandedSource) {
            PunchyAnimationManager.clearSourceHand();
        }
    }

    private boolean isChargeItem(class_1799 stack) {
        if (stack == null) {
            return false;
        }
        return stack.method_7909() instanceof class_1753 || stack.method_7909() instanceof class_1764 || stack.method_7909() instanceof class_1835;
    }

    private boolean shouldBlockChargeForTrigger(UseActionTracker.TriggerType triggerType) {
        return triggerType == null || triggerType == UseActionTracker.TriggerType.USE_ITEM;
    }

    private boolean isInteractionAwareTrigger(UseActionTracker.TriggerType triggerType) {
        return triggerType == UseActionTracker.TriggerType.USE_ITEM_ON || triggerType == UseActionTracker.TriggerType.INTERACT || triggerType == UseActionTracker.TriggerType.INTERACT_AT;
    }

    private class_2680 resolveBlockState(class_310 client, class_3965 hit) {
        if (client == null || client.field_1687 == null || hit == null) {
            return null;
        }
        return client.field_1687.method_8320(hit.method_17777());
    }

    private boolean isAttackClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "attack")) {
            return true;
        }
        String name = clip.getName();
        return name != null && name.toLowerCase(Locale.ROOT).contains("attack");
    }

    private AnimationClip resolveFishingRodUseClip(class_310 client, class_1268 hand, class_1799 stack) {
        String explicitName;
        if (client == null || client.field_1724 == null || stack == null || stack.method_7960()) {
            return null;
        }
        if (!(stack.method_7909() instanceof class_1787)) {
            return null;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "fishingrod");
        if (entry == null) {
            return null;
        }
        boolean castActive = this.isFishingRodCastActive(client);
        String string = explicitName = castActive ? entry.pull_animation : entry.throw_animation;
        if (explicitName == null || explicitName.isBlank()) {
            explicitName = CustomAnimationResolver.resolveSingleName(entry);
        }
        if (explicitName == null || explicitName.isBlank()) {
            return null;
        }
        return PunchyAnimationManager.resolveNamedClip(explicitName.trim(), client, hand);
    }

    private boolean isFishingRodCastActive(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        return ((PlayerFishingAccessor)client.field_1724).punchy$getFishing() != null;
    }

    private boolean isBowUseActive(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        return client.field_1724.method_6115() && usingStack.method_7909() instanceof class_1753 && client.field_1690 != null && client.field_1690.field_1904.method_1434();
    }

    private boolean isSpearUseActive(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        return client.field_1724.method_6115() && SpearStateMachine.isSpearStack(usingStack) && client.field_1690 != null && client.field_1690.field_1904.method_1434();
    }

    private boolean isCrossbowUseActive(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        return client.field_1724.method_6115() && usingStack.method_7909() instanceof class_1764 && client.field_1690 != null && client.field_1690.field_1904.method_1434();
    }

    private record UseSelection(AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry) {
    }
}

