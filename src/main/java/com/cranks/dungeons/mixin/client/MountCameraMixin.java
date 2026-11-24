package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mounted foliage clipping and small vertical easing; never changes the rider or camera yaw. */
@Mixin(Camera.class)
public abstract class MountCameraMixin {
    @Shadow public abstract Vec3d getPos();
    @Shadow protected abstract void setPos(Vec3d pos);
    @Shadow private Entity focusedEntity;
    @Unique private Entity cranks$focus;
    @Unique private Vec3d cranks$lastRaw;
    @Unique private double cranks$viewY;
    @Unique private long cranks$lastFrame,cranks$transitionUntil;
    @Unique private Entity cranks$vehicle;

    @ModifyArg(method="clipToSpace",at=@At(value="INVOKE",target="Lnet/minecraft/world/BlockView;raycast(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;"),index=0)
    private RaycastContext mountedCameraFoliage(RaycastContext context){
        return cranks$foliageContext(context,focusedEntity);
    }

    @Unique
    private static RaycastContext cranks$foliageContext(RaycastContext context,Entity focused){
        if(focused==null||focused.getVehicle()==null||MountKind.of(focused.getVehicle())==MountKind.UNSUPPORTED)return context;
        // Skip leaf shapes during the whole ray, so a wall behind foliage still
        // shortens the camera. Keep this local to mounted camera queries.
        return new RaycastContext(context.getStart(),context.getEnd(),RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,focused){
            @Override public VoxelShape getBlockShape(BlockState state,BlockView world,BlockPos pos){
                return state.isIn(BlockTags.LEAVES)?VoxelShapes.empty():super.getBlockShape(state,world,pos);
            }
        };
    }

    @Inject(method="update",at=@At("TAIL"))
    private void smoothMountView(BlockView area,Entity focused,boolean thirdPerson,boolean inverse,float delta,CallbackInfo ci){
        var client=MinecraftClient.getInstance();var raw=getPos();long now=System.nanoTime();
        Entity vehicle=focused.getVehicle();boolean mounted=vehicle!=null&&MountKind.of(vehicle)!=MountKind.UNSUPPORTED;
        boolean reset=cranks$focus!=focused||cranks$lastRaw==null||raw.squaredDistanceTo(cranks$lastRaw)>16
                ||!CranksDungeons.CONFIG.smoothMountCamera()||focused!=client.player||focused.isRemoved();
        if(reset){cranks$focus=focused;cranks$viewY=raw.y;cranks$vehicle=vehicle;cranks$transitionUntil=0;}
        if(vehicle!=cranks$vehicle){cranks$transitionUntil=now+300_000_000L;cranks$vehicle=vehicle;}
        double seconds=cranks$lastFrame==0?.05:Math.clamp((now-cranks$lastFrame)/1_000_000_000.0,.001,.1);
        cranks$lastFrame=now;cranks$lastRaw=raw;
        if(reset||!mounted&&now>=cranks$transitionUntil){cranks$viewY=raw.y;return;}
        cranks$viewY+=(raw.y-cranks$viewY)*(1-Math.exp(-seconds/.085));
        cranks$viewY=Math.clamp(cranks$viewY,raw.y-.40,raw.y+.40);
        Vec3d desired=new Vec3d(raw.x,cranks$viewY,raw.z);
        if(client.world!=null&&client.world.raycast(cranks$foliageContext(new RaycastContext(raw,desired,
                RaycastContext.ShapeType.VISUAL,RaycastContext.FluidHandling.NONE,focused),focused)).getType()==HitResult.Type.MISS){
            setPos(desired);
        }else cranks$viewY=raw.y;
    }
}
