package com.cranks.dungeons.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DrawContext.class)
public class EnchantingTableTooltipMixin {

    @ModifyVariable(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private List<Text> addEnchantmentDescriptions(List<Text> tooltip) {
        if (tooltip == null || tooltip.isEmpty()) return tooltip;

        List<Text> newTooltip = new ArrayList<>();

        for (Text line : tooltip) {
            newTooltip.add(line);

            String lineText = line.getString();
            String[] descKeys = getDescriptionKeys(lineText);

            if (descKeys != null) {
                for (String descKey : descKeys) {
                    Text desc = Text.translatable(descKey).formatted(Formatting.GRAY, Formatting.ITALIC);
                    if (!desc.getString().equals(descKey)) {
                        newTooltip.add(desc);
                    }
                }
            }
        }

        return newTooltip;
    }

    private String[] getDescriptionKeys(String enchantmentLine) {
        String lower = enchantmentLine.toLowerCase();

        if (lower.contains("lunge")) {
            return new String[]{
                    "enchantment.cranks-dungeons.lunge.desc",
                    "enchantment.cranks-dungeons.lunge.desc2"
            };
        }
        if (lower.contains("riptide")) {
            return new String[]{
                    "enchantment.cranks-dungeons.riptide.desc",
                    "enchantment.cranks-dungeons.riptide.desc2"
            };
        }
        if (lower.contains("wind burst")) {
            return new String[]{
                    "enchantment.cranks-dungeons.wind_burst.desc",
                    "enchantment.cranks-dungeons.wind_burst.desc2"
            };
        }

        if (lower.contains("infinity")) {
            return new String[]{
                    "enchantment.cranks-dungeons.infinity.desc",
                    "enchantment.cranks-dungeons.infinity.desc2"
            };
        }

        if (lower.contains("multishot")) {
            return new String[]{
                    "enchantment.cranks-dungeons.multishot.desc",
                    "enchantment.cranks-dungeons.multishot.desc2"
            };
        }

        if (lower.contains("silk touch")) {
            return new String[]{
                    "enchantment.cranks-dungeons.silk_touch.desc",
                    "enchantment.cranks-dungeons.silk_touch.desc2"
            };
        }

        if (lower.contains("frost walker")) {
            return new String[]{
                    "enchantment.cranks-dungeons.frost_walker.desc",
                    "enchantment.cranks-dungeons.frost_walker.desc2"
            };
        }

        if (lower.contains("loyalty")) {
            return new String[]{
                    "enchantment.cranks-dungeons.loyalty.desc",
                    "enchantment.cranks-dungeons.loyalty.desc2"
            };
        }

        return null;
    }
}