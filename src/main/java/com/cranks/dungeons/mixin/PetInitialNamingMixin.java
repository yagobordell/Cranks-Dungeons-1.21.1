package com.cranks.dungeons.mixin;

import com.cranks.dungeons.name.NameTagManager;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameableEntity.class)
public abstract class PetInitialNamingMixin {
    @Unique private boolean cranks$alreadyTamed;
    @Inject(method="setOwner",at=@At("HEAD"))
    private void beforeTame(PlayerEntity player,CallbackInfo ci){cranks$alreadyTamed=((TameableEntity)(Object)this).isTamed();}
    @Inject(method="setOwner",at=@At("TAIL"))
    private void afterTame(PlayerEntity player,CallbackInfo ci){
        if(!cranks$alreadyTamed&&player instanceof ServerPlayerEntity owner)
            NameTagManager.offerFirstName(owner,(TameableEntity)(Object)this);
    }
}
