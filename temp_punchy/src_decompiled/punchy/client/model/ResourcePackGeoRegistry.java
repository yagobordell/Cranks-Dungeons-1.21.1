/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1890
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_5321
 *  net.minecraft.class_6880
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_7225$class_7226
 *  net.minecraft.class_7923
 *  net.minecraft.class_7924
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7225;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.joml.Vector3f;
import punchy.client.model.BedrockBone;
import punchy.client.model.BedrockGeoModel;

public final class ResourcePackGeoRegistry {
    private static final Map<class_2960, BedrockGeoModel> GEO_MODELS = new HashMap<class_2960, BedrockGeoModel>();
    private static final Map<class_2960, BedrockGeoModel> MODEL_BONES = new HashMap<class_2960, BedrockGeoModel>();
    private static final Map<class_2960, class_2960> TARGET_TO_GEO = new HashMap<class_2960, class_2960>();
    private static final Map<class_2960, class_2960> TARGET_TO_MODEL = new HashMap<class_2960, class_2960>();
    private static final Map<class_2960, List<ModelCondition>> TARGET_TO_CONDITIONAL_MODEL = new HashMap<class_2960, List<ModelCondition>>();

    private ResourcePackGeoRegistry() {
    }

    public static void load(class_3300 manager) {
        GEO_MODELS.clear();
        MODEL_BONES.clear();
        TARGET_TO_GEO.clear();
        TARGET_TO_MODEL.clear();
        TARGET_TO_CONDITIONAL_MODEL.clear();
        if (manager == null) {
            return;
        }
        ResourcePackGeoRegistry.loadGeoModels(manager);
        ResourcePackGeoRegistry.loadModelBones(manager);
        ResourcePackGeoRegistry.loadMappings(manager);
    }

    public static BedrockGeoModel getGeoModel(class_2960 geoId) {
        return geoId != null ? GEO_MODELS.get(geoId) : null;
    }

    public static class_2960 getGeoForTarget(class_2960 targetId) {
        return targetId != null ? TARGET_TO_GEO.get(targetId) : null;
    }

    public static class_2960 getModelForTarget(class_2960 targetId) {
        return targetId != null ? TARGET_TO_MODEL.get(targetId) : null;
    }

    public static class_2960 getModelForTarget(class_1799 stack, class_1657 player) {
        if (stack == null || stack.method_7960() || player == null) {
            return null;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId == null) {
            return null;
        }
        class_2960 targetId = class_2960.method_60655((String)itemId.method_12836(), (String)("item/" + itemId.method_12832()));
        List<ModelCondition> conditional = TARGET_TO_CONDITIONAL_MODEL.get(targetId);
        if (conditional != null && !conditional.isEmpty()) {
            for (ModelCondition entry : conditional) {
                if (entry == null || entry.modelId == null || entry.requiredEnchant != null && !ResourcePackGeoRegistry.hasEnchantment(player, stack, entry.requiredEnchant)) continue;
                return entry.modelId;
            }
        }
        return TARGET_TO_MODEL.get(targetId);
    }

    public static Collection<class_2960> getMappedModelIds() {
        ArrayList<class_2960> ids = new ArrayList<class_2960>(TARGET_TO_MODEL.values());
        for (List<ModelCondition> entries : TARGET_TO_CONDITIONAL_MODEL.values()) {
            if (entries == null) continue;
            for (ModelCondition entry : entries) {
                if (entry == null || entry.modelId == null) continue;
                ids.add(entry.modelId);
            }
        }
        return Collections.unmodifiableCollection(ids);
    }

    public static BedrockGeoModel getBonesForTarget(class_2960 targetId) {
        BedrockGeoModel geo;
        class_2960 geoId = ResourcePackGeoRegistry.getGeoForTarget(targetId);
        if (geoId != null && (geo = ResourcePackGeoRegistry.getGeoModel(geoId)) != null) {
            return geo;
        }
        class_2960 modelId = ResourcePackGeoRegistry.getModelForTarget(targetId);
        return modelId != null ? MODEL_BONES.get(modelId) : null;
    }

    private static void loadGeoModels(class_3300 manager) {
        Map geos = manager.method_14488("geo", loc -> loc.method_12832().startsWith("geo/") && loc.method_12832().endsWith(".geo.json"));
        for (Map.Entry entry : geos.entrySet()) {
            class_2960 id = (class_2960)entry.getKey();
            try {
                JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(((class_3298)entry.getValue()).method_14482())).getAsJsonObject();
                BedrockGeoModel model = ResourcePackGeoRegistry.parseGeo(json);
                if (model == null) continue;
                GEO_MODELS.put(id, model);
            }
            catch (Exception exception) {}
        }
    }

    private static void loadModelBones(class_3300 manager) {
        Map models = manager.method_14488("models", loc -> loc.method_12832().startsWith("models/") && loc.method_12832().endsWith(".json"));
        for (Map.Entry entry : models.entrySet()) {
            class_2960 id = (class_2960)entry.getKey();
            try {
                JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(((class_3298)entry.getValue()).method_14482())).getAsJsonObject();
                BedrockGeoModel model = ResourcePackGeoRegistry.parseModelAsBones(json);
                if (model == null) continue;
                MODEL_BONES.put(id, model);
            }
            catch (Exception exception) {}
        }
    }

    private static BedrockGeoModel parseGeo(JsonObject json) {
        if (json == null) {
            return null;
        }
        JsonArray geos = json.getAsJsonArray("minecraft:geometry");
        if (geos == null || geos.isEmpty()) {
            return null;
        }
        JsonObject geo = geos.get(0).getAsJsonObject();
        JsonArray bones = geo.getAsJsonArray("bones");
        if (bones == null) {
            return null;
        }
        HashMap<String, float[]> pivotByName = new HashMap<String, float[]>();
        for (JsonElement boneEl : bones) {
            JsonObject bone = boneEl.getAsJsonObject();
            String name = bone.get("name").getAsString();
            float px = 0.0f;
            float py = 0.0f;
            float pz = 0.0f;
            if (bone.has("pivot")) {
                JsonArray pivot = bone.getAsJsonArray("pivot");
                px = pivot.get(0).getAsFloat();
                py = pivot.get(1).getAsFloat();
                pz = pivot.get(2).getAsFloat();
            }
            pivotByName.put(name, new float[]{px, py, pz});
        }
        BedrockGeoModel model = new BedrockGeoModel();
        for (JsonElement boneEl : bones) {
            float[] pPivot;
            JsonObject bone = boneEl.getAsJsonObject();
            String name = bone.get("name").getAsString();
            String parentName = bone.has("parent") ? bone.get("parent").getAsString() : null;
            float px = 0.0f;
            float py = 0.0f;
            float pz = 0.0f;
            float rx = 0.0f;
            float ry = 0.0f;
            float rz = 0.0f;
            if (bone.has("pivot")) {
                JsonArray pivot = bone.getAsJsonArray("pivot");
                px = pivot.get(0).getAsFloat();
                py = pivot.get(1).getAsFloat();
                pz = pivot.get(2).getAsFloat();
            }
            if (bone.has("rotation")) {
                JsonArray rotation = bone.getAsJsonArray("rotation");
                rx = rotation.get(0).getAsFloat();
                ry = rotation.get(1).getAsFloat();
                rz = rotation.get(2).getAsFloat();
            }
            float localX = px;
            float localY = py;
            float localZ = pz;
            if (parentName != null && (pPivot = (float[])pivotByName.get(parentName)) != null) {
                localX = px - pPivot[0];
                localY = py - pPivot[1];
                localZ = pz - pPivot[2];
            }
            BedrockBone bedrockBone = new BedrockBone(name, parentName, new Vector3f(px, py, pz), new Vector3f(rx, ry, rz), new Vector3f(localX, localY, localZ));
            model.addBone(bedrockBone);
        }
        return model;
    }

    private static BedrockGeoModel parseModelAsBones(JsonObject json) {
        if (json == null || !json.has("elements") && !json.has("groups")) {
            return null;
        }
        JsonArray elements = json.getAsJsonArray("elements");
        JsonArray groups = json.getAsJsonArray("groups");
        HashMap<Integer, String> elementParents = new HashMap<Integer, String>();
        HashMap<String, Vector3f> groupPivots = new HashMap<String, Vector3f>();
        HashMap<String, String> groupParents = new HashMap<String, String>();
        BedrockGeoModel model = new BedrockGeoModel();
        if (groups != null) {
            ResourcePackGeoRegistry.parseGroups(groups, null, groupPivots, groupParents, elementParents, model);
        }
        int index = 0;
        if (elements == null) {
            return model;
        }
        for (JsonElement el : elements) {
            Vector3f parentPivot;
            Vector3f pivot;
            JsonObject elem = el.getAsJsonObject();
            String name = elem.has("name") ? elem.get("name").getAsString() : "cube_" + index;
            Vector3f from = ResourcePackGeoRegistry.readVec3(elem, "from", new Vector3f(0.0f, 0.0f, 0.0f));
            Vector3f to = ResourcePackGeoRegistry.readVec3(elem, "to", new Vector3f(0.0f, 0.0f, 0.0f));
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
                    JsonArray origin = rot.getAsJsonArray("origin");
                    pivot = new Vector3f(origin.get(0).getAsFloat(), origin.get(1).getAsFloat(), origin.get(2).getAsFloat());
                } else {
                    pivot = new Vector3f((from.x + to.x) * 0.5f, (from.y + to.y) * 0.5f, (from.z + to.z) * 0.5f);
                }
            } else {
                pivot = new Vector3f((from.x + to.x) * 0.5f, (from.y + to.y) * 0.5f, (from.z + to.z) * 0.5f);
            }
            String parent = (String)elementParents.get(index);
            Vector3f localPivot = pivot;
            if (parent != null && (parentPivot = (Vector3f)groupPivots.get(parent)) != null) {
                localPivot = new Vector3f(pivot.x - parentPivot.x, pivot.y - parentPivot.y, pivot.z - parentPivot.z);
            }
            BedrockBone bone = new BedrockBone(name, parent, pivot, rotation, localPivot);
            model.addBone(bone);
            ++index;
        }
        return model;
    }

    private static void parseGroups(JsonArray groups, String parentName, Map<String, Vector3f> groupPivots, Map<String, String> groupParents, Map<Integer, String> elementParents, BedrockGeoModel model) {
        for (JsonElement groupEl : groups) {
            Vector3f parentPivot;
            JsonObject group;
            if (!groupEl.isJsonObject() || !(group = groupEl.getAsJsonObject()).has("name")) continue;
            String name = group.get("name").getAsString();
            Vector3f origin = ResourcePackGeoRegistry.readVec3(group, "origin", new Vector3f(0.0f, 0.0f, 0.0f));
            Vector3f rotation = ResourcePackGeoRegistry.readVec3(group, "rotation", new Vector3f(0.0f, 0.0f, 0.0f));
            Vector3f localPivot = origin;
            if (parentName != null && (parentPivot = groupPivots.get(parentName)) != null) {
                localPivot = new Vector3f(origin.x - parentPivot.x, origin.y - parentPivot.y, origin.z - parentPivot.z);
            }
            groupPivots.put(name, origin);
            if (parentName != null) {
                groupParents.put(name, parentName);
            }
            model.addBone(new BedrockBone(name, parentName, origin, rotation, localPivot));
            if (!group.has("children")) continue;
            JsonArray children = group.getAsJsonArray("children");
            for (JsonElement childEl : children) {
                if (childEl.isJsonPrimitive() && childEl.getAsJsonPrimitive().isNumber()) {
                    elementParents.put(childEl.getAsInt(), name);
                    continue;
                }
                if (!childEl.isJsonObject()) continue;
                JsonArray nested = new JsonArray();
                nested.add(childEl);
                ResourcePackGeoRegistry.parseGroups(nested, name, groupPivots, groupParents, elementParents, model);
            }
        }
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

    private static void loadMappings(class_3300 manager) {
        Map maps = manager.method_14488("punchy", loc -> loc.method_12832().startsWith("punchy/") && loc.method_12832().endsWith("rp_geo_map.json"));
        for (Map.Entry entry : maps.entrySet()) {
            try {
                JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(((class_3298)entry.getValue()).method_14482())).getAsJsonObject();
                JsonArray mappings = json.getAsJsonArray("mappings");
                if (mappings == null) continue;
                for (JsonElement mapEl : mappings) {
                    class_2960 modelId;
                    class_2960 geoId;
                    class_2960 targetId;
                    JsonObject map = mapEl.getAsJsonObject();
                    if (!map.has("target") || (targetId = class_2960.method_12829((String)map.get("target").getAsString())) == null) continue;
                    if (map.has("geo") && (geoId = class_2960.method_12829((String)map.get("geo").getAsString())) != null) {
                        TARGET_TO_GEO.put(targetId, geoId);
                    }
                    if (!map.has("model") || (modelId = ResourcePackGeoRegistry.resolveModelId(map.get("model").getAsString())) == null) continue;
                    class_2960 requiredEnchant = null;
                    if (map.has("require_enchant")) {
                        requiredEnchant = class_2960.method_12829((String)map.get("require_enchant").getAsString());
                    }
                    if (requiredEnchant != null) {
                        TARGET_TO_CONDITIONAL_MODEL.computeIfAbsent(targetId, key -> new ArrayList()).add(new ModelCondition(modelId, requiredEnchant));
                        continue;
                    }
                    TARGET_TO_MODEL.put(targetId, modelId);
                }
            }
            catch (Exception exception) {
            }
        }
    }

    private static class_2960 resolveModelId(String modelId) {
        class_2960 raw = class_2960.method_12829((String)modelId);
        if (raw == null) {
            return null;
        }
        Object path = raw.method_12832();
        if (!((String)path).startsWith("models/")) {
            path = "models/" + (String)path;
        }
        if (!((String)path).endsWith(".json")) {
            path = (String)path + ".json";
        }
        return class_2960.method_60655((String)raw.method_12836(), (String)path);
    }

    private static boolean hasEnchantment(class_1657 player, class_1799 stack, class_2960 enchantId) {
        try {
            class_5321 key;
            if (player == null || stack == null || stack.method_7960() || enchantId == null) {
                return false;
            }
            class_7225.class_7226 registry = player.method_37908().method_30349().method_46762(class_7924.field_41265);
            class_6880.class_6883 holder = registry.method_46747(key = class_5321.method_29179((class_5321)class_7924.field_41265, (class_2960)enchantId));
            return class_1890.method_8225((class_6880)holder, (class_1799)stack) > 0;
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static final class ModelCondition {
        private final class_2960 modelId;
        private final class_2960 requiredEnchant;

        private ModelCondition(class_2960 modelId, class_2960 requiredEnchant) {
            this.modelId = modelId;
            this.requiredEnchant = requiredEnchant;
        }
    }
}

