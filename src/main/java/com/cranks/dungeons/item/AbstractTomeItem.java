package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
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

        if (!ItemStatManager.canAddStat(equipment)) return false;

        // Verify the stat exists in the registry
        if (StatRegistry.getStat(statId) == null) return false;

        // Check for duplicates (same stat ID or same underlying attribute)
        var existingStats = ItemStatManager.getStats(equipment);
        for (var stat : existingStats) {
            if (stat.statId.equals(statId)) return false;

            var existingStat = StatRegistry.getStat(stat.statId);
            var newStat = StatRegistry.getStat(statId);
            if (existingStat != null && newStat != null
                    && existingStat.getAttribute().equals(newStat.getAttribute())) {
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
            case 1, 2 -> Formatting.GRAY;
            case 3, 4 -> Formatting.GREEN;
            case 5    -> Formatting.GOLD;
            default   -> Formatting.WHITE;
        };
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.empty()
                .append(super.getName(stack))
                .append(Text.literal(" "))
                .append(Text.literal("[ " + customRarity.getName() + " ]").formatted(customRarity.getColor()));
    }

    /**
     * Helper to add a tier breakdown section to a tooltip.
     * Shows how many existing stats map to what tier.
     */
    protected void addTierBreakdown(Consumer<Text> textConsumer, String statName, Formatting statColor) {
        for (int statCount = 0; statCount <= 4; statCount++) {
            int tier = getTierForStatCount(statCount);
            Formatting tierColor = getTierColor(tier);
            textConsumer.accept(Text.literal("  " + statCount + " stats → ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(statName).formatted(statColor))
                    .append(Text.literal(" [T" + tier + "]").formatted(tierColor)));
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
