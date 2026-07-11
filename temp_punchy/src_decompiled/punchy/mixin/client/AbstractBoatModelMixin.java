/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1690
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_554
 *  net.minecraft.class_630
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
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
import net.minecraft.class_1657;
import net.minecraft.class_1690;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_554;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.render.BoatLidState;
import punchy.client.render.InHandLidPhysics;
import punchy.config.PunchyConfig;

@Mixin(value={class_554.class})
public class AbstractBoatModelMixin {
    @Shadow
    protected class_630 field_27396;
    @Shadow
    protected class_630 field_27397;
    @Unique
    private static final Map<Object, class_630> LID_CACHE = new IdentityHashMap<Object, class_630>();
    @Unique
    private static final String[] LID_NAMES = new String[]{"chest_lid", "lid", "chestlid", "chest_top", "chest", "top"};

    @Inject(method={"method_22952(Lnet/minecraft/class_1690;FFFFF)V"}, at={@At(value="TAIL")})
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
        this.field_27396.field_3654 += offset;
        this.field_27397.field_3654 += offset;
        class_630 chestRoot = AbstractBoatModelMixin.findChestRoot(this);
        if (chestRoot != null) {
            AbstractBoatModelMixin.setPartVisible(chestRoot, false);
            lidState.punchy$setChestPose(chestRoot.field_3657, chestRoot.field_3656, chestRoot.field_3655, chestRoot.field_3654, chestRoot.field_3675, chestRoot.field_3674);
            lidState.punchy$setChestPoseValid(true);
        } else {
            lidState.punchy$setChestPoseValid(false);
        }
        class_630 lid = AbstractBoatModelMixin.resolveLidPart(this);
        if (lid == null) {
            return;
        }
        float target = InHandLidPhysics.computeFallTarget((class_1657)class_310.method_1551().field_1724);
        float current = lidState.punchy$getLidOpen();
        long now = System.nanoTime();
        float next = InHandLidPhysics.updateLid(current, target, lidState.punchy$getLidLastUpdateNanos(), now);
        lidState.punchy$setLidOpen(next);
        lidState.punchy$setLidLastUpdateNanos(now);
        lidState.punchy$setLidApplied(true);
        lid.field_3654 = -next * 1.5707964f;
    }

    @Unique
    private static class_630 resolveLidPart(Object model) {
        String name;
        if (model == null) {
            return null;
        }
        class_630 cached = LID_CACHE.get(model);
        if (cached != null) {
            return cached;
        }
        class_630 lid = AbstractBoatModelMixin.findLidField(model);
        if (lid != null) {
            LID_CACHE.put(model, lid);
            return lid;
        }
        class_630 root = AbstractBoatModelMixin.findRootPart(model);
        if (root == null) {
            return null;
        }
        String[] stringArray = LID_NAMES;
        int n = stringArray.length;
        for (int i = 0; i < n && (lid = AbstractBoatModelMixin.findChildRecursive(root, name = stringArray[i])) == null; ++i) {
        }
        if (lid != null) {
            LID_CACHE.put(model, lid);
        }
        return lid;
    }

    @Unique
    private static class_630 findLidField(Object model) {
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                String fieldName;
                if (!class_630.class.isAssignableFrom(field.getType()) || !(fieldName = field.getName().toLowerCase(Locale.ROOT)).contains("lid") && !fieldName.contains("chest")) continue;
                try {
                    field.setAccessible(true);
                    class_630 part = (class_630)field.get(model);
                    if (part == null) continue;
                    if (fieldName.contains("lid")) {
                        return part;
                    }
                    for (String lidName : LID_NAMES) {
                        class_630 found = AbstractBoatModelMixin.findChildRecursive(part, lidName);
                        if (found == null) continue;
                        return found;
                    }
                }
                catch (IllegalAccessException illegalAccessException) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    @Unique
    private static class_630 findChestRoot(Object model) {
        class_630 root = AbstractBoatModelMixin.findRootPart(model);
        if (root == null) {
            return null;
        }
        Map<?, ?> children = AbstractBoatModelMixin.getChildren(root);
        if (children != null) {
            for (Map.Entry<?, ?> entry : children.entrySet()) {
                Object value;
                String key;
                String lower;
                Object keyObj = entry.getKey();
                if (!(keyObj instanceof String) || !(lower = (key = (String)keyObj).toLowerCase(Locale.ROOT)).contains("chest") || lower.contains("lid") || !((value = entry.getValue()) instanceof class_630)) continue;
                class_630 part = (class_630)value;
                return part;
            }
            for (Map.Entry<?, ?> entry : children.entrySet()) {
                class_630 part;
                Object value = entry.getValue();
                if (!(value instanceof class_630) || AbstractBoatModelMixin.findChildRecursive(part = (class_630)value, "lid") == null) continue;
                return part;
            }
        }
        return null;
    }

    @Unique
    private static void setPartVisible(class_630 part, boolean visible) {
        if (part == null) {
            return;
        }
        try {
            Field field = class_630.class.getDeclaredField("visible");
            field.setAccessible(true);
            field.setBoolean(part, visible);
            return;
        }
        catch (Throwable field) {
            try {
                Field field2 = class_630.class.getDeclaredField("skipDraw");
                field2.setAccessible(true);
                field2.setBoolean(part, !visible);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
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
        Map<?, ?> children = AbstractBoatModelMixin.getChildren(root);
        if (children == null || children.isEmpty()) {
            return null;
        }
        for (Map.Entry<?, ?> entry : children.entrySet()) {
            Object value;
            String key;
            Object keyObj = entry.getKey();
            if (!(keyObj instanceof String) || !(key = (String)keyObj).toLowerCase(Locale.ROOT).contains(name) || !((value = entry.getValue()) instanceof class_630)) continue;
            class_630 part = (class_630)value;
            return part;
        }
        for (Map.Entry<Object, Object> value : children.values()) {
            class_630 child;
            class_630 found;
            if (!(value instanceof class_630) || (found = AbstractBoatModelMixin.findChildRecursive(child = (class_630)value, name)) == null) continue;
            return found;
        }
        return null;
    }

    @Unique
    private static Map<?, ?> getChildren(class_630 part) {
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

