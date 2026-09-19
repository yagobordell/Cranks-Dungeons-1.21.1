package com.cranks.dungeons.mixin;

import com.cranks.dungeons.horse.HorseBreedingManager;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseBreedingMixin {
    @Inject(method = "canBreedWith", at = @At("HEAD"), cancellable = true)
    private void cranks$validateOtherMountPair(net.minecraft.entity.passive.AnimalEntity other,
                                                CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof HorseEntity && other instanceof HorseEntity) return;
        if ((Object) this instanceof net.minecraft.entity.LivingEntity self
                && other instanceof com.cranks.dungeons.mount.MountDataProvider
                && !self.getEntityWorld().isClient()
                && !com.cranks.dungeons.mount.MountManager.canMate(self, other)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cranks$tickHorseProgression(CallbackInfo ci) {
        if ((Object) this instanceof HorseEntity horse) {
            HorseBreedingManager.tick(horse);
        } else if ((Object) this instanceof net.minecraft.entity.LivingEntity mount) {
            com.cranks.dungeons.mount.MountManager.tick(mount);
        }
    }

    @Inject(method = "setChildAttributes", at = @At("TAIL"))
    private void cranks$inheritHorseGenetics(PassiveEntity child, AbstractHorseEntity other,
                                             CallbackInfo ci) {
        if ((Object) this instanceof HorseEntity firstParent
                && other instanceof HorseEntity secondParent
                && child instanceof HorseEntity childHorse) {
            HorseBreedingManager.inherit(firstParent, secondParent, childHorse);
        } else if ((Object) this instanceof AbstractHorseEntity firstParent) {
            com.cranks.dungeons.mount.MountManager.inheritHorseFamily(firstParent, other, child);
        }
    }

    @Inject(method = "onInventoryChanged", at = @At("TAIL"))
    private void cranks$refreshMobilityAfterEquipmentChange(Inventory sender, CallbackInfo ci) {
        if ((Object) this instanceof HorseEntity horse) {
            var data = HorseBreedingManager.data(horse);
            if (data.initialized) data.modifiersDirty = true;
        }
    }
}
