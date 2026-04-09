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
        return enchantment.isIn(EnchantmentTags.IN_ENCHANTING_TABLE) || 
               enchantment.isIn(EnchantmentTags.ON_RANDOM_LOOT);
    }
}
