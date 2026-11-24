package com.cranks.dungeons.mixin;

import com.cranks.dungeons.alchemy.AlchemyItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Looted vanilla bottles still enter the same alchemy flow when filled with water. */
@Mixin(GlassBottleItem.class)
abstract class AlchemyBottleFillMixin {
    @ModifyVariable(method = "fill", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private ItemStack alchemyWater(ItemStack filled) {
        return filled.isOf(Items.POTION) && filled.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).matches(Potions.WATER)
                ? AlchemyItems.water(false, AlchemyItems.Delivery.DRINK) : filled;
    }
}
