package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldFreezeMixin {

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void cancelEntityTick(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            boolean isStunned = living.hasStatusEffect(ModEffects.FREEZE) || living.hasStatusEffect(ModEffects.ELECTROCUTE);
            if (isStunned) {
                // Manually tick ailments so the freeze duration decays
                com.cranks.dungeons.ailment.AilmentManager.tickAilments(living);
                ci.cancel();
            }
        }
    }
}
