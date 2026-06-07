package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldFreezeMixin {

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void cancelEntityTick(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            boolean isStunned = living.hasStatusEffect(ModEffects.FREEZE) || living.hasStatusEffect(ModEffects.ELECTROCUTE);
            if (isStunned) {
                ci.cancel();
            }
        }
    }
}
