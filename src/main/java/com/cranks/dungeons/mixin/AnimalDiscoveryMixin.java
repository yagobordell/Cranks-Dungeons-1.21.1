package com.cranks.dungeons.mixin;
import com.cranks.dungeons.progression.WorldDiscovery;
import net.minecraft.entity.passive.*;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AnimalEntity.class)
public abstract class AnimalDiscoveryMixin {
    @Unique private net.minecraft.server.network.ServerPlayerEntity cranks$firstBreeder, cranks$secondBreeder;
    @Inject(method="breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;Lnet/minecraft/entity/passive/PassiveEntity;)V",at=@At("HEAD"))
    private void parents(ServerWorld world,AnimalEntity mate,PassiveEntity child,CallbackInfo ci) {
        cranks$firstBreeder=((AnimalEntity)(Object)this).getLovingPlayer(); cranks$secondBreeder=mate.getLovingPlayer();
    }
    @Inject(method="breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;Lnet/minecraft/entity/passive/PassiveEntity;)V",at=@At("TAIL"))
    private void successfulBirth(ServerWorld world,AnimalEntity mate,PassiveEntity child,CallbackInfo ci) {
        if(child!=null && !child.isRemoved() && world.getEntity(child.getUuid())==child) {
            WorldDiscovery.firstAnimal(cranks$firstBreeder,child,"breed");
            WorldDiscovery.firstAnimal(cranks$secondBreeder,child,"breed");
        }
        cranks$firstBreeder=cranks$secondBreeder=null;
    }
}
