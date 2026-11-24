package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.WeakHashMap;

/** Activity eligibility, separate from the mount's physical displacement. */
public final class MountTraining {
    private static final Map<PlayerEntity,LeaderTravel> LEADERS=new WeakHashMap<>();
    private static final class LeaderTravel {
        Vec3d position;
        Object dimension;
        long tick=Long.MIN_VALUE;
        boolean moving;
    }
    private MountTraining(){}
    public static boolean activeTravel(LivingEntity mount,double distance){
        if(mount.isBaby()||mount.hasVehicle()||!Double.isFinite(distance)||distance<.001
                ||distance>Math.max(.7,mount.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)*2+.25))return false;
        if(mount.getControllingPassenger() instanceof PlayerEntity rider)return Math.abs(rider.forwardSpeed)>.01;
        if(mount instanceof LlamaEntity llama&&llama.isTame()){
            var leader=llama;
            for(int i=0;i<16;i++){
                if(leader.getLeashHolder() instanceof PlayerEntity player)
                    return !player.hasVehicle()&&leader.squaredDistanceTo(player)<256&&leaderMoving(player);
                if(!leader.isFollowing()||leader.getFollowing()==null)return false;
                leader=leader.getFollowing();
            }
        }
        return false;
    }

    private static boolean leaderMoving(PlayerEntity player){
        // Server player velocity is not a reliable record of client-driven
        // walking. Sample actual positions once per world tick for the whole caravan.
        var state=LEADERS.computeIfAbsent(player,ignored->new LeaderTravel());long now=player.getWorld().getTime();
        if(state.tick!=now){
            double distance=state.position==null?0:player.getPos().subtract(state.position).horizontalLength();
            state.moving=state.dimension==player.getWorld().getRegistryKey()&&now-state.tick==1&&distance>.001&&distance<3;
            state.tick=now;state.dimension=player.getWorld().getRegistryKey();state.position=player.getPos();
        }
        return state.moving;
    }
}
