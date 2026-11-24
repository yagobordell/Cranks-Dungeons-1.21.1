package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountBreedingAccess;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public abstract class MountBreedingPermissionMixin {
    @Inject(method="lovePlayer",at=@At("HEAD"),cancellable=true)
    private void protectBreeding(PlayerEntity player,CallbackInfo ci){
        var animal=(AnimalEntity)(Object)this;
        if(MountBreedingAccess.supported(animal)&&player!=null&&!MountBreedingAccess.allowed(player,animal))ci.cancel();
    }
    @Inject(method="breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;)V",at=@At("HEAD"),cancellable=true)
    private void recheckParents(ServerWorld world,AnimalEntity mate,CallbackInfo ci){
        var animal=(AnimalEntity)(Object)this;
        if(MountBreedingAccess.supported(animal)&&!MountBreedingAccess.authorizedPair(animal,mate))ci.cancel();
    }
}
