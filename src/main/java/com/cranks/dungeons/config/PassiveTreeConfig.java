package com.cranks.dungeons.config;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.google.gson.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Loads the passive skill tree from JSON config files.
 * Files are stored in config/cranks-dungeons/passive_tree.json and passive_classes.json.
 * If they don't exist, defaults are generated automatically.
 */
public class PassiveTreeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_DIR = Path.of("config", "cranks-dungeons");
    private static final Path TREE_FILE = CONFIG_DIR.resolve("passive_tree.json");
    private static final Path CLASSES_FILE = CONFIG_DIR.resolve("passive_classes.json");

    private static List<PassiveNode> loadedNodes = new ArrayList<>();
    private static List<ClassDefinition> loadedClasses = new ArrayList<>();

    public record ClassDefinition(String id, String name, String startNode) {}

    /**
     * Load (or generate) the config files and return the parsed nodes.
     */
    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (!Files.exists(TREE_FILE)) {
                CranksDungeons.LOGGER.info("[PassiveTree] Generating default passive_tree.json...");
                generateDefaultTree();
            }
            if (!Files.exists(CLASSES_FILE)) {
                CranksDungeons.LOGGER.info("[PassiveTree] Generating default passive_classes.json...");
                generateDefaultClasses();
            }

            loadedNodes = parseTree();
            loadedClasses = parseClasses();

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

    public static void setNodesAndClasses(List<PassiveNode> nodes, List<ClassDefinition> classes) {
        loadedNodes = new ArrayList<>(nodes);
        loadedClasses = new ArrayList<>(classes);
    }

    // ─── Parsing ────────────────────────────────────────────────────────────────

    private static List<PassiveNode> parseTree() throws IOException {
        String json = Files.readString(TREE_FILE);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray nodesArr = root.getAsJsonArray("nodes");

        List<PassiveNode> nodes = new ArrayList<>();
        for (JsonElement el : nodesArr) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("id").getAsString();
            String name = obj.get("name").getAsString();
            String description = obj.get("description").getAsString();
            int gridX = obj.get("gridX").getAsInt();
            int gridY = obj.get("gridY").getAsInt();
            String shape = obj.has("shape") ? obj.get("shape").getAsString() : null;

            List<String> connections = new ArrayList<>();
            if (obj.has("connections")) {
                for (JsonElement c : obj.getAsJsonArray("connections")) {
                    connections.add(c.getAsString());
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
                    } else {
                        com.cranks.dungeons.CranksDungeons.LOGGER.warn("[PassiveTree] Unknown attribute '{}' in node '{}'", attrId, id);
                    }
                }
            }

            // Determine Icon (Priority: JSON Override -> PassiveIconManager)
            Identifier icon;
            if (obj.has("icon") && !obj.get("icon").getAsString().isEmpty()) {
                icon = Identifier.of(obj.get("icon").getAsString());
            } else {
                icon = com.cranks.dungeons.stat.passive.PassiveIconManager.getIcon(id, modifiers);
            }

            nodes.add(new PassiveNode(id, name, description, gridX, gridY, connections, modifiers, icon, shape));
        }

        return nodes;
    }

    private static List<ClassDefinition> parseClasses() throws IOException {
        String json = Files.readString(CLASSES_FILE);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray classesArr = root.getAsJsonArray("classes");

        List<ClassDefinition> classes = new ArrayList<>();
        for (JsonElement el : classesArr) {
            JsonObject obj = el.getAsJsonObject();
            classes.add(new ClassDefinition(
                    obj.get("id").getAsString(),
                    obj.get("name").getAsString(),
                    obj.get("startNode").getAsString()
            ));
        }
        return classes;
    }

    /**
     * Resolve an attribute ID string like "cranks-dungeons:crit_chance" or "minecraft:max_health"
     * to a RegistryEntry.
     */
    private static RegistryEntry<EntityAttribute> resolveAttribute(String attrId) {
        Identifier id = Identifier.tryParse(attrId);
        if (id == null) return null;
        return Registries.ATTRIBUTE.getOptionalValue(id)
                .map(attr -> Registries.ATTRIBUTE.getEntry(id).orElse(null))
                .orElse(null);
    }

    // ─── Default Generation ──────────────────────────────────────────────────────

    private static void generateDefaultTree() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray nodes = new JsonArray();

        // Warrior tree
        nodes.add(makeNode("warrior_start", "Path of the Warrior", "Start your journey as a Warrior.",
                0, 0, List.of("warrior_physical_1", "warrior_health_1"), Map.of(), "diamond"));
        nodes.add(makeNode("warrior_physical_1", "Physical Conditioning", "+5% Physical Extra Damage",
                1, -1, List.of("warrior_start", "warrior_might"),
                Map.of("cranks-dungeons:physical_extra_percent", 0.05), null));
        nodes.add(makeNode("warrior_health_1", "Vitality", "+10 Max Health",
                1, 1, List.of("warrior_start", "warrior_might"),
                Map.of("minecraft:max_health", 10.0), null));
        nodes.add(makeNode("warrior_might", "Warrior's Might", "+2 Armor",
                2, 0, List.of("warrior_physical_1", "warrior_health_1"),
                Map.of("minecraft:armor", 2.0), "circle"));

        // Ranger tree
        nodes.add(makeNode("ranger_start", "Path of the Ranger", "Start your journey as a Ranger.",
                10, 0, List.of("ranger_speed_1", "ranger_evasion_1"), Map.of(), "diamond"));
        nodes.add(makeNode("ranger_speed_1", "Swiftness", "+5% Movement Speed",
                9, -1, List.of("ranger_start", "ranger_precision"),
                Map.of("minecraft:movement_speed", 0.05), null));
        nodes.add(makeNode("ranger_evasion_1", "Agility", "+20 Evasion Rating",
                9, 1, List.of("ranger_start", "ranger_precision"),
                Map.of("cranks-dungeons:evasion_rating", 20.0), null));
        nodes.add(makeNode("ranger_precision", "Deadly Aim", "+5% Crit Chance",
                8, 0, List.of("ranger_speed_1", "ranger_evasion_1"),
                Map.of("cranks-dungeons:crit_chance", 0.05), "circle"));

        // Mercenary tree
        nodes.add(makeNode("mercenary_start", "Path of the Mercenary", "Start your journey as a Mercenary.",
                5, -10, List.of("merc_utility_1", "merc_combat_1"), Map.of(), "diamond"));
        nodes.add(makeNode("merc_utility_1", "Opportunist", "+5% Experience Bonus",
                4, -9, List.of("mercenary_start", "merc_ruthless"),
                Map.of("cranks-dungeons:experience_bonus", 0.05), null));
        nodes.add(makeNode("merc_combat_1", "Dirty Tactics", "+5% Bleed Chance",
                6, -9, List.of("mercenary_start", "merc_ruthless"),
                Map.of("cranks-dungeons:chance_to_bleed", 0.05), null));
        nodes.add(makeNode("merc_ruthless", "Ruthless", "+10% Crit Multiplier",
                5, -8, List.of("merc_utility_1", "merc_combat_1"),
                Map.of("cranks-dungeons:crit_multiplier", 0.10), "circle"));

        root.add("nodes", nodes);
        Files.writeString(TREE_FILE, GSON.toJson(root));
    }

    private static void generateDefaultClasses() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray classes = new JsonArray();

        classes.add(makeClass("warrior", "Warrior", "warrior_start"));
        classes.add(makeClass("ranger", "Ranger", "ranger_start"));
        classes.add(makeClass("mercenary", "Mercenary", "mercenary_start"));

        root.add("classes", classes);
        Files.writeString(CLASSES_FILE, GSON.toJson(root));
    }

    // ─── JSON Helpers ───────────────────────────────────────────────────────────

    private static JsonObject makeNode(String id, String name, String desc, int gx, int gy,
                                       List<String> connections, Map<String, Double> modifiers, String shape) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("description", desc);
        obj.addProperty("gridX", gx);
        obj.addProperty("gridY", gy);
        obj.addProperty("shape", shape);

        JsonArray conns = new JsonArray();
        connections.forEach(conns::add);
        obj.add("connections", conns);

        JsonObject mods = new JsonObject();
        modifiers.forEach(mods::addProperty);
        obj.add("modifiers", mods);

        return obj;
    }

    private static JsonObject makeClass(String id, String name, String startNode) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("startNode", startNode);
        return obj;
    }
}
