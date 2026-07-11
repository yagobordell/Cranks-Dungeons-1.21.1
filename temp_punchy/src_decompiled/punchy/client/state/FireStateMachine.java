/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package punchy.client.state;

import net.minecraft.class_1268;
import net.minecraft.class_310;
import net.minecraft.class_746;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CustomAnimationResolver;
import punchy.client.state.HandEquipStateMachine;

public class FireStateMachine {
    private static final float FIRE_ANIM_SECONDS = 1.7713f;
    private static final int FIRE_ANIM_TICKS = Math.round(35.426f);
    private boolean wasOnFire;
    private boolean playedThisFire;

    public void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (HandEquipStateMachine.isEquipInProgress()) {
            return;
        }
        class_746 player = client.field_1724;
        boolean onFire = player.method_5809();
        if (onFire) {
            if (!this.wasOnFire) {
                this.playedThisFire = false;
            }
        } else if (this.wasOnFire && !this.playedThisFire) {
            this.triggerPutOut();
            this.playedThisFire = true;
        }
        this.wasOnFire = onFire;
    }

    private void triggerPutOut() {
        AnimationClip clip = PunchyAnimationManager.PUT_OUT_FIRE_CLIP;
        if (clip == null) {
            return;
        }
        if (PunchyAnimationManager.POSE_HANDLER.getCurrentClip() == clip && PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return;
        }
        float speedMultiplier = 1.0f / Math.max(0.01f, PunchyAnimationManager.ANIMATION_SPEED);
        class_310 client = class_310.method_1551();
        PunchyAnimationManager.setSourceHand(client, class_1268.field_5808);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(PunchyAnimationManager.getUseArm(client, class_1268.field_5808));
        PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        boolean skipBlendIn = CustomAnimationResolver.shouldSkipBlendInOnIdlePose();
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, skipBlendIn, false);
    }
}

