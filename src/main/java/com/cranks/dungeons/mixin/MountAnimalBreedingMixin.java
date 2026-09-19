package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalEntity.class)
public abstract class MountAnimalBreedingMixin {
    @Inject(method = "canBreedWith", at = @At("HEAD"), cancellable = true)
    private void cranks$validateMountPair(AnimalEntity other, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof net.minecraft.entity.LivingEntity self)
                || !(self instanceof MountDataProvider) || !(other instanceof MountDataProvider)
                || MountKind.of(self) == MountKind.PIG) return;
        if (!self.getEntityWorld().isClient() && !MountManager.canMate(self, other)) {
            cir.setReturnValue(false);
        }
    }
}
