package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.WorldTreeConfig;

import java.util.HashMap;
import java.util.Map;

public class WorldNodeRegistry {
    private static final Map<String, PassiveNode> NODES = new HashMap<>();

    public static void loadFromConfig() {
        NODES.clear();
        for (PassiveNode node : WorldTreeConfig.getNodes()) {
            NODES.put(node.id(), node);
        }
        CranksDungeons.LOGGER.info("[WorldTree] Registry loaded: {} nodes", NODES.size());
    }

    public static PassiveNode getNode(String id) {
        return NODES.get(id);
    }

    public static Map<String, PassiveNode> getNodes() {
        return NODES;
    }
}
