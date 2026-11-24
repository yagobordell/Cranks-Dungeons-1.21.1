package com.cranks.dungeons.effect;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

final class ElementalResistanceEffect extends StatusEffect {
    ElementalResistanceEffect(int color, RegistryEntry<EntityAttribute> attribute, String name) {
        super(StatusEffectCategory.BENEFICIAL, color);
        addAttributeModifier(attribute, Identifier.of("cranks-dungeons", "potion_" + name), 0.15, EntityAttributeModifier.Operation.ADD_VALUE);
    }
}
