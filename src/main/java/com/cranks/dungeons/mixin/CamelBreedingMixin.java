package com.cranks.dungeons.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CamelEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CamelEntity.class)
public abstract class CamelBreedingMixin {
    @Inject(method = "canBreedWith", at = @At("HEAD"), cancellable = true)
    private void cranks$validateCamelPair(AnimalEntity other, CallbackInfoReturnable<Boolean> cir) {
        CamelEntity self = (CamelEntity) (Object) this;
        if (!self.getEntityWorld().isClient() && other instanceof CamelEntity camel
                && !com.cranks.dungeons.mount.MountManager.canMate(self, camel)) {
            cir.setReturnValue(false);
        }
    }
}
