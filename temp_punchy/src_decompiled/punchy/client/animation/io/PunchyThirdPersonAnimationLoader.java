/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_2960
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.class_2960;
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
import punchy.client.animation.io.PunchyAnimationLoader;

public class PunchyThirdPersonAnimationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Punchy/ThirdPersonAnimationLoader");

    public static Map<String, AnimationClip> loadAll(class_3300 manager) {
        HashMap<String, AnimationClip> result = new HashMap<String, AnimationClip>();
        if (manager == null) {
            return result;
        }
        PunchyThirdPersonAnimationLoader.loadFromManager(manager, class_2960.method_60655((String)"punchy", (String)"animations/player_3_p.animation.json"), result);
        if (result.isEmpty()) {
            PunchyThirdPersonAnimationLoader.loadFromManager(manager, class_2960.method_60655((String)"minecraft", (String)"punchy/animations/player_3_p.animation.json"), result);
        }
        return result;
    }

    private static void loadFromManager(class_3300 manager, class_2960 id, Map<String, AnimationClip> result) {
        if (manager == null || id == null || result == null) {
            return;
        }
        try {
            class_3298 resource = manager.getResourceOrThrow(id);
            if (resource == null) {
                return;
            }
            PunchyThirdPersonAnimationLoader.loadFromResource(resource, result);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void loadFromResource(class_3298 resource, Map<String, AnimationClip> result) {
        if (resource == null || result == null) {
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(resource.method_14482());){
            JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            JsonObject animationsObj = json.getAsJsonObject("animations");
            if (animationsObj == null) {
                return;
            }
            for (Map.Entry entry : animationsObj.entrySet()) {
                AnimationClip clip;
                if (entry == null || entry.getKey() == null || !((JsonElement)entry.getValue()).isJsonObject() || (clip = PunchyThirdPersonAnimationLoader.buildClip((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsJsonObject())) == null) continue;
                result.put((String)entry.getKey(), clip);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static AnimationClip buildClip(String name, JsonObject animData) {
        if (name == null || animData == null) {
            return null;
        }
        float length = animData.has("animation_length") ? animData.get("animation_length").getAsFloat() : 0.0f;
        LoopInfo loopInfo = PunchyThirdPersonAnimationLoader.parseLoopInfo(animData);
        AnimationClip clip = new AnimationClip(name, length, loopInfo.loop);
        clip.setHoldOnLastFrame(loopInfo.holdOnLastFrame);
        clip.setLengthDefined(animData.has("animation_length"));
        clip.setLoopDefined(animData.has("loop"));
        if (animData.has("bones")) {
            JsonObject bonesObj = animData.getAsJsonObject("bones");
            for (String boneName : bonesObj.keySet()) {
                JsonObject boneData = bonesObj.getAsJsonObject(boneName);
                clip.addTrack(PunchyThirdPersonAnimationLoader.parseBoneTrack(boneName, boneData));
            }
        }
        PunchyAnimationLoader.parseEffects(clip, animData);
        PunchyAnimationLoader.parseTimelineEffects(clip, animData);
        return clip;
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

    private static BoneTrack parseBoneTrack(String boneName, JsonObject data) {
        BoneTrack track = new BoneTrack(boneName);
        TreeMap<Float, KeyframeBuilder> builders = new TreeMap<Float, KeyframeBuilder>();
        if (data.has("rotation")) {
            JsonElement rotEl = data.get("rotation");
            if (rotEl.isJsonArray()) {
                Iterator b = PunchyThirdPersonAnimationLoader.getBuilder(builders, 0.0f);
                ((KeyframeBuilder)((Object)b)).rotationEuler = PunchyThirdPersonAnimationLoader.parseRotation(rotEl);
                ((KeyframeBuilder)((Object)b)).rotation = PunchyThirdPersonAnimationLoader.parseEuler(rotEl);
            } else if (rotEl.isJsonObject()) {
                for (Map.Entry entry : rotEl.getAsJsonObject().entrySet()) {
                    KeyframeBuilder b = PunchyThirdPersonAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    b.rotationEuler = PunchyThirdPersonAnimationLoader.parseRotation((JsonElement)entry.getValue());
                    b.rotation = PunchyThirdPersonAnimationLoader.parseEuler((JsonElement)entry.getValue());
                }
            }
        }
        if (data.has("position")) {
            JsonElement posEl = data.get("position");
            if (posEl.isJsonArray()) {
                PunchyThirdPersonAnimationLoader.getBuilder(builders, (float)0.0f).position = PunchyThirdPersonAnimationLoader.parsePosition(posEl, boneName);
            } else if (posEl.isJsonObject()) {
                for (Map.Entry entry : posEl.getAsJsonObject().entrySet()) {
                    PunchyThirdPersonAnimationLoader.getBuilder(builders, (float)Float.parseFloat((String)((String)entry.getKey()))).position = PunchyThirdPersonAnimationLoader.parsePosition((JsonElement)entry.getValue(), boneName);
                }
            }
        }
        if (data.has("scale")) {
            JsonElement scaleEl = data.get("scale");
            if (scaleEl.isJsonArray()) {
                PunchyThirdPersonAnimationLoader.getBuilder(builders, (float)0.0f).scale = PunchyThirdPersonAnimationLoader.parseScale(scaleEl, boneName);
            } else if (scaleEl.isJsonObject()) {
                for (Map.Entry entry : scaleEl.getAsJsonObject().entrySet()) {
                    PunchyThirdPersonAnimationLoader.getBuilder(builders, (float)Float.parseFloat((String)((String)entry.getKey()))).scale = PunchyThirdPersonAnimationLoader.parseScale((JsonElement)entry.getValue(), boneName);
                }
            }
        }
        for (Map.Entry entry : builders.entrySet()) {
            KeyframeBuilder b = (KeyframeBuilder)entry.getValue();
            Vector3f pos = b.position != null ? new Vector3f((Vector3fc)b.position) : null;
            Vector3f rotEuler = b.rotationEuler != null ? new Vector3f((Vector3fc)b.rotationEuler) : null;
            Quaternionf rot = b.rotation != null ? new Quaternionf((Quaternionfc)b.rotation) : null;
            Vector3f scl = b.scale != null ? new Vector3f((Vector3fc)b.scale) : null;
            track.addKeyframe(new Keyframe(((Float)entry.getKey()).floatValue(), pos, rotEuler, rot, scl, "linear"));
        }
        return track;
    }

    private static KeyframeBuilder getBuilder(Map<Float, KeyframeBuilder> map, float time) {
        return map.computeIfAbsent(Float.valueOf(time), k -> new KeyframeBuilder());
    }

    private static Vector3f parsePosition(JsonElement el, String boneName) {
        JsonElement value = PunchyThirdPersonAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            if (PunchyThirdPersonAnimationLoader.shouldFlipGripZ(boneName)) {
                return new Vector3f(-x, -z, y);
            }
            return new Vector3f(x, -z, -y);
        }
        return new Vector3f(0.0f);
    }

    private static Quaternionf parseEuler(JsonElement el) {
        Vector3f e = PunchyThirdPersonAnimationLoader.parseRotation(el);
        float rx = (float)Math.toRadians(e.x);
        float ry = (float)Math.toRadians(e.y);
        float rz = (float)Math.toRadians(e.z);
        return new Quaternionf().rotationZYX(rz, ry, rx);
    }

    private static Vector3f parseRotation(JsonElement el) {
        JsonElement value = PunchyThirdPersonAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            return new Vector3f(x, y, z);
        }
        return new Vector3f(0.0f);
    }

    private static Vector3f parseScale(JsonElement el, String boneName) {
        JsonElement value = PunchyThirdPersonAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            float x = value.getAsJsonArray().get(0).getAsFloat();
            float y = value.getAsJsonArray().get(1).getAsFloat();
            float z = value.getAsJsonArray().get(2).getAsFloat();
            if (PunchyThirdPersonAnimationLoader.shouldSwapScaleAxes(boneName)) {
                return new Vector3f(x, z, y);
            }
            return new Vector3f(x, y, z);
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
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

    private static void loadBundledDefaults(Map<String, AnimationClip> result) {
    }

    private record LoopInfo(boolean loop, boolean holdOnLastFrame) {
    }

    private static class KeyframeBuilder {
        Vector3f position;
        Vector3f rotationEuler;
        Quaternionf rotation;
        Vector3f scale;

        private KeyframeBuilder() {
        }
    }
}

