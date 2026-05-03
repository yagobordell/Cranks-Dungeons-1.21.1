package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ElementalTomeItem extends AbstractTomeItem {

    public enum Element {
        FIRE("Flames"),
        COLD("Ice"),
        LIGHTNING("Electricity"),
        VOID("Ruin"),
        PHYSICAL("Abrasion");

        private final String displayName;

        Element(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Tier {
        NORMAL(1, CustomRarity.RARE, ""),
        GREATER(40, CustomRarity.UNIQUE, "greater_");

        private final int minLevel;
        private final CustomRarity rarity;
        private final String prefix;

        Tier(int minLevel, CustomRarity rarity, String prefix) {
            this.minLevel = minLevel;
            this.rarity = rarity;
            this.prefix = prefix;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public CustomRarity getRarity() {
            return rarity;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private final Element element;
    private final Tier tier;

    public ElementalTomeItem(Element element, Tier tier, Settings settings) {
        super(tier.getRarity(), settings);
        this.element = element;
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public int getMinStatLevel() {
        return tier.getMinLevel();
    }

    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        String elementStr = element.name().toLowerCase();
        String typeStr = equipType.name().toLowerCase();

        // Greater/Perfect tomes give "Extra Damage as Element" on weapons
        if (tier != Tier.NORMAL) {
            if (isWeapon(equipType)) {
                if (element == Element.PHYSICAL) {
                    return typeStr + "_attack_extra_damage";
                }
                return typeStr + "_" + elementStr + "_extra_damage";
            } else if (isArmor(equipType)) {
                // ONLY Physicality tomes give armor % at higher tiers
                if (element == Element.PHYSICAL) {
                    return typeStr + "_armor_percent";
                }
                return null;
            }
            return null;
        } else {
            // Normal tomes give flat damage on weapons or flat resistance/armor on armor
            if (isWeapon(equipType)) {
                if (element == Element.PHYSICAL) {
                    return typeStr + "_physical_damage";
                }
                return typeStr + "_" + elementStr + "_damage";
            } else if (isArmor(equipType)) {
                if (element == Element.PHYSICAL) {
                    return typeStr + "_armor";
                }
                return typeStr + "_" + elementStr + "_resistance";
            }
            return null;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
            net.minecraft.component.type.TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        addTomeDescription(textConsumer);

        if (tier != Tier.NORMAL) {
            // Greater tomes always show weapon stats
            addStatRange(textConsumer, EquipmentType.SWORD, "Weapons");
            // Only Physicality Greater/Perfect tomes show armor stats
            if (element == Element.PHYSICAL) {
                addStatRange(textConsumer, EquipmentType.CHESTPLATE, "Armor");
            }
        } else {
            addStatRange(textConsumer, EquipmentType.SWORD, "Weapons");
            addStatRange(textConsumer, EquipmentType.CHESTPLATE, "Armor");
        }

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
