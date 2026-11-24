package com.cranks.dungeons.mount;

import com.cranks.dungeons.horse.HorseBreedingManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

/** Breeding grants are independent of riding, cargo and ordinary feeding. */
public final class MountBreedingAccess {
    private MountBreedingAccess(){}
    public static boolean supported(AnimalEntity animal){
        var kind=MountKind.of(animal);return kind!=MountKind.UNSUPPORTED&&kind!=MountKind.PIG&&kind!=MountKind.MULE&&kind!=MountKind.SKELETON_HORSE;
    }
    public static MountAccessMode mode(AnimalEntity animal){
        return animal instanceof HorseEntity horse?HorseBreedingManager.ensureInitialized(horse).breedingAccessMode:MountManager.ensureInitialized(animal).breedingAccessMode;
    }
    public static boolean allowed(PlayerEntity player,AnimalEntity animal){
        return MountManager.canAccess(player,animal,mode(animal));
    }
    public static boolean authorizedPair(AnimalEntity first,AnimalEntity second){
        return authorizedParent(first)&&authorizedParent(second);
    }
    private static boolean authorizedParent(AnimalEntity animal){
        if(!supported(animal))return true;
        var player=animal.getLovingPlayer();
        return player!=null?allowed(player,animal):MountOwnership.from(animal).ownerUuid()==null;
    }
    public static void register(){
        UseEntityCallback.EVENT.register((player,world,hand,target,hit)->{
            if(world.isClient()||!(target instanceof AnimalEntity animal)||!supported(animal)
                    ||animal.isBaby()||!animal.isBreedingItem(player.getStackInHand(hand))||allowed(player,animal))return ActionResult.PASS;
            player.sendMessage(Text.literal("You do not have permission to breed this mount."),true);
            // Injured animals may still receive their normal healing food. The
            // lovePlayer guard prevents that feeding from starting breeding.
            return animal.getHealth()<animal.getMaxHealth()?ActionResult.PASS:ActionResult.FAIL;
        });
    }
}
