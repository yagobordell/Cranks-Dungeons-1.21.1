package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringChunkData;
import com.cranks.dungeons.util.PlacedBlockTracker;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(WorldChunk.class)
public abstract class GatheringWorldChunkMixin {
    @Shadow @Final private World world;
    @Inject(method="<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V",at=@At("TAIL"))
    private void inherit(ServerWorld world,ProtoChunk proto,WorldChunk.EntityLoader loader,CallbackInfo ci){
        GatheringChunkData.of((WorldChunk)(Object)this).cranks$placedResources().addAll(GatheringChunkData.of(proto).cranks$placedResources());
    }
    @Inject(method="setBlockState",at=@At("RETURN"))
    private void changed(BlockPos pos,BlockState state,boolean moved,CallbackInfoReturnable<BlockState> cir){
        var old=cir.getReturnValue();
        // Replacement callbacks may change the block again before this invocation returns.
        state=((WorldChunk)(Object)this).getBlockState(pos);
        if(world instanceof ServerWorld server&&old!=null&&!old.isOf(state.getBlock())
                &&(PlacedBlockTracker.tracked(old)||old.isOf(Blocks.MOVING_PISTON))
                &&!PlacedBlockTracker.tracked(state))PlacedBlockTracker.clear(server,pos);
    }
}
