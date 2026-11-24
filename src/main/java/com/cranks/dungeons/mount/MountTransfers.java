package com.cranks.dungeons.mount;

import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.network.MountTransferOfferPayload;
import com.cranks.dungeons.network.MountTransferPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import java.util.*;

/** Short-lived, recipient-bound consent; no inventory or progression is recreated. */
public final class MountTransfers {
    private record Offer(UUID token,UUID owner,UUID mount,ServerWorld world,long expires){}
    private static final Map<UUID,Offer> OFFERS=new HashMap<>();
    private MountTransfers(){}
    public static void register(){
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->OFFERS.entrySet().removeIf(e->e.getKey().equals(handler.player.getUuid())||e.getValue().owner.equals(handler.player.getUuid())));
    }
    private static boolean valid(ServerPlayerEntity owner,ServerPlayerEntity recipient,LivingEntity mount){
        return owner!=null&&recipient!=null&&mount!=null&&owner!=recipient&&owner.isAlive()&&recipient.isAlive()&&mount.isAlive()
                &&owner.getWorld()==mount.getWorld()&&recipient.getWorld()==mount.getWorld()
                &&owner.squaredDistanceTo(mount)<=64&&recipient.squaredDistanceTo(mount)<=64&&MountManager.isOwner(owner,mount);
    }
    public static boolean offer(ServerPlayerEntity owner,LivingEntity mount,ServerPlayerEntity recipient){
        if(!valid(owner,recipient,mount))return false;
        OFFERS.entrySet().removeIf(e->e.getValue().expires<e.getValue().world.getTime());
        if(OFFERS.containsKey(recipient.getUuid())){owner.sendMessage(Text.literal("That player already has a pending mount offer."),true);return false;}
        var offer=new Offer(UUID.randomUUID(),owner.getUuid(),mount.getUuid(),owner.getServerWorld(),owner.getServerWorld().getTime()+600);
        OFFERS.put(recipient.getUuid(),offer);
        String name=mount.getName().getString();if(name.length()>64)name=name.substring(0,64);
        ServerPlayNetworking.send(recipient,new MountTransferOfferPayload(mount.getId(),offer.token,owner.getName().getString(),name));
        owner.sendMessage(Text.literal("Ownership offer sent to "+recipient.getName().getString()+". It expires in 30 seconds."),true);return true;
    }
    public static boolean reply(ServerPlayerEntity recipient,MountTransferPayload payload){
        var offer=OFFERS.get(recipient.getUuid());
        if(offer==null||!offer.token.equals(payload.target()))return false;
        OFFERS.remove(recipient.getUuid());
        var owner=recipient.getServer().getPlayerManager().getPlayer(offer.owner);
        if(payload.action()==MountTransferPayload.DECLINE){if(owner!=null)owner.sendMessage(Text.literal("Mount ownership offer declined."),true);return false;}
        var entity=offer.world.getEntity(offer.mount);
        if(payload.action()!=MountTransferPayload.ACCEPT||offer.expires<offer.world.getTime()
                ||!(entity instanceof LivingEntity mount)||mount.getId()!=payload.entityId()||!valid(owner,recipient,mount)){
            recipient.sendMessage(Text.literal("This ownership offer is no longer valid. Stay near the owner and mount."),true);return false;
        }
        transfer(mount,recipient);
        owner.sendMessage(Text.literal("You transferred "+mount.getName().getString()+" to "+recipient.getName().getString()+"."),false);
        recipient.sendMessage(Text.literal("You now own "+mount.getName().getString()+"."),false);return true;
    }
    private static void transfer(LivingEntity mount,ServerPlayerEntity recipient){
        if(MountManager.usesCustomOwnership(mount)){var d=MountManager.ensureInitialized(mount);d.ownerUuid=recipient.getUuid();d.ownerName=recipient.getName().getString();}
        else if(mount instanceof AbstractHorseEntity horse){horse.setOwnerUuid(recipient.getUuid());horse.setTame(true);}
        if(mount instanceof HorseEntity horse){
            var d=HorseBreedingManager.ensureInitialized(horse);d.accessMode=d.storageAccessMode=d.breedingAccessMode=MountAccessMode.OWNER_ONLY;
            d.gallopRequested=d.galloping=false;d.modifiersDirty=true;HorseBreedingManager.sync(horse);HorseBreedingManager.syncToPlayer(recipient,horse);
        }else{
            var d=MountManager.ensureInitialized(mount);d.accessMode=d.storageAccessMode=d.breedingAccessMode=MountAccessMode.OWNER_ONLY;
            d.abilityRequested=d.abilityActive=false;MountManager.sync(mount);MountManager.syncToPlayer(recipient,mount);
        }
        MountManager.enforceRidingAccess(mount);LlamaPackManager.invalidate();
    }
}
