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
        for(var summary:summaries())tooltip.add(Text.literal(summary.label()+": ").formatted(Formatting.GRAY)
                .append(Text.literal(summary.stat().getDisplayName()).formatted(Formatting.WHITE)));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
