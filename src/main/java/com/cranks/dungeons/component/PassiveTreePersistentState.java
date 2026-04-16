package com.cranks.dungeons.component;

import com.cranks.dungeons.CranksDungeons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

/**
 * World-level persistent state that stores all player passive data.
 * This provides a robust backup to CCA using Minecraft's native save system.
 */
public class PassiveTreePersistentState extends PersistentState {

    private static final String STATE_KEY = "cranks_dungeons_passive_tree";

    // Per-player data, keyed by UUID string
    private final Map<String, PlayerPassiveData> playerData = new HashMap<>();

    public static class PlayerPassiveData {
        public String playerClass = "";
        public int unspentPoints = 0;
        public boolean checkedRetroactive = false;
        public Set<String> unlockedNodes = new HashSet<>();

        public static final Codec<PlayerPassiveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("playerClass").forGetter(d -> d.playerClass),
                Codec.INT.fieldOf("unspentPoints").forGetter(d -> d.unspentPoints),
                Codec.BOOL.fieldOf("checkedRetroactive").forGetter(d -> d.checkedRetroactive),
                Codec.STRING.listOf().fieldOf("unlockedNodes").forGetter(d -> new ArrayList<>(d.unlockedNodes))
        ).apply(instance, (pc, up, cr, un) -> {
            PlayerPassiveData d = new PlayerPassiveData();
            d.playerClass = pc != null ? pc.trim() : "";
            d.unspentPoints = up;
            d.checkedRetroactive = cr;
            d.unlockedNodes = new HashSet<>();
            if (un != null) {
                for (String s : un) {
                    if (s != null && !s.trim().isEmpty()) {
                        d.unlockedNodes.add(s.trim());
                    }
                }
            }
            return d;
        }));
    }

    public PassiveTreePersistentState() {
        super();
    }

    // --- Accessors ---

    public void savePlayerData(UUID playerUuid, PassiveDataComponent component) {
        PlayerPassiveData data = playerData.computeIfAbsent(playerUuid.toString(), k -> new PlayerPassiveData());
        data.playerClass = component.getPlayerClass() == null ? "" : component.getPlayerClass();
        data.unspentPoints = component.getUnspentPoints();
        data.unlockedNodes = new HashSet<>(component.getUnlockedNodes());
        this.markDirty();
    }

    public void loadIntoComponent(UUID playerUuid, PassiveDataComponent component) {
        PlayerPassiveData data = playerData.get(playerUuid.toString());
        if (data != null && data.playerClass != null && !data.playerClass.isEmpty()) {
            component.setPlayerClass(data.playerClass);
            component.setUnspentPoints(data.unspentPoints);
            for (String node : data.unlockedNodes) {
                component.unlockNode(node);
            }
            CranksDungeons.LOGGER.info("[PassiveTree] Restored data for {} (Class: {}, Nodes: {})",
                    playerUuid, data.playerClass, data.unlockedNodes.size());
        }
    }

    // --- Serialization ---

    public static final Codec<PassiveTreePersistentState> CODEC = Codec.unboundedMap(Codec.STRING, PlayerPassiveData.CODEC).xmap(
            map -> {
                PassiveTreePersistentState s = new PassiveTreePersistentState();
                s.playerData.putAll(map);
                return s;
            },
            s -> s.playerData
    );

    // --- Static Helper ---

    public static PassiveTreePersistentState get(MinecraftServer server) {
        PersistentStateType<PassiveTreePersistentState> type = new PersistentStateType<>(
                STATE_KEY,
                PassiveTreePersistentState::new,
                CODEC,
                null
        );
        return server.getOverworld().getPersistentStateManager().getOrCreate(type);
    }
}
