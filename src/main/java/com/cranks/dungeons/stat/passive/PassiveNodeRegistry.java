package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.PassiveTreeConfig;

import java.util.HashMap;
import java.util.Map;

public class PassiveNodeRegistry {
    private static final Map<String, PassiveNode> NODES = new HashMap<>();

    /**
     * Load (or reload) the node registry from the config files.
     */
    public static void loadFromConfig() {
        NODES.clear();
        for (PassiveNode node : PassiveTreeConfig.getNodes()) {
            NODES.put(node.id(), node);
        }
        CranksDungeons.LOGGER.info("[PassiveTree] Registry loaded: {} nodes", NODES.size());
    }

    /**
     * @deprecated Use {@link #loadFromConfig()} instead.
     */
    @Deprecated
    public static void register() {
        // Legacy entry point — now delegates to config-based loading
        PassiveTreeConfig.load();
        loadFromConfig();
    }

    public static PassiveNode getNode(String id) {
        return NODES.get(id);
    }

    public static Map<String, PassiveNode> getNodes() {
        return NODES;
    }
}
