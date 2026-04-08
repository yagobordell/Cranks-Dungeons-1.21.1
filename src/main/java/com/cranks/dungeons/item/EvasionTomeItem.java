package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class EvasionTomeItem extends AbstractTomeItem {

    public EvasionTomeItem(CustomRarity customRarity, Settings settings) {
        super(customRarity, settings);
    }

    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        return switch (equipType) {
            case HELMET -> "helmet_evasion_rating";
            case CHESTPLATE -> "chestplate_evasion_rating";
            case LEGGINGS -> "leggings_evasion_rating";
            case BOOTS -> "boots_evasion_rating";
            default -> null;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Power sealed within runes").formatted(Formatting.AQUA, Formatting.ITALIC));
        textConsumer.accept(Text.literal("▶ On Armor:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Evasion Rating", Formatting.WHITE);

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}