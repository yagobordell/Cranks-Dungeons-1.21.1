package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.util.ClientAilmentHelper;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClientTickFreezeMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void freezeClientTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient() && ClientAilmentHelper.isStunned(entity)) {
            // ClientWorld.tickEntity() increments entity.age just before calling entity.tick().
            // By decrementing it here, we completely freeze the entity's age,
            // which perfectly halts the renderer's breathing animation (state.age).
            entity.age--;
            // We do not need to tick Ailments here because AilmentManager.tickAilments only works on the server
            ci.cancel();
        }
    }
}
