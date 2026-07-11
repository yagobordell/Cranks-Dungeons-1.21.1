/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1819
 *  net.minecraft.class_1835
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 *  net.minecraft.class_9331
 *  net.minecraft.class_9334
 *  net.minecraft.class_9336
 *  org.joml.Vector3f
 */
package punchy.client.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1819;
import net.minecraft.class_1835;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import net.minecraft.class_9336;
import org.joml.Vector3f;
import punchy.client.PunchyClient;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;
import punchy.client.state.ConsumeStateMachine;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.MiningActionTracker;
import punchy.client.state.TridentStateMachine;
import punchy.client.state.UseActionTracker;
import punchy.compat.AdorableHamsterPetsCompat;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;
import punchy.platform.Services;

public class HandEquipStateMachine {
    private static final boolean LOG_OFFHAND_DETACH_DEBUG = false;
    private static final boolean LOG_WALK_ON_WATER_EQUIP_DEBUG = true;
    private static final long OFFHAND_USE_UNSUPPRESS_WINDOW_NANOS = 1800000000L;
    private static HandEquipStateMachine instance;
    private static volatile boolean equipActive;
    private static volatile String activeType;
    private static volatile int handOutGraceTicks;
    private static volatile boolean syncRequested;
    private static volatile int vanillaHandOutMainTicks;
    private static volatile int vanillaHandOutOffTicks;
    private static volatile int lastEquipTransitionPlayerTick;
    private static final String EFFECT_SWAP_END = "swap_end";
    private static final String EFFECT_SWAP = "swap_items";
    private static final String EFFECT_SWAP_ITEM = "swap_item";
    private static final int VANILLA_HAND_OUT_TICKS = 4;
    private static final int HOTBAR_RAPID_WINDOW_TICKS = 4;
    private static final int HOTBAR_RAPID_MIN_STREAK = 2;
    private static final int F_SWAP_BURST_WINDOW_TICKS = 10;
    private static final int F_SWAP_BURST_MIN_COUNT = 2;
    private static final float HOTBAR_HAND_OUT_SPEED_BOOST = 2.6f;
    private static final float HAND_OUT_ACTION_BLEND_SECONDS = 0.1f;
    private static final float HAND_OUT_TO_HAND_IN_EARLY_TRANSITION_SECONDS = 0.05f;
    private static final float WALK_ON_WATER_MIN_REBASE_TIME = 5.0E-4f;
    private Phase phase = Phase.IDLE;
    private boolean initialized;
    private int lastHotbarSlot = -1;
    private int hotbarRapidWindowTicks;
    private int hotbarChangeStreak;
    private final Deque<Integer> recentFunctionalSwapTicks = new ArrayDeque<Integer>();
    private class_1799 renderedMain = class_1799.field_8037;
    private class_1799 renderedOff = class_1799.field_8037;
    private class_1799 pendingMain = class_1799.field_8037;
    private class_1799 pendingOff = class_1799.field_8037;
    private boolean pendingMainChange;
    private boolean pendingOffChange;
    private boolean pickupRevealMainOnRaise;
    private boolean pickupRevealOffOnRaise;
    private boolean deferSwapWithoutEquip;
    private boolean skipNextEquipMain;
    private boolean skipNextEquipOff;
    private boolean swapTriggered;
    private boolean cycleMain;
    private boolean cycleOff;
    private boolean pausedByUseClip;
    private boolean suppressOffhandRender;
    private boolean hideOffhandTridentThrowCache;
    private boolean offhandRevealPending;
    private boolean offhandAwaitingHandIn;
    private boolean pendingBlacklistHandInMain;
    private boolean pendingBlacklistHandInOff;
    private boolean blacklistTransitionHandIn;
    private boolean lastOffhandEmpty = true;
    private boolean pendingMainActionClear;
    private boolean pendingOffActionClear;
    private AnimationClip pendingMainActionClearClip;
    private AnimationClip pendingOffActionClearClip;
    private float pendingMainActionClearTime;
    private float pendingOffActionClearTime;
    private AnimationClip activeHandInClip;
    private AnimationClip activeHandOutClip;
    private class_1306 activeHandInArm;
    private boolean detachedOffhandEquip;
    private boolean shieldOppositeEquip;
    private String lastOffhandDebugSnapshot = "";

    public void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        class_746 player = client.field_1724;
        if (!this.initialized) {
            this.initialize(player);
        }
        if (syncRequested && !this.shouldDeferRenderedSyncDuringShieldOppositeEquip()) {
            syncRequested = false;
            this.syncRenderedItems(player);
        }
        if (AdorableHamsterPetsCompat.isPettingFeedbackActive(client)) {
            this.suppressEquipDuringHamsterPetting(player);
            return;
        }
        class_1799 currentMain = player.method_6047();
        class_1799 currentOff = player.method_6079();
        this.updateSkipEquipLatches(currentMain, currentOff);
        boolean hotbarChanged = this.updateHotbarSlot(player);
        if (hotbarChanged) {
            this.hotbarChangeStreak = Math.min(8, this.hotbarChangeStreak + 1);
            this.hotbarRapidWindowTicks = 4;
        } else if (this.hotbarRapidWindowTicks > 0) {
            --this.hotbarRapidWindowTicks;
        } else {
            this.hotbarChangeStreak = 0;
        }
        boolean blockingClipActive = this.isBlockingEquipClipActive(currentMain);
        if (blockingClipActive) {
            this.pausedByUseClip = true;
            if (currentMain.method_7960() && !this.renderedMain.method_7960()) {
                this.renderedMain = class_1799.field_8037;
            }
        } else if (this.pausedByUseClip && this.phase != Phase.IDLE) {
            this.resumeEquipClip(player);
            this.pausedByUseClip = false;
        } else if (this.pausedByUseClip) {
            this.pausedByUseClip = false;
        }
        boolean suppressMainEquipChange = this.updatePendingActionClearMain(currentMain, currentOff);
        boolean suppressOffEquipChange = this.updatePendingActionClearOff(currentOff, currentMain);
        boolean mainChanged = !suppressMainEquipChange && (this.hasItemChanged(this.renderedMain, currentMain) || hotbarChanged && this.shouldAnimateHotbarChange(this.renderedMain, currentMain));
        boolean offChanged = !suppressOffEquipChange && this.hasItemChanged(this.renderedOff, currentOff);
        this.updateOffhandRevealPending(player, currentOff, offChanged);
        this.updateOffhandAwaitingHandIn(player, currentOff);
        this.suppressOffhandRender = this.shouldSuppressOffhandRender(currentOff);
        switch (this.phase.ordinal()) {
            case 0: {
                this.handleIdle(client, player, currentMain, currentOff, mainChanged, offChanged);
                break;
            }
            case 1: {
                this.handleLowering(client, player, currentMain, currentOff, hotbarChanged);
                break;
            }
            case 2: {
                this.handleRaising(client, player, currentMain, currentOff, hotbarChanged);
            }
        }
        equipActive = this.phase != Phase.IDLE;
        switch (this.phase.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 1: {
                String string = "hand_out";
                break;
            }
            case 2: {
                String string = "hand_in";
                break;
            }
            case 0: {
                String string = activeType = null;
            }
        }
        if ("hand_out".equals(activeType) && this.cycleOff) {
            handOutGraceTicks = 2;
        } else if (handOutGraceTicks > 0) {
            --handOutGraceTicks;
        }
        if (this.phase == Phase.LOWERING) {
            if (this.cycleMain) {
                ItemAnimationManager.setPersistentType(class_1268.field_5808, "hand_out", false);
            } else {
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_out");
            }
            if (this.cycleOff && !this.shouldSuppressOffhandHandOut(player, currentOff)) {
                ItemAnimationManager.setPersistentType(class_1268.field_5810, "hand_out", false);
            } else {
                ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_out");
            }
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_in");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_in");
        } else if (this.phase == Phase.RAISING) {
            if (this.cycleMain) {
                ItemAnimationManager.setPersistentType(class_1268.field_5808, "hand_in", false);
            } else {
                ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_in");
            }
            if (this.cycleOff) {
                ItemAnimationManager.setPersistentType(class_1268.field_5810, "hand_in", false);
            } else {
                ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_in");
            }
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_out");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_out");
        } else {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_out");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_out");
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_in");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_in");
        }
        this.logOffhandState(player, currentOff);
    }

    private void suppressEquipDuringHamsterPetting(class_746 player) {
        if (player == null) {
            return;
        }
        this.resetEquipState(player, true);
        this.phase = Phase.IDLE;
        equipActive = false;
        activeType = null;
        handOutGraceTicks = 0;
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_out");
        ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_out");
        ItemAnimationManager.clearPersistentType(class_1268.field_5808, "hand_in");
        ItemAnimationManager.clearPersistentType(class_1268.field_5810, "hand_in");
        this.applyEquipOverrides(player);
    }

    public HandEquipStateMachine() {
        instance = this;
    }

    public static void requestRenderedSync() {
        syncRequested = true;
    }

    public static void triggerPickupEquipCycle(class_310 client, class_1268 hand) {
        class_746 class_7462;
        if (instance == null || client == null || !((class_7462 = client.field_1724) instanceof class_746)) {
            return;
        }
        class_746 localPlayer = class_7462;
        class_1268 targetHand = hand == null ? class_1268.field_5808 : hand;
        instance.forcePickupEquipCycle(localPlayer, targetHand);
    }

    public static void triggerImmediateEquipCycle(class_310 client, class_1268 hand) {
        class_746 class_7462;
        if (instance == null || client == null || !((class_7462 = client.field_1724) instanceof class_746)) {
            return;
        }
        class_746 localPlayer = class_7462;
        class_1268 targetHand = hand == null ? class_1268.field_5808 : hand;
        instance.forceImmediateEquipCycle(localPlayer, targetHand);
    }

    private void handleIdle(class_310 client, class_746 player, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged) {
        boolean allowWalkOnWaterEquip;
        boolean walkOnWaterActive = this.isWalkOnWaterPoseActive() || CustomAnimationResolver.shouldAutoBlendFromWalkOnWater();
        boolean functionalOffhandSwap = this.isFunctionalOffhandSwap(this.renderedMain, this.renderedOff, currentMain, currentOff, mainChanged, offChanged, false);
        boolean bl = allowWalkOnWaterEquip = walkOnWaterActive && (mainChanged && !offChanged || functionalOffhandSwap);
        if (this.handleBlacklistToPunchyTransition(player, currentMain, currentOff, mainChanged, offChanged)) {
            return;
        }
        if (this.deferSwapWithoutEquip) {
            if (mainChanged || offChanged) {
                this.queuePending(player, currentMain, currentOff, mainChanged, offChanged, false);
            }
            if (!this.isSkipHandEquipWindowActive()) {
                this.applyPending(player);
                this.deferSwapWithoutEquip = false;
                this.skipNextEquipMain = false;
                this.skipNextEquipOff = false;
            } else {
                this.applyEquipOverrides(player);
            }
            return;
        }
        if ((mainChanged || offChanged) && this.shouldPreserveCurrentClipForHandOut() && (ItemAnimationManager.isTypeActive("mining") || MiningActionTracker.wasRecentMining(250000000L))) {
            this.queuePending(player, currentMain, currentOff, mainChanged, offChanged, false);
            this.startLowering(player);
            return;
        }
        if (this.isLoopingActionClip() && !allowWalkOnWaterEquip) {
            if (mainChanged) {
                this.renderedMain = this.copyStack(currentMain);
            }
            if (offChanged) {
                this.renderedOff = this.copyStack(currentOff);
            }
            this.pendingMainChange = false;
            this.pendingOffChange = false;
            this.applyEquipOverrides(player);
            return;
        }
        boolean blockEquip = this.shouldBlockEquip(client, currentMain, mainChanged, offChanged);
        if (!allowWalkOnWaterEquip && blockEquip && (mainChanged || offChanged)) {
            this.clearRenderedEmptyHandsWhileUseBlocksEquip(player, currentMain, currentOff, mainChanged, offChanged);
            this.queuePending(player, currentMain, currentOff, mainChanged, offChanged, false);
            this.applyEquipOverrides(player);
            return;
        }
        if ((allowWalkOnWaterEquip || !blockEquip) && (mainChanged || offChanged)) {
            if (this.shouldSkipHandEquipOnSwap(currentMain, currentOff, mainChanged, offChanged)) {
                this.queuePending(player, currentMain, currentOff, mainChanged, offChanged, false);
                this.deferSwapWithoutEquip = true;
                this.applyEquipOverrides(player);
                return;
            }
            this.queuePending(player, currentMain, currentOff, mainChanged, offChanged, false);
            this.hideOffhandTridentThrowCache = this.shouldHideOffhandTridentThrowCache(currentOff, offChanged);
            this.startLowering(player);
            return;
        }
        this.renderedMain = this.copyStack(currentMain);
        this.renderedOff = this.copyStack(currentOff);
        this.hideOffhandTridentThrowCache = false;
        this.applyEquipOverrides(player);
    }

    private void forcePickupEquipCycle(class_746 player, class_1268 hand) {
        if (player == null) {
            return;
        }
        class_1799 currentMain = player.method_6047();
        class_1799 currentOff = player.method_6079();
        if (hand == class_1268.field_5810) {
            this.pendingOffChange = true;
            this.pendingOff = this.copyStack(currentOff);
            this.renderedOff = class_1799.field_8037;
            this.pendingMainChange = false;
            this.pickupRevealOffOnRaise = true;
            this.pickupRevealMainOnRaise = false;
        } else {
            this.pendingMainChange = true;
            this.pendingMain = this.copyStack(currentMain);
            this.renderedMain = class_1799.field_8037;
            this.pendingOffChange = false;
            this.pickupRevealMainOnRaise = true;
            this.pickupRevealOffOnRaise = false;
        }
        this.deferSwapWithoutEquip = false;
        this.skipNextEquipMain = false;
        this.skipNextEquipOff = false;
        this.pausedByUseClip = false;
        if (this.phase == Phase.IDLE) {
            this.startLowering(player);
            return;
        }
        if (this.phase == Phase.RAISING) {
            this.activeHandInClip = null;
            this.activeHandInArm = null;
            this.phase = Phase.IDLE;
            this.startLowering(player);
            return;
        }
        this.applyEquipOverrides(player);
    }

    private void forceImmediateEquipCycle(class_746 player, class_1268 hand) {
        if (player == null) {
            return;
        }
        class_1799 currentMain = player.method_6047();
        class_1799 currentOff = player.method_6079();
        if (hand == class_1268.field_5810) {
            this.pendingOffChange = true;
            this.pendingOff = this.copyStack(currentOff);
            this.pendingMainChange = false;
        } else {
            this.pendingMainChange = true;
            this.pendingMain = this.copyStack(currentMain);
            this.pendingOffChange = false;
        }
        this.pickupRevealMainOnRaise = false;
        this.pickupRevealOffOnRaise = false;
        this.deferSwapWithoutEquip = false;
        this.skipNextEquipMain = false;
        this.skipNextEquipOff = false;
        this.pausedByUseClip = false;
        if (this.phase == Phase.RAISING) {
            this.activeHandInClip = null;
            this.activeHandInArm = null;
            this.phase = Phase.IDLE;
        }
        if (this.phase == Phase.IDLE) {
            this.startLowering(player);
        } else {
            this.applyEquipOverrides(player);
        }
    }

    private boolean handleBlacklistToPunchyTransition(class_746 player, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged) {
        boolean readyOff;
        if (player == null) {
            return false;
        }
        boolean queued = false;
        if (!this.pendingBlacklistHandInMain && mainChanged && this.wasItemBlacklisted(this.renderedMain) && !this.isHandBlacklisted(player, class_1268.field_5808)) {
            this.pendingBlacklistHandInMain = true;
            this.pendingMainChange = true;
            this.pendingMain = this.copyStack(currentMain);
            vanillaHandOutMainTicks = 4;
            queued = true;
        }
        if (!this.pendingBlacklistHandInOff && offChanged && this.wasItemBlacklisted(this.renderedOff) && !this.isHandBlacklisted(player, class_1268.field_5810)) {
            this.pendingBlacklistHandInOff = true;
            this.pendingOffChange = true;
            this.pendingOff = this.copyStack(currentOff);
            vanillaHandOutOffTicks = 4;
            queued = true;
        }
        if (!(queued || this.pendingBlacklistHandInMain || this.pendingBlacklistHandInOff)) {
            return false;
        }
        if (this.pendingBlacklistHandInMain && vanillaHandOutMainTicks > 0) {
            --vanillaHandOutMainTicks;
        }
        if (this.pendingBlacklistHandInOff && vanillaHandOutOffTicks > 0) {
            --vanillaHandOutOffTicks;
        }
        boolean readyMain = this.pendingBlacklistHandInMain && vanillaHandOutMainTicks <= 0;
        boolean bl = readyOff = this.pendingBlacklistHandInOff && vanillaHandOutOffTicks <= 0;
        if (!readyMain && !readyOff) {
            this.applyEquipOverrides(player);
            return true;
        }
        if (readyMain) {
            this.pendingBlacklistHandInMain = false;
            this.pendingMainChange = true;
            this.pendingMain = this.copyStack(currentMain);
        }
        if (readyOff) {
            this.pendingBlacklistHandInOff = false;
            this.pendingOffChange = true;
            this.pendingOff = this.copyStack(currentOff);
        }
        this.cycleMain = readyMain;
        this.cycleOff = readyOff;
        this.blacklistTransitionHandIn = true;
        this.applyPending(player);
        this.startRaising(player);
        return true;
    }

    private void handleLowering(class_310 client, class_746 player, class_1799 currentMain, class_1799 currentOff, boolean hotbarChanged) {
        boolean handOutReadyToSwap;
        if (!this.shieldOppositeEquip && this.isLoopingActionClip()) {
            this.resetToCurrent(player);
            return;
        }
        if (!this.shieldOppositeEquip && this.isBlockingEquipClipActive(currentMain)) {
            this.pausedByUseClip = true;
            return;
        }
        if (this.isInterruptedByOtherClip()) {
            if (this.tryDetachOffhandDuringAction(player)) {
                return;
            }
            this.exitEquipPreservingSecondary(player);
            return;
        }
        boolean changedNow = this.updatePendingIfChanged(player, currentMain, currentOff, hotbarChanged);
        if (changedNow) {
            if (this.isRapidHotbarScrollActive() && this.pendingMainChange && !this.pendingOffChange) {
                this.swapTriggered = true;
                this.applyPending(player);
                this.startRaising(player);
                return;
            }
            if (this.shouldForceDirectHandInOnRapidRepeat(player)) {
                this.forceDirectHandInFromPending(player);
                return;
            }
            this.startLowering(player);
            return;
        }
        boolean bl = handOutReadyToSwap = this.shouldSwapNow() || this.isHandOutInEarlyTransitionWindow();
        if (!this.swapTriggered && handOutReadyToSwap) {
            this.swapTriggered = true;
            boolean skipRaise = this.shouldSkipRaiseForOffhandRemovalBefore();
            this.applyPending(player);
            if (skipRaise || this.shouldSkipRaiseForOffhandRemovalAfter(player)) {
                this.finishLoweringWithoutRaise(player);
            } else {
                this.startRaising(player);
            }
            return;
        }
        if (!this.isClipPlaying(this.activeHandOutClip)) {
            boolean skipRaise = this.shouldSkipRaiseForOffhandRemovalBefore();
            this.applyPending(player);
            if (skipRaise || this.shouldSkipRaiseForOffhandRemovalAfter(player)) {
                this.finishLoweringWithoutRaise(player);
            } else {
                this.startRaising(player);
            }
        }
    }

    private void handleRaising(class_310 client, class_746 player, class_1799 currentMain, class_1799 currentOff, boolean hotbarChanged) {
        this.revealPickupStackOnRaise(player);
        if (!this.shieldOppositeEquip && this.isLoopingActionClip()) {
            this.resetToCurrent(player);
            return;
        }
        if (!this.shieldOppositeEquip && this.isBlockingEquipClipActive(currentMain)) {
            this.pausedByUseClip = true;
            return;
        }
        if (this.isInterruptedByOtherClip()) {
            if (this.tryDetachOffhandDuringAction(player)) {
                return;
            }
            this.exitEquipPreservingSecondary(player);
            return;
        }
        boolean changedNow = this.updatePendingIfChanged(player, currentMain, currentOff, hotbarChanged);
        if (changedNow) {
            if (this.shouldForceDirectHandInOnRapidRepeat(player)) {
                this.forceDirectHandInFromPending(player);
            } else {
                this.startLowering(player);
            }
            return;
        }
        if (!this.isClipPlaying(this.activeHandInClip)) {
            this.finishRaise(player);
        }
    }

    private boolean shouldForceDirectHandInOnRapidRepeat(class_746 player) {
        return this.wasFunctionalSwapRecordedThisTick(player) || this.isFunctionalSwapBurstActive(player);
    }

    private void initialize(class_746 player) {
        this.renderedMain = this.copyStack(player.method_6047());
        this.renderedOff = this.copyStack(player.method_6079());
        this.lastHotbarSlot = this.resolveSelectedSlot(player);
        this.lastOffhandEmpty = this.renderedOff == null || this.renderedOff.method_7960();
        this.offhandAwaitingHandIn = false;
        this.offhandRevealPending = false;
        this.suppressOffhandRender = false;
        this.activeHandInClip = null;
        this.activeHandOutClip = null;
        this.activeHandInArm = null;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        this.pendingBlacklistHandInMain = false;
        this.pendingBlacklistHandInOff = false;
        this.blacklistTransitionHandIn = false;
        this.pickupRevealMainOnRaise = false;
        this.pickupRevealOffOnRaise = false;
        vanillaHandOutMainTicks = 0;
        vanillaHandOutOffTicks = 0;
        this.hotbarRapidWindowTicks = 0;
        this.hotbarChangeStreak = 0;
        this.recentFunctionalSwapTicks.clear();
        this.clearPendingActionClears();
        PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        this.initialized = true;
    }

    private void resetToCurrent(class_746 player) {
        this.resetEquipState(player, true);
        this.phase = Phase.IDLE;
        this.markEquipTransitionTick(player);
        this.applyEquipOverrides(player);
    }

    private void resetEquipState(class_746 player, boolean clearSecondaryClip) {
        this.renderedMain = this.copyStack(player.method_6047());
        this.renderedOff = this.copyStack(player.method_6079());
        this.pendingMainChange = false;
        this.pendingOffChange = false;
        this.deferSwapWithoutEquip = false;
        this.skipNextEquipMain = false;
        this.skipNextEquipOff = false;
        this.swapTriggered = false;
        this.cycleMain = false;
        this.cycleOff = false;
        this.suppressOffhandRender = false;
        this.offhandRevealPending = false;
        this.offhandAwaitingHandIn = false;
        this.lastOffhandEmpty = this.renderedOff == null || this.renderedOff.method_7960();
        this.activeHandInClip = null;
        this.activeHandOutClip = null;
        this.activeHandInArm = null;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        this.pendingBlacklistHandInMain = false;
        this.pendingBlacklistHandInOff = false;
        this.blacklistTransitionHandIn = false;
        vanillaHandOutMainTicks = 0;
        vanillaHandOutOffTicks = 0;
        this.hotbarRapidWindowTicks = 0;
        this.hotbarChangeStreak = 0;
        this.clearPendingActionClears();
        PunchyAnimationManager.POSE_HANDLER.setIgnoreDualHanded(false);
        if (clearSecondaryClip) {
            PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        }
    }

    private void exitEquipPreservingSecondary(class_746 player) {
        this.resetEquipState(player, false);
        this.phase = Phase.IDLE;
        this.markEquipTransitionTick(player);
        this.applyEquipOverrides(player);
    }

    private void syncRenderedItems(class_746 player) {
        this.renderedMain = this.copyStack(player.method_6047());
        this.renderedOff = this.copyStack(player.method_6079());
        this.pendingMainChange = false;
        this.pendingOffChange = false;
        this.deferSwapWithoutEquip = false;
        this.skipNextEquipMain = false;
        this.skipNextEquipOff = false;
        this.suppressOffhandRender = false;
        this.offhandRevealPending = false;
        this.offhandAwaitingHandIn = false;
        this.lastOffhandEmpty = this.renderedOff == null || this.renderedOff.method_7960();
        this.pendingBlacklistHandInMain = false;
        this.pendingBlacklistHandInOff = false;
        this.blacklistTransitionHandIn = false;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        vanillaHandOutMainTicks = 0;
        vanillaHandOutOffTicks = 0;
        this.hotbarRapidWindowTicks = 0;
        this.hotbarChangeStreak = 0;
        this.recentFunctionalSwapTicks.clear();
        this.clearPendingActionClears();
        PunchyAnimationManager.POSE_HANDLER.setIgnoreDualHanded(false);
        if (!this.shouldPreserveWalkOnWaterOffhandSecondary()) {
            PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        }
        this.applyEquipOverrides(player);
    }

    private void finishRaise(class_746 player) {
        this.phase = Phase.IDLE;
        this.cycleMain = false;
        this.cycleOff = false;
        this.deferSwapWithoutEquip = false;
        this.skipNextEquipMain = false;
        this.skipNextEquipOff = false;
        this.suppressOffhandRender = false;
        this.offhandRevealPending = false;
        this.offhandAwaitingHandIn = false;
        this.activeHandInClip = null;
        this.activeHandInArm = null;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        this.pendingBlacklistHandInMain = false;
        this.pendingBlacklistHandInOff = false;
        this.blacklistTransitionHandIn = false;
        vanillaHandOutMainTicks = 0;
        vanillaHandOutOffTicks = 0;
        boolean preserveWalkOnWaterSecondary = this.shouldPreserveWalkOnWaterOffhandSecondary();
        this.debugWalkOnWaterEquip("finishRaise", preserveWalkOnWaterSecondary);
        PunchyAnimationManager.POSE_HANDLER.setIgnoreDualHanded(false);
        if (!preserveWalkOnWaterSecondary) {
            PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        }
        this.clearPendingActionClears();
        this.markEquipTransitionTick(player);
        this.applyEquipOverrides(player);
        if (player != null) {
            PunchyClient.getWalkOnWaterStateMachine().reattachMainhandAfterEquip(player);
        }
        if (this.pendingMainChange || this.pendingOffChange) {
            this.restartRaisingFromPending(player);
            return;
        }
    }

    private void restartRaisingFromPending(class_746 player) {
        if (!this.pendingMainChange && !this.pendingOffChange) {
            return;
        }
        this.cycleMain = this.pendingMainChange;
        this.cycleOff = this.pendingOffChange;
        this.applyPending(player);
        this.startRaising(player);
    }

    private void forceDirectHandInFromPending(class_746 player) {
        if (!this.pendingMainChange && !this.pendingOffChange) {
            return;
        }
        this.clearSecondaryClipUnlessPreservingWalkOnWater("forceDirectHandInFromPending");
        this.activeHandOutClip = null;
        this.activeHandInClip = null;
        this.activeHandInArm = null;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        this.swapTriggered = true;
        this.cycleMain = this.pendingMainChange;
        this.cycleOff = this.pendingOffChange;
        this.applyPending(player);
        this.startRaising(player);
    }

    private void queuePending(class_746 player, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged, boolean hotbarChanged) {
        this.recordFunctionalSwapIfNeeded(player, currentMain, currentOff, mainChanged, offChanged, hotbarChanged);
        this.pendingMainChange = mainChanged;
        this.pendingOffChange = offChanged;
        if (mainChanged) {
            this.pendingMain = this.copyStack(currentMain);
        }
        if (offChanged) {
            this.pendingOff = this.copyStack(currentOff);
        }
    }

    private void startLowering(class_746 player) {
        boolean skipBlendIn;
        boolean ignoreDynamicDualHanded;
        class_1306 previousActionArm;
        class_310 client;
        boolean autoWalkOnWaterBlend;
        this.deferSwapWithoutEquip = false;
        this.cycleMain = this.pendingMainChange;
        this.cycleOff = this.pendingOffChange;
        if (!(this.cycleOff && this.pendingOff != null && this.pendingOff.method_7960() && this.renderedOff != null && this.renderedOff.method_7909() instanceof class_1835)) {
            this.hideOffhandTridentThrowCache = false;
        }
        boolean bl = autoWalkOnWaterBlend = this.isWalkOnWaterPoseActive() || CustomAnimationResolver.shouldAutoBlendFromWalkOnWater();
        if (autoWalkOnWaterBlend && this.shouldDelayWalkOnWaterHandOut()) {
            return;
        }
        boolean rapidHotbarLowering = this.cycleMain && this.isRapidHotbarScrollActive();
        this.shieldOppositeEquip = this.shouldRunOppositeHandEquipOverShield(player);
        if (!this.shieldOppositeEquip && !this.shouldPreserveCurrentClipForHandOut()) {
            this.stopConflictingClip();
        }
        EquipSelection selection = this.resolveHandOutClip();
        AnimationClip clip = selection.clip;
        boolean customSingleHand = this.cycleMain && this.cycleOff && selection.explicitCustom && selection.hand != null;
        boolean splitRebasedSwapHands = false;
        class_1268 customSingleHandOther = null;
        AnimationClip customSingleHandSecondary = null;
        if (customSingleHand) {
            class_1306 otherArm;
            client = class_310.method_1551();
            class_1268 other = selection.hand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
            AnimationClip defaultClip = PunchyAnimationManager.resolveHandedClip("hand_out", client, other);
            class_1306 class_13062 = otherArm = player == null ? null : this.resolveArm(player, other);
            if (defaultClip != null && !this.clipHasTracksForArm(selection.clip, otherArm)) {
                customSingleHandOther = other;
                customSingleHandSecondary = defaultClip;
                clip = selection.clip;
                CustomAnimationResolver.applyLoopSpeed(selection.entry);
            } else {
                customSingleHand = false;
                CustomAnimationResolver.applyLoopSpeed(selection.entry);
            }
        }
        if (clip == null) {
            CustomAnimationResolver.applyLoopSpeed(null);
            client = class_310.method_1551();
            class_1268 hand = this.cycleOff && !this.cycleMain ? class_1268.field_5810 : class_1268.field_5808;
            clip = PunchyAnimationManager.resolveHandedClip("hand_out", client, hand);
        } else if (!customSingleHand) {
            CustomAnimationResolver.applyLoopSpeed(selection.entry);
        }
        if (!customSingleHand && this.cycleMain && this.cycleOff && !autoWalkOnWaterBlend && this.shouldBlendHandOutFromCurrent() && player != null && (previousActionArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm()) != null) {
            class_1268 primaryHand = this.resolveHandForArm(player, previousActionArm);
            class_1268 otherHand = primaryHand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
            class_310 client2 = class_310.method_1551();
            AnimationClip primaryClip = PunchyAnimationManager.resolveHandedClip("hand_out", client2, primaryHand);
            AnimationClip secondaryClip = PunchyAnimationManager.resolveHandedClip("hand_out", client2, otherHand);
            if (primaryClip != null && secondaryClip != null) {
                clip = primaryClip;
                customSingleHandOther = otherHand;
                customSingleHandSecondary = secondaryClip;
                splitRebasedSwapHands = true;
            }
        }
        if (clip == null) {
            if (this.cycleMain) {
                ItemAnimationManager.forceTypeOnce(class_1268.field_5808, "hand_out");
            }
            if (this.cycleOff && !this.shouldSuppressOffhandHandOut(player, player.method_6079())) {
                ItemAnimationManager.forceTypeOnce(class_1268.field_5810, "hand_out");
            }
            this.clearSecondaryClipUnlessPreservingWalkOnWater("startLowering:noClip");
            this.applyPending(player);
            this.startRaising(player);
            return;
        }
        this.activeHandOutClip = clip;
        this.swapTriggered = false;
        this.applyEquipOverrides(player);
        class_1306 arm = this.resolveActiveArm(player);
        if (customSingleHand) {
            arm = this.resolveArm(player, selection.hand);
        } else if (splitRebasedSwapHands && player != null) {
            arm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        }
        if (autoWalkOnWaterBlend && this.cycleMain && this.cycleOff) {
            arm = null;
        }
        boolean forceSingleArmEquip = this.cycleMain ^ this.cycleOff;
        boolean customDualHandedEquip = selection.explicitCustom && PunchyAnimationManager.hasAnyDualHandedEffect(clip);
        boolean bl2 = ignoreDynamicDualHanded = autoWalkOnWaterBlend || forceSingleArmEquip && !customDualHandedEquip;
        if (!ignoreDynamicDualHanded && PunchyAnimationManager.isDualHandedActive(class_310.method_1551(), clip, 0.0f)) {
            arm = null;
        }
        float speedMultiplier = this.resolveEquipSpeedMultiplier();
        if (rapidHotbarLowering) {
            speedMultiplier *= 2.6f;
        }
        if (this.shieldOppositeEquip) {
            float handOutStartTime = this.resolveEquipBlendStartTime(clip);
            PunchyAnimationManager.POSE_HANDLER.setSecondaryClipAtTime(clip, arm, speedMultiplier, handOutStartTime);
            this.phase = Phase.LOWERING;
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setIgnoreDualHanded(ignoreDynamicDualHanded);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        this.preserveWalkOnWaterOffhandDuringMainhandEquip(player, autoWalkOnWaterBlend);
        if ((customSingleHand || splitRebasedSwapHands) && customSingleHandSecondary != null && customSingleHandOther != null) {
            class_1306 otherArm = this.resolveArm(player, customSingleHandOther);
            PunchyAnimationManager.POSE_HANDLER.setSecondaryClip(customSingleHandSecondary, otherArm, speedMultiplier);
        }
        boolean bl3 = skipBlendIn = rapidHotbarLowering || selection.entry == null || CustomAnimationResolver.shouldSkipBlendIn(selection.entry);
        if (autoWalkOnWaterBlend) {
            float handOutStartTime = this.resolveEquipBlendStartTime(clip);
            if (this.cycleMain && this.cycleOff) {
                PunchyAnimationManager.setSourceHand(class_310.method_1551(), class_1268.field_5810);
                PunchyAnimationManager.POSE_HANDLER.setRebaseUseBothArmsForNextPlay(true);
            } else {
                PunchyAnimationManager.setSourceHand(class_310.method_1551(), this.resolveHandForArm(player, arm));
                if (arm != null) {
                    PunchyAnimationManager.POSE_HANDLER.setRebaseSourceArmForNextPlay(arm);
                }
            }
            PunchyAnimationManager.POSE_HANDLER.setRebaseReturnSpeedNext(0.95f);
            PunchyAnimationManager.POSE_HANDLER.setBlendInDurationNext(0.0f);
            PunchyAnimationManager.POSE_HANDLER.playAtTimeRebasedFromCurrent(clip, speedMultiplier, false, handOutStartTime);
        } else if (this.shouldBlendHandOutFromSwim()) {
            float handOutStartTime = this.resolveEquipBlendStartTime(clip);
            PunchyAnimationManager.POSE_HANDLER.playAtTimeRebasedFromHandlerReturnToOrigin(PunchyAnimationManager.SWIM_HANDLER, clip, speedMultiplier, false, handOutStartTime);
        } else if (CustomAnimationResolver.shouldBlendFromCurrentOrigin(selection.entry, clip)) {
            float handOutStartTime = this.resolveEquipBlendStartTime(clip);
            PunchyAnimationManager.POSE_HANDLER.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, false, handOutStartTime);
        } else if (this.shouldBlendHandOutFromCurrent()) {
            float handOutStartTime = this.resolveEquipBlendStartTime(clip);
            PunchyAnimationManager.POSE_HANDLER.playAtTimeWithBlendFromCurrent(clip, speedMultiplier, false, handOutStartTime);
        } else {
            PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn, false);
        }
        this.phase = Phase.LOWERING;
    }

    private void startRaising(class_746 player) {
        boolean skipBlendIn;
        boolean ignoreDynamicDualHanded;
        class_310 client;
        class_1799 currentOff;
        if (this.detachedOffhandEquip && this.cycleOff && !this.cycleMain) {
            this.startDetachedOffhandRaising(player);
            return;
        }
        this.suppressOffhandRender = false;
        boolean autoWalkOnWaterBlend = CustomAnimationResolver.shouldAutoBlendFromWalkOnWater();
        if (!this.shieldOppositeEquip) {
            this.stopConflictingClip();
        }
        if (player != null && this.cycleOff && ((currentOff = player.method_6079()) == null || currentOff.method_7960())) {
            if (!this.cycleMain) {
                this.finishLoweringWithoutRaise(player);
                return;
            }
            this.cycleOff = false;
        }
        EquipSelection selection = this.resolveHandInClip(player);
        AnimationClip clip = selection.clip;
        boolean splitDualHandIn = false;
        EquipSelection splitMainSelection = null;
        EquipSelection splitOffSelection = null;
        if (this.cycleMain && this.cycleOff && player != null) {
            String offClipName;
            String mainClipName;
            class_1799 mainTarget = this.resolveRaiseTargetStack(class_1268.field_5808, this.pendingMain, this.renderedMain, player);
            class_1799 offTarget = this.resolveRaiseTargetStack(class_1268.field_5810, this.pendingOff, this.renderedOff, player);
            splitMainSelection = this.resolveCustomEquipClip(mainTarget, class_1268.field_5808, "hand_in");
            splitOffSelection = this.resolveCustomEquipClip(offTarget, class_1268.field_5810, "hand_in");
            if (splitMainSelection.clip != null && splitOffSelection.clip != null && !Objects.equals(mainClipName = this.clipName(splitMainSelection.clip), offClipName = this.clipName(splitOffSelection.clip))) {
                splitDualHandIn = true;
                selection = splitMainSelection;
                clip = selection.clip;
            }
        }
        boolean customSingleHand = !splitDualHandIn && this.cycleMain && this.cycleOff && selection.explicitCustom && selection.hand != null;
        class_1268 customSingleHandOther = null;
        AnimationClip customSingleHandSecondary = null;
        if (customSingleHand) {
            class_1306 otherArm;
            client = class_310.method_1551();
            class_1268 other = selection.hand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
            AnimationClip defaultClip = PunchyAnimationManager.resolveHandedClip("hand_in", client, other);
            class_1306 class_13062 = otherArm = player == null ? null : this.resolveArm(player, other);
            if (defaultClip != null && !this.clipHasTracksForArm(selection.clip, otherArm)) {
                customSingleHandOther = other;
                customSingleHandSecondary = defaultClip;
                clip = selection.clip;
                CustomAnimationResolver.applyLoopSpeed(selection.entry);
            } else {
                customSingleHand = false;
                CustomAnimationResolver.applyLoopSpeed(selection.entry);
            }
        }
        if (clip == null) {
            CustomAnimationResolver.applyLoopSpeed(null);
            client = class_310.method_1551();
            class_1268 hand = this.cycleOff && !this.cycleMain ? class_1268.field_5810 : class_1268.field_5808;
            clip = PunchyAnimationManager.resolveHandedClip("hand_in", client, hand);
        } else if (!customSingleHand) {
            CustomAnimationResolver.applyLoopSpeed(selection.entry);
        }
        if (clip == null) {
            if (this.cycleMain) {
                ItemAnimationManager.forceTypeOnce(class_1268.field_5808, "hand_in");
            }
            if (this.cycleOff) {
                ItemAnimationManager.forceTypeOnce(class_1268.field_5810, "hand_in");
            }
            PunchyAnimationManager.clearAllEquipOverrides();
            this.clearSecondaryClipUnlessPreservingWalkOnWater("startRaising:noClip");
            this.phase = Phase.IDLE;
            this.cycleMain = false;
            this.cycleOff = false;
            this.blacklistTransitionHandIn = false;
            this.pickupRevealMainOnRaise = false;
            this.pickupRevealOffOnRaise = false;
            return;
        }
        this.activeHandInClip = clip;
        this.applyEquipOverrides(player);
        if (!(this.shieldOppositeEquip || autoWalkOnWaterBlend && this.cycleMain && !this.cycleOff)) {
            this.clearSecondaryClipUnlessPreservingWalkOnWater("startRaising");
        }
        class_1306 poseArm = this.resolveActiveArm(player);
        if (customSingleHand) {
            poseArm = this.resolveArm(player, selection.hand);
        }
        boolean forceSingleArmEquip = this.cycleMain ^ this.cycleOff;
        boolean customDualHandedEquip = selection.explicitCustom && PunchyAnimationManager.hasAnyDualHandedEffect(clip);
        boolean bl = ignoreDynamicDualHanded = splitDualHandIn || autoWalkOnWaterBlend || forceSingleArmEquip && !customDualHandedEquip;
        if (splitDualHandIn) {
            poseArm = this.resolveArm(player, class_1268.field_5808);
        }
        if (!ignoreDynamicDualHanded && PunchyAnimationManager.isDualHandedActive(class_310.method_1551(), clip, 0.0f)) {
            poseArm = null;
        }
        this.activeHandInArm = customSingleHand ? null : poseArm;
        float speedMultiplier = this.resolveEquipSpeedMultiplier();
        if (this.shieldOppositeEquip) {
            PunchyAnimationManager.POSE_HANDLER.setSecondaryClipAtTime(clip, poseArm, speedMultiplier, 0.0f);
            this.blacklistTransitionHandIn = false;
            this.phase = Phase.RAISING;
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setIgnoreDualHanded(ignoreDynamicDualHanded);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(poseArm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        this.preserveWalkOnWaterOffhandDuringMainhandEquip(player, autoWalkOnWaterBlend);
        if (splitDualHandIn && splitOffSelection != null && splitOffSelection.clip != null && player != null) {
            class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
            PunchyAnimationManager.POSE_HANDLER.requestClearArm(offArm);
            PunchyAnimationManager.POSE_HANDLER.setSecondaryClip(splitOffSelection.clip, offArm, speedMultiplier);
        }
        if (customSingleHand && customSingleHandSecondary != null && customSingleHandOther != null) {
            class_1306 otherArm = this.resolveArm(player, customSingleHandOther);
            PunchyAnimationManager.POSE_HANDLER.setSecondaryClip(customSingleHandSecondary, otherArm, speedMultiplier);
        }
        boolean bl2 = skipBlendIn = this.blacklistTransitionHandIn || selection.entry == null || CustomAnimationResolver.shouldSkipBlendIn(selection.entry);
        if (autoWalkOnWaterBlend || CustomAnimationResolver.shouldBlendFromCurrentOrigin(selection.entry, clip) || this.shouldBlendHandInFromCurrentDuringSwim()) {
            if (autoWalkOnWaterBlend) {
                PunchyAnimationManager.POSE_HANDLER.setRebaseReturnSpeedNext(0.95f);
            }
            if (poseArm == null) {
                PunchyAnimationManager.POSE_HANDLER.setRebaseUseBothArmsForNextPlay(true);
            }
            PunchyAnimationManager.POSE_HANDLER.playAtTimeRebasedFromCurrentReturnToOrigin(clip, speedMultiplier, false, 0.0f);
        } else {
            PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn, false);
        }
        this.blacklistTransitionHandIn = false;
        this.phase = Phase.RAISING;
    }

    private void applyPending(class_746 player) {
        boolean deferOffForPickupReveal;
        boolean deferMainForPickupReveal = this.pickupRevealMainOnRaise && this.phase == Phase.LOWERING;
        boolean bl = deferOffForPickupReveal = this.pickupRevealOffOnRaise && this.phase == Phase.LOWERING;
        if (this.pendingMainChange && !deferMainForPickupReveal) {
            this.renderedMain = this.copyStack(this.pendingMain);
            this.pendingMainChange = false;
            this.pickupRevealMainOnRaise = false;
        }
        if (this.pendingOffChange && !deferOffForPickupReveal) {
            this.renderedOff = this.copyStack(this.pendingOff);
            this.pendingOffChange = false;
            this.pickupRevealOffOnRaise = false;
        }
        this.applyEquipOverrides(player);
    }

    private void revealPickupStackOnRaise(class_746 player) {
        boolean changed = false;
        if (this.phase != Phase.RAISING) {
            return;
        }
        if (this.pickupRevealMainOnRaise && this.pendingMainChange) {
            this.renderedMain = this.copyStack(this.pendingMain);
            this.pendingMainChange = false;
            this.pickupRevealMainOnRaise = false;
            changed = true;
        }
        if (this.pickupRevealOffOnRaise && this.pendingOffChange) {
            this.renderedOff = this.copyStack(this.pendingOff);
            this.pendingOffChange = false;
            this.pickupRevealOffOnRaise = false;
            changed = true;
        }
        if (changed) {
            this.applyEquipOverrides(player);
        }
    }

    private void applyEquipOverrides(class_746 player) {
        class_1306 mainArm = this.resolveArm(player, class_1268.field_5808);
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        PunchyAnimationManager.setEquipOverride(mainArm, this.renderedMain);
        if (this.cycleOff || !this.renderedOff.method_7960()) {
            if (this.suppressOffhandRender && (this.renderedOff == null || this.renderedOff.method_7960())) {
                PunchyAnimationManager.clearEquipOverride(offArm);
                return;
            }
            PunchyAnimationManager.setEquipOverride(offArm, this.renderedOff);
        } else {
            PunchyAnimationManager.clearEquipOverride(offArm);
        }
        if (this.detachedOffhandEquip && this.cycleOff && !this.cycleMain) {
            PunchyAnimationManager.POSE_HANDLER.requestClearArm(offArm);
        }
    }

    private boolean updatePendingIfChanged(class_746 player, class_1799 currentMain, class_1799 currentOff, boolean hotbarChanged) {
        class_1799 previousOff;
        boolean mainChanged = false;
        boolean offChanged = false;
        class_1799 previousMain = this.pendingMainChange ? this.pendingMain : this.renderedMain;
        class_1799 class_17992 = previousOff = this.pendingOffChange ? this.pendingOff : this.renderedOff;
        mainChanged = this.pendingMainChange ? this.hasItemChanged(this.pendingMain, currentMain) : this.hasItemChanged(this.renderedMain, currentMain) || hotbarChanged && this.shouldAnimateHotbarChange(this.renderedMain, currentMain);
        offChanged = this.pendingOffChange ? this.hasItemChanged(this.pendingOff, currentOff) : this.hasItemChanged(this.renderedOff, currentOff);
        this.recordFunctionalSwapIfNeeded(player, previousMain, previousOff, currentMain, currentOff, mainChanged, offChanged, hotbarChanged);
        if (mainChanged) {
            this.pendingMainChange = true;
            this.pendingMain = this.copyStack(currentMain);
        }
        if (offChanged) {
            this.pendingOffChange = true;
            this.pendingOff = this.copyStack(currentOff);
        }
        return mainChanged || offChanged;
    }

    private boolean shouldBlockEquip(class_310 client, class_1799 currentMain, boolean mainChanged, boolean offChanged) {
        if (client == null || client.field_1724 == null) {
            return true;
        }
        if (TridentStateMachine.isRecentActionBlockingEquip(client)) {
            return true;
        }
        if (this.isLoopingActionClip()) {
            return true;
        }
        if (ConsumeStateMachine.isEquipBlockedByConsumeAnimation()) {
            return true;
        }
        if (client.field_1724.method_6115() && !this.isUsingStaleSwappedItem(client.field_1724) && !this.isOppositeHandSwapDuringShieldUse(client.field_1724, mainChanged, offChanged)) {
            return true;
        }
        return this.isBlockingEquipClipActive(currentMain);
    }

    private boolean isUsingStaleSwappedItem(class_746 player) {
        if (player == null || !player.method_6115()) {
            return false;
        }
        class_1799 useStack = player.method_6030();
        if (useStack == null || useStack.method_7960()) {
            return false;
        }
        class_1268 usedHand = player.method_6058();
        if (usedHand == null) {
            return false;
        }
        class_1799 currentStack = player.method_5998(usedHand);
        return currentStack == null || currentStack.method_7960() || !class_1799.method_31577((class_1799)currentStack, (class_1799)useStack);
    }

    private boolean isOppositeHandSwapDuringShieldUse(class_746 player, boolean mainChanged, boolean offChanged) {
        if (player == null) {
            return false;
        }
        class_1268 usedHand = this.resolveActiveShieldHand(player);
        if (usedHand == null) {
            return false;
        }
        return usedHand == class_1268.field_5810 && mainChanged && !offChanged || usedHand == class_1268.field_5808 && offChanged && !mainChanged;
    }

    private boolean shouldRunOppositeHandEquipOverShield(class_746 player) {
        return this.isOppositeHandSwapDuringShieldUse(player, this.cycleMain, this.cycleOff);
    }

    private boolean shouldDeferRenderedSyncDuringShieldOppositeEquip() {
        return this.shieldOppositeEquip && this.phase != Phase.IDLE && (this.cycleMain && !this.cycleOff || this.cycleOff && !this.cycleMain);
    }

    private class_1268 resolveActiveShieldHand(class_746 player) {
        class_1799 useStack;
        if (player == null) {
            return null;
        }
        if (player.method_6115() && (useStack = player.method_6030()) != null && useStack.method_7909() instanceof class_1819) {
            return player.method_6058();
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1690 == null || !client.field_1690.field_1904.method_1434()) {
            return null;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying() || !PunchyAnimationManager.isClipNamed(clip, "use_shield")) {
            return null;
        }
        if (player.method_6079().method_7909() instanceof class_1819) {
            return class_1268.field_5810;
        }
        if (player.method_6047().method_7909() instanceof class_1819) {
            return class_1268.field_5808;
        }
        return null;
    }

    private boolean isInterruptedByOtherClip() {
        AnimationClip secondary;
        if (this.detachedOffhandEquip && this.cycleOff && !this.cycleMain && (secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip()) != null && this.isEquipClip(secondary)) {
            return false;
        }
        if (this.shieldOppositeEquip && (secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip()) != null && this.isEquipClip(secondary)) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (this.isBlockingEquipClip(clip)) {
            return false;
        }
        return PunchyAnimationManager.POSE_HANDLER.isPlaying() && !this.isEquipClip(clip);
    }

    private void stopConflictingClip() {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || this.isEquipClip(clip) || this.isBlockingEquipClip(clip)) {
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        PunchyAnimationManager.clearSourceHand();
    }

    public static boolean isEquipInProgress() {
        return equipActive;
    }

    public static boolean isOppositeShieldEquipInProgress(class_1268 shieldHand) {
        if (instance == null || shieldHand == null || !HandEquipStateMachine.instance.shieldOppositeEquip) {
            return false;
        }
        return shieldHand == class_1268.field_5810 && HandEquipStateMachine.instance.cycleMain && !HandEquipStateMachine.instance.cycleOff || shieldHand == class_1268.field_5808 && HandEquipStateMachine.instance.cycleOff && !HandEquipStateMachine.instance.cycleMain;
    }

    public static String getActiveType() {
        return activeType;
    }

    public static boolean shouldTreatHandOutActive() {
        if ("hand_out".equals(activeType)) {
            return true;
        }
        return handOutGraceTicks > 0;
    }

    public static boolean shouldAllowVanillaHandOut(class_1268 hand) {
        class_1268 resolved;
        if (HandEquipStateMachine.isDualHandedPoseBlockingVanillaHandOut()) {
            return false;
        }
        class_1268 class_12682 = resolved = hand == null ? class_1268.field_5808 : hand;
        if (resolved == class_1268.field_5810) {
            return vanillaHandOutOffTicks > 0;
        }
        return vanillaHandOutMainTicks > 0;
    }

    public static boolean shouldKeepOffhandVisibleUntilHandOutEnds() {
        if (instance == null) {
            return false;
        }
        return HandEquipStateMachine.instance.phase == Phase.LOWERING && HandEquipStateMachine.instance.cycleOff && HandEquipStateMachine.instance.pendingOffChange && (HandEquipStateMachine.instance.pendingOff == null || HandEquipStateMachine.instance.pendingOff.method_7960());
    }

    public static boolean isOffhandEquipTransitionActive() {
        if (instance == null) {
            return false;
        }
        if (HandEquipStateMachine.instance.phase == Phase.IDLE) {
            return false;
        }
        return HandEquipStateMachine.instance.cycleOff || HandEquipStateMachine.instance.pendingOffChange || HandEquipStateMachine.instance.offhandRevealPending || HandEquipStateMachine.instance.offhandAwaitingHandIn;
    }

    public static boolean cancelOffhandTransitionForPoseOverride(class_746 player) {
        if (instance == null || player == null) {
            return false;
        }
        return instance.cancelOffhandTransitionForPoseOverrideInternal(player);
    }

    public static boolean shouldHideIncomingOffhandUntilHandInStarts(class_746 player) {
        if (instance == null || player == null) {
            return false;
        }
        class_1799 currentOff = player.method_6079();
        if (currentOff == null || currentOff.method_7960()) {
            return false;
        }
        if (instance.isHandInActive()) {
            boolean offhandHandInActive;
            class_1306 offArm = instance.resolveArm(player, class_1268.field_5810);
            boolean bl = offhandHandInActive = HandEquipStateMachine.instance.cycleOff || HandEquipStateMachine.instance.activeHandInArm == null || HandEquipStateMachine.instance.activeHandInArm == offArm;
            if (offhandHandInActive) {
                return false;
            }
        }
        boolean renderedEmpty = HandEquipStateMachine.instance.renderedOff == null || HandEquipStateMachine.instance.renderedOff.method_7960();
        boolean pendingHasItem = HandEquipStateMachine.instance.pendingOff != null && !HandEquipStateMachine.instance.pendingOff.method_7960();
        return HandEquipStateMachine.instance.offhandRevealPending || HandEquipStateMachine.instance.offhandAwaitingHandIn && !instance.isHandInActive() || renderedEmpty && !pendingHasItem;
    }

    public static class_1799 getVisualOffhandStack(class_746 player) {
        class_1799 off;
        boolean offhandTransitionActive;
        if (player == null) {
            return class_1799.field_8037;
        }
        if (instance == null) {
            class_1799 off2 = player.method_6079();
            return off2 == null ? class_1799.field_8037 : off2.method_7972();
        }
        boolean bl = offhandTransitionActive = HandEquipStateMachine.instance.cycleOff || HandEquipStateMachine.instance.pendingOffChange || HandEquipStateMachine.instance.detachedOffhandEquip || HandEquipStateMachine.instance.offhandRevealPending || HandEquipStateMachine.instance.offhandAwaitingHandIn;
        if (offhandTransitionActive) {
            if (HandEquipStateMachine.instance.renderedOff != null && !HandEquipStateMachine.instance.renderedOff.method_7960()) {
                return HandEquipStateMachine.instance.renderedOff.method_7972();
            }
            if (HandEquipStateMachine.instance.pendingOff != null && !HandEquipStateMachine.instance.pendingOff.method_7960()) {
                return HandEquipStateMachine.instance.pendingOff.method_7972();
            }
        }
        return (off = player.method_6079()) == null ? class_1799.field_8037 : off.method_7972();
    }

    private static boolean isDualHandedPoseBlockingVanillaHandOut() {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        return clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.hasAnyDualHandedEffect(clip);
    }

    public static boolean abortEquipForAction(class_310 client) {
        return HandEquipStateMachine.abortEquipForAction(client, null);
    }

    public static boolean abortEquipForAction(class_310 client, class_1268 hand) {
        if (instance == null || client == null || client.field_1724 == null) {
            return false;
        }
        return instance.abortEquipForActionInternal(client.field_1724, hand);
    }

    public static boolean prepareOffhandForMainhandAction(class_310 client) {
        if (instance == null || client == null || client.field_1724 == null) {
            return false;
        }
        return instance.prepareDetachedOffhandForMainhandAction(client.field_1724);
    }

    public static void requestFlightOffhandLandingHide(class_746 player) {
        if (instance == null || player == null) {
            return;
        }
        instance.requestFlightOffhandLandingHideInternal(player);
    }

    private boolean abortEquipForActionInternal(class_746 player, class_1268 hand) {
        class_1306 actionArm;
        if (!equipActive) {
            return false;
        }
        if (HandEquipStateMachine.isOppositeShieldEquipInProgress(hand)) {
            return true;
        }
        if (hand == class_1268.field_5808 && this.prepareDetachedOffhandForMainhandAction(player)) {
            return true;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        class_1306 class_13062 = actionArm = hand == null ? null : this.resolveArm(player, hand);
        if (clip != null && this.isEquipClip(clip) && PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            class_1306 equipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
            if (actionArm != null && equipArm != null && equipArm != actionArm) {
                return true;
            }
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            PunchyAnimationManager.clearSourceHand();
        }
        this.resetToCurrent(player);
        return true;
    }

    private boolean prepareDetachedOffhandForMainhandAction(class_746 player) {
        if (player == null || this.detachedOffhandEquip || !this.cycleOff) {
            return false;
        }
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        PoseHandlerState preserved = this.captureOffhandEquipState(offArm);
        if (preserved == null) {
            return false;
        }
        this.renderedMain = this.copyStack(player.method_6047());
        this.pendingMain = this.copyStack(player.method_6047());
        this.pendingMainChange = false;
        this.cycleMain = false;
        this.skipNextEquipMain = false;
        this.pendingBlacklistHandInMain = false;
        vanillaHandOutMainTicks = 0;
        this.clearPendingMainActionClear();
        this.detachedOffhandEquip = true;
        if (this.phase == Phase.LOWERING) {
            this.activeHandOutClip = preserved.clip();
            this.swapTriggered = true;
            this.applyPending(player);
            this.startDetachedOffhandRaising(player);
            this.debugOffhandDetach("prepareDetachedOffhandForMainhandAction.forceRaise", player, player.method_6079());
            return true;
        }
        if (this.phase == Phase.RAISING) {
            this.activeHandInClip = preserved.clip();
            this.activeHandInArm = offArm;
        }
        PunchyAnimationManager.POSE_HANDLER.setSecondaryClipAtTime(preserved.clip(), offArm, this.resolveEquipSpeedMultiplier(), preserved.time());
        this.debugOffhandDetach("prepareDetachedOffhandForMainhandAction", player, player.method_6079());
        this.applyEquipOverrides(player);
        return true;
    }

    private void requestFlightOffhandLandingHideInternal(class_746 player) {
        if (player == null || this.phase != Phase.IDLE) {
            return;
        }
        class_1799 currentOff = player.method_6079();
        if (currentOff != null && !currentOff.method_7960()) {
            return;
        }
        if (player.method_6128() || player.method_31549().field_7479 || !player.method_24828()) {
            return;
        }
        if (this.pendingMainChange || this.pendingOffChange || this.cycleMain || this.cycleOff) {
            return;
        }
        this.pendingMainChange = false;
        this.pendingOffChange = true;
        this.pendingOff = class_1799.field_8037;
        this.suppressOffhandRender = false;
        this.offhandRevealPending = false;
        this.offhandAwaitingHandIn = false;
        this.startLowering(player);
    }

    private PoseHandlerState captureOffhandEquipState(class_1306 offArm) {
        AnimationClip secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        if (secondary != null && this.isEquipClip(secondary) && PunchyAnimationManager.POSE_HANDLER.getSecondaryArm() == offArm) {
            return new PoseHandlerState(secondary, PunchyAnimationManager.POSE_HANDLER.getSecondaryTime());
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null || !this.isEquipClip(current)) {
            return null;
        }
        class_1306 activeArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (activeArm != null && activeArm != offArm) {
            return null;
        }
        return new PoseHandlerState(current, PunchyAnimationManager.POSE_HANDLER.getCurrentTime());
    }

    private boolean isEquipClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        if (clip == this.activeHandInClip || clip == this.activeHandOutClip) {
            return true;
        }
        String name = clip.getName();
        if (name != null && (name.equals("hand_in") || name.equals("hand_out") || name.startsWith("hand_in_") || name.startsWith("hand_out_"))) {
            return true;
        }
        return PunchyAnimationManager.isClipNamed(clip, "hand_in") || PunchyAnimationManager.isClipNamed(clip, "hand_out");
    }

    private boolean isLoopingActionClip() {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        if (this.isEquipClip(clip) || this.isBlockingEquipClip(clip)) {
            return false;
        }
        if (this.isInspectLikeClip(clip)) {
            return false;
        }
        return clip.isLoop() || PunchyAnimationManager.POSE_HANDLER.isForceLooping();
    }

    private boolean isActionClipActive() {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        return !this.isEquipClip(clip) && !this.isBlockingEquipClip(clip);
    }

    private boolean updatePendingActionClearMain(class_1799 currentMain, class_1799 currentOff) {
        if (currentMain != null && !currentMain.method_7960()) {
            this.clearPendingMainActionClear();
            return false;
        }
        if (this.pendingMainActionClear) {
            if (this.shouldFinishPendingActionClear(this.pendingMainActionClearClip, this.pendingMainActionClearTime)) {
                this.renderedMain = class_1799.field_8037;
                this.pendingMainChange = false;
                this.clearPendingMainActionClear();
                return false;
            }
            return true;
        }
        if (!this.shouldDelayActionClear(currentMain, currentOff, this.renderedMain, this.renderedOff, this.pendingMainChange, this.cycleMain)) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        this.pendingMainActionClear = true;
        this.pendingMainActionClearClip = clip;
        this.pendingMainActionClearTime = this.resolveActionClearTime(clip);
        return true;
    }

    private boolean updatePendingActionClearOff(class_1799 currentOff, class_1799 currentMain) {
        if (currentOff != null && !currentOff.method_7960()) {
            this.clearPendingOffActionClear();
            return false;
        }
        if (this.pendingOffActionClear) {
            if (this.shouldFinishPendingActionClear(this.pendingOffActionClearClip, this.pendingOffActionClearTime)) {
                this.renderedOff = class_1799.field_8037;
                this.pendingOffChange = false;
                this.clearPendingOffActionClear();
                return false;
            }
            return true;
        }
        if (!this.shouldDelayActionClear(currentOff, currentMain, this.renderedOff, this.renderedMain, this.pendingOffChange, this.cycleOff)) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        this.pendingOffActionClear = true;
        this.pendingOffActionClearClip = clip;
        this.pendingOffActionClearTime = this.resolveActionClearTime(clip);
        return true;
    }

    private boolean shouldDelayActionClear(class_1799 current, class_1799 oppositeCurrent, class_1799 rendered, class_1799 oppositeRendered, boolean pendingChange, boolean cycle) {
        if (!this.isActionClipActive()) {
            return false;
        }
        if (this.isFunctionalOffhandSwap(rendered, oppositeRendered, current, oppositeCurrent, true, true, false)) {
            return false;
        }
        if (this.shouldPreserveCurrentClipForHandOut() && !this.shouldFinishUseClipBeforeClearing(current, rendered)) {
            return false;
        }
        if (this.phase != Phase.IDLE || pendingChange || cycle) {
            return false;
        }
        return current != null && current.method_7960() && rendered != null && !rendered.method_7960();
    }

    private boolean shouldFinishUseClipBeforeClearing(class_1799 current, class_1799 rendered) {
        if (current == null || !current.method_7960() || rendered == null || rendered.method_7960()) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        return ItemAnimationManager.isTypeActive("useItem") || PunchyAnimationManager.isClipNamed(clip, "interact");
    }

    private float resolveActionClearTime(AnimationClip clip) {
        if (clip == null) {
            return 0.0f;
        }
        return Math.max(0.0f, clip.getLength() * 0.5f);
    }

    private boolean shouldFinishPendingActionClear(AnimationClip expectedClip, float clearTime) {
        AnimationClip currentClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (expectedClip == null || currentClip != expectedClip) {
            return true;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return true;
        }
        return PunchyAnimationManager.POSE_HANDLER.getCurrentTime() >= clearTime;
    }

    private void clearPendingActionClears() {
        this.clearPendingMainActionClear();
        this.clearPendingOffActionClear();
    }

    private void clearPendingMainActionClear() {
        this.pendingMainActionClear = false;
        this.pendingMainActionClearClip = null;
        this.pendingMainActionClearTime = 0.0f;
    }

    private void clearPendingOffActionClear() {
        this.pendingOffActionClear = false;
        this.pendingOffActionClearClip = null;
        this.pendingOffActionClearTime = 0.0f;
    }

    private boolean shouldBlendHandOutFromCurrent() {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying() && !PunchyAnimationManager.POSE_HANDLER.isBlendOutActive()) {
            return false;
        }
        if (this.isBlockingEquipClip(current)) {
            return false;
        }
        if (current == this.activeHandInClip || PunchyAnimationManager.isClipNamed(current, "hand_in")) {
            return true;
        }
        return !this.isEquipClip(current);
    }

    private boolean shouldBlendHandInFromCurrentDuringSwim() {
        if (!PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            return false;
        }
        AnimationClip swimClip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
        if (swimClip != PunchyAnimationManager.SWIMING_CLIP && swimClip != PunchyAnimationManager.SWIMING_ABOVE_CLIP) {
            return false;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying() && !PunchyAnimationManager.POSE_HANDLER.isBlendOutActive()) {
            return false;
        }
        if (this.isBlockingEquipClip(current)) {
            return false;
        }
        return current == this.activeHandOutClip || PunchyAnimationManager.isClipNamed(current, "hand_out");
    }

    private boolean shouldBlendHandOutFromSwim() {
        if (!PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            return false;
        }
        AnimationClip swimClip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
        if (swimClip != PunchyAnimationManager.SWIMING_CLIP && swimClip != PunchyAnimationManager.SWIMING_ABOVE_CLIP) {
            return false;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        return current == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying() && !PunchyAnimationManager.POSE_HANDLER.isBlendOutActive();
    }

    private boolean isInspectLikeClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        return name.trim().toLowerCase(Locale.ROOT).contains("inspect");
    }

    private boolean isWalkOnWaterPoseActive() {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null) {
            return false;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying() && !PunchyAnimationManager.POSE_HANDLER.isBlendOutActive()) {
            return false;
        }
        String name = current.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("walk_on_water_start") || normalized.contains("walk_on_water_loop") || normalized.contains("walk_on_water_end");
    }

    private void preserveWalkOnWaterOffhandDuringMainhandEquip(class_746 player, boolean autoWalkOnWaterBlend) {
        if (!autoWalkOnWaterBlend || player == null || !this.cycleMain || this.cycleOff) {
            return;
        }
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        AnimationClip secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        if (secondary != null && PunchyAnimationManager.POSE_HANDLER.getSecondaryArm() == offArm) {
            this.syncWalkOnWaterSecondaryPause(secondary);
            return;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (current == null || !this.containsWalkOnWaterName(current)) {
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setSecondaryClipAtTime(current, offArm, PunchyAnimationManager.clipSpeedMultiplier(current), PunchyAnimationManager.POSE_HANDLER.getCurrentTime());
        this.syncWalkOnWaterSecondaryPause(current);
    }

    private void syncWalkOnWaterSecondaryPause(AnimationClip clip) {
        if (clip == null || !this.containsWalkOnWaterLoopName(clip)) {
            PunchyAnimationManager.POSE_HANDLER.setSecondaryPaused(false);
            return;
        }
        class_310 client = class_310.method_1551();
        boolean movementActive = client != null && client.field_1690 != null && (client.field_1690.field_1894.method_1434() || client.field_1690.field_1881.method_1434() || client.field_1690.field_1913.method_1434() || client.field_1690.field_1849.method_1434());
        PunchyAnimationManager.POSE_HANDLER.setSecondaryPaused(!movementActive);
    }

    private boolean shouldPreserveWalkOnWaterOffhandSecondary() {
        class_746 player;
        if (this.cycleOff || this.pendingOffChange || this.detachedOffhandEquip || this.offhandRevealPending || this.offhandAwaitingHandIn) {
            return false;
        }
        AnimationClip secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        class_1306 secondaryArm = PunchyAnimationManager.POSE_HANDLER.getSecondaryArm();
        if (secondary == null || secondaryArm == null || !this.containsWalkOnWaterName(secondary)) {
            return false;
        }
        class_310 client = class_310.method_1551();
        class_746 class_7462 = player = client == null ? null : client.field_1724;
        if (player == null) {
            return false;
        }
        return secondaryArm == this.resolveArm(player, class_1268.field_5810);
    }

    private void debugWalkOnWaterEquip(String event, boolean preserveSecondary) {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        AnimationClip secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
    }

    private void clearSecondaryClipUnlessPreservingWalkOnWater(String event) {
        boolean preserveWalkOnWaterSecondary = this.shouldPreserveWalkOnWaterOffhandSecondary();
        this.debugWalkOnWaterEquip(event, preserveWalkOnWaterSecondary);
        if (!preserveWalkOnWaterSecondary) {
            PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        }
    }

    private static String debugClipName(AnimationClip clip) {
        return clip == null ? "null" : String.valueOf(clip.getName());
    }

    private boolean shouldDelayWalkOnWaterHandOut() {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (!this.containsWalkOnWaterStartName(current)) {
            return false;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        return PunchyAnimationManager.POSE_HANDLER.getCurrentTime() <= 5.0E-4f;
    }

    private boolean containsWalkOnWaterStartName(AnimationClip clip) {
        return this.containsWalkOnWaterNamedPhase(clip, "walk_on_water_start");
    }

    private boolean containsWalkOnWaterLoopName(AnimationClip clip) {
        return this.containsWalkOnWaterNamedPhase(clip, "walk_on_water_loop");
    }

    private boolean containsWalkOnWaterName(AnimationClip clip) {
        return this.containsWalkOnWaterNamedPhase(clip, "walk_on_water_");
    }

    private boolean containsWalkOnWaterNamedPhase(AnimationClip clip, String needle) {
        if (clip == null || clip.getName() == null) {
            return false;
        }
        String normalized = clip.getName().trim().toLowerCase(Locale.ROOT);
        return normalized.contains(needle);
    }

    private boolean shouldPreserveCurrentClipForHandOut() {
        return this.shouldBlendHandOutFromCurrent();
    }

    private float resolveEquipBlendStartTime(AnimationClip clip) {
        if (clip == null) {
            return 0.0f;
        }
        float firstActiveTime = Float.POSITIVE_INFINITY;
        block0: for (BoneTrack track : clip.getTracks().values()) {
            if (track == null) continue;
            for (Keyframe keyframe : track.getKeyframes()) {
                float time;
                if (keyframe == null || (time = keyframe.timeStamp()) <= 0.0f || this.isNeutralKeyframe(keyframe)) continue;
                firstActiveTime = Math.min(firstActiveTime, time);
                continue block0;
            }
        }
        if (!Float.isFinite(firstActiveTime)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(firstActiveTime, clip.getLength()));
    }

    private boolean isNeutralKeyframe(Keyframe keyframe) {
        return this.isZeroVector(keyframe.position()) && this.isZeroVector(keyframe.rotationEuler()) && this.isIdentityScale(keyframe.scale());
    }

    private boolean isZeroVector(Vector3f vector) {
        return vector == null || Math.abs(vector.x) < 1.0E-4f && Math.abs(vector.y) < 1.0E-4f && Math.abs(vector.z) < 1.0E-4f;
    }

    private boolean clipHasTracksForArm(AnimationClip clip, class_1306 arm) {
        if (clip == null || arm == null || clip.getTracks().isEmpty()) {
            return false;
        }
        for (String boneName : clip.getTracks().keySet()) {
            if (!this.isBoneForArm(boneName, arm)) continue;
            return true;
        }
        return false;
    }

    private boolean isBoneForArm(String boneName, class_1306 arm) {
        if (boneName == null || arm == null) {
            return false;
        }
        String normalized = boneName.toLowerCase(Locale.ROOT);
        if (arm == class_1306.field_6182) {
            return normalized.contains("left");
        }
        return normalized.contains("right");
    }

    private boolean isIdentityScale(Vector3f vector) {
        return vector == null || Math.abs(vector.x - 1.0f) < 1.0E-4f && Math.abs(vector.y - 1.0f) < 1.0E-4f && Math.abs(vector.z - 1.0f) < 1.0E-4f;
    }

    private boolean isBlockingEquipClip(AnimationClip clip) {
        return PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide") || PunchyAnimationManager.isClipNamed(clip, "use_crossbow_shot");
    }

    private boolean isBlockingEquipClipActive(class_1799 currentMain) {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        return PunchyAnimationManager.POSE_HANDLER.isPlaying() && this.isBlockingEquipClip(clip);
    }

    private void clearRenderedEmptyHandsWhileUseBlocksEquip(class_746 player, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged) {
        if (player == null || !player.method_6115()) {
            return;
        }
        class_1268 usedHand = player.method_6058();
        if (mainChanged && usedHand != class_1268.field_5808 && (currentMain == null || currentMain.method_7960())) {
            this.renderedMain = class_1799.field_8037;
        }
        if (offChanged && usedHand != class_1268.field_5810 && (currentOff == null || currentOff.method_7960())) {
            this.renderedOff = class_1799.field_8037;
        }
    }

    private void resumeEquipClip(class_746 player) {
        if (player == null) {
            return;
        }
        if (this.phase == Phase.LOWERING && !this.isClipPlaying(this.activeHandOutClip)) {
            this.startLowering(player);
            return;
        }
        if (this.phase == Phase.RAISING && !this.isClipPlaying(this.activeHandInClip)) {
            this.startRaising(player);
        }
    }

    private boolean shouldSkipRaiseForOffhandRemovalBefore() {
        if (this.cycleMain || !this.cycleOff) {
            return false;
        }
        if (!this.pendingOffChange) {
            return false;
        }
        return this.pendingOff == null || this.pendingOff.method_7960();
    }

    private boolean shouldSkipRaiseForOffhandRemovalAfter(class_746 player) {
        if (this.cycleMain || !this.cycleOff) {
            return false;
        }
        if (this.renderedOff == null || this.renderedOff.method_7960()) {
            class_1799 currentOff = player == null ? class_1799.field_8037 : player.method_6079();
            return currentOff == null || currentOff.method_7960();
        }
        return false;
    }

    private void finishLoweringWithoutRaise(class_746 player) {
        this.phase = Phase.IDLE;
        this.cycleMain = false;
        this.cycleOff = false;
        this.swapTriggered = false;
        this.suppressOffhandRender = false;
        this.offhandRevealPending = false;
        this.offhandAwaitingHandIn = false;
        this.activeHandOutClip = null;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        this.pendingBlacklistHandInMain = false;
        this.pendingBlacklistHandInOff = false;
        this.blacklistTransitionHandIn = false;
        vanillaHandOutMainTicks = 0;
        vanillaHandOutOffTicks = 0;
        boolean preserveWalkOnWaterSecondary = this.shouldPreserveWalkOnWaterOffhandSecondary();
        this.debugWalkOnWaterEquip("finishLoweringWithoutRaise", preserveWalkOnWaterSecondary);
        PunchyAnimationManager.POSE_HANDLER.setIgnoreDualHanded(false);
        if (!preserveWalkOnWaterSecondary) {
            PunchyAnimationManager.POSE_HANDLER.clearSecondaryClip();
        }
        this.markEquipTransitionTick(player);
        this.applyEquipOverrides(player);
        if (player != null) {
            PunchyClient.getWalkOnWaterStateMachine().reattachMainhandAfterEquip(player);
        }
        if (this.pendingMainChange || this.pendingOffChange) {
            this.startLowering(player);
        }
    }

    private void markEquipTransitionTick(class_746 player) {
        if (player == null) {
            return;
        }
        lastEquipTransitionPlayerTick = player.field_6012;
    }

    public static boolean wasEquipTransitionRecent(class_310 client, int maxTicks) {
        if (client == null || client.field_1724 == null || maxTicks < 0) {
            return false;
        }
        int mark = lastEquipTransitionPlayerTick;
        if (mark < 0) {
            return false;
        }
        int now = client.field_1724.field_6012;
        return now >= mark && now - mark <= maxTicks;
    }

    private boolean shouldBypassTridentThrowBlock(class_1799 currentMain) {
        if (currentMain == null || currentMain.method_7960()) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (!(PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide"))) {
            return false;
        }
        return this.hasItemChanged(this.renderedMain, currentMain);
    }

    public boolean isOffhandRenderSuppressed() {
        if (UseActionTracker.wasRecentUseForHand(class_1268.field_5810, 1800000000L)) {
            return false;
        }
        return this.suppressOffhandRender || this.offhandAwaitingHandIn && !this.isHandInActive();
    }

    public static boolean shouldHideOffhandTridentThrowCacheRender() {
        HandEquipStateMachine instance = HandEquipStateMachine.instance;
        return instance != null && instance.hideOffhandTridentThrowCache && instance.phase == Phase.LOWERING && instance.cycleOff;
    }

    public boolean isHandInActiveForOffhand() {
        return this.isHandInActive();
    }

    private boolean shouldSuppressOffhandHandOut(class_746 player, class_1799 currentOff) {
        if (!HandEquipStateMachine.isFabricPlatform()) {
            return false;
        }
        return currentOff == null || currentOff.method_7960();
    }

    private static boolean isFabricPlatform() {
        try {
            return Services.PLATFORM != null && "fabric".equalsIgnoreCase(Services.PLATFORM.getPlatformName());
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public boolean shouldLowerOffhand(class_746 player) {
        if (player == null) {
            return false;
        }
        if (UseActionTracker.wasRecentUseForHand(class_1268.field_5810, 1800000000L)) {
            return false;
        }
        if (this.suppressOffhandRender || this.offhandRevealPending) {
            return true;
        }
        if (this.offhandAwaitingHandIn) {
            if (!this.isHandInActive()) {
                return true;
            }
            class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
            return this.activeHandInArm != null && this.activeHandInArm != offArm;
        }
        return false;
    }

    private boolean shouldSuppressOffhandRender(class_1799 currentOff) {
        if (this.offhandRevealPending) {
            return true;
        }
        return this.phase == Phase.LOWERING && this.cycleOff && (this.renderedOff == null || this.renderedOff.method_7960());
    }

    private void updateOffhandRevealPending(class_746 player, class_1799 currentOff, boolean offChanged) {
        boolean currentHasItem;
        if (this.isHandInActive() && this.shouldClearOffhandReveal(player)) {
            this.offhandRevealPending = false;
            return;
        }
        boolean renderedEmpty = this.renderedOff == null || this.renderedOff.method_7960();
        boolean bl = currentHasItem = currentOff != null && !currentOff.method_7960();
        if (renderedEmpty && currentHasItem && (offChanged || this.pendingOffChange)) {
            this.offhandRevealPending = true;
            return;
        }
        if (!(currentHasItem || this.pendingOff != null && !this.pendingOff.method_7960() || this.pendingOffChange)) {
            this.offhandRevealPending = false;
        }
    }

    private void updateOffhandAwaitingHandIn(class_746 player, class_1799 currentOff) {
        boolean currentEmpty;
        boolean handInActive = this.isHandInActive();
        if (handInActive && this.shouldClearOffhandReveal(player)) {
            this.offhandAwaitingHandIn = false;
            this.lastOffhandEmpty = currentOff == null || currentOff.method_7960();
            return;
        }
        boolean bl = currentEmpty = currentOff == null || currentOff.method_7960();
        if (this.lastOffhandEmpty && !currentEmpty) {
            this.offhandAwaitingHandIn = true;
        } else if (currentEmpty) {
            this.offhandAwaitingHandIn = false;
        }
        this.lastOffhandEmpty = currentEmpty;
    }

    private boolean isHandInActive() {
        boolean detachedOffhandHandIn = this.detachedOffhandEquip && this.cycleOff && !this.cycleMain;
        AnimationClip clip = detachedOffhandHandIn ? PunchyAnimationManager.POSE_HANDLER.getSecondaryClip() : PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        return this.phase == Phase.RAISING && clip != null && clip == this.activeHandInClip && (detachedOffhandHandIn || PunchyAnimationManager.POSE_HANDLER.isPlaying());
    }

    private boolean shouldClearOffhandReveal(class_746 player) {
        if (player == null) {
            return true;
        }
        if (this.cycleOff) {
            return true;
        }
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        if (this.activeHandInArm == null) {
            return false;
        }
        return this.activeHandInArm == offArm;
    }

    private boolean cancelOffhandTransitionForPoseOverrideInternal(class_746 player) {
        boolean offhandStateActive;
        if (player == null) {
            return false;
        }
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        AnimationClip secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        boolean primaryOffhandEquip = current != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && this.isEquipClip(current) && PunchyAnimationManager.POSE_HANDLER.getActiveArm() == offArm;
        boolean secondaryOffhandEquip = secondary != null && this.isEquipClip(secondary);
        boolean bl = offhandStateActive = this.cycleOff || this.pendingOffChange || this.offhandRevealPending || this.offhandAwaitingHandIn || primaryOffhandEquip || secondaryOffhandEquip;
        if (!offhandStateActive) {
            return false;
        }
        this.pendingOff = this.copyStack(player.method_6079());
        this.renderedOff = this.copyStack(player.method_6079());
        this.pendingOffChange = false;
        this.cycleOff = false;
        this.suppressOffhandRender = false;
        this.offhandRevealPending = false;
        this.offhandAwaitingHandIn = false;
        this.detachedOffhandEquip = false;
        this.shieldOppositeEquip = false;
        this.pendingBlacklistHandInOff = false;
        vanillaHandOutOffTicks = 0;
        if (primaryOffhandEquip) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            PunchyAnimationManager.clearSourceHand();
        }
        if (secondaryOffhandEquip) {
            this.clearSecondaryClipUnlessPreservingWalkOnWater("finishPrimaryOffhandEquip");
        }
        if (!this.cycleMain) {
            this.phase = Phase.IDLE;
            this.activeHandInClip = null;
            this.activeHandOutClip = null;
            this.activeHandInArm = null;
        }
        this.applyEquipOverrides(player);
        return true;
    }

    private void startDetachedOffhandRaising(class_746 player) {
        this.suppressOffhandRender = false;
        if (player != null) {
            class_1799 targetOff;
            class_1799 class_17992 = targetOff = this.pendingOff != null && !this.pendingOff.method_7960() ? this.pendingOff : player.method_6079();
            if (targetOff == null || targetOff.method_7960()) {
                this.finishLoweringWithoutRaise(player);
                return;
            }
            this.renderedOff = this.copyStack(targetOff);
            this.pendingOff = this.copyStack(targetOff);
            this.pendingOffChange = false;
            this.debugOffhandDetach("startDetachedOffhandRaising.sync", player, targetOff);
        }
        EquipSelection selection = this.resolveHandInClip(player);
        AnimationClip clip = selection.clip;
        if (clip == null) {
            CustomAnimationResolver.applyLoopSpeed(null);
            clip = PunchyAnimationManager.resolveHandedClip("hand_in", class_310.method_1551(), class_1268.field_5810);
        } else {
            CustomAnimationResolver.applyLoopSpeed(selection.entry);
        }
        if (clip == null) {
            ItemAnimationManager.forceTypeOnce(class_1268.field_5810, "hand_in");
            this.phase = Phase.IDLE;
            this.cycleOff = false;
            this.detachedOffhandEquip = false;
            this.shieldOppositeEquip = false;
            this.blacklistTransitionHandIn = false;
            return;
        }
        this.activeHandInClip = clip;
        this.activeHandInArm = player == null ? null : this.resolveArm(player, class_1268.field_5810);
        this.applyEquipOverrides(player);
        if (this.activeHandInArm != null) {
            PunchyAnimationManager.POSE_HANDLER.requestClearArm(this.activeHandInArm);
        }
        PunchyAnimationManager.POSE_HANDLER.setSecondaryClipAtTime(clip, this.activeHandInArm, this.resolveEquipSpeedMultiplier(), 0.0f);
        this.debugOffhandDetach("startDetachedOffhandRaising.play", player, player != null ? player.method_6079() : class_1799.field_8037);
        this.blacklistTransitionHandIn = false;
        this.phase = Phase.RAISING;
    }

    private void debugOffhandTransition(String event, class_746 player) {
    }

    private boolean tryDetachOffhandDuringAction(class_746 player) {
        if (player == null || this.detachedOffhandEquip || !this.cycleOff) {
            return false;
        }
        AnimationClip secondary = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        class_1306 offArm = this.resolveArm(player, class_1268.field_5810);
        if (secondary == null || !this.isEquipClip(secondary) || PunchyAnimationManager.POSE_HANDLER.getSecondaryArm() != offArm) {
            return false;
        }
        this.renderedMain = this.copyStack(player.method_6047());
        this.pendingMain = this.copyStack(player.method_6047());
        this.pendingMainChange = false;
        this.cycleMain = false;
        this.skipNextEquipMain = false;
        this.pendingBlacklistHandInMain = false;
        vanillaHandOutMainTicks = 0;
        this.clearPendingMainActionClear();
        this.detachedOffhandEquip = true;
        if (this.phase == Phase.RAISING) {
            this.activeHandInArm = offArm;
        }
        this.applyEquipOverrides(player);
        this.debugOffhandDetach("tryDetachOffhandDuringAction.detach", player, player.method_6079());
        return true;
    }

    private void logOffhandState(class_746 player, class_1799 currentOff) {
    }

    private boolean shouldSwapNow() {
        if (this.detachedOffhandEquip && this.cycleOff && !this.cycleMain || this.shieldOppositeEquip) {
            AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
            if (clip == null) {
                return true;
            }
            float time = PunchyAnimationManager.POSE_HANDLER.getSecondaryTime();
            boolean swapNow = clip.isEffectActive(EFFECT_SWAP_END, time) || clip.isEffectActive(EFFECT_SWAP, time) || clip.isEffectActive(EFFECT_SWAP_ITEM, time);
            return swapNow;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null) {
            return true;
        }
        float time = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
        return clip.isEffectActive(EFFECT_SWAP_END, time) || clip.isEffectActive(EFFECT_SWAP, time) || clip.isEffectActive(EFFECT_SWAP_ITEM, time);
    }

    private boolean isClipPlaying(AnimationClip clip) {
        if (this.detachedOffhandEquip && this.cycleOff && !this.cycleMain || this.shieldOppositeEquip) {
            return clip != null && PunchyAnimationManager.POSE_HANDLER.getSecondaryClip() == clip;
        }
        return clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && PunchyAnimationManager.POSE_HANDLER.getCurrentClip() == clip;
    }

    private boolean isHandOutInEarlyTransitionWindow() {
        if (this.detachedOffhandEquip && this.cycleOff && !this.cycleMain || this.shieldOppositeEquip) {
            AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
            if (clip == null || clip != this.activeHandOutClip) {
                return false;
            }
            float length = clip.getLength();
            if (length <= 0.0f) {
                return true;
            }
            float time = PunchyAnimationManager.POSE_HANDLER.getSecondaryTime();
            return time > 0.05f && time >= Math.max(0.0f, length - 0.05f);
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || clip != this.activeHandOutClip || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        float length = clip.getLength();
        if (length <= 0.0f) {
            return true;
        }
        return PunchyAnimationManager.POSE_HANDLER.getCurrentTime() >= Math.max(0.0f, length - 0.05f);
    }

    private boolean updateHotbarSlot(class_746 player) {
        int slot = this.resolveSelectedSlot(player);
        if (slot < 0) {
            return false;
        }
        boolean changed = slot != this.lastHotbarSlot;
        this.lastHotbarSlot = slot;
        return changed;
    }

    private boolean shouldAnimateHotbarChange(class_1799 rendered, class_1799 current) {
        return !rendered.method_7960() || !current.method_7960();
    }

    private boolean isRapidHotbarScrollActive() {
        return this.hotbarRapidWindowTicks > 0 && this.hotbarChangeStreak >= 2;
    }

    private void recordFunctionalSwapIfNeeded(class_746 player, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged, boolean hotbarChanged) {
        class_1799 previousMain = this.pendingMainChange ? this.pendingMain : this.renderedMain;
        class_1799 previousOff = this.pendingOffChange ? this.pendingOff : this.renderedOff;
        this.recordFunctionalSwapIfNeeded(player, previousMain, previousOff, currentMain, currentOff, mainChanged, offChanged, hotbarChanged);
    }

    private void recordFunctionalSwapIfNeeded(class_746 player, class_1799 previousMain, class_1799 previousOff, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged, boolean hotbarChanged) {
        if (player == null || !this.isFunctionalOffhandSwap(previousMain, previousOff, currentMain, currentOff, mainChanged, offChanged, hotbarChanged)) {
            return;
        }
        this.trimFunctionalSwapTicks(player.field_6012);
        this.recentFunctionalSwapTicks.addLast(player.field_6012);
    }

    private boolean isFunctionalSwapBurstActive(class_746 player) {
        if (player == null) {
            return false;
        }
        this.trimFunctionalSwapTicks(player.field_6012);
        return this.recentFunctionalSwapTicks.size() >= 2;
    }

    private boolean wasFunctionalSwapRecordedThisTick(class_746 player) {
        if (player == null) {
            return false;
        }
        this.trimFunctionalSwapTicks(player.field_6012);
        Integer latestTick = this.recentFunctionalSwapTicks.peekLast();
        return latestTick != null && latestTick == player.field_6012;
    }

    private void trimFunctionalSwapTicks(int now) {
        int tick;
        while (!this.recentFunctionalSwapTicks.isEmpty() && now - (tick = this.recentFunctionalSwapTicks.peekFirst().intValue()) > 10) {
            this.recentFunctionalSwapTicks.removeFirst();
        }
    }

    private boolean isFunctionalOffhandSwap(class_1799 previousMain, class_1799 previousOff, class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged, boolean hotbarChanged) {
        if (hotbarChanged || !mainChanged || !offChanged) {
            return false;
        }
        return this.areEquivalentStacks(previousMain, currentOff) && this.areEquivalentStacks(previousOff, currentMain);
    }

    private boolean areEquivalentStacks(class_1799 left, class_1799 right) {
        return !this.hasItemChanged(left, right);
    }

    private class_1306 resolveActiveArm(class_746 player) {
        if (this.cycleMain && this.cycleOff) {
            return null;
        }
        if (this.cycleMain) {
            return this.resolveArm(player, class_1268.field_5808);
        }
        if (this.cycleOff) {
            return this.resolveArm(player, class_1268.field_5810);
        }
        return null;
    }

    private class_1306 resolveArm(class_746 player, class_1268 hand) {
        boolean mainRight;
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        if (hand == class_1268.field_5808) {
            return mainRight ? class_1306.field_6183 : class_1306.field_6182;
        }
        return mainRight ? class_1306.field_6182 : class_1306.field_6183;
    }

    private class_1268 resolveHandForArm(class_746 player, class_1306 arm) {
        if (player == null || arm == null) {
            return class_1268.field_5808;
        }
        return this.resolveArm(player, class_1268.field_5808) == arm ? class_1268.field_5808 : class_1268.field_5810;
    }

    private EquipSelection resolveHandOutClip() {
        EquipSelection mainSelection = this.cycleMain ? this.resolveCustomEquipClip(this.renderedMain, class_1268.field_5808, "hand_out") : new EquipSelection(null, null, class_1268.field_5808, false);
        EquipSelection offSelection = this.cycleOff ? this.resolveCustomEquipClip(this.renderedOff, class_1268.field_5810, "hand_out") : new EquipSelection(null, null, class_1268.field_5810, false);
        return this.chooseEquipSelection(mainSelection, offSelection);
    }

    private EquipSelection resolveHandInClip(class_746 player) {
        class_1799 mainTarget = this.resolveRaiseTargetStack(class_1268.field_5808, this.pendingMain, this.renderedMain, player);
        class_1799 offTarget = this.resolveRaiseTargetStack(class_1268.field_5810, this.pendingOff, this.renderedOff, player);
        EquipSelection mainSelection = this.cycleMain ? this.resolveCustomEquipClip(mainTarget, class_1268.field_5808, "hand_in") : new EquipSelection(null, null, class_1268.field_5808, false);
        EquipSelection offSelection = this.cycleOff ? this.resolveCustomEquipClip(offTarget, class_1268.field_5810, "hand_in") : new EquipSelection(null, null, class_1268.field_5810, false);
        return this.chooseEquipSelection(mainSelection, offSelection);
    }

    private class_1799 resolveRaiseTargetStack(class_1268 hand, class_1799 pending, class_1799 rendered, class_746 player) {
        if (pending != null && !pending.method_7960()) {
            return pending;
        }
        class_1799 live = class_1799.field_8037;
        if (player != null) {
            class_1799 class_17992 = live = hand == class_1268.field_5810 ? player.method_6079() : player.method_6047();
        }
        if (live != null && !live.method_7960()) {
            return live;
        }
        if (rendered != null && !rendered.method_7960()) {
            return rendered;
        }
        return class_1799.field_8037;
    }

    private EquipSelection resolveCustomEquipClip(class_1799 stack, class_1268 hand, String type) {
        class_310 mc;
        if (stack == null || type == null) {
            return new EquipSelection(null, null, hand, false);
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, type);
        CustomAnimationResolver.ClipLookup lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, type, entry, mc = class_310.method_1551(), hand);
        if (lookup.clip() == null) {
            return new EquipSelection(null, entry, hand, false);
        }
        boolean explicitCustom = !lookup.usedTypeFallback() && lookup.resolvedName() != null && !lookup.resolvedName().isBlank();
        return new EquipSelection(lookup.clip(), entry, hand, explicitCustom);
    }

    private EquipSelection chooseEquipSelection(EquipSelection mainSelection, EquipSelection offSelection) {
        if (mainSelection.clip == null) {
            return offSelection;
        }
        if (offSelection.clip == null) {
            return mainSelection;
        }
        if (this.isWalkOnWaterPoseActive() && this.cycleOff && !this.cycleMain) {
            return offSelection;
        }
        if (mainSelection.explicitCustom != offSelection.explicitCustom) {
            return mainSelection.explicitCustom ? mainSelection : offSelection;
        }
        return mainSelection;
    }

    private float resolveEquipSpeedMultiplier() {
        return 1.0f / Math.max(0.01f, PunchyAnimationManager.ANIMATION_SPEED);
    }

    private class_1799 copyStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return class_1799.field_8037;
        }
        return stack.method_7972();
    }

    private String debugStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return "empty";
        }
        return String.valueOf(class_7923.field_41178.method_10221((Object)stack.method_7909()));
    }

    private String clipName(AnimationClip clip) {
        if (clip == null) {
            return "null";
        }
        String name = clip.getName();
        return name == null || name.isBlank() ? "<unnamed>" : name;
    }

    private String formatDebugTime(float value) {
        return String.format(Locale.ROOT, "%.3f", Float.valueOf(value));
    }

    private void debugOffhandDetach(String event, class_746 player, class_1799 currentOff) {
    }

    private boolean wasItemBlacklisted(class_1799 stack) {
        return PunchyConfig.isItemBlacklisted(stack);
    }

    private boolean isHandBlacklisted(class_746 player, class_1268 hand) {
        return PunchyConfig.isHandBlacklisted((class_1657)player, hand);
    }

    private boolean hasItemChanged(class_1799 previous, class_1799 current) {
        class_1799 now;
        class_1799 prev = previous == null ? class_1799.field_8037 : previous;
        class_1799 class_17992 = now = current == null ? class_1799.field_8037 : current;
        if (!prev.method_31574(now.method_7909())) {
            return true;
        }
        if (prev.method_7909() instanceof class_1764 && now.method_7909() instanceof class_1764) {
            return false;
        }
        for (class_9336 dataComponent : now.method_57353()) {
            if (this.shouldIgnoreComponentForEquipDiff(dataComponent.comp_2443())) continue;
            if (!prev.method_57353().method_57832(dataComponent.comp_2443())) {
                return true;
            }
            if (Objects.equals(prev.method_57824(dataComponent.comp_2443()), dataComponent.comp_2444())) continue;
            return true;
        }
        for (class_9336 dataComponent : prev.method_57353()) {
            if (this.shouldIgnoreComponentForEquipDiff(dataComponent.comp_2443()) || now.method_57353().method_57832(dataComponent.comp_2443())) continue;
            return true;
        }
        return false;
    }

    private boolean shouldIgnoreComponentForEquipDiff(class_9331<?> type) {
        if (type == null) {
            return false;
        }
        if (type == class_9334.field_49629) {
            return true;
        }
        class_2960 id = class_7923.field_49658.method_10221(type);
        return id != null && !"minecraft".equals(id.method_12836());
    }

    private boolean shouldSkipHandEquipOnSwap(class_1799 currentMain, class_1799 currentOff, boolean mainChanged, boolean offChanged) {
        if (!mainChanged && !offChanged) {
            return false;
        }
        if (mainChanged && this.shouldIgnoreMainhandTridentSwap(currentMain)) {
            this.skipNextEquipMain = false;
            return true;
        }
        if (mainChanged && (this.hasSkipHandEquipFlag(this.renderedMain, currentMain) || this.skipNextEquipMain)) {
            this.skipNextEquipMain = false;
            return true;
        }
        if (offChanged && (this.hasSkipHandEquipFlag(this.renderedOff, currentOff) || this.skipNextEquipOff)) {
            this.skipNextEquipOff = false;
            return true;
        }
        return false;
    }

    private boolean shouldIgnoreMainhandTridentSwap(class_1799 currentMain) {
        boolean tridentRelated;
        class_1799 previousMain = this.renderedMain == null ? class_1799.field_8037 : this.renderedMain;
        class_1799 nextMain = currentMain == null ? class_1799.field_8037 : currentMain;
        boolean bl = tridentRelated = previousMain.method_7909() instanceof class_1835 || nextMain.method_7909() instanceof class_1835;
        if (!tridentRelated) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (TridentStateMachine.isRecentActionBlockingEquip(client)) {
            return true;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (PunchyAnimationManager.isClipNamed(clip, "use_trident") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide")) {
            return true;
        }
        return PunchyAnimationManager.POSE_HANDLER.isBlendOutActive() && (PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide"));
    }

    private boolean shouldHideOffhandTridentThrowCache(class_1799 currentOff, boolean offChanged) {
        if (!offChanged || currentOff == null || !currentOff.method_7960()) {
            return false;
        }
        class_1799 previousOff = this.renderedOff == null ? class_1799.field_8037 : this.renderedOff;
        return previousOff.method_7909() instanceof class_1835;
    }

    private void updateSkipEquipLatches(class_1799 currentMain, class_1799 currentOff) {
        if (!this.isSkipHandEquipWindowActive()) {
            return;
        }
        if (this.hasSkipHandEquipFlagForStack(currentMain)) {
            this.skipNextEquipMain = true;
        }
        if (this.hasSkipHandEquipFlagForStack(currentOff)) {
            this.skipNextEquipOff = true;
        }
    }

    private boolean hasSkipHandEquipFlag(class_1799 previous, class_1799 current) {
        if (this.isThrowableConsumedToEmpty(previous, current)) {
            return true;
        }
        return this.hasSkipHandEquipFlagForStack(previous) || this.hasSkipHandEquipFlagForStack(current);
    }

    private boolean isThrowableConsumedToEmpty(class_1799 previous, class_1799 current) {
        if (!this.isThrowableSkipEquipStack(previous)) {
            return false;
        }
        if (current != null && !current.method_7960()) {
            return false;
        }
        AnimationClip activeClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (activeClip == null) {
            return false;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(previous, "useItem");
        if (entry != null && this.isEntryActiveForCurrentUseClip(entry, "useItem", activeClip)) {
            return true;
        }
        return PunchyAnimationManager.isClipNamed(activeClip, "throw_item");
    }

    private boolean isThrowableSkipEquipStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150) || stack.method_31574(class_1802.field_8287) || stack.method_31574(class_1802.field_8543) || stack.method_31574(class_1802.field_8803) || stack.method_31574(class_1802.field_8634) || stack.method_31574(class_1802.field_8449);
    }

    private boolean hasSkipHandEquipFlagForStack(class_1799 stack) {
        String[] types;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        AnimationClip activeClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (activeClip == null) {
            return false;
        }
        for (String type : types = new String[]{"useDrink", "useEat", "useItem"}) {
            PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, type);
            if (!CustomAnimationResolver.shouldSkipHandEquip(entry) || !this.isEntryActiveForCurrentUseClip(entry, type, activeClip)) continue;
            return true;
        }
        return false;
    }

    private boolean isSkipHandEquipWindowActive() {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        return !this.isEquipClip(clip) && !this.isBlockingEquipClip(clip);
    }

    private boolean isEntryActiveForCurrentUseClip(PunchyTuningConfig.CustomAnimationEntry entry, String type, AnimationClip activeClip) {
        if (activeClip == null || type == null) {
            return false;
        }
        String customName = CustomAnimationResolver.resolveSingleName(entry);
        if (customName != null && !customName.isBlank()) {
            return PunchyAnimationManager.isClipNamed(activeClip, customName);
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT).replace("_", "");
        if ("usedrink".equals(normalized)) {
            return PunchyAnimationManager.isClipNamed(activeClip, "use_drink");
        }
        if ("useeat".equals(normalized)) {
            return PunchyAnimationManager.isClipNamed(activeClip, "use_eat");
        }
        if ("useitem".equals(normalized)) {
            return PunchyAnimationManager.isClipNamed(activeClip, "interact") || PunchyAnimationManager.isClipNamed(activeClip, "mining_use_foward") || PunchyAnimationManager.isClipNamed(activeClip, "mining_use_digging");
        }
        return false;
    }

    private int resolveSelectedSlot(class_746 player) {
        if (player == null) {
            return -1;
        }
        class_1661 inventory = player.method_31548();
        if (inventory == null) {
            return -1;
        }
        try {
            return inventory.field_7545;
        }
        catch (Exception exception) {
            return -1;
        }
    }

    static {
        lastEquipTransitionPlayerTick = -1;
    }

    private static enum Phase {
        IDLE,
        LOWERING,
        RAISING;

    }

    private record EquipSelection(AnimationClip clip, PunchyTuningConfig.CustomAnimationEntry entry, class_1268 hand, boolean explicitCustom) {
    }

    private record PoseHandlerState(AnimationClip clip, float time) {
    }
}

