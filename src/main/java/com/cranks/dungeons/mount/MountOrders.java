package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

/** Whistles never load chunks, teleport animals, or authorize another owner's mount. */
public final class MountOrders {
    public static final double WHISTLE_RANGE=48,FOLLOW_RANGE=96;
    private MountOrders(){}
    public static UUID owner(LivingEntity mount){
        if(MountKind.of(mount)==MountKind.UNSUPPORTED)return null;
        if(MountManager.usesCustomOwnership(mount)&&mount instanceof MountDataProvider provider)return provider.cranksdungeons$getMountData().ownerUuid;
        return mount instanceof AbstractHorseEntity horse&&horse.isTame()?horse.getOwnerUuid():null;
    }
    public static MountOrder state(MobEntity mount){return ((MountOrderAccess)mount).cranks$getOrder();}
    public static String command(ServerPlayerEntity player,UUID id,MountOrder.Mode mode){
        if(mode==null||mode==MountOrder.Mode.NONE)return "Unknown mount command.";
        var entity=player.getServerWorld().getEntity(id);
        if(!(entity instanceof MobEntity mount)||MountKind.of(mount)==MountKind.UNSUPPORTED||!mount.isAlive()
                ||!player.getUuid().equals(owner(mount)))return "That mount is unavailable or does not belong to you.";
        if(!player.isAlive()||player.isSpectator()||player.squaredDistanceTo(mount)>WHISTLE_RANGE*WHISTLE_RANGE)return "That mount is too far away to hear your whistle.";
        if(mount.hasPassengers()||mount.hasVehicle())return "Dismount before giving this mount a command.";
        if(mount.isLeashed())return "Remove the mount's leash before giving it a command.";
        if(mount.isAiDisabled())return "This mount cannot respond to commands.";
        if(mode!=MountOrder.Mode.STAY&&!prepareMovement(mount))return "This camel needs more room to stand before it can follow.";
        Vec3d target=mode==MountOrder.Mode.STAY?mount.getPos():player.getPos();
        if(mode!=MountOrder.Mode.STAY&&mount.squaredDistanceTo(target)>6.25){
            var path=pathTo(mount,target);
            if(path==null||!path.reachesTarget())return "This mount cannot find a safe path to you. Move closer or clear the way.";
        }
        if(mount instanceof LlamaEntity llama&&llama.isFollowing())llama.stopFollowing();
        var order=state(mount);order.mode=mode;order.issuer=player.getUuid();order.destination=target;
        order.dimension=mount.getWorld().getRegistryKey().getValue().toString();order.revision++;
        return null;
    }
    public static void tick(MobEntity mount){
        var order=state(mount);if(order.mode==MountOrder.Mode.NONE)return;
        if(!java.util.Objects.equals(order.issuer,owner(mount))||!order.dimension.equals(mount.getWorld().getRegistryKey().getValue().toString())){
            com.cranks.dungeons.CranksDungeons.LOGGER.debug("Cleared obsolete mount order for {} after owner or dimension changed",mount.getUuid());
            order.clear();return;
        }
        // Riding and leashes take precedence. Parking then riding the animal
        // must not send it back to its old parking spot after dismounting.
        if(order.mode==MountOrder.Mode.STAY&&(mount.hasPassengers()||mount.isLeashed()||mount.hasVehicle()))order.destination=mount.getPos();
    }
    public static void feedback(ServerPlayerEntity player,String message){player.sendMessage(Text.literal(message),true);}
    public static net.minecraft.entity.ai.pathing.Path pathTo(MobEntity mount,Vec3d target){
        int distance=Math.min(104,Math.max(16,(int)Math.ceil(Math.sqrt(mount.squaredDistanceTo(target)))+8));
        return mount.getNavigation().findPathTo(net.minecraft.util.math.BlockPos.ofFloored(target),1,distance);
    }
    public static boolean prepareMovement(MobEntity mount){
        if(mount instanceof net.minecraft.entity.passive.CamelEntity camel&&camel.isSitting()){
            if(!camel.getWorld().isSpaceEmpty(camel,camel.getDimensions(net.minecraft.entity.EntityPose.STANDING).getBoxAt(camel.getPos())))return false;
            camel.startStanding();
        }
        return true;
    }
}
