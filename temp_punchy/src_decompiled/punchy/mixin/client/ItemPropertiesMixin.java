/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1753
 *  net.minecraft.class_1764
 *  net.minecraft.class_1787
 *  net.minecraft.class_1799
 *  net.minecraft.class_1800
 *  net.minecraft.class_1819
 *  net.minecraft.class_1835
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_5272
 *  net.minecraft.class_5498
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package punchy.mixin.client;

import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1800;
import net.minecraft.class_1819;
import net.minecraft.class_1835;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5272;
import net.minecraft.class_5498;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.UseAnimationSuppressor;
import punchy.config.PunchyConfig;
import punchy.mixin.client.accessor.PlayerFishingAccessor;

@Mixin(value={class_5272.class})
public class ItemPropertiesMixin {
    private static final Set<String> SUPPRESSED_USE_PROPERTIES = Set.of("pulling", "charged", "firework", "throwing", "blocking", "cast", "brushing");

    @Inject(method={"method_27878(Lnet/minecraft/class_1799;Lnet/minecraft/class_2960;)Lnet/minecraft/class_1800;"}, at={@At(value="RETURN")}, cancellable=true)
    private static void punchy$wrapUseProperties(class_1799 stack, class_2960 id, CallbackInfoReturnable<class_1800> cir) {
        class_1800 original = (class_1800)cir.getReturnValue();
        if (original == null || id == null) {
            return;
        }
        if (!ItemPropertiesMixin.isUseRelatedProperty(id)) {
            return;
        }
        String path = id.method_12832();
        class_1800 wrapped = (valueStack, level, entity, seed) -> ItemPropertiesMixin.handleUseProperty(path, original, valueStack, level, entity, seed);
        cir.setReturnValue((Object)wrapped);
    }

    private static float handleUseProperty(String path, class_1800 original, class_1799 stack, class_638 level, class_1309 entity, int seed) {
        if (original == null) {
            return 0.0f;
        }
        if (!ItemPropertiesMixin.shouldSkipBlacklist(stack, entity)) {
            Float override;
            if ("use_duration".equals(path) && (override = ItemPropertiesMixin.computeUseDuration(stack, level, entity)) != null) {
                return override.floatValue();
            }
            if ("use_blink".equals(path) && (override = ItemPropertiesMixin.computeUseBlink(stack, level, entity)) != null) {
                return override.floatValue();
            }
            if ("pull".equals(path)) {
                if (stack != null && stack.method_7909() instanceof class_1753 && (override = ItemPropertiesMixin.computeBowPull(stack, level, entity)) != null) {
                    return override.floatValue();
                }
                if (stack != null && stack.method_7909() instanceof class_1764 && (override = ItemPropertiesMixin.computeCrossbowPull(stack, level, entity)) != null) {
                    return override.floatValue();
                }
            }
            if ("pulling".equals(path) && stack != null && (stack.method_7909() instanceof class_1753 || stack.method_7909() instanceof class_1764) && (override = ItemPropertiesMixin.computePulling(stack, entity)) != null) {
                return override.floatValue();
            }
            if ("cast".equals(path) && stack != null && stack.method_7909() instanceof class_1787) {
                float orig = original.call(stack, level, entity, seed);
                if (orig > 0.0f) {
                    return orig;
                }
                Float override2 = ItemPropertiesMixin.computeFishingRodCast(stack, entity);
                if (override2 != null) {
                    return override2.floatValue();
                }
            }
            if (SUPPRESSED_USE_PROPERTIES.contains(path) && ItemPropertiesMixin.shouldSuppress(stack, entity)) {
                if ("cast".equals(path) && stack != null && stack.method_7909() instanceof class_1787) {
                    return original.call(stack, level, entity, seed);
                }
                return 0.0f;
            }
        }
        return original.call(stack, level, entity, seed);
    }

    private static boolean isUseRelatedProperty(class_2960 id) {
        String path = id.method_12832();
        return "use_duration".equals(path) || "use_blink".equals(path) || "pull".equals(path) || SUPPRESSED_USE_PROPERTIES.contains(path);
    }

    private static Float computeFishingRodCast(class_1799 stack, class_1309 entity) {
        if (stack == null || stack.method_7960() || !(stack.method_7909() instanceof class_1787)) {
            return null;
        }
        if (!(entity instanceof class_1657)) {
            return null;
        }
        class_1657 player = (class_1657)entity;
        if (((PlayerFishingAccessor)player).punchy$getFishing() == null) {
            return Float.valueOf(0.0f);
        }
        class_1799 main = player.method_6047();
        if (!main.method_7960() && class_1799.method_31577((class_1799)main, (class_1799)stack)) {
            return Float.valueOf(1.0f);
        }
        class_1799 off = player.method_6079();
        if (!off.method_7960() && class_1799.method_31577((class_1799)off, (class_1799)stack)) {
            return Float.valueOf(1.0f);
        }
        return Float.valueOf(0.0f);
    }

    private static Float computePulling(class_1799 stack, class_1309 entity) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        if (entity == null) {
            return Float.valueOf(0.0f);
        }
        if (ItemPropertiesMixin.shouldSuppress(stack, entity)) {
            return Float.valueOf(0.0f);
        }
        if (!entity.method_6115()) {
            return Float.valueOf(0.0f);
        }
        class_1799 useStack = entity.method_6030();
        if (useStack == null || useStack.method_7960() || useStack.method_7909() != stack.method_7909()) {
            return Float.valueOf(0.0f);
        }
        return Float.valueOf(1.0f);
    }

    private static Float computeBowPull(class_1799 stack, class_638 level, class_1309 entity) {
        if (stack == null || stack.method_7960() || !(stack.method_7909() instanceof class_1753)) {
            return null;
        }
        if (entity == null) {
            return Float.valueOf(0.0f);
        }
        if (ItemPropertiesMixin.shouldSuppress(stack, entity)) {
            return Float.valueOf(0.0f);
        }
        if (!(entity instanceof class_1657)) {
            return null;
        }
        class_1657 player = (class_1657)entity;
        if (!player.method_6115()) {
            UseAnimationSuppressor.clearReleaseStart();
            UseAnimationSuppressor.clearMeshStart();
            return Float.valueOf(0.0f);
        }
        if (!ItemPropertiesMixin.matchesUseStackLoose(player, stack)) {
            return Float.valueOf(0.0f);
        }
        class_1268 hand = ItemPropertiesMixin.resolveUseHand(player, stack);
        if (hand == null) {
            return Float.valueOf(0.0f);
        }
        int remaining = ItemPropertiesMixin.resolveRemainingTicks(player, level, hand);
        int startRemaining = UseAnimationSuppressor.getReleaseStartRemainingTicks(hand);
        long startTick = UseAnimationSuppressor.getReleaseStartTick(hand);
        int elapsed = startTick >= 0L && startRemaining >= 0 ? Math.max(0, startRemaining - remaining) : stack.method_7935((class_1309)player) - remaining;
        return Float.valueOf((float)elapsed / 20.0f);
    }

    private static Float computeCrossbowPull(class_1799 stack, class_638 level, class_1309 entity) {
        if (ItemPropertiesMixin.shouldSkipBlacklist(stack, entity)) {
            return null;
        }
        if (ItemPropertiesMixin.shouldSuppress(stack, entity)) {
            return Float.valueOf(0.0f);
        }
        if (!(entity instanceof class_1657)) {
            return null;
        }
        class_1657 player = (class_1657)entity;
        if (!player.method_6115()) {
            UseAnimationSuppressor.clearReleaseStart();
            UseAnimationSuppressor.clearMeshStart();
            return null;
        }
        if (!ItemPropertiesMixin.matchesUseStackLoose(player, stack)) {
            return null;
        }
        if (class_1764.method_7781((class_1799)stack)) {
            return Float.valueOf(0.0f);
        }
        int chargeDuration = class_1764.method_7775((class_1799)stack, (class_1309)player);
        if (chargeDuration <= 0) {
            return Float.valueOf(0.0f);
        }
        class_1268 hand = ItemPropertiesMixin.resolveUseHand(player, stack);
        if (hand == null) {
            return Float.valueOf(0.0f);
        }
        int remaining = ItemPropertiesMixin.resolveRemainingTicks(player, level, hand);
        int totalUse = stack.method_7935((class_1309)player);
        int endRemaining = Math.max(0, totalUse - chargeDuration);
        int meshStartRemaining = UseAnimationSuppressor.getMeshStartRemainingTicks(hand);
        if (meshStartRemaining >= 0 && meshStartRemaining <= totalUse && meshStartRemaining >= endRemaining) {
            int window = Math.max(1, meshStartRemaining - endRemaining);
            int shownElapsed = Math.max(0, meshStartRemaining - remaining);
            float progress = (float)shownElapsed / (float)window;
            if (progress < 0.0f) {
                progress = 0.0f;
            }
            if (progress > 1.0f) {
                progress = 1.0f;
            }
            return Float.valueOf(progress);
        }
        int startRemaining = UseAnimationSuppressor.getReleaseStartRemainingTicks(hand);
        long startTick = UseAnimationSuppressor.getReleaseStartTick(hand);
        if (startTick >= 0L && startRemaining >= 0 && startRemaining <= totalUse && startRemaining >= endRemaining) {
            int window = Math.max(1, startRemaining - endRemaining);
            int shownElapsed = Math.max(0, startRemaining - remaining);
            float progress = (float)shownElapsed / (float)window;
            if (progress < 0.0f) {
                progress = 0.0f;
            }
            if (progress > 1.0f) {
                progress = 1.0f;
            }
            return Float.valueOf(progress);
        }
        int elapsed = totalUse - remaining;
        return Float.valueOf((float)elapsed / (float)chargeDuration);
    }

    private static Float computeUseDuration(class_1799 stack, class_638 level, class_1309 entity) {
        if (ItemPropertiesMixin.shouldSkipBlacklist(stack, entity)) {
            return null;
        }
        if (ItemPropertiesMixin.shouldGateUseDuration(entity)) {
            return Float.valueOf(0.0f);
        }
        if (ItemPropertiesMixin.shouldSuppress(stack, entity)) {
            return Float.valueOf(0.0f);
        }
        if (!(entity instanceof class_1657)) {
            return null;
        }
        class_1657 player = (class_1657)entity;
        if (!player.method_6115()) {
            UseAnimationSuppressor.clearReleaseStart();
            UseAnimationSuppressor.clearMeshStart();
            return null;
        }
        if (!ItemPropertiesMixin.matchesUseStack(player, stack)) {
            return null;
        }
        class_1268 hand = ItemPropertiesMixin.resolveUseHand(player, stack);
        if (hand == null) {
            return null;
        }
        int remaining = ItemPropertiesMixin.resolveRemainingTicks(player, level, hand);
        int elapsed = stack.method_7935((class_1309)player) - remaining;
        return Float.valueOf((float)elapsed / 20.0f);
    }

    private static Float computeUseBlink(class_1799 stack, class_638 level, class_1309 entity) {
        if (ItemPropertiesMixin.shouldSkipBlacklist(stack, entity)) {
            return null;
        }
        if (ItemPropertiesMixin.shouldSuppress(stack, entity)) {
            return Float.valueOf(0.0f);
        }
        if (!(entity instanceof class_1657)) {
            return null;
        }
        class_1657 player = (class_1657)entity;
        if (!player.method_6115()) {
            UseAnimationSuppressor.clearReleaseStart();
            UseAnimationSuppressor.clearMeshStart();
            return null;
        }
        if (!ItemPropertiesMixin.matchesUseStack(player, stack)) {
            return null;
        }
        class_1268 hand = ItemPropertiesMixin.resolveUseHand(player, stack);
        if (hand == null) {
            return null;
        }
        int remaining = ItemPropertiesMixin.resolveRemainingTicks(player, level, hand);
        return Float.valueOf((float)(remaining % 10) / 10.0f);
    }

    private static boolean shouldSuppress(class_1799 stack, class_1309 entity) {
        class_1657 player;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (ItemPropertiesMixin.isUsingBlacklistedItem(entity)) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null || mc.field_1690.method_31044() != class_5498.field_26664) {
            return false;
        }
        if (!(entity instanceof class_1657) || mc.field_1724 != (player = (class_1657)entity)) {
            return false;
        }
        if (!player.method_6115() || !ItemPropertiesMixin.matchesUseStackLoose(player, stack)) {
            return false;
        }
        if (ItemPropertiesMixin.shouldPreserveUseProperties(stack)) {
            return false;
        }
        class_1268 hand = player.method_6058();
        if (hand == null) {
            return false;
        }
        if (UseAnimationSuppressor.getReleaseStartTick() < 0L && !UseAnimationSuppressor.isSuppressed()) {
            AnimationClip clip;
            if (stack.method_7909() instanceof class_1753) {
                AnimationClip clip2 = PunchyAnimationManager.resolveHandedClip("use_bow", mc, hand);
                if (clip2 != null && (clip2.hasEffectAny("start_mesh_animation") || clip2.hasEffectAny("loadstart") || clip2.hasEffectAny("load_start"))) {
                    UseAnimationSuppressor.suppress(hand);
                }
            } else if (stack.method_7909() instanceof class_1764 && !class_1764.method_7781((class_1799)stack) && (clip = PunchyAnimationManager.resolveHandedClip("use_crossbow", mc, hand)) != null && (clip.hasEffectAny("start_mesh_animation") || clip.hasEffectAny("loadstart") || clip.hasEffectAny("load_start") || clip.hasEffectAny("suppress_mesh_animation"))) {
                UseAnimationSuppressor.suppress(hand);
            }
        }
        if (ItemPropertiesMixin.shouldGateUseDuration(entity) || ItemPropertiesMixin.shouldGateCrossbowCharge(stack, entity)) {
            return true;
        }
        return UseAnimationSuppressor.isSuppressed(hand) || UseAnimationSuppressor.isForced();
    }

    private static boolean shouldPreserveUseProperties(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (stack.method_7909() instanceof class_1819 || stack.method_7909() instanceof class_1835) {
            return false;
        }
        if (UseAnimationSuppressor.isForced()) {
            return false;
        }
        return !(stack.method_7909() instanceof class_1753) && !(stack.method_7909() instanceof class_1764);
    }

    private static boolean shouldGateUseDuration(class_1309 entity) {
        boolean hasTrigger;
        class_1657 player;
        if (ItemPropertiesMixin.isUsingBlacklistedItem(entity)) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null || mc.field_1690.method_31044() != class_5498.field_26664) {
            return false;
        }
        if (!(entity instanceof class_1657) || mc.field_1724 != (player = (class_1657)entity)) {
            return false;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return false;
        }
        if (!PunchyAnimationManager.isClipNamed(clip, "use_bow")) {
            return false;
        }
        boolean bl = hasTrigger = clip.hasEffectAny("start_mesh_animation") || clip.hasEffectAny("loadstart") || clip.hasEffectAny("load_start");
        if (!hasTrigger) {
            return false;
        }
        float time = PunchyAnimationManager.POSE_HANDLER.getCurrentTime();
        boolean started = clip.isEffectActive("start_mesh_animation", time) || clip.isEffectActive("loadstart", time) || clip.isEffectActive("load_start", time);
        return !started;
    }

    private static boolean shouldGateCrossbowCharge(class_1799 stack, class_1309 entity) {
        class_1657 player;
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (!(stack.method_7909() instanceof class_1764)) {
            return false;
        }
        if (class_1764.method_7781((class_1799)stack)) {
            return false;
        }
        if (!(entity instanceof class_1657) || !(player = (class_1657)entity).method_6115()) {
            return false;
        }
        class_1268 hand = ItemPropertiesMixin.resolveUseHand(player, stack);
        return hand != null && UseAnimationSuppressor.isSuppressed(hand) && UseAnimationSuppressor.getReleaseStartTick(hand) < 0L;
    }

    private static boolean matchesUseStack(class_1657 player, class_1799 stack) {
        if (player == null || stack == null || stack.method_7960()) {
            return false;
        }
        class_1799 useStack = player.method_6030();
        if (useStack == null || useStack.method_7960()) {
            return false;
        }
        return class_1799.method_31577((class_1799)useStack, (class_1799)stack);
    }

    private static boolean matchesUseStackLoose(class_1657 player, class_1799 stack) {
        if (player == null || stack == null || stack.method_7960()) {
            return false;
        }
        class_1799 useStack = player.method_6030();
        if (useStack == null || useStack.method_7960()) {
            return false;
        }
        return useStack.method_7909() == stack.method_7909();
    }

    private static boolean shouldSkipBlacklist(class_1799 stack, class_1309 entity) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (!(entity instanceof class_1657)) {
            return false;
        }
        class_1657 player = (class_1657)entity;
        if (!PunchyConfig.isItemBlacklisted(stack)) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null || mc.field_1690.method_31044() != class_5498.field_26664) {
            return false;
        }
        return mc.field_1724 == player;
    }

    private static boolean isUsingBlacklistedItem(class_1309 entity) {
        if (!(entity instanceof class_1657)) {
            return false;
        }
        class_1657 player = (class_1657)entity;
        if (!player.method_6115()) {
            return false;
        }
        class_1799 useStack = player.method_6030();
        return PunchyConfig.isItemBlacklisted(useStack);
    }

    private static class_1268 resolveUseHand(class_1657 player, class_1799 stack) {
        if (player == null || stack == null || stack.method_7960() || !player.method_6115()) {
            return null;
        }
        class_1799 useStack = player.method_6030();
        if (useStack == null || useStack.method_7960()) {
            return null;
        }
        if (class_1799.method_31577((class_1799)useStack, (class_1799)stack) || useStack.method_7909() == stack.method_7909()) {
            return player.method_6058();
        }
        return null;
    }

    private static int resolveRemainingTicks(class_1657 player, class_638 level, class_1268 hand) {
        if (player == null) {
            return 0;
        }
        long start = UseAnimationSuppressor.getReleaseStartTick(hand);
        if (start >= 0L && level != null) {
            long elapsed = Math.max(0L, level.method_8510() - start);
            int startRemaining = UseAnimationSuppressor.getReleaseStartRemainingTicks(hand);
            if (startRemaining >= 0) {
                return Math.max(0, startRemaining - (int)elapsed);
            }
        }
        return player.method_6014();
    }
}

