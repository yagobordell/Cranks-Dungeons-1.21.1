/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_1011
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_3532
 *  net.minecraft.class_9334
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package punchy.client.bedrockitems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import net.minecraft.class_1011;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3532;
import net.minecraft.class_9334;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import punchy.client.animation.data.AnimationClip;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;
import punchy.client.bedrockitems.BedrockItemAnimationLoader;
import punchy.client.bedrockitems.BedrockItemGeoLoader;
import punchy.client.bedrockitems.BedrockItemGeoModel;
import punchy.client.render.ToolTuning;
import punchy.config.PunchyTuningConfig;
import punchy.util.ResourceLocationCompat;

final class BedrockItemDefinition {
    private static final Gson GSON = new GsonBuilder().create();
    private final class_2960 id;
    private final List<String> items;
    private final class_2960 geoId;
    private final class_2960 textureId;
    private final List<class_2960> animationFiles;
    private final List<PunchyTuningConfig.CustomAnimationEntry> modelPartsAnimation;
    private final List<String> definitionParticleEffects;
    private final Map<class_1306, DisplayTransform> displayTransforms;
    private final Map<String, Integer> boneEmission;
    private final Map<String, class_2960> boneTextures;
    private final Map<String, TextureAnimSpec> boneTextureAnim;
    private final List<DynamicBoneTextureSpec> dynamicBoneTextures;
    private final Map<String, BoneMirrorSpec> boneMirror;
    private final Map<String, BonePhysicsSpec> bonePhysics;
    private final Set<String> disablePhysicsBones;
    private final Set<String> cameraTransparentBones;
    private final boolean boneEmissionBlur;
    private final boolean boneEmissionClamp;
    private final int boneEmissionStrength;
    private final int textureAnimFrames;
    private final int textureAnimFrameTime;
    private final int textureAnimFrameHeight;
    private final boolean physicsFeedback;
    private final String customName;
    private BedrockItemGeoModel geoModel;
    private final Map<class_2960, BedrockItemGeoModel> geoModelCache = new HashMap<class_2960, BedrockItemGeoModel>();
    private final Map<String, AnimationClip> animations = new HashMap<String, AnimationClip>();
    private int texturePixelWidth;
    private int texturePixelHeight;
    private final Map<class_2960, int[]> textureSizeCache = new HashMap<class_2960, int[]>();

    BedrockItemDefinition(class_2960 id, List<String> items, class_2960 geoId, class_2960 textureId, List<class_2960> animationFiles, List<PunchyTuningConfig.CustomAnimationEntry> modelPartsAnimation, List<String> definitionParticleEffects, Map<String, Integer> boneEmission, Map<String, class_2960> boneTextures, Map<String, TextureAnimSpec> boneTextureAnim, List<DynamicBoneTextureSpec> dynamicBoneTextures, Map<String, BoneMirrorSpec> boneMirror, Map<String, BonePhysicsSpec> bonePhysics, Set<String> disablePhysicsBones, Set<String> cameraTransparentBones, boolean boneEmissionBlur, boolean boneEmissionClamp, int boneEmissionStrength, int textureAnimFrames, int textureAnimFrameTime, int textureAnimFrameHeight, boolean physicsFeedback, Map<class_1306, DisplayTransform> displayTransforms, String customName) {
        this.id = id;
        this.items = items == null ? List.of() : items;
        this.geoId = geoId;
        this.textureId = textureId;
        this.animationFiles = animationFiles == null ? List.of() : animationFiles;
        this.modelPartsAnimation = modelPartsAnimation == null ? List.of() : modelPartsAnimation;
        this.definitionParticleEffects = definitionParticleEffects == null ? List.of() : new ArrayList<String>(definitionParticleEffects);
        this.displayTransforms = displayTransforms == null ? Map.of() : displayTransforms;
        this.boneEmission = boneEmission == null ? Map.of() : new HashMap<String, Integer>(boneEmission);
        this.boneTextures = boneTextures == null ? Map.of() : new HashMap<String, class_2960>(boneTextures);
        this.boneTextureAnim = boneTextureAnim == null ? Map.of() : new HashMap<String, TextureAnimSpec>(boneTextureAnim);
        this.dynamicBoneTextures = dynamicBoneTextures == null ? List.of() : List.copyOf(dynamicBoneTextures);
        this.boneMirror = boneMirror == null ? Map.of() : new HashMap<String, BoneMirrorSpec>(boneMirror);
        this.bonePhysics = bonePhysics == null ? Map.of() : new HashMap<String, BonePhysicsSpec>(bonePhysics);
        this.disablePhysicsBones = disablePhysicsBones == null ? Set.of() : new HashSet<String>(disablePhysicsBones);
        this.cameraTransparentBones = cameraTransparentBones == null ? Set.of() : new HashSet<String>(cameraTransparentBones);
        this.boneEmissionBlur = boneEmissionBlur;
        this.boneEmissionClamp = boneEmissionClamp;
        this.boneEmissionStrength = class_3532.method_15340((int)boneEmissionStrength, (int)0, (int)100);
        this.textureAnimFrames = textureAnimFrames < 1 ? 1 : textureAnimFrames;
        this.textureAnimFrameTime = textureAnimFrameTime < 1 ? 1 : textureAnimFrameTime;
        this.textureAnimFrameHeight = textureAnimFrameHeight < 1 ? 0 : textureAnimFrameHeight;
        this.physicsFeedback = physicsFeedback;
        this.customName = customName;
    }

    static List<BedrockItemDefinition> loadAll(class_3300 manager) {
        if (manager == null) {
            return List.of();
        }
        HashMap found = new HashMap();
        found.putAll(manager.method_14488("punchy/model_parts_items/definitions", loc -> loc.method_12832().startsWith("punchy/model_parts_items/definitions/") && loc.method_12832().endsWith(".json")));
        if (found.isEmpty()) {
            found.putAll(manager.method_14488("punchy/model_parts_items", loc -> loc.method_12832().contains("/definitions/") && loc.method_12832().endsWith(".json")));
        }
        if (found.isEmpty()) {
            return List.of();
        }
        ArrayList ids = new ArrayList(found.keySet());
        ids.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));
        ArrayList<BedrockItemDefinition> defs = new ArrayList<BedrockItemDefinition>();
        for (class_2960 id : ids) {
            BedrockItemDefinition def;
            class_3298 res = (class_3298)found.get(id);
            if (res == null || (def = BedrockItemDefinition.parse(id, res)) == null) continue;
            defs.add(def);
        }
        return defs;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static BedrockItemDefinition parse(class_2960 id, class_3298 res) {
        try (InputStreamReader reader = new InputStreamReader(res.method_14482());){
            Set<String> tmp;
            JsonElement mirrorEl;
            HashMap<String, Object> tmp2;
            JsonObject animObj;
            HashSet<String> tmp3;
            String key;
            JsonObject boneObj;
            JsonElement value;
            String value2;
            JsonArray arr;
            class_2960 animId;
            class_2960 textureId;
            String value3;
            JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            ArrayList<String> items = new ArrayList<String>();
            if (json.has("items")) {
                JsonArray arr2 = json.getAsJsonArray("items");
                if (arr2 != null) {
                    for (JsonElement el : arr2) {
                        String value4;
                        if (el == null || !el.isJsonPrimitive() || (value4 = el.getAsString()) == null || value4.isBlank()) continue;
                        items.add(value4.trim());
                    }
                }
            } else if (json.has("item") && (value3 = json.get("item").getAsString()) != null && !value3.isBlank()) {
                items.add(value3.trim());
            }
            class_2960 geoId = json.has("geo") ? class_2960.method_12829((String)json.get("geo").getAsString()) : null;
            class_2960 class_29602 = textureId = json.has("texture") ? class_2960.method_12829((String)json.get("texture").getAsString()) : null;
            if (textureId != null) {
                textureId = BedrockItemDefinition.normalizeTextureId(textureId);
            }
            ArrayList<class_2960> animations = new ArrayList<class_2960>();
            if (json.has("animations")) {
                JsonArray arr3 = json.getAsJsonArray("animations");
                if (arr3 != null) {
                    for (Object el : arr3) {
                        class_2960 animId2;
                        if (el == null || !el.isJsonPrimitive() || (animId2 = class_2960.method_12829((String)el.getAsString())) == null) continue;
                        animations.add(animId2);
                    }
                }
            } else if (json.has("animation") && (animId = class_2960.method_12829((String)json.get("animation").getAsString())) != null) {
                animations.add(animId);
            }
            ArrayList<PunchyTuningConfig.CustomAnimationEntry> modelParts = new ArrayList<PunchyTuningConfig.CustomAnimationEntry>();
            if (json.has("modelPartsAnimation") && (arr = json.getAsJsonArray("modelPartsAnimation")) != null) {
                for (JsonElement el : arr) {
                    PunchyTuningConfig.CustomAnimationEntry entry;
                    if (el == null || !el.isJsonObject() || (entry = (PunchyTuningConfig.CustomAnimationEntry)GSON.fromJson(el, PunchyTuningConfig.CustomAnimationEntry.class)) == null) continue;
                    modelParts.add(entry);
                }
            }
            HashMap<class_1306, DisplayTransform> display = new HashMap<class_1306, DisplayTransform>();
            if (json.has("display")) {
                DisplayTransform left;
                JsonObject displayObj = json.getAsJsonObject("display");
                DisplayTransform right = BedrockItemDefinition.readDisplay(displayObj, "thirdperson_righthand");
                if (right == null) {
                    right = BedrockItemDefinition.readDisplay(displayObj, "firstperson_righthand");
                }
                if (right != null) {
                    display.put(class_1306.field_6183, right);
                }
                if ((left = BedrockItemDefinition.readDisplay(displayObj, "thirdperson_lefthand")) == null) {
                    left = BedrockItemDefinition.readDisplay(displayObj, "firstperson_lefthand");
                }
                if (left != null) {
                    display.put(class_1306.field_6182, left);
                }
            }
            String customName = null;
            if (json.has("customName")) {
                String value22 = json.get("customName").getAsString();
                if (value22 != null && !value22.isBlank()) {
                    customName = value22;
                }
            } else if (json.has("name") && (value2 = json.get("name").getAsString()) != null && !value2.isBlank()) {
                customName = value2;
            }
            if (items.isEmpty() || geoId == null || textureId == null) {
                value2 = null;
                return value2;
            }
            boolean physicsFeedback = false;
            if (json.has("physicsFeedback")) {
                value = json.get("physicsFeedback");
                if (value != null && value.isJsonPrimitive()) {
                    physicsFeedback = value.getAsBoolean();
                }
            } else if (json.has("fallingFeedBack")) {
                value = json.get("fallingFeedBack");
                if (value != null && value.isJsonPrimitive()) {
                    physicsFeedback = value.getAsBoolean();
                }
            } else if (json.has("fallingFeedback") && (value = json.get("fallingFeedback")) != null && value.isJsonPrimitive()) {
                physicsFeedback = value.getAsBoolean();
            }
            Map<String, Integer> boneEmission = Map.of();
            boolean boneEmissionBlur = false;
            boolean boneEmissionClamp = false;
            int boneEmissionStrength = 100;
            int textureAnimFrames = 1;
            int textureAnimFrameTime = 1;
            int textureAnimFrameHeight = 0;
            Map<String, class_2960> boneTextures = Map.of();
            Map<String, TextureAnimSpec> boneTextureAnim = Map.of();
            List<DynamicBoneTextureSpec> dynamicBoneTextures = List.of();
            Map<String, BoneMirrorSpec> boneMirror = Map.of();
            Map<String, BonePhysicsSpec> bonePhysics = Map.of();
            Set<String> disablePhysicsBones = Set.of();
            Set<String> cameraTransparentBones = Set.of();
            if (json.has("bone_emission") && json.get("bone_emission").isJsonObject() && (boneObj = json.getAsJsonObject("bone_emission")) != null) {
                HashMap<String, Integer> tmp32 = new HashMap<String, Integer>();
                for (Object entry : boneObj.entrySet()) {
                    key = (String)entry.getKey();
                    if (key == null || key.isBlank()) continue;
                    if ("blur".equalsIgnoreCase(key)) {
                        if (entry.getValue() == null || !((JsonElement)entry.getValue()).isJsonPrimitive()) continue;
                        boneEmissionBlur = ((JsonElement)entry.getValue()).getAsBoolean();
                        continue;
                    }
                    if ("clamp".equalsIgnoreCase(key)) {
                        if (entry.getValue() == null || !((JsonElement)entry.getValue()).isJsonPrimitive()) continue;
                        boneEmissionClamp = ((JsonElement)entry.getValue()).getAsBoolean();
                        continue;
                    }
                    if ("strength".equalsIgnoreCase(key) || "intensity".equalsIgnoreCase(key) || "brightness".equalsIgnoreCase(key)) {
                        if (entry.getValue() == null || !((JsonElement)entry.getValue()).isJsonPrimitive()) continue;
                        int strength = ((JsonElement)entry.getValue()).getAsInt();
                        if (strength < 0) {
                            strength = 0;
                        } else if (strength > 100) {
                            strength = 100;
                        }
                        boneEmissionStrength = strength;
                        continue;
                    }
                    JsonElement value5 = (JsonElement)entry.getValue();
                    if (value5 == null || !value5.isJsonPrimitive() || !value5.getAsJsonPrimitive().isNumber()) continue;
                    int emission = value5.getAsInt();
                    if (emission < 0) {
                        emission = 0;
                    } else if (emission > 100) {
                        emission = 100;
                    }
                    if (emission <= 0) continue;
                    tmp32.put(key, emission);
                }
                if (!tmp32.isEmpty()) {
                    boneEmission = tmp32;
                }
            }
            if (json.has("bone_textures")) {
                JsonElement boneTexEl = json.get("bone_textures");
                if (boneTexEl.isJsonObject()) {
                    JsonObject texObj = boneTexEl.getAsJsonObject();
                    HashMap<String, class_2960> tmp22 = new HashMap<String, class_2960>();
                    for (Map.Entry entry : texObj.entrySet()) {
                        String key2 = (String)entry.getKey();
                        if (key2 == null || key2.isBlank() || entry.getValue() == null || !((JsonElement)entry.getValue()).isJsonPrimitive()) continue;
                        key2 = key2.trim();
                        class_2960 texId = class_2960.method_12829((String)((JsonElement)entry.getValue()).getAsString());
                        if (texId == null) continue;
                        texId = BedrockItemDefinition.normalizeTextureId(texId);
                        tmp22.put(key2, texId);
                    }
                    if (!tmp22.isEmpty()) {
                        boneTextures = tmp22;
                    }
                } else if (boneTexEl.isJsonArray()) {
                    JsonArray arr2 = boneTexEl.getAsJsonArray();
                    tmp3 = new HashMap();
                    for (JsonElement el : arr2) {
                        JsonArray bones;
                        class_2960 texId;
                        JsonObject obj;
                        if (el == null || !el.isJsonObject() || !(obj = el.getAsJsonObject()).has("texture") || !obj.get("texture").isJsonPrimitive() || (texId = class_2960.method_12829((String)obj.get("texture").getAsString())) == null) continue;
                        texId = BedrockItemDefinition.normalizeTextureId(texId);
                        JsonArray jsonArray = bones = obj.has("bones") ? obj.getAsJsonArray("bones") : null;
                        if (bones == null) continue;
                        for (JsonElement boneEl : bones) {
                            String boneName;
                            if (boneEl == null || !boneEl.isJsonPrimitive() || (boneName = boneEl.getAsString()) == null || boneName.isBlank()) continue;
                            tmp3.put(boneName.trim(), texId);
                        }
                    }
                    if (!tmp3.isEmpty()) {
                        boneTextures = tmp3;
                    }
                }
            }
            if (json.has("texture_anim") && json.get("texture_anim").isJsonObject()) {
                animObj = json.getAsJsonObject("texture_anim");
                if (animObj.has("frames")) {
                    textureAnimFrames = Math.max(1, animObj.get("frames").getAsInt());
                }
                if (animObj.has("frametime")) {
                    textureAnimFrameTime = Math.max(1, animObj.get("frametime").getAsInt());
                }
                if (animObj.has("frame_height")) {
                    textureAnimFrameHeight = Math.max(1, animObj.get("frame_height").getAsInt());
                }
            }
            if (json.has("bone_texture_anim") && json.get("bone_texture_anim").isJsonObject() && (animObj = json.getAsJsonObject("bone_texture_anim")) != null) {
                tmp2 = new HashMap<String, Object>();
                for (Object entry : animObj.entrySet()) {
                    TextureAnimSpec spec;
                    key = (String)entry.getKey();
                    if (key == null || key.isBlank() || entry.getValue() == null || !((JsonElement)entry.getValue()).isJsonObject() || (spec = TextureAnimSpec.fromJson(((JsonElement)entry.getValue()).getAsJsonObject())) == null) continue;
                    tmp2.put(key, spec);
                }
                if (!tmp2.isEmpty()) {
                    boneTextureAnim = tmp2;
                }
            }
            if (json.has("dynamic_bone_textures")) {
                dynamicBoneTextures = BedrockItemDefinition.parseDynamicBoneTextures(json.get("dynamic_bone_textures"));
            } else if (json.has("dynamicBoneTextures")) {
                dynamicBoneTextures = BedrockItemDefinition.parseDynamicBoneTextures(json.get("dynamicBoneTextures"));
            }
            if (json.has("bone_mirror") && (mirrorEl = json.get("bone_mirror")) != null && mirrorEl.isJsonObject()) {
                tmp2 = new HashMap();
                JsonObject mirrorObj = mirrorEl.getAsJsonObject();
                for (Map.Entry entry : mirrorObj.entrySet()) {
                    BoneMirrorSpec spec;
                    String bone = (String)entry.getKey();
                    if (bone == null || bone.isBlank() || entry.getValue() == null || (spec = BoneMirrorSpec.fromJson((JsonElement)entry.getValue())) == null) continue;
                    tmp2.put(bone.trim(), spec);
                }
                if (!tmp2.isEmpty()) {
                    boneMirror = tmp2;
                }
            }
            JsonElement bonePhysicsEl = null;
            if (json.has("bone_physics")) {
                bonePhysicsEl = json.get("bone_physics");
            } else if (json.has("bonePhysics")) {
                bonePhysicsEl = json.get("bonePhysics");
            }
            bonePhysics = BedrockItemDefinition.parseBonePhysics(bonePhysicsEl);
            JsonElement physicsEl = null;
            if (json.has("disable_physics_bones")) {
                physicsEl = json.get("disable_physics_bones");
            } else if (json.has("disable_pendulum_bones")) {
                physicsEl = json.get("disable_pendulum_bones");
            }
            if (physicsEl != null && physicsEl.isJsonArray()) {
                tmp3 = new HashSet<String>();
                for (JsonElement el : physicsEl.getAsJsonArray()) {
                    String name;
                    if (el == null || !el.isJsonPrimitive() || (name = el.getAsString()) == null || name.isBlank()) continue;
                    tmp3.add(name.trim());
                }
                if (!tmp3.isEmpty()) {
                    disablePhysicsBones = tmp3;
                }
            }
            JsonElement cameraTransparentEl = null;
            if (json.has("camera_transparent_bones")) {
                cameraTransparentEl = json.get("camera_transparent_bones");
            } else if (json.has("inside_visible_bones")) {
                cameraTransparentEl = json.get("inside_visible_bones");
            }
            if (cameraTransparentEl != null && !(tmp = BedrockItemDefinition.parseBoneNameSet(cameraTransparentEl)).isEmpty()) {
                cameraTransparentBones = tmp;
            }
            List<String> definitionParticleEffects = BedrockItemDefinition.readDefinitionParticleEffects(json);
            BedrockItemDefinition bedrockItemDefinition = new BedrockItemDefinition(id, items, geoId, textureId, animations, modelParts, definitionParticleEffects, boneEmission, boneTextures, boneTextureAnim, dynamicBoneTextures, boneMirror, bonePhysics, disablePhysicsBones, cameraTransparentBones, boneEmissionBlur, boneEmissionClamp, boneEmissionStrength, textureAnimFrames, textureAnimFrameTime, textureAnimFrameHeight, physicsFeedback, display, customName);
            return bedrockItemDefinition;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    static Map<String, BonePhysicsSpec> parseBonePhysics(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Map.of();
        }
        HashMap<String, BonePhysicsSpec> out = new HashMap<String, BonePhysicsSpec>();
        if (element.isJsonObject()) {
            BedrockItemDefinition.appendBonePhysicsObject(out, element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (child == null || !child.isJsonObject()) continue;
                BedrockItemDefinition.appendBonePhysicsObject(out, child.getAsJsonObject());
            }
        }
        return out.isEmpty() ? Map.of() : out;
    }

    private static List<DynamicBoneTextureSpec> parseDynamicBoneTextures(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        ArrayList<DynamicBoneTextureSpec> out = new ArrayList<DynamicBoneTextureSpec>();
        if (element.isJsonObject()) {
            DynamicBoneTextureSpec spec = DynamicBoneTextureSpec.fromJson(element.getAsJsonObject());
            if (spec != null) {
                out.add(spec);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                DynamicBoneTextureSpec spec;
                if (child == null || !child.isJsonObject() || (spec = DynamicBoneTextureSpec.fromJson(child.getAsJsonObject())) == null) continue;
                out.add(spec);
            }
        }
        return out.isEmpty() ? List.of() : out;
    }

    private static void appendBonePhysicsObject(Map<String, BonePhysicsSpec> out, JsonObject object) {
        if (object == null) {
            return;
        }
        for (Map.Entry entry : object.entrySet()) {
            BonePhysicsSpec spec;
            String key = (String)entry.getKey();
            if (key == null || key.isBlank() || (spec = BonePhysicsSpec.fromJson((JsonElement)entry.getValue())) == null || spec.isEmpty()) continue;
            BonePhysicsSpec previous = out.get(key = key.trim());
            out.put(key, previous == null ? spec : previous.merge(spec));
        }
    }

    private static Set<String> parseBoneNameSet(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return Set.of();
        }
        HashSet<String> out = new HashSet<String>();
        for (JsonElement el : element.getAsJsonArray()) {
            String value;
            if (el == null || !el.isJsonPrimitive() || (value = el.getAsString()) == null || value.isBlank()) continue;
            out.add(value.trim());
        }
        return out;
    }

    private static List<String> readDefinitionParticleEffects(JsonObject json) {
        if (json == null) {
            return List.of();
        }
        ArrayList<String> out = new ArrayList<String>();
        BedrockItemDefinition.appendDefinitionParticleEffects(out, json.get("particle"));
        BedrockItemDefinition.appendDefinitionParticleEffects(out, json.get("particles"));
        BedrockItemDefinition.appendDefinitionParticleEffects(out, json.get("particle_effects"));
        BedrockItemDefinition.appendDefinitionParticleEffects(out, json.get("particle_emission"));
        BedrockItemDefinition.appendDefinitionParticleEffects(out, json.get("particleEmission"));
        return out.isEmpty() ? List.of() : out;
    }

    private static void appendDefinitionParticleEffects(List<String> out, JsonElement element) {
        String normalized;
        if (out == null || element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                BedrockItemDefinition.appendDefinitionParticleEffects(out, child);
            }
            return;
        }
        if (element.isJsonObject()) {
            String fromObject = BedrockItemDefinition.definitionParticleObjectToScript(element.getAsJsonObject());
            if (fromObject != null && !fromObject.isBlank()) {
                out.add(fromObject);
            }
            return;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() && (normalized = BedrockItemDefinition.normalizeDefinitionParticleEffect(element.getAsString())) != null && !normalized.isBlank()) {
            out.add(normalized);
        }
    }

    private static String normalizeDefinitionParticleEffect(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("particle(") && trimmed.endsWith(")")) {
            return trimmed;
        }
        if (trimmed.contains("=")) {
            return "particle(" + trimmed + ")";
        }
        class_2960 parsed = class_2960.method_12829((String)trimmed);
        if (parsed != null) {
            return "particle(type=" + String.valueOf(parsed) + ")";
        }
        return null;
    }

    private static String definitionParticleObjectToScript(JsonObject object) {
        if (object == null) {
            return null;
        }
        if (object.has("enabled") && object.get("enabled").isJsonPrimitive() && !object.get("enabled").getAsBoolean()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Map.Entry entry : object.entrySet()) {
            String key = (String)entry.getKey();
            if (key == null || key.isBlank() || "enabled".equalsIgnoreCase(key) || BedrockItemDefinition.isIgnoredDefinitionParticleRenderHint(key)) continue;
            if ("triggers".equalsIgnoreCase(key) && entry.getValue() != null && ((JsonElement)entry.getValue()).isJsonObject()) {
                BedrockItemDefinition.appendDefinitionTriggerTokens(joiner, ((JsonElement)entry.getValue()).getAsJsonObject());
                continue;
            }
            if ("on_animation".equalsIgnoreCase(key) && entry.getValue() != null && ((JsonElement)entry.getValue()).isJsonObject()) {
                BedrockItemDefinition.appendOnAnimationTokens(joiner, ((JsonElement)entry.getValue()).getAsJsonObject());
                continue;
            }
            String value = BedrockItemDefinition.definitionParticleValueToToken((JsonElement)entry.getValue());
            if (value == null || value.isBlank()) continue;
            joiner.add(key.trim() + "=" + value);
        }
        String body = joiner.toString();
        if (body.isBlank()) {
            return null;
        }
        return "particle(" + body + ")";
    }

    private static boolean isIgnoredDefinitionParticleRenderHint(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return "emissive_strength".equals(normalized) || "emission_strength".equals(normalized) || "emissive_blur".equals(normalized) || "emission_blur".equals(normalized) || "emissive_clamp".equals(normalized) || "emission_clamp".equals(normalized);
    }

    private static String definitionParticleValueToToken(JsonElement value) {
        JsonObject obj;
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        if (value.isJsonArray()) {
            JsonArray arr = value.getAsJsonArray();
            boolean allPrimitive = true;
            for (JsonElement element : arr) {
                if (element != null && element.isJsonPrimitive()) continue;
                allPrimitive = false;
                break;
            }
            if (!allPrimitive) {
                return null;
            }
            if (arr.size() == 3) {
                StringJoiner vecJoiner = new StringJoiner(":");
                for (JsonElement element : arr) {
                    vecJoiner.add(element.getAsString());
                }
                return vecJoiner.toString();
            }
            StringJoiner listJoiner = new StringJoiner("|");
            for (JsonElement element : arr) {
                listJoiner.add(element.getAsString());
            }
            return listJoiner.toString();
        }
        if (value.isJsonObject() && (obj = value.getAsJsonObject()).has("x") && obj.has("y") && obj.has("z") && obj.get("x").isJsonPrimitive() && obj.get("y").isJsonPrimitive() && obj.get("z").isJsonPrimitive()) {
            return obj.get("x").getAsString() + ":" + obj.get("y").getAsString() + ":" + obj.get("z").getAsString();
        }
        return null;
    }

    private static void appendDefinitionTriggerTokens(StringJoiner joiner, JsonObject triggers) {
        JsonElement biome;
        List<String> biomeNames;
        if (joiner == null || triggers == null) {
            return;
        }
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_jump", "jump", "triggerJump");
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_landing", "landing", "triggerLanding");
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_walking", "walking", "walkgin", "triggerWalking");
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_sprinting", "sprinting", "triggerSprinting");
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_swimming", "swimming", "swiming", "triggerSwimming");
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_attacking", "attacking", "attack", "triggerAttacking");
        BedrockItemDefinition.appendTrueTrigger(joiner, triggers, "trigger_mining", "mining", "triggerMining");
        JsonElement falling = triggers.get("falling");
        if (falling != null && !falling.isJsonNull()) {
            if (falling.isJsonPrimitive()) {
                if (BedrockItemDefinition.readBoolean(falling, false)) {
                    joiner.add("trigger_falling=true");
                }
            } else if (falling.isJsonObject()) {
                JsonObject obj = falling.getAsJsonObject();
                joiner.add("trigger_falling=true");
                Float minBlocks = BedrockItemDefinition.readFallingMinBlocks(obj);
                if (minBlocks != null) {
                    joiner.add("trigger_falling_min_blocks=" + minBlocks);
                }
            }
        }
        if (!(biomeNames = BedrockItemDefinition.readTriggerBiomeNames(biome = triggers.get("biome"))).isEmpty()) {
            joiner.add("trigger_biomes=" + String.join((CharSequence)"|", biomeNames));
        }
    }

    private static void appendOnAnimationTokens(StringJoiner joiner, JsonObject onAnimation) {
        String n;
        if (joiner == null || onAnimation == null) {
            return;
        }
        JsonElement name = onAnimation.get("name");
        JsonElement timecode = onAnimation.get("timecode");
        if (name != null && name.isJsonPrimitive() && (n = name.getAsString()) != null && !n.isBlank()) {
            joiner.add("on_animation_name=" + n.trim());
        }
        if (timecode != null && timecode.isJsonPrimitive()) {
            joiner.add("on_animation_timecode=" + timecode.getAsString());
        }
    }

    private static void appendTrueTrigger(StringJoiner joiner, JsonObject triggers, String tokenKey, String ... keys) {
        if (joiner == null || triggers == null || tokenKey == null || keys == null || keys.length == 0) {
            return;
        }
        for (String key : keys) {
            JsonElement element;
            if (key == null || key.isBlank() || !BedrockItemDefinition.readBoolean(element = triggers.get(key), false)) continue;
            joiner.add(tokenKey + "=true");
            return;
        }
    }

    private static boolean readBoolean(JsonElement element, boolean fallback) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt() != 0;
            }
            if (element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString();
                if (value == null) {
                    return fallback;
                }
                String normalized = value.trim().toLowerCase(Locale.ROOT);
                if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
                    return true;
                }
                if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
                    return false;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return fallback;
    }

    private static Float readFallingMinBlocks(JsonObject obj) {
        String[] keys;
        if (obj == null) {
            return null;
        }
        for (String key : keys = new String[]{"min_blocks", "minBlocks", "min_height", "minHeight", "min"}) {
            JsonElement element = obj.get(key);
            if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) continue;
            float value = element.getAsFloat();
            return Float.valueOf(value < 0.0f ? 0.0f : value);
        }
        return null;
    }

    private static List<String> readTriggerBiomeNames(JsonElement biome) {
        if (biome == null || biome.isJsonNull()) {
            return List.of();
        }
        ArrayList<String> out = new ArrayList<String>();
        if (biome.isJsonPrimitive() && biome.getAsJsonPrimitive().isString()) {
            String value = biome.getAsString();
            if (value != null && !value.isBlank()) {
                out.add(value.trim());
            }
            return out;
        }
        if (biome.isJsonArray()) {
            for (JsonElement element : biome.getAsJsonArray()) {
                String value;
                if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString() || (value = element.getAsString()) == null || value.isBlank()) continue;
                out.add(value.trim());
            }
            return out;
        }
        if (!biome.isJsonObject()) {
            return out;
        }
        JsonObject obj = biome.getAsJsonObject();
        JsonElement names = obj.get("names");
        if (names == null) {
            names = obj.get("biomes");
        }
        if (names != null) {
            out.addAll(BedrockItemDefinition.readTriggerBiomeNames(names));
            return out;
        }
        JsonElement one = obj.get("name");
        if (one == null) {
            one = obj.get("biome");
        }
        if (one != null) {
            out.addAll(BedrockItemDefinition.readTriggerBiomeNames(one));
        }
        return out;
    }

    private static DisplayTransform readDisplay(JsonObject displayObj, String key) {
        if (displayObj == null || key == null || !displayObj.has(key)) {
            return null;
        }
        JsonObject entry = displayObj.getAsJsonObject(key);
        return DisplayTransform.fromJson(entry);
    }

    class_2960 getId() {
        return this.id;
    }

    List<String> getItems() {
        return this.items;
    }

    class_2960 getGeoId() {
        return this.geoId;
    }

    class_2960 getTextureId() {
        return this.textureId;
    }

    List<class_2960> getAnimationFiles() {
        return this.animationFiles;
    }

    List<PunchyTuningConfig.CustomAnimationEntry> getModelPartsAnimation() {
        return this.modelPartsAnimation;
    }

    List<String> getDefinitionParticleEffects() {
        return this.definitionParticleEffects;
    }

    Map<String, Integer> getBoneEmission() {
        return this.boneEmission;
    }

    Map<String, class_2960> getBoneTextures() {
        return this.boneTextures;
    }

    Map<String, TextureAnimSpec> getBoneTextureAnim() {
        return this.boneTextureAnim;
    }

    List<DynamicBoneTextureSpec> getDynamicBoneTextures() {
        return this.dynamicBoneTextures;
    }

    Set<String> getDisablePhysicsBones() {
        return this.disablePhysicsBones;
    }

    Set<String> getCameraTransparentBones() {
        return this.cameraTransparentBones;
    }

    boolean isBoneEmissionBlur() {
        return this.boneEmissionBlur;
    }

    boolean isBoneEmissionClamp() {
        return this.boneEmissionClamp;
    }

    int getBoneEmissionStrength() {
        return this.boneEmissionStrength;
    }

    int getTextureAnimFrames() {
        return this.textureAnimFrames;
    }

    int getTextureAnimFrameTime() {
        return this.textureAnimFrameTime;
    }

    int getTextureAnimFrameHeight() {
        return this.textureAnimFrameHeight;
    }

    boolean isPhysicsFeedbackEnabled() {
        return this.physicsFeedback;
    }

    int getTexturePixelWidth() {
        return this.texturePixelWidth;
    }

    int getTexturePixelHeight() {
        return this.texturePixelHeight;
    }

    int[] getTexturePixelSize(class_2960 id) {
        if (id == null) {
            return null;
        }
        return this.textureSizeCache.get(id);
    }

    Map<String, BonePhysicsSpec> getBonePhysics() {
        return this.bonePhysics;
    }

    boolean hasCreatureBonePhysics() {
        if (this.bonePhysics == null || this.bonePhysics.isEmpty()) {
            return false;
        }
        for (BonePhysicsSpec spec : this.bonePhysics.values()) {
            if (spec == null || !spec.enableCreaturePhysics()) continue;
            return true;
        }
        return false;
    }

    boolean hasSlimeBonePhysics() {
        if (this.bonePhysics == null || this.bonePhysics.isEmpty()) {
            return false;
        }
        for (BonePhysicsSpec spec : this.bonePhysics.values()) {
            if (spec == null || !spec.enableSlimePhysics()) continue;
            return true;
        }
        return false;
    }

    boolean matchesCustomName(class_1799 stack) {
        if (this.customName == null || this.customName.isBlank()) {
            return true;
        }
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_2561 name = (class_2561)stack.method_57824(class_9334.field_49631);
        if (name == null) {
            return false;
        }
        String value = name.getString();
        if (value == null) {
            return false;
        }
        return value.trim().equalsIgnoreCase(this.customName.trim());
    }

    boolean hasCustomName() {
        return this.customName != null && !this.customName.isBlank();
    }

    DisplayTransform getDisplayTransform(class_1306 arm) {
        return this.displayTransforms.get(arm);
    }

    void loadResources(class_3300 manager) {
        this.geoModelCache.clear();
        this.geoModel = this.loadGeoModel(manager, this.geoId);
        this.animations.clear();
        if (manager == null) {
            return;
        }
        this.textureSizeCache.clear();
        if (this.textureId != null) {
            this.loadTextureSize(manager, this.textureId);
            int[] size = this.textureSizeCache.get(this.textureId);
            if (size != null) {
                this.texturePixelWidth = size[0];
                this.texturePixelHeight = size[1];
            }
        }
        for (class_2960 tex : this.boneTextures.values()) {
            this.loadTextureSize(manager, tex);
        }
        for (DynamicBoneTextureSpec spec : this.dynamicBoneTextures) {
            if (spec == null) continue;
            if (spec.isTextureType()) {
                this.loadTextureSize(manager, spec.fallbackResource());
                for (class_2960 tex : spec.resourceMap().values()) {
                    this.loadTextureSize(manager, tex);
                }
                continue;
            }
            if (spec.fallbackResource() != null) {
                this.loadGeoModel(manager, spec.fallbackResource());
            }
            for (class_2960 geo : spec.resourceMap().values()) {
                this.loadGeoModel(manager, geo);
            }
        }
        for (class_2960 animId : this.animationFiles) {
            Map<String, AnimationClip> loaded = BedrockItemAnimationLoader.loadAll(manager, animId);
            if (loaded == null || loaded.isEmpty()) continue;
            this.animations.putAll(loaded);
        }
        if (!this.boneMirror.isEmpty()) {
            this.applyBoneMirrorOverrides();
        }
    }

    BedrockItemGeoModel getGeoModel() {
        return this.geoModel;
    }

    BedrockItemGeoModel getGeoModel(class_2960 overrideGeoId) {
        if (overrideGeoId == null || overrideGeoId.equals((Object)this.geoId)) {
            return this.geoModel;
        }
        return this.geoModelCache.get(overrideGeoId);
    }

    Map<String, AnimationClip> getAnimations() {
        return this.animations;
    }

    private void applyBoneMirrorOverrides() {
        for (Map.Entry<String, AnimationClip> clipEntry : this.animations.entrySet()) {
            AnimationClip clip = clipEntry.getValue();
            if (clip == null) continue;
            for (Map.Entry<String, BoneTrack> trackEntry : clip.getTracks().entrySet()) {
                BoneTrack track;
                BoneMirrorSpec spec;
                String boneName = trackEntry.getKey();
                if (boneName == null || (spec = this.boneMirror.get(boneName)) == null || spec.isDefault() || (track = trackEntry.getValue()) == null) continue;
                for (Keyframe key : track.getKeyframes()) {
                    if (key == null) continue;
                    if (spec.mirrorPosition == Boolean.FALSE && key.position() != null) {
                        key.position().x = -key.position().x;
                    }
                    if (spec.mirrorRotation != Boolean.FALSE || key.rotationEuler() == null) continue;
                    key.rotationEuler().x = -key.rotationEuler().x;
                    key.rotationEuler().y = -key.rotationEuler().y;
                    if (key.rotation() == null) continue;
                    float rx = (float)Math.toRadians(key.rotationEuler().x);
                    float ry = (float)Math.toRadians(key.rotationEuler().y);
                    float rz = (float)Math.toRadians(key.rotationEuler().z);
                    key.rotation().set((Quaternionfc)new Quaternionf().rotationXYZ(rx, ry, rz));
                }
            }
        }
    }

    private BedrockItemGeoModel loadGeoModel(class_3300 manager, class_2960 modelId) {
        if (manager == null || modelId == null) {
            return null;
        }
        BedrockItemGeoModel cached = this.geoModelCache.get(modelId);
        if (cached != null) {
            return cached;
        }
        BedrockItemGeoModel loaded = BedrockItemGeoLoader.load(manager, modelId);
        if (loaded != null) {
            this.geoModelCache.put(modelId, loaded);
        }
        return loaded;
    }

    private static class_2960 normalizeTextureId(class_2960 id) {
        if (id == null) {
            return null;
        }
        String path = id.method_12832();
        if (path == null || path.isBlank()) {
            return id;
        }
        Object normalized = path;
        if (((String)normalized).startsWith("punchy/model_parts_items/textures/")) {
            if (!((String)normalized).endsWith(".png")) {
                normalized = (String)normalized + ".png";
            }
            return ResourceLocationCompat.fromNamespaceAndPath(id.method_12836(), (String)normalized);
        }
        if (!((String)normalized).startsWith("textures/")) {
            normalized = "textures/" + (String)normalized;
        }
        if (!((String)normalized).endsWith(".png")) {
            normalized = (String)normalized + ".png";
        }
        return ResourceLocationCompat.fromNamespaceAndPath(id.method_12836(), (String)normalized);
    }

    private static class_2960 normalizeGeoId(class_2960 id) {
        if (id == null) {
            return null;
        }
        String path = id.method_12832();
        if (path == null || path.isBlank()) {
            return id;
        }
        Object normalized = path;
        if (((String)normalized).startsWith("punchy/model_parts_items/geo/")) {
            if (!((String)normalized).endsWith(".geo.json")) {
                normalized = ((String)normalized).endsWith(".json") ? ((String)normalized).substring(0, ((String)normalized).length() - 5) + ".geo.json" : (String)normalized + ".geo.json";
            }
            return ResourceLocationCompat.fromNamespaceAndPath(id.method_12836(), (String)normalized);
        }
        if (!((String)normalized).startsWith("geo/")) {
            normalized = "geo/" + (String)normalized;
        }
        if (!((String)normalized).endsWith(".geo.json")) {
            normalized = ((String)normalized).endsWith(".json") ? ((String)normalized).substring(0, ((String)normalized).length() - 5) + ".geo.json" : (String)normalized + ".geo.json";
        }
        return ResourceLocationCompat.fromNamespaceAndPath(id.method_12836(), (String)normalized);
    }

    private void loadTextureSize(class_3300 manager, class_2960 id) {
        if (manager == null || id == null || this.textureSizeCache.containsKey(id)) {
            return;
        }
        try {
            Optional resourceOpt = manager.method_14486(id);
            if (resourceOpt.isEmpty()) {
                return;
            }
            try (InputStream stream = ((class_3298)resourceOpt.get()).method_14482();){
                class_1011 image = class_1011.method_4309((InputStream)stream);
                if (image != null) {
                    this.textureSizeCache.put(id, new int[]{image.method_4307(), image.method_4323()});
                    image.close();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    static final class DisplayTransform {
        final float rotX;
        final float rotY;
        final float rotZ;
        final float transX;
        final float transY;
        final float transZ;
        final float scaleX;
        final float scaleY;
        final float scaleZ;

        private DisplayTransform(float rotX, float rotY, float rotZ, float transX, float transY, float transZ, float scaleX, float scaleY, float scaleZ) {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            this.transX = transX;
            this.transY = transY;
            this.transZ = transZ;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
        }

        static DisplayTransform fromJson(JsonObject obj) {
            if (obj == null) {
                return null;
            }
            float[] rot = DisplayTransform.readVec3(obj, "rotation", 0.0f, 0.0f, 0.0f);
            float[] trans = DisplayTransform.readVec3(obj, "translation", 0.0f, 0.0f, 0.0f);
            float[] scale = DisplayTransform.readVec3(obj, "scale", 1.0f, 1.0f, 1.0f);
            return new DisplayTransform(rot[0], rot[1], rot[2], trans[0], trans[1], trans[2], scale[0], scale[1], scale[2]);
        }

        private static float[] readVec3(JsonObject obj, String key, float defX, float defY, float defZ) {
            if (obj == null || !obj.has(key)) {
                return new float[]{defX, defY, defZ};
            }
            JsonArray arr = obj.getAsJsonArray(key);
            if (arr == null || arr.size() < 3) {
                return new float[]{defX, defY, defZ};
            }
            return new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat()};
        }
    }

    static final class TextureAnimSpec {
        final int frames;
        final int frameTime;
        final int frameHeight;

        private TextureAnimSpec(int frames, int frameTime, int frameHeight) {
            this.frames = frames;
            this.frameTime = frameTime;
            this.frameHeight = frameHeight;
        }

        static TextureAnimSpec fromJson(JsonObject obj) {
            if (obj == null) {
                return null;
            }
            int frames = obj.has("frames") ? Math.max(1, obj.get("frames").getAsInt()) : 1;
            int frameTime = obj.has("frametime") ? Math.max(1, obj.get("frametime").getAsInt()) : 1;
            int frameHeight = obj.has("frame_height") ? Math.max(1, obj.get("frame_height").getAsInt()) : 0;
            return new TextureAnimSpec(frames, frameTime, frameHeight);
        }
    }

    static final class BoneMirrorSpec {
        final Boolean mirrorPosition;
        final Boolean mirrorRotation;

        private BoneMirrorSpec(Boolean mirrorPosition, Boolean mirrorRotation) {
            this.mirrorPosition = mirrorPosition;
            this.mirrorRotation = mirrorRotation;
        }

        boolean isDefault() {
            return this.mirrorPosition == null && this.mirrorRotation == null;
        }

        static BoneMirrorSpec fromJson(JsonElement el) {
            if (el == null || el.isJsonNull()) {
                return null;
            }
            if (el.isJsonPrimitive()) {
                boolean mirror = el.getAsBoolean();
                return new BoneMirrorSpec(mirror, mirror);
            }
            if (el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                Boolean pos = obj.has("position") ? Boolean.valueOf(obj.get("position").getAsBoolean()) : null;
                Boolean rot = obj.has("rotation") ? Boolean.valueOf(obj.get("rotation").getAsBoolean()) : null;
                return new BoneMirrorSpec(pos, rot);
            }
            return null;
        }
    }

    record DynamicBoneTextureSpec(String type, String bone, String source, class_2960 fallbackResource, Map<String, class_2960> resourceMap) {
        static final String TYPE_TEXTURE = "texture";
        static final String TYPE_GEO = "geo";
        static final String SOURCE_AXOL_VARIANTS = "axol_variants";

        DynamicBoneTextureSpec {
            type = type == null || type.isBlank() ? TYPE_TEXTURE : type.trim();
            resourceMap = resourceMap == null ? Map.of() : Map.copyOf(resourceMap);
        }

        boolean isTextureType() {
            return !TYPE_GEO.equalsIgnoreCase(this.type);
        }

        boolean isGeoType() {
            return TYPE_GEO.equalsIgnoreCase(this.type);
        }

        boolean usesAxolVariants() {
            return SOURCE_AXOL_VARIANTS.equalsIgnoreCase(this.source);
        }

        static DynamicBoneTextureSpec fromJson(JsonObject obj) {
            JsonObject mapObj;
            if (obj == null) {
                return null;
            }
            String type = DynamicBoneTextureSpec.readString(obj, "type");
            if (type == null || type.isBlank()) {
                type = TYPE_TEXTURE;
            }
            boolean geoType = TYPE_GEO.equalsIgnoreCase(type);
            String bone = DynamicBoneTextureSpec.readString(obj, "bone");
            if (!geoType && (bone == null || bone.isBlank())) {
                return null;
            }
            String source = DynamicBoneTextureSpec.readString(obj, "source");
            if (source != null) {
                source = source.trim();
            }
            class_2960 fallback = DynamicBoneTextureSpec.readResourceId(obj, "fallback", geoType);
            HashMap<String, class_2960> map = new HashMap<String, class_2960>();
            JsonObject jsonObject = mapObj = obj.has("map") && obj.get("map").isJsonObject() ? obj.getAsJsonObject("map") : null;
            if (mapObj != null) {
                for (Map.Entry entry : mapObj.entrySet()) {
                    class_2960 resource;
                    String key = (String)entry.getKey();
                    if (key == null || key.isBlank() || entry.getValue() == null || !((JsonElement)entry.getValue()).isJsonPrimitive() || (resource = class_2960.method_12829((String)((JsonElement)entry.getValue()).getAsString())) == null) continue;
                    map.put(key.trim(), geoType ? BedrockItemDefinition.normalizeGeoId(resource) : BedrockItemDefinition.normalizeTextureId(resource));
                }
            }
            if (fallback == null && map.isEmpty()) {
                return null;
            }
            return new DynamicBoneTextureSpec(type.trim(), bone == null ? null : bone.trim(), source, fallback, map);
        }

        private static String readString(JsonObject obj, String key) {
            if (obj == null || key == null || !obj.has(key) || obj.get(key) == null || !obj.get(key).isJsonPrimitive()) {
                return null;
            }
            String value = obj.get(key).getAsString();
            return value == null || value.isBlank() ? null : value.trim();
        }

        private static class_2960 readResourceId(JsonObject obj, String key, boolean geoType) {
            String value = DynamicBoneTextureSpec.readString(obj, key);
            if (value == null) {
                return null;
            }
            class_2960 id = class_2960.method_12829((String)value);
            if (id == null) {
                return null;
            }
            return geoType ? BedrockItemDefinition.normalizeGeoId(id) : BedrockItemDefinition.normalizeTextureId(id);
        }
    }

    record BonePhysicsSpec(boolean enableCreaturePhysics, boolean enableSlimePhysics, ToolTuning.PhysicsMode pendulumMode, ToolTuning.PhysicsSettings pendulumSettings, float impactInclinationPercent, float fallInclinationPercent, float impactTranslationPercent, float fallTranslationPercent, float slimeImpactPercent, float slimeFallPercent) {
        private static final float DEFAULT_INCLINATION_PERCENT = 100.0f;

        boolean isEmpty() {
            return !this.enableCreaturePhysics && !this.enableSlimePhysics && (this.pendulumMode == null || this.pendulumMode == ToolTuning.PhysicsMode.NONE);
        }

        float impactInclinationScale() {
            return this.impactInclinationPercent / 100.0f;
        }

        float fallInclinationScale() {
            return this.fallInclinationPercent / 100.0f;
        }

        float impactTranslationScale() {
            return this.impactTranslationPercent / 100.0f;
        }

        float fallTranslationScale() {
            return this.fallTranslationPercent / 100.0f;
        }

        float slimeImpactScale() {
            return this.slimeImpactPercent / 100.0f;
        }

        float slimeFallScale() {
            return this.slimeFallPercent / 100.0f;
        }

        BonePhysicsSpec merge(BonePhysicsSpec other) {
            if (other == null) {
                return this;
            }
            return new BonePhysicsSpec(this.enableCreaturePhysics || other.enableCreaturePhysics, this.enableSlimePhysics || other.enableSlimePhysics, BonePhysicsSpec.choosePendulumMode(this.pendulumMode, other.pendulumMode), BonePhysicsSpec.mergePendulumSettings(this.pendulumSettings, other.pendulumSettings), BonePhysicsSpec.choosePercent(this.impactInclinationPercent, other.impactInclinationPercent), BonePhysicsSpec.choosePercent(this.fallInclinationPercent, other.fallInclinationPercent), BonePhysicsSpec.choosePercent(this.impactTranslationPercent, other.impactTranslationPercent), BonePhysicsSpec.choosePercent(this.fallTranslationPercent, other.fallTranslationPercent), BonePhysicsSpec.choosePercent(this.slimeImpactPercent, other.slimeImpactPercent), BonePhysicsSpec.choosePercent(this.slimeFallPercent, other.slimeFallPercent));
        }

        static BonePhysicsSpec fromJson(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return null;
            }
            if (element.isJsonArray()) {
                BonePhysicsSpec merged = null;
                for (JsonElement child : element.getAsJsonArray()) {
                    BonePhysicsSpec childSpec = BonePhysicsSpec.fromJson(child);
                    if (childSpec == null) continue;
                    merged = merged == null ? childSpec : merged.merge(childSpec);
                }
                return merged;
            }
            if (element.isJsonPrimitive()) {
                boolean enabled = BedrockItemDefinition.readBoolean(element, false);
                return new BonePhysicsSpec(enabled, enabled, ToolTuning.PhysicsMode.NONE, null, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f);
            }
            if (!element.isJsonObject()) {
                return null;
            }
            JsonObject obj = element.getAsJsonObject();
            boolean creature = false;
            boolean slime = false;
            if (obj.has("enableCreaturePhysics")) {
                creature = BedrockItemDefinition.readBoolean(obj.get("enableCreaturePhysics"), false);
            } else if (obj.has("creature")) {
                creature = BedrockItemDefinition.readBoolean(obj.get("creature"), false);
            } else if (obj.has("puffer")) {
                creature = BedrockItemDefinition.readBoolean(obj.get("puffer"), false);
            }
            if (obj.has("enableSlimePhysics")) {
                slime = BedrockItemDefinition.readBoolean(obj.get("enableSlimePhysics"), false);
            } else if (obj.has("slime")) {
                slime = BedrockItemDefinition.readBoolean(obj.get("slime"), false);
            }
            ToolTuning.PhysicsMode pendulumMode = BonePhysicsSpec.readPendulumMode(obj);
            ToolTuning.PhysicsSettings pendulumSettings = BonePhysicsSpec.readPendulumSettings(obj);
            float impactPercent = BonePhysicsSpec.readPercent(obj, "impactInclinationPercent", "impact_inclination_percent", "impactInclination", "impact_inclination", "landingInclinationPercent", "landing_inclination_percent", "landingInclination", "landing_inclination");
            float fallPercent = BonePhysicsSpec.readPercent(obj, "fallInclinationPercent", "fall_inclination_percent", "fallInclination", "fall_inclination", "upwardInclinationPercent", "upward_inclination_percent", "upwardInclination", "upward_inclination");
            float impactTranslationPercent = BonePhysicsSpec.readPercent(obj, "impactTranslationPercent", "impact_translation_percent", "landingTranslationPercent", "landing_translation_percent", "impactTranslation", "impact_translation", "landingTranslation", "landing_translation");
            float fallTranslationPercent = BonePhysicsSpec.readPercent(obj, "fallTranslationPercent", "fall_translation_percent", "upwardTranslationPercent", "upward_translation_percent", "fallTranslation", "fall_translation", "upwardTranslation", "upward_translation");
            float slimeImpactPercent = BonePhysicsSpec.readPercent(obj, "slimeImpactPercent", "slime_impact_percent", "impactSlimePercent", "impact_slime_percent", "slimeImpact", "slime_impact");
            float slimeFallPercent = BonePhysicsSpec.readPercent(obj, "slimeFallPercent", "slime_fall_percent", "fallSlimePercent", "fall_slime_percent", "slimeFall", "slime_fall");
            return new BonePhysicsSpec(creature, slime, pendulumMode, pendulumSettings, impactPercent, fallPercent, impactTranslationPercent, fallTranslationPercent, slimeImpactPercent, slimeFallPercent);
        }

        private static float choosePercent(float current, float other) {
            return other != 100.0f ? other : current;
        }

        private static ToolTuning.PhysicsMode choosePendulumMode(ToolTuning.PhysicsMode current, ToolTuning.PhysicsMode other) {
            return other != null && other != ToolTuning.PhysicsMode.NONE ? other : current;
        }

        private static ToolTuning.PhysicsSettings mergePendulumSettings(ToolTuning.PhysicsSettings current, ToolTuning.PhysicsSettings other) {
            if (current == null) {
                return other == null ? null : other.copy();
            }
            if (other == null) {
                return current.copy();
            }
            ToolTuning.PhysicsSettings merged = current.copy();
            if (other.limitDeg != null) {
                merged.limitDeg = other.limitDeg;
            }
            if (other.limitDegUp != null) {
                merged.limitDegUp = other.limitDegUp;
            }
            if (other.limitDegDown != null) {
                merged.limitDegDown = other.limitDegDown;
            }
            if (other.force != null) {
                merged.force = other.force;
            }
            if (other.speed != null) {
                merged.speed = other.speed;
            }
            if (other.cameraSens != null) {
                merged.cameraSens = other.cameraSens;
            }
            if (other.gravity != null) {
                merged.gravity = other.gravity;
            }
            if (other.motionSens != null) {
                merged.motionSens = other.motionSens;
            }
            if (other.invert != null) {
                merged.invert = other.invert;
            }
            return merged;
        }

        private static float readPercent(JsonObject obj, String ... keys) {
            if (obj == null || keys == null) {
                return 100.0f;
            }
            for (String key : keys) {
                JsonElement value;
                if (key == null || !obj.has(key) || (value = obj.get(key)) == null || value.isJsonNull()) continue;
                try {
                    return class_3532.method_15363((float)value.getAsFloat(), (float)0.0f, (float)100.0f);
                }
                catch (RuntimeException runtimeException) {
                    // empty catch block
                }
            }
            return 100.0f;
        }

        private static ToolTuning.PhysicsMode readPendulumMode(JsonObject obj) {
            if (obj == null) {
                return ToolTuning.PhysicsMode.NONE;
            }
            ToolTuning.PhysicsMode parsed = BonePhysicsSpec.parsePendulumMode(obj.get("pendulum_mode"));
            if (parsed != ToolTuning.PhysicsMode.NONE) {
                return parsed;
            }
            parsed = BonePhysicsSpec.parsePendulumMode(obj.get("pendulumMode"));
            if (parsed != ToolTuning.PhysicsMode.NONE) {
                return parsed;
            }
            parsed = BonePhysicsSpec.parsePendulumMode(obj.get("pendulum"));
            if (parsed != ToolTuning.PhysicsMode.NONE) {
                return parsed;
            }
            return BonePhysicsSpec.parsePendulumMode(obj.get("physics_mode"));
        }

        private static ToolTuning.PhysicsMode parsePendulumMode(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return ToolTuning.PhysicsMode.NONE;
            }
            try {
                String normalized;
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                    return element.getAsBoolean() ? ToolTuning.PhysicsMode.FULL_PENDULUM : ToolTuning.PhysicsMode.NONE;
                }
                String value = element.getAsString();
                if (value == null) {
                    return ToolTuning.PhysicsMode.NONE;
                }
                return switch (normalized = value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_')) {
                    case "full", "pendulum_full", "full_pendulum" -> ToolTuning.PhysicsMode.FULL_PENDULUM;
                    case "forward", "foward", "pendulum_forward", "pendulum_foward", "forward_pendulum", "foward_pendulum" -> ToolTuning.PhysicsMode.FORWARD_PENDULUM;
                    default -> ToolTuning.PhysicsMode.NONE;
                };
            }
            catch (RuntimeException ignored) {
                return ToolTuning.PhysicsMode.NONE;
            }
        }

        private static ToolTuning.PhysicsSettings readPendulumSettings(JsonObject obj) {
            if (obj == null) {
                return null;
            }
            ToolTuning.PhysicsSettings settings = new ToolTuning.PhysicsSettings();
            settings.limitDeg = BonePhysicsSpec.readOptionalFloat(obj, "limit_deg", "limitDeg", "pendulum_limit_deg", "pendulumLimitDeg");
            settings.limitDegUp = BonePhysicsSpec.readOptionalFloat(obj, "limit_deg_up", "limitDegUp", "pendulum_limit_deg_up", "pendulumLimitDegUp");
            settings.limitDegDown = BonePhysicsSpec.readOptionalFloat(obj, "limit_deg_down", "limitDegDown", "pendulum_limit_deg_down", "pendulumLimitDegDown");
            settings.force = BonePhysicsSpec.readOptionalFloat(obj, "force", "pendulum_force", "pendulumForce");
            settings.speed = BonePhysicsSpec.readOptionalFloat(obj, "speed", "pendulum_speed", "pendulumSpeed");
            settings.cameraSens = BonePhysicsSpec.readOptionalFloat(obj, "camera_sens", "cameraSens", "pendulum_camera_sens", "pendulumCameraSens");
            settings.gravity = BonePhysicsSpec.readOptionalFloat(obj, "gravity", "pendulum_gravity", "pendulumGravity");
            settings.motionSens = BonePhysicsSpec.readOptionalFloat(obj, "motion_sens", "motionSens", "pendulum_motion_sens", "pendulumMotionSens");
            settings.invert = BonePhysicsSpec.readOptionalBoolean(obj, "invert", "pendulum_invert", "pendulumInvert");
            return BonePhysicsSpec.hasPendulumSettings(settings) ? settings : null;
        }

        private static boolean hasPendulumSettings(ToolTuning.PhysicsSettings settings) {
            return settings != null && (settings.limitDeg != null || settings.limitDegUp != null || settings.limitDegDown != null || settings.force != null || settings.speed != null || settings.cameraSens != null || settings.gravity != null || settings.motionSens != null || settings.invert != null);
        }

        private static Float readOptionalFloat(JsonObject obj, String ... keys) {
            if (obj == null || keys == null) {
                return null;
            }
            for (String key : keys) {
                JsonElement value;
                if (key == null || !obj.has(key) || (value = obj.get(key)) == null || value.isJsonNull()) continue;
                try {
                    return Float.valueOf(value.getAsFloat());
                }
                catch (RuntimeException runtimeException) {
                    // empty catch block
                }
            }
            return null;
        }

        private static Boolean readOptionalBoolean(JsonObject obj, String ... keys) {
            if (obj == null || keys == null) {
                return null;
            }
            for (String key : keys) {
                JsonElement value;
                if (key == null || !obj.has(key) || (value = obj.get(key)) == null || value.isJsonNull()) continue;
                try {
                    return value.getAsBoolean();
                }
                catch (RuntimeException runtimeException) {
                    // empty catch block
                }
            }
            return null;
        }
    }
}

