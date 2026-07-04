package com.cranks.dungeons.config;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.google.gson.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WorldTreeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_DIR = Path.of("config", "cranks-dungeons");
    private static final Path TREE_FILE = CONFIG_DIR.resolve("world_tree.json");

    private static List<PassiveNode> loadedNodes = new ArrayList<>();

    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (!Files.exists(TREE_FILE)) {
                CranksDungeons.LOGGER.info("[WorldTree] Generating default world_tree.json...");
                generateDefaultTree();
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
        String json = Files.readString(TREE_FILE);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray nodesArr = root.getAsJsonArray("nodes");

        List<PassiveNode> nodes = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (JsonElement el : nodesArr) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("id").getAsString().trim();
            
            if (seenIds.contains(id)) {
                CranksDungeons.LOGGER.error("[WorldTree] CRITICAL ERROR: Duplicate Node ID '{}' found. Ignoring.", id);
                continue;
            }
            seenIds.add(id);

            String name = obj.get("name").getAsString();
            String description = obj.get("description").getAsString();
            double gridX = obj.get("gridX").getAsDouble();
            double gridY = obj.get("gridY").getAsDouble();
            String shape = obj.has("shape") ? obj.get("shape").getAsString() : null;

            List<String> connections = new ArrayList<>();
            if (obj.has("connections")) {
                for (JsonElement c : obj.getAsJsonArray("connections")) {
                    connections.add(c.getAsString().trim());
                }
            }

            Map<RegistryEntry<EntityAttribute>, Double> modifiers = new LinkedHashMap<>();
            if (obj.has("modifiers")) {
                JsonObject modsObj = obj.getAsJsonObject("modifiers");
                for (Map.Entry<String, JsonElement> entry : modsObj.entrySet()) {
                    String attrId = entry.getKey();
                    double value = entry.getValue().getAsDouble();

                    RegistryEntry<EntityAttribute> attr = resolveAttribute(attrId);
                    if (attr != null) {
                        modifiers.put(attr, value);
                    }
                }
            }

            boolean isAttributeSelection = obj.has("is_attribute_selection") && obj.get("is_attribute_selection").getAsBoolean();
            double attributeSelectionAmount = obj.has("attribute_selection_amount") ? obj.get("attribute_selection_amount").getAsDouble() : 0.0;

            Identifier icon;
            if (obj.has("icon") && !obj.get("icon").getAsString().isEmpty()) {
                icon = Identifier.of(obj.get("icon").getAsString());
            } else {
                icon = com.cranks.dungeons.stat.passive.PassiveIconManager.getIcon(id, modifiers, isAttributeSelection);
            }

            nodes.add(new PassiveNode(id, name, description, gridX, gridY, connections, modifiers, icon, shape, isAttributeSelection, attributeSelectionAmount));
        }

        return nodes;
    }

    private static RegistryEntry<EntityAttribute> resolveAttribute(String attrId) {
        Identifier id = Identifier.tryParse(attrId);
        if (id == null) return null;
        return Registries.ATTRIBUTE.getOptionalValue(id)
                .map(attr -> Registries.ATTRIBUTE.getEntry(id).orElse(null))
                .orElse(null);
    }

    private static void generateDefaultTree() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray nodes = new JsonArray();

        // Starting node
        nodes.add(makeNode("world_start", "Roots of the World", "Your journey begins here.",
                0, 0, List.of("gather_speed_1", "ore_fortune_1"), Map.of(), "diamond", false, 0.0));
        nodes.add(makeNode("gather_speed_1", "Swift Hands", "+5% Gathering Speed",
                -1, -1, List.of("world_start"),
                Map.of("cranks-dungeons:mining_efficiency", 0.05), null, false, 0.0));
        nodes.add(makeNode("ore_fortune_1", "Prospector", "+5% Ore Fortune",
                1, -1, List.of("world_start"),
                Map.of("cranks-dungeons:fortune", 0.05), null, false, 0.0));

        root.add("nodes", nodes);
        Files.writeString(TREE_FILE, GSON.toJson(root));
    }

    private static JsonObject makeNode(String id, String name, String desc, double gx, double gy,
                                       List<String> connections, Map<String, Double> modifiers, String shape, boolean isChoice, double choiceAmt) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("description", desc);
        obj.addProperty("gridX", gx);
        obj.addProperty("gridY", gy);
        obj.addProperty("shape", shape);
        
        if (isChoice) {
            obj.addProperty("is_attribute_selection", true);
            obj.addProperty("attribute_selection_amount", choiceAmt);
        }

        JsonArray conns = new JsonArray();
        connections.forEach(conns::add);
        obj.add("connections", conns);

        JsonObject mods = new JsonObject();
        modifiers.forEach(mods::addProperty);
        obj.add("modifiers", mods);

        return obj;
    }

    public static String exportNodesToJson(Collection<PassiveNode> nodes) {
        JsonObject root = new JsonObject();
        JsonArray nodesArr = new JsonArray();

        List<PassiveNode> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator.comparing(PassiveNode::id));

        for (PassiveNode node : sorted) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", node.id());
            obj.addProperty("name", node.name());
            obj.addProperty("description", node.description());
            
            obj.addProperty("gridX", Math.round(node.gridX() * 1000.0) / 1000.0);
            obj.addProperty("gridY", Math.round(node.gridY() * 1000.0) / 1000.0);
            
            if (node.shape() != null) {
                obj.addProperty("shape", node.shape());
            }

            if (node.isAttributeSelection()) {
                obj.addProperty("is_attribute_selection", true);
                obj.addProperty("attribute_selection_amount", node.attributeSelectionAmount());
            }

            JsonArray conns = new JsonArray();
            for (String conn : node.connections()) {
                conns.add(conn);
            }
            obj.add("connections", conns);

            JsonObject mods = new JsonObject();
            for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                Identifier id = Registries.ATTRIBUTE.getId(entry.getKey().value());
                if (id != null) {
                    mods.addProperty(id.toString(), entry.getValue());
                }
            }
            obj.add("modifiers", mods);

            if (node.icon() != null) {
                obj.addProperty("icon", node.icon().toString());
            } else {
                obj.addProperty("icon", "");
            }

            nodesArr.add(obj);
        }

        root.add("nodes", nodesArr);
        return GSON.toJson(root);
    }

    public static void saveNodesToFile(Collection<PassiveNode> nodes) {
        try {
            String json = exportNodesToJson(nodes);
            Files.writeString(TREE_FILE, json);
            CranksDungeons.LOGGER.info("[WorldTree] Successfully saved {} nodes to config.", nodes.size());
        } catch (IOException e) {
            CranksDungeons.LOGGER.error("[WorldTree] FAILED to save nodes to file!", e);
        }
    }
}
