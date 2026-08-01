package com.cranks.dungeons.util;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;

public class EnchantmentFilterUtil {

    /**
     * Filters the enchantments on an ItemStack, removing any that are not in the approved tags.
     */
    public static void filterStack(ItemStack stack) {
        if (stack.isEmpty()) return;

        filterComponent(stack, DataComponentTypes.ENCHANTMENTS);
        filterComponent(stack, DataComponentTypes.STORED_ENCHANTMENTS);
    }

    /**
     * Filters a specific enchantment component type on an ItemStack.
     */
    public static void filterComponent(ItemStack stack, net.minecraft.component.ComponentType<ItemEnchantmentsComponent> componentType) {
        ItemEnchantmentsComponent enchants = stack.get(componentType);
        if (enchants == null || enchants.isEmpty()) return;

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        boolean changed = false;
        int count = 0;
        boolean isEnchantedBook = stack.isOf(net.minecraft.item.Items.ENCHANTED_BOOK);

        for (var entry : enchants.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();

            // Enchanted books are limited to AT MOST one enchantment
            if (isEnchantedBook && count >= 1) {
                changed = true;
                continue;
            }

            // Use the user's overridden tags as the source of truth
            if (isApproved(enchantment)) {
                builder.add(enchantment, level);
                count++;
            } else {
                changed = true;
            }
        }

        if (changed) {
            stack.set(componentType, builder.build());
        }
    }

    /**
     * Checks if an enchantment is approved based on the custom tags.
     */
    public static boolean isApproved(RegistryEntry<Enchantment> enchantment) {
        if (enchantment.matchesId(net.minecraft.util.Identifier.of("minecraft", "loyalty")) ||
            enchantment.matchesId(net.minecraft.util.Identifier.of("minecraft", "riptide")) ||
            enchantment.matchesId(net.minecraft.util.Identifier.of("minecraft", "density")) ||
            enchantment.matchesId(net.minecraft.util.Identifier.of("minecraft", "wind_burst")) ||
            enchantment.matchesId(net.minecraft.util.Identifier.of("minecraft", "breach"))) {
            return false;
        }

        return enchantment.isIn(EnchantmentTags.IN_ENCHANTING_TABLE) || 
               enchantment.isIn(EnchantmentTags.ON_RANDOM_LOOT);
    }

    public static boolean isArmorAttribute(RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        String key = attr.value().getTranslationKey();
        return (key.contains("resistance") || key.contains("armor") || key.contains("health") || 
               key.contains("shield") || key.contains("evasion") || key.contains("regeneration") ||
               key.contains("mitigation") || key.contains("movement_speed") || key.contains("flying_speed") ||
               key.contains("feather_falling")) 
               && !key.contains("penetration");
    }

    public static boolean isWeaponAttribute(RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        String key = attr.value().getTranslationKey();
        return (key.contains("damage") || key.contains("chance_to") || key.contains("duration") ||
               key.contains("penetration") || key.contains("steal") || key.contains("attack") ||
               key.contains("knockback") || key.contains("breaking_range") || key.contains("precision_mining") ||
               key.contains("efficiency") || key.contains("reload_speed") ||
               key.contains("extra") || key.contains("stacks") || key.contains("fortune") ||
               key.contains("experience") || key.contains("crit_chance") || key.contains("crit_multiplier"))
               && !key.contains("resistance");
    }

    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var typeOpt = com.cranks.dungeons.equipment.EquipmentType.getTypeForItem(stack);
        if (typeOpt.isEmpty()) return false;
        var type = typeOpt.get();
        return type == com.cranks.dungeons.equipment.EquipmentType.SWORD ||
               type == com.cranks.dungeons.equipment.EquipmentType.AXE ||
               type == com.cranks.dungeons.equipment.EquipmentType.MACE ||
               type == com.cranks.dungeons.equipment.EquipmentType.SPEAR ||
               type == com.cranks.dungeons.equipment.EquipmentType.BOW ||
               type == com.cranks.dungeons.equipment.EquipmentType.CROSSBOW ||
               type == com.cranks.dungeons.equipment.EquipmentType.TRIDENT ||
               type == com.cranks.dungeons.equipment.EquipmentType.PICKAXE ||
               type == com.cranks.dungeons.equipment.EquipmentType.SHOVEL ||
               type == com.cranks.dungeons.equipment.EquipmentType.HOE;
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var typeOpt = com.cranks.dungeons.equipment.EquipmentType.getTypeForItem(stack);
        if (typeOpt.isEmpty()) return false;
        var type = typeOpt.get();
        return type == com.cranks.dungeons.equipment.EquipmentType.HELMET ||
               type == com.cranks.dungeons.equipment.EquipmentType.CHESTPLATE ||
               type == com.cranks.dungeons.equipment.EquipmentType.LEGGINGS ||
               type == com.cranks.dungeons.equipment.EquipmentType.BOOTS ||
               type == com.cranks.dungeons.equipment.EquipmentType.ELYTRA ||
               type == com.cranks.dungeons.equipment.EquipmentType.SHIELD;
    }
}
