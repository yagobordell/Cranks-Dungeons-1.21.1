package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class FoodTooltipMixin {

    @Inject(method = "appendTooltip", at = @At("RETURN"))
    private void appendFoodTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if (stack.contains(DataComponentTypes.FOOD)) {
            FoodComponent food = stack.get(DataComponentTypes.FOOD);
            if (food != null) {
                int nutrition = food.nutrition();
                tooltip.add(Text.literal(" "));
                tooltip.add(Text.literal("Restores: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+" + nutrition + " Hunger").formatted(Formatting.GOLD)));
            }
        }
    }
}
