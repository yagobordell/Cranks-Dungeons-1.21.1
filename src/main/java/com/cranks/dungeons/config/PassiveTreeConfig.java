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

    public record ClassDefinition(String id, String name, String subtitle, String description, String recommendedStats, String color, String startNode) {}

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
        Set<String> seenIds = new HashSet<>();

        for (JsonElement el : nodesArr) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("id").getAsString().trim();
            
            if (seenIds.contains(id)) {
                CranksDungeons.LOGGER.error("[PassiveTree] CRITICAL ERROR: Duplicate Node ID '{}' found at gridX: {}, gridY: {}. This node will be IGNORED. Please fix your passive_tree.json!", 
                    id, obj.get("gridX").getAsInt(), obj.get("gridY").getAsInt());
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
                    } else {
                        com.cranks.dungeons.CranksDungeons.LOGGER.warn("[PassiveTree] Unknown attribute '{}' in node '{}'", attrId, id);
                    }
                }
            }

            // Attribute Selection Fields
            boolean isAttributeSelection = obj.has("is_attribute_selection") && obj.get("is_attribute_selection").getAsBoolean();
            double attributeSelectionAmount = obj.has("attribute_selection_amount") ? obj.get("attribute_selection_amount").getAsDouble() : 0.0;

            // Determine Icon (Priority: JSON Override -> PassiveIconManager)
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

    private static List<ClassDefinition> parseClasses() throws IOException {
        String json = Files.readString(CLASSES_FILE);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray classesArr = root.getAsJsonArray("classes");

        List<ClassDefinition> classes = new ArrayList<>();
        for (JsonElement el : classesArr) {
            JsonObject obj = el.getAsJsonObject();
            classes.add(new ClassDefinition(
                    obj.get("id").getAsString().trim(),
                    obj.get("name").getAsString(),
                    obj.has("subtitle") ? obj.get("subtitle").getAsString() : "No subtitle provided.",
                    obj.has("description") ? obj.get("description").getAsString() : "No description provided.",
                    obj.has("recommendedStats") ? obj.get("recommendedStats").getAsString() : "No recommendations provided.",
                    obj.has("color") ? obj.get("color").getAsString() : "#000000",
                    obj.get("startNode").getAsString().trim()
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
                0, 0, List.of("warrior_physical_1", "warrior_health_1"), Map.of(), "diamond", false, 0.0));
        nodes.add(makeNode("warrior_physical_1", "Physical Conditioning", "+5% Physical Extra Damage",
                1, -1, List.of("warrior_start", "warrior_might"),
                Map.of("cranks-dungeons:physical_extra_percent", 0.05), null, false, 0.0));
        nodes.add(makeNode("warrior_health_1", "Vitality", "+10 Max Health",
                1, 1, List.of("warrior_start", "warrior_might"),
                Map.of("minecraft:max_health", 10.0), null, false, 0.0));
        nodes.add(makeNode("warrior_might", "Warrior's Might", "+2 Armor",
                2, 0, List.of("warrior_physical_1", "warrior_health_1"),
                Map.of("minecraft:armor", 2.0), "circle", false, 0.0));

        // Ranger tree
        nodes.add(makeNode("ranger_start", "Path of the Ranger", "Start your journey as a Ranger.",
                10, 0, List.of("ranger_speed_1", "ranger_evasion_1"), Map.of(), "diamond", false, 0.0));
        nodes.add(makeNode("ranger_speed_1", "Swiftness", "+5% Movement Speed",
                9, -1, List.of("ranger_start", "ranger_precision"),
                Map.of("minecraft:movement_speed", 0.05), null, false, 0.0));
        nodes.add(makeNode("ranger_evasion_1", "Agility", "+20 Evasion Rating",
                9, 1, List.of("ranger_start", "ranger_precision"),
                Map.of("cranks-dungeons:evasion_rating", 20.0), null, false, 0.0));
        nodes.add(makeNode("ranger_precision", "Deadly Aim", "+5% Crit Chance",
                8, 0, List.of("ranger_speed_1", "ranger_evasion_1"),
                Map.of("cranks-dungeons:crit_chance", 0.05), "circle", false, 0.0));

        // Mercenary tree
        nodes.add(makeNode("mercenary_start", "Path of the Mercenary", "Start your journey as a Mercenary.",
                5, -10, List.of("merc_utility_1", "merc_combat_1"), Map.of(), "diamond", false, 0.0));
        nodes.add(makeNode("merc_utility_1", "Opportunist", "+5% Experience Bonus",
                4, -9, List.of("mercenary_start", "merc_ruthless"),
                Map.of("cranks-dungeons:experience_bonus", 0.05), null, false, 0.0));
        nodes.add(makeNode("merc_combat_1", "Dirty Tactics", "+5% Bleed Chance",
                6, -9, List.of("mercenary_start", "merc_ruthless"),
                Map.of("cranks-dungeons:chance_to_bleed", 0.05), null, false, 0.0));
        nodes.add(makeNode("merc_ruthless", "Ruthless", "+10% Crit Multiplier",
                5, -8, List.of("merc_utility_1", "merc_combat_1"),
                Map.of("cranks-dungeons:crit_multiplier", 0.10), "circle", false, 0.0));

        root.add("nodes", nodes);
        Files.writeString(TREE_FILE, GSON.toJson(root));
    }

    private static void generateDefaultClasses() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray classes = new JsonArray();

        classes.add(makeClass("warrior", "Warrior", 
            "Heavy hitting slow tank, melee.",
            "A master of brute force and resilience. Warriors excel at close-quarters combat, trading powerful blows with their enemies while shrugging off damage with heavy armor and high health.", 
            "Focus on Max Health, Armor, and Physical Damage. Strength and Resilience are your best friends to survive the front lines while crushing foes.",
            "#AA0000",
            "warrior_start"));
        classes.add(makeClass("ranger", "Ranger", 
            "Squishy fast, normal damage, ranged.",
            "Quick, lethal, and precise. Rangers dominate from a distance, using their superior movement speed and evasion to stay out of reach while landing deadly critical strikes with bows.", 
            "Prioritize Attack Speed, Crit Chance, and Movement Speed. Stay agile and use Evasion to avoid taking hits, as your health pool is limited.",
            "#00AA00",
            "ranger_start"));
        classes.add(makeClass("mercenary", "Mercenary", 
            "Jack of all trades, both melee and ranged, decent tankiness and damage.",
            "A ruthless tactician who fights for profit. Mercenaries utilize dirty tactics and utility to gain the upper hand, specializing in bleeds, critical multipliers, and experience gains.", 
            "Balance between Physical Damage and Utility. Crit Multiplier and Bleed Chance are great for damage, while Experience Bonus helps you outscale your enemies.",
            "#00AAAA",
            "mercenary_start"));

        root.add("classes", classes);
        Files.writeString(CLASSES_FILE, GSON.toJson(root));
    }

    // ─── JSON Helpers ───────────────────────────────────────────────────────────

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

    private static JsonObject makeClass(String id, String name, String subtitle, String description, String recommendedStats, String color, String startNode) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("subtitle", subtitle);
        obj.addProperty("description", description);
        obj.addProperty("recommendedStats", recommendedStats);
        obj.addProperty("color", color);
        obj.addProperty("startNode", startNode);
        return obj;
    }

    // ─── Export ─────────────────────────────────────────────────────────────

    /**
     * Converts a collection of PassiveNodes back into a pretty-printed JSON string
     * that matches the passive_tree.json schema.
     */
    public static String exportNodesToJson(Collection<PassiveNode> nodes) {
        JsonObject root = new JsonObject();
        JsonArray nodesArr = new JsonArray();

        // Sort nodes by ID for a consistent, clean file
        List<PassiveNode> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator.comparing(PassiveNode::id));

        for (PassiveNode node : sorted) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", node.id());
            obj.addProperty("name", node.name());
            obj.addProperty("description", node.description());
            
            // Format to 3 decimal places to keep the JSON clean
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

            // Icon is only saved if it's explicitly overwritten from the default
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

    /**
     * Directly saves the provided nodes to config/cranks-dungeons/passive_tree.json.
     * Use with CAUTION as it overwrites the file.
     */
    public static void saveNodesToFile(Collection<PassiveNode> nodes) {
        try {
            String json = exportNodesToJson(nodes);
            Files.writeString(TREE_FILE, json);
            CranksDungeons.LOGGER.info("[PassiveTree] Successfully saved {} nodes to config.", nodes.size());
        } catch (IOException e) {
            CranksDungeons.LOGGER.error("[PassiveTree] FAILED to save nodes to file!", e);
        }
    }
}
