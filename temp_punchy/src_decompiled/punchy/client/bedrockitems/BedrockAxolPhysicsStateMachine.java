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

final class BedrockAxolPhysicsStateMachine {
    private static final float BASE_DT = 0.05f;
    private static final float TICK_TIME_SCALE = 2.16f;
    private static final float FALL_INTENSITY = 0.24f;
    private static final float FALL_GRAVITY = 0.04f;
    private static final float FALL_DAMPING = 0.85f;
    private final EnumMap<class_1306, ArmState> states = new EnumMap(class_1306.class);

    BedrockAxolPhysicsStateMachine() {
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
            class_1799 stack = BedrockAxolPhysicsStateMachine.resolveStack(client, arm);
            boolean enabled = state.resolveEnabled(stack);
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
        private float fall;
        private float fallSpeed;
        private float fallFastGate;
        private float wobbleTime;
        private float prevWeight;
        private float prevFall;
        private float prevFallSpeed;
        private float prevFallFastGate;
        private float prevArmRaiseGate;
        private float prevLandingPunch;
        private float prevWobbleTime;
        private float prevWalkWeight;
        private float prevWalkPhase;
        private float prevCamLat;
        private float prevCamVert;
        private float prevPitchVisual;
        private float lastVy;
        private float armRaiseGate;
        private float landingPunch;
        private float walkWeight;
        private float walkPhase;
        private float camLat;
        private float camLatSpeed;
        private float camVert;
        private float camVertSpeed;
        private float pitchVisual;
        private float prevYaw;
        private float prevPitch;
        private boolean cameraPrimed;
        private int airTicks;
        private boolean prevOnGround;
        private class_1799 lastResolvedStack = class_1799.field_8037;
        private boolean lastResolvedEnabled;

        private ArmState() {
        }

        boolean resolveEnabled(class_1799 stack) {
            if (stack == null) {
                stack = class_1799.field_8037;
            }
            if (!this.lastResolvedStack.method_7960() && class_1799.method_31577((class_1799)this.lastResolvedStack, (class_1799)stack)) {
                return this.lastResolvedEnabled;
            }
            BedrockItemDefinition def = BedrockItemSystem.get().resolveDefinition(stack);
            this.lastResolvedEnabled = def != null && def.isPhysicsFeedbackEnabled();
            this.lastResolvedStack = stack.method_7972();
            return this.lastResolvedEnabled;
        }

        void tick(class_310 client, boolean enabled) {
            this.prevWeight = this.weight;
            this.prevFall = this.fall;
            this.prevFallSpeed = this.fallSpeed;
            this.prevFallFastGate = this.fallFastGate;
            this.prevArmRaiseGate = this.armRaiseGate;
            this.prevLandingPunch = this.landingPunch;
            this.prevWobbleTime = this.wobbleTime;
            this.prevWalkWeight = this.walkWeight;
            this.prevWalkPhase = this.walkPhase;
            this.prevCamLat = this.camLat;
            this.prevCamVert = this.camVert;
            this.prevPitchVisual = this.pitchVisual;
            class_746 player = client.field_1724;
            float vy = (float)player.method_18798().field_1351;
            float vx = (float)player.method_18798().field_1352;
            float vz = (float)player.method_18798().field_1350;
            float yaw = player.method_36454();
            float pitchLook = player.method_36455();
            boolean onGround = player.method_24828();
            float fallDistance = player.field_6017;
            if (!enabled) {
                this.tickDisabled();
                return;
            }
            this.weight = class_3532.method_16439((float)ArmState.rate(0.42f), (float)this.weight, (float)1.0f);
            this.fallSpeed += -vy * 0.24f * 2.16f;
            this.fallSpeed -= 0.04f * this.fall * 2.16f;
            this.fallSpeed *= (float)Math.pow(0.85f, 2.16f);
            this.fall += this.fallSpeed * 2.16f;
            this.fall = class_3532.method_15363((float)this.fall, (float)-1.8f, (float)2.4f);
            this.fallFastGate = !onGround && -vy > 0.08f ? (this.fallFastGate += 0.2592f) : (this.fallFastGate -= 0.216f);
            this.fallFastGate = class_3532.method_15363((float)this.fallFastGate, (float)0.0f, (float)1.0f);
            float armTarget = !onGround && fallDistance >= 5.0f ? 1.0f : 0.0f;
            this.armRaiseGate = ArmState.moveTowards(this.armRaiseGate, armTarget, 0.06666667f);
            if (!onGround) {
                this.airTicks = Math.min(this.airTicks + 1, 200);
            }
            float horizontalSpeed = class_3532.method_15355((float)(vx * vx + vz * vz));
            float walkTarget = onGround && horizontalSpeed > 0.02f ? class_3532.method_15363((float)((horizontalSpeed - 0.02f) / 0.1f), (float)0.0f, (float)1.0f) : 0.0f;
            this.walkWeight = class_3532.method_16439((float)ArmState.rate(onGround ? 0.25f : 0.4f), (float)this.walkWeight, (float)walkTarget);
            this.walkPhase += 0.108f * (1.2f + this.walkWeight * 3.2f);
            if (!this.cameraPrimed) {
                this.prevYaw = yaw;
                this.prevPitch = pitchLook;
                this.cameraPrimed = true;
            }
            float yawDelta = class_3532.method_15393((float)(yaw - this.prevYaw));
            float pitchDelta = pitchLook - this.prevPitch;
            this.prevYaw = yaw;
            this.prevPitch = pitchLook;
            float yawAbs = Math.abs(yawDelta);
            float pitchAbs = Math.abs(pitchDelta);
            float yawInput = yawAbs > 0.9f ? yawDelta : 0.0f;
            float pitchInput = pitchAbs > 0.9f ? pitchDelta : 0.0f;
            float cameraSensitivity = 0.0224f;
            float cameraSpring = 0.108f;
            float camLatTarget = class_3532.method_15363((float)(-yawInput * cameraSensitivity), (float)-1.25f, (float)1.25f);
            float camVertTarget = class_3532.method_15363((float)(-pitchInput * cameraSensitivity), (float)-1.0f, (float)1.0f);
            this.camLatSpeed += camLatTarget * cameraSpring * 2.16f;
            this.camVertSpeed += camVertTarget * cameraSpring * 2.16f;
            this.camLatSpeed -= 0.06f * this.camLat * 2.16f;
            this.camVertSpeed -= 0.06f * this.camVert * 2.16f;
            this.camLatSpeed *= (float)Math.pow(0.85f, 2.16f);
            this.camVertSpeed *= (float)Math.pow(0.85f, 2.16f);
            this.camLat += this.camLatSpeed * 2.16f;
            this.camVert += this.camVertSpeed * 2.16f;
            this.camLat = class_3532.method_15363((float)this.camLat, (float)-1.25f, (float)1.25f);
            this.camVert = class_3532.method_15363((float)this.camVert, (float)-1.0f, (float)1.0f);
            boolean justLanded = false;
            if (!this.prevOnGround && onGround) {
                float impactBase = 0.95f;
                float largeT = class_3532.method_15363((float)(((float)this.airTicks - 3.0f) / 12.0f), (float)0.0f, (float)1.0f);
                float impactScale = 0.5f + 0.5f * largeT;
                float impact = impactBase * impactScale;
                this.fallSpeed -= impact * 1.65f;
                this.fall -= impact * 0.45f;
                this.fallSpeed *= 0.9f;
                this.landingPunch = 1.0f;
                justLanded = true;
                this.airTicks = 0;
            } else if (onGround) {
                this.airTicks = 0;
            }
            if (!justLanded) {
                this.landingPunch = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.landingPunch, (float)0.0f);
            }
            float wf = class_3532.method_15363((float)(this.fall * 0.4f), (float)0.0f, (float)2.0f) * (this.fallFastGate * this.fallFastGate);
            this.wobbleTime += 0.108f * (1.0f + wf * 1.8f);
            float camVertNormTick = class_3532.method_15363((float)this.camVert, (float)-1.0f, (float)1.0f);
            float camVertEasedTick = ArmState.easeInOutSigned(camVertNormTick);
            float fallSignedTick = class_3532.method_15363((float)(this.fall - camVertEasedTick * 0.9f), (float)-2.4f, (float)2.4f);
            float targetPitch = class_3532.method_15363((float)(16.8f * fallSignedTick * (1.0f - this.landingPunch * 1.25f) - 4.14f * this.landingPunch), (float)-44.0f, (float)44.0f) * this.weight;
            float pitchFollow = !onGround && fallDistance >= 5.0f ? ArmState.rate(0.07f) : ArmState.rate(0.16f);
            this.pitchVisual = class_3532.method_16439((float)pitchFollow, (float)this.pitchVisual, (float)targetPitch);
            this.prevOnGround = onGround;
            this.lastVy = vy;
        }

        void tickDisabled() {
            this.prevWeight = this.weight;
            this.prevFall = this.fall;
            this.prevFallSpeed = this.fallSpeed;
            this.prevFallFastGate = this.fallFastGate;
            this.prevArmRaiseGate = this.armRaiseGate;
            this.prevLandingPunch = this.landingPunch;
            this.prevWobbleTime = this.wobbleTime;
            this.prevWalkWeight = this.walkWeight;
            this.prevWalkPhase = this.walkPhase;
            this.prevCamLat = this.camLat;
            this.prevCamVert = this.camVert;
            this.prevPitchVisual = this.pitchVisual;
            this.weight = class_3532.method_16439((float)ArmState.rate(0.25f), (float)this.weight, (float)0.0f);
            this.fallSpeed = class_3532.method_16439((float)ArmState.rate(0.25f), (float)this.fallSpeed, (float)0.0f);
            this.fall = class_3532.method_16439((float)ArmState.rate(0.25f), (float)this.fall, (float)0.0f);
            this.fallFastGate = class_3532.method_16439((float)ArmState.rate(0.2f), (float)this.fallFastGate, (float)0.0f);
            this.armRaiseGate = ArmState.moveTowards(this.armRaiseGate, 0.0f, 0.06666667f);
            this.landingPunch = class_3532.method_16439((float)ArmState.rate(0.45f), (float)this.landingPunch, (float)0.0f);
            this.walkWeight = class_3532.method_16439((float)ArmState.rate(0.4f), (float)this.walkWeight, (float)0.0f);
            this.walkPhase += 0.108f;
            this.wobbleTime += 0.108f;
            this.camLatSpeed = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camLatSpeed, (float)0.0f);
            this.camLat = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camLat, (float)0.0f);
            this.camVertSpeed = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camVertSpeed, (float)0.0f);
            this.camVert = class_3532.method_16439((float)ArmState.rate(0.35f), (float)this.camVert, (float)0.0f);
            this.pitchVisual = class_3532.method_16439((float)ArmState.rate(0.2f), (float)this.pitchVisual, (float)0.0f);
            this.airTicks = 0;
            this.cameraPrimed = false;
        }

        FeedbackPose sample(class_1306 arm, float partialTicks) {
            float t = class_3532.method_15363((float)partialTicks, (float)0.0f, (float)1.0f);
            float w = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevWeight, (float)this.weight), (float)0.0f, (float)1.0f);
            float f = class_3532.method_16439((float)t, (float)this.prevFall, (float)this.fall);
            float ff = class_3532.method_16439((float)t, (float)this.prevFallFastGate, (float)this.fallFastGate);
            float armGateRaw = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevArmRaiseGate, (float)this.armRaiseGate), (float)0.0f, (float)1.0f);
            float armGate = ArmState.smoothStep01(armGateRaw);
            float punch = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevLandingPunch, (float)this.landingPunch), (float)0.0f, (float)1.0f);
            float wt = class_3532.method_16439((float)t, (float)this.prevWobbleTime, (float)this.wobbleTime);
            float walkW = class_3532.method_15363((float)class_3532.method_16439((float)t, (float)this.prevWalkWeight, (float)this.walkWeight), (float)0.0f, (float)1.0f);
            float walkT = class_3532.method_16439((float)t, (float)this.prevWalkPhase, (float)this.walkPhase);
            float camLatNow = class_3532.method_16439((float)t, (float)this.prevCamLat, (float)this.camLat);
            float camVertNow = class_3532.method_16439((float)t, (float)this.prevCamVert, (float)this.camVert);
            float pitchNow = class_3532.method_16439((float)t, (float)this.prevPitchVisual, (float)this.pitchVisual);
            float wf = class_3532.method_15363((float)(f * 0.4f), (float)0.0f, (float)2.0f) * (ff * ff * ff);
            float walkBob = (float)Math.sin(walkT) * 0.70686f * walkW;
            float walkPitch = (float)Math.sin(walkT + 0.65f) * 7.084f * walkW;
            float camLatNorm = class_3532.method_15363((float)(camLatNow / 1.25f), (float)-1.0f, (float)1.0f);
            float camLatEased = ArmState.easeInOutSigned(camLatNorm) * 1.25f;
            float camVertNorm = class_3532.method_15363((float)camVertNow, (float)-1.0f, (float)1.0f);
            float camVertEased = ArmState.easeInOutSigned(camVertNorm);
            float fallSigned = class_3532.method_15363((float)(f - camVertEased * 0.9f), (float)-2.4f, (float)2.4f);
            float armLatSign = arm == class_1306.field_6182 ? -1.0f : 1.0f;
            float translateX = camLatEased * 0.42f * w * armLatSign;
            float translateY = (fallSigned * 0.38f - punch * 0.85f) * w + walkBob - camVertEased * 0.34f * w;
            float translateZ = fallSigned * 0.44f * w;
            float pitch = pitchNow;
            float armSign = arm == class_1306.field_6182 ? 1.0f : -1.0f;
            float roll = armSign * (wf * 3.0f) * w;
            float wobble = (float)Math.sin(wt * 7.2f) * wf * 0.004f * w;
            float stretchY = 1.0f + 0.02f * fallSigned * w + wobble;
            float squashXZ = 1.0f - 0.02f * fallSigned * w - wobble * 0.25f;
            stretchY = class_3532.method_15363((float)stretchY, (float)0.97f, (float)1.05f);
            squashXZ = class_3532.method_15363((float)squashXZ, (float)0.95f, (float)1.03f);
            return new FeedbackPose(translateX, translateY, translateZ, 0.0f, armGate, punch * w, walkPitch, pitch, roll, squashXZ, stretchY, squashXZ, wf * w);
        }

        private static float rate(float base) {
            float b = class_3532.method_15363((float)base, (float)0.0f, (float)0.999f);
            return 1.0f - (float)Math.pow(1.0f - b, 2.16f);
        }

        private static float easeOutBack(float t) {
            float x = class_3532.method_15363((float)t, (float)0.0f, (float)1.0f) - 1.0f;
            float c1 = 2.6f;
            float c3 = c1 + 1.0f;
            return 1.0f + c3 * x * x * x + c1 * x * x;
        }

        private static float smoothStep01(float t) {
            float x = class_3532.method_15363((float)t, (float)0.0f, (float)1.0f);
            return x * x * (3.0f - 2.0f * x);
        }

        private static float moveTowards(float current, float target, float step) {
            float s = Math.abs(step);
            if (current < target) {
                return Math.min(current + s, target);
            }
            return Math.max(current - s, target);
        }

        private static float easeInOutSigned(float x) {
            float a = class_3532.method_15363((float)Math.abs(x), (float)0.0f, (float)1.0f);
            float eased = 0.5f - 0.5f * (float)Math.cos(Math.PI * (double)a);
            return Math.signum(x) * eased;
        }
    }

    record FeedbackPose(float translateXUnits, float translateYUnits, float translateZUnits, float highFallLiftUnits, float armRaiseGate, float landingPunch, float walkPitchDeg, float pitchDeg, float rollDeg, float scaleX, float scaleY, float scaleZ, float impactWave) {
        boolean isIdentity() {
            return Math.abs(this.translateXUnits) < 1.0E-4f && Math.abs(this.translateYUnits) < 1.0E-4f && Math.abs(this.translateZUnits) < 1.0E-4f && Math.abs(this.highFallLiftUnits) < 1.0E-4f && Math.abs(this.armRaiseGate) < 1.0E-4f && Math.abs(this.landingPunch) < 1.0E-4f && Math.abs(this.walkPitchDeg) < 1.0E-4f && Math.abs(this.pitchDeg) < 1.0E-4f && Math.abs(this.rollDeg) < 1.0E-4f && Math.abs(this.scaleX - 1.0f) < 1.0E-4f && Math.abs(this.scaleY - 1.0f) < 1.0E-4f && Math.abs(this.scaleZ - 1.0f) < 1.0E-4f && Math.abs(this.impactWave) < 1.0E-4f;
        }
    }
}

