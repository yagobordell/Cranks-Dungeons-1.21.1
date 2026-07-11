/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1306
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_4587
 *  net.minecraft.class_746
 *  net.minecraft.class_7833
 *  net.minecraft.class_9362
 *  org.joml.Matrix4f
 */
package punchy.client.render;

import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_7833;
import net.minecraft.class_9362;
import org.joml.Matrix4f;

public class MacePhysics {
    private static final MacePhysics INSTANCE = new MacePhysics();
    private static final float FALL_THRESHOLD = -0.15f;
    private static final float TARGET_PITCH_UP = -50.0f;
    private static final float BLEND_SPEED = 0.25f;
    private static final double MIN_FALL_DIST = 5.0;
    private float pitch;
    private float prevPitch;
    private float targetPitch;
    private double fallStartY = Double.NaN;

    public static MacePhysics get() {
        return INSTANCE;
    }

    public void tick(class_746 player) {
        boolean falling;
        this.prevPitch = this.pitch;
        if (player == null) {
            this.pitch = 0.0f;
            this.targetPitch = 0.0f;
            this.fallStartY = Double.NaN;
            return;
        }
        boolean bl = falling = player.method_18798().field_1351 < (double)-0.15f && !player.method_24828() && !player.method_5740();
        if (falling) {
            if (Double.isNaN(this.fallStartY)) {
                this.fallStartY = player.method_23318();
            }
        } else {
            this.fallStartY = Double.NaN;
        }
        double currentFallDist = Double.isNaN(this.fallStartY) ? 0.0 : this.fallStartY - player.method_23318();
        double distToGround = this.getDistanceToGround(player);
        double totalPotentialFall = currentFallDist + distToGround;
        boolean holdingMace = this.isHoldingMace(player);
        this.targetPitch = holdingMace && falling && totalPotentialFall > 5.0 ? -50.0f : 0.0f;
        this.pitch = class_3532.method_16439((float)0.25f, (float)this.pitch, (float)this.targetPitch);
    }

    private double getDistanceToGround(class_746 player) {
        class_243 start = player.method_19538();
        class_243 end = start.method_1031(0.0, -20.0, 0.0);
        class_3965 result = player.method_37908().method_17742(new class_3959(start, end, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)player));
        if (result.method_17783() == class_239.class_240.field_1333) {
            return 20.0;
        }
        return start.field_1351 - result.method_17784().field_1351;
    }

    public void applyTo(class_4587 poseStack, class_746 player, class_1306 arm, float partialTicks) {
        float interpolatedPitch = class_3532.method_16439((float)partialTicks, (float)this.prevPitch, (float)this.pitch);
        if (player == null || Math.abs(interpolatedPitch) < 0.01f) {
            return;
        }
        if (!this.isArmHoldingMace(player, arm)) {
            return;
        }
        poseStack.method_22907(class_7833.field_40714.rotationDegrees(interpolatedPitch));
    }

    public void applyTo(Matrix4f matrix, class_746 player, class_1306 arm, float partialTicks) {
        float interpolatedPitch = class_3532.method_16439((float)partialTicks, (float)this.prevPitch, (float)this.pitch);
        if (player == null || Math.abs(interpolatedPitch) < 0.01f) {
            return;
        }
        if (!this.isArmHoldingMace(player, arm)) {
            return;
        }
        matrix.rotateX((float)Math.toRadians(interpolatedPitch));
    }

    private boolean isHoldingMace(class_746 player) {
        return player.method_6047().method_7909() instanceof class_9362 || player.method_6079().method_7909() instanceof class_9362;
    }

    private boolean isArmHoldingMace(class_746 player, class_1306 arm) {
        boolean mainRight = player.method_6068() == class_1306.field_6183;
        class_1268 hand = arm == class_1306.field_6183 == mainRight ? class_1268.field_5808 : class_1268.field_5810;
        return player.method_5998(hand).method_7909() instanceof class_9362;
    }
}

