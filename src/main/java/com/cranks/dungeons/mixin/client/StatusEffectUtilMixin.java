package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.effect.ModEffects;
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
        if (effect.getEffectType() == ModEffects.WELL_FED) {
            cir.setReturnValue(Text.literal("+10% Movement Speed & Attack/Cast Speed"));
        } else if (effect.getEffectType() == ModEffects.STARVING) {
            cir.setReturnValue(Text.literal("-10% Movement Speed & Attack/Cast Speed"));
        }
    }
}
