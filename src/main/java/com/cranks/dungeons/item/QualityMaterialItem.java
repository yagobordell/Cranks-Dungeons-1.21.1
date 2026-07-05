package com.cranks.dungeons.item;

import net.minecraft.item.Item;

public class QualityMaterialItem extends Item {
    public enum Type {
        ARMOR,
        WEAPON
    }

    private final Type materialType;

    public QualityMaterialItem(Type materialType, Settings settings) {
        super(settings);
        this.materialType = materialType;
    }

    public Type getMaterialType() {
        return materialType;
    }

    @Override
    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              java.util.function.Consumer<net.minecraft.text.Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {
        textConsumer.accept(net.minecraft.text.Text.translatable(this.getTranslationKey() + ".desc").formatted(net.minecraft.util.Formatting.GRAY));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
