package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MountAccessRidingMixin {
    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
    private void cranks$ejectUnauthorizedRider(Entity vehicle, boolean force,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !((Object) this instanceof ServerPlayerEntity player)
                || !(vehicle instanceof LivingEntity mount)
                || MountManager.canAccessMount(player, mount)) {
            return;
        }
        player.stopRiding();
        MountManager.denyMountAccess(player);
    }
}
