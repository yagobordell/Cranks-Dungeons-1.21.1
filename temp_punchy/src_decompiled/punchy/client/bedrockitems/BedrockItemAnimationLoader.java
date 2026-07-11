/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.bedrockitems;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.data.AnimationExpression;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;
import punchy.client.animation.data.VectorExpression;

final class BedrockItemAnimationLoader {
    private BedrockItemAnimationLoader() {
    }

    static Map<String, AnimationClip> loadAll(class_3300 manager, class_2960 id) {
        HashMap<String, AnimationClip> result = new HashMap<String, AnimationClip>();
        if (manager == null || id == null) {
            return result;
        }
        try {
            class_3298 resource = manager.getResourceOrThrow(id);
            JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(resource.method_14482())).getAsJsonObject();
            JsonObject animationsObj = json.getAsJsonObject("animations");
            if (animationsObj == null) {
                return result;
            }
            for (Map.Entry entry : animationsObj.entrySet()) {
                String name;
                AnimationClip clip;
                if (!((JsonElement)entry.getValue()).isJsonObject() || (clip = BedrockItemAnimationLoader.buildClip(name = (String)entry.getKey(), ((JsonElement)entry.getValue()).getAsJsonObject())) == null) continue;
                result.put(name, clip);
                result.put(id.method_12836() + ":" + name, clip);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return result;
    }

    private static AnimationClip buildClip(String name, JsonObject animData) {
        JsonElement loopEl;
        if (animData == null) {
            return null;
        }
        float length = animData.has("animation_length") ? animData.get("animation_length").getAsFloat() : 0.0f;
        boolean loop = false;
        boolean holdOnLastFrame = false;
        if (animData.has("loop") && (loopEl = animData.get("loop")) != null && loopEl.isJsonPrimitive()) {
            JsonPrimitive prim = loopEl.getAsJsonPrimitive();
            if (prim.isBoolean()) {
                loop = prim.getAsBoolean();
            } else if (prim.isString()) {
                String value = prim.getAsString().trim().toLowerCase(Locale.ROOT);
                if ("hold_on_last_frame".equals(value)) {
                    holdOnLastFrame = true;
                    loop = false;
                } else {
                    loop = Boolean.parseBoolean(value);
                }
            }
        }
        AnimationClip clip = new AnimationClip(name, length, loop);
        if (holdOnLastFrame) {
            clip.setHoldOnLastFrame(true);
        }
        if (!animData.has("bones")) {
            return clip;
        }
        JsonObject bonesObj = animData.getAsJsonObject("bones");
        for (Map.Entry entry : bonesObj.entrySet()) {
            BoneTrack track;
            if (!((JsonElement)entry.getValue()).isJsonObject() || (track = BedrockItemAnimationLoader.parseBoneTrack((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsJsonObject())) == null) continue;
            clip.addTrack(track);
        }
        BedrockItemAnimationLoader.parseTimelineEffects(clip, animData);
        return clip;
    }

    private static BoneTrack parseBoneTrack(String boneName, JsonObject data) {
        KeyframeBuilder b;
        ParsedVec3 parsed;
        KeyframeBuilder b2;
        Iterator parsed2;
        if (boneName == null || data == null) {
            return null;
        }
        BoneTrack track = new BoneTrack(boneName);
        TreeMap<Float, KeyframeBuilder> builders = new TreeMap<Float, KeyframeBuilder>();
        if (data.has("rotation")) {
            JsonElement rotEl = data.get("rotation");
            if (rotEl.isJsonArray()) {
                KeyframeBuilder b3 = BedrockItemAnimationLoader.getBuilder(builders, 0.0f);
                ParsedVec3 parsed3 = BedrockItemAnimationLoader.parseRotation(rotEl);
                b3.rotationEuler = parsed3.value;
                b3.rotationExpression = parsed3.expression;
                b3.rotation = BedrockItemAnimationLoader.parseEuler(parsed3.value);
            } else if (rotEl.isJsonObject()) {
                for (Map.Entry entry : rotEl.getAsJsonObject().entrySet()) {
                    KeyframeBuilder b4 = BedrockItemAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    ParsedVec3 parsed4 = BedrockItemAnimationLoader.parseRotation((JsonElement)entry.getValue());
                    b4.rotationEuler = parsed4.value;
                    b4.rotationExpression = parsed4.expression;
                    b4.rotation = BedrockItemAnimationLoader.parseEuler(parsed4.value);
                }
            }
        }
        if (data.has("position")) {
            JsonElement posEl = data.get("position");
            if (posEl.isJsonArray()) {
                parsed2 = BedrockItemAnimationLoader.parsePosition(posEl);
                b2 = BedrockItemAnimationLoader.getBuilder(builders, 0.0f);
                b2.position = ((ParsedVec3)((Object)parsed2)).value;
                b2.positionExpression = ((ParsedVec3)((Object)parsed2)).expression;
            } else if (posEl.isJsonObject()) {
                for (Map.Entry entry : posEl.getAsJsonObject().entrySet()) {
                    parsed = BedrockItemAnimationLoader.parsePosition((JsonElement)entry.getValue());
                    b = BedrockItemAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    b.position = parsed.value;
                    b.positionExpression = parsed.expression;
                }
            }
        }
        if (data.has("scale")) {
            JsonElement scaleEl = data.get("scale");
            if (scaleEl.isJsonArray()) {
                parsed2 = BedrockItemAnimationLoader.parseScale(scaleEl);
                b2 = BedrockItemAnimationLoader.getBuilder(builders, 0.0f);
                b2.scale = ((ParsedVec3)((Object)parsed2)).value;
                b2.scaleExpression = ((ParsedVec3)((Object)parsed2)).expression;
            } else if (scaleEl.isJsonObject()) {
                for (Map.Entry entry : scaleEl.getAsJsonObject().entrySet()) {
                    parsed = BedrockItemAnimationLoader.parseScale((JsonElement)entry.getValue());
                    b = BedrockItemAnimationLoader.getBuilder(builders, Float.parseFloat((String)entry.getKey()));
                    b.scale = parsed.value;
                    b.scaleExpression = parsed.expression;
                }
            }
        }
        for (Map.Entry<Float, KeyframeBuilder> entry : builders.entrySet()) {
            b2 = entry.getValue();
            Vector3f pos = b2.position != null ? new Vector3f((Vector3fc)b2.position) : null;
            Vector3f rotEuler = b2.rotationEuler != null ? new Vector3f((Vector3fc)b2.rotationEuler) : null;
            Quaternionf rot = b2.rotation != null ? new Quaternionf((Quaternionfc)b2.rotation) : null;
            Vector3f scl = b2.scale != null ? new Vector3f((Vector3fc)b2.scale) : null;
            track.addKeyframe(new Keyframe(entry.getKey().floatValue(), pos, rotEuler, rot, scl, "linear", b2.positionExpression, b2.rotationExpression, b2.scaleExpression));
        }
        return track;
    }

    private static KeyframeBuilder getBuilder(Map<Float, KeyframeBuilder> map, float time) {
        return map.computeIfAbsent(Float.valueOf(time), k -> new KeyframeBuilder());
    }

    private static ParsedVec3 parsePosition(JsonElement el) {
        JsonElement value = BedrockItemAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            ValueOrExpr x = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(0));
            ValueOrExpr y = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(1));
            ValueOrExpr z = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(2));
            return new ParsedVec3(new Vector3f(-((float)x.value), (float)y.value, (float)z.value), new VectorExpression(BedrockItemAnimationLoader.negate(x.expr), y.expr, z.expr));
        }
        return new ParsedVec3(new Vector3f(0.0f), null);
    }

    private static Quaternionf parseEuler(Vector3f e) {
        float rx = (float)Math.toRadians(e.x);
        float ry = (float)Math.toRadians(e.y);
        float rz = (float)Math.toRadians(e.z);
        return new Quaternionf().rotationXYZ(rx, ry, rz);
    }

    private static ParsedVec3 parseRotation(JsonElement el) {
        JsonElement value = BedrockItemAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            ValueOrExpr x = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(0));
            ValueOrExpr y = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(1));
            ValueOrExpr z = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(2));
            return new ParsedVec3(new Vector3f(-((float)x.value), -((float)y.value), (float)z.value), new VectorExpression(BedrockItemAnimationLoader.negate(x.expr), BedrockItemAnimationLoader.negate(y.expr), z.expr));
        }
        return new ParsedVec3(new Vector3f(0.0f), null);
    }

    private static ParsedVec3 parseScale(JsonElement el) {
        JsonElement value = BedrockItemAnimationLoader.unwrapKeyframeElement(el);
        if (value != null && value.isJsonArray()) {
            ValueOrExpr x = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(0));
            ValueOrExpr y = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(1));
            ValueOrExpr z = BedrockItemAnimationLoader.parseValue(value.getAsJsonArray().get(2));
            return new ParsedVec3(new Vector3f((float)x.value, (float)y.value, (float)z.value), new VectorExpression(x.expr, y.expr, z.expr));
        }
        return new ParsedVec3(new Vector3f(1.0f, 1.0f, 1.0f), null);
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

    private static ValueOrExpr parseValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return new ValueOrExpr(0.0, null);
        }
        try {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return new ValueOrExpr(element.getAsDouble(), null);
            }
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String text = element.getAsString();
                if (text == null) {
                    return new ValueOrExpr(0.0, null);
                }
                AnimationExpression expr = AnimationExpression.compile(text);
                if (expr != null) {
                    return new ValueOrExpr(0.0, expr);
                }
                return new ValueOrExpr(Double.parseDouble(text), null);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new ValueOrExpr(0.0, null);
    }

    private static AnimationExpression negate(AnimationExpression expr) {
        return AnimationExpression.negated(expr);
    }

    private static void parseTimelineEffects(AnimationClip clip, JsonObject animData) {
        if (clip == null || animData == null || !animData.has("timeline")) {
            return;
        }
        JsonObject timeline = animData.getAsJsonObject("timeline");
        for (Map.Entry entry : timeline.entrySet()) {
            float time = BedrockItemAnimationLoader.parseTime((String)entry.getKey());
            BedrockItemAnimationLoader.collectTimelineEffects(clip, time, (JsonElement)entry.getValue());
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
                    clip.addTimelineEffect(time, trimmed.toLowerCase(Locale.ROOT));
                }
            }
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                BedrockItemAnimationLoader.collectTimelineEffects(clip, time, child);
            }
            return;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry entry : obj.entrySet()) {
                BedrockItemAnimationLoader.collectTimelineEffects(clip, time, (JsonElement)entry.getValue());
            }
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

    private static class KeyframeBuilder {
        Vector3f position;
        Vector3f rotationEuler;
        Quaternionf rotation;
        Vector3f scale;
        VectorExpression positionExpression;
        VectorExpression rotationExpression;
        VectorExpression scaleExpression;

        private KeyframeBuilder() {
        }
    }

    private record ParsedVec3(Vector3f value, VectorExpression expression) {
    }

    private record ValueOrExpr(double value, AnimationExpression expr) {
    }
}

