package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ElementalTomeItem extends Item {

    public enum Element {
        FIRE("Fire", Formatting.RED),
        COLD("Cold", Formatting.AQUA),
        LIGHTNING("Lightning", Formatting.DARK_BLUE),
        VOID("Void", Formatting.DARK_PURPLE),
        PHYSICAL("Physical", Formatting.WHITE);

        private final String displayName;
        private final Formatting color;

        Element(String displayName, Formatting color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public Formatting getColor() { return color; }

        public String getDamageSuffix() {
            if (this == PHYSICAL) return "attack_damage";
            return name().toLowerCase() + "_damage";
        }

        public String getResistanceSuffix() {
            if (this == PHYSICAL) return "armor";
            return name().toLowerCase() + "_resistance";
        }

        public String getDamagePercentSuffix() {
            if (this == PHYSICAL) return "attack_damage_percent";
            return name().toLowerCase() + "_damage_percent";
        }
    }

    private final Element element;
    private final boolean isGreater;
    private final CustomRarity customRarity;

    public ElementalTomeItem(Element element, boolean isGreater, CustomRarity customRarity, Settings settings) {
        super(settings);
        this.element = element;
        this.isGreater = isGreater;
        this.customRarity = customRarity;
    }

    public Element getElement() {
        return element;
    }

    public boolean isGreater() {
        return isGreater;
    }

    public CustomRarity getCustomRarity() {
        return customRarity;
    }

    private static String getEquipmentPrefix(EquipmentType equipType) {
        return equipType.name().toLowerCase();
    }

    /**
     * Get the stat ID this tome applies for the given equipment type.
     * Returns the fully prefixed stat ID like "sword_fire_damage" or "helmet_fire_resistance".
     * Returns null if the tome cannot be applied to this equipment type.
     */
    public String getStatIdForEquipment(EquipmentType equipType) {
        String prefix = getEquipmentPrefix(equipType);

        if (isGreater) {
            // Greater tomes: percent damage, weapons only
            if (!isWeapon(equipType)) return null;
            return prefix + "_" + element.getDamagePercentSuffix();
        } else {
            // Normal tomes: damage for weapons, resistance/armor for armor
            if (isWeapon(equipType)) {
                return prefix + "_" + element.getDamageSuffix();
            } else if (isArmor(equipType)) {
                return prefix + "_" + element.getResistanceSuffix();
            }
            return null;
        }
    }

    /**
     * Calculate the tier based on current stat count.
     * 0 stats -> tier 5, 1 stat -> tier 4, ..., 4 stats -> tier 1
     */
    public static int getTierForStatCount(int statCount) {
        return Math.max(1, 5 - statCount);
    }

    /**
     * Check if equipment can accept this tome.
     */
    public boolean canApplyTo(ItemStack equipment) {
        var equipTypeOpt = EquipmentType.getTypeForItem(equipment);
        if (equipTypeOpt.isEmpty()) return false;

        EquipmentType equipType = equipTypeOpt.get();

        // Greater tomes are weapon-only
        if (isGreater && !isWeapon(equipType)) return false;

        // Normal tomes work on weapons and armor
        if (!isGreater && !isWeapon(equipType) && !isArmor(equipType)) return false;

        // Check if can add more stats
        if (!ItemStatManager.canAddStat(equipment)) return false;

        // Check if the stat already exists on the item
        String statId = getStatIdForEquipment(equipType);
        if (statId == null) return false;

        // Verify the stat actually exists in the registry
        if (com.cranks.dungeons.stat.StatRegistry.getStat(statId) == null) return false;

        var existingStats = ItemStatManager.getStats(equipment);
        for (var stat : existingStats) {
            if (stat.statId.equals(statId)) return false;

            // Also check for same attribute to prevent duplicates
            var existingStat = com.cranks.dungeons.stat.StatRegistry.getStat(stat.statId);
            var newStat = com.cranks.dungeons.stat.StatRegistry.getStat(statId);
            if (existingStat != null && newStat != null
                    && existingStat.getAttribute().equals(newStat.getAttribute())) {
                return false;
            }
        }

        return true;
    }

    public static boolean isWeapon(EquipmentType type) {
        return switch (type) {
            case SWORD, SPEAR, MACE, BOW, CROSSBOW, TRIDENT -> true;
            default -> false;
        };
    }

    public static boolean isArmor(EquipmentType type) {
        return switch (type) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> true;
            default -> false;
        };
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.empty()
                .append(super.getName(stack))
                .append(Text.literal(" "))
                .append(Text.literal("[ " + customRarity.getName() + " ]").formatted(customRarity.getColor(), Formatting.BOLD));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Power sealed within runes").formatted(Formatting.AQUA, Formatting.ITALIC));
        if (isGreater) {
            textConsumer.accept(Text.literal("▶ On Weapons:").formatted(Formatting.WHITE, Formatting.BOLD));
            String statDisplayName = "Increased " + element.getDisplayName() + " Damage";
            addTierBreakdown(textConsumer, statDisplayName, Formatting.WHITE);
        } else {
            // Weapon stats
            textConsumer.accept(Text.literal("▶ On Weapons:").formatted(Formatting.WHITE, Formatting.BOLD));
            String weaponStatName = element.getDisplayName() + " Damage";
            addTierBreakdown(textConsumer, weaponStatName, Formatting.WHITE);

            textConsumer.accept(Text.empty());

            // Armor stats
            textConsumer.accept(Text.literal("▶ On Armor:").formatted(Formatting.WHITE, Formatting.BOLD));
            String armorStatName = element.getDisplayName() + " Resistance";
            addTierBreakdown(textConsumer, armorStatName, Formatting.WHITE);
        }

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }

    private String getWeaponDisplayName() {
        if (element == Element.PHYSICAL) return "Attack Damage";
        return element.getDisplayName() + " Damage";
    }

    private String getArmorDisplayName() {
        if (element == Element.PHYSICAL) return "Armor";
        return element.getDisplayName() + " Resistance";
    }

    private String getGreaterDisplayName() {
        if (element == Element.PHYSICAL) return "Increased Attack Damage";
        return "Increased " + element.getDisplayName() + " Damage";
    }

    private void addTierBreakdown(Consumer<Text> textConsumer, String statName, Formatting statColor) {
        for (int statCount = 0; statCount <= 4; statCount++) {
            int tier = getTierForStatCount(statCount);
            Formatting tierColor = getTierColor(tier);
            textConsumer.accept(Text.literal("  " + statCount + " stats → ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(statName).formatted(statColor))
                    .append(Text.literal(" [T" + tier + "]").formatted(tierColor)));
        }
    }

    private static Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1 -> Formatting.GRAY;
            case 2 -> Formatting.GRAY;
            case 3 -> Formatting.GREEN;
            case 4 -> Formatting.GREEN;
            case 5 -> Formatting.GOLD;
            default -> Formatting.WHITE;
        };
    }
}