/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_746
 */
package punchy.client.render;

import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public class AxolotlPhysics {
    private static final float ROLL_STIFFNESS = 0.18f;
    private static final float ROLL_DAMPING = 0.75f;
    private static final float ROLL_MAX_VELOCITY = 0.18f;
    private static final float ROLL_YAW_SCALE = 0.7f;
    private static final float LIFT_STIFFNESS = 0.12f;
    private static final float LIFT_DAMPING = 0.8f;
    private static final float LIFT_MAX_VELOCITY = 0.12f;
    private static final float LIFT_FALL_SCALE = 0.6f;
    private static final float LIFT_FALL_MAX = 0.28f;
    private static final float LIFT_MIN_FALL_SPEED = 0.05f;
    private static final float LANDING_SLOW_TIME = 0.2f;
    private static final float LANDING_RETURN_MULT = 0.5f;
    private static final float MAX_DT = 0.1f;
    private final SpringAxis rollSpring = new SpringAxis(0.18f, 0.75f, 0.18f);
    private final SpringAxis liftSpring = new SpringAxis(0.12f, 0.8f, 0.12f);
    private float roll;
    private float lift;
    private float lastYaw = Float.NaN;
    private long lastUpdateTime = System.nanoTime();
    private boolean prevOnGround = true;
    private float landingSlowTimer;

    public void update(class_746 player) {
        long now = System.nanoTime();
        float dt = (float)(now - this.lastUpdateTime) / 1.0E9f;
        this.lastUpdateTime = now;
        if (dt <= 0.0f) {
            return;
        }
        if (dt > 0.1f) {
            dt = 0.1f;
        }
        float rollTarget = 0.0f;
        float liftTarget = 0.0f;
        if (player != null) {
            float yaw = AxolotlPhysics.resolveCameraYaw(player);
            if (Float.isNaN(this.lastYaw)) {
                this.lastYaw = yaw;
            }
            float yawDelta = class_3532.method_15393((float)(yaw - this.lastYaw));
            this.lastYaw = yaw;
            rollTarget = (float)Math.toRadians(-yawDelta) * 0.7f;
            float velY = (float)player.method_18798().field_1351;
            if (velY < -0.05f) {
                liftTarget = class_3532.method_15363((float)(-velY * 0.6f), (float)0.0f, (float)0.28f);
            }
            boolean onGround = player.method_24828();
            if (!this.prevOnGround && onGround) {
                this.landingSlowTimer = 0.2f;
            }
            this.prevOnGround = onGround;
        } else {
            this.lastYaw = Float.NaN;
            this.prevOnGround = true;
            this.landingSlowTimer = 0.0f;
        }
        this.roll = this.rollSpring.update(rollTarget);
        if (this.landingSlowTimer > 0.0f) {
            this.landingSlowTimer = Math.max(0.0f, this.landingSlowTimer - dt);
        }
        float liftStiffness = 0.12f;
        float liftDamping = 0.8f;
        float liftMaxVelocity = 0.12f;
        if (this.landingSlowTimer > 0.0f && liftTarget == 0.0f) {
            liftStiffness *= 0.5f;
            liftMaxVelocity *= 0.5f;
        }
        this.lift = this.liftSpring.update(liftTarget, liftStiffness, liftDamping, liftMaxVelocity);
    }

    public float getRoll() {
        return this.roll;
    }

    public float getLift() {
        return this.lift;
    }

    private static float resolveCameraYaw(class_746 player) {
        if (player == null) {
            return 0.0f;
        }
        class_243 view = player.method_5828(1.0f);
        if (view.method_1027() < 1.0E-6) {
            return player.method_36454();
        }
        return (float)Math.toDegrees(Math.atan2(-view.field_1352, view.field_1350));
    }

    private static final class SpringAxis {
        private final float stiffness;
        private final float damping;
        private final float maxVelocity;
        private float value;
        private float velocity;

        SpringAxis(float stiffness, float damping, float maxVelocity) {
            this.stiffness = stiffness;
            this.damping = damping;
            this.maxVelocity = maxVelocity;
        }

        float update(float target) {
            return this.update(target, this.stiffness, this.damping, this.maxVelocity);
        }

        float update(float target, float stiffness, float damping, float maxVelocity) {
            float accel = (target - this.value) * stiffness;
            this.velocity += accel;
            this.velocity = class_3532.method_15363((float)this.velocity, (float)(-maxVelocity), (float)maxVelocity);
            this.velocity *= damping;
            this.value += this.velocity;
            float delta = target - this.value;
            if (Math.signum(this.velocity) != Math.signum(delta)) {
                this.velocity *= 0.25f;
            }
            if (Math.abs(delta) < 0.001f && Math.abs(this.velocity) < 0.001f) {
                this.value = target;
                this.velocity = 0.0f;
            }
            return this.value;
        }
    }
}

