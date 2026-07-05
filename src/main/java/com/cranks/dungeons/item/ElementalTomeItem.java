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
        GREATER(40, CustomRarity.UNIQUE, "greater_"),
        PERFECT(80, CustomRarity.UNIQUE, "perfect_");

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
                if (element == Element.VOID) {
                    return null;
                }
                return typeStr + "_" + elementStr + "_extra_damage";
            } else if (isArmor(equipType)) {
                if (element == Element.VOID) {
                    return typeStr + "_physical_taken_as_void";
                }
                return null;
            }
            return null;
        } else {
            // Normal tomes give flat damage on weapons or flat resistance/armor on armor
            if (isWeapon(equipType)) {
                if (element == Element.PHYSICAL) {
                    return typeStr + "_attack_damage";
                }
                if (element == Element.VOID) {
                    return null;
                }
                return typeStr + "_local_" + elementStr + "_damage";
            } else if (isArmor(equipType)) {
                if (element == Element.PHYSICAL) {
                    return null;
                }
                return typeStr + "_" + elementStr + "_resistance";
            }
            return null;
        }
    }

    @Override
    protected void addTomeDescription(Consumer<net.minecraft.text.Text> textConsumer) {
        if (element == Element.VOID && tier == Tier.GREATER) {
            textConsumer.accept(net.minecraft.text.Text.literal("Upgrades a magic item to a rare item,").formatted(net.minecraft.util.Formatting.GRAY));
            textConsumer.accept(net.minecraft.text.Text.literal("adding a guaranteed Void rune to armor").formatted(net.minecraft.util.Formatting.GRAY));
        } else {
            super.addTomeDescription(textConsumer);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
            net.minecraft.component.type.TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {

        addTomeDescription(textConsumer);

        if (tier != Tier.NORMAL) {
            // Greater/Perfect tomes show weapon stats (if applicable)
            addStatRange(textConsumer, EquipmentType.SWORD, "1H Weapons");
            addStatRange(textConsumer, EquipmentType.SPEAR, "2H Weapons");
            // Only Void Greater/Perfect tomes show armor stats
            if (element == Element.VOID) {
                addStatRange(textConsumer, EquipmentType.CHESTPLATE, "Armor");
            }
        } else {
            addStatRange(textConsumer, EquipmentType.SWORD, "1H Weapons");
            addStatRange(textConsumer, EquipmentType.SPEAR, "2H Weapons");
            if (element != Element.PHYSICAL) {
                addStatRange(textConsumer, EquipmentType.CHESTPLATE, "Armor");
            }
        }

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
