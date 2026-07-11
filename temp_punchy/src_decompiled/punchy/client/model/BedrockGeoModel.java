/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package punchy.client.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.joml.Vector3f;
import punchy.client.model.BedrockBone;

public class BedrockGeoModel {
    private final Map<String, BedrockBone> bones = new LinkedHashMap<String, BedrockBone>();

    public void addBone(BedrockBone bone) {
        if (bone == null) {
            return;
        }
        this.bones.put(bone.getName(), bone);
    }

    public Map<String, BedrockBone> getBones() {
        return Collections.unmodifiableMap(this.bones);
    }

    public Vector3f getLocalPivot(String boneName) {
        BedrockBone bone = this.bones.get(boneName);
        return bone != null ? bone.getLocalPivot() : null;
    }
}

