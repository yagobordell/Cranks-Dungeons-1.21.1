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
    public void appendTooltip(ItemStack stack, TooltipContext context,
            net.minecraft.component.type.TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Power sealed within runes").formatted(Formatting.AQUA, Formatting.ITALIC));

        textConsumer.accept(Text.literal("▶ On Melee Weapons & Chestplates:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Attack Speed", Formatting.WHITE);

        textConsumer.accept(Text.empty());

        textConsumer.accept(Text.literal("▶ On Ranged Weapons:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Reload Speed", Formatting.WHITE);

        textConsumer.accept(Text.empty());

        textConsumer.accept(Text.literal("▶ On Boots & Elytra:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Movement Speed", Formatting.WHITE);

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}