package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.StatusEffectStatManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(DrawContext.class)
public class StatusEffectTooltipMixin {

    @ModifyVariable(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private List<Text> appendStatusEffectDescriptionToTooltip4(List<Text> tooltip) {
        return cranks$enrichTooltip(tooltip);
    }

    @ModifyVariable(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 0
    )
    private List<Text> appendStatusEffectDescriptionToTooltip5(List<Text> tooltip) {
        return cranks$enrichTooltip(tooltip);
    }

    @Unique
    private static List<Text> cranks$enrichTooltip(List<Text> tooltip) {
        if (tooltip == null || tooltip.isEmpty()) return tooltip;

        // Never modify tooltips inside the Compendium / Passive Skill Tree screen
        if (net.minecraft.client.MinecraftClient.getInstance().currentScreen instanceof com.cranks.dungeons.client.compendium.CompendiumScreen) {
            return tooltip;
        }

        // Filter out empty lines (such as empty duration strings for infinite effects)
        List<Text> cleaned = new ArrayList<>();
        for (Text line : tooltip) {
            if (line != null && !line.getString().trim().isEmpty()) {
                cleaned.add(line);
            }
        }
        if (cleaned.isEmpty()) return tooltip;

        Text titleLine = cleaned.get(0);
        String title = titleLine.getString().trim();
        List<Text> descs = StatusEffectStatManager.getDescriptionsByTitle(title);
        if (descs != null && !descs.isEmpty()) {
            for (Text line : cleaned) {
                if (line.getString().equals(descs.get(0).getString())) {
                    return cleaned; // Already has description
                }
            }
            cleaned.addAll(descs);
            return cleaned;
        }

        return cleaned;
    }
}
