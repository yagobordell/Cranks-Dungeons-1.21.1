package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.PassiveTreeConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PassiveNodeRegistry {
    private static final Map<String, PassiveNode> NODES = new HashMap<>();

    /**
     * Load (or reload) the node registry from the config files.
     */
    private static Map<String, Set<String>> neighbors = Map.of();

    public static void loadFromConfig() {
        NODES.clear();
        for (PassiveNode node : PassiveTreeConfig.getNodes()) {
            NODES.put(node.id(), node);
        }
        neighbors = PassiveTreeGraph.index(NODES, PassiveTreeConfig.getClasses().stream().map(PassiveTreeConfig.ClassDefinition::startNode).toList(), "PassiveTree");
        CranksDungeons.LOGGER.info("[PassiveTree] Registry loaded: {} nodes", NODES.size());
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
