/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_5498
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package punchy.mixin.client;

import java.lang.reflect.Method;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_5498;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.UseAnimationSuppressor;
import punchy.config.PunchyConfig;

@Pseudo
@Mixin(targets={"timmychips/modefiteitemdefinitions/property/resolver/rangeentry/UseDurationFloat"}, remap=false)
public class ModefiteUseDurationMixin {
    @Inject(method={"getValue"}, at={@At(value="HEAD")}, cancellable=true)
    private void punchy$gateUseDuration(class_1799 stack, class_1309 entity, @Coerce Object definition, CallbackInfoReturnable<Float> cir) {
        if (ModefiteUseDurationMixin.shouldGateUseDuration(stack, entity)) {
            cir.setReturnValue((Object)Float.valueOf(0.0f));
            return;
        }
        Float adjusted = ModefiteUseDurationMixin.resolveMeshStartValue(stack, entity, definition);
        if (adjusted != null) {
            cir.setReturnValue((Object)adjusted);
        }
    }

    private static boolean shouldGateUseDuration(class_1799 stack, class_1309 entity) {
        boolean hasTrigger;
        class_1657 player;
        if (stack != null && PunchyConfig.isItemBlacklisted(stack)) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null || mc.field_1690.method_31044() != class_5498.field_26664) {
            return false;
        }
        if (!(entity instanceof class_1657) || mc.field_1724 != (player = (class_1657)entity)) {
            return false;
        }
        if (!player.method_6115()) {
            return false;
        }
        class_1799 useStack = player.method_6030();
        if (stack == null || stack.method_7960() || useStack == null || useStack.method_7960()) {
            return false;
        }
        if (!class_1799.method_31577((class_1799)stack, (class_1799)useStack)) {
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

    private static Float resolveMeshStartValue(class_1799 stack, class_1309 entity, Object definition) {
        boolean hasTrigger;
        class_1657 player;
        if (stack != null && PunchyConfig.isItemBlacklisted(stack)) {
            return null;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1690 == null || mc.field_1690.method_31044() != class_5498.field_26664) {
            return null;
        }
        if (!(entity instanceof class_1657) || mc.field_1724 != (player = (class_1657)entity)) {
            return null;
        }
        if (!player.method_6115()) {
            return null;
        }
        class_1799 useStack = player.method_6030();
        if (stack == null || stack.method_7960() || useStack == null || useStack.method_7960()) {
            return null;
        }
        if (!class_1799.method_31577((class_1799)stack, (class_1799)useStack)) {
            return null;
        }
        AnimationClip clip = PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
        if (clip == null || !PunchyAnimationManager.POSE_HANDLER.isPlaying()) {
            return null;
        }
        if (!PunchyAnimationManager.isClipNamed(clip, "use_bow")) {
            return null;
        }
        boolean bl = hasTrigger = clip.hasEffectAny("start_mesh_animation") || clip.hasEffectAny("loadstart") || clip.hasEffectAny("load_start");
        if (!hasTrigger) {
            return null;
        }
        int startRemaining = UseAnimationSuppressor.getMeshStartRemainingTicks(player.method_6058());
        if (startRemaining < 0) {
            return null;
        }
        int remaining = player.method_6014();
        int elapsed = Math.max(0, startRemaining - remaining);
        boolean useRemaining = ModefiteUseDurationMixin.resolveUseRemaining(definition);
        return Float.valueOf(useRemaining ? (float)remaining : (float)elapsed);
    }

    private static boolean resolveUseRemaining(Object definition) {
        if (definition == null) {
            return false;
        }
        try {
            Method method = definition.getClass().getMethod("useRemaining", new Class[0]);
            Object value = method.invoke(definition, new Object[0]);
            return Boolean.TRUE.equals(value);
        }
        catch (Exception ignored) {
            return false;
        }
    }
}

