package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringChunkData;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;

@Mixin(Chunk.class)
public abstract class GatheringChunkMixin implements GatheringChunkData {
    @Unique private final LongSet cranks$placedResources=new LongOpenHashSet();
    @Override public LongSet cranks$placedResources(){return cranks$placedResources;}
}
