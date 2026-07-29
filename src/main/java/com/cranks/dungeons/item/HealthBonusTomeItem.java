package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

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
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {

        addTomeDescription(tooltip::add);

        addStatRange(tooltip::add, EquipmentType.LEGGINGS, "Leggings");
        addStatRange(tooltip::add, EquipmentType.HELMET, "Helmet");
        addStatRange(tooltip::add, EquipmentType.CHESTPLATE, "Chest/Boots");

        super.appendTooltip(stack, context, tooltip, type);
    }
}
