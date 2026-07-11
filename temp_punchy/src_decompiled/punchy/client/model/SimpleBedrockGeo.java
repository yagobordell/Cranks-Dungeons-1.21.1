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
 *  org.joml.Vector3f
 */
package punchy.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_5603;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_630;
import org.joml.Vector3f;

public class SimpleBedrockGeo {
    public static Map<String, class_630> loadedParts = new HashMap<String, class_630>();
    public static Map<String, Vector3f> bedrockLocalPivots = new HashMap<String, Vector3f>();

    public static void load(class_2960 geoId) {
        block33: {
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
                for (JsonElement boneEl : bones) {
                    bone = boneEl.getAsJsonObject();
                    name = bone.get("name").getAsString();
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
                try {
                    class_630 leftArm;
                    block34: {
                        class_630 camera;
                        class_630 cameraAnimation = rootPart.method_32086("camera_animation");
                        class_630 class_6302 = camera = cameraAnimation != null ? cameraAnimation.method_32086("camera") : null;
                        if (camera == null) {
                            camera = rootPart.method_32086("camera");
                        }
                        if (cameraAnimation == null) {
                            cameraAnimation = camera;
                        }
                        class_630 body = camera != null ? camera.method_32086("body") : null;
                        loadedParts.put("camera_animation", cameraAnimation);
                        loadedParts.put("camera", camera);
                        loadedParts.put("body", body);
                        if (body == null) break block33;
                        class_630 rightArm = body.method_32086("right_arm");
                        leftArm = body.method_32086("left_arm");
                        loadedParts.put("right_arm", rightArm);
                        loadedParts.put("left_arm", leftArm);
                        loadedParts.put("torso", body.method_32086("torso"));
                        loadedParts.put("head", body.method_32086("head"));
                        if (rightArm != null) {
                            try {
                                loadedParts.put("item_right", rightArm.method_32086("item_right"));
                                class_630 itemRight = loadedParts.get("item_right");
                                if (itemRight == null) break block34;
                                loadedParts.put("itemgrip_right", itemRight.method_32086("itemgrip_right"));
                                class_630 gripRight = loadedParts.get("itemgrip_right");
                                if (gripRight == null) break block34;
                                try {
                                    loadedParts.put("itembase_right", gripRight.method_32086("bone"));
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                try {
                                    loadedParts.put("itemtop_right", gripRight.method_32086("top2"));
                                }
                                catch (Exception exception) {}
                            }
                            catch (Exception itemRight) {
                                // empty catch block
                            }
                        }
                    }
                    if (leftArm == null) break block33;
                    try {
                        loadedParts.put("item_left", leftArm.method_32086("item_left"));
                        class_630 itemLeft = loadedParts.get("item_left");
                        if (itemLeft == null) break block33;
                        loadedParts.put("itemgrip_left", itemLeft.method_32086("itemgrip_left"));
                        class_630 gripLeft = loadedParts.get("itemgrip_left");
                        if (gripLeft == null) break block33;
                        try {
                            loadedParts.put("itembase_left", gripLeft.method_32086("base"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            loadedParts.put("itemtop_left", gripLeft.method_32086("top"));
                        }
                        catch (Exception exception) {}
                    }
                    catch (Exception exception) {
                    }
                }
                catch (Exception exception) {}
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

