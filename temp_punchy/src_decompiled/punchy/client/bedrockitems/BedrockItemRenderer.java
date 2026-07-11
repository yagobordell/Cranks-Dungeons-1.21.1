/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.class_1087
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_1921
 *  net.minecraft.class_2350
 *  net.minecraft.class_2487
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_630
 *  net.minecraft.class_746
 *  net.minecraft.class_7833
 *  net.minecraft.class_804
 *  net.minecraft.class_809
 *  net.minecraft.class_811
 *  net.minecraft.class_9279
 *  net.minecraft.class_9334
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.bedrockitems;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1087;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2350;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_630;
import net.minecraft.class_746;
import net.minecraft.class_7833;
import net.minecraft.class_804;
import net.minecraft.class_809;
import net.minecraft.class_811;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.bedrockitems.BedrockAxolPhysicsStateMachine;
import punchy.client.bedrockitems.BedrockItemAnimationManager;
import punchy.client.bedrockitems.BedrockItemDefinition;
import punchy.client.bedrockitems.BedrockItemGeoModel;
import punchy.client.bedrockitems.BedrockItemSystem;
import punchy.client.bedrockitems.BedrockPufferPhysicsStateMachine;
import punchy.client.modelparts.ModelPartsParticleSystem;
import punchy.client.modelparts.util.ModelPartMath;
import punchy.client.render.PendulumPhysics;
import punchy.client.render.ResourcePackItemTransformResolver;
import punchy.client.render.SlimeInHandPhysics;
import punchy.client.render.ToolTuning;
import punchy.client.render.UseAnimationSuppressor;
import punchy.compat.IrisShaderPackCompat;
import punchy.config.PunchyTuningConfig;

final class BedrockItemRenderer {
    private static final EnumMap<class_1306, class_2960> LAST_RENDER_DEF_ID = new EnumMap(class_1306.class);
    private static final float MODEL_UNIT = 0.0625f;
    private static final float EMISSIVE_BLUR_SCALE = 1.08f;
    private static final float EMISSIVE_BLUR_ALPHA = 0.4f;
    private static final float EMISSIVE_BLUR_COLOR = 0.6f;
    private static final float PUFFER_INCLINATION_SCALE = 1.19f;
    private static final float PUFFER_PITCH_BOOST = 1.3f;
    private static final Set<String> AXOL_PHYSICS_BONES = Set.of("fish", "head", "top_gills", "left_gills", "right_gills", "body", "legs_front", "leg1", "leg2", "leg4", "leg5");
    private static final Set<String> PUFFER_PHYSICS_BONES = Set.of("fish", "body", "right_fin", "left_fin", "fin_top", "tail", "eye", "fin_left", "fin_right", "spikes");
    private static final Map<EmissiveKey, class_1921> EMISSIVE_OVERLAY_TYPES = new HashMap<EmissiveKey, class_1921>();

    private BedrockItemRenderer() {
    }

    static boolean render(BedrockItemDefinition def, class_1799 stack, class_746 player, class_1306 arm, class_4587 poseStack, class_4597 buffer, int packedLight, float partialTicks, boolean applyDisplay) {
        boolean flipNormalsX;
        if (def == null || stack == null || stack.method_7960() || player == null || arm == null) {
            return false;
        }
        class_2960 dynamicGeoId = BedrockItemRenderer.resolveDynamicGeoId(stack, def);
        BedrockItemGeoModel geo = def.getGeoModel(dynamicGeoId);
        if (geo == null || geo.getRoot() == null) {
            return false;
        }
        class_2960 texture = def.getTextureId();
        if (texture == null) {
            return false;
        }
        class_2960 renderTexture = texture;
        class_2960 currentDefId = def.getId();
        class_2960 lastDefId = LAST_RENDER_DEF_ID.get(arm);
        if (arm != null) {
            if (lastDefId != null && currentDefId != null && !lastDefId.equals((Object)currentDefId)) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            LAST_RENDER_DEF_ID.put(arm, currentDefId);
        }
        geo.resetPose();
        BedrockItemAnimationManager.applyToParts(arm, geo.getParts(), partialTicks);
        poseStack.method_22903();
        Matrix4f internalTransform = new Matrix4f();
        poseStack.method_22905(0.0625f, 0.0625f, 0.0625f);
        internalTransform.scale(0.0625f, 0.0625f, 0.0625f);
        poseStack.method_46416(0.5f, 0.0f, 0.5f);
        internalTransform.translate(0.5f, 0.0f, 0.5f);
        if (applyDisplay) {
            BedrockItemRenderer.applyDisplayTransform(def, stack, player, arm, poseStack);
            BedrockItemRenderer.applyDisplayTransformMatrix(def, stack, player, arm, internalTransform);
        }
        class_1268 interactionHand = BedrockItemRenderer.resolveInteractionHand(player, arm);
        boolean animMirrorActive = BedrockItemAnimationManager.isAnimMirrorActive(arm);
        if (arm == class_1306.field_6182) {
            poseStack.method_22905(-1.0f, 1.0f, 1.0f);
            internalTransform.scale(-1.0f, 1.0f, 1.0f);
            poseStack.method_23760().method_23762().scale(-1.0f, 1.0f, 1.0f);
        }
        poseStack.method_46416(-0.5f, -0.5f, -0.5f);
        internalTransform.translate(-0.5f, -0.5f, -0.5f);
        Map<String, List<BedrockItemGeoModel.BedrockCube>> cubesByBone = geo.getCubesByBone();
        boolean bl = flipNormalsX = arm == class_1306.field_6182;
        if (cubesByBone != null && !cubesByBone.isEmpty()) {
            float animTime = (float)player.field_6012 + partialTicks;
            BedrockItemRenderer.renderCubes(stack, geo, poseStack, buffer, packedLight, renderTexture, def.getBoneEmission(), def.getBoneTextures(), def.getBoneTextureAnim(), def.isBoneEmissionBlur(), def.isBoneEmissionClamp(), def.getBoneEmissionStrength(), def.getTextureAnimFrames(), def.getTextureAnimFrameTime(), def.getTextureAnimFrameHeight(), def.getTexturePixelWidth(), def.getTexturePixelHeight(), animTime, partialTicks, def, arm, interactionHand, internalTransform, flipNormalsX);
        } else {
            class_630 root = geo.getRoot();
            class_4588 consumer = buffer.getBuffer(class_1921.method_23580((class_2960)renderTexture));
            root.method_22698(poseStack, consumer, packedLight, class_4608.field_21444);
        }
        poseStack.method_22909();
        return true;
    }

    private static void applyDisplayTransform(BedrockItemDefinition def, class_1799 stack, class_746 player, class_1306 arm, class_4587 poseStack) {
        BedrockItemDefinition.DisplayTransform transform = def.getDisplayTransform(arm);
        if (transform == null) {
            BedrockItemRenderer.applyVanillaDisplayTransform(stack, player, arm, poseStack);
            return;
        }
        float tx = transform.transX * 0.0625f;
        float ty = transform.transY * 0.0625f;
        float tz = transform.transZ * 0.0625f;
        float rx = transform.rotX;
        float ry = transform.rotY;
        float rz = transform.rotZ;
        if (arm == class_1306.field_6182) {
            tx = -tx;
            ry = -ry;
            rz = -rz;
        }
        poseStack.method_46416(tx, ty, tz);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(rx));
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(ry));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(rz));
        poseStack.method_22905(transform.scaleX, transform.scaleY, transform.scaleZ);
    }

    private static void applyDisplayTransformMatrix(BedrockItemDefinition def, class_1799 stack, class_746 player, class_1306 arm, Matrix4f matrix) {
        if (matrix == null) {
            return;
        }
        BedrockItemDefinition.DisplayTransform transform = def.getDisplayTransform(arm);
        if (transform == null) {
            class_811 context = arm == class_1306.field_6182 ? class_811.field_4321 : class_811.field_4322;
            class_804 vanilla = BedrockItemRenderer.resolveDisplayTransform(stack, context, (class_1309)player);
            if (vanilla == null) {
                return;
            }
            Vector3f rotation = vanilla.field_4287;
            Vector3f translation = vanilla.field_4286;
            Vector3f scale = vanilla.field_4285;
            if (rotation == null || translation == null || scale == null) {
                return;
            }
            matrix.translate(translation.x() * 0.0625f, translation.y() * 0.0625f, translation.z() * 0.0625f);
            matrix.rotateXYZ((float)Math.toRadians(rotation.x()), (float)Math.toRadians(rotation.y()), (float)Math.toRadians(rotation.z()));
            matrix.scale(scale.x(), scale.y(), scale.z());
            return;
        }
        float tx = transform.transX * 0.0625f;
        float ty = transform.transY * 0.0625f;
        float tz = transform.transZ * 0.0625f;
        float rx = transform.rotX;
        float ry = transform.rotY;
        float rz = transform.rotZ;
        if (arm == class_1306.field_6182) {
            tx = -tx;
            ry = -ry;
            rz = -rz;
        }
        matrix.translate(tx, ty, tz);
        matrix.rotateXYZ((float)Math.toRadians(rx), (float)Math.toRadians(ry), (float)Math.toRadians(rz));
        matrix.scale(transform.scaleX, transform.scaleY, transform.scaleZ);
    }

    private static void applyVanillaDisplayTransform(class_1799 stack, class_746 player, class_1306 arm, class_4587 poseStack) {
        if (stack == null || stack.method_7960() || player == null || arm == null) {
            return;
        }
        class_811 context = arm == class_1306.field_6182 ? class_811.field_4321 : class_811.field_4322;
        class_804 transform = BedrockItemRenderer.resolveDisplayTransform(stack, context, (class_1309)player);
        if (transform == null) {
            return;
        }
        Vector3f rotation = transform.field_4287;
        Vector3f translation = transform.field_4286;
        Vector3f scale = transform.field_4285;
        if (rotation == null || translation == null || scale == null) {
            return;
        }
        poseStack.method_46416(translation.x() * 0.0625f, translation.y() * 0.0625f, translation.z() * 0.0625f);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(rotation.x()));
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(rotation.y()));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(rotation.z()));
        poseStack.method_22905(scale.x(), scale.y(), scale.z());
    }

    private static class_804 resolveDisplayTransform(class_1799 stack, class_811 context, class_1309 entity) {
        if (stack == null || stack.method_7960() || context == null) {
            return null;
        }
        class_804 resourcePack = ResourcePackItemTransformResolver.resolve(stack, context);
        if (resourcePack != null) {
            return resourcePack;
        }
        return BedrockItemRenderer.resolveVanillaTransform(stack, context, entity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static class_804 resolveVanillaTransform(class_1799 stack, class_811 context, class_1309 entity) {
        if (stack == null || stack.method_7960() || context == null || entity == null) {
            return null;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return null;
        }
        boolean prevForced = UseAnimationSuppressor.isForced();
        UseAnimationSuppressor.force(true);
        try {
            class_1087 model = mc.method_1480().method_4019(stack, entity.method_37908(), entity, 0);
            if (model == null) {
                class_804 class_8042 = null;
                return class_8042;
            }
            class_809 transforms = model.method_4709();
            if (transforms == null) {
                class_804 class_8043 = null;
                return class_8043;
            }
            class_804 class_8044 = transforms.method_3503(context);
            return class_8044;
        }
        catch (Throwable ignored) {
            class_804 class_8045 = null;
            return class_8045;
        }
        finally {
            UseAnimationSuppressor.force(prevForced);
        }
    }

    private static class_1268 resolveInteractionHand(class_746 player, class_1306 arm) {
        boolean mainRight;
        if (player == null || arm == null) {
            return class_1268.field_5808;
        }
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        if (arm == class_1306.field_6183) {
            return mainRight ? class_1268.field_5808 : class_1268.field_5810;
        }
        return mainRight ? class_1268.field_5810 : class_1268.field_5808;
    }

    private static void renderCubes(class_1799 stack, BedrockItemGeoModel geo, class_4587 poseStack, class_4597 buffer, int packedLight, class_2960 texture, Map<String, Integer> boneEmission, Map<String, class_2960> boneTextures, Map<String, BedrockItemDefinition.TextureAnimSpec> boneTextureAnim, boolean emissiveBlur, boolean emissiveClamp, int emissiveStrength, int textureAnimFrames, int textureAnimFrameTime, int textureAnimFrameHeight, int texturePixelWidth, int texturePixelHeight, float animTime, float partialTicks, BedrockItemDefinition def, class_1306 arm, class_1268 interactionHand, Matrix4f internalTransform, boolean flipNormalsX) {
        List cubes;
        String boneName;
        PunchyTuningConfig.CustomAnimationEntry entry;
        Map<String, class_630> parts = geo.getParts();
        Map<String, String> parentByBone = geo.getParentByBone();
        Map<String, List<BedrockItemGeoModel.BedrockCube>> cubesByBone = geo.getCubesByBone();
        boolean pufferModel = BedrockItemRenderer.isPufferModel(parts);
        boolean axolModel = !pufferModel && BedrockItemRenderer.isAxolModel(parts, cubesByBone);
        HashMap<String, Matrix4f> matrixCache = new HashMap<String, Matrix4f>();
        Matrix4f headWorld = axolModel ? BedrockItemRenderer.computeWorldMatrix("head", parts, parentByBone, matrixCache) : null;
        Matrix4f bodyWorld = axolModel ? BedrockItemRenderer.computeWorldMatrix("body", parts, parentByBone, matrixCache) : null;
        Matrix4f fishWorld = pufferModel ? BedrockItemRenderer.computeWorldMatrix("fish", parts, parentByBone, matrixCache) : null;
        int texW = geo.getTextureWidth();
        int texH = geo.getTextureHeight();
        Map<String, Integer> emissionByBone = boneEmission == null ? Map.of() : boneEmission;
        float strengthScale = class_3532.method_15363((float)((float)emissiveStrength / 100.0f), (float)0.0f, (float)1.0f);
        Map<String, class_2960> texturesByBone = BedrockItemRenderer.mergeDynamicBoneTextures(stack, def, boneTextures);
        HashMap<String, class_2960> resolvedTextureByBone = new HashMap<String, class_2960>();
        Map<Object, Object> animByBone = boneTextureAnim == null ? Map.of() : boneTextureAnim;
        boolean shaderPackInUse = BedrockItemRenderer.isShaderPackInUse();
        Set<String> cameraTransparentBones = def != null ? def.getCameraTransparentBones() : Set.of();
        Map<String, BedrockItemDefinition.BonePhysicsSpec> bonePhysics = BedrockItemRenderer.mergeBonePhysics(def != null ? def.getBonePhysics() : null, ToolTuning.getSpecificBonePhysics(stack));
        Set<Object> noPhysicsBones = null;
        if (arm != null && (entry = BedrockItemAnimationManager.getActiveEntry(arm)) != null && entry.disable_physics_bones != null) {
            noPhysicsBones = new HashSet();
            for (String bone : entry.disable_physics_bones) {
                if (bone == null || bone.isBlank()) continue;
                noPhysicsBones.add(bone.trim());
            }
        }
        if (noPhysicsBones == null) {
            noPhysicsBones = def != null ? def.getDisablePhysicsBones() : Set.of();
        }
        Matrix4f physicsRemoval = null;
        if (noPhysicsBones != null && !noPhysicsBones.isEmpty() && arm != null) {
            Matrix4f physicsDelta = BedrockItemSystem.getCurrentPhysicsDelta(arm);
            if (physicsDelta != null) {
                physicsRemoval = new Matrix4f((Matrix4fc)physicsDelta);
            } else {
                Matrix4f pendulum = PendulumPhysics.get().getLastAppliedMatrix(arm);
                if (pendulum != null) {
                    physicsRemoval = new Matrix4f((Matrix4fc)pendulum).invert();
                }
            }
        }
        Matrix4f internal = internalTransform != null ? new Matrix4f((Matrix4fc)internalTransform) : new Matrix4f();
        Matrix4f internalInv = new Matrix4f((Matrix4fc)internal).invert();
        Matrix4f adjustedPhysicsRemoval = null;
        if (physicsRemoval != null) {
            adjustedPhysicsRemoval = new Matrix4f((Matrix4fc)internalInv).mul((Matrix4fc)physicsRemoval).mul((Matrix4fc)internal);
        }
        Matrix4f adjustedRemoval = adjustedPhysicsRemoval == null ? null : new Matrix4f(adjustedPhysicsRemoval);
        BedrockAxolPhysicsStateMachine.FeedbackPose axolPose = null;
        BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose = null;
        if (def != null && def.isPhysicsFeedbackEnabled() && axolModel && (axolPose = BedrockItemSystem.get().getAxolPhysicsPose(arm, partialTicks)) != null && axolPose.isIdentity()) {
            axolPose = null;
        }
        if (def != null && (pufferModel || BedrockItemRenderer.hasCreatureBonePhysics(bonePhysics)) && (pufferPose = BedrockItemSystem.get().getPufferPhysicsPose(arm, partialTicks)) != null && pufferPose.isIdentity()) {
            pufferPose = null;
        }
        ArrayList<Map.Entry<String, List<BedrockItemGeoModel.BedrockCube>>> entries = new ArrayList<Map.Entry<String, List<BedrockItemGeoModel.BedrockCube>>>(cubesByBone.entrySet());
        HashMap<String, Matrix4f> adjustedWorldByBone = new HashMap<String, Matrix4f>();
        HashMap<String, Boolean> suppressPhysicsByBone = new HashMap<String, Boolean>();
        HashMap<String, Matrix4f> renderBoneTransforms = new HashMap<String, Matrix4f>();
        Matrix4f renderRoot = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
        LinkedHashSet<String> particleBoneNames = new LinkedHashSet<String>(parts.keySet());
        particleBoneNames.addAll(cubesByBone.keySet());
        for (String boneName2 : particleBoneNames) {
            Matrix4f world;
            if (boneName2 == null || (world = BedrockItemRenderer.computeWorldMatrix(boneName2, parts, parentByBone, matrixCache)) == null) continue;
            boolean bl = noPhysicsBones != null && BedrockItemRenderer.isPendulumSuppressed(boneName2, parentByBone, noPhysicsBones);
            Matrix4f adjustedWorld = world;
            if (axolPose != null) {
                adjustedWorld = BedrockItemRenderer.applyAxolPhysicsIfNeeded(adjustedWorld, boneName2, axolPose, parentByBone, headWorld, bodyWorld);
            }
            if (pufferPose != null) {
                adjustedWorld = BedrockItemRenderer.applyPufferPhysicsIfNeeded(adjustedWorld, boneName2, pufferPose, parentByBone, fishWorld);
            }
            adjustedWorld = BedrockItemRenderer.applyCustomBonePhysicsIfNeeded(adjustedWorld, boneName2, bonePhysics, renderRoot, parts, parentByBone, matrixCache, pufferPose, arm, interactionHand, partialTicks);
            adjustedWorldByBone.put(boneName2, adjustedWorld);
            suppressPhysicsByBone.put(boneName2, bl);
            Matrix4f finalMatrix = new Matrix4f((Matrix4fc)renderRoot);
            if (adjustedRemoval != null && bl) {
                finalMatrix.mul((Matrix4fc)adjustedRemoval);
            }
            finalMatrix.mul((Matrix4fc)adjustedWorld);
            renderBoneTransforms.put(boneName2, finalMatrix);
        }
        List<String> compatParticleEffects = ToolTuning.getParticleEmitters(stack);
        ModelPartsParticleSystem.setRenderRoot(arm, poseStack.method_23760().method_23761());
        List<String> definitionParticleEffects = BedrockItemRenderer.mergeParticleEffects(def == null ? List.of() : def.getDefinitionParticleEffects(), compatParticleEffects);
        ModelPartsParticleSystem.emitDefinitionForRenderBones(class_310.method_1551(), arm, renderBoneTransforms, definitionParticleEffects);
        ModelPartsParticleSystem.emitPendingForRenderBones(class_310.method_1551(), arm, renderBoneTransforms);
        ModelPartsParticleSystem.renderForArm(class_310.method_1551(), arm, buffer, poseStack, packedLight);
        for (Map.Entry entry2 : entries) {
            class_2960 boneTexture;
            boneName = (String)entry2.getKey();
            cubes = (List)entry2.getValue();
            if (boneName == null || cubes == null || cubes.isEmpty() || (boneTexture = BedrockItemRenderer.resolveBoneTexture(boneName, texturesByBone, parentByBone, resolvedTextureByBone, texture)) == null) continue;
            class_1921 baseRenderType = class_1921.method_23580((class_2960)boneTexture);
            TextureAnim anim = BedrockItemRenderer.resolveAnimForBone((BedrockItemDefinition.TextureAnimSpec)animByBone.get(boneName), textureAnimFrames, textureAnimFrameTime, textureAnimFrameHeight, def, boneTexture, texW, texH, animTime);
            Matrix4f adjustedWorld = (Matrix4f)adjustedWorldByBone.get(boneName);
            if (adjustedWorld == null) continue;
            boolean suppressBonePhysics = Boolean.TRUE.equals(suppressPhysicsByBone.get(boneName));
            poseStack.method_22903();
            if (adjustedRemoval != null && suppressBonePhysics) {
                poseStack.method_23760().method_23761().mul((Matrix4fc)adjustedRemoval);
                poseStack.method_23760().method_23762().mul((Matrix3fc)new Matrix3f((Matrix4fc)adjustedRemoval));
            }
            poseStack.method_23760().method_23761().mul((Matrix4fc)adjustedWorld);
            poseStack.method_23760().method_23762().mul((Matrix3fc)new Matrix3f((Matrix4fc)adjustedWorld));
            boolean cameraTransparent = BedrockItemRenderer.findBonePhysicsSpec(boneName, bonePhysics, parentByBone) == null && BedrockItemRenderer.isCameraTransparentBone(boneName, parentByBone, cameraTransparentBones);
            class_4588 baseConsumer = buffer.getBuffer(baseRenderType);
            class_4587.class_4665 basePose = poseStack.method_23760();
            for (BedrockItemGeoModel.BedrockCube cube : cubes) {
                BedrockItemRenderer.renderCubeFaces(basePose, baseConsumer, cube, packedLight, texW, texH, 1.0f, 1.0f, anim, flipNormalsX, cameraTransparent);
            }
            poseStack.method_22909();
        }
        for (Map.Entry entry3 : entries) {
            class_2960 boneTexture;
            int emission;
            boneName = (String)entry3.getKey();
            cubes = (List)entry3.getValue();
            if (boneName == null || cubes == null || cubes.isEmpty() || (emission = BedrockItemRenderer.getBoneEmission(emissionByBone, boneName)) <= 0 || (boneTexture = BedrockItemRenderer.resolveBoneTexture(boneName, texturesByBone, parentByBone, resolvedTextureByBone, texture)) == null) continue;
            class_1921 emissiveRenderType = BedrockItemRenderer.emissiveOverlay(boneTexture, emissiveBlur, emissiveClamp, shaderPackInUse);
            TextureAnim anim = BedrockItemRenderer.resolveAnimForBone((BedrockItemDefinition.TextureAnimSpec)animByBone.get(boneName), textureAnimFrames, textureAnimFrameTime, textureAnimFrameHeight, def, boneTexture, texW, texH, animTime);
            Matrix4f adjustedWorld = (Matrix4f)adjustedWorldByBone.get(boneName);
            if (adjustedWorld == null) continue;
            boolean suppressBonePhysics = Boolean.TRUE.equals(suppressPhysicsByBone.get(boneName));
            float baseIntensity = BedrockItemRenderer.emissionToBaseIntensity(emission) * strengthScale;
            float extraGlow = BedrockItemRenderer.emissionToExtraGlow(emission) * strengthScale;
            if (baseIntensity <= 0.0f && extraGlow <= 0.0f) continue;
            float colorScale = Math.min(1.0f, baseIntensity);
            poseStack.method_22903();
            if (adjustedRemoval != null && suppressBonePhysics) {
                poseStack.method_23760().method_23761().mul((Matrix4fc)adjustedRemoval);
                poseStack.method_23760().method_23762().mul((Matrix3fc)new Matrix3f((Matrix4fc)adjustedRemoval));
            }
            poseStack.method_23760().method_23761().mul((Matrix4fc)adjustedWorld);
            poseStack.method_23760().method_23762().mul((Matrix3fc)new Matrix3f((Matrix4fc)adjustedWorld));
            boolean cameraTransparent = BedrockItemRenderer.findBonePhysicsSpec(boneName, bonePhysics, parentByBone) == null && BedrockItemRenderer.isCameraTransparentBone(boneName, parentByBone, cameraTransparentBones);
            class_4588 emissiveConsumer = buffer.getBuffer(emissiveRenderType);
            class_4587.class_4665 emissivePose = poseStack.method_23760();
            for (BedrockItemGeoModel.BedrockCube cube : cubes) {
                BedrockItemRenderer.renderCubeFaces(emissivePose, emissiveConsumer, cube, 0xF000F0, texW, texH, colorScale, 1.0f, anim, flipNormalsX, cameraTransparent);
            }
            poseStack.method_22909();
            if (!emissiveBlur) continue;
            float glowBoost = 1.0f + extraGlow * 1.5f;
            float blurScale = 1.08f + extraGlow * 0.08f;
            float blurColor = Math.min(1.0f, colorScale * (0.6f + extraGlow * 0.4f));
            float blurAlpha = Math.min(1.0f, 0.4f * glowBoost);
            poseStack.method_22903();
            if (adjustedRemoval != null && suppressBonePhysics) {
                poseStack.method_23760().method_23761().mul((Matrix4fc)adjustedRemoval);
                poseStack.method_23760().method_23762().mul((Matrix3fc)new Matrix3f((Matrix4fc)adjustedRemoval));
            }
            poseStack.method_23760().method_23761().mul((Matrix4fc)adjustedWorld);
            poseStack.method_23760().method_23762().mul((Matrix3fc)new Matrix3f((Matrix4fc)adjustedWorld));
            poseStack.method_22905(blurScale, blurScale, blurScale);
            class_4588 blurConsumer = buffer.getBuffer(emissiveRenderType);
            class_4587.class_4665 blurPose = poseStack.method_23760();
            for (BedrockItemGeoModel.BedrockCube cube : cubes) {
                BedrockItemRenderer.renderCubeFaces(blurPose, blurConsumer, cube, 0xF000F0, texW, texH, blurColor, blurAlpha, anim, flipNormalsX, cameraTransparent);
            }
            poseStack.method_22909();
        }
    }

    private static Matrix4f applyAxolPhysicsIfNeeded(Matrix4f world, String boneName, BedrockAxolPhysicsStateMachine.FeedbackPose axolPose, Map<String, String> parentByBone, Matrix4f headWorld, Matrix4f bodyWorld) {
        boolean inBodyRig;
        boolean inHeadRig;
        if (world == null || axolPose == null || boneName == null) {
            return world;
        }
        if ("fish".equals(boneName)) {
            return world;
        }
        boolean bl = inHeadRig = headWorld != null && ("head".equals(boneName) || BedrockItemRenderer.isBoneOrDescendantOf(boneName, "head", parentByBone));
        if (inHeadRig) {
            Matrix4f headFeedback = BedrockItemRenderer.buildAxolPhysicsTransform("head", axolPose);
            Matrix4f invHead = new Matrix4f((Matrix4fc)headWorld).invert();
            return new Matrix4f((Matrix4fc)headWorld).mul((Matrix4fc)headFeedback).mul((Matrix4fc)invHead).mul((Matrix4fc)world);
        }
        boolean bl2 = inBodyRig = bodyWorld != null && ("body".equals(boneName) || BedrockItemRenderer.isBoneOrDescendantOf(boneName, "body", parentByBone));
        if (inBodyRig) {
            Matrix4f bodyFeedback = BedrockItemRenderer.buildAxolPhysicsTransform("body", axolPose);
            Matrix4f invBody = new Matrix4f((Matrix4fc)bodyWorld).invert();
            Matrix4f adjusted = new Matrix4f((Matrix4fc)bodyWorld).mul((Matrix4fc)bodyFeedback).mul((Matrix4fc)invBody).mul((Matrix4fc)world);
            if ("leg1".equals(boneName) || "leg2".equals(boneName) || "leg4".equals(boneName) || "leg5".equals(boneName)) {
                adjusted.mul((Matrix4fc)BedrockItemRenderer.buildAxolPhysicsTransform(boneName, axolPose));
            }
            return adjusted;
        }
        if (!AXOL_PHYSICS_BONES.contains(boneName)) {
            return world;
        }
        Matrix4f fallbackFeedback = BedrockItemRenderer.buildAxolPhysicsTransform(boneName, axolPose);
        return new Matrix4f((Matrix4fc)world).mul((Matrix4fc)fallbackFeedback);
    }

    private static Matrix4f buildAxolPhysicsTransform(String boneName, BedrockAxolPhysicsStateMachine.FeedbackPose axolPose) {
        float tyMul = 1.0f;
        float tzMul = 1.0f;
        float pitchMul = 1.0f;
        float rollMul = 1.0f;
        float scaleMul = 1.0f;
        float extraPitch = 0.0f;
        float extraYaw = 0.0f;
        float extraRoll = 0.0f;
        float extraTranslateX = 0.0f;
        float extraTranslateZ = 0.0f;
        float wf = axolPose.impactWave();
        float armGate = axolPose.armRaiseGate();
        float landingPunch = axolPose.landingPunch();
        float walkPitch = axolPose.walkPitchDeg();
        float walkRollMul = 0.5f;
        if ("head".equals(boneName)) {
            tyMul = 1.0f;
            tzMul = 1.0f;
            pitchMul = 0.975f;
            rollMul = 1.6575f;
            scaleMul = 0.45f;
            walkRollMul = -0.5f;
            extraTranslateX = -walkPitch * 0.035f;
            extraRoll += axolPose.translateXUnits() * 31.2f;
        } else if ("body".equals(boneName) || "legs_front".equals(boneName) || "legs_rear".equals(boneName)) {
            tyMul = 0.65f;
            tzMul = 0.6f;
            pitchMul = 0.4f;
            rollMul = 0.55f;
            scaleMul = 0.35f;
            walkRollMul = 0.3f;
            extraRoll += axolPose.translateXUnits() * 12.0f;
        } else if ("leg1".equals(boneName) || "leg2".equals(boneName) || "leg4".equals(boneName) || "leg5".equals(boneName)) {
            tyMul = 0.25f;
            tzMul = 0.2f;
            pitchMul = 0.25f;
            rollMul = 0.3f;
            scaleMul = 0.2f;
            float side = "leg2".equals(boneName) || "leg5".equals(boneName) ? -1.0f : 1.0f;
            extraPitch = 24.0f * armGate;
            extraYaw = 18.0f * armGate * side;
            extraRoll = 12.0f * wf * armGate * side;
            extraTranslateZ = 0.08f * armGate;
        } else if ("tail".equals(boneName)) {
            tyMul = 0.35f;
            tzMul = 0.3f;
            pitchMul = 0.45f;
            rollMul = 0.55f;
            scaleMul = 0.25f;
        }
        float tx = axolPose.translateXUnits();
        float ty = axolPose.translateYUnits() * tyMul + axolPose.highFallLiftUnits();
        float tz = axolPose.translateZUnits() * tzMul;
        float pitchBase = axolPose.pitchDeg();
        if (("body".equals(boneName) || "legs_front".equals(boneName) || "legs_rear".equals(boneName)) && pitchBase < 0.0f) {
            pitchBase *= 0.32f;
        }
        float pitch = pitchBase * pitchMul + extraPitch;
        float yaw = extraYaw;
        float roll = axolPose.rollDeg() * rollMul + walkPitch * walkRollMul + extraRoll;
        float sx = 1.0f + (axolPose.scaleX() - 1.0f) * scaleMul;
        float sy = 1.0f + (axolPose.scaleY() - 1.0f) * scaleMul;
        float sz = 1.0f + (axolPose.scaleZ() - 1.0f) * scaleMul;
        return new Matrix4f().translate(extraTranslateX + tx, ty, tz + extraTranslateZ).rotateXYZ((float)Math.toRadians(pitch), (float)Math.toRadians(yaw), (float)Math.toRadians(roll)).scale(sx, sy, sz);
    }

    private static Matrix4f applyPufferPhysicsIfNeeded(Matrix4f world, String boneName, BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose, Map<String, String> parentByBone, Matrix4f fishWorld) {
        if (world == null || boneName == null || pufferPose == null) {
            return world;
        }
        boolean underFish = BedrockItemRenderer.isBoneOrDescendantOf(boneName, "fish", parentByBone);
        if (!underFish) {
            return world;
        }
        Matrix4f adjusted = new Matrix4f((Matrix4fc)world);
        if (fishWorld != null) {
            Matrix4f base = BedrockItemRenderer.buildPufferBaseTransform(pufferPose);
            Matrix4f invFish = new Matrix4f((Matrix4fc)fishWorld).invert();
            adjusted = new Matrix4f((Matrix4fc)fishWorld).mul((Matrix4fc)base).mul((Matrix4fc)invFish).mul((Matrix4fc)adjusted);
        }
        if (BedrockItemRenderer.isPufferFinBone(boneName)) {
            adjusted.mul((Matrix4fc)BedrockItemRenderer.buildPufferInverseScaleTransform(pufferPose));
            adjusted.mul((Matrix4fc)BedrockItemRenderer.buildPufferFinTransform(boneName, pufferPose));
            return adjusted;
        }
        String finAnchor = null;
        if (BedrockItemRenderer.isBoneOrDescendantOf(boneName, "right_fin", parentByBone)) {
            finAnchor = "right_fin";
        } else if (BedrockItemRenderer.isBoneOrDescendantOf(boneName, "fin_right", parentByBone)) {
            finAnchor = "fin_right";
        } else if (BedrockItemRenderer.isBoneOrDescendantOf(boneName, "left_fin", parentByBone)) {
            finAnchor = "left_fin";
        } else if (BedrockItemRenderer.isBoneOrDescendantOf(boneName, "fin_left", parentByBone)) {
            finAnchor = "fin_left";
        }
        if (finAnchor != null) {
            adjusted.mul((Matrix4fc)BedrockItemRenderer.buildPufferInverseScaleTransform(pufferPose));
            adjusted.mul((Matrix4fc)BedrockItemRenderer.buildPufferFinTransform(finAnchor, pufferPose));
        }
        return adjusted;
    }

    private static Matrix4f applyCustomBonePhysicsIfNeeded(Matrix4f world, String boneName, Map<String, BedrockItemDefinition.BonePhysicsSpec> bonePhysics, Matrix4f renderRoot, Map<String, class_630> parts, Map<String, String> parentByBone, Map<String, Matrix4f> matrixCache, BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose, class_1306 arm, class_1268 hand, float partialTicks) {
        if (world == null || boneName == null || bonePhysics == null || bonePhysics.isEmpty()) {
            return world;
        }
        List<String> lineage = BedrockItemRenderer.collectBoneLineage(boneName, parentByBone);
        if (lineage.isEmpty()) {
            return world;
        }
        Matrix4f adjusted = new Matrix4f((Matrix4fc)world);
        Matrix4f accumulatedDelta = new Matrix4f();
        for (String currentBone : lineage) {
            Matrix4f inheritedWorld;
            Matrix4f delta;
            Matrix4f baseWorld;
            BedrockItemDefinition.BonePhysicsSpec spec = bonePhysics.get(currentBone);
            if (spec == null || spec.isEmpty() || (baseWorld = BedrockItemRenderer.computeWorldMatrix(currentBone, parts, parentByBone, matrixCache)) == null || (delta = BedrockItemRenderer.computeCustomBonePhysicsDelta(inheritedWorld = new Matrix4f((Matrix4fc)accumulatedDelta).mul((Matrix4fc)baseWorld), baseWorld, renderRoot, spec, pufferPose, arm, hand, partialTicks, currentBone)) == null) continue;
            adjusted = new Matrix4f((Matrix4fc)delta).mul((Matrix4fc)adjusted);
            accumulatedDelta = new Matrix4f((Matrix4fc)delta).mul((Matrix4fc)accumulatedDelta);
        }
        return adjusted;
    }

    private static Matrix4f computeCustomBonePhysicsDelta(Matrix4f applicationWorld, Matrix4f solverWorld, Matrix4f renderRoot, BedrockItemDefinition.BonePhysicsSpec spec, BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose, class_1306 arm, class_1268 hand, float partialTicks, String bonePhysicsChannel) {
        float deform;
        if (applicationWorld == null || solverWorld == null || spec == null || spec.isEmpty()) {
            return null;
        }
        Matrix4f adjusted = new Matrix4f((Matrix4fc)applicationWorld);
        if (spec.enableCreaturePhysics() && pufferPose != null) {
            adjusted.mul((Matrix4fc)BedrockItemRenderer.buildPufferBaseTransform(pufferPose, spec));
        }
        if (spec.enableSlimePhysics() && Math.abs(deform = SlimeInHandPhysics.get().sampleSlimeBlockDeform(hand, partialTicks)) > 1.0E-4f) {
            if (deform > 0.0f) {
                deform *= spec.slimeFallScale();
            } else if (deform < 0.0f) {
                deform *= spec.slimeImpactScale();
            }
            float horizontal = SlimeInHandPhysics.horizontalScaleForDeform(deform);
            float vertical = SlimeInHandPhysics.verticalScaleForDeform(deform);
            adjusted.scale(horizontal, vertical, horizontal);
        }
        if (spec.pendulumMode() != null && spec.pendulumMode() != ToolTuning.PhysicsMode.NONE && arm != null) {
            Matrix4f pendulumSolverBasis = new Matrix4f((Matrix4fc)solverWorld);
            Matrix4f pendulumReference = renderRoot == null ? pendulumSolverBasis : new Matrix4f((Matrix4fc)renderRoot).mul((Matrix4fc)pendulumSolverBasis);
            float inheritedPitchDeg = BedrockItemRenderer.extractInheritedPitchDeg(applicationWorld, solverWorld);
            ToolTuning.PhysicsSettings effectiveSettings = BedrockItemRenderer.applyRemainingPendulumLimit(spec.pendulumSettings(), inheritedPitchDeg);
            Matrix4f pendulum = PendulumPhysics.get().computeAppliedMatrix(pendulumReference, spec.pendulumMode(), new Vector3f(), effectiveSettings, arm, partialTicks, bonePhysicsChannel, inheritedPitchDeg);
            if (pendulum != null) {
                adjusted.mul((Matrix4fc)pendulum);
            }
        }
        return adjusted.mul((Matrix4fc)new Matrix4f((Matrix4fc)applicationWorld).invert());
    }

    private static float extractInheritedPitchDeg(Matrix4f applicationWorld, Matrix4f solverWorld) {
        if (applicationWorld == null || solverWorld == null) {
            return 0.0f;
        }
        Matrix4f inheritedDelta = new Matrix4f((Matrix4fc)applicationWorld).mul((Matrix4fc)new Matrix4f((Matrix4fc)solverWorld).invert());
        Quaternionf rotation = inheritedDelta.getUnnormalizedRotation(new Quaternionf());
        if (rotation.lengthSquared() < 1.0E-8f) {
            return 0.0f;
        }
        rotation.normalize();
        Vector3f forward = rotation.transform(new Vector3f(0.0f, 0.0f, 1.0f));
        float horizontalLen = (float)Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        return (float)Math.toDegrees(Math.atan2(-forward.y, horizontalLen));
    }

    private static ToolTuning.PhysicsSettings applyRemainingPendulumLimit(ToolTuning.PhysicsSettings settings, float inheritedPitchDeg) {
        boolean hasAnyLimit;
        if (settings == null) {
            return null;
        }
        boolean bl = hasAnyLimit = settings.limitDeg != null || settings.limitDegUp != null || settings.limitDegDown != null;
        if (!hasAnyLimit) {
            return settings;
        }
        ToolTuning.PhysicsSettings adjusted = settings.copy();
        float consumed = Math.abs(inheritedPitchDeg);
        if (adjusted.limitDeg != null) {
            adjusted.limitDeg = Float.valueOf(Math.max(0.0f, adjusted.limitDeg.floatValue() - consumed));
        }
        if (adjusted.limitDegUp != null) {
            adjusted.limitDegUp = Float.valueOf(Math.max(0.0f, adjusted.limitDegUp.floatValue() - consumed));
        }
        if (adjusted.limitDegDown != null) {
            adjusted.limitDegDown = Float.valueOf(Math.max(0.0f, adjusted.limitDegDown.floatValue() - consumed));
        }
        return adjusted;
    }

    private static List<String> collectBoneLineage(String boneName, Map<String, String> parentByBone) {
        if (boneName == null) {
            return List.of();
        }
        ArrayList<String> reversed = new ArrayList<String>();
        String current = boneName;
        for (int guard = 0; current != null && guard < 64; ++guard) {
            reversed.add(current);
            current = parentByBone != null ? parentByBone.get(current) : null;
        }
        Collections.reverse(reversed);
        return reversed;
    }

    private static BedrockItemDefinition.BonePhysicsSpec findBonePhysicsSpec(String boneName, Map<String, BedrockItemDefinition.BonePhysicsSpec> bonePhysics, Map<String, String> parentByBone) {
        if (boneName == null || bonePhysics == null || bonePhysics.isEmpty()) {
            return null;
        }
        String current = boneName;
        for (int guard = 0; current != null && guard < 64; ++guard) {
            BedrockItemDefinition.BonePhysicsSpec spec = bonePhysics.get(current);
            if (spec != null) {
                return spec;
            }
            current = parentByBone != null ? parentByBone.get(current) : null;
        }
        return null;
    }

    private static Map<String, BedrockItemDefinition.BonePhysicsSpec> mergeBonePhysics(Map<String, BedrockItemDefinition.BonePhysicsSpec> definitionPhysics, JsonElement specificPhysicsJson) {
        Map<String, BedrockItemDefinition.BonePhysicsSpec> parsedSpecific = BedrockItemDefinition.parseBonePhysics(specificPhysicsJson);
        if ((definitionPhysics == null || definitionPhysics.isEmpty()) && parsedSpecific.isEmpty()) {
            return Map.of();
        }
        HashMap<String, BedrockItemDefinition.BonePhysicsSpec> merged = new HashMap<String, BedrockItemDefinition.BonePhysicsSpec>();
        if (definitionPhysics != null && !definitionPhysics.isEmpty()) {
            merged.putAll(definitionPhysics);
        }
        for (Map.Entry<String, BedrockItemDefinition.BonePhysicsSpec> entry : parsedSpecific.entrySet()) {
            String bone = entry.getKey();
            BedrockItemDefinition.BonePhysicsSpec spec = entry.getValue();
            if (bone == null || bone.isBlank() || spec == null) continue;
            BedrockItemDefinition.BonePhysicsSpec previous = (BedrockItemDefinition.BonePhysicsSpec)merged.get(bone);
            merged.put(bone, previous == null ? spec : previous.merge(spec));
        }
        return merged;
    }

    private static boolean hasCreatureBonePhysics(Map<String, BedrockItemDefinition.BonePhysicsSpec> bonePhysics) {
        if (bonePhysics == null || bonePhysics.isEmpty()) {
            return false;
        }
        for (BedrockItemDefinition.BonePhysicsSpec spec : bonePhysics.values()) {
            if (spec == null || !spec.enableCreaturePhysics()) continue;
            return true;
        }
        return false;
    }

    private static Matrix4f buildPufferBaseTransform(BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose, BedrockItemDefinition.BonePhysicsSpec spec) {
        float pitch = pufferPose.pitchDeg() * 1.19f * 1.3f;
        float translationScale = 1.0f;
        if (spec != null) {
            if (pitch > 0.0f) {
                pitch *= spec.impactInclinationScale();
                translationScale = spec.impactTranslationScale();
            } else if (pitch < 0.0f) {
                pitch *= spec.fallInclinationScale();
                translationScale = spec.fallTranslationScale();
            }
        }
        float tx = pufferPose.translateXUnits();
        float ty = pufferPose.translateYUnits() * translationScale;
        float tz = pufferPose.translateZUnits() * translationScale;
        float roll = pufferPose.rollDeg() * 1.19f;
        float sx = pufferPose.scaleX();
        float sy = pufferPose.scaleY();
        float sz = pufferPose.scaleZ();
        return new Matrix4f().translate(tx, ty, tz).rotateXYZ((float)Math.toRadians(pitch), 0.0f, (float)Math.toRadians(roll)).scale(sx, sy, sz);
    }

    private static Matrix4f buildPufferBaseTransform(BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose) {
        return BedrockItemRenderer.buildPufferBaseTransform(pufferPose, new BedrockItemDefinition.BonePhysicsSpec(true, false, ToolTuning.PhysicsMode.NONE, null, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f));
    }

    private static Matrix4f buildPufferInverseScaleTransform(BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose) {
        float sx = pufferPose.scaleX();
        float sy = pufferPose.scaleY();
        float sz = pufferPose.scaleZ();
        float invX = Math.abs(sx) > 1.0E-4f ? 1.0f / sx : 1.0f;
        float invY = Math.abs(sy) > 1.0E-4f ? 1.0f / sy : 1.0f;
        float invZ = Math.abs(sz) > 1.0E-4f ? 1.0f / sz : 1.0f;
        return new Matrix4f().scale(invX, invY, invZ);
    }

    private static Matrix4f buildPufferFinTransform(String boneName, BedrockPufferPhysicsStateMachine.FeedbackPose pufferPose) {
        boolean rightFin = "right_fin".equals(boneName) || "fin_right".equals(boneName);
        float side = rightFin ? 1.0f : -1.0f;
        float fin = class_3532.method_15363((float)pufferPose.finLift(), (float)0.0f, (float)1.0f);
        float finCurve = fin * fin * (3.0f - 2.0f * fin);
        float liftRoll = 32.0f * finCurve * 1.19f;
        float sideRoll = 6.0f * finCurve * 1.19f;
        float yaw = 0.0f;
        float pitch = 0.0f;
        float roll = (liftRoll + sideRoll) * side;
        return new Matrix4f().translate(0.0f, 0.06f * finCurve, 0.085f * finCurve).rotateXYZ((float)Math.toRadians(pitch), (float)Math.toRadians(yaw), (float)Math.toRadians(roll));
    }

    private static boolean isPufferFinBone(String boneName) {
        return "right_fin".equals(boneName) || "fin_right".equals(boneName) || "left_fin".equals(boneName) || "fin_left".equals(boneName);
    }

    private static boolean isPufferModel(Map<String, class_630> parts) {
        if (parts == null || parts.isEmpty()) {
            return false;
        }
        return !(!parts.containsKey("fish") || !parts.containsKey("right_fin") && !parts.containsKey("fin_right") || !parts.containsKey("left_fin") && !parts.containsKey("fin_left"));
    }

    private static boolean isAxolModel(Map<String, class_630> parts, Map<String, List<BedrockItemGeoModel.BedrockCube>> cubesByBone) {
        boolean hasLegPart;
        boolean hasHeadPart = parts != null && parts.containsKey("head");
        boolean hasBodyPart = parts != null && (parts.containsKey("body") || parts.containsKey("body_root"));
        boolean bl = hasLegPart = parts != null && (parts.containsKey("legs_front") || parts.containsKey("leg1"));
        if (hasHeadPart && hasBodyPart && hasLegPart) {
            return true;
        }
        if (cubesByBone == null || cubesByBone.isEmpty()) {
            return false;
        }
        return !(!cubesByBone.containsKey("head") || !cubesByBone.containsKey("body") && !cubesByBone.containsKey("body_root") || !cubesByBone.containsKey("legs_front") && !cubesByBone.containsKey("leg1"));
    }

    private static boolean isBoneOrDescendantOf(String boneName, String ancestor, Map<String, String> parentByBone) {
        if (boneName == null || ancestor == null) {
            return false;
        }
        if (ancestor.equals(boneName)) {
            return true;
        }
        String current = boneName;
        for (int guard = 0; current != null && guard < 128; ++guard) {
            String string = current = parentByBone != null ? parentByBone.get(current) : null;
            if (!ancestor.equals(current)) continue;
            return true;
        }
        return false;
    }

    private static Matrix4f computeWorldMatrix(String boneName, Map<String, class_630> parts, Map<String, String> parentByBone, Map<String, Matrix4f> cache) {
        Matrix4f parentMatrix;
        Matrix4f cached = cache.get(boneName);
        if (cached != null) {
            return cached;
        }
        class_630 part = parts.get(boneName);
        if (part == null) {
            return null;
        }
        Matrix4f local = ModelPartMath.getLocalMatrix(part);
        String parent = parentByBone.get(boneName);
        Matrix4f world = parent != null ? ((parentMatrix = BedrockItemRenderer.computeWorldMatrix(parent, parts, parentByBone, cache)) != null ? new Matrix4f((Matrix4fc)parentMatrix).mul((Matrix4fc)local) : local) : local;
        cache.put(boneName, world);
        return world;
    }

    private static Map<String, class_2960> mergeDynamicBoneTextures(class_1799 stack, BedrockItemDefinition def, Map<String, class_2960> baseTextures) {
        Map<String, class_2960> base;
        Map<Object, Object> map = base = baseTextures == null ? Map.of() : baseTextures;
        if (def == null || def.getDynamicBoneTextures().isEmpty()) {
            return base;
        }
        HashMap<String, class_2960> merged = new HashMap<String, class_2960>(base);
        DynamicValueCache cache = new DynamicValueCache();
        for (BedrockItemDefinition.DynamicBoneTextureSpec spec : def.getDynamicBoneTextures()) {
            class_2960 texture;
            if (spec == null || !spec.isTextureType() || spec.bone() == null || spec.bone().isBlank()) continue;
            String dynamicKey = BedrockItemRenderer.resolveDynamicKey(stack, spec, cache);
            class_2960 class_29602 = texture = dynamicKey == null ? null : spec.resourceMap().get(dynamicKey);
            if (texture == null) {
                texture = spec.fallbackResource();
            }
            if (texture == null) continue;
            merged.put(spec.bone().trim(), texture);
        }
        return merged;
    }

    private static class_2960 resolveDynamicGeoId(class_1799 stack, BedrockItemDefinition def) {
        if (def == null || def.getDynamicBoneTextures().isEmpty()) {
            return null;
        }
        class_2960 resolved = null;
        DynamicValueCache cache = new DynamicValueCache();
        for (BedrockItemDefinition.DynamicBoneTextureSpec spec : def.getDynamicBoneTextures()) {
            class_2960 geoId;
            if (spec == null || !spec.isGeoType()) continue;
            String dynamicKey = BedrockItemRenderer.resolveDynamicKey(stack, spec, cache);
            class_2960 class_29602 = geoId = dynamicKey == null ? null : spec.resourceMap().get(dynamicKey);
            if (geoId == null) {
                geoId = spec.fallbackResource();
            }
            if (geoId == null) continue;
            resolved = geoId;
        }
        return resolved;
    }

    private static String resolveDynamicKey(class_1799 stack, BedrockItemDefinition.DynamicBoneTextureSpec spec, DynamicValueCache cache) {
        if (spec == null) {
            return null;
        }
        if (spec.usesAxolVariants()) {
            if (!cache.axolResolved) {
                cache.axolResolved = true;
                cache.axolVariant = BedrockItemRenderer.resolveAxolotlVariant(stack);
            }
            return cache.axolVariant;
        }
        return null;
    }

    private static String resolveAxolotlVariant(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        class_9279 data = (class_9279)stack.method_57824(class_9334.field_49610);
        if (data == null || data.method_57458()) {
            return null;
        }
        class_2487 tag = data.method_57461();
        if (tag == null || !tag.method_10545("Variant")) {
            return null;
        }
        if (tag.method_10573("Variant", 8)) {
            return BedrockItemRenderer.normalizeAxolotlVariant(tag.method_10558("Variant"));
        }
        if (tag.method_10573("Variant", 99)) {
            return BedrockItemRenderer.axolotlVariantNameById(tag.method_10550("Variant"));
        }
        return null;
    }

    private static String normalizeAxolotlVariant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        int namespaceSplit = normalized.indexOf(58);
        if (namespaceSplit >= 0 && namespaceSplit + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSplit + 1);
        }
        return switch (normalized) {
            case "lucy", "wild", "gold", "cyan", "blue" -> normalized;
            default -> null;
        };
    }

    private static String axolotlVariantNameById(int id) {
        return switch (id) {
            case 0 -> "lucy";
            case 1 -> "wild";
            case 2 -> "gold";
            case 3 -> "cyan";
            case 4 -> "blue";
            default -> null;
        };
    }

    private static class_2960 resolveBoneTexture(String boneName, Map<String, class_2960> texturesByBone, Map<String, String> parentByBone, Map<String, class_2960> resolvedTextureByBone, class_2960 fallback) {
        if (boneName == null) {
            return fallback;
        }
        class_2960 direct = BedrockItemRenderer.findTextureOverride(texturesByBone, boneName);
        if (direct != null) {
            resolvedTextureByBone.put(boneName, direct);
            return direct;
        }
        class_2960 cached = resolvedTextureByBone.get(boneName);
        if (cached != null) {
            return cached;
        }
        String current = boneName;
        for (int guard = 0; current != null && guard < 64; ++guard) {
            class_2960 found = BedrockItemRenderer.findTextureOverride(texturesByBone, current);
            if (found != null) {
                resolvedTextureByBone.put(boneName, found);
                return found;
            }
            current = parentByBone != null ? parentByBone.get(current) : null;
        }
        if (fallback != null) {
            resolvedTextureByBone.put(boneName, fallback);
        }
        return fallback;
    }

    private static class_2960 findTextureOverride(Map<String, class_2960> texturesByBone, String boneName) {
        class_2960 trimmedMatch;
        if (texturesByBone == null || texturesByBone.isEmpty() || boneName == null) {
            return null;
        }
        class_2960 direct = texturesByBone.get(boneName);
        if (direct != null) {
            return direct;
        }
        String trimmed = boneName.trim();
        if (!trimmed.equals(boneName) && (trimmedMatch = texturesByBone.get(trimmed)) != null) {
            return trimmedMatch;
        }
        for (Map.Entry<String, class_2960> entry : texturesByBone.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.trim().equalsIgnoreCase(trimmed)) continue;
            return entry.getValue();
        }
        return null;
    }

    private static void renderCubeFaces(class_4587.class_4665 pose, class_4588 consumer, BedrockItemGeoModel.BedrockCube cube, int packedLight, int texW, int texH, float colorScale, float alphaScale, TextureAnim anim, boolean flipNormalsX, boolean cameraTransparent) {
        Map<class_2350, BedrockItemGeoModel.BedrockFace> faces = cube.faces;
        if (faces == null || faces.isEmpty()) {
            return;
        }
        Map<class_2350, Boolean> skipFaces = BedrockItemRenderer.computeThinFaceSkips(cube, faces);
        for (Map.Entry<class_2350, BedrockItemGeoModel.BedrockFace> entry : faces.entrySet()) {
            class_2350 direction = entry.getKey();
            BedrockItemGeoModel.BedrockFace face = entry.getValue();
            if (direction == null || face == null || skipFaces != null && Boolean.TRUE.equals(skipFaces.get(direction))) continue;
            BedrockItemRenderer.emitFace(pose, consumer, direction, cube, face, packedLight, texW, texH, colorScale, alphaScale, anim, flipNormalsX, cameraTransparent);
        }
    }

    private static Map<class_2350, Boolean> computeThinFaceSkips(BedrockItemGeoModel.BedrockCube cube, Map<class_2350, BedrockItemGeoModel.BedrockFace> faces) {
        boolean thinZ;
        boolean thinY;
        if (cube == null || faces == null || faces.isEmpty()) {
            return null;
        }
        float dx = Math.abs(cube.x2 - cube.x1);
        float dy = Math.abs(cube.y2 - cube.y1);
        float dz = Math.abs(cube.z2 - cube.z1);
        float eps = 0.001f;
        if (dx > eps && dy > eps && dz > eps) {
            return null;
        }
        HashMap<class_2350, Boolean> skip = new HashMap<class_2350, Boolean>();
        boolean thinX = dx <= eps;
        int thinCount = (thinX ? 1 : 0) + ((thinY = dy <= eps) ? 1 : 0) + ((thinZ = dz <= eps) ? 1 : 0);
        if (thinCount >= 2) {
            class_2350 keep = BedrockItemRenderer.findLargestFace(faces);
            for (class_2350 dir : faces.keySet()) {
                if (dir == keep) continue;
                skip.put(dir, true);
            }
            return skip;
        }
        if (thinX) {
            skip.put(class_2350.field_11043, true);
            skip.put(class_2350.field_11035, true);
            skip.put(class_2350.field_11036, true);
            skip.put(class_2350.field_11033, true);
            return skip;
        }
        if (thinY) {
            skip.put(class_2350.field_11043, true);
            skip.put(class_2350.field_11035, true);
            skip.put(class_2350.field_11034, true);
            skip.put(class_2350.field_11039, true);
            return skip;
        }
        if (thinZ) {
            skip.put(class_2350.field_11034, true);
            skip.put(class_2350.field_11039, true);
            skip.put(class_2350.field_11036, true);
            skip.put(class_2350.field_11033, true);
            return skip;
        }
        return skip;
    }

    private static class_2350 findLargestFace(Map<class_2350, BedrockItemGeoModel.BedrockFace> faces) {
        class_2350 best = null;
        float bestArea = -1.0f;
        for (Map.Entry<class_2350, BedrockItemGeoModel.BedrockFace> entry : faces.entrySet()) {
            float area;
            BedrockItemGeoModel.BedrockFace face = entry.getValue();
            if (face == null || !((area = Math.abs(face.uSize * face.vSize)) > bestArea)) continue;
            bestArea = area;
            best = entry.getKey();
        }
        return best;
    }

    private static void emitFace(class_4587.class_4665 pose, class_4588 consumer, class_2350 direction, BedrockItemGeoModel.BedrockCube cube, BedrockItemGeoModel.BedrockFace face, int packedLight, int texW, int texH, float colorScale, float alphaScale, TextureAnim anim, boolean flipNormalsX, boolean cameraTransparent) {
        if (cube == null) {
            return;
        }
        float x1 = cube.x1;
        float y1 = cube.y1;
        float z1 = cube.z1;
        float x2 = cube.x2;
        float y2 = cube.y2;
        float z2 = cube.z2;
        float uPx = face.u;
        float vPx = face.v;
        float uSize = face.uSize;
        float vSize = face.vSize;
        int rotation = (face.rotation % 360 + 360) % 360;
        int rotSteps = rotation / 90 % 4;
        float uWidth = texW > 0 ? (uPx + uSize) / (float)texW : 0.0f;
        float vHeight = texH > 0 ? (vPx + vSize) / (float)texH : 0.0f;
        float u = texW > 0 ? uPx / (float)texW : 0.0f;
        float v = texH > 0 ? vPx / (float)texH : 0.0f;
        Vector3f normal = new Vector3f((float)direction.method_10148(), (float)direction.method_10164(), (float)direction.method_10165());
        if (flipNormalsX) {
            normal.x = -normal.x;
        }
        if (!cube.mirror) {
            float tmp = uWidth;
            uWidth = u;
            u = tmp;
        } else {
            normal.mul(-1.0f, 1.0f, 1.0f);
        }
        float[] uvs = BedrockItemRenderer.rotateUvs(u, v, uWidth, vHeight, rotSteps);
        Vector3f[] vertices = BedrockItemRenderer.getQuadVertices(direction, x1, y1, z1, x2, y2, z2, cube.mirror);
        if (vertices == null) {
            return;
        }
        if (flipNormalsX) {
            Vector3f tmpV = vertices[1];
            vertices[1] = vertices[3];
            vertices[3] = tmpV;
            float tmpU = uvs[2];
            float tmpVuv = uvs[3];
            uvs[2] = uvs[6];
            uvs[3] = uvs[7];
            uvs[6] = tmpU;
            uvs[7] = tmpVuv;
        }
        if (cube.hasRotation) {
            Matrix4f rotationMatrix = new Matrix4f().translate(cube.pivotX, cube.pivotY, cube.pivotZ).rotateZ(cube.rotZ).rotateY(cube.rotY).rotateX(cube.rotX).translate(-cube.pivotX, -cube.pivotY, -cube.pivotZ);
            Matrix3f normalMatrix = new Matrix3f().rotateZ(cube.rotZ).rotateY(cube.rotY).rotateX(cube.rotX);
            for (Vector3f vertex : vertices) {
                rotationMatrix.transformPosition(vertex);
            }
            normalMatrix.transform(normal);
        }
        if (cameraTransparent && !BedrockItemRenderer.isInsideVisibleFace(pose, vertices, normal, flipNormalsX)) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            float uNorm = class_3532.method_15363((float)uvs[i * 2], (float)0.0f, (float)1.0f);
            float vNorm = class_3532.method_15363((float)uvs[i * 2 + 1], (float)0.0f, (float)1.0f);
            if (anim != null) {
                vNorm = (vNorm + (float)anim.frameIndex) * anim.invFrames;
            }
            BedrockItemRenderer.addVertex(pose, consumer, vertex.x, vertex.y, vertex.z, uNorm, vNorm, packedLight, normal.x, normal.y, normal.z, colorScale, alphaScale);
        }
    }

    private static boolean isInsideVisibleFace(class_4587.class_4665 pose, Vector3f[] vertices, Vector3f normal, boolean flipNormalsX) {
        if (pose == null || vertices == null || vertices.length < 4) {
            return true;
        }
        Vector3f[] transformed = new Vector3f[4];
        Vector3f center = new Vector3f();
        int count = 0;
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            if (vertex == null) continue;
            Vector3f tv = new Vector3f((Vector3fc)vertex);
            pose.method_23761().transformPosition(tv);
            transformed[i] = tv;
            center.add((Vector3fc)tv);
            ++count;
        }
        if (count < 3) {
            return true;
        }
        center.mul(1.0f / (float)count);
        Vector3f worldNormal = null;
        if (transformed[0] != null && transformed[1] != null && transformed[2] != null) {
            Vector3f e1 = new Vector3f((Vector3fc)transformed[1]).sub((Vector3fc)transformed[0]);
            Vector3f e2 = new Vector3f((Vector3fc)transformed[2]).sub((Vector3fc)transformed[0]);
            worldNormal = e1.cross((Vector3fc)e2, new Vector3f());
        }
        if ((worldNormal == null || worldNormal.lengthSquared() <= 1.0E-6f) && normal != null) {
            worldNormal = new Vector3f((Vector3fc)normal);
            pose.method_23762().transform(worldNormal);
        }
        if (worldNormal.lengthSquared() <= 1.0E-6f || center.lengthSquared() <= 1.0E-6f) {
            return true;
        }
        worldNormal.normalize();
        Vector3f toCamera = new Vector3f((Vector3fc)center).negate().normalize();
        float dot = worldNormal.dot((Vector3fc)toCamera);
        return dot >= 0.0f;
    }

    private static Vector3f[] getQuadVertices(class_2350 direction, float x1, float y1, float z1, float x2, float y2, float z2, boolean mirror) {
        Vector3f[] vector3fArray;
        if (direction == null) {
            return null;
        }
        Vector3f bottomLeftBack = new Vector3f(x1, y1, z1);
        Vector3f bottomRightBack = new Vector3f(x1, y1, z2);
        Vector3f topLeftBack = new Vector3f(x1, y2, z1);
        Vector3f topRightBack = new Vector3f(x1, y2, z2);
        Vector3f topLeftFront = new Vector3f(x2, y2, z1);
        Vector3f topRightFront = new Vector3f(x2, y2, z2);
        Vector3f bottomLeftFront = new Vector3f(x2, y1, z1);
        Vector3f bottomRightFront = new Vector3f(x2, y1, z2);
        switch (direction) {
            default: {
                throw new MatchException(null, null);
            }
            case field_11039: {
                if (mirror) {
                    Vector3f[] vector3fArray2 = new Vector3f[4];
                    vector3fArray2[0] = topLeftFront;
                    vector3fArray2[1] = topRightFront;
                    vector3fArray2[2] = bottomRightFront;
                    vector3fArray = vector3fArray2;
                    vector3fArray2[3] = bottomLeftFront;
                    break;
                }
                Vector3f[] vector3fArray3 = new Vector3f[4];
                vector3fArray3[0] = topRightBack;
                vector3fArray3[1] = topLeftBack;
                vector3fArray3[2] = bottomLeftBack;
                vector3fArray = vector3fArray3;
                vector3fArray3[3] = bottomRightBack;
                break;
            }
            case field_11034: {
                if (mirror) {
                    Vector3f[] vector3fArray4 = new Vector3f[4];
                    vector3fArray4[0] = topRightBack;
                    vector3fArray4[1] = topLeftBack;
                    vector3fArray4[2] = bottomLeftBack;
                    vector3fArray = vector3fArray4;
                    vector3fArray4[3] = bottomRightBack;
                    break;
                }
                Vector3f[] vector3fArray5 = new Vector3f[4];
                vector3fArray5[0] = topLeftFront;
                vector3fArray5[1] = topRightFront;
                vector3fArray5[2] = bottomRightFront;
                vector3fArray = vector3fArray5;
                vector3fArray5[3] = bottomLeftFront;
                break;
            }
            case field_11043: {
                Vector3f[] vector3fArray6 = new Vector3f[4];
                vector3fArray6[0] = topLeftBack;
                vector3fArray6[1] = topLeftFront;
                vector3fArray6[2] = bottomLeftFront;
                vector3fArray = vector3fArray6;
                vector3fArray6[3] = bottomLeftBack;
                break;
            }
            case field_11035: {
                Vector3f[] vector3fArray7 = new Vector3f[4];
                vector3fArray7[0] = topRightFront;
                vector3fArray7[1] = topRightBack;
                vector3fArray7[2] = bottomRightBack;
                vector3fArray = vector3fArray7;
                vector3fArray7[3] = bottomRightFront;
                break;
            }
            case field_11036: {
                if (mirror) {
                    Vector3f[] vector3fArray8 = new Vector3f[4];
                    vector3fArray8[0] = bottomLeftBack;
                    vector3fArray8[1] = bottomLeftFront;
                    vector3fArray8[2] = bottomRightFront;
                    vector3fArray = vector3fArray8;
                    vector3fArray8[3] = bottomRightBack;
                    break;
                }
                Vector3f[] vector3fArray9 = new Vector3f[4];
                vector3fArray9[0] = topRightBack;
                vector3fArray9[1] = topRightFront;
                vector3fArray9[2] = topLeftFront;
                vector3fArray = vector3fArray9;
                vector3fArray9[3] = topLeftBack;
                break;
            }
            case field_11033: {
                if (mirror) {
                    Vector3f[] vector3fArray10 = new Vector3f[4];
                    vector3fArray10[0] = topRightBack;
                    vector3fArray10[1] = topRightFront;
                    vector3fArray10[2] = topLeftFront;
                    vector3fArray = vector3fArray10;
                    vector3fArray10[3] = topLeftBack;
                    break;
                }
                Vector3f[] vector3fArray11 = new Vector3f[4];
                vector3fArray11[0] = bottomLeftBack;
                vector3fArray11[1] = bottomLeftFront;
                vector3fArray11[2] = bottomRightFront;
                vector3fArray = vector3fArray11;
                vector3fArray11[3] = bottomRightBack;
            }
        }
        return vector3fArray;
    }

    private static float[] rotateUvs(float u, float v, float uWidth, float vHeight, int rotSteps) {
        float[] fArray;
        switch (rotSteps & 3) {
            case 0: {
                float[] fArray2 = new float[8];
                fArray2[0] = u;
                fArray2[1] = v;
                fArray2[2] = uWidth;
                fArray2[3] = v;
                fArray2[4] = uWidth;
                fArray2[5] = vHeight;
                fArray2[6] = u;
                fArray = fArray2;
                fArray2[7] = vHeight;
                break;
            }
            case 1: {
                float[] fArray3 = new float[8];
                fArray3[0] = uWidth;
                fArray3[1] = v;
                fArray3[2] = uWidth;
                fArray3[3] = vHeight;
                fArray3[4] = u;
                fArray3[5] = vHeight;
                fArray3[6] = u;
                fArray = fArray3;
                fArray3[7] = v;
                break;
            }
            case 2: {
                float[] fArray4 = new float[8];
                fArray4[0] = uWidth;
                fArray4[1] = vHeight;
                fArray4[2] = u;
                fArray4[3] = vHeight;
                fArray4[4] = u;
                fArray4[5] = v;
                fArray4[6] = uWidth;
                fArray = fArray4;
                fArray4[7] = v;
                break;
            }
            default: {
                float[] fArray5 = new float[8];
                fArray5[0] = u;
                fArray5[1] = vHeight;
                fArray5[2] = u;
                fArray5[3] = v;
                fArray5[4] = uWidth;
                fArray5[5] = v;
                fArray5[6] = uWidth;
                fArray = fArray5;
                fArray5[7] = vHeight;
            }
        }
        return fArray;
    }

    private static void addVertex(class_4587.class_4665 pose, class_4588 consumer, float x, float y, float z, float u, float v, int packedLight, float nx, float ny, float nz, float colorScale, float alphaScale) {
        int color = BedrockItemRenderer.scaleToColor(colorScale);
        int alpha = BedrockItemRenderer.scaleToColor(alphaScale);
        consumer.method_22918(pose.method_23761(), x, y, z).method_1336(color, color, color, alpha).method_22913(u, v).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, nx, ny, nz);
    }

    private static boolean isPendulumSuppressed(String boneName, Map<String, String> parentByBone, Set<String> suppressed) {
        if (boneName == null || suppressed == null || suppressed.isEmpty()) {
            return false;
        }
        String current = boneName;
        for (int guard = 0; current != null && guard < 64; ++guard) {
            if (suppressed.contains(current)) {
                return true;
            }
            current = parentByBone != null ? parentByBone.get(current) : null;
        }
        return false;
    }

    private static boolean isCameraTransparentBone(String boneName, Map<String, String> parentByBone, Set<String> cameraTransparentBones) {
        if (boneName == null || cameraTransparentBones == null || cameraTransparentBones.isEmpty()) {
            return false;
        }
        String current = boneName;
        for (int guard = 0; current != null && guard < 64; ++guard) {
            if (cameraTransparentBones.contains(current)) {
                return true;
            }
            current = parentByBone != null ? parentByBone.get(current) : null;
        }
        return false;
    }

    private static int getBoneEmission(Map<String, Integer> emissionByBone, String boneName) {
        if (emissionByBone == null || emissionByBone.isEmpty() || boneName == null) {
            return 0;
        }
        Integer value = emissionByBone.get(boneName);
        return value == null ? 0 : value;
    }

    private static float emissionToBaseIntensity(int emission) {
        if (emission <= 0) {
            return 0.0f;
        }
        if (emission <= 15) {
            return (float)emission / 15.0f;
        }
        return 1.0f;
    }

    private static float emissionToExtraGlow(int emission) {
        if (emission <= 15) {
            return 0.0f;
        }
        return Math.min(1.0f, (float)(emission - 15) / 85.0f);
    }

    private static int scaleToColor(float scale) {
        if (scale <= 0.0f) {
            return 0;
        }
        if (scale >= 1.0f) {
            return 255;
        }
        return Math.max(0, Math.min(255, Math.round(255.0f * scale)));
    }

    private static class_1921 emissiveOverlay(class_2960 texture, boolean blur, boolean clamp, boolean shaderPackInUse) {
        if (shaderPackInUse) {
            return class_1921.method_42600((class_2960)texture);
        }
        EmissiveKey key = new EmissiveKey(texture, blur, clamp);
        class_1921 cached = EMISSIVE_OVERLAY_TYPES.get(key);
        if (cached != null) {
            return cached;
        }
        class_1921 created = class_1921.method_23026((class_2960)texture);
        EMISSIVE_OVERLAY_TYPES.put(key, created);
        return created;
    }

    private static boolean isShaderPackInUse() {
        return IrisShaderPackCompat.isShaderPackInUse();
    }

    private static TextureAnim resolveTextureAnim(int frames, int frameTime, int frameHeightOverride, int texturePixelWidth, int texturePixelHeight, int texW, int texH, float animTime) {
        if (frames <= 1 || frameTime <= 0 || texturePixelWidth <= 0 || texturePixelHeight <= 0 || texW <= 0 || texH <= 0) {
            return null;
        }
        int frameHeight = frameHeightOverride > 0 ? frameHeightOverride : texH;
        int expectedHeight = frameHeight * frames;
        if (texturePixelHeight != expectedHeight) {
            return null;
        }
        int frameIndex = (int)(animTime / (float)frameTime) % frames;
        float invFrames = 1.0f / (float)frames;
        return new TextureAnim(frameIndex, invFrames);
    }

    private static TextureAnim resolveAnimForBone(BedrockItemDefinition.TextureAnimSpec spec, int defaultFrames, int defaultFrameTime, int defaultFrameHeight, BedrockItemDefinition def, class_2960 textureId, int texW, int texH, float animTime) {
        int frames = defaultFrames;
        int frameTime = defaultFrameTime;
        int frameHeight = defaultFrameHeight;
        if (spec != null) {
            frames = spec.frames;
            frameTime = spec.frameTime;
            frameHeight = spec.frameHeight;
        }
        int[] size = def != null ? def.getTexturePixelSize(textureId) : null;
        int pixelW = size != null ? size[0] : 0;
        int pixelH = size != null ? size[1] : 0;
        return BedrockItemRenderer.resolveTextureAnim(frames, frameTime, frameHeight, pixelW, pixelH, texW, texH, animTime);
    }

    private static List<String> mergeParticleEffects(List<String> primary, List<String> secondary) {
        if ((primary == null || primary.isEmpty()) && (secondary == null || secondary.isEmpty())) {
            return List.of();
        }
        ArrayList<String> out = new ArrayList<String>();
        if (primary != null && !primary.isEmpty()) {
            out.addAll(primary);
        }
        if (secondary != null && !secondary.isEmpty()) {
            out.addAll(secondary);
        }
        return out;
    }

    private record TextureAnim(int frameIndex, float invFrames) {
    }

    private static final class DynamicValueCache {
        private boolean axolResolved;
        private String axolVariant;

        private DynamicValueCache() {
        }
    }

    private record EmissiveKey(class_2960 texture, boolean blur, boolean clamp) {
    }
}

