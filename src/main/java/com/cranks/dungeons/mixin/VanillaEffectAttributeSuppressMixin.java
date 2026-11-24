package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.StatusEffectRules;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(StatusEffect.class)
abstract class VanillaEffectAttributeSuppressMixin {
    @Unique
    private StatusEffectRules cranks$rule() {
        return StatusEffectRules.of(Registries.STATUS_EFFECT.getEntry((StatusEffect) (Object) this));
    }

    @Inject(method = "onApplied(Lnet/minecraft/entity/attribute/AttributeContainer;I)V", at = @At("HEAD"), cancellable = true)
    private void applyCustomModifier(AttributeContainer attributes, int amplifier, CallbackInfo ci) {
        var rule = cranks$rule();
        if (rule == null) return;
        var instance = attributes.getCustomInstance(rule.attribute);
        if (instance != null) {
            instance.removeModifier(rule.modifierId);
            instance.addTemporaryModifier(new EntityAttributeModifier(rule.modifierId, rule.modifier(amplifier), rule.operation));
        }
        ci.cancel();
    }

    @Inject(method = "onRemoved", at = @At("HEAD"), cancellable = true)
    private void removeCustomModifier(AttributeContainer attributes, CallbackInfo ci) {
        var rule = cranks$rule();
        if (rule == null) return;
        var instance = attributes.getCustomInstance(rule.attribute);
        if (instance != null) instance.removeModifier(rule.modifierId);
        ci.cancel();
    }

    @Inject(method = "forEachAttributeModifier", at = @At("HEAD"), cancellable = true)
    private void describeCustomModifier(int amplifier, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer,
                                       CallbackInfo ci) {
        var rule = cranks$rule();
        if (rule == null) return;
        consumer.accept(rule.attribute, new EntityAttributeModifier(rule.modifierId, rule.modifier(amplifier), rule.operation));
        ci.cancel();
    }
}
