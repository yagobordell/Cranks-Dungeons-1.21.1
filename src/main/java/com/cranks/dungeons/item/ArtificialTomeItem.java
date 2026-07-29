package com.cranks.dungeons.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ArtificialTomeItem extends Item {

    private final CustomRarity customRarity;

    public ArtificialTomeItem(CustomRarity customRarity, Settings settings) {
        super(settings);
        this.customRarity = customRarity;
    }

    public CustomRarity getCustomRarity() {
        return customRarity;
    }



    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {

        tooltip.add(Text.literal("Adds an artificial rune to a rare item.").formatted(Formatting.GRAY));

        tooltip.add(Text.literal("A synthetic echo of a forgotten craft").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
