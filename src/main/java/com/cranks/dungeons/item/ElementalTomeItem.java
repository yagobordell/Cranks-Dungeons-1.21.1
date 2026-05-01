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
        FIRE("Fire"),
        COLD("Cold"),
        LIGHTNING("Lightning"),
        VOID("Void"),
        PHYSICAL("Physical");

        private final String displayName;

        Element(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Element element;
    private final boolean isGreater;

    public ElementalTomeItem(Element element, boolean isGreater, CustomRarity rarity, Settings settings) {
        super(rarity, settings);
        this.element = element;
        this.isGreater = isGreater;
    }

    public boolean isGreater() {
        return isGreater;
    }

    @Override
    public String getStatIdForEquipment(EquipmentType equipType) {
        String elementStr = element.name().toLowerCase();
        String typeStr = equipType.name().toLowerCase();

        if (isGreater) {
            // Greater tomes ONLY give damage percent on all slots
            if (element == Element.PHYSICAL) {
                return typeStr + "_attack_damage_percent";
            }
            return typeStr + "_" + elementStr + "_damage_percent";
        } else {
            if (isWeapon(equipType)) {
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

        if (isGreater) {
            // Greater tomes only show damage stats
            addStatRange(textConsumer, EquipmentType.SWORD, "Weapons");
        } else {
            addStatRange(textConsumer, EquipmentType.SWORD, "Weapons");
            addStatRange(textConsumer, EquipmentType.CHESTPLATE, "Armor");
        }

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
