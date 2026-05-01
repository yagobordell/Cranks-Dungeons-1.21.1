package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class RangeTomeItem extends AbstractTomeItem {

    public RangeTomeItem(CustomRarity customRarity, Settings settings) {
        super(customRarity, settings);
    }

    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        if (isWeapon(equipType)) {
            return equipType.name().toLowerCase() + "_attack_range";
        }
        return null;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        addTomeDescription(textConsumer);

        addStatRange(textConsumer, EquipmentType.SWORD, "Weapons");

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
