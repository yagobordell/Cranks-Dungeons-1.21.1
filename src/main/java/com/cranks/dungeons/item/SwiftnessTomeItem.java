package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
            case SWORD, SPEAR, MACE, TRIDENT -> equipType.name().toLowerCase() + "_attack_speed";
            case BOW, CROSSBOW -> equipType.name().toLowerCase() + "_reload_speed";
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
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        addTomeDescription(textConsumer);

        addStatRange(textConsumer, EquipmentType.SWORD, "Melee Weapons");
        addStatRange(textConsumer, EquipmentType.BOW, "Ranged Weapons");
        addStatRange(textConsumer, EquipmentType.BOOTS, "Boots");

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
