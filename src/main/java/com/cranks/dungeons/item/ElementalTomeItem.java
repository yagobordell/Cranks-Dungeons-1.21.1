package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ElementalTomeItem extends AbstractTomeItem {

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

    public ElementalTomeItem(Element element, boolean isGreater, CustomRarity customRarity, Settings settings) {
        super(customRarity, settings);
        this.element = element;
        this.isGreater = isGreater;
    }

    public Element getElement() {
        return element;
    }

    public boolean isGreater() {
        return isGreater;
    }

    private static String getEquipmentPrefix(EquipmentType equipType) {
        return equipType.name().toLowerCase();
    }

    /**
     * Get the stat ID this tome applies for the given equipment type.
     * Returns the fully prefixed stat ID like "sword_fire_damage" or "helmet_fire_resistance".
     * Returns null if the tome cannot be applied to this equipment type.
     */
    @Override
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
     * Override canApplyTo to enforce weapon-only / weapon+armor restrictions.
     */
    @Override
    public boolean canApplyTo(ItemStack equipment) {
        var equipTypeOpt = EquipmentType.getTypeForItem(equipment);
        if (equipTypeOpt.isEmpty()) return false;

        EquipmentType equipType = equipTypeOpt.get();

        // Greater tomes are weapon-only
        if (isGreater && !isWeapon(equipType)) return false;

        // Normal tomes work on weapons and armor
        if (!isGreater && !isWeapon(equipType) && !isArmor(equipType)) return false;

        // Delegate remaining checks to parent
        return super.canApplyTo(equipment);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              net.minecraft.component.type.TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        textConsumer.accept(Text.literal("Power sealed within runes").formatted(Formatting.AQUA, Formatting.ITALIC));
        if (isGreater) {
            textConsumer.accept(Text.literal("▶ On Weapons:").formatted(Formatting.WHITE));
            String statDisplayName = "Increased " + element.getDisplayName() + " Damage";
            addTierBreakdown(textConsumer, statDisplayName, Formatting.WHITE);
        } else {
            // Weapon stats
            textConsumer.accept(Text.literal("▶ On Weapons:").formatted(Formatting.WHITE));
            String weaponStatName = element.getDisplayName() + " Damage";
            addTierBreakdown(textConsumer, weaponStatName, Formatting.WHITE);

            textConsumer.accept(Text.empty());

            // Armor stats
            textConsumer.accept(Text.literal("▶ On Armor:").formatted(Formatting.WHITE));
            String armorStatName = element.getDisplayName() + " Resistance";
            addTierBreakdown(textConsumer, armorStatName, Formatting.WHITE);
        }

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}