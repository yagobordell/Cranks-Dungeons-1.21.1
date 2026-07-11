/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3283
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package punchy.client.animation.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3283;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;

public class PunchyAnimationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Punchy/AnimationLoader");
    public static final String DEFAULT_PACK_OPTION_ID = "punchy_default";
    private static final String BUILTIN_PACK_OPTION_ID = "pack:punchy:punchy";
    private static final List<class_2960> INTERNAL_ANIMATION_IDS = List.of(class_2960.method_60655((String)"punchy", (String)"animations/player_3_p.animation.json"), class_2960.method_60655((String)"minecraft", (String)"punchy/animations/player.animation.json"));

    public static AnimationClip load(class_3300 manager, class_2960 id, String animationNameInJson) {
        if (manager == null || id == null || animationNameInJson == null || animationNameInJson.isBlank()) {
            return null;
        }
        try {
            class_3298 resource = manager.getResourceOrThrow(id);
            Map<String, AnimationClip> clips = PunchyAnimationLoader.readClipsFromResource(resource);
            return clips.get(animationNameInJson.trim());
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static Map<String, AnimationClip> loadAll(class_3300 manager) {
        HashMap<String, AnimationClip> selected = new HashMap<String, AnimationClip>();
        Map<String, LinkedHashMap<String, AnimationClip>> variants = PunchyAnimationLoader.loadAllWithPackVariants(manager);
        for (Map.Entry<String, LinkedHashMap<String, AnimationClip>> entry : variants.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) continue;
            AnimationClip clip = entry.getValue().get(DEFAULT_PACK_OPTION_ID);
            if (clip == null) {
                clip = entry.getValue().values().iterator().next();
            }
            if (clip == null) continue;
            selected.put(entry.getKey(), AnimationClip.copyOf(clip));
        }
        return selected;
    }

    public static Map<String, LinkedHashMap<String, AnimationClip>> loadAllWithPackVariants(class_3300 manager) {
        HashMap<String, LinkedHashMap<String, AnimationClip>> result = new HashMap<String, LinkedHashMap<String, AnimationClip>>();
        if (manager == null) {
            return result;
        }
        List<AnimationResourceEntry> entries = PunchyAnimationLoader.collectAnimationResourceEntries(manager);
        HashMap<String, AnimationClip> selectedByPriority = new HashMap<String, AnimationClip>();
        for (AnimationResourceEntry animationResourceEntry : entries) {
            Map<String, AnimationClip> clips;
            if (animationResourceEntry == null || animationResourceEntry.resource == null || (clips = PunchyAnimationLoader.readClipsFromResource(animationResourceEntry.resource)).isEmpty()) continue;
            String optionId = PunchyAnimationLoader.optionIdForAnimationEntry(animationResourceEntry);
            for (Map.Entry<String, AnimationClip> entry : clips.entrySet()) {
                String clipName;
                if (entry == null || entry.getKey() == null || entry.getValue() == null || (clipName = entry.getKey().trim()).isBlank()) continue;
                LinkedHashMap variants = result.computeIfAbsent(clipName, ignored -> new LinkedHashMap());
                variants.putIfAbsent(optionId, AnimationClip.copyOf(entry.getValue()));
                selectedByPriority.putIfAbsent(clipName, AnimationClip.copyOf(entry.getValue()));
            }
        }
        for (Map.Entry entry : selectedByPriority.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            LinkedHashMap variants = result.computeIfAbsent((String)entry.getKey(), ignored -> new LinkedHashMap());
            LinkedHashMap<String, AnimationClip> ordered = new LinkedHashMap<String, AnimationClip>();
            ordered.put(DEFAULT_PACK_OPTION_ID, AnimationClip.copyOf((AnimationClip)entry.getValue()));
            for (Map.Entry<String, AnimationClip> entry2 : variants.entrySet()) {
                if (entry2 == null || entry2.getKey() == null || entry2.getValue() == null) continue;
                ordered.putIfAbsent(entry2.getKey(), AnimationClip.copyOf(entry2.getValue()));
            }
            result.put((String)entry.getKey(), ordered);
        }
        return result;
    }

    private static String optionIdForAnimationEntry(AnimationResourceEntry entry) {
        if (entry != null && entry.id != null && "punchy".equals(entry.id.method_12836()) && entry.id.method_12832() != null && entry.id.method_12832().startsWith("animations/")) {
            return BUILTIN_PACK_OPTION_ID;
        }
        return PunchyAnimationLoader.optionIdFromPackId(entry == null || entry.resource == null ? null : entry.resource.method_14480());
    }

    private static List<AnimationResourceEntry> collectAnimationResourceEntries(class_3300 manager) {
        Map<class_2960, Boolean> animationResources = PunchyAnimationLoader.discoverAnimationResourceIds(manager);
        if (animationResources.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Integer> packPriorities = PunchyAnimationLoader.resolvePackPriorities();
        int nextFallbackPriority = packPriorities.values().stream().mapToInt(Integer::intValue).min().orElse(0) - 1;
        int discoveryOrder = 0;
        ArrayList<AnimationResourceEntry> entries = new ArrayList<AnimationResourceEntry>();
        for (class_2960 id : animationResources.keySet()) {
            List<class_3298> stack = PunchyAnimationLoader.tryGetResourceStack(manager, id);
            if (stack != null && !stack.isEmpty()) {
                int stackOrder = 0;
                for (int i = stack.size() - 1; i >= 0; --i) {
                    class_3298 resource = stack.get(i);
                    if (resource == null) continue;
                    int priority = packPriorities.getOrDefault(resource.method_14480(), nextFallbackPriority--);
                    entries.add(new AnimationResourceEntry(id, resource, priority, discoveryOrder, stackOrder++));
                }
                ++discoveryOrder;
                continue;
            }
            try {
                class_3298 resource = manager.getResourceOrThrow(id);
                int priority = packPriorities.getOrDefault(resource.method_14480(), nextFallbackPriority--);
                entries.add(new AnimationResourceEntry(id, resource, priority, discoveryOrder, 0));
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++discoveryOrder;
        }
        entries.sort(Comparator.comparingInt(entry -> entry.packPriority).reversed().thenComparingInt(entry -> entry.discoveryOrder).thenComparingInt(entry -> entry.stackOrder).thenComparing(entry -> entry.id.toString()));
        return entries;
    }

    private static Map<class_2960, Boolean> discoverAnimationResourceIds(class_3300 manager) {
        LinkedHashMap<class_2960, Boolean> result = new LinkedHashMap<class_2960, Boolean>();
        if (manager == null) {
            return result;
        }
        PunchyAnimationLoader.collectAnimationResourceIds(manager, "animations", "punchy", "animations/", result);
        PunchyAnimationLoader.collectAnimationResourceIds(manager, "punchy/animations", "minecraft", "punchy/animations/", result);
        return result;
    }

    private static void collectAnimationResourceIds(class_3300 manager, String searchPath, String namespace, String prefix, Map<class_2960, Boolean> result) {
        Map found = manager.method_14488(searchPath, loc -> loc.method_12836().equals(namespace) && loc.method_12832().startsWith(prefix) && loc.method_12832().endsWith(".json"));
        for (class_2960 id : found.keySet()) {
            result.putIfAbsent(id, Boolean.TRUE);
        }
    }

    private static List<class_3298> tryGetResourceStack(class_3300 manager, class_2960 id) {
        if (manager == null || id == null) {
            return null;
        }
        try {
            List stack = manager.method_14489(id);
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            return new ArrayList<class_3298>(stack);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static Map<String, Integer> resolvePackPriorities() {
        HashMap<String, Integer> priorities = new HashMap<String, Integer>();
        List<String> selected = PunchyAnimationLoader.getSelectedResourcePackIdsHighToLow();
        int weight = selected.size();
        for (String packId : selected) {
            if (packId == null || packId.isBlank()) continue;
            priorities.putIfAbsent(packId, weight--);
        }
        return priorities;
    }

    private static List<String> getSelectedResourcePackIdsHighToLow() {
        class_310 client = class_310.method_1551();
        if (client == null) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<String>();
        try {
            class_3283 repository = client.method_1520();
            if (repository != null) {
                for (String packId : repository.method_29210()) {
                    if (packId == null || packId.isBlank()) continue;
                    result.add(packId);
                }
            }
        }
        catch (Exception repository) {
            // empty catch block
        }
        Collections.reverse(result);
        String builtinPackId = PunchyAnimationLoader.resolveActiveBuiltInPackId(client);
        if (builtinPackId != null && !builtinPackId.isBlank() && !result.contains(builtinPackId)) {
            result.add(0, builtinPackId);
        }
        return result;
    }

    private static String resolveActiveBuiltInPackId(class_310 client) {
        if (client == null || client.method_1478() == null) {
            return null;
        }
        try {
            class_2960 id = class_2960.method_60655((String)"minecraft", (String)"punchy/compat/default_tuning.json");
            return client.method_1478().method_14486(id).map(class_3298::method_14480).filter(packId -> packId != null && !packId.isBlank()).orElse(null);
        }
        catch (Exception exception) {
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Map<String, AnimationClip> readClipsFromResource(class_3298 resource) {
        HashMap<String, AnimationClip> clips = new HashMap<String, AnimationClip>();
        if (resource == null) {
            return clips;
        }
        try (InputStreamReader reader = new InputStreamReader(resource.method_14482());){
            JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            JsonObject animationsObj = json.getAsJsonObject("animations");
            if (animationsObj == null) {
                HashMap<String, AnimationClip> hashMap = clips;
                return hashMap;
            }
            Iterator iterator = animationsObj.entrySet().iterator();
            while (iterator.hasNext()) {
                AnimationClip clip;
                Map.Entry entry = (Map.Entry)iterator.next();
                if (entry == null || entry.getKey() == null || !((JsonElement)entry.getValue()).isJsonObject() || (clip = PunchyAnimationLoader.buildClip((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsJsonObject())) == null) continue;
                clips.put((String)entry.getKey(), clip);
            }
            return clips;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return clips;
    }

    private static BoneTrack parseBoneTrack(String boneName, JsonObject data, float animationLength, boolean stretchToLength) {
        KeyframeBuilder last;
        Float lastTime;
        String interpolation;
        KeyframeBuilder b;
        Iterator b2;
        BoneTrack track = new BoneTrack(boneName);
        TreeMap<Float, KeyframeBuilder> builders = new TreeMap<Float, KeyframeBuilder>();
        if (data.has("rotation")) {
            JsonElement rotEl = data.get("rotation");
            if (rotEl.isJsonArray()) {
                b2 = PunchyAnimationLoader.getBuilder(builders, 0.0f);
                ((KeyframeBuilder)((Object)b2)).rotationEuler = PunchyAnimationLoader.parseRotation(rotEl);
                ((KeyframeBuilder)((Object)b2)).rotation = PunchyAnimationLoader.parseEuler(rotEl);
                Vector3f rotV2 = PunchyAnimationLoader.parseRotationV2(rotEl);
                ((KeyframeBuilder)((Object)b2)).rotationPreEulerV2 = rotV2 != null ? new Vector3f((Vector3fc)rotV2) : null;
                ((KeyframeBuilder)((Object)b2)).rotationPostEulerV2 = rotV2 != null ? new Vector3f((Vector3fc)rotV2) : null;
            } else if (rotEl.isJsonObject()) {
                for (Map.Entry entry : rotEl.getAsJsonObject().entrySet()) {
                    String interpolation2;
                    b = PunchyAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    b.rotationEuler = PunchyAnimationLoader.parseRotation((JsonElement)entry.getValue());
                    b.rotation = PunchyAnimationLoader.parseEuler((JsonElement)entry.getValue());
                    Vector3f preV2 = PunchyAnimationLoader.parseRotationEndpointV2((JsonElement)entry.getValue(), "pre");
                    Vector3f postV2 = PunchyAnimationLoader.parseRotationEndpointV2((JsonElement)entry.getValue(), "post");
                    if (preV2 != null || postV2 != null) {
                        Object object = preV2 != null ? preV2 : (b.rotationPreEulerV2 = postV2 != null ? new Vector3f((Vector3fc)postV2) : null);
                        b.rotationPostEulerV2 = postV2 != null ? postV2 : (preV2 != null ? new Vector3f((Vector3fc)preV2) : null);
                    } else {
                        Vector3f rotV2 = PunchyAnimationLoader.parseRotationV2((JsonElement)entry.getValue());
                        if (rotV2 != null) {
                            b.rotationPreEulerV2 = new Vector3f((Vector3fc)rotV2);
                            b.rotationPostEulerV2 = new Vector3f((Vector3fc)rotV2);
                        }
                    }
                    if ((interpolation2 = PunchyAnimationLoader.readInterpolation((JsonElement)entry.getValue())) == null) continue;
                    b.interpolation = interpolation2;
                }
            }
        }
        if (data.has("position")) {
            JsonElement posEl = data.get("position");
            if (posEl.isJsonArray()) {
                b2 = PunchyAnimationLoader.getBuilder(builders, 0.0f);
                ((KeyframeBuilder)((Object)b2)).position = PunchyAnimationLoader.parsePosition(posEl, boneName);
                ((KeyframeBuilder)((Object)b2)).positionV2 = PunchyAnimationLoader.parsePositionV2(posEl);
            } else if (posEl.isJsonObject()) {
                for (Map.Entry entry : posEl.getAsJsonObject().entrySet()) {
                    b = PunchyAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    b.position = PunchyAnimationLoader.parsePosition((JsonElement)entry.getValue(), boneName);
                    b.positionV2 = PunchyAnimationLoader.parsePositionV2((JsonElement)entry.getValue());
                    interpolation = PunchyAnimationLoader.readInterpolation((JsonElement)entry.getValue());
                    if (interpolation == null) continue;
                    b.interpolation = interpolation;
                }
            }
        }
        if (data.has("scale")) {
            JsonElement scaleEl = data.get("scale");
            if (scaleEl.isJsonArray()) {
                PunchyAnimationLoader.getBuilder(builders, (float)0.0f).scale = PunchyAnimationLoader.parseScale(scaleEl, boneName);
            } else if (scaleEl.isJsonObject()) {
                for (Map.Entry entry : scaleEl.getAsJsonObject().entrySet()) {
                    b = PunchyAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    b.scale = PunchyAnimationLoader.parseScale((JsonElement)entry.getValue(), boneName);
                    interpolation = PunchyAnimationLoader.readInterpolation((JsonElement)entry.getValue());
                    if (interpolation == null) continue;
                    b.interpolation = interpolation;
                }
            }
        }
        if (stretchToLength && animationLength > 0.0f && !builders.isEmpty() && (lastTime = builders.lastKey()) != null && animationLength - lastTime.floatValue() > 1.0E-4f && !builders.containsKey(Float.valueOf(animationLength)) && (last = builders.remove(lastTime)) != null) {
            builders.put(Float.valueOf(animationLength), last);
        }
        for (Map.Entry<Float, KeyframeBuilder> entry : builders.entrySet()) {
            KeyframeBuilder b3 = entry.getValue();
            Vector3f pos = b3.position != null ? new Vector3f((Vector3fc)b3.position) : null;
            Vector3f rotEuler = b3.rotationEuler != null ? new Vector3f((Vector3fc)b3.rotationEuler) : null;
            Quaternionf rot = b3.rotation != null ? new Quaternionf((Quaternionfc)b3.rotation) : null;
            Vector3f scl = b3.scale != null ? new Vector3f((Vector3fc)b3.scale) : null;
            String interpolation3 = b3.interpolation != null ? b3.interpolation : "linear";
            Vector3f posV2 = b3.positionV2 != null ? new Vector3f((Vector3fc)b3.positionV2) : null;
            Vector3f rotPreV2 = b3.rotationPreEulerV2 != null ? new Vector3f((Vector3fc)b3.rotationPreEulerV2) : null;
            Vector3f rotPostV2 = b3.rotationPostEulerV2 != null ? new Vector3f((Vector3fc)b3.rotationPostEulerV2) : null;
            track.addKeyframe(new Keyframe(entry.getKey().floatValue(), pos, rotEuler, rot, scl, interpolation3, posV2, rotPreV2, rotPostV2, null, null, null));
        }
        return track;
    }

    private static KeyframeBuilder getBuilder(Map<Float, KeyframeBuilder> map, float time) {
        return map.computeIfAbsent(Float.valueOf(time), k -> new KeyframeBuilder());
    }

    private static Vector3f parsePosition(JsonElement el, String boneName) {
        JsonElement value = PunchyAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            if (PunchyAnimationLoader.shouldFlipGripZ(boneName)) {
                return new Vector3f(-x, -z, y);
            }
            return new Vector3f(x, -z, -y);
        }
        return new Vector3f(0.0f);
    }

    private static Vector3f parsePositionV2(JsonElement el) {
        JsonElement value = PunchyAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            return new Vector3f(x, -y, z);
        }
        return new Vector3f(0.0f);
    }

    private static Quaternionf parseEuler(JsonElement el) {
        Vector3f e = PunchyAnimationLoader.parseRotation(el);
        float rx = (float)Math.toRadians(e.x);
        float ry = (float)Math.toRadians(e.y);
        float rz = (float)Math.toRadians(e.z);
        return new Quaternionf().rotationZYX(rz, ry, rx);
    }

    private static Vector3f parseRotationEndpoint(JsonElement el, String member) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (!el.isJsonObject()) {
            return PunchyAnimationLoader.parseRotation(el);
        }
        JsonObject obj = el.getAsJsonObject();
        if (!obj.has(member)) {
            return null;
        }
        JsonElement endpoint = obj.get(member);
        if (endpoint == null || endpoint.isJsonNull()) {
            return null;
        }
        return PunchyAnimationLoader.parseRotation(endpoint);
    }

    private static Vector3f parseRotationEndpointV2(JsonElement el, String member) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (!el.isJsonObject()) {
            return PunchyAnimationLoader.parseRotationV2(el);
        }
        JsonObject obj = el.getAsJsonObject();
        if (!obj.has(member)) {
            return null;
        }
        JsonElement endpoint = obj.get(member);
        if (endpoint == null || endpoint.isJsonNull()) {
            return null;
        }
        return PunchyAnimationLoader.parseRotationV2(endpoint);
    }

    private static Vector3f parseRotation(JsonElement el) {
        JsonElement value = PunchyAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            return new Vector3f(x, -z, y);
        }
        return new Vector3f(0.0f);
    }

    private static Vector3f parseRotationV2(JsonElement el) {
        JsonElement value = PunchyAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            return new Vector3f(x, y, z);
        }
        return new Vector3f(0.0f);
    }

    private static Vector3f parseScale(JsonElement el, String boneName) {
        JsonElement value = PunchyAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            if (PunchyAnimationLoader.shouldSwapScaleAxes(boneName)) {
                return new Vector3f(x, z, y);
            }
            return new Vector3f(x, y, z);
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    private static String readInterpolation(JsonElement el) {
        if (el == null || !el.isJsonObject()) {
            return null;
        }
        JsonObject obj = el.getAsJsonObject();
        if (obj.has("lerp_mode")) {
            return obj.get("lerp_mode").getAsString().trim().toLowerCase(Locale.ROOT);
        }
        if (obj.has("easing")) {
            return obj.get("easing").getAsString().trim().toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private static boolean shouldSwapScaleAxes(String boneName) {
        if (boneName == null) {
            return false;
        }
        String name = boneName.toLowerCase(Locale.ROOT);
        return name.contains("item");
    }

    private static boolean shouldFlipGripZ(String boneName) {
        if (boneName == null) {
            return false;
        }
        String name = boneName.toLowerCase(Locale.ROOT);
        return name.contains("itemgrip");
    }

    private static JsonElement unwrapKeyframeElement(JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (!el.isJsonObject()) {
            return el;
        }
        JsonObject obj = el.getAsJsonObject();
        if (obj.has("post")) {
            return obj.get("post");
        }
        if (obj.has("vector")) {
            return obj.get("vector");
        }
        if (obj.has("pre")) {
            return obj.get("pre");
        }
        return el;
    }

    static void parseEffects(AnimationClip clip, JsonObject animData) {
        if (clip == null || animData == null) {
            return;
        }
        if (animData.has("effects")) {
            PunchyAnimationLoader.collectEffectStrings(clip, animData.get("effects"));
        }
        if (animData.has("effect")) {
            PunchyAnimationLoader.collectEffectStrings(clip, animData.get("effect"));
        }
        if (animData.has("events")) {
            PunchyAnimationLoader.collectEffectStrings(clip, animData.get("events"));
        }
        if (animData.has("event")) {
            PunchyAnimationLoader.collectEffectStrings(clip, animData.get("event"));
        }
        if (animData.has("sound_effects")) {
            PunchyAnimationLoader.collectSoundEffects(clip, animData.get("sound_effects"));
        }
    }

    static void parseTimelineEffects(AnimationClip clip, JsonObject animData) {
        if (clip == null || animData == null || !animData.has("timeline")) {
            return;
        }
        JsonObject timeline = animData.getAsJsonObject("timeline");
        for (Map.Entry entry : timeline.entrySet()) {
            float time = PunchyAnimationLoader.parseTime((String)entry.getKey());
            PunchyAnimationLoader.collectTimelineEffects(clip, time, (JsonElement)entry.getValue());
        }
    }

    private static void collectEffectStrings(AnimationClip clip, JsonElement element) {
        if (clip == null || element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (value != null && !value.isEmpty()) {
                String[] parts;
                for (String part : parts = value.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) continue;
                    clip.addEffect(trimmed.toLowerCase(Locale.ROOT));
                }
            }
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                PunchyAnimationLoader.collectEffectStrings(clip, child);
            }
            return;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry entry : obj.entrySet()) {
                PunchyAnimationLoader.collectEffectStrings(clip, (JsonElement)entry.getValue());
            }
        }
    }

    private static void collectTimelineEffects(AnimationClip clip, float time, JsonElement element) {
        if (clip == null || element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (value != null && !value.isEmpty()) {
                String[] parts;
                for (String part : parts = value.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) continue;
                    String normalized = trimmed.toLowerCase(Locale.ROOT);
                    clip.addTimelineEffect(time, normalized);
                    String legacySound = PunchyAnimationLoader.extractLegacyTimelineSoundEffect(normalized);
                    if (legacySound == null) continue;
                    clip.addSoundEffect(time, legacySound);
                }
            }
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                PunchyAnimationLoader.collectTimelineEffects(clip, time, child);
            }
            return;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry entry : obj.entrySet()) {
                PunchyAnimationLoader.collectTimelineEffects(clip, time, (JsonElement)entry.getValue());
            }
        }
    }

    private static String extractLegacyTimelineSoundEffect(String normalizedEffect) {
        if (normalizedEffect == null || normalizedEffect.isBlank()) {
            return null;
        }
        if (normalizedEffect.startsWith("play_hamster_sound_") && normalizedEffect.length() > "play_hamster_sound_".length()) {
            return normalizedEffect.substring("play_hamster_sound_".length()).trim();
        }
        if (normalizedEffect.startsWith("play_sound_") && normalizedEffect.length() > "play_sound_".length()) {
            return normalizedEffect.substring("play_sound_".length()).trim();
        }
        return null;
    }

    private static void collectSoundEffects(AnimationClip clip, JsonElement element) {
        if (clip == null || element == null || element.isJsonNull() || !element.isJsonObject()) {
            return;
        }
        JsonObject obj = element.getAsJsonObject();
        for (Map.Entry entry : obj.entrySet()) {
            String effect;
            float time = PunchyAnimationLoader.parseTime((String)entry.getKey());
            JsonElement value = (JsonElement)entry.getValue();
            if (value != null && value.isJsonObject()) {
                String effect2;
                JsonObject soundObj = value.getAsJsonObject();
                if (!soundObj.has("effect") || (effect2 = soundObj.get("effect").getAsString()) == null || effect2.isBlank()) continue;
                clip.addSoundEffect(time, effect2.trim());
                continue;
            }
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || (effect = value.getAsString()) == null || effect.isBlank()) continue;
            clip.addSoundEffect(time, effect.trim());
        }
    }

    private static float parseTime(String key) {
        if (key == null || key.isEmpty()) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(key);
        }
        catch (NumberFormatException ignored) {
            return 0.0f;
        }
    }

    private static LoopInfo parseLoopInfo(JsonObject animData) {
        JsonElement loopEl;
        boolean loop = false;
        boolean holdOnLastFrame = false;
        if (animData != null && animData.has("loop") && (loopEl = animData.get("loop")).isJsonPrimitive()) {
            if (loopEl.getAsJsonPrimitive().isBoolean()) {
                loop = loopEl.getAsBoolean();
            } else if (loopEl.getAsJsonPrimitive().isString()) {
                String value = loopEl.getAsString().toLowerCase(Locale.ROOT).trim();
                if ("hold_on_last_frame".equals(value)) {
                    holdOnLastFrame = true;
                } else if ("loop".equals(value) || "true".equals(value)) {
                    loop = true;
                }
            }
        }
        return new LoopInfo(loop, holdOnLastFrame);
    }

    private static AnimationClip buildClip(String name, JsonObject animData) {
        if (name == null || animData == null) {
            return null;
        }
        float length = animData.has("animation_length") ? animData.get("animation_length").getAsFloat() : 0.0f;
        LoopInfo loopInfo = PunchyAnimationLoader.parseLoopInfo(animData);
        AnimationClip clip = new AnimationClip(name, length, loopInfo.loop);
        clip.setHoldOnLastFrame(loopInfo.holdOnLastFrame);
        clip.setLengthDefined(animData.has("animation_length"));
        clip.setLoopDefined(animData.has("loop"));
        if (animData.has("bones")) {
            JsonObject bonesObj = animData.getAsJsonObject("bones");
            for (String boneName : bonesObj.keySet()) {
                JsonObject boneData = bonesObj.getAsJsonObject(boneName);
                clip.addTrack(PunchyAnimationLoader.parseBoneTrack(boneName, boneData, length, loopInfo.holdOnLastFrame));
            }
        }
        PunchyAnimationLoader.parseEffects(clip, animData);
        PunchyAnimationLoader.parseTimelineEffects(clip, animData);
        return clip;
    }

    public static String optionIdFromPackId(String packId) {
        if (packId == null || packId.isBlank()) {
            return DEFAULT_PACK_OPTION_ID;
        }
        String normalized = packId.trim().toLowerCase(Locale.ROOT);
        if ("vanilla".equals(normalized) || "mod_resources".equals(normalized)) {
            return DEFAULT_PACK_OPTION_ID;
        }
        if (normalized.startsWith("pack:")) {
            return normalized;
        }
        return "pack:" + normalized;
    }

    private static boolean isPunchyBuiltInPackId(String packId) {
        if (packId == null || packId.isBlank()) {
            return false;
        }
        String normalized = packId.trim().toLowerCase(Locale.ROOT);
        return "punchy".equals(normalized) || normalized.startsWith("punchy:") || normalized.endsWith(":punchy") || normalized.endsWith("/punchy") || normalized.endsWith("\\punchy") || "builtin/punchy".equals(normalized) || "builtin\\punchy".equals(normalized);
    }

    private record AnimationResourceEntry(class_2960 id, class_3298 resource, int packPriority, int discoveryOrder, int stackOrder) {
    }

    private static class KeyframeBuilder {
        Vector3f position;
        Vector3f positionV2;
        Vector3f rotationEuler;
        Quaternionf rotation;
        Vector3f scale;
        String interpolation;
        Vector3f rotationPreEulerV2;
        Vector3f rotationPostEulerV2;

        private KeyframeBuilder() {
        }
    }

    private record LoopInfo(boolean loop, boolean holdOnLastFrame) {
    }
}

