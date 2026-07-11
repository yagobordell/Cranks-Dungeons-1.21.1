/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 */
package punchy.client.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1041;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.TriggerableActionTracker;
import punchy.client.state.TriggerableKeyResolver;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public class TriggerableStateMachine {
    private static final Map<Integer, Boolean> LAST_KEY_STATE = new HashMap<Integer, Boolean>();

    public static void trigger(class_1268 hand) {
        TriggerableActionTracker.record(hand, -1);
    }

    public void tick(class_310 client) {
        ClipSelection selection;
        if (client == null || client.field_1724 == null) {
            return;
        }
        TriggerableActionTracker.Trigger trigger = TriggerableActionTracker.consumeTrigger();
        if (trigger == null) {
            trigger = this.pollKeyTrigger(client);
        }
        if (trigger == null) {
            return;
        }
        ItemAnimationManager.forceTypeOnce(trigger.hand(), "triggerable");
        if (trigger.hand() != null) {
            class_1799 stack = client.field_1724.method_5998(trigger.hand());
            selection = this.resolveFromStack(client, stack, trigger.hand(), "triggerable", trigger.keyCode());
        } else {
            selection = this.resolveCustomClip(client, "triggerable", trigger.keyCode());
        }
        if (selection.clip == null) {
            return;
        }
        if (HandEquipStateMachine.isEquipInProgress() && this.shouldAbortEquipForHand(client, selection.hand) && !HandEquipStateMachine.abortEquipForAction(client, selection.hand)) {
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
        this.primeOppositeArmOverlapDuringEquip(client, selection.hand, arm);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendIn(selection.entry);
        CustomAnimationResolver.playConfiguredClip(clip, selection.entry, speedMultiplier, skipBlendIn, false);
    }

    private ClipSelection resolveCustomClip(class_310 client, String type, int keyCode) {
        if (client == null || client.field_1724 == null) {
            return new ClipSelection(null, null, null);
        }
        class_1799 main = client.field_1724.method_6047();
        ClipSelection selection = this.resolveFromStack(client, main, class_1268.field_5808, type, keyCode);
        if (selection.clip != null) {
            return selection;
        }
        class_1799 off = client.field_1724.method_6079();
        return this.resolveFromStack(client, off, class_1268.field_5810, type, keyCode);
    }

    private ClipSelection resolveFromStack(class_310 client, class_1799 stack, class_1268 hand, String type, int keyCode) {
        if (stack == null) {
            return new ClipSelection(null, null, null);
        }
        if (PunchyConfig.isItemBlacklisted(stack)) {
            return new ClipSelection(null, null, null);
        }
        PunchyTuningConfig.CustomAnimationEntry entry = this.resolveEntryForKey(CustomAnimationResolver.findEntries(stack, type), keyCode);
        CustomAnimationResolver.ClipLookup lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, type, entry, client, hand);
        if (lookup.clip() == null) {
            return new ClipSelection(null, null, null);
        }
        return new ClipSelection(lookup.clip(), hand, entry);
    }

    private PunchyTuningConfig.CustomAnimationEntry resolveEntryForKey(List<PunchyTuningConfig.CustomAnimationEntry> entries, int keyCode) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        if (keyCode >= 0) {
            for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
                if (entry == null || !TriggerableKeyResolver.matches(entry.key, keyCode)) continue;
                return entry;
            }
            return null;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (entry == null || entry.key != null && !entry.key.isBlank()) continue;
            return entry;
        }
        return entries.get(0);
    }

    private TriggerableActionTracker.Trigger pollKeyTrigger(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1755 != null) {
            return null;
        }
        class_1041 window = client.method_22683();
        if (window == null) {
            return null;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> mainEntries = CustomAnimationResolver.findEntries(client.field_1724.method_6047(), "triggerable");
        List<PunchyTuningConfig.CustomAnimationEntry> offEntries = CustomAnimationResolver.findEntries(client.field_1724.method_6079(), "triggerable");
        if (mainEntries.isEmpty() && offEntries.isEmpty()) {
            LAST_KEY_STATE.clear();
            return null;
        }
        ArrayList<KeyCandidate> candidates = new ArrayList<KeyCandidate>();
        this.addCandidates(candidates, mainEntries, class_1268.field_5808);
        this.addCandidates(candidates, offEntries, class_1268.field_5810);
        if (candidates.isEmpty()) {
            LAST_KEY_STATE.clear();
            return null;
        }
        HashSet<Integer> activeCodes = new HashSet<Integer>();
        KeyCandidate fired = null;
        for (KeyCandidate candidate : candidates) {
            int code2 = candidate.keyCode;
            activeCodes.add(code2);
            boolean down = class_3675.method_15987((long)window.method_4490(), (int)code2);
            boolean wasDown = LAST_KEY_STATE.getOrDefault(code2, false);
            if (down && !wasDown && fired == null) {
                fired = candidate;
            }
            LAST_KEY_STATE.put(code2, down);
        }
        LAST_KEY_STATE.keySet().removeIf(code -> !activeCodes.contains(code));
        if (fired == null) {
            return null;
        }
        return new TriggerableActionTracker.Trigger(fired.hand, fired.keyCode);
    }

    private void addCandidates(List<KeyCandidate> out, List<PunchyTuningConfig.CustomAnimationEntry> entries, class_1268 hand) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            int keyCode;
            if (entry == null || (keyCode = TriggerableKeyResolver.resolveKeyCode(entry.key)) < 0) continue;
            out.add(new KeyCandidate(hand, keyCode));
        }
    }

    private boolean shouldAbortEquipForHand(class_310 client, class_1268 hand) {
        if (client == null || hand == null) {
            return false;
        }
        class_1306 equipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (equipArm == null) {
            return false;
        }
        class_1306 actionArm = PunchyAnimationManager.getUseArm(client, hand);
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

    private record ClipSelection(AnimationClip clip, class_1268 hand, PunchyTuningConfig.CustomAnimationEntry entry) {
    }

    private record KeyCandidate(class_1268 hand, int keyCode) {
    }
}

