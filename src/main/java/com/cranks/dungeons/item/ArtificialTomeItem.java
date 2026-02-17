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
    public Text getName(ItemStack stack) {
        return Text.empty()
                .append(super.getName(stack))
                .append(Text.literal(" "))
                .append(Text.literal("[ " + customRarity.getName() + " ]").formatted(customRarity.getColor(), Formatting.BOLD));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Artificial Enchantment Tome").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("▶ Step 1:").formatted(Formatting.WHITE, Formatting.BOLD));
        textConsumer.accept(Text.literal("  Use on the Runic Enhancement Altar").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("  to apply a ").formatted(Formatting.GRAY)
                .append(Text.literal("Cryptic Stat").formatted(Formatting.DARK_GREEN))
                .append(Text.literal(" to equipment").formatted(Formatting.GRAY)));
        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("▶ Step 2:").formatted(Formatting.WHITE, Formatting.BOLD));
        textConsumer.accept(Text.literal("  Place the item in the").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("  Artificial Enhancement Altar").formatted(Formatting.DARK_GREEN));
        textConsumer.accept(Text.literal("  to choose from 3 random stats").formatted(Formatting.GRAY));
        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("Only one artificial stat per item").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}