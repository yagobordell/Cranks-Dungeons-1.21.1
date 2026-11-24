package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.CranksDungeons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fuzs.overflowingbars.client.gui.RowCountRenderer", remap = false)
abstract class OverflowingBarsRowCountMixin {
    @Inject(method = "drawBarRowCount*", at = @At("HEAD"), cancellable = true, require = 0)
    private static void cranks$suppressRowCount(CallbackInfo ci) {
        if (CranksDungeons.CONFIG.useStatBars()) ci.cancel();
    }
}
