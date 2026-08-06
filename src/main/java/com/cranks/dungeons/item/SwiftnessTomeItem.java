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
    protected void addTomeDescription(Consumer<Text> textConsumer) {
        textConsumer.accept(Text.literal("Upgrades a magic item to a rare item,").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("adding a guaranteed Haste rune").formatted(Formatting.GRAY));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {

        addTomeDescription(tooltip::add);

        addStatRange(tooltip::add, EquipmentType.SWORD, "Martial Weapons");
        addStatRange(tooltip::add, EquipmentType.BOOTS, "Boots");

        super.appendTooltip(stack, context, tooltip, type);
    }
}
