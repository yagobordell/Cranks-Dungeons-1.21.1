package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class StunBlockActionMixin {
    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method="tryBreakBlock",at=@At("HEAD"),cancellable=true)
    private void preventStunnedBreak(CallbackInfoReturnable<Boolean> cir) {
        if(AilmentManager.isStunned(player))cir.setReturnValue(false);
    }

    @Inject(method={"interactItem","interactBlock"},at=@At("HEAD"),cancellable=true)
    private void preventStunnedUse(CallbackInfoReturnable<ActionResult> cir) {
        if(AilmentManager.isStunned(player))cir.setReturnValue(ActionResult.FAIL);
    }
}
