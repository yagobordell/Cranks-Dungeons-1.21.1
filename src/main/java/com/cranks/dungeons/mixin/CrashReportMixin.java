package com.cranks.dungeons.mixin;

import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public class CrashReportMixin {
    @Inject(method = "create", at = @At("HEAD"))
    private static void onCreate(Throwable cause, String title, CallbackInfoReturnable<CrashReport> cir) {
        System.err.println("--- CRANKS DUNGEONS CRASH INTERCEPT ---");
        System.err.println("The original exception before CrashReport failed:");
        cause.printStackTrace();
        System.err.println("---------------------------------------");
    }
}
