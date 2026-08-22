package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.EnchantmentFilterUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {

    /**
     * Intercepts loot generation to:
     * 1. Completely remove Smithing Templates and Armor Trims from all loot.
     * 2. Filter disabled enchantments from generated items.
     * 3. Discard empty Enchanted Books so mobs/chests never drop blank enchantment books.
     */
    @ModifyVariable(
            method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<ItemStack> cranksDungeons$filterLoot(Consumer<ItemStack> original) {
        return stack -> {
            if (stack == null || stack.isEmpty()) return;

            if (stack.getItem() instanceof SmithingTemplateItem) {
                return;
            }

            EnchantmentFilterUtil.filterStack(stack);

            if (stack.isOf(Items.ENCHANTED_BOOK)) {
                ItemEnchantmentsComponent stored = stack.getOrDefault(
                        DataComponentTypes.STORED_ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT);
                ItemEnchantmentsComponent regular = stack.getOrDefault(
                        DataComponentTypes.ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT);

                if (stored.isEmpty() && regular.isEmpty()) {
                    return; // Drop nothing if the enchanted book has no valid enchantments
                }
            }

            original.accept(stack);
        };
    }

    @ModifyVariable(
            method = "generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<ItemStack> cranksDungeons$filterLootParameterSet(Consumer<ItemStack> original) {
        return stack -> {
            if (stack == null || stack.isEmpty()) return;

            if (stack.getItem() instanceof SmithingTemplateItem) {
                return;
            }

            EnchantmentFilterUtil.filterStack(stack);

            if (stack.isOf(Items.ENCHANTED_BOOK)) {
                ItemEnchantmentsComponent stored = stack.getOrDefault(
                        DataComponentTypes.STORED_ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT);
                ItemEnchantmentsComponent regular = stack.getOrDefault(
                        DataComponentTypes.ENCHANTMENTS,
                        ItemEnchantmentsComponent.DEFAULT);

                if (stored.isEmpty() && regular.isEmpty()) {
                    return;
                }
            }

            original.accept(stack);
        };
    }
}
