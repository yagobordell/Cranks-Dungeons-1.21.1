package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.RarityTooltipContext;
import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts TooltipBackgroundRenderer.render() to draw rarity-coloured
 * background and frame sprites when an RPG equipment item is hovered.
 *
 * Actual runtime signature (from crash log):
 *   render(DrawContext, int x, int y, int width, int height, Identifier tooltipStyle)
 */
@Mixin(TooltipBackgroundRenderer.class)
public abstract class TooltipBackgroundMixin {

    private static final Identifier BG_NORMAL  = Identifier.of("cranks-dungeons", "tooltip/background_normal");
    private static final Identifier BG_MAGIC   = Identifier.of("cranks-dungeons", "tooltip/background_magic");
    private static final Identifier BG_RARE    = Identifier.of("cranks-dungeons", "tooltip/background_rare");
    private static final Identifier BG_UNIQUE  = Identifier.of("cranks-dungeons", "tooltip/background_unique");

    private static final Identifier FR_NORMAL  = Identifier.of("cranks-dungeons", "tooltip/frame_normal");
    private static final Identifier FR_MAGIC   = Identifier.of("cranks-dungeons", "tooltip/frame_magic");
    private static final Identifier FR_RARE    = Identifier.of("cranks-dungeons", "tooltip/frame_rare");
    private static final Identifier FR_UNIQUE  = Identifier.of("cranks-dungeons", "tooltip/frame_unique");

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void cranks$customBackground(
            DrawContext context,
            int x, int y, int width, int height,
            Identifier tooltipStyle,
            CallbackInfo ci) {

        CustomRarity rarity = RarityTooltipContext.get();
        if (rarity == null) rarity = CustomRarity.NORMAL; // default all tooltips to our normal frame

        Identifier bg, frame;
        switch (rarity) {
            case MAGIC  -> { bg = BG_MAGIC;  frame = FR_MAGIC;  }
            case RARE   -> { bg = BG_RARE;   frame = FR_RARE;   }
            case UNIQUE -> { bg = BG_UNIQUE; frame = FR_UNIQUE; }
            default     -> { bg = BG_NORMAL; frame = FR_NORMAL; }
        }

        // Vanilla uses 3px padding on sides, +1 extra px on height for bottom border
        int bx = x - 3;
        int by = y - 3;
        int bw = width  + 6;
        int bh = height + 7;

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, bg,    bx, by, bw, bh);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, frame, bx, by, bw, bh);

        ci.cancel();
    }
}
