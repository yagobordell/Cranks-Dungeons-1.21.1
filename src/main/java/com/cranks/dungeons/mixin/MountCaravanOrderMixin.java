package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountOrder;
import com.cranks.dungeons.mount.MountOrders;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Automatic caravan joining must not replace an explicit whistle order. */
@Mixin(FormCaravanGoal.class)
public abstract class MountCaravanOrderMixin {
    @Shadow @Final public LlamaEntity llama;
    @Inject(method={"canStart","shouldContinue"},at=@At("HEAD"),cancellable=true)
    private void honorOwnerOrder(CallbackInfoReturnable<Boolean> cir){
        if(!llama.hasPassengers()&&!llama.hasVehicle()&&!llama.isLeashed()&&MountOrders.state(llama).mode!=MountOrder.Mode.NONE)cir.setReturnValue(false);
    }
}
