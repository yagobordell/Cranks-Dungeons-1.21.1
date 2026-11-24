package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.RarityTooltipContext;
import com.cranks.dungeons.client.tooltip.TooltipTheme;
import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipBackgroundRenderer.class)
abstract class TooltipBackgroundMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private static void cranks$darkTooltip(DrawContext context,int x,int y,int width,int height,int z,CallbackInfo ci) {
        CustomRarity rarity=RarityTooltipContext.get();
        TooltipTheme.frame(context,x,y,width,height,z,rarity==null?CustomRarity.NORMAL:rarity);
        ci.cancel();
    }
}
