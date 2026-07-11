/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1262
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1306
 *  net.minecraft.class_1309
 *  net.minecraft.class_1676
 *  net.minecraft.class_1799
 *  net.minecraft.class_2371
 *  net.minecraft.class_238
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 *  net.minecraft.class_2487
 *  net.minecraft.class_2520
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3883
 *  net.minecraft.class_3966
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_7225$class_7874
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 *  net.minecraft.class_7833
 *  net.minecraft.class_7923
 *  org.lwjgl.glfw.GLFW
 *  org.slf4j.Logger
 *  org.slf4j.helpers.NOPLogger
 */
package punchy.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1262;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1676;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3883;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7225;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.ToolTuning;
import punchy.config.PunchyTuningConfig;
import punchy.platform.Services;

public final class AdorableHamsterPetsCompat {
    private static final Logger LOGGER = NOPLogger.NOP_LOGGER;
    private static final boolean DEBUG_THROW_LOGS = true;
    private static final String MOD_ID = "adorablehamsterpets";
    private static final String CHANGE_TO_HAMSTER = "change_to_hamster";
    private static final String CHANGE_TO_HAMSTER_PREFIX = "change_to_hamster_";
    private static final String CHANGE_TO_HAMSTER_OFF_PREFIX = "change_to_hamster_off_";
    private static final String CHANGE_TO_ITEM = "change_to_item";
    private static final String SHOULDER_MOUNT_ANIMATION = "adorable_cute_feat";
    private static final String SHOULDER_YEET_ANIMATION = "adorable_cute_feat2";
    private static final String TYPE_HAMSTER_RIGHT_SHOULDER = "hamster_r_shoulder";
    private static final String TYPE_HAMSTER_LEFT_SHOULDER = "hamster_l_shoulder";
    private static final String TYPE_HAMSTER_HEAD = "hamster_head";
    private static final String TYPE_HAMSTER_YEET = "hamster_yeet";
    private static final String TYPE_HAMSTER_THROW_RIGHT_SOULDER = "hamster_throw_right_soulder";
    private static final String TYPE_HAMSTER_THROW_LEFT_SOULDER = "hamster_throw_left_soulder";
    private static final String TYPE_HAMSTER_THROW_HEAD = "hamster_throw_head";
    private static final String TYPE_HAMSTER_PET = "hamster_pet";
    private static final String TYPE_HAMSTER_DISMOUNT = "hamster_dismount";
    private static final String TYPE_USE_ITEM = "useitem";
    private static final class_2960 HAMSTER_ID = class_2960.method_60655((String)"adorablehamsterpets", (String)"hamster");
    private static final class_2960 HAMSTER_PROJECTILE_ID = class_2960.method_60655((String)"adorablehamsterpets", (String)"hamster_projectile");
    private static final float HAMSTER_SCALE = 0.55f;
    private static final double WALK_SPEED = 0.03;
    private static final int TRANSIT_HOLD_TICKS = 30;
    private static boolean availabilityChecked;
    private static boolean available;
    private static boolean reflectionReady;
    private static boolean reflectionFailed;
    private static Object rightShoulderSlot;
    private static Object leftShoulderSlot;
    private static Object headSlot;
    private static Method getShoulderHamsterMethod;
    private static Method getMountOrderQueueMethod;
    private static Method getNextHamsterToDismountDataMethod;
    private static Method getClientHamsterStateMethod;
    private static Method hamsterStateFromNbtMethod;
    private static Method hamsterStateToNbtMethod;
    private static Method saveToHamsterStateMethod;
    private static Method applyHamsterStateMethod;
    private static Method updateDummyStateMethod;
    private static Method setShoulderPetMethod;
    private static Method setSittingMethod;
    private static Method setBeggingMethod;
    private static Method setSleepingMethod;
    private static Method setKnockedOutMethod;
    private static Method setSulkingMethod;
    private static Method setTauntingMethod;
    private static Method setPresentingItemMethod;
    private static Method setOreTargetAboveMethod;
    private static Method setCelebratingDiamondMethod;
    private static Method setActiveCustomGoalDebugNameMethod;
    private static Method setCurrentDeepSleepAnimIdMethod;
    private static Method setDozingPhaseMethod;
    private static Method setHamsterFlagMethod;
    private static Method triggerAnimMethod;
    private static Method stopTriggeredAnimMethod;
    private static Method isLureItemMethod;
    private static Method getAnimatableInstanceCacheMethod;
    private static Method hamsterGetItemsMethod;
    private static Method getManagerForIdMethod;
    private static Method updatedAtMethod;
    private static Field lastRenderTimeField;
    private static Field cleaningFlagField;
    private static Field beggingFlagField;
    private static Field throwIsQueuingField;
    private static Field throwQueueTicksField;
    private static Field throwQueueRequiredTicksField;
    private static Field clientPettingTicksField;
    private static Field petHamsterKeyBindingField;
    private static Field playerPettingHamsterField;
    private static Field playerPettingTimerField;
    private static Method keyBindingIsDownMethod;
    private static Method keyBindingConsumeClickMethod;
    private static Method keyBindingGetKeyMethod;
    private static boolean lastPetHamsterPhysicalDown;
    private static Object shoulderRendererInstance;
    private static UUID trackedPlayerUuid;
    private static class_2487 previousRightShoulder;
    private static class_2487 previousLeftShoulder;
    private static class_2487 previousHead;
    private static class_2487 activeTransitHamster;
    private static Object activeTransitLocation;
    private static int activeTransitTicks;
    private static class_1268 lastLureHand;
    private static Object pendingYeetLocation;
    private static int pendingYeetTicks;
    private static boolean lastThrowQueued;
    private static int lastThrowQueueTicks;
    private static Object activeThrowLocation;
    private static boolean throwChargeActive;
    private static boolean lastPettingActive;
    private static String lastPettingClipName;
    private static boolean lastAttackKeyDown;
    private static boolean lastUseKeyDown;
    private static boolean lastPetHamsterKeyDown;
    private static int pettingCancelSuppressTicks;
    private static final int PETTING_CANCEL_SUPPRESS_TICKS = 8;
    private static final float PETTING_NATURAL_END_PROGRESS_THRESHOLD = 0.9f;
    private static int throwOutcomeWaitTicks;
    private static final int THROW_OUTCOME_TIMEOUT_TICKS = 8;
    private static int postReleaseCleanupTicks;
    private static final int POST_RELEASE_CLEANUP_DELAY_TICKS = 6;
    private static int suppressTimelineStateTicks;
    private static final int SUPPRESS_TIMELINE_AFTER_RELEASE_TICKS = 12;
    private static int preserveTriggeredAnimTicks;
    private static final int PRESERVE_TRIGGERED_ANIM_TRANSITION_TICKS = 14;
    private static class_2487 offhandProxyHamsterState;
    private static int offhandProxyHamsterStateTicks;
    private static final int OFFHAND_PROXY_STATE_TTL = 40;
    private static int offhandProxyHamsterEntityId;
    private static boolean lockOffhandProxyDuringPetting;
    private static String lastTimelineTriggerToken;
    private static AnimationClip lastTimelineClipRef;
    private static float lastTimelineClipTime;
    private static String lastTimelineTriggeredAnimName;
    private static final Map<Object, class_1297> gripRenderDummies;

    private AdorableHamsterPetsCompat() {
    }

    public static void tickClient(class_310 client) {
        class_746 player;
        block39: {
            block38: {
                if (!AdorableHamsterPetsCompat.isAvailable() || !AdorableHamsterPetsCompat.ensureReflectionReady() || client == null) {
                    AdorableHamsterPetsCompat.resetTracking();
                    return;
                }
                class_746 class_7462 = client.field_1724;
                if (!(class_7462 instanceof class_746)) break block38;
                player = class_7462;
                if (client.field_1687 != null) break block39;
            }
            AdorableHamsterPetsCompat.resetTracking();
            return;
        }
        UUID playerUuid = player.method_5667();
        class_2487 currentRight = AdorableHamsterPetsCompat.getShoulderHamster((class_742)player, rightShoulderSlot);
        class_2487 currentLeft = AdorableHamsterPetsCompat.getShoulderHamster((class_742)player, leftShoulderSlot);
        class_2487 currentHead = AdorableHamsterPetsCompat.getShoulderHamster((class_742)player, headSlot);
        if (trackedPlayerUuid == null || !trackedPlayerUuid.equals(playerUuid)) {
            trackedPlayerUuid = playerUuid;
            previousRightShoulder = AdorableHamsterPetsCompat.copyTag(currentRight);
            previousLeftShoulder = AdorableHamsterPetsCompat.copyTag(currentLeft);
            previousHead = AdorableHamsterPetsCompat.copyTag(currentHead);
            activeTransitHamster = new class_2487();
            activeTransitTicks = 0;
            lastThrowQueued = false;
            lastThrowQueueTicks = 0;
            activeThrowLocation = null;
            throwChargeActive = false;
            throwOutcomeWaitTicks = 0;
            postReleaseCleanupTicks = 0;
            return;
        }
        AdorableHamsterPetsCompat.updateThrowQueueAnimations(client, player, currentRight, currentLeft, currentHead);
        AdorableHamsterPetsCompat.updatePettingAnimation(client, player);
        Object mountedLocation = null;
        Object dismountedLocation = null;
        class_2487 mountedHamster = AdorableHamsterPetsCompat.newlyOccupied(currentRight, previousRightShoulder);
        if (mountedHamster != null) {
            mountedLocation = rightShoulderSlot;
        } else if (AdorableHamsterPetsCompat.becameEmpty(currentRight, previousRightShoulder)) {
            dismountedLocation = rightShoulderSlot;
        }
        if (mountedHamster == null) {
            mountedHamster = AdorableHamsterPetsCompat.newlyOccupied(currentLeft, previousLeftShoulder);
            if (mountedHamster != null) {
                mountedLocation = leftShoulderSlot;
            } else if (dismountedLocation == null && AdorableHamsterPetsCompat.becameEmpty(currentLeft, previousLeftShoulder)) {
                dismountedLocation = leftShoulderSlot;
            }
        }
        if (mountedHamster == null) {
            mountedHamster = AdorableHamsterPetsCompat.newlyOccupied(currentHead, previousHead);
            if (mountedHamster != null) {
                mountedLocation = headSlot;
            } else if (dismountedLocation == null && AdorableHamsterPetsCompat.becameEmpty(currentHead, previousHead)) {
                dismountedLocation = headSlot;
            }
        }
        previousRightShoulder = AdorableHamsterPetsCompat.copyTag(currentRight);
        previousLeftShoulder = AdorableHamsterPetsCompat.copyTag(currentLeft);
        previousHead = AdorableHamsterPetsCompat.copyTag(currentHead);
        if (mountedHamster != null) {
            activeTransitHamster = mountedHamster;
            activeTransitLocation = mountedLocation;
            activeTransitTicks = 30;
            AdorableHamsterPetsCompat.playShoulderMountAnimation(client, player, mountedLocation);
            pendingYeetLocation = null;
            pendingYeetTicks = 0;
            throwChargeActive = false;
            throwOutcomeWaitTicks = 0;
        } else if (dismountedLocation != null) {
            if (AdorableHamsterPetsCompat.isPettingAnimationActive()) {
                pendingYeetLocation = null;
                pendingYeetTicks = 0;
                throwChargeActive = false;
                throwOutcomeWaitTicks = 0;
            } else if (AdorableHamsterPetsCompat.isHamsterProjectileNearby((class_742)player)) {
                AdorableHamsterPetsCompat.playShoulderYeetAnimation(client, player, dismountedLocation);
                pendingYeetLocation = null;
                pendingYeetTicks = 0;
                throwChargeActive = false;
                throwOutcomeWaitTicks = 0;
            } else {
                AdorableHamsterPetsCompat.playShoulderThrowPhaseAnimation(client, player, dismountedLocation, "dismount");
                pendingYeetLocation = null;
                pendingYeetTicks = 0;
            }
        } else if (activeTransitTicks > 0) {
            --activeTransitTicks;
        } else if (AdorableHamsterPetsCompat.isItemEffectActive(class_1306.field_6183) || AdorableHamsterPetsCompat.isItemEffectActive(class_1306.field_6182) || !AdorableHamsterPetsCompat.isGripEffectActive(class_1306.field_6183) && !AdorableHamsterPetsCompat.isGripEffectActive(class_1306.field_6182)) {
            activeTransitHamster = new class_2487();
            activeTransitLocation = null;
            activeTransitTicks = 0;
        }
        if (pendingYeetTicks > 0) {
            if (AdorableHamsterPetsCompat.isHamsterProjectileNearby((class_742)player)) {
                AdorableHamsterPetsCompat.playShoulderYeetAnimation(client, player, pendingYeetLocation);
                pendingYeetLocation = null;
                pendingYeetTicks = 0;
                throwChargeActive = false;
                throwOutcomeWaitTicks = 0;
            } else if (--pendingYeetTicks <= 0) {
                pendingYeetLocation = null;
            }
        }
        if (throwChargeActive && throwOutcomeWaitTicks > 0 && --throwOutcomeWaitTicks <= 0) {
            if (activeThrowLocation != null) {
                AdorableHamsterPetsCompat.playShoulderThrowAbortAnimation(client, player, activeThrowLocation);
            }
            throwChargeActive = false;
            activeThrowLocation = null;
        }
        if (postReleaseCleanupTicks > 0 && --postReleaseCleanupTicks == 0) {
            AdorableHamsterPetsCompat.stopLastTimelineTriggeredAnimationOnAllDummies();
            AdorableHamsterPetsCompat.clearTimelineTriggerState();
            AdorableHamsterPetsCompat.resetAllGripDummies();
        }
        if (suppressTimelineStateTicks > 0) {
            --suppressTimelineStateTicks;
        }
        if (preserveTriggeredAnimTicks > 0) {
            --preserveTriggeredAnimTicks;
        }
        AdorableHamsterPetsCompat.refreshOffhandProxyHamsterState((class_742)player);
        boolean offOnlyActiveNow = AdorableHamsterPetsCompat.hasAnyOffPrefixedEffectActive();
        if (!offOnlyActiveNow && offhandProxyHamsterStateTicks > 0 && --offhandProxyHamsterStateTicks <= 0) {
            offhandProxyHamsterState = new class_2487();
            offhandProxyHamsterEntityId = -1;
        }
        if (!AdorableHamsterPetsCompat.isGripEffectActive(class_1306.field_6183) && !AdorableHamsterPetsCompat.isGripEffectActive(class_1306.field_6182)) {
            AdorableHamsterPetsCompat.clearTimelineTriggerState();
        }
    }

    public static boolean isPettingFeedbackActive(class_310 client) {
        if (!AdorableHamsterPetsCompat.isAvailable() || !AdorableHamsterPetsCompat.ensureReflectionReady() || client == null) {
            return false;
        }
        return AdorableHamsterPetsCompat.isClientPettingActive();
    }

    public static boolean tryRenderHamsterInGrip(class_742 player, class_1306 arm, class_4587 poseStack, class_4597 buffer, int combinedLight, float partialTicks) {
        if (!AdorableHamsterPetsCompat.isAvailable() || !AdorableHamsterPetsCompat.ensureReflectionReady() || player == null || arm == null || poseStack == null || buffer == null) {
            return false;
        }
        boolean offOnlyActive = AdorableHamsterPetsCompat.isOffOnlyGripActiveNow();
        if (!AdorableHamsterPetsCompat.isGripEffectActive(arm)) {
            return false;
        }
        if (offOnlyActive && !AdorableHamsterPetsCompat.isPlayerOffhandArm(player, arm)) {
            return false;
        }
        if (!offOnlyActive && AdorableHamsterPetsCompat.isItemEffectActive(arm)) {
            return false;
        }
        AdorableHamsterPetsCompat.refreshTimelineTriggerLifecycle();
        class_1297 entity = null;
        if (offOnlyActive) {
            Object offhandSlot;
            if (offhandProxyHamsterState != null && !offhandProxyHamsterState.method_33133() && (offhandSlot = AdorableHamsterPetsCompat.resolvePlayerOffhandSlot()) != null) {
                entity = AdorableHamsterPetsCompat.getTransitDummy(player, offhandProxyHamsterState, offhandSlot);
            }
            if (entity == null) {
                return false;
            }
        }
        if (!offOnlyActive && entity == null) {
            entity = AdorableHamsterPetsCompat.resolveRenderEntity(player);
        }
        if (entity == null) {
            return false;
        }
        RequestedHamsterState requestedState = AdorableHamsterPetsCompat.resolveRequestedHamsterState(arm);
        if (suppressTimelineStateTicks > 0) {
            requestedState = null;
        }
        AdorableHamsterPetsCompat.updateTransitState(entity, player, requestedState == null ? null : requestedState.token);
        AdorableHamsterPetsCompat.applyRequestedHamsterState(entity, requestedState);
        AdorableHamsterPetsCompat.applyShaderMultipassAnimationFix(entity, partialTicks);
        poseStack.method_22903();
        poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0f));
        poseStack.method_22905(0.55f, 0.55f, 0.55f);
        class_310.method_1551().method_1561().method_3954(entity, 0.0, 0.0, 0.0, 0.0f, partialTicks, poseStack, buffer, combinedLight);
        poseStack.method_22909();
        return true;
    }

    private static void playShoulderMountAnimation(class_310 client, class_746 player, Object mountedLocation) {
        class_1268 hand = AdorableHamsterPetsCompat.resolveLureHand(player);
        lastLureHand = hand == null ? class_1268.field_5808 : hand;
        String animationName = AdorableHamsterPetsCompat.resolveShoulderMountAnimationName(player, hand, mountedLocation);
        AnimationClip clip = PunchyAnimationManager.resolveNamedClip(animationName, client, hand);
        if (clip == null && !SHOULDER_MOUNT_ANIMATION.equals(animationName)) {
            clip = PunchyAnimationManager.resolveNamedClip(SHOULDER_MOUNT_ANIMATION, client, hand);
        }
        if (clip == null) {
            return;
        }
        PunchyAnimationManager.setSourceHand(client, hand);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, hand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, true, clip.isHoldOnLastFrame());
    }

    private static void playShoulderYeetAnimation(class_310 client, class_746 player, Object dismountedLocation) {
        class_1268 preferredHand = lastLureHand == null ? class_1268.field_5808 : lastLureHand;
        String animationName = AdorableHamsterPetsCompat.resolveShoulderYeetAnimationName(player, preferredHand, dismountedLocation, "release");
        if (animationName == null || animationName.isBlank()) {
            return;
        }
        preserveTriggeredAnimTicks = 14;
        postReleaseCleanupTicks = 6;
        suppressTimelineStateTicks = 12;
        AnimationClip clip = PunchyAnimationManager.resolveNamedClip(animationName, client, preferredHand);
        class_1268 sourceHand = preferredHand;
        if (clip == null && preferredHand != class_1268.field_5810 && (clip = PunchyAnimationManager.resolveNamedClip(animationName, client, class_1268.field_5810)) != null) {
            sourceHand = class_1268.field_5810;
        }
        if (clip == null && !SHOULDER_YEET_ANIMATION.equals(animationName) && (clip = PunchyAnimationManager.resolveNamedClip(SHOULDER_YEET_ANIMATION, client, preferredHand)) == null && preferredHand != class_1268.field_5810 && (clip = PunchyAnimationManager.resolveNamedClip(SHOULDER_YEET_ANIMATION, client, class_1268.field_5810)) != null) {
            sourceHand = class_1268.field_5810;
        }
        if (clip == null) {
            return;
        }
        PunchyAnimationManager.setSourceHand(client, sourceHand);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, sourceHand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, true, clip.isHoldOnLastFrame());
    }

    private static void playShoulderThrowChargeAnimation(class_310 client, class_746 player, Object throwLocation) {
        AdorableHamsterPetsCompat.playShoulderThrowPhaseAnimation(client, player, throwLocation, "charge");
    }

    private static void playShoulderThrowAbortAnimation(class_310 client, class_746 player, Object throwLocation) {
        AdorableHamsterPetsCompat.playShoulderThrowPhaseAnimation(client, player, throwLocation, "abbort");
        preserveTriggeredAnimTicks = 0;
        postReleaseCleanupTicks = 6;
        suppressTimelineStateTicks = 12;
        AdorableHamsterPetsCompat.stopLastTimelineTriggeredAnimationOnAllDummies();
        AdorableHamsterPetsCompat.clearTimelineTriggerState();
    }

    private static void playShoulderThrowPhaseAnimation(class_310 client, class_746 player, Object throwLocation, String phase) {
        if (client == null || player == null || phase == null || phase.isBlank()) {
            return;
        }
        class_1268 preferredHand = lastLureHand == null ? class_1268.field_5808 : lastLureHand;
        String animationName = AdorableHamsterPetsCompat.resolveShoulderYeetAnimationName(player, preferredHand, throwLocation, phase);
        if (animationName == null || animationName.isBlank()) {
            return;
        }
        AnimationClip clip = PunchyAnimationManager.resolveNamedClip(animationName, client, preferredHand);
        class_1268 sourceHand = preferredHand;
        if (clip == null && preferredHand != class_1268.field_5810 && (clip = PunchyAnimationManager.resolveNamedClip(animationName, client, class_1268.field_5810)) != null) {
            sourceHand = class_1268.field_5810;
        }
        if (clip == null) {
            return;
        }
        PunchyAnimationManager.setSourceHand(client, sourceHand);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, sourceHand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, true, clip.isHoldOnLastFrame());
    }

    private static String resolveShoulderMountAnimationName(class_746 player, class_1268 hand, Object mountedLocation) {
        String specific;
        class_1799 stack;
        if (player == null || hand == null) {
            return SHOULDER_MOUNT_ANIMATION;
        }
        class_1799 class_17992 = stack = hand == class_1268.field_5810 ? player.method_6079() : player.method_6047();
        if (stack == null || stack.method_7960()) {
            return SHOULDER_MOUNT_ANIMATION;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return SHOULDER_MOUNT_ANIMATION;
        }
        String preferredSlotType = AdorableHamsterPetsCompat.resolveShoulderSlotType(mountedLocation);
        if (!preferredSlotType.isEmpty() && (specific = AdorableHamsterPetsCompat.resolveAnimationNameByType(entries, preferredSlotType)) != null && !specific.isBlank()) {
            return specific;
        }
        String legacyUseItem = AdorableHamsterPetsCompat.resolveAnimationNameByType(entries, TYPE_USE_ITEM);
        if (legacyUseItem != null && !legacyUseItem.isBlank()) {
            return legacyUseItem;
        }
        return SHOULDER_MOUNT_ANIMATION;
    }

    private static String resolveShoulderYeetAnimationName(class_746 player, class_1268 preferredHand, Object dismountedLocation, String phase) {
        class_1799 otherStack;
        if (player == null) {
            return SHOULDER_YEET_ANIMATION;
        }
        boolean dismountPhase = "dismount".equalsIgnoreCase(phase);
        String slotThrowType = AdorableHamsterPetsCompat.resolveThrowSlotType(dismountedLocation);
        class_1799 preferredStack = preferredHand == class_1268.field_5810 ? player.method_6079() : player.method_6047();
        String resolved = null;
        if (dismountPhase && (resolved = AdorableHamsterPetsCompat.resolveAnimationNameForStackType(preferredStack, TYPE_HAMSTER_DISMOUNT)) != null && !resolved.isBlank()) {
            return resolved;
        }
        resolved = AdorableHamsterPetsCompat.resolveThrowAnimationName(preferredStack, slotThrowType, phase);
        if (resolved == null || resolved.isBlank()) {
            resolved = AdorableHamsterPetsCompat.resolveAnimationNameForStackType(preferredStack, TYPE_HAMSTER_YEET);
        }
        if (resolved != null && !resolved.isBlank()) {
            return resolved;
        }
        class_1268 otherHand = preferredHand == class_1268.field_5810 ? class_1268.field_5808 : class_1268.field_5810;
        class_1799 class_17992 = otherStack = otherHand == class_1268.field_5810 ? player.method_6079() : player.method_6047();
        if (dismountPhase && (resolved = AdorableHamsterPetsCompat.resolveAnimationNameForStackType(otherStack, TYPE_HAMSTER_DISMOUNT)) != null && !resolved.isBlank()) {
            return resolved;
        }
        resolved = AdorableHamsterPetsCompat.resolveThrowAnimationName(otherStack, slotThrowType, phase);
        if (resolved == null || resolved.isBlank()) {
            resolved = AdorableHamsterPetsCompat.resolveAnimationNameForStackType(otherStack, TYPE_HAMSTER_YEET);
        }
        if (resolved != null && !resolved.isBlank()) {
            return resolved;
        }
        if (dismountPhase) {
            return TYPE_HAMSTER_DISMOUNT;
        }
        return SHOULDER_YEET_ANIMATION;
    }

    private static String resolveThrowSlotType(Object location) {
        if (location == null) {
            return "";
        }
        if (location == rightShoulderSlot) {
            return TYPE_HAMSTER_THROW_RIGHT_SOULDER;
        }
        if (location == leftShoulderSlot) {
            return TYPE_HAMSTER_THROW_LEFT_SOULDER;
        }
        if (location == headSlot) {
            return TYPE_HAMSTER_THROW_HEAD;
        }
        return "";
    }

    private static String resolveThrowAnimationName(class_1799 stack, String throwType, String phase) {
        if (stack == null || throwType == null || throwType.isBlank() || phase == null || phase.isBlank()) {
            return null;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            String clipName;
            if (entry == null || !AdorableHamsterPetsCompat.matchesType(entry.type, throwType)) continue;
            switch (phase.toLowerCase(Locale.ROOT)) {
                case "charge": {
                    String string = AdorableHamsterPetsCompat.trimToNull(entry.charge);
                    break;
                }
                case "release": {
                    String string = AdorableHamsterPetsCompat.trimToNull(entry.release);
                    break;
                }
                case "abort": 
                case "abbort": {
                    String string = AdorableHamsterPetsCompat.trimToNull(entry.abbort != null ? entry.abbort : entry.abort);
                    break;
                }
                default: {
                    String string = clipName = null;
                }
            }
            if (clipName != null) {
                return clipName;
            }
            return AdorableHamsterPetsCompat.resolveAnimationName(entry);
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean matchesType(String actualType, String targetType) {
        if (actualType == null || targetType == null) {
            return false;
        }
        String normalizedType = actualType.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        String normalizedTarget = targetType.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        return normalizedType.equals(normalizedTarget);
    }

    private static String resolveShoulderSlotType(Object mountedLocation) {
        if (mountedLocation == null) {
            return "";
        }
        if (mountedLocation == rightShoulderSlot) {
            return TYPE_HAMSTER_RIGHT_SHOULDER;
        }
        if (mountedLocation == leftShoulderSlot) {
            return TYPE_HAMSTER_LEFT_SHOULDER;
        }
        if (mountedLocation == headSlot) {
            return TYPE_HAMSTER_HEAD;
        }
        return "";
    }

    private static String resolveAnimationNameByType(List<PunchyTuningConfig.CustomAnimationEntry> entries, String targetType) {
        if (entries == null || entries.isEmpty() || targetType == null || targetType.isBlank()) {
            return null;
        }
        for (PunchyTuningConfig.CustomAnimationEntry entry : entries) {
            String resolved;
            if (entry == null || entry.type == null || !AdorableHamsterPetsCompat.matchesType(entry.type, targetType) || (resolved = AdorableHamsterPetsCompat.resolveAnimationName(entry)) == null || resolved.isBlank()) continue;
            return resolved;
        }
        return null;
    }

    private static String resolveAnimationNameForStackType(class_1799 stack, String targetType) {
        if (stack == null || targetType == null || targetType.isBlank()) {
            return null;
        }
        List<PunchyTuningConfig.CustomAnimationEntry> entries = ToolTuning.getCustomAnimation(stack);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        return AdorableHamsterPetsCompat.resolveAnimationNameByType(entries, targetType);
    }

    private static String resolveAnimationName(PunchyTuningConfig.CustomAnimationEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.animation != null && !entry.animation.isBlank()) {
            return entry.animation.trim();
        }
        if (entry.name != null && !entry.name.isBlank()) {
            return entry.name.trim();
        }
        if (entry.var_1 != null && !entry.var_1.isBlank()) {
            return entry.var_1.trim();
        }
        if (entry.var_2 != null && !entry.var_2.isBlank()) {
            return entry.var_2.trim();
        }
        if (entry.var_3 != null && !entry.var_3.isBlank()) {
            return entry.var_3.trim();
        }
        return null;
    }

    private static class_1268 resolveLureHand(class_746 player) {
        if (player == null) {
            return class_1268.field_5808;
        }
        try {
            if (isLureItemMethod != null) {
                Boolean b;
                Boolean b2;
                Object mainMatch = isLureItemMethod.invoke(null, player.method_6047());
                if (mainMatch instanceof Boolean && (b2 = (Boolean)mainMatch).booleanValue()) {
                    return class_1268.field_5808;
                }
                Object offMatch = isLureItemMethod.invoke(null, player.method_6079());
                if (offMatch instanceof Boolean && (b = (Boolean)offMatch).booleanValue()) {
                    return class_1268.field_5810;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return class_1268.field_5808;
    }

    private static class_2487 newlyOccupied(class_2487 current, class_2487 previous) {
        if (current == null || current.method_33133()) {
            return null;
        }
        if (previous != null && !previous.method_33133()) {
            return null;
        }
        return AdorableHamsterPetsCompat.copyTag(current);
    }

    private static boolean becameEmpty(class_2487 current, class_2487 previous) {
        boolean currentEmpty = current == null || current.method_33133();
        boolean previousOccupied = previous != null && !previous.method_33133();
        return previousOccupied && currentEmpty;
    }

    private static boolean isHamsterProjectileNearby(class_742 player) {
        if (player == null || player.method_37908() == null) {
            return false;
        }
        class_238 searchBox = player.method_5829().method_1014(5.0);
        List matches = player.method_37908().method_8333((class_1297)player, searchBox, entity -> {
            if (entity == null) {
                return false;
            }
            class_2960 typeId = class_7923.field_41177.method_10221((Object)entity.method_5864());
            if (!HAMSTER_PROJECTILE_ID.equals((Object)typeId)) {
                return false;
            }
            if (entity instanceof class_1676) {
                class_1676 projectile = (class_1676)entity;
                return projectile.method_24921() == player;
            }
            return true;
        });
        return !matches.isEmpty();
    }

    private static void updateThrowQueueAnimations(class_310 client, class_746 player, class_2487 currentRight, class_2487 currentLeft, class_2487 currentHead) {
        boolean isQueued = AdorableHamsterPetsCompat.readStaticBoolean(throwIsQueuingField, false);
        int queueTicks = AdorableHamsterPetsCompat.readStaticInt(throwQueueTicksField, 0);
        int requiredTicks = Math.max(1, AdorableHamsterPetsCompat.readStaticInt(throwQueueRequiredTicksField, 15));
        if (isQueued && !lastThrowQueued) {
            class_2487 queuedThrowHamster = AdorableHamsterPetsCompat.getNextQueuedThrowHamsterData(player);
            if (queuedThrowHamster == null || queuedThrowHamster.method_33133()) {
                activeThrowLocation = null;
                throwChargeActive = false;
                throwOutcomeWaitTicks = 0;
                lastThrowQueued = isQueued;
                lastThrowQueueTicks = queueTicks;
                return;
            }
            if (AdorableHamsterPetsCompat.isThrowCooldownActive(player, queuedThrowHamster)) {
                activeThrowLocation = null;
                throwChargeActive = false;
                throwOutcomeWaitTicks = 0;
                lastThrowQueued = isQueued;
                lastThrowQueueTicks = queueTicks;
                return;
            }
            activeTransitHamster = queuedThrowHamster;
            activeTransitLocation = AdorableHamsterPetsCompat.resolveThrowLocationFromNbt(queuedThrowHamster, currentRight, currentLeft, currentHead);
            activeTransitTicks = 30;
            Object object = activeThrowLocation = activeTransitLocation != null ? activeTransitLocation : AdorableHamsterPetsCompat.predictThrowLocation(player, currentRight, currentLeft, currentHead);
            if (activeTransitLocation == null) {
                activeTransitLocation = activeThrowLocation;
            }
            if (activeThrowLocation != null) {
                AdorableHamsterPetsCompat.playShoulderThrowChargeAnimation(client, player, activeThrowLocation);
                throwChargeActive = true;
                throwOutcomeWaitTicks = 0;
            }
        }
        if (!isQueued && lastThrowQueued) {
            if (activeThrowLocation != null) {
                if (lastThrowQueueTicks < requiredTicks) {
                    AdorableHamsterPetsCompat.playShoulderThrowAbortAnimation(client, player, activeThrowLocation);
                    throwChargeActive = false;
                    throwOutcomeWaitTicks = 0;
                } else if (throwChargeActive) {
                    throwOutcomeWaitTicks = 8;
                }
            }
            if (!throwChargeActive) {
                activeThrowLocation = null;
            }
        }
        lastThrowQueued = isQueued;
        lastThrowQueueTicks = queueTicks;
    }

    private static boolean isThrowCooldownActive(class_746 player, class_2487 queuedThrowHamster) {
        if (player == null || player.method_37908() == null || queuedThrowHamster == null) {
            return false;
        }
        if (!queuedThrowHamster.method_10545("throwCooldownEndTick")) {
            return false;
        }
        return queuedThrowHamster.method_10537("throwCooldownEndTick") > player.method_37908().method_8510();
    }

    private static void updatePettingAnimation(class_310 client, class_746 player) {
        if (client == null || player == null) {
            if (lastPettingActive) {
                AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
            }
            lastPettingActive = false;
            lastPettingClipName = "";
            lastAttackKeyDown = false;
            lastUseKeyDown = false;
            lastPetHamsterKeyDown = false;
            return;
        }
        boolean pettingActive = AdorableHamsterPetsCompat.isClientPettingActive();
        AnimationClip currentClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        boolean petClipPlaying = AdorableHamsterPetsCompat.isCurrentPettingClip(currentClip) && PunchyAnimationManager.POSE_HANDLER.isPlaying();
        boolean cancelClickJustPressed = AdorableHamsterPetsCompat.isPettingCancelClickJustPressed(client);
        boolean petKeyJustPressed = AdorableHamsterPetsCompat.isPetHamsterKeyJustPressed(client);
        if (pettingCancelSuppressTicks > 0) {
            --pettingCancelSuppressTicks;
            if (!(pettingActive || AdorableHamsterPetsCompat.isCurrentPettingClip(PunchyAnimationManager.POSE_HANDLER.getCurrentClip()) && PunchyAnimationManager.POSE_HANDLER.isPlaying())) {
                AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
            }
            lastPettingActive = pettingActive;
            return;
        }
        if (pettingActive && !lastPettingActive) {
            AdorableHamsterPetsCompat.playPetHamsterAnimation(client, player);
            lastPettingActive = true;
            return;
        }
        if (cancelClickJustPressed && (lastPettingActive || petClipPlaying)) {
            AdorableHamsterPetsCompat.forceStopPettingAnimation(client);
            AdorableHamsterPetsCompat.forceClearClientPettingFeedback();
            AdorableHamsterPetsCompat.playPettingCancelDismountAnimation(client, player);
            AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
            lastPettingActive = false;
            return;
        }
        if (petKeyJustPressed && (pettingActive || lastPettingActive || petClipPlaying)) {
            AdorableHamsterPetsCompat.forceStopPettingAnimation(client);
            AdorableHamsterPetsCompat.forceClearClientPettingFeedback();
            AdorableHamsterPetsCompat.playPettingCancelDismountAnimation(client, player);
            AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
            lastPettingActive = false;
            pettingCancelSuppressTicks = 8;
            return;
        }
        if (!pettingActive && lastPettingActive && petKeyJustPressed) {
            AdorableHamsterPetsCompat.forceStopPettingAnimation(client);
            AdorableHamsterPetsCompat.forceClearClientPettingFeedback();
            AdorableHamsterPetsCompat.playPettingCancelDismountAnimation(client, player);
            AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
        } else if (!pettingActive && lastPettingActive) {
            boolean playingPetClip;
            AnimationClip active = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
            boolean bl = playingPetClip = AdorableHamsterPetsCompat.isCurrentPettingClip(active) && PunchyAnimationManager.POSE_HANDLER.isPlaying();
            if (playingPetClip && AdorableHamsterPetsCompat.shouldTreatPettingDropAsExplicitCancel(active)) {
                AdorableHamsterPetsCompat.forceStopPettingAnimation(client);
                AdorableHamsterPetsCompat.forceClearClientPettingFeedback();
                AdorableHamsterPetsCompat.playPettingCancelDismountAnimation(client, player);
                AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
            } else if (!playingPetClip) {
                AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
            }
        }
        if (!(pettingActive || lastPettingActive || AdorableHamsterPetsCompat.isCurrentPettingClip(PunchyAnimationManager.POSE_HANDLER.getCurrentClip()) && PunchyAnimationManager.POSE_HANDLER.isPlaying())) {
            AdorableHamsterPetsCompat.clearOffhandHamsterPetState();
        }
        lastPettingActive = pettingActive;
    }

    private static class_304 resolvePetHamsterKeyMapping() {
        try {
            Object value;
            if (petHamsterKeyBindingField != null && (value = petHamsterKeyBindingField.get(null)) instanceof class_304) {
                class_304 mapping = (class_304)value;
                return mapping;
            }
        }
        catch (Throwable value) {
            // empty catch block
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1690 == null) {
            return null;
        }
        try {
            class_304[] mappings;
            Field mappingsField = AdorableHamsterPetsCompat.findField(client.field_1690.getClass(), "keyMappings");
            if (mappingsField == null) {
                return null;
            }
            Object raw = mappingsField.get(client.field_1690);
            if (!(raw instanceof class_304[]) || (mappings = (class_304[])raw).length == 0) {
                return null;
            }
            class_304 firstCandidate = null;
            for (class_304 mapping : mappings) {
                String hint;
                if (mapping == null || (hint = AdorableHamsterPetsCompat.buildKeyMappingHint(mapping)).isBlank()) continue;
                String normalized = hint.toLowerCase(Locale.ROOT);
                boolean looksAhp = normalized.contains(MOD_ID) || normalized.contains("hamster");
                boolean looksPet = normalized.contains("pet");
                if (looksAhp && looksPet) {
                    return mapping;
                }
                if (!looksAhp || firstCandidate != null) continue;
                firstCandidate = mapping;
            }
            return firstCandidate;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static String buildKeyMappingHint(class_304 mapping) {
        if (mapping == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            Object name;
            Method getName = AdorableHamsterPetsCompat.findMethod(mapping.getClass(), "getName", 0);
            if (getName != null && (name = getName.invoke((Object)mapping, new Object[0])) != null) {
                sb.append(name).append(' ');
            }
        }
        catch (Throwable getName) {
            // empty catch block
        }
        try {
            Object category;
            Method getCategory = AdorableHamsterPetsCompat.findMethod(mapping.getClass(), "getCategory", 0);
            if (getCategory != null && (category = getCategory.invoke((Object)mapping, new Object[0])) != null) {
                sb.append(category).append(' ');
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        sb.append(mapping.toString());
        return sb.toString();
    }

    private static boolean shouldTreatPettingDropAsExplicitCancel(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        float len = clip.getLength();
        if (!(len > 0.0f)) {
            return true;
        }
        float t = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
        float progress = t / len;
        return progress < 0.9f;
    }

    private static class_3675.class_306 resolveBoundInputKey(class_304 keyMapping) {
        if (keyMapping == null) {
            return null;
        }
        try {
            class_3675.class_306 bound;
            class_304 keyBindingObj = keyMapping;
            if ((keyBindingGetKeyMethod == null || keyBindingGetKeyMethod.getDeclaringClass() != keyBindingObj.getClass()) && (keyBindingGetKeyMethod = AdorableHamsterPetsCompat.findMethod(keyBindingObj.getClass(), "getKey", 0)) == null) {
                keyBindingGetKeyMethod = AdorableHamsterPetsCompat.findMethod(keyBindingObj.getClass(), "key", 0);
            }
            if (keyBindingGetKeyMethod == null) {
                return null;
            }
            Object value = keyBindingGetKeyMethod.invoke((Object)keyBindingObj, new Object[0]);
            return value instanceof class_3675.class_306 ? (bound = (class_3675.class_306)value) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isPetHamsterKeyJustPressed(class_310 client) {
        boolean down = false;
        boolean clickConsumed = false;
        boolean physicalDown = false;
        try {
            class_304 keyBinding = AdorableHamsterPetsCompat.resolvePetHamsterKeyMapping();
            if (keyBinding != null) {
                Boolean b;
                class_304 keyBindingObj = keyBinding;
                if ((keyBindingConsumeClickMethod == null || keyBindingConsumeClickMethod.getDeclaringClass() != keyBindingObj.getClass()) && (keyBindingConsumeClickMethod = AdorableHamsterPetsCompat.findMethod(keyBindingObj.getClass(), "consumeClick", 0)) == null) {
                    keyBindingConsumeClickMethod = AdorableHamsterPetsCompat.findMethod(keyBindingObj.getClass(), "wasPressed", 0);
                }
                if (keyBindingConsumeClickMethod != null) {
                    Object consumed = keyBindingConsumeClickMethod.invoke((Object)keyBindingObj, new Object[0]);
                    boolean bl = clickConsumed = consumed instanceof Boolean && (b = (Boolean)consumed) != false;
                }
                if ((keyBindingIsDownMethod == null || keyBindingIsDownMethod.getDeclaringClass() != keyBindingObj.getClass()) && (keyBindingIsDownMethod = AdorableHamsterPetsCompat.findMethod(keyBindingObj.getClass(), "isDown", 0)) == null) {
                    keyBindingIsDownMethod = AdorableHamsterPetsCompat.findMethod(keyBindingObj.getClass(), "isPressed", 0);
                }
                if (keyBindingIsDownMethod != null) {
                    Object value = keyBindingIsDownMethod.invoke((Object)keyBindingObj, new Object[0]);
                    down = value instanceof Boolean && (b = (Boolean)value) != false;
                }
                class_3675.class_306 boundKey = AdorableHamsterPetsCompat.resolveBoundInputKey(keyBinding);
                if (client != null && client.method_22683() != null && boundKey != null) {
                    int code = boundKey.method_1444();
                    switch (boundKey.method_1442()) {
                        case field_1668: {
                            if (code == class_3675.field_16237.method_1444()) break;
                            physicalDown = class_3675.method_15987((long)client.method_22683().method_4490(), (int)code);
                            break;
                        }
                        case field_1671: {
                            if (code == class_3675.field_16237.method_1444()) break;
                            physicalDown = GLFW.glfwGetKeyScancode((int)code) != -1 && class_3675.method_15987((long)client.method_22683().method_4490(), (int)code);
                            break;
                        }
                        case field_1672: {
                            if (code < 0) break;
                            physicalDown = GLFW.glfwGetMouseButton((long)client.method_22683().method_4490(), (int)code) == 1;
                        }
                    }
                }
            }
        }
        catch (Throwable ignored) {
            down = false;
            clickConsumed = false;
            physicalDown = false;
        }
        boolean physicalJustPressed = physicalDown && !lastPetHamsterPhysicalDown;
        boolean logicalJustPressed = down && !lastPetHamsterKeyDown;
        boolean justPressed = clickConsumed || physicalJustPressed || logicalJustPressed;
        lastPetHamsterPhysicalDown = physicalDown;
        lastPetHamsterKeyDown = down;
        return justPressed;
    }

    private static void forceClearClientPettingFeedback() {
        if (clientPettingTicksField == null) {
            return;
        }
        try {
            clientPettingTicksField.setInt(null, 0);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void forceStopPettingAnimation(class_310 client) {
        if (client == null) {
            return;
        }
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (!AdorableHamsterPetsCompat.isCurrentPettingClip(current)) {
            return;
        }
        PunchyAnimationManager.POSE_HANDLER.stop();
        PunchyAnimationManager.clearSourceHand();
        lastPettingClipName = "";
    }

    private static boolean isCurrentPettingClip(AnimationClip clip) {
        if (clip == null) {
            return false;
        }
        String name = clip.getName();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        if (TYPE_HAMSTER_PET.equals(normalized) || normalized.contains(TYPE_HAMSTER_PET)) {
            return true;
        }
        return !lastPettingClipName.isBlank() && normalized.equals(lastPettingClipName.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean isClientPettingActive() {
        return AdorableHamsterPetsCompat.getClientPettingTicks() > 0;
    }

    private static boolean isPettingAnimationActive() {
        AnimationClip current = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        return AdorableHamsterPetsCompat.isClientPettingActive() || AdorableHamsterPetsCompat.isCurrentPettingClip(current) && PunchyAnimationManager.POSE_HANDLER.isPlaying();
    }

    private static int getClientPettingTicks() {
        if (clientPettingTicksField == null) {
            return 0;
        }
        try {
            return clientPettingTicksField.getInt(null);
        }
        catch (Throwable ignored) {
            return 0;
        }
    }

    private static void playPetHamsterAnimation(class_310 client, class_746 player) {
        AdorableHamsterPetsCompat.lockCurrentOffhandProxyTarget((class_742)player);
        class_1268 preferredHand = lastLureHand == null ? class_1268.field_5808 : lastLureHand;
        String animationName = AdorableHamsterPetsCompat.resolvePetAnimationName(player, preferredHand);
        String clipName = animationName == null || animationName.isBlank() ? TYPE_HAMSTER_PET : animationName;
        AnimationClip clip = PunchyAnimationManager.resolveNamedClip(clipName, client, preferredHand);
        class_1268 sourceHand = preferredHand;
        if (clip == null && preferredHand != class_1268.field_5810 && (clip = PunchyAnimationManager.resolveNamedClip(clipName, client, class_1268.field_5810)) != null) {
            sourceHand = class_1268.field_5810;
        }
        if (clip == null && !TYPE_HAMSTER_PET.equals(clipName) && (clip = PunchyAnimationManager.resolveNamedClip(TYPE_HAMSTER_PET, client, preferredHand)) == null && preferredHand != class_1268.field_5810 && (clip = PunchyAnimationManager.resolveNamedClip(TYPE_HAMSTER_PET, client, class_1268.field_5810)) != null) {
            sourceHand = class_1268.field_5810;
        }
        if (clip == null) {
            return;
        }
        lastPettingClipName = clip.getName() == null ? "" : clip.getName();
        PunchyAnimationManager.setSourceHand(client, sourceHand);
        class_1306 arm = PunchyAnimationManager.getUseArm(client, sourceHand);
        PunchyAnimationManager.POSE_HANDLER.setActiveArm(arm);
        float speedMultiplier = PunchyAnimationManager.clipSpeedMultiplier(clip);
        PunchyAnimationManager.POSE_HANDLER.play(clip, speedMultiplier, true, clip.isHoldOnLastFrame());
    }

    private static void playPettingCancelDismountAnimation(class_310 client, class_746 player) {
        Object location = activeTransitLocation != null ? activeTransitLocation : AdorableHamsterPetsCompat.resolvePlayerOffhandSlot();
        AdorableHamsterPetsCompat.playShoulderThrowPhaseAnimation(client, player, location, "dismount");
    }

    private static String resolvePetAnimationName(class_746 player, class_1268 preferredHand) {
        if (player == null) {
            return null;
        }
        class_1799 preferredStack = preferredHand == class_1268.field_5810 ? player.method_6079() : player.method_6047();
        String resolved = AdorableHamsterPetsCompat.resolveAnimationNameForStackType(preferredStack, TYPE_HAMSTER_PET);
        if (resolved != null && !resolved.isBlank()) {
            return resolved;
        }
        class_1268 otherHand = preferredHand == class_1268.field_5810 ? class_1268.field_5808 : class_1268.field_5810;
        class_1799 otherStack = otherHand == class_1268.field_5810 ? player.method_6079() : player.method_6047();
        resolved = AdorableHamsterPetsCompat.resolveAnimationNameForStackType(otherStack, TYPE_HAMSTER_PET);
        if (resolved != null && !resolved.isBlank()) {
            return resolved;
        }
        return null;
    }

    private static boolean isPettingCancelClickJustPressed(class_310 client) {
        if (client == null || client.field_1690 == null) {
            return false;
        }
        boolean attackDown = client.field_1690.field_1886.method_1434();
        boolean useDown = client.field_1690.field_1904.method_1434();
        boolean justPressed = attackDown && !lastAttackKeyDown || useDown && !lastUseKeyDown;
        lastAttackKeyDown = attackDown;
        lastUseKeyDown = useDown;
        return justPressed;
    }

    private static void clearOffhandHamsterPetState() {
        AdorableHamsterPetsCompat.stopTimelineAnimationOnPetOffhandProxy();
        AdorableHamsterPetsCompat.clearTimelineTriggerState();
        lockOffhandProxyDuringPetting = false;
        offhandProxyHamsterState = new class_2487();
        offhandProxyHamsterStateTicks = 0;
        offhandProxyHamsterEntityId = -1;
        lastPettingClipName = "";
        lastAttackKeyDown = false;
        lastUseKeyDown = false;
        lastPetHamsterKeyDown = false;
        lastPetHamsterPhysicalDown = false;
        pettingCancelSuppressTicks = 0;
    }

    private static void stopTimelineAnimationOnPetOffhandProxy() {
        class_746 class_7462;
        if (stopTriggeredAnimMethod == null || lastTimelineTriggeredAnimName == null || lastTimelineTriggeredAnimName.isBlank()) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client == null || !((class_7462 = client.field_1724) instanceof class_746)) {
            return;
        }
        class_746 player = class_7462;
        if (offhandProxyHamsterState == null || offhandProxyHamsterState.method_33133()) {
            return;
        }
        Object offhandSlot = AdorableHamsterPetsCompat.resolvePlayerOffhandSlot();
        if (offhandSlot == null) {
            return;
        }
        class_1297 proxy = AdorableHamsterPetsCompat.getTransitDummy((class_742)player, offhandProxyHamsterState, offhandSlot);
        if (proxy == null) {
            return;
        }
        try {
            stopTriggeredAnimMethod.invoke((Object)proxy, "mainController", lastTimelineTriggeredAnimName);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object predictThrowLocation(class_746 player, class_2487 currentRight, class_2487 currentLeft, class_2487 currentHead) {
        if (player != null && getMountOrderQueueMethod != null) {
            try {
                ArrayDeque queue;
                Object queueObj = getMountOrderQueueMethod.invoke((Object)player, new Object[0]);
                if (queueObj instanceof ArrayDeque && !(queue = (ArrayDeque)queueObj).isEmpty()) {
                    Object candidate = queue.peekLast();
                    if (candidate != null) {
                        return candidate;
                    }
                    candidate = queue.peekFirst();
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (currentRight != null && !currentRight.method_33133()) {
            return rightShoulderSlot;
        }
        if (currentLeft != null && !currentLeft.method_33133()) {
            return leftShoulderSlot;
        }
        if (currentHead != null && !currentHead.method_33133()) {
            return headSlot;
        }
        return null;
    }

    private static class_2487 getNextQueuedThrowHamsterData(class_746 player) {
        if (player == null || getNextHamsterToDismountDataMethod == null) {
            return new class_2487();
        }
        try {
            class_2487 class_24872;
            Object result = getNextHamsterToDismountDataMethod.invoke(null, player);
            if (result instanceof class_2487) {
                class_2487 tag = (class_2487)result;
                class_24872 = tag.method_10553();
            } else {
                class_24872 = new class_2487();
            }
            return class_24872;
        }
        catch (Throwable ignored) {
            return new class_2487();
        }
    }

    private static Object resolveThrowLocationFromNbt(class_2487 hamsterNbt, class_2487 currentRight, class_2487 currentLeft, class_2487 currentHead) {
        if (hamsterNbt == null || hamsterNbt.method_33133()) {
            return null;
        }
        if (!currentRight.method_33133() && currentRight.equals((Object)hamsterNbt)) {
            return rightShoulderSlot;
        }
        if (!currentLeft.method_33133() && currentLeft.equals((Object)hamsterNbt)) {
            return leftShoulderSlot;
        }
        if (!currentHead.method_33133() && currentHead.equals((Object)hamsterNbt)) {
            return headSlot;
        }
        return null;
    }

    private static boolean readStaticBoolean(Field field, boolean fallback) {
        if (field == null) {
            return fallback;
        }
        try {
            return field.getBoolean(null);
        }
        catch (Throwable ignored) {
            return fallback;
        }
    }

    private static int readStaticInt(Field field, int fallback) {
        if (field == null) {
            return fallback;
        }
        try {
            return field.getInt(null);
        }
        catch (Throwable ignored) {
            return fallback;
        }
    }

    private static class_2487 copyTag(class_2487 tag) {
        return tag == null ? new class_2487() : tag.method_10553();
    }

    private static void updateTransitState(class_1297 entity, class_742 player) {
        AdorableHamsterPetsCompat.updateTransitState(entity, player, null);
    }

    private static void updateTransitState(class_1297 entity, class_742 player, String requestedState) {
        String normalizedState = AdorableHamsterPetsCompat.normalizeRequestedStateToken(requestedState);
        double horizontalSpeed = 0.03;
        if (!normalizedState.isEmpty()) {
            horizontalSpeed = 0.0;
            if ("walk".equals(normalizedState) || "walking".equals(normalizedState)) {
                horizontalSpeed = 0.03;
            } else if ("run".equals(normalizedState) || "running".equals(normalizedState)) {
                horizontalSpeed = 0.06;
            } else if ("sprint".equals(normalizedState) || "sprinting".equals(normalizedState)) {
                horizontalSpeed = 0.1;
            }
        }
        entity.field_6012 = player.field_6012;
        entity.method_18799(new class_243(horizontalSpeed, 0.0, 0.0));
        if (entity instanceof class_1309) {
            class_1309 living = (class_1309)entity;
            living.method_36456(0.0f);
            living.method_5847(0.0f);
            living.method_5636(0.0f);
            living.field_5982 = 0.0f;
            living.field_6259 = 0.0f;
            living.field_6220 = 0.0f;
        }
    }

    private static class_1297 resolveRenderEntity(class_742 player) {
        class_1297 transitEntity;
        if (player != null && activeTransitHamster != null && !activeTransitHamster.method_33133() && (transitEntity = AdorableHamsterPetsCompat.getTransitDummy(player, activeTransitHamster, activeTransitLocation)) != null) {
            AdorableHamsterPetsCompat.applyTransitOverrides(transitEntity, player);
            return transitEntity;
        }
        class_1297 entity = AdorableHamsterPetsCompat.getShoulderDummy(player, activeTransitLocation);
        if (entity != null) {
            AdorableHamsterPetsCompat.applyTransitOverrides(entity, player);
            return entity;
        }
        entity = AdorableHamsterPetsCompat.getShoulderDummy(player, rightShoulderSlot);
        if (entity != null) {
            AdorableHamsterPetsCompat.applyTransitOverrides(entity, player);
            return entity;
        }
        entity = AdorableHamsterPetsCompat.getShoulderDummy(player, leftShoulderSlot);
        if (entity != null) {
            AdorableHamsterPetsCompat.applyTransitOverrides(entity, player);
            return entity;
        }
        entity = AdorableHamsterPetsCompat.getShoulderDummy(player, headSlot);
        if (entity != null) {
            AdorableHamsterPetsCompat.applyTransitOverrides(entity, player);
            return entity;
        }
        return null;
    }

    private static class_1297 getTransitDummy(class_742 player, class_2487 hamsterNbt, Object preferredSlot) {
        Object slot;
        if (player == null || hamsterNbt == null || hamsterNbt.method_33133()) {
            return null;
        }
        Object object = slot = preferredSlot != null ? preferredSlot : AdorableHamsterPetsCompat.resolveAnyOccupiedSlot(player);
        if (slot == null) {
            Object object2 = slot = rightShoulderSlot != null ? rightShoulderSlot : leftShoulderSlot;
        }
        if (slot == null) {
            return null;
        }
        try {
            Object clientData;
            class_1297 entity = AdorableHamsterPetsCompat.getOrCreateGripRenderDummy(player, slot);
            if (entity == null) {
                return null;
            }
            Optional hamsterState = (Optional)hamsterStateFromNbtMethod.invoke(null, hamsterNbt);
            if (hamsterState == null || hamsterState.isEmpty()) {
                return null;
            }
            Object object3 = clientData = getClientHamsterStateMethod != null ? getClientHamsterStateMethod.invoke((Object)player, new Object[0]) : null;
            if (updateDummyStateMethod != null && clientData != null) {
                updateDummyStateMethod.invoke(shoulderRendererInstance, entity, hamsterState.get(), clientData, slot, player);
            } else {
                applyHamsterStateMethod.invoke(shoulderRendererInstance, entity, hamsterState.get(), player);
            }
            return entity;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static Object resolveAnyOccupiedSlot(class_742 player) {
        if (player == null) {
            return null;
        }
        class_2487 right = AdorableHamsterPetsCompat.getShoulderHamster(player, rightShoulderSlot);
        if (!right.method_33133()) {
            return rightShoulderSlot;
        }
        class_2487 left = AdorableHamsterPetsCompat.getShoulderHamster(player, leftShoulderSlot);
        if (!left.method_33133()) {
            return leftShoulderSlot;
        }
        class_2487 head = AdorableHamsterPetsCompat.getShoulderHamster(player, headSlot);
        if (!head.method_33133()) {
            return headSlot;
        }
        return null;
    }

    private static boolean isGripEffectActive(class_1306 arm) {
        if (AdorableHamsterPetsCompat.isItemEffectActive(arm)) {
            return false;
        }
        if (AdorableHamsterPetsCompat.isEffectActive(CHANGE_TO_HAMSTER, arm) || AdorableHamsterPetsCompat.isNormalHamsterPrefixedEffectActive(arm)) {
            return true;
        }
        return AdorableHamsterPetsCompat.isOffPrefixedEffectActive(arm) || AdorableHamsterPetsCompat.hasAnyOffPrefixedEffectActive();
    }

    private static boolean isOffPrefixedEffectActive(class_1306 requestedArm) {
        class_1306 secondaryExpectedArm;
        class_1306 expectedArm;
        if (requestedArm == null) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        class_1306 clipArm = PunchyAnimationManager.POSE_HANDLER.getActiveArm();
        class_1306 class_13062 = expectedArm = clipArm != null ? AdorableHamsterPetsCompat.oppositeArm(clipArm) : AdorableHamsterPetsCompat.resolvePlayerOffhandArm();
        if (clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && expectedArm != null && requestedArm == expectedArm) {
            for (String effect : clip.getActiveEffects(PunchyAnimationManager.POSE_HANDLER.getCurrentTime())) {
                if (effect == null || !effect.startsWith(CHANGE_TO_HAMSTER_OFF_PREFIX) || effect.length() <= CHANGE_TO_HAMSTER_OFF_PREFIX.length()) continue;
                return true;
            }
        }
        AnimationClip secondaryClip = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        class_1306 secondaryArm = PunchyAnimationManager.POSE_HANDLER.getSecondaryArm();
        class_1306 class_13063 = secondaryExpectedArm = secondaryArm != null ? AdorableHamsterPetsCompat.oppositeArm(secondaryArm) : AdorableHamsterPetsCompat.resolvePlayerOffhandArm();
        if (secondaryClip != null && PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle() && secondaryExpectedArm != null && requestedArm == secondaryExpectedArm) {
            for (String effect : secondaryClip.getActiveEffects(PunchyAnimationManager.POSE_HANDLER.getSecondaryTime())) {
                if (effect == null || !effect.startsWith(CHANGE_TO_HAMSTER_OFF_PREFIX) || effect.length() <= CHANGE_TO_HAMSTER_OFF_PREFIX.length()) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean isOffOnlyGripActiveNow() {
        if (!AdorableHamsterPetsCompat.hasAnyOffPrefixedEffectActive()) {
            return false;
        }
        boolean normalRight = AdorableHamsterPetsCompat.isEffectActive(CHANGE_TO_HAMSTER, class_1306.field_6183) || AdorableHamsterPetsCompat.isNormalHamsterPrefixedEffectActive(class_1306.field_6183);
        boolean normalLeft = AdorableHamsterPetsCompat.isEffectActive(CHANGE_TO_HAMSTER, class_1306.field_6182) || AdorableHamsterPetsCompat.isNormalHamsterPrefixedEffectActive(class_1306.field_6182);
        return !normalRight && !normalLeft;
    }

    private static boolean hasAnyOffPrefixedEffectActive() {
        AnimationClip secondaryClip;
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            for (String effect : clip.getActiveEffects(PunchyAnimationManager.POSE_HANDLER.getCurrentTime())) {
                if (effect == null || !effect.startsWith(CHANGE_TO_HAMSTER_OFF_PREFIX) || effect.length() <= CHANGE_TO_HAMSTER_OFF_PREFIX.length()) continue;
                return true;
            }
        }
        if ((secondaryClip = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip()) != null && PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle()) {
            for (String effect : secondaryClip.getActiveEffects(PunchyAnimationManager.POSE_HANDLER.getSecondaryTime())) {
                if (effect == null || !effect.startsWith(CHANGE_TO_HAMSTER_OFF_PREFIX) || effect.length() <= CHANGE_TO_HAMSTER_OFF_PREFIX.length()) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean isNormalHamsterPrefixedEffectActive(class_1306 requestedArm) {
        return AdorableHamsterPetsCompat.hasNormalHamsterPrefixedEffectInClip(PunchyAnimationManager.POSE_HANDLER.getCurrentClip(), PunchyAnimationManager.POSE_HANDLER.isPlaying(), PunchyAnimationManager.POSE_HANDLER.getActiveArm(), PunchyAnimationManager.POSE_HANDLER.getCurrentTime(), requestedArm) || AdorableHamsterPetsCompat.hasNormalHamsterPrefixedEffectInClip(PunchyAnimationManager.POSE_HANDLER.getSecondaryClip(), PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle(), PunchyAnimationManager.POSE_HANDLER.getSecondaryArm(), PunchyAnimationManager.POSE_HANDLER.getSecondaryTime(), requestedArm);
    }

    private static boolean hasNormalHamsterPrefixedEffectInClip(AnimationClip clip, boolean clipActive, class_1306 clipArm, float time, class_1306 requestedArm) {
        if (clip == null || !clipActive || !AdorableHamsterPetsCompat.isArmCompatible(requestedArm, clipArm)) {
            return false;
        }
        for (String effect : clip.getActiveEffects(time)) {
            if (effect == null || effect.startsWith(CHANGE_TO_HAMSTER_OFF_PREFIX) || !effect.startsWith(CHANGE_TO_HAMSTER_PREFIX) || effect.length() <= CHANGE_TO_HAMSTER_PREFIX.length()) continue;
            return true;
        }
        return false;
    }

    private static boolean isPlayerOffhandArm(class_742 player, class_1306 arm) {
        if (player == null || arm == null) {
            return false;
        }
        class_1306 main = player.method_6068();
        class_1306 off = main == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
        return arm == off;
    }

    private static boolean isItemEffectActive(class_1306 arm) {
        return AdorableHamsterPetsCompat.isEffectActive(CHANGE_TO_ITEM, arm);
    }

    private static boolean isEffectActive(String effect, class_1306 requestedArm) {
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && AdorableHamsterPetsCompat.isArmCompatible(requestedArm, PunchyAnimationManager.POSE_HANDLER.getActiveArm()) && clip.isEffectActive(effect, PunchyAnimationManager.POSE_HANDLER.getCurrentTime())) {
            return true;
        }
        AnimationClip secondaryClip = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip();
        return secondaryClip != null && PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle() && AdorableHamsterPetsCompat.isArmCompatible(requestedArm, PunchyAnimationManager.POSE_HANDLER.getSecondaryArm()) && secondaryClip.isEffectActive(effect, PunchyAnimationManager.POSE_HANDLER.getSecondaryTime());
    }

    private static boolean isPrefixedEffectActive(String prefix, class_1306 requestedArm) {
        AnimationClip secondaryClip;
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip != null && PunchyAnimationManager.POSE_HANDLER.isPlaying() && AdorableHamsterPetsCompat.isArmCompatible(requestedArm, PunchyAnimationManager.POSE_HANDLER.getActiveArm())) {
            for (String effect : clip.getActiveEffects(PunchyAnimationManager.POSE_HANDLER.getCurrentTime())) {
                if (effect == null || !effect.startsWith(prefix) || effect.length() <= prefix.length()) continue;
                return true;
            }
        }
        if ((secondaryClip = PunchyAnimationManager.POSE_HANDLER.getSecondaryClip()) != null && PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle() && AdorableHamsterPetsCompat.isArmCompatible(requestedArm, PunchyAnimationManager.POSE_HANDLER.getSecondaryArm())) {
            for (String effect : secondaryClip.getActiveEffects(PunchyAnimationManager.POSE_HANDLER.getSecondaryTime())) {
                if (effect == null || !effect.startsWith(prefix) || effect.length() <= prefix.length()) continue;
                return true;
            }
        }
        return false;
    }

    private static RequestedHamsterState resolveRequestedHamsterState(class_1306 requestedArm) {
        RequestedHamsterState primary = AdorableHamsterPetsCompat.resolveRequestedHamsterStateFromClip(PunchyAnimationManager.POSE_HANDLER.getCurrentClip(), PunchyAnimationManager.POSE_HANDLER.isPlaying(), PunchyAnimationManager.POSE_HANDLER.getActiveArm(), PunchyAnimationManager.POSE_HANDLER.getCurrentTime(), requestedArm);
        if (primary != null) {
            return primary;
        }
        return AdorableHamsterPetsCompat.resolveRequestedHamsterStateFromClip(PunchyAnimationManager.POSE_HANDLER.getSecondaryClip(), PunchyAnimationManager.POSE_HANDLER.isSecondaryCompatIdle(), PunchyAnimationManager.POSE_HANDLER.getSecondaryArm(), PunchyAnimationManager.POSE_HANDLER.getSecondaryTime(), requestedArm);
    }

    private static RequestedHamsterState resolveRequestedHamsterStateFromClip(AnimationClip clip, boolean clipActive, class_1306 clipArm, float time, class_1306 requestedArm) {
        if (clip == null || !clipActive) {
            return null;
        }
        RequestedHamsterState latestFromTimeline = AdorableHamsterPetsCompat.resolveLatestTimelineStateToken(clip, time, requestedArm, clipArm);
        if (latestFromTimeline != null && !latestFromTimeline.token.isEmpty()) {
            return latestFromTimeline;
        }
        ArrayList<String> matches = new ArrayList<String>();
        for (String effect : clip.getActiveEffects(time)) {
            String token = AdorableHamsterPetsCompat.resolveHamsterStateTokenForArm(effect, requestedArm, clipArm);
            if (token.isEmpty()) continue;
            matches.add(token);
        }
        if (matches.isEmpty()) {
            return null;
        }
        Collections.sort(matches);
        return new RequestedHamsterState((String)matches.get(0), null);
    }

    private static RequestedHamsterState resolveLatestTimelineStateToken(AnimationClip clip, float time, class_1306 requestedArm, class_1306 clipArm) {
        if (clip == null) {
            return null;
        }
        NavigableMap<Float, Set<String>> timeline = clip.getTimelineEffects();
        if (timeline == null || timeline.isEmpty()) {
            return null;
        }
        String latest = "";
        Float latestKeyTime = null;
        for (Map.Entry entry : timeline.headMap(Float.valueOf(time), true).entrySet()) {
            if (entry == null || entry.getValue() == null || ((Set)entry.getValue()).isEmpty()) continue;
            for (String effect : (Set)entry.getValue()) {
                String token = AdorableHamsterPetsCompat.resolveHamsterStateTokenForArm(effect, requestedArm, clipArm);
                if (token.isEmpty()) continue;
                latest = token;
                latestKeyTime = (Float)entry.getKey();
            }
        }
        if (latest.isEmpty()) {
            return null;
        }
        String triggerKey = null;
        if (latestKeyTime != null) {
            triggerKey = System.identityHashCode(clip) + ":" + latestKeyTime + ":" + latest;
        }
        return new RequestedHamsterState(latest, triggerKey);
    }

    private static String resolveHamsterStateTokenForArm(String effect, class_1306 requestedArm, class_1306 clipArm) {
        if (effect == null) {
            return "";
        }
        if (effect.startsWith(CHANGE_TO_HAMSTER_OFF_PREFIX) && effect.length() > CHANGE_TO_HAMSTER_OFF_PREFIX.length()) {
            class_1306 offArm;
            class_1306 class_13062 = offArm = clipArm != null ? AdorableHamsterPetsCompat.oppositeArm(clipArm) : AdorableHamsterPetsCompat.resolvePlayerOffhandArm();
            if (!AdorableHamsterPetsCompat.isArmCompatible(requestedArm, offArm)) {
                return "";
            }
            return AdorableHamsterPetsCompat.normalizeRequestedStateToken(effect.substring(CHANGE_TO_HAMSTER_OFF_PREFIX.length()));
        }
        if (effect.startsWith(CHANGE_TO_HAMSTER_PREFIX) && effect.length() > CHANGE_TO_HAMSTER_PREFIX.length()) {
            if (!AdorableHamsterPetsCompat.isArmCompatible(requestedArm, clipArm)) {
                return "";
            }
            return AdorableHamsterPetsCompat.normalizeRequestedStateToken(effect.substring(CHANGE_TO_HAMSTER_PREFIX.length()));
        }
        return "";
    }

    private static void applyRequestedHamsterState(class_1297 entity, RequestedHamsterState requestedState) {
        if (entity == null) {
            return;
        }
        String normalizedState = AdorableHamsterPetsCompat.normalizeRequestedStateToken(requestedState == null ? null : requestedState.token);
        String triggerKey = requestedState == null ? null : requestedState.triggerKey;
        AdorableHamsterPetsCompat.clearForcedHamsterState(entity);
        if (normalizedState.isEmpty()) {
            AdorableHamsterPetsCompat.clearTimelineTriggerState();
            return;
        }
        if (AdorableHamsterPetsCompat.tryApplyTimelineAnimationToken(entity, normalizedState, triggerKey)) {
            return;
        }
        switch (normalizedState) {
            case "begging": {
                AdorableHamsterPetsCompat.setBegging(entity, true);
                break;
            }
            case "sitting": {
                AdorableHamsterPetsCompat.invokeSitting(entity, true);
                break;
            }
            case "cleaning": {
                AdorableHamsterPetsCompat.invokeSitting(entity, true);
                AdorableHamsterPetsCompat.setCleaning(entity, true);
                break;
            }
            case "sleep": 
            case "sleeping": 
            case "deep_sleep": {
                AdorableHamsterPetsCompat.invokeBoolean(setSleepingMethod, entity, true);
                AdorableHamsterPetsCompat.invokeString(setCurrentDeepSleepAnimIdMethod, entity, "anim_hamster_sleep_pose1");
                AdorableHamsterPetsCompat.setDozingPhase(entity, "DEEP_SLEEP");
                break;
            }
            case "sleep_pose1": {
                AdorableHamsterPetsCompat.invokeBoolean(setSleepingMethod, entity, true);
                AdorableHamsterPetsCompat.invokeString(setCurrentDeepSleepAnimIdMethod, entity, "anim_hamster_sleep_pose1");
                AdorableHamsterPetsCompat.setDozingPhase(entity, "DEEP_SLEEP");
                break;
            }
            case "sleep_pose2": {
                AdorableHamsterPetsCompat.invokeBoolean(setSleepingMethod, entity, true);
                AdorableHamsterPetsCompat.invokeString(setCurrentDeepSleepAnimIdMethod, entity, "anim_hamster_sleep_pose2");
                AdorableHamsterPetsCompat.setDozingPhase(entity, "DEEP_SLEEP");
                break;
            }
            case "sleep_pose3": {
                AdorableHamsterPetsCompat.invokeBoolean(setSleepingMethod, entity, true);
                AdorableHamsterPetsCompat.invokeString(setCurrentDeepSleepAnimIdMethod, entity, "anim_hamster_sleep_pose3");
                AdorableHamsterPetsCompat.setDozingPhase(entity, "DEEP_SLEEP");
                break;
            }
            case "drifting_off": 
            case "drifting_off_pose1": 
            case "drifting_off_pose2": 
            case "drifting_off_pose3": {
                AdorableHamsterPetsCompat.setDozingPhase(entity, "DRIFTING_OFF");
                break;
            }
            case "knocked_out": 
            case "ko": {
                AdorableHamsterPetsCompat.invokeBoolean(setKnockedOutMethod, entity, true);
                break;
            }
            case "sulk": 
            case "sulking": {
                AdorableHamsterPetsCompat.invokeBoolean(setSulkingMethod, entity, true);
                break;
            }
            case "taunt": 
            case "taunting": {
                AdorableHamsterPetsCompat.invokeBoolean(setTauntingMethod, entity, true);
                break;
            }
            case "presenting": 
            case "presenting_item": {
                AdorableHamsterPetsCompat.invokeBoolean(setPresentingItemMethod, entity, true);
                break;
            }
            case "seeking_ore": {
                AdorableHamsterPetsCompat.invokeString(setActiveCustomGoalDebugNameMethod, entity, "HamsterSniffForOreGoal");
                AdorableHamsterPetsCompat.invokeBoolean(setOreTargetAboveMethod, entity, false);
                entity.method_18799(new class_243(0.05, 0.0, 0.0));
                break;
            }
            case "wants_to_seek_ore_above": {
                AdorableHamsterPetsCompat.invokeString(setActiveCustomGoalDebugNameMethod, entity, "HamsterSniffForOreGoal");
                AdorableHamsterPetsCompat.invokeBoolean(setOreTargetAboveMethod, entity, true);
                entity.method_18799(class_243.field_1353);
                break;
            }
            case "wants_to_seek_ore_below": {
                AdorableHamsterPetsCompat.invokeString(setActiveCustomGoalDebugNameMethod, entity, "HamsterSniffForOreGoal");
                AdorableHamsterPetsCompat.invokeBoolean(setOreTargetAboveMethod, entity, false);
                entity.method_18799(class_243.field_1353);
                break;
            }
            case "idle_looking_up": 
            case "idle_looking_up1": 
            case "idle_looking_up2": 
            case "idle_looking_up3": {
                AdorableHamsterPetsCompat.invokeString(setActiveCustomGoalDebugNameMethod, entity, "HamsterLookAtEntityGoal");
                break;
            }
            case "flying": {
                entity.method_18799(new class_243(0.0, -0.05, 0.0));
                break;
            }
            case "walk": 
            case "walking": 
            case "run": 
            case "running": 
            case "sprint": 
            case "sprinting": 
            case "idle": 
            case "idle1": 
            case "idle2": {
                break;
            }
        }
    }

    private static boolean tryApplyTimelineAnimationToken(class_1297 entity, String normalizedState, String triggerKey) {
        if (entity == null || normalizedState == null || normalizedState.isBlank()) {
            return false;
        }
        if ("assume_throw_pose".equals(normalizedState)) {
            return AdorableHamsterPetsCompat.triggerTimelineHamsterAnimation(entity, normalizedState, "anim_hamster_assume_throw_pose", triggerKey);
        }
        if (normalizedState.startsWith("anim_")) {
            return AdorableHamsterPetsCompat.triggerTimelineHamsterAnimation(entity, normalizedState, normalizedState, triggerKey);
        }
        if (normalizedState.endsWith("_pose")) {
            return AdorableHamsterPetsCompat.triggerTimelineHamsterAnimation(entity, normalizedState, "anim_hamster_" + normalizedState, triggerKey);
        }
        return false;
    }

    private static boolean triggerTimelineHamsterAnimation(class_1297 entity, String token, String animName, String triggerKey) {
        String key;
        if (token == null || token.isBlank()) {
            return false;
        }
        String string = key = triggerKey == null || triggerKey.isBlank() ? token : triggerKey;
        if (preserveTriggeredAnimTicks > 0 && lastTimelineTriggeredAnimName != null && !lastTimelineTriggeredAnimName.isBlank() && lastTimelineTriggeredAnimName.equals(animName)) {
            return true;
        }
        if (key.equals(lastTimelineTriggerToken)) {
            return true;
        }
        AdorableHamsterPetsCompat.stopLastTimelineTriggeredAnimation(entity);
        if (!AdorableHamsterPetsCompat.triggerHamsterAnimation(entity, animName)) {
            return false;
        }
        lastTimelineTriggerToken = key;
        lastTimelineTriggeredAnimName = animName;
        return true;
    }

    private static boolean triggerHamsterAnimation(class_1297 entity, String animName) {
        if (entity == null || animName == null || animName.isBlank() || triggerAnimMethod == null) {
            return false;
        }
        try {
            triggerAnimMethod.invoke((Object)entity, "mainController", animName);
            return true;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static void clearTimelineTriggerState() {
        if (!lastTimelineTriggerToken.isBlank() || !lastTimelineTriggeredAnimName.isBlank()) {
            // empty if block
        }
        lastTimelineTriggerToken = "";
        lastTimelineClipRef = null;
        lastTimelineClipTime = -1.0f;
        lastTimelineTriggeredAnimName = "";
    }

    private static void refreshTimelineTriggerLifecycle() {
        AnimationClip currentClip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        boolean playing = PunchyAnimationManager.POSE_HANDLER.isPlaying();
        float currentTime = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
        if (!playing || currentClip == null) {
            if (preserveTriggeredAnimTicks > 0) {
                return;
            }
            class_1297 entity = AdorableHamsterPetsCompat.resolveRenderEntity((class_742)class_310.method_1551().field_1724);
            AdorableHamsterPetsCompat.stopLastTimelineTriggeredAnimation(entity);
            AdorableHamsterPetsCompat.clearTimelineTriggerState();
            return;
        }
        if (lastTimelineClipRef != currentClip) {
            if (preserveTriggeredAnimTicks > 0) {
                lastTimelineClipRef = currentClip;
                lastTimelineClipTime = currentTime;
                return;
            }
            class_1297 entity = AdorableHamsterPetsCompat.resolveRenderEntity((class_742)class_310.method_1551().field_1724);
            AdorableHamsterPetsCompat.stopLastTimelineTriggeredAnimation(entity);
            lastTimelineTriggerToken = "";
            lastTimelineClipRef = currentClip;
            lastTimelineClipTime = currentTime;
            return;
        }
        if (currentTime + 1.0E-4f < lastTimelineClipTime) {
            if (preserveTriggeredAnimTicks > 0) {
                lastTimelineClipTime = currentTime;
                return;
            }
            class_1297 entity = AdorableHamsterPetsCompat.resolveRenderEntity((class_742)class_310.method_1551().field_1724);
            AdorableHamsterPetsCompat.stopLastTimelineTriggeredAnimation(entity);
            lastTimelineTriggerToken = "";
        }
        lastTimelineClipTime = currentTime;
    }

    private static void stopLastTimelineTriggeredAnimation(class_1297 entity) {
        if (entity == null || stopTriggeredAnimMethod == null || lastTimelineTriggeredAnimName == null || lastTimelineTriggeredAnimName.isBlank()) {
            return;
        }
        try {
            stopTriggeredAnimMethod.invoke((Object)entity, "mainController", lastTimelineTriggeredAnimName);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void stopLastTimelineTriggeredAnimationOnAllDummies() {
        if (stopTriggeredAnimMethod == null || lastTimelineTriggeredAnimName == null || lastTimelineTriggeredAnimName.isBlank()) {
            return;
        }
        for (class_1297 entity : gripRenderDummies.values()) {
            if (entity == null) continue;
            try {
                stopTriggeredAnimMethod.invoke((Object)entity, "mainController", lastTimelineTriggeredAnimName);
            }
            catch (Throwable throwable) {}
        }
    }

    private static void resetGripDummyForLocation(Object location) {
        if (location == null) {
            return;
        }
        gripRenderDummies.remove(location);
    }

    private static void resetAllGripDummies() {
        gripRenderDummies.clear();
    }

    private static String normalizeRequestedStateToken(String raw) {
        char last;
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        while (!(normalized.isEmpty() || (last = normalized.charAt(normalized.length() - 1)) >= 'a' && last <= 'z' || last >= '0' && last <= '9' || last == '_')) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private static void clearForcedHamsterState(class_1297 entity) {
        AdorableHamsterPetsCompat.invokeSitting(entity, false);
        AdorableHamsterPetsCompat.setBegging(entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setSleepingMethod, entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setKnockedOutMethod, entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setSulkingMethod, entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setTauntingMethod, entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setPresentingItemMethod, entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setOreTargetAboveMethod, entity, false);
        AdorableHamsterPetsCompat.invokeBoolean(setCelebratingDiamondMethod, entity, false);
        AdorableHamsterPetsCompat.invokeString(setCurrentDeepSleepAnimIdMethod, entity, "");
        AdorableHamsterPetsCompat.invokeString(setActiveCustomGoalDebugNameMethod, entity, "None");
        AdorableHamsterPetsCompat.setDozingPhase(entity, "NONE");
        AdorableHamsterPetsCompat.setCleaning(entity, false);
    }

    private static void setCleaning(class_1297 entity, boolean value) {
        if (entity == null || setHamsterFlagMethod == null || cleaningFlagField == null) {
            return;
        }
        try {
            int cleaningFlag = cleaningFlagField.getInt(null);
            setHamsterFlagMethod.invoke((Object)entity, cleaningFlag, value);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void setBegging(class_1297 entity, boolean value) {
        AdorableHamsterPetsCompat.invokeBoolean(setBeggingMethod, entity, value);
        AdorableHamsterPetsCompat.setBooleanFlag(entity, beggingFlagField, value);
    }

    private static void setBooleanFlag(class_1297 entity, Field flagField, boolean value) {
        if (entity == null || setHamsterFlagMethod == null || flagField == null) {
            return;
        }
        try {
            int flag = flagField.getInt(null);
            setHamsterFlagMethod.invoke((Object)entity, flag, value);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void setDozingPhase(class_1297 entity, String phaseName) {
        if (entity == null || setDozingPhaseMethod == null || phaseName == null || phaseName.isBlank()) {
            return;
        }
        try {
            Class<?> phaseClass = setDozingPhaseMethod.getParameterTypes()[0];
            if (!phaseClass.isEnum()) {
                return;
            }
            Enum enumValue = Enum.valueOf(phaseClass.asSubclass(Enum.class), phaseName);
            setDozingPhaseMethod.invoke((Object)entity, enumValue);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void invokeSitting(class_1297 entity, boolean sitting) {
        if (entity == null || setSittingMethod == null) {
            return;
        }
        try {
            setSittingMethod.invoke((Object)entity, sitting, true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void invokeBoolean(Method method, class_1297 entity, boolean value) {
        if (method == null || entity == null) {
            return;
        }
        try {
            method.invoke((Object)entity, value);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void invokeString(Method method, class_1297 entity, String value) {
        if (method == null || entity == null) {
            return;
        }
        try {
            method.invoke((Object)entity, value);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean isArmCompatible(class_1306 requestedArm, class_1306 clipArm) {
        return requestedArm == null || clipArm == null || requestedArm == clipArm;
    }

    private static class_1306 oppositeArm(class_1306 arm) {
        if (arm == null) {
            return null;
        }
        return arm == class_1306.field_6182 ? class_1306.field_6183 : class_1306.field_6182;
    }

    private static class_1306 resolvePlayerOffhandArm() {
        class_746 class_7462;
        class_310 client = class_310.method_1551();
        if (client == null || !((class_7462 = client.field_1724) instanceof class_746)) {
            return null;
        }
        class_746 player = class_7462;
        class_1306 main = player.method_6068();
        return main == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
    }

    private static Object resolvePlayerOffhandSlot() {
        class_1306 offhandArm = AdorableHamsterPetsCompat.resolvePlayerOffhandArm();
        if (offhandArm == null) {
            return null;
        }
        return offhandArm == class_1306.field_6182 ? leftShoulderSlot : rightShoulderSlot;
    }

    private static void refreshOffhandProxyHamsterState(class_742 player) {
        if (player == null || player.method_37908() == null) {
            return;
        }
        if (lockOffhandProxyDuringPetting) {
            AdorableHamsterPetsCompat.refreshLockedOffhandProxyFromEntityId(player);
            return;
        }
        class_310 client = class_310.method_1551();
        if (client == null) {
            return;
        }
        class_239 hit = client.field_1765;
        if (!(hit instanceof class_3966)) {
            class_2487 snapshot;
            class_1297 cached;
            if (offhandProxyHamsterEntityId > 0 && AdorableHamsterPetsCompat.isHamsterEntity(cached = player.method_37908().method_8469(offhandProxyHamsterEntityId)) && (snapshot = AdorableHamsterPetsCompat.snapshotHamsterEntityToStateNbt(cached)) != null && !snapshot.method_33133()) {
                offhandProxyHamsterState = snapshot;
                offhandProxyHamsterStateTicks = 40;
            }
            return;
        }
        class_3966 entityHit = (class_3966)hit;
        class_1297 target = entityHit.method_17782();
        if (!AdorableHamsterPetsCompat.isHamsterEntity(target)) {
            return;
        }
        class_2487 snapshot = AdorableHamsterPetsCompat.snapshotHamsterEntityToStateNbt(target);
        if (snapshot == null || snapshot.method_33133()) {
            return;
        }
        offhandProxyHamsterState = snapshot;
        offhandProxyHamsterStateTicks = 40;
        offhandProxyHamsterEntityId = target.method_5628();
    }

    private static void lockCurrentOffhandProxyTarget(class_742 player) {
        lockOffhandProxyDuringPetting = true;
        if (player == null || player.method_37908() == null) {
            return;
        }
        if (AdorableHamsterPetsCompat.lockActivePettingHamsterState(player)) {
            return;
        }
        if (AdorableHamsterPetsCompat.lockCachedOffhandProxyTarget(player)) {
            return;
        }
        AdorableHamsterPetsCompat.refreshLockedOffhandProxyFromEntityId(player);
    }

    private static boolean lockCachedOffhandProxyTarget(class_742 player) {
        if (offhandProxyHamsterEntityId <= 0 || offhandProxyHamsterState == null || offhandProxyHamsterState.method_33133()) {
            return false;
        }
        class_1297 target = player.method_37908().method_8469(offhandProxyHamsterEntityId);
        if (AdorableHamsterPetsCompat.isHamsterEntity(target)) {
            class_2487 snapshot = AdorableHamsterPetsCompat.snapshotHamsterEntityToStateNbt(target);
            AdorableHamsterPetsCompat.setOffhandProxyHamsterTarget(target.method_5628(), snapshot == null || snapshot.method_33133() ? offhandProxyHamsterState : snapshot);
            return true;
        }
        AdorableHamsterPetsCompat.setOffhandProxyHamsterTarget(offhandProxyHamsterEntityId, offhandProxyHamsterState);
        return true;
    }

    private static boolean lockActivePettingHamsterState(class_742 player) {
        class_2487 pettingState = AdorableHamsterPetsCompat.getActivePettingHamsterNbt(player);
        if (pettingState == null || pettingState.method_33133()) {
            return false;
        }
        AdorableHamsterPetsCompat.setOffhandProxyHamsterTarget(-1, pettingState);
        return true;
    }

    private static class_2487 getActivePettingHamsterNbt(class_742 player) {
        if (player == null) {
            return new class_2487();
        }
        AdorableHamsterPetsCompat.ensurePlayerPettingFields(player.getClass());
        if (playerPettingHamsterField == null) {
            return new class_2487();
        }
        try {
            if (playerPettingTimerField != null && playerPettingTimerField.getInt(player) <= 0) {
                return new class_2487();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            class_2487 tag;
            Object value = playerPettingHamsterField.get(player);
            if (value instanceof class_2487 && !(tag = (class_2487)value).method_33133()) {
                return tag.method_10553();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new class_2487();
    }

    private static void ensurePlayerPettingFields(Class<?> playerClass) {
        if (playerPettingHamsterField != null) {
            return;
        }
        playerPettingHamsterField = AdorableHamsterPetsCompat.findFieldInHierarchy(playerClass, "ahp$pettingHamster");
        playerPettingTimerField = AdorableHamsterPetsCompat.findFieldInHierarchy(playerClass, "ahp$pettingTimer");
    }

    private static void setOffhandProxyHamsterTarget(int entityId, class_2487 snapshot) {
        if (snapshot == null || snapshot.method_33133()) {
            return;
        }
        offhandProxyHamsterState = snapshot.method_10553();
        offhandProxyHamsterStateTicks = 40;
        offhandProxyHamsterEntityId = entityId;
    }

    private static void refreshLockedOffhandProxyFromEntityId(class_742 player) {
        if (player == null || player.method_37908() == null || offhandProxyHamsterEntityId <= 0) {
            return;
        }
        class_1297 locked = player.method_37908().method_8469(offhandProxyHamsterEntityId);
        if (!AdorableHamsterPetsCompat.isHamsterEntity(locked)) {
            return;
        }
        class_2487 snapshot = AdorableHamsterPetsCompat.snapshotHamsterEntityToStateNbt(locked);
        if (snapshot == null || snapshot.method_33133()) {
            return;
        }
        AdorableHamsterPetsCompat.setOffhandProxyHamsterTarget(locked.method_5628(), snapshot);
    }

    private static boolean isHamsterEntity(class_1297 entity) {
        if (entity == null) {
            return false;
        }
        class_2960 typeId = class_7923.field_41177.method_10221((Object)entity.method_5864());
        return HAMSTER_ID.equals((Object)typeId);
    }

    private static class_2487 snapshotHamsterEntityToStateNbt(class_1297 entity) {
        if (entity == null || saveToHamsterStateMethod == null || hamsterStateToNbtMethod == null) {
            return new class_2487();
        }
        try {
            Object itemsObj;
            Object hamsterState = saveToHamsterStateMethod.invoke(null, entity);
            if (hamsterState == null) {
                return new class_2487();
            }
            Object nbtObj = hamsterStateToNbtMethod.invoke(hamsterState, new Object[0]);
            if (!(nbtObj instanceof class_2487)) {
                return new class_2487();
            }
            class_2487 nbt = (class_2487)nbtObj;
            class_2487 snapshot = nbt.method_10553();
            if (hamsterGetItemsMethod == null) {
                hamsterGetItemsMethod = AdorableHamsterPetsCompat.findMethod(entity.getClass(), "getItems", 0);
            }
            if (hamsterGetItemsMethod != null && (itemsObj = hamsterGetItemsMethod.invoke((Object)entity, new Object[0])) instanceof class_2371) {
                class_2371 items = (class_2371)itemsObj;
                class_2487 inventoryNbt = new class_2487();
                class_1262.method_5426((class_2487)inventoryNbt, (class_2371)items, (class_7225.class_7874)entity.method_37908().method_30349());
                snapshot.method_10566("inventoryNbt", (class_2520)inventoryNbt);
            }
            return snapshot;
        }
        catch (Throwable ignored) {
            return new class_2487();
        }
    }

    private static class_2487 getShoulderHamster(class_742 player, Object slot) {
        if (player == null || slot == null) {
            return new class_2487();
        }
        try {
            class_2487 class_24872;
            Object result = getShoulderHamsterMethod.invoke((Object)player, slot);
            if (result instanceof class_2487) {
                class_2487 tag = (class_2487)result;
                class_24872 = tag.method_10553();
            } else {
                class_24872 = new class_2487();
            }
            return class_24872;
        }
        catch (Throwable ignored) {
            return new class_2487();
        }
    }

    private static class_1297 createHamsterEntity(class_742 player, class_2487 shoulderNbt) {
        class_1299 entityType = class_7923.field_41177.method_17966(HAMSTER_ID).orElse(null);
        if (entityType == null) {
            return null;
        }
        class_1297 entity = entityType.method_5883(player.method_37908());
        if (entity == null) {
            return null;
        }
        try {
            Optional hamsterState = (Optional)hamsterStateFromNbtMethod.invoke(null, shoulderNbt);
            if (hamsterState == null || hamsterState.isEmpty()) {
                return null;
            }
            applyHamsterStateMethod.invoke(shoulderRendererInstance, entity, hamsterState.get(), player);
            if (setShoulderPetMethod != null) {
                setShoulderPetMethod.invoke((Object)entity, false);
            }
            if (setSittingMethod != null) {
                setSittingMethod.invoke((Object)entity, false, true);
            }
            return entity;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static class_1297 getShoulderDummy(class_742 player, Object slot) {
        if (player == null || slot == null) {
            return null;
        }
        class_2487 shoulderNbt = AdorableHamsterPetsCompat.getShoulderHamster(player, slot);
        if (shoulderNbt.method_33133()) {
            return null;
        }
        try {
            Object clientData;
            class_1297 entity = AdorableHamsterPetsCompat.getOrCreateGripRenderDummy(player, slot);
            if (entity == null) {
                return null;
            }
            Optional hamsterState = (Optional)hamsterStateFromNbtMethod.invoke(null, shoulderNbt);
            if (hamsterState == null || hamsterState.isEmpty()) {
                return null;
            }
            Object object = clientData = getClientHamsterStateMethod != null ? getClientHamsterStateMethod.invoke((Object)player, new Object[0]) : null;
            if (updateDummyStateMethod != null && clientData != null) {
                updateDummyStateMethod.invoke(shoulderRendererInstance, entity, hamsterState.get(), clientData, slot, player);
            } else {
                applyHamsterStateMethod.invoke(shoulderRendererInstance, entity, hamsterState.get(), player);
            }
            return entity;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static class_1297 getOrCreateGripRenderDummy(class_742 player, Object slot) {
        if (player == null || slot == null) {
            return null;
        }
        class_1297 existing = gripRenderDummies.get(slot);
        if (existing != null && existing.method_37908() == player.method_37908()) {
            return existing;
        }
        class_1299 entityType = class_7923.field_41177.method_17966(HAMSTER_ID).orElse(null);
        if (entityType == null) {
            return null;
        }
        class_1297 created = entityType.method_5883(player.method_37908());
        if (created == null) {
            return null;
        }
        gripRenderDummies.put(slot, created);
        return created;
    }

    private static void applyTransitOverrides(class_1297 entity, class_742 player) {
        if (entity == null || player == null) {
            return;
        }
        AdorableHamsterPetsCompat.updateTransitState(entity, player);
        if (setShoulderPetMethod != null) {
            try {
                setShoulderPetMethod.invoke((Object)entity, false);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (setSittingMethod != null) {
            try {
                setSittingMethod.invoke((Object)entity, false, true);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private static void applyShaderMultipassAnimationFix(class_1297 entity, float partialTicks) {
        if (entity == null || getAnimatableInstanceCacheMethod == null || getManagerForIdMethod == null || updatedAtMethod == null || lastRenderTimeField == null) {
            return;
        }
        try {
            Object cache = getAnimatableInstanceCacheMethod.invoke((Object)entity, new Object[0]);
            if (cache == null) {
                return;
            }
            Class<?>[] managerKeyTypes = getManagerForIdMethod.getParameterTypes();
            Long key = managerKeyTypes.length > 0 && (managerKeyTypes[0] == Long.TYPE || managerKeyTypes[0] == Long.class) ? (long)entity.method_5628() : (long)entity.method_5628();
            Object manager = getManagerForIdMethod.invoke(cache, key);
            if (manager == null) {
                return;
            }
            double currentTick = (float)entity.field_6012 + partialTicks;
            double lastRenderTime = lastRenderTimeField.getDouble(entity);
            if (currentTick == lastRenderTime) {
                updatedAtMethod.invoke(manager, currentTick - 1.0E-7);
            }
            lastRenderTimeField.setDouble(entity, currentTick);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void resetTracking() {
        trackedPlayerUuid = null;
        previousRightShoulder = new class_2487();
        previousLeftShoulder = new class_2487();
        previousHead = new class_2487();
        activeTransitHamster = new class_2487();
        activeTransitLocation = null;
        activeTransitTicks = 0;
        pendingYeetLocation = null;
        pendingYeetTicks = 0;
        lastThrowQueued = false;
        lastThrowQueueTicks = 0;
        activeThrowLocation = null;
        throwChargeActive = false;
        lastPettingActive = false;
        lastPettingClipName = "";
        lastAttackKeyDown = false;
        lastUseKeyDown = false;
        lastPetHamsterKeyDown = false;
        lastPetHamsterPhysicalDown = false;
        pettingCancelSuppressTicks = 0;
        throwOutcomeWaitTicks = 0;
        postReleaseCleanupTicks = 0;
        suppressTimelineStateTicks = 0;
        preserveTriggeredAnimTicks = 0;
        AdorableHamsterPetsCompat.clearTimelineTriggerState();
        gripRenderDummies.clear();
    }

    private static boolean isAvailable() {
        if (!availabilityChecked) {
            availabilityChecked = true;
            available = Services.PLATFORM != null && Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return available;
    }

    private static boolean ensureReflectionReady() {
        if (reflectionReady) {
            return true;
        }
        if (reflectionFailed || !AdorableHamsterPetsCompat.isAvailable()) {
            return false;
        }
        try {
            Class<?> shoulderLocationClass = Class.forName("net.dawson.adorablehamsterpets.entity.ShoulderLocation");
            Class<?> hamsterStateClass = Class.forName("net.dawson.adorablehamsterpets.util.HamsterState");
            Class<?> shoulderRendererClass = Class.forName("net.dawson.adorablehamsterpets.entity.client.feature.HamsterShoulderFeatureRenderer");
            Class<?> hamsterClass = Class.forName("net.dawson.adorablehamsterpets.entity.custom.HamsterEntity");
            Class<?> configDataCacheClass = Class.forName("net.dawson.adorablehamsterpets.config.ConfigDataCache");
            Class<?> hamsterInteractionUtilClass = Class.forName("net.dawson.adorablehamsterpets.util.HamsterInteractionUtil");
            Class<?> hamsterNbtUtilClass = Class.forName("net.dawson.adorablehamsterpets.util.HamsterNbtUtil");
            Class<?> modKeyBindingsClass = Class.forName("net.dawson.adorablehamsterpets.client.option.ModKeyBindings");
            rightShoulderSlot = Enum.valueOf(shoulderLocationClass.asSubclass(Enum.class), "RIGHT_SHOULDER");
            leftShoulderSlot = Enum.valueOf(shoulderLocationClass.asSubclass(Enum.class), "LEFT_SHOULDER");
            headSlot = Enum.valueOf(shoulderLocationClass.asSubclass(Enum.class), "HEAD");
            getShoulderHamsterMethod = AdorableHamsterPetsCompat.findMethod(class_742.class, "getShoulderHamster", 1);
            if (getShoulderHamsterMethod == null) {
                getShoulderHamsterMethod = AdorableHamsterPetsCompat.findMethod(class_746.class, "getShoulderHamster", 1);
            }
            if ((getMountOrderQueueMethod = AdorableHamsterPetsCompat.findMethod(class_742.class, "adorablehamsterpets$getMountOrderQueue", 0)) == null) {
                getMountOrderQueueMethod = AdorableHamsterPetsCompat.findMethod(class_746.class, "adorablehamsterpets$getMountOrderQueue", 0);
            }
            getNextHamsterToDismountDataMethod = AdorableHamsterPetsCompat.findMethod(hamsterInteractionUtilClass, "getNextHamsterToDismountData", 1);
            getClientHamsterStateMethod = AdorableHamsterPetsCompat.findMethod(class_742.class, "adorablehamsterpets$getClientHamsterState", 0);
            if (getClientHamsterStateMethod == null) {
                getClientHamsterStateMethod = AdorableHamsterPetsCompat.findMethod(class_746.class, "adorablehamsterpets$getClientHamsterState", 0);
            }
            hamsterStateFromNbtMethod = AdorableHamsterPetsCompat.findMethod(hamsterStateClass, "fromNbt", 1);
            hamsterStateToNbtMethod = AdorableHamsterPetsCompat.findMethod(hamsterStateClass, "toNbt", 0);
            saveToHamsterStateMethod = AdorableHamsterPetsCompat.findMethod(hamsterNbtUtilClass, "saveToHamsterState", 1);
            isLureItemMethod = AdorableHamsterPetsCompat.findMethod(configDataCacheClass, "isLureItem", 1);
            Constructor<?> ctor = shoulderRendererClass.getConstructor(class_3883.class);
            shoulderRendererInstance = ctor.newInstance(new Object[]{null});
            applyHamsterStateMethod = AdorableHamsterPetsCompat.findMethod(shoulderRendererClass, "applyHamsterState", 3);
            updateDummyStateMethod = AdorableHamsterPetsCompat.findMethod(shoulderRendererClass, "updateDummyState", 5);
            setShoulderPetMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setShoulderPet", 1);
            setSittingMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setSitting", 2);
            setBeggingMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setBegging", 1);
            setSleepingMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setSleeping", 1);
            setKnockedOutMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setKnockedOut", 1);
            setSulkingMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setSulking", 1);
            setTauntingMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setTaunting", 1);
            setPresentingItemMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setPresentingItem", 1);
            setOreTargetAboveMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setOreTargetAbove", 1);
            setCelebratingDiamondMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setCelebratingDiamond", 1);
            setActiveCustomGoalDebugNameMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setActiveCustomGoalDebugName", 1);
            setCurrentDeepSleepAnimIdMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setCurrentDeepSleepAnimId", 1);
            setDozingPhaseMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setDozingPhase", 1);
            setHamsterFlagMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "setHamsterFlag", 2);
            triggerAnimMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "triggerAnim", 2);
            stopTriggeredAnimMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "stopTriggeredAnim", 2);
            hamsterGetItemsMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "getItems", 0);
            getAnimatableInstanceCacheMethod = AdorableHamsterPetsCompat.findMethod(hamsterClass, "getAnimatableInstanceCache", 0);
            lastRenderTimeField = AdorableHamsterPetsCompat.findField(hamsterClass, "lastRenderTime");
            cleaningFlagField = AdorableHamsterPetsCompat.findField(hamsterClass, "CLEANING_FLAG");
            beggingFlagField = AdorableHamsterPetsCompat.findField(hamsterClass, "BEGGING_FLAG");
            Class<?> ahpClientClass = Class.forName("net.dawson.adorablehamsterpets.AdorableHamsterPetsClient");
            throwIsQueuingField = AdorableHamsterPetsCompat.findField(ahpClientClass, "isQueuingThrow");
            throwQueueTicksField = AdorableHamsterPetsCompat.findField(ahpClientClass, "throwQueueTicks");
            throwQueueRequiredTicksField = AdorableHamsterPetsCompat.findField(ahpClientClass, "THROW_QUEUE_REQUIRED_TICKS");
            clientPettingTicksField = AdorableHamsterPetsCompat.findField(ahpClientClass, "clientPettingTicks");
            petHamsterKeyBindingField = AdorableHamsterPetsCompat.findField(modKeyBindingsClass, "PET_HAMSTER_KEY");
            if (petHamsterKeyBindingField == null) {
                petHamsterKeyBindingField = AdorableHamsterPetsCompat.findKeyMappingFieldByHint(modKeyBindingsClass, "pet", "hamster");
            }
            try {
                Class<?> animatableCacheClass = Class.forName("software.bernie.geckolib.animatable.instance.AnimatableInstanceCache");
                Class<?> animatableManagerClass = Class.forName("software.bernie.geckolib.animation.AnimatableManager");
                getManagerForIdMethod = AdorableHamsterPetsCompat.findMethod(animatableCacheClass, "getManagerForId", 1);
                updatedAtMethod = AdorableHamsterPetsCompat.findMethod(animatableManagerClass, "updatedAt", 1);
            }
            catch (Throwable ignored) {
                getManagerForIdMethod = null;
                updatedAtMethod = null;
            }
            if (getShoulderHamsterMethod == null || hamsterStateFromNbtMethod == null || applyHamsterStateMethod == null) {
                throw new IllegalStateException("Required AHP reflection methods were not found");
            }
            reflectionReady = true;
            return true;
        }
        catch (Throwable t) {
            reflectionFailed = true;
            return false;
        }
    }

    private static Method findMethod(Class<?> owner, String name, int parameterCount) {
        if (owner == null || name == null) {
            return null;
        }
        for (Method method : owner.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != parameterCount) continue;
            method.setAccessible(true);
            return method;
        }
        for (Method method : owner.getDeclaredMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != parameterCount) continue;
            method.setAccessible(true);
            return method;
        }
        return null;
    }

    private static Field findField(Class<?> owner, String name) {
        if (owner == null || name == null) {
            return null;
        }
        try {
            Field field = owner.getField(name);
            field.setAccessible(true);
            return field;
        }
        catch (Throwable field) {
            try {
                Field field2 = owner.getDeclaredField(name);
                field2.setAccessible(true);
                return field2;
            }
            catch (Throwable ignored) {
                return null;
            }
        }
    }

    private static Field findFieldInHierarchy(Class<?> owner, String name) {
        for (Class<?> type = owner; type != null && type != Object.class; type = type.getSuperclass()) {
            Field field = AdorableHamsterPetsCompat.findField(type, name);
            if (field == null) continue;
            return field;
        }
        return null;
    }

    private static Field findKeyMappingFieldByHint(Class<?> owner, String ... hints) {
        if (owner == null) {
            return null;
        }
        String[] safeHints = hints == null ? new String[]{} : hints;
        for (Field field : owner.getDeclaredFields()) {
            if (field == null || !class_304.class.isAssignableFrom(field.getType())) continue;
            String name = field.getName() == null ? "" : field.getName().toLowerCase(Locale.ROOT);
            boolean matches = safeHints.length == 0;
            for (String hint : safeHints) {
                if (hint == null || hint.isBlank() || !name.contains(hint.toLowerCase(Locale.ROOT))) continue;
                matches = true;
                break;
            }
            if (!matches) continue;
            try {
                field.setAccessible(true);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return field;
        }
        for (Field field : owner.getDeclaredFields()) {
            if (field == null || !class_304.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return field;
        }
        return null;
    }

    static {
        previousRightShoulder = new class_2487();
        previousLeftShoulder = new class_2487();
        previousHead = new class_2487();
        activeTransitHamster = new class_2487();
        lastLureHand = class_1268.field_5808;
        lastPettingClipName = "";
        offhandProxyHamsterState = new class_2487();
        offhandProxyHamsterEntityId = -1;
        lastTimelineTriggerToken = "";
        lastTimelineClipTime = -1.0f;
        lastTimelineTriggeredAnimName = "";
        gripRenderDummies = new HashMap<Object, class_1297>();
    }

    private static final class RequestedHamsterState {
        final String token;
        final String triggerKey;

        private RequestedHamsterState(String token, String triggerKey) {
            this.token = token == null ? "" : token;
            this.triggerKey = triggerKey;
        }
    }
}

