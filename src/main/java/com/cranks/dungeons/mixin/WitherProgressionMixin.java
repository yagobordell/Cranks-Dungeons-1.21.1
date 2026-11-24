package com.cranks.dungeons.mixin;
import com.cranks.dungeons.progression.CombatEncounterAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(WitherSkullBlock.class)
public abstract class WitherProgressionMixin {
    @WrapOperation(method="onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private static boolean ritual(World world,Entity boss,Operation<Boolean> original) {
        ((CombatEncounterAccess)boss).cranks$encounter().origin=1;
        return original.call(world,boss);
    }
}
