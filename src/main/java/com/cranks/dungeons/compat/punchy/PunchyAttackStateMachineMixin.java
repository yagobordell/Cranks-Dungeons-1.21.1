package com.cranks.dungeons.compat.punchy;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.state.AttackStateMachine;

@Mixin(value = AttackStateMachine.class, remap = false)
public class PunchyAttackStateMachineMixin {

    /**
     * Prevents Punchy from applying an artificial visual throttle (delay) 
     * to attack animations. This ensures that when a player attacks, the 
     * animation starts instantly, respecting the game's actual attack speed 
     * cooldown instead of Punchy's visual 3.5 CPS throttle.
     */
    @Inject(method = "isAttackAnimationCoolingDown", at = @At("HEAD"), cancellable = true)
    private void removeVisualThrottle(MinecraftClient client, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    /**
     * Forces attacks to skip blend-in. By default, Punchy blends in the 
     * attack animation from idle if you haven't attacked recently. With very 
     * low attack speed, every attack triggers this blend-in, causing a 
     * perceived visual delay before the strike lands.
     */
    @Inject(method = "shouldSkipBlendIn", at = @At("HEAD"), cancellable = true)
    private void forceInstantAttackBlend(MinecraftClient client, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
