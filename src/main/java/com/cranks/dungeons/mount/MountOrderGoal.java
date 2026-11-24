package com.cranks.dungeons.mount;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

/** Higher priority than wandering, lower than swimming, panic and breeding. */
public final class MountOrderGoal extends Goal {
    private final MobEntity mount;
    private int retry,failed;
    private boolean warned;
    private long revision=-1;
    public MountOrderGoal(MobEntity mount){this.mount=mount;setControls(EnumSet.of(Control.MOVE,Control.LOOK));}
    @Override public boolean canStart(){return MountOrders.state(mount).mode!=MountOrder.Mode.NONE&&!mount.hasPassengers()&&!mount.hasVehicle()&&!mount.isLeashed()&&!mount.isAiDisabled();}
    @Override public boolean shouldContinue(){return canStart();}
    @Override public boolean shouldRunEveryTick(){return true;}
    @Override public void start(){MountOrders.state(mount).running=true;retry=failed=0;warned=false;}
    @Override public void stop(){mount.getNavigation().stop();MountOrders.state(mount).running=false;}
    @Override public void tick(){
        var order=MountOrders.state(mount);
        if(revision!=order.revision){revision=order.revision;retry=failed=0;warned=false;mount.getNavigation().stop();}
        ServerPlayerEntity owner=mount.getServer().getPlayerManager().getPlayer(order.issuer);
        Vec3d target=order.destination;
        if(order.mode==MountOrder.Mode.FOLLOW){
            if(owner==null||!owner.isAlive()||owner.getWorld()!=mount.getWorld()||owner.squaredDistanceTo(mount)>MountOrders.FOLLOW_RANGE*MountOrders.FOLLOW_RANGE){mount.getNavigation().stop();return;}
            target=owner.getPos();
        }
        if(order.mode!=MountOrder.Mode.STAY&&!MountOrders.prepareMovement(mount)){mount.getNavigation().stop();return;}
        double stopDistance=order.mode==MountOrder.Mode.STAY?1:6.25;
        if(mount.squaredDistanceTo(target)<=stopDistance){
            mount.getNavigation().stop();retry=failed=0;warned=false;
            if(order.mode==MountOrder.Mode.COME){order.mode=MountOrder.Mode.STAY;order.destination=mount.getPos();order.revision++;}
            return;
        }
        if(--retry>0)return;retry=20;
        if(!mount.getWorld().isChunkLoaded(net.minecraft.util.math.BlockPos.ofFloored(target))){mount.getNavigation().stop();return;}
        var path=MountOrders.pathTo(mount,target);
        if(path!=null&&path.reachesTarget()&&mount.getNavigation().startMovingAlong(path,1.1)){failed=0;warned=false;return;}
        mount.getNavigation().stop();
        if(++failed>=5&&!warned){warned=true;if(owner!=null)MountOrders.feedback(owner,mount.getName().getString()+" cannot reach its destination. Clear a path or whistle from closer by.");}
    }
}
