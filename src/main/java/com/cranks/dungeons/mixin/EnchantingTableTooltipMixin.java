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

        if (lower.contains("channeling")) {
            return new String[]{
                    "enchantment.cranks-dungeons.channeling.desc",
                    "enchantment.cranks-dungeons.channeling.desc2"
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

        if (lower.contains("depth strider")) {
            return new String[]{
                    "enchantment.cranks-dungeons.depth_strider.desc",
                    "enchantment.cranks-dungeons.depth_strider.desc2"
            };
        }

        if (lower.contains("respiration")) {
            return new String[]{
                    "enchantment.cranks-dungeons.respiration.desc",
                    "enchantment.cranks-dungeons.respiration.desc2"
            };
        }

        if (lower.contains("soul speed")) {
            return new String[]{
                    "enchantment.cranks-dungeons.soul_speed.desc",
                    "enchantment.cranks-dungeons.soul_speed.desc2"
            };
        }

        if (lower.contains("aqua affinity")) {
            return new String[]{
                    "enchantment.cranks-dungeons.aqua_affinity.desc",
                    "enchantment.cranks-dungeons.aqua_affinity.desc2"
            };
        }

        return null;
    }
}