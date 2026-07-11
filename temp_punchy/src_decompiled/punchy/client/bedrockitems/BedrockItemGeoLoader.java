/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_2350
 *  net.minecraft.class_2960
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_5603
 *  net.minecraft.class_5606
 *  net.minecraft.class_5607
 *  net.minecraft.class_5609
 *  net.minecraft.class_5610
 *  net.minecraft.class_630
 */
package punchy.client.bedrockitems;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2350;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_5603;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_630;
import punchy.client.bedrockitems.BedrockItemGeoModel;
import punchy.mixin.client.accessor.ModelPartAccessor;

final class BedrockItemGeoLoader {
    private BedrockItemGeoLoader() {
    }

    static BedrockItemGeoModel load(class_3300 manager, class_2960 geoId) {
        if (manager == null || geoId == null) {
            return null;
        }
        try {
            String name;
            JsonObject bone;
            class_3298 resource = manager.getResourceOrThrow(geoId);
            JsonObject json = JsonParser.parseReader((Reader)new InputStreamReader(resource.method_14482())).getAsJsonObject();
            JsonArray geos = json.getAsJsonArray("minecraft:geometry");
            if (geos == null || geos.isEmpty()) {
                return null;
            }
            JsonObject geo = geos.get(0).getAsJsonObject();
            JsonArray bones = geo.getAsJsonArray("bones");
            if (bones == null) {
                return null;
            }
            int texW = 64;
            int texH = 64;
            if (geo.has("description")) {
                JsonObject desc = geo.getAsJsonObject("description");
                if (desc != null) {
                    if (desc.has("texture_width")) {
                        texW = desc.get("texture_width").getAsInt();
                    }
                    if (desc.has("texture_height")) {
                        texH = desc.get("texture_height").getAsInt();
                    }
                }
            } else {
                if (geo.has("texture_width")) {
                    texW = geo.get("texture_width").getAsInt();
                }
                if (geo.has("texture_height")) {
                    texH = geo.get("texture_height").getAsInt();
                }
            }
            class_5609 mesh = new class_5609();
            class_5610 root = mesh.method_32111();
            HashMap<String, class_5610> definitions = new HashMap<String, class_5610>();
            HashMap<String, float[]> pivotByName = new HashMap<String, float[]>();
            HashMap<String, String> parentByBone = new HashMap<String, String>();
            HashMap<String, List<BedrockItemGeoModel.BedrockCube>> cubesByBone = new HashMap<String, List<BedrockItemGeoModel.BedrockCube>>();
            for (JsonElement boneEl : bones) {
                bone = boneEl.getAsJsonObject();
                name = bone.get("name").getAsString();
                float px = 0.0f;
                float py = 0.0f;
                float pz = 0.0f;
                if (bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    px = BedrockItemGeoLoader.getFloat(pivot, 0);
                    py = BedrockItemGeoLoader.getFloat(pivot, 1);
                    pz = BedrockItemGeoLoader.getFloat(pivot, 2);
                }
                pivotByName.put(name, new float[]{px, py, pz});
            }
            for (JsonElement boneEl : bones) {
                float[] pPivot;
                class_5610 parentDef;
                String parentName;
                bone = boneEl.getAsJsonObject();
                name = bone.get("name").getAsString();
                String string = parentName = bone.has("parent") ? bone.get("parent").getAsString() : null;
                if (parentName != null) {
                    parentByBone.put(name, parentName);
                }
                float px = 0.0f;
                float py = 0.0f;
                float pz = 0.0f;
                float rx = 0.0f;
                float ry = 0.0f;
                float rz = 0.0f;
                if (bone.has("pivot")) {
                    JsonArray pivot = bone.getAsJsonArray("pivot");
                    px = BedrockItemGeoLoader.getFloat(pivot, 0);
                    py = BedrockItemGeoLoader.getFloat(pivot, 1);
                    pz = BedrockItemGeoLoader.getFloat(pivot, 2);
                }
                if (bone.has("rotation")) {
                    JsonArray rotation = bone.getAsJsonArray("rotation");
                    rx = -BedrockItemGeoLoader.getFloat(rotation, 0);
                    ry = -BedrockItemGeoLoader.getFloat(rotation, 1);
                    rz = BedrockItemGeoLoader.getFloat(rotation, 2);
                }
                class_5606 cubes = class_5606.method_32108();
                if (bone.has("cubes")) {
                    for (JsonElement cubeEl : bone.getAsJsonArray("cubes")) {
                        JsonArray uv;
                        JsonObject uvObj;
                        Map<class_2350, BedrockItemGeoModel.BedrockFace> faces;
                        JsonArray cubeRot;
                        JsonArray cubePivot;
                        JsonObject cube = cubeEl.getAsJsonObject();
                        JsonArray origin = cube.getAsJsonArray("origin");
                        JsonArray size = cube.getAsJsonArray("size");
                        if (origin == null || size == null) continue;
                        float ox = BedrockItemGeoLoader.getFloat(origin, 0);
                        float oy = BedrockItemGeoLoader.getFloat(origin, 1);
                        float oz = BedrockItemGeoLoader.getFloat(origin, 2);
                        float sx = BedrockItemGeoLoader.getFloat(size, 0);
                        float sy = BedrockItemGeoLoader.getFloat(size, 1);
                        float sz = BedrockItemGeoLoader.getFloat(size, 2);
                        float inflate = 0.0f;
                        if (cube.has("inflate") && (inflate = cube.get("inflate").getAsFloat()) < 0.0f) {
                            inflate = 0.0f;
                        }
                        boolean mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
                        float pivotX = ox;
                        float pivotY = oy;
                        float pivotZ = oz;
                        if (cube.has("pivot") && (cubePivot = cube.getAsJsonArray("pivot")) != null && cubePivot.size() >= 3) {
                            pivotX = BedrockItemGeoLoader.getFloat(cubePivot, 0);
                            pivotY = BedrockItemGeoLoader.getFloat(cubePivot, 1);
                            pivotZ = BedrockItemGeoLoader.getFloat(cubePivot, 2);
                        }
                        float rotX = 0.0f;
                        float rotY = 0.0f;
                        float rotZ = 0.0f;
                        if (cube.has("rotation") && (cubeRot = cube.getAsJsonArray("rotation")) != null && cubeRot.size() >= 3) {
                            rotX = -BedrockItemGeoLoader.getFloat(cubeRot, 0);
                            rotY = -BedrockItemGeoLoader.getFloat(cubeRot, 1);
                            rotZ = BedrockItemGeoLoader.getFloat(cubeRot, 2);
                        }
                        if (inflate > 0.0f) {
                            ox -= inflate;
                            oy -= inflate;
                            oz -= inflate;
                            sx += inflate * 2.0f;
                            sy += inflate * 2.0f;
                            sz += inflate * 2.0f;
                        }
                        float localOx = ox - px;
                        float localOy = oy - py;
                        float localOz = oz - pz;
                        float mcBoxX = BedrockItemGeoLoader.toModelX(localOx + sx);
                        float mcBoxY = BedrockItemGeoLoader.toModelY(localOy);
                        float mcBoxZ = BedrockItemGeoLoader.toModelZ(localOz);
                        float x2 = mcBoxX + sx;
                        float y2 = mcBoxY + sy;
                        float z2 = mcBoxZ + sz;
                        float localPivotX = BedrockItemGeoLoader.toModelX(pivotX - px);
                        float localPivotY = BedrockItemGeoLoader.toModelY(pivotY - py);
                        float localPivotZ = BedrockItemGeoLoader.toModelZ(pivotZ - pz);
                        float radX = (float)Math.toRadians(rotX);
                        float radY = (float)Math.toRadians(rotY);
                        float radZ = (float)Math.toRadians(rotZ);
                        if (cube.has("uv") && cube.get("uv").isJsonObject() && !(faces = BedrockItemGeoLoader.parseFaces(uvObj = cube.getAsJsonObject("uv"))).isEmpty()) {
                            cubesByBone.computeIfAbsent(name, key -> new ArrayList()).add(new BedrockItemGeoModel.BedrockCube(mcBoxX, mcBoxY, mcBoxZ, x2, y2, z2, faces, mirror, localPivotX, localPivotY, localPivotZ, radX, radY, radZ));
                            continue;
                        }
                        if (!cube.has("uv") || !cube.get("uv").isJsonArray() || (uv = cube.getAsJsonArray("uv")) == null || uv.size() < 2) continue;
                        int u = uv.get(0).getAsInt();
                        int v = uv.get(1).getAsInt();
                        cubes.method_32101(u, v).method_32097(mcBoxX, mcBoxY, mcBoxZ, sx, sy, sz);
                    }
                }
                class_5610 class_56102 = parentDef = parentName != null ? (class_5610)definitions.get(parentName) : root;
                if (parentDef == null) {
                    parentDef = root;
                }
                float localPX = px;
                float localPY = py;
                float localPZ = pz;
                if (parentName != null && (pPivot = (float[])pivotByName.get(parentName)) != null) {
                    localPX = px - pPivot[0];
                    localPY = py - pPivot[1];
                    localPZ = pz - pPivot[2];
                }
                float renderPX = BedrockItemGeoLoader.toModelX(localPX);
                float renderPY = BedrockItemGeoLoader.toModelY(localPY);
                float renderPZ = BedrockItemGeoLoader.toModelZ(localPZ);
                float radX = (float)Math.toRadians(rx);
                float radY = (float)Math.toRadians(ry);
                float radZ = (float)Math.toRadians(rz);
                definitions.put(name, parentDef.method_32117(name, cubes, class_5603.method_32091((float)renderPX, (float)renderPY, (float)renderPZ, (float)radX, (float)radY, (float)radZ)));
            }
            class_630 rootPart = class_5607.method_32110((class_5609)mesh, (int)texW, (int)texH).method_32109();
            HashMap<String, class_630> parts = new HashMap<String, class_630>();
            BedrockItemGeoLoader.collectParts(rootPart, parts);
            return new BedrockItemGeoModel(rootPart, parts, texW, texH, parentByBone, cubesByBone);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static float getFloat(JsonArray array, int index) {
        if (array == null || index < 0 || index >= array.size()) {
            return 0.0f;
        }
        return array.get(index).getAsFloat();
    }

    private static float toModelX(float x) {
        return -x;
    }

    private static float toModelY(float y) {
        return y;
    }

    private static float toModelZ(float z) {
        return z;
    }

    private static void collectParts(class_630 part, Map<String, class_630> out) {
        ModelPartAccessor accessor;
        Map<String, class_630> children;
        if (part == null) {
            return;
        }
        class_630 class_6302 = part;
        if (class_6302 instanceof ModelPartAccessor && (children = (accessor = (ModelPartAccessor)class_6302).punchy$getChildren()) != null) {
            for (Map.Entry<String, class_630> entry : children.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                out.put(entry.getKey(), entry.getValue());
                BedrockItemGeoLoader.collectParts(entry.getValue(), out);
            }
        }
    }

    private static Map<class_2350, BedrockItemGeoModel.BedrockFace> parseFaces(JsonObject uvObj) {
        HashMap<class_2350, BedrockItemGeoModel.BedrockFace> faces = new HashMap<class_2350, BedrockItemGeoModel.BedrockFace>();
        if (uvObj == null) {
            return faces;
        }
        for (Map.Entry entry : uvObj.entrySet()) {
            JsonObject face;
            class_2350 dir = BedrockItemGeoLoader.parseDirection((String)entry.getKey());
            if (dir == null || !((JsonElement)entry.getValue()).isJsonObject() || !(face = ((JsonElement)entry.getValue()).getAsJsonObject()).has("uv") || !face.has("uv_size")) continue;
            JsonArray uv = face.getAsJsonArray("uv");
            JsonArray size = face.getAsJsonArray("uv_size");
            if (uv == null || size == null || uv.size() < 2 || size.size() < 2) continue;
            float u = uv.get(0).getAsFloat();
            float v = uv.get(1).getAsFloat();
            float uSize = size.get(0).getAsFloat();
            float vSize = size.get(1).getAsFloat();
            int rotation = 0;
            if (face.has("uv_rotation")) {
                rotation = face.get("uv_rotation").getAsInt();
            } else if (face.has("rotation")) {
                rotation = face.get("rotation").getAsInt();
            }
            faces.put(dir, new BedrockItemGeoModel.BedrockFace(u, v, uSize, vSize, rotation));
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
}

