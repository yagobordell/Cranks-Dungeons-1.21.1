package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 100)
public class VanillaPoisonConversionMixin {

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void convertVanillaPoison(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        handlePoisonConversion(effect, null, cir);
    }

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void convertVanillaPoisonWithSource(StatusEffectInstance effect, net.minecraft.entity.Entity source, CallbackInfoReturnable<Boolean> cir) {
        handlePoisonConversion(effect, source, cir);
    }

    private void handlePoisonConversion(StatusEffectInstance effect, net.minecraft.entity.Entity source, CallbackInfoReturnable<Boolean> cir) {
        // Check if this is vanilla poison
        if (effect.getEffectType() == StatusEffects.POISON) {
            LivingEntity entity = (LivingEntity) (Object) this;

            // Don't process on client side
            if (entity.getEntityWorld().isClient()) {
                return;
            }
            source = com.cranks.dungeons.effect.PotionRules.effectOwner(source);

            // Calculate damage based on vanilla poison amplifier
            // Vanilla poison: level I = 0 amplifier, level II = 1 amplifier
            int amplifier = effect.getAmplifier();
            int stacksToAdd = com.cranks.dungeons.effect.StatusEffectRules.poisonStacks(amplifier);

            // Use default base damage of 1.0
            float baseDamage = 1.0f;

            // Convert duration from vanilla (ticks) to seconds
            // Vanilla poison duration is in ticks (20 ticks = 1 second)
            double durationSeconds = effect.getDuration() / 20.0;
            if (effect.isInfinite()) durationSeconds = Double.POSITIVE_INFINITY;

            // Apply stacks to the mod's poison system
            for (int i = 0; i < stacksToAdd; i++) {
                AilmentManager.applyAilment(
                    entity,
                    AilmentType.POISON,
                    baseDamage,
                    durationSeconds,
                    source
                );
            }

            // Cancel the vanilla poison effect - don't apply it
            cir.setReturnValue(entity.hasStatusEffect(com.cranks.dungeons.effect.ModEffects.POISON));
            cir.cancel();
        }
    }
}
