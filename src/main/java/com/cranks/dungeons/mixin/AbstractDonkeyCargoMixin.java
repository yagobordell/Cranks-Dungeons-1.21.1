package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractDonkeyEntity.class)
public abstract class AbstractDonkeyCargoMixin {
    @Inject(method = "getInventoryColumns", at = @At("HEAD"), cancellable = true)
    private void cranks$geneticCargoColumns(CallbackInfoReturnable<Integer> cir) {
        AbstractDonkeyEntity self = (AbstractDonkeyEntity) (Object) this;
        if (self instanceof com.cranks.dungeons.mount.MountDataProvider
                && com.cranks.dungeons.mount.MountKind.of(self)
                != com.cranks.dungeons.mount.MountKind.UNSUPPORTED) {
            int capacity = MountManager.maxHorseCargoCapacity(self);
            cir.setReturnValue((capacity + 2) / 3);
        }
    }
}
