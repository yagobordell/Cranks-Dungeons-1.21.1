package com.cranks.dungeons.util;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class PlacedBlockTracker {

    private PlacedBlockTracker() {}

    public static final String DIG_BLOCKS_ID = "cranks_dungeons_placed_dig_blocks";
    public static final String LOGS_ID = "cranks_dungeons_placed_logs";
    public static final String ORES_ID = "cranks_dungeons_placed_ores";
    public static final String GATHERING_BLOCKS_ID = "cranks_dungeons_placed_gathering_blocks";

    public static void markPlaced(ServerWorld world, BlockPos pos, String trackerId) {
        State state = State.get(world, trackerId);
        if (state.markPlaced(pos)) {
            state.markDirty();
        }
    }

    public static void removePlaced(ServerWorld world, BlockPos pos, String trackerId) {
        State state = State.get(world, trackerId);
        if (state.removePlaced(pos)) {
            state.markDirty();
        }
    }

    public static boolean isPlacedByPlayer(ServerWorld world, BlockPos pos, String trackerId) {
        State state = State.get(world, trackerId);
        return state.isPlaced(pos);
    }

    private static class State extends PersistentState {

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

        private static final String NBT_KEY = "placed";

        public static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
            if (!nbt.contains(NBT_KEY)) {
                return new State();
            }
            return CODEC.parse(registryLookup.getOps(net.minecraft.nbt.NbtOps.INSTANCE), nbt.get(NBT_KEY))
                    .resultOrPartial(err -> com.cranks.dungeons.CranksDungeons.LOGGER.error("Failed to parse PlacedBlockTracker State: {}", err))
                    .orElseGet(State::new);
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
            CODEC.encodeStart(registryLookup.getOps(net.minecraft.nbt.NbtOps.INSTANCE), this)
                    .resultOrPartial(err -> com.cranks.dungeons.CranksDungeons.LOGGER.error("Failed to encode PlacedBlockTracker State: {}", err))
                    .ifPresent(encoded -> nbt.put(NBT_KEY, encoded));
            return nbt;
        }

        public static State get(ServerWorld world, String trackerId) {
            PersistentState.Type<State> type = new PersistentState.Type<>(
                    State::new,
                    State::fromNbt,
                    null
            );
            return world.getPersistentStateManager().getOrCreate(type, trackerId);
        }
    }
}
