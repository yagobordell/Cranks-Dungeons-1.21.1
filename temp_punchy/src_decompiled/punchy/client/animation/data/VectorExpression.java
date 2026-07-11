/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.animation.data;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.animation.data.AnimationExpression;

public record VectorExpression(AnimationExpression x, AnimationExpression y, AnimationExpression z) {
    public boolean isPresent() {
        return this.x != null || this.y != null || this.z != null;
    }

    public Vector3f evaluate(Vector3f base, float animTimeSeconds) {
        Vector3f out = base != null ? new Vector3f((Vector3fc)base) : new Vector3f();
        double t = animTimeSeconds;
        if (this.x != null) {
            out.x = (float)this.x.evaluate(t);
        }
        if (this.y != null) {
            out.y = (float)this.y.evaluate(t);
        }
        if (this.z != null) {
            out.z = (float)this.z.evaluate(t);
        }
        return out;
    }
}

