/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.platform.GlStateManager
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1306
 *  net.minecraft.class_1920
 *  net.minecraft.class_1921
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2382
 *  net.minecraft.class_239
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_2394
 *  net.minecraft.class_2396
 *  net.minecraft.class_2398
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_2769
 *  net.minecraft.class_290
 *  net.minecraft.class_293
 *  net.minecraft.class_293$class_5596
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_5321
 *  net.minecraft.class_5819
 *  net.minecraft.class_630
 *  net.minecraft.class_638
 *  net.minecraft.class_6862
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 *  net.minecraft.class_761
 *  net.minecraft.class_7923
 *  net.minecraft.class_7924
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package punchy.client.modelparts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongConsumer;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1920;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5321;
import net.minecraft.class_5819;
import net.minecraft.class_630;
import net.minecraft.class_638;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_761;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.data.AnimationClip;
import punchy.client.bedrockitems.BedrockItemAnimationManager;
import punchy.client.modelparts.util.ModelPartMath;
import punchy.client.state.AttackActionTracker;
import punchy.client.state.MiningActionTracker;
import punchy.client.state.UseActionTracker;
import punchy.compat.IrisShaderPackCompat;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;
import punchy.mixin.client.accessor.ModelPartAccessor;

public final class ModelPartsParticleSystem {
    private static final float EPS = 1.0E-4f;
    private static final float TWO_PI = (float)Math.PI * 2;
    private static final float EMISSIVE_BLUR_SCALE = 1.08f;
    private static final float EMISSIVE_BLUR_ALPHA = 0.4f;
    private static final float EMISSIVE_BLUR_COLOR = 0.6f;
    private static final long ITEM_PARTICLE_WORLD_RELEASE_DELAY_NANOS = 25000000L;
    private static final long BEDROCK_BONE_PARTICLE_WORLD_RELEASE_DELAY_NANOS = 0L;
    private static final float DEFAULT_WORLD_PREVIEW_BEHIND_LOCAL_BIAS = 0.0015f;
    private static final double BLOCK_FACE_SURFACE_EPSILON = 0.0015;
    private static final double PARTICLE_COLLISION_SURFACE_EPSILON = 0.0025;
    private static final double PARTICLE_COLLISION_REST_EPSILON = 0.0035;
    private static final Vector3f GHOST_ANCHOR_RENDER_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final double PARTICLE_OCCLUSION_ENTRY_MARGIN = 0.22;
    private static final double PARTICLE_OCCLUSION_EPSILON = 0.01;
    private static final float PARTICLE_TEXTURE_ANIMATION_FPS = 12.0f;
    private static final int MAX_PARTICLES_PER_EVENT = 120;
    private static final EnumMap<class_1306, PlaybackState> PLAYBACK = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, List<PendingParticleEvent>> PENDING = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> PENDING_TICK = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> EMITTED_TICK = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, List<ActiveEmitter>> EMITTERS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_EMITTER_RENDER_NANOS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Map<String, PassiveEmitter>> DEFINITION_EMITTERS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_DEFINITION_EMITTER_NANOS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, TriggerRuntimeState> LAST_TRIGGER_STATE = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_USE_TRIGGER_EPOCH = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_ATTACK_TRIGGER_EPOCH = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_MINING_TRIGGER_EPOCH = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, List<RenderParticle>> ACTIVE_RENDER_PARTICLES = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, AnimationTriggerWindow> ANIMATION_TRIGGER_WINDOWS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Matrix4f> LAST_SIM_ROOT = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Long> LAST_SIM_NANOS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, class_243> LAST_CAMERA_POS = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Quaternionf> LAST_CAMERA_ROT = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Quaternionf> LAST_CAMERA_TURN_DELTA_CAMERA_LOCAL = new EnumMap(class_1306.class);
    private static final Map<String, class_2960> PARTICLE_TEXTURE_CACHE = new HashMap<String, class_2960>();
    private static final Map<class_2960, Integer> PARTICLE_ANIM_FRAME_COUNT_CACHE = new HashMap<class_2960, Integer>();
    private static final EnumMap<class_1306, Matrix4f> RENDER_ROOT = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Matrix4f> RENDER_ROOT_INV = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, Matrix4f> EXACT_INHAND_GHOST_ROOT = new EnumMap(class_1306.class);
    private static final EnumMap<class_1306, String> ACTIVE_DEFINITION_SOURCE = new EnumMap(class_1306.class);
    private static final class_2960 DEFAULT_PARTICLE_TEXTURE = class_2960.method_60655((String)"minecraft", (String)"textures/particle/flame.png");
    private static final Map<ParticleEmissiveKey, class_1921> PARTICLE_EMISSIVE_OVERLAY_TYPES = new HashMap<ParticleEmissiveKey, class_1921>();

    private ModelPartsParticleSystem() {
    }

    public static void resetArm(class_1306 arm) {
        if (arm == null) {
            return;
        }
        ModelPartsParticleSystem.clearArmControllerState(arm);
        ModelPartsParticleSystem.purgeArmParticles(arm);
    }

    public static void clearArmAnimationState(class_1306 arm) {
        if (arm == null) {
            return;
        }
        PLAYBACK.remove(arm);
        PENDING.remove(arm);
        PENDING_TICK.remove(arm);
        EMITTED_TICK.remove(arm);
        EMITTERS.remove(arm);
        LAST_EMITTER_RENDER_NANOS.remove(arm);
        ANIMATION_TRIGGER_WINDOWS.remove(arm);
    }

    public static void clearArmControllerState(class_1306 arm) {
        if (arm == null) {
            return;
        }
        ModelPartsParticleSystem.clearArmAnimationState(arm);
        DEFINITION_EMITTERS.remove(arm);
        LAST_DEFINITION_EMITTER_NANOS.remove(arm);
        ACTIVE_DEFINITION_SOURCE.remove(arm);
        LAST_TRIGGER_STATE.remove(arm);
        LAST_ATTACK_TRIGGER_EPOCH.remove(arm);
        LAST_MINING_TRIGGER_EPOCH.remove(arm);
    }

    public static void resetArmPlayback(class_1306 arm) {
        ModelPartsParticleSystem.clearArmAnimationState(arm);
    }

    public static void purgeArmParticles(class_1306 arm) {
        if (arm == null) {
            return;
        }
        ACTIVE_RENDER_PARTICLES.remove(arm);
        LAST_SIM_ROOT.remove(arm);
        LAST_SIM_NANOS.remove(arm);
        LAST_CAMERA_POS.remove(arm);
        LAST_CAMERA_ROT.remove(arm);
        LAST_CAMERA_TURN_DELTA_CAMERA_LOCAL.remove(arm);
        RENDER_ROOT.remove(arm);
        RENDER_ROOT_INV.remove(arm);
        EXACT_INHAND_GHOST_ROOT.remove(arm);
    }

    public static void resetAll() {
        for (class_1306 arm : class_1306.values()) {
            ModelPartsParticleSystem.resetArm(arm);
        }
    }

    public static void setRenderRoot(class_1306 arm, Matrix4f rootRenderMatrix) {
        if (arm == null || rootRenderMatrix == null) {
            return;
        }
        Matrix4f root = new Matrix4f((Matrix4fc)rootRenderMatrix);
        RENDER_ROOT.put(arm, root);
        RENDER_ROOT_INV.put(arm, new Matrix4f((Matrix4fc)root).invert());
    }

    public static void setExactInHandGhostRoot(class_1306 arm, Matrix4f rootRenderMatrix) {
        if (arm == null || rootRenderMatrix == null) {
            return;
        }
        EXACT_INHAND_GHOST_ROOT.put(arm, new Matrix4f((Matrix4fc)rootRenderMatrix));
    }

    public static void onHandlerTick(class_310 client, class_1306 arm, PoseHandler handler) {
        boolean active;
        if (!PunchyConfig.enableParticles()) {
            if (arm != null) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            return;
        }
        if (client == null || client.field_1687 == null || arm == null || handler == null) {
            return;
        }
        long gameTick = client.field_1687.method_8510();
        PlaybackState state = PLAYBACK.computeIfAbsent(arm, key -> new PlaybackState());
        if (state.lastProcessedTick == gameTick) {
            return;
        }
        state.lastProcessedTick = gameTick;
        AnimationClip clip = handler.getCurrentClip();
        boolean bl = active = handler.isPlaying() && clip != null;
        if (!active) {
            state.lastClip = null;
            state.lastTime = 0.0f;
            PENDING.remove(arm);
            PENDING_TICK.remove(arm);
            EMITTERS.remove(arm);
            LAST_EMITTER_RENDER_NANOS.remove(arm);
            ANIMATION_TRIGGER_WINDOWS.remove(arm);
            return;
        }
        float currentTime = handler.getCurrentTime();
        ModelPartsParticleSystem.updateAnimationTriggerWindow(arm, state.lastClip, state.lastTime, clip, currentTime);
        PENDING.remove(arm);
        PENDING_TICK.remove(arm);
        EMITTERS.remove(arm);
        LAST_EMITTER_RENDER_NANOS.remove(arm);
        state.lastClip = clip;
        state.lastTime = currentTime;
    }

    public static void emitPendingForParts(class_310 client, class_1306 arm, Map<String, class_630> parts) {
        if (!PunchyConfig.enableParticles()) {
            if (arm != null) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            return;
        }
        if (client == null || client.field_1687 == null || client.field_1724 == null || arm == null || parts == null || parts.isEmpty()) {
            return;
        }
        Long pendingTick = PENDING_TICK.get(arm);
        if (pendingTick == null || pendingTick.longValue() != client.field_1687.method_8510()) {
            return;
        }
        if (EMITTED_TICK.getOrDefault(arm, Long.MIN_VALUE) == pendingTick) {
            return;
        }
        List<PendingParticleEvent> events = PENDING.get(arm);
        if (events == null || events.isEmpty()) {
            EMITTED_TICK.put(arm, pendingTick);
            return;
        }
        class_746 player = client.field_1724;
        for (PendingParticleEvent pending : events) {
            ModelPartsParticleSystem.spawnEvent(client, player, arm, parts, pending);
        }
        events.clear();
        EMITTED_TICK.put(arm, pendingTick);
    }

    public static void emitPendingForRenderBones(class_310 client, class_1306 arm, Map<String, Matrix4f> renderBoneTransforms) {
        if (!PunchyConfig.enableParticles()) {
            if (arm != null) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            return;
        }
        if (ModelPartsParticleSystem.isParticleSuppressedByActiveItemAnimation(arm)) {
            ModelPartsParticleSystem.resetArm(arm);
            return;
        }
        if (client == null || client.field_1687 == null || client.field_1724 == null || arm == null || renderBoneTransforms == null || renderBoneTransforms.isEmpty()) {
            return;
        }
        class_746 player = client.field_1724;
        ModelPartsParticleSystem.emitFromActiveEmittersRender(client, player, arm, renderBoneTransforms);
        Long pendingTick = PENDING_TICK.get(arm);
        if (pendingTick == null || pendingTick.longValue() != client.field_1687.method_8510()) {
            return;
        }
        if (EMITTED_TICK.getOrDefault(arm, Long.MIN_VALUE) == pendingTick) {
            return;
        }
        List<PendingParticleEvent> events = PENDING.get(arm);
        if (events == null || events.isEmpty()) {
            EMITTED_TICK.put(arm, pendingTick);
            return;
        }
        for (PendingParticleEvent pending : events) {
            ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, pending);
        }
        events.clear();
        EMITTED_TICK.put(arm, pendingTick);
    }

    public static void emitDefinitionForRenderBones(class_310 client, class_1306 arm, Map<String, Matrix4f> renderBoneTransforms, List<String> definitionEffects) {
        if (!PunchyConfig.enableParticles()) {
            if (arm != null) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            return;
        }
        if (ModelPartsParticleSystem.isParticleSuppressedByActiveItemAnimation(arm)) {
            ModelPartsParticleSystem.resetArm(arm);
            return;
        }
        if (client == null || client.field_1687 == null || client.field_1724 == null || arm == null || renderBoneTransforms == null || renderBoneTransforms.isEmpty()) {
            return;
        }
        ModelPartsParticleSystem.resetDefinitionEmittersIfSourceChanged(arm, "render_bones");
        if (definitionEffects == null || definitionEffects.isEmpty()) {
            DEFINITION_EMITTERS.remove(arm);
            LAST_DEFINITION_EMITTER_NANOS.remove(arm);
            return;
        }
        long now = System.nanoTime();
        long last = LAST_DEFINITION_EMITTER_NANOS.getOrDefault(arm, 0L);
        LAST_DEFINITION_EMITTER_NANOS.put(arm, now);
        float dt = last <= 0L ? 0.016666668f : (float)((double)(now - last) / 1.0E9);
        dt = class_3532.method_15363((float)dt, (float)0.0f, (float)0.1f);
        Map emitters = DEFINITION_EMITTERS.computeIfAbsent(arm, key -> new HashMap());
        HashSet<String> activeKeys = new HashSet<String>();
        class_746 player = client.field_1724;
        long gameTick = client.field_1687.method_8510();
        TriggerContext triggerContext = ModelPartsParticleSystem.resolveTriggerContext(client, arm);
        for (String rawEffect : definitionEffects) {
            if (rawEffect == null || rawEffect.isBlank()) continue;
            String effect = rawEffect.trim();
            activeKeys.add(effect);
            PassiveEmitter emitter = (PassiveEmitter)emitters.get(effect);
            if (emitter == null) {
                ParticleScriptSpec spec = ModelPartsParticleSystem.parseParticleSpec(effect);
                if (spec == null) continue;
                emitter = new PassiveEmitter(spec);
                emitters.put(effect, emitter);
            }
            emitter.ageSeconds += dt;
            if (emitter.spec.emitForSeconds > 0.0f && emitter.ageSeconds > emitter.spec.emitForSeconds + 1.0E-4f || !ModelPartsParticleSystem.matchesDefinitionTriggers(client, emitter.spec, triggerContext)) continue;
            boolean pulseOnly = ModelPartsParticleSystem.hasPulseTriggers(emitter.spec);
            boolean pulseConfigured = ModelPartsParticleSystem.hasAnyPulseTriggers(emitter.spec);
            if (pulseConfigured) {
                if (ModelPartsParticleSystem.consumePulseEvents(emitter, arm, triggerContext)) {
                    ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
                }
                if (pulseOnly) continue;
            }
            if (emitter.spec.emitIntervalSeconds > 0.0f) {
                if (!ModelPartsParticleSystem.canEmitByInterval(emitter.spec, emitter, now)) continue;
                ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
                continue;
            }
            if (emitter.spec.ratePerSecond > 0.0f) {
                emitter.accumulator += (double)(emitter.spec.ratePerSecond * dt);
                int pulses = class_3532.method_15340((int)((int)Math.floor(emitter.accumulator)), (int)0, (int)24);
                if (pulses <= 0) continue;
                emitter.accumulator -= (double)pulses;
                for (int i = 0; i < pulses; ++i) {
                    ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
                }
                continue;
            }
            if (emitter.lastTick == gameTick) continue;
            emitter.lastTick = gameTick;
            if (!ModelPartsParticleSystem.canEmitByInterval(emitter.spec, emitter, now)) continue;
            ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
        }
        if (!emitters.isEmpty()) {
            emitters.entrySet().removeIf(entry -> !activeKeys.contains(entry.getKey()));
            if (emitters.isEmpty()) {
                DEFINITION_EMITTERS.remove(arm);
            }
        }
    }

    public static void emitDefinitionForItemRender(class_310 client, class_1306 arm, Matrix4f itemRenderMatrix, List<String> definitionEffects) {
        if (!PunchyConfig.enableParticles()) {
            if (arm != null) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            return;
        }
        if (ModelPartsParticleSystem.isParticleSuppressedByActiveItemAnimation(arm)) {
            ModelPartsParticleSystem.resetArm(arm);
            return;
        }
        if (client == null || client.field_1687 == null || client.field_1724 == null || arm == null || itemRenderMatrix == null) {
            return;
        }
        ModelPartsParticleSystem.resetDefinitionEmittersIfSourceChanged(arm, "item_render");
        if (definitionEffects == null || definitionEffects.isEmpty()) {
            DEFINITION_EMITTERS.remove(arm);
            LAST_DEFINITION_EMITTER_NANOS.remove(arm);
            return;
        }
        long now = System.nanoTime();
        long last = LAST_DEFINITION_EMITTER_NANOS.getOrDefault(arm, 0L);
        LAST_DEFINITION_EMITTER_NANOS.put(arm, now);
        float dt = last <= 0L ? 0.016666668f : (float)((double)(now - last) / 1.0E9);
        dt = class_3532.method_15363((float)dt, (float)0.0f, (float)0.1f);
        Map emitters = DEFINITION_EMITTERS.computeIfAbsent(arm, key -> new HashMap());
        HashSet<String> activeKeys = new HashSet<String>();
        class_746 player = client.field_1724;
        long gameTick = client.field_1687.method_8510();
        TriggerContext triggerContext = ModelPartsParticleSystem.resolveTriggerContext(client, arm);
        for (String rawEffect : definitionEffects) {
            if (rawEffect == null || rawEffect.isBlank()) continue;
            String effect = rawEffect.trim();
            activeKeys.add(effect);
            PassiveEmitter emitter = (PassiveEmitter)emitters.get(effect);
            if (emitter == null) {
                ParticleScriptSpec spec = ModelPartsParticleSystem.parseParticleSpec(effect);
                if (spec == null) continue;
                emitter = new PassiveEmitter(spec);
                emitters.put(effect, emitter);
            }
            emitter.ageSeconds += dt;
            if (emitter.spec.emitForSeconds > 0.0f && emitter.ageSeconds > emitter.spec.emitForSeconds + 1.0E-4f || !ModelPartsParticleSystem.matchesDefinitionTriggers(client, emitter.spec, triggerContext)) continue;
            boolean pulseOnly = ModelPartsParticleSystem.hasPulseTriggers(emitter.spec);
            boolean pulseConfigured = ModelPartsParticleSystem.hasAnyPulseTriggers(emitter.spec);
            if (pulseConfigured) {
                if (ModelPartsParticleSystem.consumePulseEvents(emitter, arm, triggerContext)) {
                    ModelPartsParticleSystem.spawnEventForItemRender(client, player, arm, itemRenderMatrix, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
                }
                if (pulseOnly) continue;
            }
            if (emitter.spec.emitIntervalSeconds > 0.0f) {
                if (!ModelPartsParticleSystem.canEmitByInterval(emitter.spec, emitter, now)) continue;
                ModelPartsParticleSystem.spawnEventForItemRender(client, player, arm, itemRenderMatrix, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
                continue;
            }
            if (emitter.spec.ratePerSecond > 0.0f) {
                emitter.accumulator += (double)(emitter.spec.ratePerSecond * dt);
                int pulses = class_3532.method_15340((int)((int)Math.floor(emitter.accumulator)), (int)0, (int)24);
                if (pulses <= 0) continue;
                emitter.accumulator -= (double)pulses;
                for (int i = 0; i < pulses; ++i) {
                    ModelPartsParticleSystem.spawnEventForItemRender(client, player, arm, itemRenderMatrix, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
                }
                continue;
            }
            if (emitter.lastTick == gameTick) continue;
            emitter.lastTick = gameTick;
            if (!ModelPartsParticleSystem.canEmitByInterval(emitter.spec, emitter, now)) continue;
            ModelPartsParticleSystem.spawnEventForItemRender(client, player, arm, itemRenderMatrix, new PendingParticleEvent(emitter.spec, -1.0f, 1.0f));
        }
        if (!emitters.isEmpty()) {
            emitters.entrySet().removeIf(entry -> !activeKeys.contains(entry.getKey()));
            if (emitters.isEmpty()) {
                DEFINITION_EMITTERS.remove(arm);
            }
        }
    }

    private static void resetDefinitionEmittersIfSourceChanged(class_1306 arm, String sourceKey) {
        if (arm == null || sourceKey == null) {
            return;
        }
        String last = ACTIVE_DEFINITION_SOURCE.get(arm);
        if (last != null && !last.equals(sourceKey)) {
            DEFINITION_EMITTERS.remove(arm);
            LAST_DEFINITION_EMITTER_NANOS.remove(arm);
            ModelPartsParticleSystem.purgeArmParticles(arm);
        }
        ACTIVE_DEFINITION_SOURCE.put(arm, sourceKey);
    }

    private static TriggerContext resolveTriggerContext(class_310 client, class_1306 arm) {
        long lastSeenMiningEpoch;
        long lastSeenAttackEpoch;
        if (client == null || client.field_1724 == null || client.field_1687 == null || arm == null) {
            return TriggerContext.EMPTY;
        }
        class_746 player = client.field_1724;
        TriggerRuntimeState runtime = LAST_TRIGGER_STATE.computeIfAbsent(arm, key -> new TriggerRuntimeState());
        boolean onGround = player.method_24828();
        class_243 move = player.method_18798();
        double horizontalSq = move.field_1352 * move.field_1352 + move.field_1350 * move.field_1350;
        boolean movingHorizontal = horizontalSq > 1.0E-4;
        boolean movementInput = client.field_1690 != null && (client.field_1690.field_1894.method_1434() || client.field_1690.field_1881.method_1434() || client.field_1690.field_1913.method_1434() || client.field_1690.field_1849.method_1434());
        boolean swimming = player.method_5681() || player.method_5799();
        boolean sprinting = player.method_5624() && (movingHorizontal || movementInput);
        boolean walking = (movingHorizontal || movementInput) && onGround && !sprinting && !swimming;
        float fallDistance = Math.max(0.0f, player.field_6017);
        boolean falling = !onGround && move.field_1351 < -0.03 && fallDistance > 0.0f;
        boolean attackKeyDown = client.field_1690 != null && client.field_1690.field_1886.method_1434();
        boolean useKeyDown = client.field_1690 != null && client.field_1690.field_1904.method_1434();
        class_239 hit = client.field_1765;
        boolean aimingBlock = hit != null && hit.method_17783() == class_239.class_240.field_1332;
        boolean isMainArm = arm == player.method_6068();
        class_1268 armHand = ModelPartsParticleSystem.resolveHand(player, arm);
        boolean usingItem = player.method_6115() && useKeyDown;
        class_1268 usedHand = usingItem ? player.method_6058() : null;
        boolean useItem = usingItem && usedHand == armHand;
        UseActionTracker.Trigger useTrigger = UseActionTracker.peekTrigger();
        long useEpoch = UseActionTracker.peekEpoch();
        class_1268 useEventHand = useTrigger != null ? useTrigger.hand() : UseActionTracker.peekLastHand();
        boolean useHandMatches = useEventHand == armHand;
        class_1268 trackedAttackHand = AttackActionTracker.peekHand();
        long attackEpoch = AttackActionTracker.peekEpoch();
        long miningEpoch = MiningActionTracker.peekEpoch();
        boolean attackHandMatches = trackedAttackHand == null ? isMainArm : trackedAttackHand == armHand;
        boolean mining = isMainArm && MiningActionTracker.isMiningActive() && aimingBlock;
        boolean jumpPulse = false;
        boolean landingPulse = false;
        boolean attackingPulse = false;
        boolean miningPulse = false;
        boolean useItemPulse = false;
        boolean attackTrackerPulse = false;
        if (attackHandMatches && attackEpoch > (lastSeenAttackEpoch = LAST_ATTACK_TRIGGER_EPOCH.getOrDefault(arm, 0L).longValue())) {
            attackTrackerPulse = true;
            LAST_ATTACK_TRIGGER_EPOCH.put(arm, attackEpoch);
        }
        boolean miningTrackerPulse = false;
        if (isMainArm && miningEpoch > (lastSeenMiningEpoch = LAST_MINING_TRIGGER_EPOCH.getOrDefault(arm, 0L).longValue())) {
            miningTrackerPulse = true;
            LAST_MINING_TRIGGER_EPOCH.put(arm, miningEpoch);
        }
        if (runtime.initialized) {
            long lastSeenEpoch;
            boolean attackTransition;
            boolean landingTransition;
            boolean jumpTransition;
            boolean bl = jumpTransition = !onGround && runtime.lastOnGround && (move.field_1351 > 0.04 || client.field_1690 != null && client.field_1690.field_1903.method_1434());
            if (jumpTransition) {
                ++runtime.jumpEventId;
                runtime.jumpGraceTicks = 2;
            }
            if (runtime.jumpGraceTicks > 0) {
                jumpPulse = true;
                --runtime.jumpGraceTicks;
            }
            boolean bl2 = landingTransition = onGround && !runtime.lastOnGround;
            if (landingTransition) {
                ++runtime.landingEventId;
                runtime.landingGraceTicks = 2;
            }
            if (onGround && runtime.landingGraceTicks > 0) {
                landingPulse = true;
                --runtime.landingGraceTicks;
            } else if (!onGround) {
                runtime.landingGraceTicks = 0;
            }
            boolean bl3 = attackTransition = attackHandMatches && (player.field_6252 && !runtime.lastSwinging || attackKeyDown && !runtime.lastAttackKeyDown);
            if (attackTransition || attackTrackerPulse) {
                ++runtime.attackEventId;
                runtime.attackGraceTicks = 2;
            }
            if (runtime.attackGraceTicks > 0) {
                attackingPulse = true;
                --runtime.attackGraceTicks;
            }
            if (miningTrackerPulse) {
                ++runtime.miningEventId;
                runtime.miningGraceTicks = 2;
            }
            if (runtime.miningGraceTicks > 0) {
                miningPulse = true;
                --runtime.miningGraceTicks;
            }
            if (useHandMatches && useEpoch > (lastSeenEpoch = LAST_USE_TRIGGER_EPOCH.getOrDefault(arm, 0L).longValue())) {
                useItemPulse = true;
                LAST_USE_TRIGGER_EPOCH.put(arm, useEpoch);
            }
        } else {
            long lastSeenEpoch;
            runtime.initialized = true;
            if (attackTrackerPulse) {
                ++runtime.attackEventId;
                runtime.attackGraceTicks = 1;
                attackingPulse = true;
            }
            if (miningTrackerPulse) {
                ++runtime.miningEventId;
                runtime.miningGraceTicks = 1;
                miningPulse = true;
            }
            if (useHandMatches && useEpoch > (lastSeenEpoch = LAST_USE_TRIGGER_EPOCH.getOrDefault(arm, 0L).longValue())) {
                useItemPulse = true;
                LAST_USE_TRIGGER_EPOCH.put(arm, useEpoch);
            }
        }
        runtime.lastOnGround = onGround;
        runtime.lastAttackKeyDown = attackKeyDown;
        runtime.lastSwinging = player.field_6252;
        mining = mining || miningPulse;
        useItem = useItem || useItemPulse;
        long useItemEventId = useItemPulse ? useEpoch : 0L;
        return new TriggerContext(jumpPulse, landingPulse, walking, sprinting, swimming, falling, fallDistance, attackingPulse, useItem, mining, runtime.jumpEventId, runtime.landingEventId, runtime.attackEventId, runtime.miningEventId, useItemEventId);
    }

    private static boolean isDamageableAttackTarget(class_239 hit) {
        if (!(hit instanceof class_3966)) {
            return false;
        }
        class_3966 entityHit = (class_3966)hit;
        class_1297 entity = entityHit.method_17782();
        if (entity == null) {
            return false;
        }
        if (!entity.method_5805() || entity.method_7325()) {
            return false;
        }
        if (!entity.method_5732()) {
            return false;
        }
        return !entity.method_5655();
    }

    private static class_1268 resolveHand(class_746 player, class_1306 arm) {
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

    private static boolean isParticleSuppressedByActiveItemAnimation(class_1306 arm) {
        if (arm == null) {
            return false;
        }
        PunchyTuningConfig.CustomAnimationEntry entry = BedrockItemAnimationManager.getActiveEntry(arm);
        return entry != null && Boolean.TRUE.equals(entry.disable_all_particles);
    }

    private static boolean matchesDefinitionTriggers(class_310 client, ParticleScriptSpec spec, TriggerContext context) {
        if (spec == null || context == null || !ModelPartsParticleSystem.hasDefinitionTriggers(spec)) {
            return true;
        }
        MiningActionTracker.Target miningTarget = ModelPartsParticleSystem.resolveMiningTarget(client, spec);
        if (spec.triggerModeAny) {
            boolean anyConfigured = false;
            boolean anyMatched = false;
            if (spec.triggerJump) {
                anyConfigured = true;
                anyMatched |= context.jump;
            }
            if (spec.triggerLanding) {
                anyConfigured = true;
                anyMatched |= context.landing;
            }
            if (spec.triggerWalking) {
                anyConfigured = true;
                anyMatched |= context.walking;
            }
            if (spec.triggerSprinting) {
                anyConfigured = true;
                anyMatched |= context.sprinting;
            }
            if (spec.triggerSwimming) {
                anyConfigured = true;
                anyMatched |= context.swimming;
            }
            if (spec.triggerFalling) {
                anyConfigured = true;
                anyMatched |= context.falling && context.fallDistance + 1.0E-4f >= spec.triggerFallingMinBlocks;
            }
            if (spec.triggerAttacking) {
                anyConfigured = true;
                anyMatched |= context.attacking;
            }
            if (spec.triggerUseItem) {
                anyConfigured = true;
                anyMatched |= context.useItem;
            }
            if (spec.triggerMining) {
                anyConfigured = true;
                anyMatched |= context.mining;
            }
            if (spec.triggerBiomeNames != null && !spec.triggerBiomeNames.isEmpty()) {
                anyConfigured = true;
                anyMatched |= ModelPartsParticleSystem.matchesBiomeNames(client, spec.triggerBiomeNames);
            }
            if (ModelPartsParticleSystem.hasMiningTargetFilter(spec)) {
                if (!anyConfigured) {
                    return ModelPartsParticleSystem.matchesMiningTarget(spec, miningTarget);
                }
                if (!ModelPartsParticleSystem.matchesMiningTarget(spec, miningTarget)) {
                    return false;
                }
            }
            return !anyConfigured || anyMatched;
        }
        if (spec.triggerJump && !context.jump) {
            return false;
        }
        if (spec.triggerLanding && !context.landing) {
            return false;
        }
        if (spec.triggerWalking && !context.walking) {
            return false;
        }
        if (spec.triggerSprinting && !context.sprinting) {
            return false;
        }
        if (spec.triggerSwimming && !context.swimming) {
            return false;
        }
        if (spec.triggerFalling) {
            if (!context.falling) {
                return false;
            }
            if (context.fallDistance + 1.0E-4f < spec.triggerFallingMinBlocks) {
                return false;
            }
        }
        if (spec.triggerAttacking && !context.attacking) {
            return false;
        }
        if (spec.triggerUseItem && !context.useItem) {
            return false;
        }
        if (spec.triggerMining && !context.mining) {
            return false;
        }
        if (spec.triggerBiomeNames != null && !spec.triggerBiomeNames.isEmpty() && !ModelPartsParticleSystem.matchesBiomeNames(client, spec.triggerBiomeNames)) {
            return false;
        }
        return !ModelPartsParticleSystem.hasMiningTargetFilter(spec) || ModelPartsParticleSystem.matchesMiningTarget(spec, miningTarget);
    }

    private static boolean matchesBiomeNames(class_310 client, Set<String> targets) {
        if (client == null || client.field_1687 == null || client.field_1724 == null || targets == null || targets.isEmpty()) {
            return false;
        }
        class_638 level = client.field_1687;
        class_2338 pos = client.field_1724.method_24515();
        class_6880 holder = level.method_23753(pos);
        for (String entry : targets) {
            class_5321 key;
            class_2960 parsed;
            class_6862 tag;
            class_2960 tagId;
            String trimmed;
            if (entry == null || entry.isBlank() || !((trimmed = entry.trim()).startsWith("#") ? (tagId = class_2960.method_12829((String)trimmed.substring(1))) != null && holder.method_40220(tag = class_6862.method_40092((class_5321)class_7924.field_41236, (class_2960)tagId)) : (parsed = class_2960.method_12829((String)trimmed)) != null && holder.method_40225(key = class_5321.method_29179((class_5321)class_7924.field_41236, (class_2960)parsed)))) continue;
            return true;
        }
        return false;
    }

    private static MiningActionTracker.Target resolveMiningTarget(class_310 client, ParticleScriptSpec spec) {
        class_239 class_2392;
        if (!ModelPartsParticleSystem.hasMiningTargetFilter(spec)) {
            return null;
        }
        MiningActionTracker.Target tracked = MiningActionTracker.peekTarget();
        if (tracked != null) {
            return tracked;
        }
        tracked = MiningActionTracker.peekRecentTarget(250000000L);
        if (tracked != null) {
            return tracked;
        }
        if (!ModelPartsParticleSystem.isAnyBlockFaceTarget(spec) || client == null || client.field_1687 == null || !((class_2392 = client.field_1765) instanceof class_3965)) {
            return null;
        }
        class_3965 blockHit = (class_3965)class_2392;
        class_2338 pos = blockHit.method_17777();
        class_2680 state = client.field_1687.method_8320(pos);
        if (pos == null || state == null || state.method_26215()) {
            return null;
        }
        return new MiningActionTracker.Target(pos, blockHit.method_17780(), state, blockHit.method_17784());
    }

    private static boolean hasMiningTargetFilter(ParticleScriptSpec spec) {
        if (spec == null) {
            return false;
        }
        return ModelPartsParticleSystem.isAnyBlockFaceTarget(spec) || spec.triggerBlockIds != null && !spec.triggerBlockIds.isEmpty() || spec.triggerBlockState != null && !spec.triggerBlockState.isEmpty();
    }

    private static boolean matchesMiningTarget(ParticleScriptSpec spec, MiningActionTracker.Target target) {
        if (!ModelPartsParticleSystem.hasMiningTargetFilter(spec)) {
            return true;
        }
        if (target == null || target.state() == null || target.state().method_26215()) {
            return false;
        }
        if (spec.triggerBlockIds != null && !spec.triggerBlockIds.isEmpty() && !ModelPartsParticleSystem.matchesBlockSelectors(target.state(), spec.triggerBlockIds)) {
            return false;
        }
        return spec.triggerBlockState == null || spec.triggerBlockState.isEmpty() || ModelPartsParticleSystem.matchesRequiredBlockState(target.state(), spec.triggerBlockState);
    }

    private static boolean matchesBlockSelectors(class_2680 state, Set<String> selectors) {
        if (state == null || selectors == null || selectors.isEmpty()) {
            return true;
        }
        class_2960 blockId = class_7923.field_41175.method_10221((Object)state.method_26204());
        if (blockId == null) {
            return false;
        }
        String current = blockId.toString();
        for (String entry : selectors) {
            class_2960 parsed;
            class_2960 tagId;
            String normalized;
            if (entry == null || entry.isBlank() || !((normalized = entry.trim().toLowerCase(Locale.ROOT)).startsWith("#") ? (tagId = class_2960.method_12829((String)normalized.substring(1))) != null && state.method_26164(class_6862.method_40092((class_5321)class_7924.field_41254, (class_2960)tagId)) : (parsed = class_2960.method_12829((String)normalized)) != null && current.equals(parsed.toString()))) continue;
            return true;
        }
        return false;
    }

    private static boolean matchesRequiredBlockState(class_2680 state, Map<String, String> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }
        if (state == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : required.entrySet()) {
            String key = entry.getKey();
            String wanted = entry.getValue();
            if (key == null || key.isBlank() || wanted == null || wanted.isBlank()) continue;
            class_2769<?> property = ModelPartsParticleSystem.findBlockProperty(state, key.trim());
            if (property == null) {
                return false;
            }
            String actual = String.valueOf(state.method_11654(property));
            if (wanted.equalsIgnoreCase(actual)) continue;
            return false;
        }
        return true;
    }

    private static class_2769<?> findBlockProperty(class_2680 state, String name) {
        if (state == null || name == null || name.isBlank()) {
            return null;
        }
        for (class_2769 property : state.method_28501()) {
            if (property == null || !name.equalsIgnoreCase(property.method_11899())) continue;
            return property;
        }
        return null;
    }

    private static boolean hasDefinitionTriggers(ParticleScriptSpec spec) {
        if (spec == null) {
            return false;
        }
        return spec.triggerJump || spec.triggerLanding || spec.triggerWalking || spec.triggerSprinting || spec.triggerSwimming || spec.triggerFalling || spec.triggerAttacking || spec.triggerUseItem || spec.triggerMining || spec.hasAnimationTrigger() || spec.triggerBiomeNames != null && !spec.triggerBiomeNames.isEmpty() || ModelPartsParticleSystem.hasMiningTargetFilter(spec);
    }

    private static boolean hasPulseTriggers(ParticleScriptSpec spec) {
        if (spec == null) {
            return false;
        }
        boolean hasPulse = ModelPartsParticleSystem.hasAnyPulseTriggers(spec);
        if (!hasPulse) {
            return false;
        }
        boolean hasContinuous = spec.triggerWalking || spec.triggerSprinting || spec.triggerSwimming || spec.triggerFalling || spec.triggerMining || spec.triggerBiomeNames != null && !spec.triggerBiomeNames.isEmpty();
        return !hasContinuous;
    }

    private static boolean hasAnyPulseTriggers(ParticleScriptSpec spec) {
        if (spec == null) {
            return false;
        }
        return spec.triggerJump || spec.triggerLanding || spec.triggerAttacking || spec.triggerUseItem || spec.triggerMining || spec.hasAnimationTrigger();
    }

    private static boolean consumePulseEvents(PassiveEmitter emitter, class_1306 arm, TriggerContext context) {
        if (emitter == null || context == null || emitter.spec == null) {
            return false;
        }
        boolean emit = false;
        if (emitter.spec.triggerJump && context.jump && context.jumpEventId > emitter.lastJumpEventId) {
            emitter.lastJumpEventId = context.jumpEventId;
            emit = true;
        }
        if (emitter.spec.triggerLanding && context.landing && context.landingEventId > emitter.lastLandingEventId) {
            emitter.lastLandingEventId = context.landingEventId;
            emit = true;
        }
        if (emitter.spec.triggerAttacking && context.attacking && context.attackEventId > emitter.lastAttackEventId) {
            emitter.lastAttackEventId = context.attackEventId;
            emit = true;
        }
        if (emitter.spec.triggerMining && context.mining && context.miningEventId > emitter.lastMiningEventId) {
            emitter.lastMiningEventId = context.miningEventId;
            emit = true;
        }
        if (emitter.spec.triggerUseItem && context.useItemEventId > emitter.lastUseItemEventId) {
            emitter.lastUseItemEventId = context.useItemEventId;
            emit = true;
        }
        if (ModelPartsParticleSystem.consumeAnimationPulse(emitter, arm)) {
            emit = true;
        }
        return emit;
    }

    private static boolean consumeAnimationPulse(PassiveEmitter emitter, class_1306 arm) {
        String currentName;
        if (emitter == null || emitter.spec == null || arm == null || !emitter.spec.hasAnimationTrigger()) {
            return false;
        }
        AnimationTriggerWindow window = ANIMATION_TRIGGER_WINDOWS.get(arm);
        if (window == null || !window.active || window.serial <= 0L) {
            return false;
        }
        if (emitter.lastAnimationWindowSerial == window.serial) {
            return false;
        }
        emitter.lastAnimationWindowSerial = window.serial;
        String wantedName = emitter.spec.triggerAnimationName == null ? "" : emitter.spec.triggerAnimationName.trim();
        String string = currentName = window.currentClipName == null ? "" : window.currentClipName;
        if (wantedName.isBlank() || currentName.isBlank() || !wantedName.equalsIgnoreCase(currentName)) {
            return false;
        }
        float targetTime = class_3532.method_15363((float)emitter.spec.triggerAnimationTimecode, (float)0.0f, (float)Math.max(0.0f, window.clipLengthSeconds));
        float start = window.previousTimeSeconds;
        float end = window.currentTimeSeconds;
        if (window.clipChanged) {
            start = -1.0E-4f;
        }
        if (window.looping && end + 1.0E-4f < start && window.clipLengthSeconds > 1.0E-4f) {
            boolean tail = targetTime > start + 1.0E-4f && targetTime <= window.clipLengthSeconds + 1.0E-4f;
            boolean head = targetTime >= -1.0E-4f && targetTime <= end + 1.0E-4f;
            return tail || head;
        }
        return targetTime > start + 1.0E-4f && targetTime <= end + 1.0E-4f;
    }

    private static void updateAnimationTriggerWindow(class_1306 arm, AnimationClip previousClip, float previousTime, AnimationClip currentClip, float currentTime) {
        if (arm == null || currentClip == null) {
            ANIMATION_TRIGGER_WINDOWS.remove(arm);
            return;
        }
        AnimationTriggerWindow window = ANIMATION_TRIGGER_WINDOWS.computeIfAbsent(arm, key -> new AnimationTriggerWindow());
        ++window.serial;
        window.active = true;
        window.currentClipName = currentClip.getName();
        window.currentTimeSeconds = currentTime;
        window.previousTimeSeconds = previousClip != currentClip ? -1.0E-4f : previousTime;
        window.clipLengthSeconds = Math.max(0.0f, currentClip.getLength());
        window.looping = currentClip.isLoop();
        window.clipChanged = previousClip != currentClip;
    }

    private static boolean canEmitByInterval(ParticleScriptSpec spec, PassiveEmitter emitter, long nowNanos) {
        if (spec == null || emitter == null) {
            return false;
        }
        return ModelPartsParticleSystem.canEmitByInterval(spec.emitIntervalSeconds, nowNanos, emitter.lastEmitNanos, n -> {
            emitter.lastEmitNanos = n;
        });
    }

    private static boolean canEmitByInterval(ParticleScriptSpec spec, ActiveEmitter emitter, long nowNanos) {
        if (spec == null || emitter == null) {
            return false;
        }
        return ModelPartsParticleSystem.canEmitByInterval(spec.emitIntervalSeconds, nowNanos, emitter.lastEmitNanos, n -> {
            emitter.lastEmitNanos = n;
        });
    }

    private static boolean canEmitByInterval(float intervalSeconds, long nowNanos, long lastEmitNanos, LongConsumer setter) {
        if (intervalSeconds <= 0.0f) {
            if (setter != null) {
                setter.accept(nowNanos);
            }
            return true;
        }
        long intervalNanos = (long)Math.max(1.0f, intervalSeconds * 1.0E9f);
        if (lastEmitNanos > 0L && nowNanos - lastEmitNanos < intervalNanos) {
            return false;
        }
        if (setter != null) {
            setter.accept(nowNanos);
        }
        return true;
    }

    private static void emitFromActiveEmittersRender(class_310 client, class_746 player, class_1306 arm, Map<String, Matrix4f> renderBoneTransforms) {
        List<ActiveEmitter> emitters = EMITTERS.get(arm);
        if (emitters == null || emitters.isEmpty() || client == null || renderBoneTransforms == null || renderBoneTransforms.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        long last = LAST_EMITTER_RENDER_NANOS.getOrDefault(arm, 0L);
        LAST_EMITTER_RENDER_NANOS.put(arm, now);
        float dt = last <= 0L ? 0.016666668f : (float)((double)(now - last) / 1.0E9);
        dt = class_3532.method_15363((float)dt, (float)0.0f, (float)0.1f);
        for (int i = emitters.size() - 1; i >= 0; --i) {
            float lifeScale;
            ActiveEmitter emitter = emitters.get(i);
            if (emitter == null || emitter.spec == null) {
                emitters.remove(i);
                continue;
            }
            emitter.ageSeconds += dt;
            float totalSeconds = Math.max(0.05f, (float)emitter.totalTicks / 20.0f);
            if (emitter.ageSeconds > totalSeconds + 1.0E-4f) {
                emitters.remove(i);
                continue;
            }
            float progress = class_3532.method_15363((float)(emitter.ageSeconds / totalSeconds), (float)0.0f, (float)1.0f);
            float f = lifeScale = emitter.spec.emitFullAnimation ? 1.0f : ModelPartsParticleSystem.applyEase(emitter.spec.emitEase, progress);
            if (emitter.spec.emitIntervalSeconds > 0.0f) {
                if (!ModelPartsParticleSystem.canEmitByInterval(emitter.spec, emitter, now)) continue;
                ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, new PendingParticleEvent(emitter.spec, -1.0f, lifeScale));
                continue;
            }
            emitter.accumulator += (double)(emitter.spec.ratePerSecond * lifeScale * dt);
            int pulses = (int)Math.floor(emitter.accumulator);
            if (pulses <= 0) continue;
            emitter.accumulator -= (double)pulses;
            for (int j = 0; j < pulses; ++j) {
                ModelPartsParticleSystem.spawnEventFromRender(client, player, arm, renderBoneTransforms, new PendingParticleEvent(emitter.spec, -1.0f, lifeScale));
            }
        }
    }

    public static void renderForArm(class_310 client, class_1306 arm, class_4597 buffer, class_4587 poseStack, int packedLight) {
        class_4184 camera;
        if (!PunchyConfig.enableParticles()) {
            if (arm != null) {
                ModelPartsParticleSystem.resetArm(arm);
            }
            return;
        }
        if (ModelPartsParticleSystem.isParticleSuppressedByActiveItemAnimation(arm)) {
            ModelPartsParticleSystem.resetArm(arm);
            return;
        }
        if (client == null || client.field_1687 == null || arm == null || buffer == null || poseStack == null) {
            return;
        }
        ModelPartsParticleSystem.tickRenderParticles(client, arm);
        List<RenderParticle> particles = ACTIVE_RENDER_PARTICLES.get(arm);
        if (particles == null || particles.isEmpty()) {
            return;
        }
        class_4184 class_41842 = camera = client.field_1773 == null ? null : client.field_1773.method_19418();
        if (camera == null || camera.method_19326() == null || camera.method_23767() == null) {
            return;
        }
        Quaternionf cameraRot = new Quaternionf((Quaternionfc)camera.method_23767());
        class_243 cameraPos = camera.method_19326();
        boolean shaderPackInUse = ModelPartsParticleSystem.isShaderPackInUse();
        Matrix4f localRenderPose = RENDER_ROOT.get(arm);
        if (localRenderPose == null) {
            localRenderPose = poseStack.method_23760().method_23761();
        }
        int renderedCount = 0;
        int skippedTiny = 0;
        int skippedInvalid = 0;
        int localPendingCount = 0;
        int localReleasedWaitingCount = 0;
        int worldCount = 0;
        for (RenderParticle particle : particles) {
            boolean emissiveOnlyPass;
            boolean emissiveActive;
            Vector3f renderPos;
            if (particle == null || (particle.brightAtDark || particle.glowTargetMenu) && !PunchyConfig.enableBlurGlowAtDark() || particle.renderInWorldPass && !particle.localSpace) continue;
            if (particle.localSpace) {
                if (particle.pendingWorldRelease) {
                    ++localPendingCount;
                } else {
                    ++localReleasedWaitingCount;
                }
            } else {
                ++worldCount;
            }
            float progress = particle.totalTicks <= 1 ? 1.0f : class_3532.method_15363((float)(particle.ageTicks / (float)particle.totalTicks), (float)0.0f, (float)1.0f);
            float eased = ModelPartsParticleSystem.applyEase(particle.ease, progress);
            float size = class_3532.method_16439((float)eased, (float)particle.sizeStart, (float)particle.sizeEnd);
            float alpha = class_3532.method_16439((float)eased, (float)particle.alphaStart, (float)particle.alphaEnd);
            if (size <= 1.0E-4f || alpha <= 0.001f) {
                ++skippedTiny;
                continue;
            }
            class_4587 worldStack = new class_4587();
            if (particle.localSpace) {
                boolean immediateWorldRelease;
                renderPos = particle.ignoreItemRotationForLayer ? ModelPartsParticleSystem.renderPointIgnoringPoseRotation(localRenderPose, particle.position, cameraRot, shaderPackInUse) : ModelPartsParticleSystem.transformRenderPoint(localRenderPose, particle.position);
                worldStack.method_46416(renderPos.x, renderPos.y, renderPos.z);
                if (!shaderPackInUse || particle.pendingWorldRelease) {
                    worldStack.method_22907(cameraRot);
                }
                ModelPartsParticleSystem.tryReleaseParticleFromRenderedPose(client, arm, particle, localRenderPose, cameraPos, cameraRot, renderPos, shaderPackInUse);
                boolean bl = immediateWorldRelease = particle.pendingWorldRelease && (shaderPackInUse || particle.worldReleaseDelayNanos <= 0L) && particle.renderInWorldPass;
                if (immediateWorldRelease) {
                    continue;
                }
            } else {
                if (ModelPartsParticleSystem.isParticleOccludedByWorld(client, cameraPos, particle.position)) continue;
                class_243 delta = particle.position.method_1020(cameraPos);
                renderPos = new Vector3f((float)delta.field_1352, (float)delta.field_1351, (float)delta.field_1350);
                worldStack.method_46416(renderPos.x, renderPos.y, renderPos.z);
                if (!shaderPackInUse) {
                    worldStack.method_22907(cameraRot);
                }
            }
            if (!ModelPartsParticleSystem.isFinite(renderPos)) {
                ++skippedInvalid;
                continue;
            }
            class_2960 baseTexture = particle.texture != null ? particle.texture : DEFAULT_PARTICLE_TEXTURE;
            class_2960 texture = ModelPartsParticleSystem.resolveAnimatedParticleTexture(client, particle, baseTexture);
            int sampledLight = ModelPartsParticleSystem.resolveParticleWorldLight(client, particle, arm, cameraPos, renderPos, packedLight);
            float darkGlowFactor = ModelPartsParticleSystem.resolveBrightAtDarkGlowFactor(client, particle, sampledLight);
            if ((alpha = ModelPartsParticleSystem.resolveParticleRenderAlpha(client, particle, sampledLight, alpha)) <= 0.001f) {
                ++skippedInvalid;
                continue;
            }
            int light = ModelPartsParticleSystem.resolveParticleLight(client, particle, sampledLight);
            float baseRed = class_3532.method_15363((float)particle.red, (float)0.0f, (float)1.0f);
            float baseGreen = class_3532.method_15363((float)particle.green, (float)0.0f, (float)1.0f);
            float baseBlue = class_3532.method_15363((float)particle.blue, (float)0.0f, (float)1.0f);
            float renderAlpha = alpha;
            boolean bl = emissiveActive = ModelPartsParticleSystem.isParticleEmissiveActive(client, particle) || darkGlowFactor > 0.001f;
            if (particle.lockInModel && emissiveActive) {
                class_1921 lockType = ModelPartsParticleSystem.emissiveOverlayRenderType(texture, shaderPackInUse, false, particle.emissiveClamp);
                class_4588 lockConsumer = buffer.getBuffer(lockType);
                float lockAlpha = particle.brightAtDark ? Math.min(1.0f, renderAlpha * darkGlowFactor) : renderAlpha;
                ModelPartsParticleSystem.addBillboardQuad(worldStack.method_23760(), lockConsumer, new Vector3f(0.0f, 0.0f, 0.0f), size, lockAlpha, 0xF000F0, baseRed, baseGreen, baseBlue);
                ++renderedCount;
                continue;
            }
            boolean allowEmissiveOverlay = emissiveActive && (particle.lightPercent < 0.0f || particle.lightPercent >= 99.999f) && !particle.lockInModel;
            boolean bl2 = emissiveOnlyPass = allowEmissiveOverlay && !particle.emissiveBlur && particle.emissiveStrength >= 100;
            if (!emissiveOnlyPass) {
                class_4588 baseConsumer = buffer.getBuffer(class_1921.method_23580((class_2960)texture));
                ModelPartsParticleSystem.addBillboardQuad(worldStack.method_23760(), baseConsumer, new Vector3f(0.0f, 0.0f, 0.0f), size, renderAlpha, light, baseRed, baseGreen, baseBlue);
            }
            if (allowEmissiveOverlay) {
                float emissiveBase = ModelPartsParticleSystem.emissionToBaseIntensity(particle.emissiveStrength);
                float emissiveGlow = ModelPartsParticleSystem.emissionToExtraGlow(particle.emissiveStrength);
                if (emissiveBase > 0.0f || emissiveGlow > 0.0f) {
                    float overlayAlpha;
                    float colorScale = Math.min(1.0f, emissiveBase);
                    float red = class_3532.method_15363((float)(baseRed * colorScale), (float)0.0f, (float)1.0f);
                    float green = class_3532.method_15363((float)(baseGreen * colorScale), (float)0.0f, (float)1.0f);
                    float blue = class_3532.method_15363((float)(baseBlue * colorScale), (float)0.0f, (float)1.0f);
                    float f = overlayAlpha = emissiveOnlyPass ? alpha : Math.min(1.0f, alpha * 0.55f);
                    if (particle.brightAtDark) {
                        overlayAlpha *= darkGlowFactor;
                    }
                    class_1921 emissiveType = ModelPartsParticleSystem.emissiveOverlayRenderType(texture, shaderPackInUse, particle.emissiveBlur, particle.emissiveClamp);
                    class_4588 emissiveConsumer = buffer.getBuffer(emissiveType);
                    ModelPartsParticleSystem.addBillboardQuad(worldStack.method_23760(), emissiveConsumer, new Vector3f(0.0f, 0.0f, 0.0f), size, overlayAlpha, 0xF000F0, red, green, blue);
                }
                if (particle.emissiveBlur && emissiveGlow > 0.0f) {
                    float red = class_3532.method_15363((float)(baseRed * Math.min(1.0f, emissiveBase)), (float)0.0f, (float)1.0f);
                    float green = class_3532.method_15363((float)(baseGreen * Math.min(1.0f, emissiveBase)), (float)0.0f, (float)1.0f);
                    float blue = class_3532.method_15363((float)(baseBlue * Math.min(1.0f, emissiveBase)), (float)0.0f, (float)1.0f);
                    float glowBoost = 1.0f + emissiveGlow * 1.5f;
                    float blurScale = 1.08f + emissiveGlow * 0.08f;
                    float blurColor = Math.min(1.0f, 0.6f + emissiveGlow * 0.4f);
                    float blurAlpha = Math.min(1.0f, alpha * 0.4f * glowBoost);
                    if (particle.brightAtDark) {
                        blurAlpha *= darkGlowFactor;
                    }
                    ModelPartsParticleSystem.addEmissiveBlurHalo(worldStack.method_23760(), buffer, texture, shaderPackInUse, particle.emissiveClamp, size, blurScale, blurAlpha, class_3532.method_15363((float)(red * blurColor), (float)0.0f, (float)1.0f), class_3532.method_15363((float)(green * blurColor), (float)0.0f, (float)1.0f), class_3532.method_15363((float)(blue * blurColor), (float)0.0f, (float)1.0f));
                }
            }
            ++renderedCount;
        }
        if (renderedCount != 0 || !particles.isEmpty()) {
            // empty if block
        }
    }

    public static void renderReleasedParticlesInWorld(class_310 client, class_4597 buffer, class_4587 worldBasePose) {
        class_4184 camera;
        if (client == null || client.field_1687 == null || buffer == null || worldBasePose == null) {
            return;
        }
        class_4184 class_41842 = camera = client.field_1773 == null ? null : client.field_1773.method_19418();
        if (camera == null || camera.method_19326() == null || camera.method_23767() == null) {
            return;
        }
        Quaternionf cameraRot = new Quaternionf((Quaternionfc)camera.method_23767());
        class_243 cameraPos = camera.method_19326();
        boolean shaderPackInUse = ModelPartsParticleSystem.isShaderPackInUse();
        int submitted = 0;
        for (class_1306 arm : class_1306.values()) {
            List<RenderParticle> particles = ACTIVE_RENDER_PARTICLES.get(arm);
            if (particles == null || particles.isEmpty()) continue;
            for (RenderParticle particle : particles) {
                boolean emissiveOnlyPass;
                class_243 worldPos;
                boolean previewBehindLocal;
                if (particle == null || !particle.renderInWorldPass || (particle.brightAtDark || particle.glowTargetMenu) && !PunchyConfig.enableBlurGlowAtDark() || particle.lockInModel) continue;
                boolean bl = previewBehindLocal = particle.localSpace && particle.pendingWorldRelease && particle.pendingReleaseSnapshotReady && particle.worldPreviewBehindLocal;
                if (particle.localSpace && !previewBehindLocal) continue;
                float progress = particle.totalTicks <= 1 ? 1.0f : class_3532.method_15363((float)(particle.ageTicks / (float)particle.totalTicks), (float)0.0f, (float)1.0f);
                float eased = ModelPartsParticleSystem.applyEase(particle.ease, progress);
                float size = class_3532.method_16439((float)eased, (float)particle.sizeStart, (float)particle.sizeEnd);
                float alpha = class_3532.method_16439((float)eased, (float)particle.alphaStart, (float)particle.alphaEnd);
                if (size <= 1.0E-4f || alpha <= 0.001f) continue;
                class_243 class_2432 = worldPos = previewBehindLocal && particle.pendingReleaseWorldPos != null ? particle.pendingReleaseWorldPos : particle.position;
                if (previewBehindLocal) {
                    worldPos = ModelPartsParticleSystem.pushBehindLocalLayer(cameraPos, worldPos, particle.worldPreviewBehindLocalBias, cameraRot);
                }
                if (ModelPartsParticleSystem.isParticleOccludedByWorld(client, cameraPos, worldPos)) continue;
                class_243 delta = worldPos.method_1020(cameraPos);
                Vector3f renderPos = new Vector3f((float)delta.field_1352, (float)delta.field_1351, (float)delta.field_1350);
                if (!ModelPartsParticleSystem.isFinite(renderPos)) continue;
                class_4587 worldStack = new class_4587();
                worldStack.method_23760().method_23761().set((Matrix4fc)worldBasePose.method_23760().method_23761());
                worldStack.method_23760().method_23762().set((Matrix3fc)worldBasePose.method_23760().method_23762());
                worldStack.method_46416(renderPos.x, renderPos.y, renderPos.z);
                worldStack.method_22907(cameraRot);
                class_2960 baseTexture = particle.texture != null ? particle.texture : DEFAULT_PARTICLE_TEXTURE;
                class_2960 texture = ModelPartsParticleSystem.resolveAnimatedParticleTexture(client, particle, baseTexture);
                int sampledLight = ModelPartsParticleSystem.resolveParticleWorldLight(client, particle, arm, cameraPos, renderPos, 0);
                float darkGlowFactor = ModelPartsParticleSystem.resolveBrightAtDarkGlowFactor(client, particle, sampledLight);
                alpha = ModelPartsParticleSystem.resolveParticleRenderAlpha(client, particle, sampledLight, alpha);
                if (alpha <= 0.001f) continue;
                int light = ModelPartsParticleSystem.resolveParticleLight(client, particle, sampledLight);
                float baseRed = class_3532.method_15363((float)particle.red, (float)0.0f, (float)1.0f);
                float baseGreen = class_3532.method_15363((float)particle.green, (float)0.0f, (float)1.0f);
                float baseBlue = class_3532.method_15363((float)particle.blue, (float)0.0f, (float)1.0f);
                boolean allowEmissiveOverlay = (ModelPartsParticleSystem.isParticleEmissiveActive(client, particle) || darkGlowFactor > 0.001f) && (particle.lightPercent < 0.0f || particle.lightPercent >= 99.999f);
                boolean bl2 = emissiveOnlyPass = allowEmissiveOverlay && !particle.emissiveBlur && particle.emissiveStrength >= 100;
                if (!emissiveOnlyPass) {
                    class_4588 baseConsumer = buffer.getBuffer(class_1921.method_23580((class_2960)texture));
                    ModelPartsParticleSystem.addBillboardQuad(worldStack.method_23760(), baseConsumer, new Vector3f(0.0f, 0.0f, 0.0f), size, alpha, light, baseRed, baseGreen, baseBlue);
                    ++submitted;
                }
                if (!allowEmissiveOverlay) continue;
                float emissiveBase = ModelPartsParticleSystem.emissionToBaseIntensity(particle.emissiveStrength);
                float emissiveGlow = ModelPartsParticleSystem.emissionToExtraGlow(particle.emissiveStrength);
                if (emissiveBase > 0.0f || emissiveGlow > 0.0f) {
                    float overlayAlpha;
                    float colorScale = Math.min(1.0f, emissiveBase);
                    float red = class_3532.method_15363((float)(baseRed * colorScale), (float)0.0f, (float)1.0f);
                    float green = class_3532.method_15363((float)(baseGreen * colorScale), (float)0.0f, (float)1.0f);
                    float blue = class_3532.method_15363((float)(baseBlue * colorScale), (float)0.0f, (float)1.0f);
                    float f = overlayAlpha = emissiveOnlyPass ? alpha : Math.min(1.0f, alpha * 0.55f);
                    if (particle.brightAtDark) {
                        overlayAlpha *= darkGlowFactor;
                    }
                    class_1921 emissiveType = ModelPartsParticleSystem.emissiveOverlayRenderType(texture, shaderPackInUse, particle.emissiveBlur, particle.emissiveClamp);
                    class_4588 emissiveConsumer = buffer.getBuffer(emissiveType);
                    ModelPartsParticleSystem.addBillboardQuad(worldStack.method_23760(), emissiveConsumer, new Vector3f(0.0f, 0.0f, 0.0f), size, overlayAlpha, 0xF000F0, red, green, blue);
                }
                if (!particle.emissiveBlur || !(emissiveGlow > 0.0f)) continue;
                float red = class_3532.method_15363((float)(baseRed * Math.min(1.0f, emissiveBase)), (float)0.0f, (float)1.0f);
                float green = class_3532.method_15363((float)(baseGreen * Math.min(1.0f, emissiveBase)), (float)0.0f, (float)1.0f);
                float blue = class_3532.method_15363((float)(baseBlue * Math.min(1.0f, emissiveBase)), (float)0.0f, (float)1.0f);
                float glowBoost = 1.0f + emissiveGlow * 1.5f;
                float blurScale = 1.08f + emissiveGlow * 0.08f;
                float blurColor = Math.min(1.0f, 0.6f + emissiveGlow * 0.4f);
                float blurAlpha = Math.min(1.0f, alpha * 0.4f * glowBoost);
                if (particle.brightAtDark) {
                    blurAlpha *= darkGlowFactor;
                }
                ModelPartsParticleSystem.addEmissiveBlurHalo(worldStack.method_23760(), buffer, texture, shaderPackInUse, particle.emissiveClamp, size, blurScale, blurAlpha, class_3532.method_15363((float)(red * blurColor), (float)0.0f, (float)1.0f), class_3532.method_15363((float)(green * blurColor), (float)0.0f, (float)1.0f), class_3532.method_15363((float)(blue * blurColor), (float)0.0f, (float)1.0f));
            }
        }
    }

    private static void collectFromRange(AnimationClip clip, float startExclusive, float endInclusive, List<PendingParticleEvent> out) {
        if (clip == null || out == null || endInclusive + 1.0E-4f < startExclusive) {
            return;
        }
        for (Map.Entry<Float, Set<String>> entry : clip.getTimelineEffectsUpTo(endInclusive)) {
            float time = entry.getKey().floatValue();
            if (time < startExclusive + 1.0E-4f || time > endInclusive + 1.0E-4f) continue;
            for (String effect : entry.getValue()) {
                ParticleScriptSpec spec = ModelPartsParticleSystem.parseParticleSpec(effect);
                if (spec == null) continue;
                out.add(new PendingParticleEvent(spec, time, 1.0f));
            }
        }
    }

    private static void spawnEvent(class_310 client, class_746 player, class_1306 arm, Map<String, class_630> parts, PendingParticleEvent pending) {
        ParticleScriptSpec spec = pending.spec;
        if (spec == null) {
            return;
        }
        float eased = ModelPartsParticleSystem.applyEase(spec.ease, spec.t) * class_3532.method_15363((float)pending.scale, (float)0.0f, (float)1.0f);
        int count = class_3532.method_15340((int)Math.round((float)spec.count * eased), (int)0, (int)120);
        if (count <= 0) {
            return;
        }
        if (spec.chance < 1.0f && client.field_1687.field_9229.method_43057() > spec.chance) {
            return;
        }
        class_2394 options = ModelPartsParticleSystem.resolveParticle(spec.typeId);
        class_243 base = ModelPartsParticleSystem.resolveBoneBase(player, arm, parts, spec.boneName);
        class_243 orientedOffset = ModelPartsParticleSystem.applyBasis(player, arm, ModelPartsParticleSystem.effectiveParticleOffset(spec, arm, "bone"));
        class_243 velocityBase = ModelPartsParticleSystem.applyBasis(player, arm, new Vector3f((Vector3fc)spec.velocity).mul(eased));
        class_243 spread = new class_243((double)spec.spread.x, (double)spec.spread.y, (double)spec.spread.z);
        for (int i = 0; i < count; ++i) {
            double px = base.field_1352 + orientedOffset.field_1352 + ModelPartsParticleSystem.randomSpread(client, spread.field_1352);
            double py = base.field_1351 + orientedOffset.field_1351 + ModelPartsParticleSystem.randomSpread(client, spread.field_1351);
            double pz = base.field_1350 + orientedOffset.field_1350 + ModelPartsParticleSystem.randomSpread(client, spread.field_1350);
            double vx = velocityBase.field_1352 + ModelPartsParticleSystem.randomSpread(client, spread.field_1352 * 0.25);
            double vy = velocityBase.field_1351 + ModelPartsParticleSystem.randomSpread(client, spread.field_1351 * 0.25);
            double vz = velocityBase.field_1350 + ModelPartsParticleSystem.randomSpread(client, spread.field_1350 * 0.25);
            client.field_1687.method_8406(options, px, py, pz, vx, vy, vz);
        }
    }

    private static void spawnEventFromRender(class_310 client, class_746 player, class_1306 arm, Map<String, Matrix4f> renderBoneTransforms, PendingParticleEvent pending) {
        boolean localSimulation;
        float roll;
        ParticleScriptSpec spec = pending.spec;
        if (spec == null) {
            return;
        }
        float eased = ModelPartsParticleSystem.applyEase(spec.ease, spec.t) * class_3532.method_15363((float)pending.scale, (float)0.0f, (float)1.0f);
        int count = class_3532.method_15340((int)Math.round((float)spec.count * eased), (int)0, (int)120);
        if (count <= 0) {
            return;
        }
        if (spec.chance < 1.0f && (roll = client.field_1687.field_9229.method_43057()) > spec.chance) {
            return;
        }
        String bone = ModelPartsParticleSystem.resolveRenderBoneName(renderBoneTransforms, spec.boneName, arm);
        Matrix4f boneTransform = bone == null ? null : renderBoneTransforms.get(bone);
        Matrix4f rootInv = RENDER_ROOT_INV.get(arm);
        Matrix4f localBoneTransform = ModelPartsParticleSystem.toLocalBoneTransform(boneTransform, rootInv);
        class_243 handFallback = ModelPartsParticleSystem.resolveHandFallback(client, player, arm);
        class_243 base = ModelPartsParticleSystem.resolveRenderBoneBase(client, player, handFallback, boneTransform, localBoneTransform, arm, spec.projectionMode);
        if (base == null) {
            return;
        }
        if (!ModelPartsParticleSystem.isFinite(base)) {
            base = handFallback;
        } else if (handFallback != null && base.method_1022(handFallback) > 8.0) {
            base = handFallback;
        }
        Vector3f renderBoneOffset = ModelPartsParticleSystem.effectiveParticleOffset(spec, arm, spec.offsetSpace);
        class_243 orientedOffset = ModelPartsParticleSystem.applyRenderBasis(client, boneTransform, localBoneTransform, renderBoneOffset, player, arm, spec.offsetSpace, false, handFallback, spec.projectionMode, spec.ignoreModelRotationForEmissionPoint);
        class_243 velocityBase = ModelPartsParticleSystem.applyRenderBasis(client, boneTransform, localBoneTransform, new Vector3f((Vector3fc)spec.velocity).mul(eased), player, arm, spec.velocitySpace, true, handFallback, spec.projectionMode, false);
        if (orientedOffset == null || !ModelPartsParticleSystem.isFinite(orientedOffset)) {
            orientedOffset = class_243.field_1353;
        }
        if (velocityBase == null || !ModelPartsParticleSystem.isFinite(velocityBase)) {
            velocityBase = class_243.field_1353;
        }
        class_243 spread = new class_243((double)spec.spread.x, (double)spec.spread.y, (double)spec.spread.z);
        class_2394 options = ModelPartsParticleSystem.resolveParticle(spec.typeId);
        class_2960 texture = ModelPartsParticleSystem.resolveCustomParticleTexture(spec);
        Vector3f tint = ModelPartsParticleSystem.resolveCustomParticleTint(spec);
        int totalTicks = spec.lockInModel ? Integer.MAX_VALUE : Math.max(1, Math.round(spec.lifetimeSeconds * 20.0f));
        float sizeEnd = spec.lockInModel ? spec.sizeStart : spec.sizeEnd;
        float alphaEnd = spec.lockInModel ? spec.alphaStart : spec.alphaEnd;
        float drag = spec.lockInModel ? 1.0f : spec.drag;
        float gravity = spec.lockInModel ? 0.0f : spec.gravity;
        String motionMode = spec.lockInModel ? "linear" : ModelPartsParticleSystem.parseMotionMode(spec.motionMode, "linear");
        boolean orbit = !spec.lockInModel && "orbit".equals(motionMode);
        float orbitRadiusWorld = Math.max(0.0f, spec.orbitRadius);
        float orbitRadiusPixels = orbitRadiusWorld * 16.0f;
        float orbitAngularVelocity = spec.orbitSpeed * ((float)Math.PI * 2) / 20.0f;
        float orbitPhaseBase = (float)Math.toRadians(spec.orbitPhaseDegrees);
        List active = ACTIVE_RENDER_PARTICLES.computeIfAbsent(arm, key -> new ArrayList());
        boolean bl = localSimulation = localBoneTransform != null;
        if (spec.lockInModel && !localSimulation) {
            return;
        }
        Vector3f localBasePixels = localSimulation ? localBoneTransform.getTranslation(new Vector3f()) : null;
        Vector3f localOffsetPixels = null;
        Vector3f localVelocityPixels = null;
        Vector3f localSpreadPixels = null;
        Vector3f localCenterOffsetPixels = null;
        Vector3f localOrbitAxis = null;
        class_243 worldCenterOffset = null;
        class_243 worldOrbitAxis = null;
        if (localSimulation) {
            localOffsetPixels = ModelPartsParticleSystem.applyRenderBasisLocal(localBoneTransform, new Vector3f((Vector3fc)renderBoneOffset).mul(16.0f), spec.offsetSpace, false, spec.ignoreModelRotationForEmissionPoint);
            localVelocityPixels = ModelPartsParticleSystem.applyRenderBasisLocal(localBoneTransform, new Vector3f((Vector3fc)spec.velocity).mul(eased * 16.0f), spec.velocitySpace, true, false);
            localSpreadPixels = new Vector3f((Vector3fc)spec.spread).mul(16.0f);
            localCenterOffsetPixels = ModelPartsParticleSystem.applyRenderBasisLocal(localBoneTransform, new Vector3f((Vector3fc)spec.orbitCenterOffset).mul(16.0f), spec.motionSpace, false, spec.ignoreModelRotationForEmissionPoint);
            localOrbitAxis = ModelPartsParticleSystem.applyRenderBasisLocal(localBoneTransform, new Vector3f((Vector3fc)spec.orbitAxis), spec.motionSpace, false, false);
        } else {
            worldCenterOffset = ModelPartsParticleSystem.applyRenderBasis(client, boneTransform, localBoneTransform, spec.orbitCenterOffset, player, arm, spec.motionSpace, false, handFallback, spec.projectionMode, spec.ignoreModelRotationForEmissionPoint);
            worldOrbitAxis = ModelPartsParticleSystem.applyRenderBasis(client, boneTransform, localBoneTransform, spec.orbitAxis, player, arm, spec.motionSpace, false, handFallback, spec.projectionMode, false);
        }
        if (worldCenterOffset == null || !ModelPartsParticleSystem.isFinite(worldCenterOffset)) {
            worldCenterOffset = class_243.field_1353;
        }
        if (worldOrbitAxis == null || !ModelPartsParticleSystem.isFinite(worldOrbitAxis)) {
            worldOrbitAxis = new class_243(0.0, 1.0, 0.0);
        }
        class_243 normalizedWorldOrbitAxis = ModelPartsParticleSystem.normalizeAxis(worldOrbitAxis);
        class_243 normalizedLocalOrbitAxis = localOrbitAxis == null ? new class_243(0.0, 1.0, 0.0) : ModelPartsParticleSystem.normalizeAxis(new class_243((double)localOrbitAxis.x, (double)localOrbitAxis.y, (double)localOrbitAxis.z));
        String lockKeyBase = spec.lockInModel ? spec.lockStableKey : null;
        for (int i = 0; i < count; ++i) {
            RenderParticle particle;
            double vz;
            double vy;
            double vx;
            class_243 spawn;
            class_243 orbitOffset;
            class_243 center;
            double cz;
            double cy;
            double cx;
            String lockKey = lockKeyBase == null ? null : lockKeyBase + ":" + i;
            float particleOrbitPhase = orbitPhaseBase + (count > 1 ? (float)Math.PI * 2 * ((float)i / (float)count) : 0.0f);
            if (localSimulation) {
                cx = (double)(localBasePixels.x + localOffsetPixels.x + localCenterOffsetPixels.x) + ModelPartsParticleSystem.randomSpread(client, localSpreadPixels.x);
                cy = (double)(localBasePixels.y + localOffsetPixels.y + localCenterOffsetPixels.y) + ModelPartsParticleSystem.randomSpread(client, localSpreadPixels.y);
                cz = (double)(localBasePixels.z + localOffsetPixels.z + localCenterOffsetPixels.z) + ModelPartsParticleSystem.randomSpread(client, localSpreadPixels.z);
                center = new class_243(cx, cy, cz);
                orbitOffset = orbit ? ModelPartsParticleSystem.orbitOffset(normalizedLocalOrbitAxis, orbitRadiusPixels, particleOrbitPhase) : class_243.field_1353;
                spawn = center.method_1019(orbitOffset);
                vx = spec.lockInModel ? 0.0 : (double)localVelocityPixels.x + ModelPartsParticleSystem.randomSpread(client, (double)localSpreadPixels.x * 0.25);
                vy = spec.lockInModel ? 0.0 : (double)localVelocityPixels.y + ModelPartsParticleSystem.randomSpread(client, (double)localSpreadPixels.y * 0.25);
                vz = spec.lockInModel ? 0.0 : (double)localVelocityPixels.z + ModelPartsParticleSystem.randomSpread(client, (double)localSpreadPixels.z * 0.25);
                particle = new RenderParticle(spawn, new class_243(vx, vy, vz), totalTicks, spec.ease, spec.sizeStart, sizeEnd, spec.alphaStart, alphaEnd, drag, gravity, spec.fullBright, spec.fullDayLight, spec.brightAtDark, spec.glowTargetMenu, spec.lightPercent, spec.emissiveStrength, spec.emissiveBlur, spec.emissiveClamp, texture, tint.x, tint.y, tint.z, spec.debug, spec.lockInModel, lockKey, true, motionMode, center, normalizedLocalOrbitAxis, orbitRadiusPixels, particleOrbitPhase, orbitAngularVelocity);
                ModelPartsParticleSystem.addInHandPipelineParticle(active, particle, false, 0L, spec.worldPreviewBehindLocal, spec.worldPreviewBehindLocalBias, spec.ignoreModelRotationForEmissionPoint, spec.syntheticWorldLocal);
                continue;
            }
            cx = base.field_1352 + orientedOffset.field_1352 + worldCenterOffset.field_1352 + ModelPartsParticleSystem.randomSpread(client, spread.field_1352);
            cy = base.field_1351 + orientedOffset.field_1351 + worldCenterOffset.field_1351 + ModelPartsParticleSystem.randomSpread(client, spread.field_1351);
            cz = base.field_1350 + orientedOffset.field_1350 + worldCenterOffset.field_1350 + ModelPartsParticleSystem.randomSpread(client, spread.field_1350);
            center = new class_243(cx, cy, cz);
            orbitOffset = orbit ? ModelPartsParticleSystem.orbitOffset(normalizedWorldOrbitAxis, orbitRadiusWorld, particleOrbitPhase) : class_243.field_1353;
            spawn = center.method_1019(orbitOffset);
            vx = spec.lockInModel ? 0.0 : velocityBase.field_1352 + ModelPartsParticleSystem.randomSpread(client, spread.field_1352 * 0.25);
            vy = spec.lockInModel ? 0.0 : velocityBase.field_1351 + ModelPartsParticleSystem.randomSpread(client, spread.field_1351 * 0.25);
            vz = spec.lockInModel ? 0.0 : velocityBase.field_1350 + ModelPartsParticleSystem.randomSpread(client, spread.field_1350 * 0.25);
            particle = new RenderParticle(spawn, new class_243(vx, vy, vz), totalTicks, spec.ease, spec.sizeStart, sizeEnd, spec.alphaStart, alphaEnd, drag, gravity, spec.fullBright, spec.fullDayLight, spec.brightAtDark, spec.glowTargetMenu, spec.lightPercent, spec.emissiveStrength, spec.emissiveBlur, spec.emissiveClamp, texture, tint.x, tint.y, tint.z, false, spec.lockInModel, lockKey, false, motionMode, center, normalizedWorldOrbitAxis, orbitRadiusWorld, particleOrbitPhase, orbitAngularVelocity);
            ModelPartsParticleSystem.addInHandPipelineParticle(active, particle, false, 0L, spec.worldPreviewBehindLocal, spec.worldPreviewBehindLocalBias, spec.ignoreModelRotationForEmissionPoint, spec.syntheticWorldLocal);
        }
    }

    private static void spawnEventForItemRender(class_310 client, class_746 player, class_1306 arm, Matrix4f itemRenderMatrix, PendingParticleEvent pending) {
        ParticleScriptSpec spec = pending.spec;
        if (spec == null || client == null || arm == null || itemRenderMatrix == null) {
            return;
        }
        float eased = ModelPartsParticleSystem.applyEase(spec.ease, spec.t) * class_3532.method_15363((float)pending.scale, (float)0.0f, (float)1.0f);
        int count = class_3532.method_15340((int)Math.round((float)spec.count * eased), (int)0, (int)120);
        if (count <= 0) {
            return;
        }
        if (spec.chance < 1.0f && client.field_1687 != null && client.field_1687.field_9229.method_43057() > spec.chance) {
            return;
        }
        if (ModelPartsParticleSystem.isWorldBlockFaceTarget(spec)) {
            ModelPartsParticleSystem.spawnEventFromBlockFace(client, player, arm, pending, eased, count);
            return;
        }
        if (ModelPartsParticleSystem.isHeldBlockFaceTarget(spec)) {
            ModelPartsParticleSystem.spawnEventForHeldItemBlockFace(client, player, arm, itemRenderMatrix, pending, eased, count);
            return;
        }
        class_243 localBase = ModelPartsParticleSystem.resolveItemTargetLocalBase(spec);
        if (localBase == null) {
            localBase = class_243.field_1353;
        }
        Vector3f localOffset = ModelPartsParticleSystem.resolveItemLocalVector(ModelPartsParticleSystem.effectiveParticleOffset(spec, arm, spec.offsetSpace), spec.offsetSpace, itemRenderMatrix, player, arm, false, spec.ignoreModelRotationForEmissionPoint);
        Vector3f localVelocity = ModelPartsParticleSystem.resolveItemLocalVector(new Vector3f((Vector3fc)spec.velocity).mul(eased), spec.velocitySpace, itemRenderMatrix, player, arm, true, false);
        Vector3f localSpread = new Vector3f((Vector3fc)spec.spread);
        class_2960 texture = ModelPartsParticleSystem.resolveCustomParticleTexture(spec);
        Vector3f tint = ModelPartsParticleSystem.resolveCustomParticleTint(spec);
        int totalTicks = spec.lockInModel ? Integer.MAX_VALUE : Math.max(1, Math.round(spec.lifetimeSeconds * 20.0f));
        float sizeEnd = spec.lockInModel ? spec.sizeStart : spec.sizeEnd;
        float alphaEnd = spec.lockInModel ? spec.alphaStart : spec.alphaEnd;
        float drag = spec.lockInModel ? 1.0f : spec.drag;
        float gravity = spec.lockInModel ? 0.0f : spec.gravity;
        String motionMode = spec.lockInModel ? "linear" : ModelPartsParticleSystem.parseMotionMode(spec.motionMode, "linear");
        boolean orbit = !spec.lockInModel && "orbit".equals(motionMode);
        float orbitRadius = Math.max(0.0f, spec.orbitRadius);
        float orbitAngularVelocity = spec.orbitSpeed * ((float)Math.PI * 2) / 20.0f;
        float orbitPhaseBase = (float)Math.toRadians(spec.orbitPhaseDegrees);
        Vector3f localCenterOffset = ModelPartsParticleSystem.resolveItemLocalVector(spec.orbitCenterOffset, spec.motionSpace, itemRenderMatrix, player, arm, false, spec.ignoreModelRotationForEmissionPoint);
        Vector3f localOrbitAxisVec = ModelPartsParticleSystem.resolveItemLocalVector(spec.orbitAxis, spec.motionSpace, itemRenderMatrix, player, arm, false, false);
        class_243 localOrbitAxis = ModelPartsParticleSystem.normalizeAxis(new class_243((double)localOrbitAxisVec.x, (double)localOrbitAxisVec.y, (double)localOrbitAxisVec.z));
        List active = ACTIVE_RENDER_PARTICLES.computeIfAbsent(arm, key -> new ArrayList());
        String lockKeyBase = spec.lockInModel ? spec.lockStableKey : null;
        for (int i = 0; i < count; ++i) {
            String lockKey = lockKeyBase == null ? null : lockKeyBase + ":" + i;
            float particleOrbitPhase = orbitPhaseBase + (count > 1 ? (float)Math.PI * 2 * ((float)i / (float)count) : 0.0f);
            double cx = localBase.field_1352 + (double)localOffset.x + (double)localCenterOffset.x + ModelPartsParticleSystem.randomSpread(client, localSpread.x);
            double cy = localBase.field_1351 + (double)localOffset.y + (double)localCenterOffset.y + ModelPartsParticleSystem.randomSpread(client, localSpread.y);
            double cz = localBase.field_1350 + (double)localOffset.z + (double)localCenterOffset.z + ModelPartsParticleSystem.randomSpread(client, localSpread.z);
            class_243 center = new class_243(cx, cy, cz);
            class_243 orbitOffset = orbit ? ModelPartsParticleSystem.orbitOffset(localOrbitAxis, orbitRadius, particleOrbitPhase) : class_243.field_1353;
            class_243 spawn = center.method_1019(orbitOffset);
            double vx = spec.lockInModel ? 0.0 : (double)localVelocity.x + ModelPartsParticleSystem.randomSpread(client, (double)localSpread.x * 0.25);
            double vy = spec.lockInModel ? 0.0 : (double)localVelocity.y + ModelPartsParticleSystem.randomSpread(client, (double)localSpread.y * 0.25);
            double vz = spec.lockInModel ? 0.0 : (double)localVelocity.z + ModelPartsParticleSystem.randomSpread(client, (double)localSpread.z * 0.25);
            RenderParticle particle = new RenderParticle(spawn, new class_243(vx, vy, vz), totalTicks, spec.ease, spec.sizeStart, sizeEnd, spec.alphaStart, alphaEnd, drag, gravity, spec.fullBright, spec.fullDayLight, spec.brightAtDark, spec.glowTargetMenu, spec.lightPercent, spec.emissiveStrength, spec.emissiveBlur, spec.emissiveClamp, texture, tint.x, tint.y, tint.z, spec.debug, spec.lockInModel, lockKey, true, motionMode, center, localOrbitAxis, orbitRadius, particleOrbitPhase, orbitAngularVelocity);
            ModelPartsParticleSystem.addInHandPipelineParticle(active, particle, false, 0L, spec.worldPreviewBehindLocal, spec.worldPreviewBehindLocalBias, spec.ignoreModelRotationForEmissionPoint, spec.syntheticWorldLocal);
        }
    }

    private static void spawnEventFromBlockFace(class_310 client, class_746 player, class_1306 arm, PendingParticleEvent pending, float eased, int count) {
        ParticleScriptSpec spec = pending.spec;
        if (spec == null || client == null || client.field_1687 == null || arm == null) {
            return;
        }
        MiningActionTracker.Target target = ModelPartsParticleSystem.resolveMiningTarget(client, spec);
        if (!ModelPartsParticleSystem.matchesMiningTarget(spec, target) || target == null || target.pos() == null) {
            return;
        }
        class_2350 face = ModelPartsParticleSystem.resolveBlockFaceSelector(spec.blockFaceSelector, target.face());
        FaceFrame frame = ModelPartsParticleSystem.createFaceFrame(target.pos(), face);
        if (frame == null) {
            return;
        }
        class_243 baseOffset = ModelPartsParticleSystem.resolveBlockFaceVector(ModelPartsParticleSystem.effectiveParticleOffset(spec, arm, spec.offsetSpace), spec.offsetSpace, frame, player, arm);
        class_243 velocityBase = ModelPartsParticleSystem.resolveBlockFaceVector(new Vector3f((Vector3fc)spec.velocity).mul(eased), spec.velocitySpace, frame, player, arm);
        class_243 centerOffset = ModelPartsParticleSystem.resolveBlockFaceVector(spec.orbitCenterOffset, spec.motionSpace, frame, player, arm);
        class_243 orbitAxis = ModelPartsParticleSystem.resolveBlockFaceVector(spec.orbitAxis, spec.motionSpace, frame, player, arm);
        if (baseOffset == null || !ModelPartsParticleSystem.isFinite(baseOffset)) {
            baseOffset = class_243.field_1353;
        }
        if (velocityBase == null || !ModelPartsParticleSystem.isFinite(velocityBase)) {
            velocityBase = class_243.field_1353;
        }
        if (centerOffset == null || !ModelPartsParticleSystem.isFinite(centerOffset)) {
            centerOffset = class_243.field_1353;
        }
        if (orbitAxis == null || !ModelPartsParticleSystem.isFinite(orbitAxis)) {
            orbitAxis = new class_243(0.0, 1.0, 0.0);
        }
        class_2960 texture = ModelPartsParticleSystem.resolveCustomParticleTexture(spec);
        Vector3f tint = ModelPartsParticleSystem.resolveCustomParticleTint(spec);
        int totalTicks = Math.max(1, Math.round(spec.lifetimeSeconds * 20.0f));
        float sizeEnd = spec.sizeEnd;
        float alphaEnd = spec.alphaEnd;
        float drag = spec.drag;
        float gravity = spec.gravity;
        String motionMode = ModelPartsParticleSystem.parseMotionMode(spec.motionMode, "linear");
        boolean orbit = "orbit".equals(motionMode);
        float orbitRadiusWorld = Math.max(0.0f, spec.orbitRadius);
        float orbitAngularVelocity = spec.orbitSpeed * ((float)Math.PI * 2) / 20.0f;
        float orbitPhaseBase = (float)Math.toRadians(spec.orbitPhaseDegrees);
        class_243 normalizedOrbitAxis = ModelPartsParticleSystem.normalizeAxis(orbitAxis);
        List active = ACTIVE_RENDER_PARTICLES.computeIfAbsent(arm, key -> new ArrayList());
        FaceUv impactUv = ModelPartsParticleSystem.resolveMiningImpactFaceUv(target);
        for (int i = 0; i < count; ++i) {
            double u = impactUv != null && ModelPartsParticleSystem.shouldUseImpactFacePoint(spec) ? impactUv.u() : class_3532.method_16436((double)client.field_1687.field_9229.method_43058(), (double)spec.faceMinU, (double)spec.faceMaxU);
            double v = impactUv != null && ModelPartsParticleSystem.shouldUseImpactFacePoint(spec) ? impactUv.v() : class_3532.method_16436((double)client.field_1687.field_9229.method_43058(), (double)spec.faceMinV, (double)spec.faceMaxV);
            class_243 facePoint = frame.corner().method_1019(frame.axisU().method_1021(u)).method_1019(frame.axisV().method_1021(v)).method_1019(frame.normal().method_1021(0.0015));
            class_243 positionJitter = ModelPartsParticleSystem.randomBlockFaceJitter(client, spec.spread, frame, 1.0);
            class_243 center = facePoint.method_1019(baseOffset).method_1019(centerOffset).method_1019(positionJitter);
            float particleOrbitPhase = orbitPhaseBase + (count > 1 ? (float)Math.PI * 2 * ((float)i / (float)count) : 0.0f);
            class_243 orbitOffset = orbit ? ModelPartsParticleSystem.orbitOffset(normalizedOrbitAxis, orbitRadiusWorld, particleOrbitPhase) : class_243.field_1353;
            class_243 spawn = center.method_1019(orbitOffset);
            class_243 velocityJitter = ModelPartsParticleSystem.randomBlockFaceJitter(client, spec.spread, frame, 0.25);
            RenderParticle particle = new RenderParticle(spawn, new class_243(velocityBase.field_1352 + velocityJitter.field_1352, velocityBase.field_1351 + velocityJitter.field_1351, velocityBase.field_1350 + velocityJitter.field_1350), totalTicks, spec.ease, spec.sizeStart, sizeEnd, spec.alphaStart, alphaEnd, drag, gravity, spec.fullBright, spec.fullDayLight, spec.brightAtDark, spec.glowTargetMenu, spec.lightPercent, spec.emissiveStrength, spec.emissiveBlur, spec.emissiveClamp, texture, tint.x, tint.y, tint.z, false, false, null, false, motionMode, center, normalizedOrbitAxis, orbitRadiusWorld, particleOrbitPhase, orbitAngularVelocity);
            ModelPartsParticleSystem.addInHandPipelineParticle(active, particle, false, 0L, spec.worldPreviewBehindLocal, spec.worldPreviewBehindLocalBias, false, false);
        }
    }

    private static void spawnEventForHeldItemBlockFace(class_310 client, class_746 player, class_1306 arm, Matrix4f itemRenderMatrix, PendingParticleEvent pending, float eased, int count) {
        ParticleScriptSpec spec = pending.spec;
        if (spec == null || client == null || arm == null || itemRenderMatrix == null) {
            return;
        }
        MiningActionTracker.Target target = ModelPartsParticleSystem.resolveMiningTarget(client, spec);
        if (!ModelPartsParticleSystem.matchesMiningTarget(spec, target)) {
            return;
        }
        class_2350 face = ModelPartsParticleSystem.resolveBlockFaceSelector(spec.blockFaceSelector, target == null ? null : target.face());
        FaceFrame frame = ModelPartsParticleSystem.createUnitCubeFaceFrame(face);
        if (frame == null) {
            return;
        }
        Vector3f localOffset = ModelPartsParticleSystem.resolveHeldItemBlockFaceVector(ModelPartsParticleSystem.effectiveParticleOffset(spec, arm, spec.offsetSpace), spec.offsetSpace, frame, itemRenderMatrix, player, arm, false);
        Vector3f localVelocity = ModelPartsParticleSystem.resolveHeldItemBlockFaceVector(new Vector3f((Vector3fc)spec.velocity).mul(eased), spec.velocitySpace, frame, itemRenderMatrix, player, arm, true);
        Vector3f localCenterOffset = ModelPartsParticleSystem.resolveHeldItemBlockFaceVector(spec.orbitCenterOffset, spec.motionSpace, frame, itemRenderMatrix, player, arm, false);
        Vector3f localOrbitAxisVec = ModelPartsParticleSystem.resolveHeldItemBlockFaceVector(spec.orbitAxis, spec.motionSpace, frame, itemRenderMatrix, player, arm, false);
        if (localOffset == null) {
            localOffset = new Vector3f();
        }
        if (localVelocity == null) {
            localVelocity = new Vector3f();
        }
        if (localCenterOffset == null) {
            localCenterOffset = new Vector3f();
        }
        if (localOrbitAxisVec == null) {
            localOrbitAxisVec = new Vector3f(0.0f, 1.0f, 0.0f);
        }
        class_2960 texture = ModelPartsParticleSystem.resolveCustomParticleTexture(spec);
        Vector3f tint = ModelPartsParticleSystem.resolveCustomParticleTint(spec);
        int totalTicks = spec.lockInModel ? Integer.MAX_VALUE : Math.max(1, Math.round(spec.lifetimeSeconds * 20.0f));
        float sizeEnd = spec.lockInModel ? spec.sizeStart : spec.sizeEnd;
        float alphaEnd = spec.lockInModel ? spec.alphaStart : spec.alphaEnd;
        float drag = spec.lockInModel ? 1.0f : spec.drag;
        float gravity = spec.lockInModel ? 0.0f : spec.gravity;
        String motionMode = spec.lockInModel ? "linear" : ModelPartsParticleSystem.parseMotionMode(spec.motionMode, "linear");
        boolean orbit = !spec.lockInModel && "orbit".equals(motionMode);
        float orbitRadius = Math.max(0.0f, spec.orbitRadius);
        float orbitAngularVelocity = spec.orbitSpeed * ((float)Math.PI * 2) / 20.0f;
        float orbitPhaseBase = (float)Math.toRadians(spec.orbitPhaseDegrees);
        class_243 localOrbitAxis = ModelPartsParticleSystem.normalizeAxis(new class_243((double)localOrbitAxisVec.x, (double)localOrbitAxisVec.y, (double)localOrbitAxisVec.z));
        List active = ACTIVE_RENDER_PARTICLES.computeIfAbsent(arm, key -> new ArrayList());
        String lockKeyBase = spec.lockInModel ? spec.lockStableKey : null;
        FaceUv impactUv = ModelPartsParticleSystem.resolveMiningImpactFaceUv(target);
        for (int i = 0; i < count; ++i) {
            String lockKey = lockKeyBase == null ? null : lockKeyBase + ":" + i;
            double u = impactUv != null && ModelPartsParticleSystem.shouldUseImpactFacePoint(spec) ? impactUv.u() : class_3532.method_16436((double)client.field_1687.field_9229.method_43058(), (double)spec.faceMinU, (double)spec.faceMaxU);
            double v = impactUv != null && ModelPartsParticleSystem.shouldUseImpactFacePoint(spec) ? impactUv.v() : class_3532.method_16436((double)client.field_1687.field_9229.method_43058(), (double)spec.faceMinV, (double)spec.faceMaxV);
            class_243 facePoint = frame.corner().method_1019(frame.axisU().method_1021(u)).method_1019(frame.axisV().method_1021(v)).method_1019(frame.normal().method_1021(0.0015));
            Vector3f spreadJitter = ModelPartsParticleSystem.randomHeldItemBlockFaceJitter(client, spec.spread, frame, 1.0f);
            double cx = facePoint.field_1352 + (double)localOffset.x + (double)localCenterOffset.x + (double)spreadJitter.x;
            double cy = facePoint.field_1351 + (double)localOffset.y + (double)localCenterOffset.y + (double)spreadJitter.y;
            double cz = facePoint.field_1350 + (double)localOffset.z + (double)localCenterOffset.z + (double)spreadJitter.z;
            class_243 center = new class_243(cx, cy, cz);
            float particleOrbitPhase = orbitPhaseBase + (count > 1 ? (float)Math.PI * 2 * ((float)i / (float)count) : 0.0f);
            class_243 orbitOffset = orbit ? ModelPartsParticleSystem.orbitOffset(localOrbitAxis, orbitRadius, particleOrbitPhase) : class_243.field_1353;
            class_243 spawn = center.method_1019(orbitOffset);
            Vector3f velocityJitter = ModelPartsParticleSystem.randomHeldItemBlockFaceJitter(client, spec.spread, frame, 0.25f);
            RenderParticle particle = new RenderParticle(spawn, new class_243(spec.lockInModel ? 0.0 : (double)(localVelocity.x + velocityJitter.x), spec.lockInModel ? 0.0 : (double)(localVelocity.y + velocityJitter.y), spec.lockInModel ? 0.0 : (double)(localVelocity.z + velocityJitter.z)), totalTicks, spec.ease, spec.sizeStart, sizeEnd, spec.alphaStart, alphaEnd, drag, gravity, spec.fullBright, spec.fullDayLight, spec.brightAtDark, spec.glowTargetMenu, spec.lightPercent, spec.emissiveStrength, spec.emissiveBlur, spec.emissiveClamp, texture, tint.x, tint.y, tint.z, spec.debug, spec.lockInModel, lockKey, true, motionMode, center, localOrbitAxis, orbitRadius, particleOrbitPhase, orbitAngularVelocity);
            ModelPartsParticleSystem.addInHandPipelineParticle(active, particle, false, 0L, spec.worldPreviewBehindLocal, spec.worldPreviewBehindLocalBias, spec.ignoreModelRotationForEmissionPoint, spec.syntheticWorldLocal);
        }
    }

    private static void addInHandPipelineParticle(List<RenderParticle> active, RenderParticle particle, boolean delayedWorldRelease, long releaseDelayNanos, boolean worldPreviewBehindLocal, float worldPreviewBehindLocalBias, boolean ignoreItemRotationForLayer, boolean syntheticWorldLocal) {
        if (active == null || particle == null) {
            return;
        }
        boolean bl = particle.renderInWorldPass = !particle.localSpace;
        if (particle.localSpace && syntheticWorldLocal) {
            particle.enableSyntheticWorldLocal();
        }
        if (particle.lockInModel) {
            particle.localSpace = true;
            particle.renderInWorldPass = false;
            particle.pendingWorldRelease = false;
            particle.pendingReleaseSnapshotReady = false;
            particle.worldPreviewBehindLocal = false;
        }
        if (particle.lockInModel && particle.lockKey != null && !particle.lockKey.isBlank()) {
            active.removeIf(existing -> existing != null && existing.lockInModel && particle.lockKey.equals(existing.lockKey));
        }
        active.add(particle);
    }

    private static class_243 resolveItemTargetLocalBase(ParticleScriptSpec spec) {
        if (spec == null) {
            return class_243.field_1353;
        }
        String targetType = ModelPartsParticleSystem.normalizeTargetType(spec.targetType, "item_center");
        if ("item_center".equals(targetType)) {
            return new class_243(0.5, 0.5, 0.5);
        }
        if ("bone".equals(targetType)) {
            return new class_243(0.5, 0.5, 0.5);
        }
        return new class_243(0.5, 0.5, 0.5);
    }

    private static Vector3f resolveHeldItemBlockFaceVector(Vector3f source, String space, FaceFrame frame, Matrix4f itemRenderMatrix, class_746 player, class_1306 arm, boolean directionOnly) {
        Vector3f value = source == null ? new Vector3f() : new Vector3f((Vector3fc)source);
        String resolved = ModelPartsParticleSystem.normalizeSpace(space, directionOnly);
        if ("block_face".equals(resolved)) {
            class_243 transformed = frame.axisU().method_1021((double)value.x).method_1019(frame.axisV().method_1021((double)value.y)).method_1019(frame.normal().method_1021((double)value.z));
            return new Vector3f((float)transformed.field_1352, (float)transformed.field_1351, (float)transformed.field_1350);
        }
        if ("block".equals(resolved) || "bone".equals(resolved)) {
            return value;
        }
        return ModelPartsParticleSystem.resolveItemLocalVector(value, resolved, itemRenderMatrix, player, arm, directionOnly, false);
    }

    private static Vector3f randomHeldItemBlockFaceJitter(class_310 client, Vector3f spread, FaceFrame frame, float scale) {
        if (client == null || spread == null || frame == null || scale <= 0.0f) {
            return new Vector3f();
        }
        return ModelPartsParticleSystem.resolveHeldItemBlockFaceVector(new Vector3f((float)ModelPartsParticleSystem.randomSpread(client, spread.x * scale), (float)ModelPartsParticleSystem.randomSpread(client, spread.y * scale), (float)ModelPartsParticleSystem.randomSpread(client, spread.z * scale)), "block_face", frame, null, null, null, false);
    }

    private static Vector3f resolveItemLocalVector(Vector3f source, String space, Matrix4f itemRenderMatrix, class_746 player, class_1306 arm, boolean directionOnly, boolean ignoreBoneRotation) {
        Vector3f value = source == null ? new Vector3f() : new Vector3f((Vector3fc)source);
        String normalized = ModelPartsParticleSystem.normalizeSpace(space, directionOnly);
        if (arm == class_1306.field_6182 && !"world".equals(normalized) && !"camera".equals(normalized)) {
            value.x = -value.x;
        }
        if ("bone".equals(normalized) || ignoreBoneRotation) {
            return value;
        }
        class_243 worldOrCamera = ModelPartsParticleSystem.applyBasis(player, arm, value);
        Matrix4f inv = new Matrix4f((Matrix4fc)itemRenderMatrix).invert();
        Vector4f v4 = new Vector4f((float)worldOrCamera.field_1352, (float)worldOrCamera.field_1351, (float)worldOrCamera.field_1350, 0.0f);
        inv.transform(v4);
        return new Vector3f(v4.x, v4.y, v4.z);
    }

    private static boolean isHeldBlockFaceTarget(ParticleScriptSpec spec) {
        return spec != null && "block_face".equals(ModelPartsParticleSystem.normalizeTargetType(spec.targetType, spec.targetType));
    }

    private static boolean isWorldBlockFaceTarget(ParticleScriptSpec spec) {
        return spec != null && "world_block_face".equals(ModelPartsParticleSystem.normalizeTargetType(spec.targetType, spec.targetType));
    }

    private static boolean isAnyBlockFaceTarget(ParticleScriptSpec spec) {
        return ModelPartsParticleSystem.isHeldBlockFaceTarget(spec) || ModelPartsParticleSystem.isWorldBlockFaceTarget(spec);
    }

    private static boolean shouldUseImpactFacePoint(ParticleScriptSpec spec) {
        if (spec == null) {
            return false;
        }
        if (spec.useImpactFacePoint) {
            return true;
        }
        return spec.faceMinU <= 1.0E-4f && spec.faceMinV <= 1.0E-4f && spec.faceMaxU >= 0.9999f && spec.faceMaxV >= 0.9999f;
    }

    private static class_243 resolveBlockFaceVector(Vector3f value, String space, FaceFrame frame, class_746 player, class_1306 arm) {
        if (value == null) {
            return class_243.field_1353;
        }
        String resolved = ModelPartsParticleSystem.normalizeSpace(space, false);
        if ("world".equals(resolved)) {
            return new class_243((double)value.x, (double)value.y, (double)value.z);
        }
        if ("camera".equals(resolved)) {
            class_243 viaCamera = ModelPartsParticleSystem.applyBasis(player, arm, value);
            return viaCamera == null ? class_243.field_1353 : viaCamera;
        }
        if ("block".equals(resolved)) {
            return new class_243((double)value.x, (double)value.y, (double)value.z);
        }
        if (frame == null) {
            return class_243.field_1353;
        }
        return frame.axisU().method_1021((double)value.x).method_1019(frame.axisV().method_1021((double)value.y)).method_1019(frame.normal().method_1021((double)value.z));
    }

    private static class_243 randomBlockFaceJitter(class_310 client, Vector3f spread, FaceFrame frame, double scale) {
        if (client == null || spread == null || frame == null || scale <= 0.0) {
            return class_243.field_1353;
        }
        Vector3f local = new Vector3f((float)ModelPartsParticleSystem.randomSpread(client, (double)spread.x * scale), (float)ModelPartsParticleSystem.randomSpread(client, (double)spread.y * scale), (float)ModelPartsParticleSystem.randomSpread(client, (double)spread.z * scale));
        return ModelPartsParticleSystem.resolveBlockFaceVector(local, "block_face", frame, null, null);
    }

    private static FaceUv resolveMiningImpactFaceUv(MiningActionTracker.Target target) {
        if (target == null || target.pos() == null || target.face() == null || target.hitLocation() == null) {
            return null;
        }
        FaceFrame hitFrame = ModelPartsParticleSystem.createFaceFrame(target.pos(), target.face());
        if (hitFrame == null) {
            return null;
        }
        class_243 relative = target.hitLocation().method_1020(hitFrame.corner());
        double u = class_3532.method_15350((double)relative.method_1026(hitFrame.axisU()), (double)0.0, (double)1.0);
        double v = class_3532.method_15350((double)relative.method_1026(hitFrame.axisV()), (double)0.0, (double)1.0);
        return new FaceUv(u, v);
    }

    private static class_2350 resolveBlockFaceSelector(String selector, class_2350 hitFace) {
        String normalized;
        return switch (normalized = ModelPartsParticleSystem.normalizeBlockFaceSelector(selector, "hit")) {
            case "down" -> class_2350.field_11033;
            case "up" -> class_2350.field_11036;
            case "north" -> class_2350.field_11043;
            case "south" -> class_2350.field_11035;
            case "west" -> class_2350.field_11039;
            case "east" -> class_2350.field_11034;
            case "opposite" -> {
                if (hitFace == null) {
                    yield null;
                }
                yield hitFace.method_10153();
            }
            case "random" -> class_2350.method_10162((class_5819)class_5819.method_43047());
            case "hit" -> hitFace;
            default -> hitFace;
        };
    }

    private static FaceFrame createFaceFrame(class_2338 pos, class_2350 face) {
        if (pos == null || face == null) {
            return null;
        }
        class_243 origin = new class_243((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260());
        return switch (face) {
            default -> throw new MatchException(null, null);
            case class_2350.field_11043 -> new FaceFrame(origin, new class_243(1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0), new class_243(0.0, 0.0, -1.0));
            case class_2350.field_11035 -> new FaceFrame(origin.method_1031(1.0, 0.0, 1.0), new class_243(-1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0), new class_243(0.0, 0.0, 1.0));
            case class_2350.field_11039 -> new FaceFrame(origin.method_1031(0.0, 0.0, 1.0), new class_243(0.0, 0.0, -1.0), new class_243(0.0, 1.0, 0.0), new class_243(-1.0, 0.0, 0.0));
            case class_2350.field_11034 -> new FaceFrame(origin.method_1031(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), new class_243(0.0, 1.0, 0.0), new class_243(1.0, 0.0, 0.0));
            case class_2350.field_11033 -> new FaceFrame(origin.method_1031(0.0, 0.0, 1.0), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, -1.0), new class_243(0.0, -1.0, 0.0));
            case class_2350.field_11036 -> new FaceFrame(origin.method_1031(0.0, 1.0, 0.0), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), new class_243(0.0, 1.0, 0.0));
        };
    }

    private static FaceFrame createUnitCubeFaceFrame(class_2350 face) {
        if (face == null) {
            return null;
        }
        return switch (face) {
            default -> throw new MatchException(null, null);
            case class_2350.field_11043 -> new FaceFrame(new class_243(0.0, 0.0, 0.0), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0), new class_243(0.0, 0.0, -1.0));
            case class_2350.field_11035 -> new FaceFrame(new class_243(1.0, 0.0, 1.0), new class_243(-1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0), new class_243(0.0, 0.0, 1.0));
            case class_2350.field_11039 -> new FaceFrame(new class_243(0.0, 0.0, 1.0), new class_243(0.0, 0.0, -1.0), new class_243(0.0, 1.0, 0.0), new class_243(-1.0, 0.0, 0.0));
            case class_2350.field_11034 -> new FaceFrame(new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), new class_243(0.0, 1.0, 0.0), new class_243(1.0, 0.0, 0.0));
            case class_2350.field_11033 -> new FaceFrame(new class_243(0.0, 0.0, 1.0), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, -1.0), new class_243(0.0, -1.0, 0.0));
            case class_2350.field_11036 -> new FaceFrame(new class_243(0.0, 1.0, 0.0), new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0), new class_243(0.0, 1.0, 0.0));
        };
    }

    private static void tickRenderParticles(class_310 client, class_1306 arm) {
        List<RenderParticle> particles;
        if (client == null || client.field_1687 == null || arm == null) {
            return;
        }
        long nowNanos = System.nanoTime();
        long lastNanos = LAST_SIM_NANOS.getOrDefault(arm, 0L);
        LAST_SIM_NANOS.put(arm, nowNanos);
        float dtSeconds = lastNanos <= 0L ? 0.016666668f : (float)((double)(nowNanos - lastNanos) / 1.0E9);
        dtSeconds = class_3532.method_15363((float)dtSeconds, (float)0.0f, (float)0.1f);
        float tickScale = dtSeconds * 20.0f;
        Matrix4f currentRoot = RENDER_ROOT.get(arm);
        Matrix4f lastRoot = LAST_SIM_ROOT.get(arm);
        Matrix4f localDelta = null;
        if (currentRoot != null && lastRoot != null) {
            localDelta = new Matrix4f((Matrix4fc)currentRoot).invert().mul((Matrix4fc)lastRoot);
        }
        if (currentRoot != null) {
            LAST_SIM_ROOT.put(arm, new Matrix4f((Matrix4fc)currentRoot));
        }
        class_243 cameraMotionPixels = ModelPartsParticleSystem.resolveCameraMotionPixels(client, arm);
        class_243 cameraTurnPixels = ModelPartsParticleSystem.resolveCameraTurnPixels(client, arm, tickScale);
        boolean shaderPackInUse = ModelPartsParticleSystem.isShaderPackInUse();
        Matrix4f syntheticCameraTurnLocalDelta = null;
        if (shaderPackInUse && currentRoot != null) {
            Matrix4f currentRootInv = RENDER_ROOT_INV.get(arm);
            Quaternionf cameraTurnDeltaCameraLocal = LAST_CAMERA_TURN_DELTA_CAMERA_LOCAL.get(arm);
            if (currentRootInv != null && cameraTurnDeltaCameraLocal != null) {
                Matrix4f cameraDelta = new Matrix4f().rotation((Quaternionfc)new Quaternionf((Quaternionfc)cameraTurnDeltaCameraLocal));
                syntheticCameraTurnLocalDelta = new Matrix4f((Matrix4fc)currentRootInv).mul((Matrix4fc)cameraDelta).mul((Matrix4fc)currentRoot);
            }
        }
        if ((particles = ACTIVE_RENDER_PARTICLES.get(arm)) == null || particles.isEmpty()) {
            return;
        }
        for (int i = particles.size() - 1; i >= 0; --i) {
            RenderParticle particle = particles.get(i);
            if (particle == null) {
                particles.remove(i);
                continue;
            }
            particle.ageTicks += tickScale;
            if (particle.ageTicks > (float)particle.totalTicks) {
                particles.remove(i);
                continue;
            }
            if (particle.localSpace && particle.pendingWorldRelease && particle.pendingReleaseSnapshotReady && ModelPartsParticleSystem.applyPendingWorldReleaseSnapshot(particle) || particle.localSpace && particle.pendingWorldRelease || particle.lockInModel) continue;
            if (particle.localSpace && localDelta != null) {
                Vector3f deltaT;
                if (particle.ignoreItemRotationForLayer) {
                    deltaT = localDelta.getTranslation(new Vector3f());
                    particle.position = particle.position.method_1031((double)deltaT.x, (double)deltaT.y, (double)deltaT.z);
                } else {
                    Vector4f p4 = new Vector4f((float)particle.position.field_1352, (float)particle.position.field_1351, (float)particle.position.field_1350, 1.0f);
                    localDelta.transform(p4);
                    particle.position = new class_243((double)p4.x, (double)p4.y, (double)p4.z);
                    Vector4f v4 = new Vector4f((float)particle.velocity.field_1352, (float)particle.velocity.field_1351, (float)particle.velocity.field_1350, 0.0f);
                    localDelta.transform(v4);
                    particle.velocity = new class_243((double)v4.x, (double)v4.y, (double)v4.z);
                }
                if (particle.orbit()) {
                    if (particle.ignoreItemRotationForLayer) {
                        deltaT = localDelta.getTranslation(new Vector3f());
                        particle.orbitCenter = particle.orbitCenter.method_1031((double)deltaT.x, (double)deltaT.y, (double)deltaT.z);
                    } else {
                        Vector4f c4 = new Vector4f((float)particle.orbitCenter.field_1352, (float)particle.orbitCenter.field_1351, (float)particle.orbitCenter.field_1350, 1.0f);
                        localDelta.transform(c4);
                        particle.orbitCenter = new class_243((double)c4.x, (double)c4.y, (double)c4.z);
                        Vector4f a4 = new Vector4f((float)particle.orbitAxis.field_1352, (float)particle.orbitAxis.field_1351, (float)particle.orbitAxis.field_1350, 0.0f);
                        localDelta.transform(a4);
                        particle.orbitAxis = ModelPartsParticleSystem.normalizeAxis(new class_243((double)a4.x, (double)a4.y, (double)a4.z));
                    }
                }
            }
            if (particle.localSpace && cameraMotionPixels != null && !particle.ignoreItemRotationForLayer) {
                particle.position = particle.position.method_1020(cameraMotionPixels);
                if (particle.orbit()) {
                    particle.orbitCenter = particle.orbitCenter.method_1020(cameraMotionPixels);
                }
            }
            if (particle.localSpace && particle.syntheticWorldLocal && syntheticCameraTurnLocalDelta != null && !particle.ignoreItemRotationForLayer) {
                Vector4f pTurn = new Vector4f((float)particle.position.field_1352, (float)particle.position.field_1351, (float)particle.position.field_1350, 1.0f);
                syntheticCameraTurnLocalDelta.transform(pTurn);
                particle.position = new class_243((double)pTurn.x, (double)pTurn.y, (double)pTurn.z);
                Vector4f vTurn = new Vector4f((float)particle.velocity.field_1352, (float)particle.velocity.field_1351, (float)particle.velocity.field_1350, 0.0f);
                syntheticCameraTurnLocalDelta.transform(vTurn);
                particle.velocity = new class_243((double)vTurn.x, (double)vTurn.y, (double)vTurn.z);
                if (particle.orbit()) {
                    Vector4f cTurn = new Vector4f((float)particle.orbitCenter.field_1352, (float)particle.orbitCenter.field_1351, (float)particle.orbitCenter.field_1350, 1.0f);
                    syntheticCameraTurnLocalDelta.transform(cTurn);
                    particle.orbitCenter = new class_243((double)cTurn.x, (double)cTurn.y, (double)cTurn.z);
                    Vector4f aTurn = new Vector4f((float)particle.orbitAxis.field_1352, (float)particle.orbitAxis.field_1351, (float)particle.orbitAxis.field_1350, 0.0f);
                    syntheticCameraTurnLocalDelta.transform(aTurn);
                    particle.orbitAxis = ModelPartsParticleSystem.normalizeAxis(new class_243((double)aTurn.x, (double)aTurn.y, (double)aTurn.z));
                }
            }
            if (particle.localSpace && cameraTurnPixels != null && !particle.syntheticWorldLocal && !particle.ignoreItemRotationForLayer) {
                particle.position = particle.position.method_1019(cameraTurnPixels);
                if (particle.orbit()) {
                    particle.orbitCenter = particle.orbitCenter.method_1019(cameraTurnPixels);
                }
            }
            if (particle.orbit()) {
                particle.orbitAngle += particle.orbitAngularVelocity * tickScale;
                class_243 target = particle.orbitCenter.method_1019(ModelPartsParticleSystem.orbitOffset(particle.orbitAxis, particle.orbitRadius, particle.orbitAngle));
                double inv = 1.0 / (double)Math.max(0.001f, tickScale);
                particle.velocity = target.method_1020(particle.position).method_1021(inv);
                particle.position = target;
                continue;
            }
            float dragTick = class_3532.method_15363((float)particle.drag, (float)0.0f, (float)1.2f);
            double dragScale = Math.pow(dragTick, tickScale);
            particle.velocity = particle.velocity.method_1021(dragScale);
            particle.velocity = particle.velocity.method_1031(0.0, (double)(particle.gravity / 20.0f * tickScale), 0.0);
            class_243 motion = particle.velocity.method_1021((double)tickScale);
            if (particle.localSpace) {
                particle.position = particle.position.method_1019(motion);
                continue;
            }
            ModelPartsParticleSystem.applyWorldCollisionStep(client, particle, motion);
        }
    }

    private static void applyWorldCollisionStep(class_310 client, RenderParticle particle, class_243 motion) {
        class_3965 blockHit;
        if (particle == null || motion == null) {
            return;
        }
        class_243 start = particle.position;
        class_243 end = start.method_1019(motion);
        if (client == null || client.field_1687 == null || !ModelPartsParticleSystem.isFinite(start) || !ModelPartsParticleSystem.isFinite(end) || motion.method_1027() <= 1.0E-8) {
            particle.position = end;
            return;
        }
        class_3965 hit = client.field_1687.method_17742(new class_3959(start, end, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)client.field_1724));
        if (!(hit instanceof class_3965) || (blockHit = hit).method_17783() != class_239.class_240.field_1332 || blockHit.method_17784() == null) {
            particle.position = end;
            return;
        }
        class_2350 face = blockHit.method_17780();
        class_243 normal = new class_243((double)face.method_10148(), (double)face.method_10164(), (double)face.method_10165());
        particle.position = blockHit.method_17784().method_1019(normal.method_1021(0.0025));
        double normalVelocity = particle.velocity.method_1026(normal);
        class_243 adjustedVelocity = particle.velocity;
        if (normalVelocity < 0.0) {
            adjustedVelocity = adjustedVelocity.method_1020(normal.method_1021(normalVelocity));
        }
        double friction = face.method_10166().method_10178() ? 0.68 : 0.55;
        adjustedVelocity = adjustedVelocity.method_1021(friction);
        if (face == class_2350.field_11036 && adjustedVelocity.field_1351 < 0.0035) {
            adjustedVelocity = new class_243(adjustedVelocity.field_1352, 0.0, adjustedVelocity.field_1350);
        } else if (face == class_2350.field_11033 && adjustedVelocity.field_1351 > -0.0035) {
            adjustedVelocity = new class_243(adjustedVelocity.field_1352, 0.0, adjustedVelocity.field_1350);
        } else if (face.method_10166().method_10179()) {
            adjustedVelocity = new class_243(Math.abs(adjustedVelocity.field_1352) < 0.0035 ? 0.0 : adjustedVelocity.field_1352, adjustedVelocity.field_1351, Math.abs(adjustedVelocity.field_1350) < 0.0035 ? 0.0 : adjustedVelocity.field_1350);
        }
        particle.velocity = adjustedVelocity;
    }

    private static boolean tryReleaseParticleFromRenderedPose(class_310 client, class_1306 arm, RenderParticle particle, Matrix4f pose, class_243 cameraPos, Quaternionf cameraRot, Vector3f currentRenderPos, boolean shaderPackInUse) {
        class_243 converted;
        class_243 converted2;
        class_243 worldPos;
        long requiredDelay;
        if (particle == null || !particle.localSpace || !particle.pendingWorldRelease || pose == null || cameraPos == null) {
            return false;
        }
        long l = requiredDelay = shaderPackInUse ? 0L : particle.worldReleaseDelayNanos;
        if (System.nanoTime() - particle.spawnNanos < requiredDelay) {
            return false;
        }
        if (currentRenderPos == null || !ModelPartsParticleSystem.isFinite(currentRenderPos)) {
            return false;
        }
        boolean releaseRenderVectorsAreCameraSpace = shaderPackInUse;
        class_243 handFallback = client != null && client.field_1724 != null && arm != null ? ModelPartsParticleSystem.resolveHandFallback(client, client.field_1724, arm) : null;
        Vector3f ghostReleaseOffset = particle.ignoreItemRotationForLayer ? ModelPartsParticleSystem.ghostReleaseOffsetForRenderPos(arm, cameraRot, shaderPackInUse) : new Vector3f();
        Vector3f adjustedRenderPos = new Vector3f((Vector3fc)currentRenderPos).add((Vector3fc)ghostReleaseOffset);
        if (particle.ignoreItemRotationForLayer) {
            class_243 ghostRootWorld = ModelPartsParticleSystem.resolveRenderRootBase(client, arm, handFallback);
            Vector3f ghostRootRender = ModelPartsParticleSystem.resolveRenderRootRenderDelta(arm);
            if (ghostRootWorld != null && ghostRootRender != null) {
                Vector3f rel = new Vector3f((Vector3fc)adjustedRenderPos).sub((Vector3fc)ghostRootRender);
                class_243 relWorld = shaderPackInUse ? ModelPartsParticleSystem.releaseVectorFromRenderSpace(new Vector4f(rel.x, rel.y, rel.z, 0.0f), cameraRot, true) : new class_243((double)rel.x, (double)rel.y, (double)rel.z);
                worldPos = ghostRootWorld.method_1019(relWorld);
            } else if (shaderPackInUse) {
                worldPos = ModelPartsParticleSystem.cameraSpaceToWorld(client, adjustedRenderPos, arm, handFallback);
                if (worldPos == null || !ModelPartsParticleSystem.isFinite(worldPos)) {
                    worldPos = ModelPartsParticleSystem.releasePointFromRenderSpace(cameraPos, adjustedRenderPos, cameraRot, true);
                }
            } else {
                worldPos = ModelPartsParticleSystem.releasePointFromRenderSpace(cameraPos, adjustedRenderPos, cameraRot, false);
            }
        } else {
            worldPos = ModelPartsParticleSystem.releasePointFromRenderSpace(cameraPos, currentRenderPos, cameraRot, releaseRenderVectorsAreCameraSpace);
        }
        Vector4f v4 = new Vector4f((float)particle.velocity.field_1352, (float)particle.velocity.field_1351, (float)particle.velocity.field_1350, 0.0f);
        if (!particle.ignoreItemRotationForLayer) {
            pose.transform(v4);
        } else {
            Vector3f scaled = ModelPartsParticleSystem.transformVectorIgnoringPoseRotation(pose, new Vector3f(v4.x, v4.y, v4.z));
            v4.set(scaled.x, scaled.y, scaled.z, 0.0f);
        }
        class_243 worldVel = particle.ignoreItemRotationForLayer ? ((converted2 = ModelPartsParticleSystem.cameraSpaceDirectionToWorld(client, new Vector3f(v4.x, v4.y, v4.z), arm, handFallback)) != null ? converted2 : ModelPartsParticleSystem.releaseVectorFromRenderSpace(v4, cameraRot, true)) : ModelPartsParticleSystem.releaseVectorFromRenderSpace(v4, cameraRot, releaseRenderVectorsAreCameraSpace);
        class_243 worldCenter = worldPos;
        if (particle.orbitCenter != null) {
            Vector4f centerDelta4 = new Vector4f((float)(particle.orbitCenter.field_1352 - particle.position.field_1352), (float)(particle.orbitCenter.field_1351 - particle.position.field_1351), (float)(particle.orbitCenter.field_1350 - particle.position.field_1350), 0.0f);
            if (!particle.ignoreItemRotationForLayer) {
                pose.transform(centerDelta4);
            } else {
                Vector3f scaled = ModelPartsParticleSystem.transformVectorIgnoringPoseRotation(pose, new Vector3f(centerDelta4.x, centerDelta4.y, centerDelta4.z));
                centerDelta4.set(scaled.x, scaled.y, scaled.z, 0.0f);
            }
            class_243 centerDeltaWorld = particle.ignoreItemRotationForLayer ? ((converted = ModelPartsParticleSystem.cameraSpaceDirectionToWorld(client, new Vector3f(centerDelta4.x, centerDelta4.y, centerDelta4.z), arm, handFallback)) != null ? converted : ModelPartsParticleSystem.releaseVectorFromRenderSpace(centerDelta4, cameraRot, true)) : ModelPartsParticleSystem.releaseVectorFromRenderSpace(centerDelta4, cameraRot, releaseRenderVectorsAreCameraSpace);
            worldCenter = worldPos.method_1019(centerDeltaWorld);
        }
        class_243 worldAxis = particle.orbitAxis == null ? new class_243(0.0, 1.0, 0.0) : particle.orbitAxis;
        Vector4f a4 = new Vector4f((float)worldAxis.field_1352, (float)worldAxis.field_1351, (float)worldAxis.field_1350, 0.0f);
        if (!particle.ignoreItemRotationForLayer) {
            pose.transform(a4);
        } else {
            Vector3f scaled = ModelPartsParticleSystem.transformVectorIgnoringPoseRotation(pose, new Vector3f(a4.x, a4.y, a4.z));
            a4.set(scaled.x, scaled.y, scaled.z, 0.0f);
        }
        worldAxis = particle.ignoreItemRotationForLayer ? ModelPartsParticleSystem.normalizeAxis((converted = ModelPartsParticleSystem.cameraSpaceDirectionToWorld(client, new Vector3f(a4.x, a4.y, a4.z), arm, handFallback)) != null ? converted : ModelPartsParticleSystem.releaseVectorFromRenderSpace(a4, cameraRot, true)) : ModelPartsParticleSystem.normalizeAxis(ModelPartsParticleSystem.releaseVectorFromRenderSpace(a4, cameraRot, releaseRenderVectorsAreCameraSpace));
        if (!(ModelPartsParticleSystem.isFinite(worldPos) && ModelPartsParticleSystem.isFinite(worldVel) && ModelPartsParticleSystem.isFinite(worldCenter) && ModelPartsParticleSystem.isFinite(worldAxis))) {
            return false;
        }
        class_243 localPosBeforeRelease = particle.position;
        Vector3f renderPosSnapshot = new Vector3f((Vector3fc)currentRenderPos);
        particle.pendingReleaseWorldPos = worldPos;
        particle.pendingReleaseWorldVel = worldVel;
        particle.pendingReleaseWorldCenter = worldCenter;
        particle.pendingReleaseWorldAxis = worldAxis;
        particle.pendingReleaseSnapshotReady = true;
        return false;
    }

    private static boolean applyPendingWorldReleaseSnapshot(RenderParticle particle) {
        if (!(particle != null && particle.localSpace && particle.pendingWorldRelease && particle.pendingReleaseSnapshotReady)) {
            return false;
        }
        if (!(ModelPartsParticleSystem.isFinite(particle.pendingReleaseWorldPos) && ModelPartsParticleSystem.isFinite(particle.pendingReleaseWorldVel) && ModelPartsParticleSystem.isFinite(particle.pendingReleaseWorldCenter) && ModelPartsParticleSystem.isFinite(particle.pendingReleaseWorldAxis))) {
            particle.pendingReleaseSnapshotReady = false;
            return false;
        }
        particle.position = particle.pendingReleaseWorldPos;
        particle.velocity = particle.pendingReleaseWorldVel;
        particle.orbitCenter = particle.pendingReleaseWorldCenter;
        particle.orbitAxis = particle.pendingReleaseWorldAxis;
        particle.localSpace = false;
        particle.pendingWorldRelease = false;
        particle.pendingReleaseSnapshotReady = false;
        return true;
    }

    private static class_243 releasePointFromRenderSpace(class_243 cameraPos, Vector3f renderPos, Quaternionf cameraRot, boolean shaderPackInUse) {
        if (cameraPos == null || renderPos == null) {
            return cameraPos == null ? class_243.field_1353 : cameraPos;
        }
        if (shaderPackInUse && cameraRot != null) {
            Vector3f rotated = new Vector3f((Vector3fc)renderPos).rotate((Quaternionfc)cameraRot);
            return cameraPos.method_1031((double)rotated.x, (double)rotated.y, (double)rotated.z);
        }
        return cameraPos.method_1031((double)renderPos.x, (double)renderPos.y, (double)renderPos.z);
    }

    private static Vector3f transformRenderPoint(Matrix4f pose, class_243 localPos) {
        if (localPos == null) {
            return new Vector3f();
        }
        Vector4f view4 = new Vector4f((float)localPos.field_1352, (float)localPos.field_1351, (float)localPos.field_1350, 1.0f);
        if (pose != null) {
            pose.transform(view4);
        }
        return new Vector3f(view4.x, view4.y, view4.z);
    }

    private static Vector3f renderPointIgnoringPoseRotation(Matrix4f pose, class_243 localPos, Quaternionf cameraRot, boolean shaderPackInUse) {
        if (localPos == null) {
            return new Vector3f();
        }
        if (pose == null) {
            return new Vector3f((float)localPos.field_1352, (float)localPos.field_1351, (float)localPos.field_1350);
        }
        Vector3f translation = pose.getTranslation(new Vector3f());
        float sx = (float)Math.sqrt(pose.m00() * pose.m00() + pose.m10() * pose.m10() + pose.m20() * pose.m20());
        float sy = (float)Math.sqrt(pose.m01() * pose.m01() + pose.m11() * pose.m11() + pose.m21() * pose.m21());
        float sz = (float)Math.sqrt(pose.m02() * pose.m02() + pose.m12() * pose.m12() + pose.m22() * pose.m22());
        if (!Float.isFinite(sx) || sx <= 0.0f) {
            sx = 1.0f;
        }
        if (!Float.isFinite(sy) || sy <= 0.0f) {
            sy = 1.0f;
        }
        if (!Float.isFinite(sz) || sz <= 0.0f) {
            sz = 1.0f;
        }
        Vector3f localScaled = new Vector3f((float)localPos.field_1352 * sx, (float)localPos.field_1351 * sy, (float)localPos.field_1350 * sz);
        if (!shaderPackInUse && cameraRot != null) {
            localScaled.rotate((Quaternionfc)cameraRot);
        }
        return new Vector3f(translation.x + localScaled.x, translation.y + localScaled.y, translation.z + localScaled.z);
    }

    private static Vector3f transformVectorIgnoringPoseRotation(Matrix4f pose, Vector3f localVec) {
        if (localVec == null) {
            return new Vector3f();
        }
        if (pose == null) {
            return new Vector3f((Vector3fc)localVec);
        }
        float sx = (float)Math.sqrt(pose.m00() * pose.m00() + pose.m10() * pose.m10() + pose.m20() * pose.m20());
        float sy = (float)Math.sqrt(pose.m01() * pose.m01() + pose.m11() * pose.m11() + pose.m21() * pose.m21());
        float sz = (float)Math.sqrt(pose.m02() * pose.m02() + pose.m12() * pose.m12() + pose.m22() * pose.m22());
        if (!Float.isFinite(sx) || sx <= 0.0f) {
            sx = 1.0f;
        }
        if (!Float.isFinite(sy) || sy <= 0.0f) {
            sy = 1.0f;
        }
        if (!Float.isFinite(sz) || sz <= 0.0f) {
            sz = 1.0f;
        }
        return new Vector3f(localVec.x * sx, localVec.y * sy, localVec.z * sz);
    }

    private static class_243 releaseVectorFromRenderSpace(Vector4f renderVec, Quaternionf cameraRot, boolean shaderPackInUse) {
        if (renderVec == null) {
            return class_243.field_1353;
        }
        Vector3f v = new Vector3f(renderVec.x, renderVec.y, renderVec.z);
        if (shaderPackInUse && cameraRot != null) {
            v.rotate((Quaternionfc)cameraRot);
        }
        return new class_243((double)v.x, (double)v.y, (double)v.z);
    }

    private static class_243 pushBehindLocalLayer(class_243 cameraPos, class_243 worldPos, float bias, Quaternionf cameraRot) {
        if (worldPos == null || !ModelPartsParticleSystem.isFinite(worldPos)) {
            return worldPos;
        }
        double distSq = cameraPos == null ? 0.0 : worldPos.method_1025(cameraPos);
        double amount = Math.max(1.0E-4, (double)bias);
        if (cameraPos != null && distSq > 1.0E-10) {
            class_243 dir = worldPos.method_1020(cameraPos).method_1029();
            return worldPos.method_1019(dir.method_1021(amount));
        }
        if (cameraRot != null) {
            Vector3f forward = new Vector3f(0.0f, 0.0f, 1.0f).rotate((Quaternionfc)cameraRot);
            return worldPos.method_1031((double)forward.x * amount, (double)forward.y * amount, (double)forward.z * amount);
        }
        return worldPos.method_1031(0.0, 0.0, amount);
    }

    private static boolean isParticleOccludedByWorld(class_310 client, class_243 cameraPos, class_243 worldPos) {
        if (client == null || client.field_1687 == null || cameraPos == null || worldPos == null) {
            return false;
        }
        if (!ModelPartsParticleSystem.isFinite(cameraPos) || !ModelPartsParticleSystem.isFinite(worldPos)) {
            return false;
        }
        class_3965 hit = client.field_1687.method_17742(new class_3959(cameraPos, worldPos, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)client.field_1724));
        if (hit == null || hit.method_17783() != class_239.class_240.field_1332 || hit.method_17784() == null) {
            return false;
        }
        double hitDist = Math.sqrt(hit.method_17784().method_1025(cameraPos));
        double particleDist = Math.sqrt(worldPos.method_1025(cameraPos));
        if (!Double.isFinite(hitDist) || !Double.isFinite(particleDist)) {
            return false;
        }
        return hitDist + 0.22 < particleDist - 0.01;
    }

    private static class_243 resolveCameraMotionPixels(class_310 client, class_1306 arm) {
        float lz;
        if (client == null || client.field_1773 == null || arm == null) {
            return class_243.field_1353;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera == null || camera.method_19326() == null || camera.method_23767() == null) {
            return class_243.field_1353;
        }
        class_243 pos = camera.method_19326();
        class_243 last = LAST_CAMERA_POS.put(arm, pos);
        if (last == null) {
            return class_243.field_1353;
        }
        class_243 delta = pos.method_1020(last);
        if (!ModelPartsParticleSystem.isFinite(delta) || delta.method_1027() > 4.0) {
            return class_243.field_1353;
        }
        Quaternionf inv = new Quaternionf((Quaternionfc)camera.method_23767()).invert();
        Vector3f cameraLocalDelta = new Vector3f((float)delta.field_1352, (float)delta.field_1351, (float)delta.field_1350).rotate((Quaternionfc)inv);
        Matrix4f invRoot = RENDER_ROOT_INV.get(arm);
        if (invRoot != null) {
            boolean shaderPackInUse = ModelPartsParticleSystem.isShaderPackInUse();
            Vector3f source = shaderPackInUse ? cameraLocalDelta : new Vector3f((float)delta.field_1352, (float)delta.field_1351, (float)delta.field_1350);
            Vector4f local = new Vector4f(source.x, source.y, source.z, 0.0f);
            invRoot.transform(local);
            if (Float.isFinite(local.x) && Float.isFinite(local.y) && Float.isFinite(local.z)) {
                return new class_243((double)local.x, (double)local.y, (double)local.z);
            }
        }
        Vector3f localDelta = cameraLocalDelta.mul(16.0f);
        float lx = localDelta.x;
        localDelta.x = lz = localDelta.z;
        localDelta.z = -lx;
        return new class_243((double)localDelta.x, (double)localDelta.y, (double)localDelta.z);
    }

    private static class_243 resolveCameraTurnPixels(class_310 client, class_1306 arm, float tickScale) {
        float simZ;
        float simY;
        float simX;
        if (client == null || client.field_1773 == null || arm == null) {
            return class_243.field_1353;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera == null || camera.method_23767() == null) {
            return class_243.field_1353;
        }
        Quaternionf current = new Quaternionf((Quaternionfc)camera.method_23767());
        Quaternionf previous = LAST_CAMERA_ROT.put(arm, new Quaternionf((Quaternionfc)current));
        if (previous == null) {
            LAST_CAMERA_TURN_DELTA_CAMERA_LOCAL.remove(arm);
            return class_243.field_1353;
        }
        Vector3f prevForward = new Vector3f(0.0f, 0.0f, 1.0f).rotate((Quaternionfc)previous);
        Vector3f currForward = new Vector3f(0.0f, 0.0f, 1.0f).rotate((Quaternionfc)current);
        Vector3f deltaWorld = currForward.sub((Vector3fc)prevForward);
        Quaternionf invCurrent = new Quaternionf((Quaternionfc)current).invert();
        Vector3f deltaCameraLocal = deltaWorld.rotate((Quaternionfc)invCurrent);
        Quaternionf cameraTurnDeltaCameraLocal = new Quaternionf((Quaternionfc)invCurrent).mul((Quaternionfc)previous).normalize();
        LAST_CAMERA_TURN_DELTA_CAMERA_LOCAL.put(arm, cameraTurnDeltaCameraLocal);
        Matrix4f invRoot = RENDER_ROOT_INV.get(arm);
        if (invRoot != null) {
            boolean shaderPackInUse = ModelPartsParticleSystem.isShaderPackInUse();
            Vector3f source = shaderPackInUse ? deltaCameraLocal : deltaWorld;
            Vector4f local = new Vector4f(source.x, source.y, source.z, 0.0f);
            invRoot.transform(local);
            if (Float.isFinite(local.x) && Float.isFinite(local.y) && Float.isFinite(local.z)) {
                simX = local.x;
                simY = local.y;
                simZ = local.z;
            } else {
                float lz;
                float lx = deltaCameraLocal.x;
                float ly = deltaCameraLocal.y;
                simX = lz = deltaCameraLocal.z;
                simY = ly;
                simZ = -lx;
            }
        } else {
            float lz;
            float lx = deltaCameraLocal.x;
            float ly = deltaCameraLocal.y;
            simX = lz = deltaCameraLocal.z;
            simY = ly;
            simZ = -lx;
        }
        float strength = 12.8f * class_3532.method_15363((float)tickScale, (float)0.0f, (float)3.0f);
        return new class_243((double)(simX * strength), (double)(simY * strength), (double)(simZ * strength));
    }

    private static class_243 orbitOffset(class_243 axis, float radius, float angleRad) {
        if (radius <= 1.0E-4f) {
            return class_243.field_1353;
        }
        class_243 normalizedAxis = ModelPartsParticleSystem.normalizeAxis(axis);
        class_243 perpendicular = ModelPartsParticleSystem.perpendicularUnit(normalizedAxis);
        Quaternionf rotation = new Quaternionf().fromAxisAngleRad((float)normalizedAxis.field_1352, (float)normalizedAxis.field_1351, (float)normalizedAxis.field_1350, angleRad);
        Vector3f rotated = new Vector3f((float)perpendicular.field_1352, (float)perpendicular.field_1351, (float)perpendicular.field_1350).rotate((Quaternionfc)rotation).mul(radius);
        return new class_243((double)rotated.x, (double)rotated.y, (double)rotated.z);
    }

    private static class_243 normalizeAxis(class_243 axis) {
        if (!ModelPartsParticleSystem.isFinite(axis) || axis.method_1027() <= 1.0E-8) {
            return new class_243(0.0, 1.0, 0.0);
        }
        return axis.method_1029();
    }

    private static class_243 perpendicularUnit(class_243 axis) {
        class_243 reference;
        class_243 normalized = ModelPartsParticleSystem.normalizeAxis(axis);
        class_243 perpendicular = normalized.method_1036(reference = Math.abs(normalized.field_1351) > 0.98 ? new class_243(1.0, 0.0, 0.0) : new class_243(0.0, 1.0, 0.0));
        if (perpendicular.method_1027() <= 1.0E-8) {
            perpendicular = normalized.method_1036(new class_243(0.0, 0.0, 1.0));
        }
        return perpendicular.method_1027() <= 1.0E-8 ? new class_243(1.0, 0.0, 0.0) : perpendicular.method_1029();
    }

    private static int resolveParticleLight(class_310 client, RenderParticle particle, int packedLight) {
        if (particle == null) {
            return packedLight;
        }
        if (PunchyConfig.enableModelPartsGlow() && particle.fullBright && particle.lightPercent < 0.0f) {
            return 0xF000F0;
        }
        float mix = particle.lightPercent < 0.0f ? (PunchyConfig.enableModelPartsGlow() && particle.fullBright ? 1.0f : 0.0f) : class_3532.method_15363((float)(particle.lightPercent / 100.0f), (float)0.0f, (float)1.0f);
        int current = packedLight;
        if (mix > 0.0f) {
            current = mix >= 1.0f ? 0xF000F0 : ModelPartsParticleSystem.lerpPackedLight(current, 0xF000F0, mix);
        }
        if (PunchyConfig.enableModelPartsGlow() && ModelPartsParticleSystem.isFullDayLightActive(client, particle)) {
            current = 0xF000F0;
        }
        return current;
    }

    private static boolean isParticleEmissiveActive(class_310 client, RenderParticle particle) {
        if (particle == null || !PunchyConfig.enableModelPartsGlow()) {
            return false;
        }
        return particle.fullBright || ModelPartsParticleSystem.isFullDayLightActive(client, particle);
    }

    private static boolean isFullDayLightActive(class_310 client, RenderParticle particle) {
        if (particle == null || !particle.fullDayLight || client == null || client.field_1687 == null) {
            return false;
        }
        if (!client.field_1687.method_8597().comp_642()) {
            return false;
        }
        long dayTime = client.field_1687.method_8532() % 24000L;
        return dayTime >= 0L && dayTime < 12000L;
    }

    private static float resolvePackedLightBrightness(class_310 client, int packedLight) {
        int block = class_3532.method_15340((int)(packedLight >> 4 & 0xFFFF), (int)0, (int)15);
        int sky = class_3532.method_15340((int)(packedLight >> 20 & 0xFFFF), (int)0, (int)15);
        float blockBrightness = (float)block / 15.0f;
        float skyBrightness = (float)sky / 15.0f;
        if (client != null && client.field_1687 != null && client.field_1687.method_8597().comp_642()) {
            float skyDarken = class_3532.method_15363((float)((float)client.field_1687.method_8594() / 15.0f), (float)0.0f, (float)1.0f);
            skyBrightness *= 1.0f - skyDarken;
        }
        return class_3532.method_15363((float)Math.max(blockBrightness, skyBrightness), (float)0.0f, (float)1.0f);
    }

    private static float smoothBrightAtDark(float current, float target) {
        if (!Float.isFinite(current)) {
            return target;
        }
        float rate = target > current ? 0.18f : 0.05f;
        return class_3532.method_16439((float)rate, (float)current, (float)target);
    }

    private static float resolveParticleRenderAlpha(class_310 client, RenderParticle particle, int sampledLight, float alpha) {
        if (particle == null || !particle.brightAtDark) {
            return alpha;
        }
        return ModelPartsParticleSystem.resolveBrightAtDarkVisibilityAlpha(client, particle, sampledLight, alpha);
    }

    private static float resolveBrightAtDarkVisibilityAlpha(class_310 client, RenderParticle particle, int packedLight, float maxAlpha) {
        if (particle == null || !particle.brightAtDark) {
            return maxAlpha;
        }
        float brightness = ModelPartsParticleSystem.resolvePackedLightBrightness(client, packedLight);
        float darkness = 1.0f - brightness;
        float clampedMaxAlpha = class_3532.method_15363((float)maxAlpha, (float)0.1f, (float)1.0f);
        float target = class_3532.method_16439((float)darkness, (float)0.1f, (float)clampedMaxAlpha);
        particle.brightAtDarkVisibilityFactor = ModelPartsParticleSystem.smoothBrightAtDark(particle.brightAtDarkVisibilityFactor, target);
        return particle.brightAtDarkVisibilityFactor;
    }

    private static float resolveBrightAtDarkGlowFactor(class_310 client, RenderParticle particle, int packedLight) {
        if (particle == null || !particle.brightAtDark) {
            return 0.0f;
        }
        float brightness = ModelPartsParticleSystem.resolvePackedLightBrightness(client, packedLight);
        float darkness = 1.0f - brightness;
        float target = class_3532.method_16439((float)darkness, (float)0.2f, (float)1.0f);
        particle.brightAtDarkGlowFactor = ModelPartsParticleSystem.smoothBrightAtDark(particle.brightAtDarkGlowFactor, target);
        return particle.brightAtDarkGlowFactor;
    }

    private static int lerpPackedLight(int from, int to, float mix) {
        float t = class_3532.method_15363((float)mix, (float)0.0f, (float)1.0f);
        int fromBlock = from >> 4 & 0xFFFF;
        int fromSky = from >> 20 & 0xFFFF;
        int toBlock = to >> 4 & 0xFFFF;
        int toSky = to >> 20 & 0xFFFF;
        int outBlock = class_3532.method_15340((int)Math.round(class_3532.method_16439((float)t, (float)fromBlock, (float)toBlock)), (int)0, (int)65535);
        int outSky = class_3532.method_15340((int)Math.round(class_3532.method_16439((float)t, (float)fromSky, (float)toSky)), (int)0, (int)65535);
        return outBlock << 4 | outSky << 20;
    }

    private static class_2960 resolveAnimatedParticleTexture(class_310 client, RenderParticle particle, class_2960 baseTexture) {
        if (client == null || particle == null || baseTexture == null) {
            return baseTexture;
        }
        if (particle.lockInModel) {
            return baseTexture;
        }
        String path = baseTexture.method_12832();
        if (path == null || !path.endsWith(".png")) {
            return baseTexture;
        }
        int dot = path.length() - 4;
        int underscore = path.lastIndexOf(95, dot - 1);
        if (underscore < 0 || underscore >= dot - 1) {
            return baseTexture;
        }
        String suffix = path.substring(underscore + 1, dot);
        if (!"0".equals(suffix)) {
            return baseTexture;
        }
        int frameCount = ModelPartsParticleSystem.getParticleAnimationFrameCount(client, baseTexture);
        if (frameCount <= 1) {
            return baseTexture;
        }
        float animFps = Math.max(1.0f, 12.0f);
        int frame = (int)Math.floor(Math.max(0.0f, particle.ageTicks) * (animFps / 20.0f)) % frameCount;
        if (frame <= 0) {
            return baseTexture;
        }
        String animatedPath = path.substring(0, underscore + 1) + frame + ".png";
        return class_2960.method_60655((String)baseTexture.method_12836(), (String)animatedPath);
    }

    private static int getParticleAnimationFrameCount(class_310 client, class_2960 baseTexture) {
        Integer cached = PARTICLE_ANIM_FRAME_COUNT_CACHE.get(baseTexture);
        if (cached != null) {
            return cached;
        }
        int detected = ModelPartsParticleSystem.detectParticleAnimationFrameCount(client, baseTexture);
        PARTICLE_ANIM_FRAME_COUNT_CACHE.put(baseTexture, detected);
        return detected;
    }

    private static int detectParticleAnimationFrameCount(class_310 client, class_2960 baseTexture) {
        try {
            if (client == null || client.method_1478() == null || baseTexture == null) {
                return 1;
            }
            String path = baseTexture.method_12832();
            if (path == null || !path.endsWith(".png")) {
                return 1;
            }
            int dot = path.length() - 4;
            int underscore = path.lastIndexOf(95, dot - 1);
            if (underscore < 0 || underscore >= dot - 1) {
                return 1;
            }
            if (!"0".equals(path.substring(underscore + 1, dot))) {
                return 1;
            }
            int count = 1;
            for (int frame = 1; frame < 64; ++frame) {
                class_2960 candidate = class_2960.method_60655((String)baseTexture.method_12836(), (String)(path.substring(0, underscore + 1) + frame + ".png"));
                Optional resource = client.method_1478().method_14486(candidate);
                if (!resource.isPresent()) break;
                ++count;
            }
            return count;
        }
        catch (Exception ignored) {
            return 1;
        }
    }

    private static int resolveParticleWorldLight(class_310 client, RenderParticle particle, class_1306 arm, class_243 cameraPos, Vector3f renderPos, int fallbackPackedLight) {
        if (client == null || client.field_1687 == null || particle == null) {
            return fallbackPackedLight;
        }
        try {
            class_243 worldPos;
            if (particle.localSpace) {
                if (client.field_1724 == null) {
                    return fallbackPackedLight;
                }
                worldPos = ModelPartsParticleSystem.resolveParticleLightSampleWorld(client, client.field_1724, arm, null, true);
                if (worldPos == null && cameraPos != null) {
                    worldPos = cameraPos;
                }
            } else {
                worldPos = particle.position;
            }
            if (worldPos == null || !ModelPartsParticleSystem.isFinite(worldPos)) {
                return fallbackPackedLight;
            }
            class_2338 pos = class_2338.method_49637((double)worldPos.field_1352, (double)worldPos.field_1351, (double)worldPos.field_1350);
            return class_761.method_23794((class_1920)client.field_1687, (class_2338)pos);
        }
        catch (Throwable ignored) {
            return fallbackPackedLight;
        }
    }

    private static class_243 resolveParticleLightSampleWorld(class_310 client, class_746 player, class_1306 arm, MiningActionTracker.Target target, boolean preferHand) {
        if (target != null) {
            if (target.hitLocation() != null && ModelPartsParticleSystem.isFinite(target.hitLocation())) {
                return target.hitLocation();
            }
            if (target.pos() != null) {
                return class_243.method_24953((class_2382)target.pos());
            }
        }
        if (player != null) {
            class_243 handFallback = ModelPartsParticleSystem.resolveHandFallback(client, player, arm);
            if (handFallback != null && ModelPartsParticleSystem.isFinite(handFallback)) {
                return handFallback;
            }
            class_243 eye = player.method_5836(1.0f);
            if (eye != null && ModelPartsParticleSystem.isFinite(eye)) {
                return eye;
            }
        }
        if (!preferHand && client != null && client.field_1765 != null && client.field_1765.method_17784() != null && ModelPartsParticleSystem.isFinite(client.field_1765.method_17784())) {
            return client.field_1765.method_17784();
        }
        return null;
    }

    private static void addBillboardQuad(class_4587.class_4665 pose, class_4588 consumer, Vector3f center, float size, float alpha, int packedLight, float red, float green, float blue) {
        if (pose == null || consumer == null || center == null) {
            return;
        }
        int r = ModelPartsParticleSystem.scaleToByte(red);
        int g = ModelPartsParticleSystem.scaleToByte(green);
        int b = ModelPartsParticleSystem.scaleToByte(blue);
        int a = ModelPartsParticleSystem.scaleToByte(alpha);
        if (a <= 0) {
            return;
        }
        float x0 = center.x - size;
        float x1 = center.x + size;
        float y0 = center.y - size;
        float y1 = center.y + size;
        float z = center.z;
        consumer.method_22918(pose.method_23761(), x0, y0, z).method_1336(r, g, b, a).method_22913(0.0f, 1.0f).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, 0.0f, 0.0f, 1.0f);
        consumer.method_22918(pose.method_23761(), x1, y0, z).method_1336(r, g, b, a).method_22913(1.0f, 1.0f).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, 0.0f, 0.0f, 1.0f);
        consumer.method_22918(pose.method_23761(), x1, y1, z).method_1336(r, g, b, a).method_22913(1.0f, 0.0f).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, 0.0f, 0.0f, 1.0f);
        consumer.method_22918(pose.method_23761(), x0, y1, z).method_1336(r, g, b, a).method_22913(0.0f, 0.0f).method_22922(class_4608.field_21444).method_60803(packedLight).method_60831(pose, 0.0f, 0.0f, 1.0f);
    }

    private static void addEmissiveBlurHalo(class_4587.class_4665 sourcePose, class_4597 buffer, class_2960 texture, boolean shaderPackInUse, boolean clamp, float size, float baseScale, float baseAlpha, float red, float green, float blue) {
        if (sourcePose == null || buffer == null || baseAlpha <= 0.0f) {
            return;
        }
        class_1921 blurRenderType = ModelPartsParticleSystem.emissiveOverlayRenderType(texture, shaderPackInUse, true, clamp);
        class_4588 blurConsumer = buffer.getBuffer(blurRenderType);
        float[] alphaWeights = new float[]{0.52f, 0.31f, 0.17f};
        float[] scaleOffsets = new float[]{0.0f, 0.08f, 0.18f};
        for (int i = 0; i < alphaWeights.length; ++i) {
            float layerAlpha = Math.min(1.0f, baseAlpha * alphaWeights[i]);
            if (layerAlpha <= 0.0f) continue;
            class_4587 blurStack = new class_4587();
            blurStack.method_23760().method_23761().set((Matrix4fc)sourcePose.method_23761());
            blurStack.method_23760().method_23762().set((Matrix3fc)sourcePose.method_23762());
            float layerScale = baseScale + scaleOffsets[i];
            blurStack.method_22905(layerScale, layerScale, layerScale);
            float layerColorScale = 1.0f - (float)i * 0.08f;
            ModelPartsParticleSystem.addBillboardQuad(blurStack.method_23760(), blurConsumer, new Vector3f(0.0f, 0.0f, 0.0f), size, layerAlpha, 0xF000F0, class_3532.method_15363((float)(red * layerColorScale), (float)0.0f, (float)1.0f), class_3532.method_15363((float)(green * layerColorScale), (float)0.0f, (float)1.0f), class_3532.method_15363((float)(blue * layerColorScale), (float)0.0f, (float)1.0f));
        }
    }

    private static class_2960 resolveCustomParticleTexture(ParticleScriptSpec spec) {
        class_2960 parsed;
        if (spec != null && spec.textureId != null && !spec.textureId.isBlank() && (parsed = class_2960.method_12829((String)spec.textureId.trim())) != null) {
            class_2960 resolved = ModelPartsParticleSystem.resolveExplicitParticleTexture(parsed);
            if (resolved != null) {
                return resolved;
            }
            Object fallbackPath = parsed.method_12832().endsWith(".png") ? parsed.method_12832() : parsed.method_12832() + ".png";
            return class_2960.method_60655((String)parsed.method_12836(), (String)fallbackPath);
        }
        if (spec == null || spec.typeId == null || spec.typeId.isBlank()) {
            return DEFAULT_PARTICLE_TEXTURE;
        }
        class_2960 fromDefinition = ModelPartsParticleSystem.resolveTextureFromParticleDefinition(spec.typeId);
        if (fromDefinition != null) {
            return fromDefinition;
        }
        class_2960 fromType = ModelPartsParticleSystem.resolveTextureFromType(spec.typeId);
        if (fromType != null) {
            return fromType;
        }
        String lowered = spec.typeId.toLowerCase(Locale.ROOT);
        if (lowered.contains("soul")) {
            return class_2960.method_60655((String)"minecraft", (String)"textures/particle/soul_0.png");
        }
        if (lowered.contains("smoke")) {
            return class_2960.method_60655((String)"minecraft", (String)"textures/particle/generic_0.png");
        }
        if (lowered.contains("campfire") || lowered.contains("ash")) {
            return class_2960.method_60655((String)"minecraft", (String)"textures/particle/ash.png");
        }
        return DEFAULT_PARTICLE_TEXTURE;
    }

    private static class_2960 resolveExplicitParticleTexture(class_2960 parsed) {
        if (parsed == null) {
            return null;
        }
        String path = parsed.method_12832();
        if (path == null || path.isBlank()) {
            return null;
        }
        LinkedHashSet<Object> candidates = new LinkedHashSet<Object>();
        Object direct = path.endsWith(".png") ? path : path + ".png";
        candidates.add(direct);
        if (!path.startsWith("textures/")) {
            String texturesPath = "textures/" + path;
            candidates.add(texturesPath.endsWith(".png") ? texturesPath : texturesPath + ".png");
            String string = "textures/particle/" + path;
            candidates.add(string.endsWith(".png") ? string : string + ".png");
        }
        if (path.startsWith("particle/")) {
            String fromParticle = "textures/" + path;
            candidates.add(fromParticle.endsWith(".png") ? fromParticle : fromParticle + ".png");
        }
        for (String string : candidates) {
            class_2960 id = class_2960.method_60655((String)parsed.method_12836(), (String)string);
            if (!ModelPartsParticleSystem.textureExists(id)) continue;
            return id;
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static class_2960 resolveTextureFromParticleDefinition(String typeId) {
        if (typeId == null) return null;
        if (typeId.isBlank()) {
            return null;
        }
        String key = typeId.trim().toLowerCase(Locale.ROOT);
        if (PARTICLE_TEXTURE_CACHE.containsKey(key)) {
            return PARTICLE_TEXTURE_CACHE.get(key);
        }
        class_2960 type = class_2960.method_12829((String)key);
        if (type == null) {
            PARTICLE_TEXTURE_CACHE.put(key, null);
            return null;
        }
        class_2960 particleDef = class_2960.method_60655((String)type.method_12836(), (String)("particles/" + type.method_12832() + ".json"));
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1478() == null) {
            PARTICLE_TEXTURE_CACHE.put(key, null);
            return null;
        }
        Optional res = mc.method_1478().method_14486(particleDef);
        if (res.isEmpty()) {
            PARTICLE_TEXTURE_CACHE.put(key, null);
            return null;
        }
        try (BufferedReader reader = ((class_3298)res.get()).method_43039();){
            class_2960 texture;
            JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            JsonArray textures = root.getAsJsonArray("textures");
            if (textures == null || textures.isEmpty()) {
                PARTICLE_TEXTURE_CACHE.put(key, null);
                class_2960 class_29602 = null;
                return class_29602;
            }
            JsonElement first = textures.get(0);
            if (first == null || !first.isJsonPrimitive()) {
                PARTICLE_TEXTURE_CACHE.put(key, null);
                class_2960 class_29603 = null;
                return class_29603;
            }
            class_2960 texRef = class_2960.method_12829((String)first.getAsString());
            if (texRef == null) {
                PARTICLE_TEXTURE_CACHE.put(key, null);
                class_2960 class_29604 = null;
                return class_29604;
            }
            String path = texRef.method_12832();
            if (path.startsWith("particle/")) {
                path = path.substring("particle/".length());
            }
            if (ModelPartsParticleSystem.textureExists(texture = class_2960.method_60655((String)texRef.method_12836(), (String)("textures/particle/" + path + ".png")))) {
                PARTICLE_TEXTURE_CACHE.put(key, texture);
                class_2960 class_29605 = texture;
                return class_29605;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        PARTICLE_TEXTURE_CACHE.put(key, null);
        return null;
    }

    private static class_2960 resolveTextureFromType(String typeId) {
        class_2960 type = class_2960.method_12829((String)typeId.trim());
        if (type == null) {
            return null;
        }
        String ns = type.method_12836();
        String path = type.method_12832();
        ArrayList<String> bases = new ArrayList<String>();
        bases.add(path);
        if (path.endsWith("_particle")) {
            bases.add(path.substring(0, path.length() - "_particle".length()));
        }
        if (path.startsWith("particle/")) {
            bases.add(path.substring("particle/".length()));
        }
        for (String base : bases) {
            if (base == null || base.isBlank()) continue;
            class_2960 direct = class_2960.method_60655((String)ns, (String)("textures/particle/" + base + ".png"));
            if (ModelPartsParticleSystem.textureExists(direct)) {
                return direct;
            }
            class_2960 frame0 = class_2960.method_60655((String)ns, (String)("textures/particle/" + base + "_0.png"));
            if (!ModelPartsParticleSystem.textureExists(frame0)) continue;
            return frame0;
        }
        return null;
    }

    private static boolean textureExists(class_2960 texture) {
        if (texture == null) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1478() == null) {
            return false;
        }
        return mc.method_1478().method_14486(texture).isPresent();
    }

    private static Vector3f resolveCustomParticleTint(ParticleScriptSpec spec) {
        String typeId;
        if (spec != null && spec.tint != null) {
            return new Vector3f(class_3532.method_15363((float)spec.tint.x, (float)0.0f, (float)1.0f), class_3532.method_15363((float)spec.tint.y, (float)0.0f, (float)1.0f), class_3532.method_15363((float)spec.tint.z, (float)0.0f, (float)1.0f));
        }
        String string = typeId = spec == null ? null : spec.typeId;
        if (typeId == null || typeId.isBlank()) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        String id = typeId.trim().toLowerCase(Locale.ROOT);
        if (id.contains("falling_lava") || id.contains("landing_lava") || id.contains("dripping_lava") || id.contains("dripstone_lava")) {
            return new Vector3f(1.0f, 0.2857143f, 0.083333336f);
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    private static int scaleToByte(float value) {
        float clamped = class_3532.method_15363((float)value, (float)0.0f, (float)1.0f);
        return Math.max(0, Math.min(255, Math.round(clamped * 255.0f)));
    }

    private static double randomSpread(class_310 client, double amount) {
        if (amount <= 0.0) {
            return 0.0;
        }
        return (client.field_1687.field_9229.method_43058() * 2.0 - 1.0) * amount;
    }

    private static class_243 resolveBoneBase(class_746 player, class_1306 arm, Map<String, class_630> parts, String requestedBone) {
        class_243 eye = player.method_5836(1.0f);
        class_243 upWorld = new class_243(0.0, 1.0, 0.0);
        class_243 forward = player.method_5828(1.0f).method_1029();
        class_243 right = upWorld.method_1036(forward);
        right = right.method_1027() < 1.0E-6 ? new class_243(arm == class_1306.field_6183 ? 1.0 : -1.0, 0.0, 0.0) : right.method_1029();
        class_243 up = forward.method_1036(right).method_1029();
        class_243 handBase = eye.method_1019(forward.method_1021(0.45)).method_1019(up.method_1021(-0.25)).method_1019(right.method_1021(arm == class_1306.field_6183 ? 0.22 : -0.22));
        String bone = ModelPartsParticleSystem.resolveBoneName(parts, requestedBone, arm);
        if (bone == null) {
            return handBase;
        }
        Matrix4f world = ModelPartsParticleSystem.computeBoneWorld(parts, bone);
        if (world == null) {
            return handBase;
        }
        Vector3f translationPixels = world.getTranslation(new Vector3f());
        class_243 local = new class_243((double)translationPixels.x / 16.0, (double)translationPixels.y / 16.0, (double)translationPixels.z / 16.0);
        return handBase.method_1019(right.method_1021(local.field_1352)).method_1019(up.method_1021(local.field_1351)).method_1019(forward.method_1021(local.field_1350));
    }

    private static class_243 applyBasis(class_746 player, class_1306 arm, Vector3f local) {
        class_243 upWorld = new class_243(0.0, 1.0, 0.0);
        class_243 forward = player.method_5828(1.0f).method_1029();
        class_243 right = upWorld.method_1036(forward);
        right = right.method_1027() < 1.0E-6 ? new class_243(arm == class_1306.field_6183 ? 1.0 : -1.0, 0.0, 0.0) : right.method_1029();
        class_243 up = forward.method_1036(right).method_1029();
        return right.method_1021((double)local.x).method_1019(up.method_1021((double)local.y)).method_1019(forward.method_1021((double)local.z));
    }

    private static class_243 resolveRenderBoneBase(class_310 client, class_746 player, class_243 fallback, Matrix4f boneTransform, Matrix4f localBoneTransform, class_1306 arm, String projectionMode) {
        if (fallback == null) {
            return class_243.field_1353;
        }
        if (boneTransform == null) {
            return fallback;
        }
        Vector3f renderPos = boneTransform.getTranslation(new Vector3f());
        class_243 exact = ModelPartsParticleSystem.cameraSpaceToWorld(client, renderPos, arm, fallback);
        if (exact != null && ModelPartsParticleSystem.isFinite(exact)) {
            return exact;
        }
        if ("raw".equals(projectionMode)) {
            if (localBoneTransform == null) {
                return fallback;
            }
            Vector3f localPos = localBoneTransform.getTranslation(new Vector3f()).mul(0.0625f);
            class_243 localOffset = ModelPartsParticleSystem.applyCameraBasis(client, localPos);
            if (localOffset == null) {
                localOffset = ModelPartsParticleSystem.applyBasis(player, arm, localPos);
            }
            return fallback.method_1019(localOffset);
        }
        return fallback;
    }

    private static class_243 applyRenderBasis(class_310 client, Matrix4f boneTransform, Matrix4f localBoneTransform, Vector3f local, class_746 player, class_1306 arm, String space, boolean worldDefault, class_243 fallback, String projectionMode, boolean ignoreBoneRotation) {
        class_243 transformed;
        Matrix4f basis;
        String resolved = ModelPartsParticleSystem.normalizeSpace(space, worldDefault);
        if ("world".equals(resolved)) {
            return new class_243((double)local.x, (double)local.y, (double)local.z);
        }
        if ("raw".equals(projectionMode)) {
            class_243 viaCamera;
            Matrix4f basis2;
            if ("camera".equals(resolved)) {
                class_243 viaCamera2 = ModelPartsParticleSystem.applyCameraBasis(client, local);
                return viaCamera2 != null ? viaCamera2 : ModelPartsParticleSystem.applyBasis(player, arm, local);
            }
            Vector3f oriented = new Vector3f((Vector3fc)local);
            Matrix4f matrix4f = ignoreBoneRotation ? null : (basis2 = localBoneTransform != null ? localBoneTransform : boneTransform);
            if (basis2 != null) {
                ModelPartsParticleSystem.transformVectorByMatrixBasis(basis2, oriented);
            }
            return (viaCamera = ModelPartsParticleSystem.applyCameraBasis(client, oriented)) != null ? viaCamera : ModelPartsParticleSystem.applyBasis(player, arm, oriented);
        }
        if ("camera".equals(resolved)) {
            class_243 transformed2 = ModelPartsParticleSystem.cameraSpaceDirectionToWorld(client, local, arm, fallback);
            if (transformed2 != null) {
                return transformed2;
            }
            return ModelPartsParticleSystem.applyBasis(player, arm, local);
        }
        Vector3f orientedLocal = new Vector3f((Vector3fc)local);
        Matrix4f matrix4f = ignoreBoneRotation ? null : (basis = localBoneTransform != null ? localBoneTransform : boneTransform);
        if (basis != null) {
            ModelPartsParticleSystem.transformVectorByMatrixBasis(basis, orientedLocal);
        }
        if ((transformed = ModelPartsParticleSystem.cameraSpaceDirectionToWorld(client, orientedLocal, arm, fallback)) != null) {
            return transformed;
        }
        return ModelPartsParticleSystem.applyBasis(player, arm, orientedLocal);
    }

    private static Vector3f applyRenderBasisLocal(Matrix4f boneTransform, Vector3f local, String space, boolean worldDefault, boolean ignoreBoneRotation) {
        String resolved = ModelPartsParticleSystem.normalizeSpace(space, worldDefault);
        Vector3f out = new Vector3f((Vector3fc)local);
        if (!ignoreBoneRotation && "bone".equals(resolved) && boneTransform != null) {
            ModelPartsParticleSystem.transformVectorByMatrixBasis(boneTransform, out);
        }
        return out;
    }

    private static void transformVectorByMatrixBasis(Matrix4f matrix, Vector3f vector) {
        if (matrix == null || vector == null) {
            return;
        }
        Vector3f bxRaw = new Vector3f(matrix.m00(), matrix.m10(), matrix.m20());
        Vector3f byRaw = new Vector3f(matrix.m01(), matrix.m11(), matrix.m21());
        Vector3f bzRaw = new Vector3f(matrix.m02(), matrix.m12(), matrix.m22());
        Vector3f bx = ModelPartsParticleSystem.normalizeOrFallback(bxRaw, new Vector3f(1.0f, 0.0f, 0.0f));
        Vector3f by = new Vector3f((Vector3fc)byRaw);
        by.sub((Vector3fc)new Vector3f((Vector3fc)bx).mul(by.dot((Vector3fc)bx)));
        by = ModelPartsParticleSystem.normalizeOrFallback(by, ModelPartsParticleSystem.pickPerpendicular(bx));
        Vector3f bz = new Vector3f((Vector3fc)bx).cross((Vector3fc)by);
        bz = ModelPartsParticleSystem.normalizeOrFallback(bz, ModelPartsParticleSystem.normalizeOrFallback(bzRaw, new Vector3f(0.0f, 0.0f, 1.0f)));
        float handedness = bxRaw.cross((Vector3fc)new Vector3f((Vector3fc)byRaw)).dot((Vector3fc)bzRaw);
        if (Float.isFinite(handedness) && handedness < 0.0f) {
            bz.negate();
        }
        float x = vector.x;
        float y = vector.y;
        float z = vector.z;
        vector.set(bx.x * x + by.x * y + bz.x * z, bx.y * x + by.y * y + bz.y * z, bx.z * x + by.z * y + bz.z * z);
    }

    private static Vector3f normalizeOrFallback(Vector3f value, Vector3f fallback) {
        Vector3f out;
        Vector3f vector3f = out = value == null ? new Vector3f() : new Vector3f((Vector3fc)value);
        if (out.lengthSquared() > 1.0E-12f && Float.isFinite(out.x) && Float.isFinite(out.y) && Float.isFinite(out.z)) {
            out.normalize();
            return out;
        }
        if (fallback != null && fallback.lengthSquared() > 1.0E-12f) {
            return new Vector3f((Vector3fc)fallback).normalize();
        }
        return new Vector3f(1.0f, 0.0f, 0.0f);
    }

    private static Vector3f pickPerpendicular(Vector3f axis) {
        if (axis == null) {
            return new Vector3f(0.0f, 1.0f, 0.0f);
        }
        Vector3f base = Math.abs(axis.y) < 0.9f ? new Vector3f(0.0f, 1.0f, 0.0f) : new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f perp = new Vector3f((Vector3fc)base).sub((Vector3fc)new Vector3f((Vector3fc)axis).mul(base.dot((Vector3fc)axis)));
        if (perp.lengthSquared() <= 1.0E-12f) {
            return new Vector3f(0.0f, 0.0f, 1.0f);
        }
        return perp.normalize();
    }

    private static Matrix4f toLocalBoneTransform(Matrix4f boneTransform, Matrix4f invRoot) {
        if (boneTransform == null) {
            return null;
        }
        if (invRoot == null) {
            return new Matrix4f((Matrix4fc)boneTransform);
        }
        return new Matrix4f((Matrix4fc)invRoot).mul((Matrix4fc)boneTransform);
    }

    private static String normalizeSpace(String value, boolean worldDefault) {
        if (value == null || value.isBlank()) {
            return worldDefault ? "world" : "bone";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("face".equals(normalized) || "blockface".equals(normalized) || "block_face".equals(normalized)) {
            return "block_face";
        }
        if ("block".equals(normalized)) {
            return "block";
        }
        if ("world".equals(normalized) || "camera".equals(normalized) || "bone".equals(normalized)) {
            return normalized;
        }
        return worldDefault ? "world" : "bone";
    }

    private static class_243 resolveHandFallback(class_310 client, class_746 player, class_1306 arm) {
        class_243 cameraBased = ModelPartsParticleSystem.resolveCameraHandFallback(client, arm);
        if (cameraBased != null) {
            return cameraBased;
        }
        return ModelPartsParticleSystem.resolvePlayerHandFallback(player, arm);
    }

    private static class_243 resolvePlayerHandFallback(class_746 player, class_1306 arm) {
        class_243 eye = player.method_5836(1.0f);
        class_243 upWorld = new class_243(0.0, 1.0, 0.0);
        class_243 forward = player.method_5828(1.0f).method_1029();
        class_243 right = upWorld.method_1036(forward);
        right = right.method_1027() < 1.0E-6 ? new class_243(arm == class_1306.field_6183 ? 1.0 : -1.0, 0.0, 0.0) : right.method_1029();
        class_243 up = forward.method_1036(right).method_1029();
        return eye.method_1019(forward.method_1021(0.45)).method_1019(up.method_1021(-0.25)).method_1019(right.method_1021(arm == class_1306.field_6183 ? 0.22 : -0.22));
    }

    private static class_243 resolveCameraHandFallback(class_310 client, class_1306 arm) {
        if (client == null || client.field_1773 == null || arm == null) {
            return null;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera == null || camera.method_19326() == null || camera.method_23767() == null) {
            return null;
        }
        class_243 cameraPos = camera.method_19326();
        Quaternionf rot = camera.method_23767();
        Vector3f rightV = new Vector3f(1.0f, 0.0f, 0.0f).rotate((Quaternionfc)rot);
        Vector3f upV = new Vector3f(0.0f, 1.0f, 0.0f).rotate((Quaternionfc)rot);
        Vector3f fwdV = new Vector3f(0.0f, 0.0f, 1.0f).rotate((Quaternionfc)rot);
        class_243 right = new class_243((double)rightV.x, (double)rightV.y, (double)rightV.z);
        class_243 up = new class_243((double)upV.x, (double)upV.y, (double)upV.z);
        class_243 forward = new class_243((double)fwdV.x, (double)fwdV.y, (double)fwdV.z);
        return cameraPos.method_1019(forward.method_1021(0.45)).method_1019(up.method_1021(-0.25)).method_1019(right.method_1021(arm == class_1306.field_6183 ? 0.22 : -0.22));
    }

    private static class_243 applyCameraBasis(class_310 client, Vector3f local) {
        if (client == null || client.field_1773 == null || local == null) {
            return null;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera == null || camera.method_23767() == null) {
            return null;
        }
        Quaternionf rot = camera.method_23767();
        Vector3f rightV = new Vector3f(1.0f, 0.0f, 0.0f).rotate((Quaternionfc)rot);
        Vector3f upV = new Vector3f(0.0f, 1.0f, 0.0f).rotate((Quaternionfc)rot);
        Vector3f fwdV = new Vector3f(0.0f, 0.0f, 1.0f).rotate((Quaternionfc)rot);
        class_243 right = new class_243((double)rightV.x, (double)rightV.y, (double)rightV.z);
        class_243 up = new class_243((double)upV.x, (double)upV.y, (double)upV.z);
        class_243 forward = new class_243((double)fwdV.x, (double)fwdV.y, (double)fwdV.z);
        return right.method_1021((double)local.x).method_1019(up.method_1021((double)local.y)).method_1019(forward.method_1021((double)local.z));
    }

    private static class_243 cameraSpaceToWorld(class_310 client, Vector3f cameraSpacePos, class_1306 arm, class_243 fallback) {
        if (client == null || client.field_1773 == null || cameraSpacePos == null) {
            return null;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera == null) {
            return null;
        }
        class_243 cameraPos = camera.method_19326();
        Quaternionf rotation = camera.method_23767();
        if (cameraPos == null || rotation == null) {
            return null;
        }
        Vector3f worldOffset = ModelPartsParticleSystem.transformByCalibration(cameraSpacePos, camera, arm, fallback);
        return cameraPos.method_1031((double)worldOffset.x, (double)worldOffset.y, (double)worldOffset.z);
    }

    private static class_243 resolveRenderRootBase(class_310 client, class_1306 arm, class_243 fallback) {
        boolean shaderPackInUse;
        Quaternionf cameraRot;
        Vector3f rootPos;
        if (arm == null) {
            return fallback == null ? class_243.field_1353 : fallback;
        }
        Matrix4f root = EXACT_INHAND_GHOST_ROOT.get(arm);
        if (root == null) {
            root = RENDER_ROOT.get(arm);
        }
        if (root == null) {
            return fallback == null ? class_243.field_1353 : fallback;
        }
        class_4184 camera = client == null || client.field_1773 == null ? null : client.field_1773.method_19418();
        class_243 cameraPos = camera == null ? null : camera.method_19326();
        class_243 resolved = ModelPartsParticleSystem.releasePointFromRenderSpace(cameraPos, rootPos = root.getTranslation(new Vector3f()), cameraRot = camera == null ? null : camera.method_23767(), shaderPackInUse = ModelPartsParticleSystem.isShaderPackInUse());
        if (resolved == null || !ModelPartsParticleSystem.isFinite(resolved)) {
            resolved = ModelPartsParticleSystem.cameraSpaceToWorld(client, rootPos, arm, fallback);
        }
        if (resolved == null || !ModelPartsParticleSystem.isFinite(resolved)) {
            return fallback == null ? class_243.field_1353 : fallback;
        }
        return resolved;
    }

    private static Vector3f ghostAnchorRenderOffsetForArm(class_1306 arm) {
        if (GHOST_ANCHOR_RENDER_OFFSET == null) {
            return new Vector3f();
        }
        Vector3f out = new Vector3f((Vector3fc)GHOST_ANCHOR_RENDER_OFFSET);
        if (arm == class_1306.field_6182) {
            out.x = -out.x;
        }
        return out;
    }

    private static Vector3f ghostReleaseOffsetForRenderPos(class_1306 arm, Quaternionf cameraRot, boolean shaderPackInUse) {
        Vector3f raw = ModelPartsParticleSystem.ghostAnchorRenderOffsetForArm(arm);
        if (raw == null) {
            return new Vector3f();
        }
        Vector3f out = new Vector3f((Vector3fc)raw);
        if (!shaderPackInUse && cameraRot != null) {
            out.rotate((Quaternionfc)cameraRot);
        }
        return out;
    }

    private static Vector3f resolveRenderRootRenderDelta(class_1306 arm) {
        if (arm == null) {
            return null;
        }
        Matrix4f root = EXACT_INHAND_GHOST_ROOT.get(arm);
        if (root == null) {
            root = RENDER_ROOT.get(arm);
        }
        if (root == null) {
            return null;
        }
        Vector3f t = root.getTranslation(new Vector3f());
        return Float.isFinite(t.x) && Float.isFinite(t.y) && Float.isFinite(t.z) ? t : null;
    }

    private static class_243 cameraSpaceDirectionToWorld(class_310 client, Vector3f cameraSpaceDir, class_1306 arm, class_243 fallback) {
        if (client == null || client.field_1773 == null || cameraSpaceDir == null) {
            return null;
        }
        class_4184 camera = client.field_1773.method_19418();
        if (camera == null) {
            return null;
        }
        Quaternionf rotation = camera.method_23767();
        if (rotation == null) {
            return null;
        }
        Vector3f worldOffset = ModelPartsParticleSystem.transformByCalibration(cameraSpaceDir, camera, arm, fallback);
        return new class_243((double)worldOffset.x, (double)worldOffset.y, (double)worldOffset.z);
    }

    private static Vector3f transformByCalibration(Vector3f source, class_4184 camera, class_1306 arm, class_243 fallback) {
        Quaternionf rotation;
        if (source == null) {
            return new Vector3f();
        }
        Quaternionf quaternionf = rotation = camera == null ? null : camera.method_23767();
        if (camera == null || rotation == null) {
            return new Vector3f((Vector3fc)source);
        }
        return new Vector3f((Vector3fc)source).rotate((Quaternionfc)rotation);
    }

    private static boolean isFinite(class_243 vec) {
        if (vec == null) {
            return false;
        }
        return Double.isFinite(vec.field_1352) && Double.isFinite(vec.field_1351) && Double.isFinite(vec.field_1350);
    }

    private static boolean isFinite(Vector3f vec) {
        if (vec == null) {
            return false;
        }
        return Float.isFinite(vec.x) && Float.isFinite(vec.y) && Float.isFinite(vec.z);
    }

    private static boolean isShaderPackInUse() {
        return IrisShaderPackCompat.isShaderPackInUse();
    }

    private static class_1921 emissiveOverlayRenderType(class_2960 texture, boolean shaderPackInUse, boolean blur, boolean clamp) {
        if (shaderPackInUse) {
            return class_1921.method_42600((class_2960)texture);
        }
        return ModelPartsParticleSystem.particleEmissiveOverlay(texture, blur, clamp);
    }

    private static class_1921 particleEmissiveOverlay(class_2960 texture, boolean blur, boolean clamp) {
        ParticleEmissiveKey key = new ParticleEmissiveKey(texture, blur, clamp);
        class_1921 cached = PARTICLE_EMISSIVE_OVERLAY_TYPES.get(key);
        if (cached != null) {
            return cached;
        }
        class_1921 created = ModelPartsParticleSystem.tryCreateParticleEmissiveOverlay(texture, blur, clamp);
        if (created == null) {
            created = class_1921.method_42600((class_2960)texture);
        }
        PARTICLE_EMISSIVE_OVERLAY_TYPES.put(key, created);
        return created;
    }

    private static class_1921 tryCreateParticleEmissiveOverlay(class_2960 texture, boolean blur, boolean clamp) {
        try {
            class_1921 value;
            Object overlayState;
            Object writeMaskState;
            Object cullState;
            Object transparencyState;
            Class<?> compositeStateClass = Class.forName("net.minecraft.client.renderer.RenderType$CompositeState");
            Method builderMethod = compositeStateClass.getDeclaredMethod("builder", new Class[0]);
            builderMethod.setAccessible(true);
            Object builder = builderMethod.invoke(null, new Object[0]);
            Class<?> builderClass = builder.getClass();
            Class<?> emptyTextureStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard");
            Class<?> textureStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$TextureStateShard");
            Class<?> texturingStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$TexturingStateShard");
            Class<?> shaderStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$ShaderStateShard");
            Class<?> transparencyStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$TransparencyStateShard");
            Class<?> cullStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$CullStateShard");
            Class<?> overlayStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$OverlayStateShard");
            Class<?> writeMaskStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard$WriteMaskStateShard");
            Constructor<?> textureCtor = textureStateClass.getDeclaredConstructor(class_2960.class, Boolean.TYPE, Boolean.TYPE);
            textureCtor.setAccessible(true);
            Object textureState = textureCtor.newInstance(texture, blur, blur);
            Method setTextureState = builderClass.getDeclaredMethod("setTextureState", emptyTextureStateClass);
            setTextureState.setAccessible(true);
            builder = setTextureState.invoke(builder, textureState);
            Constructor<?> texturingCtor = texturingStateClass.getDeclaredConstructor(String.class, Runnable.class, Runnable.class);
            texturingCtor.setAccessible(true);
            Object texturingState = texturingCtor.newInstance("punchy_particle_texture_wrap", () -> {
                GlStateManager._texParameter((int)3553, (int)10242, (int)(clamp ? 33071 : 10497));
                GlStateManager._texParameter((int)3553, (int)10243, (int)(clamp ? 33071 : 10497));
            }, () -> {});
            Method setTexturingState = builderClass.getDeclaredMethod("setTexturingState", texturingStateClass);
            setTexturingState.setAccessible(true);
            builder = setTexturingState.invoke(builder, texturingState);
            Object shaderState = ModelPartsParticleSystem.getRenderStateShardField(blur ? "RENDERTYPE_EYES_SHADER" : "RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER");
            if (shaderState != null) {
                Method setShaderState = builderClass.getDeclaredMethod("setShaderState", shaderStateClass);
                setShaderState.setAccessible(true);
                builder = setShaderState.invoke(builder, shaderState);
            }
            if ((transparencyState = ModelPartsParticleSystem.getRenderStateShardField(blur ? "ADDITIVE_TRANSPARENCY" : "TRANSLUCENT_TRANSPARENCY")) != null) {
                Method setTransparencyState = builderClass.getDeclaredMethod("setTransparencyState", transparencyStateClass);
                setTransparencyState.setAccessible(true);
                builder = setTransparencyState.invoke(builder, transparencyState);
            }
            if (!blur && (cullState = ModelPartsParticleSystem.getRenderStateShardField("NO_CULL")) != null) {
                Method setCullState = builderClass.getDeclaredMethod("setCullState", cullStateClass);
                setCullState.setAccessible(true);
                builder = setCullState.invoke(builder, cullState);
            }
            if ((writeMaskState = ModelPartsParticleSystem.getRenderStateShardField("COLOR_WRITE")) != null) {
                Method setWriteMaskState = builderClass.getDeclaredMethod("setWriteMaskState", writeMaskStateClass);
                setWriteMaskState.setAccessible(true);
                builder = setWriteMaskState.invoke(builder, writeMaskState);
            }
            if (!blur && (overlayState = ModelPartsParticleSystem.getRenderStateShardField("OVERLAY")) != null) {
                Method setOverlayState = builderClass.getDeclaredMethod("setOverlayState", overlayStateClass);
                setOverlayState.setAccessible(true);
                builder = setOverlayState.invoke(builder, overlayState);
            }
            Method createCompositeState = builderClass.getDeclaredMethod("createCompositeState", Boolean.TYPE);
            createCompositeState.setAccessible(true);
            Object compositeState = createCompositeState.invoke(builder, !blur);
            Method create = class_1921.class.getDeclaredMethod("create", String.class, class_293.class, class_293.class_5596.class, Integer.TYPE, Boolean.TYPE, Boolean.TYPE, compositeStateClass);
            create.setAccessible(true);
            Object renderType = create.invoke(null, blur ? "punchy_particle_emissive_blur" : "punchy_particle_emissive_overlay", class_290.field_1580, class_293.class_5596.field_27382, 1536, !blur, true, compositeState);
            return renderType instanceof class_1921 ? (value = (class_1921)renderType) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static Object getRenderStateShardField(String name) {
        try {
            Class<?> renderStateClass = Class.forName("net.minecraft.client.renderer.RenderStateShard");
            Field field = renderStateClass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        }
        catch (Throwable ignored) {
            return null;
        }
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

    private static float estimateUniformScale(Matrix4f matrix) {
        if (matrix == null) {
            return 1.0f;
        }
        Vector3f x = new Vector3f(matrix.m00(), matrix.m01(), matrix.m02());
        Vector3f y = new Vector3f(matrix.m10(), matrix.m11(), matrix.m12());
        Vector3f z = new Vector3f(matrix.m20(), matrix.m21(), matrix.m22());
        float sx = x.length();
        float sy = y.length();
        float sz = z.length();
        return Math.max(1.0E-4f, (sx + sy + sz) / 3.0f);
    }

    private static String resolveRenderBoneName(Map<String, Matrix4f> renderBoneTransforms, String requested, class_1306 arm) {
        if (renderBoneTransforms == null || renderBoneTransforms.isEmpty()) {
            return null;
        }
        if (requested != null && !requested.isBlank()) {
            String n;
            String side;
            if (renderBoneTransforms.containsKey(requested)) {
                return requested;
            }
            String lowered = requested.toLowerCase(Locale.ROOT);
            for (String key : renderBoneTransforms.keySet()) {
                if (key == null || !key.toLowerCase(Locale.ROOT).equals(lowered)) continue;
                return key;
            }
            if (lowered.contains("grip")) {
                side = arm == class_1306.field_6183 ? "right" : "left";
                for (String key : renderBoneTransforms.keySet()) {
                    n = key == null ? "" : key.toLowerCase(Locale.ROOT);
                    if (!n.contains("grip") || !n.contains(side)) continue;
                    return key;
                }
            }
            if (lowered.contains("item")) {
                side = arm == class_1306.field_6183 ? "right" : "left";
                for (String key : renderBoneTransforms.keySet()) {
                    n = key == null ? "" : key.toLowerCase(Locale.ROOT);
                    if (!n.contains("item") || !n.contains(side)) continue;
                    return key;
                }
            }
        }
        return null;
    }

    private static String resolveBoneName(Map<String, class_630> parts, String requested, class_1306 arm) {
        if (requested != null && !requested.isBlank()) {
            String n;
            String side;
            if (parts.containsKey(requested)) {
                return requested;
            }
            String lowered = requested.toLowerCase(Locale.ROOT);
            for (String key : parts.keySet()) {
                if (key == null || !key.toLowerCase(Locale.ROOT).equals(lowered)) continue;
                return key;
            }
            if (lowered.contains("grip")) {
                side = arm == class_1306.field_6183 ? "right" : "left";
                for (String key : parts.keySet()) {
                    n = key == null ? "" : key.toLowerCase(Locale.ROOT);
                    if (!n.contains("grip") || !n.contains(side)) continue;
                    return key;
                }
            }
            if (lowered.contains("item")) {
                side = arm == class_1306.field_6183 ? "right" : "left";
                for (String key : parts.keySet()) {
                    n = key == null ? "" : key.toLowerCase(Locale.ROOT);
                    if (!n.contains("item") || !n.contains(side)) continue;
                    return key;
                }
            }
        }
        return null;
    }

    private static Matrix4f computeBoneWorld(Map<String, class_630> parts, String boneName) {
        if (parts == null || boneName == null) {
            return null;
        }
        IdentityHashMap<class_630, String> reverse = new IdentityHashMap<class_630, String>();
        for (Map.Entry<String, class_630> entry : parts.entrySet()) {
            if (entry.getValue() == null) continue;
            reverse.put((class_630)entry.getValue(), (String)entry.getKey());
        }
        HashMap<String, String> parentByChild = new HashMap<String, String>();
        for (Map.Entry entry : parts.entrySet()) {
            Object object;
            String parentName = (String)entry.getKey();
            class_630 parentPart = (class_630)entry.getValue();
            if (parentPart == null || !((object = parentPart) instanceof ModelPartAccessor)) continue;
            ModelPartAccessor accessor = (ModelPartAccessor)object;
            object = accessor.punchy$getChildren().entrySet().iterator();
            while (object.hasNext()) {
                String byIdentity;
                Map.Entry child = (Map.Entry)object.next();
                String childName = (String)child.getKey();
                if (!parts.containsKey(childName) && (byIdentity = (String)reverse.get(child.getValue())) != null) {
                    childName = byIdentity;
                }
                if (!parts.containsKey(childName)) continue;
                parentByChild.put(childName, parentName);
            }
        }
        HashMap<String, Matrix4f> cache = new HashMap<String, Matrix4f>();
        return ModelPartsParticleSystem.computeBoneWorldRecursive(parts, parentByChild, boneName, cache);
    }

    private static Matrix4f computeBoneWorldRecursive(Map<String, class_630> parts, Map<String, String> parentByChild, String boneName, Map<String, Matrix4f> cache) {
        Matrix4f parentWorld;
        Matrix4f cached = cache.get(boneName);
        if (cached != null) {
            return new Matrix4f((Matrix4fc)cached);
        }
        class_630 part = parts.get(boneName);
        if (part == null) {
            return null;
        }
        Matrix4f local = ModelPartMath.getLocalMatrix(part);
        String parent = parentByChild.get(boneName);
        Matrix4f world = parent != null && !parent.equals(boneName) ? ((parentWorld = ModelPartsParticleSystem.computeBoneWorldRecursive(parts, parentByChild, parent, cache)) != null ? new Matrix4f((Matrix4fc)parentWorld).mul((Matrix4fc)local) : local) : local;
        cache.put(boneName, new Matrix4f((Matrix4fc)world));
        return world;
    }

    private static float resolveEmitDurationSeconds(ParticleScriptSpec spec, AnimationClip clip, float timelineTime) {
        if (spec == null || spec.ratePerSecond <= 0.0f) {
            return 0.0f;
        }
        if (spec.emitFullAnimation && clip != null && clip.getLength() > 1.0E-4f) {
            return clip.getLength();
        }
        if (spec.emitForSeconds > 0.0f) {
            return spec.emitForSeconds;
        }
        if (clip == null) {
            return 0.0f;
        }
        float length = clip.getLength();
        if (length <= 1.0E-4f) {
            return 0.0f;
        }
        float keyTime = class_3532.method_15363((float)timelineTime, (float)0.0f, (float)length);
        float remaining = length - keyTime;
        if (remaining <= 1.0E-4f && clip.isLoop()) {
            remaining = length;
        }
        return Math.max(0.0f, remaining);
    }

    private static class_2394 resolveParticle(String typeId) {
        class_2396 type;
        class_2960 id;
        if (typeId != null && !typeId.isBlank() && (id = class_2960.method_12829((String)typeId.trim())) != null && (type = (class_2396)class_7923.field_41180.method_17966(id).orElse(null)) instanceof class_2394) {
            class_2394 options = (class_2394)type;
            return options;
        }
        return class_2398.field_11205;
    }

    private static ParticleScriptSpec parseParticleSpec(String effect) {
        String[] tokens;
        if (effect == null || effect.isBlank()) {
            return null;
        }
        String normalized = effect.trim();
        if (!normalized.startsWith("particle(") || !normalized.endsWith(")")) {
            return null;
        }
        String body = normalized.substring("particle(".length(), normalized.length() - 1).trim();
        if (body.isEmpty()) {
            return null;
        }
        ParticleScriptSpec spec = new ParticleScriptSpec();
        block243: for (String token : tokens = body.split(",")) {
            int eq;
            String kv = token.trim();
            if (kv.isEmpty() || (eq = kv.indexOf(61)) <= 0 || eq >= kv.length() - 1) continue;
            String key = kv.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            String value = kv.substring(eq + 1).trim();
            switch (key) {
                case "type": {
                    spec.typeId = value;
                    continue block243;
                }
                case "bone": {
                    spec.boneName = value;
                    continue block243;
                }
                case "target": 
                case "emit_target": {
                    ModelPartsParticleSystem.applyTargetToken(spec, value);
                    continue block243;
                }
                case "target_type": 
                case "emit_target_type": {
                    spec.targetType = ModelPartsParticleSystem.normalizeTargetType(value, spec.targetType);
                    continue block243;
                }
                case "count": 
                case "burst": {
                    spec.count = ModelPartsParticleSystem.clampInt(ModelPartsParticleSystem.parseInt(value, spec.count), 0, 120);
                    continue block243;
                }
                case "chance": {
                    spec.chance = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.chance), (float)0.0f, (float)1.0f);
                    continue block243;
                }
                case "offset": {
                    spec.offset = ModelPartsParticleSystem.parseVec3(value, spec.offset);
                    continue block243;
                }
                case "spread": {
                    spec.spread = ModelPartsParticleSystem.parseVec3(value, spec.spread);
                    continue block243;
                }
                case "velocity": {
                    spec.velocity = ModelPartsParticleSystem.parseVec3(value, spec.velocity);
                    continue block243;
                }
                case "ease": {
                    spec.ease = value.toLowerCase(Locale.ROOT);
                    continue block243;
                }
                case "t": {
                    spec.t = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.t), (float)0.0f, (float)1.0f);
                    continue block243;
                }
                case "emit_for": 
                case "duration": {
                    spec.emitForSeconds = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.emitForSeconds), (float)0.0f, (float)30.0f);
                    continue block243;
                }
                case "rate": 
                case "rate_per_sec": 
                case "pps": {
                    spec.ratePerSecond = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.ratePerSecond), (float)0.0f, (float)120.0f);
                    continue block243;
                }
                case "emit_interval": 
                case "emit_every_seconds": 
                case "interval_seconds": 
                case "spawn_every_seconds": {
                    spec.emitIntervalSeconds = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.emitIntervalSeconds), (float)0.0f, (float)60.0f);
                    continue block243;
                }
                case "emit_ease": {
                    spec.emitEase = value.toLowerCase(Locale.ROOT);
                    continue block243;
                }
                case "emit_full_anim": 
                case "emit_full_animation": 
                case "full_anim": 
                case "full_animation": {
                    spec.emitFullAnimation = ModelPartsParticleSystem.parseBoolean(value, spec.emitFullAnimation);
                    continue block243;
                }
                case "life": 
                case "lifetime": {
                    spec.lifetimeSeconds = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.lifetimeSeconds), (float)0.05f, (float)60.0f);
                    continue block243;
                }
                case "size": {
                    spec.sizeStart = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.sizeStart), (float)0.001f, (float)2.0f);
                    continue block243;
                }
                case "size_end": {
                    spec.sizeEnd = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.sizeEnd), (float)0.0f, (float)2.0f);
                    continue block243;
                }
                case "alpha": {
                    spec.alphaStart = ModelPartsParticleSystem.parseNormalizedAlpha(value, spec.alphaStart);
                    continue block243;
                }
                case "alpha_end": {
                    spec.alphaEnd = ModelPartsParticleSystem.parseNormalizedAlpha(value, spec.alphaEnd);
                    continue block243;
                }
                case "drag": {
                    spec.drag = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.drag), (float)0.0f, (float)1.2f);
                    continue block243;
                }
                case "gravity": {
                    spec.gravity = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.gravity), (float)-2.0f, (float)2.0f);
                    continue block243;
                }
                case "fullbright": 
                case "full_bright": {
                    spec.fullBright = ModelPartsParticleSystem.parseBoolean(value, spec.fullBright);
                    spec.fullBrightExplicit = true;
                    continue block243;
                }
                case "full_day_light": 
                case "fulldaylight": 
                case "day_fullbright": 
                case "daylight_fullbright": {
                    spec.fullDayLight = ModelPartsParticleSystem.parseBoolean(value, spec.fullDayLight);
                    continue block243;
                }
                case "blur_glow_at_dark": 
                case "bright_at_dark": 
                case "dark_bright": 
                case "bright_in_dark": 
                case "bright_at_night": 
                case "night_bright": 
                case "full_night_light": 
                case "night_fullbright": {
                    spec.brightAtDark = ModelPartsParticleSystem.parseBoolean(value, spec.brightAtDark);
                    if (!spec.brightAtDark || spec.fullBrightExplicit) continue block243;
                    spec.fullBright = false;
                    continue block243;
                }
                case "glow_target_menu": {
                    spec.glowTargetMenu = ModelPartsParticleSystem.parseBoolean(value, spec.glowTargetMenu);
                    continue block243;
                }
                case "light_percent": 
                case "light_pct": 
                case "light": 
                case "brightness_percent": {
                    spec.lightPercent = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.lightPercent), (float)0.0f, (float)100.0f);
                    spec.lightPercentExplicit = true;
                    continue block243;
                }
                case "emissive_strength": 
                case "emission_strength": 
                case "emissive": 
                case "strength": {
                    spec.emissiveStrength = ModelPartsParticleSystem.clampInt(ModelPartsParticleSystem.parseInt(value, spec.emissiveStrength), 0, 100);
                    spec.emissiveStrengthExplicit = true;
                    continue block243;
                }
                case "emissive_blur": 
                case "emission_blur": 
                case "blur": {
                    spec.emissiveBlur = ModelPartsParticleSystem.parseBoolean(value, spec.emissiveBlur);
                    continue block243;
                }
                case "emissive_clamp": 
                case "emission_clamp": 
                case "clamp": {
                    spec.emissiveClamp = ModelPartsParticleSystem.parseBoolean(value, spec.emissiveClamp);
                    continue block243;
                }
                case "tint": 
                case "color": 
                case "rgb": {
                    spec.tint = ModelPartsParticleSystem.parseColorVec3(value, spec.tint);
                    continue block243;
                }
                case "texture": {
                    spec.textureId = value;
                    continue block243;
                }
                case "offset_space": 
                case "space_offset": {
                    spec.offsetSpace = value;
                    continue block243;
                }
                case "velocity_space": 
                case "space_velocity": 
                case "vel_space": {
                    spec.velocitySpace = value;
                    continue block243;
                }
                case "ignore_model_rotation_for_emit": 
                case "ignore_model_rotation": 
                case "emit_ignore_model_rotation": 
                case "no_model_rot_emit": {
                    spec.ignoreModelRotationForEmissionPoint = ModelPartsParticleSystem.parseBoolean(value, spec.ignoreModelRotationForEmissionPoint);
                    continue block243;
                }
                case "synthetic_world_local": 
                case "fake_world_local": 
                case "pseudo_world_local": {
                    spec.syntheticWorldLocal = ModelPartsParticleSystem.parseBoolean(value, spec.syntheticWorldLocal);
                    continue block243;
                }
                case "world_preview_behind_local": 
                case "world_layer_behind_local": 
                case "world_behind_local": 
                case "release_preview_layer": {
                    spec.worldPreviewBehindLocal = ModelPartsParticleSystem.parseBoolean(value, spec.worldPreviewBehindLocal);
                    continue block243;
                }
                case "world_preview_bias": 
                case "world_layer_bias": 
                case "behind_local_bias": 
                case "release_preview_bias": {
                    spec.worldPreviewBehindLocalBias = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.worldPreviewBehindLocalBias), (float)0.0f, (float)0.05f);
                    continue block243;
                }
                case "lock_in_model": 
                case "lock_model": 
                case "locked_in_model": 
                case "pin_in_model": 
                case "pinned_in_model": {
                    spec.lockInModel = ModelPartsParticleSystem.parseBoolean(value, spec.lockInModel);
                    continue block243;
                }
                case "motion": 
                case "movement": 
                case "motion_mode": {
                    spec.motionMode = ModelPartsParticleSystem.parseMotionMode(value, spec.motionMode);
                    continue block243;
                }
                case "motion_space": 
                case "orbit_space": {
                    spec.motionSpace = ModelPartsParticleSystem.normalizeSpace(value, false);
                    continue block243;
                }
                case "orbit_radius": 
                case "radius": {
                    spec.orbitRadius = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.orbitRadius), (float)0.0f, (float)4.0f);
                    continue block243;
                }
                case "orbit_speed": 
                case "angular_speed": {
                    spec.orbitSpeed = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.orbitSpeed), (float)-20.0f, (float)20.0f);
                    continue block243;
                }
                case "orbit_axis": 
                case "axis": {
                    spec.orbitAxis = ModelPartsParticleSystem.parseVec3(value, spec.orbitAxis);
                    continue block243;
                }
                case "orbit_phase": 
                case "phase": {
                    spec.orbitPhaseDegrees = ModelPartsParticleSystem.parseFloat(value, spec.orbitPhaseDegrees);
                    continue block243;
                }
                case "orbit_center": 
                case "center_offset": 
                case "motion_center": {
                    spec.orbitCenterOffset = ModelPartsParticleSystem.parseVec3(value, spec.orbitCenterOffset);
                    continue block243;
                }
                case "block_face": 
                case "emit_face": 
                case "mining_face": 
                case "face": {
                    spec.blockFaceSelector = ModelPartsParticleSystem.normalizeBlockFaceSelector(value, spec.blockFaceSelector);
                    continue block243;
                }
                case "trigger_block": 
                case "trigger_blocks": 
                case "block": 
                case "blocks": 
                case "mining_block": 
                case "mining_blocks": {
                    spec.triggerBlockIds = ModelPartsParticleSystem.parseNameSet(value);
                    continue block243;
                }
                case "trigger_block_state": 
                case "block_state": 
                case "mining_block_state": {
                    spec.triggerBlockState = ModelPartsParticleSystem.parseBlockStateRequirements(value);
                    continue block243;
                }
                case "face_region": 
                case "face_bounds": 
                case "surface": 
                case "surface_region": 
                case "surface_bounds": {
                    ModelPartsParticleSystem.applyFaceRegion(spec, value);
                    continue block243;
                }
                case "trigger_jump": 
                case "jump": {
                    spec.triggerJump = ModelPartsParticleSystem.parseBoolean(value, spec.triggerJump);
                    continue block243;
                }
                case "trigger_landing": 
                case "landing": {
                    spec.triggerLanding = ModelPartsParticleSystem.parseBoolean(value, spec.triggerLanding);
                    continue block243;
                }
                case "trigger_walking": 
                case "walking": 
                case "walkgin": {
                    spec.triggerWalking = ModelPartsParticleSystem.parseBoolean(value, spec.triggerWalking);
                    continue block243;
                }
                case "trigger_sprinting": 
                case "sprinting": {
                    spec.triggerSprinting = ModelPartsParticleSystem.parseBoolean(value, spec.triggerSprinting);
                    continue block243;
                }
                case "trigger_swimming": 
                case "swimming": 
                case "swiming": {
                    spec.triggerSwimming = ModelPartsParticleSystem.parseBoolean(value, spec.triggerSwimming);
                    continue block243;
                }
                case "trigger_falling": 
                case "falling": {
                    spec.triggerFalling = ModelPartsParticleSystem.parseBoolean(value, spec.triggerFalling);
                    continue block243;
                }
                case "trigger_falling_min_blocks": 
                case "falling_min_blocks": 
                case "min_fall_blocks": 
                case "min_blocks": {
                    spec.triggerFallingMinBlocks = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(value, spec.triggerFallingMinBlocks), (float)0.0f, (float)512.0f);
                    continue block243;
                }
                case "trigger_attacking": 
                case "attacking": 
                case "attack": {
                    spec.triggerAttacking = ModelPartsParticleSystem.parseBoolean(value, spec.triggerAttacking);
                    continue block243;
                }
                case "trigger_use_item": 
                case "trigger_useitem": 
                case "use_item": 
                case "useitem": {
                    spec.triggerUseItem = ModelPartsParticleSystem.parseBoolean(value, spec.triggerUseItem);
                    continue block243;
                }
                case "trigger_mining": 
                case "mining": {
                    spec.triggerMining = ModelPartsParticleSystem.parseBoolean(value, spec.triggerMining);
                    continue block243;
                }
                case "on_animation_name": 
                case "trigger_animation_name": 
                case "animation_name": {
                    spec.triggerAnimationName = value;
                    continue block243;
                }
                case "on_animation_timecode": 
                case "trigger_animation_timecode": 
                case "animation_timecode": 
                case "timecode": {
                    spec.triggerAnimationTimecode = Math.max(0.0f, ModelPartsParticleSystem.parseFloat(value, spec.triggerAnimationTimecode));
                    continue block243;
                }
                case "on_animation": 
                case "trigger_on_animation": {
                    ModelPartsParticleSystem.applyOnAnimationToken(spec, value);
                    continue block243;
                }
                case "trigger_biomes": 
                case "biome_names": 
                case "biomes": 
                case "trigger_biome": 
                case "biome": {
                    spec.triggerBiomeNames = ModelPartsParticleSystem.parseNameSet(value);
                    continue block243;
                }
                case "trigger_mode": 
                case "trigger_logic": 
                case "triggers_mode": 
                case "triggers_logic": {
                    spec.triggerModeAny = "any".equalsIgnoreCase(value) || "or".equalsIgnoreCase(value);
                    continue block243;
                }
            }
        }
        spec.projectionMode = "raw";
        spec.renderMode = "custom";
        spec.debug = false;
        ModelPartsParticleSystem.applyParticleVisualDefaults(spec);
        spec.lockStableKey = ModelPartsParticleSystem.buildLockStableKey(spec);
        return spec;
    }

    private static String buildLockStableKey(ParticleScriptSpec spec) {
        if (spec == null) {
            return "lock";
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("type=").append(spec.typeId == null ? "" : spec.typeId.trim().toLowerCase(Locale.ROOT));
        sb.append("|bone=").append(spec.boneName == null ? "" : spec.boneName.trim().toLowerCase(Locale.ROOT));
        sb.append("|target=").append(spec.targetType == null ? "" : spec.targetType.trim().toLowerCase(Locale.ROOT));
        sb.append("|texture=").append(spec.textureId == null ? "" : spec.textureId.trim().toLowerCase(Locale.ROOT));
        sb.append("|count=").append(spec.count);
        sb.append("|lock=").append(spec.lockInModel);
        return Integer.toHexString(sb.toString().hashCode());
    }

    private static float applyEase(String name, float t) {
        float clamped = class_3532.method_15363((float)t, (float)0.0f, (float)1.0f);
        if (name == null || name.isBlank() || "linear".equals(name)) {
            return clamped;
        }
        return switch (name) {
            case "ease_in_quad" -> clamped * clamped;
            case "ease_out_quad" -> 1.0f - (1.0f - clamped) * (1.0f - clamped);
            case "ease_in_out_cubic" -> {
                if (clamped < 0.5f) {
                    yield 4.0f * clamped * clamped * clamped;
                }
                yield 1.0f - (float)Math.pow(-2.0f * clamped + 2.0f, 3.0) * 0.5f;
            }
            default -> clamped;
        };
    }

    private static Vector3f effectiveParticleOffset(ParticleScriptSpec spec, class_1306 arm, String offsetSpace) {
        return spec == null || spec.offset == null ? new Vector3f() : new Vector3f((Vector3fc)spec.offset);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        }
        catch (Exception ignored) {
            return fallback;
        }
    }

    private static float parseNormalizedAlpha(String value, float fallback) {
        float parsed = ModelPartsParticleSystem.parseFloat(value, fallback);
        if (!Float.isFinite(parsed)) {
            return class_3532.method_15363((float)fallback, (float)0.0f, (float)1.0f);
        }
        if (parsed > 1.0f) {
            parsed /= 255.0f;
        }
        return class_3532.method_15363((float)parsed, (float)0.0f, (float)1.0f);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        }
        catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
            return false;
        }
        return fallback;
    }

    private static String parseProjectionMode(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("rotation".equals(normalized) || "rotate".equals(normalized)) {
            normalized = "rotated";
        }
        if ("raw".equals(normalized) || "rotated".equals(normalized)) {
            return normalized;
        }
        return fallback;
    }

    private static String parseRenderMode(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("vanilla".equals(normalized)) {
            return "world";
        }
        if ("world".equals(normalized) || "custom".equals(normalized) || "hand".equals(normalized)) {
            return normalized;
        }
        return fallback;
    }

    private static String parseMotionMode(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("linear".equals(normalized) || "default".equals(normalized) || "drag".equals(normalized)) {
            return "linear";
        }
        if ("orbit".equals(normalized) || "orbital".equals(normalized) || "circle".equals(normalized)) {
            return "orbit";
        }
        return fallback;
    }

    private static Vector3f parseVec3(String text, Vector3f fallback) {
        if (text == null || text.isBlank()) {
            return new Vector3f((Vector3fc)fallback);
        }
        String[] parts = text.trim().split(":");
        if (parts.length != 3) {
            return new Vector3f((Vector3fc)fallback);
        }
        return new Vector3f(ModelPartsParticleSystem.parseFloat(parts[0], fallback.x), ModelPartsParticleSystem.parseFloat(parts[1], fallback.y), ModelPartsParticleSystem.parseFloat(parts[2], fallback.z));
    }

    private static Vector3f parseColorVec3(String text, Vector3f fallback) {
        Vector3f base = fallback == null ? new Vector3f(1.0f, 1.0f, 1.0f) : new Vector3f((Vector3fc)fallback);
        Vector3f parsed = ModelPartsParticleSystem.parseVec3(text, base);
        if (parsed.x > 1.0f || parsed.y > 1.0f || parsed.z > 1.0f) {
            parsed.x /= 255.0f;
            parsed.y /= 255.0f;
            parsed.z /= 255.0f;
        }
        parsed.x = class_3532.method_15363((float)parsed.x, (float)0.0f, (float)1.0f);
        parsed.y = class_3532.method_15363((float)parsed.y, (float)0.0f, (float)1.0f);
        parsed.z = class_3532.method_15363((float)parsed.z, (float)0.0f, (float)1.0f);
        return parsed;
    }

    private static void applyParticleVisualDefaults(ParticleScriptSpec spec) {
        if (spec == null) {
            return;
        }
        if (spec.brightAtDark && !spec.fullBrightExplicit) {
            spec.fullBright = false;
        }
        if (ModelPartsParticleSystem.isLeafLikeParticleType(spec.typeId)) {
            if (!spec.fullBrightExplicit) {
                spec.fullBright = false;
            }
            if (!spec.emissiveStrengthExplicit) {
                spec.emissiveStrength = 0;
            }
        }
    }

    private static boolean isLeafLikeParticleType(String typeId) {
        if (typeId == null || typeId.isBlank()) {
            return false;
        }
        String normalized = typeId.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("leaves") || normalized.contains("leaf");
    }

    private static Set<String> parseNameSet(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        String[] parts = text.split("[|;]");
        HashSet<String> out = new HashSet<String>();
        for (String part : parts) {
            String normalized;
            if (part == null || (normalized = part.trim().toLowerCase(Locale.ROOT)).isBlank()) continue;
            out.add(normalized);
        }
        return out.isEmpty() ? Set.of() : out;
    }

    private static Map<String, String> parseBlockStateRequirements(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        String[] parts = text.split("[|;]");
        HashMap<String, String> out = new HashMap<String, String>();
        for (String part : parts) {
            if (part == null || part.isBlank()) continue;
            int sep = part.indexOf(58);
            if (sep <= 0 || sep >= part.length() - 1) {
                sep = part.indexOf(61);
            }
            if (sep <= 0 || sep >= part.length() - 1) continue;
            String key = part.substring(0, sep).trim();
            String value = part.substring(sep + 1).trim();
            if (key.isBlank() || value.isBlank()) continue;
            out.put(key, value);
        }
        return out.isEmpty() ? Map.of() : out;
    }

    private static void applyFaceRegion(ParticleScriptSpec spec, String value) {
        if (spec == null || value == null || value.isBlank()) {
            return;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("hit".equals(normalized) || "impact".equals(normalized) || "exact".equals(normalized)) {
            spec.useImpactFacePoint = true;
            spec.faceMinU = 0.0f;
            spec.faceMinV = 0.0f;
            spec.faceMaxU = 1.0f;
            spec.faceMaxV = 1.0f;
            return;
        }
        String[] parts = value.trim().split(":");
        if (parts.length != 4) {
            return;
        }
        spec.useImpactFacePoint = false;
        float minU = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(parts[0], spec.faceMinU), (float)0.0f, (float)1.0f);
        float minV = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(parts[1], spec.faceMinV), (float)0.0f, (float)1.0f);
        float maxU = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(parts[2], spec.faceMaxU), (float)0.0f, (float)1.0f);
        float maxV = class_3532.method_15363((float)ModelPartsParticleSystem.parseFloat(parts[3], spec.faceMaxV), (float)0.0f, (float)1.0f);
        spec.faceMinU = Math.min(minU, maxU);
        spec.faceMinV = Math.min(minV, maxV);
        spec.faceMaxU = Math.max(minU, maxU);
        spec.faceMaxV = Math.max(minV, maxV);
    }

    private static String normalizeBlockFaceSelector(String value, String fallback) {
        String normalized;
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (normalized = value.trim().toLowerCase(Locale.ROOT)) {
            case "hit", "impact", "clicked" -> "hit";
            case "random", "rand" -> "random";
            case "opposite", "back", "reverse" -> "opposite";
            case "up", "down", "north", "south", "west", "east" -> normalized;
            default -> fallback;
        };
    }

    private static void applyTargetToken(ParticleScriptSpec spec, String value) {
        if (spec == null || value == null || value.isBlank()) {
            return;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        spec.targetType = ModelPartsParticleSystem.normalizeTargetType(normalized, spec.targetType);
    }

    private static void applyOnAnimationToken(ParticleScriptSpec spec, String value) {
        if (spec == null || value == null || value.isBlank()) {
            return;
        }
        String token = value.trim();
        int at = token.lastIndexOf(64);
        if (at > 0 && at < token.length() - 1) {
            spec.triggerAnimationName = token.substring(0, at).trim();
            spec.triggerAnimationTimecode = Math.max(0.0f, ModelPartsParticleSystem.parseFloat(token.substring(at + 1).trim(), spec.triggerAnimationTimecode));
            return;
        }
        spec.triggerAnimationName = token;
    }

    private static String normalizeTargetType(String value, String fallback) {
        String normalized;
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (normalized = value.trim().toLowerCase(Locale.ROOT)) {
            case "bone", "model_part", "modelpart" -> "bone";
            case "item_center", "center_item", "center", "item" -> "item_center";
            case "block_face", "blockface", "face", "mining_face", "hit_face" -> "block_face";
            case "world_block_face", "worldblockface", "world_face", "world_hit_face", "target_block_face" -> "world_block_face";
            default -> fallback;
        };
    }

    private static final class PlaybackState {
        private AnimationClip lastClip;
        private float lastTime;
        private long lastProcessedTick = Long.MIN_VALUE;

        private PlaybackState() {
        }
    }

    private record PendingParticleEvent(ParticleScriptSpec spec, float timelineTime, float scale) {
    }

    private record TriggerContext(boolean jump, boolean landing, boolean walking, boolean sprinting, boolean swimming, boolean falling, float fallDistance, boolean attacking, boolean useItem, boolean mining, long jumpEventId, long landingEventId, long attackEventId, long miningEventId, long useItemEventId) {
        private static final TriggerContext EMPTY = new TriggerContext(false, false, false, false, false, false, 0.0f, false, false, false, 0L, 0L, 0L, 0L, 0L);
    }

    private static final class PassiveEmitter {
        private final ParticleScriptSpec spec;
        private float ageSeconds;
        private double accumulator;
        private long lastTick;
        private long lastJumpEventId;
        private long lastLandingEventId;
        private long lastAttackEventId;
        private long lastMiningEventId;
        private long lastUseItemEventId;
        private long lastAnimationWindowSerial;
        private long lastEmitNanos;

        private PassiveEmitter(ParticleScriptSpec spec) {
            this.spec = spec;
            this.ageSeconds = 0.0f;
            this.accumulator = 0.0;
            this.lastTick = Long.MIN_VALUE;
            this.lastJumpEventId = 0L;
            this.lastLandingEventId = 0L;
            this.lastAttackEventId = 0L;
            this.lastMiningEventId = 0L;
            this.lastUseItemEventId = 0L;
            this.lastAnimationWindowSerial = Long.MIN_VALUE;
            this.lastEmitNanos = 0L;
        }
    }

    private static final class ParticleScriptSpec {
        private String typeId = "minecraft:crit";
        private String boneName = "";
        private String targetType = "bone";
        private int count = 1;
        private float chance = 1.0f;
        private Vector3f offset = new Vector3f();
        private Vector3f spread = new Vector3f();
        private Vector3f velocity = new Vector3f();
        private String ease = "linear";
        private float t = 1.0f;
        private float emitForSeconds = 0.0f;
        private float ratePerSecond = 0.0f;
        private String emitEase = "linear";
        private boolean emitFullAnimation = false;
        private float emitIntervalSeconds = 0.0f;
        private float lifetimeSeconds = 1.1f;
        private float sizeStart = 0.028f;
        private float sizeEnd = 0.006f;
        private float alphaStart = 0.95f;
        private float alphaEnd = 0.0f;
        private float drag = 0.93f;
        private float gravity = 0.0f;
        private boolean fullBright = true;
        private boolean fullDayLight = false;
        private boolean brightAtDark = false;
        private boolean glowTargetMenu = false;
        private float lightPercent = -1.0f;
        private int emissiveStrength = 100;
        private boolean emissiveBlur = false;
        private boolean emissiveClamp = true;
        private boolean fullBrightExplicit = false;
        private boolean lightPercentExplicit = false;
        private boolean emissiveStrengthExplicit = false;
        private Vector3f tint = null;
        private String textureId = "";
        private String offsetSpace = "bone";
        private String velocitySpace = "world";
        private boolean ignoreModelRotationForEmissionPoint = true;
        private boolean syntheticWorldLocal = true;
        private boolean worldPreviewBehindLocal = true;
        private float worldPreviewBehindLocalBias = 0.0015f;
        private boolean lockInModel = false;
        private String motionMode = "linear";
        private String motionSpace = "bone";
        private float orbitRadius = 0.0f;
        private float orbitSpeed = 0.0f;
        private Vector3f orbitAxis = new Vector3f(0.0f, 1.0f, 0.0f);
        private float orbitPhaseDegrees = 0.0f;
        private Vector3f orbitCenterOffset = new Vector3f();
        private String blockFaceSelector = "hit";
        private float faceMinU = 0.0f;
        private float faceMinV = 0.0f;
        private float faceMaxU = 1.0f;
        private float faceMaxV = 1.0f;
        private boolean useImpactFacePoint = false;
        private boolean triggerJump = false;
        private boolean triggerLanding = false;
        private boolean triggerWalking = false;
        private boolean triggerSprinting = false;
        private boolean triggerSwimming = false;
        private boolean triggerFalling = false;
        private float triggerFallingMinBlocks = 0.0f;
        private boolean triggerAttacking = false;
        private boolean triggerUseItem = false;
        private boolean triggerMining = false;
        private Set<String> triggerBlockIds = Set.of();
        private Map<String, String> triggerBlockState = Map.of();
        private String triggerAnimationName = "";
        private float triggerAnimationTimecode = -1.0f;
        private Set<String> triggerBiomeNames = Set.of();
        private boolean triggerModeAny = false;
        private String projectionMode = "raw";
        private boolean debug = false;
        private String renderMode = "custom";
        private String lockStableKey = "lock";

        private ParticleScriptSpec() {
        }

        private boolean hasAnimationTrigger() {
            return this.triggerAnimationName != null && !this.triggerAnimationName.isBlank() && this.triggerAnimationTimecode >= 0.0f;
        }
    }

    private static final class TriggerRuntimeState {
        private boolean initialized = false;
        private boolean lastOnGround = true;
        private boolean lastAttackKeyDown = false;
        private boolean lastSwinging = false;
        private int jumpGraceTicks = 0;
        private int landingGraceTicks = 0;
        private int attackGraceTicks = 0;
        private int miningGraceTicks = 0;
        private long jumpEventId = 0L;
        private long landingEventId = 0L;
        private long attackEventId = 0L;
        private long miningEventId = 0L;

        private TriggerRuntimeState() {
        }
    }

    private static final class AnimationTriggerWindow {
        private long serial;
        private boolean active;
        private boolean clipChanged;
        private boolean looping;
        private String currentClipName = "";
        private float previousTimeSeconds;
        private float currentTimeSeconds;
        private float clipLengthSeconds;

        private AnimationTriggerWindow() {
        }
    }

    private static final class ActiveEmitter {
        private final ParticleScriptSpec spec;
        private final int totalTicks;
        private float ageSeconds;
        private double accumulator;
        private long lastEmitNanos;

        private ActiveEmitter(ParticleScriptSpec spec, int totalTicks) {
            this.spec = spec;
            this.totalTicks = Math.max(1, totalTicks);
            this.ageSeconds = 0.0f;
            this.accumulator = 0.0;
            this.lastEmitNanos = 0L;
        }
    }

    private static final class RenderParticle {
        private class_243 position;
        private class_243 velocity;
        private final int totalTicks;
        private float ageTicks;
        private final String ease;
        private final float sizeStart;
        private final float sizeEnd;
        private final float alphaStart;
        private final float alphaEnd;
        private final float drag;
        private final float gravity;
        private final boolean fullBright;
        private final boolean fullDayLight;
        private final boolean brightAtDark;
        private final boolean glowTargetMenu;
        private final float lightPercent;
        private final int emissiveStrength;
        private final boolean emissiveBlur;
        private final boolean emissiveClamp;
        private final class_2960 texture;
        private final float red;
        private final float green;
        private final float blue;
        private final boolean debug;
        private final boolean lockInModel;
        private final String lockKey;
        private boolean localSpace;
        private boolean pendingWorldRelease;
        private long spawnNanos;
        private long worldReleaseDelayNanos;
        private boolean renderInWorldPass;
        private boolean worldPreviewBehindLocal;
        private float worldPreviewBehindLocalBias;
        private boolean ignoreItemRotationForLayer;
        private boolean syntheticWorldLocal;
        private boolean pendingReleaseSnapshotReady;
        private class_243 pendingReleaseWorldPos;
        private class_243 pendingReleaseWorldVel;
        private class_243 pendingReleaseWorldCenter;
        private class_243 pendingReleaseWorldAxis;
        private float brightAtDarkVisibilityFactor;
        private float brightAtDarkGlowFactor;
        private final String motionMode;
        private class_243 orbitCenter;
        private class_243 orbitAxis;
        private final float orbitRadius;
        private float orbitAngle;
        private final float orbitAngularVelocity;

        private RenderParticle(class_243 position, class_243 velocity, int totalTicks, String ease, float sizeStart, float sizeEnd, float alphaStart, float alphaEnd, float drag, float gravity, boolean fullBright, boolean fullDayLight, boolean brightAtDark, boolean glowTargetMenu, float lightPercent, int emissiveStrength, boolean emissiveBlur, boolean emissiveClamp, class_2960 texture, float red, float green, float blue, boolean debug, boolean lockInModel, String lockKey, boolean localSpace, String motionMode, class_243 orbitCenter, class_243 orbitAxis, float orbitRadius, float orbitAngle, float orbitAngularVelocity) {
            this.position = position;
            this.velocity = velocity;
            this.totalTicks = Math.max(1, totalTicks);
            this.ageTicks = 0.0f;
            this.ease = ease == null || ease.isBlank() ? "linear" : ease;
            this.sizeStart = sizeStart;
            this.sizeEnd = sizeEnd;
            this.alphaStart = alphaStart;
            this.alphaEnd = alphaEnd;
            this.drag = drag;
            this.gravity = gravity;
            this.fullBright = fullBright;
            this.fullDayLight = fullDayLight;
            this.brightAtDark = brightAtDark;
            this.glowTargetMenu = glowTargetMenu;
            this.lightPercent = Float.isFinite(lightPercent) ? lightPercent : -1.0f;
            this.emissiveStrength = class_3532.method_15340((int)emissiveStrength, (int)0, (int)100);
            this.emissiveBlur = emissiveBlur;
            this.emissiveClamp = emissiveClamp;
            this.texture = texture;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.debug = debug;
            this.lockInModel = lockInModel;
            this.lockKey = lockKey;
            this.localSpace = localSpace;
            this.pendingWorldRelease = false;
            this.spawnNanos = System.nanoTime();
            this.worldReleaseDelayNanos = 0L;
            this.renderInWorldPass = false;
            this.worldPreviewBehindLocal = false;
            this.worldPreviewBehindLocalBias = 0.0015f;
            this.ignoreItemRotationForLayer = false;
            this.syntheticWorldLocal = false;
            this.pendingReleaseSnapshotReady = false;
            this.pendingReleaseWorldPos = null;
            this.pendingReleaseWorldVel = null;
            this.pendingReleaseWorldCenter = null;
            this.pendingReleaseWorldAxis = null;
            this.brightAtDarkVisibilityFactor = Float.NaN;
            this.brightAtDarkGlowFactor = Float.NaN;
            this.motionMode = ModelPartsParticleSystem.parseMotionMode(motionMode, "linear");
            this.orbitCenter = orbitCenter == null ? position : orbitCenter;
            this.orbitAxis = ModelPartsParticleSystem.normalizeAxis(orbitAxis);
            this.orbitRadius = Math.max(0.0f, orbitRadius);
            this.orbitAngle = orbitAngle;
            this.orbitAngularVelocity = orbitAngularVelocity;
        }

        private boolean orbit() {
            return "orbit".equals(this.motionMode) && this.orbitRadius > 1.0E-4f;
        }

        private void enableDelayedWorldRelease(long delayNanos) {
            if (!this.localSpace) {
                return;
            }
            this.pendingWorldRelease = true;
            this.spawnNanos = System.nanoTime();
            this.worldReleaseDelayNanos = Math.max(0L, delayNanos);
            this.pendingReleaseSnapshotReady = false;
            this.pendingReleaseWorldPos = null;
            this.pendingReleaseWorldVel = null;
            this.pendingReleaseWorldCenter = null;
            this.pendingReleaseWorldAxis = null;
        }

        private void enableWorldPreviewBehindLocal(float bias) {
            this.worldPreviewBehindLocal = true;
            this.worldPreviewBehindLocalBias = class_3532.method_15363((float)bias, (float)0.0f, (float)0.05f);
        }

        private void enableIgnoreItemRotationForLayer() {
            this.ignoreItemRotationForLayer = true;
        }

        private void enableSyntheticWorldLocal() {
            this.syntheticWorldLocal = true;
        }
    }

    private record FaceFrame(class_243 corner, class_243 axisU, class_243 axisV, class_243 normal) {
    }

    private record FaceUv(double u, double v) {
    }

    private record ParticleEmissiveKey(class_2960 texture, boolean blur, boolean clamp) {
    }
}

