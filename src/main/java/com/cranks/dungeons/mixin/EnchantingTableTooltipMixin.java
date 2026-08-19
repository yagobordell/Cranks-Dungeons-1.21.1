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
            String lineText = line.getString();
            String lowerLine = lineText.toLowerCase();

            boolean isLevelCost = (lowerLine.contains("level") || lowerLine.contains("cost"))
                    && !lowerLine.contains("lapis");

            boolean isLapisCost = lowerLine.contains("lapis");

            if (isLevelCost) {
                continue;
            }
            else if (isLapisCost) {
                String modifiedLine = lineText.replaceFirst("\\d+", "32");
                newTooltip.add(Text.literal(modifiedLine).setStyle(line.getStyle()));
            }
            else {
                String cleanedLine = lineText.replace(" . . . ?", "").replace("... ?", "").trim();

                if (!cleanedLine.isEmpty()) {
                    newTooltip.add(Text.literal(cleanedLine).setStyle(line.getStyle()));
                }
            }

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

        String[] types = {
            "adept", "body", "desert", "glacial", "inspiration", "iron", "stone", "storm", "vision"
        };

        for (String type : types) {
            if (lower.contains("perfect " + type)) {
                return new String[]{
                    "enchantment.cranks-dungeons.perfect_" + type + ".desc",
                    "enchantment.cranks-dungeons.perfect_" + type + ".desc2"
                };
            }
            if (lower.contains("greater " + type)) {
                return new String[]{
                    "enchantment.cranks-dungeons.greater_" + type + ".desc",
                    "enchantment.cranks-dungeons.greater_" + type + ".desc2"
                };
            }
            if (lower.contains(type)) {
                return new String[]{
                    "enchantment.cranks-dungeons." + type + ".desc",
                    "enchantment.cranks-dungeons." + type + ".desc2"
                };
            }
        }

        if (lower.contains("silk touch")) {
            return new String[]{
                "enchantment.cranks-dungeons.silk_touch.desc",
                "enchantment.cranks-dungeons.silk_touch.desc2"
            };
        }

        if (lower.contains("vein mining")) {
            return new String[]{
                "enchantment.cranks-dungeons.vein_mining.desc",
                "enchantment.cranks-dungeons.vein_mining.desc2"
            };
        }

        return null;
    }
}