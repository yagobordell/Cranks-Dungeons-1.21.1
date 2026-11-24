package com.cranks.dungeons.gathering;

import com.cranks.dungeons.util.PlacedBlockTracker;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import java.util.*;

/** Capture the real movement plan after vanilla removes a retracting piston head. */
public final class GatheringPistonMove implements AutoCloseable {
    private static final ThreadLocal<ArrayDeque<GatheringPistonMove>> ACTIVE=ThreadLocal.withInitial(ArrayDeque::new);
    private final ServerWorld world;
    private final Map<BlockPos,Boolean> marks=new LinkedHashMap<>();
    private GatheringPistonMove(ServerWorld world){this.world=world;ACTIVE.get().push(this);}
    public static GatheringPistonMove begin(ServerWorld world){return new GatheringPistonMove(world);}
    public static void capture(PistonHandler handler){
        var context=ACTIVE.get().peek();if(context==null)return;
        for(var source:handler.getMovedBlocks())context.marks.put(source.toImmutable(),PlacedBlockTracker.isPlaced(context.world,source));
    }
    public void finish(Direction movement){
        // Clear the entire movement footprint before writing any destination.
        for(var source:marks.keySet()){PlacedBlockTracker.clear(world,source);PlacedBlockTracker.clear(world,source.offset(movement));}
        for(var entry:marks.entrySet())if(entry.getValue()){
            var destination=entry.getKey().offset(movement);
            PlacedBlockTracker.mark(world,destination);
            if(world.getBlockEntity(destination) instanceof GatheringPistonData data){
                data.cranks$placedResource(true);world.getBlockEntity(destination).markDirty();
            }
        }
    }
    @Override public void close(){var stack=ACTIVE.get();if(stack.pop()!=this)throw new IllegalStateException("Piston context order");if(stack.isEmpty())ACTIVE.remove();}
}
