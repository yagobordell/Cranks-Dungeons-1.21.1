package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemDefaultComponentsMixin {

    @Inject(method = "getComponents", at = @At("RETURN"), cancellable = true)
    private void modifyDefaultComponents(CallbackInfoReturnable<net.minecraft.component.ComponentMap> cir) {
        Item item = (Item)(Object)this;
        String itemName = item.toString().toLowerCase();

        if (!shouldModify(itemName)) return;

        var originalMap = cir.getReturnValue();
        var attributes = originalMap.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        if (attributes == null) return;

        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();

        boolean modified = false;

        for (var entry : attributes.modifiers()) {
            if (isArmor(itemName) && entry.attribute().equals(EntityAttributes.ARMOR)) {
                // Boost armor value
                double multiplier = getArmorMultiplier(itemName);
                builder.add(
                        entry.attribute(),
                        new EntityAttributeModifier(
                                entry.modifier().id(),
                                entry.modifier().value() * multiplier,
                                entry.modifier().operation()
                        ),
                        entry.slot()
                );
                modified = true;

                double toughness = getToughnessBonus(itemName);
                if (toughness > 0) {
                    builder.add(
                            EntityAttributes.ARMOR_TOUGHNESS,
                            new EntityAttributeModifier(
                                    Identifier.of("minecraft", "armor.toughness"),
                                    toughness,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            entry.slot()
                    );
                }
            } else if (isWeapon(itemName) && entry.attribute().equals(EntityAttributes.ATTACK_DAMAGE)) {
                double bonus = getDamageBonus(itemName);
                builder.add(
                        entry.attribute(),
                        new EntityAttributeModifier(
                                entry.modifier().id(),
                                entry.modifier().value() + bonus,
                                entry.modifier().operation()
                        ),
                        entry.slot()
                );
                modified = true;
            } else {
                // Keep other modifiers unchanged
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        if (modified) {
            // Create new component map with modified attributes
            var newMap = net.minecraft.component.ComponentMap.builder()
                    .addAll(originalMap)
                    .add(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build())
                    .build();
            cir.setReturnValue(newMap);
        }
    }

    private boolean shouldModify(String itemName) {
        return isArmor(itemName) || isWeapon(itemName);
    }

    private boolean isArmor(String itemName) {
        return (itemName.contains("helmet") || itemName.contains("chestplate") ||
                itemName.contains("leggings") || itemName.contains("boots")) &&
                (itemName.contains("leather") || itemName.contains("chainmail") ||
                        itemName.contains("iron") || itemName.contains("golden") ||
                        itemName.contains("diamond") || itemName.contains("netherite"));
    }

    private boolean isWeapon(String itemName) {
        return (itemName.contains("sword") || itemName.contains("axe")) &&
                (itemName.contains("wood") || itemName.contains("stone") ||
                        itemName.contains("iron") || itemName.contains("golden") ||
                        itemName.contains("diamond") || itemName.contains("netherite"));
    }

    private double getArmorMultiplier(String itemName) {
        if (itemName.contains("leather")) return 2.0;
        if (itemName.contains("chainmail")) return 2.0;
        if (itemName.contains("iron")) return 2.0;
        if (itemName.contains("golden")) return 2.0;
        if (itemName.contains("diamond")) return 2.0;
        if (itemName.contains("netherite")) return 2.0;
        return 1.0;
    }

    private double getToughnessBonus(String itemName) {
        if (itemName.contains("leather")) return 2.0;
        if (itemName.contains("chainmail")) return 2.0;
        if (itemName.contains("iron")) return 3.0;
        if (itemName.contains("golden")) return 2.0;
        if (itemName.contains("diamond")) return 3.0;
        if (itemName.contains("netherite")) return 4.0;
        return 0.0;
    }

    private double getDamageBonus(String itemName) {
        if (itemName.contains("wood")) return 2.0;
        if (itemName.contains("stone")) return 2.0;
        if (itemName.contains("iron")) return 2.0;
        if (itemName.contains("golden")) return 2.0;
        if (itemName.contains("diamond")) return 3.0;
        if (itemName.contains("netherite")) return 4.0;
        return 0.0;
    }
}