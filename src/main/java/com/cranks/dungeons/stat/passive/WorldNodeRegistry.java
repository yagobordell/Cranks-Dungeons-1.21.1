package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.WorldTreeConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldNodeRegistry {
    private static final Map<String, PassiveNode> NODES = new HashMap<>();

    private static Map<String, Set<String>> neighbors = Map.of();

    public static void loadFromConfig() {
        NODES.clear();
        for (PassiveNode node : WorldTreeConfig.getNodes()) {
            NODES.put(node.id(), node);
        }
        neighbors = PassiveTreeGraph.index(NODES, java.util.List.of("world_start"), "WorldTree");
        CranksDungeons.LOGGER.info("[WorldTree] Registry loaded: {} nodes", NODES.size());
    }

    public static Set<String> getNeighbors(String id) {
        return neighbors.getOrDefault(id, Set.of());
    }

    public static PassiveNode getNode(String id) {
        return NODES.get(id);
    }

    public static Map<String, PassiveNode> getNodes() {
        return NODES;
    }
}
