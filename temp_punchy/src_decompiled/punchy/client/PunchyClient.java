/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3300
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package punchy.client;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import net.minecraft.class_638;
import net.minecraft.class_746;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.PunchyThirdPersonAnimationManager;
import punchy.client.bedrockitems.BedrockItemSystem;
import punchy.client.input.AnimationKeybinds;
import punchy.client.modelparts.ModelPartsParticleSystem;
import punchy.client.modelparts.ModelPartsSystem;
import punchy.client.render.MacePhysics;
import punchy.client.state.AttackActionTracker;
import punchy.client.state.AttackStateMachine;
import punchy.client.state.BowStateMachine;
import punchy.client.state.ClimbStateMachine;
import punchy.client.state.ConsumeStateMachine;
import punchy.client.state.CrawlStateMachine;
import punchy.client.state.CrossbowStateMachine;
import punchy.client.state.DryOutStateMachine;
import punchy.client.state.FallingStateMachine;
import punchy.client.state.FireStateMachine;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.IdlePoseStateMachine;
import punchy.client.state.InspectActionTracker;
import punchy.client.state.InspectStateMachine;
import punchy.client.state.InterfaceDelayState;
import punchy.client.state.MiningStateMachine;
import punchy.client.state.MovementStateMachine;
import punchy.client.state.MudStateMachine;
import punchy.client.state.ShieldStateMachine;
import punchy.client.state.SpearStateMachine;
import punchy.client.state.SweatStateMachine;
import punchy.client.state.SwimmingStateMachine;
import punchy.client.state.TridentStateMachine;
import punchy.client.state.TriggerableActionTracker;
import punchy.client.state.TriggerableStateMachine;
import punchy.client.state.UseActionTracker;
import punchy.client.state.UseItemStateMachine;
import punchy.client.state.WakeUpStateMachine;
import punchy.client.state.WalkOnWaterStateMachine;
import punchy.client.tuning.MixPackKeybinds;
import punchy.client.tuning.TuningEditorKeybinds;
import punchy.client.tuning.TuningKeybinds;
import punchy.compat.AdorableHamsterPetsCompat;
import punchy.compat.BetterCombatCompat;
import punchy.compat.EmfEtfCemCompat;
import punchy.compat.FirstPersonModelCompat;
import punchy.compat.SpellEngineCompat;
import punchy.compat.SwordBlockingMechanicsCompat;
import punchy.compat.entityculling.EntityCullingCompat;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;
import punchy.platform.Services;

public final class PunchyClient {
    private static final float ELYTRA_OFFHAND_SHOW_SPEED_MIN = 0.55f;
    private static final MiningStateMachine MINING_STATE = new MiningStateMachine();
    public static final MudStateMachine MUD_STATE = new MudStateMachine();
    private static final SweatStateMachine SWEAT_STATE = new SweatStateMachine();
    private static final UseItemStateMachine USE_STATE = new UseItemStateMachine();
    private static final AttackStateMachine ATTACK_STATE = new AttackStateMachine();
    private static final BowStateMachine BOW_STATE = new BowStateMachine();
    private static final CrossbowStateMachine CROSSBOW_STATE = new CrossbowStateMachine();
    private static final SpearStateMachine SPEAR_STATE = new SpearStateMachine();
    private static final ShieldStateMachine SHIELD_STATE = new ShieldStateMachine();
    private static final TridentStateMachine TRIDENT_STATE = new TridentStateMachine();
    private static final ConsumeStateMachine CONSUME_STATE = new ConsumeStateMachine();
    private static final SwimmingStateMachine SWIM_STATE = new SwimmingStateMachine();
    private static final WalkOnWaterStateMachine WALK_ON_WATER_STATE = new WalkOnWaterStateMachine();
    private static final DryOutStateMachine DRY_OUT_STATE = new DryOutStateMachine();
    private static final CrawlStateMachine CRAWL_STATE = new CrawlStateMachine();
    private static final ClimbStateMachine CLIMB_STATE = new ClimbStateMachine();
    private static final FireStateMachine FIRE_STATE = new FireStateMachine();
    private static final MovementStateMachine MOVEMENT_STATE = MovementStateMachine.get();
    private static final HandEquipStateMachine HAND_EQUIP_STATE = new HandEquipStateMachine();
    private static final FallingStateMachine FALLING_STATE = new FallingStateMachine();
    private static final InspectStateMachine INSPECT_STATE = new InspectStateMachine();
    private static final IdlePoseStateMachine IDLE_POSE_STATE = new IdlePoseStateMachine();
    private static final TriggerableStateMachine TRIGGERABLE_STATE = new TriggerableStateMachine();
    private static final WakeUpStateMachine WAKE_UP_STATE = new WakeUpStateMachine();
    private static boolean RESOURCES_READY = false;
    private static Object LAST_RESOURCE_REFRESH_LEVEL;

    private PunchyClient() {
    }

    public static WalkOnWaterStateMachine getWalkOnWaterStateMachine() {
        return WALK_ON_WATER_STATE;
    }

    public static boolean shouldSuppressOffhandRender(class_1657 player) {
        class_1306 offArm;
        if (HAND_EQUIP_STATE == null || player == null) {
            return false;
        }
        if (PunchyAnimationManager.shouldSuppressOffhandForAttack()) {
            return true;
        }
        boolean keepVisibleForFlightPose = PunchyClient.shouldKeepOffhandVisibleForFastFlight(player);
        if (keepVisibleForFlightPose && player instanceof class_746) {
            class_746 localPlayer = (class_746)player;
            if (HAND_EQUIP_STATE.isOffhandRenderSuppressed() || HAND_EQUIP_STATE.shouldLowerOffhand(localPlayer)) {
                keepVisibleForFlightPose = false;
            }
        }
        if (keepVisibleForFlightPose) {
            return false;
        }
        class_1306 class_13062 = offArm = player.method_6068() == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
        if (PunchyAnimationManager.isItemForceShown(offArm)) {
            return false;
        }
        boolean suppressed = HAND_EQUIP_STATE.isOffhandRenderSuppressed();
        if (suppressed) {
            return true;
        }
        if (PunchyAnimationManager.isSwimDualHandedActive()) {
            return false;
        }
        if (PunchyAnimationManager.SWIM_HANDLER.isPlaying()) {
            class_1306 swimArm = PunchyAnimationManager.SWIM_HANDLER.getActiveArm();
            if (swimArm == null) {
                return false;
            }
            if (swimArm == offArm) {
                return false;
            }
        }
        return false;
    }

    private static boolean shouldKeepOffhandVisibleForFastFlight(class_1657 player) {
        if (!(player instanceof class_746)) {
            return false;
        }
        class_746 localPlayer = (class_746)player;
        if (!PunchyConfig.enableElytraFlightAnimation()) {
            return false;
        }
        if (localPlayer.method_24828() || localPlayer.method_5681() || localPlayer.method_5799() || !localPlayer.method_6128()) {
            return false;
        }
        class_243 velocity = localPlayer.method_18798();
        float speed = velocity == null ? 0.0f : (float)velocity.method_1033();
        return speed >= 0.55f;
    }

    public static boolean shouldLowerOffhand(class_746 player) {
        if (HAND_EQUIP_STATE == null || player == null) {
            return false;
        }
        return HAND_EQUIP_STATE.shouldLowerOffhand(player);
    }

    public static void init() {
        TuningEditorKeybinds.register();
        TuningKeybinds.register();
        MixPackKeybinds.register();
        AnimationKeybinds.register();
        PunchyConfig.load();
        PunchyAnimationManager.setAnimationSpeed(PunchyConfig.getAnimationSpeed());
        BetterCombatCompat.initialize();
        SpellEngineCompat.initialize();
        SwordBlockingMechanicsCompat.initialize();
        Services.CLIENT.registerReloadListener(class_2960.method_60655((String)"punchy", (String)"animation_loader"), PunchyClient::onResourceReload);
        Services.CLIENT.registerClientTickEnd(client -> {
            if (!RESOURCES_READY && client.method_1478() != null) {
                PunchyClient.onResourceReload(client.method_1478());
            }
            if (client.field_1724 != null && client.field_1687 != null && client.method_1478() != null) {
                class_638 currentLevel = client.field_1687;
                if (LAST_RESOURCE_REFRESH_LEVEL != currentLevel) {
                    PunchyClient.resetTransientWorldState();
                    LAST_RESOURCE_REFRESH_LEVEL = currentLevel;
                    PunchyClient.onResourceReload(client.method_1478());
                }
            } else if (client.field_1687 == null) {
                PunchyClient.resetTransientWorldState();
                LAST_RESOURCE_REFRESH_LEVEL = null;
            }
            if (client.field_1724 == null) {
                return;
            }
            if (!client.method_1493()) {
                boolean hasBlacklistedHand;
                boolean firstPerson;
                if (!PunchyConfig.isModEnabled()) {
                    return;
                }
                EntityCullingCompat.tick();
                class_746 player = client.field_1724;
                if (BetterCombatCompat.isAvailable()) {
                    firstPerson = client.field_1690.method_31044().method_31034();
                    hasBlacklistedHand = PunchyConfig.isHandBlacklisted((class_1657)player, class_1268.field_5808) || PunchyConfig.isHandBlacklisted((class_1657)player, class_1268.field_5810);
                    boolean firstPersonModelActive = FirstPersonModelCompat.isRenderingPlayer() || FirstPersonModelCompat.shouldApplyThirdPersonAnimations(client);
                    boolean compatActive = PunchyConfig.isBetterCombatCompatEnabled() && firstPerson && !hasBlacklistedHand && !firstPersonModelActive;
                    BetterCombatCompat.applyVanillaItemDisplay(compatActive);
                    if (compatActive) {
                        BetterCombatCompat.forceStopAttackAnimation(player);
                        if (!PunchyAnimationManager.isAnyHandBlacklisted()) {
                            BetterCombatCompat.suppressItemPoses(player);
                        }
                    }
                }
                if (SpellEngineCompat.isAvailable()) {
                    firstPerson = client.field_1690.method_31044().method_31034();
                    hasBlacklistedHand = PunchyConfig.isHandBlacklisted((class_1657)player, class_1268.field_5808) || PunchyConfig.isHandBlacklisted((class_1657)player, class_1268.field_5810);
                    boolean compatActive = PunchyConfig.isBetterCombatCompatEnabled() && firstPerson && !hasBlacklistedHand;
                    SpellEngineCompat.applyFirstPersonAnimations(compatActive);
                }
                FirstPersonModelCompat.tick(client);
                PunchyAnimationManager.enforceBlacklist(client);
                float speed = PunchyAnimationManager.ANIMATION_SPEED;
                PunchyAnimationManager.POSE_HANDLER.tick(0.05f, speed);
                PunchyAnimationManager.syncSourceHandToActiveClip();
                PunchyAnimationManager.SWIM_HANDLER.tick(0.05f, speed);
                PunchyThirdPersonAnimationManager.tick(client);
                MINING_STATE.tick((class_310)client);
                MUD_STATE.tick((class_310)client);
                SweatStateMachine.stopWet();
                CONSUME_STATE.tick((class_310)client);
                USE_STATE.tick((class_310)client);
                AdorableHamsterPetsCompat.tickClient(client);
                BOW_STATE.tick((class_310)client);
                CROSSBOW_STATE.tick((class_310)client);
                SPEAR_STATE.tick((class_310)client);
                SHIELD_STATE.tick((class_310)client);
                TRIDENT_STATE.tick((class_310)client);
                FIRE_STATE.tick((class_310)client);
                ATTACK_STATE.tick((class_310)client);
                FALLING_STATE.tick((class_310)client);
                INSPECT_STATE.tick((class_310)client);
                TRIGGERABLE_STATE.tick((class_310)client);
                BedrockItemSystem.get().tick((class_310)client);
                WALK_ON_WATER_STATE.tick((class_310)client);
                SWIM_STATE.tick((class_310)client);
                DRY_OUT_STATE.tick((class_310)client);
                CRAWL_STATE.tick((class_310)client);
                CLIMB_STATE.tick((class_310)client);
                WAKE_UP_STATE.tick((class_310)client);
                HAND_EQUIP_STATE.tick((class_310)client);
                ModelPartsSystem.get().tick((class_310)client);
                IDLE_POSE_STATE.tick((class_310)client);
                if (!PunchyConfig.disableNativeItemPhysics()) {
                    MacePhysics.get().tick(player);
                }
                InterfaceDelayState.tick(client);
                PunchyAnimationManager.updateUseMeshSuppression(client);
                PunchyAnimationManager.updateItemOverrides(client);
                PunchyAnimationManager.updateSoundEffects(client);
            }
        });
    }

    private static void onResourceReload(class_3300 resourceManager) {
        if (resourceManager == null) {
            RESOURCES_READY = false;
            return;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1697 == null) {
            RESOURCES_READY = false;
            return;
        }
        PunchyConfig.load();
        PunchyAnimationManager.setAnimationSpeed(PunchyConfig.getAnimationSpeed());
        PunchyAnimationManager.loadAnimations(resourceManager);
        PunchyThirdPersonAnimationManager.loadAnimations(resourceManager);
        PunchyTuningConfig.loadAndApply(resourceManager);
        ModelPartsParticleSystem.resetAll();
        ModelPartsSystem.get().onResourceReload(resourceManager);
        BedrockItemSystem.get().onResourceReload(resourceManager);
        EmfEtfCemCompat.onResourceReload(resourceManager);
        RESOURCES_READY = true;
    }

    private static void resetTransientWorldState() {
        ATTACK_STATE.resetSessionState();
        AttackActionTracker.clear();
        UseActionTracker.clear();
        InspectActionTracker.clear();
        TriggerableActionTracker.clear();
    }
}

