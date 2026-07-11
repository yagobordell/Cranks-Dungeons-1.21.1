/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package punchy.client.model;

import org.joml.Vector3f;

public class BedrockBone {
    private final String name;
    private final String parent;
    private final Vector3f pivot;
    private final Vector3f rotation;
    private final Vector3f localPivot;

    public BedrockBone(String name, String parent, Vector3f pivot, Vector3f rotation, Vector3f localPivot) {
        this.name = name;
        this.parent = parent;
        this.pivot = pivot;
        this.rotation = rotation;
        this.localPivot = localPivot;
    }

    public String getName() {
        return this.name;
    }

    public String getParent() {
        return this.parent;
    }

    public Vector3f getPivot() {
        return this.pivot;
    }

    public Vector3f getRotation() {
        return this.rotation;
    }

    public Vector3f getLocalPivot() {
        return this.localPivot;
    }
}

