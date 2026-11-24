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

    public static boolean tracked(net.minecraft.block.BlockState state) {
        return state.isIn(com.cranks.dungeons.registry.ModBlockTags.LOG_FORTUNE_LOGS)
                ||state.isIn(net.minecraft.registry.tag.BlockTags.LEAVES)
                ||state.isIn(com.cranks.dungeons.registry.ModBlockTags.ORE_FORTUNE_ORES)
                ||state.isIn(com.cranks.dungeons.registry.ModBlockTags.DIG_FORTUNE_BLOCKS);
    }
    private static final java.util.List<String> LEGACY_IDS=java.util.List.of(LOGS_ID,ORES_ID,DIG_BLOCKS_ID);

    public static void mark(ServerWorld world,BlockPos pos){
        var chunk=world.getChunk(pos);
        if(com.cranks.dungeons.gathering.GatheringChunkData.of(chunk).cranks$placedResources().add(pos.asLong()))chunk.setNeedsSaving(true);
    }
    public static void clear(ServerWorld world,BlockPos pos){
        var chunk=world.getChunk(pos);
        if(com.cranks.dungeons.gathering.GatheringChunkData.of(chunk).cranks$placedResources().remove(pos.asLong()))chunk.setNeedsSaving(true);
        for(String id:LEGACY_IDS){
            var legacy=State.get(world,id);if(legacy.removePlaced(pos))legacy.markDirty();
        }
    }
    public static boolean isPlaced(ServerWorld world,BlockPos pos){
        if(com.cranks.dungeons.gathering.GatheringChunkData.of(world.getChunk(pos)).cranks$placedResources().contains(pos.asLong()))return true;
        // Read old saves without losing protection. New markers are written only into chunks.
        for(String id:LEGACY_IDS){
            var legacy=State.get(world,id);if(legacy.isPlaced(pos)){mark(world,pos);if(legacy.removePlaced(pos))legacy.markDirty();return true;}
        }
        return false;
    }
    private static class State extends PersistentState {

        private final Set<Long> placed = new HashSet<>();

        private static final PersistentState.Type<State> TYPE=new PersistentState.Type<>(State::new,State::fromNbt,null);
        State() {}

        boolean removePlaced(BlockPos pos) {
            return placed.remove(pos.asLong());
        }

        boolean isPlaced(BlockPos pos) {
            return placed.contains(pos.asLong());
        }

        public static final Codec<State> CODEC = Codec.LONG.listOf().xmap(
                list -> {
                    State s = new State();
                    int limit = list.size();
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
            return world.getPersistentStateManager().getOrCreate(TYPE, trackerId);
        }
    }
}
