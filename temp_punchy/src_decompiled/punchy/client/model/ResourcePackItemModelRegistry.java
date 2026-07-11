/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1306
 *  net.minecraft.class_2350
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  org.joml.Vector3f
 */
package punchy.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1306;
import net.minecraft.class_2350;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.joml.Vector3f;
import punchy.client.model.ItemModelDefinition;
import punchy.client.model.ResourcePackGeoRegistry;

public final class ResourcePackItemModelRegistry {
    private static final Map<class_2960, ItemModelDefinition> MODELS = new HashMap<class_2960, ItemModelDefinition>();

    private ResourcePackItemModelRegistry() {
    }

    public static void load(class_3300 manager) {
        MODELS.clear();
        if (manager == null) {
            return;
        }
        for (class_2960 modelId : ResourcePackGeoRegistry.getMappedModelIds()) {
            if (modelId == null) continue;
            try {
                class_3298 resource = manager.getResourceOrThrow(modelId);
                JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(resource.method_14482())).getAsJsonObject();
                ItemModelDefinition model = ResourcePackItemModelRegistry.parseModel(modelId, json);
                if (model == null) continue;
                MODELS.put(modelId, model);
            }
            catch (Exception exception) {}
        }
    }

    public static ItemModelDefinition getModel(class_2960 modelId) {
        return modelId == null ? null : MODELS.get(modelId);
    }

    private static ItemModelDefinition parseModel(class_2960 modelId, JsonObject json) {
        JsonArray texSize;
        if (modelId == null || json == null || !json.has("elements")) {
            return null;
        }
        int texWidth = 16;
        int texHeight = 16;
        if (json.has("texture_size") && (texSize = json.getAsJsonArray("texture_size")) != null && texSize.size() >= 2) {
            texWidth = texSize.get(0).getAsInt();
            texHeight = texSize.get(1).getAsInt();
        }
        HashMap<String, String> textures = new HashMap<String, String>();
        if (json.has("textures")) {
            JsonObject texturesObj = json.getAsJsonObject("textures");
            for (Map.Entry entry : texturesObj.entrySet()) {
                if (!((JsonElement)entry.getValue()).isJsonPrimitive()) continue;
                textures.put((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
            }
        }
        HashMap<Integer, String> elementParents = new HashMap<Integer, String>();
        HashMap<String, Vector3f> groupPivots = new HashMap<String, Vector3f>();
        HashMap<String, Vector3f> groupRotations = new HashMap<String, Vector3f>();
        HashMap<String, String> groupParents = new HashMap<String, String>();
        LinkedHashMap<String, ItemModelDefinition.BoneDef> bones = new LinkedHashMap<String, ItemModelDefinition.BoneDef>();
        HashMap<String, String> parentByBone = new HashMap<String, String>();
        HashSet<String> usedNames = new HashSet<String>();
        if (json.has("groups")) {
            ResourcePackItemModelRegistry.parseGroups(json.getAsJsonArray("groups"), null, groupPivots, groupRotations, groupParents, elementParents);
            for (Map.Entry entry : groupPivots.entrySet()) {
                String name = (String)entry.getKey();
                String parent = (String)groupParents.get(name);
                Vector3f rot = groupRotations.getOrDefault(name, new Vector3f());
                bones.put(name, new ItemModelDefinition.BoneDef(name, parent, (Vector3f)entry.getValue(), rot));
                parentByBone.put(name, parent);
                usedNames.add(name);
            }
        }
        JsonArray elements = json.getAsJsonArray("elements");
        ArrayList<ItemModelDefinition.Element> modelElements = new ArrayList<ItemModelDefinition.Element>();
        if (elements != null) {
            int index = 0;
            for (JsonElement el : elements) {
                JsonObject elem = el.getAsJsonObject();
                String rawName = elem.has("name") ? elem.get("name").getAsString() : "cube_" + index;
                String name = ResourcePackItemModelRegistry.uniqueName(rawName, usedNames, index);
                Vector3f from = ResourcePackItemModelRegistry.readVec3(elem, "from", new Vector3f(0.0f, 0.0f, 0.0f));
                Vector3f to = ResourcePackItemModelRegistry.readVec3(elem, "to", new Vector3f(0.0f, 0.0f, 0.0f));
                Vector3f pivot = new Vector3f((from.x + to.x) * 0.5f, (from.y + to.y) * 0.5f, (from.z + to.z) * 0.5f);
                Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
                if (elem.has("rotation")) {
                    String axis;
                    JsonObject rot = elem.getAsJsonObject("rotation");
                    float angle = rot.has("angle") ? rot.get("angle").getAsFloat() : 0.0f;
                    String string = axis = rot.has("axis") ? rot.get("axis").getAsString() : "y";
                    if ("x".equals(axis)) {
                        rotation.x = angle;
                    }
                    if ("y".equals(axis)) {
                        rotation.y = angle;
                    }
                    if ("z".equals(axis)) {
                        rotation.z = angle;
                    }
                    if (rot.has("origin")) {
                        pivot = ResourcePackItemModelRegistry.readVec3(rot, "origin", pivot);
                    }
                }
                String parent = (String)elementParents.get(index);
                bones.put(name, new ItemModelDefinition.BoneDef(name, parent, pivot, rotation));
                parentByBone.put(name, parent);
                usedNames.add(name);
                Vector3f localFrom = new Vector3f(from.x - pivot.x, from.y - pivot.y, from.z - pivot.z);
                Vector3f localTo = new Vector3f(to.x - pivot.x, to.y - pivot.y, to.z - pivot.z);
                EnumMap<class_2350, ItemModelDefinition.Face> faces = ResourcePackItemModelRegistry.parseFaces(elem.getAsJsonObject("faces"));
                int lightEmission = 0;
                if (elem.has("light_emission")) {
                    lightEmission = elem.get("light_emission").getAsInt();
                    if (lightEmission < 0) {
                        lightEmission = 0;
                    } else if (lightEmission > 15) {
                        lightEmission = 15;
                    }
                }
                boolean worldLight = elem.has("world_light") && elem.get("world_light").getAsBoolean();
                modelElements.add(new ItemModelDefinition.Element(name, localFrom, localTo, faces, lightEmission, worldLight));
                ++index;
            }
        }
        ItemModelDefinition model = new ItemModelDefinition(modelId.method_12836(), texWidth, texHeight, textures, modelElements, bones, parentByBone, ItemModelDefinition.buildPaths(parentByBone));
        ResourcePackItemModelRegistry.parseDisplay(json, model);
        return model;
    }

    private static void parseGroups(JsonArray groups, String parentName, Map<String, Vector3f> groupPivots, Map<String, Vector3f> groupRotations, Map<String, String> groupParents, Map<Integer, String> elementParents) {
        if (groups == null) {
            return;
        }
        for (JsonElement groupEl : groups) {
            JsonObject group;
            if (!groupEl.isJsonObject() || !(group = groupEl.getAsJsonObject()).has("name")) continue;
            String name = group.get("name").getAsString();
            Vector3f origin = ResourcePackItemModelRegistry.readVec3(group, "origin", new Vector3f(0.0f, 0.0f, 0.0f));
            Vector3f rotation = ResourcePackItemModelRegistry.readVec3(group, "rotation", new Vector3f(0.0f, 0.0f, 0.0f));
            groupPivots.put(name, origin);
            groupRotations.put(name, rotation);
            if (parentName != null) {
                groupParents.put(name, parentName);
            }
            if (!group.has("children")) continue;
            JsonArray children = group.getAsJsonArray("children");
            for (JsonElement child : children) {
                if (child.isJsonPrimitive() && child.getAsJsonPrimitive().isNumber()) {
                    elementParents.put(child.getAsInt(), name);
                    continue;
                }
                if (!child.isJsonObject()) continue;
                JsonArray nested = new JsonArray();
                nested.add(child);
                ResourcePackItemModelRegistry.parseGroups(nested, name, groupPivots, groupRotations, groupParents, elementParents);
            }
        }
    }

    private static EnumMap<class_2350, ItemModelDefinition.Face> parseFaces(JsonObject facesObj) {
        EnumMap<class_2350, ItemModelDefinition.Face> faces = new EnumMap<class_2350, ItemModelDefinition.Face>(class_2350.class);
        if (facesObj == null) {
            return faces;
        }
        for (Map.Entry entry : facesObj.entrySet()) {
            JsonArray uv;
            JsonObject faceObj;
            class_2350 dir = ResourcePackItemModelRegistry.parseDirection((String)entry.getKey());
            if (dir == null || !((JsonElement)entry.getValue()).isJsonObject() || !(faceObj = ((JsonElement)entry.getValue()).getAsJsonObject()).has("uv") || !faceObj.has("texture") || (uv = faceObj.getAsJsonArray("uv")) == null || uv.size() < 4) continue;
            float u0 = uv.get(0).getAsFloat();
            float v0 = uv.get(1).getAsFloat();
            float u1 = uv.get(2).getAsFloat();
            float v1 = uv.get(3).getAsFloat();
            int rotation = faceObj.has("rotation") ? faceObj.get("rotation").getAsInt() : 0;
            String texture = faceObj.get("texture").getAsString();
            faces.put(dir, new ItemModelDefinition.Face(u0, v0, u1, v1, rotation, texture));
        }
        return faces;
    }

    private static class_2350 parseDirection(String name) {
        if (name == null) {
            return null;
        }
        return switch (name.toLowerCase()) {
            case "north" -> class_2350.field_11043;
            case "south" -> class_2350.field_11035;
            case "east" -> class_2350.field_11034;
            case "west" -> class_2350.field_11039;
            case "up" -> class_2350.field_11036;
            case "down" -> class_2350.field_11033;
            default -> null;
        };
    }

    private static Vector3f readVec3(JsonObject obj, String key, Vector3f fallback) {
        if (!obj.has(key)) {
            return fallback;
        }
        JsonArray arr = obj.getAsJsonArray(key);
        if (arr == null || arr.size() < 3) {
            return fallback;
        }
        return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
    }

    private static void parseDisplay(JsonObject json, ItemModelDefinition model) {
        if (json == null || model == null || !json.has("display")) {
            return;
        }
        JsonObject display = json.getAsJsonObject("display");
        ResourcePackItemModelRegistry.parseDisplayEntry(display, "thirdperson_righthand", class_1306.field_6183, model);
        ResourcePackItemModelRegistry.parseDisplayEntry(display, "thirdperson_lefthand", class_1306.field_6182, model);
    }

    private static void parseDisplayEntry(JsonObject display, String key, class_1306 arm, ItemModelDefinition model) {
        if (!display.has(key)) {
            return;
        }
        JsonObject entry = display.getAsJsonObject(key);
        Vector3f rot = ResourcePackItemModelRegistry.readVec3(entry, "rotation", new Vector3f(0.0f, 0.0f, 0.0f));
        Vector3f trans = ResourcePackItemModelRegistry.readVec3(entry, "translation", new Vector3f(0.0f, 0.0f, 0.0f));
        Vector3f scale = ResourcePackItemModelRegistry.readVec3(entry, "scale", new Vector3f(1.0f, 1.0f, 1.0f));
        model.setDisplayTransform(arm, new ItemModelDefinition.DisplayTransform(rot, trans, scale));
    }

    private static String uniqueName(String name, Set<String> used, int index) {
        if (name == null || ((String)name).isBlank()) {
            name = "cube_" + index;
        }
        if (!used.contains(name)) {
            return name;
        }
        String candidate = (String)name + "_" + index;
        if (!used.contains(candidate)) {
            return candidate;
        }
        candidate = "cube_" + index;
        used.add(candidate);
        return candidate;
    }
}

