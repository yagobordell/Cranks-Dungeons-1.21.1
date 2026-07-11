/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_5603
 *  net.minecraft.class_5606
 *  net.minecraft.class_5607
 *  net.minecraft.class_5609
 *  net.minecraft.class_5610
 *  net.minecraft.class_630
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package punchy.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_5603;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_630;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class SimpleBedrockGeoThirdPerson {
    public static Map<String, class_630> loadedParts = new HashMap<String, class_630>();
    public static Map<String, Vector3f> bedrockLocalPivots = new HashMap<String, Vector3f>();
    public static Map<String, String> parentByName = new HashMap<String, String>();
    public static Map<String, Matrix4f> restWorldByName = new HashMap<String, Matrix4f>();

    public static void load(class_2960 geoId) {
        try {
            String name;
            JsonObject bone;
            class_3298 resource = class_310.method_1551().method_1478().getResourceOrThrow(geoId);
            JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(resource.method_14482())).getAsJsonObject();
            JsonArray geos = json.getAsJsonArray("minecraft:geometry");
            if (geos == null || geos.isEmpty()) {
                return;
            }
            JsonObject geo = geos.get(0).getAsJsonObject();
            JsonArray bones = geo.getAsJsonArray("bones");
            if (bones == null) {
                return;
            }
            class_5609 mesh = new class_5609();
            class_5610 root = mesh.method_32111();
            HashMap<String, class_5610> definitions = new HashMap<String, class_5610>();
            HashMap<String, float[]> pivotByName = new HashMap<String, float[]>();
            bedrockLocalPivots.clear();
            parentByName.clear();
            for (JsonElement boneEl : bones) {
                bone = boneEl.getAsJsonObject();
                name = bone.get("name").getAsString();
                if (bone.has("parent")) {
                    parentByName.put(name, bone.get("parent").getAsString());
                }
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
            for (JsonElement boneEl : bones) {
                class_5610 parentDef;
                float[] pPivot;
                bone = boneEl.getAsJsonObject();
                name = bone.get("name").getAsString();
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
                bedrockLocalPivots.put(name, new Vector3f(localX, localY, localZ));
                class_5606 cubes = class_5606.method_32108();
                if (bone.has("cubes")) {
                    for (JsonElement cubeEl : bone.getAsJsonArray("cubes")) {
                        JsonObject cube = cubeEl.getAsJsonObject();
                        JsonArray origin = cube.getAsJsonArray("origin");
                        JsonArray size = cube.getAsJsonArray("size");
                        Iterator uv = cube.getAsJsonArray("uv");
                        float ox = origin.get(0).getAsFloat();
                        float oy = origin.get(1).getAsFloat();
                        float oz = origin.get(2).getAsFloat();
                        float sx = size.get(0).getAsFloat();
                        float sy = size.get(1).getAsFloat();
                        float sz = size.get(2).getAsFloat();
                        int u = uv.get(0).getAsInt();
                        int v = uv.get(1).getAsInt();
                        float mcBoxX = ox - px;
                        float mcBoxY = py - (oy + sy);
                        float mcBoxZ = oz - pz;
                        cubes.method_32101(u, v).method_32097(mcBoxX, mcBoxY, mcBoxZ, sx, sy, sz);
                    }
                }
                class_5610 class_56102 = parentDef = parentName != null ? (class_5610)definitions.get(parentName) : root;
                if (parentDef == null) {
                    parentDef = root;
                }
                float renderPX = px;
                float renderPY = 28.0f - py;
                float renderPZ = pz;
                if (parentName != null) {
                    JsonObject pBone = null;
                    for (JsonElement el : bones) {
                        if (!el.getAsJsonObject().get("name").getAsString().equals(parentName)) continue;
                        pBone = el.getAsJsonObject();
                        break;
                    }
                    if (pBone != null && pBone.has("pivot")) {
                        JsonArray pPivot2 = pBone.getAsJsonArray("pivot");
                        renderPX = px - pPivot2.get(0).getAsFloat();
                        renderPY = pPivot2.get(1).getAsFloat() - py;
                        renderPZ = pPivot2.get(2).getAsFloat() - pz;
                    }
                }
                renderPZ = -renderPZ;
                float radX = (float)Math.toRadians(rx);
                float radY = (float)Math.toRadians(ry);
                float radZ = (float)Math.toRadians(rz);
                definitions.put(name, parentDef.method_32117(name, cubes, class_5603.method_32091((float)renderPX, (float)renderPY, (float)renderPZ, (float)radX, (float)radY, (float)radZ)));
            }
            class_630 rootPart = class_5607.method_32110((class_5609)mesh, (int)64, (int)64).method_32109();
            loadedParts.clear();
            restWorldByName.clear();
            for (JsonElement boneEl : bones) {
                JsonObject bone2 = boneEl.getAsJsonObject();
                String name2 = bone2.get("name").getAsString();
                try {
                    class_630 part = SimpleBedrockGeoThirdPerson.findChildRecursive(rootPart, name2);
                    if (part == null) continue;
                    loadedParts.put(name2, part);
                }
                catch (Exception exception) {}
            }
            for (String boneName : loadedParts.keySet()) {
                Matrix4f world = SimpleBedrockGeoThirdPerson.buildRestWorldMatrix(boneName, new HashSet<String>());
                if (world == null) continue;
                restWorldByName.put(boneName, world);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static class_630 findChildRecursive(class_630 root, String name) {
        if (root == null || name == null) {
            return null;
        }
        try {
            class_630 direct = root.method_32086(name);
            if (direct != null) {
                return direct;
            }
        }
        catch (Throwable direct) {
            // empty catch block
        }
        Map<String, class_630> children = SimpleBedrockGeoThirdPerson.getChildren(root);
        if (children == null || children.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, class_630> entry : children.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.equals(name)) continue;
            return entry.getValue();
        }
        for (class_630 child : children.values()) {
            class_630 found = SimpleBedrockGeoThirdPerson.findChildRecursive(child, name);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    private static Map<String, class_630> getChildren(class_630 part) {
        for (Class<?> type = part.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!Map.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(part);
                    if (!(value instanceof Map)) continue;
                    Map map = (Map)value;
                    return map;
                }
                catch (IllegalAccessException illegalAccessException) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    private static Matrix4f buildRestWorldMatrix(String boneName, Set<String> visited) {
        if (boneName == null || visited.contains(boneName)) {
            return null;
        }
        visited.add(boneName);
        class_630 part = loadedParts.get(boneName);
        if (part == null) {
            return null;
        }
        Matrix4f local = new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
        String parent = parentByName.get(boneName);
        if (parent == null) {
            return local;
        }
        Matrix4f parentWorld = SimpleBedrockGeoThirdPerson.buildRestWorldMatrix(parent, visited);
        if (parentWorld == null) {
            return local;
        }
        return parentWorld.mul((Matrix4fc)local, new Matrix4f());
    }
}

