package com.cranks.dungeons.util;

import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PlacedDigBlockTracker {

    private static final String ID = "cranks_dungeons_placed_dig_blocks";

    private PlacedDigBlockTracker() {}

    public static void markPlaced(ServerWorld world, BlockPos pos) {
        State state = State.get(world);
        if (state.markPlaced(pos)) {
            state.markDirty();
            System.out.println("[PlacedDigBlockTracker] Marked placed dig block at " + pos);
        }
    }

    public static void removePlaced(ServerWorld world, BlockPos pos) {
        State state = State.get(world);
        if (state.removePlaced(pos)) {
            state.markDirty();
            System.out.println("[PlacedDigBlockTracker] Removed placed dig block at " + pos);
        }
    }

    public static boolean isPlacedByPlayer(ServerWorld world, BlockPos pos) {
        State state = State.get(world);
        boolean placed = state.isPlaced(pos);
        System.out.println("[PlacedDigBlockTracker] isPlacedByPlayer(" + pos + ") = " + placed);
        return placed;
    }

    private static class State extends PersistentState {

        // Maximum number of player-placed blocks to track for fortune checks.
    // This prevents memory growth from unlimited tracking while being high enough
    // to not affect normal gameplay (players would need to place ~100k blocks
    // that require fortune checks to hit this limit).
    private static final int MAX_TRACKED_PLACED_BLOCKS = 100_000;
        private final Set<Long> placed = new HashSet<>();

        State() {}

        boolean markPlaced(BlockPos pos) {
            if (placed.size() >= MAX_TRACKED_PLACED_BLOCKS) return false;
            return placed.add(pos.asLong());
        }

        boolean removePlaced(BlockPos pos) {
            return placed.remove(pos.asLong());
        }

        boolean isPlaced(BlockPos pos) {
            return placed.contains(pos.asLong());
        }

        // Codec: store list of longs
        public static final Codec<State> CODEC = Codec.LONG.listOf().xmap(
                list -> {
                    State s = new State();
                    int limit = Math.min(list.size(), MAX_TRACKED_PLACED_BLOCKS);
                    for (int i = 0; i < limit; i++) {
                        s.placed.add(list.get(i));
                    }
                    return s;
                },
                s -> new ArrayList<>(s.placed)
        );

        public static final PersistentStateType<State> TYPE =
                new PersistentStateType<>(ID, State::new, CODEC, null);

        public static State get(ServerWorld world) {
            return world.getPersistentStateManager().getOrCreate(TYPE);
        }
    }
}