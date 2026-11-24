package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class SwiftnessTomeItem extends AbstractTomeItem {

    public SwiftnessTomeItem(CustomRarity customRarity, Settings settings) {
        super(customRarity, settings);
    }

    /**
     * Returns the stat ID for a given equipment type, or null if not applicable.
     */
    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        return switch (equipType) {
            case SWORD, SPEAR, MACE, TRIDENT, BOW, CROSSBOW -> equipType.name().toLowerCase() + "_attack_speed";
            case CHESTPLATE -> "chestplate_attack_speed";
            case BOOTS -> "boots_movement_speed";
            case ELYTRA -> "elytra_movement_speed";
            default -> null;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        addTomeDescription(tooltip::add);
        for(var summary:summaries())tooltip.add(Text.literal(summary.label()+": ").formatted(Formatting.GRAY)
                .append(Text.literal(summary.stat().getDisplayName()).formatted(Formatting.WHITE)));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
