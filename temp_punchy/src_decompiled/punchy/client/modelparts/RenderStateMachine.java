/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_746
 */
package punchy.client.modelparts;

import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_746;
import punchy.client.render.CustomItemModelRenderer;

final class RenderStateMachine {
    RenderStateMachine() {
    }

    boolean tryRenderItem(class_1799 stack, class_746 player, class_1306 arm, class_4587 poseStack, class_4597 buffer, int packedLight, float partialTicks, boolean applyDisplay) {
        return CustomItemModelRenderer.tryRender(stack, player, arm, poseStack, buffer, packedLight, partialTicks, applyDisplay);
    }
}

