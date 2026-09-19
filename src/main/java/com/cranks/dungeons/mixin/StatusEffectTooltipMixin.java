package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.StatusEffectStatManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(DrawContext.class)
public class StatusEffectTooltipMixin {

    @ModifyVariable(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 1
    )
    private List<Text> appendStatusEffectDescriptionToTooltip4(List<Text> tooltip) {
        return cranks$enrichTooltip(tooltip);
    }

    @ModifyVariable(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 1
    )
    private List<Text> appendStatusEffectDescriptionToTooltip5(List<Text> tooltip) {
        return cranks$enrichTooltip(tooltip);
    }

    @Unique
    private static List<Text> cranks$enrichTooltip(List<Text> tooltip) {
        // Never modify tooltips inside the Compendium / Passive Skill Tree screen
        if (net.minecraft.client.MinecraftClient.getInstance().currentScreen instanceof com.cranks.dungeons.client.compendium.CompendiumScreen) {
            return tooltip;
        }
        return StatusEffectStatManager.enrichStatusEffectTooltip(tooltip);
    }
}
