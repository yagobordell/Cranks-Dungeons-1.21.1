/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.render.HandRenderBobContext;

@Mixin(value={class_757.class})
public class GameRendererMixin {
    @Inject(method={"method_3186(Lnet/minecraft/class_4587;F)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void punchy$disableViewBobDuringSprint(class_4587 poseStack, float tickDelta, CallbackInfo ci) {
        if (tickDelta < 0.0f) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return;
        }
        class_1297 cameraEntity = mc.method_1560();
        if (!(cameraEntity instanceof class_1657)) {
            return;
        }
        class_1657 player = (class_1657)cameraEntity;
        float delta = player.field_5973 - player.field_6039;
        float walk = -(player.field_5973 + delta * tickDelta);
        float bob = class_3532.method_16439((float)tickDelta, (float)player.field_7505, (float)player.field_7483);
        HandRenderBobContext.updateBobSample(walk, bob);
    }
}

