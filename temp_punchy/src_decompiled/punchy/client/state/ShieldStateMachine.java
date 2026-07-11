/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_1819
 *  net.minecraft.class_310
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1819;
import net.minecraft.class_310;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.config.PunchyTuningConfig;

public class ShieldStateMachine {
    private boolean wasUsing;
    private AnimationClip activeClip;
    private class_1268 lastShieldHand;

    public void tick(class_310 client) {
        boolean usingShield;
        if (client == null || client.field_1724 == null) {
            return;
        }
        class_1799 usingStack = client.field_1724.method_6030();
        boolean isShield = usingStack.method_7909() instanceof class_1819;
        class_1268 usedHand = client.field_1724.method_6115() ? client.field_1724.method_6058() : this.lastShieldHand;
        boolean visualHold = false;
        boolean bl = usingShield = client.field_1724.method_6115() && isShield;
        if (usingShield) {
            this.lastShieldHand = usedHand;
        } else if (this.wasUsing && this.shouldKeepShieldVisualHold(client)) {
            usedHand = this.lastShieldHand;
            usingShield = true;
            visualHold = true;
        }
        if (usingShield && PunchyAnimationManager.isHandBlacklisted(client, usedHand)) {
            if (this.wasUsing) {
                PunchyAnimationManager.POSE_HANDLER.setLooping(false);
                PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
                PunchyAnimationManager.clearSourceHand();
                this.activeClip = null;
            }
            this.wasUsing = false;
            this.lastShieldHand = null;
            return;
        }
        if (usingShield && !visualHold && HandEquipStateMachine.isEquipInProgress() && !HandEquipStateMachine.isOppositeShieldEquipInProgress(usedHand) && !HandEquipStateMachine.abortEquipForAction(client, usedHand)) {
            return;
        }
        if (usingShield) {
            if (!this.wasUsing || !PunchyAnimationManager.POSE_HANDLER.isPlaying() || this.activeClip == null) {
                this.playShield(client, usedHand);
            }
        } else if (this.wasUsing) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
            PunchyAnimationManager.clearSourceHand();
            this.activeClip = null;
            this.lastShieldHand = null;
        }
        this.wasUsing = usingShield;
    }

    private boolean shouldKeepShieldVisualHold(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1690 == null || this.lastShieldHand == null) {
            return false;
        }
        if (!client.field_1690.field_1904.method_1434()) {
            return false;
        }
        class_1799 stack = client.field_1724.method_5998(this.lastShieldHand);
        return stack != null && stack.method_7909() instanceof class_1819;
    }

    private void playShield(class_310 client, class_1268 hand) {
        AnimationClip clip = this.resolveShieldClip(client, hand);
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
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, true, hold);
        this.activeClip = clip;
    }

    private AnimationClip resolveShieldClip(class_310 client, class_1268 hand) {
        PunchyTuningConfig.CustomAnimationEntry entry;
        class_1799 stack;
        CustomAnimationResolver.ClipLookup lookup;
        if (client != null && client.field_1724 != null && (lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack = client.field_1724.method_5998(hand), "use_shield", entry = CustomAnimationResolver.findEntry(stack, "use_shield"), client, hand)).clip() != null) {
            return lookup.clip();
        }
        return PunchyAnimationManager.resolveHandedClip("use_shield", client, hand);
    }
}

