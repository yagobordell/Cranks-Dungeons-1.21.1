package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffect.class)
public class StatusEffectNameMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void modifyEffectName(CallbackInfoReturnable<Text> cir) {
        StatusEffect self = (StatusEffect) (Object) this;
        
        // Check if this is our poison effect
        // Need to check if the effect type matches our poison effect
        // We'll do this by checking the translation key
        String translationKey = self.getTranslationKey();
        if (translationKey.equals("effect.cranks-dungeons.poison")) {
            // We need to get the amplifier from somewhere - this is tricky
            // since StatusEffect doesn't know about instances
            // For now, just return the base name without the roman numeral
            Text customName = Text.translatable(translationKey);
            cir.setReturnValue(customName);
        }
    }
}
