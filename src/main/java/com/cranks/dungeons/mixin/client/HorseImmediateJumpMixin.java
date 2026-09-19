package com.cranks.dungeons.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.passive.HorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class HorseImmediateJumpMixin {
    @Unique private boolean cranks$horseJumpWasPressed;

    @Shadow protected abstract void startRidingJump();

    @Inject(method = "getMountJumpStrength", at = @At("HEAD"), cancellable = true)
    private void cranks$reportFullHorseJumpStrength(CallbackInfoReturnable<Float> cir) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getJumpingMount() instanceof HorseEntity) cir.setReturnValue(1.0f);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void cranks$jumpHorseOnKeyPress(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        JumpingMount mount = player.getJumpingMount();
        boolean isHorse = mount instanceof HorseEntity;
        boolean pressed = player.input.jumping;
        if (isHorse && pressed && !cranks$horseJumpWasPressed
                && ((HorseEntity) mount).isOnGround()) {
            mount.setJumpStrength(100);
            startRidingJump();
        }
        cranks$horseJumpWasPressed = isHorse && pressed;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/JumpingMount;setJumpStrength(I)V"))
    private void cranks$skipVanillaHorseJumpRelease(JumpingMount mount, int strength) {
        if (!(mount instanceof HorseEntity)) mount.setJumpStrength(strength);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;startRidingJump()V"))
    private void cranks$skipVanillaHorseJumpPacket(ClientPlayerEntity player) {
        if (!(player.getJumpingMount() instanceof HorseEntity)) startRidingJump();
    }
}
