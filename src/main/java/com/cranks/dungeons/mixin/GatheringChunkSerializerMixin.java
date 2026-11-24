package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringChunkData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.*;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class GatheringChunkSerializerMixin {
    @Inject(method="serialize",at=@At("RETURN"))
    private static void save(ServerWorld world,Chunk chunk,CallbackInfoReturnable<NbtCompound> cir){
        var marks=GatheringChunkData.of(chunk).cranks$placedResources();
        if(!marks.isEmpty())cir.getReturnValue().putLongArray("cranks_gathering_placed",marks.toLongArray());
    }
    @Inject(method="deserialize",at=@At("RETURN"))
    private static void load(ServerWorld world,PointOfInterestStorage poi,StorageKey storage,ChunkPos pos,NbtCompound nbt,CallbackInfoReturnable<ProtoChunk> cir){
        var chunk=cir.getReturnValue();var marks=GatheringChunkData.of(chunk).cranks$placedResources();
        for(long packed:nbt.getLongArray("cranks_gathering_placed")){
            var block=BlockPos.fromLong(packed);
            if((block.getX()>>4)==pos.x&&(block.getZ()>>4)==pos.z&&!world.isOutOfHeightLimit(block))marks.add(packed);
        }
    }
}
