/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_630
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package punchy.client.modelparts.util;

import net.minecraft.class_630;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import punchy.client.access.TransformablePart;

public final class ModelPartMath {
    private ModelPartMath() {
    }

    public static Matrix4f getLocalMatrix(class_630 part) {
        TransformablePart transformable;
        Matrix4f explicit;
        if (part == null) {
            return null;
        }
        class_630 class_6302 = part;
        if (class_6302 instanceof TransformablePart && (explicit = (transformable = (TransformablePart)class_6302).punchy$getExplicitTransform()) != null) {
            return new Matrix4f((Matrix4fc)explicit);
        }
        return new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
    }
}

