package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class EnergyTomeItem extends AbstractTomeItem {

    public EnergyTomeItem(CustomRarity customRarity, Settings settings) {
        super(customRarity, settings);
    }

    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        if (isArmor(equipType) || equipType == EquipmentType.ELYTRA) {
            return equipType.name().toLowerCase() + "_energy_shield";
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
