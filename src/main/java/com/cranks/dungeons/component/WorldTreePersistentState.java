package com.cranks.dungeons.component;

import com.cranks.dungeons.CranksDungeons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

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
            component.setUnspentPoints(data.unspentPoints);
            component.setGatheringXp(data.gatheringXp, data.gatheringLevel);
            for (String node : data.unlockedNodes) {
                component.unlockNode(node);
            }
            for (Map.Entry<String, String> entry : data.nodeChoices.entrySet()) {
                component.setNodeChoice(entry.getKey(), entry.getValue());
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

    // --- Static Helper ---

    public static WorldTreePersistentState get(MinecraftServer server) {
        PersistentStateType<WorldTreePersistentState> type = new PersistentStateType<>(
                STATE_KEY,
                WorldTreePersistentState::new,
                CODEC,
                null
        );
        return server.getOverworld().getPersistentStateManager().getOrCreate(type);
    }
}
