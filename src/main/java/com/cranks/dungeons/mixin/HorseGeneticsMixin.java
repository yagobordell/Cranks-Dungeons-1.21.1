package com.cranks.dungeons.mixin;

import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.horse.HorseGeneticsData;
import com.cranks.dungeons.horse.HorseGeneticsProvider;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseEntity.class)
public abstract class HorseGeneticsMixin implements HorseGeneticsProvider {
    @Unique
    private HorseGeneticsData cranks$horseGenetics = new HorseGeneticsData();

    @Override
    public HorseGeneticsData cranksdungeons$getHorseGenetics() {
        return cranks$horseGenetics;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void cranks$writeGenetics(NbtCompound nbt, CallbackInfo ci) {
        if (cranks$horseGenetics.initialized) {
            nbt.put("CranksHorseGenetics", cranks$horseGenetics.toNbt());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void cranks$readGenetics(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("CranksHorseGenetics")) {
            cranks$horseGenetics.readNbt(nbt.getCompound("CranksHorseGenetics"));
        }
    }

    @Inject(method = "canBreedWith", at = @At("HEAD"), cancellable = true)
    private void cranks$validateGeneticPair(AnimalEntity other, CallbackInfoReturnable<Boolean> cir) {
        HorseEntity self = (HorseEntity) (Object) this;
        if (!self.getEntityWorld().isClient()
                && other instanceof HorseEntity otherHorse
                && !HorseBreedingManager.canMate(self, otherHorse)) {
            cir.setReturnValue(false);
        } else if (!self.getEntityWorld().isClient()
                && other instanceof net.minecraft.entity.passive.DonkeyEntity donkey
                && !com.cranks.dungeons.mount.MountManager.canMate(self, donkey)) {
            cir.setReturnValue(false);
        }
    }
}
