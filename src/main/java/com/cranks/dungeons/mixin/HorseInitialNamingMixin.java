package com.cranks.dungeons.mixin;

import com.cranks.dungeons.name.NameTagManager;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class HorseInitialNamingMixin {
    @Unique private boolean cranks$alreadyTamed;
    @Inject(method="bondWithPlayer",at=@At("HEAD"))
    private void beforeTame(PlayerEntity player,CallbackInfoReturnable<Boolean> cir){cranks$alreadyTamed=((AbstractHorseEntity)(Object)this).isTame();}
    @Inject(method="bondWithPlayer",at=@At("RETURN"))
    private void afterTame(PlayerEntity player,CallbackInfoReturnable<Boolean> cir){
        if(!cranks$alreadyTamed&&cir.getReturnValueZ()&&player instanceof ServerPlayerEntity owner)
            NameTagManager.offerFirstName(owner,(AbstractHorseEntity)(Object)this);
    }
}
