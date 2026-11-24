package com.cranks.dungeons.mount;

import com.cranks.dungeons.network.MountWhistlePayload;
import com.cranks.dungeons.network.MountWhistleListPayload;
import com.cranks.dungeons.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import java.util.*;

public final class MountWhistleNetworking {
    private static final Map<ServerPlayerEntity,Integer> LAST_LIST=new WeakHashMap<>(),LAST_COMMAND=new WeakHashMap<>();
    private MountWhistleNetworking(){}
    public static void register(){ServerPlayNetworking.registerGlobalReceiver(MountWhistlePayload.ID,(p,c)->c.server().execute(()->handle(c.player(),p)));}
    public static MountWhistleListPayload nearby(ServerPlayerEntity player){
        var mounts=player.getServerWorld().getEntitiesByClass(MobEntity.class,player.getBoundingBox().expand(MountOrders.WHISTLE_RANGE),
                m->m.isAlive()&&player.getUuid().equals(MountOrders.owner(m))&&player.squaredDistanceTo(m)<=MountOrders.WHISTLE_RANGE*MountOrders.WHISTLE_RANGE);
        mounts.sort(Comparator.comparingDouble(player::squaredDistanceTo));
        return new MountWhistleListPayload(mounts.stream().limit(64).map(m->{String name=m.getName().getString();if(name.length()>64)name=name.substring(0,64);
            return new MountWhistleListPayload.Entry(m.getUuid(),m.getId(),name,MountKind.of(m).displayName(),(float)Math.sqrt(player.squaredDistanceTo(m)),MountOrders.state(m).mode.name());}).toList());
    }
    private static void handle(ServerPlayerEntity player,MountWhistlePayload payload){
        if(!player.isAlive()||player.isSpectator())return;
        if(payload.action()==-1){
            if(player.age-LAST_LIST.getOrDefault(player,-100)<10)return;LAST_LIST.put(player,player.age);
            ServerPlayNetworking.send(player,nearby(player));return;
        }
        if(player.age-LAST_COMMAND.getOrDefault(player,-100)<5)return;LAST_COMMAND.put(player,player.age);
        var modes=MountOrder.Mode.values();if(payload.action()<=0||payload.action()>=modes.length)return;
        var mode=modes[payload.action()];String error=MountOrders.command(player,payload.mount(),mode);
        if(error!=null){MountOrders.feedback(player,error);return;}
        MountOrders.feedback(player,switch(mode){case COME -> "Your mount is coming to this spot.";case FOLLOW -> "Your mount will follow you.";case STAY -> "Your mount will stay here.";default -> "";});
        playWhistle(player,mode);
    }

    /** Server broadcast includes the caller and other nearby players, with positional falloff. */
    public static void playWhistle(ServerPlayerEntity player,MountOrder.Mode mode){
        SoundEvent sound=switch(mode){
            case COME -> ModSounds.MOUNT_WHISTLE_COME;
            case FOLLOW -> ModSounds.MOUNT_WHISTLE_FOLLOW;
            case STAY -> ModSounds.MOUNT_WHISTLE_STAY;
            case NONE -> null;
        };
        if(sound!=null)player.getServerWorld().playSound(null,player.getX(),player.getEyeY(),player.getZ(),sound,SoundCategory.PLAYERS,.8f,1f);
    }
}
