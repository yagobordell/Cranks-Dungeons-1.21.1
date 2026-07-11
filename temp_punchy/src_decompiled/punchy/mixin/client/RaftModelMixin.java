/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1690
 *  net.minecraft.class_3532
 *  net.minecraft.class_630
 *  net.minecraft.class_7754
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1690;
import net.minecraft.class_3532;
import net.minecraft.class_630;
import net.minecraft.class_7754;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.render.BoatLidState;
import punchy.config.PunchyConfig;

@Mixin(value={class_7754.class})
public class RaftModelMixin {
    @Unique
    private static final Map<Object, class_630> LEFT_PADDLE_CACHE = new IdentityHashMap<Object, class_630>();
    @Unique
    private static final Map<Object, class_630> RIGHT_PADDLE_CACHE = new IdentityHashMap<Object, class_630>();
    @Unique
    private static final String[] LEFT_PADDLE_NAMES = new String[]{"left_paddle", "paddle_left", "leftpaddle", "paddleleft"};
    @Unique
    private static final String[] RIGHT_PADDLE_NAMES = new String[]{"right_paddle", "paddle_right", "rightpaddle", "paddleright"};

    @Inject(method={"method_45711(Lnet/minecraft/class_1690;FFFFF)V"}, at={@At(value="TAIL")})
    private void punchy$applyPaddlePhysics(class_1690 boat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        BoatLidState lidState;
        if (PunchyConfig.disableBoatFirstPersonAnimations()) {
            return;
        }
        if (!(boat instanceof BoatLidState) || !(lidState = (BoatLidState)boat).punchy$isInHand()) {
            return;
        }
        double vy = lidState.punchy$getVy();
        float fall = vy < -0.01 ? (float)(-vy) * 1.275f : 0.0f;
        float drift = 0.0f;
        float offset = class_3532.method_15363((float)(fall + drift), (float)-1.275f, (float)1.275f);
        class_630 leftPaddle = RaftModelMixin.resolvePaddle(this, true);
        class_630 rightPaddle = RaftModelMixin.resolvePaddle(this, false);
        if (leftPaddle != null) {
            leftPaddle.field_3654 += offset;
        }
        if (rightPaddle != null) {
            rightPaddle.field_3654 += offset;
        }
    }

    @Unique
    private static class_630 resolvePaddle(Object model, boolean left) {
        String[] names;
        if (model == null) {
            return null;
        }
        Map<Object, class_630> cache = left ? LEFT_PADDLE_CACHE : RIGHT_PADDLE_CACHE;
        class_630 cached = cache.get(model);
        if (cached != null) {
            return cached;
        }
        String key = left ? "left" : "right";
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                String fieldName;
                if (!class_630.class.isAssignableFrom(field.getType()) || !(fieldName = field.getName().toLowerCase(Locale.ROOT)).contains("paddle") || !fieldName.contains(key)) continue;
                try {
                    field.setAccessible(true);
                    class_630 part = (class_630)field.get(model);
                    if (part == null) continue;
                    cache.put(model, part);
                    return part;
                }
                catch (IllegalAccessException part) {
                    // empty catch block
                }
            }
        }
        class_630 root = RaftModelMixin.findRootPart(model);
        if (root == null) {
            return null;
        }
        for (String name : names = left ? LEFT_PADDLE_NAMES : RIGHT_PADDLE_NAMES) {
            class_630 part = RaftModelMixin.findChildRecursive(root, name);
            if (part == null) continue;
            cache.put(model, part);
            return part;
        }
        return null;
    }

    @Unique
    private static class_630 findRootPart(Object model) {
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Method method = type.getDeclaredMethod("root", new Class[0]);
                if (class_630.class.isAssignableFrom(method.getReturnType())) {
                    method.setAccessible(true);
                    return (class_630)method.invoke(model, new Object[0]);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            for (Field field : type.getDeclaredFields()) {
                if (!class_630.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    return (class_630)field.get(model);
                }
                catch (IllegalAccessException illegalAccessException) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    @Unique
    private static class_630 findChildRecursive(class_630 root, String name) {
        if (root == null || name == null) {
            return null;
        }
        try {
            class_630 direct = root.method_32086(name);
            if (direct != null) {
                return direct;
            }
        }
        catch (Throwable direct) {
            // empty catch block
        }
        Map<String, class_630> children = RaftModelMixin.getChildren(root);
        if (children == null || children.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, class_630> entry : children.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.toLowerCase(Locale.ROOT).contains(name)) continue;
            return entry.getValue();
        }
        for (class_630 child : children.values()) {
            class_630 found = RaftModelMixin.findChildRecursive(child, name);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    @Unique
    private static Map<String, class_630> getChildren(class_630 part) {
        for (Class<?> type = part.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!Map.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(part);
                    if (!(value instanceof Map)) continue;
                    Map map = (Map)value;
                    return map;
                }
                catch (IllegalAccessException illegalAccessException) {
                    // empty catch block
                }
            }
        }
        return null;
    }
}

