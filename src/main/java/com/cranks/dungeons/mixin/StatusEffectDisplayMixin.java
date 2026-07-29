package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractInventoryScreen.class)
public class StatusEffectDisplayMixin {

    @Inject(method = "getStatusEffectDescription", at = @At("RETURN"), cancellable = true)
    private void modifyPoisonDescription(StatusEffectInstance effect, CallbackInfoReturnable<Text> cir) {
        // Check if this is our poison effect
        if (effect.getEffectType() == ModEffects.POISON) {
            int amplifier = effect.getAmplifier();
            int stackCount = amplifier + 1;
            
            // Create "Poison +N" text
            MutableText customName = Text.literal("Poison +" + stackCount);
            cir.setReturnValue(customName);
        }
    }
}
