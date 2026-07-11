/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_746
 */
package punchy.client.bedrockitems;

import java.util.EnumMap;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import punchy.client.bedrockitems.BedrockItemDefinition;
import punchy.client.bedrockitems.BedrockItemSystem;

final class BedrockPufferPhysicsStateMachine {
    private static final float BASE_DT = 0.05f;
    private static final float TICK_TIME_SCALE = 2.16f;
    private static final float SPRING_INPUT = 0.32f;
    private static final float SPRING_GRAVITY = 0.055f;
    private static final float SPRING_DAMPING = 0.82f;
    private final EnumMap<class_1306, ArmState> states = new EnumMap(class_1306.class);

    BedrockPufferPhysicsStateMachine() {
    }

    void tick(class_310 client) {
        if (client == null || client.field_1724 == null) {
            for (class_1306 arm : class_1306.values()) {
                this.states.computeIfAbsent(arm, key -> new ArmState()).tickDisabled();
            }
            return;
        }
        for (class_1306 arm : class_1306.values()) {
            ArmState state = this.states.computeIfAbsent(arm, key -> new ArmState());
            class_1799 stack = BedrockPufferPhysicsStateMachine.resolveStack(client, arm);
            boolean enabled = state.resolveEnabled(client, stack);
            state.tick(client, enabled);
        }
    }

    FeedbackPose getPose(class_1306 arm, float partialTicks) {
        if (arm == null) {
            return null;
        }
        ArmState state = this.states.get(arm);
        if (state == null) {
            return null;
        }
        return state.sample(arm, partialTicks);
    }

    private static class_1799 resolveStack(class_310 client, class_1306 arm) {
        boolean mainRight;
        if (client == null || client.field_1724 == null || arm == null) {
            return class_1799.field_8037;
        }
        boolean bl = mainRight = client.field_1724.method_6068() == class_1306.field_6183;
        if (arm == class_1306.field_6183) {
            return mainRight ? client.field_1724.method_6047() : client.field_1724.method_6079();
        }
        return mainRight ? client.field_1724.method_6079() : client.field_1724.method_6047();
    }

    private static final class ArmState {
        private float weight;
        private float spring;
        private float springSpeed;
        private float wobbleTime;
        private float landingPunch;
        private float finLift;
        private float walkWeight;
        private float walkPhase;
        private float camLat;
        private float camLatSpeed;
        private float camVert;
        private float camVertSpeed;
        private float highFallTiltGate;
        private float highFallPitchDrive;
        private float prevYaw;
        private float prevPitch;
        private boolean cameraPrimed;
        private float prevWeight;
        private float prevSpring;
        private float prevWobbleTime;
        private float prevLandingPunch;
        private float prevLandingPitchVisual;
        private float prevFinLift;
        private float prevWalkWeight;
        private float prevWalkPhase;
        private float prevCamLat;
        private float prevCamVert;
        private float prevHighFallTiltGate;
        private float prevHighFallPitchDrive;
        private float jumpTiltCap;
        private float prevJumpTiltCap;
        private boolean prevHighFallMode;
        private boolean prevOnGround;
        private int airTicks;
        private float landingPitchVisual;
        private class_1799 lastResolvedStack = class_1799.field_8037;
        private boolean lastResolvedEnabled;

        private ArmState() {
        }

        boolean resolveEnabled(class_310 client, class_1799 stack) {
            if (stack == null) {
                stack = class_1799.field_8037;
            }
            if (!this.lastResolvedStack.method_7960() && class_1799.method_31577((class_1799)this.lastResolvedStack, (class_1799)stack)) {
                return this.lastResolvedEnabled;
            }
            BedrockItemDefinition def = BedrockItemSystem.get().resolveDefinition(stack);
            this.lastResolvedEnabled = def != null;
            this.lastResolvedStack = stack.method_7972();
            return this.lastResolvedEnabled;
        }

        void tick(class_310 client, boolean enabled) {
            this.prevWeight = this.weight;
            this.prevSpring = this.spring;
            this.prevWobbleTime = this.wobbleTime;
            this.prevLandingPunch = this.landingPunch;
            this.prevLandingPitchVisual = this.landingPitchVisual;
            this.prevFinLift = this.finLift;
            this.prevWalkWeight = this.walkWeight;
            this.prevWalkPhase = this.walkPhase;
            this.prevCamLat = this.camLat;
            this.prevCamVert = this.camVert;
            this.prevHighFallTiltGate = this.highFallTiltGate;
            this.prevHighFallPitchDrive = this.highFallPitchDrive;
            this.prevJumpTiltCap = this.jumpTiltCap;
            if (!enabled || client == null || client.field_1724 == null) {
                this.tickDisabled();
                return;
            }
            class_746 player = client.field_1724;
            float vy = (float)player.method_18798().field_1351;
            float vx = (float)player.method_18798().field_1352;
            float vz = (float)player.method_18798().field_1350;
            float yaw = player.method_36454();
            float pitchLook = player.method_36455();
            boolean onGround = player.method_24828();
            float fallDistance = player.field_6017;
            boolean creativeFlight = player.method_31549().field_7479;
            boolean descending = -vy > 0.08f;
            boolean airbornePhysics = !onGround && !creativeFlight;
            boolean meaningfulFall = airbornePhysics && (descending || fallDistance > 1.25f);
            this.weight = class_3532.method_16439((float)ArmState.rate(0.48f), (float)this.weight, (float)1.0f);
            this.springSpeed += -vy * 0.32f * 2.16f;
            this.springSpeed -= 0.055f * this.spring * 2.16f;
            this.springSpeed *= (float)Math.pow(0.82f, 2.16f);
            this.spring += this.springSpeed * 2.16f;
            this.spring = class_3532.method_15363((float)this.spring, (float)-2.2f, (float)2.8f);
            boolean justLanded = false;
            if (!this.prevOnGround && onGround) {
                this.landingPunch = 1.0f;
                this.spring *= 0.5f;
                this.springSpeed *= 0.45f;
                this.springSpeed -= 0.55f;
                this.spring -= 0.28f;
                justLanded = true;
                this.airTicks = 0;
            } else {
                this.airTicks = onGround || creativeFlight ? 0 : Math.min(this.airTicks + 1, 200);
            }
            if (!justLanded) {
                this.landingPunch = class_3532.method_16439((float)ArmState.rate(0.22f), (float)this.landingPunch, (float)0.0f);
            }
            float landingRise = ArmState.rate(0.07f);
            float landingFall = ArmState.rate(0.11f);
            this.landingPitchVisual = class_3532.method_16439((float)(this.landingPunch > this.landingPitchVisual ? landingRise : landingFall), (float)this.landingPitchVisual, (float)this.landingPunch);
            float highFallTarget = meaningfulFall ? 1.0f : 0.0f;
            this.highFallTiltGate = ArmState.moveTowards(this.highFallTiltGate, highFallTarget, 0.1f);
            boolean highFallMode = meaningfulFall;
            if (highFallMode) {
                float velNorm = class_3532.method_15363((float)((-vy - 0.05f) / 0.45f), (float)0.0f, (float)1.0f);
                float distNorm = class_3532.method_15363((float)((fallDistance - 5.0f) / 7.0f), (float)0.0f, (float)1.0f);
                float targetDrive = Math.max(velNorm, distNorm);
                if (!this.prevHighFallMode) {
                    float springSeed = class_3532.method_15363((float)(Math.max(0.0f, this.spring) / 2.4f), (float)0.0f, (float)1.0f);
                    this.highFallPitchDrive = Math.max(this.highFallPitchDrive, springSeed);
                }
                float step = targetDrive >= this.highFallPitchDrive ? 0.03125f : 0.008333334f;
                this.highFallPitchDrive = ArmState.moveTowards(this.highFallPitchDrive, targetDrive, step);
            } else {
                this.highFallPitchDrive = ArmState.moveTowards(this.highFallPitchDrive, 0.0f, 0.06666667f);
            }
            if (airbornePhysics) {
                float jumpRef = Math.abs(this.spring * 11.5f);
                this.jumpTiltCap = Math.max(this.jumpTiltCap, jumpRef);
            } else if (!justLanded) {
                this.jumpTiltCap = class_3532.method_16439((float)ArmState.rate(0.3f), (float)this.jumpTiltCap, (float)0.0f);
            }
            float finTarget = meaningfulFall && this.airTicks >= 2 ? 1.0f : 0.0f;
            float finStep = finTarget > this.finLift ? 0.055555556f : 0.0625f;
            this.finLift = ArmState.moveTowards(this.finLift, finTarget, finStep);
            float horizontalSpeed = class_3532.method_15355((float)(vx * vx + vz * vz));
            float walkTarget = onGround && horizontalSpeed > 0.02f ? class_3532.method_15363((float)((horizontalSpeed - 0.02f) / 0.1f), (float)0.0f, (float)1.0f) : 0.0f;
            this.walkWeight = class_3532.method_16439((float)ArmState.rate(onGround ? 0.24f : 0.36f), (float)this.walkWeight, (float)walkTarget);
            this.walkPhase += 0.108f * (1.2f + this.walkWeight * 3.0f);
            if (!this.cameraPrimed) {
                this.prevYaw = yaw;
                this.prevPitch = pitchLook;
                this.cameraPrimed = true;
            }
            float yawDelta = class_3532.method_15393((float)(yaw - this.prevYaw));
            float pitchDelta = pitchLook - this.prevPitch;
            this.prevYaw = yaw;
            this.prevPitch = pitchLook;
            float yawInput = Math.abs(yawDelta) > 0.9f ? yawDelta : 0.0f;
            float pitchInput = Math.abs(pitchDelta) > 0.9f ? pitchDelta : 0.0f;
            float sensitivity = 0.019f;
            float cameraSpring = 0.14f;
            float camLatTarget = class_3532.method_15363((float)(-yawInput * sensitivity), (float)-1.1f, (float)1.1f);
            float camVertTarget = class_3532.method_15363((float)(-pitchInput * sensitivity), (float)-1.0f, (float)1.0f);
            this.camLatSpeed += camLatTarget * cameraSpring * 2.16f;
            this.camVertSpeed += camVertTarget * cameraSpring * 2.16f;
            this.camLatSpeed -= 0.07f * this.camLat * 2.16f;
            this.camVertSpeed -= 0.07f * this.camVert * 2.16f;
            this.camLatSpeed *= (float)Math.pow(0.84f, 2.16f);
            this.camVertSpeed *= (float)Math.pow(0.84f, 2.16f);
            this.camLat += this.camLatSpeed * 2.16f;
            this.camVert += this.camVertSpeed * 2.16f;
            this.camLat = class_3532.method_15363((float)this.camLat, (float)-1.1f, (float)1.1f);
            this.camVert = class_3532.method_15363((float)this.camVert, (float)-1.0f, (float)1.0f);
            this.wobbleTime += 0.108f * (1.1f + Math.abs(this.spring) * 0.7f);
            this.prevOnGround = onGround;
            this.prevHighFallMode = highFallMode;
        }

        void tickDisabled() {
            this.prevWeight = this.weight;
            this.prevSpring = this.spring;
            this.prevWobbleTime = this.wobbleTime;
            this.prevLandingPunch = this.landingPunch;
            this.prevLandingPitchVisual = this.landingPitchVisual;
            this.prevFinLift = this.finLift;
            this.prevWalkWeight = this.walkWeight;
            this.prevWalkPhase = this.walkPhase;
            this.prevCamLat = this.camLat;
            this.prevCamVert = this.camVert;
            this.prevHighFallTiltGate = this.highFallTiltGate;
            this.prevHighFallPitchDrive = this.highFallPitchDrive;
            this.prevJumpTiltCap = this.jumpTiltCap;
            this.weight = class_3532.method_16439((float)ArmState.rate(0.28f), (float)this.weight, (float)0.0f);
            this.springSpeed = class_3532.method_16439((float)ArmState.rate(0.28f), (float)this.springSpeed, (float)0.0f);
            this.spring = class_3532.method_16439((float)ArmState.rate(0.28f), (float)this.spring, (float)0.0f);
            this.landingPunch = class_3532.method_16439((float)ArmState.rate(0.24f), (float)this.landingPunch, (float)0.0f);
            this.landingPitchVisual = class_3532.method_16439((float)ArmState.rate(0.12f), (float)this.landingPitchVisual, (float)this.landingPunch);
            this.finLift = class_3532.method_16439((float)ArmState.rate(0.4f), (float)this.finLift, (float)0.0f);
            this.walkWeight = class_3532.method_16439((float)ArmState.rate(0.36f), (float)this.walkWeight, (float)0.0f);
            this.walkPhase += 0.108f;
            this.camLatSpeed = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camLatSpeed, (float)0.0f);
            this.camVertSpeed = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camVertSpeed, (float)0.0f);
            this.camLat = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camLat, (float)0.0f);
            this.camVert = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camVert, (float)0.0f);
            this.highFallTiltGate = ArmState.moveTowards(this.highFallTiltGate, 0.0f, 0.06666667f);
            this.highFallPitchDrive = ArmState.moveTowards(this.highFallPitchDrive, 0.0f, 0.06666667f);
            this.jumpTiltCap = class_3532.method_16439((float)ArmState.rate(0.32f), (float)this.jumpTiltCap, (float)0.0f);
            this.wobbleTime += 0.108f;
            this.airTicks = 0;
            this.prevOnGround = false;
            this.cameraPrimed = false;
            this.prevHighFallMode = false;
        }

        FeedbackPose sample(class_1306 arm, float partialTicks) {
            float t = class_3532.method_15363((float)partialTicks, (float)0.0f, (float)1.0f);
            float w = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevWeight, (float)this.weight), (float)0.0f, (float)1.0f);
            float s = class_3532.method_16439((float)t, (float)this.prevSpring, (float)this.spring);
            float wt = class_3532.method_16439((float)t, (float)this.prevWobbleTime, (float)this.wobbleTime);
            float punch = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevLandingPunch, (float)this.landingPunch), (float)0.0f, (float)1.0f);
            float landingPitch = ArmState.smoothStep01(class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevLandingPitchVisual, (float)this.landingPitchVisual), (float)0.0f, (float)1.0f));
            float fin = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevFinLift, (float)this.finLift), (float)0.0f, (float)1.0f);
            fin = ArmState.smootherStep01(fin);
            float walkW = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevWalkWeight, (float)this.walkWeight), (float)0.0f, (float)1.0f);
            float walkT = class_3532.method_16439((float)t, (float)this.prevWalkPhase, (float)this.walkPhase);
            float camLatNow = class_3532.method_16439((float)t, (float)this.prevCamLat, (float)this.camLat);
            float camVertNow = class_3532.method_16439((float)t, (float)this.prevCamVert, (float)this.camVert);
            float wave = (float)Math.sin(wt * 8.2f) * 0.012f * w;
            float walkBob = (float)Math.sin(walkT) * 0.5f * walkW;
            float walkRoll = (float)Math.sin(walkT + 0.75f) * 3.0f * walkW;
            float camLatNorm = class_3532.method_15363((float)(camLatNow / 1.1f), (float)-1.0f, (float)1.0f);
            float camLatEased = ArmState.easeInOutSigned(camLatNorm) * 1.1f;
            float camVertNorm = class_3532.method_15363((float)camVertNow, (float)-1.0f, (float)1.0f);
            float camVertEased = ArmState.easeInOutSigned(camVertNorm);
            float armSign = arm == class_1306.field_6182 ? -1.0f : 1.0f;
            float translateX = camLatEased * 0.24f * w * armSign;
            float translateY = (s * 0.62f - punch * 0.92f) * w + walkBob - camVertEased * 0.2f * w;
            float translateZ = s * 0.26f * w;
            float highFallTiltNow = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevHighFallTiltGate, (float)this.highFallTiltGate), (float)0.0f, (float)1.0f);
            float highFallPitchDriveNow = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevHighFallPitchDrive, (float)this.highFallPitchDrive), (float)0.0f, (float)1.0f);
            float jumpCapNow = Math.max(0.0f, class_3532.method_16439((float)t, (float)this.prevJumpTiltCap, (float)this.jumpTiltCap));
            float highFallMul = class_3532.method_16439((float)highFallTiltNow, (float)1.0f, (float)1.22f);
            float fallPitchSource = Math.max(0.0f, s);
            float fallPitchTerm = (fallPitchSource * 11.5f + highFallPitchDriveNow * 5.0f) * highFallMul * w;
            float rawPitch = (fallPitchTerm *= 1.0f - landingPitch) - landingPitch * 30.0f * w - camVertEased * 8.0f * w;
            float pitchCap = Math.max(2.0f, Math.max(8.0f + landingPitch * 15.0f, jumpCapNow));
            float pitch = class_3532.method_15363((float)rawPitch, (float)(-pitchCap), (float)pitchCap);
            float roll = (float)Math.sin(wt * 1.6f) * 1.8f * w + walkRoll + camLatEased * 5.0f * w;
            return new FeedbackPose(translateX, translateY, translateZ, pitch, roll, 1.0f, 1.0f, 1.0f, fin, Math.abs(s) * w);
        }

        private static float rate(float base) {
            float b = class_3532.method_15363((float)base, (float)0.0f, (float)0.999f);
            return 1.0f - (float)Math.pow(1.0f - b, 2.16f);
        }

        private static float easeInOutSigned(float x) {
            float a = class_3532.method_15363((float)Math.abs(x), (float)0.0f, (float)1.0f);
            float eased = 0.5f - 0.5f * (float)Math.cos(Math.PI * (double)a);
            return Math.signum(x) * eased;
        }

        private static float moveTowards(float current, float target, float step) {
            float s = Math.abs(step);
            if (current < target) {
                return Math.min(current + s, target);
            }
            return Math.max(current - s, target);
        }

        private static float smoothStep01(float t) {
            float x = class_3532.method_15363((float)t, (float)0.0f, (float)1.0f);
            return x * x * (3.0f - 2.0f * x);
        }

        private static float smootherStep01(float t) {
            float x = class_3532.method_15363((float)t, (float)0.0f, (float)1.0f);
            return x * x * x * (x * (x * 6.0f - 15.0f) + 10.0f);
        }
    }

    record FeedbackPose(float translateXUnits, float translateYUnits, float translateZUnits, float pitchDeg, float rollDeg, float scaleX, float scaleY, float scaleZ, float finLift, float impactWave) {
        boolean isIdentity() {
            return Math.abs(this.translateXUnits) < 1.0E-4f && Math.abs(this.translateYUnits) < 1.0E-4f && Math.abs(this.translateZUnits) < 1.0E-4f && Math.abs(this.pitchDeg) < 1.0E-4f && Math.abs(this.rollDeg) < 1.0E-4f && Math.abs(this.scaleX - 1.0f) < 1.0E-4f && Math.abs(this.scaleY - 1.0f) < 1.0E-4f && Math.abs(this.scaleZ - 1.0f) < 1.0E-4f && Math.abs(this.finLift) < 1.0E-4f && Math.abs(this.impactWave) < 1.0E-4f;
        }
    }
}

