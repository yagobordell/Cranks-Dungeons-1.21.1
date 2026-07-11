/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 */
package punchy.client.state;

import java.util.Locale;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public final class IdlePoseStateMachine {
    private static final float IDLE_RETURN_BLEND_SECONDS = 0.27f;
    private String activeIdleClipName = null;
    private class_1306 activeIdleArm = null;

    public void tick(class_310 client) {
        String desiredClipName;
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (client == null || client.field_1724 == null) {
            this.clearActiveIdle(handler);
            return;
        }
        String blockReason = this.resolveBlockReason(handler);
        if (blockReason != null) {
            this.clearActiveIdle(handler);
            return;
        }
        IdleSelection desired = this.resolveDesiredIdle(client, handler);
        if (desired == null || desired.clip() == null || desired.arm() == null) {
            this.clearActiveIdle(handler);
            return;
        }
        if (this.isForeignSecondaryActive(handler, desired.arm())) {
            if (this.shouldIgnoreForeignSecondary(handler)) {
                handler.clearSecondaryClip();
            } else {
                this.activeIdleClipName = null;
                this.activeIdleArm = null;
                return;
            }
        }
        if (this.isDesiredIdleAlreadyApplied(handler, desired, desiredClipName = this.clipName(desired.clip()))) {
            return;
        }
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(desired.clip());
        handler.setSecondaryClipAtTimeWithBlendFromCurrentAndFlags(desired.clip(), desired.arm(), speedMultiplier, 0.0f, true, 0.27f, desired.flags());
        handler.setSecondaryCompatIdle(true);
        this.activeIdleClipName = desiredClipName;
        this.activeIdleArm = desired.arm();
    }

    private IdleSelection resolveDesiredIdle(class_310 client, PoseHandler handler) {
        class_1306 mainArm = client.field_1724.method_6068();
        return this.resolveIdleForArm(client, mainArm);
    }

    private IdleSelection resolveIdleForArm(class_310 client, class_1306 arm) {
        CustomAnimationResolver.ClipLookup lookup;
        if (client == null || client.field_1724 == null || arm == null) {
            return null;
        }
        class_1268 hand = PunchyAnimationManager.getHandForArm(client, arm);
        if (hand == null || PunchyConfig.isHandBlacklisted((class_1657)client.field_1724, hand)) {
            return null;
        }
        class_1799 stack = this.resolveStack(client, arm);
        if (stack == null || stack.method_7960()) {
            return null;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = CustomAnimationResolver.findEntry(stack, "idle", false);
        if (entry == null) {
            entry = CustomAnimationResolver.findEntry(stack, "idle");
        }
        if ((lookup = CustomAnimationResolver.resolveNamedOrTypeFallback(stack, "idle", entry, client, hand)) == null || lookup.clip() == null) {
            return null;
        }
        return new IdleSelection(lookup.clip(), arm, entry != null ? entry.customFlags : null);
    }

    private void clearActiveIdle(PoseHandler handler) {
        if (handler == null) {
            this.activeIdleClipName = null;
            this.activeIdleArm = null;
            return;
        }
        if (handler.isSecondaryCompatIdle()) {
            handler.clearSecondaryClipWithBlendOut();
            handler.setSecondaryCompatIdle(false);
        }
        this.activeIdleClipName = null;
        this.activeIdleArm = null;
    }

    private boolean isForeignSecondaryActive(PoseHandler handler, class_1306 desiredArm) {
        if (handler == null || desiredArm == null) {
            return false;
        }
        if (handler.isSecondaryCompatIdle()) {
            return false;
        }
        return handler.getSecondaryClip() != null && handler.getSecondaryArm() != null && handler.getSecondaryArm() != desiredArm;
    }

    private boolean shouldIgnoreForeignSecondary(PoseHandler handler) {
        if (handler == null) {
            return false;
        }
        AnimationClip secondary = handler.getSecondaryClip();
        if (secondary == null) {
            return false;
        }
        if (this.isEquipClip(secondary)) {
            return false;
        }
        return handler.getCurrentClip() == null && !handler.isPlaying() && !handler.isBlendOutActive();
    }

    private boolean isDesiredIdleAlreadyApplied(PoseHandler handler, IdleSelection desired, String desiredClipName) {
        if (handler == null || desired == null || desiredClipName == null) {
            return false;
        }
        if (!desiredClipName.equals(this.activeIdleClipName) || desired.arm() != this.activeIdleArm) {
            return false;
        }
        if (!handler.isSecondaryCompatIdle()) {
            return false;
        }
        AnimationClip secondary = handler.getSecondaryClip();
        return secondary != null && desiredClipName.equals(this.clipName(secondary)) && handler.getSecondaryArm() == desired.arm();
    }

    private class_1799 resolveStack(class_310 client, class_1306 arm) {
        boolean mainRight;
        boolean bl = mainRight = client.field_1724.method_6068() == class_1306.field_6183;
        return arm == class_1306.field_6183 ? (mainRight ? client.field_1724.method_6047() : client.field_1724.method_6079()) : (mainRight ? client.field_1724.method_6079() : client.field_1724.method_6047());
    }

    private String resolveBlockReason(PoseHandler handler) {
        if (handler == null) {
            return "no_handler";
        }
        AnimationClip primary = handler.getCurrentClip();
        if (primary != null && !this.isEquipClip(primary) && handler.isPlaying()) {
            return "pose_playing";
        }
        boolean walkOnWaterActive = ItemAnimationManager.isTypeActive("walk_on_water_v2");
        boolean compatIdleActive = handler.isSecondaryCompatIdle();
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            if (walkOnWaterActive && compatIdleActive) {
                return null;
            }
            return "swim_playing";
        }
        if (PunchyAnimationManager.SWIM_HANDLER.isBlendOutActive()) {
            if (walkOnWaterActive && compatIdleActive) {
                return null;
            }
            return "swim_blend_out";
        }
        if (PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() != null) {
            if (walkOnWaterActive && compatIdleActive) {
                return null;
            }
            return "swim_clip_lingering";
        }
        if (walkOnWaterActive) {
            return "item_walk_on_water";
        }
        if (ItemAnimationManager.isTypeActive("mining")) {
            return "item_mining";
        }
        if (ItemAnimationManager.isTypeActive("useItem")) {
            return "item_use";
        }
        return null;
    }

    private boolean isEquipClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name == null) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("hand_in") || normalized.equals("hand_out") || normalized.startsWith("hand_in_") || normalized.startsWith("hand_out_");
    }

    private String clipName(AnimationClip clip) {
        return clip == null ? "null" : clip.getName();
    }

    private record IdleSelection(AnimationClip clip, class_1306 arm, PunchyTuningConfig.CustomFlags flags) {
    }
}

