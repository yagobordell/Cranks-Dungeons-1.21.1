package com.cranks.dungeons.compat.punchy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.cranks.dungeons.client.RangedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.data.AnimationClip;
import punchy.client.state.CrossbowStateMachine;
import com.llamalad7.mixinextras.sugar.Local;

/** Punchy 3.2 uses playChargePlayback rather than the old playCrossbow entry point. */
@Mixin(value=CrossbowStateMachine.class,remap=false)
public abstract class PunchyCrossbowStateMachineMixin {
    @Shadow abstract boolean isShotPlaying(PoseHandler pose);

    @Inject(method="observeShot",at=@At("HEAD"),cancellable=true)
    private void cranks$observeActualShot(Hand hand, boolean charged, boolean using, boolean usePressed,
                                        CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(RangedClient.consumeCrossbowShot(hand));
    }

    @Inject(method="tick",at=@At("TAIL"))
    private void cranks$finishShotBeforeReload(MinecraftClient client, CallbackInfo ci) {
        RangedClient.crossbowShotPlaying(client!=null && client.player!=null
                && isShotPlaying(PunchyAnimationManager.POSE_HANDLER));
    }
    @Inject(method={"playChargePlayback(Lpunchy/client/animation/PoseHandler;Lpunchy/client/animation/data/AnimationClip;ZZLnet/minecraft/util/Arm;)V",
                    "playChargePlayback(Lpunchy/client/animation/PoseHandler;Lpunchy/client/animation/data/AnimationClip;ZZ)V"},at=@At("TAIL"),require=1)
    private void cranks$singleReload(CallbackInfo ci, @Local(argsOnly=true) PoseHandler pose) {
        pose.setLooping(false);
    }
}
