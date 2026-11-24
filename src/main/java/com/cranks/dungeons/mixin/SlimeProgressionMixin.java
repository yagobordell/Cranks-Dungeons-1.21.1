package com.cranks.dungeons.mixin;
import com.cranks.dungeons.progression.CombatEncounterAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(SlimeEntity.class)
public abstract class SlimeProgressionMixin {
    @WrapOperation(method="remove",at=@At(value="INVOKE",target="Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean splitOrigin(World world,Entity child,Operation<Boolean> original) {
        if(child instanceof CombatEncounterAccess access) access.cranks$encounter().origin=((CombatEncounterAccess)this).cranks$encounter().origin;
        return original.call(world,child);
    }
}
