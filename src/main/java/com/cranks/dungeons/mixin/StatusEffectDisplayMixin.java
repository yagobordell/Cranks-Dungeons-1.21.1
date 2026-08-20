package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.StatusEffectStatManager;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractInventoryScreen.class)
public class StatusEffectDisplayMixin {

    @Inject(method = "getStatusEffectDescription", at = @At("RETURN"), cancellable = true)
    private void formatCustomStatusEffectDescription(StatusEffectInstance effect, CallbackInfoReturnable<Text> cir) {
        Text customName = StatusEffectStatManager.getFormattedEffectName(effect);
        if (customName != null && !customName.getString().isEmpty()) {
            cir.setReturnValue(customName);
        }
    }
}
