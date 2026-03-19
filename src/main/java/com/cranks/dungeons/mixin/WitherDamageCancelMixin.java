package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class WitherDamageCancelMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelWitherDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Cancel wither damage - we'll handle it through resistance reduction instead
        if (source.isOf(DamageTypes.WITHER)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
