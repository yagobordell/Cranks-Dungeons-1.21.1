package com.cranks.dungeons.mixin.client;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;
@Mixin(DrawContext.class)
public interface GuideTooltipInvoker {
    @Invoker("drawTooltip") void cranks$drawGuideTooltip(TextRenderer font,List<TooltipComponent> components,int x,int y,TooltipPositioner positioner);
}
