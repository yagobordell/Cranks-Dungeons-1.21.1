/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_310
 *  net.minecraft.class_6880
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_7225$class_7226
 *  net.minecraft.class_7924
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_310;
import net.minecraft.class_6880;
import net.minecraft.class_7225;
import net.minecraft.class_7924;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.config.PunchyTuningConfig;

public class TridentStateMachine {
    private static final int THROW_MIN_CHARGE_TICKS = 10;
    private static final int RECENT_ACTION_BLOCK_TICKS = 12;
    private static int lastRecentActionPlayerTick = -1;
    private boolean wasUsing;
    private class_1268 lastHand = class_1268.field_5808;
    private AnimationClip activeClip;
    private boolean lastWasRiptide;
    private boolean lastWasLoyalty;
    private int lastUseTicks;
    private class_1268 lastUsedHand;
    private boolean waitingLoyaltyReturn;
    private boolean loyaltyHandBecameEmpty;
    private int loyaltyThrowStartTick = -1;
    private int loyaltyThrowStartHotbarSlot = -1;
    private class_1268 loyaltyReturnHand = class_1268.field_5808;
    private String lastPreferredOption;
    private int chargedHotbarSlot = -1;

    public void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        boolean isTrident = usingStack.method_7909() instanceof class_1835;
        boolean useDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
        boolean usingTrident = client.field_1724.method_6115() && isTrident && useDown;
        class_1268 usedHand = client.field_1724.method_6058();
        if (usingTrident && PunchyAnimationManager.isHandBlacklisted(client, usedHand)) {
            if (this.wasUsing) {
                this.activeClip = null;
                PunchyAnimationManager.clearSourceHand();
            }
            this.wasUsing = false;
            return;
        }
        if (usingTrident && HandEquipStateMachine.isEquipInProgress() && !HandEquipStateMachine.abortEquipForAction(client, usedHand)) {
            return;
        }
        if (usingTrident) {
            lastRecentActionPlayerTick = client.field_1724.field_6012;
            this.lastUseTicks = usingStack.method_7935((class_1309)client.field_1724) - (client.field_1724.method_6014() + 1);
            this.lastPreferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(usingStack);
            try {
                if (client.field_1687 != null) {
                    class_7225.class_7226 registry = client.field_1687.method_30349().method_46762(class_7924.field_41265);
                    class_6880.class_6883 riptide = registry.method_46747(class_1893.field_9104);
                    class_6880.class_6883 loyalty = registry.method_46747(class_1893.field_9120);
                    this.lastWasRiptide = class_1890.method_8225((class_6880)riptide, (class_1799)usingStack) > 0;
                    this.lastWasLoyalty = class_1890.method_8225((class_6880)loyalty, (class_1799)usingStack) > 0;
                }
            }
            catch (Throwable t) {
                this.lastWasRiptide = false;
                this.lastWasLoyalty = false;
            }
            if (this.wasUsing && this.lastUsedHand != null && this.lastUsedHand != usedHand) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                this.activeClip = null;
                PunchyAnimationManager.clearSourceHand();
                HandEquipStateMachine.requestRenderedSync();
            }
            if (!this.wasUsing || this.lastUsedHand != usedHand) {
                int n = this.chargedHotbarSlot = usedHand == class_1268.field_5808 ? client.field_1724.method_31548().field_7545 : -1;
            }
            if (!this.wasUsing || this.lastUsedHand != usedHand || !PunchyAnimationManager.POSE_HANDLER.isPlaying() || this.activeClip == null) {
                this.lastHand = client.field_1724.method_6058();
                this.playUse(client, this.lastHand);
            }
        } else if (this.wasUsing) {
            boolean cancelledByHotbarSwap;
            boolean releasedUseKey = !useDown;
            boolean sameMainHandSlot = this.lastHand != class_1268.field_5808 || this.chargedHotbarSlot < 0 || client.field_1724.method_31548().field_7545 == this.chargedHotbarSlot;
            boolean bl = cancelledByHotbarSwap = this.lastHand == class_1268.field_5808 && this.chargedHotbarSlot >= 0 && client.field_1724.method_31548().field_7545 != this.chargedHotbarSlot;
            if (releasedUseKey && sameMainHandSlot && this.lastUseTicks >= 10) {
                this.playThrow(client, this.lastHand);
                lastRecentActionPlayerTick = client.field_1724.field_6012;
            } else {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                if (cancelledByHotbarSwap) {
                    lastRecentActionPlayerTick = -1;
                    HandEquipStateMachine.triggerImmediateEquipCycle(client, this.lastHand);
                }
            }
            this.activeClip = null;
            PunchyAnimationManager.clearSourceHand();
            this.lastUseTicks = 0;
            this.lastUsedHand = null;
            this.lastPreferredOption = null;
            this.chargedHotbarSlot = -1;
        }
        if (this.waitingLoyaltyReturn) {
            if (this.shouldCancelLoyaltyThrowOnEquipSwap(client)) {
                this.cancelLoyaltyThrowForEquipSwap();
            }
            this.monitorLoyaltyReturn(client);
        }
        this.wasUsing = usingTrident;
        this.lastUsedHand = usingTrident ? usedHand : null;
    }

    private void playUse(class_310 client, class_1268 hand) {
        AnimationClip clip = PunchyAnimationManager.resolveHandedClip("use_trident", client, hand, this.lastPreferredOption);
        if (clip == null) {
            return;
        }
        PunchyAnimationManager.setSourceHand(client, hand);
        boolean dualHanded = PunchyAnimationManager.isDualHandedActive(client, clip, 0.0f);
        if (dualHanded) {
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(null);
        } else {
            class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        }
        boolean hold = clip.isHoldOnLastFrame();
        PunchyAnimationManager.POSE_HANDLER.setLooping(!hold);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        CustomAnimationResolver.playConfiguredClip(clip, null, speedMultiplier, true, hold);
        this.activeClip = clip;
    }

    private void playThrow(class_310 client, class_1268 hand) {
        AnimationClip clip = PunchyAnimationManager.resolveHandedClip("use_trident_throw", client, hand, this.lastPreferredOption);
        if (this.lastWasRiptide) {
            clip = PunchyAnimationManager.resolveHandedClip("use_trident_throw_riptide", client, hand, this.lastPreferredOption);
        } else if (this.lastWasLoyalty) {
            clip = PunchyAnimationManager.resolveHandedClip("use_trident_throw_loyalty", client, hand, this.lastPreferredOption);
        }
        if (clip == null) {
            clip = PunchyAnimationManager.resolveHandedClip("use_trident_throw", client, hand, this.lastPreferredOption);
        }
        if (clip == null) {
            return;
        }
        PunchyAnimationManager.setSourceHand(client, hand);
        boolean dualHanded = PunchyAnimationManager.isDualHandedActive(client, clip, 0.0f);
        if (dualHanded) {
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(null);
        } else {
            class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        }
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        CustomAnimationResolver.playConfiguredClip(clip, null, speedMultiplier, true, false);
        if (this.lastWasLoyalty && !this.lastWasRiptide) {
            this.waitingLoyaltyReturn = true;
            this.loyaltyHandBecameEmpty = false;
            this.loyaltyThrowStartTick = client.field_1724 != null ? client.field_1724.field_6012 : -1;
            this.loyaltyThrowStartHotbarSlot = client.field_1724 != null ? client.field_1724.method_31548().field_7545 : -1;
            this.loyaltyReturnHand = hand;
        } else {
            this.waitingLoyaltyReturn = false;
            this.loyaltyHandBecameEmpty = false;
            this.loyaltyThrowStartTick = -1;
            this.loyaltyThrowStartHotbarSlot = -1;
        }
    }

    private void monitorLoyaltyReturn(class_310 client) {
        boolean handEmpty;
        if (client == null || client.field_1724 == null) {
            this.clearLoyaltyReturnTracking();
            return;
        }
        if (client.field_1724.method_6115()) {
            return;
        }
        int nowTick = client.field_1724.field_6012;
        if (this.loyaltyThrowStartTick >= 0 && nowTick - this.loyaltyThrowStartTick > 200) {
            this.clearLoyaltyReturnTracking();
            return;
        }
        class_1799 stack = client.field_1724.method_5998(this.loyaltyReturnHand);
        boolean bl = handEmpty = stack == null || stack.method_7960();
        if (!this.loyaltyHandBecameEmpty) {
            if (handEmpty) {
                this.loyaltyHandBecameEmpty = true;
            }
            return;
        }
        if (handEmpty || !(stack.method_7909() instanceof class_1835)) {
            return;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (TridentStateMachine.isThrowClip(current)) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        }
        PunchyAnimationManager.clearSourceHand();
        this.activeClip = null;
        lastRecentActionPlayerTick = -1;
        HandEquipStateMachine.triggerPickupEquipCycle(client, this.loyaltyReturnHand);
        this.clearLoyaltyReturnTracking();
    }

    private void clearLoyaltyReturnTracking() {
        this.waitingLoyaltyReturn = false;
        this.loyaltyHandBecameEmpty = false;
        this.loyaltyThrowStartTick = -1;
        this.loyaltyThrowStartHotbarSlot = -1;
    }

    private boolean shouldCancelLoyaltyThrowOnEquipSwap(class_310 client) {
        int selectedNow;
        boolean handEmpty;
        if (client == null || client.field_1724 == null || !this.waitingLoyaltyReturn) {
            return false;
        }
        class_1799 handStack = client.field_1724.method_5998(this.loyaltyReturnHand);
        boolean bl = handEmpty = handStack == null || handStack.method_7960();
        if (!this.loyaltyHandBecameEmpty) {
            return false;
        }
        if (!handEmpty && !(handStack.method_7909() instanceof class_1835)) {
            return true;
        }
        return this.loyaltyReturnHand == class_1268.field_5808 && this.loyaltyThrowStartHotbarSlot >= 0 && (selectedNow = client.field_1724.method_31548().field_7545) != this.loyaltyThrowStartHotbarSlot;
    }

    private void cancelLoyaltyThrowForEquipSwap() {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (TridentStateMachine.isThrowClip(current)) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        }
        PunchyAnimationManager.clearSourceHand();
        this.activeClip = null;
        lastRecentActionPlayerTick = -1;
        this.clearLoyaltyReturnTracking();
    }

    private static boolean isThrowClip(AnimationClip clip) {
        return PunchyAnimationManager.isClipNamed(clip, "use_trident_throw") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_loyalty") || PunchyAnimationManager.isClipNamed(clip, "use_trident_throw_riptide");
    }

    public static boolean isRecentActionBlockingEquip(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        int tick = lastRecentActionPlayerTick;
        if (tick < 0) {
            return false;
        }
        int now = client.field_1724.field_6012;
        return now >= tick && now - tick <= 12;
    }
}

