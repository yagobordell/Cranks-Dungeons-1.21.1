package com.cranks.dungeons.mixin;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.util.ClientAilmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerEntity.class) public class StunPlayerActionMixin {
    @Unique private boolean cranks$stunned() {
        PlayerEntity player=(PlayerEntity)(Object)this;
        return player.getWorld().isClient()?ClientAilmentHelper.isStunned(player):AilmentManager.isStunned(player);
    }
    @Inject(method="attack",at=@At("HEAD"),cancellable=true) private void stopStunnedAttack(Entity target,CallbackInfo ci) {
        if(cranks$stunned())ci.cancel();
    }
    @Inject(method="interact",at=@At("HEAD"),cancellable=true) private void stopStunnedInteraction(Entity target,Hand hand,CallbackInfoReturnable<ActionResult> cir) {
        if(cranks$stunned())cir.setReturnValue(ActionResult.FAIL);
    }
}
