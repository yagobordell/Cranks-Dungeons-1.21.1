package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.EnchantmentFilterUtil;
import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public class ItemStackAttributeMixin {

    @ModifyVariable(
            method = "applyAttributeModifiers",
            at = @At("HEAD"),
            argsOnly = true,
            index = 2
    )
    private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> filterAndScaleAttributes(
            BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> original,
            EquipmentSlot slot) {
        
        ItemStack stack = (ItemStack) (Object) this;
        
        return (attr, modifier) -> {
            boolean isMiningEfficiency = attr.value() == com.cranks.dungeons.registry.ModAttributes.MINING_EFFICIENCY.value();
            
            boolean isWeaponAttr = EnchantmentFilterUtil.isWeaponAttribute(attr);
            boolean isArmorAttr = EnchantmentFilterUtil.isArmorAttribute(attr);
            
            boolean isWeapon = EnchantmentFilterUtil.isWeapon(stack);
            boolean isArmor = EnchantmentFilterUtil.isArmor(stack);

            // Armor item modifiers, including enchantment effects, belong to the slot
            // that equips that item. Do not grant them while the piece is held in hand.
            if (isArmor && !isEquippedInArmorSlot(stack, slot)) return;

            if (isWeaponAttr && (!isWeapon || slot != EquipmentSlot.MAINHAND)) return;
            if (isArmorAttr && !isArmor) return;

            // Base attributes have namespace "minecraft" and don't contain "enchantment"
            // Runes have namespace "cranks-dungeons"
            // Enchantments have namespace "minecraft" and path starts with "enchantment."
            
            boolean isEnchantment = modifier.id().getPath().contains("enchantment");

            // Only get effectiveness if we are dealing with an enchantment to save performance
            double effectiveness = 0.0;
            if (isEnchantment) {
                effectiveness = com.cranks.dungeons.equipment.ItemStatManager.getAttributeValue(stack, com.cranks.dungeons.registry.ModAttributes.ENCHANTMENT_EFFECTIVENESS);
            }
            
            EntityAttributeModifier.Operation operation = modifier.operation();
            double value = modifier.value();

            // Special case: Mining Efficiency (Gathering Speed) from enchantments should be multiplicative
            // We detect it's an enchantment if it's using ADD_VALUE (runes use MULTIPLIED_TOTAL)
            if (isMiningEfficiency && operation == EntityAttributeModifier.Operation.ADD_VALUE && isEnchantment) {
                operation = EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            }

            // Only scale if it's an enchantment
            double finalValue = isEnchantment ? value * (1.0 + effectiveness) : value;

            if (finalValue != value || operation != modifier.operation()) {
                EntityAttributeModifier newModifier = new EntityAttributeModifier(
                        modifier.id(),
                        finalValue,
                        operation
                );
                original.accept(attr, newModifier);
            } else {
                original.accept(attr, modifier);
            }
        };
    }

    @Unique
    private static boolean isEquippedInArmorSlot(ItemStack stack, EquipmentSlot slot) {
        if (com.cranks.dungeons.equipment.AnimalArmorHelper.isSupported(stack)) {
            return slot == EquipmentSlot.BODY;
        }

        EquipmentType type = EquipmentType.getTypeForItem(stack).orElse(null);
        if (type == null) return false;

        return switch (type) {
            case HELMET -> slot == EquipmentSlot.HEAD;
            case CHESTPLATE, ELYTRA -> slot == EquipmentSlot.CHEST;
            case LEGGINGS -> slot == EquipmentSlot.LEGS;
            case BOOTS -> slot == EquipmentSlot.FEET;
            case SHIELD -> slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
            default -> false;
        };
    }
}
