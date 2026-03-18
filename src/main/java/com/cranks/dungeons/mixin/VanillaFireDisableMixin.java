package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class VanillaFireDisableMixin {

    @Inject(method = "setOnFireFor", at = @At("HEAD"), cancellable = true)
    private void convertVanillaFireToCustomBurn(int seconds, CallbackInfo ci) {
        // We no longer apply custom BURN here to avoid environmental fire applying ailments.
        // Mobs and fireballs now have their own chance to apply BURN via DamageReductionMixin.
        ci.cancel();
    }

    @Inject(method = "setOnFireForTicks", at = @At("HEAD"), cancellable = true)
    private void convertVanillaFireTicksToCustomBurn(int ticks, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "setFireTicks", at = @At("HEAD"), cancellable = true)
    private void preventManualFireTicks(int ticks, CallbackInfo ci) {
        // We cancel all manual fire tick sets to prevent vanilla fire mechanics entirely
        ci.cancel();
    }
    
    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    private void hideFireOverlayIfHasBurnAilment(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living && AilmentManager.hasAilment(living, AilmentType.BURN)) {
            cir.setReturnValue(false);
        }
    }
}
