package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class SwiftnessTomeItem extends Item {

    private final CustomRarity customRarity;

    public SwiftnessTomeItem(CustomRarity customRarity, Settings settings) {
        super(settings);
        this.customRarity = customRarity;
    }

    /**
     * Returns the stat ID for a given equipment type, or null if not applicable.
     */
    public String getStatIdForEquipment(EquipmentType equipType) {
        return switch (equipType) {
            case SWORD, SPEAR, MACE, TRIDENT -> equipType.name().toLowerCase() + "_attack_speed";
            case BOW, CROSSBOW               -> equipType.name().toLowerCase() + "_reload_speed";
            case BOOTS                       -> "boots_movement_speed";
            case ELYTRA                      -> "elytra_movement_speed";
            default                          -> null;
        };
    }

    public boolean canApplyTo(ItemStack equipment) {
        var equipTypeOpt = EquipmentType.getTypeForItem(equipment);
        if (equipTypeOpt.isEmpty()) return false;

        EquipmentType equipType = equipTypeOpt.get();
        String statId = getStatIdForEquipment(equipType);
        if (statId == null) return false;

        if (!ItemStatManager.canAddStat(equipment)) return false;

        // Verify the stat exists in the registry
        if (com.cranks.dungeons.stat.StatRegistry.getStat(statId) == null) return false;

        // Check for duplicates (same stat ID or same underlying attribute)
        var existingStats = ItemStatManager.getStats(equipment);
        for (var stat : existingStats) {
            if (stat.statId.equals(statId)) return false;

            var existingStat = com.cranks.dungeons.stat.StatRegistry.getStat(stat.statId);
            var newStat = com.cranks.dungeons.stat.StatRegistry.getStat(statId);
            if (existingStat != null && newStat != null
                    && existingStat.getAttribute().equals(newStat.getAttribute())) {
                return false;
            }
        }

        return true;
    }

    public CustomRarity getCustomRarity() {
        return customRarity;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.empty()
                .append(super.getName(stack))
                .append(Text.literal(" "))
                .append(Text.literal("[ " + customRarity.getName() + " ]").formatted(customRarity.getColor()));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Power sealed within runes").formatted(Formatting.AQUA, Formatting.ITALIC));

        textConsumer.accept(Text.literal("▶ On Melee Weapons:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Attack Speed", Formatting.WHITE);

        textConsumer.accept(Text.empty());

        textConsumer.accept(Text.literal("▶ On Ranged Weapons:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Reload Speed", Formatting.WHITE);

        textConsumer.accept(Text.empty());

        textConsumer.accept(Text.literal("▶ On Boots & Elytra:").formatted(Formatting.WHITE));
        addTierBreakdown(textConsumer, "Movement Speed", Formatting.WHITE);

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }

    private void addTierBreakdown(Consumer<Text> textConsumer, String statName, Formatting statColor) {
        for (int statCount = 0; statCount <= 4; statCount++) {
            int tier = ElementalTomeItem.getTierForStatCount(statCount);
            Formatting tierColor = getTierColor(tier);
            textConsumer.accept(Text.literal("  " + statCount + " stats → ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(statName).formatted(statColor))
                    .append(Text.literal(" [T" + tier + "]").formatted(tierColor)));
        }
    }

    private static Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1, 2 -> Formatting.GRAY;
            case 3, 4 -> Formatting.GREEN;
            case 5    -> Formatting.GOLD;
            default   -> Formatting.WHITE;
        };
    }
}