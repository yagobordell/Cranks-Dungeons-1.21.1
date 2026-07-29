package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
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
                                              LootContextParameterSet parameters,
                                              long seed,
                                              CallbackInfo ci) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            com.cranks.dungeons.util.EnchantmentFilterUtil.filterStack(stack);

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
}