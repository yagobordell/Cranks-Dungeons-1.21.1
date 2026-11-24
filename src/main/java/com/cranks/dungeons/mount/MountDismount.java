package com.cranks.dungeons.mount;

import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

/** Small local rescue search, with normal on-foot collision rather than mounted leaf phasing. */
public final class MountDismount {
    private record Landing(Vec3d position,EntityPose pose,double score){}
    private MountDismount(){}
    public static Vec3d choose(Entity mount,LivingEntity passenger,Vec3d vanilla){
        var pose=passenger.getPose()==EntityPose.CROUCHING?EntityPose.CROUCHING:EntityPose.STANDING;
        if(safe(passenger,vanilla,pose,false)&&vanilla.y>=mount.getY()-1.1&&reachable(mount,vanilla))return vanilla;
        Landing best=null;var base=mount.getBlockPos();var world=mount.getWorld();
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=-1;y<=1;y++){
            var block=base.add(x,y,z);if(!world.isChunkLoaded(block))continue;
            double height=world.getDismountHeight(Dismounting.getCollisionShape(world,block),()->Dismounting.getCollisionShape(world,block.down()));
            if(!Dismounting.canDismountInBlock(height))continue;
            Vec3d position=Vec3d.ofBottomCenter(block).add(0,height,0);
            if(position.y<mount.getY()-1.1||position.y>mount.getY()+1.25||!reachable(mount,position))continue;
            for(var candidatePose:new EntityPose[]{EntityPose.STANDING,EntityPose.CROUCHING})if(safe(passenger,position,candidatePose,false)){
                double score=position.squaredDistanceTo(vanilla)+(candidatePose==EntityPose.CROUCHING?2:0)+Math.max(0,mount.getY()-position.y)*2;
                if(best==null||score<best.score)best=new Landing(position,candidatePose,score);break;
            }
        }
        if(best!=null){passenger.setPose(best.pose);return best.position;}
        // Water can be a reasonable last resort, but do not put the rider's
        // head underwater or treat lava as an alternative to solid footing.
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=-1;y<=1;y++){
            var block=base.add(x,y,z);if(!world.isChunkLoaded(block))continue;
            var fluid=world.getFluidState(block);if(!fluid.isIn(FluidTags.WATER))continue;
            var position=new Vec3d(block.getX()+.5,block.getY()+fluid.getHeight(world,block)-.4,block.getZ()+.5);
            if(!reachable(mount,position)||!safe(passenger,position,EntityPose.STANDING,true))continue;
            double score=position.squaredDistanceTo(vanilla);if(best==null||score<best.score)best=new Landing(position,EntityPose.STANDING,score);
        }
        if(best!=null){passenger.setPose(best.pose);return best.position;}
        if(passenger instanceof ServerPlayerEntity player&&(mount.isOnGround()||mount.isTouchingWater()||mount.isInLava()))
            player.sendMessage(Text.literal("No safe landing nearby. Dismount carefully."),true);
        return vanilla;
    }
    private static boolean reachable(Entity mount,Vec3d destination){
        var start=mount.getPos().add(0,Math.min(1,mount.getHeight()*.5),0);
        return mount.getWorld().raycast(new RaycastContext(start,destination.add(0,.75,0),RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,mount)).getType()==HitResult.Type.MISS;
    }
    public static boolean safe(LivingEntity rider,Vec3d position,EntityPose pose,boolean water){
        var world=rider.getWorld();var box=rider.getDimensions(pose).getBoxAt(position).contract(.00001);
        if(!world.getWorldBorder().contains(box)||!world.isSpaceEmpty(null,box))return false;
        if(!water&&world.isSpaceEmpty(null,new Box(box.minX,position.y-.1,box.minZ,box.maxX,position.y,box.maxZ)))return false;
        if(water&&!world.getFluidState(BlockPos.ofFloored(position.add(0,rider.getDimensions(pose).height()-.1,0))).isEmpty())return false;
        for(var block:BlockPos.iterate(MathHelper.floor(box.minX),MathHelper.floor(position.y-.05),MathHelper.floor(box.minZ),MathHelper.floor(box.maxX),MathHelper.floor(box.maxY),MathHelper.floor(box.maxZ))){
            var state=world.getBlockState(block);var fluid=state.getFluidState();
            if(fluid.isIn(FluidTags.LAVA)||!water&&!fluid.isEmpty()||state.isIn(BlockTags.FIRE)||state.isOf(Blocks.CACTUS)
                    ||state.isOf(Blocks.MAGMA_BLOCK)||state.isOf(Blocks.POWDER_SNOW)||state.isOf(Blocks.SWEET_BERRY_BUSH)
                    ||state.isOf(Blocks.WITHER_ROSE)||state.getBlock() instanceof CampfireBlock&&state.get(CampfireBlock.LIT))return false;
        }
        return true;
    }
}
