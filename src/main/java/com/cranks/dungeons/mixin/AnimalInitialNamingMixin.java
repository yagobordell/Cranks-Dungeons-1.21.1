package com.cranks.dungeons.mixin;

import com.cranks.dungeons.name.InitialNamingAccess;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public abstract class AnimalInitialNamingMixin implements InitialNamingAccess {
    @Unique private boolean cranks$namingOffered;
    public boolean cranks$wasNamingOffered(){return cranks$namingOffered;}
    public void cranks$setNamingOffered(boolean offered){cranks$namingOffered=offered;}
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void saveNaming(NbtCompound tag,CallbackInfo ci){if(cranks$namingOffered)tag.putBoolean("CranksInitialNamingOffered",true);}
    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void loadNaming(NbtCompound tag,CallbackInfo ci){cranks$namingOffered=tag.getBoolean("CranksInitialNamingOffered");}
}
