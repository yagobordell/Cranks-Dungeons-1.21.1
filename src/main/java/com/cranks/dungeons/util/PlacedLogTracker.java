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

public final class PlacedLogTracker {

    private static final String ID = "cranks_dungeons_placed_logs";

    private PlacedLogTracker() {}

    /* =====================  PUBLIC API  ===================== */

    public static void markPlaced(ServerWorld world, BlockPos pos) {
        State state = State.get(world);
        if (state.markPlaced(pos)) {
            state.markDirty();
            // Debug:
            System.out.println("[PlacedLogTracker] Marked placed log at " + pos + " (persistent)");
        }
    }

    public static void removePlaced(ServerWorld world, BlockPos pos) {
        State state = State.get(world);
        if (state.removePlaced(pos)) {
            state.markDirty();
            System.out.println("[PlacedLogTracker] Removed placed log at " + pos + " (persistent)");
        }
    }

    public static boolean isPlacedByPlayer(ServerWorld world, BlockPos pos) {
        State state = State.get(world);
        boolean placed = state.isPlaced(pos);
        System.out.println("[PlacedLogTracker] isPlacedByPlayer(" + pos + ") = " + placed);
        return placed;
    }

    /* =====================  INTERNAL PERSISTENT STATE  ===================== */

    private static class State extends PersistentState {

        private static final int MAX_TRACKED = 100_000;

        // Stored as BlockPos.asLong()
        private final Set<Long> placedLogs = new HashSet<>();

        State() {}

        boolean markPlaced(BlockPos pos) {
            if (placedLogs.size() >= MAX_TRACKED) return false;
            return placedLogs.add(pos.asLong());
        }

        boolean removePlaced(BlockPos pos) {
            return placedLogs.remove(pos.asLong());
        }

        boolean isPlaced(BlockPos pos) {
            return placedLogs.contains(pos.asLong());
        }

        /* ===== CODEC ===== */

        public static final Codec<State> CODEC = Codec.LONG.listOf().xmap(
                // Decode: List<Long> -> State
                list -> {
                    State s = new State();
                    int limit = Math.min(list.size(), MAX_TRACKED);
                    for (int i = 0; i < limit; i++) {
                        s.placedLogs.add(list.get(i));
                    }
                    return s;
                },
                // Encode: State -> List<Long>
                s -> new ArrayList<>(s.placedLogs)
        );

        public static final PersistentStateType<State> TYPE =
                new PersistentStateType<>(ID, State::new, CODEC, null);

        public static State get(ServerWorld world) {
            return world.getPersistentStateManager().getOrCreate(TYPE);
        }
    }
}