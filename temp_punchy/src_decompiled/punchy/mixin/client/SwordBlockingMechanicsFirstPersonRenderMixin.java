/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package punchy.mixin.client;

import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.compat.SwordBlockingMechanicsCompat;

@Pseudo
@Mixin(targets={"fuzs/swordblockingmechanics/client/handler/FirstPersonRenderingHandler"}, remap=false)
public class SwordBlockingMechanicsFirstPersonRenderMixin {
    @Inject(method={"onRenderHand(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFFFF)Lfuzs/puzzleslib/api/event/v1/core/EventResult;"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private static void punchy$disableSwordBlockingFirstPersonRender(class_1657 player, class_1268 interactionHand, class_1799 itemStack, class_4587 poseStack, class_4597 multiBufferSource, int combinedLight, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress, CallbackInfoReturnable<Object> cir) {
        if (!SwordBlockingMechanicsCompat.isAvailable()) {
            return;
        }
        Object pass = SwordBlockingMechanicsCompat.getEventResultPass();
        if (pass != null) {
            cir.setReturnValue(pass);
            cir.cancel();
        }
    }
}

