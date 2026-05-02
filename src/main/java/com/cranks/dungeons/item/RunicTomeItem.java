package com.cranks.dungeons.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RunicTomeItem extends Item {

    public enum TomeType {
        TRANSMUTATION("Tome of Transmutation", Formatting.AQUA, "Upgrades a Normal item to a Magic with 1 rune"),
        AUGMENTATION("Tome of Augmentation", Formatting.AQUA, "Augments a Magic item with a new random rune"),
        REGAL("Regal Tome", Formatting.YELLOW, "Upgrades a Magic item to a Rare item, adding 1 rune"),
        ALCHEMY("Tome of Alchemy", Formatting.YELLOW, "Upgrades a Normal item to a Rare item with 4 runes"),
        EXALTED("Exalted Tome", Formatting.YELLOW, "Augments a Rare item with a new random rune");

        private final String displayName;
        private final Formatting color;
        private final String description;

        TomeType(String displayName, Formatting color, String description) {
            this.displayName = displayName;
            this.color = color;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Formatting getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }
    }

    private final TomeType tomeType;

    public RunicTomeItem(TomeType tomeType, Settings settings) {
        super(settings);
        this.tomeType = tomeType;
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
        textConsumer.accept(Text.literal(tomeType.getDescription()).formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
