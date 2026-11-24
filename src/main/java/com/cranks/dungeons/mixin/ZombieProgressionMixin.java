package com.cranks.dungeons.mixin;
import com.cranks.dungeons.progression.CombatEncounterAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ZombieEntity.class)
public abstract class ZombieProgressionMixin {
    @WrapOperation(method="damage",at=@At(value="INVOKE",target="Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"))
    private void reinforcement(ServerWorld world,Entity child,Operation<Void> original) {
        if(child instanceof CombatEncounterAccess access) access.cranks$encounter().origin=((CombatEncounterAccess)this).cranks$encounter().origin;
        original.call(world,child);
    }
}
