package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class VanillaFireDisableMixin {

    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    private void makeBurnAilmentRenderOnFire(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity living) {
            if (AilmentManager.hasAilment(living, AilmentType.BURN)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "setOnFireForTicks", at = @At("HEAD"), cancellable = true)
    private void interceptSetOnFireForTicks(int ticks, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity living && !living.getWorld().isClient()) {
            float seconds = Math.max(1.0f, ticks / 20.0f);
            float baseDps = 3.0f;

            if (living.isInLava()) {
                baseDps = 8.0f;
                seconds = 4.0f;
            } else {
                BlockState state = living.getWorld().getBlockState(living.getBlockPos());
                if (state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE) 
                        || state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.SOUL_CAMPFIRE)) {
                    seconds = 1.0f;
                    baseDps = 3.0f;
                } else {
                    // Sunlight or other environmental flame
                    seconds = Math.min(seconds, 1.0f);
                    baseDps = 2.0f;
                }
            }

            AilmentManager.applyAilment(
                living,
                AilmentType.BURN,
                baseDps,
                seconds,
                null,
                null,
                null,
                0
            );
            ci.cancel();
        }
    }
}
