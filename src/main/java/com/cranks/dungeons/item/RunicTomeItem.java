package com.cranks.dungeons.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RunicTomeItem extends Item {

    public enum TomeType {
        TRANSMUTATION("Tome of Transmutation", Formatting.AQUA, "Upgrades a Normal item", "to a Magic with 1 rune", 1),
        GREATER_TRANSMUTATION("Greater Tome of Transmutation", Formatting.YELLOW,
                "Upgrades a Normal item", "to a Magic with 1 rune", 35),
        PERFECT_TRANSMUTATION("Perfect Tome of Transmutation", Formatting.GOLD,
                "Upgrades a Normal item", "to a Magic with 1 rune", 70),

        AUGMENTATION("Tome of Augmentation", Formatting.AQUA, "Augments a Magic item", "with a new random rune", 1),
        GREATER_AUGMENTATION("Greater Tome of Augmentation", Formatting.YELLOW,
                "Augments a Magic item", "with a new random rune", 35),
        PERFECT_AUGMENTATION("Perfect Tome of Augmentation", Formatting.GOLD,
                "Augments a Magic item", "with a new random rune", 70),

        REGAL("Regal Tome", Formatting.YELLOW, "Upgrades a Magic item", "to a Rare item, adding 1 rune", 1),
        GREATER_REGAL("Greater Regal Tome", Formatting.GOLD, "Upgrades a Magic item", "to a Rare item, adding 1 rune", 35),
        PERFECT_REGAL("Perfect Regal Tome", Formatting.LIGHT_PURPLE,
                "Upgrades a Magic item", "to a Rare item, adding 1 rune", 70),

        ALCHEMY("Tome of Alchemy", Formatting.YELLOW, "Upgrades a Normal item", "to a Rare item with 4 runes", 1),

        EXALTED("Exalted Tome", Formatting.YELLOW, "Augments a Rare item", "with a new random rune", 1),
        GREATER_EXALTED("Greater Exalted Tome", Formatting.GOLD, "Augments a Rare item", "with a new random rune", 35),
        PERFECT_EXALTED("Perfect Exalted Tome", Formatting.LIGHT_PURPLE, "Augments a Rare item", "with a new random rune",
                70);

        private final String displayName;
        private final Formatting color;
        private final String desc1;
        private final String desc2;
        private final int minLevel;

        TomeType(String displayName, Formatting color, String desc1, String desc2, int minLevel) {
            this.displayName = displayName;
            this.color = color;
            this.desc1 = desc1;
            this.desc2 = desc2;
            this.minLevel = minLevel;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Formatting getColor() {
            return color;
        }

        public String getDesc1() {
            return desc1;
        }

        public String getDesc2() {
            return desc2;
        }

        public int getMinLevel() {
            return minLevel;
        }
    }

    private final TomeType tomeType;

    public RunicTomeItem(TomeType tomeType, Settings settings) {
        super(settings);
        this.tomeType = tomeType;
    }

    public int getMinStatLevel() {
        return tomeType.getMinLevel();
    }

    public TomeType getTomeType() {
        return tomeType;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal(tomeType.getDisplayName()).formatted(tomeType.getColor());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
            net.minecraft.component.type.TooltipDisplayComponent displayComponent,
            java.util.function.Consumer<Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {
        textConsumer.accept(Text.literal(tomeType.getDesc1()).formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal(tomeType.getDesc2()).formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
