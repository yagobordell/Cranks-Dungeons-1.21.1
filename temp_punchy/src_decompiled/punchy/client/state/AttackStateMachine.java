/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3966
 *  net.minecraft.class_5321
 *  net.minecraft.class_6862
 *  net.minecraft.class_7924
 *  net.minecraft.class_9362
 */
package punchy.client.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_239;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_7924;
import net.minecraft.class_9362;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.ToolKindResolver;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.AttackActionTracker;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.MiningActionTracker;
import punchy.compat.BetterCombatCompat;
import punchy.compat.CrawlCompat;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public class AttackStateMachine {
    private static final long FAST_TRIGGER_WINDOW_TICKS = 4L;
    private static final double VISUAL_ATTACK_MAX_CPS = 3.5;
    private static final double VISUAL_ATTACK_INTERVAL_TICKS = 5.714285714285714;
    private static final float THROTTLED_VISUAL_SPEED_MULTIPLIER = 1.12f;
    private static final int RECENT_EQUIP_SKIP_BLEND_WINDOW_TICKS = 8;
    private static final long COMBO_RESET_TICKS = 30L;
    private static final long TOOL_MINING_FALLBACK_MAX_TICKS = 4L;
    private long lastTriggerTick = -1L;
    private long lastComboTick = -1L;
    private int comboIndex;
    private int lastComboSize;
    private boolean toolMiningFallbackActive;
    private boolean toolMiningFallbackStopImmediate;
    private boolean toolMiningFallbackSkipBlendOut;
    private long toolMiningFallbackExpireTick = -1L;
    private String toolMiningFallbackClipName;
    private double nextVisualAttackTick = -1.0;
    private boolean visualThrottlePending;

    public void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        this.updateToolMiningFallback(client);
        boolean abortedEquipForAttack = false;
        class_1268 swingHand = AttackActionTracker.peekHand();
        if (swingHand == null) {
            return;
        }
        this.clearStaleCrawlOverlayBeforeAttack(client, "queued");
        if (this.isAttackAnimationCoolingDown(client)) {
            this.visualThrottlePending = true;
            return;
        }
        if (PunchyAnimationManager.isHandBlacklisted(client, swingHand)) {
            AttackActionTracker.consumeHand();
            return;
        }
        if (this.shouldAbortEquipForHand(client, swingHand) && HandEquipStateMachine.isEquipInProgress()) {
            if (!HandEquipStateMachine.abortEquipForAction(client, swingHand)) {
                return;
            }
            abortedEquipForAttack = true;
        }
        if ((swingHand = AttackActionTracker.consumeHand()) == null) {
            return;
        }
        this.playAttack(client, swingHand, abortedEquipForAttack);
    }

    private void playAttack(class_310 client, class_1268 hand, boolean abortedEquipForAttack) {
        AnimationClip tridentClip;
        AnimationClip maceClip;
        String criticalName;
        class_1799 stack = this.resolveAttackStack(client, hand);
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "attack");
        if (this.requiresEntityHit(stack) && !this.isEntityTarget(client) && entry == null) {
            if (this.shouldSuppressToolMiningFallback(client)) {
                this.clearToolMiningFallbackState();
                return;
            }
            this.playToolMiningFallback(client, hand, stack);
            return;
        }
        ItemAnimationManager.forceTypeOnce(hand, "attack", true);
        boolean criticalHit = AttackActionTracker.consumeCritical();
        AnimationClip clip = null;
        boolean usedBetterCombat = false;
        if (criticalHit && (criticalName = CustomAnimationResolver.resolveCriticalName(entry)) != null) {
            clip = PunchyAnimationManager.resolveNamedClip(criticalName, client, hand);
        }
        if (clip == null && BetterCombatCompat.isAvailable() && PunchyConfig.isBetterCombatCompatEnabled()) {
            clip = this.resolveBetterCombatClip(stack, hand, client);
            boolean bl = usedBetterCombat = clip != null;
        }
        if (clip == null) {
            clip = this.resolveCustomAttackClip(stack, hand, client, entry);
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        if (clip == null) {
            CustomAnimationResolver.applyLoopSpeed(null);
            clip = PunchyAnimationManager.resolveHandedClip("attack", client, hand, preferredOption);
        } else if (usedBetterCombat) {
            CustomAnimationResolver.applyLoopSpeed(null);
        } else {
            CustomAnimationResolver.applyLoopSpeed(entry);
        }
        if (PunchyAnimationManager.isClipNamed(clip, "attack") && stack.method_7909() instanceof class_9362 && (maceClip = PunchyAnimationManager.resolveHandedClip("attack_mace", client, hand, preferredOption)) != null) {
            clip = maceClip;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "attack") && stack.method_7909() instanceof class_1835 && (tridentClip = PunchyAnimationManager.resolveHandedClip("attack_trident", client, hand, preferredOption)) != null) {
            clip = tridentClip;
        }
        if (clip != null) {
            class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
            boolean dualHanded = PunchyAnimationManager.isDualHandedActive(client, clip, 0.0f);
            boolean skipOffhandCombo = dualHanded && usedBetterCombat && !this.isOffhandWeapon(client, hand);
            boolean preserveOppositeEquipFlow = dualHanded && HandEquipStateMachine.isEquipInProgress() && arm != null;
            class_1306 offArm = arm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
            PunchyAnimationManager.POSE_HANDLER.setForcedSkipArmForNextPlay((class_1306)(skipOffhandCombo || preserveOppositeEquipFlow ? offArm : null));
            PunchyAnimationManager.setSuppressOffhandForAttack(clip, false);
            if (dualHanded && !skipOffhandCombo && !preserveOppositeEquipFlow) {
                arm = null;
            }
            this.primeOppositeArmOverlapDuringEquip(client, hand, arm);
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            long now = this.currentTick(client);
            float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
            if (this.visualThrottlePending) {
                speedMultiplier *= 1.12f;
            }
            boolean skipBlendIn = abortedEquipForAttack || HandEquipStateMachine.wasEquipTransitionRecent(client, 8) || this.shouldSkipBlendIn(client) || CustomAnimationResolver.shouldSkipBlendIn(entry);
            CustomAnimationResolver.playConfiguredClip(clip, entry, speedMultiplier, skipBlendIn, false);
            this.lastTriggerTick = now;
            this.scheduleNextVisualAttack(now);
            this.visualThrottlePending = false;
        }
    }

    private boolean isOffhandWeapon(class_310 client, class_1268 hand) {
        if (client == null || client.field_1724 == null) {
            return false;
        }
        class_1268 offhand = hand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
        class_1799 stack = client.field_1724.method_5998(offhand);
        return BetterCombatCompat.isWeaponStack(stack);
    }

    private class_1799 resolveAttackStack(class_310 client, class_1268 hand) {
        if (client == null || client.field_1724 == null) {
            return class_1799.field_8037;
        }
        class_1268 resolved = hand == null ? class_1268.field_5808 : hand;
        return client.field_1724.method_5998(resolved);
    }

    private boolean requiresEntityHit(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return stack.method_31573(class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)class_2960.method_60655((String)"minecraft", (String)"pickaxes"))) || stack.method_31573(class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)class_2960.method_60655((String)"minecraft", (String)"hoes"))) || stack.method_31573(class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)class_2960.method_60655((String)"minecraft", (String)"shovels")));
    }

    private boolean isEntityTarget(class_310 client) {
        if (client == null) {
            return false;
        }
        class_239 hit = client.field_1765;
        if (hit == null || hit.method_17783() != class_239.class_240.field_1331) {
            return false;
        }
        if (hit instanceof class_3966) {
            class_3966 entityHit = (class_3966)hit;
            return entityHit.method_17782() != null;
        }
        return false;
    }

    private void playToolMiningFallback(class_310 client, class_1268 hand, class_1799 stack) {
        if (client == null || stack == null || stack.method_7960()) {
            return;
        }
        ItemAnimationManager.forceTypeOnce(hand, "mining", true);
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "mining");
        AnimationClip clip = this.resolveCustomMiningClip(client, hand, entry);
        if (clip == null) {
            VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
            if (kind == VanillaFirstPersonItemLayer.ToolKind.PICKAXE || kind == VanillaFirstPersonItemLayer.ToolKind.HOE || kind == VanillaFirstPersonItemLayer.ToolKind.AXE || kind == VanillaFirstPersonItemLayer.ToolKind.SHOVEL) {
                if (kind == VanillaFirstPersonItemLayer.ToolKind.AXE) {
                    clip = PunchyAnimationManager.resolveHandedClip("mining_sideways", client, hand, preferredOption);
                }
                if (clip == null) {
                    clip = PunchyAnimationManager.resolveTypeFallbackClip("mining", client, hand, preferredOption);
                }
                if (clip == null) {
                    clip = kind == VanillaFirstPersonItemLayer.ToolKind.PICKAXE || kind == VanillaFirstPersonItemLayer.ToolKind.HOE ? PunchyAnimationManager.resolveHandedClip("mining_foward", client, hand, preferredOption) : (kind == VanillaFirstPersonItemLayer.ToolKind.AXE ? PunchyAnimationManager.resolveHandedClip("mining_foward", client, hand, preferredOption) : PunchyAnimationManager.resolveHandedClip("mining_digging", client, hand, preferredOption));
                }
            }
        } else {
            CustomAnimationResolver.applyLoopSpeed(entry);
        }
        if (clip == null) {
            return;
        }
        boolean stopImmediate = CustomAnimationResolver.shouldStopImmediately(entry);
        boolean skipBlendOut = CustomAnimationResolver.shouldSkipBlendOut(entry);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        this.primeOppositeArmOverlapDuringEquip(client, hand, arm);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        boolean skipBlendIn = this.shouldSkipBlendIn(client) || CustomAnimationResolver.shouldSkipBlendIn(entry);
        CustomAnimationResolver.playConfiguredClip(clip, entry, speedMultiplier, skipBlendIn, false);
        this.lastTriggerTick = this.currentTick(client);
        this.toolMiningFallbackActive = true;
        this.toolMiningFallbackStopImmediate = stopImmediate;
        this.toolMiningFallbackSkipBlendOut = skipBlendOut;
        this.toolMiningFallbackExpireTick = this.lastTriggerTick + 4L;
        this.toolMiningFallbackClipName = clip.getName();
    }

    private void updateToolMiningFallback(class_310 client) {
        boolean expired;
        if (!this.toolMiningFallbackActive) {
            return;
        }
        if (client == null || client.field_1724 == null) {
            this.clearToolMiningFallbackState();
            return;
        }
        if (this.shouldSuppressToolMiningFallback(client)) {
            this.clearToolMiningFallbackState();
            return;
        }
        AnimationClip activeClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying() || activeClip == null || this.toolMiningFallbackClipName == null || !this.toolMiningFallbackClipName.equals(activeClip.getName())) {
            this.clearToolMiningFallbackState();
            return;
        }
        long now = this.currentTick(client);
        boolean bl = expired = this.toolMiningFallbackExpireTick >= 0L && now >= this.toolMiningFallbackExpireTick;
        if (!expired) {
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        if (this.toolMiningFallbackSkipBlendOut) {
            PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
        }
        if (this.toolMiningFallbackStopImmediate) {
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        } else {
            PunchyAnimationManager.POSE_HANDLER.requestStopAfterCurrent();
        }
        this.clearToolMiningFallbackState();
    }

    private boolean shouldSuppressToolMiningFallback(class_310 client) {
        if (client == null) {
            return false;
        }
        if (MiningActionTracker.isMiningActive()) {
            return true;
        }
        class_239 hit = client.field_1765;
        return hit != null && hit.method_17783() == class_239.class_240.field_1332;
    }

    private void clearToolMiningFallbackState() {
        this.toolMiningFallbackActive = false;
        this.toolMiningFallbackStopImmediate = false;
        this.toolMiningFallbackSkipBlendOut = false;
        this.toolMiningFallbackExpireTick = -1L;
        this.toolMiningFallbackClipName = null;
    }

    public void resetSessionState() {
        this.lastTriggerTick = -1L;
        this.lastComboTick = -1L;
        this.comboIndex = 0;
        this.lastComboSize = 0;
        this.nextVisualAttackTick = -1.0;
        this.visualThrottlePending = false;
        this.clearToolMiningFallbackState();
    }

    private AnimationClip resolveCustomMiningClip(class_310 client, class_1268 hand, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (client == null) {
            return null;
        }
        class_1799 stack = this.resolveAttackStack(client, hand);
        return CustomAnimationResolver.resolveNamedOrTypeFallback(stack, "mining", entry, client, hand).clip();
    }

    private boolean shouldSkipBlendIn(class_310 client) {
        long now = this.currentTick(client);
        return this.lastTriggerTick > 0L && now - this.lastTriggerTick <= 4L;
    }

    private boolean isAttackAnimationCoolingDown(class_310 client) {
        double now = this.currentTick(client);
        return this.nextVisualAttackTick >= 0.0 && now < this.nextVisualAttackTick;
    }

    private void scheduleNextVisualAttack(long now) {
        if (this.nextVisualAttackTick < 0.0 || (double)now >= this.nextVisualAttackTick + 5.714285714285714) {
            this.nextVisualAttackTick = (double)now + 5.714285714285714;
            return;
        }
        this.nextVisualAttackTick += 5.714285714285714;
    }

    private long currentTick(class_310 client) {
        return client.field_1687 == null ? 0L : client.field_1687.method_8510();
    }

    private void clearStaleCrawlOverlayBeforeAttack(class_310 client, String stage) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (CrawlCompat.isCrawling((class_1657)client.field_1724)) {
            return;
        }
        AnimationClip swimClip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
        if (!PunchyAnimationManager.isClipNamed(swimClip, "crawl")) {
            return;
        }
        PunchyAnimationManager.SWIM_HANDLER.clearRequestedArmFilters();
        PunchyAnimationManager.SWIM_HANDLER.setPaused(false);
        PunchyAnimationManager.SWIM_HANDLER.setFrozen(false);
        PunchyAnimationManager.SWIM_HANDLER.clearCurrentClipState();
    }

    private boolean shouldAbortEquipForHand(class_310 client, class_1268 hand) {
        if (client == null || hand == null) {
            return true;
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

    private AnimationClip resolveCustomAttackClip(class_1799 stack, class_1268 hand, class_310 client, PunchyTuningConfig.CustomAnimationEntry entry) {
        if (stack == null) {
            return null;
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForEntry(stack, entry);
        List<String> variants = CustomAnimationResolver.resolveComboNames(entry);
        if (variants.isEmpty()) {
            return null;
        }
        int index = this.nextComboIndex(this.currentTick(client), variants.size());
        String clipName = variants.get(index);
        return PunchyAnimationManager.resolveNamedClip(clipName, client, hand, preferredOption);
    }

    private AnimationClip resolveBetterCombatClip(class_1799 stack, class_1268 hand, class_310 client) {
        List<String> variants;
        PunchyTuningConfig.BetterCombatEntry entry = CustomAnimationResolver.findBetterCombatEntry(stack, "melee_combo");
        if (entry == null) {
            return null;
        }
        String preferredOption = PunchyTuningConfig.getMixPreferredOptionForStack(stack);
        boolean singleWeaponMode = !this.isOffhandWeapon(client, hand);
        List<String> list = variants = singleWeaponMode ? this.resolveSingleWeaponBetterCombatNames(entry) : CustomAnimationResolver.resolveBetterCombatComboNames(entry);
        if (variants.isEmpty()) {
            return null;
        }
        int index = this.nextComboIndex(this.currentTick(client), variants.size());
        String clipName = variants.get(index);
        return PunchyAnimationManager.resolveNamedClip(clipName, client, hand, preferredOption);
    }

    private List<String> resolveSingleWeaponBetterCombatNames(PunchyTuningConfig.BetterCombatEntry entry) {
        ArrayList<String> variants = new ArrayList<String>(2);
        AttackStateMachine.addIfPresent(variants, entry == null ? null : entry.attack_1);
        AttackStateMachine.addIfPresent(variants, entry == null ? null : entry.attack_3);
        if (!variants.isEmpty()) {
            return variants;
        }
        return CustomAnimationResolver.resolveBetterCombatComboNames(entry);
    }

    private static void addIfPresent(List<String> out, String name) {
        if (out == null || name == null || name.isBlank()) {
            return;
        }
        String trimmed = name.trim();
        if (!trimmed.isEmpty()) {
            out.add(trimmed);
        }
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
}

