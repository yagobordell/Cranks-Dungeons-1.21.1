package com.cranks.dungeons.mount;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/** Foliage can brush past a mount's body without becoming an overhead ceiling. */
public final class MountFoliageCollision {
    private MountFoliageCollision(){}

    public static boolean canPass(Entity actor,BlockPos leaves){
        Entity subject=actor instanceof PlayerEntity&&actor.getVehicle()!=null?actor.getVehicle():actor;
        if(!(subject instanceof LivingEntity mount)||MountKind.of(mount)==MountKind.UNSUPPORTED)return false;
        boolean domestic=mount instanceof AbstractHorseEntity horse&&horse.isTame()
                ||mount instanceof Saddleable saddle&&saddle.isSaddled()
                ||mount.getPassengerList().stream().anyMatch(PlayerEntity.class::isInstance);
        // Use the mount's feet for its rider too. Retain support when already
        // above a leaf block, so this does not make treetop footing disappear.
        return domestic&&mount.getBoundingBox().minY<leaves.getY()+1.0-.0001;
    }
}
