/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1058
 *  net.minecraft.class_1059
 *  net.minecraft.class_1068
 *  net.minecraft.class_1087
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1806
 *  net.minecraft.class_1835
 *  net.minecraft.class_1921
 *  net.minecraft.class_1935
 *  net.minecraft.class_1937
 *  net.minecraft.class_2350
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_5321
 *  net.minecraft.class_5538
 *  net.minecraft.class_5602
 *  net.minecraft.class_583
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_6862
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  net.minecraft.class_759
 *  net.minecraft.class_7833
 *  net.minecraft.class_7924
 *  net.minecraft.class_804
 *  net.minecraft.class_809
 *  net.minecraft.class_811
 *  net.minecraft.class_8685
 *  net.minecraft.class_8685$class_7920
 *  net.minecraft.class_897
 *  net.minecraft.class_922
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.render;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1011;
import net.minecraft.class_1058;
import net.minecraft.class_1059;
import net.minecraft.class_1068;
import net.minecraft.class_1087;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1806;
import net.minecraft.class_1835;
import net.minecraft.class_1921;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5321;
import net.minecraft.class_5538;
import net.minecraft.class_5602;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_6862;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_7924;
import net.minecraft.class_804;
import net.minecraft.class_809;
import net.minecraft.class_811;
import net.minecraft.class_8685;
import net.minecraft.class_897;
import net.minecraft.class_922;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.PunchyClient;
import punchy.client.access.TransformablePart;
import punchy.client.animation.Easing;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.bedrockitems.BedrockItemAnimationManager;
import punchy.client.bedrockitems.BedrockItemSystem;
import punchy.client.debug.PunchyDebug;
import punchy.client.model.SimpleBedrockGeo;
import punchy.client.modelparts.ModelPartsParticleSystem;
import punchy.client.modelparts.ModelPartsSystem;
import punchy.client.render.BoatInHandRenderer;
import punchy.client.render.CameraAnimationState;
import punchy.client.render.ChestInHandRenderer;
import punchy.client.render.InHandBlockEntityRenderer;
import punchy.client.render.MacePhysics;
import punchy.client.render.MinecartInHandRenderer;
import punchy.client.render.PendulumPhysics;
import punchy.client.render.ResourcePackItemTransformResolver;
import punchy.client.render.ShieldImpactPhysics;
import punchy.client.render.SlimeInHandPhysics;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.ArmMeshTuning;
import punchy.client.renderer.layer.ArmToolTuning;
import punchy.client.renderer.layer.PlayerArmModelLayer;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.ClimbStateMachine;
import punchy.client.state.CrawlStateMachine;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.MovementStateMachine;
import punchy.client.state.MudStateMachine;
import punchy.client.state.SweatStateMachine;
import punchy.compat.AdorableHamsterPetsCompat;
import punchy.compat.BetterCombatCompat;
import punchy.compat.EmfFirstPersonHandCompat;
import punchy.compat.FirstPersonModelCompat;
import punchy.compat.IrisShaderPackCompat;
import punchy.compat.SkinLayersCompat;
import punchy.config.PunchyConfig;
import punchy.mixin.client.accessor.ItemInHandRendererInvoker;
import punchy.mixin.client.accessor.PlayerModelAccessor;

public class PunchyArmRenderer {
    private static class_591 fpModelDefault;
    private static class_591 fpModelSlim;
    private static final class_1799 DEBUG_PIVOT_STACK;
    private static final class_1799 DEBUG_GRIP_STACK;
    private static final Vector3f DEBUG_PIVOT_NUDGE;
    private static final float PIVOT_BASE_OFFSET_Y = -0.14f;
    private static final float PIVOT_BASE_OFFSET_Z = 0.11f;
    private static final float PIVOT_ITEM_COMP_Y = 0.14f;
    private static final float ITEM_BASE_OFFSET_Y = -0.17f;
    private static final float ITEM_BASE_OFFSET_Z = -0.02f;
    private static final float PIVOT_BASE_OFFSET_X_RIGHT = -0.04f;
    private static final float PIVOT_BASE_OFFSET_X_LEFT = 0.04f;
    private static final float ITEM_BASE_OFFSET_X_RIGHT = -0.04f;
    private static final float ITEM_BASE_OFFSET_X_LEFT = 0.04f;
    private static final class_2960 FREEZE_ARM_STAGE_1;
    private static final class_2960 FREEZE_ARM_STAGE_2;
    private static final class_2960 FREEZE_ARM_STAGE_3;
    private static final class_2960 MUD_ARM_STAGE_1;
    private static final class_2960 MUD_ARM_STAGE_2;
    private static final class_2960 MUD_ARM_STAGE_3;
    private static final class_2960 SWEAT_ARM_STAGE_1;
    private static final class_2960 SWEAT_ARM_STAGE_2;
    private static final class_2960 SWEAT_ARM_STAGE_3;
    private static final class_2960 SWEAT_ARM_STAGE_4;
    private static final class_2960 BURN_ARM_HAND_OVERLAY;
    private static final class_2960 FIRE_ATLAS_ID;
    private static final class_2960 FIRE_ATLAS_TEXTURE;
    private static final class_2960 FIRE_SPRITE_ID;
    private static final class_2960 BURN_ARM_HAND_OVERLAY_LEGACY;
    private static final int FREEZE_TIP_RIGHT_U0 = 40;
    private static final int FREEZE_TIP_RIGHT_V0 = 28;
    private static final int FREEZE_TIP_LEFT_U0 = 32;
    private static final int FREEZE_TIP_LEFT_V0 = 60;
    private static final int FREEZE_TIP_W = 16;
    private static final int FREEZE_TIP_H = 4;
    private static final int SWEAT_TIP_RIGHT_U0 = 40;
    private static final int SWEAT_TIP_LEFT_U0 = 40;
    private static final int SWEAT_TIP_V0 = 28;
    private static final int SWEAT_TIP_W = 16;
    private static final int SWEAT_TIP_H = 16;
    private static final float FREEZE_VOXEL_THICKNESS = 0.45f;
    private static final float SWEAT_VOXEL_THICKNESS = 0.09f;
    private static final float SWEAT_PIXEL_SCALE_X = 0.65f;
    private static final float SWEAT_PIXEL_SCALE_Y = 0.15f;
    private static final float BURN_FADE_IN_TICKS = 30.0f;
    private static final float BURN_FADE_OUT_TICKS = 60.0f;
    private static final Map<String, FreezeVoxelMask> FREEZE_MASK_CACHE;
    private static final float FREEZE_SHAKE_TRANSLATION = 7.65E-4f;
    private static final float FREEZE_SHAKE_ROTATION_DEG = 0.1275f;
    private static final Map<class_2960, Integer> SKIN_HEIGHT_CACHE;
    private static final Map<UUID, SlimResolutionState> SLIM_RESOLUTION_CACHE;
    private static final long SLIM_RESOLUTION_TTL_MS = 15000L;
    private static final int SLIM_RESOLUTION_MAX_ENTRIES = 256;
    private static final float OFFHAND_LOWER_Y = 0.0f;
    private static final float OFFHAND_LOWER_Z = 0.0f;
    private static final float OFFHAND_LOWER_X = 0.0f;
    private static final float OFFHAND_LOWER_ROT_X = 90.0f;
    private static final int OFFHAND_HIDE_AFTER_LOWER_FRAMES = 6;
    private static int offhandLowerExitDelayFrames;
    private static int offhandHideAfterLowerFrames;
    private static boolean lastOffhandEmptyForLower;
    private static int offhandAwaitHandInFrames;
    private static boolean lastOffhandHasItemForLower;
    private static int offhandAwaitHandOutFrames;
    private static boolean lastDualHandedForHide;
    private static boolean hideOffhandAfterDualExit;
    private static int hideOffhandAfterDualFrames;
    private static boolean keepEmptyOffhandVisibleForFlight;
    private static boolean flightOffhandLandingHideRequested;
    private static final float ELYTRA_OFFHAND_SHOW_SPEED_MIN = 0.55f;
    private static final EnumMap<class_1306, Matrix4f> ARM_WORLD_CACHE;
    private static final EnumMap<class_1306, Float> DRY_OUT_ARM_TUNING_BLEND;
    private static final EnumMap<class_1306, Float> ARM_ROTATION_RESET_PROGRESS;
    private static final EnumMap<class_1306, BurnFadeState> BURN_FADE_STATE;
    private static long lastArmCacheTick;
    private static final ThreadLocal<Boolean> FORCE_VANILLA_DISPLAY;
    private static final ThreadLocal<Boolean> NEUTRALIZE_USE_TRANSFORMS;
    private static final ThreadLocal<Integer> ITEM_RENDER_CAPTURE_DEPTH;
    private static volatile boolean FIRSTPERSON_HIDE;
    private static final float CRAWL_PITCH_START = 0.0f;
    private static final float CRAWL_PITCH_END = 0.0f;
    private static final float CLIMB_PITCH_START = 0.0f;
    private static final float CLIMB_PITCH_END = 0.0f;
    private static final float ARM_ROTATION_RESET_ENTER_STEP_MIN = 0.1f;
    private static final float ARM_ROTATION_RESET_ENTER_STEP_MAX = 0.16f;
    private static final float ARM_ROTATION_RESET_EXIT_STEP_MIN = 0.08f;
    private static final float ARM_ROTATION_RESET_EXIT_STEP_MAX = 0.13f;
    private static final float ARM_ROTATION_RESET_BACK_MIX = 0.45f;
    private static final class_6862<class_1792> SPEAR_TAG;
    private static final Map<class_1792, EnumMap<class_811, class_804>> VANILLA_THIRD_PERSON_CACHE;
    private static final Map<class_1792, EnumMap<class_811, class_804>> IDLE_USE_TRANSFORM_CACHE;
    private static final ThreadLocal<class_804> BASELINE_USE_TRANSFORM;
    private static final ThreadLocal<class_804> LAST_RENDERED_ITEM_TRANSFORM;
    private static final ThreadLocal<Matrix4f> LAST_RENDERED_ITEM_POSE;
    private static final String HIDE_ABOVE_INTERFACE_EFFECT = "hide_above_interface";
    private static final Set<String> EXPOSURE_INTERFACE_SCREEN_NAMES;
    private static final String EXPOSURE_CAMERA_CLIENT_CLASS = "io.github.mortuusars.exposure.client.camera.CameraClient";
    private static boolean exposureCameraClientLookupDone;
    private static Method exposureCameraClientIsActiveMethod;
    private static Method exposureCameraClientViewfinderMethod;
    private static Boolean shaderPackInUseCached;
    private static long shaderPackInUseLastCheckNanos;

    public static boolean shouldForceVanillaDisplay() {
        return FORCE_VANILLA_DISPLAY.get();
    }

    private static void setForceVanillaDisplay(boolean value) {
        FORCE_VANILLA_DISPLAY.set(value);
    }

    public static boolean isCapturingItemRenderer() {
        return ITEM_RENDER_CAPTURE_DEPTH.get() > 0;
    }

    public static void beginItemRendererCapture() {
        ITEM_RENDER_CAPTURE_DEPTH.set(ITEM_RENDER_CAPTURE_DEPTH.get() + 1);
    }

    public static void endItemRendererCapture() {
        int depth = ITEM_RENDER_CAPTURE_DEPTH.get();
        if (depth <= 1) {
            ITEM_RENDER_CAPTURE_DEPTH.remove();
        } else {
            ITEM_RENDER_CAPTURE_DEPTH.set(depth - 1);
        }
    }

    public static boolean shouldNeutralizeUseTransforms() {
        return NEUTRALIZE_USE_TRANSFORMS.get();
    }

    private static void setNeutralizeUseTransforms(boolean value) {
        NEUTRALIZE_USE_TRANSFORMS.set(value);
    }

    private static float updateWaterArmTuningBlend(class_1306 arm, float partialTicks) {
        float blend = DRY_OUT_ARM_TUNING_BLEND.getOrDefault(arm, Float.valueOf(1.0f)).floatValue();
        float dt = class_3532.method_15363((float)partialTicks, (float)0.0f, (float)1.0f);
        float target = 1.0f;
        float lerpAlpha = 0.35f + 0.25f * dt;
        boolean waterAnimationActive = false;
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            AnimationClip swimClip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
            boolean bl = waterAnimationActive = swimClip == PunchyAnimationManager.SWIMING_CLIP;
            if (waterAnimationActive && arm != null && swimClip != null) {
                boolean suppressWaterTuningFade;
                boolean bl2 = suppressWaterTuningFade = ItemAnimationManager.isSwimSuppressed(arm) || BedrockItemAnimationManager.isSwimSuppressed(arm);
                if (suppressWaterTuningFade) {
                    waterAnimationActive = false;
                }
            }
        }
        if (waterAnimationActive) {
            target = 0.0f;
            lerpAlpha = 0.5f + 0.3f * dt;
        }
        if (Math.abs((blend = class_3532.method_16439((float)class_3532.method_15363((float)lerpAlpha, (float)0.0f, (float)1.0f), (float)blend, (float)target)) - target) < 0.001f) {
            blend = target;
        }
        DRY_OUT_ARM_TUNING_BLEND.put(arm, Float.valueOf(blend));
        return blend;
    }

    private static float updateArmRotationResetBlend(class_1306 arm, float partialTicks) {
        float progress = ARM_ROTATION_RESET_PROGRESS.getOrDefault(arm, Float.valueOf(0.0f)).floatValue();
        float clampedPartial = class_3532.method_15363((float)partialTicks, (float)0.0f, (float)1.0f);
        if (PunchyArmRenderer.shouldZeroArmRotationForActiveAnimation()) {
            frameStep = class_3532.method_16439((float)clampedPartial, (float)0.1f, (float)0.16f);
            progress = class_3532.method_15363((float)(progress + frameStep), (float)0.0f, (float)1.0f);
        } else {
            frameStep = class_3532.method_16439((float)clampedPartial, (float)0.08f, (float)0.13f);
            progress = class_3532.method_15363((float)(progress - frameStep), (float)0.0f, (float)1.0f);
        }
        ARM_ROTATION_RESET_PROGRESS.put(arm, Float.valueOf(progress));
        float smooth = progress * progress * (3.0f - 2.0f * progress);
        float back = class_3532.method_15363((float)Easing.easeOutBack(progress), (float)0.0f, (float)1.0f);
        float eased = class_3532.method_16439((float)0.45f, (float)smooth, (float)back);
        return 1.0f - eased;
    }

    private static boolean shouldZeroArmRotationForActiveAnimation() {
        return PunchyArmRenderer.shouldZeroArmRotationForClip(PunchyAnimationManager.POSE_HANDLER.getCurrentClip(), PunchyAnimationManager.POSE_HANDLER.isPlaying()) || PunchyArmRenderer.shouldZeroArmRotationForClip(PunchyAnimationManager.SWIM_HANDLER.getCurrentClip(), PunchyAnimationManager.SWIM_HANDLER.isPlaying());
    }

    private static float resolveGlobalHandFactorForFlightPose() {
        MovementStateMachine movement = MovementStateMachine.get();
        if (movement == null || !movement.isFlightPoseActive()) {
            return 1.0f;
        }
        float easedProgress = class_3532.method_15363((float)(movement.getFlightPoseBlend() * 1.12f), (float)0.0f, (float)1.0f);
        float eased = class_3532.method_15363((float)Easing.easeOutBack(easedProgress), (float)0.0f, (float)1.0f);
        return 1.0f - eased;
    }

    private static float resolveFlightPoseTuningFactor(float partialTicks, class_1306 arm) {
        float tuningFactor = PunchyAnimationManager.POSE_HANDLER.getArmTuningFactor(partialTicks, arm);
        float globalHandFactor = PunchyArmRenderer.resolveGlobalHandFactorForFlightPose();
        if (globalHandFactor < 0.999f) {
            tuningFactor *= globalHandFactor;
        }
        return tuningFactor;
    }

    private static boolean isActualFlightActive(class_746 player) {
        if (player == null) {
            return false;
        }
        if (!PunchyConfig.enableElytraFlightAnimation()) {
            return false;
        }
        if (player.method_24828() || player.method_5681() || player.method_5799() || !player.method_6128()) {
            return false;
        }
        class_243 velocity = player.method_18798();
        float speed = velocity == null ? 0.0f : (float)velocity.method_1033();
        return speed >= 0.55f;
    }

    private static boolean hasEquippedElytra(class_746 player) {
        if (player == null) {
            return false;
        }
        class_1799 chest = player.method_6118(class_1304.field_6174);
        return chest != null && chest.method_31574(class_1802.field_8833);
    }

    private static void resetFlightOffhandState() {
        keepEmptyOffhandVisibleForFlight = false;
        flightOffhandLandingHideRequested = false;
    }

    public static void forceResetOffhandVisualState() {
        offhandLowerExitDelayFrames = 0;
        offhandHideAfterLowerFrames = 0;
        offhandAwaitHandInFrames = 0;
        offhandAwaitHandOutFrames = 0;
        hideOffhandAfterDualExit = false;
        hideOffhandAfterDualFrames = 0;
        lastDualHandedForHide = false;
        lastOffhandEmptyForLower = true;
        lastOffhandHasItemForLower = false;
        PunchyArmRenderer.resetFlightOffhandState();
    }

    private static boolean shouldZeroArmRotationForClip(AnimationClip clip, boolean playing) {
        if (!playing || clip == null) {
            return false;
        }
        return clip == PunchyAnimationManager.FALLING_IN_WATER_CLIP || clip == PunchyAnimationManager.PUT_OUT_FIRE_CLIP || PunchyAnimationManager.isClipNamed(clip, "crawl") || PunchyAnimationManager.isClipNamed(clip, "climb");
    }

    public static class_804 getCachedVanillaThirdPersonTransform(class_1799 stack, class_811 context) {
        if (stack == null || stack.method_7960() || context == null) {
            return null;
        }
        class_1792 item = stack.method_7909();
        if (item == null) {
            return null;
        }
        EnumMap<class_811, class_804> perContext = VANILLA_THIRD_PERSON_CACHE.get(item);
        if (perContext == null) {
            return null;
        }
        class_804 cached = perContext.get(context);
        return cached == null ? null : PunchyArmRenderer.cloneTransform(cached);
    }

    public static void cacheVanillaThirdPersonTransform(class_1799 stack, class_811 context, class_804 transform) {
        if (stack == null || stack.method_7960() || context == null || transform == null) {
            return;
        }
        class_1792 item = stack.method_7909();
        if (item == null) {
            return;
        }
        EnumMap perContext = VANILLA_THIRD_PERSON_CACHE.computeIfAbsent(item, key -> new EnumMap(class_811.class));
        perContext.put(context, PunchyArmRenderer.cloneTransform(transform));
    }

    private static class_804 cloneTransform(class_804 transform) {
        return transform;
    }

    public static class_804 getBaselineUseTransform() {
        return BASELINE_USE_TRANSFORM.get();
    }

    public static void setBaselineUseTransform(class_804 transform) {
        BASELINE_USE_TRANSFORM.set(transform);
    }

    public static void setLastRenderedItemTransform(class_804 transform) {
        if (transform == null) {
            LAST_RENDERED_ITEM_TRANSFORM.remove();
            return;
        }
        LAST_RENDERED_ITEM_TRANSFORM.set(PunchyArmRenderer.cloneTransform(transform));
    }

    public static class_804 consumeLastRenderedItemTransform() {
        class_804 transform = LAST_RENDERED_ITEM_TRANSFORM.get();
        LAST_RENDERED_ITEM_TRANSFORM.remove();
        return transform == null ? null : PunchyArmRenderer.cloneTransform(transform);
    }

    public static void setLastRenderedItemPose(Matrix4f pose) {
        if (pose == null) {
            LAST_RENDERED_ITEM_POSE.remove();
            return;
        }
        LAST_RENDERED_ITEM_POSE.set(new Matrix4f((Matrix4fc)pose));
    }

    public static Matrix4f consumeLastRenderedItemPose() {
        Matrix4f pose = LAST_RENDERED_ITEM_POSE.get();
        LAST_RENDERED_ITEM_POSE.remove();
        return pose == null ? null : new Matrix4f((Matrix4fc)pose);
    }

    public static void cacheIdleUseTransform(class_1799 stack, class_811 context, class_804 transform) {
        if (stack == null || stack.method_7960() || context == null || transform == null) {
            return;
        }
        class_1792 item = stack.method_7909();
        if (item == null) {
            return;
        }
        EnumMap perContext = IDLE_USE_TRANSFORM_CACHE.computeIfAbsent(item, key -> new EnumMap(class_811.class));
        perContext.put(context, PunchyArmRenderer.cloneTransform(transform));
    }

    public static class_804 getCachedIdleTransform(class_1799 stack, class_811 context) {
        if (stack == null || stack.method_7960() || context == null) {
            return null;
        }
        class_1792 item = stack.method_7909();
        if (item == null) {
            return null;
        }
        EnumMap<class_811, class_804> perContext = IDLE_USE_TRANSFORM_CACHE.get(item);
        if (perContext == null) {
            return null;
        }
        class_804 cached = perContext.get(context);
        return cached == null ? null : PunchyArmRenderer.cloneTransform(cached);
    }

    public static class_804 resolveBaselineUseTransform(class_1799 stack, class_811 context, class_1309 entity) {
        if (stack == null || stack.method_7960() || context == null) {
            return null;
        }
        class_804 resourcePackTransform = ResourcePackItemTransformResolver.resolve(stack, context);
        if (resourcePackTransform != null) {
            return PunchyArmRenderer.cloneTransform(resourcePackTransform);
        }
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return null;
        }
        class_1087 model = mc.method_1480().method_4019(stack, entity != null ? entity.method_37908() : null, entity, 0);
        if (model == null) {
            return null;
        }
        class_809 transforms = model.method_4709();
        if (transforms == null) {
            return null;
        }
        class_804 transform = transforms.method_3503(context);
        return PunchyArmRenderer.cloneTransform(transform);
    }

    public static Matrix4f getCachedArmWorld(class_746 player, class_1306 arm) {
        if (player == null || arm == null || player.method_37908() == null) {
            return null;
        }
        if (lastArmCacheTick != player.method_37908().method_8510()) {
            return null;
        }
        Matrix4f cached = ARM_WORLD_CACHE.get(arm);
        return cached == null ? null : new Matrix4f((Matrix4fc)cached);
    }

    public static void renderFirstPerson(class_759 handRenderer, class_746 player, float partialTicks, class_4587 poseStack, class_4597 buffer, int combinedLight) {
        try (EmfFirstPersonHandCompat.Scope ignored = EmfFirstPersonHandCompat.enter(player);){
            PunchyArmRenderer.renderFirstPersonInternal(handRenderer, player, partialTicks, poseStack, buffer, combinedLight);
        }
    }

    private static void renderFirstPersonInternal(class_759 handRenderer, class_746 player, float partialTicks, class_4587 poseStack, class_4597 buffer, int combinedLight) {
        boolean hidePunchyArmsForBetterCombat;
        class_1306 swimArm;
        boolean hideLoweredOffhandArm;
        boolean hideLoweredOffhandTransiently;
        boolean baseLowerOffhand;
        boolean hideOffhandOnDualExit;
        boolean keepEmptyOffhandVisibleForSprint;
        boolean renderSuppressedOffhand;
        boolean keepFlightPoseOffhandVisible;
        if (player == null || handRenderer == null) {
            return;
        }
        if (!PunchyConfig.isModEnabled()) {
            return;
        }
        MovementStateMachine.get().updateFrame(player, partialTicks);
        if (!PunchyConfig.disableNativeItemPhysics()) {
            PendulumPhysics.get().updateFrame(player, partialTicks);
            SlimeInHandPhysics.get().updateFrame(player);
        }
        class_310 mc = class_310.method_1551();
        if (!mc.field_1690.method_31044().method_31034()) {
            return;
        }
        if (PunchyArmRenderer.shouldHideAboveInterface(mc)) {
            return;
        }
        if (FIRSTPERSON_HIDE || FirstPersonModelCompat.shouldHideHands()) {
            return;
        }
        class_591 playerModel = PunchyArmRenderer.getFirstPersonModel(PunchyArmRenderer.isSlim((class_742)player));
        if (SimpleBedrockGeo.loadedParts.isEmpty() || !SimpleBedrockGeo.loadedParts.containsKey("camera_animation") && !SimpleBedrockGeo.loadedParts.containsKey("camera")) {
            class_2960 playerGeoId = class_2960.method_60655((String)"punchy", (String)"geo/player.geo.json");
            SimpleBedrockGeo.load(playerGeoId);
        }
        poseStack.method_22903();
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(180.0f));
        PunchyArmRenderer.applyCameraAnimationCompensation(poseStack);
        PunchyArmRenderer.resetPlayerModelPose(playerModel);
        playerModel.field_3391.field_3665 = false;
        playerModel.field_3398.field_3665 = false;
        playerModel.field_3394.field_3665 = false;
        playerModel.field_3397.field_3665 = false;
        playerModel.field_3392.field_3665 = false;
        playerModel.field_3483.field_3665 = false;
        playerModel.field_3482.field_3665 = false;
        playerModel.field_3479.field_3665 = false;
        boolean mainRight = player.method_6068() == class_1306.field_6183;
        class_1306 offArm = mainRight ? class_1306.field_6182 : class_1306.field_6183;
        boolean hasOffhandItem = !player.method_6079().method_7960();
        boolean offhandOverrideActive = PunchyAnimationManager.isEquipOverrideActive(offArm);
        boolean offhandOverrideRenderable = PunchyAnimationManager.hasRenderableEquipOverride(offArm);
        boolean offhandEquipTransitionActive = HandEquipStateMachine.isOffhandEquipTransitionActive();
        boolean effectiveOffhandVisualActive = hasOffhandItem || offhandOverrideActive || offhandEquipTransitionActive;
        boolean currentOffhandEmpty = !effectiveOffhandVisualActive;
        boolean flightPoseActive = MovementStateMachine.get().isFlightPoseActive();
        boolean actualFlightActive = PunchyArmRenderer.isActualFlightActive(player);
        boolean elytraEquipped = PunchyArmRenderer.hasEquippedElytra(player);
        if (!elytraEquipped && !actualFlightActive) {
            PunchyArmRenderer.resetFlightOffhandState();
        }
        if (!currentOffhandEmpty) {
            PunchyArmRenderer.resetFlightOffhandState();
        } else if (actualFlightActive) {
            keepEmptyOffhandVisibleForFlight = true;
            flightOffhandLandingHideRequested = false;
        } else if (keepEmptyOffhandVisibleForFlight && player.method_24828() && !flightOffhandLandingHideRequested) {
            HandEquipStateMachine.requestFlightOffhandLandingHide(player);
            keepEmptyOffhandVisibleForFlight = false;
            flightOffhandLandingHideRequested = true;
        } else if (!keepEmptyOffhandVisibleForFlight) {
            flightOffhandLandingHideRequested = false;
        }
        AnimationClip activeClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        boolean exactHandOutActive = PunchyAnimationManager.POSE_HANDLER.isPlaying() && activeClip != null && (PunchyAnimationManager.isClipNamed(activeClip, "hand_out") || activeClip.getName() != null && activeClip.getName().startsWith("hand_out_"));
        boolean handInStateActive = "hand_in".equals(HandEquipStateMachine.getActiveType());
        boolean handOutActive = HandEquipStateMachine.shouldTreatHandOutActive();
        boolean offhandRemovalHandOutActive = HandEquipStateMachine.shouldKeepOffhandVisibleUntilHandOutEnds();
        boolean hideIncomingOffhandBeforeHandIn = HandEquipStateMachine.shouldHideIncomingOffhandUntilHandInStarts(player);
        boolean dualEffectActive = PunchyAnimationManager.POSE_HANDLER.isCurrentDualHandedActive() || ItemAnimationManager.isAnyDualHandedActive() || BedrockItemAnimationManager.isAnyDualHandedActive();
        boolean dualHanded = dualEffectActive || PunchyAnimationManager.isSwimDualHandedActive();
        boolean hideOffhandIfEmpty = PunchyAnimationManager.isOffhandHiddenIfEmptyActive(partialTicks);
        boolean suppressOffhand = PunchyClient.shouldSuppressOffhandRender((class_1657)player);
        if (dualHanded) {
            suppressOffhand = false;
        }
        boolean keepEmptyOffhandVisibleAfterFlight = flightOffhandLandingHideRequested && (HandEquipStateMachine.shouldKeepOffhandVisibleUntilHandOutEnds() || HandEquipStateMachine.isOffhandEquipTransitionActive());
        boolean hideEmptyOffhandForFlightLanding = flightOffhandLandingHideRequested && !actualFlightActive && player.method_24828() && currentOffhandEmpty;
        boolean bl = keepFlightPoseOffhandVisible = (actualFlightActive || keepEmptyOffhandVisibleForFlight && !player.method_24828()) && !hideEmptyOffhandForFlightLanding;
        if (keepEmptyOffhandVisibleForFlight && !actualFlightActive && player.method_24828() && !keepEmptyOffhandVisibleAfterFlight) {
            PunchyArmRenderer.resetFlightOffhandState();
        }
        boolean renderOffhandArm = (effectiveOffhandVisualActive || keepFlightPoseOffhandVisible || keepEmptyOffhandVisibleAfterFlight) && !suppressOffhand;
        boolean renderOffhandItem = (hasOffhandItem || offhandOverrideRenderable) && !suppressOffhand;
        boolean bl2 = renderSuppressedOffhand = suppressOffhand && currentOffhandEmpty && !offhandOverrideRenderable;
        if (hideEmptyOffhandForFlightLanding) {
            renderOffhandArm = false;
            renderOffhandItem = false;
        }
        boolean offhandInUse = player.method_6115() && player.method_6058() == PunchyArmRenderer.resolveHand(player, offArm);
        boolean usingSpyglass = PunchyArmRenderer.isUsingSpyglass((class_742)player);
        boolean anyAnimationPlaying = PunchyAnimationManager.POSE_HANDLER.isPlaying() || PunchyAnimationManager.SWIM_HANDLER.isPlaying();
        boolean bl3 = keepEmptyOffhandVisibleForSprint = currentOffhandEmpty && MovementStateMachine.get().isSprintSwingActive() && !dualHanded && !offhandInUse && !handOutActive && !handInStateActive && !keepFlightPoseOffhandVisible && !keepEmptyOffhandVisibleAfterFlight && !hideEmptyOffhandForFlightLanding;
        if (lastDualHandedForHide && !dualHanded && currentOffhandEmpty) {
            hideOffhandAfterDualExit = true;
            hideOffhandAfterDualFrames = 3;
        }
        if (dualHanded) {
            hideOffhandAfterDualExit = false;
            hideOffhandAfterDualFrames = 0;
        } else if (hideOffhandAfterDualFrames > 0) {
            --hideOffhandAfterDualFrames;
        } else {
            hideOffhandAfterDualExit = false;
        }
        boolean bl4 = hideOffhandOnDualExit = !dualHanded && (hideOffhandAfterDualExit || hideOffhandAfterDualFrames > 0);
        if (lastOffhandEmptyForLower && !currentOffhandEmpty) {
            offhandAwaitHandInFrames = 5;
        }
        lastOffhandEmptyForLower = currentOffhandEmpty;
        if (lastOffhandHasItemForLower && currentOffhandEmpty) {
            offhandAwaitHandOutFrames = 5;
        }
        boolean bl5 = lastOffhandHasItemForLower = !currentOffhandEmpty;
        if (anyAnimationPlaying) {
            offhandAwaitHandInFrames = 0;
            offhandAwaitHandOutFrames = 0;
        } else if (offhandAwaitHandInFrames > 0) {
            --offhandAwaitHandInFrames;
        } else if (offhandAwaitHandOutFrames > 0) {
            --offhandAwaitHandOutFrames;
        }
        boolean bl6 = baseLowerOffhand = currentOffhandEmpty && !dualHanded && !offhandInUse && !anyAnimationPlaying;
        if (!currentOffhandEmpty) {
            offhandLowerExitDelayFrames = 0;
        } else if (baseLowerOffhand) {
            offhandLowerExitDelayFrames = 3;
        } else if (!anyAnimationPlaying && offhandLowerExitDelayFrames > 0) {
            --offhandLowerExitDelayFrames;
        }
        boolean awaitingHandIn = !currentOffhandEmpty && offhandAwaitHandInFrames > 0 && !anyAnimationPlaying;
        boolean awaitingHandOut = currentOffhandEmpty && offhandAwaitHandOutFrames > 0 && !anyAnimationPlaying;
        boolean keepEmptyOffhandVisibleForHandOut = currentOffhandEmpty && !dualHanded && (exactHandOutActive || offhandRemovalHandOutActive);
        boolean effectiveRenderSuppressedOffhand = renderSuppressedOffhand && !keepEmptyOffhandVisibleForHandOut;
        boolean lowerOffhand = !hideEmptyOffhandForFlightLanding && !keepFlightPoseOffhandVisible && !keepEmptyOffhandVisibleAfterFlight && !keepEmptyOffhandVisibleForSprint && !handOutActive && !handInStateActive && !dualHanded && !anyAnimationPlaying && (baseLowerOffhand || offhandLowerExitDelayFrames > 0 || awaitingHandIn || awaitingHandOut);
        boolean loweredOffhandActive = currentOffhandEmpty && (effectiveRenderSuppressedOffhand || lowerOffhand);
        boolean bl7 = hideLoweredOffhandTransiently = currentOffhandEmpty && !keepEmptyOffhandVisibleForHandOut && (effectiveRenderSuppressedOffhand || awaitingHandIn || awaitingHandOut || hideOffhandOnDualExit);
        if (!currentOffhandEmpty) {
            offhandHideAfterLowerFrames = 0;
        } else if (hideLoweredOffhandTransiently) {
            offhandHideAfterLowerFrames = 6;
        } else if (loweredOffhandActive) {
            offhandHideAfterLowerFrames = Math.min(6, offhandHideAfterLowerFrames + 1);
        }
        boolean bl8 = hideLoweredOffhandArm = loweredOffhandActive && (hideLoweredOffhandTransiently || offhandHideAfterLowerFrames >= 6);
        if (effectiveRenderSuppressedOffhand || lowerOffhand) {
            renderOffhandArm = keepEmptyOffhandVisibleForHandOut || !hideLoweredOffhandArm;
            renderOffhandItem = false;
        }
        if (keepEmptyOffhandVisibleForSprint) {
            renderOffhandArm = true;
            renderOffhandItem = false;
        }
        if (hideEmptyOffhandForFlightLanding) {
            renderOffhandArm = false;
            renderOffhandItem = false;
        }
        boolean renderRightArm = mainRight || offArm == class_1306.field_6183 && renderOffhandArm;
        boolean renderLeftArm = !mainRight || offArm == class_1306.field_6182 && renderOffhandArm;
        boolean renderRightItem = mainRight || offArm == class_1306.field_6183 && renderOffhandItem;
        boolean renderLeftItem = !mainRight || offArm == class_1306.field_6182 && renderOffhandItem;
        boolean vanillaMainWindow = HandEquipStateMachine.shouldAllowVanillaHandOut(class_1268.field_5808);
        boolean vanillaOffWindow = HandEquipStateMachine.shouldAllowVanillaHandOut(class_1268.field_5810);
        if (vanillaMainWindow) {
            if (mainRight) {
                renderRightArm = false;
                renderRightItem = false;
            } else {
                renderLeftArm = false;
                renderLeftItem = false;
            }
        }
        if (vanillaOffWindow) {
            if (offArm == class_1306.field_6183) {
                renderRightArm = false;
                renderRightItem = false;
            } else {
                renderLeftArm = false;
                renderLeftItem = false;
            }
        }
        boolean disableRightHand = PunchyArmRenderer.isBlacklistedForArm(player, class_1306.field_6183);
        boolean disableLeftHand = PunchyArmRenderer.isBlacklistedForArm(player, class_1306.field_6182);
        if (disableRightHand) {
            renderRightArm = false;
            renderRightItem = false;
        }
        if (disableLeftHand) {
            renderLeftArm = false;
            renderLeftItem = false;
        }
        if (hideOffhandOnDualExit) {
            if (offArm == class_1306.field_6183) {
                renderRightArm = mainRight;
                renderRightItem = mainRight;
            } else {
                renderLeftArm = !mainRight;
                renderLeftItem = !mainRight;
            }
        }
        boolean keepOffhandVisibleForSwim = false;
        if (!(!currentOffhandEmpty || hideEmptyOffhandForFlightLanding || keepFlightPoseOffhandVisible || keepEmptyOffhandVisibleAfterFlight || dualHanded || keepEmptyOffhandVisibleForHandOut)) {
            if (offArm == class_1306.field_6183) {
                renderRightArm = mainRight;
                renderRightItem = mainRight;
            } else {
                renderLeftArm = !mainRight;
                boolean bl9 = renderLeftItem = !mainRight;
            }
        }
        if (!hideEmptyOffhandForFlightLanding && !keepFlightPoseOffhandVisible && !dualHanded && hideIncomingOffhandBeforeHandIn) {
            if (offArm == class_1306.field_6183) {
                renderRightArm = mainRight;
                renderRightItem = mainRight;
            } else {
                renderLeftArm = !mainRight;
                renderLeftItem = !mainRight;
            }
        }
        lastDualHandedForHide = dualHanded;
        if (usingSpyglass) {
            renderRightArm = false;
            renderLeftArm = false;
            renderRightItem = false;
            renderLeftItem = false;
        }
        if (!suppressOffhand && PunchyAnimationManager.SWIM_HANDLER.isPlaying() && (swimArm = PunchyAnimationManager.SWIM_HANDLER.getActiveArm()) == offArm) {
            keepOffhandVisibleForSwim = true;
            renderOffhandArm = true;
            renderOffhandItem = true;
            renderRightArm = mainRight || offArm == class_1306.field_6183 && renderOffhandArm;
            renderLeftArm = !mainRight || offArm == class_1306.field_6182 && renderOffhandArm;
            renderRightItem = mainRight || offArm == class_1306.field_6183 && renderOffhandItem;
            boolean bl10 = renderLeftItem = !mainRight || offArm == class_1306.field_6182 && renderOffhandItem;
        }
        if (dualHanded) {
            renderRightArm = true;
            renderLeftArm = true;
            if (!suppressOffhand) {
                renderRightItem = true;
                renderLeftItem = true;
            }
        }
        if (hideOffhandIfEmpty) {
            if (offArm == class_1306.field_6183) {
                renderRightArm = mainRight;
                renderRightItem = mainRight;
            } else {
                renderLeftArm = !mainRight;
                renderLeftItem = !mainRight;
            }
        }
        boolean keepRightArmForDualHanded = PunchyAnimationManager.shouldKeepBlacklistedOppositeHandForDualHanded(mc, PunchyArmRenderer.resolveHand(player, class_1306.field_6183));
        boolean keepLeftArmForDualHanded = PunchyAnimationManager.shouldKeepBlacklistedOppositeHandForDualHanded(mc, PunchyArmRenderer.resolveHand(player, class_1306.field_6182));
        boolean forceShowRightItem = PunchyAnimationManager.isItemForceShown(class_1306.field_6183);
        boolean forceShowLeftItem = PunchyAnimationManager.isItemForceShown(class_1306.field_6182);
        if (disableRightHand) {
            renderRightArm = keepRightArmForDualHanded;
            boolean bl11 = renderRightItem = keepRightArmForDualHanded && forceShowRightItem;
        }
        if (disableLeftHand) {
            renderLeftArm = keepLeftArmForDualHanded;
            boolean bl12 = renderLeftItem = keepLeftArmForDualHanded && forceShowLeftItem;
        }
        if (hideEmptyOffhandForFlightLanding) {
            if (offArm == class_1306.field_6183) {
                renderRightArm = mainRight;
                renderRightItem = mainRight;
            } else {
                renderLeftArm = !mainRight;
                boolean bl13 = renderLeftItem = !mainRight;
            }
        }
        if (currentOffhandEmpty && keepOffhandVisibleForSwim) {
            renderOffhandArm = true;
            renderOffhandItem = true;
            renderRightArm = mainRight || offArm == class_1306.field_6183 && renderOffhandArm;
            renderLeftArm = !mainRight || offArm == class_1306.field_6182 && renderOffhandArm;
            renderRightItem = mainRight || offArm == class_1306.field_6183 && renderOffhandItem;
            renderLeftItem = !mainRight || offArm == class_1306.field_6182 && renderOffhandItem;
        }
        boolean tridentInHands = player.method_6047().method_7909() instanceof class_1835 || player.method_6079().method_7909() instanceof class_1835;
        boolean betterCombatWeaponInHands = BetterCombatCompat.isWeaponStack(player.method_6047()) || BetterCombatCompat.isWeaponStack(player.method_6079());
        boolean bl14 = hidePunchyArmsForBetterCombat = BetterCombatCompat.isAvailable() && !PunchyConfig.isBetterCombatCompatEnabled() && (PunchyArmRenderer.isShaderPackInUse() || tridentInHands) && betterCombatWeaponInHands && BetterCombatCompat.isAttackVisualActive(player);
        if (hidePunchyArmsForBetterCombat) {
            renderRightArm = false;
            renderLeftArm = false;
            renderRightItem = false;
            renderLeftItem = false;
        }
        disableRightHand = PunchyArmRenderer.isBlacklistedForArm(player, class_1306.field_6183);
        disableLeftHand = PunchyArmRenderer.isBlacklistedForArm(player, class_1306.field_6182);
        if (disableRightHand) {
            renderRightArm = false;
            renderRightItem = false;
        }
        if (disableLeftHand) {
            renderLeftArm = false;
            renderLeftItem = false;
        }
        playerModel.field_3401.field_3665 = hidePunchyArmsForBetterCombat ? false : renderRightArm;
        playerModel.field_27433.field_3665 = hidePunchyArmsForBetterCombat ? false : renderLeftArm;
        playerModel.field_3486.field_3665 = false;
        playerModel.field_3484.field_3665 = false;
        class_630 cameraAnimPart = SimpleBedrockGeo.loadedParts.get("camera_animation");
        class_630 cameraPart = SimpleBedrockGeo.loadedParts.get("camera");
        if (cameraAnimPart == null) {
            cameraAnimPart = cameraPart;
        }
        class_630 bodyPart = SimpleBedrockGeo.loadedParts.get("body");
        class_630 geoRightArm = SimpleBedrockGeo.loadedParts.get("right_arm");
        class_630 geoLeftArm = SimpleBedrockGeo.loadedParts.get("left_arm");
        class_630 geoRightItem = SimpleBedrockGeo.loadedParts.get("item_right");
        class_630 geoLeftItem = SimpleBedrockGeo.loadedParts.get("item_left");
        class_630 geoRightGrip = PunchyArmRenderer.resolveGripPart("itemgrip_right", "item_grip_right");
        class_630 geoLeftGrip = PunchyArmRenderer.resolveGripPart("itemgrip_left", "item_grip_left");
        if (cameraAnimPart != null) {
            cameraAnimPart.method_41923();
        }
        if (cameraPart != null) {
            cameraPart.method_41923();
        }
        if (bodyPart != null) {
            bodyPart.method_41923();
        }
        if (geoRightArm != null) {
            geoRightArm.method_41923();
        }
        if (geoLeftArm != null) {
            geoLeftArm.method_41923();
        }
        if (geoRightItem != null) {
            geoRightItem.method_41923();
        }
        if (geoLeftItem != null) {
            geoLeftItem.method_41923();
        }
        if (geoRightGrip != null) {
            geoRightGrip.method_41923();
        }
        if (geoLeftGrip != null) {
            geoLeftGrip.method_41923();
        }
        HashMap<String, class_630> geoMap = new HashMap<String, class_630>();
        if (cameraAnimPart != null) {
            geoMap.put("camera_animation", cameraAnimPart);
        }
        if (cameraPart != null) {
            geoMap.put("camera", cameraPart);
        }
        if (bodyPart != null) {
            geoMap.put("body", bodyPart);
        }
        if (geoRightArm != null) {
            geoMap.put("right_arm", geoRightArm);
        }
        if (geoLeftArm != null) {
            geoMap.put("left_arm", geoLeftArm);
        }
        if (geoRightItem != null) {
            geoMap.put("item_right", geoRightItem);
        }
        if (geoLeftItem != null) {
            geoMap.put("item_left", geoLeftItem);
        }
        if (geoRightGrip != null) {
            geoMap.put("itemgrip_right", geoRightGrip);
        }
        if (geoLeftGrip != null) {
            geoMap.put("itemgrip_left", geoLeftGrip);
        }
        PunchyAnimationManager.POSE_HANDLER.applyTransformations(geoMap, partialTicks);
        if (PunchyArmRenderer.shouldApplySwimTransforms()) {
            PunchyAnimationManager.SWIM_HANDLER.applyTransformations(geoMap, partialTicks, false);
        }
        CameraAnimationState.update(cameraPart);
        Matrix4f cameraLocal = PunchyArmRenderer.getLocalMatrix(cameraPart);
        Matrix4f bodyLocal = PunchyArmRenderer.getLocalMatrix(bodyPart);
        Matrix4f rootWorld = new Matrix4f();
        if (cameraLocal != null) {
            rootWorld.set((Matrix4fc)cameraLocal);
        }
        if (bodyLocal != null) {
            rootWorld.mul((Matrix4fc)bodyLocal);
        }
        PunchyArmRenderer.applyCrawlCameraDecouple(rootWorld, player);
        PunchyArmRenderer.applyCameraLookOffset(rootWorld);
        PunchyArmRenderer.applyGeoArmToPlayer(playerModel.field_3401, geoRightArm, rootWorld, player, class_1306.field_6183, partialTicks);
        PunchyArmRenderer.applyGeoArmToPlayer(playerModel.field_27433, geoLeftArm, rootWorld, player, class_1306.field_6182, partialTicks);
        if ((effectiveRenderSuppressedOffhand || lowerOffhand) && !hideLoweredOffhandArm) {
            PunchyArmRenderer.applyLoweredOffhandPose(player, playerModel, offArm);
        }
        PunchyArmRenderer.clearExplicitTransform(playerModel.field_3486);
        PunchyArmRenderer.clearExplicitTransform(playerModel.field_3484);
        playerModel.field_3486.field_3665 = renderRightArm;
        playerModel.field_3484.field_3665 = renderLeftArm;
        if (PunchyDebug.DEBUG_POSITION_ENABLED) {
            poseStack.method_46416(PunchyDebug.POS_X * 0.0625f, PunchyDebug.POS_Y * 0.0625f, PunchyDebug.POS_Z * 0.0625f);
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(PunchyDebug.ROT_X));
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(PunchyDebug.ROT_Y));
            poseStack.method_22907(class_7833.field_40718.rotationDegrees(PunchyDebug.ROT_Z));
        }
        class_2960 texture = PunchyArmRenderer.getPlayerTexture((class_742)player);
        boolean slim = PunchyArmRenderer.isSlim((class_742)player);
        SkinLayersCompat.applyFirstPersonSleeves((class_742)player, playerModel, slim, renderLeftArm, renderRightArm);
        if (renderLeftArm) {
            PunchyArmRenderer.renderArm(playerModel, (class_742)player, class_1306.field_6182, poseStack, buffer, combinedLight, texture, slim, partialTicks);
        }
        if (renderRightArm) {
            PunchyArmRenderer.renderArm(playerModel, (class_742)player, class_1306.field_6183, poseStack, buffer, combinedLight, texture, slim, partialTicks);
        }
        if (renderLeftItem) {
            PunchyArmRenderer.renderItemInHand(handRenderer, playerModel, player, class_1306.field_6182, poseStack, buffer, combinedLight, rootWorld, geoLeftArm, geoLeftItem, geoLeftGrip, partialTicks);
        }
        if (renderRightItem) {
            PunchyArmRenderer.renderItemInHand(handRenderer, playerModel, player, class_1306.field_6183, poseStack, buffer, combinedLight, rootWorld, geoRightArm, geoRightItem, geoRightGrip, partialTicks);
        }
        poseStack.method_22909();
    }

    public static void render(class_742 player, float partialTicks, class_4587 poseStack, class_4597 buffer, int combinedLight, class_1306 arm) {
    }

    public static void renderPlayerArm(class_742 player, float partialTicks, class_4587 poseStack, class_4597 buffer, int combinedLight, class_1306 arm) {
        class_310 mc = class_310.method_1551();
        class_897 renderer = mc.method_1561().method_3953((class_1297)player);
        if (!(renderer instanceof class_922)) {
            return;
        }
        class_922 livingRenderer = (class_922)renderer;
        class_583 modelObj = livingRenderer.method_4038();
        if (!(modelObj instanceof class_591)) {
            return;
        }
        class_591 playerModel = (class_591)modelObj;
        poseStack.method_22903();
        if (PunchyDebug.DEBUG_POSITION_ENABLED) {
            poseStack.method_46416(PunchyDebug.POS_X * 0.0625f, PunchyDebug.POS_Y * 0.0625f, PunchyDebug.POS_Z * 0.0625f);
        }
        playerModel.field_3401.method_41923();
        playerModel.field_27433.method_41923();
        playerModel.field_3486.method_41923();
        playerModel.field_3484.method_41923();
        playerModel.field_3401.field_3665 = arm == class_1306.field_6183;
        playerModel.field_27433.field_3665 = arm == class_1306.field_6182;
        playerModel.field_3486.field_3665 = arm == class_1306.field_6183;
        playerModel.field_3484.field_3665 = arm == class_1306.field_6182;
        HashMap<String, class_630> partsMap = new HashMap<String, class_630>();
        partsMap.put("right_arm", playerModel.field_3401);
        partsMap.put("left_arm", playerModel.field_27433);
        PunchyAnimationManager.POSE_HANDLER.applyTransformations(partsMap, partialTicks);
        if (PunchyArmRenderer.shouldApplySwimTransforms()) {
            PunchyAnimationManager.SWIM_HANDLER.applyTransformations(partsMap, partialTicks, false);
        }
        PunchyArmRenderer.copyMatrixToSleeve(playerModel.field_3401, playerModel.field_3486);
        PunchyArmRenderer.copyMatrixToSleeve(playerModel.field_27433, playerModel.field_3484);
        boolean slim = PunchyArmRenderer.isSlim(player);
        SkinLayersCompat.applyFirstPersonSleeves(player, playerModel, slim, arm == class_1306.field_6182, arm == class_1306.field_6183);
        class_2960 texture = PunchyArmRenderer.getPlayerTexture(player);
        if (arm == class_1306.field_6183) {
            class_4588 consumer = buffer.getBuffer(class_1921.method_23580((class_2960)texture));
            playerModel.field_3401.method_22698(poseStack, consumer, combinedLight, class_4608.field_21444);
            playerModel.field_3486.method_22698(poseStack, consumer, combinedLight, class_4608.field_21444);
        } else {
            class_4588 consumer = buffer.getBuffer(class_1921.method_23580((class_2960)texture));
            playerModel.field_27433.method_22698(poseStack, consumer, combinedLight, class_4608.field_21444);
            playerModel.field_3484.method_22698(poseStack, consumer, combinedLight, class_4608.field_21444);
        }
        poseStack.method_22909();
    }

    private static void copyMatrixToSleeve(class_630 arm, class_630 sleeve) {
        if (arm == null || sleeve == null) {
            return;
        }
        sleeve.field_3657 = arm.field_3657;
        sleeve.field_3656 = arm.field_3656;
        sleeve.field_3655 = arm.field_3655;
        sleeve.field_3654 = arm.field_3654;
        sleeve.field_3675 = arm.field_3675;
        sleeve.field_3674 = arm.field_3674;
        class_630 class_6302 = arm;
        if (class_6302 instanceof TransformablePart) {
            TransformablePart tArm = (TransformablePart)class_6302;
            class_6302 = sleeve;
            if (class_6302 instanceof TransformablePart) {
                TransformablePart tSleeve = (TransformablePart)class_6302;
                Matrix4f armMatrix = tArm.punchy$getExplicitTransform();
                if (armMatrix == null) {
                    tSleeve.punchy$setExplicitTransform(null);
                    return;
                }
                tSleeve.punchy$setExplicitTransform(new Matrix4f((Matrix4fc)armMatrix));
            }
        }
    }

    private static boolean shouldApplySwimTransforms() {
        AnimationClip overlay;
        if (CrawlStateMachine.shouldSuppressSwimForSwap() || ClimbStateMachine.shouldSuppressSwimForSwap()) {
            return false;
        }
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying() && PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() != null && (PunchyAnimationManager.isClipNamed(overlay = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip(), "swiming") || PunchyAnimationManager.isClipNamed(overlay, "crawl") || PunchyAnimationManager.isClipNamed(overlay, "climb"))) {
            return true;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return true;
        }
        if (PunchyArmRenderer.isDualHandedEquipPoseActive(clip)) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return true;
        }
        String key = name.toLowerCase(Locale.ROOT);
        return !key.startsWith("use_") && !key.startsWith("set_fire") && !key.startsWith("put_out_fire");
    }

    private static boolean isDualHandedEquipPoseActive(AnimationClip clip) {
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String key = name.toLowerCase(Locale.ROOT);
        if (!(key.equals("hand_in") || key.equals("hand_out") || key.startsWith("hand_in_") || key.startsWith("hand_out_"))) {
            return false;
        }
        if (PunchyAnimationManager.POSE_HANDLER.getActiveArm() != null) {
            return false;
        }
        return PunchyAnimationManager.isDualHandedActive(clip, PunchyAnimationManager.POSE_HANDLER.getCurrentTime());
    }

    private static void applyGeoArmToPlayer(class_630 playerArm, class_630 geoArm, Matrix4f rootWorld, class_746 player, class_1306 arm, float partialTicks) {
        class_630 class_6302;
        if (playerArm == null || !((class_6302 = playerArm) instanceof TransformablePart)) {
            return;
        }
        TransformablePart tPlayer = (TransformablePart)class_6302;
        if (geoArm == null) {
            tPlayer.punchy$setExplicitTransform(null);
            return;
        }
        Matrix4f armLocal = PunchyArmRenderer.getLocalMatrix(geoArm);
        if (armLocal == null) {
            tPlayer.punchy$setExplicitTransform(null);
            return;
        }
        if (PunchyArmRenderer.isBlacklistedForArm(player, arm)) {
            tPlayer.punchy$setExplicitTransform(null);
            return;
        }
        Matrix4f world = new Matrix4f((Matrix4fc)rootWorld);
        if (!PunchyConfig.disableArmPhysics()) {
            MovementStateMachine.get().applyRootSpaceMotion(world, partialTicks, arm);
            MovementStateMachine.get().applyShieldImpactToRoot(world, partialTicks, arm);
        }
        world.mul((Matrix4fc)armLocal);
        PunchyArmRenderer.applyArmOffsets(world, player, arm, partialTicks);
        MovementStateMachine.get().applyCameraYawLagToArm(world);
        if (MovementStateMachine.APPLY_TO_ARMS) {
            MovementStateMachine.get().applyToMatrix(world, partialTicks, arm);
        }
        PunchyArmRenderer.cacheArmWorld(player, arm, world);
        tPlayer.punchy$setExplicitTransform(world);
    }

    private static void cacheArmWorld(class_746 player, class_1306 arm, Matrix4f world) {
        if (player == null || arm == null || world == null || player.method_37908() == null) {
            return;
        }
        long tick = player.method_37908().method_8510();
        if (tick != lastArmCacheTick) {
            ARM_WORLD_CACHE.clear();
            lastArmCacheTick = tick;
        }
        ARM_WORLD_CACHE.put(arm, new Matrix4f((Matrix4fc)world));
    }

    private static void applyArmOffsets(Matrix4f world, class_746 player, class_1306 arm, float partialTicks) {
        float globalHandFactor;
        if (player == null || arm == null) {
            return;
        }
        MovementStateMachine movement = MovementStateMachine.get();
        boolean syncWalkArms = movement != null && movement.isWalkArmSyncEnabled();
        class_1306 sourceArm = syncWalkArms ? movement.getSyncWalkReferenceArm(arm) : arm;
        boolean leftArm = arm == class_1306.field_6182;
        boolean sourceLeftArm = sourceArm == class_1306.field_6182;
        class_1268 hand = PunchyArmRenderer.resolveHand(player, sourceArm);
        class_1799 actualArmStack = player.method_5998(PunchyArmRenderer.resolveHand(player, arm));
        class_1799 baseStack = player.method_5998(hand);
        class_1799 stack = PunchyAnimationManager.resolveRenderStack((class_1657)player, sourceArm, baseStack);
        PlayerArmModelLayer.ToolKind armKind = ToolKindResolver.resolveArmKind(stack, PlayerArmModelLayer.ToolKind.AXE);
        boolean hasSpecificArmOverride = !stack.method_7960() && ArmToolTuning.hasSpecificProfile(stack, sourceLeftArm);
        ArmToolTuning.Profile profile = ArmToolTuning.resolveProfile(stack, armKind, sourceLeftArm);
        if (syncWalkArms && sourceArm != arm && profile != null) {
            profile = profile.mirror();
        }
        if (PunchyArmRenderer.shouldResetBurningEmptyMainHandTuning(player, arm, actualArmStack)) {
            profile = PunchyArmRenderer.zeroArmRotations(profile);
        }
        if (!PunchyConfig.disableNativeItemPhysics()) {
            MacePhysics.get().applyTo(world, player, arm, partialTicks);
        }
        if (profile == null) {
            return;
        }
        ArmToolTuning.Profile globalProfile = ArmToolTuning.getGlobal(sourceLeftArm);
        if (globalProfile == null) {
            globalProfile = new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        if (syncWalkArms && sourceArm != arm) {
            globalProfile = globalProfile.mirror();
        }
        if (profile == null) {
            profile = globalProfile;
        }
        if ((globalHandFactor = PunchyArmRenderer.resolveGlobalHandFactorForFlightPose()) < 0.999f) {
            globalProfile = new ArmToolTuning.Profile(globalProfile.scale, globalProfile.offX * globalHandFactor, globalProfile.offY * globalHandFactor, globalProfile.offZ * globalHandFactor, globalProfile.rotX * globalHandFactor, globalProfile.rotY * globalHandFactor, globalProfile.rotZ * globalHandFactor);
        }
        if (globalHandFactor < 0.999f && armKind == PlayerArmModelLayer.ToolKind.EMPTY_HAND && profile != null) {
            profile = new ArmToolTuning.Profile(profile.scale, profile.offX * globalHandFactor, profile.offY * globalHandFactor, profile.offZ * globalHandFactor, profile.rotX * globalHandFactor, profile.rotY * globalHandFactor, profile.rotZ * globalHandFactor);
        }
        float tuningFactor = PunchyArmRenderer.resolveFlightPoseTuningFactor(partialTicks, arm);
        if (armKind == PlayerArmModelLayer.ToolKind.EMPTY_HAND && movement != null) {
            tuningFactor *= 1.0f - class_3532.method_15363((float)movement.getSprintBlendAlpha(), (float)0.0f, (float)1.0f);
        }
        float factor = class_3532.method_15363((float)(tuningFactor *= PunchyArmRenderer.updateWaterArmTuningBlend(arm, partialTicks)), (float)0.0f, (float)1.0f);
        if (hasSpecificArmOverride) {
            factor = 1.0f;
        }
        float profileRotationFactor = class_3532.method_15363((float)PunchyArmRenderer.updateArmRotationResetBlend(arm, partialTicks), (float)0.0f, (float)1.0f);
        profile = PunchyArmRenderer.blendArmRotations(profile, profileRotationFactor);
        float posX = (globalProfile.offX + (profile.offX - globalProfile.offX) * factor) * 16.0f;
        float posY = (globalProfile.offY + (profile.offY - globalProfile.offY) * factor) * 16.0f;
        float posZ = (globalProfile.offZ + (profile.offZ - globalProfile.offZ) * factor) * 16.0f;
        float rotX = globalProfile.rotX + (profile.rotX - globalProfile.rotX) * factor;
        float rotY = globalProfile.rotY + (profile.rotY - globalProfile.rotY) * factor;
        float rotZ = globalProfile.rotZ + (profile.rotZ - globalProfile.rotZ) * factor;
        float scale = globalProfile.scale + (profile.scale - globalProfile.scale) * factor;
        world.translateLocal(posX, posY, posZ);
        Matrix4f debug = new Matrix4f().rotateX((float)Math.toRadians(rotX)).rotateY((float)Math.toRadians(rotY)).rotateZ((float)Math.toRadians(rotZ)).scale(scale, scale, scale);
        world.mul((Matrix4fc)debug);
    }

    private static boolean shouldResetBurningEmptyMainHandTuning(class_746 player, class_1306 arm, class_1799 actualStack) {
        if (player == null || arm == null || actualStack == null || !actualStack.method_7960() || !player.method_5809()) {
            return false;
        }
        return PunchyArmRenderer.resolveHand(player, arm) == class_1268.field_5808;
    }

    private static ArmToolTuning.Profile zeroArmRotations(ArmToolTuning.Profile profile) {
        if (profile == null) {
            return null;
        }
        return new ArmToolTuning.Profile(profile.scale, profile.offX, profile.offY, profile.offZ, 0.0f, 0.0f, 0.0f);
    }

    private static ArmToolTuning.Profile blendArmRotations(ArmToolTuning.Profile profile, float factor) {
        if (profile == null) {
            return null;
        }
        float clamped = class_3532.method_15363((float)factor, (float)0.0f, (float)1.0f);
        return new ArmToolTuning.Profile(profile.scale, profile.offX, profile.offY, profile.offZ, profile.rotX * clamped, profile.rotY * clamped, profile.rotZ * clamped);
    }

    private static ToolTuning.Profile zeroItemRotations(ToolTuning.Profile profile) {
        if (profile == null) {
            return null;
        }
        ToolTuning.Profile adjusted = new ToolTuning.Profile(profile.scale, profile.offX, profile.offY, profile.offZ, 0.0f, 0.0f, 0.0f, profile.pivotX, profile.pivotY, profile.pivotZ, profile.physicsMode);
        adjusted.disableSprintLateralTilt = profile.disableSprintLateralTilt;
        adjusted.keepArmTuning = profile.keepArmTuning;
        adjusted.keepTriggerArmTuning = profile.keepTriggerArmTuning;
        if (profile.physicsSettings != null) {
            adjusted.physicsSettings = profile.physicsSettings.copy();
        }
        return adjusted;
    }

    private static void applyCrawlCameraDecouple(Matrix4f world, class_746 player) {
        float end;
        if (world == null || player == null) {
            return;
        }
        if (!PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            return;
        }
        AnimationClip clip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
        if (clip == null) {
            return;
        }
        String clipName = clip.getName();
        boolean crawl = "crawl".equals(clipName);
        boolean climb = "climb".equals(clipName);
        if (!crawl && !climb) {
            return;
        }
        float pitch = player.method_36455();
        float absPitch = Math.abs(pitch);
        float start = crawl ? 0.0f : 0.0f;
        float f = end = crawl ? 0.0f : 0.0f;
        if (absPitch <= start) {
            return;
        }
        float t = end <= start ? 1.0f : (absPitch - start) / (end - start);
        t = class_3532.method_15363((float)t, (float)0.0f, (float)1.0f);
        float rotLimit = 75.0f;
        float rotPitch = Math.min(absPitch, rotLimit) * Math.signum(pitch);
        float counter = -rotPitch * t;
        MovementStateMachine movement = MovementStateMachine.get();
        if (movement == null) {
            world.rotateX((float)Math.toRadians(counter));
            return;
        }
        world.rotate((float)Math.toRadians(counter), 1.0f, 0.0f, 0.0f);
        if (crawl && pitch > 0.0f) {
            float extra = class_3532.method_15363((float)(pitch / 90.0f), (float)0.0f, (float)1.0f);
            world.translate(0.0f, 20.0f * extra, 0.0f);
        }
    }

    private static void applyCameraLookOffset(Matrix4f world) {
        if (world == null || PunchyConfig.disableArmPhysics()) {
            return;
        }
        MovementStateMachine movement = MovementStateMachine.get();
        if (movement == null) {
            return;
        }
        movement.applyCameraLookToRoot(world);
    }

    static Matrix4f getLocalMatrix(class_630 part) {
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

    private static Vector3f getAnimatedTranslation(class_630 part) {
        class_630 class_6302;
        if (part == null || !((class_6302 = part) instanceof TransformablePart)) {
            return new Vector3f();
        }
        TransformablePart transformable = (TransformablePart)class_6302;
        Matrix4f explicit = transformable.punchy$getExplicitTransform();
        if (explicit == null) {
            if (transformable.punchy$hasDirectPoseBase()) {
                return new Vector3f(part.field_3657 - transformable.punchy$getDirectPoseBaseX(), part.field_3656 - transformable.punchy$getDirectPoseBaseY(), part.field_3655 - transformable.punchy$getDirectPoseBaseZ());
            }
            return new Vector3f();
        }
        Matrix4f base = new Matrix4f().translate(part.field_3657, part.field_3656, part.field_3655).rotateZ(part.field_3674).rotateY(part.field_3675).rotateX(part.field_3654).scale(part.field_37938, part.field_37939, part.field_37940);
        Matrix4f baseInv = new Matrix4f((Matrix4fc)base).invert();
        Matrix4f anim = baseInv.mul((Matrix4fc)explicit, new Matrix4f());
        return anim.getTranslation(new Vector3f());
    }

    private static Vector3f getAnimatedGripTranslation(class_630 part) {
        class_630 class_6302;
        if (part == null || !((class_6302 = part) instanceof TransformablePart)) {
            return new Vector3f();
        }
        TransformablePart transformable = (TransformablePart)class_6302;
        Matrix4f explicit = transformable.punchy$getExplicitTransform();
        if (explicit == null && transformable.punchy$hasDirectPoseBase()) {
            Vector3f directDelta = new Vector3f(part.field_3657 - transformable.punchy$getDirectPoseBaseX(), part.field_3656 - transformable.punchy$getDirectPoseBaseY(), part.field_3655 - transformable.punchy$getDirectPoseBaseZ());
            return new Vector3f(-directDelta.x, -directDelta.z, -directDelta.y);
        }
        return PunchyArmRenderer.getAnimatedTranslation(part);
    }

    private static void applyGripLocalTransform(class_4587 poseStack, class_630 geoGrip, Vector3f pivotOffset) {
        float pivotZ;
        TransformablePart transformable;
        if (geoGrip == null) {
            return;
        }
        class_630 class_6302 = geoGrip;
        if (class_6302 instanceof TransformablePart && (transformable = (TransformablePart)class_6302).punchy$getExplicitTransform() == null && transformable.punchy$hasDirectPoseBase()) {
            float pivotZ2;
            float deltaX = geoGrip.field_3654 - transformable.punchy$getDirectPoseBaseXRot();
            float deltaY = geoGrip.field_3675 - transformable.punchy$getDirectPoseBaseYRot();
            float deltaZ = geoGrip.field_3674 - transformable.punchy$getDirectPoseBaseZRot();
            float pivotX = pivotOffset != null ? pivotOffset.x : 0.0f;
            float pivotY = pivotOffset != null ? pivotOffset.y : 0.0f;
            float f = pivotZ2 = pivotOffset != null ? pivotOffset.z : 0.0f;
            if (pivotX != 0.0f || pivotY != 0.0f || pivotZ2 != 0.0f) {
                poseStack.method_46416(pivotX, pivotY, pivotZ2);
            }
            poseStack.method_22907(class_7833.field_40714.rotation(-deltaX));
            poseStack.method_22907(class_7833.field_40716.rotation(-deltaZ));
            poseStack.method_22907(class_7833.field_40718.rotation(-deltaY));
            if (pivotX != 0.0f || pivotY != 0.0f || pivotZ2 != 0.0f) {
                poseStack.method_46416(-pivotX, -pivotY, -pivotZ2);
            }
            return;
        }
        Matrix4f gripLocal = PunchyArmRenderer.getLocalMatrix(geoGrip);
        if (gripLocal == null) {
            return;
        }
        Quaternionf rot = new Quaternionf();
        gripLocal.getUnnormalizedRotation(rot);
        rot.invert();
        float pivotX = pivotOffset != null ? pivotOffset.x : 0.0f;
        float pivotY = pivotOffset != null ? pivotOffset.y : 0.0f;
        float f = pivotZ = pivotOffset != null ? pivotOffset.z : 0.0f;
        if (pivotX != 0.0f || pivotY != 0.0f || pivotZ != 0.0f) {
            poseStack.method_46416(pivotX, pivotY, pivotZ);
        }
        poseStack.method_22907(rot);
        if (pivotX != 0.0f || pivotY != 0.0f || pivotZ != 0.0f) {
            poseStack.method_46416(-pivotX, -pivotY, -pivotZ);
        }
    }

    private static void applyGripTranslation(class_4587 poseStack, Vector3f gripPos) {
        if (gripPos == null) {
            return;
        }
        poseStack.method_46416(gripPos.x, gripPos.y, gripPos.z);
    }

    private static void applyItemPreTransform(class_4587 poseStack, Vector3f pivot, ToolTuning.Profile tuning, boolean legacyOrder) {
        float pivotZ;
        if (tuning == null) {
            return;
        }
        float rotX = tuning.rotX;
        float rotY = tuning.rotY;
        float rotZ = tuning.rotZ;
        float pivotX = pivot != null ? pivot.x : 0.0f;
        float pivotY = pivot != null ? pivot.y : 0.0f;
        float f = pivotZ = pivot != null ? pivot.z : 0.0f;
        if (pivotX != 0.0f || pivotY != 0.0f || pivotZ != 0.0f) {
            poseStack.method_46416(pivotX, pivotY, pivotZ);
        }
        if (legacyOrder) {
            poseStack.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(rotX));
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(rotY));
        } else {
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(rotX));
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(rotY));
            poseStack.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
        }
        if (pivotX != 0.0f || pivotY != 0.0f || pivotZ != 0.0f) {
            poseStack.method_46416(-pivotX, -pivotY, -pivotZ);
        }
    }

    private static void applyItemTranslation(class_4587 poseStack, ToolTuning.Profile tuning, boolean isLeft) {
        if (tuning == null) {
            return;
        }
        float itemBaseX = isLeft ? 0.04f : -0.04f;
        poseStack.method_46416(tuning.offX + itemBaseX, tuning.offY + 0.14f + -0.17f, tuning.offZ + -0.02f);
    }

    private static Vector3f getGeoItemScale(class_630 geoGrip, class_630 geoItem) {
        class_630 source;
        class_630 class_6302 = source = geoGrip != null ? geoGrip : geoItem;
        if (source == null) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        Matrix4f local = PunchyArmRenderer.getLocalMatrix(source);
        if (local == null) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        return PunchyArmRenderer.getScaleFromMatrix(local);
    }

    private static void applyGeoItemScale(class_4587 poseStack, class_630 geoGrip, class_630 geoItem) {
        Vector3f scale = PunchyArmRenderer.getGeoItemScale(geoGrip, geoItem);
        poseStack.method_22905(scale.x, scale.y, scale.z);
    }

    private static Vector3f getScaleFromMatrix(Matrix4f matrix) {
        Vector3f x = new Vector3f(matrix.m00(), matrix.m01(), matrix.m02());
        Vector3f y = new Vector3f(matrix.m10(), matrix.m11(), matrix.m12());
        Vector3f z = new Vector3f(matrix.m20(), matrix.m21(), matrix.m22());
        return new Vector3f(x.length(), y.length(), z.length());
    }

    private static float getFreezeProgress(class_742 player, float partialTicks) {
        if (player == null) {
            return 0.0f;
        }
        int ticksFrozen = player.method_32312();
        if (ticksFrozen <= 0) {
            return 0.0f;
        }
        int required = player.method_32315();
        if (required <= 0) {
            return 0.0f;
        }
        float progress = ((float)ticksFrozen + partialTicks) / (float)required;
        return class_3532.method_15363((float)progress, (float)0.0f, (float)1.0f);
    }

    private static float smoothStep(float edge0, float edge1, float x) {
        float t = class_3532.method_15363((float)((x - edge0) / (edge1 - edge0)), (float)0.0f, (float)1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static void renderFreezeOverlay(class_630 armPart, boolean isLeft, boolean slim, class_4587 poseStack, class_4597 buffer, int combinedLight, class_742 player, float partialTicks) {
        float a3;
        float a2;
        if (!PunchyConfig.enableFreezeArmOverlay()) {
            return;
        }
        float progress = PunchyArmRenderer.getFreezeProgress(player, partialTicks);
        if (player != null) {
            float delayTicks = 24.0f;
            if ((float)player.method_32312() + partialTicks < delayTicks) {
                return;
            }
            int required = player.method_32315();
            if (required > 0) {
                float delayed = ((float)player.method_32312() + partialTicks - delayTicks) / (float)required;
                progress = class_3532.method_15363((float)delayed, (float)0.0f, (float)1.0f);
            }
        }
        if (progress <= 0.0f) {
            return;
        }
        if (progress < 0.33f) {
            a1 = 1.0f;
            a2 = 0.0f;
            a3 = 0.0f;
        } else if (progress < 0.66f) {
            a1 = 0.0f;
            a2 = 1.0f;
            a3 = 0.0f;
        } else {
            a1 = 0.0f;
            a2 = 0.0f;
            a3 = 1.0f;
        }
        class_2960 tex = a3 > 0.0f ? FREEZE_ARM_STAGE_3 : (a2 > 0.0f ? FREEZE_ARM_STAGE_2 : FREEZE_ARM_STAGE_1);
        PunchyArmRenderer.submitFreezeLayer3d(armPart, poseStack, buffer, combinedLight, tex, isLeft, slim);
    }

    private static void renderMudOverlay(class_630 armPart, boolean isLeft, boolean slim, class_4587 poseStack, class_4597 buffer, int combinedLight) {
        if (!PunchyConfig.enableMud()) {
            return;
        }
        int stage = MudStateMachine.getStageForArm(isLeft ? class_1306.field_6182 : class_1306.field_6183);
        if (stage <= 0) {
            return;
        }
        class_2960 tex = stage == 1 ? MUD_ARM_STAGE_1 : (stage == 2 ? MUD_ARM_STAGE_2 : MUD_ARM_STAGE_3);
        PunchyArmRenderer.submitFreezeLayer3d(armPart, poseStack, buffer, combinedLight, tex, isLeft, slim);
    }

    private static void renderSweatOverlay(class_630 armPart, boolean isLeft, boolean slim, class_4587 poseStack, class_4597 buffer, int combinedLight) {
        if (!PunchyConfig.enableSweat()) {
            return;
        }
        int stage = SweatStateMachine.getStage();
        if (stage <= 0) {
            return;
        }
        class_2960 tex = switch (stage) {
            case 1 -> SWEAT_ARM_STAGE_1;
            case 2 -> SWEAT_ARM_STAGE_2;
            case 3 -> SWEAT_ARM_STAGE_3;
            default -> SWEAT_ARM_STAGE_4;
        };
        float alpha = SweatStateMachine.getFadeAlpha();
        if (alpha <= 0.0f) {
            return;
        }
        FreezeVoxelMask mask = PunchyArmRenderer.getSweatMask(tex, isLeft);
        PunchyArmRenderer.submitFreezeLayer3dWithMask(armPart, poseStack, buffer, combinedLight, tex, mask, isLeft, slim, 0.09f, 0.65f, 0.15f, true, alpha);
    }

    private static void renderLavaHandOverlay(class_630 armPart, class_4587 poseStack, class_4597 buffer, int combinedLight, class_742 player, class_1306 arm, float partialTicks) {
        if (player == null || arm == null) {
            return;
        }
        if (!PunchyConfig.enableBurn()) {
            return;
        }
        class_1799 stack = PunchyArmRenderer.getStackForArm(player, arm);
        float burnAmount = PunchyArmRenderer.updateBurnFade(player, arm, stack, partialTicks);
        if (burnAmount <= 0.001f) {
            return;
        }
        int alpha = class_3532.method_15340((int)((int)(burnAmount * 255.0f)), (int)0, (int)255);
        int color = alpha << 24 | 0xFFFFFF;
        class_2960 burnTex = PunchyArmRenderer.resolveBurnOverlayTexture(player);
        class_4588 consumer = buffer.getBuffer(class_1921.method_23580((class_2960)burnTex));
        armPart.method_22699(poseStack, consumer, combinedLight, class_4608.field_21444, color);
    }

    private static class_2960 resolveBurnOverlayTexture(class_742 player) {
        if (player == null) {
            return BURN_ARM_HAND_OVERLAY;
        }
        class_2960 skinId = PunchyArmRenderer.getPlayerTexture(player);
        if (skinId == null) {
            return BURN_ARM_HAND_OVERLAY;
        }
        Integer cachedHeight = SKIN_HEIGHT_CACHE.get(skinId);
        if (cachedHeight == null) {
            cachedHeight = PunchyArmRenderer.readTextureHeight(skinId);
            SKIN_HEIGHT_CACHE.put(skinId, cachedHeight);
        }
        return cachedHeight != null && cachedHeight <= 32 ? BURN_ARM_HAND_OVERLAY_LEGACY : BURN_ARM_HAND_OVERLAY;
    }

    private static Integer readTextureHeight(class_2960 texture) {
        Integer n;
        block9: {
            class_3300 manager = class_310.method_1551().method_1478();
            Optional res = manager.method_14486(texture);
            if (res.isEmpty()) {
                return null;
            }
            InputStream stream = ((class_3298)res.get()).method_14482();
            try {
                class_1011 image = class_1011.method_4309((InputStream)stream);
                n = image.method_4323();
                if (stream == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    return null;
                }
            }
            stream.close();
        }
        return n;
    }

    private static void submitFreezeLayer3d(class_630 armPart, class_4587 poseStack, class_4597 buffer, int combinedLight, class_2960 texture, boolean isLeft, boolean slim) {
        FreezeVoxelMask mask = PunchyArmRenderer.getFreezeMask(texture, isLeft);
        PunchyArmRenderer.submitFreezeLayer3dWithMask(armPart, poseStack, buffer, combinedLight, texture, mask, isLeft, slim, 0.45f, 1.0f, 1.0f, false, 1.0f);
    }

    private static void submitFreezeLayer3dWithMask(class_630 armPart, class_4587 poseStack, class_4597 buffer, int combinedLight, class_2960 texture, FreezeVoxelMask mask, boolean isLeft, boolean slim, float thickness, float pixelScaleX, float pixelScaleY, boolean translucent, float alpha) {
        PunchyArmRenderer.submitFreezeLayer3dWithMask(armPart, poseStack, buffer, combinedLight, texture, mask, isLeft, slim, thickness, pixelScaleX, pixelScaleY, translucent, alpha, 6.0f);
    }

    private static void submitFreezeLayer3dWithMask(class_630 armPart, class_4587 poseStack, class_4597 buffer, int combinedLight, class_2960 texture, FreezeVoxelMask mask, boolean isLeft, boolean slim, float thickness, float pixelScaleX, float pixelScaleY, boolean translucent, float alpha, float yBandMin) {
        float width;
        if (mask == null || mask.pixels.isEmpty()) {
            return;
        }
        float f = width = slim ? 3.0f : 4.0f;
        float xMin = isLeft ? -1.0f : (slim ? -2.0f : -3.0f);
        float xMax = xMin + width;
        float fxMin = xMin;
        float fxMax = xMax;
        float zMin = -2.0f;
        float zMax = 2.0f;
        boolean fSlim = slim;
        float yBandMinLocal = yBandMin;
        float pxH = 1.0f;
        poseStack.method_22903();
        armPart.method_22703(poseStack);
        poseStack.method_22905(0.0625f, 0.0625f, 0.0625f);
        class_4588 consumer = buffer.getBuffer(translucent ? class_1921.method_23580((class_2960)texture) : class_1921.method_23578((class_2960)texture));
        class_4587.class_4665 pose = poseStack.method_23760();
        for (FreezeVoxelMask.Pixel p : mask.pixels) {
            float x1;
            float x0;
            float z1;
            float z0;
            int seg = p.x / 4;
            int segX = p.x % 4;
            if (!(!fSlim || seg != 1 && seg != 3 || p.y != 0 && p.y != 3 && p.y != 1)) continue;
            float y0 = yBandMinLocal + (float)p.y * 1.0f;
            float y1 = y0 + 1.0f;
            float overlap = thickness;
            if (seg == 0) {
                z0 = -2.0f + (float)segX;
                z1 = z0 + 1.0f;
                x0 = fxMax;
                x1 = fxMax + thickness;
                if (segX == 0) {
                    z0 -= overlap;
                }
                if (segX == 3) {
                    z1 += overlap;
                }
            } else if (seg == 1) {
                x0 = fxMin + (float)segX;
                x1 = x0 + 1.0f;
                z0 = -2.0f - thickness;
                z1 = -2.0f;
                if (segX == 0) {
                    x0 -= overlap;
                }
                if (segX == 3) {
                    x1 += overlap;
                }
            } else if (seg == 2) {
                z0 = -2.0f + (float)segX;
                z1 = z0 + 1.0f;
                x0 = fxMin - thickness;
                x1 = fxMin;
                if (segX == 0) {
                    z0 -= overlap;
                }
                if (segX == 3) {
                    z1 += overlap;
                }
            } else {
                x0 = fxMin + (float)segX;
                x1 = x0 + 1.0f;
                z0 = 2.0f;
                z1 = 2.0f + thickness;
                if (segX == 0) {
                    x0 -= overlap;
                }
                if (segX == 3) {
                    x1 += overlap;
                }
            }
            if (pixelScaleX < 1.0f || pixelScaleY < 1.0f) {
                float insetX = (1.0f - pixelScaleX) * 0.5f;
                float insetY = (1.0f - pixelScaleY) * 0.5f;
                if (seg == 0 || seg == 2) {
                    z0 += insetX;
                    z1 -= insetX;
                    y0 += insetY;
                    y1 -= insetY;
                } else {
                    x0 += insetX;
                    x1 -= insetX;
                    y0 += insetY;
                    y1 -= insetY;
                }
            }
            PunchyArmRenderer.renderVoxel(pose, consumer, x0, x1, y0, y1, z0, z1, p.u0, p.v0, p.u1, p.v1, combinedLight, alpha);
        }
        poseStack.method_22909();
    }

    private static void renderVoxel(class_4587.class_4665 pose, class_4588 consumer, float x0, float x1, float y0, float y1, float z0, float z1, float u0, float v0, float u1, float v1, int packedLight) {
        PunchyArmRenderer.addVertex(pose, consumer, x0, y1, z0, u0, v0, packedLight, 0.0f, 1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y1, z0, u1, v0, packedLight, 0.0f, 1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y1, z1, u1, v1, packedLight, 0.0f, 1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y1, z1, u0, v1, packedLight, 0.0f, 1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y0, z1, u0, v1, packedLight, 0.0f, -1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y0, z1, u1, v1, packedLight, 0.0f, -1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y0, z0, u1, v0, packedLight, 0.0f, -1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y0, z0, u0, v0, packedLight, 0.0f, -1.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y0, z0, u0, v0, packedLight, 0.0f, 0.0f, -1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y0, z0, u1, v0, packedLight, 0.0f, 0.0f, -1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y1, z0, u1, v1, packedLight, 0.0f, 0.0f, -1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y1, z0, u0, v1, packedLight, 0.0f, 0.0f, -1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y1, z1, u0, v1, packedLight, 0.0f, 0.0f, 1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y1, z1, u1, v1, packedLight, 0.0f, 0.0f, 1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y0, z1, u1, v0, packedLight, 0.0f, 0.0f, 1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y0, z1, u0, v0, packedLight, 0.0f, 0.0f, 1.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y0, z1, u0, v1, packedLight, -1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y1, z1, u1, v1, packedLight, -1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y1, z0, u1, v0, packedLight, -1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x0, y0, z0, u0, v0, packedLight, -1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y0, z0, u0, v0, packedLight, 1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y1, z0, u1, v0, packedLight, 1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y1, z1, u1, v1, packedLight, 1.0f, 0.0f, 0.0f);
        PunchyArmRenderer.addVertex(pose, consumer, x1, y0, z1, u0, v1, packedLight, 1.0f, 0.0f, 0.0f);
    }

    private static void renderVoxel(class_4587.class_4665 pose, class_4588 consumer, float x0, float x1, float y0, float y1, float z0, float z1, float u0, float v0, float u1, float v1, int packedLight, float alpha) {
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y1, z0, u0, v0, packedLight, 0.0f, 1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y1, z0, u1, v0, packedLight, 0.0f, 1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y1, z1, u1, v1, packedLight, 0.0f, 1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y1, z1, u0, v1, packedLight, 0.0f, 1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y0, z1, u0, v1, packedLight, 0.0f, -1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y0, z1, u1, v1, packedLight, 0.0f, -1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y0, z0, u1, v0, packedLight, 0.0f, -1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y0, z0, u0, v0, packedLight, 0.0f, -1.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y0, z0, u0, v0, packedLight, 0.0f, 0.0f, -1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y0, z0, u1, v0, packedLight, 0.0f, 0.0f, -1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y1, z0, u1, v1, packedLight, 0.0f, 0.0f, -1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y1, z0, u0, v1, packedLight, 0.0f, 0.0f, -1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y1, z1, u0, v1, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y1, z1, u1, v1, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y0, z1, u1, v0, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y0, z1, u0, v0, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y0, z1, u0, v1, packedLight, -1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y1, z1, u1, v1, packedLight, -1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y1, z0, u1, v0, packedLight, -1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x0, y0, z0, u0, v0, packedLight, -1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y0, z0, u0, v0, packedLight, 1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y1, z0, u1, v0, packedLight, 1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y1, z1, u1, v1, packedLight, 1.0f, 0.0f, 0.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, x1, y0, z1, u0, v1, packedLight, 1.0f, 0.0f, 0.0f, alpha);
    }

    private static void addVertex(class_4587.class_4665 pose, class_4588 consumer, float x, float y, float z, float u, float v, int packedLight, float nx, float ny, float nz) {
        consumer.method_22918(pose.method_23761(), x, y, z).method_1336(255, 255, 255, 255).method_22913(u, v).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, nx, ny, nz);
    }

    private static void addVertexAlpha(class_4587.class_4665 pose, class_4588 consumer, float x, float y, float z, float u, float v, int packedLight, float nx, float ny, float nz, float alpha) {
        int a = class_3532.method_15340((int)((int)(alpha * 255.0f)), (int)0, (int)255);
        consumer.method_22918(pose.method_23761(), x, y, z).method_1336(255, 255, 255, a).method_22913(u, v).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, nx, ny, nz);
    }

    private static FreezeVoxelMask getFreezeMask(class_2960 texture, boolean isLeft) {
        return PunchyArmRenderer.getOverlayMask(texture, isLeft, "freeze", 40, 28, 32, 60, 16, 4);
    }

    private static FreezeVoxelMask getSweatMask(class_2960 texture, boolean isLeft) {
        return PunchyArmRenderer.getOverlayMask(texture, isLeft, "sweat", 40, 28, 40, 28, 16, 16);
    }

    private static FreezeVoxelMask getOverlayMask(class_2960 texture, boolean isLeft, String prefix, int rightU0, int rightV0, int leftU0, int leftV0, int width, int height) {
        FreezeVoxelMask freezeVoxelMask;
        block12: {
            String key = prefix + ":" + String.valueOf(texture) + ":" + (isLeft ? "L" : "R");
            FreezeVoxelMask cached = FREEZE_MASK_CACHE.get(key);
            if (cached != null) {
                return cached;
            }
            class_3300 manager = class_310.method_1551().method_1478();
            Optional res = manager.method_14486(texture);
            if (res.isEmpty()) {
                return null;
            }
            InputStream stream = ((class_3298)res.get()).method_14482();
            try {
                class_1011 image = class_1011.method_4309((InputStream)stream);
                int w = image.method_4307();
                int h = image.method_4323();
                ArrayList<FreezeVoxelMask.Pixel> pixels = new ArrayList<FreezeVoxelMask.Pixel>();
                int u0 = isLeft ? leftU0 : rightU0;
                int v0 = isLeft ? leftV0 : rightV0;
                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        int argb = image.method_4315(u0 + x, v0 + y);
                        int a = argb >> 24 & 0xFF;
                        if (a <= 0) continue;
                        float uA = (float)(u0 + x) / (float)w;
                        float vA = (float)(v0 + y) / (float)h;
                        float uB = (float)(u0 + x + 1) / (float)w;
                        float vB = (float)(v0 + y + 1) / (float)h;
                        pixels.add(new FreezeVoxelMask.Pixel(x, y, uA, vA, uB, vB));
                    }
                }
                FreezeVoxelMask mask = new FreezeVoxelMask(pixels);
                FREEZE_MASK_CACHE.put(key, mask);
                freezeVoxelMask = mask;
                if (stream == null) break block12;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    return null;
                }
            }
            stream.close();
        }
        return freezeVoxelMask;
    }

    private static boolean isHotItem(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_31574(class_1802.field_8187)) {
            return true;
        }
        if (stack.method_31574(class_1802.field_8354)) {
            return true;
        }
        if (stack.method_31574(class_1802.field_8135)) {
            return true;
        }
        if (stack.method_31574(class_1802.field_8894)) {
            return true;
        }
        if (stack.method_31574(class_1802.field_8183)) {
            return true;
        }
        if (stack.method_31574(class_1802.field_8814)) {
            return true;
        }
        if (stack.method_31574(class_1802.field_17346)) {
            return true;
        }
        return stack.method_31574(class_1802.field_23842);
    }

    private static float updateBurnFade(class_742 player, class_1306 arm, class_1799 stack, float partialTicks) {
        if (player == null || arm == null || player.method_37908() == null) {
            return 0.0f;
        }
        boolean target = PunchyArmRenderer.isHotItem(stack);
        String equipType = HandEquipStateMachine.getActiveType();
        class_1306 equipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        boolean equipMatchesArm = equipArm != null && equipArm == arm;
        BurnFadeState state = BURN_FADE_STATE.computeIfAbsent(arm, key -> new BurnFadeState());
        float time = (float)player.method_37908().method_8510() + partialTicks;
        if (state.lastTime < 0.0f) {
            state.lastTime = time;
            state.value = 0.0f;
            return 0.0f;
        }
        if (equipMatchesArm && target && "hand_in".equals(equipType)) {
            state.value = 0.0f;
            state.lastTime = time;
            return 0.0f;
        }
        if (equipMatchesArm && !target && "hand_out".equals(equipType)) {
            state.lastTime = time;
            return state.value;
        }
        float dt = time - state.lastTime;
        state.lastTime = time;
        float desired = target ? 1.0f : 0.0f;
        float fadeTicks = target ? 30.0f : 60.0f;
        float step = fadeTicks <= 0.0f ? 1.0f : class_3532.method_15363((float)(dt / fadeTicks), (float)0.0f, (float)1.0f);
        state.value = class_3532.method_15363((float)(state.value + (desired - state.value) * step), (float)0.0f, (float)1.0f);
        return state.value;
    }

    private static void applyFreezeShake(class_4587 poseStack, class_742 player, float partialTicks) {
        if (!PunchyConfig.enableFreezeShake()) {
            return;
        }
        float progress = PunchyArmRenderer.getFreezeProgress(player, partialTicks);
        if (progress <= 0.0f) {
            return;
        }
        float time = (float)player.field_6012 + partialTicks;
        float translateAmp = 7.65E-4f * progress;
        float rotAmp = 0.1275f * progress;
        float offsetX = class_3532.method_15374((float)(time * 3.2f)) * translateAmp;
        float offsetY = class_3532.method_15374((float)(time * 4.6f + 1.7f)) * translateAmp * 0.6f;
        float offsetZ = class_3532.method_15374((float)(time * 2.4f + 2.1f)) * translateAmp;
        poseStack.method_46416(offsetX, offsetY, offsetZ);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(class_3532.method_15374((float)(time * 5.1f)) * rotAmp));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(class_3532.method_15374((float)(time * 4.3f + 0.5f)) * rotAmp));
    }

    private static void renderArm(class_591 playerModel, class_742 player, class_1306 arm, class_4587 poseStack, class_4597 buffer, int combinedLight, class_2960 texture, boolean slim, float partialTicks) {
        class_630 armPart = arm == class_1306.field_6182 ? playerModel.field_27433 : playerModel.field_3401;
        class_630 sleevePart = arm == class_1306.field_6182 ? playerModel.field_3484 : playerModel.field_3486;
        boolean isLeft = arm == class_1306.field_6182;
        PunchyArmRenderer.copyMatrixToSleeve(armPart, sleevePart);
        poseStack.method_22903();
        if (slim) {
            poseStack.method_46416((isLeft ? 1.0f : -1.0f) * 0.5f / 16.0f, 0.0f, 0.0f);
        }
        PunchyArmRenderer.applyArmMeshOffsets(poseStack, isLeft);
        PunchyArmRenderer.applyFreezeShake(poseStack, player, partialTicks);
        class_4588 consumer = buffer.getBuffer(class_1921.method_23580((class_2960)texture));
        armPart.method_22698(poseStack, consumer, combinedLight, class_4608.field_21444);
        if (sleevePart.field_3665) {
            sleevePart.method_22698(poseStack, consumer, combinedLight, class_4608.field_21444);
        }
        PunchyArmRenderer.renderLavaHandOverlay(armPart, poseStack, buffer, combinedLight, player, isLeft ? class_1306.field_6182 : class_1306.field_6183, partialTicks);
        PunchyArmRenderer.renderFreezeOverlay(armPart, isLeft, slim, poseStack, buffer, combinedLight, player, partialTicks);
        PunchyArmRenderer.renderMudOverlay(armPart, isLeft, slim, poseStack, buffer, combinedLight);
        PunchyArmRenderer.renderSweatOverlay(armPart, isLeft, slim, poseStack, buffer, combinedLight);
        if (player.method_5809()) {
            poseStack.method_22903();
            armPart.method_22703(poseStack);
            if (!PunchyConfig.disableEnhancedFireArmEffects()) {
                PunchyArmRenderer.renderFlameOnArm(poseStack, buffer);
            }
            poseStack.method_22909();
        }
        poseStack.method_22909();
    }

    private static void clearExplicitTransform(class_630 part) {
        class_630 class_6302;
        if (part != null && (class_6302 = part) instanceof TransformablePart) {
            TransformablePart transformable = (TransformablePart)class_6302;
            transformable.punchy$setExplicitTransform(null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderItemInHand(class_759 handRenderer, class_591 playerModel, class_746 player, class_1306 arm, class_4587 poseStack, class_4597 buffer, int combinedLight, Matrix4f rootWorld, class_630 geoArm, class_630 geoItem, class_630 geoGrip, float partialTicks) {
        class_811 context;
        Vector3f gripPos;
        ToolTuning.Profile tuning;
        boolean mainRight;
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        class_1799 baseStack = arm == class_1306.field_6183 ? (mainRight ? player.method_6047() : (player.method_6115() && player.method_6058() == class_1268.field_5810 ? player.method_6079() : HandEquipStateMachine.getVisualOffhandStack(player))) : (!mainRight ? player.method_6047() : (player.method_6115() && player.method_6058() == class_1268.field_5810 ? player.method_6079() : HandEquipStateMachine.getVisualOffhandStack(player)));
        class_1799 stack = PunchyAnimationManager.resolveRenderStack((class_1657)player, arm, baseStack);
        boolean isLeft = arm == class_1306.field_6182;
        class_1268 interactionHand = PunchyArmRenderer.resolveHand(player, arm);
        boolean isSlimeBlock = stack.method_31574(class_1802.field_8828);
        boolean isSlimeBall = stack.method_31574(class_1802.field_8777);
        boolean forceBlockTuning = PunchyArmRenderer.shouldForceBlockTuning(stack);
        VanillaFirstPersonItemLayer.ToolKind kind = forceBlockTuning ? VanillaFirstPersonItemLayer.ToolKind.BLOCK : ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        ToolTuning.Profile profile = tuning = forceBlockTuning ? ToolTuning.get(VanillaFirstPersonItemLayer.ToolKind.BLOCK, isLeft) : ToolTuning.resolveProfile(stack, kind, isLeft);
        if (PunchyArmRenderer.shouldResetBurningEmptyMainHandTuning(player, arm, baseStack)) {
            tuning = PunchyArmRenderer.zeroItemRotations(tuning);
        }
        ToolTuning.Profile effectiveTuning = tuning;
        poseStack.method_22903();
        playerModel.method_2803(arm, poseStack);
        PunchyArmRenderer.applyFreezeShake(poseStack, (class_742)player, partialTicks);
        if (!PunchyConfig.disableNativeItemPhysics()) {
            ShieldImpactPhysics.get().applyTo(poseStack, arm, partialTicks);
        }
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(-90.0f));
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0f));
        boolean hasSlimeBonePhysics = BedrockItemSystem.get().hasSlimeBonePhysics(stack);
        if (!(isSlimeBall || isSlimeBlock || hasSlimeBonePhysics || PunchyConfig.disableNativeItemPhysics())) {
            SlimeInHandPhysics.get().resetHand(interactionHand);
        }
        float side = isLeft ? -1.0f : 1.0f;
        poseStack.method_46416(side / 16.0f, 0.125f, -0.625f);
        if (!PunchyConfig.disableBoatFirstPersonAnimations()) {
            // empty if block
        }
        boolean stackEmpty = stack.method_7960();
        Vector3f vector3f = gripPos = geoGrip != null ? PunchyArmRenderer.getGripPos(geoGrip, geoItem, isLeft) : null;
        if (geoGrip != null && gripPos != null) {
            Vector3f animPos = PunchyArmRenderer.getAnimatedGripTranslation(geoGrip).mul(0.0625f);
            gripPos = new Vector3f((Vector3fc)gripPos).add((Vector3fc)animPos);
        }
        PunchyArmRenderer.applyGripTranslation(poseStack, gripPos);
        Vector3f tuningPivot = new Vector3f();
        Vector3f pivotFull = new Vector3f();
        if (effectiveTuning != null) {
            tuningPivot.set(effectiveTuning.pivotX, effectiveTuning.pivotY, effectiveTuning.pivotZ);
            pivotFull = PunchyArmRenderer.getTuningPivot(effectiveTuning, isLeft);
        }
        Vector3f hamsterPivot = new Vector3f((Vector3fc)pivotFull).sub((Vector3fc)tuningPivot);
        poseStack.method_22903();
        PunchyArmRenderer.applyGripLocalTransform(poseStack, geoGrip, hamsterPivot);
        if (AdorableHamsterPetsCompat.tryRenderHamsterInGrip((class_742)player, arm, poseStack, buffer, combinedLight, partialTicks)) {
            poseStack.method_22909();
            poseStack.method_22909();
            return;
        }
        poseStack.method_22909();
        if (stackEmpty) {
            poseStack.method_22909();
            return;
        }
        PunchyArmRenderer.applyGripLocalTransform(poseStack, geoGrip, pivotFull);
        Vector3f geoScale = PunchyArmRenderer.getGeoItemScale(geoGrip, geoItem);
        poseStack.method_22905(geoScale.x, geoScale.y, geoScale.z);
        if (effectiveTuning != null && effectiveTuning.scale != 1.0f) {
            poseStack.method_22905(effectiveTuning.scale, effectiveTuning.scale, effectiveTuning.scale);
        }
        if (tuningPivot.x != 0.0f || tuningPivot.y != 0.0f || tuningPivot.z != 0.0f) {
            poseStack.method_46416(tuningPivot.x, tuningPivot.y, tuningPivot.z);
        }
        Vector3f markerPivot = new Vector3f((Vector3fc)pivotFull).sub((Vector3fc)tuningPivot);
        if (geoGrip != null && effectiveTuning != null) {
            poseStack.method_22903();
            poseStack.method_46416(markerPivot.x, markerPivot.y, markerPivot.z);
            InHandBlockEntityRenderer.updateHeldPivot(player, arm, poseStack);
            if (PunchyDebug.DEBUG_POSITION_ENABLED) {
                poseStack.method_22905(geoScale.x * 0.1f, geoScale.y * 0.1f, geoScale.z * 0.1f);
                handRenderer.method_3233((class_1309)player, DEBUG_PIVOT_STACK, class_811.field_4319, false, poseStack, buffer, combinedLight);
            }
            poseStack.method_22909();
        }
        boolean legacyRotationOrder = ChestInHandRenderer.isChestStack(stack) || stack.method_31574(class_1802.field_8249) || stack.method_31574(class_1802.field_8105);
        boolean pendulumHandSpace = effectiveTuning != null && effectiveTuning.physicsMode != null && effectiveTuning.physicsMode != ToolTuning.PhysicsMode.NONE && (effectiveTuning.physicsSettings == null || !Boolean.FALSE.equals(effectiveTuning.physicsSettings.handSpace));
        boolean disablePendulumByAnimation = PunchyAnimationManager.POSE_HANDLER.isPendulumDisabled(partialTicks, arm);
        Matrix4f baseBeforeItem = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
        Matrix4f finalPose = null;
        if (pendulumHandSpace) {
            if (!PunchyConfig.disableNativeItemPhysics() && !disablePendulumByAnimation) {
                PendulumPhysics.get().applyTo(poseStack, effectiveTuning.physicsMode, markerPivot, effectiveTuning.physicsSettings, arm, partialTicks);
            }
            PunchyArmRenderer.applyItemPreTransform(poseStack, markerPivot, effectiveTuning, legacyRotationOrder);
        } else {
            PunchyArmRenderer.applyItemPreTransform(poseStack, markerPivot, effectiveTuning, legacyRotationOrder);
            if (!PunchyConfig.disableNativeItemPhysics() && !disablePendulumByAnimation) {
                PendulumPhysics.get().applyTo(poseStack, effectiveTuning.physicsMode, markerPivot, effectiveTuning.physicsSettings, arm, partialTicks);
            }
        }
        PunchyArmRenderer.applyItemTranslation(poseStack, effectiveTuning, isLeft);
        if (isSlimeBall) {
            if (!PunchyConfig.disableNativeItemPhysics()) {
                SlimeInHandPhysics.get().applySlimeBall(poseStack, interactionHand, partialTicks);
                finalPose = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
            }
        } else if (isSlimeBlock && !PunchyConfig.disableNativeItemPhysics()) {
            SlimeInHandPhysics.get().applySlimeBlock(poseStack, interactionHand, partialTicks);
            finalPose = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
        }
        if (finalPose == null) {
            finalPose = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
        }
        if (stack.method_31574(class_1802.field_42696)) {
            context = arm == class_1306.field_6183 ? class_811.field_4322 : class_811.field_4321;
        } else {
            class_811 class_8112 = context = arm == class_1306.field_6183 ? class_811.field_4320 : class_811.field_4323;
        }
        if (!PunchyConfig.disableBoatMinecartRaftModels()) {
            if (BoatInHandRenderer.isBoatStack(stack) && BoatInHandRenderer.tryRender(stack, interactionHand, poseStack, buffer, combinedLight)) {
                poseStack.method_22909();
                return;
            }
            if (MinecartInHandRenderer.isMinecartStack(stack) && MinecartInHandRenderer.tryRender(stack, interactionHand, poseStack, buffer, combinedLight)) {
                poseStack.method_22909();
                return;
            }
        }
        if (!PunchyConfig.disableChestModels() && ChestInHandRenderer.isChestStack(stack) && ChestInHandRenderer.tryRender(stack, interactionHand, poseStack, buffer, combinedLight)) {
            poseStack.method_22909();
            return;
        }
        if (InHandBlockEntityRenderer.renderIfSupported(stack, player, arm, poseStack, buffer, combinedLight, partialTicks)) {
            poseStack.method_22909();
            return;
        }
        Matrix4f noPhysicsPose = new Matrix4f((Matrix4fc)baseBeforeItem);
        class_4587 tmp = new class_4587();
        tmp.method_23760().method_23761().set((Matrix4fc)noPhysicsPose);
        PunchyArmRenderer.applyItemPreTransform(tmp, markerPivot, effectiveTuning, legacyRotationOrder);
        PunchyArmRenderer.applyItemTranslation(tmp, effectiveTuning, isLeft);
        noPhysicsPose = new Matrix4f((Matrix4fc)tmp.method_23760().method_23761());
        Matrix4f physicsRemoval = new Matrix4f((Matrix4fc)finalPose).invert().mul((Matrix4fc)noPhysicsPose);
        boolean usingItem = player.method_6115() && player.method_6058() == interactionHand;
        boolean bypassPackModelRenderers = false;
        BedrockItemSystem.setCurrentPhysicsDelta(arm, physicsRemoval);
        try {
            if (!bypassPackModelRenderers && BedrockItemSystem.get().tryRenderItem(stack, player, arm, poseStack, buffer, combinedLight, partialTicks, true)) {
                poseStack.method_22909();
                return;
            }
        }
        finally {
            BedrockItemSystem.clearCurrentPhysicsDelta(arm);
        }
        if (!bypassPackModelRenderers && ModelPartsSystem.get().tryRenderItem(stack, player, arm, poseStack, buffer, combinedLight, partialTicks, true)) {
            poseStack.method_22909();
            return;
        }
        boolean prevNeutralize = PunchyArmRenderer.shouldNeutralizeUseTransforms();
        if (PunchyArmRenderer.isFunctionalMap(player, stack)) {
            ((ItemInHandRendererInvoker)handRenderer).punchy$renderMap(poseStack, buffer, combinedLight, stack);
            Matrix4f particleRoot = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
            ModelPartsParticleSystem.setRenderRoot(arm, particleRoot);
            ModelPartsParticleSystem.setExactInHandGhostRoot(arm, particleRoot);
            ModelPartsParticleSystem.emitDefinitionForItemRender(class_310.method_1551(), arm, particleRoot, ToolTuning.getParticleEmitters(stack));
            ModelPartsParticleSystem.renderForArm(class_310.method_1551(), arm, buffer, poseStack, combinedLight);
            poseStack.method_22909();
            return;
        }
        PunchyArmRenderer.setForceVanillaDisplay(!PunchyArmRenderer.isSpearStack(stack));
        PunchyArmRenderer.setNeutralizeUseTransforms(usingItem);
        Matrix4f particleBaseRoot = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
        PunchyArmRenderer.setLastRenderedItemTransform(null);
        PunchyArmRenderer.setLastRenderedItemPose(null);
        try {
            PunchyArmRenderer.beginItemRendererCapture();
            handRenderer.method_3233((class_1309)player, stack, context, isLeft, poseStack, buffer, combinedLight);
        }
        finally {
            PunchyArmRenderer.endItemRendererCapture();
            PunchyArmRenderer.setForceVanillaDisplay(false);
            PunchyArmRenderer.setNeutralizeUseTransforms(prevNeutralize);
        }
        Matrix4f renderedItemPose = PunchyArmRenderer.consumeLastRenderedItemPose();
        if (renderedItemPose == null) {
            renderedItemPose = new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761());
        }
        Matrix4f particleRoot = PunchyArmRenderer.resolveVanillaItemParticleRoot(player, stack, context, particleBaseRoot, renderedItemPose, isLeft);
        ModelPartsParticleSystem.setRenderRoot(arm, particleRoot);
        ModelPartsParticleSystem.setExactInHandGhostRoot(arm, particleRoot);
        ModelPartsParticleSystem.emitDefinitionForItemRender(class_310.method_1551(), arm, particleRoot, ToolTuning.getParticleEmitters(stack));
        ModelPartsParticleSystem.renderForArm(class_310.method_1551(), arm, buffer, poseStack, combinedLight);
        poseStack.method_22909();
    }

    private static boolean isSpearStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_7909() instanceof class_1835) {
            return true;
        }
        return stack.method_31573(SPEAR_TAG);
    }

    private static Matrix4f resolveVanillaItemParticleRoot(class_746 player, class_1799 stack, class_811 context, Matrix4f baseRoot, Matrix4f renderedPose, boolean isLeft) {
        if (!(renderedPose == null || baseRoot != null && renderedPose.equals((Matrix4fc)baseRoot, 1.0E-5f))) {
            return new Matrix4f((Matrix4fc)renderedPose);
        }
        Matrix4f resolved = baseRoot == null ? new Matrix4f() : new Matrix4f((Matrix4fc)baseRoot);
        class_804 displayTransform = PunchyArmRenderer.consumeLastRenderedItemTransform();
        if (displayTransform == null) {
            displayTransform = PunchyArmRenderer.resolveBaselineUseTransform(stack, context, (class_1309)player);
        }
        if (displayTransform == null) {
            return resolved;
        }
        class_4587 tmp = new class_4587();
        tmp.method_23760().method_23761().set((Matrix4fc)resolved);
        PunchyArmRenderer.applyItemTransformToPose(displayTransform, isLeft, tmp);
        return new Matrix4f((Matrix4fc)tmp.method_23760().method_23761());
    }

    private static void applyItemTransformToPose(class_804 transform, boolean isLeft, class_4587 poseStack) {
        if (transform == null || poseStack == null) {
            return;
        }
        Vector3f rotation = transform.field_4287;
        Vector3f translation = transform.field_4286;
        Vector3f scale = transform.field_4285;
        if (rotation == null || translation == null || scale == null) {
            return;
        }
        float side = isLeft ? -1.0f : 1.0f;
        poseStack.method_46416(side * translation.x() / 16.0f, translation.y() / 16.0f, translation.z() / 16.0f);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(rotation.x()));
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(side * rotation.y()));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(side * rotation.z()));
        poseStack.method_22905(scale.x(), scale.y(), scale.z());
    }

    private static boolean isFunctionalMap(class_746 player, class_1799 stack) {
        if (player == null || stack == null || stack.method_7960()) {
            return false;
        }
        return class_1806.method_8001((class_1799)stack, (class_1937)player.method_37908()) != null;
    }

    private static boolean isBlacklistedForArm(class_746 player, class_1306 arm) {
        boolean mainRight;
        if (player == null || arm == null) {
            return false;
        }
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        class_1268 hand = arm == class_1306.field_6183 ? (mainRight ? class_1268.field_5808 : class_1268.field_5810) : (mainRight ? class_1268.field_5810 : class_1268.field_5808);
        return PunchyConfig.isHandBlacklisted((class_1657)player, hand);
    }

    public static void setFirstPersonHidden(boolean hidden) {
        FIRSTPERSON_HIDE = hidden;
    }

    public static boolean shouldHideAboveInterface(class_310 mc) {
        String screenClass;
        boolean hideRequestedByTimeline;
        if (mc == null) {
            return false;
        }
        boolean hideRequestedByItem = PunchyArmRenderer.isExposureHideConfigured((class_742)mc.field_1724);
        boolean bl = hideRequestedByTimeline = PunchyArmRenderer.isInterfaceHideEffectActive(PunchyAnimationManager.POSE_HANDLER.getCurrentClip(), PunchyAnimationManager.POSE_HANDLER.isPlaying(), PunchyAnimationManager.POSE_HANDLER.getCurrentTime()) || PunchyArmRenderer.isInterfaceHideEffectActive(PunchyAnimationManager.POSE_HANDLER.getSecondaryClip(), PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle(), PunchyAnimationManager.POSE_HANDLER.getSecondaryTime()) || PunchyArmRenderer.isInterfaceHideEffectActive(PunchyAnimationManager.SWIM_HANDLER.getCurrentClip(), PunchyAnimationManager.SWIM_HANDLER.isPlaying(), PunchyAnimationManager.SWIM_HANDLER.getCurrentTime());
        if (!hideRequestedByItem && !hideRequestedByTimeline) {
            return false;
        }
        if (mc.field_1755 != null && EXPOSURE_INTERFACE_SCREEN_NAMES.contains(screenClass = mc.field_1755.getClass().getSimpleName())) {
            return true;
        }
        return PunchyArmRenderer.isExposureInterfaceActive(mc);
    }

    private static boolean isExposureHideConfigured(class_742 player) {
        if (player == null) {
            return false;
        }
        return ToolTuning.shouldHideAboveInterface(player.method_6047()) || ToolTuning.shouldHideAboveInterface(player.method_6079());
    }

    public static boolean isExposureInterfaceActive(class_310 mc) {
        String screenClass;
        if (mc == null) {
            return false;
        }
        if (mc.field_1755 != null && EXPOSURE_INTERFACE_SCREEN_NAMES.contains(screenClass = mc.field_1755.getClass().getSimpleName())) {
            return true;
        }
        return PunchyArmRenderer.isExposureViewfinderVisible();
    }

    private static boolean isInterfaceHideEffectActive(AnimationClip clip, boolean active, float time) {
        return clip != null && active && clip.hasEffectAny(HIDE_ABOVE_INTERFACE_EFFECT) && clip.isEffectActive(HIDE_ABOVE_INTERFACE_EFFECT, time);
    }

    private static boolean isExposureViewfinderActive() {
        if (!exposureCameraClientLookupDone) {
            exposureCameraClientLookupDone = true;
            try {
                Class<?> cameraClientClass = Class.forName(EXPOSURE_CAMERA_CLIENT_CLASS);
                exposureCameraClientIsActiveMethod = cameraClientClass.getMethod("isActive", new Class[0]);
                exposureCameraClientViewfinderMethod = cameraClientClass.getMethod("viewfinder", new Class[0]);
            }
            catch (Throwable ignored) {
                exposureCameraClientIsActiveMethod = null;
                exposureCameraClientViewfinderMethod = null;
            }
        }
        if (exposureCameraClientIsActiveMethod == null) {
            return false;
        }
        try {
            Object active = exposureCameraClientIsActiveMethod.invoke(null, new Object[0]);
            return active instanceof Boolean && (Boolean)active != false;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isExposureViewfinderVisible() {
        if (!exposureCameraClientLookupDone) {
            exposureCameraClientLookupDone = true;
            try {
                Class<?> cameraClientClass = Class.forName(EXPOSURE_CAMERA_CLIENT_CLASS);
                exposureCameraClientIsActiveMethod = cameraClientClass.getMethod("isActive", new Class[0]);
                exposureCameraClientViewfinderMethod = cameraClientClass.getMethod("viewfinder", new Class[0]);
            }
            catch (Throwable ignored) {
                exposureCameraClientIsActiveMethod = null;
                exposureCameraClientViewfinderMethod = null;
            }
        }
        if (exposureCameraClientViewfinderMethod == null) {
            return false;
        }
        try {
            return exposureCameraClientViewfinderMethod.invoke(null, new Object[0]) != null;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isUsingSpyglass(class_742 player) {
        class_1799 useStack;
        if (player == null) {
            return false;
        }
        if (player.method_6115() && (useStack = player.method_6030()) != null && useStack.method_7909() instanceof class_5538) {
            return true;
        }
        return player.method_31550();
    }

    private static boolean shouldForceBlockTuning(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (PunchyConfig.disableBoatMinecartRaftModels() && (BoatInHandRenderer.isBoatStack(stack) || MinecartInHandRenderer.isMinecartStack(stack))) {
            return true;
        }
        if (PunchyConfig.disableChestModels() && ChestInHandRenderer.isChestStack(stack)) {
            return true;
        }
        if (PunchyConfig.disablePistonModels() && (stack.method_31574(class_1802.field_8249) || stack.method_31574(class_1802.field_8105))) {
            return true;
        }
        return PunchyConfig.disableEnchantingTableModels() && stack.method_31574(class_1802.field_8657);
    }

    private static boolean isShaderPackInUse() {
        long now = System.nanoTime();
        if (shaderPackInUseCached != null && now - shaderPackInUseLastCheckNanos < 250000000L) {
            return shaderPackInUseCached;
        }
        Boolean irisState = PunchyArmRenderer.queryIrisShaderPackInUse();
        boolean active = irisState != null && irisState != false;
        shaderPackInUseCached = active;
        shaderPackInUseLastCheckNanos = now;
        return active;
    }

    private static Boolean queryIrisShaderPackInUse() {
        return IrisShaderPackCompat.queryShaderPackInUse();
    }

    private static void renderFlameOnArm(class_4587 poseStack, class_4597 buffer) {
        if (poseStack == null || buffer == null) {
            return;
        }
        class_1058 sprite = class_310.method_1551().method_1554().method_24153(FIRE_ATLAS_ID).method_4608(FIRE_SPRITE_ID);
        class_4588 consumer = buffer.getBuffer(class_1921.method_23580((class_2960)FIRE_ATLAS_TEXTURE));
        float[] segments = new float[]{-0.02f, -0.22f, -0.42f, -0.62f};
        float outerRadius = 0.06f;
        float innerRadius = 0.02f;
        float forwardBias = 0.56f;
        float heightBias = -0.04f;
        float lateralBias = 0.004f;
        float[] angles = new float[]{0.0f, 1.5707964f, (float)Math.PI, 4.712389f};
        poseStack.method_22903();
        poseStack.method_46416(lateralBias, forwardBias, heightBias);
        Quaternionf baseRotation = new Quaternionf().rotateX(-1.5707964f);
        for (float y : segments) {
            for (float angle : angles) {
                float x = (float)Math.cos(angle) * outerRadius;
                float z = (float)Math.sin(angle) * outerRadius;
                Quaternionf rotation = new Quaternionf((Quaternionfc)baseRotation).rotateY(angle);
                poseStack.method_22903();
                poseStack.method_46416(x, y, z);
                poseStack.method_22907(rotation);
                PunchyArmRenderer.renderArmFlameQuad(poseStack, consumer, sprite, 0.24f, 0.32f, 0.95f);
                poseStack.method_22909();
            }
            poseStack.method_22903();
            poseStack.method_46416(0.0f, y, -innerRadius);
            poseStack.method_22907(baseRotation);
            PunchyArmRenderer.renderArmFlameQuad(poseStack, consumer, sprite, 0.18f, 0.28f, 0.7f);
            poseStack.method_22909();
        }
        poseStack.method_22909();
    }

    private static void renderArmFlameQuad(class_4587 poseStack, class_4588 consumer, class_1058 sprite, float width, float height, float alpha) {
        class_4587.class_4665 pose = poseStack.method_23760();
        float halfW = width * 0.5f;
        float top = height * 0.5f;
        float bottom = -height * 0.5f;
        int packedLight = 0xF000F0;
        float u0 = sprite.method_4594();
        float u1 = sprite.method_4577();
        float v0 = sprite.method_4593();
        float v1 = sprite.method_4575();
        PunchyArmRenderer.addVertexAlpha(pose, consumer, -halfW, top, 0.0f, u0, v0, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, halfW, top, 0.0f, u1, v0, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, halfW, bottom, 0.0f, u1, v1, packedLight, 0.0f, 0.0f, 1.0f, alpha);
        PunchyArmRenderer.addVertexAlpha(pose, consumer, -halfW, bottom, 0.0f, u0, v1, packedLight, 0.0f, 0.0f, 1.0f, alpha);
    }

    private static Vector3f getGripPos(class_630 geoGrip, class_630 geoItem, boolean leftArm) {
        String gripName = leftArm ? "itemgrip_left" : "itemgrip_right";
        Vector3f bbLocal = SimpleBedrockGeo.bedrockLocalPivots.get(gripName);
        if (bbLocal == null) {
            String aliasGripName = leftArm ? "item_grip_left" : "item_grip_right";
            bbLocal = SimpleBedrockGeo.bedrockLocalPivots.get(aliasGripName);
        }
        if (bbLocal != null) {
            return PunchyArmRenderer.convertPivot(bbLocal).mul(0.0625f);
        }
        return new Vector3f(-geoGrip.field_3657, -geoGrip.field_3656, geoGrip.field_3655).mul(0.0625f);
    }

    private static class_630 resolveGripPart(String primaryName, String aliasName) {
        class_630 primary = SimpleBedrockGeo.loadedParts.get(primaryName);
        if (primary != null) {
            return primary;
        }
        return SimpleBedrockGeo.loadedParts.get(aliasName);
    }

    private static Vector3f convertPivot(Vector3f bb) {
        return new Vector3f(-bb.x, bb.z, bb.y);
    }

    private static Vector3f getTuningPivot(ToolTuning.Profile tuning, boolean isLeft) {
        if (tuning == null) {
            return new Vector3f();
        }
        float pivotBaseX = isLeft ? 0.04f : -0.04f;
        return new Vector3f(tuning.pivotX + pivotBaseX, tuning.pivotY + -0.14f, tuning.pivotZ + 0.11f);
    }

    private static void applyPivotMarkerTransform(class_4587 poseStack, ToolTuning.Profile tuning, boolean isLeft) {
        if (tuning == null) {
            return;
        }
        poseStack.method_22905(tuning.scale, tuning.scale, tuning.scale);
        Vector3f pivot = PunchyArmRenderer.getTuningPivot(tuning, isLeft);
        pivot.add((Vector3fc)DEBUG_PIVOT_NUDGE);
        poseStack.method_46416(pivot.x, pivot.y, pivot.z);
    }

    private static class_1268 resolveHand(class_746 player, class_1306 arm) {
        boolean mainRight;
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        if (arm == class_1306.field_6183) {
            return mainRight ? class_1268.field_5808 : class_1268.field_5810;
        }
        return mainRight ? class_1268.field_5810 : class_1268.field_5808;
    }

    private static class_1799 getStackForArm(class_742 player, class_1306 arm) {
        boolean mainRight;
        if (player == null || arm == null) {
            return class_1799.field_8037;
        }
        boolean bl = mainRight = player.method_6068() == class_1306.field_6183;
        if (arm == class_1306.field_6183) {
            return mainRight ? player.method_6047() : player.method_6079();
        }
        return mainRight ? player.method_6079() : player.method_6047();
    }

    private static void applyArmMeshOffsets(class_4587 poseStack, boolean leftArm) {
        ArmMeshTuning.Profile profile = ArmMeshTuning.getGlobal(leftArm);
        if (profile == null) {
            return;
        }
        poseStack.method_46416(profile.offX, profile.offY, profile.offZ);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(profile.rotX));
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(profile.rotY));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(profile.rotZ));
        poseStack.method_22905(profile.scale, profile.scale, profile.scale);
    }

    private static void renderPivotMarker(class_4597 buffer, class_4587 poseStack) {
    }

    private static void renderFace(Matrix4f pose, class_4588 consumer, class_2350 direction, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        switch (direction) {
            case field_11033: {
                PunchyArmRenderer.addVertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y1, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y1, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y1, z2, red, green, blue, alpha);
                break;
            }
            case field_11036: {
                PunchyArmRenderer.addVertex(consumer, pose, x1, y2, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y2, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y2, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y2, z1, red, green, blue, alpha);
                break;
            }
            case field_11043: {
                PunchyArmRenderer.addVertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y2, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y2, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y1, z1, red, green, blue, alpha);
                break;
            }
            case field_11035: {
                PunchyArmRenderer.addVertex(consumer, pose, x1, y1, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y1, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y2, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y2, z2, red, green, blue, alpha);
                break;
            }
            case field_11039: {
                PunchyArmRenderer.addVertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y1, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y2, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x1, y2, z1, red, green, blue, alpha);
                break;
            }
            case field_11034: {
                PunchyArmRenderer.addVertex(consumer, pose, x2, y1, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y2, z1, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y2, z2, red, green, blue, alpha);
                PunchyArmRenderer.addVertex(consumer, pose, x2, y1, z2, red, green, blue, alpha);
            }
        }
    }

    private static void addVertex(class_4588 consumer, Matrix4f pose, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.method_22918(pose, x, y, z).method_22915(red, green, blue, alpha);
    }

    private static void resetPlayerModelPose(class_591 model) {
        if (model == null) {
            return;
        }
        model.field_3398.method_41923();
        model.field_3394.method_41923();
        model.field_3391.method_41923();
        model.field_3401.method_41923();
        model.field_27433.method_41923();
        model.field_3392.method_41923();
        model.field_3397.method_41923();
        model.field_3483.method_41923();
        model.field_3486.method_41923();
        model.field_3484.method_41923();
        model.field_3479.method_41923();
        model.field_3482.method_41923();
    }

    private static class_2960 getPlayerTexture(class_742 player) {
        class_2960 texture = class_1068.method_4649();
        try {
            if (player.method_52814() != null && player.method_52814().comp_1626() != null) {
                texture = player.method_52814().comp_1626();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return texture;
    }

    private static class_591 getFirstPersonModel(boolean slim) {
        if (slim) {
            if (fpModelSlim == null) {
                class_630 root = class_310.method_1551().method_31974().method_32072(class_5602.field_27581);
                fpModelSlim = new class_591(root, true);
            }
            return fpModelSlim;
        }
        if (fpModelDefault == null) {
            class_630 root = class_310.method_1551().method_31974().method_32072(class_5602.field_27577);
            fpModelDefault = new class_591(root, false);
        }
        return fpModelDefault;
    }

    private static boolean isSlim(class_742 player) {
        if (player == null) {
            return false;
        }
        Boolean rendererSlim = PunchyArmRenderer.resolveSlimFromRendererModel(player);
        if (rendererSlim != null) {
            PunchyArmRenderer.rememberSlimResolution(player, rendererSlim);
            return rendererSlim;
        }
        try {
            class_8685 skin = player.method_52814();
            if (skin == null || skin.comp_1629() == null) {
                return PunchyArmRenderer.getCachedSlimResolution(player);
            }
            class_8685.class_7920 model = skin.comp_1629();
            if (PunchyArmRenderer.isSlimModelValue(model)) {
                PunchyArmRenderer.rememberSlimResolution(player, true);
                return true;
            }
            PunchyArmRenderer.rememberSlimResolution(player, false);
            return false;
        }
        catch (Throwable skin) {
            try {
                Method getModelName = player.getClass().getMethod("getModelName", new Class[0]);
                Object modelName = getModelName.invoke((Object)player, new Object[0]);
                if (PunchyArmRenderer.isSlimModelValue(modelName)) {
                    PunchyArmRenderer.rememberSlimResolution(player, true);
                    return true;
                }
                PunchyArmRenderer.rememberSlimResolution(player, false);
                return false;
            }
            catch (Throwable ignored) {
                return PunchyArmRenderer.getCachedSlimResolution(player);
            }
        }
    }

    private static void rememberSlimResolution(class_742 player, boolean slim) {
        if (player == null || player.method_5667() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        SLIM_RESOLUTION_CACHE.put(player.method_5667(), new SlimResolutionState(slim, now));
        if (SLIM_RESOLUTION_CACHE.size() > 256) {
            PunchyArmRenderer.pruneSlimResolutionCache(now);
        }
    }

    private static boolean getCachedSlimResolution(class_742 player) {
        if (player == null || player.method_5667() == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        SlimResolutionState cached = SLIM_RESOLUTION_CACHE.get(player.method_5667());
        if (cached == null) {
            return false;
        }
        if (now - cached.updatedAtMs > 15000L) {
            SLIM_RESOLUTION_CACHE.remove(player.method_5667());
            return false;
        }
        return cached.slim;
    }

    private static void pruneSlimResolutionCache(long nowMs) {
        Iterator<Map.Entry<UUID, SlimResolutionState>> it = SLIM_RESOLUTION_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, SlimResolutionState> entry = it.next();
            if (nowMs - entry.getValue().updatedAtMs <= 15000L) continue;
            it.remove();
        }
    }

    private static Boolean resolveSlimFromRendererModel(class_742 player) {
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || mc.method_1561() == null) {
                return null;
            }
            class_897 renderer = mc.method_1561().method_3953((class_1297)player);
            if (!(renderer instanceof class_922)) {
                return null;
            }
            class_922 livingRenderer = (class_922)renderer;
            class_583 modelObj = livingRenderer.method_4038();
            if (!(modelObj instanceof class_591)) {
                return null;
            }
            class_591 playerModel = (class_591)modelObj;
            return ((PlayerModelAccessor)playerModel).punchy$isSlim();
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isSlimModelValue(Object modelValue) {
        if (modelValue == null) {
            return false;
        }
        if (modelValue instanceof CharSequence) {
            CharSequence text = (CharSequence)modelValue;
            return "slim".equalsIgnoreCase(text.toString());
        }
        if (modelValue instanceof Enum) {
            Enum enumValue = (Enum)modelValue;
            return "SLIM".equalsIgnoreCase(enumValue.name()) || "slim".equalsIgnoreCase(enumValue.toString());
        }
        String value = String.valueOf(modelValue);
        return "slim".equalsIgnoreCase(value) || value.endsWith(".SLIM");
    }

    private static void applyCameraAnimationCompensation(class_4587 poseStack) {
        if (poseStack == null || !CameraAnimationState.isActive()) {
            return;
        }
        Vector3f offset = CameraAnimationState.offset();
        Quaternionf rotation = CameraAnimationState.rotation();
        if (Math.abs(rotation.w - 1.0f) > 1.0E-4f || rotation.x != 0.0f || rotation.y != 0.0f || rotation.z != 0.0f) {
            Quaternionf inverse = new Quaternionf((Quaternionfc)rotation).invert();
            Vector3f euler = inverse.getEulerAnglesZYX(new Vector3f());
            euler.x = -euler.x;
            euler.y = -euler.y;
            inverse.rotationZYX(euler.z, euler.y, euler.x);
            poseStack.method_22907(inverse);
        }
        if (offset.lengthSquared() > 0.0f) {
            poseStack.method_46416(offset.x, offset.y, -offset.z);
        }
    }

    private static void applyLoweredOffhandPose(class_746 player, class_591 model, class_1306 offArm) {
        if (player == null || model == null || offArm == null) {
            return;
        }
        class_630 armPart = offArm == class_1306.field_6182 ? model.field_27433 : model.field_3401;
        class_630 class_6302 = armPart;
        if (!(class_6302 instanceof TransformablePart)) {
            return;
        }
        TransformablePart tArm = (TransformablePart)class_6302;
        Matrix4f world = tArm.punchy$getExplicitTransform();
        if (world == null) {
            return;
        }
        float side = offArm == class_1306.field_6182 ? -1.0f : 1.0f;
        Matrix4f lowered = new Matrix4f().translate(side * 0.0f, 0.0f, 0.0f).rotateX((float)Math.toRadians(90.0));
        world.mul((Matrix4fc)lowered);
        tArm.punchy$setExplicitTransform(world);
    }

    static {
        DEBUG_PIVOT_STACK = new class_1799((class_1935)class_1802.field_8197);
        DEBUG_GRIP_STACK = new class_1799((class_1935)class_1802.field_8737);
        DEBUG_PIVOT_NUDGE = new Vector3f(0.0f, 0.0f, 0.13f);
        FREEZE_ARM_STAGE_1 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_freeze_tip_1.png");
        FREEZE_ARM_STAGE_2 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_freeze_tip_2.png");
        FREEZE_ARM_STAGE_3 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_freeze_tip_3.png");
        MUD_ARM_STAGE_1 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_mud_tip_1.png");
        MUD_ARM_STAGE_2 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_mud_tip_2.png");
        MUD_ARM_STAGE_3 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_mud_tip_3.png");
        SWEAT_ARM_STAGE_1 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_sweat_tip_1.png");
        SWEAT_ARM_STAGE_2 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_sweat_tip_2.png");
        SWEAT_ARM_STAGE_3 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_sweat_tip_3.png");
        SWEAT_ARM_STAGE_4 = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_sweat_tip_4.png");
        BURN_ARM_HAND_OVERLAY = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_burn_hand_overlay.png");
        FIRE_ATLAS_ID = class_1059.field_5275;
        FIRE_ATLAS_TEXTURE = class_1059.field_5275;
        FIRE_SPRITE_ID = class_2960.method_60655((String)"minecraft", (String)"block/fire_1");
        BURN_ARM_HAND_OVERLAY_LEGACY = class_2960.method_60655((String)"punchy", (String)"textures/misc/arm_burn_hand_overlay_legacy.png");
        FREEZE_MASK_CACHE = new HashMap<String, FreezeVoxelMask>();
        SKIN_HEIGHT_CACHE = new HashMap<class_2960, Integer>();
        SLIM_RESOLUTION_CACHE = new HashMap<UUID, SlimResolutionState>();
        offhandLowerExitDelayFrames = 0;
        offhandHideAfterLowerFrames = 0;
        lastOffhandEmptyForLower = true;
        offhandAwaitHandInFrames = 0;
        lastOffhandHasItemForLower = false;
        offhandAwaitHandOutFrames = 0;
        lastDualHandedForHide = false;
        hideOffhandAfterDualExit = false;
        hideOffhandAfterDualFrames = 0;
        keepEmptyOffhandVisibleForFlight = false;
        flightOffhandLandingHideRequested = false;
        ARM_WORLD_CACHE = new EnumMap(class_1306.class);
        DRY_OUT_ARM_TUNING_BLEND = new EnumMap(class_1306.class);
        ARM_ROTATION_RESET_PROGRESS = new EnumMap(class_1306.class);
        BURN_FADE_STATE = new EnumMap(class_1306.class);
        lastArmCacheTick = -1L;
        FORCE_VANILLA_DISPLAY = ThreadLocal.withInitial(() -> false);
        NEUTRALIZE_USE_TRANSFORMS = ThreadLocal.withInitial(() -> false);
        ITEM_RENDER_CAPTURE_DEPTH = ThreadLocal.withInitial(() -> 0);
        SPEAR_TAG = class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)class_2960.method_60655((String)"minecraft", (String)"spears"));
        VANILLA_THIRD_PERSON_CACHE = new HashMap<class_1792, EnumMap<class_811, class_804>>();
        IDLE_USE_TRANSFORM_CACHE = new HashMap<class_1792, EnumMap<class_811, class_804>>();
        BASELINE_USE_TRANSFORM = new ThreadLocal();
        LAST_RENDERED_ITEM_TRANSFORM = new ThreadLocal();
        LAST_RENDERED_ITEM_POSE = new ThreadLocal();
        EXPOSURE_INTERFACE_SCREEN_NAMES = Set.of("ViewfinderCameraControlsScreen", "CameraAttachmentsScreen", "InstantCameraControlsScreen");
    }

    private record FreezeVoxelMask(List<Pixel> pixels) {

        private record Pixel(int x, int y, float u0, float v0, float u1, float v1) {
        }
    }

    private static final class BurnFadeState {
        private float value = 0.0f;
        private float lastTime = -1.0f;

        private BurnFadeState() {
        }
    }

    private static final class SlimResolutionState {
        private final boolean slim;
        private final long updatedAtMs;

        private SlimResolutionState(boolean slim, long updatedAtMs) {
            this.slim = slim;
            this.updatedAtMs = updatedAtMs;
        }
    }
}

