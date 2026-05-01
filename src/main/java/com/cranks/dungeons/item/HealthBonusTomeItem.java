package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class HealthBonusTomeItem extends AbstractTomeItem {

    public HealthBonusTomeItem(CustomRarity rarity, Settings settings) {
        super(rarity, settings);
    }

    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        if (isArmor(equipType)) {
            return equipType.name().toLowerCase() + "_max_health";
        }
        return null;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        addTomeDescription(textConsumer);

        addStatRange(textConsumer, EquipmentType.CHESTPLATE, "Armor");

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
