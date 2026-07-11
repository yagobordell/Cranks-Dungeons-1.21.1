/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_310;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.state.InterfaceDelayState;

@Mixin(value={class_310.class})
public class MinecraftScreenDelayMixin {
    @Inject(method={"method_1507(Lnet/minecraft/class_437;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void punchy$delayInterface(class_437 screen, CallbackInfo ci) {
        if (InterfaceDelayState.shouldDelay(screen)) {
            InterfaceDelayState.queue(screen);
            ci.cancel();
        }
    }
}

