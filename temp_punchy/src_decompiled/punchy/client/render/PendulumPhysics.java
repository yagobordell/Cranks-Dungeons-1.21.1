/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_746
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.render;

import java.util.HashMap;
import net.minecraft.class_1306;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.render.ToolTuning;
import punchy.client.state.MovementStateMachine;
import punchy.compat.IrisShaderPackCompat;

public class PendulumPhysics {
    private static final PendulumPhysics INSTANCE = new PendulumPhysics();
    public static float PENDULUM_LIMIT_DEG = 55.0f;
    public static float PENDULUM_FORCE = 0.75f;
    public static float PENDULUM_SPEED = 16.0f;
    public static float PENDULUM_CAMERA_SENS = 0.3f;
    public static float PENDULUM_GRAVITY = 25.0f;
    public static float PENDULUM_MOTION_SENS = 0.2f;
    private float lastYaw = Float.NaN;
    private float lastPitch = Float.NaN;
    private long lastUpdateTime = System.nanoTime();
    private float frameDt;
    private float frameYawDelta;
    private float framePitchDelta;
    private float framePitch;
    private float frameWalkSpeed;
    private float frameWalkPos;
    private float frameVelY;
    private boolean frameOnGround;
    private boolean frameShaderActive;
    private boolean frameValid;
    private long frameId;
    private Boolean lastLoggedShaderState;
    private final HashMap<StateKey, State> states = new HashMap();
    private final HashMap<StateKey, Matrix4f> lastApplied = new HashMap();

    public static PendulumPhysics get() {
        return INSTANCE;
    }

    public void updateFrame(class_746 player, float partialTicks) {
        if (player == null) {
            this.reset();
            return;
        }
        long now = System.nanoTime();
        float dt = (float)(now - this.lastUpdateTime) / 1.0E9f;
        this.lastUpdateTime = now;
        if (dt <= 0.0f) {
            return;
        }
        if (dt > 0.033333335f) {
            dt = 0.033333335f;
        }
        float yaw = player.method_5705(partialTicks);
        float pitch = player.method_5695(partialTicks);
        if (Float.isNaN(this.lastYaw)) {
            this.lastYaw = yaw;
        }
        if (Float.isNaN(this.lastPitch)) {
            this.lastPitch = pitch;
        }
        float yawDelta = class_3532.method_15393((float)(yaw - this.lastYaw));
        float pitchDelta = pitch - this.lastPitch;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        this.frameWalkSpeed = player.field_42108.method_48566();
        this.frameWalkPos = player.field_42108.method_48569();
        this.frameVelY = (float)player.method_18798().field_1351;
        this.frameOnGround = player.method_24828();
        this.frameShaderActive = PendulumPhysics.detectShaderStateStable();
        if (this.lastLoggedShaderState == null || this.lastLoggedShaderState != this.frameShaderActive) {
            this.lastLoggedShaderState = this.frameShaderActive;
        }
        this.frameDt = dt;
        this.frameYawDelta = yawDelta;
        this.framePitchDelta = pitchDelta;
        this.framePitch = pitch;
        this.frameValid = true;
        ++this.frameId;
    }

    public void applyTo(class_4587 poseStack, ToolTuning.PhysicsMode mode, Vector3f pivot, ToolTuning.PhysicsSettings overrides, class_1306 arm, float partialTicks) {
        if (poseStack == null || mode == null || mode == ToolTuning.PhysicsMode.NONE) {
            return;
        }
        Matrix4f applied = this.computeAppliedMatrix(new Matrix4f((Matrix4fc)poseStack.method_23760().method_23761()), mode, pivot, overrides, arm, partialTicks, "default");
        if (applied == null) {
            return;
        }
        poseStack.method_23760().method_23761().mul((Matrix4fc)applied);
        poseStack.method_23760().method_23762().set((Matrix3fc)new Matrix3f((Matrix4fc)poseStack.method_23760().method_23761()).invert().transpose());
        StateKey stateKey = new StateKey(arm, "default");
        this.lastApplied.put(stateKey, applied);
        State state = this.states.get(stateKey);
        if (state != null) {
            state.lastAppliedFrameId = this.frameId;
        }
    }

    public Matrix4f computeAppliedMatrix(Matrix4f currentPose, ToolTuning.PhysicsMode mode, Vector3f pivot, ToolTuning.PhysicsSettings overrides, class_1306 arm, float partialTicks) {
        return this.computeAppliedMatrix(currentPose, mode, pivot, overrides, arm, partialTicks, "default", 0.0f);
    }

    public Matrix4f computeAppliedMatrix(Matrix4f currentPose, ToolTuning.PhysicsMode mode, Vector3f pivot, ToolTuning.PhysicsSettings overrides, class_1306 arm, float partialTicks, String stateChannel) {
        return this.computeAppliedMatrix(currentPose, mode, pivot, overrides, arm, partialTicks, stateChannel, 0.0f);
    }

    public Matrix4f computeAppliedMatrix(Matrix4f currentPose, ToolTuning.PhysicsMode mode, Vector3f pivot, ToolTuning.PhysicsSettings overrides, class_1306 arm, float partialTicks, String stateChannel, float referencePitchDeg) {
        Matrix4f result;
        if (currentPose == null || mode == null || mode == ToolTuning.PhysicsMode.NONE) {
            return null;
        }
        if (!this.frameValid || arm == null) {
            return null;
        }
        if (!PendulumPhysics.isFiniteMatrix(currentPose)) {
            return new Matrix4f();
        }
        ResolvedSettings settings = PendulumPhysics.resolveSettings(overrides);
        StateKey stateKey = new StateKey(arm, stateChannel == null || stateChannel.isBlank() ? "default" : stateChannel);
        State state = this.states.computeIfAbsent(stateKey, key -> new State());
        if (state.lastFrameId != this.frameId) {
            state.update(this.frameDt, this.frameYawDelta, this.framePitchDelta, this.framePitch, referencePitchDeg, this.frameWalkSpeed, this.frameWalkPos, this.frameVelY, this.frameOnGround, settings, arm);
            state.lastFrameId = this.frameId;
        }
        if (!state.isFinite()) {
            state.resetDynamic();
            state.lastFrameId = this.frameId;
        }
        float rotX = class_3532.method_16439((float)partialTicks, (float)state.prevAngleX, (float)state.angleX);
        float rotZ = class_3532.method_16439((float)partialTicks, (float)state.prevAngleZ, (float)state.angleZ);
        if (mode == ToolTuning.PhysicsMode.FORWARD_PENDULUM) {
            rotZ = 0.0f;
        }
        if (this.frameShaderActive) {
            rotZ = -rotZ;
        }
        if (settings.invert) {
            rotX = -rotX;
            rotZ = -rotZ;
        }
        if (!(Float.isFinite(rotX) && Float.isFinite(rotZ) && Float.isFinite(state.angleX) && Float.isFinite(state.angleZ) && Float.isFinite(state.velocityX) && Float.isFinite(state.velocityZ))) {
            state.resetDynamic();
            return new Matrix4f();
        }
        if (rotX == 0.0f && rotZ == 0.0f) {
            return new Matrix4f();
        }
        float pivotX = pivot != null ? pivot.x : 0.0f;
        float pivotY = pivot != null ? pivot.y : 0.0f;
        float pivotZ = pivot != null ? pivot.z : 0.0f;
        Vector3f pivotWorld = new Vector3f(pivotX, pivotY, pivotZ);
        currentPose.transformPosition(pivotWorld);
        Vector3f translation = currentPose.getTranslation(new Vector3f());
        Quaternionf rotation = currentPose.getUnnormalizedRotation(new Quaternionf());
        if (rotation.lengthSquared() < 1.0E-8f) {
            rotation.identity();
        } else {
            rotation.normalize();
        }
        Matrix4f rigidPose = new Matrix4f().translationRotate((Vector3fc)translation, (Quaternionfc)rotation);
        Matrix4f worldPendulum = new Matrix4f();
        if (pivotWorld.x != 0.0f || pivotWorld.y != 0.0f || pivotWorld.z != 0.0f) {
            worldPendulum.translate((Vector3fc)pivotWorld);
        }
        if (this.frameShaderActive) {
            if (rotZ != 0.0f) {
                worldPendulum.rotateZ((float)Math.toRadians(rotZ));
            }
            if (rotX != 0.0f) {
                worldPendulum.rotateX((float)Math.toRadians(rotX));
            }
        } else {
            float yawRad = (float)Math.toRadians(this.lastYaw);
            float pitchRad = (float)Math.toRadians(this.framePitch);
            float cosPitch = class_3532.method_15362((float)pitchRad);
            Vector3f cameraForward = new Vector3f(-class_3532.method_15374((float)yawRad) * cosPitch, -class_3532.method_15374((float)pitchRad), class_3532.method_15362((float)yawRad) * cosPitch);
            if (cameraForward.lengthSquared() < 1.0E-6f) {
                cameraForward.set(0.0f, 0.0f, 1.0f);
            } else {
                cameraForward.normalize();
            }
            Vector3f cameraRight = new Vector3f(-class_3532.method_15362((float)yawRad), 0.0f, -class_3532.method_15374((float)yawRad));
            if (cameraRight.lengthSquared() < 1.0E-6f) {
                cameraRight.set(1.0f, 0.0f, 0.0f);
            } else {
                cameraRight.normalize();
            }
            if (rotZ != 0.0f) {
                worldPendulum.rotate((float)Math.toRadians(rotZ), cameraForward.x, cameraForward.y, cameraForward.z);
            }
            if (rotX != 0.0f) {
                worldPendulum.rotate((float)Math.toRadians(rotX), cameraRight.x, cameraRight.y, cameraRight.z);
            }
        }
        if (pivotWorld.x != 0.0f || pivotWorld.y != 0.0f || pivotWorld.z != 0.0f) {
            worldPendulum.translate(-pivotWorld.x, -pivotWorld.y, -pivotWorld.z);
        }
        return PendulumPhysics.isFiniteMatrix(result = new Matrix4f((Matrix4fc)rigidPose).invert().mul((Matrix4fc)worldPendulum).mul((Matrix4fc)rigidPose)) ? result : new Matrix4f();
    }

    private static boolean isFiniteMatrix(Matrix4f matrix) {
        return matrix != null && Float.isFinite(matrix.m00()) && Float.isFinite(matrix.m01()) && Float.isFinite(matrix.m02()) && Float.isFinite(matrix.m03()) && Float.isFinite(matrix.m10()) && Float.isFinite(matrix.m11()) && Float.isFinite(matrix.m12()) && Float.isFinite(matrix.m13()) && Float.isFinite(matrix.m20()) && Float.isFinite(matrix.m21()) && Float.isFinite(matrix.m22()) && Float.isFinite(matrix.m23()) && Float.isFinite(matrix.m30()) && Float.isFinite(matrix.m31()) && Float.isFinite(matrix.m32()) && Float.isFinite(matrix.m33());
    }

    private static boolean detectShaderStateStable() {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return false;
        }
        Boolean irisState = PendulumPhysics.queryIrisShaderPackInUse();
        if (irisState != null) {
            return irisState;
        }
        try {
            return class_310.method_29611();
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static Boolean queryIrisShaderPackInUse() {
        return IrisShaderPackCompat.queryShaderPackInUse();
    }

    public Matrix4f getLastAppliedMatrix(class_1306 arm) {
        if (arm == null) {
            return null;
        }
        StateKey stateKey = new StateKey(arm, "default");
        State state = this.states.get(stateKey);
        if (state == null || state.lastAppliedFrameId != this.frameId) {
            return null;
        }
        Matrix4f matrix = this.lastApplied.get(stateKey);
        return matrix == null ? null : new Matrix4f((Matrix4fc)matrix);
    }

    private static float clampAngle(float value, float limit) {
        if (value > limit) {
            return limit;
        }
        if (value < -limit) {
            return -limit;
        }
        return value;
    }

    private void reset() {
        this.lastYaw = Float.NaN;
        this.lastPitch = Float.NaN;
        this.frameValid = false;
        this.frameId = 0L;
        this.states.clear();
    }

    private static ResolvedSettings resolveSettings(ToolTuning.PhysicsSettings overrides) {
        ResolvedSettings settings = new ResolvedSettings();
        settings.limitDeg = PENDULUM_LIMIT_DEG;
        settings.limitDegUp = PENDULUM_LIMIT_DEG;
        settings.limitDegDown = PENDULUM_LIMIT_DEG;
        settings.force = PENDULUM_FORCE;
        settings.speed = PENDULUM_SPEED;
        settings.cameraSens = PENDULUM_CAMERA_SENS;
        settings.gravity = PENDULUM_GRAVITY;
        settings.motionSens = PENDULUM_MOTION_SENS;
        settings.invert = false;
        boolean hasUpDown = false;
        boolean hasLimitDeg = false;
        if (overrides != null) {
            if (overrides.limitDeg != null) {
                settings.limitDeg = overrides.limitDeg.floatValue();
                hasLimitDeg = true;
            }
            if (overrides.limitDegUp != null) {
                settings.limitDegUp = overrides.limitDegUp.floatValue();
                hasUpDown = true;
            }
            if (overrides.limitDegDown != null) {
                settings.limitDegDown = overrides.limitDegDown.floatValue();
                hasUpDown = true;
            }
            if (overrides.force != null) {
                settings.force = overrides.force.floatValue();
            }
            if (overrides.speed != null) {
                settings.speed = overrides.speed.floatValue();
            }
            if (overrides.cameraSens != null) {
                settings.cameraSens = overrides.cameraSens.floatValue();
            }
            if (overrides.gravity != null) {
                settings.gravity = overrides.gravity.floatValue();
            }
            if (overrides.motionSens != null) {
                settings.motionSens = overrides.motionSens.floatValue();
            }
            if (overrides.invert != null) {
                settings.invert = overrides.invert;
            }
        }
        if (hasLimitDeg && !hasUpDown) {
            settings.limitDegUp = settings.limitDeg;
            settings.limitDegDown = settings.limitDeg;
        } else if (!hasUpDown && settings.limitDeg > 0.0f) {
            settings.limitDegUp = settings.limitDeg;
            settings.limitDegDown = settings.limitDeg;
        }
        settings.hasExplicitAngularLimits = hasLimitDeg || hasUpDown;
        return settings;
    }

    private record StateKey(class_1306 arm, String channel) {
    }

    private static final class State {
        private static final float WALK_TO_YAW = 18.0f;
        private static final float WALK_TO_PITCH = 16.0f;
        private static final float WALK_BOB_TO_YAW = 12.0f;
        private static final float WALK_MOTION_REFERENCE_RATE = 60.0f;
        private static final float JUMP_TO_PITCH = 36.0f;
        private static final float IMPACT_TO_PITCH = 42.0f;
        private static final float SHIELD_TO_PITCH = 30.0f;
        private static final float ACTION_ANIM_TO_PITCH = 34.0f;
        private static final float ACTION_ANIM_KICK_TO_PITCH = 220.0f;
        private static final float ACTION_ANIM_KICK_ANGLE = 1.8f;
        private static final float VERTICAL_DV_TO_PITCH = 460.0f;
        private static final float VERTICAL_IMPULSE_MAX = 120.0f;
        private static final float TAKEOFF_IMPULSE = 180.0f;
        private static final float LANDING_IMPULSE = 240.0f;
        private static final float TAKEOFF_ANGLE_KICK = 4.5f;
        private static final float LANDING_ANGLE_KICK = 6.0f;
        private static final float VERTICAL_VEL_TO_TARGET = 28.0f;
        private static final float VERTICAL_TARGET_MAX = 18.0f;
        private static final float VERTICAL_REFERENCE_DT = 0.016666668f;
        private static final float GROUND_TRANSITION_COOLDOWN_S = 0.08f;
        float angleX;
        float angleZ;
        float prevAngleX;
        float prevAngleZ;
        float velocityX;
        float velocityZ;
        float lastOffsetX;
        float lastOffsetZ;
        float lastJumpValue;
        float lastImpactValue;
        float lastShieldImpactValue;
        float lastActionAnimValue;
        float lastVelY;
        boolean motionInitialized;
        boolean verticalInitialized;
        boolean prevOnGround = true;
        float groundTransitionCooldown;
        boolean hitLimitX;
        boolean hitLimitZ;
        long lastFrameId = -1L;
        long lastAppliedFrameId = -1L;

        private State() {
        }

        boolean isFinite() {
            return Float.isFinite(this.angleX) && Float.isFinite(this.angleZ) && Float.isFinite(this.prevAngleX) && Float.isFinite(this.prevAngleZ) && Float.isFinite(this.velocityX) && Float.isFinite(this.velocityZ) && Float.isFinite(this.lastOffsetX) && Float.isFinite(this.lastOffsetZ) && Float.isFinite(this.lastJumpValue) && Float.isFinite(this.lastImpactValue) && Float.isFinite(this.lastShieldImpactValue) && Float.isFinite(this.lastActionAnimValue) && Float.isFinite(this.lastVelY) && Float.isFinite(this.groundTransitionCooldown);
        }

        void resetDynamic() {
            this.angleX = 0.0f;
            this.angleZ = 0.0f;
            this.prevAngleX = 0.0f;
            this.prevAngleZ = 0.0f;
            this.velocityX = 0.0f;
            this.velocityZ = 0.0f;
            this.lastOffsetX = 0.0f;
            this.lastOffsetZ = 0.0f;
            this.lastJumpValue = 0.0f;
            this.lastImpactValue = 0.0f;
            this.lastShieldImpactValue = 0.0f;
            this.lastActionAnimValue = 0.0f;
            this.lastVelY = 0.0f;
            this.motionInitialized = false;
            this.verticalInitialized = false;
            this.prevOnGround = true;
            this.groundTransitionCooldown = 0.0f;
            this.hitLimitX = false;
            this.hitLimitZ = false;
            this.lastAppliedFrameId = -1L;
        }

        void update(float dt, float yawDeltaDeg, float pitchDeltaDeg, float playerPitchDeg, float referencePitchDeg, float walkSpeed, float walkPos, float velY, boolean onGround, ResolvedSettings settings, class_1306 arm) {
            if (!Float.isFinite(referencePitchDeg)) {
                referencePitchDeg = 0.0f;
            }
            float rebound = 0.1f;
            float limitDamp = 0.25f;
            float dampingBase = 2.2f;
            float settleEps = 2.5E-4f;
            this.prevAngleX = this.angleX;
            this.prevAngleZ = this.angleZ;
            float walkSprintScale = MovementStateMachine.get().getWalkAndSprintForceScale();
            float othersScale = MovementStateMachine.get().getRemainingProceduralForceScale();
            float inputYaw = -yawDeltaDeg * settings.cameraSens * othersScale;
            float inputPitch = -pitchDeltaDeg * settings.cameraSens * othersScale;
            float inertia = settings.force * 20.0f;
            this.velocityZ -= inputYaw * inertia * 5.0f;
            this.velocityX += inputPitch * inertia;
            MovementStateMachine movement = MovementStateMachine.get();
            if (movement != null) {
                float offsetX = movement.getOffsetX();
                float offsetZ = movement.getOffsetZ();
                float jumpValue = movement.getJumpValue();
                float impactValue = movement.getImpactValue();
                float shieldImpactValue = movement.getShieldImpactValue();
                float actionAnimValue = movement.getActionAnimValue(arm);
                float actionAnimKick = movement.consumeActionAnimKick(arm);
                if (!this.motionInitialized) {
                    this.lastOffsetX = offsetX;
                    this.lastOffsetZ = offsetZ;
                    this.lastJumpValue = jumpValue;
                    this.lastImpactValue = impactValue;
                    this.lastShieldImpactValue = shieldImpactValue;
                    this.lastActionAnimValue = actionAnimValue;
                    this.motionInitialized = true;
                }
                float deltaOffsetX = offsetX - this.lastOffsetX;
                float deltaOffsetZ = offsetZ - this.lastOffsetZ;
                float deltaJump = (jumpValue - this.lastJumpValue) * othersScale;
                float deltaImpact = (impactValue - this.lastImpactValue) * othersScale;
                float deltaShield = (shieldImpactValue - this.lastShieldImpactValue) * othersScale;
                float deltaActionAnim = (actionAnimValue - this.lastActionAnimValue) * othersScale;
                this.lastOffsetX = offsetX;
                this.lastOffsetZ = offsetZ;
                this.lastJumpValue = jumpValue;
                this.lastImpactValue = impactValue;
                this.lastShieldImpactValue = shieldImpactValue;
                this.lastActionAnimValue = actionAnimValue;
                float walkYawVelocity = deltaOffsetX * 60.0f;
                float walkPitchVelocity = deltaOffsetZ * 60.0f;
                if (settings.motionSens != 0.0f) {
                    float motionYaw = walkYawVelocity * 18.0f * walkSprintScale;
                    float motionPitch = walkPitchVelocity * 16.0f * walkSprintScale + deltaJump * 36.0f + deltaImpact * 42.0f + deltaShield * 30.0f;
                    if (walkSpeed > 0.01f) {
                        float walkBob = (float)Math.sin(walkPos * 1.1f) * walkSpeed * walkSprintScale;
                        motionYaw += walkBob * 12.0f;
                    }
                    this.velocityZ -= motionYaw * settings.motionSens;
                    this.velocityX += motionPitch * settings.motionSens;
                }
                this.velocityX += deltaActionAnim * 34.0f;
                if (actionAnimKick != 0.0f) {
                    float scaledKick = actionAnimKick * othersScale;
                    this.velocityX += scaledKick * 220.0f;
                    this.angleX += scaledKick * 1.8f;
                }
            }
            float motionWeight = Math.max(0.55f, Math.abs(settings.motionSens));
            if (this.groundTransitionCooldown > 0.0f) {
                this.groundTransitionCooldown = Math.max(0.0f, this.groundTransitionCooldown - dt);
            }
            if (!this.verticalInitialized) {
                this.lastVelY = velY;
                this.prevOnGround = onGround;
                this.verticalInitialized = true;
            } else {
                float deltaVelY = velY - this.lastVelY;
                this.lastVelY = velY;
                float dtScale = class_3532.method_15363((float)(0.016666668f / Math.max(dt, 1.0E-4f)), (float)0.35f, (float)1.25f);
                float verticalImpulse = class_3532.method_15363((float)(-deltaVelY * 460.0f * dtScale), (float)-120.0f, (float)120.0f) * othersScale;
                this.velocityX += verticalImpulse;
                if (this.groundTransitionCooldown <= 0.0f && this.prevOnGround && !onGround && velY > 0.08f) {
                    this.angleX -= 4.5f * othersScale;
                    this.velocityX -= 180.0f * othersScale;
                    this.groundTransitionCooldown = 0.08f;
                } else if (this.groundTransitionCooldown <= 0.0f && !this.prevOnGround && onGround) {
                    this.angleX += 6.0f * othersScale;
                    this.velocityX += 240.0f * othersScale;
                    this.groundTransitionCooldown = 0.08f;
                }
                this.prevOnGround = onGround;
            }
            float targetPitch = (playerPitchDeg - referencePitchDeg) * othersScale;
            float verticalTargetPitch = class_3532.method_15363((float)(-velY * 28.0f), (float)-18.0f, (float)18.0f) * othersScale;
            targetPitch += verticalTargetPitch * motionWeight;
            if (settings.hasExplicitAngularLimits || settings.limitDegUp > 0.0f || settings.limitDegDown > 0.0f) {
                float up = settings.limitDegUp > 0.0f ? settings.limitDegUp : settings.limitDegDown;
                float down = settings.limitDegDown > 0.0f ? settings.limitDegDown : settings.limitDegUp;
                targetPitch = class_3532.method_15363((float)targetPitch, (float)(-down), (float)up);
            }
            float accelX = (targetPitch - this.angleX) * settings.gravity;
            float accelZ = -this.angleZ * (settings.gravity * 0.6f);
            float damping = 2.2f / Math.max(settings.speed, 0.001f);
            this.velocityX += (accelX - this.velocityX * damping) * dt;
            this.velocityZ += (accelZ - this.velocityZ * damping) * dt;
            float velocityDecay = (float)Math.exp(-damping * dt);
            this.velocityX *= velocityDecay;
            this.velocityZ *= velocityDecay;
            this.angleX += this.velocityX * dt;
            this.angleZ += this.velocityZ * dt;
            if (settings.hasExplicitAngularLimits || settings.limitDegUp > 0.0f || settings.limitDegDown > 0.0f) {
                boolean crossedLimitZ;
                float limitUp = settings.limitDegUp > 0.0f ? settings.limitDegUp : settings.limitDegDown;
                float limitDown = settings.limitDegDown > 0.0f ? settings.limitDegDown : settings.limitDegUp;
                float limitZ = Math.max(limitUp, limitDown);
                if (settings.hasExplicitAngularLimits && limitUp == 0.0f && limitDown == 0.0f) {
                    this.angleX = 0.0f;
                    this.velocityX = 0.0f;
                }
                if (settings.hasExplicitAngularLimits && limitZ == 0.0f) {
                    this.angleZ = 0.0f;
                    this.velocityZ = 0.0f;
                }
                boolean crossedLimitX = this.prevAngleX > -limitDown && this.prevAngleX < limitUp;
                boolean bl = crossedLimitZ = Math.abs(this.prevAngleZ) < limitZ;
                if (this.angleX > limitUp) {
                    this.angleX = limitUp;
                    if (crossedLimitX && this.velocityX > 0.0f) {
                        this.velocityX = -this.velocityX * 0.1f;
                        this.velocityX *= 0.25f;
                    }
                } else if (this.angleX < -limitDown) {
                    this.angleX = -limitDown;
                    if (crossedLimitX && this.velocityX < 0.0f) {
                        this.velocityX = -this.velocityX * 0.1f;
                        this.velocityX *= 0.25f;
                    }
                }
                if (this.angleZ > limitZ) {
                    this.angleZ = limitZ;
                    if (crossedLimitZ && this.velocityZ > 0.0f) {
                        this.velocityZ = -this.velocityZ * 0.1f;
                        this.velocityZ *= 0.25f;
                    }
                } else if (this.angleZ < -limitZ) {
                    this.angleZ = -limitZ;
                    if (crossedLimitZ && this.velocityZ < 0.0f) {
                        this.velocityZ = -this.velocityZ * 0.1f;
                        this.velocityZ *= 0.25f;
                    }
                }
            }
            if (Math.abs(this.angleX) < 2.5E-4f && Math.abs(this.velocityX) < 2.5E-4f) {
                this.angleX = 0.0f;
                this.velocityX = 0.0f;
            }
            if (Math.abs(this.angleZ) < 2.5E-4f && Math.abs(this.velocityZ) < 2.5E-4f) {
                this.angleZ = 0.0f;
                this.velocityZ = 0.0f;
            }
        }
    }

    private static final class ResolvedSettings {
        float limitDeg;
        float limitDegUp;
        float limitDegDown;
        float force;
        float speed;
        float cameraSens;
        float gravity;
        float motionSens;
        boolean invert;
        boolean hasExplicitAngularLimits;

        private ResolvedSettings() {
        }
    }
}

