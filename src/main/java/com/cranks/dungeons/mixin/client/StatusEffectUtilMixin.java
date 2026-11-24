package com.cranks.dungeons.mixin.client;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin {

    @Inject(method = "getDurationText", at = @At("HEAD"), cancellable = true)
    private static void hideInfiniteDurationText(StatusEffectInstance effect, float multiplier, float tickRate, CallbackInfoReturnable<Text> cir) {
        // For infinite or permanent effects (e.g. Well Fed, Starving, Bad Omen), hide duration string so no timer or infinity symbol appears
        if (effect.isInfinite() || effect.getDuration() <= 0) {
            cir.setReturnValue(Text.empty());
        }
    }
}
