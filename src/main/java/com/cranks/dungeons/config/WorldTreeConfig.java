package com.cranks.dungeons.config;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WorldTreeConfig {
    private static final Path CONFIG_DIR = Path.of("config", "cranks-dungeons");
    private static final Path TREE_FILE = CONFIG_DIR.resolve("world_tree.json");

    private static List<PassiveNode> loadedNodes = new ArrayList<>();

    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (!Files.exists(TREE_FILE)) {
                CranksDungeons.LOGGER.info("[WorldTree] Generating default world_tree.json...");
                ConfigFileUtil.installDefault(TREE_FILE);
            }

            loadedNodes = parseTree();

            CranksDungeons.LOGGER.info("[WorldTree] Loaded {} nodes from config.", loadedNodes.size());
        } catch (Exception e) {
            CranksDungeons.LOGGER.error("[WorldTree] Failed to load config!", e);
        }
    }

    public static List<PassiveNode> getNodes() {
        return loadedNodes;
    }

    public static void setNodes(List<PassiveNode> nodes) {
        loadedNodes = new ArrayList<>(nodes);
    }

    private static List<PassiveNode> parseTree() throws IOException {
        return TreeNodeJson.parseTree(TREE_FILE, "WorldTree",
                PassiveStatHelper.WHITELISTED_WORLD_ATTRIBUTES, java.util.function.UnaryOperator.identity());
    }

    public static String exportNodesToJson(Collection<PassiveNode> nodes) {
        return TreeNodeJson.exportNodesToJson(nodes);
    }

    public static void saveNodesToFile(Collection<PassiveNode> nodes) {
        try {
            String json = exportNodesToJson(nodes);
            ConfigFileUtil.atomicWrite(TREE_FILE, json);
            CranksDungeons.LOGGER.info("[WorldTree] Successfully saved {} nodes to config.", nodes.size());
        } catch (IOException e) {
            CranksDungeons.LOGGER.error("[WorldTree] FAILED to save nodes to file!", e);
        }
    }
}
