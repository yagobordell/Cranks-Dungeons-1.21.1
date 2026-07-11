/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1690
 *  net.minecraft.class_1747
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1799
 *  net.minecraft.class_1835
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3486
 *  net.minecraft.class_3532
 *  net.minecraft.class_5819
 *  net.minecraft.class_746
 *  org.joml.Matrix4f
 */
package punchy.client.state;

import java.util.LinkedHashSet;
import java.util.Locale;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1690;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1835;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_5819;
import net.minecraft.class_746;
import org.joml.Matrix4f;
import punchy.client.PunchyClient;
import punchy.client.animation.Easing;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.SpearStateMachine;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public class MovementStateMachine {
    private static final MovementStateMachine INSTANCE = new MovementStateMachine();
    public static boolean APPLY_TO_ARMS = true;
    public static float WALK_SCALE = 1.0f;
    private static final boolean LOG_UNDERWATER_SINK_POSE = true;
    private static final float FORWARD_OFFSET = 1.6f;
    private static final float STRAFE_OFFSET = 1.5f;
    private static final float SMOOTH = 0.3f;
    public static float JUMP_IMPULSE = -2.8f;
    public static float LAND_IMPULSE = 2.4f;
    public static float JUMP_STIFFNESS = 75.0f;
    public static float JUMP_DAMPING = 16.0f;
    public static float JUMP_OFFSET_SCALE = 0.85f;
    public static float JUMP_ROT_SCALE = 1.65f;
    public static float CAMERA_TILT_SMOOTH = 0.45f;
    public static float CAMERA_YAW_TILT_Y_SCALE = 0.018f;
    public static float CAMERA_YAW_TILT_Z_SCALE = 0.005f;
    public static float CAMERA_YAW_ARM_TILT_Y_SCALE = 0.03264f;
    public static float CAMERA_YAW_ARM_TILT_MAX = (float)Math.toRadians(48.0);
    public static float CAMERA_YAW_ARM_TILT_STIFFNESS = 64.0f;
    public static float CAMERA_YAW_ARM_TILT_DAMPING = 15.0f;
    private static final float CAMERA_LOOK_REFERENCE_DT = 0.016666668f;
    private static final float CAMERA_LOOK_SPRING_STEP_DT = 0.008333334f;
    private static final float CAMERA_LOOK_MIN_DT = 1.0E-4f;
    public static float CAMERA_PITCH_OFFSET_SCALE = 0.01105f;
    public static float CAMERA_PITCH_TILT_X_SCALE = 0.027625f;
    public static float SPEAR_CHARGE_IMPACT_SCALE = 0.5f;
    public static float IMPACT_STIFFNESS = 60.0f;
    public static float IMPACT_DAMPING = 15.5f;
    public static float SHIELD_BLOCK_IMPACT_SCALE = 0.5f;
    public static float SHIELD_IMPACT_STIFFNESS = 60.0f;
    public static float SHIELD_IMPACT_DAMPING = 15.5f;
    public static float ACTION_ANIM_IMPULSE_SCALE = 0.5f;
    public static float ACTION_ANIM_STIFFNESS = 60.0f;
    public static float ACTION_ANIM_DAMPING = 15.5f;
    public static float IDLE_INTENSITY = 2.6f;
    public static float IDLE_SPEED = 2.0f;
    private static final float IDLE_BALANCE_INTENSITY_SCALE = 3.3f;
    private static final float IDLE_BALANCE_SPEED_BONUS = 0.0f;
    private static final float IDLE_BALANCE_DECAY = 1.0f;
    private static final float IDLE_BALANCE_RAMP = 0.55f;
    private static final float IDLE_BALANCE_JITTER_SCALE = 0.0078f;
    private static final float IDLE_BALANCE_JITTER_RAMP = 0.35f;
    private static final float IDLE_BALANCE_JITTER_MIN = 0.35f;
    private static final float IDLE_BALANCE_JITTER_MAX = 0.8f;
    private static final float SPRINT_BLEND_IN_TIME = 0.07f;
    private static final float SPRINT_BLEND_OUT_TIME = 0.35f;
    private static final float SPRINT_SPEED_SCALE = 1.6f;
    private static final float SPRINT_PHASE_SPEED = 6.63f;
    private static final float SPRINT_PHASE_SPEED_MAX = 9.69f;
    private static final float SPRINT_PITCH_AMP = 0.135f;
    private static final float SPRINT_PITCH_AMP_2 = 0.06f;
    private static final float SPRINT_YAW_AMP = 0.0375f;
    private static final float SPRINT_YAW_AMP_2 = 0.015f;
    private static final float SPRINT_ROLL_AMP = 0.0525f;
    private static final float SPRINT_ROLL_AMP_2 = 0.0225f;
    private static final float SPRINT_FWD_AMP = 3.6f;
    private static final float SPRINT_FWD_AMP_2 = 1.56f;
    private static final float SPRINT_FWD_PITCH_UP = 0.28f;
    private static final float SPRINT_INWARD_YAW = 0.045f;
    private static final float SPRINT_INWARD_ROLL = 0.03f;
    private static final float SPRINT_INWARD_YAW_STATIC = (float)Math.toRadians(25.0);
    private static final float SPRINT_UP_AMP = 0.12f;
    private static final float SPRINT_TILT_BOOST = 1.28f;
    private static final float WALK_BLEND_IN_TIME = 0.35f;
    private static final float WALK_BLEND_OUT_TIME = 0.6f;
    private static final float WALK_PHASE_SPEED = 5.31f;
    private static final float WALK_PHASE_SPEED_MAX = 8.13f;
    private static final float WALK_PITCH_AMP = 0.085f;
    private static final float WALK_PITCH_AMP_2 = 0.035f;
    private static final float WALK_YAW_AMP = 0.03f;
    private static final float WALK_YAW_AMP_2 = 0.012f;
    private static final float WALK_ROLL_AMP = 0.038f;
    private static final float WALK_ROLL_AMP_2 = 0.016f;
    private static final float WALK_SIDE_AMP = 1.85f;
    private static final float WALK_SIDE_UP_AMP = 0.11f;
    private static final float WALK_UP_AMP = 0.06f;
    private static final float WALK_INWARD_YAW = 0.022f;
    private static final float WALK_INWARD_ROLL = 0.018f;
    private static final float WALK_TILT_BOOST = 1.22f;
    private static final float FALL_ORBIT_BLEND_IN_TIME = 0.16f;
    private static final float FALL_ORBIT_BLEND_OUT_TIME = 0.2f;
    private static final float FALL_ORBIT_PHASE_SPEED = -5.4f;
    private static final float FALL_ORBIT_PHASE_SPEED_MAX = -8.8f;
    private static final float FALL_ORBIT_FWD_AMP = 1.17f;
    private static final float FALL_ORBIT_UP_AMP = 0.68f;
    private static final float FALL_ORBIT_PITCH_AMP = 0.14f;
    private static final float FALL_ORBIT_ROLL_AMP = 0.1f;
    private static final float FLIGHT_POSE_BLEND_IN_TIME = 0.52f;
    private static final float FLIGHT_POSE_BLEND_OUT_TIME = 0.24f;
    private static final float ELYTRA_POSE_SPEED_MIN = 0.55f;
    private static final float ELYTRA_POSE_SPEED_MAX = 1.75f;
    private static final float CREATIVE_POSE_SPEED_MIN = 0.16f;
    private static final float CREATIVE_POSE_SPEED_MAX = 0.85f;
    private static final float FLIGHT_POSE_BACK_HIGH_SCALE = 1.12f;
    private static final float FLIGHT_POSE_INWARD_DEGREES = 35.0f;
    private static final float FLIGHT_POSE_SIDE = 0.0f;
    private static final float FLIGHT_POSE_FORWARD = 5.8f;
    private static final float FLIGHT_POSE_UP = -0.55f;
    private static final float FLIGHT_POSE_LIFT = 0.0f;
    private static final float FLIGHT_POSE_SWAY_SPEED = 1.15f;
    private static final float FLIGHT_POSE_SWAY_SIDE_AMP = 0.12f;
    private static final float FLIGHT_POSE_SWAY_UP_AMP = 0.012f;
    private static final float UNDERWATER_SINK_POSE_BLEND_OUT_TIME = 0.26f;
    private static final float UNDERWATER_SINK_POSE_BLEND_IN_TIME = 2.25f;
    private static final float UNDERWATER_SINK_POSE_DESCEND_MIN = 0.0f;
    private static final float UNDERWATER_SINK_POSE_DESCEND_MAX = 0.08f;
    private static final float UNDERWATER_SINK_POSE_FORWARD = 4.6400003f;
    private static final float UNDERWATER_SINK_POSE_UP = 3.168f;
    private static final float UNDERWATER_SINK_POSE_SIDE = 0.6f;
    private static final float UNDERWATER_SINK_POSE_INWARD_YAW = (float)Math.toRadians(20.0);
    private static final float UNDERWATER_SINK_POSE_PITCH = -0.09f;
    private static final float UNDERWATER_SINK_POSE_ROLL = 0.03f;
    private static final String SYNC_WALK_ARMS_EFFECT = "sync_walk_arms";
    private static final float SYNC_WALK_MOTION_SCALE = 0.7f;
    private static final float SYNC_ROOT_MOTION_SCALE = 0.75f;
    private static final float SYNC_WALK_ROTATION_SCALE = 0.38f;
    private static final float SYNC_WALK_SIDE_SCALE = 1.35f;
    private static final float SYNC_WALK_UP_SCALE = 1.12f;
    private float offsetX;
    private float offsetZ;
    private float jumpValue;
    private float jumpVelocity;
    private float impactValue;
    private float impactVelocity;
    private class_1306 impactArm;
    private float shieldImpactValue;
    private float shieldImpactVelocity;
    private class_1306 shieldImpactArm;
    private final float[] actionAnimValue = new float[2];
    private final float[] actionAnimVelocity = new float[2];
    private final float[] actionAnimKick = new float[2];
    private boolean prevOnGround = true;
    private int airborneTicks;
    private float airborneMaxFallDistance;
    private double airborneMinVy = 0.0;
    private boolean airborneJumped;
    private boolean animGrounded;
    private long lastUpdateTime = System.nanoTime();
    private float lastCameraYaw = Float.NaN;
    private float lastCameraPitch = Float.NaN;
    private float cameraTiltY;
    private float cameraTiltZ;
    private float cameraArmTiltY;
    private float cameraArmTiltYVelocity;
    private float cameraPitchOffset;
    private float cameraPitchTiltX;
    private float cameraForwardX;
    private float cameraForwardY;
    private float cameraForwardZ;
    private float cameraRightX;
    private float cameraRightY;
    private float cameraRightZ;
    private float cameraUpX;
    private float cameraUpY;
    private float cameraUpZ;
    private float idleTime;
    private float idleWeight;
    private float idleBoostLeft;
    private float idleBoostRight;
    private float idleBoostLeftTarget;
    private float idleBoostRightTarget;
    private float idleJitterLeft;
    private float idleJitterRight;
    private float idleJitterLeftTarget;
    private float idleJitterRightTarget;
    private float idleJitterLeftTimer;
    private float idleJitterRightTimer;
    private final class_5819 idleJitterRandom = class_5819.method_43047();
    private final SneakSpringState sneakSpring = new SneakSpringState();
    private boolean suppressWalkSway;
    private float sprintBlend;
    private float sprintPhase;
    private float sprintPhaseVelocity;
    private float sprintTarget;
    private final float[] sprintRotX = new float[2];
    private final float[] sprintRotY = new float[2];
    private final float[] sprintRotZ = new float[2];
    private final float[] sprintOffFwd = new float[2];
    private final float[] sprintOffUp = new float[2];
    private final float[] sprintLateralGate = new float[]{1.0f, 1.0f};
    private float sprintSpeedFactor;
    private float sprintDt;
    private float walkBlend;
    private float walkPhase;
    private float walkPhaseVelocity;
    private final float[] walkRotX = new float[2];
    private final float[] walkRotY = new float[2];
    private final float[] walkRotZ = new float[2];
    private final float[] walkOffSide = new float[2];
    private final float[] walkOffUp = new float[2];
    private float walkSpeedFactor;
    private boolean isGrounded;
    private float fallOrbitBlend;
    private float fallOrbitPhase;
    private float fallOrbitPhaseVelocity;
    private float fallOrbitSpeedFactor;
    private float flightPoseBlend;
    private float underwaterSinkPoseBlend;
    private float lastLoggedUnderwaterSinkTarget = Float.NaN;
    private float lastLoggedUnderwaterSinkBlend = Float.NaN;
    private String lastLoggedUnderwaterSinkReason;
    private static final double MICRO_AIR_VEL_EPS = 0.1;
    private static final double VEL_IMPULSE_THRESHOLD = 0.1;
    private static final int MICRO_AIR_TICKS_MAX = 1;
    private static final float LAND_FALLDIST_THRESHOLD = 0.05f;
    private static final float AIR_FREEZE_FALLDIST_THRESHOLD = 3.0f;
    private static final float JUMP_LAND_TRANSITION_COOLDOWN_S = 0.08f;
    private float jumpLandTransitionCooldown;

    public static MovementStateMachine get() {
        return INSTANCE;
    }

    public void triggerSpearImpact(class_1306 arm) {
        this.impactArm = arm;
        this.impactVelocity += SPEAR_CHARGE_IMPACT_SCALE * 40.0f;
    }

    public void triggerShieldBlockImpact(class_1306 arm) {
        this.shieldImpactArm = arm;
        this.shieldImpactVelocity += SHIELD_BLOCK_IMPACT_SCALE * 20.0f;
    }

    public void triggerActionAnimation(class_1306 arm) {
        int idx;
        if (arm == null) {
            return;
        }
        int n = idx = arm == class_1306.field_6182 ? 0 : 1;
        this.actionAnimVelocity[n] = this.actionAnimVelocity[n] + ACTION_ANIM_IMPULSE_SCALE * 20.0f;
        int n2 = idx;
        this.actionAnimKick[n2] = this.actionAnimKick[n2] + 1.0f;
    }

    public void tick(class_310 client) {
    }

    public void updateFrame(class_746 player, float partialTicks) {
        long now = System.nanoTime();
        float dt = (float)(now - this.lastUpdateTime) / 1.0E9f;
        this.lastUpdateTime = now;
        if (dt <= 0.0f) {
            return;
        }
        if (dt > 0.033333335f) {
            dt = 0.033333335f;
        }
        float lerpFactor = 1.0f - (float)Math.pow(0.7f, dt * 20.0f);
        float camLerp = 1.0f - (float)Math.pow(1.0f - CAMERA_TILT_SMOOTH, dt * 20.0f);
        this.sprintDt = dt;
        float targetX = 0.0f;
        float targetZ = 0.0f;
        float movementForceMultiplier = 1.0f;
        if (player != null) {
            movementForceMultiplier = class_3532.method_15363((float)PunchyAnimationManager.resolveMovementForceMultiplier(), (float)0.0f, (float)1.0f);
            float forwardBack = class_3532.method_15363((float)player.field_6250, (float)-1.0f, (float)1.0f);
            float strafe = class_3532.method_15363((float)player.field_6212, (float)-1.0f, (float)1.0f);
            targetX = strafe * 1.5f * movementForceMultiplier;
            targetZ = -forwardBack * 1.6f * movementForceMultiplier;
            this.suppressWalkSway = player.method_5854() instanceof class_1690;
        } else {
            this.suppressWalkSway = false;
        }
        this.offsetX = class_3532.method_16439((float)lerpFactor, (float)this.offsetX, (float)(targetX *= WALK_SCALE));
        this.offsetZ = class_3532.method_16439((float)lerpFactor, (float)this.offsetZ, (float)(targetZ *= WALK_SCALE));
        if (player != null) {
            double vy;
            if (this.jumpLandTransitionCooldown > 0.0f) {
                this.jumpLandTransitionCooldown = Math.max(0.0f, this.jumpLandTransitionCooldown - dt);
            }
            float clampedPitch = class_3532.method_15363((float)player.method_36455(), (float)-89.5f, (float)89.5f);
            class_243 forward = class_243.method_1030((float)clampedPitch, (float)player.method_36454());
            this.cameraForwardX = (float)forward.field_1352;
            this.cameraForwardY = (float)forward.field_1351;
            this.cameraForwardZ = (float)forward.field_1350;
            class_243 worldUp = new class_243(0.0, 1.0, 0.0);
            class_243 right = forward.method_1036(worldUp);
            right = right.method_1027() < 1.0E-6 ? new class_243(1.0, 0.0, 0.0) : right.method_1029();
            class_243 up = right.method_1036(forward);
            up = up.method_1027() < 1.0E-6 ? worldUp : up.method_1029();
            this.cameraRightX = (float)right.field_1352;
            this.cameraRightY = (float)right.field_1351;
            this.cameraRightZ = (float)right.field_1350;
            this.cameraUpX = (float)up.field_1352;
            this.cameraUpY = (float)up.field_1351;
            this.cameraUpZ = (float)up.field_1350;
            boolean slimeBelow = MovementStateMachine.isStandingOnSlime(player);
            boolean onGround = player.method_24828();
            if (!onGround && slimeBelow && Math.abs(vy = player.method_18798().field_1351) <= 0.1 && player.field_6017 <= 0.0f) {
                onGround = true;
            }
            this.isGrounded = onGround;
            if (this.prevOnGround && !onGround) {
                float impulse;
                this.airborneTicks = 0;
                this.airborneMaxFallDistance = 0.0f;
                this.airborneMinVy = player.method_18798().field_1351;
                boolean jumped = class_310.method_1551().field_1690.field_1903.method_1434();
                this.airborneJumped = jumped || player.method_18798().field_1351 > 0.1;
                float f = impulse = jumped ? JUMP_IMPULSE : JUMP_IMPULSE * 0.5f;
                if ((!slimeBelow || jumped) && this.jumpLandTransitionCooldown <= 0.0f && (jumped || player.method_18798().field_1351 > 0.1)) {
                    this.jumpVelocity += impulse;
                    this.jumpLandTransitionCooldown = 0.08f;
                }
            } else if (!this.prevOnGround && onGround) {
                boolean realAir;
                boolean bl = realAir = this.airborneTicks > 1 || this.airborneMaxFallDistance > 0.05f || this.airborneMinVy < -0.1;
                if (realAir && this.jumpLandTransitionCooldown <= 0.0f) {
                    float landImpulse = LAND_IMPULSE;
                    if (slimeBelow) {
                        float f = class_3532.method_15363((float)(this.airborneMaxFallDistance / 1.0f), (float)0.0f, (float)1.0f);
                        landImpulse *= 0.35f + 0.65f * f;
                    }
                    this.jumpVelocity += landImpulse;
                    this.jumpLandTransitionCooldown = 0.08f;
                }
                this.airborneTicks = 0;
                this.airborneMaxFallDistance = 0.0f;
                this.airborneMinVy = 0.0;
                this.airborneJumped = false;
            }
            this.prevOnGround = onGround;
            if (!onGround) {
                ++this.airborneTicks;
                this.airborneMaxFallDistance = Math.max(this.airborneMaxFallDistance, player.field_6017);
                this.airborneMinVy = Math.min(this.airborneMinVy, player.method_18798().field_1351);
            }
            boolean allowAirAdvance = !this.isGrounded && !this.airborneJumped && this.airborneMaxFallDistance < 3.0f;
            this.animGrounded = this.isGrounded || allowAirAdvance;
            boolean submergedAirborne = this.isSubmergedWithoutGround(player);
            float fallOrbitTargetBlend = 0.0f;
            if (!(!PunchyConfig.enableFallArmAnimation() || onGround || submergedAirborne || player.method_6128() || this.suppressWalkSway)) {
                float downwardSpeed = (float)Math.max(0.0, -player.method_18798().field_1351);
                fallOrbitTargetBlend = class_3532.method_15363((float)((downwardSpeed - 0.04f) / 0.85f), (float)0.0f, (float)1.0f);
                if (player.field_6017 > 0.15f) {
                    fallOrbitTargetBlend = Math.max(fallOrbitTargetBlend, class_3532.method_15363((float)(player.field_6017 / 3.0f), (float)0.0f, (float)1.0f));
                }
            }
            float fallBlendTime = fallOrbitTargetBlend > this.fallOrbitBlend ? 0.16f : 0.2f;
            float fallBlendStep = fallBlendTime <= 0.0f ? 1.0f : dt / fallBlendTime;
            this.fallOrbitBlend = class_3532.method_15363((float)(this.fallOrbitBlend + (fallOrbitTargetBlend - this.fallOrbitBlend) * fallBlendStep), (float)0.0f, (float)1.0f);
            this.fallOrbitSpeedFactor = class_3532.method_16439((float)fallBlendStep, (float)this.fallOrbitSpeedFactor, (float)fallOrbitTargetBlend);
            if (this.fallOrbitBlend > 0.001f) {
                float targetFallPhase = class_3532.method_16439((float)this.fallOrbitSpeedFactor, (float)-5.4f, (float)-8.8f);
                float fallPhaseLerp = 1.0f - (float)Math.pow(0.74f, dt * 20.0f);
                this.fallOrbitPhaseVelocity = class_3532.method_16439((float)fallPhaseLerp, (float)this.fallOrbitPhaseVelocity, (float)targetFallPhase);
                this.fallOrbitPhase += this.fallOrbitPhaseVelocity * dt;
            } else {
                this.fallOrbitPhaseVelocity = 0.0f;
            }
            float flightPoseTargetBlend = this.resolveFlightPoseTargetBlend(player);
            float flightBlendTime = flightPoseTargetBlend > this.flightPoseBlend ? 0.52f : 0.24f;
            float flightBlendStep = flightBlendTime <= 0.0f ? 1.0f : dt / flightBlendTime;
            this.flightPoseBlend = class_3532.method_15363((float)(this.flightPoseBlend + (flightPoseTargetBlend - this.flightPoseBlend) * flightBlendStep), (float)0.0f, (float)1.0f);
            float underwaterSinkTargetBlend = this.resolveUnderwaterSinkPoseTargetBlend(player);
            float underwaterSinkBlendTime = underwaterSinkTargetBlend > this.underwaterSinkPoseBlend ? 2.25f : 0.26f;
            float underwaterSinkBlendStep = underwaterSinkBlendTime <= 0.0f ? 1.0f : dt / underwaterSinkBlendTime;
            this.underwaterSinkPoseBlend = MovementStateMachine.approach(this.underwaterSinkPoseBlend, underwaterSinkTargetBlend, underwaterSinkBlendStep);
            this.debugUnderwaterSinkPoseBlend(player, underwaterSinkTargetBlend);
            float yaw = player.method_36454();
            if (Float.isNaN(this.lastCameraYaw)) {
                this.lastCameraYaw = yaw;
            }
            float yawDelta = MovementStateMachine.normalizeLookDelta(class_3532.method_15393((float)(yaw - this.lastCameraYaw)), dt);
            this.lastCameraYaw = yaw;
            float targetTiltY = -yawDelta * CAMERA_YAW_TILT_Y_SCALE;
            this.cameraTiltY = class_3532.method_16439((float)camLerp, (float)this.cameraTiltY, (float)targetTiltY);
            float targetTiltZ = yawDelta * CAMERA_YAW_TILT_Z_SCALE;
            this.cameraTiltZ = class_3532.method_16439((float)camLerp, (float)this.cameraTiltZ, (float)targetTiltZ);
            float targetArmTiltY = class_3532.method_15363((float)(-yawDelta * CAMERA_YAW_ARM_TILT_Y_SCALE), (float)(-CAMERA_YAW_ARM_TILT_MAX), (float)CAMERA_YAW_ARM_TILT_MAX);
            this.stepCameraArmTiltSpring(targetArmTiltY, dt);
            float pitch = player.method_36455();
            if (Float.isNaN(this.lastCameraPitch)) {
                this.lastCameraPitch = pitch;
            }
            float pitchDelta = MovementStateMachine.normalizeLookDelta(pitch - this.lastCameraPitch, dt);
            this.lastCameraPitch = pitch;
            float targetPitchTiltX = -pitchDelta * CAMERA_PITCH_TILT_X_SCALE;
            this.cameraPitchTiltX = class_3532.method_16439((float)camLerp, (float)this.cameraPitchTiltX, (float)targetPitchTiltX);
            float targetPitchOffset = pitch * CAMERA_PITCH_OFFSET_SCALE;
            this.cameraPitchOffset = class_3532.method_16439((float)camLerp, (float)this.cameraPitchOffset, (float)targetPitchOffset);
            float moveMag = class_3532.method_15355((float)(targetZ * targetZ + targetX * targetX));
            float idleTarget = 1.0f - class_3532.method_15363((float)(moveMag / 0.2f), (float)0.0f, (float)1.0f);
            this.idleWeight = class_3532.method_16439((float)camLerp, (float)this.idleWeight, (float)idleTarget);
            boolean sneaking = player.method_18276() || player.method_5715();
            this.sneakSpring.setSneaking(sneaking);
            if (this.animGrounded && !submergedAirborne) {
                float walkBlendStep;
                float walkAnim;
                float blendStep;
                float targetSprint;
                boolean sprintAllowed = this.shouldApplySprintSwing(player);
                float walkSpeed = player.field_42108.method_48566();
                this.sprintTarget = targetSprint = sprintAllowed ? 1.0f : 0.0f;
                float blendTime = targetSprint > this.sprintBlend ? 0.07f : 0.35f;
                float f = blendStep = blendTime <= 0.0f ? 1.0f : dt / blendTime;
                if (targetSprint < this.sprintBlend) {
                    blendStep *= blendStep;
                }
                this.sprintBlend = class_3532.method_15363((float)(this.sprintBlend + (targetSprint - this.sprintBlend) * blendStep), (float)0.0f, (float)1.0f);
                float targetSprintSpeed = sprintAllowed ? class_3532.method_15363((float)(walkSpeed * 1.6f), (float)0.0f, (float)1.0f) : 0.0f;
                float sprintSpeedLerp = blendTime <= 0.0f ? 1.0f : dt / blendTime;
                this.sprintSpeedFactor = class_3532.method_16439((float)sprintSpeedLerp, (float)this.sprintSpeedFactor, (float)targetSprintSpeed);
                float targetPhaseSpeed = class_3532.method_16439((float)this.sprintSpeedFactor, (float)6.63f, (float)9.69f);
                float sprintSpeedAdjust = PunchyConfig.getSprintArmSwingSpeed();
                float speedScale = 1.0f + sprintSpeedAdjust;
                if (speedScale < 0.0f) {
                    speedScale = 0.0f;
                }
                float phaseLerp = 1.0f - (float)Math.pow(0.75, dt * 20.0f);
                this.sprintPhaseVelocity = class_3532.method_16439((float)phaseLerp, (float)this.sprintPhaseVelocity, (float)(targetPhaseSpeed *= speedScale));
                this.sprintPhase += this.sprintPhaseVelocity * dt;
                float inputMag = class_3532.method_15355((float)(player.field_6250 * player.field_6250 + player.field_6212 * player.field_6212));
                float moveFactor = class_3532.method_15363((float)Math.max(inputMag, walkAnim = player.field_42108.method_48566()), (float)0.0f, (float)1.0f) * movementForceMultiplier;
                boolean walkAllowed = this.shouldApplyWalkSwing(player, moveFactor);
                float targetWalk = walkAllowed ? 1.0f : 0.0f;
                float walkBlendTime = targetWalk > this.walkBlend ? 0.35f : 0.6f;
                float f2 = walkBlendStep = walkBlendTime <= 0.0f ? 1.0f : dt / walkBlendTime;
                if (targetWalk < this.walkBlend) {
                    walkBlendStep *= walkBlendStep;
                }
                this.walkBlend = class_3532.method_15363((float)(this.walkBlend + (targetWalk - this.walkBlend) * walkBlendStep), (float)0.0f, (float)1.0f);
                float walkSpeedLerp = walkBlendTime <= 0.0f ? 1.0f : dt / walkBlendTime;
                float targetWalkSpeedFactor = walkAllowed ? moveFactor : 0.0f;
                this.walkSpeedFactor = class_3532.method_16439((float)walkSpeedLerp, (float)this.walkSpeedFactor, (float)targetWalkSpeedFactor);
                float walkTargetPhase = class_3532.method_16439((float)this.walkSpeedFactor, (float)5.31f, (float)8.13f);
                float walkPhaseLerp = 1.0f - (float)Math.pow(0.75, dt * 20.0f);
                this.walkPhaseVelocity = class_3532.method_16439((float)walkPhaseLerp, (float)this.walkPhaseVelocity, (float)walkTargetPhase);
                this.walkPhase += this.walkPhaseVelocity * dt;
                if (this.walkBlend <= 0.001f && !walkAllowed) {
                    this.walkBlend = 0.0f;
                    this.walkPhaseVelocity = 0.0f;
                    this.walkRotX[0] = 0.0f;
                    this.walkRotX[1] = 0.0f;
                    this.walkRotY[0] = 0.0f;
                    this.walkRotY[1] = 0.0f;
                    this.walkRotZ[0] = 0.0f;
                    this.walkRotZ[1] = 0.0f;
                    this.walkOffSide[0] = 0.0f;
                    this.walkOffSide[1] = 0.0f;
                    this.walkOffUp[0] = 0.0f;
                    this.walkOffUp[1] = 0.0f;
                }
            } else if (submergedAirborne) {
                float fadeStep = class_3532.method_15363((float)(dt / 0.22f), (float)0.0f, (float)1.0f);
                this.sprintTarget = 0.0f;
                this.sprintBlend = class_3532.method_16439((float)fadeStep, (float)this.sprintBlend, (float)0.0f);
                this.sprintSpeedFactor = class_3532.method_16439((float)fadeStep, (float)this.sprintSpeedFactor, (float)0.0f);
                this.walkBlend = class_3532.method_16439((float)fadeStep, (float)this.walkBlend, (float)0.0f);
                this.walkSpeedFactor = class_3532.method_16439((float)fadeStep, (float)this.walkSpeedFactor, (float)0.0f);
                if (this.sprintBlend <= 0.001f) {
                    this.sprintBlend = 0.0f;
                    this.sprintPhaseVelocity = 0.0f;
                }
                if (this.walkBlend <= 0.001f) {
                    this.walkBlend = 0.0f;
                    this.walkPhaseVelocity = 0.0f;
                }
            }
        } else {
            this.underwaterSinkPoseBlend = 0.0f;
        }
        this.idleTime += dt;
        if (this.idleBoostLeftTarget > 0.0f) {
            this.idleBoostLeftTarget = Math.max(0.0f, this.idleBoostLeftTarget - dt * 1.0f);
        }
        if (this.idleBoostRightTarget > 0.0f) {
            this.idleBoostRightTarget = Math.max(0.0f, this.idleBoostRightTarget - dt * 1.0f);
        }
        float boostLerp = 1.0f - (float)Math.pow(0.45f, dt * 20.0f);
        this.idleBoostLeft = class_3532.method_16439((float)boostLerp, (float)this.idleBoostLeft, (float)this.idleBoostLeftTarget);
        this.idleBoostRight = class_3532.method_16439((float)boostLerp, (float)this.idleBoostRight, (float)this.idleBoostRightTarget);
        this.updateIdleJitter(dt);
        float accel = -JUMP_STIFFNESS * this.jumpValue - JUMP_DAMPING * this.jumpVelocity;
        this.jumpVelocity += accel * dt;
        this.jumpValue += this.jumpVelocity * dt;
        if (Math.abs(this.jumpValue) < 1.0E-4f && Math.abs(this.jumpVelocity) < 1.0E-4f) {
            this.jumpValue = 0.0f;
            this.jumpVelocity = 0.0f;
        }
        float impactAccel = -IMPACT_STIFFNESS * this.impactValue - IMPACT_DAMPING * this.impactVelocity;
        this.impactVelocity += impactAccel * dt;
        this.impactValue += this.impactVelocity * dt;
        if (Math.abs(this.impactValue) < 1.0E-4f && Math.abs(this.impactVelocity) < 1.0E-4f) {
            this.impactValue = 0.0f;
            this.impactVelocity = 0.0f;
            this.impactArm = null;
        }
        float shieldImpactAccel = -SHIELD_IMPACT_STIFFNESS * this.shieldImpactValue - SHIELD_IMPACT_DAMPING * this.shieldImpactVelocity;
        this.shieldImpactVelocity += shieldImpactAccel * dt;
        this.shieldImpactValue += this.shieldImpactVelocity * dt;
        if (Math.abs(this.shieldImpactValue) < 1.0E-4f && Math.abs(this.shieldImpactVelocity) < 1.0E-4f) {
            this.shieldImpactValue = 0.0f;
            this.shieldImpactVelocity = 0.0f;
            this.shieldImpactArm = null;
        }
        for (int i = 0; i < 2; ++i) {
            float actionAccel = -ACTION_ANIM_STIFFNESS * this.actionAnimValue[i] - ACTION_ANIM_DAMPING * this.actionAnimVelocity[i];
            int n = i;
            this.actionAnimVelocity[n] = this.actionAnimVelocity[n] + actionAccel * dt;
            int n2 = i;
            this.actionAnimValue[n2] = this.actionAnimValue[n2] + this.actionAnimVelocity[i] * dt;
            if (!(Math.abs(this.actionAnimValue[i]) < 1.0E-4f) || !(Math.abs(this.actionAnimVelocity[i]) < 1.0E-4f)) continue;
            this.actionAnimValue[i] = 0.0f;
            this.actionAnimVelocity[i] = 0.0f;
        }
        this.sneakSpring.tick(dt);
    }

    public boolean isSprintSwingActive() {
        return this.sprintTarget > 0.0f || this.sprintBlend > 0.001f;
    }

    public boolean isWalkSwingActive() {
        return this.walkBlend > 0.001f;
    }

    public float getSprintBlendAlpha() {
        return MovementStateMachine.smoothStep(this.sprintBlend) * MovementStateMachine.smoothStep(this.sprintSpeedFactor);
    }

    public void applyToMatrix(Matrix4f matrix, float partialTicks, class_1306 currentArm) {
        float sprintAlpha;
        SwingTransform sprint;
        SwingTransform walk;
        SwingTransform blended;
        if (matrix == null) {
            return;
        }
        if (this.suppressWalkSway) {
            return;
        }
        boolean syncWalkArms = this.isWalkArmSyncActive();
        float walkSprintScale = this.getWalkAndSprintForceScale();
        float othersScale = this.getRemainingProceduralForceScale();
        float rootScale = (syncWalkArms ? 0.75f : 1.0f) * walkSprintScale;
        boolean skipWalkSway = false;
        if (!(skipWalkSway || this.offsetX == 0.0f && this.offsetZ == 0.0f)) {
            if (syncWalkArms) {
                matrix.translateLocal(this.cameraRightX * (this.offsetX * rootScale) + this.cameraForwardX * (this.offsetZ * rootScale), this.cameraRightY * (this.offsetX * rootScale) + this.cameraForwardY * (this.offsetZ * rootScale), this.cameraRightZ * (this.offsetX * rootScale) + this.cameraForwardZ * (this.offsetZ * rootScale));
            } else {
                matrix.translate(this.offsetX * rootScale, this.offsetZ * rootScale, 0.0f);
            }
        }
        if ((blended = SwingTransform.blend(walk = this.computeWalkSwing(currentArm), sprint = this.computeSprintSwing(currentArm), sprintAlpha = this.resolveSprintAlpha(currentArm, partialTicks))) != null && currentArm != null) {
            int armIndex = currentArm == class_1306.field_6182 ? 0 : 1;
            float dt = this.sprintDt > 0.0f ? this.sprintDt : 0.05f;
            float smooth = 1.0f - (float)Math.pow(0.72f, dt * 20.0f);
            float targetGate = MovementStateMachine.isSprintLateralTiltEnabled(currentArm) ? 1.0f : 0.0f;
            this.sprintLateralGate[armIndex] = class_3532.method_16439((float)smooth, (float)this.sprintLateralGate[armIndex], (float)targetGate);
            float lateralScale = class_3532.method_16439((float)sprintAlpha, (float)1.0f, (float)this.sprintLateralGate[armIndex]);
            blended = new SwingTransform(blended.rotX, blended.rotY * lateralScale, blended.rotZ * lateralScale, blended.offX * lateralScale, blended.offY, blended.offZ);
        }
        if (blended != null) {
            float swingScale = (syncWalkArms ? 0.7f : 1.0f) * walkSprintScale;
            blended = blended.scaled(swingScale);
        }
        if (blended != null && blended.hasMotion()) {
            if (syncWalkArms) {
                this.applySwingRootSpace(matrix, blended);
            } else {
                matrix.rotateX(blended.rotX);
                matrix.rotateY(blended.rotY);
                matrix.rotateZ(blended.rotZ);
                matrix.translate(blended.offX, blended.offY, blended.offZ);
            }
        }
        this.applyFallOrbitMotion(matrix, currentArm, syncWalkArms, othersScale);
        this.applyFlightPoseMotion(matrix, currentArm);
        this.applyUnderwaterSinkPoseMotion(matrix, currentArm);
    }

    private void applyFallOrbitMotion(Matrix4f matrix, class_1306 currentArm, boolean syncWalkArms, float othersScale) {
        float armSign;
        boolean armIndex;
        if (matrix == null || currentArm == null || this.fallOrbitBlend <= 0.001f) {
            return;
        }
        boolean bl = armIndex = currentArm != class_1306.field_6182;
        float f = syncWalkArms ? 1.0f : (armSign = !armIndex ? 1.0f : -1.0f);
        float phase = this.fallOrbitPhase + (syncWalkArms ? 0.0f : (armSign < 0.0f ? (float)Math.PI : 0.0f));
        float w = MovementStateMachine.smoothStep(this.fallOrbitBlend) * othersScale;
        float forward = class_3532.method_15374((float)phase) * 1.17f * w;
        float up = class_3532.method_15362((float)phase) * 0.68f * w;
        float pitch = class_3532.method_15374((float)(phase + 0.35f)) * 0.14f * w;
        float roll = class_3532.method_15362((float)(phase + 0.12f)) * 0.1f * w * armSign;
        if (syncWalkArms) {
            matrix.rotate(pitch *= 0.65f, this.cameraRightX, this.cameraRightY, this.cameraRightZ);
            matrix.rotate(roll *= 0.65f, this.cameraForwardX, this.cameraForwardY, this.cameraForwardZ);
            matrix.translateLocal(this.cameraForwardX * forward + this.cameraUpX * (up *= 0.75f), this.cameraForwardY * (forward *= 0.75f) + this.cameraUpY * up, this.cameraForwardZ * forward + this.cameraUpZ * up);
        } else {
            matrix.rotateX(pitch);
            matrix.rotateZ(roll);
            matrix.translate(0.0f, forward, up);
        }
    }

    private void applyFlightPoseMotion(Matrix4f matrix, class_1306 currentArm) {
        if (matrix == null || currentArm == null || this.flightPoseBlend <= 0.001f) {
            return;
        }
        float easedProgress = class_3532.method_15363((float)(this.flightPoseBlend * 1.12f), (float)0.0f, (float)1.0f);
        float w = class_3532.method_15363((float)Easing.easeOutBack(easedProgress), (float)0.0f, (float)1.0f);
        class_746 player = class_310.method_1551().field_1724;
        class_243 velocity = player == null ? null : player.method_18798();
        float speed = velocity == null ? 0.0f : (float)velocity.method_1033();
        float speedBlend = player != null && player.method_6128() ? class_3532.method_15363((float)((speed - 0.55f) / 1.2f), (float)0.0f, (float)1.0f) : class_3532.method_15363((float)((speed - 0.16f) / 0.69000006f), (float)0.0f, (float)1.0f);
        float smoothSpeedBlend = MovementStateMachine.smoothStep(speedBlend);
        float swayWeight = w * smoothSpeedBlend * smoothSpeedBlend * smoothSpeedBlend;
        float swayTime = player == null ? 0.0f : (float)player.field_6012 * 1.15f;
        float armSign = currentArm == class_1306.field_6182 ? -1.0f : 1.0f;
        float inwardRot = (float)Math.toRadians(35.0) * w;
        if (currentArm == class_1306.field_6182) {
            matrix.rotateY(inwardRot);
        } else {
            matrix.rotateY(-inwardRot);
        }
        float side = (0.0f + class_3532.method_15374((float)swayTime) * 0.12f * armSign) * swayWeight;
        float forward = 5.8f * w;
        float up = -0.55f * w + class_3532.method_15374((float)(swayTime * 0.25f + 0.6f)) * 0.012f * swayWeight;
        float lift = 0.0f * w;
        matrix.translate(side, forward + lift, up);
    }

    private void applyUnderwaterSinkPoseMotion(Matrix4f matrix, class_1306 currentArm) {
        if (matrix == null || currentArm == null || this.underwaterSinkPoseBlend <= 0.001f) {
            return;
        }
        float w = this.underwaterSinkPoseBlend;
        float armSign = currentArm == class_1306.field_6182 ? -1.0f : 1.0f;
        float inwardYaw = UNDERWATER_SINK_POSE_INWARD_YAW * w;
        if (currentArm == class_1306.field_6182) {
            matrix.rotateY(inwardYaw);
        } else {
            matrix.rotateY(-inwardYaw);
        }
        matrix.rotateX(-0.09f * w);
        matrix.rotateZ(0.03f * w * armSign);
        matrix.translate(-0.6f * w * armSign, 4.6400003f * w, -3.168f * w);
        this.debugUnderwaterSinkPoseApply(currentArm, w);
    }

    private float resolveFlightPoseTargetBlend(class_746 player) {
        float speed;
        if (player == null || this.suppressWalkSway) {
            return 0.0f;
        }
        if (player.method_24828() || player.method_5681() || player.method_5799()) {
            return 0.0f;
        }
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying() && PunchyAnimationManager.SWIM_HANDLER.getCurrentClip() == PunchyAnimationManager.SWIMING_CLIP) {
            return 0.0f;
        }
        if (!PunchyConfig.enableElytraFlightAnimation()) {
            return 0.0f;
        }
        class_243 velocity = player.method_18798();
        float f = speed = velocity == null ? 0.0f : (float)velocity.method_1033();
        if (player.method_6128()) {
            return class_3532.method_15363((float)((speed - 0.55f) / 1.2f), (float)0.0f, (float)1.0f);
        }
        return 0.0f;
    }

    private float resolveUnderwaterSinkPoseTargetBlend(class_746 player) {
        boolean inputActive;
        boolean submerged;
        class_310 client = class_310.method_1551();
        if (player == null || client == null || client.field_1690 == null || this.suppressWalkSway) {
            this.debugUnderwaterSinkPoseState("invalid_context", player, 0.0f, false, false, 0.0f);
            return 0.0f;
        }
        boolean bl = submerged = player.method_5869() || player.method_5777(class_3486.field_15517);
        if (!submerged) {
            this.debugUnderwaterSinkPoseState("not_submerged", player, 0.0f, false, false, 0.0f);
            return 0.0f;
        }
        if (player.method_5681() || player.method_31549().field_7479 || player.method_6128()) {
            this.debugUnderwaterSinkPoseState("swimming_or_flying", player, 0.0f, submerged, false, 0.0f);
            return 0.0f;
        }
        if (client.field_1690.field_1903.method_1434()) {
            this.debugUnderwaterSinkPoseState("jump_pressed", player, 0.0f, submerged, false, 0.0f);
            return 0.0f;
        }
        if (this.hasSolidSupportAtFeet(player)) {
            this.debugUnderwaterSinkPoseState("solid_support", player, 0.0f, submerged, false, 0.0f);
            return 0.0f;
        }
        boolean bl2 = inputActive = Math.abs(player.field_6250) > 0.05f || Math.abs(player.field_6212) > 0.05f || client.field_1690.field_1894.method_1434() || client.field_1690.field_1881.method_1434() || client.field_1690.field_1913.method_1434() || client.field_1690.field_1849.method_1434();
        if (inputActive) {
            this.debugUnderwaterSinkPoseState("wasd_active", player, 0.0f, submerged, false, 0.0f);
            return 0.0f;
        }
        class_243 velocity = player.method_18798();
        float downwardSpeed = velocity == null ? 0.0f : (float)Math.max(0.0, -velocity.field_1351);
        float target = class_3532.method_15363((float)((downwardSpeed - 0.0f) / 0.08f), (float)0.0f, (float)1.0f);
        float resolved = Math.max(1.0f, target);
        this.debugUnderwaterSinkPoseState("active_idle_sink", player, resolved, submerged, false, downwardSpeed);
        return resolved;
    }

    private boolean hasSolidSupportAtFeet(class_746 player) {
        if (player == null || player.method_37908() == null) {
            return false;
        }
        class_238 bounds = player.method_5829();
        double sampleY = bounds.field_1322 - 0.05;
        double insetX = Math.min(0.2, bounds.method_17939() * 0.25);
        double insetZ = Math.min(0.2, bounds.method_17941() * 0.25);
        double[] sampleXs = new double[]{bounds.field_1323 + insetX, bounds.field_1320 - insetX, (bounds.field_1323 + bounds.field_1320) * 0.5};
        double[] sampleZs = new double[]{bounds.field_1321 + insetZ, bounds.field_1324 - insetZ, (bounds.field_1321 + bounds.field_1324) * 0.5};
        for (double sampleX : sampleXs) {
            for (double sampleZ : sampleZs) {
                class_2338 supportPos = class_2338.method_49637((double)sampleX, (double)sampleY, (double)sampleZ);
                if (!player.method_37908().method_8320(supportPos).method_51366()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean isFlightPoseActive() {
        return this.flightPoseBlend > 0.001f;
    }

    public float getFlightPoseBlend() {
        return this.flightPoseBlend;
    }

    public boolean isUnderwaterSinkPoseActive() {
        return this.underwaterSinkPoseBlend > 0.001f;
    }

    public float getUnderwaterSinkPoseBlend() {
        return this.underwaterSinkPoseBlend;
    }

    private void debugUnderwaterSinkPoseState(String reason, class_746 player, float target, boolean submerged, boolean moving, float downwardSpeed) {
        if (MovementStateMachine.safeEquals(this.lastLoggedUnderwaterSinkReason, reason) && Math.abs(this.lastLoggedUnderwaterSinkTarget - target) < 0.01f) {
            return;
        }
        this.lastLoggedUnderwaterSinkReason = reason;
        this.lastLoggedUnderwaterSinkTarget = target;
    }

    private void debugUnderwaterSinkPoseBlend(class_746 player, float target) {
        if (Math.abs(this.lastLoggedUnderwaterSinkBlend - this.underwaterSinkPoseBlend) < 0.02f && Math.abs(this.lastLoggedUnderwaterSinkTarget - target) < 0.01f) {
            return;
        }
        this.lastLoggedUnderwaterSinkBlend = this.underwaterSinkPoseBlend;
    }

    private void debugUnderwaterSinkPoseApply(class_1306 arm, float easedBlend) {
    }

    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static float approach(float current, float target, float step) {
        float delta = target - current;
        if (Math.abs(delta) <= step) {
            return target;
        }
        return current + Math.copySign(step, delta);
    }

    private static String clipName(AnimationClip clip) {
        return clip == null ? "null" : clip.getName();
    }

    private void applySwingRootSpace(Matrix4f matrix, SwingTransform swing) {
        if (matrix == null || swing == null) {
            return;
        }
        if (swing.rotX != 0.0f) {
            matrix.rotate(swing.rotX, this.cameraRightX, this.cameraRightY, this.cameraRightZ);
        }
        if (swing.rotY != 0.0f) {
            matrix.rotate(swing.rotY, this.cameraUpX, this.cameraUpY, this.cameraUpZ);
        }
        if (swing.rotZ != 0.0f) {
            matrix.rotate(swing.rotZ, this.cameraForwardX, this.cameraForwardY, this.cameraForwardZ);
        }
        if (swing.offX != 0.0f || swing.offY != 0.0f || swing.offZ != 0.0f) {
            matrix.translateLocal(this.cameraRightX * swing.offX + this.cameraForwardX * swing.offY + this.cameraUpX * swing.offZ, this.cameraRightY * swing.offX + this.cameraForwardY * swing.offY + this.cameraUpY * swing.offZ, this.cameraRightZ * swing.offX + this.cameraForwardZ * swing.offY + this.cameraUpZ * swing.offZ);
        }
    }

    public void applyRootSpaceMotion(Matrix4f matrix, float partialTicks, class_1306 currentArm) {
        float sneakOffset;
        if (matrix == null) {
            return;
        }
        if (this.suppressWalkSway) {
            return;
        }
        boolean syncWalkArms = this.isWalkArmSyncActive();
        float othersScale = this.getRemainingProceduralForceScale();
        float rootScale = (syncWalkArms ? 0.75f : 1.0f) * othersScale;
        if (this.jumpValue != 0.0f) {
            float offset = this.jumpValue * JUMP_OFFSET_SCALE * rootScale;
            matrix.translateLocal(this.cameraUpX * offset, this.cameraUpY * offset, this.cameraUpZ * offset);
            matrix.rotateX(this.jumpValue * JUMP_ROT_SCALE * rootScale);
        }
        if ((sneakOffset = this.sneakSpring.offset()) != 0.0f) {
            float offset = sneakOffset * JUMP_OFFSET_SCALE * rootScale;
            matrix.translateLocal(this.cameraUpX * offset, this.cameraUpY * offset, this.cameraUpZ * offset);
            matrix.rotateX(sneakOffset * JUMP_ROT_SCALE * rootScale);
        }
        if (this.impactValue != 0.0f && (syncWalkArms || currentArm == this.impactArm)) {
            float recuo = -this.impactValue * 2.5f * rootScale;
            float up = this.impactValue * 0.5f * rootScale;
            matrix.translateLocal(this.cameraForwardX * recuo + this.cameraUpX * up, this.cameraForwardY * recuo + this.cameraUpY * up, this.cameraForwardZ * recuo + this.cameraUpZ * up);
            matrix.rotateX(-this.impactValue * 0.6f * rootScale);
        }
        if (!this.suppressWalkSway && this.idleWeight > 0.0f && IDLE_INTENSITY != 0.0f) {
            float jitter;
            float boost = syncWalkArms ? (this.idleBoostLeft + this.idleBoostRight) * 0.5f : this.getIdleBoost(currentArm);
            float speed = IDLE_SPEED * (1.0f + boost * 0.0f);
            float phase = this.idleTime * Math.max(0.01f, speed);
            float intensity = IDLE_INTENSITY * this.idleWeight * (1.0f + boost * 3.3f) * rootScale;
            float bob = class_3532.method_15374((float)(phase * 1.35f)) * 0.015f * intensity;
            float sway = class_3532.method_15374((float)(phase * 0.9f + 1.2f)) * 0.02f * intensity;
            float tilt = class_3532.method_15374((float)(phase * 0.75f + 0.5f)) * 0.012f * intensity;
            matrix.translateLocal(this.cameraForwardX * sway + this.cameraUpX * bob, this.cameraForwardY * sway + this.cameraUpY * bob, this.cameraForwardZ * sway + this.cameraUpZ * bob);
            matrix.rotateX(tilt);
            float f = jitter = syncWalkArms ? (this.idleJitterLeft + this.idleJitterRight) * 0.5f : this.getIdleJitter(currentArm);
            if (jitter != 0.0f && boost > 0.0f) {
                float lateral = jitter * 0.0078f * boost * othersScale;
                matrix.translateLocal(this.cameraRightX * lateral, this.cameraRightY * lateral, this.cameraRightZ * lateral);
            }
        }
    }

    public void applyCameraLookToRoot(Matrix4f matrix) {
        if (matrix == null) {
            return;
        }
        if (this.suppressWalkSway) {
            return;
        }
        float othersScale = this.getRemainingProceduralForceScale();
        if (this.cameraTiltY != 0.0f) {
            matrix.rotate(this.cameraTiltY * othersScale, this.cameraUpX, this.cameraUpY, this.cameraUpZ);
        }
        if (this.cameraTiltZ != 0.0f) {
            matrix.rotate(this.cameraTiltZ * othersScale, this.cameraForwardX, this.cameraForwardY, this.cameraForwardZ);
        }
        if (this.cameraPitchTiltX != 0.0f) {
            matrix.rotate(this.cameraPitchTiltX * othersScale, this.cameraRightX, this.cameraRightY, this.cameraRightZ);
        }
        if (this.cameraPitchOffset != 0.0f) {
            matrix.translate(this.cameraUpX * this.cameraPitchOffset * othersScale, this.cameraUpY * this.cameraPitchOffset * othersScale, this.cameraUpZ * this.cameraPitchOffset * othersScale);
        }
    }

    public void applyCameraYawLagToArm(Matrix4f matrix) {
        if (matrix == null || PunchyConfig.disableArmPhysics()) {
            return;
        }
        if (this.cameraArmTiltY != 0.0f) {
            float othersScale = this.getRemainingProceduralForceScale();
            matrix.rotateY(this.cameraArmTiltY * othersScale);
        }
    }

    private static float normalizeLookDelta(float delta, float dt) {
        return delta * (0.016666668f / Math.max(dt, 1.0E-4f));
    }

    private void stepCameraArmTiltSpring(float targetArmTiltY, float dt) {
        float stepDt;
        for (float remainingDt = Math.max(dt, 0.0f); remainingDt > 0.0f; remainingDt -= stepDt) {
            stepDt = Math.min(remainingDt, 0.008333334f);
            float armYawAccel = CAMERA_YAW_ARM_TILT_STIFFNESS * (targetArmTiltY - this.cameraArmTiltY) - CAMERA_YAW_ARM_TILT_DAMPING * this.cameraArmTiltYVelocity;
            this.cameraArmTiltYVelocity += armYawAccel * stepDt;
            this.cameraArmTiltY += this.cameraArmTiltYVelocity * stepDt;
            if (this.cameraArmTiltY > CAMERA_YAW_ARM_TILT_MAX) {
                this.cameraArmTiltY = CAMERA_YAW_ARM_TILT_MAX;
                if (!(this.cameraArmTiltYVelocity > 0.0f)) continue;
                this.cameraArmTiltYVelocity = 0.0f;
                continue;
            }
            if (!(this.cameraArmTiltY < -CAMERA_YAW_ARM_TILT_MAX)) continue;
            this.cameraArmTiltY = -CAMERA_YAW_ARM_TILT_MAX;
            if (!(this.cameraArmTiltYVelocity < 0.0f)) continue;
            this.cameraArmTiltYVelocity = 0.0f;
        }
    }

    public float getCameraRightX() {
        return this.cameraRightX;
    }

    public float getCameraRightY() {
        return this.cameraRightY;
    }

    public float getCameraRightZ() {
        return this.cameraRightZ;
    }

    public void applyShieldImpactToRoot(Matrix4f matrix, float partialTicks, class_1306 currentArm) {
        if (matrix == null) {
            return;
        }
        boolean syncWalkArms = this.isWalkArmSyncActive();
        float othersScale = this.getRemainingProceduralForceScale();
        if (this.shieldImpactValue != 0.0f && this.shieldImpactArm != null && currentArm != null) {
            float multiplier = (syncWalkArms ? 0.85f : (currentArm == this.shieldImpactArm ? 1.0f : 0.6f)) * othersScale;
            float recuo = this.shieldImpactValue * 5.0f * multiplier;
            matrix.translateLocal(this.cameraForwardX * recuo, this.cameraForwardY * recuo, this.cameraForwardZ * recuo);
            matrix.rotateX(-this.shieldImpactValue * 0.5f * multiplier);
        }
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetZ() {
        return this.offsetZ;
    }

    public float getJumpValue() {
        return this.jumpValue;
    }

    public float getImpactValue() {
        return this.impactValue;
    }

    public float getShieldImpactValue() {
        return this.shieldImpactValue;
    }

    public float getActionAnimValue(class_1306 arm) {
        if (arm == null) {
            return 0.0f;
        }
        return this.actionAnimValue[arm == class_1306.field_6182 ? 0 : 1];
    }

    public float consumeActionAnimKick(class_1306 arm) {
        if (arm == null) {
            return 0.0f;
        }
        int idx = arm == class_1306.field_6182 ? 0 : 1;
        float value = this.actionAnimKick[idx];
        this.actionAnimKick[idx] = 0.0f;
        return value;
    }

    public float getIdleTime() {
        return this.idleTime;
    }

    public float getIdleWeight() {
        return this.idleWeight;
    }

    public void triggerIdleBalance(class_1306 arm) {
        if (arm == null) {
            return;
        }
        if (arm == class_1306.field_6182) {
            this.idleBoostLeftTarget = 1.0f;
            this.idleJitterLeftTimer = 0.0f;
        } else {
            this.idleBoostRightTarget = 1.0f;
            this.idleJitterRightTimer = 0.0f;
        }
    }

    private float getIdleBoost(class_1306 arm) {
        if (arm == null) {
            return 0.0f;
        }
        return arm == class_1306.field_6182 ? this.idleBoostLeft : this.idleBoostRight;
    }

    private float getIdleJitter(class_1306 arm) {
        if (arm == null) {
            return 0.0f;
        }
        return arm == class_1306.field_6182 ? this.idleJitterLeft : this.idleJitterRight;
    }

    private void updateIdleJitter(float dt) {
        this.idleJitterLeftTimer -= dt;
        if (this.idleJitterLeftTimer <= 0.0f) {
            this.idleJitterLeftTarget = this.idleJitterRandom.method_43057() * 2.0f - 1.0f;
            this.idleJitterLeftTimer = class_3532.method_16439((float)this.idleJitterRandom.method_43057(), (float)0.35f, (float)0.8f);
        }
        this.idleJitterRightTimer -= dt;
        if (this.idleJitterRightTimer <= 0.0f) {
            this.idleJitterRightTarget = this.idleJitterRandom.method_43057() * 2.0f - 1.0f;
            this.idleJitterRightTimer = class_3532.method_16439((float)this.idleJitterRandom.method_43057(), (float)0.35f, (float)0.8f);
        }
        float jitterLerp = 1.0f - (float)Math.pow(0.65f, dt * 20.0f);
        this.idleJitterLeft = class_3532.method_16439((float)jitterLerp, (float)this.idleJitterLeft, (float)this.idleJitterLeftTarget);
        this.idleJitterRight = class_3532.method_16439((float)jitterLerp, (float)this.idleJitterRight, (float)this.idleJitterRightTarget);
    }

    private static boolean isStandingOnSlime(class_746 player) {
        if (player == null || player.method_37908() == null) {
            return false;
        }
        return player.method_37908().method_8320(player.method_24515().method_10074()).method_27852(class_2246.field_10030);
    }

    private boolean shouldApplySprintSwing(class_746 player) {
        if (player == null) {
            return false;
        }
        if (!PunchyConfig.enableSprintArmSwing() || PunchyConfig.getSprintArmSwingIntensity() <= 0.0f) {
            return false;
        }
        if (this.suppressWalkSway) {
            return false;
        }
        if (this.isSubmergedWithoutGround(player)) {
            return false;
        }
        if (!player.method_5624()) {
            return false;
        }
        if (player.method_18276() || player.method_5681() || player.method_6128()) {
            return false;
        }
        if (player.method_6115() && !this.isSprintCompatibleUseItem(player)) {
            return false;
        }
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            return false;
        }
        return player.field_6250 > 0.1f || player.field_42108.method_48566() > 0.05f;
    }

    private float resolveSprintAlpha(class_1306 currentArm, float partialTicks) {
        float sprintAlpha = MovementStateMachine.smoothStep(this.sprintBlend) * MovementStateMachine.smoothStep(this.sprintSpeedFactor);
        if (sprintAlpha <= 1.0E-4f || currentArm == null) {
            return sprintAlpha;
        }
        if (PunchyAnimationManager.POSE_HANDLER.isCurrentDualHandedActive()) {
            return sprintAlpha;
        }
        AnimationClip currentClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (this.isSprintBlendAllowedDuringPrimaryPose(currentClip)) {
            return sprintAlpha;
        }
        class_1306 activeArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        if (!PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            if (activeArm != null && activeArm == currentArm && PunchyAnimationManager.POSE_HANDLER.isBlendOutActive()) {
                return sprintAlpha * PunchyAnimationManager.POSE_HANDLER.getBlendOutProgress(partialTicks);
            }
            return sprintAlpha;
        }
        if (activeArm != null && activeArm == currentArm) {
            if (PunchyAnimationManager.POSE_HANDLER.isRebaseReturnToOriginActive()) {
                return sprintAlpha * PunchyAnimationManager.POSE_HANDLER.getRebaseReturnToOriginProgress(partialTicks);
            }
            return 0.0f;
        }
        return sprintAlpha;
    }

    private boolean isSprintBlendAllowedDuringPrimaryPose(AnimationClip clip) {
        return this.isSprintBlendAllowedDuringEquip(clip) || this.isSprintBlendAllowedDuringAttack(clip) || this.isSprintBlendAllowedDuringMining(clip) || this.isSprintBlendAllowedDuringUseItem(clip);
    }

    private boolean isSprintBlendAllowedDuringEquip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        return "hand_in".equals(name) || "hand_out".equals(name) || name.startsWith("hand_in_") || name.startsWith("hand_out_");
    }

    private boolean isSprintBlendAllowedDuringAttack(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "attack") || PunchyAnimationManager.isClipNamed(clip, "attack_mace") || PunchyAnimationManager.isClipNamed(clip, "attack_trident") || PunchyAnimationManager.isClipNamed(clip, "attack_spear") || PunchyAnimationManager.isClipNamed(clip, "punch")) {
            return true;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("attack") || normalized.startsWith("punch");
    }

    private boolean isSprintBlendAllowedDuringMining(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "mining") || PunchyAnimationManager.isClipNamed(clip, "mining_foward") || PunchyAnimationManager.isClipNamed(clip, "mining_forward") || PunchyAnimationManager.isClipNamed(clip, "mining_sideways") || PunchyAnimationManager.isClipNamed(clip, "mining_digging")) {
            return true;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("mining") || normalized.startsWith("mining_") || normalized.contains("mining_v2");
    }

    private boolean isSprintBlendAllowedDuringUseItem(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        if (PunchyAnimationManager.isClipNamed(clip, "interact") || PunchyAnimationManager.isClipNamed(clip, "use_eat") || PunchyAnimationManager.isClipNamed(clip, "use_drink") || PunchyAnimationManager.isClipNamed(clip, "use_item") || PunchyAnimationManager.isClipNamed(clip, "useitem") || PunchyAnimationManager.isClipNamed(clip, "mining_use_foward") || PunchyAnimationManager.isClipNamed(clip, "mining_use_forward") || PunchyAnimationManager.isClipNamed(clip, "mining_use_digging")) {
            return true;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("interact") || normalized.equals("interaction") || normalized.equals("use_item") || normalized.equals("useitem") || normalized.equals("use_eat") || normalized.equals("use_drink") || normalized.startsWith("mining_use_")) {
            return true;
        }
        class_310 client = class_310.method_1551();
        return client != null && client.field_1724 != null && this.isSprintCompatibleUseItem(client.field_1724);
    }

    private boolean shouldApplyWalkSwing(class_746 player, float moveFactor) {
        if (player == null) {
            return false;
        }
        if (!PunchyConfig.enableCustomWalk()) {
            return false;
        }
        if (this.suppressWalkSway) {
            return false;
        }
        if (this.isSubmergedWithoutGround(player)) {
            return false;
        }
        if (player.method_18276() || player.method_5681() || player.method_6128()) {
            return false;
        }
        if (player.method_6115() && !this.isSprintCompatibleUseItem(player)) {
            return false;
        }
        if (player.method_5799() || player.method_5869() || player.method_5777(class_3486.field_15517)) {
            class_310 client = class_310.method_1551();
            if (!PunchyClient.getWalkOnWaterStateMachine().isTriggerActiveOrInGap(client)) {
                return false;
            }
        }
        return moveFactor > 0.02f;
    }

    private boolean isSubmergedWithoutGround(class_746 player) {
        if (player == null) {
            return false;
        }
        return player.method_5869() && !this.isGrounded;
    }

    private SwingTransform computeWalkSwing(class_1306 currentArm) {
        float armSign;
        int sourceIndex;
        if (currentArm == null) {
            return SwingTransform.empty();
        }
        boolean syncWalkArms = this.isWalkArmSyncActive();
        float walkEased = MovementStateMachine.smoothStep(this.walkBlend);
        float effectiveSpeed = this.animGrounded ? this.walkSpeedFactor : 1.0f;
        float weight = walkEased * effectiveSpeed;
        int armIndex = currentArm == class_1306.field_6182 ? 0 : 1;
        int n = sourceIndex = syncWalkArms ? 0 : armIndex;
        float f = syncWalkArms ? 1.0f : (armSign = armIndex == 0 ? 1.0f : -1.0f);
        float phase = this.walkPhase + (syncWalkArms ? 0.0f : (armSign < 0.0f ? (float)Math.PI : 0.0f));
        float targetPitch = 0.0f;
        float targetYaw = 0.0f;
        float targetRoll = 0.0f;
        float targetSide = 0.0f;
        float targetUp = 0.0f;
        if (weight > 1.0E-4f) {
            float stride = class_3532.method_15374((float)phase);
            float stride2 = class_3532.method_15374((float)(phase * 2.0f + 0.35f));
            float side = class_3532.method_15374((float)(phase + 0.4f));
            float contact = Math.abs(stride);
            targetPitch = (stride * 0.085f + stride2 * 0.035f * 0.55f) * weight;
            targetYaw = (class_3532.method_15374((float)(phase + 1.1f)) * 0.03f * 0.75f + class_3532.method_15374((float)(phase * 2.0f + 0.2f)) * 0.012f * 0.5f) * weight * armSign + 0.022f * weight * armSign;
            targetRoll = (class_3532.method_15362((float)(phase + 0.35f)) * 0.038f * 0.8f + class_3532.method_15374((float)(phase * 1.4f + 0.3f)) * 0.016f * 0.45f) * weight * armSign + 0.018f * weight * armSign;
            targetSide = side * 1.85f * 0.7f * weight * armSign;
            float uBias = stride * stride * 0.11f * 0.35f * weight;
            targetUp = (-contact * 0.06f * 0.55f + class_3532.method_15362((float)(phase * 2.0f + 0.2f)) * 0.06f * 0.25f) * weight + uBias;
            targetPitch *= 1.22f;
            targetYaw *= 1.22f;
            targetRoll *= 1.22f;
            if (syncWalkArms) {
                targetPitch *= 0.38f;
                targetYaw *= 0.38f;
                targetRoll *= 0.38f;
                targetSide *= 1.35f;
                targetUp *= 1.12f;
            }
        }
        if (this.animGrounded) {
            float dt = this.sprintDt > 0.0f ? this.sprintDt : 0.05f;
            float smooth = 1.0f - (float)Math.pow(0.6f, dt * 20.0f);
            this.walkRotX[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.walkRotX[sourceIndex], (float)targetPitch);
            this.walkRotY[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.walkRotY[sourceIndex], (float)targetYaw);
            this.walkRotZ[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.walkRotZ[sourceIndex], (float)targetRoll);
            this.walkOffSide[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.walkOffSide[sourceIndex], (float)targetSide);
            this.walkOffUp[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.walkOffUp[sourceIndex], (float)targetUp);
            if (syncWalkArms) {
                this.walkRotX[1] = this.walkRotX[0];
                this.walkRotY[1] = this.walkRotY[0];
                this.walkRotZ[1] = this.walkRotZ[0];
                this.walkOffSide[1] = this.walkOffSide[0];
                this.walkOffUp[1] = this.walkOffUp[0];
            }
        }
        return new SwingTransform(this.walkRotX[sourceIndex], this.walkRotY[sourceIndex], this.walkRotZ[sourceIndex], this.walkOffSide[sourceIndex], 0.0f, this.walkOffUp[sourceIndex]);
    }

    private boolean isWalkArmSyncActive() {
        AnimationClip poseClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (poseClip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && poseClip.hasEffectAny(SYNC_WALK_ARMS_EFFECT)) {
            return true;
        }
        PunchyTuningConfig.CustomFlags flags = PunchyAnimationManager.POSE_HANDLER.getOverlapCustomFlags();
        if (flags != null && Boolean.TRUE.equals(flags.syncProceduralArmAnimations)) {
            return true;
        }
        if (this.isDualHandedSpearChargeActive(poseClip)) {
            return true;
        }
        AnimationClip swimClip = PunchyAnimationManager.SWIM_HANDLER.getCurrentClip();
        return swimClip != null && PunchyAnimationManager.SWIM_HANDLER.isPlaying() && swimClip.hasEffectAny(SYNC_WALK_ARMS_EFFECT);
    }

    public boolean isWalkArmSyncEnabled() {
        return this.isWalkArmSyncActive();
    }

    public float getWalkAndSprintForceScale() {
        PunchyTuningConfig.CustomFlags flags = PunchyAnimationManager.POSE_HANDLER.getOverlapCustomFlags();
        if (flags != null && flags.walkAndSprintForce != null) {
            return (float)flags.walkAndSprintForce.intValue() / 100.0f;
        }
        return 1.0f;
    }

    public float getRemainingProceduralForceScale() {
        PunchyTuningConfig.CustomFlags flags = PunchyAnimationManager.POSE_HANDLER.getOverlapCustomFlags();
        if (flags != null && flags.remainingProceduralAnimationsForce != null) {
            return (float)flags.remainingProceduralAnimationsForce.intValue() / 100.0f;
        }
        return 1.0f;
    }

    public class_1306 getSyncWalkReferenceArm(class_1306 arm) {
        if (!this.isWalkArmSyncActive()) {
            return arm;
        }
        return class_1306.field_6182;
    }

    private boolean isSpearChargeUseActive(class_746 player) {
        if (player == null || !player.method_6115()) {
            return false;
        }
        return SpearStateMachine.isSpearStack(player.method_6030());
    }

    private boolean isSprintCompatibleUseItem(class_746 player) {
        if (player == null || !player.method_6115()) {
            return false;
        }
        if (this.isSpearChargeUseActive(player)) {
            return true;
        }
        class_1799 useStack = player.method_6030();
        if (useStack == null || useStack.method_7960()) {
            return false;
        }
        return !(useStack.method_7909() instanceof class_1753) && !(useStack.method_7909() instanceof class_1764) && !(useStack.method_7909() instanceof class_1835);
    }

    private boolean isDualHandedSpearChargeActive(AnimationClip clip) {
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        if (!PunchyAnimationManager.POSE_HANDLER.isCurrentDualHandedActive()) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("use_spear_pose_");
    }

    private SwingTransform computeSprintSwing(class_1306 currentArm) {
        float armSign;
        int sourceIndex;
        if (currentArm == null) {
            return SwingTransform.empty();
        }
        boolean allowLateralTilt = MovementStateMachine.isSprintLateralTiltEnabled(currentArm);
        boolean syncWalkArms = this.isWalkArmSyncActive();
        float eased = MovementStateMachine.smoothStep(this.sprintBlend);
        float intensity = PunchyConfig.getSprintArmSwingIntensity() * 0.75f;
        float effectiveSpeed = this.animGrounded ? this.sprintSpeedFactor : 1.0f;
        float weight = eased * effectiveSpeed * intensity * (syncWalkArms ? 0.7f : 1.0f);
        int armIndex = currentArm == class_1306.field_6182 ? 0 : 1;
        int n = sourceIndex = syncWalkArms ? 0 : armIndex;
        float f = syncWalkArms ? 1.0f : (armSign = armIndex == 0 ? 1.0f : -1.0f);
        float phase = this.sprintPhase + (syncWalkArms ? 0.0f : (armSign < 0.0f ? (float)Math.PI : 0.0f));
        float targetPitch = 0.0f;
        float targetYaw = 0.0f;
        float targetRoll = 0.0f;
        float targetForward = 0.0f;
        float targetUp = 0.0f;
        if (weight > 1.0E-4f) {
            float stride = class_3532.method_15374((float)phase);
            float stride2 = class_3532.method_15374((float)(phase * 2.0f + 0.2f));
            float contact = Math.abs(stride);
            targetPitch = (stride * 0.135f * 1.2f + stride2 * 0.06f * 0.9f - contact * 0.035f) * weight;
            targetYaw = (class_3532.method_15374((float)(phase + 1.1f)) * 0.0375f * 0.65f + class_3532.method_15374((float)(phase * 2.2f + 0.22f)) * 0.015f * 0.45f) * weight * armSign + 0.045f * weight * armSign;
            targetYaw += armSign * SPRINT_INWARD_YAW_STATIC * eased * intensity;
            if (allowLateralTilt) {
                targetRoll = (class_3532.method_15362((float)(phase + 0.35f)) * 0.0525f * 0.85f + class_3532.method_15374((float)(phase * 1.6f + 0.35f)) * 0.0225f * 0.55f) * weight * armSign + 0.03f * weight * armSign;
            }
            targetForward = (Math.max(0.0f, stride) * 3.6f * 0.9f + stride2 * 1.56f * 0.45f) * weight;
            float forwardUpBias = class_3532.method_15363((float)targetForward, (float)0.0f, (float)1.0f) * 0.28f * weight;
            targetPitch -= forwardUpBias;
            targetUp = (-contact * 0.12f * 0.7f + class_3532.method_15362((float)(phase * 2.0f + 0.3f)) * 0.12f * 0.25f) * weight;
            targetPitch *= 1.28f;
            targetYaw *= 1.28f;
            targetRoll *= 1.28f;
        }
        if (this.animGrounded) {
            float dt = this.sprintDt > 0.0f ? this.sprintDt : 0.05f;
            float smooth = 1.0f - (float)Math.pow(0.52f, dt * 20.0f);
            this.sprintRotX[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.sprintRotX[sourceIndex], (float)targetPitch);
            this.sprintRotY[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.sprintRotY[sourceIndex], (float)targetYaw);
            this.sprintRotZ[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.sprintRotZ[sourceIndex], (float)targetRoll);
            this.sprintOffFwd[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.sprintOffFwd[sourceIndex], (float)targetForward);
            this.sprintOffUp[sourceIndex] = class_3532.method_16439((float)smooth, (float)this.sprintOffUp[sourceIndex], (float)targetUp);
            if (syncWalkArms) {
                this.sprintRotX[1] = this.sprintRotX[0];
                this.sprintRotY[1] = this.sprintRotY[0];
                this.sprintRotZ[1] = this.sprintRotZ[0];
                this.sprintOffFwd[1] = this.sprintOffFwd[0];
                this.sprintOffUp[1] = this.sprintOffUp[0];
            }
        }
        return new SwingTransform(this.sprintRotX[sourceIndex], this.sprintRotY[sourceIndex], this.sprintRotZ[sourceIndex], 0.0f, this.sprintOffFwd[sourceIndex], this.sprintOffUp[sourceIndex]);
    }

    private static boolean isSprintLateralTiltEnabled(class_1306 currentArm) {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null || currentArm == null) {
            return true;
        }
        class_746 player = client.field_1724;
        class_1799 baseStack = MovementStateMachine.getStackForArm(player, currentArm);
        boolean left = currentArm == class_1306.field_6182;
        class_1799 renderStack = PunchyAnimationManager.resolveRenderStack((class_1657)player, currentArm, baseStack);
        if (MovementStateMachine.isSprintLateralTiltDisabledForStack(renderStack, left)) {
            return false;
        }
        return !MovementStateMachine.isSprintLateralTiltDisabledForStack(baseStack, left);
    }

    private static boolean isSprintLateralTiltDisabledForStack(class_1799 stack, boolean left) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        LinkedHashSet<VanillaFirstPersonItemLayer.ToolKind> candidates = new LinkedHashSet<VanillaFirstPersonItemLayer.ToolKind>();
        VanillaFirstPersonItemLayer.ToolKind resolved = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        if (resolved != null) {
            candidates.add(resolved);
        }
        if (stack.method_7909() instanceof class_1747) {
            candidates.add(VanillaFirstPersonItemLayer.ToolKind.FULL_BLOCK);
            candidates.add(VanillaFirstPersonItemLayer.ToolKind.BLOCK);
            candidates.add(VanillaFirstPersonItemLayer.ToolKind.BUILDING_BLOCKS);
            candidates.add(VanillaFirstPersonItemLayer.ToolKind.COLORED_BLOCKS);
            candidates.add(VanillaFirstPersonItemLayer.ToolKind.NATURAL_BLOCKS);
            candidates.add(VanillaFirstPersonItemLayer.ToolKind.FUNCTIONAL_BLOCKS);
        }
        candidates.add(VanillaFirstPersonItemLayer.ToolKind.INGREDIENT);
        for (VanillaFirstPersonItemLayer.ToolKind kind : candidates) {
            ToolTuning.Profile profile = ToolTuning.resolveProfile(stack, kind, left);
            if (profile == null || !Boolean.TRUE.equals(profile.disableSprintLateralTilt)) continue;
            return true;
        }
        return false;
    }

    private static class_1799 getStackForArm(class_746 player, class_1306 arm) {
        if (player == null || arm == null) {
            return class_1799.field_8037;
        }
        boolean mainRight = player.method_6068() == class_1306.field_6183;
        boolean armIsMain = arm == class_1306.field_6183 == mainRight;
        return armIsMain ? player.method_6047() : player.method_6079();
    }

    private static float smoothStep(float value) {
        float t = class_3532.method_15363((float)value, (float)0.0f, (float)1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static float wrapRadians(float value) {
        return (float)Math.atan2(Math.sin(value), Math.cos(value));
    }

    private static final class SneakSpringState {
        private static final float TARGET_DEPTH = -0.12f;
        private static final float DOWN_STIFFNESS = 22.0f;
        private static final float UP_STIFFNESS = 22.0f;
        private static final float DAMPING = 14.0f;
        private static final float DOWN_IMPULSE = -0.4f;
        private static final float UP_IMPULSE = 1.0f;
        private float value;
        private float velocity;
        private boolean sneaking;

        private SneakSpringState() {
        }

        void setSneaking(boolean sneaking) {
            if (this.sneaking == sneaking) {
                return;
            }
            this.sneaking = sneaking;
            this.velocity += sneaking ? -0.4f : 1.0f;
        }

        void tick(float dt) {
            float target = this.sneaking ? -0.12f : 0.0f;
            float stiffness = this.sneaking ? 22.0f : 22.0f;
            float accel = stiffness * (target - this.value) - 14.0f * this.velocity;
            this.velocity += accel * dt;
            this.value += this.velocity * dt;
            if (!this.sneaking && Math.abs(this.value) < 1.0E-4f && Math.abs(this.velocity) < 1.0E-4f) {
                this.value = 0.0f;
                this.velocity = 0.0f;
            }
        }

        float offset() {
            return this.value;
        }
    }

    private static final class SwingTransform {
        final float rotX;
        final float rotY;
        final float rotZ;
        final float offX;
        final float offY;
        final float offZ;

        private SwingTransform(float rotX, float rotY, float rotZ, float offX, float offY, float offZ) {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            this.offX = offX;
            this.offY = offY;
            this.offZ = offZ;
        }

        static SwingTransform empty() {
            return new SwingTransform(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }

        boolean hasMotion() {
            return Math.abs(this.rotX) > 1.0E-5f || Math.abs(this.rotY) > 1.0E-5f || Math.abs(this.rotZ) > 1.0E-5f || Math.abs(this.offX) > 1.0E-5f || Math.abs(this.offY) > 1.0E-5f || Math.abs(this.offZ) > 1.0E-5f;
        }

        SwingTransform scaled(float scale) {
            float s = class_3532.method_15363((float)scale, (float)0.0f, (float)2.0f);
            return new SwingTransform(this.rotX * s, this.rotY * s, this.rotZ * s, this.offX * s, this.offY * s, this.offZ * s);
        }

        static SwingTransform blend(SwingTransform a, SwingTransform b, float alpha) {
            if (a == null && b == null) {
                return null;
            }
            if (a == null) {
                return b;
            }
            if (b == null) {
                return a;
            }
            float t = class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f);
            float inv = 1.0f - t;
            return new SwingTransform(a.rotX * inv + b.rotX * t, a.rotY * inv + b.rotY * t, a.rotZ * inv + b.rotZ * t, a.offX * inv + b.offX * t, a.offY * inv + b.offY * t, a.offZ * inv + b.offZ * t);
        }
    }
}

