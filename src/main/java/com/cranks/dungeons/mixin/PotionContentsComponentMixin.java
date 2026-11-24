package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.EffectDescriptions;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(PotionContentsComponent.class)
public abstract class PotionContentsComponentMixin {

    @Inject(method = "buildTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V", at = @At("HEAD"), cancellable = true)
    private static void customPotionTooltip(Iterable<StatusEffectInstance> effects, Consumer<Text> textConsumer,
                                            float durationMultiplier, float tickRate, CallbackInfo ci) {
        boolean hasAny = false;
        for (StatusEffectInstance instance : effects) {
            hasAny = true;
            float instantMultiplier = durationMultiplier == com.cranks.dungeons.effect.PotionRules.CLOUD_DURATION_MULTIPLIER
                    ? com.cranks.dungeons.effect.PotionRules.CLOUD_INSTANT_MULTIPLIER : 1;
            EffectDescriptions.tooltip(instance, durationMultiplier, tickRate, instantMultiplier).forEach(textConsumer);
        }

        if (hasAny) {
            ci.cancel();
        }
    }
}
