/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package punchy.mixin.client;

import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.state.ExposureInterfaceDelayState;

@Pseudo
@Mixin(targets={"io/github/mortuusars/exposure/client/camera/CameraClient"}, remap=false)
public class ExposureCameraClientMixin {
    @Inject(method={"isActive"}, at={@At(value="HEAD")}, cancellable=true)
    private static void punchy$delayExposureInterface(CallbackInfoReturnable<Boolean> cir) {
        if (ExposureInterfaceDelayState.shouldBlockExposureInterface()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"getActive"}, at={@At(value="HEAD")}, cancellable=true)
    private static void punchy$delayExposureActiveCamera(CallbackInfoReturnable<Optional<?>> cir) {
        if (ExposureInterfaceDelayState.shouldBlockExposureInterface()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method={"viewfinder"}, at={@At(value="HEAD")}, cancellable=true)
    private static void punchy$delayExposureViewfinder(CallbackInfoReturnable<Object> cir) {
        if (ExposureInterfaceDelayState.shouldBlockExposureInterface()) {
            cir.setReturnValue(null);
        }
    }
}

