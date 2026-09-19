package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountDataProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MountTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void cranks$tickNonHorseMount(CallbackInfo ci) {
        if ((Object) this instanceof MountDataProvider
                && !((Object) this instanceof AbstractHorseEntity)) {
            com.cranks.dungeons.mount.MountManager.tick((LivingEntity) (Object) this);
        }
    }
}
