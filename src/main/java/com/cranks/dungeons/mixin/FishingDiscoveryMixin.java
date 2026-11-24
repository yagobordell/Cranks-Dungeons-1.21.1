package com.cranks.dungeons.mixin;
import com.cranks.dungeons.progression.WorldDiscovery;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(FishingBobberEntity.class)
public abstract class FishingDiscoveryMixin {
    @WrapOperation(method="use",at=@At(value="INVOKE",target="Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean caught(World world,Entity entity,Operation<Boolean> original) {
        boolean spawned=original.call(world,entity);
        if(spawned && entity instanceof ItemEntity item && ((FishingBobberEntity)(Object)this).getPlayerOwner() instanceof ServerPlayerEntity player)
            WorldDiscovery.firstFish(player,item.getStack());
        return spawned;
    }
}
