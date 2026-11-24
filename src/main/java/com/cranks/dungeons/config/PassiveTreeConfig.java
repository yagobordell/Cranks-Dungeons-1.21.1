package com.cranks.dungeons.config;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import com.google.gson.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Loads the passive skill tree from JSON config files.
 * Files are stored in config/cranks-dungeons/passive_tree.json and passive_classes.json.
 * If they don't exist, defaults are generated automatically.
 */
public class PassiveTreeConfig {
    private static final Path CONFIG_DIR = Path.of("config", "cranks-dungeons");
    private static final Path TREE_FILE = CONFIG_DIR.resolve("passive_tree.json");
    private static final Path CLASSES_FILE = CONFIG_DIR.resolve("passive_classes.json");

    private static List<PassiveNode> loadedNodes = new ArrayList<>();
    private static List<ClassDefinition> loadedClasses = new ArrayList<>();

    public record ClassDefinition(String id, String name, String subtitle, String description, String recommendedStats, String color, String startNode, Map<String, Double> baseAttributes) {}

    /**
     * Load (or generate) the config files and return the parsed nodes.
     */
    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (!Files.exists(TREE_FILE)) {
                CranksDungeons.LOGGER.info("[PassiveTree] Generating default passive_tree.json...");
                ConfigFileUtil.installDefault(TREE_FILE);
            }
            if (!Files.exists(CLASSES_FILE)) {
                CranksDungeons.LOGGER.info("[PassiveTree] Generating default passive_classes.json...");
                ConfigFileUtil.installDefault(CLASSES_FILE);
            }

            List<PassiveNode> nodes = parseTree();
            List<ClassDefinition> classes = parseClasses();
            setNodesAndClasses(nodes, classes);

            CranksDungeons.LOGGER.info("[PassiveTree] Loaded {} nodes and {} classes from config.",
                    loadedNodes.size(), loadedClasses.size());
        } catch (Exception e) {
            CranksDungeons.LOGGER.error("[PassiveTree] Failed to load config!", e);
        }
    }

    public static List<PassiveNode> getNodes() {
        return loadedNodes;
    }

    public static List<ClassDefinition> getClasses() {
        return loadedClasses;
    }

    public static ClassDefinition getClassDef(String id) {
        if (id == null || id.isEmpty()) return null;
        for (ClassDefinition cd : loadedClasses) {
            if (cd.id().equals(id)) {
                return cd;
            }
        }
        return null;
    }

    public static void setNodesAndClasses(List<PassiveNode> nodes, List<ClassDefinition> classes) {
        loadedNodes = new ArrayList<>(nodes);
        loadedClasses = new ArrayList<>(classes);
    }

    // Parsing

    private static List<PassiveNode> parseTree() throws IOException {
        return TreeNodeJson.parseTree(TREE_FILE, "PassiveTree",
                PassiveStatHelper.WHITELISTED_PASSIVE_ATTRIBUTES, id -> switch (id) {
                    case "minecraft:max_health" -> "cranks-dungeons:max_health_bonus";
                    case "minecraft:armor" -> "cranks-dungeons:bonus_armor";
                    case "minecraft:movement_speed" -> "cranks-dungeons:movement_speed";
                    default -> id;
                });
    }

    private static List<ClassDefinition> parseClasses() throws IOException {
        String json = Files.readString(CLASSES_FILE);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray classesArr = root.getAsJsonArray("classes");

        List<ClassDefinition> classes = new ArrayList<>();
        for (JsonElement el : classesArr) {
            JsonObject obj = el.getAsJsonObject();
            Map<String, Double> baseAttrs = new HashMap<>();
            if (obj.has("baseAttributes")) {
                JsonObject attrsObj = obj.getAsJsonObject("baseAttributes");
                for (Map.Entry<String, JsonElement> entry : attrsObj.entrySet()) {
                    String attrId = entry.getKey();
                    if (attrId.equals("cranks-dungeons:strength") || 
                        attrId.equals("cranks-dungeons:dexterity") || 
                        attrId.equals("cranks-dungeons:intelligence")) {
                        baseAttrs.put(attrId, entry.getValue().getAsDouble());
                    } else {
                        CranksDungeons.LOGGER.warn("[PassiveTree] Ignored invalid baseAttribute '{}' for class '{}'. Only strength, dexterity, and intelligence are permitted.", attrId, obj.get("id").getAsString());
                    }
                }
            }
            classes.add(new ClassDefinition(
                    obj.get("id").getAsString().trim(),
                    obj.get("name").getAsString(),
                    obj.has("subtitle") ? obj.get("subtitle").getAsString() : "No subtitle provided.",
                    obj.has("description") ? obj.get("description").getAsString() : "No description provided.",
                    obj.has("recommendedStats") ? obj.get("recommendedStats").getAsString() : "No recommendations provided.",
                    obj.has("color") ? obj.get("color").getAsString() : "#000000",
                    obj.get("startNode").getAsString().trim(),
                    baseAttrs
            ));
        }
        return classes;
    }

    /** Exports the passive tree using the config file schema. */
    public static String exportNodesToJson(Collection<PassiveNode> nodes) {
        return TreeNodeJson.exportNodesToJson(nodes);
    }

    /**
     * Directly saves the provided nodes to config/cranks-dungeons/passive_tree.json.
     * Use with CAUTION as it overwrites the file.
     */
    public static void saveNodesToFile(Collection<PassiveNode> nodes) {
        try {
            String json = exportNodesToJson(nodes);
            ConfigFileUtil.atomicWrite(TREE_FILE, json);
            CranksDungeons.LOGGER.info("[PassiveTree] Successfully saved {} nodes to config.", nodes.size());
        } catch (IOException e) {
            CranksDungeons.LOGGER.error("[PassiveTree] FAILED to save nodes to file!", e);
        }
    }
}
