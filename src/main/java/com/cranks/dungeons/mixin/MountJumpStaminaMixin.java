package com.cranks.dungeons.mixin;

import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Charges mount stamina on the authoritative server riding-jump command. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class MountJumpStaminaMixin {
    @Redirect(method = "onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/JumpingMount;startJumping(I)V"))
    private void cranks$chargeMountJump(JumpingMount mount, int height) {
        if (mount instanceof CamelEntity camel) {
            if (!MountManager.trySpendCamelDash(camel)) return;
        } else if (mount instanceof HorseEntity horse) {
            HorseBreedingManager.spendJumpStamina(horse);
        } else if (mount instanceof LivingEntity livingMount) {
            MountManager.spendJumpResource(livingMount);
        }

        mount.startJumping(height);
    }
}
