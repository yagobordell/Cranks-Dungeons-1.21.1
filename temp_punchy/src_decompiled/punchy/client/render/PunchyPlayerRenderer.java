/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1068
 *  net.minecraft.class_1297
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_583
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_742
 *  net.minecraft.class_7833
 *  net.minecraft.class_897
 *  net.minecraft.class_922
 */
package punchy.client.render;

import java.util.HashMap;
import net.minecraft.class_1068;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import net.minecraft.class_897;
import net.minecraft.class_922;
import punchy.client.access.TransformablePart;
import punchy.client.animation.PunchyAnimationManager;

public class PunchyPlayerRenderer {
    public static void render(class_742 player, float partialTicks, class_4587 poseStack, class_4597 buffer, int combinedLight) {
        class_310 mc = class_310.method_1551();
        if (mc == null || player == null || buffer == null) {
            return;
        }
        class_897 renderer = mc.method_1561().method_3953((class_1297)player);
        if (!(renderer instanceof class_922)) {
            return;
        }
        class_922 livingRenderer = (class_922)renderer;
        class_583 modelObj = livingRenderer.method_4038();
        if (!(modelObj instanceof class_591)) {
            return;
        }
        class_591 playerModel = (class_591)modelObj;
        playerModel.field_3401.method_41923();
        playerModel.field_27433.method_41923();
        poseStack.method_22903();
        poseStack.method_22907(class_7833.field_40718.rotationDegrees(180.0f));
        HashMap<String, class_630> partsMap = new HashMap<String, class_630>();
        partsMap.put("RightArm", playerModel.field_3401);
        partsMap.put("LeftArm", playerModel.field_27433);
        PunchyAnimationManager.POSE_HANDLER.applyTransformations(partsMap, partialTicks);
        PunchyAnimationManager.SWIM_HANDLER.applyTransformations(partsMap, partialTicks, false);
        PunchyPlayerRenderer.copyMatrixToSleeve(playerModel.field_3401, playerModel.field_3486);
        PunchyPlayerRenderer.copyMatrixToSleeve(playerModel.field_27433, playerModel.field_3484);
        class_2960 texture = class_1068.method_4649();
        try {
            if (player.method_52814() != null && player.method_52814().comp_1626() != null) {
                texture = player.method_52814().comp_1626();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        class_4588 vertexConsumer = buffer.getBuffer(class_1921.method_23580((class_2960)texture));
        playerModel.field_3401.method_22698(poseStack, vertexConsumer, combinedLight, class_4608.field_21444);
        playerModel.field_27433.method_22698(poseStack, vertexConsumer, combinedLight, class_4608.field_21444);
        playerModel.field_3486.method_22698(poseStack, vertexConsumer, combinedLight, class_4608.field_21444);
        playerModel.field_3484.method_22698(poseStack, vertexConsumer, combinedLight, class_4608.field_21444);
        poseStack.method_22909();
    }

    private static void copyMatrixToSleeve(class_630 arm, class_630 sleeve) {
        class_630 class_6302 = arm;
        if (class_6302 instanceof TransformablePart) {
            TransformablePart tArm = (TransformablePart)class_6302;
            class_6302 = sleeve;
            if (class_6302 instanceof TransformablePart) {
                TransformablePart tSleeve = (TransformablePart)class_6302;
                tSleeve.punchy$setExplicitTransform(tArm.punchy$getExplicitTransform());
            }
        }
    }
}

