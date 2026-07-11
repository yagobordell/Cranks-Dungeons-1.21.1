/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1690
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_7923
 */
package punchy.client.tuning;

import java.util.Locale;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1690;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_7923;
import punchy.client.debug.PunchyDebug;
import punchy.client.render.ChestProxyTuning;
import punchy.client.render.EnchantParticleProxyTuning;
import punchy.client.render.FishingLineProxyTuning;
import punchy.client.render.FluidProxyTuning;
import punchy.client.render.PaddleProxyTuning;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.ArmMeshTuning;
import punchy.client.renderer.layer.ArmToolTuning;
import punchy.client.renderer.layer.PlayerArmModelLayer;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.SpearStateMachine;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public final class TuningKeybinds {
    private static final String CATEGORY = "key.categories.punchy.tuning";
    private static final boolean ENABLE_TUNING_HOTKEYS = false;
    private static final VanillaFirstPersonItemLayer.ToolKind[] ITEM_KINDS = VanillaFirstPersonItemLayer.ToolKind.values();
    private static class_304 toggleContextKey;
    private static class_304 cycleKindKey;
    private static class_304 toggleSpecificityKey;
    private static class_304 toggleHandKey;
    private static class_304 cycleModeKey;
    private static class_304 cycleAxisKey;
    private static class_304 setAxisXKey;
    private static class_304 setAxisYKey;
    private static class_304 setAxisZKey;
    private static class_304 increaseKey;
    private static class_304 decreaseKey;
    private static class_304 saveKey;
    private static class_304 resetKey;
    private static class_304 mirrorKey;
    private static boolean registered;
    private static Target target;
    private static Axis axis;
    private static Mode mode;
    private static class_1268 hand;
    private static VanillaFirstPersonItemLayer.ToolKind currentKind;
    private static class_2960 currentItemId;
    private static ChestProxyTuning.Kind chestProxyKind;
    private static FluidProxyTuning.Kind fluidProxyKind;
    private static EnchantParticleProxyTuning.Kind enchantParticleKind;
    private static PaddleProxyTuning.Kind paddleProxyKind;
    private static FishingLineProxyTuning.Kind fishingLineProxyKind;

    private TuningKeybinds() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
    }

    public static void handleClientTick(class_310 client) {
        boolean decreased;
        if (!registered || client == null || client.field_1724 == null) {
            return;
        }
        if (!PunchyConfig.isTuningEnabled()) {
            return;
        }
        if (client.field_1755 != null) {
            return;
        }
        if (!PunchyDebug.DEBUG_POSITION_ENABLED) {
            return;
        }
        TuningKeybinds.autoSelectCurrentItem(client);
        while (toggleContextKey.method_1436()) {
            TuningKeybinds.toggleContext(client);
        }
        while (cycleKindKey.method_1436()) {
            TuningKeybinds.cycleKind(client);
        }
        while (toggleSpecificityKey.method_1436()) {
            TuningKeybinds.toggleSpecificity(client);
        }
        while (toggleHandKey.method_1436()) {
            hand = hand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
            TuningKeybinds.autoSelectCurrentItem(client);
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
        }
        while (cycleModeKey.method_1436()) {
            mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
        }
        while (cycleAxisKey.method_1436()) {
            TuningKeybinds.cycleAxis(client);
        }
        if (setAxisXKey.method_1436()) {
            TuningKeybinds.setAxis(client, Axis.X);
        }
        if (setAxisYKey.method_1436()) {
            TuningKeybinds.setAxis(client, Axis.Y);
        }
        if (setAxisZKey.method_1436()) {
            TuningKeybinds.setAxis(client, Axis.Z);
        }
        while (saveKey.method_1436()) {
            PunchyTuningConfig.saveCurrent();
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.saved_config"));
        }
        while (resetKey.method_1436()) {
            TuningKeybinds.resetCurrentTarget(client);
        }
        while (mirrorKey.method_1436()) {
            TuningKeybinds.mirrorCurrentTarget(client);
        }
        boolean increased = increaseKey.method_1434() && !decreaseKey.method_1434();
        boolean bl = decreased = decreaseKey.method_1434() && !increaseKey.method_1434();
        if (increased) {
            TuningKeybinds.applyDelta(client, true);
        } else if (decreased) {
            TuningKeybinds.applyDelta(client, false);
        }
    }

    private static void autoSelectCurrentItem(class_310 client) {
        if (client.field_1724 == null) {
            return;
        }
        TuningKeybinds.autoSelectPaddleProxyKind(client);
        class_1799 stack = client.field_1724.method_5998(hand);
        if (stack.method_7960()) {
            currentItemId = null;
            return;
        }
        currentItemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        currentKind = TuningKeybinds.resolveTuningKind(stack);
        TuningKeybinds.autoSelectChestProxyKind();
        TuningKeybinds.autoSelectFluidProxyKind(stack);
    }

    private static VanillaFirstPersonItemLayer.ToolKind resolveTuningKind(class_1799 stack) {
        if (SpearStateMachine.isSpearStack(stack)) {
            return VanillaFirstPersonItemLayer.ToolKind.SPEAR;
        }
        return ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.AXE);
    }

    private static void autoSelectPaddleProxyKind(class_310 client) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        class_1297 class_12972 = client.field_1724.method_5854();
        if (!(class_12972 instanceof class_1690)) {
            return;
        }
        class_1690 boat = (class_1690)class_12972;
        class_2960 id = class_7923.field_41177.method_10221((Object)boat.method_5864());
        paddleProxyKind = id != null && id.method_12832().contains("raft") ? PaddleProxyTuning.Kind.RAFT : PaddleProxyTuning.Kind.BOAT;
    }

    private static void autoSelectChestProxyKind() {
        if (currentItemId == null) {
            return;
        }
        String path = currentItemId.method_12832();
        if (path.endsWith("_chest_boat")) {
            chestProxyKind = ChestProxyTuning.Kind.BOAT;
        } else if (path.endsWith("_chest_raft")) {
            chestProxyKind = ChestProxyTuning.Kind.RAFT;
        } else if ("chest_minecart".equals(path)) {
            chestProxyKind = ChestProxyTuning.Kind.MINECART;
        }
    }

    private static void autoSelectFluidProxyKind(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return;
        }
        if (stack.method_31574(class_1802.field_8705)) {
            fluidProxyKind = FluidProxyTuning.Kind.WATER;
        } else if (stack.method_31574(class_1802.field_8187)) {
            fluidProxyKind = FluidProxyTuning.Kind.LAVA;
        } else if (stack.method_31574(class_1802.field_8103)) {
            fluidProxyKind = FluidProxyTuning.Kind.MILK;
        }
    }

    private static void toggleContext(class_310 client) {
        target = switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Target.ARM_KIND;
            case 1 -> Target.ARM_SPECIFIC;
            case 2 -> Target.ARM_GLOBAL;
            case 3, 4, 5, 6 -> Target.CHEST_PROXY;
            case 7 -> Target.FLUID_PROXY;
            case 8 -> Target.ENCHANT_PARTICLE_PROXY;
            case 9 -> Target.PADDLE_PROXY;
            case 10 -> Target.FISHING_LINE_PROXY;
            case 11 -> Target.ITEM_KIND;
        };
        TuningKeybinds.toast(client, TuningKeybinds.describeState());
    }

    private static void toggleSpecificity(class_310 client) {
        if (target == Target.CHEST_PROXY || target == Target.FLUID_PROXY || target == Target.ENCHANT_PARTICLE_PROXY || target == Target.PADDLE_PROXY || target == Target.FISHING_LINE_PROXY) {
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.use_home_proxy"));
            return;
        }
        target = switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Target.ITEM_SPECIFIC;
            case 1 -> Target.ITEM_GLOBAL;
            case 2 -> Target.ITEM_KIND;
            case 3 -> Target.ARM_SPECIFIC;
            case 4 -> Target.ARM_GLOBAL;
            case 5 -> Target.ARM_MESH;
            case 6 -> Target.ARM_KIND;
            case 7 -> Target.CHEST_PROXY;
            case 8 -> Target.FLUID_PROXY;
            case 9 -> Target.ENCHANT_PARTICLE_PROXY;
            case 10 -> Target.PADDLE_PROXY;
            case 11 -> Target.FISHING_LINE_PROXY;
        };
        TuningKeybinds.toast(client, TuningKeybinds.describeState());
    }

    private static void cycleKind(class_310 client) {
        if (target == Target.CHEST_PROXY) {
            ChestProxyTuning.Kind[] kinds = ChestProxyTuning.Kind.values();
            int idx = chestProxyKind.ordinal() + 1;
            if (idx >= kinds.length) {
                idx = 0;
            }
            chestProxyKind = kinds[idx];
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
            return;
        }
        if (target == Target.FLUID_PROXY) {
            FluidProxyTuning.Kind[] kinds = FluidProxyTuning.Kind.values();
            int idx = fluidProxyKind.ordinal() + 1;
            if (idx >= kinds.length) {
                idx = 0;
            }
            fluidProxyKind = kinds[idx];
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
            return;
        }
        if (target == Target.PADDLE_PROXY) {
            PaddleProxyTuning.Kind[] kinds = PaddleProxyTuning.Kind.values();
            int idx = paddleProxyKind.ordinal() + 1;
            if (idx >= kinds.length) {
                idx = 0;
            }
            paddleProxyKind = kinds[idx];
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
            return;
        }
        if (target == Target.FISHING_LINE_PROXY) {
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
            return;
        }
        if (target == Target.ENCHANT_PARTICLE_PROXY) {
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
            return;
        }
        if (target == Target.ARM_SPECIFIC) {
            PlayerArmModelLayer.ToolKind newKind;
            VanillaFirstPersonItemLayer.ToolKind newItemKind;
            int idx;
            if (currentItemId == null) {
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.hold_item_change_arm_kind"));
                return;
            }
            PlayerArmModelLayer.ToolKind[] kinds = PlayerArmModelLayer.ToolKind.values();
            PlayerArmModelLayer.ToolKind currentArmKind = TuningKeybinds.toArmKind(currentKind);
            if (currentArmKind == null) {
                currentArmKind = PlayerArmModelLayer.ToolKind.AXE;
            }
            if ((idx = currentArmKind.ordinal() + 1) >= kinds.length) {
                idx = 0;
            }
            if ((newItemKind = TuningKeybinds.findMatchingItemKind(newKind = kinds[idx])) != null) {
                currentKind = newItemKind;
            }
            boolean leftHand = hand == class_1268.field_5810;
            ArmToolTuning.Profile profile = ArmToolTuning.getSpecificProfile(currentItemId, newKind, leftHand);
            ArmToolTuning.applySpecific(currentItemId, newKind, leftHand ? null : profile, leftHand ? profile : null);
            ArmToolTuning.SpecificSnapshot fullSpec = ArmToolTuning.snapshotSpecifics().get(currentItemId);
            if (fullSpec != null) {
                ArmToolTuning.Profile right = fullSpec.right();
                ArmToolTuning.Profile left = fullSpec.left();
                ArmToolTuning.applySpecific(currentItemId, newKind, right, left);
            }
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.arm_kind_set", (Object[])new Object[]{newKind.name()}));
            return;
        }
        int idx = currentKind.ordinal() + 1;
        if (idx >= ITEM_KINDS.length) {
            idx = 0;
        }
        currentKind = ITEM_KINDS[idx];
        if (target == Target.ITEM_KIND || target == Target.ARM_KIND) {
            TuningKeybinds.toast(client, TuningKeybinds.describeState());
        } else {
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.kind_selected_group", (Object[])new Object[]{currentKind.name()}));
        }
    }

    private static VanillaFirstPersonItemLayer.ToolKind findMatchingItemKind(PlayerArmModelLayer.ToolKind armKind) {
        try {
            return VanillaFirstPersonItemLayer.ToolKind.valueOf(armKind.name());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void cycleAxis(class_310 client) {
        if (!TuningKeybinds.mode.usesAxis) {
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.axis_not_used", (Object[])new Object[]{TuningKeybinds.modeLabel(mode)}));
            return;
        }
        axis = Axis.values()[(axis.ordinal() + 1) % Axis.values().length];
        TuningKeybinds.toast(client, TuningKeybinds.describeState());
    }

    private static void setAxis(class_310 client, Axis desired) {
        if (!TuningKeybinds.mode.usesAxis) {
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.axis_not_used", (Object[])new Object[]{TuningKeybinds.modeLabel(mode)}));
            return;
        }
        axis = desired;
        TuningKeybinds.toast(client, TuningKeybinds.describeState());
    }

    private static void applyDelta(class_310 client, boolean positive) {
        if (mode == Mode.PIVOT && (target == Target.ARM_GLOBAL || target == Target.ARM_MESH || target == Target.ARM_KIND || target == Target.ARM_SPECIFIC || target == Target.CHEST_PROXY || target == Target.FLUID_PROXY || target == Target.ENCHANT_PARTICLE_PROXY || target == Target.PADDLE_PROXY || target == Target.FISHING_LINE_PROXY)) {
            TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.pivot_item_only"));
            return;
        }
        float delta = TuningKeybinds.computeStep(client);
        if (!positive) {
            delta *= -1.0f;
        }
        boolean leftHand = hand == class_1268.field_5810;
        switch (target.ordinal()) {
            case 0: {
                ToolTuning.Profile profile = ToolTuning.get(currentKind, leftHand);
                AdjustmentResult result = TuningKeybinds.adjustToolProfile(profile, delta);
                if (result == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.apply_failed"));
                    return;
                }
                ToolTuning.commitKind(currentKind, leftHand);
                ToolTuning.markKindDetached(currentKind, leftHand);
                TuningKeybinds.report(client, result);
                break;
            }
            case 1: {
                if (currentItemId == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.hold_item_edit_specific"));
                    return;
                }
                ToolTuning.Profile profile = ToolTuning.getSpecificProfile(currentItemId, currentKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.prepare_specific_failed"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustToolProfile(profile, delta);
                if (result == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.apply_specific_failed"));
                    return;
                }
                TuningKeybinds.report(client, result);
                break;
            }
            case 2: {
                ToolTuning.Profile profile = ToolTuning.getGlobal(leftHand);
                AdjustmentResult result = TuningKeybinds.adjustToolProfile(profile, delta);
                if (result == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.apply_global_failed"));
                    return;
                }
                ToolTuning.commitGlobal(leftHand);
                TuningKeybinds.report(client, result);
                break;
            }
            case 3: {
                PlayerArmModelLayer.ToolKind armKind = TuningKeybinds.toArmKind(currentKind);
                if (armKind == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.item_kind_no_arm_kind"));
                    return;
                }
                ArmToolTuning.Profile profile = ArmToolTuning.get(armKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.arm_kind_profile_missing"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustArmProfile(profile, delta);
                ArmToolTuning.commitKind(armKind, leftHand);
                ArmToolTuning.markKindDetached(armKind, leftHand);
                TuningKeybinds.report(client, result);
                break;
            }
            case 4: {
                if (currentItemId == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.hold_item_edit_arm_specific"));
                    return;
                }
                PlayerArmModelLayer.ToolKind armKind = TuningKeybinds.toArmKind(currentKind);
                if (armKind == null) {
                    armKind = PlayerArmModelLayer.ToolKind.AXE;
                }
                ArmToolTuning.Profile profile = ArmToolTuning.getSpecificProfile(currentItemId, armKind, leftHand);
                TuningKeybinds.report(client, TuningKeybinds.adjustArmProfile(profile, delta));
                ArmToolTuning.commitSpecific(currentItemId, leftHand);
                break;
            }
            case 5: {
                ArmToolTuning.Profile profile = ArmToolTuning.getGlobal(leftHand);
                TuningKeybinds.report(client, TuningKeybinds.adjustArmProfile(profile, delta));
                ArmToolTuning.commitGlobal(leftHand);
                break;
            }
            case 6: {
                ArmMeshTuning.Profile profile = ArmMeshTuning.getGlobal(leftHand);
                TuningKeybinds.report(client, TuningKeybinds.adjustArmMeshProfile(profile, delta));
                break;
            }
            case 7: {
                ChestProxyTuning.Profile profile = ChestProxyTuning.get(chestProxyKind);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.chest_proxy_profile_missing"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustChestProxyProfile(profile, delta);
                TuningKeybinds.report(client, result);
                break;
            }
            case 8: {
                FluidProxyTuning.Profile profile = FluidProxyTuning.get(fluidProxyKind);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.fluid_proxy_profile_missing"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustFluidProxyProfile(profile, delta);
                TuningKeybinds.report(client, result);
                break;
            }
            case 9: {
                EnchantParticleProxyTuning.Profile profile = EnchantParticleProxyTuning.get(enchantParticleKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.enchant_proxy_profile_missing"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustEnchantParticleProxyProfile(profile, delta);
                TuningKeybinds.report(client, result);
                break;
            }
            case 10: {
                PaddleProxyTuning.Profile profile = PaddleProxyTuning.get(paddleProxyKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.paddle_proxy_profile_missing"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustPaddleProxyProfile(profile, delta);
                TuningKeybinds.report(client, result);
                break;
            }
            case 11: {
                FishingLineProxyTuning.Profile profile = FishingLineProxyTuning.get(fishingLineProxyKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.fishing_line_proxy_profile_missing"));
                    return;
                }
                AdjustmentResult result = TuningKeybinds.adjustFishingLineProxyProfile(profile, delta);
                TuningKeybinds.report(client, result);
            }
        }
    }

    private static PlayerArmModelLayer.ToolKind toArmKind(VanillaFirstPersonItemLayer.ToolKind vk) {
        try {
            return PlayerArmModelLayer.ToolKind.valueOf(vk.name());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void resetCurrentTarget(class_310 client) {
        boolean leftHand = hand == class_1268.field_5810;
        switch (target.ordinal()) {
            case 0: {
                ToolTuning.Profile profile = ToolTuning.get(currentKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.no_item_profile_loaded"));
                    return;
                }
                TuningKeybinds.resetToolProfile(profile);
                ToolTuning.commitKind(currentKind, leftHand);
                ToolTuning.markKindAttached(currentKind, leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.offsets_reset_kind"));
                break;
            }
            case 2: {
                ToolTuning.Profile profile = ToolTuning.getGlobal(leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.no_global_item_profile"));
                    return;
                }
                TuningKeybinds.resetToolProfile(profile);
                ToolTuning.commitGlobal(leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.offsets_reset_global"));
                break;
            }
            case 1: {
                if (currentItemId == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.hold_item_reset_specific"));
                    return;
                }
                ToolTuning.Profile profile = ToolTuning.getSpecificProfile(currentItemId, currentKind, leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.specific_profile_missing"));
                    return;
                }
                TuningKeybinds.resetToolProfile(profile);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.offsets_reset_specific"));
                break;
            }
            case 3: {
                ArmToolTuning.Profile profile;
                PlayerArmModelLayer.ToolKind armKind = TuningKeybinds.toArmKind(currentKind);
                if (armKind == null || (profile = ArmToolTuning.get(armKind, leftHand)) == null) break;
                TuningKeybinds.resetArmProfile(profile, armKind, leftHand);
                ArmToolTuning.commitKind(armKind, leftHand);
                ArmToolTuning.markKindAttached(armKind, leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.arm_reset_kind", (Object[])new Object[]{armKind.name()}));
                break;
            }
            case 4: {
                ArmToolTuning.Profile profile;
                if (currentItemId == null) break;
                PlayerArmModelLayer.ToolKind armKind = TuningKeybinds.toArmKind(currentKind);
                if (armKind == null) {
                    armKind = PlayerArmModelLayer.ToolKind.AXE;
                }
                if ((profile = ArmToolTuning.getSpecificProfile(currentItemId, armKind, leftHand)) == null) break;
                TuningKeybinds.resetArmProfile(profile, armKind, leftHand);
                ArmToolTuning.commitSpecific(currentItemId, leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.arm_reset_specific"));
                break;
            }
            case 5: {
                ArmToolTuning.Profile profile = ArmToolTuning.getGlobal(leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.arm_profile_missing"));
                    return;
                }
                TuningKeybinds.resetArmProfile(profile, null, leftHand);
                ArmToolTuning.commitGlobal(leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.arm_offsets_reset"));
                break;
            }
            case 6: {
                ArmMeshTuning.Profile profile = ArmMeshTuning.getGlobal(leftHand);
                if (profile == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.arm_profile_missing"));
                    return;
                }
                TuningKeybinds.resetArmMeshProfile(profile);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.arm_mesh_offsets_reset"));
                break;
            }
            case 7: {
                ChestProxyTuning.reset(chestProxyKind);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.chest_proxy_reset", (Object[])new Object[]{chestProxyKind.name()}));
                break;
            }
            case 8: {
                FluidProxyTuning.reset(fluidProxyKind);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.fluid_proxy_reset", (Object[])new Object[]{fluidProxyKind.name()}));
                break;
            }
            case 9: {
                EnchantParticleProxyTuning.reset(enchantParticleKind, leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.enchant_proxy_reset", (Object[])new Object[]{enchantParticleKind.name()}));
                break;
            }
            case 10: {
                PaddleProxyTuning.reset(paddleProxyKind, leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.paddle_proxy_reset", (Object[])new Object[]{paddleProxyKind.name()}));
                break;
            }
            case 11: {
                FishingLineProxyTuning.reset(fishingLineProxyKind, leftHand);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.fishing_line_proxy_reset", (Object[])new Object[]{fishingLineProxyKind.name()}));
            }
        }
    }

    private static void mirrorCurrentTarget(class_310 client) {
        boolean leftHand = hand == class_1268.field_5810;
        boolean otherLeft = !leftHand;
        switch (target.ordinal()) {
            case 0: {
                ToolTuning.Profile src = ToolTuning.get(currentKind, leftHand);
                if (src == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.item_profile_missing"));
                    return;
                }
                ToolTuning.apply(currentKind, otherLeft, src.mirror());
                ToolTuning.markKindDetached(currentKind, otherLeft);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.mirror_hand", (Object[])new Object[]{otherLeft ? class_2561.method_43471((String)"punchy.tuning.hand.off") : class_2561.method_43471((String)"punchy.tuning.hand.main")}));
                break;
            }
            case 2: {
                ToolTuning.Profile src = ToolTuning.getGlobal(leftHand);
                if (src == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.global_profile_missing"));
                    return;
                }
                ToolTuning.applyGlobal(otherLeft, src.mirror());
                ToolTuning.commitGlobal(otherLeft);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.global_mirror_hand", (Object[])new Object[]{otherLeft ? class_2561.method_43471((String)"punchy.tuning.hand.off") : class_2561.method_43471((String)"punchy.tuning.hand.main")}));
                break;
            }
            case 1: {
                if (currentItemId == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.hold_item_mirror"));
                    return;
                }
                ToolTuning.Profile src = ToolTuning.getSpecificProfile(currentItemId, currentKind, leftHand);
                if (src == null) {
                    TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.specific_profile_missing_mirror"));
                    return;
                }
                ToolTuning.Profile mirrored = src.mirror();
                ToolTuning.Profile right = leftHand ? mirrored : ToolTuning.getSpecificProfile(currentItemId, currentKind, false);
                ToolTuning.Profile left = leftHand ? ToolTuning.getSpecificProfile(currentItemId, currentKind, true) : mirrored;
                ToolTuning.applySpecific(currentItemId, currentKind, null, right, left);
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.mirror_hand_specific", (Object[])new Object[]{otherLeft ? class_2561.method_43471((String)"punchy.tuning.hand.off") : class_2561.method_43471((String)"punchy.tuning.hand.main")}));
                break;
            }
            default: {
                TuningKeybinds.toast(client, (class_2561)class_2561.method_43471((String)"punchy.tuning.toast.mirror_item_only"));
            }
        }
    }

    private static void resetToolProfile(ToolTuning.Profile profile) {
        profile.offZ = 0.0f;
        profile.offY = 0.0f;
        profile.offX = 0.0f;
        profile.rotZ = 0.0f;
        profile.rotY = 0.0f;
        profile.rotX = 0.0f;
        profile.pivotZ = 0.0f;
        profile.pivotY = 0.0f;
        profile.pivotX = 0.0f;
        profile.recomputeRadians();
    }

    private static void resetArmProfile(ArmToolTuning.Profile profile, PlayerArmModelLayer.ToolKind kind, boolean leftHand) {
        ArmToolTuning.Profile base = kind == null ? PunchyTuningConfig.getDefaultGlobalArmBase(leftHand) : (kind == PlayerArmModelLayer.ToolKind.EMPTY_HAND ? PunchyTuningConfig.getDefaultArmEditorBase(kind, leftHand) : new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        profile.offX = base.offX;
        profile.offY = base.offY;
        profile.offZ = base.offZ;
        profile.rotX = base.rotX;
        profile.rotY = base.rotY;
        profile.rotZ = base.rotZ;
        profile.scale = base.scale;
    }

    private static void resetArmMeshProfile(ArmMeshTuning.Profile profile) {
        profile.offZ = 0.0f;
        profile.offY = 0.0f;
        profile.offX = 0.0f;
        profile.rotZ = 0.0f;
        profile.rotY = 0.0f;
        profile.rotX = 0.0f;
    }

    private static AdjustmentResult adjustToolProfile(ToolTuning.Profile profile, float delta) {
        AdjustmentResult result = switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                AdjustmentResult res;
                ToolTuning.RotationOrder order = ToolTuning.rotationOrderForItemId(currentItemId);
                char v1 = switch (axis.ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> 'X';
                    case 1 -> 'Y';
                    case 2 -> 'Z';
                };
                char rotationAxis = v1;
                ToolTuning.rotateProfileLocal(profile, order, rotationAxis, delta);
                AdjustmentResult v2 = switch (axis.ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> new AdjustmentResult("rotX", profile.rotX);
                    case 1 -> new AdjustmentResult("rotY", profile.rotY);
                    case 2 -> new AdjustmentResult("rotZ", profile.rotZ);
                };
                yield res = v2;
            }
            case 2 -> {
                profile.scale = Math.max(0.01f, profile.scale + delta);
                AdjustmentResult v3 = new AdjustmentResult("scale", profile.scale);
                yield v3;
            }
            case 3 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("pivotX", profile.pivotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("pivotY", profile.pivotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("pivotZ", profile.pivotZ += delta);
            }
        };
        return result;
    }

    private static AdjustmentResult adjustArmProfile(ArmToolTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.25f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for arms");
        };
    }

    private static AdjustmentResult adjustArmMeshProfile(ArmMeshTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.25f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for arms");
        };
    }

    private static AdjustmentResult adjustChestProxyProfile(ChestProxyTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.01f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for chest proxy");
        };
    }

    private static AdjustmentResult adjustFluidProxyProfile(FluidProxyTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.01f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for fluid proxy");
        };
    }

    private static AdjustmentResult adjustEnchantParticleProxyProfile(EnchantParticleProxyTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.01f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for particle proxy");
        };
    }

    private static AdjustmentResult adjustPaddleProxyProfile(PaddleProxyTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.01f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for paddle proxy");
        };
    }

    private static AdjustmentResult adjustFishingLineProxyProfile(FishingLineProxyTuning.Profile profile, float delta) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("offX", profile.offX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("offY", profile.offY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("offZ", profile.offZ += delta);
            }
            case 1 -> {
                switch (axis.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield new AdjustmentResult("rotX", profile.rotX += delta);
                    }
                    case 1: {
                        yield new AdjustmentResult("rotY", profile.rotY += delta);
                    }
                    case 2: 
                }
                yield new AdjustmentResult("rotZ", profile.rotZ += delta);
            }
            case 2 -> {
                profile.scale = Math.max(0.01f, profile.scale + delta);
                AdjustmentResult v1 = new AdjustmentResult("scale", profile.scale);
                yield v1;
            }
            case 3 -> throw new IllegalStateException("Pivot editing should be gated for fishing line proxy");
        };
    }

    private static float computeStep(class_310 client) {
        float base;
        switch (mode.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: 
            case 3: {
                float f = 0.01f;
                break;
            }
            case 1: {
                float f = 1.0f;
                break;
            }
            case 2: {
                float f = base = 0.01f;
            }
        }
        if (client.field_1690 == null) {
            return base;
        }
        if (client.field_1690.field_1867.method_1434()) {
            base *= 5.0f;
        }
        if (client.field_1690.field_1832.method_1434()) {
            base *= 0.2f;
        }
        return base;
    }

    private static void report(class_310 client, AdjustmentResult result) {
        if (result == null) {
            return;
        }
        TuningKeybinds.toast(client, TuningKeybinds.describeState(result));
    }

    private static class_2561 describeState() {
        return TuningKeybinds.describeState(null);
    }

    private static class_2561 describeState(AdjustmentResult change) {
        class_2561 targetLabel = TuningKeybinds.targetLabel(target);
        class_2561 selectionLabel = TuningKeybinds.selectionLabel();
        class_2561 handLabel = TuningKeybinds.handLabel(hand);
        class_2561 modeLabel = TuningKeybinds.modeLabel(mode);
        class_2561 axisLabel = TuningKeybinds.axisLabel(axis);
        if (change != null) {
            class_2561 property = TuningKeybinds.propertyLabel(change.property);
            String valueStr = String.format(Locale.ROOT, "%.4f", Float.valueOf(change.value));
            if (TuningKeybinds.mode.usesAxis) {
                return class_2561.method_43469((String)"punchy.tuning.status.with_axis_change", (Object[])new Object[]{targetLabel, selectionLabel, handLabel, modeLabel, axisLabel, property, valueStr});
            }
            return class_2561.method_43469((String)"punchy.tuning.status.no_axis_change", (Object[])new Object[]{targetLabel, selectionLabel, handLabel, modeLabel, property, valueStr});
        }
        if (TuningKeybinds.mode.usesAxis) {
            return class_2561.method_43469((String)"punchy.tuning.status.with_axis", (Object[])new Object[]{targetLabel, selectionLabel, handLabel, modeLabel, axisLabel});
        }
        return class_2561.method_43469((String)"punchy.tuning.status.no_axis", (Object[])new Object[]{targetLabel, selectionLabel, handLabel, modeLabel});
    }

    private static void toast(class_310 client, class_2561 message) {
        if (client == null || client.field_1724 == null) {
            return;
        }
        client.field_1705.method_1758(message, false);
    }

    private static class_2561 targetLabel(Target target) {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_2561.method_43471((String)"punchy.tuning.target.item_kind");
            case 1 -> class_2561.method_43471((String)"punchy.tuning.target.item_specific");
            case 2 -> class_2561.method_43471((String)"punchy.tuning.target.item_global");
            case 3 -> class_2561.method_43471((String)"punchy.tuning.target.arm_kind");
            case 4 -> class_2561.method_43471((String)"punchy.tuning.target.arm_specific");
            case 5 -> class_2561.method_43471((String)"punchy.tuning.target.arm_global");
            case 6 -> class_2561.method_43471((String)"punchy.tuning.target.arm_mesh");
            case 7 -> class_2561.method_43471((String)"punchy.tuning.target.chest_proxy");
            case 8 -> class_2561.method_43471((String)"punchy.tuning.target.fluid_proxy");
            case 9 -> class_2561.method_43471((String)"punchy.tuning.target.enchant_proxy");
            case 10 -> class_2561.method_43471((String)"punchy.tuning.target.paddle_proxy");
            case 11 -> class_2561.method_43471((String)"punchy.tuning.target.fishing_line_proxy");
        };
    }

    private static class_2561 selectionLabel() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 3 -> class_2561.method_43470((String)TuningKeybinds.friendlyName(currentKind.name()));
            case 1, 4 -> {
                if (currentItemId != null) {
                    yield class_2561.method_43470((String)currentItemId.toString());
                }
                yield class_2561.method_43471((String)"punchy.tuning.selection.none_item");
            }
            case 2 -> class_2561.method_43471((String)"punchy.tuning.selection.global_item");
            case 5 -> class_2561.method_43471((String)"punchy.tuning.selection.global_arm");
            case 6 -> class_2561.method_43471((String)"punchy.tuning.selection.arm_mesh");
            case 7 -> class_2561.method_43469((String)"punchy.tuning.selection.chest_proxy", (Object[])new Object[]{chestProxyKind.name()});
            case 8 -> class_2561.method_43469((String)"punchy.tuning.selection.fluid_proxy", (Object[])new Object[]{fluidProxyKind.name()});
            case 9 -> class_2561.method_43469((String)"punchy.tuning.selection.enchant_proxy", (Object[])new Object[]{enchantParticleKind.name()});
            case 10 -> class_2561.method_43469((String)"punchy.tuning.selection.paddle_proxy", (Object[])new Object[]{paddleProxyKind.name()});
            case 11 -> class_2561.method_43469((String)"punchy.tuning.selection.fishing_line_proxy", (Object[])new Object[]{fishingLineProxyKind.name()});
        };
    }

    private static class_2561 handLabel(class_1268 hand) {
        return hand == class_1268.field_5808 ? class_2561.method_43471((String)"punchy.tuning.hand.main") : class_2561.method_43471((String)"punchy.tuning.hand.off");
    }

    private static class_2561 modeLabel(Mode mode) {
        return switch (mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_2561.method_43471((String)"punchy.tuning.mode.translate");
            case 1 -> class_2561.method_43471((String)"punchy.tuning.mode.rotate");
            case 2 -> class_2561.method_43471((String)"punchy.tuning.mode.scale");
            case 3 -> class_2561.method_43471((String)"punchy.tuning.mode.pivot");
        };
    }

    private static class_2561 axisLabel(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_2561.method_43471((String)"punchy.tuning.axis.x");
            case 1 -> class_2561.method_43471((String)"punchy.tuning.axis.y");
            case 2 -> class_2561.method_43471((String)"punchy.tuning.axis.z");
        };
    }

    private static class_2561 propertyLabel(String property) {
        return switch (property) {
            case "offX" -> class_2561.method_43471((String)"punchy.tuning.property.off_x");
            case "offY" -> class_2561.method_43471((String)"punchy.tuning.property.off_y");
            case "offZ" -> class_2561.method_43471((String)"punchy.tuning.property.off_z");
            case "rotX" -> class_2561.method_43471((String)"punchy.tuning.property.rot_x");
            case "rotY" -> class_2561.method_43471((String)"punchy.tuning.property.rot_y");
            case "rotZ" -> class_2561.method_43471((String)"punchy.tuning.property.rot_z");
            case "scale" -> class_2561.method_43471((String)"punchy.tuning.property.scale");
            case "pivotX" -> class_2561.method_43471((String)"punchy.tuning.property.pivot_x");
            case "pivotY" -> class_2561.method_43471((String)"punchy.tuning.property.pivot_y");
            case "pivotZ" -> class_2561.method_43471((String)"punchy.tuning.property.pivot_z");
            default -> class_2561.method_43470((String)property);
        };
    }

    private static String friendlyName(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    static {
        registered = false;
        target = Target.ITEM_KIND;
        axis = Axis.X;
        mode = Mode.TRANSLATE;
        hand = class_1268.field_5808;
        currentKind = ITEM_KINDS[0];
        currentItemId = null;
        chestProxyKind = ChestProxyTuning.Kind.BOAT;
        fluidProxyKind = FluidProxyTuning.Kind.WATER;
        enchantParticleKind = EnchantParticleProxyTuning.Kind.BOOK;
        paddleProxyKind = PaddleProxyTuning.Kind.BOAT;
        fishingLineProxyKind = FishingLineProxyTuning.Kind.ROD;
    }

    private static enum Mode {
        TRANSLATE("offsets", true),
        ROTATE("rotation", true),
        SCALE("scale", false),
        PIVOT("pivot", true);

        final String label;
        final boolean usesAxis;

        private Mode(String label, boolean usesAxis) {
            this.label = label;
            this.usesAxis = usesAxis;
        }
    }

    private static enum Axis {
        X,
        Y,
        Z;

    }

    private static enum Target {
        ITEM_KIND,
        ITEM_SPECIFIC,
        ITEM_GLOBAL,
        ARM_KIND,
        ARM_SPECIFIC,
        ARM_GLOBAL,
        ARM_MESH,
        CHEST_PROXY,
        FLUID_PROXY,
        ENCHANT_PARTICLE_PROXY,
        PADDLE_PROXY,
        FISHING_LINE_PROXY;

    }

    private record AdjustmentResult(String property, float value) {
    }
}

