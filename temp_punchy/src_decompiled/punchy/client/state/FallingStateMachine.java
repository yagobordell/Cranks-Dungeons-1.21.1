/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_746
 */
package punchy.client.state;

import java.util.List;
import java.util.Locale;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;
import punchy.config.PunchyTuningConfig;

public class FallingStateMachine {
    private static final float MIN_FALL_DISTANCE_BLOCKS = 0.5f;
    private static final float FALLING_ENTRY_EPSILON = 1.0E-4f;
    private static final double FALLING_GROUND_DISTANCE_TRIGGER = 2.0;
    private static final double GROUND_SCAN_DEPTH = 12.0;
    private static final double EARLY_TRIGGER_FALL_VELOCITY = -0.04;
    private static final float FALLING_SPEED_START_BOOST = 15.0f;
    private static final float FALLING_SPEED_END_BOOST = 8.0f;
    private static final long FALLING_SPEED_RAMP_NANOS = 1000000000L;
    private static String activeType;
    private AnimationClip activeClip;
    private PunchyTuningConfig.CustomAnimationEntry activeEntry;
    private long fallingStartNanos = -1L;

    public void tick(class_310 client) {
        boolean falling;
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (HandEquipStateMachine.isEquipInProgress()) {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "falling");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "falling");
            return;
        }
        boolean onGround = client.field_1724.method_24828();
        boolean inWater = client.field_1724.method_5799() || client.field_1724.method_5869();
        class_746 player = client.field_1724;
        double deltaY = player.method_18798().field_1351;
        boolean descending = deltaY < 0.0 || player.field_6017 > 0.0f;
        boolean largeFall = client.field_1724.field_6017 >= 0.5f || deltaY <= -0.04;
        double groundDistance = FallingStateMachine.distanceToGround(player);
        float triggerFallBlocks = FallingStateMachine.resolveTriggerFallBlocks(player, groundDistance);
        boolean twoBlocksAboveGround = groundDistance >= 2.0;
        boolean bl = falling = !onGround && !inWater && (twoBlocksAboveGround || descending && largeFall);
        if (!falling) {
            ItemAnimationManager.clearPersistentType(class_1268.field_5808, "falling");
            ItemAnimationManager.clearPersistentType(class_1268.field_5810, "falling");
            this.stopIfActive();
            return;
        }
        ItemAnimationManager.setPersistentType(class_1268.field_5808, "falling", true, true);
        ItemAnimationManager.setPersistentType(class_1268.field_5810, "falling", true, true);
        activeType = "falling";
        String desiredType = "falling";
        ClipSelection selection = this.resolveCustomClip(client, desiredType, triggerFallBlocks);
        if (selection.clip == null) {
            if (this.activeClip != null) {
                this.stopIfActive();
            }
            return;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        boolean playing = PunchyAnimationManager.POSE_HANDLER.isPlaying();
        if (playing && FallingStateMachine.isAttackLikeClip(current)) {
            return;
        }
        if (playing && current != selection.clip) {
            this.stopIfActive();
            playing = PunchyAnimationManager.POSE_HANDLER.isPlaying();
            current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        }
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(selection.clip) * this.resolveFallingSpeedBoost();
        if (current != selection.clip || !playing) {
            class_1306 arm;
            class_1306 class_13062 = arm = selection.hand == null ? null : PunchyAnimationManager.getUseArm(client, selection.hand);
            if (PunchyAnimationManager.isDualHandedActive(client, selection.clip, 0.0f)) {
                arm = null;
            }
            CustomAnimationResolver.applyLoopSpeed(selection.entry);
            PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
            PunchyAnimationManager.POSE_HANDLER.setLooping(true);
            boolean skipBlendIn = true;
            PunchyAnimationManager.POSE_HANDLER.play(selection.clip, speedMultiplier, skipBlendIn, false);
            this.activeClip = selection.clip;
            this.activeEntry = selection.entry;
            activeType = desiredType;
        } else {
            PunchyAnimationManager.POSE_HANDLER.setSpeedMultiplier(speedMultiplier);
        }
    }

    private void stopIfActive() {
        AnimationClip current;
        if (this.activeClip != null && (current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip()) == this.activeClip) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
            if (CustomAnimationResolver.shouldSkipBlendOut(this.activeEntry)) {
                PunchyAnimationManager.POSE_HANDLER.setBlendOutDurationNext(0.0f);
            }
            PunchyAnimationManager.POSE_HANDLER.stopImmediateWithBlendOut();
        }
        this.activeClip = null;
        activeType = null;
        this.activeEntry = null;
        this.fallingStartNanos = -1L;
    }

    public static String getActiveType() {
        return activeType;
    }

    private ClipSelection resolveCustomClip(class_310 client, String type, float triggerFallBlocks) {
        if (client == null || client.field_1724 == null) {
            return new ClipSelection(null, null, null);
        }
        class_1799 main = client.field_1724.method_6047();
        ClipSelection selection = this.resolveFromStack(client, main, class_1268.field_5808, type, triggerFallBlocks);
        if (selection.clip != null) {
            return selection;
        }
        class_1799 off = client.field_1724.method_6079();
        return this.resolveFromStack(client, off, class_1268.field_5810, type, triggerFallBlocks);
    }

    private ClipSelection resolveFromStack(class_310 client, class_1799 stack, class_1268 hand, String type, float triggerFallBlocks) {
        if (stack == null) {
            return new ClipSelection(null, null, null);
        }
        PunchyTuningConfig.CustomAnimationEntry entry = this.resolveBestEntryForFalling(stack, type, triggerFallBlocks);
        CustomAnimationResolver.ClipLookup lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, type, entry, client, hand);
        if (lookup.clip() == null) {
            return new ClipSelection(null, null, null);
        }
        return new ClipSelection(lookup.clip(), hand, entry);
    }

    private PunchyTuningConfig.CustomAnimationEntry resolveBestEntryForFalling(class_1799 stack, String type, float triggerFallBlocks) {
        List<PunchyTuningConfig.CustomAnimationEntry> entries = CustomAnimationResolver.findEntries(stack, type);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        PunchyTuningConfig.CustomAnimationEntry best = null;
        float bestMinBlocks = -1.0f;
        for (PunchyTuningConfig.CustomAnimationEntry candidate : entries) {
            float minBlocks;
            if (candidate == null || triggerFallBlocks + 1.0E-4f < (minBlocks = FallingStateMachine.resolveMinBlocks(candidate)) || best != null && !(minBlocks > bestMinBlocks)) continue;
            best = candidate;
            bestMinBlocks = minBlocks;
        }
        return best;
    }

    private static float resolveMinBlocks(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null || entry.min_blocks == null) {
            return 0.0f;
        }
        return Math.max(0.0f, entry.min_blocks.floatValue());
    }

    private static float resolveTriggerFallBlocks(class_746 player, double groundDistance) {
        if (player == null) {
            return 0.0f;
        }
        float current = (float)Math.max(0.0, (double)player.field_6017);
        float distanceToGround = (float)Math.max(0.0, groundDistance);
        return current + distanceToGround;
    }

    private static double distanceToGround(class_746 player) {
        if (player == null || player.method_37908() == null) {
            return -1.0;
        }
        class_238 box = player.method_5829();
        double feetY = box.field_1322 + 0.01;
        double insetX = Math.max(0.01, (box.field_1320 - box.field_1323) * 0.35);
        double insetZ = Math.max(0.01, (box.field_1324 - box.field_1321) * 0.35);
        double centerX = (box.field_1323 + box.field_1320) * 0.5;
        double centerZ = (box.field_1321 + box.field_1324) * 0.5;
        double[] xs = new double[]{centerX, box.field_1323 + insetX, box.field_1320 - insetX, box.field_1323 + insetX, box.field_1320 - insetX};
        double[] zs = new double[]{centerZ, box.field_1321 + insetZ, box.field_1321 + insetZ, box.field_1324 - insetZ, box.field_1324 - insetZ};
        double best = -1.0;
        for (int i = 0; i < xs.length; ++i) {
            double distance;
            class_243 start = new class_243(xs[i], feetY, zs[i]);
            class_243 end = start.method_1023(0.0, 12.0, 0.0);
            class_3965 hit = player.method_37908().method_17742(new class_3959(start, end, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)player));
            if (hit.method_17783() != class_239.class_240.field_1332 || !((distance = Math.max(0.0, start.field_1351 - hit.method_17784().field_1351)) > best)) continue;
            best = distance;
        }
        return best;
    }

    private float resolveFallingSpeedBoost() {
        long now = System.nanoTime();
        if (this.fallingStartNanos < 0L) {
            this.fallingStartNanos = now;
            return 15.0f;
        }
        long elapsed = Math.max(0L, now - this.fallingStartNanos);
        float t = Math.min(1.0f, (float)elapsed / 1.0E9f);
        return class_3532.method_16439((float)t, (float)15.0f, (float)8.0f);
    }

    private static boolean isAttackLikeClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("attack") || normalized.contains("critical");
    }

    private record ClipSelection(AnimationClip clip, class_1268 hand, PunchyTuningConfig.CustomAnimationEntry entry) {
    }
}

