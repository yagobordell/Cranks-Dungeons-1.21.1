package com.cranks.dungeons.component;

import com.cranks.dungeons.CranksDungeons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.*;

/**
 * World-level persistent state that stores all player world tree data.
 * This provides a robust backup to CCA using Minecraft's native save system.
 */
public class WorldTreePersistentState extends PersistentState {

    private static final String STATE_KEY = "cranks_dungeons_world_tree";

    // Per-player data, keyed by UUID string
    private final Map<String, PlayerWorldTreeData> playerData = new HashMap<>();

    public static class PlayerWorldTreeData {
        public int unspentPoints = 0;
        public int gatheringXp = 0;
        public int gatheringLevel = 1;
        public Set<String> unlockedNodes = new HashSet<>();
        public Map<String, String> nodeChoices = new HashMap<>();

        public static final Codec<PlayerWorldTreeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("unspentPoints").forGetter(d -> d.unspentPoints),
                Codec.INT.fieldOf("gatheringXp").forGetter(d -> d.gatheringXp),
                Codec.INT.fieldOf("gatheringLevel").forGetter(d -> d.gatheringLevel),
                Codec.STRING.listOf().fieldOf("unlockedNodes").forGetter(d -> new ArrayList<>(d.unlockedNodes)),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("nodeChoices").forGetter(d -> d.nodeChoices)
        ).apply(instance, (up, gx, gl, un, nc) -> {
            PlayerWorldTreeData d = new PlayerWorldTreeData();
            d.unspentPoints = up;
            d.gatheringXp = gx;
            d.gatheringLevel = gl;
            d.unlockedNodes = new HashSet<>();
            if (un != null) {
                for (String s : un) {
                    if (s != null && !s.trim().isEmpty()) {
                        d.unlockedNodes.add(s.trim());
                    }
                }
            }
            d.nodeChoices = new HashMap<>();
            if (nc != null) {
                d.nodeChoices.putAll(nc);
            }
            return d;
        }));
    }

    public WorldTreePersistentState() {
        super();
    }

    // --- Accessors ---

    public void savePlayerData(UUID playerUuid, WorldTreeDataComponent component) {
        PlayerWorldTreeData data = playerData.computeIfAbsent(playerUuid.toString(), k -> new PlayerWorldTreeData());
        data.unspentPoints = component.getUnspentPoints();
        data.gatheringXp = component.getGatheringXp();
        data.gatheringLevel = component.getGatheringLevel();
        data.unlockedNodes = new HashSet<>(component.getUnlockedNodes());
        data.nodeChoices = new HashMap<>(component.getAllNodeChoices());
        this.markDirty();
    }

    public void loadIntoComponent(UUID playerUuid, WorldTreeDataComponent component) {
        PlayerWorldTreeData data = playerData.get(playerUuid.toString());
        if (data != null) {
            component.beginBatch();
            try {
                component.setUnspentPoints(data.unspentPoints);
                component.setGatheringXp(data.gatheringXp, data.gatheringLevel);
                for (String node : data.unlockedNodes) {
                    component.unlockNode(node);
                }
                for (Map.Entry<String, String> entry : data.nodeChoices.entrySet()) {
                    component.setNodeChoice(entry.getKey(), entry.getValue());
                }
            } finally {
                component.endBatch();
            }
            CranksDungeons.LOGGER.info("[WorldTree] Restored data for {} (Level: {}, Nodes: {})",
                    playerUuid, data.gatheringLevel, data.unlockedNodes.size());
        }
    }

    // --- Serialization ---

    public static final Codec<WorldTreePersistentState> CODEC = Codec.unboundedMap(Codec.STRING, PlayerWorldTreeData.CODEC).xmap(
            map -> {
                WorldTreePersistentState s = new WorldTreePersistentState();
                s.playerData.putAll(map);
                return s;
            },
            s -> s.playerData
    );

    public static WorldTreePersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        WorldTreePersistentState state = new WorldTreePersistentState();
        NbtCompound players = nbt.contains("players", NbtElement.COMPOUND_TYPE)
                ? nbt.getCompound("players") : nbt;

        for (String playerId : players.getKeys()) {
            if (!players.contains(playerId, NbtElement.COMPOUND_TYPE)) continue;
            try {
                UUID.fromString(playerId);
                NbtCompound entry = players.getCompound(playerId);
                PlayerWorldTreeData data = new PlayerWorldTreeData();
                data.unspentPoints = Math.max(0, entry.getInt("unspentPoints"));
                data.gatheringXp = Math.max(0, entry.getInt("gatheringXp"));
                data.gatheringLevel = Math.max(1, entry.getInt("gatheringLevel"));
                if(!entry.contains("progressionVersion")) {
                    double oldRequired=200+75.0*data.gatheringLevel+.5*data.gatheringLevel*data.gatheringLevel;
                    data.gatheringXp=(int)(Math.clamp(data.gatheringXp/oldRequired,0,1)*com.cranks.dungeons.config.ProgressionConfig.RULES.required(false,data.gatheringLevel));
                }

                NbtList nodes = entry.getList("unlockedNodes", NbtElement.STRING_TYPE);
                for (int i = 0; i < nodes.size(); i++) {
                    String node = nodes.getString(i).trim();
                    if (!node.isEmpty()) data.unlockedNodes.add(node);
                }

                if (entry.contains("nodeChoices", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound choices = entry.getCompound("nodeChoices");
                    for (String node : choices.getKeys()) {
                        String choice = choices.getString(node).trim();
                        if (!node.isBlank() && !choice.isEmpty()) data.nodeChoices.put(node.trim(), choice);
                    }
                }
                state.playerData.put(playerId, data);
            } catch (Exception e) {
                CranksDungeons.LOGGER.error("[WorldTree] Skipping corrupt backup entry for {}", playerId, e);
            }
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound players = new NbtCompound();
        for (Map.Entry<String, PlayerWorldTreeData> playerEntry : playerData.entrySet()) {
            PlayerWorldTreeData data = playerEntry.getValue();
            NbtCompound entry = new NbtCompound();
            entry.putInt("unspentPoints", Math.max(0, data.unspentPoints));
            entry.putInt("gatheringXp", Math.max(0, data.gatheringXp));
            entry.putInt("gatheringLevel", Math.max(1, data.gatheringLevel));
            entry.putInt("progressionVersion",1);

            NbtList nodes = new NbtList();
            data.unlockedNodes.forEach(node -> nodes.add(NbtString.of(node)));
            entry.put("unlockedNodes", nodes);

            NbtCompound choices = new NbtCompound();
            data.nodeChoices.forEach(choices::putString);
            entry.put("nodeChoices", choices);
            players.put(playerEntry.getKey(), entry);
        }
        nbt.put("players", players);
        return nbt;
    }

    // --- Static Helper ---

    public static WorldTreePersistentState get(MinecraftServer server) {
        PersistentState.Type<WorldTreePersistentState> type = new PersistentState.Type<>(
                WorldTreePersistentState::new,
                WorldTreePersistentState::fromNbt,
                null
        );
        return server.getOverworld().getPersistentStateManager().getOrCreate(type, STATE_KEY);
    }
}
