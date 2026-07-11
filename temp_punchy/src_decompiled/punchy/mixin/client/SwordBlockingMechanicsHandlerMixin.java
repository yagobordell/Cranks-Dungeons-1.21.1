/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package punchy.mixin.client;

import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.compat.SwordBlockingMechanicsCompat;

@Pseudo
@Mixin(targets={"fuzs/swordblockingmechanics/handler/SwordBlockingHandler"}, remap=false)
public class SwordBlockingMechanicsHandlerMixin {
    @Inject(method={"onUseItem"}, at={@At(value="RETURN")}, require=0)
    private static void punchy$onSwordBlockingUseItem(class_1657 player, class_1937 level, class_1268 interactionHand, CallbackInfoReturnable<Object> cir) {
        if (player != null && player.method_6115() && player.method_6058() == interactionHand) {
            SwordBlockingMechanicsCompat.recordBlockingStart(player, interactionHand);
        }
    }

    @Inject(method={"onUseItemStop"}, at={@At(value="HEAD")}, require=0)
    private static void punchy$onSwordBlockingUseItemStop(class_1309 livingEntity, class_1799 itemStack, int remainingUseDuration, CallbackInfoReturnable<Object> cir) {
        if (livingEntity instanceof class_1657) {
            class_1657 player = (class_1657)livingEntity;
            SwordBlockingMechanicsCompat.recordBlockingStop(player, player.method_6058());
        }
    }
}

