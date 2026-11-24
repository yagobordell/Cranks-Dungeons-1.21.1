package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.RangedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
abstract class RangedFovMixin {
    @Inject(method="getFovMultiplier",at=@At("RETURN"),cancellable=true)
    private void cranks$crossbowZoom(CallbackInfoReturnable<Float> cir) {
        var client=MinecraftClient.getInstance();
        if((Object)this==client.player && RangedClient.aimingLoadedCrossbow(client))cir.setReturnValue(cir.getReturnValue()*0.80f);
    }
}
