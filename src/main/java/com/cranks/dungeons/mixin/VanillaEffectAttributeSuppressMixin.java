package com.cranks.dungeons.mixin;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(StatusEffect.class)
public abstract class VanillaEffectAttributeSuppressMixin {

    @Inject(method = "forEachAttributeModifier", at = @At("HEAD"), cancellable = true)
    private void suppressVanillaAttributeModifiers(int amplifier, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer, CallbackInfo ci) {
        StatusEffect self = (StatusEffect) (Object) this;
        String key = self.getTranslationKey();
        // Suppress vanilla attribute modifiers in favor of Cranks Dungeons ModAttributes
        if ("effect.minecraft.strength".equals(key) 
                || "effect.minecraft.weakness".equals(key) 
                || "effect.minecraft.haste".equals(key)
                || "effect.minecraft.mining_fatigue".equals(key)
                || "effect.minecraft.absorption".equals(key)
                || "effect.minecraft.luck".equals(key)
                || "effect.minecraft.unluck".equals(key)) {
            ci.cancel();
        }
    }
}
