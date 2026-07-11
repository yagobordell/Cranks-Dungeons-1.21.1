/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1819
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_746
 *  net.minecraft.class_7833
 */
package punchy.client.render;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1819;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_7833;

public class ShieldImpactPhysics {
    private static final ShieldImpactPhysics INSTANCE = new ShieldImpactPhysics();
    private static final int IMPACT_TICKS = 5;
    private static final float IMPACT_ROT_X = -18.0f;
    private static final float IMPACT_ROT_Z = 4.0f;
    private static final float IMPACT_OFF_Z = -0.12f;
    private static final float IMPACT_OFF_Y = -0.02f;
    private long startTick = -1L;
    private class_1306 impactArm = class_1306.field_6183;
    private float jitterPhase = 0.0f;

    public static ShieldImpactPhysics get() {
        return INSTANCE;
    }

    public void trigger(class_746 player) {
        class_1306 arm = ShieldImpactPhysics.resolveImpactArm(player);
        if (arm == null) {
            return;
        }
        this.trigger(arm);
    }

    public void trigger(class_1306 arm) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return;
        }
        this.startTick = mc.field_1687.method_8510();
        this.impactArm = arm;
        this.jitterPhase = (float)(Math.random() * Math.PI * 2.0);
    }

    public static class_1306 resolveImpactArm(class_746 player) {
        if (player == null) {
            return null;
        }
        class_1306 arm = player.method_6068();
        if (player.method_6079().method_7909() instanceof class_1819) {
            arm = arm == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
        } else if (player.method_6047().method_7909() instanceof class_1819) {
            arm = player.method_6068();
        } else if (player.method_6115() && player.method_6030().method_7909() instanceof class_1819) {
            boolean right;
            class_1268 hand = player.method_6058();
            boolean bl = right = player.method_6068() == class_1306.field_6183;
            arm = hand == class_1268.field_5808 ? (right ? class_1306.field_6183 : class_1306.field_6182) : (right ? class_1306.field_6182 : class_1306.field_6183);
        } else {
            return null;
        }
        return arm;
    }

    public void applyTo(class_4587 poseStack, class_1306 arm, float partialTicks) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || this.startTick < 0L || arm != this.impactArm) {
            return;
        }
        long now = mc.field_1687.method_8510();
        long elapsed = now - this.startTick;
        if (elapsed < 0L) {
            elapsed = 0L;
        }
        if (elapsed >= 5L) {
            return;
        }
        float t = ((float)elapsed + partialTicks) / 5.0f;
        if (t > 1.0f) {
            t = 1.0f;
        }
        float impact = t < 0.4f ? t / 0.4f : 1.0f - (t - 0.4f) / 0.6f;
        impact = impact * impact * (3.0f - 2.0f * impact);
        float wobble = 1.0f + 0.05f * (float)Math.sin(this.jitterPhase + t * (float)Math.PI * 2.0f);
        float strength = impact * wobble;
        float sign = arm == class_1306.field_6182 ? -1.0f : 1.0f;
        float rotX = -18.0f * strength;
        float rotZ = 4.0f * strength * sign;
        float offZ = -0.12f * strength;
        float offY = -0.02f * strength;
        poseStack.method_46416(0.0f, offY, offZ);
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(rotX));
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
    }
}

