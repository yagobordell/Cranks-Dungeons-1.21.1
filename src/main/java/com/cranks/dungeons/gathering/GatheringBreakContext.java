package com.cranks.dungeons.gathering;

import com.cranks.dungeons.util.PlacedBlockTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayDeque;

/** Native player breaking removes the block before rolling drops; retain its original provenance. */
public final class GatheringBreakContext implements AutoCloseable {
    private static final ThreadLocal<ArrayDeque<GatheringBreakContext>> ACTIVE=ThreadLocal.withInitial(ArrayDeque::new);
    private final ServerWorld world;
    private final BlockPos pos;
    private final boolean placed;
    private GatheringBreakContext(ServerWorld world,BlockPos pos){
        this.world=world;this.pos=pos.toImmutable();placed=PlacedBlockTracker.tracked(world.getBlockState(pos))&&PlacedBlockTracker.isPlaced(world,pos);ACTIVE.get().push(this);
    }
    public static GatheringBreakContext begin(ServerWorld world,BlockPos pos){return new GatheringBreakContext(world,pos);}
    public static boolean placed(ServerWorld world,BlockPos pos){
        for(var context:ACTIVE.get())if(context.world==world&&context.pos.equals(pos))return context.placed;
        return PlacedBlockTracker.isPlaced(world,pos);
    }
    @Override public void close(){var stack=ACTIVE.get();if(stack.pop()!=this)throw new IllegalStateException("Gathering context order");if(stack.isEmpty())ACTIVE.remove();}
}
