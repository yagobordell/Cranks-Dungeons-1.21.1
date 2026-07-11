/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_1839
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_5321
 *  net.minecraft.class_6862
 *  net.minecraft.class_7924
 */
package punchy.client.state;

import java.util.List;
import java.util.Locale;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_1839;
import net.minecraft.class_239;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_7924;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.ToolTuning;
import punchy.client.state.AttackActionTracker;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.MovementStateMachine;
import punchy.client.state.UseActionTracker;
import punchy.config.PunchyTuningConfig;

public class SpearStateMachine {
    private static final long COMBO_RESET_TICKS = 30L;
    private static final class_6862<class_1792> SPEAR_TAG = class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)class_2960.method_60655((String)"minecraft", (String)"spears"));
    private boolean wasUsing;
    private class_1268 usingHand;
    private int activeStage;
    private AnimationClip activeUseClip;
    private boolean chargeMaxed;
    private boolean wasHitting;
    private long lastComboTick = -1L;
    private int comboIndex;
    private int lastComboSize;

    public void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (HandEquipStateMachine.isEquipInProgress()) {
            if (!this.shouldAbortEquipForAction(client)) {
                return;
            }
            if (!HandEquipStateMachine.abortEquipForAction(client)) {
                return;
            }
        }
        this.tickUse(client);
        this.checkImpact(client);
        class_1268 swingHand = AttackActionTracker.peekHand();
        if (swingHand == null) {
            return;
        }
        if (PunchyAnimationManager.isHandBlacklisted(client, swingHand)) {
            return;
        }
        class_1268 spearHand = this.resolveSpearHand(client);
        if (spearHand == null || spearHand != swingHand) {
            return;
        }
        AttackActionTracker.consumeHand();
        AnimationClip clip = this.resolveSpearAttackClip(client, spearHand);
        if (clip == null) {
            return;
        }
        this.playSpearAttack(client, spearHand, clip);
    }

    private boolean shouldAbortEquipForAction(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        boolean useDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
        boolean usingItem = client.field_1724.method_6115();
        class_1799 usingStack = client.field_1724.method_6030();
        boolean usingSpear = usingItem && useDown && SpearStateMachine.isSpearStack(usingStack);
        class_1268 swingHand = AttackActionTracker.peekHand();
        class_1268 spearHand = this.resolveSpearHand(client);
        boolean attacking = swingHand != null && spearHand != null && spearHand == swingHand;
        return usingSpear || attacking;
    }

    private void tickUse(class_310 client) {
        class_1268 hand;
        boolean usingSpear;
        if (client == null || client.field_1724 == null) {
            return;
        }
        boolean useDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
        boolean usingItem = client.field_1724.method_6115();
        class_1799 usingStack = client.field_1724.method_6030();
        if (!useDown) {
            this.chargeMaxed = false;
        }
        boolean bl = usingSpear = usingItem && useDown && SpearStateMachine.isSpearStack(usingStack);
        if (usingSpear && PunchyAnimationManager.isHandBlacklisted(client, hand = client.field_1724.method_6058())) {
            if (this.wasUsing) {
                UseActionTracker.clear();
                this.stopUse();
                this.wasUsing = false;
            }
            return;
        }
        if (this.chargeMaxed && useDown) {
            if (this.wasUsing) {
                UseActionTracker.clear();
                this.stopUse();
                this.wasUsing = false;
            }
            return;
        }
        if (usingSpear) {
            UseActionTracker.clear();
            hand = client.field_1724.method_6058();
            ChargeInfo info = this.resolveChargeInfo(client, usingStack);
            if (info.hasLimit && info.elapsedTicks > info.stage3Limit) {
                this.chargeMaxed = true;
                this.stopUse();
                this.wasUsing = false;
                return;
            }
            int stage = info.stage;
            if (stage == 0) {
                this.stopUse();
            } else {
                this.playUseStage(client, usingStack, hand, stage);
            }
        } else if (this.wasUsing) {
            UseActionTracker.clear();
            this.stopUse();
        }
        this.wasUsing = usingSpear;
    }

    private void checkImpact(class_310 client) {
        double dist;
        if (!this.wasUsing || client.field_1765 == null) {
            this.wasHitting = false;
            return;
        }
        boolean isHitting = false;
        if (client.field_1765.method_17783() == class_239.class_240.field_1331) {
            isHitting = true;
        } else if (client.field_1765.method_17783() == class_239.class_240.field_1332 && (dist = client.field_1765.method_17784().method_1022(client.field_1724.method_33571())) < 3.0) {
            isHitting = true;
        }
        if (isHitting && !this.wasHitting) {
            class_1268 hand = client.field_1724.method_6058();
            class_1306 arm = hand == class_1268.field_5810 ? (client.field_1724.method_6068() == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182) : client.field_1724.method_6068();
            MovementStateMachine.get().triggerSpearImpact(arm);
        }
        this.wasHitting = isHitting;
    }

    private void playUseStage(class_310 client, class_1799 stack, class_1268 hand, int stage) {
        AnimationClip clip = this.resolveUseClip(client, stack, hand, stage);
        if (clip == null) {
            return;
        }
        if (this.activeUseClip == clip && this.activeStage == stage && this.usingHand == hand) {
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
        float speedMultiplier = 1.0f / Math.max(0.01f, PunchyAnimationManager.ANIMATION_SPEED);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose();
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn, clip.isHoldOnLastFrame());
        this.activeUseClip = clip;
        this.activeStage = stage;
        this.usingHand = hand;
    }

    private void stopUse() {
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        PunchyAnimationManager.clearSourceHand();
        this.activeUseClip = null;
        this.activeStage = 0;
        this.usingHand = null;
    }

    private AnimationClip resolveUseClip(class_310 client, class_1799 stack, class_1268 hand, int stage) {
        AnimationClip custom = this.resolveCustomSpearChargeClip(client, stack, hand, stage);
        if (custom != null) {
            return custom;
        }
        if (stage == 1) {
            return PunchyAnimationManager.USE_SPEAR_POSE_1;
        }
        if (stage == 2) {
            return PunchyAnimationManager.USE_SPEAR_POSE_2;
        }
        if (stage == 3) {
            return PunchyAnimationManager.resolveHandedClip("use_spear_pose_3", client, hand);
        }
        return null;
    }

    private AnimationClip resolveCustomSpearChargeClip(class_310 client, class_1799 stack, class_1268 hand, int stage) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            String clipName;
            if (!SpearStateMachine.isSpearChargeType(entry)) continue;
            switch (stage) {
                case 1: {
                    String string = entry.stage_1;
                    break;
                }
                case 2: {
                    String string = entry.stage_2;
                    break;
                }
                case 3: {
                    String string = entry.stage_3;
                    break;
                }
                default: {
                    String string = clipName = null;
                }
            }
            if (clipName == null || clipName.isBlank()) {
                return null;
            }
            AnimationClip exact = PunchyAnimationManager.resolveCustomClip(clipName);
            if (exact != null) {
                return exact;
            }
            return PunchyAnimationManager.resolveNamedClip(clipName, client, hand);
        }
        return null;
    }

    private ChargeInfo resolveChargeInfo(class_310 client, class_1799 stack) {
        if (client == null || client.field_1724 == null) {
            return new ChargeInfo(1, 0, Integer.MAX_VALUE, false);
        }
        int useDuration = stack.method_7935((class_1309)client.field_1724) - (client.field_1724.method_6014() + 1);
        int elapsed = Math.max(0, useDuration);
        int stage1Limit = 10;
        int stage2Limit = 20;
        int stage3Limit = 30;
        if (elapsed < stage1Limit) {
            return new ChargeInfo(1, elapsed, stage3Limit, false);
        }
        if (elapsed < stage2Limit) {
            return new ChargeInfo(2, elapsed, stage3Limit, false);
        }
        return new ChargeInfo(3, elapsed, stage3Limit, false);
    }

    private AnimationClip resolveSpearAttackClip(class_310 client, class_1268 hand) {
        PunchyTuningConfig.CustomAnimationEntry entry;
        if (client == null || client.field_1724 == null) {
            return null;
        }
        class_1799 stack = client.field_1724.method_5998(hand);
        AnimationClip custom = this.resolveCustomSpearAttackClip(stack, hand, client, entry = CustomAnimationResolver.findEntry(stack, "attack"));
        if (custom != null) {
            return custom;
        }
        AnimationClip clip = PunchyAnimationManager.ATTACK_SPEAR_CLIP;
        if (clip != null) {
            return clip;
        }
        return PunchyAnimationManager.resolveHandedClip("attack_spear", client, hand);
    }

    private AnimationClip resolveCustomSpearAttackClip(class_1799 stack, class_1268 hand, class_310 client, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        List<String> variants = CustomAnimationResolver.resolveComboNames(entry);
        if (variants.isEmpty()) {
            return null;
        }
        int index = this.nextComboIndex(this.currentTick(client), variants.size());
        String clipName = variants.get(index);
        return PunchyAnimationManager.resolveNamedClip(clipName, client, hand);
    }

    private int nextComboIndex(long now, int total) {
        if (total <= 1) {
            return 0;
        }
        if (this.lastComboTick < 0L || now - this.lastComboTick > 30L || total != this.lastComboSize) {
            this.comboIndex = 0;
        }
        if (this.comboIndex < 0 || this.comboIndex >= total) {
            this.comboIndex = 0;
        }
        int current = this.comboIndex;
        this.comboIndex = (this.comboIndex + 1) % total;
        this.lastComboTick = now;
        this.lastComboSize = total;
        return current;
    }

    private void playSpearAttack(class_310 client, class_1268 hand, AnimationClip clip) {
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose();
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn);
    }

    private class_1268 resolveSpearHand(class_310 client) {
        class_1799 main = client.field_1724.method_6047();
        if (this.isSpear(main)) {
            return class_1268.field_5808;
        }
        class_1799 offhand = client.field_1724.method_6079();
        if (this.isSpear(offhand)) {
            return class_1268.field_5810;
        }
        return null;
    }

    public static boolean isSpearStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_7909() instanceof class_1835) {
            return false;
        }
        if (SpearStateMachine.hasCustomSpearChargeAnimation(stack)) {
            return true;
        }
        if (ToolTuning.getSpecificCustomAnimation(stack) != null) {
            return false;
        }
        if (stack.method_7976() == class_1839.field_8951) {
            return true;
        }
        return stack.method_31573(SPEAR_TAG);
    }

    private static boolean hasCustomSpearChargeAnimation(class_1799 stack) {
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return false;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            if (!SpearStateMachine.isSpearChargeType(entry)) continue;
            return true;
        }
        return false;
    }

    private static boolean isSpearChargeType(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null || entry.type == null || entry.type.isBlank()) {
            return false;
        }
        String normalized = entry.type.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        return "spearcharge".equals(normalized);
    }

    private long currentTick(class_310 client) {
        return client.field_1687 == null ? 0L : client.field_1687.method_8510();
    }

    private boolean isSpear(class_1799 stack) {
        return SpearStateMachine.isSpearStack(stack);
    }

    private static class ChargeInfo {
        final int stage;
        final int elapsedTicks;
        final int stage3Limit;
        final boolean hasLimit;

        private ChargeInfo(int stage, int elapsedTicks, int stage3Limit, boolean hasLimit) {
            this.stage = stage;
            this.elapsedTicks = elapsedTicks;
            this.stage3Limit = stage3Limit;
            this.hasLimit = hasLimit;
        }
    }
}

