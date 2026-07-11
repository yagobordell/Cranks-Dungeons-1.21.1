package com.cranks.dungeons.compat.punchy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.state.CrossbowStateMachine;

@Mixin(value = CrossbowStateMachine.class, remap = false)
public class PunchyCrossbowStateMachineMixin {
    
    @Inject(method = "playCrossbow", at = @At("TAIL"))
    private void cranks$preventLooping(MinecraftClient client, Hand hand, CallbackInfo ci) {
        // Prevent Crossbow from looping awkwardly when charge time is longer than animation length
        if (PunchyAnimationManager.POSE_HANDLER != null) {
            PunchyAnimationManager.POSE_HANDLER.setLooping(false);
        }
    }
}
