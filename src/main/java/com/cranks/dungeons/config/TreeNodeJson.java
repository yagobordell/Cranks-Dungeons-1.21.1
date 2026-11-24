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
import java.util.function.UnaryOperator;

final class TreeNodeJson {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private TreeNodeJson() {}

    static List<PassiveNode> parseTree(Path file, String treeName, Set<String> allowed, UnaryOperator<String> normalize) throws IOException {
        String json = Files.readString(file);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray nodesArr = root.getAsJsonArray("nodes");

        List<PassiveNode> nodes = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (JsonElement el : nodesArr) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.get("id").getAsString().trim();

            if (id.isEmpty() || !seenIds.add(id)) {
                CranksDungeons.LOGGER.warn("[{}] Ignoring blank or duplicate node ID '{}'.", treeName, id);
                continue;
            }

            String name = obj.get("name").getAsString();
            String description = "";
            if (obj.has("description")) {
                JsonElement descElement = obj.get("description");
                if (descElement.isJsonArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonElement el2 : descElement.getAsJsonArray()) {
                        if (sb.length() > 0) sb.append("\n");
                        sb.append(el2.getAsString());
                    }
                    description = sb.toString();
                } else {
                    description = descElement.getAsString();
                }
            }
            description = com.cranks.dungeons.stat.StatDescriptions.reviseNodeDescription(description);
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
                    String attrId = normalize.apply(entry.getKey());
                    double value = entry.getValue().getAsDouble();

                    Identifier key = Identifier.tryParse(attrId);
                    var attr = key == null ? null : Registries.ATTRIBUTE.getEntry(key).orElse(null);
                    if (!Double.isFinite(value) || attr == null
                            || !com.cranks.dungeons.stat.passive.TreeAttributeRules.supported(key.getNamespace(), key.getPath(), allowed)) {
                        CranksDungeons.LOGGER.warn("[{}] Ignoring unsupported modifier '{}' on node '{}'.",
                                treeName, attrId, id);
                        continue;
                    }
                    modifiers.put(attr, value);
                }
            }

            boolean isAttributeSelection = obj.has("is_attribute_selection") && obj.get("is_attribute_selection").getAsBoolean();
            double attributeSelectionAmount = obj.has("attribute_selection_amount") ? obj.get("attribute_selection_amount").getAsDouble() : 0.0;

            Identifier icon;
            if (obj.has("icon") && !obj.get("icon").getAsString().isEmpty()) {
                icon = Identifier.of(obj.get("icon").getAsString());
            } else {
                icon = com.cranks.dungeons.stat.passive.PassiveIconManager.getIcon(id, name, isAttributeSelection);
            }

            nodes.add(new PassiveNode(id, name, description, gridX, gridY, connections, modifiers, icon, shape, isAttributeSelection, attributeSelectionAmount));
        }

        return nodes;
    }

    static String exportNodesToJson(Collection<PassiveNode> nodes) {
        JsonObject root = new JsonObject();
        JsonArray nodesArr = new JsonArray();

        List<PassiveNode> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator.comparing(PassiveNode::id));

        for (PassiveNode node : sorted) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", node.id());
            obj.addProperty("name", node.name());
            if (node.description() != null && node.description().contains("\n")) {
                JsonArray descArray = new JsonArray();
                for (String line : node.description().split("\n")) {
                    descArray.add(line);
                }
                obj.add("description", descArray);
            } else {
                obj.addProperty("description", node.description());
            }

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

            Identifier defaultTitleIcon = com.cranks.dungeons.stat.passive.PassiveIconManager.getIcon(node.id(), node.name(), node.isAttributeSelection());
            boolean isCustomOverride = node.icon() != null && !node.icon().equals(defaultTitleIcon) && !node.icon().equals(com.cranks.dungeons.stat.passive.PassiveIconManager.PLACEHOLDER_ICON);

            if (node.isAttributeSelection() || isCustomOverride) {
                obj.addProperty("icon", node.icon() != null ? node.icon().toString() : "");
            } else {
                obj.addProperty("icon", "");
            }

            nodesArr.add(obj);
        }

        root.add("nodes", nodesArr);
        return GSON.toJson(root);
    }

}
