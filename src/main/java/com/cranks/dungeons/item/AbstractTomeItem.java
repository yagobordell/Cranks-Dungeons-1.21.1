package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

/**
 * Base class for all tome items (Elemental, Swiftness, Energy, Evasion).
 * Provides shared logic for stat application, tooltips, and rarity display.
 */
public abstract class AbstractTomeItem extends Item {

    private final CustomRarity customRarity;

    protected AbstractTomeItem(CustomRarity customRarity, Settings settings) {
        super(settings);
        this.customRarity = customRarity;
    }

    public CustomRarity getCustomRarity() {
        return customRarity;
    }

    public int getMinStatLevel() {
        return 1;
    }

    /**
     * Returns the stat ID this tome applies for the given equipment type,
     * or null if the tome cannot be applied to this equipment type.
     */
    public abstract String getStatIdForEquipment(EquipmentType equipType);

    /**
     * Check if equipment can accept this tome.
     * Validates equipment type compatibility, stat slot availability,
     * and prevents duplicate stats/attributes.
     */
    public boolean canApplyTo(ItemStack equipment) {
        var equipTypeOpt = EquipmentType.getTypeForItem(equipment);
        if (equipTypeOpt.isEmpty()) return false;

        EquipmentType equipType = equipTypeOpt.get();
        String statId = getStatIdForEquipment(equipType);
        if (statId == null) return false;

        // Verify the stat exists in the registry
        var newStat = StatRegistry.getStat(statId);
        if (newStat == null) return false;

        if (!ItemStatManager.canAddStat(equipment, newStat.getAffixType())) return false;

        // Check for duplicates (same stat ID or same underlying attribute)
        var existingStats = ItemStatManager.getStats(equipment);
        for (var stat : existingStats) {
            if (stat.statId.equals(statId)) return false;

            var existingStat = StatRegistry.getStat(stat.statId);
            if (existingStat != null && existingStat.getGroupId().equals(newStat.getGroupId())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculate the tier based on current stat count.
     * 0 stats -> tier 5, 1 stat -> tier 4, ..., 4 stats -> tier 1
     */
    public static int getTierForStatCount(int statCount) {
        return Math.max(1, 5 - statCount);
    }

    /**
     * Returns the Formatting color for a given tier.
     * Shared across all tome items and stat display code.
     */
    public static Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1, 2 -> Formatting.GOLD;
            case 3, 4 -> Formatting.AQUA;
            case 5, 6 -> Formatting.GREEN;
            case 7, 8 -> Formatting.GRAY;
            case 9, 10 -> Formatting.DARK_GRAY;
            default   -> Formatting.WHITE;
        };
    }

    /**
     * Helper to add a standardized description to specialized tomes.
     */
    protected void addTomeDescription(Consumer<Text> textConsumer) {
        textConsumer.accept(Text.literal("Upgrades a magic item to a rare item,").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("adding a guaranteed modifier").formatted(Formatting.GRAY));
    }

    protected void addStatRange(Consumer<Text> textConsumer, EquipmentType type, String label) {
        String statId = getStatIdForEquipment(type);
        if (statId != null) {
            CustomStat stat = StatRegistry.getStat(statId);
            if (stat != null) {
                int tierValue = 3;
                int maxTier = 1;
                if (this instanceof ElementalTomeItem et) {
                    switch (et.getTier()) {
                        case NORMAL -> {
                            tierValue = 4;
                            maxTier = 4;
                        }
                        case GREATER -> tierValue = 3;
                        case PERFECT -> tierValue = 1;
                    }
                } else if (this instanceof HealthBonusTomeItem || this instanceof SwiftnessTomeItem) {
                    tierValue = 4;
                    maxTier = 4;
                }
                String range = stat.getComponents().get(0).getFormattedRange(tierValue, maxTier, stat.getEquipmentType());
                textConsumer.accept(Text.literal(label + ": ").formatted(Formatting.GRAY)
                        .append(Text.literal(range).formatted(Formatting.WHITE)));
            }
        }
    }

    /**
     * Check if the equipment type is a weapon.
     */
    public static boolean isWeapon(EquipmentType type) {
        return switch (type) {
            case SWORD, SPEAR, MACE, BOW, CROSSBOW, TRIDENT -> true;
            default -> false;
        };
    }

    /**
     * Check if the equipment type is armor (excluding elytra/shield).
     */
    public static boolean isArmor(EquipmentType type) {
        return switch (type) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> true;
            default -> false;
        };
    }
}
