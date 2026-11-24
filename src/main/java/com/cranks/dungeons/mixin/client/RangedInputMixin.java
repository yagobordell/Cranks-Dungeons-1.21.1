package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.RangedClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
abstract class RangedInputMixin {
    @Shadow private boolean doAttack() { throw new AssertionError(); }
    @Inject(method="handleInputEvents",at=@At("HEAD"))
    private void cranks$rangedInput(CallbackInfo ci) { if(RangedClient.beforeInput((MinecraftClient)(Object)this))doAttack(); }
    @Inject(method="doAttack",at=@At("HEAD"),cancellable=true)
    private void cranks$fireInsteadOfMelee(CallbackInfoReturnable<Boolean> cir) {
        if(RangedClient.suppressAttack() || RangedClient.fire((MinecraftClient)(Object)this))cir.setReturnValue(false);
    }
    @Inject(method="handleBlockBreaking",at=@At("HEAD"),cancellable=true)
    private void cranks$preventMiningWhileFiring(boolean breaking,CallbackInfo ci) { if(RangedClient.suppressAttack())ci.cancel(); }
    @Inject(method="doItemUse",at=@At("HEAD"),cancellable=true)
    private void cranks$separateReleaseAndRedraw(CallbackInfo ci) { if(RangedClient.deferUse())ci.cancel(); }
}
