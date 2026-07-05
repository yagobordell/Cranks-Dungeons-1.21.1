package com.cranks.dungeons.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

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
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Adds an artificial rune to a rare item.").formatted(Formatting.GRAY));

        textConsumer.accept(Text.literal("A synthetic echo of a forgotten craft").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
