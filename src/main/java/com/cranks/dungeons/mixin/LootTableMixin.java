package com.cranks.dungeons.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {

    /**
     * Intercepts loot generation to completely remove Smithing Templates and Armor Trims from all loot chests,
     * archaeology brushing, and mob drops (like Elder Guardian).
     */
    @ModifyVariable(
            method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<ItemStack> cranksDungeons$filterSmithingTemplates(Consumer<ItemStack> original) {
        return stack -> {
            if (!(stack.getItem() instanceof SmithingTemplateItem)) {
                original.accept(stack);
            }
        };
    }
}
