package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.MountClientData;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.network.MountDataSyncPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CamelEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes JumpingMounts activate immediately, with a tuned strength for camel dashes. */
@Mixin(ClientPlayerEntity.class)
public abstract class MountImmediateJumpMixin {
    @Unique private static final int CRANKS_CAMEL_DASH_STRENGTH = 15;
    @Unique private boolean cranks$mountJumpWasPressed;

    @Shadow protected abstract void startRidingJump();

    @Inject(method = "getMountJumpStrength", at = @At("HEAD"), cancellable = true)
    private void cranks$reportImmediateMountJumpStrength(CallbackInfoReturnable<Float> cir) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        JumpingMount mount = player.getJumpingMount();
        if (mount instanceof CamelEntity) {
            cir.setReturnValue(CRANKS_CAMEL_DASH_STRENGTH / 100.0f);
        } else if (mount != null) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void cranks$jumpMountOnKeyPress(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        JumpingMount mount = player.getJumpingMount();
        boolean pressed = player.input.jumping;
        boolean onGround = mount instanceof LivingEntity livingMount && livingMount.isOnGround();
        boolean canAffordCamelDash = !(mount instanceof CamelEntity camel)
                || cranks$canAffordCamelDash(camel);
        if (mount != null && pressed && !cranks$mountJumpWasPressed && onGround && canAffordCamelDash) {
            mount.setJumpStrength(mount instanceof CamelEntity
                    ? CRANKS_CAMEL_DASH_STRENGTH : 100);
            startRidingJump();
        }
        cranks$mountJumpWasPressed = mount != null && pressed;
    }

    @Unique
    private boolean cranks$canAffordCamelDash(CamelEntity camel) {
        MountDataSyncPacket data = MountClientData.get(camel.getId());
        if (data == null || !"CAMEL".equals(data.kind())) return true;
        double cost = MountManager.camelDashCost(
                data.maximumResource(), data.utility(), data.trait());
        return data.resource() + 1.0e-6 >= cost;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/JumpingMount;setJumpStrength(I)V"))
    private void cranks$skipVanillaMountJumpRelease(JumpingMount mount, int strength) {
        // The key-press handler sets full strength directly; ignore vanilla charge updates.
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;startRidingJump()V"))
    private void cranks$skipVanillaMountJumpPacket(ClientPlayerEntity player) {
        if (player.getJumpingMount() == null) startRidingJump();
    }
}
