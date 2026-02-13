package com.cranks.dungeons.mixin;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class EnchantmentFilterMixin {

    @Unique
    private static boolean dungeons$filtering = false;

    @Inject(method = "set", at = @At("RETURN"))
    private <T> void dungeons$filterOnSet(ComponentType<? super T> type, T value,
                                          CallbackInfoReturnable<T> cir) {
        if (type != DataComponentTypes.ENCHANTMENTS) return;
        if (value == null) return;
        if (dungeons$filtering) return;

        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) return;

        ItemEnchantmentsComponent enchants = (ItemEnchantmentsComponent) value;
        if (enchants.isEmpty()) return;

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                ItemEnchantmentsComponent.DEFAULT);

        boolean changed = false;

        for (var entry : enchants.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();

            // Allow if in enchanting table OR random loot tags
            if (enchantment.isIn(EnchantmentTags.ON_RANDOM_LOOT)) {
                builder.add(enchantment, level);
            } else {
                changed = true;
            }
        }

        if (changed) {
            dungeons$filtering = true;
            try {
                self.set(DataComponentTypes.ENCHANTMENTS, builder.build());
            } finally {
                dungeons$filtering = false;
            }
        }
    }
}