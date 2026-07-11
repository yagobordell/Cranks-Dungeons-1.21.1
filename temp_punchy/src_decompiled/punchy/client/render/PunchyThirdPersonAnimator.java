/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_742
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package punchy.client.render;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import punchy.client.access.TransformablePart;
import punchy.client.animation.PunchyThirdPersonAnimationManager;
import punchy.client.model.SimpleBedrockGeoThirdPerson;
import punchy.compat.FirstPersonModelCompat;

public final class PunchyThirdPersonAnimator {
    private static final String GEO_PATH = "geo/player_3_per.geo.json";

    private PunchyThirdPersonAnimator() {
    }

    public static void apply(class_742 player, class_591 model, float partialTicks) {
        if (player == null || model == null) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null) {
            return;
        }
        if (mc.field_1690.method_31044().method_31034()) {
            if (FirstPersonModelCompat.shouldApplyThirdPersonAnimations(mc)) {
                PunchyThirdPersonAnimator.clearModel(model);
                return;
            }
            PunchyThirdPersonAnimator.clearModel(model);
            return;
        }
        if (mc.field_1724 != player) {
            PunchyThirdPersonAnimator.clearModel(model);
            return;
        }
        PunchyThirdPersonAnimator.ensureGeoLoaded();
        Map<String, class_630> geoMap = SimpleBedrockGeoThirdPerson.loadedParts;
        if (geoMap.isEmpty()) {
            PunchyThirdPersonAnimator.clearModel(model);
            return;
        }
        PunchyThirdPersonAnimationManager.POSE_HANDLER.applyTransformations(geoMap, partialTicks);
        class_630 geoHead = geoMap.get("head");
        class_630 geoBody = geoMap.get("body");
        class_630 geoLeftArm = geoMap.get("left_arm");
        class_630 geoRightArm = geoMap.get("right_arm");
        class_630 geoLeftLeg = geoMap.get("left_leg");
        class_630 geoRightLeg = geoMap.get("right_leg");
        PunchyThirdPersonAnimator.applyWorldPart("head", model.field_3398, geoHead, geoMap);
        PunchyThirdPersonAnimator.applyWorldPart("body", model.field_3391, geoBody, geoMap);
        PunchyThirdPersonAnimator.applyWorldPart("left_arm", model.field_27433, geoLeftArm, geoMap);
        PunchyThirdPersonAnimator.applyWorldPart("right_arm", model.field_3401, geoRightArm, geoMap);
        PunchyThirdPersonAnimator.applyWorldPart("left_leg", model.field_3397, geoLeftLeg, geoMap);
        PunchyThirdPersonAnimator.applyWorldPart("right_leg", model.field_3392, geoRightLeg, geoMap);
        PunchyThirdPersonAnimator.applyLocalPart("hat", "head", model.field_3394, geoMap);
        PunchyThirdPersonAnimator.applyLocalPart("jacket", "body", model.field_3483, geoMap);
        PunchyThirdPersonAnimator.applyLocalPart("left_sleeve", "left_arm", model.field_3484, geoMap);
        PunchyThirdPersonAnimator.applyLocalPart("right_sleeve", "right_arm", model.field_3486, geoMap);
        PunchyThirdPersonAnimator.applyLocalPart("left_pants", "left_leg", model.field_3482, geoMap);
        PunchyThirdPersonAnimator.applyLocalPart("right_pants", "right_leg", model.field_3479, geoMap);
    }

    private static void ensureGeoLoaded() {
        if (!SimpleBedrockGeoThirdPerson.loadedParts.isEmpty()) {
            return;
        }
        class_2960 geoId = class_2960.method_60655((String)"punchy", (String)GEO_PATH);
        SimpleBedrockGeoThirdPerson.load(geoId);
    }

    private static void applyWorldPart(String boneName, class_630 target, class_630 source, Map<String, class_630> geoMap) {
        if (target == null) {
            return;
        }
        boolean[] hasExplicit = new boolean[1];
        Matrix4f world = PunchyThirdPersonAnimator.buildWorldMatrix(boneName, geoMap, new HashSet<String>(), hasExplicit);
        Matrix4f restWorld = SimpleBedrockGeoThirdPerson.restWorldByName.get(boneName);
        if (world == null || restWorld == null || !hasExplicit[0]) {
            PunchyThirdPersonAnimator.clearPart(target);
            return;
        }
        Matrix4f delta = new Matrix4f((Matrix4fc)restWorld).invert().mul((Matrix4fc)world);
        Matrix4f base = PunchyThirdPersonAnimator.getVanillaBaseMatrix(target);
        Matrix4f finalWorld = base.mul((Matrix4fc)delta);
        class_630 class_6302 = target;
        if (class_6302 instanceof TransformablePart) {
            TransformablePart transformable = (TransformablePart)class_6302;
            transformable.punchy$setExplicitTransform(finalWorld);
        }
    }

    private static void applyLocalPart(String boneName, String parentBone, class_630 target, Map<String, class_630> geoMap) {
        if (target == null) {
            return;
        }
        boolean[] hasExplicit = new boolean[1];
        Matrix4f localDelta = PunchyThirdPersonAnimator.buildLocalDeltaMatrix(boneName, parentBone, geoMap, hasExplicit);
        if (localDelta == null || !hasExplicit[0]) {
            PunchyThirdPersonAnimator.clearPart(target);
            return;
        }
        Matrix4f base = PunchyThirdPersonAnimator.getVanillaBaseMatrix(target);
        Matrix4f finalLocal = base.mul((Matrix4fc)localDelta);
        class_630 class_6302 = target;
        if (class_6302 instanceof TransformablePart) {
            TransformablePart transformable = (TransformablePart)class_6302;
            transformable.punchy$setExplicitTransform(finalLocal);
        }
    }

    private static Matrix4f getExplicitTransform(class_630 part) {
        TransformablePart transformable;
        Matrix4f explicit;
        if (part == null) {
            return null;
        }
        class_630 class_6302 = part;
        if (class_6302 instanceof TransformablePart && (explicit = (transformable = (TransformablePart)class_6302).punchy$getExplicitTransform()) != null) {
            return new Matrix4f((Matrix4fc)explicit);
        }
        return null;
    }

    private static Matrix4f buildWorldMatrix(String boneName, Map<String, class_630> geoMap, Set<String> visited, boolean[] hasExplicit) {
        if (boneName == null || geoMap == null || visited.contains(boneName)) {
            return null;
        }
        visited.add(boneName);
        class_630 part = geoMap.get(boneName);
        if (part == null) {
            return null;
        }
        Matrix4f local = PunchyThirdPersonAnimator.getLocalMatrix(part, hasExplicit);
        String parent = SimpleBedrockGeoThirdPerson.parentByName.get(boneName);
        if (parent == null) {
            return local;
        }
        Matrix4f parentWorld = PunchyThirdPersonAnimator.buildWorldMatrix(parent, geoMap, visited, hasExplicit);
        if (parentWorld == null) {
            return local;
        }
        return parentWorld.mul((Matrix4fc)local, new Matrix4f());
    }

    private static Matrix4f buildLocalDeltaMatrix(String boneName, String parentBone, Map<String, class_630> geoMap, boolean[] hasExplicit) {
        boolean explicit;
        if (boneName == null || parentBone == null || geoMap == null) {
            return null;
        }
        boolean[] childExplicit = new boolean[1];
        boolean[] parentExplicit = new boolean[1];
        Matrix4f childWorld = PunchyThirdPersonAnimator.buildWorldMatrix(boneName, geoMap, new HashSet<String>(), childExplicit);
        Matrix4f parentWorld = PunchyThirdPersonAnimator.buildWorldMatrix(parentBone, geoMap, new HashSet<String>(), parentExplicit);
        Matrix4f childRest = SimpleBedrockGeoThirdPerson.restWorldByName.get(boneName);
        Matrix4f parentRest = SimpleBedrockGeoThirdPerson.restWorldByName.get(parentBone);
        boolean bl = explicit = childExplicit[0] || parentExplicit[0];
        if (hasExplicit != null) {
            hasExplicit[0] = explicit;
        }
        if (childWorld == null || parentWorld == null || childRest == null || parentRest == null) {
            return null;
        }
        Matrix4f localAnim = new Matrix4f((Matrix4fc)parentWorld).invert().mul((Matrix4fc)childWorld);
        Matrix4f localRest = new Matrix4f((Matrix4fc)parentRest).invert().mul((Matrix4fc)childRest);
        return new Matrix4f((Matrix4fc)localRest).invert().mul((Matrix4fc)localAnim);
    }

    private static Matrix4f getLocalMatrix(class_630 part, boolean[] hasExplicit) {
        Matrix4f explicit = PunchyThirdPersonAnimator.getExplicitTransform(part);
        if (explicit != null) {
            if (hasExplicit != null) {
                hasExplicit[0] = true;
            }
            return new Matrix4f((Matrix4fc)explicit);
        }
        return new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
    }

    private static Matrix4f getVanillaBaseMatrix(class_630 part) {
        return new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
    }

    private static void clearPart(class_630 part) {
        class_630 class_6302;
        if (part != null && (class_6302 = part) instanceof TransformablePart) {
            TransformablePart transformable = (TransformablePart)class_6302;
            transformable.punchy$setExplicitTransform(null);
        }
    }

    private static void clearModel(class_591 model) {
        if (model == null) {
            return;
        }
        PunchyThirdPersonAnimator.clearPart(model.field_3398);
        PunchyThirdPersonAnimator.clearPart(model.field_3391);
        PunchyThirdPersonAnimator.clearPart(model.field_27433);
        PunchyThirdPersonAnimator.clearPart(model.field_3401);
        PunchyThirdPersonAnimator.clearPart(model.field_3397);
        PunchyThirdPersonAnimator.clearPart(model.field_3392);
        PunchyThirdPersonAnimator.clearPart(model.field_3394);
        PunchyThirdPersonAnimator.clearPart(model.field_3483);
        PunchyThirdPersonAnimator.clearPart(model.field_3484);
        PunchyThirdPersonAnimator.clearPart(model.field_3486);
        PunchyThirdPersonAnimator.clearPart(model.field_3482);
        PunchyThirdPersonAnimator.clearPart(model.field_3479);
    }
}

