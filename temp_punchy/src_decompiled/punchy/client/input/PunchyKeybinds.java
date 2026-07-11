/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_2561
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package punchy.client.input;

import java.util.Locale;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_746;
import punchy.client.debug.PunchyDebug;
import punchy.client.debug.PunchyTuningIO;
import punchy.platform.Services;

public class PunchyKeybinds {
    private static final String CATEGORY = "key.categories.punchy.tuning";
    private static class_304 axisXKey;
    private static class_304 axisYKey;
    private static class_304 axisZKey;
    private static class_304 toggleModeKey;
    private static class_304 targetArmRightKey;
    private static class_304 targetArmLeftKey;
    private static class_304 targetItemRightKey;
    private static class_304 targetItemLeftKey;
    private static class_304 increaseKey;
    private static class_304 decreaseKey;
    private static class_304 resetKey;
    private static class_304 printKey;
    private static class_304 saveKey;
    private static class_304 togglePivotKey;
    private static Mode mode;
    private static Axis axis;
    private static Target target;

    public static void register() {
        axisXKey = new class_304("key.punchy.tuning.axis_x", 324, CATEGORY);
        axisYKey = new class_304("key.punchy.tuning.axis_y", 325, CATEGORY);
        axisZKey = new class_304("key.punchy.tuning.axis_z", 326, CATEGORY);
        toggleModeKey = new class_304("key.punchy.tuning.toggle_mode", 261, CATEGORY);
        targetArmRightKey = new class_304("key.punchy.tuning.target_arm_right", 327, CATEGORY);
        targetArmLeftKey = new class_304("key.punchy.tuning.target_arm_left", 328, CATEGORY);
        targetItemRightKey = new class_304("key.punchy.tuning.target_item_right", 329, CATEGORY);
        targetItemLeftKey = new class_304("key.punchy.tuning.target_item_left", 320, CATEGORY);
        increaseKey = new class_304("key.punchy.tuning.increase", 265, CATEGORY);
        decreaseKey = new class_304("key.punchy.tuning.decrease", 264, CATEGORY);
        resetKey = new class_304("key.punchy.tuning.reset", 82, CATEGORY);
        printKey = new class_304("key.punchy.tuning.print", 335, CATEGORY);
        saveKey = new class_304("key.punchy.tuning.save_session", 295, CATEGORY);
        togglePivotKey = new class_304("key.punchy.tuning.toggle_pivot", 296, CATEGORY);
        Services.CLIENT.registerKeyMapping(axisXKey);
        Services.CLIENT.registerKeyMapping(axisYKey);
        Services.CLIENT.registerKeyMapping(axisZKey);
        Services.CLIENT.registerKeyMapping(toggleModeKey);
        Services.CLIENT.registerKeyMapping(targetArmRightKey);
        Services.CLIENT.registerKeyMapping(targetArmLeftKey);
        Services.CLIENT.registerKeyMapping(targetItemRightKey);
        Services.CLIENT.registerKeyMapping(targetItemLeftKey);
        Services.CLIENT.registerKeyMapping(increaseKey);
        Services.CLIENT.registerKeyMapping(decreaseKey);
        Services.CLIENT.registerKeyMapping(resetKey);
        Services.CLIENT.registerKeyMapping(printKey);
        Services.CLIENT.registerKeyMapping(saveKey);
        Services.CLIENT.registerKeyMapping(togglePivotKey);
        Services.CLIENT.registerClientTickEnd(PunchyKeybinds::handleTick);
    }

    private static void handleTick(class_310 client) {
        boolean dec;
        if (client == null || client.field_1724 == null) {
            return;
        }
        if (!PunchyDebug.DEBUG_POSITION_ENABLED) {
            return;
        }
        if (client.field_1755 != null) {
            return;
        }
        class_746 player = client.field_1724;
        while (axisXKey.method_1436()) {
            axis = Axis.X;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.axis", (Object[])new Object[]{PunchyKeybinds.axisLabel(axis)}));
        }
        while (axisYKey.method_1436()) {
            axis = Axis.Y;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.axis", (Object[])new Object[]{PunchyKeybinds.axisLabel(axis)}));
        }
        while (axisZKey.method_1436()) {
            axis = Axis.Z;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.axis", (Object[])new Object[]{PunchyKeybinds.axisLabel(axis)}));
        }
        while (toggleModeKey.method_1436()) {
            mode = switch (mode.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Mode.ROTATE;
                case 1 -> Mode.SCALE;
                case 2 -> Mode.PIVOT;
                case 3 -> Mode.TRANSLATE;
            };
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.mode", (Object[])new Object[]{PunchyKeybinds.modeLabel(mode)}));
        }
        while (targetArmRightKey.method_1436()) {
            target = Target.ARM_RIGHT;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.target", (Object[])new Object[]{PunchyKeybinds.targetLabel(target)}));
        }
        while (targetArmLeftKey.method_1436()) {
            target = Target.ARM_LEFT;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.target", (Object[])new Object[]{PunchyKeybinds.targetLabel(target)}));
        }
        while (targetItemRightKey.method_1436()) {
            target = Target.ITEM_RIGHT;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.target", (Object[])new Object[]{PunchyKeybinds.targetLabel(target)}));
        }
        while (targetItemLeftKey.method_1436()) {
            target = Target.ITEM_LEFT;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.target", (Object[])new Object[]{PunchyKeybinds.targetLabel(target)}));
        }
        while (resetKey.method_1436()) {
            PunchyKeybinds.resetTarget();
            PunchyKeybinds.toast((class_1657)player, PunchyKeybinds.formatOffset());
        }
        boolean inc = increaseKey.method_1434() && !decreaseKey.method_1434();
        boolean bl = dec = decreaseKey.method_1434() && !increaseKey.method_1434();
        if (inc || dec) {
            float delta = PunchyKeybinds.computeStep(client, mode);
            if (dec) {
                delta *= -1.0f;
            }
            PunchyKeybinds.applyDelta(delta, mode);
            PunchyKeybinds.toast((class_1657)player, PunchyKeybinds.formatOffset());
        }
        while (printKey.method_1436()) {
            player.method_7353(PunchyKeybinds.formatOffset(), false);
        }
        while (saveKey.method_1436()) {
            PunchyTuningIO.saveSession();
            player.method_7353((class_2561)class_2561.method_43469((String)"punchy.tuning.toast.saved_session", (Object[])new Object[]{PunchyTuningIO.getSessionPath()}), false);
        }
        while (togglePivotKey.method_1436()) {
            PunchyDebug.SHOW_GRIP_PIVOT = !PunchyDebug.SHOW_GRIP_PIVOT;
            PunchyKeybinds.toast((class_1657)player, (class_2561)class_2561.method_43469((String)"punchy.tuning.toast.grip_pivot", (Object[])new Object[]{PunchyDebug.SHOW_GRIP_PIVOT ? class_2561.method_43471((String)"punchy.toggle.on") : class_2561.method_43471((String)"punchy.toggle.off")}));
        }
    }

    private static float computeStep(class_310 client, Mode mode) {
        float base;
        float f = base = mode == Mode.ROTATE ? 1.0f : 0.05f;
        if (mode == Mode.SCALE || mode == Mode.PIVOT) {
            base = 0.05f;
        }
        if (client.field_1690 != null) {
            if (client.field_1690.field_1867.method_1434()) {
                base *= 5.0f;
            }
            if (client.field_1690.field_1832.method_1434()) {
                base *= 0.2f;
            }
        }
        return base;
    }

    private static void applyDelta(float delta, Mode mode) {
        if (mode == Mode.SCALE) {
            PunchyKeybinds.addScale(delta);
            return;
        }
        if (mode == Mode.PIVOT) {
            PunchyKeybinds.addPivot(delta);
            return;
        }
        switch (axis.ordinal()) {
            case 0: {
                if (mode == Mode.ROTATE) {
                    PunchyKeybinds.addRotX(delta);
                    break;
                }
                PunchyKeybinds.addPosX(delta);
                break;
            }
            case 1: {
                if (mode == Mode.ROTATE) {
                    PunchyKeybinds.addRotY(delta);
                    break;
                }
                PunchyKeybinds.addPosY(delta);
                break;
            }
            case 2: {
                if (mode == Mode.ROTATE) {
                    PunchyKeybinds.addRotZ(delta);
                    break;
                }
                PunchyKeybinds.addPosZ(delta);
            }
        }
    }

    private static class_2561 formatOffset() {
        float posX = PunchyKeybinds.getPosX();
        float posY = PunchyKeybinds.getPosY();
        float posZ = PunchyKeybinds.getPosZ();
        float rotX = PunchyKeybinds.getRotX();
        float rotY = PunchyKeybinds.getRotY();
        float rotZ = PunchyKeybinds.getRotZ();
        float scale = PunchyKeybinds.getScale();
        float pivotX = PunchyKeybinds.getPivotX();
        float pivotY = PunchyKeybinds.getPivotY();
        float pivotZ = PunchyKeybinds.getPivotZ();
        String posXStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(posX));
        String posYStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(posY));
        String posZStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(posZ));
        String rotXStr = String.format(Locale.ROOT, "%.1f", Float.valueOf(rotX));
        String rotYStr = String.format(Locale.ROOT, "%.1f", Float.valueOf(rotY));
        String rotZStr = String.format(Locale.ROOT, "%.1f", Float.valueOf(rotZ));
        String scaleStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(scale));
        String pivotXStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(pivotX));
        String pivotYStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(pivotY));
        String pivotZStr = String.format(Locale.ROOT, "%.3f", Float.valueOf(pivotZ));
        return class_2561.method_43469((String)"punchy.tuning.offset_status", (Object[])new Object[]{PunchyKeybinds.targetLabel(target), PunchyKeybinds.modeLabel(mode), PunchyKeybinds.axisLabel(axis), posXStr, posYStr, posZStr, rotXStr, rotYStr, rotZStr, scaleStr, pivotXStr, pivotYStr, pivotZStr});
    }

    private static void resetTarget() {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_POS_X = 0.0f;
                PunchyDebug.ARM_R_POS_Y = 0.0f;
                PunchyDebug.ARM_R_POS_Z = 0.0f;
                PunchyDebug.ARM_R_ROT_X = 0.0f;
                PunchyDebug.ARM_R_ROT_Y = 0.0f;
                PunchyDebug.ARM_R_ROT_Z = 0.0f;
                PunchyDebug.ARM_R_SCALE_X = 1.0f;
                PunchyDebug.ARM_R_SCALE_Y = 1.0f;
                PunchyDebug.ARM_R_SCALE_Z = 1.0f;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_POS_X = 0.0f;
                PunchyDebug.ARM_L_POS_Y = 0.0f;
                PunchyDebug.ARM_L_POS_Z = 0.0f;
                PunchyDebug.ARM_L_ROT_X = 0.0f;
                PunchyDebug.ARM_L_ROT_Y = 0.0f;
                PunchyDebug.ARM_L_ROT_Z = 0.0f;
                PunchyDebug.ARM_L_SCALE_X = 1.0f;
                PunchyDebug.ARM_L_SCALE_Y = 1.0f;
                PunchyDebug.ARM_L_SCALE_Z = 1.0f;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_POS_X = 0.0f;
                PunchyDebug.ITEM_R_POS_Y = 0.0f;
                PunchyDebug.ITEM_R_POS_Z = 0.0f;
                PunchyDebug.ITEM_R_ROT_X = 0.0f;
                PunchyDebug.ITEM_R_ROT_Y = 0.0f;
                PunchyDebug.ITEM_R_ROT_Z = 0.0f;
                PunchyDebug.ITEM_R_SCALE_X = 1.0f;
                PunchyDebug.ITEM_R_SCALE_Y = 1.0f;
                PunchyDebug.ITEM_R_SCALE_Z = 1.0f;
                PunchyDebug.ITEM_R_PIVOT_X = 0.0f;
                PunchyDebug.ITEM_R_PIVOT_Y = 0.0f;
                PunchyDebug.ITEM_R_PIVOT_Z = 0.0f;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_POS_X = 0.0f;
                PunchyDebug.ITEM_L_POS_Y = 0.0f;
                PunchyDebug.ITEM_L_POS_Z = 0.0f;
                PunchyDebug.ITEM_L_ROT_X = 0.0f;
                PunchyDebug.ITEM_L_ROT_Y = 0.0f;
                PunchyDebug.ITEM_L_ROT_Z = 0.0f;
                PunchyDebug.ITEM_L_SCALE_X = 1.0f;
                PunchyDebug.ITEM_L_SCALE_Y = 1.0f;
                PunchyDebug.ITEM_L_SCALE_Z = 1.0f;
                PunchyDebug.ITEM_L_PIVOT_X = 0.0f;
                PunchyDebug.ITEM_L_PIVOT_Y = 0.0f;
                PunchyDebug.ITEM_L_PIVOT_Z = 0.0f;
            }
        }
    }

    private static float getPosX() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_POS_X;
            case 1 -> PunchyDebug.ARM_L_POS_X;
            case 2 -> PunchyDebug.ITEM_R_POS_X;
            case 3 -> PunchyDebug.ITEM_L_POS_X;
        };
    }

    private static float getPosY() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_POS_Y;
            case 1 -> PunchyDebug.ARM_L_POS_Y;
            case 2 -> PunchyDebug.ITEM_R_POS_Y;
            case 3 -> PunchyDebug.ITEM_L_POS_Y;
        };
    }

    private static float getPosZ() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_POS_Z;
            case 1 -> PunchyDebug.ARM_L_POS_Z;
            case 2 -> PunchyDebug.ITEM_R_POS_Z;
            case 3 -> PunchyDebug.ITEM_L_POS_Z;
        };
    }

    private static float getRotX() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_ROT_X;
            case 1 -> PunchyDebug.ARM_L_ROT_X;
            case 2 -> PunchyDebug.ITEM_R_ROT_X;
            case 3 -> PunchyDebug.ITEM_L_ROT_X;
        };
    }

    private static float getRotY() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_ROT_Y;
            case 1 -> PunchyDebug.ARM_L_ROT_Y;
            case 2 -> PunchyDebug.ITEM_R_ROT_Y;
            case 3 -> PunchyDebug.ITEM_L_ROT_Y;
        };
    }

    private static float getRotZ() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_ROT_Z;
            case 1 -> PunchyDebug.ARM_L_ROT_Z;
            case 2 -> PunchyDebug.ITEM_R_ROT_Z;
            case 3 -> PunchyDebug.ITEM_L_ROT_Z;
        };
    }

    private static float getScale() {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyDebug.ARM_R_SCALE_X;
            case 1 -> PunchyDebug.ARM_L_SCALE_X;
            case 2 -> PunchyDebug.ITEM_R_SCALE_X;
            case 3 -> PunchyDebug.ITEM_L_SCALE_X;
        };
    }

    private static float getPivotX() {
        return switch (target.ordinal()) {
            case 2 -> PunchyDebug.ITEM_R_PIVOT_X;
            case 3 -> PunchyDebug.ITEM_L_PIVOT_X;
            default -> 0.0f;
        };
    }

    private static float getPivotY() {
        return switch (target.ordinal()) {
            case 2 -> PunchyDebug.ITEM_R_PIVOT_Y;
            case 3 -> PunchyDebug.ITEM_L_PIVOT_Y;
            default -> 0.0f;
        };
    }

    private static float getPivotZ() {
        return switch (target.ordinal()) {
            case 2 -> PunchyDebug.ITEM_R_PIVOT_Z;
            case 3 -> PunchyDebug.ITEM_L_PIVOT_Z;
            default -> 0.0f;
        };
    }

    private static void addPosX(float delta) {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_POS_X += delta;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_POS_X += delta;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_POS_X += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_POS_X += delta;
            }
        }
    }

    private static void addPosY(float delta) {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_POS_Y += delta;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_POS_Y += delta;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_POS_Y += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_POS_Y += delta;
            }
        }
    }

    private static void addPosZ(float delta) {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_POS_Z += delta;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_POS_Z += delta;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_POS_Z += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_POS_Z += delta;
            }
        }
    }

    private static void addRotX(float delta) {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_ROT_X += delta;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_ROT_X += delta;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_ROT_X += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_ROT_X += delta;
            }
        }
    }

    private static void addRotY(float delta) {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_ROT_Y += delta;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_ROT_Y += delta;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_ROT_Y += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_ROT_Y += delta;
            }
        }
    }

    private static void addRotZ(float delta) {
        switch (target.ordinal()) {
            case 0: {
                PunchyDebug.ARM_R_ROT_Z += delta;
                break;
            }
            case 1: {
                PunchyDebug.ARM_L_ROT_Z += delta;
                break;
            }
            case 2: {
                PunchyDebug.ITEM_R_ROT_Z += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_ROT_Z += delta;
            }
        }
    }

    private static void addScale(float delta) {
        switch (target.ordinal()) {
            case 0: {
                float next;
                PunchyDebug.ARM_R_SCALE_X = next = PunchyKeybinds.clampScale(PunchyDebug.ARM_R_SCALE_X + delta);
                PunchyDebug.ARM_R_SCALE_Y = next;
                PunchyDebug.ARM_R_SCALE_Z = next;
                break;
            }
            case 1: {
                float next;
                PunchyDebug.ARM_L_SCALE_X = next = PunchyKeybinds.clampScale(PunchyDebug.ARM_L_SCALE_X + delta);
                PunchyDebug.ARM_L_SCALE_Y = next;
                PunchyDebug.ARM_L_SCALE_Z = next;
                break;
            }
            case 2: {
                float next;
                PunchyDebug.ITEM_R_SCALE_X = next = PunchyKeybinds.clampScale(PunchyDebug.ITEM_R_SCALE_X + delta);
                PunchyDebug.ITEM_R_SCALE_Y = next;
                PunchyDebug.ITEM_R_SCALE_Z = next;
                break;
            }
            case 3: {
                float next;
                PunchyDebug.ITEM_L_SCALE_X = next = PunchyKeybinds.clampScale(PunchyDebug.ITEM_L_SCALE_X + delta);
                PunchyDebug.ITEM_L_SCALE_Y = next;
                PunchyDebug.ITEM_L_SCALE_Z = next;
            }
        }
    }

    private static void addPivot(float delta) {
        switch (axis.ordinal()) {
            case 0: {
                PunchyKeybinds.addPivotX(delta);
                break;
            }
            case 1: {
                PunchyKeybinds.addPivotY(delta);
                break;
            }
            case 2: {
                PunchyKeybinds.addPivotZ(delta);
            }
        }
    }

    private static void addPivotX(float delta) {
        switch (target.ordinal()) {
            case 2: {
                PunchyDebug.ITEM_R_PIVOT_X += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_PIVOT_X += delta;
            }
        }
    }

    private static void addPivotY(float delta) {
        switch (target.ordinal()) {
            case 2: {
                PunchyDebug.ITEM_R_PIVOT_Y += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_PIVOT_Y += delta;
            }
        }
    }

    private static void addPivotZ(float delta) {
        switch (target.ordinal()) {
            case 2: {
                PunchyDebug.ITEM_R_PIVOT_Z += delta;
                break;
            }
            case 3: {
                PunchyDebug.ITEM_L_PIVOT_Z += delta;
            }
        }
    }

    private static float clampScale(float value) {
        return Math.max(0.01f, value);
    }

    private static void toast(class_1657 player, class_2561 message) {
        player.method_7353(message, true);
    }

    private static class_2561 axisLabel(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_2561.method_43471((String)"punchy.tuning.axis.x");
            case 1 -> class_2561.method_43471((String)"punchy.tuning.axis.y");
            case 2 -> class_2561.method_43471((String)"punchy.tuning.axis.z");
        };
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

    private static class_2561 targetLabel(Target target) {
        return switch (target.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_2561.method_43471((String)"punchy.tuning.target.arm_right");
            case 1 -> class_2561.method_43471((String)"punchy.tuning.target.arm_left");
            case 2 -> class_2561.method_43471((String)"punchy.tuning.target.item_right");
            case 3 -> class_2561.method_43471((String)"punchy.tuning.target.item_left");
        };
    }

    static {
        mode = Mode.TRANSLATE;
        axis = Axis.X;
        target = Target.ARM_RIGHT;
    }

    private static enum Axis {
        X,
        Y,
        Z;

    }

    private static enum Mode {
        TRANSLATE,
        ROTATE,
        SCALE,
        PIVOT;

    }

    private static enum Target {
        ARM_RIGHT,
        ARM_LEFT,
        ITEM_RIGHT,
        ITEM_LEFT;

    }
}

