package com.cranks.dungeons.gathering;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WrapperProtoChunk;

/** Provenance follows chunk loading/saving rather than a capped global coordinate history. */
public interface GatheringChunkData {
    LongSet cranks$placedResources();
    static GatheringChunkData of(Chunk chunk) {
        return (GatheringChunkData)(Object)(chunk instanceof WrapperProtoChunk wrapper?wrapper.getWrappedChunk():chunk);
    }
}
