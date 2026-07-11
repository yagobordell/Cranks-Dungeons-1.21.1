/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_630
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.render;

import net.minecraft.class_630;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.access.TransformablePart;

public final class CameraAnimationState {
    public static float CAMERA_POS_SCALE = 0.0625f;
    public static float CAMERA_ROT_SCALE = 1.0f;
    private static final float EPSILON = 1.0E-4f;
    private static final Quaternionf ROTATION = new Quaternionf();
    private static final Vector3f OFFSET = new Vector3f();
    private static boolean active;

    private CameraAnimationState() {
    }

    public static void clear() {
        ROTATION.identity();
        OFFSET.zero();
        active = false;
    }

    public static void update(class_630 cameraPart) {
        class_630 class_6302;
        if (cameraPart == null || !((class_6302 = cameraPart) instanceof TransformablePart)) {
            CameraAnimationState.clear();
            return;
        }
        TransformablePart transformable = (TransformablePart)class_6302;
        Matrix4f explicit = transformable.punchy$getExplicitTransform();
        if (explicit == null) {
            CameraAnimationState.clear();
            return;
        }
        Matrix4f base = new Matrix4f().translate(cameraPart.field_3657, cameraPart.field_3656, cameraPart.field_3655).rotateZ(cameraPart.field_3674).rotateY(cameraPart.field_3675).rotateX(cameraPart.field_3654).scale(cameraPart.field_37938, cameraPart.field_37939, cameraPart.field_37940);
        Matrix4f baseInv = new Matrix4f((Matrix4fc)base).invert();
        Matrix4f anim = baseInv.mul((Matrix4fc)explicit, new Matrix4f());
        Vector3f delta = anim.getTranslation(new Vector3f()).mul(CAMERA_POS_SCALE);
        delta.x = -delta.x;
        Quaternionf rot = new Quaternionf();
        anim.getUnnormalizedRotation(rot);
        rot.normalize();
        Vector3f euler = rot.getEulerAnglesZYX(new Vector3f());
        euler.x = -euler.x;
        float swap = euler.y;
        euler.y = euler.z;
        euler.z = swap;
        euler.y = -euler.y;
        euler.z = -euler.z;
        euler.mul(CAMERA_ROT_SCALE);
        rot.rotationZYX(euler.z, euler.y, euler.x);
        ROTATION.set((Quaternionfc)rot);
        OFFSET.set((Vector3fc)delta);
        active = delta.lengthSquared() > 1.0E-4f || Math.abs(CameraAnimationState.ROTATION.w - 1.0f) > 1.0E-4f;
    }

    public static boolean isActive() {
        return active;
    }

    public static Quaternionf rotation() {
        return new Quaternionf((Quaternionfc)ROTATION);
    }

    public static Vector3f offset() {
        return new Vector3f((Vector3fc)OFFSET);
    }
}

