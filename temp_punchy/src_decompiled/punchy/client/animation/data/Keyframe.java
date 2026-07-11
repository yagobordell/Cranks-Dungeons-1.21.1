/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.animation.data;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.animation.data.VectorExpression;

public record Keyframe(float timeStamp, Vector3f position, Vector3f rotationEuler, Quaternionf rotation, Vector3f scale, String interpolation, Vector3f positionV2, Vector3f rotationPreEulerV2, Vector3f rotationPostEulerV2, VectorExpression positionExpression, VectorExpression rotationExpression, VectorExpression scaleExpression) {
    public Keyframe(float timeStamp, Vector3f position, Vector3f rotationEuler, Quaternionf rotation, Vector3f scale, String interpolation) {
        this(timeStamp, position, rotationEuler, rotation, scale, interpolation, null, null, null, null, null, null);
    }

    public Keyframe(float timeStamp, Vector3f position, Vector3f rotationEuler, Quaternionf rotation, Vector3f scale, String interpolation, VectorExpression positionExpression, VectorExpression rotationExpression, VectorExpression scaleExpression) {
        this(timeStamp, position, rotationEuler, rotation, scale, interpolation, null, null, null, positionExpression, rotationExpression, scaleExpression);
    }

    public Vector3f sampledPosition(float animTimeSeconds) {
        return Keyframe.sampled(this.position, this.positionExpression, animTimeSeconds, new Vector3f(0.0f, 0.0f, 0.0f));
    }

    public Vector3f sampledPositionV2(float animTimeSeconds) {
        if (this.positionV2 != null) {
            return new Vector3f((Vector3fc)this.positionV2);
        }
        return this.sampledPosition(animTimeSeconds);
    }

    public Vector3f sampledRotationEuler(float animTimeSeconds) {
        return Keyframe.sampled(this.rotationEuler, this.rotationExpression, animTimeSeconds, new Vector3f(0.0f, 0.0f, 0.0f));
    }

    public Vector3f sampledScale(float animTimeSeconds) {
        return Keyframe.sampled(this.scale, this.scaleExpression, animTimeSeconds, new Vector3f(1.0f, 1.0f, 1.0f));
    }

    public boolean hasRotationV2() {
        return this.rotationPreEulerV2 != null || this.rotationPostEulerV2 != null;
    }

    public Vector3f sampledRotationPreEulerV2(float animTimeSeconds) {
        if (this.rotationPreEulerV2 != null) {
            return new Vector3f((Vector3fc)this.rotationPreEulerV2);
        }
        Vector3f legacy = this.sampledRotationEuler(animTimeSeconds);
        return legacy == null ? null : new Vector3f((Vector3fc)legacy);
    }

    public Vector3f sampledRotationPostEulerV2(float animTimeSeconds) {
        if (this.rotationPostEulerV2 != null) {
            return new Vector3f((Vector3fc)this.rotationPostEulerV2);
        }
        Vector3f legacy = this.sampledRotationEuler(animTimeSeconds);
        return legacy == null ? null : new Vector3f((Vector3fc)legacy);
    }

    private static Vector3f sampled(Vector3f base, VectorExpression expr, float animTimeSeconds, Vector3f fallback) {
        Vector3f resolvedBase;
        Vector3f vector3f = resolvedBase = base != null ? base : fallback;
        if (expr == null || !expr.isPresent()) {
            return new Vector3f((Vector3fc)resolvedBase);
        }
        return expr.evaluate(resolvedBase, animTimeSeconds);
    }
}

