package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffect.class)
public abstract class RegenerationNeutralizeMixin {

    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    private void neutralizeVanillaRegenUpdate(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        StatusEffect self = (StatusEffect) (Object) this;
        String key = self.getTranslationKey();
        if ("effect.minecraft.regeneration".equals(key)) {
            // Regeneration in Cranks Dungeons applies ModAttributes.LIFE_REGENERATION (+3.0 HP/s per tier),
            // which is smoothly handled in LifeRegenerationMixin and respects Vaal Pact / Zealot's Oath.
            cir.setReturnValue(true);
        }
    }
}
