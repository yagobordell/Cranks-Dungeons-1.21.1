/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.InspectActionTracker;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public class InspectStateMachine {
    public void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (HandEquipStateMachine.isEquipInProgress()) {
            return;
        }
        if (!InspectActionTracker.consume()) {
            return;
        }
        ItemAnimationManager.forceTypeOnce(class_1268.field_5808, "inspect");
        ClipSelection selection = this.resolveCustomClip(client, "inspect");
        if (selection.clip == null) {
            return;
        }
        this.playClip(client, selection);
    }

    private void playClip(class_310 client, ClipSelection selection) {
        class_1306 arm;
        AnimationClip clip = selection.clip;
        if (clip == null) {
            return;
        }
        CustomAnimationResolver.applyLoopSpeed(selection.entry);
        class_1306 class_13062 = arm = selection.hand == null ? null : PunchyAnimationManager.getUseArm(client, selection.hand);
        if (PunchyAnimationManager.isDualHandedActive(client, clip, 0.0f)) {
            arm = null;
        }
        PunchyAnimationManager.setSourceHand(client, selection.hand == null ? class_1268.field_5808 : selection.hand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, false);
    }

    private ClipSelection resolveCustomClip(class_310 client, String type) {
        if (client == null || client.field_1724 == null) {
            return new ClipSelection(null, null, null);
        }
        class_1799 main = client.field_1724.method_6047();
        ClipSelection selection = this.resolveFromStack(client, main, class_1268.field_5808, type);
        if (selection.clip != null) {
            return selection;
        }
        class_1799 off = client.field_1724.method_6079();
        return this.resolveFromStack(client, off, class_1268.field_5810, type);
    }

    private ClipSelection resolveFromStack(class_310 client, class_1799 stack, class_1268 hand, String type) {
        if (stack == null) {
            return new ClipSelection(null, null, null);
        }
        if (PunchyConfig.isItemBlacklisted(stack)) {
            return new ClipSelection(null, null, null);
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, type);
        CustomAnimationResolver.ClipLookup lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, type, entry, client, hand);
        if (lookup.clip() == null) {
            return new ClipSelection(null, null, null);
        }
        return new ClipSelection(lookup.clip(), hand, entry);
    }

    private record ClipSelection(AnimationClip clip, class_1268 hand, PunchyTuningConfig.CustomAnimationEntry entry) {
    }
}

