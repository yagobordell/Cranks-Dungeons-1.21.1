package com.cranks.dungeons.inventory;

import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EquipmentEnchanting {
    private EquipmentEnchanting() {}

    public static int lapisCost(RegistryEntry<Enchantment> enchantment) {
        String path = enchantment.getKey().orElseThrow().getValue().getPath();
        if (path.startsWith("greater_")) return 32;
        return 16;
    }

    public static int availableLapis(MaterialCrafting.Storage storage) {
        int total = 0;
        for (int i = 0; i < storage.size(); i++) {
            ItemStack stack = storage.stack(i);
            if (stack.isOf(Items.LAPIS_LAZULI)) total += stack.getCount();
        }
        return total;
    }

    public static boolean supports(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
        if (stack.getCount() != 1 || !com.cranks.dungeons.util.EnchantmentFilterUtil.isApproved(enchantment) || !enchantment.isIn(EnchantmentTags.IN_ENCHANTING_TABLE)
                || !enchantment.value().isAcceptableItem(stack)) return false;
        var slots = EquipmentType.getTypeForItem(stack).map(EquipmentType::getApplicableSlots)
                .orElse(Set.of(EquipmentSlot.MAINHAND));
        if (slots.stream().noneMatch(enchantment.value()::slotMatches)) return false;
        // Utility enchantments rely on their item tags and active slots rather than attribute bonuses.
        if (enchantment.value().effects().getOrDefault(EnchantmentEffectComponentTypes.ATTRIBUTES, List.of()).isEmpty()) return true;

        // Ask the actual equipment pipeline: some definitions have both armor and weapon bonuses,
        // and ItemStack's filtering decides which of those bonuses can affect this piece of gear.
        ItemStack probe = stack.copy();
        probe.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        probe.addEnchantment(enchantment, 1);
        for (var slot : slots) {
            Set<Identifier> modifiers = new HashSet<>();
            EnchantmentHelper.applyAttributeModifiers(probe, slot, (attribute, modifier) -> modifiers.add(modifier.id()));
            boolean[] effective = {false};
            probe.applyAttributeModifiers(slot, (attribute, modifier) -> {
                if (modifiers.contains(modifier.id()) && modifier.value() != 0) effective[0] = true;
            });
            if (effective[0]) return true;
        }
        return false;
    }

    public static int capacity(ItemStack stack) {
        return EnchantmentSlotHelper.getEnchantmentSlots(stack);
    }

    public static boolean isCompatible(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
        return supports(stack, enchantment) && stack.getEnchantments().getEnchantments().stream()
                .allMatch(existing -> existing.equals(enchantment)
                        ? EnchantmentSlotHelper.isStackable(enchantment)
                        : Enchantment.canBeCombined(existing, enchantment));
    }

    public static boolean canApply(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
        return hasOpenSlot(stack) && isCompatible(stack, enchantment);
    }

    public static boolean hasOpenSlot(ItemStack stack) {
        return EnchantmentSlotHelper.usedSlots(stack) < capacity(stack);
    }

    public static ItemStack withoutOneCopy(ItemStack stack, RegistryEntry<Enchantment> removed) {
        int level = stack.getEnchantments().getLevel(removed);
        if (level == 0 || stack.getCount() != 1 || stack.isOf(Items.ENCHANTED_BOOK)) return ItemStack.EMPTY;
        ItemStack result = stack.copy();
        EnchantmentHelper.apply(result, builder -> builder.set(removed, EnchantmentSlotHelper.isStackable(removed) ? level - 1 : 0));
        return result;
    }

    public static ItemStack withEnchantment(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
        if (!canApply(stack, enchantment)) return ItemStack.EMPTY;
        ItemStack result = stack.copy();
        EnchantmentHelper.apply(result, builder -> builder.set(enchantment, builder.getLevel(enchantment) + 1));
        return result;
    }
}
