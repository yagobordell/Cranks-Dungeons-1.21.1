/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_310
 */
package punchy.compat;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.class_1268;
import net.minecraft.class_310;
import punchy.client.animation.ItemAnimationManager;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.animation.data.AnimationClip;
import punchy.client.render.PunchyArmRenderer;
import punchy.config.PunchyConfig;
import punchy.platform.Services;

public final class FirstPersonModelCompat {
    private static final String MOD_ID = "firstperson";
    private static final String FPM_API_CLASS = "dev.tr7zw.firstperson.api.FirstPersonAPI";
    private static final String FPM_ACTIVATION_HANDLER = "dev.tr7zw.firstperson.api.ActivationHandler";
    private static boolean available;
    private static boolean checked;
    private static boolean active;
    private static boolean hidden;
    private static int transitionTicks;
    private static boolean pendingHide;
    private static boolean pendingShow;
    private static State state;
    private static boolean fpmLookupDone;
    private static Method fpmIsRenderingPlayer;
    private static boolean activationRegistered;
    private static boolean bodyRenderActive;

    private FirstPersonModelCompat() {
    }

    public static boolean isAvailable() {
        if (!checked) {
            checked = true;
            available = Services.PLATFORM != null && Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return available;
    }

    public static void tick(class_310 client) {
        float hidePitch;
        boolean wantsHide;
        boolean activeNow;
        if (client == null || client.field_1724 == null) {
            FirstPersonModelCompat.deactivate();
            return;
        }
        boolean bl = activeNow = FirstPersonModelCompat.isAvailable() && client.field_1690.method_31044().method_31034();
        if (!activeNow) {
            FirstPersonModelCompat.deactivate();
            return;
        }
        FirstPersonModelCompat.ensureActivationHandlerRegistered();
        active = true;
        if (!PunchyConfig.isFirstPersonModelHideEnabled()) {
            pendingHide = false;
            pendingShow = false;
            hidden = false;
            transitionTicks = 0;
            state = State.VISIBLE;
            bodyRenderActive = false;
            PunchyArmRenderer.setFirstPersonHidden(false);
            return;
        }
        if (PunchyConfig.isFirstPersonModelHideNever()) {
            pendingHide = false;
            pendingShow = false;
            hidden = false;
            transitionTicks = 0;
            state = State.VISIBLE;
            bodyRenderActive = true;
            PunchyArmRenderer.setFirstPersonHidden(false);
            return;
        }
        float pitch = client.field_1724.method_36455();
        boolean bl2 = wantsHide = pitch > (hidePitch = PunchyConfig.getFirstPersonModelHidePitch());
        if (wantsHide) {
            pendingHide = true;
            pendingShow = false;
            if ((state == State.VISIBLE || state == State.SHOWING) && FirstPersonModelCompat.beginHide(client)) {
                pendingHide = false;
            }
        } else {
            pendingShow = true;
            pendingHide = false;
            if ((state == State.HIDDEN || state == State.HIDING) && FirstPersonModelCompat.beginShow(client)) {
                pendingShow = false;
            }
        }
        FirstPersonModelCompat.advanceTransitions();
        PunchyArmRenderer.setFirstPersonHidden(hidden);
    }

    public static boolean shouldHideHands() {
        return hidden && active;
    }

    public static boolean shouldApplyThirdPersonAnimations(class_310 client) {
        if (client == null || client.field_1724 == null || client.field_1690 == null) {
            return false;
        }
        if (!FirstPersonModelCompat.isAvailable() || !client.field_1690.method_31044().method_31034()) {
            return false;
        }
        return state == State.HIDDEN || state == State.HIDING;
    }

    public static boolean isRenderingPlayer() {
        if (!FirstPersonModelCompat.isAvailable()) {
            return false;
        }
        if (!fpmLookupDone) {
            fpmLookupDone = true;
            try {
                Class<?> api = Class.forName(FPM_API_CLASS);
                fpmIsRenderingPlayer = api.getMethod("isRenderingPlayer", new Class[0]);
            }
            catch (Throwable ignored) {
                fpmIsRenderingPlayer = null;
            }
        }
        if (fpmIsRenderingPlayer == null) {
            return false;
        }
        try {
            Object value = fpmIsRenderingPlayer.invoke(null, new Object[0]);
            return value instanceof Boolean && (Boolean)value != false;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static void ensureActivationHandlerRegistered() {
        if (activationRegistered || !FirstPersonModelCompat.isAvailable()) {
            return;
        }
        try {
            Class<?> api = Class.forName(FPM_API_CLASS);
            Class<?> activationHandler = Class.forName(FPM_ACTIVATION_HANDLER);
            Object handler = Proxy.newProxyInstance(FirstPersonModelCompat.class.getClassLoader(), new Class[]{activationHandler}, (proxy, method, args) -> {
                if ("preventFirstperson".equals(method.getName())) {
                    return !FirstPersonModelCompat.shouldAllowBodyRender(class_310.method_1551());
                }
                return null;
            });
            api.getMethod("registerPlayerHandler", Object.class).invoke(null, handler);
            activationRegistered = true;
        }
        catch (Throwable ignored) {
            activationRegistered = false;
        }
    }

    private static boolean shouldAllowBodyRender(class_310 client) {
        float hidePitch;
        boolean wantsBody;
        if (client == null || client.field_1724 == null) {
            return false;
        }
        if (!PunchyConfig.isFirstPersonModelHideEnabled()) {
            bodyRenderActive = false;
            return false;
        }
        if (PunchyConfig.isFirstPersonModelHideNever()) {
            bodyRenderActive = true;
            return true;
        }
        float pitch = client.field_1724.method_36455();
        bodyRenderActive = wantsBody = pitch > (hidePitch = PunchyConfig.getFirstPersonModelHidePitch());
        return wantsBody;
    }

    private static void deactivate() {
        if (active && state != State.VISIBLE) {
            ItemAnimationManager.forceTypeOnce(class_1268.field_5808, "hand_in");
            ItemAnimationManager.forceTypeOnce(class_1268.field_5810, "hand_in");
        }
        active = false;
        hidden = false;
        transitionTicks = 0;
        pendingHide = false;
        pendingShow = false;
        bodyRenderActive = false;
        state = State.VISIBLE;
        PunchyArmRenderer.setFirstPersonHidden(false);
    }

    private static boolean beginHide(class_310 client) {
        hidden = true;
        state = State.HIDDEN;
        transitionTicks = 0;
        return true;
    }

    private static boolean beginShow(class_310 client) {
        if (!FirstPersonModelCompat.canPlayEquipClip()) {
            hidden = false;
            state = State.VISIBLE;
            transitionTicks = 0;
            return true;
        }
        ItemAnimationManager.forceTypeOnce(class_1268.field_5808, "hand_in");
        ItemAnimationManager.forceTypeOnce(class_1268.field_5810, "hand_in");
        FirstPersonModelCompat.playEquipClip(client, "hand_in");
        state = State.SHOWING;
        transitionTicks = 0;
        hidden = false;
        return true;
    }

    private static void advanceTransitions() {
        class_310 client;
        if (state == State.SHOWING) {
            ++transitionTicks;
            if (FirstPersonModelCompat.isPoseClipPlaying("hand_in")) {
                return;
            }
            if (transitionTicks <= 2) {
                return;
            }
            if (transitionTicks > 30 || !FirstPersonModelCompat.isPoseClipPlaying("hand_in")) {
                hidden = false;
                state = State.VISIBLE;
                transitionTicks = 0;
            }
        }
        if (state == State.VISIBLE && pendingHide) {
            class_310 client2 = class_310.method_1551();
            if (FirstPersonModelCompat.beginHide(client2)) {
                pendingHide = false;
            }
        } else if (state == State.HIDDEN && pendingShow && FirstPersonModelCompat.beginShow(client = class_310.method_1551())) {
            pendingShow = false;
        }
    }

    private static boolean canPlayEquipClip() {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null || !handler.isPlaying()) {
            return true;
        }
        AnimationClip current = handler.getCurrentClip();
        if (current == null) {
            return true;
        }
        return PunchyAnimationManager.isClipNamed(current, "hand_in") || PunchyAnimationManager.isClipNamed(current, "hand_out");
    }

    private static void playEquipClip(class_310 client, String name) {
        if (client == null || name == null || name.isBlank()) {
            return;
        }
        AnimationClip clip = PunchyAnimationManager.resolveHandedClip(name, client, class_1268.field_5808);
        if (clip == null) {
            AnimationClip animationClip = clip = "hand_in".equals(name) ? PunchyAnimationManager.HAND_IN_CLIP : PunchyAnimationManager.HAND_OUT_CLIP;
        }
        if (clip == null) {
            return;
        }
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        handler.setActiveArm(null);
        handler.setLooping(false);
        float speed = PunchyAnimationManager.clipSpeedMultiplier(clip);
        handler.play(clip, speed, true, false);
    }

    private static boolean isPoseClipPlaying(String name) {
        PoseHandler handler = PunchyAnimationManager.POSE_HANDLER;
        if (handler == null || !handler.isPlaying()) {
            return false;
        }
        AnimationClip clip = handler.getCurrentClip();
        return PunchyAnimationManager.isClipNamed(clip, name);
    }

    static {
        state = State.VISIBLE;
    }

    private static enum State {
        VISIBLE,
        HIDING,
        HIDDEN,
        SHOWING;

    }
}

