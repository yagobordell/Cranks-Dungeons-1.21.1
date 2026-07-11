/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_7923
 *  net.minecraft.class_804
 *  net.minecraft.class_811
 *  org.joml.Vector3f
 */
package punchy.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_7923;
import net.minecraft.class_804;
import net.minecraft.class_811;
import org.joml.Vector3f;

public final class ResourcePackItemTransformResolver {
    private static class_3300 cachedManager;
    private static final Map<class_2960, JsonObject> ITEM_DEFINITIONS;
    private static final Map<class_2960, JsonObject> MODEL_DEFINITIONS;

    private ResourcePackItemTransformResolver() {
    }

    public static class_804 resolve(class_1799 stack, class_811 context) {
        class_2960 itemId;
        if (stack == null || stack.method_7960() || context == null) {
            return null;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1478() == null) {
            return null;
        }
        class_3300 manager = mc.method_1478();
        if (manager != cachedManager) {
            cachedManager = manager;
            ITEM_DEFINITIONS.clear();
            MODEL_DEFINITIONS.clear();
        }
        if ((itemId = class_7923.field_41178.method_10221((Object)stack.method_7909())) == null) {
            return null;
        }
        class_2960 itemDefId = class_2960.method_60655((String)itemId.method_12836(), (String)("items/" + itemId.method_12832() + ".json"));
        JsonObject itemDef = ResourcePackItemTransformResolver.loadTopResourceJson(manager, itemDefId, ITEM_DEFINITIONS);
        if (itemDef == null || !itemDef.has("model") || !itemDef.get("model").isJsonObject()) {
            return null;
        }
        JsonObject modelNode = itemDef.getAsJsonObject("model");
        class_2960 modelId = ResourcePackItemTransformResolver.resolveModelId(modelNode, stack, context);
        if (modelId == null) {
            return null;
        }
        return ResourcePackItemTransformResolver.loadDisplayTransform(manager, modelId, context);
    }

    private static class_2960 resolveModelId(JsonObject node, class_1799 stack, class_811 context) {
        if (node == null) {
            return null;
        }
        String type = ResourcePackItemTransformResolver.getString(node, "type");
        if ("minecraft:model".equals(type)) {
            String model = ResourcePackItemTransformResolver.getString(node, "model");
            return model == null ? null : class_2960.method_12829((String)model);
        }
        if ("minecraft:condition".equals(type)) {
            String property = ResourcePackItemTransformResolver.getString(node, "property");
            if ("minecraft:using_item".equals(property)) {
                class_2960 onFalse = ResourcePackItemTransformResolver.resolveBranch(node, "on_false", stack, context);
                if (onFalse != null) {
                    return onFalse;
                }
                return ResourcePackItemTransformResolver.resolveBranch(node, "on_true", stack, context);
            }
            class_2960 onFalse = ResourcePackItemTransformResolver.resolveBranch(node, "on_false", stack, context);
            if (onFalse != null) {
                return onFalse;
            }
            return ResourcePackItemTransformResolver.resolveBranch(node, "on_true", stack, context);
        }
        if ("minecraft:range_dispatch".equals(type)) {
            JsonArray entries = node.getAsJsonArray("entries");
            if (entries != null) {
                for (JsonElement entryEl : entries) {
                    class_2960 resolved;
                    JsonObject entry;
                    if (!entryEl.isJsonObject() || !(entry = entryEl.getAsJsonObject()).has("model") || !entry.get("model").isJsonObject() || (resolved = ResourcePackItemTransformResolver.resolveModelId(entry.getAsJsonObject("model"), stack, context)) == null) continue;
                    return resolved;
                }
            }
            return ResourcePackItemTransformResolver.resolveFallback(node, stack, context);
        }
        if ("minecraft:select".equals(type)) {
            String property = ResourcePackItemTransformResolver.getString(node, "property");
            if (property == null) {
                return ResourcePackItemTransformResolver.resolveFallback(node, stack, context);
            }
            if ("minecraft:display_context".equals(property)) {
                String contextName = ResourcePackItemTransformResolver.toDisplayContextName(context);
                JsonArray cases = node.getAsJsonArray("cases");
                if (cases != null) {
                    for (JsonElement caseEl : cases) {
                        JsonObject caseObj;
                        if (!caseEl.isJsonObject() || !ResourcePackItemTransformResolver.matchesDisplayContext((caseObj = caseEl.getAsJsonObject()).get("when"), contextName) || !caseObj.has("model") || !caseObj.get("model").isJsonObject()) continue;
                        return ResourcePackItemTransformResolver.resolveModelId(caseObj.getAsJsonObject("model"), stack, context);
                    }
                }
                return ResourcePackItemTransformResolver.resolveFallback(node, stack, context);
            }
            if ("minecraft:component".equals(property)) {
                return ResourcePackItemTransformResolver.resolveFallback(node, stack, context);
            }
        }
        return ResourcePackItemTransformResolver.resolveFallback(node, stack, context);
    }

    private static class_2960 resolveFallback(JsonObject node, class_1799 stack, class_811 context) {
        if (node == null || !node.has("fallback") || !node.get("fallback").isJsonObject()) {
            return null;
        }
        return ResourcePackItemTransformResolver.resolveModelId(node.getAsJsonObject("fallback"), stack, context);
    }

    private static class_2960 resolveBranch(JsonObject node, String branchKey, class_1799 stack, class_811 context) {
        if (node == null || branchKey == null || !node.has(branchKey) || !node.get(branchKey).isJsonObject()) {
            return null;
        }
        return ResourcePackItemTransformResolver.resolveModelId(node.getAsJsonObject(branchKey), stack, context);
    }

    private static boolean matchesDisplayContext(JsonElement whenElement, String contextName) {
        if (whenElement == null || contextName == null) {
            return false;
        }
        if (whenElement.isJsonPrimitive()) {
            return contextName.equals(whenElement.getAsString());
        }
        if (whenElement.isJsonArray()) {
            for (JsonElement element : whenElement.getAsJsonArray()) {
                if (element == null || !element.isJsonPrimitive() || !contextName.equals(element.getAsString())) continue;
                return true;
            }
        }
        return false;
    }

    private static String toDisplayContextName(class_811 context) {
        return switch (context) {
            case class_811.field_4321 -> "firstperson_lefthand";
            case class_811.field_4322 -> "firstperson_righthand";
            case class_811.field_4323 -> "thirdperson_lefthand";
            case class_811.field_4320 -> "thirdperson_righthand";
            case class_811.field_4316 -> "head";
            case class_811.field_4317 -> "gui";
            case class_811.field_4318 -> "ground";
            case class_811.field_4319 -> "fixed";
            default -> context.name().toLowerCase(Locale.ROOT);
        };
    }

    private static class_804 loadDisplayTransform(class_3300 manager, class_2960 modelId, class_811 context) {
        class_2960 modelResId = class_2960.method_60655((String)modelId.method_12836(), (String)("models/" + modelId.method_12832() + ".json"));
        JsonObject modelJson = ResourcePackItemTransformResolver.loadTopResourceJson(manager, modelResId, MODEL_DEFINITIONS);
        if (modelJson == null) {
            return null;
        }
        class_804 direct = ResourcePackItemTransformResolver.parseDisplayTransform(modelJson, context);
        if (direct != null) {
            return direct;
        }
        String parent = ResourcePackItemTransformResolver.getString(modelJson, "parent");
        if (parent == null || parent.isBlank()) {
            return null;
        }
        class_2960 parentId = class_2960.method_12829((String)parent);
        if (parentId == null || parentId.equals((Object)modelId)) {
            return null;
        }
        return ResourcePackItemTransformResolver.loadDisplayTransform(manager, parentId, context);
    }

    private static class_804 parseDisplayTransform(JsonObject modelJson, class_811 context) {
        String key;
        if (modelJson == null || context == null || !modelJson.has("display") || !modelJson.get("display").isJsonObject()) {
            return null;
        }
        JsonObject display = modelJson.getAsJsonObject("display");
        if (!display.has(key = ResourcePackItemTransformResolver.toDisplayContextName(context)) || !display.get(key).isJsonObject()) {
            return null;
        }
        JsonObject entry = display.getAsJsonObject(key);
        Vector3f rotation = ResourcePackItemTransformResolver.readVec3(entry, "rotation", new Vector3f());
        Vector3f translation = ResourcePackItemTransformResolver.readVec3(entry, "translation", new Vector3f());
        Vector3f scale = ResourcePackItemTransformResolver.readVec3(entry, "scale", new Vector3f(1.0f, 1.0f, 1.0f));
        return new class_804(rotation, translation, scale);
    }

    private static JsonObject loadTopResourceJson(class_3300 manager, class_2960 id, Map<class_2960, JsonObject> cache) {
        JsonObject jsonObject;
        if (manager == null || id == null) {
            return null;
        }
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        List stack = manager.method_14489(id);
        if (stack == null || stack.isEmpty()) {
            cache.put(id, null);
            return null;
        }
        class_3298 resource = (class_3298)stack.get(stack.size() - 1);
        InputStreamReader reader = new InputStreamReader(resource.method_14482());
        try {
            JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            cache.put(id, json);
            jsonObject = json;
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception ignored) {
                cache.put(id, null);
                return null;
            }
        }
        reader.close();
        return jsonObject;
    }

    private static Vector3f readVec3(JsonObject obj, String key, Vector3f fallback) {
        if (obj == null || key == null || !obj.has(key) || !obj.get(key).isJsonArray()) {
            return fallback;
        }
        JsonArray array = obj.getAsJsonArray(key);
        if (array.size() < 3) {
            return fallback;
        }
        return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private static String getString(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        return obj.get(key).getAsString();
    }

    static {
        ITEM_DEFINITIONS = new HashMap<class_2960, JsonObject>();
        MODEL_DEFINITIONS = new HashMap<class_2960, JsonObject>();
    }
}

