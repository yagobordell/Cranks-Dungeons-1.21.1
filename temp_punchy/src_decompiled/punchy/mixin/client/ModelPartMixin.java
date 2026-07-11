/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 *  net.minecraft.class_630
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_4587;
import net.minecraft.class_630;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.access.TransformablePart;

@Mixin(value={class_630.class})
public class ModelPartMixin
implements TransformablePart {
    @Unique
    private Matrix4f punchy$explicitMatrix;
    @Unique
    private boolean punchy$hasDirectPoseBase;
    @Unique
    private float punchy$directPoseBaseX;
    @Unique
    private float punchy$directPoseBaseY;
    @Unique
    private float punchy$directPoseBaseZ;
    @Unique
    private float punchy$directPoseBaseXRot;
    @Unique
    private float punchy$directPoseBaseYRot;
    @Unique
    private float punchy$directPoseBaseZRot;
    @Unique
    private final Matrix4f punchy$tempExplicitMatrix = new Matrix4f();
    @Unique
    private final Matrix4f punchy$tempVanillaMatrix = new Matrix4f();
    @Unique
    private final Matrix4f punchy$tempCorrectionMatrix = new Matrix4f();

    @Override
    public void punchy$setExplicitTransform(Matrix4f matrix) {
        this.punchy$explicitMatrix = matrix;
    }

    @Override
    public Matrix4f punchy$getExplicitTransform() {
        return this.punchy$explicitMatrix;
    }

    @Override
    public void punchy$setDirectPoseBase(float x, float y, float z, float xRot, float yRot, float zRot) {
        this.punchy$hasDirectPoseBase = true;
        this.punchy$directPoseBaseX = x;
        this.punchy$directPoseBaseY = y;
        this.punchy$directPoseBaseZ = z;
        this.punchy$directPoseBaseXRot = xRot;
        this.punchy$directPoseBaseYRot = yRot;
        this.punchy$directPoseBaseZRot = zRot;
    }

    @Override
    public boolean punchy$hasDirectPoseBase() {
        return this.punchy$hasDirectPoseBase;
    }

    @Override
    public float punchy$getDirectPoseBaseX() {
        return this.punchy$directPoseBaseX;
    }

    @Override
    public float punchy$getDirectPoseBaseY() {
        return this.punchy$directPoseBaseY;
    }

    @Override
    public float punchy$getDirectPoseBaseZ() {
        return this.punchy$directPoseBaseZ;
    }

    @Override
    public float punchy$getDirectPoseBaseXRot() {
        return this.punchy$directPoseBaseXRot;
    }

    @Override
    public float punchy$getDirectPoseBaseYRot() {
        return this.punchy$directPoseBaseYRot;
    }

    @Override
    public float punchy$getDirectPoseBaseZRot() {
        return this.punchy$directPoseBaseZRot;
    }

    @Override
    public void punchy$clearDirectPoseBase() {
        this.punchy$hasDirectPoseBase = false;
        this.punchy$directPoseBaseX = 0.0f;
        this.punchy$directPoseBaseY = 0.0f;
        this.punchy$directPoseBaseZ = 0.0f;
        this.punchy$directPoseBaseXRot = 0.0f;
        this.punchy$directPoseBaseYRot = 0.0f;
        this.punchy$directPoseBaseZRot = 0.0f;
    }

    @Inject(method={"method_22703(Lnet/minecraft/class_4587;)V"}, at={@At(value="TAIL")})
    private void punchy$applyExplicitTransformAtTail(class_4587 poseStack, CallbackInfo ci) {
        if (this.punchy$explicitMatrix == null) {
            return;
        }
        class_630 self = (class_630)this;
        Matrix4f explicit = this.punchy$tempExplicitMatrix.set((Matrix4fc)this.punchy$explicitMatrix);
        explicit.m30(explicit.m30() / 16.0f);
        explicit.m31(explicit.m31() / 16.0f);
        explicit.m32(explicit.m32() / 16.0f);
        Matrix4f vanilla = this.punchy$tempVanillaMatrix.identity().translate(self.field_3657 / 16.0f, self.field_3656 / 16.0f, self.field_3655 / 16.0f).rotateZ(self.field_3674).rotateY(self.field_3675).rotateX(self.field_3654).scale(self.field_37938, self.field_37939, self.field_37940);
        Matrix4f correction = this.punchy$tempCorrectionMatrix.set((Matrix4fc)vanilla).invert().mul((Matrix4fc)explicit);
        poseStack.method_34425(correction);
    }
}

