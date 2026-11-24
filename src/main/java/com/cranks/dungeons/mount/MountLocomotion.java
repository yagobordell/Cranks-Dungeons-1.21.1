package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;

/** Shared rider controls and collision-resolved terrain following. */
public final class MountLocomotion {
    private static final double STEP_DOWN=1.05, CAUTION_DROP=2.05, SEGMENT=.30;
    private record Profile(double acceleration,double brake,float idleTurn,float fastTurn){}
    private static final Profile HORSE=new Profile(.065,.13,12,6.5f);
    private static final Profile DONKEY=new Profile(.05,.14,8,2.4f);
    private static final Profile MULE=new Profile(.06,.15,8.5f,2.7f);
    private static final Profile CAMEL=new Profile(.045,.10,6.5f,1.9f);
    private static final Profile PIG=new Profile(.09,.16,10,3.8f);
    private static final Profile STRIDER=new Profile(.055,.12,8,2.6f);
    private MountLocomotion(){}

    public static boolean supported(LivingEntity mount){return MountKind.of(mount)!=MountKind.UNSUPPORTED;}
    public static MountMotionState state(LivingEntity mount){return ((MountMotionAccess)mount).cranksdungeons$motion();}
    private static Profile profile(LivingEntity mount){return switch(MountKind.of(mount)){
        case DONKEY -> DONKEY;case MULE -> MULE;case CAMEL -> CAMEL;case PIG -> PIG;case STRIDER -> STRIDER;default -> HORSE;
    };}
    private static MountMotionState riderState(LivingEntity mount,PlayerEntity rider){
        var state=state(mount);
        if(!rider.getUuid().equals(state.rider)){state.reset();state.rider=rider.getUuid();}
        return state;
    }
    public static void tick(LivingEntity mount){
        var state=state(mount);
        if(!(mount.getControllingPassenger() instanceof PlayerEntity rider)||!supported(mount)) {state.reset();return;}
        riderState(mount,rider);
        if(state.lastAge!=mount.age){state.lastAge=mount.age;if(state.jumpGrace>0)state.jumpGrace--;}
        if(mount.isOnGround()&&state.jumpGrace==0&&!(mount instanceof CamelEntity camel&&camel.isDashing()))state.jumping=false;
    }

    public static Vec3d input(LivingEntity mount,PlayerEntity rider,Vec3d vanilla){
        var state=riderState(mount,rider);var fit=profile(mount);
        double requested=Math.clamp(rider.forwardSpeed,-1,1);
        if(requested<0)requested*=.25;
        if(requested>0&&groundAssist(mount,state)){
            var heading=Vec3d.fromPolar(0,mount.getYaw());
            double speed=mount.getVelocity().horizontalLength();
            double look=Math.clamp(.65+speed*7,.8,3.5);
            double safe=walkableDistance(mount,footprint(mount.getBoundingBox()),heading.multiply(look));
            // Caution slows an approach; holding Forward still lets the rider commit
            // to the descent. It must not create an invisible wall at the edge.
            if(safe<look-.01)requested*=Math.clamp((safe-.25)/Math.max(.1,look-.25),.25,1);
        }
        return new Vec3d(0,vanilla.y,state.approach(requested,
                RidingStats.agility(mount,fit.acceleration),RidingStats.agility(mount,fit.brake)));
    }

    public static float yaw(LivingEntity mount,PlayerEntity rider){
        var state=riderState(mount,rider);var fit=profile(mount);
        double speed=mount.getVelocity().horizontalLength();
        double maximum=Math.max(.05,RidingStats.speed(mount,(float)mount.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
        // Throttle alone is not speed: holding Forward against an obstacle must
        // still allow a tight turn to get unstuck.
        float ratio=(float)Math.clamp(speed/maximum,0,1);
        float rate=mount.isOnGround()?MathHelper.lerp(ratio,fit.idleTurn,fit.fastTurn):.8f;
        float target=-MathHelper.clamp(rider.sidewaysSpeed,-1,1)*rate;
        float response=fit==HORSE?.45f:.30f;
        state.turn+=(target-state.turn)*(Math.abs(target)>.001f?response:.55f);
        if(Math.abs(state.turn)<.01f)state.turn=0;
        return mount.getYaw()+state.turn;
    }

    public static void beginJump(LivingEntity mount){
        if(!(mount.getControllingPassenger() instanceof PlayerEntity rider))return;
        var state=riderState(mount,rider);state.jumping=true;state.jumpGrace=8;
    }
    public static void capJumpImpulse(LivingEntity mount){
        if(!state(mount).jumping)return;
        var v=mount.getVelocity();double speed=v.horizontalLength();
        double limit=Math.max(.05,RidingStats.speed(mount,(float)mount.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
        if(speed>limit)mount.setVelocity(v.x*limit/speed,v.y,v.z*limit/speed);
    }

    private static boolean groundAssist(LivingEntity mount,MountMotionState state){
        return mount.isOnGround()&&!state.jumping&&!mount.isTouchingWater()&&!mount.isInLava()
                &&!mount.hasNoGravity()&&!mount.isClimbing()&&!mount.isFallFlying()&&mount.getVelocity().y<=.001;
    }

    /** Called only at vanilla travel's actual move sites, on the logical movement side. */
    public static void move(LivingEntity mount,MovementType type,Vec3d movement){
        if(type!=MovementType.SELF||!supported(mount)||!(mount.getControllingPassenger() instanceof PlayerEntity)
                ||!groundAssist(mount,state(mount))||movement.y>.001||movement.horizontalLength()<.0001){
            mount.move(type,movement);return;
        }
        int count=Math.min(16,Math.max(1,(int)Math.ceil(movement.horizontalLength()/SEGMENT)));
        var step=movement.multiply(1.0/count);
        boolean hitWall=false,hitVertical=false;
        for(int i=0;i<count;i++){
            boolean grounded=mount.isOnGround();
            var horizontal=new Vec3d(step.x,0,step.z);
            mount.move(type,new Vec3d(horizontal.x,step.y,horizontal.z));
            hitWall|=mount.horizontalCollision;hitVertical|=mount.verticalCollision;
            if(grounded&&!mount.isOnGround()&&mount.getVelocity().y<=.001){
                double below=drop(mount,mount.getBoundingBox(),STEP_DOWN);
                if(!Double.isNaN(below)&&below>.00001){
                    mount.move(type,new Vec3d(0,-below-.001,0));
                    var v=mount.getVelocity();mount.setVelocity(v.x,0,v.z);
                    hitVertical|=mount.verticalCollision;
                }
            }
        }
        mount.horizontalCollision|=hitWall;mount.verticalCollision|=hitVertical;
    }

    private static Box footprint(Box box){
        double inset=Math.min(.18,(box.maxX-box.minX)*.2);
        return new Box(box.minX+inset,box.minY+.0001,box.minZ+inset,box.maxX-inset,box.minY+.05,box.maxZ-inset);
    }

    /** Bounded collision-shape scan; follows descending steps rather than probing a flat horizon. */
    private static double walkableDistance(LivingEntity mount,Box start,Vec3d horizontal){
        double distance=horizontal.horizontalLength();if(distance<.00001)return distance;
        int checks=Math.min(16,Math.max(1,(int)Math.ceil(distance/SEGMENT)));
        Vec3d step=horizontal.multiply(1.0/checks);Box feet=start;
        for(int i=1;i<=checks;i++){
            feet=feet.offset(step);
            if(fluid(mount,feet))return distance;
            // An ascending step/wall is left to vanilla collision and step-up handling.
            if(!mount.getWorld().isSpaceEmpty(mount,feet))return distance;
            // Looking two blocks down permits ordinary ledges. Only the movement
            // helper's separate one-block scan may pull the mount onto the ground;
            // larger descents retain native gravity and fall-distance accounting.
            double below=drop(mount,feet,CAUTION_DROP);
            if(Double.isNaN(below))return distance*(i-1)/checks;
            feet=feet.offset(0,-below,0);
        }
        return distance;
    }

    private static boolean fluid(LivingEntity mount,Box feet){
        return !mount.getWorld().getFluidState(BlockPos.ofFloored((feet.minX+feet.maxX)*.5,feet.minY-.05,(feet.minZ+feet.maxZ)*.5)).isEmpty();
    }

    private static double drop(LivingEntity mount,Box box,double maximum){
        double allowed=VoxelShapes.calculateMaxOffset(Direction.Axis.Y,box,
                mount.getWorld().getBlockCollisions(mount,box.stretch(0,-maximum,0)),-maximum);
        return allowed>-maximum+.000001?Math.max(0,-allowed):Double.NaN;
    }
}
