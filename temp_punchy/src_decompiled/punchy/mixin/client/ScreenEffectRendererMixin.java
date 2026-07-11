/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4603
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package punchy.mixin.client;

import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import punchy.config.PunchyConfig;

@Mixin(value={class_4603.class})
public class ScreenEffectRendererMixin {
    @Invoker(value="method_23070")
    public static void punchy$renderFire(class_310 client, class_4587 poseStack) {
        throw new AssertionError();
    }

    @Redirect(method={"method_23067(Lnet/minecraft/class_310;Lnet/minecraft/class_4587;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_4603;method_23070(Lnet/minecraft/class_310;Lnet/minecraft/class_4587;)V"))
    private static void punchy$skipFireOverlay(class_310 client, class_4587 poseStack) {
        if (!PunchyConfig.isModEnabled() || PunchyConfig.disableEnhancedFireArmEffects()) {
            ScreenEffectRendererMixin.punchy$renderFire(client, poseStack);
        }
    }
}

