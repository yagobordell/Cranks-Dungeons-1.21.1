package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public class EnchantmentFilterMixin {

    @Inject(method = "supplyInventory", at = @At("RETURN"))
    private void dungeons$filterInventoryLoot(Inventory inventory,
                                              LootWorldContext parameters,
                                              long seed,
                                              CallbackInfo ci) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            dungeons$filterComponent(stack, DataComponentTypes.ENCHANTMENTS, false);
            dungeons$filterComponent(stack, DataComponentTypes.STORED_ENCHANTMENTS,
                    stack.isOf(Items.ENCHANTED_BOOK));

            if (stack.isOf(Items.ENCHANTED_BOOK)) {
                ItemEnchantmentsComponent stored = stack.getOrDefault(
                        DataComponentTypes.STORED_ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT);
                ItemEnchantmentsComponent regular = stack.getOrDefault(
                        DataComponentTypes.ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT);

                if (stored.isEmpty() && regular.isEmpty()) {
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Unique
    private static void dungeons$filterComponent(
            ItemStack stack,
            ComponentType<ItemEnchantmentsComponent> componentType,
            boolean limitToOne) {

        ItemEnchantmentsComponent enchants = stack.getOrDefault(
                componentType, ItemEnchantmentsComponent.DEFAULT);

        if (enchants.isEmpty()) return;

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                ItemEnchantmentsComponent.DEFAULT);

        boolean changed = false;
        int count = 0;

        for (var entry : enchants.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();

            if (!enchantment.isIn(EnchantmentTags.IN_ENCHANTING_TABLE)
                    && !enchantment.isIn(EnchantmentTags.ON_RANDOM_LOOT)) {
                changed = true;
                continue;
            }

            if (limitToOne && count >= 1) {
                changed = true;
                continue;
            }

            builder.add(enchantment, level);
            count++;
        }

        if (changed) {
            stack.set(componentType, builder.build());
        }
    }
}