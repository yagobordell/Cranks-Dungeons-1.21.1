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
        return switch (equipType) {
            case HELMET -> "helmet_energy_shield";
            case CHESTPLATE -> "chestplate_energy_shield";
            case LEGGINGS -> "leggings_energy_shield";
            case BOOTS -> "boots_energy_shield";
            case ELYTRA -> "elytra_energy_shield";
            default -> null;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Power sealed within runes").formatted(Formatting.AQUA, Formatting.ITALIC));
        textConsumer.accept(Text.literal("▶ On Armor & Elytra:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Energy Shield", Formatting.WHITE);

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}