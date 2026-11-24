package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.RarityTooltipContext;
import com.cranks.dungeons.client.tooltip.EquipmentTooltipCard;
import com.cranks.dungeons.client.tooltip.EquipmentTooltipPart;
import com.cranks.dungeons.item.CustomRarity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.item.ItemStack;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(DrawContext.class)
abstract class EquipmentTooltipRenderMixin {
    @Unique private ItemStack cranks$tooltipStack=ItemStack.EMPTY;

    @WrapMethod(method="drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V")
    private void cranks$fitPlainText(TextRenderer renderer,List<net.minecraft.text.Text> lines,int x,int y,Operation<Void> original){
        int available=((DrawContext)(Object)this).getScaledWindowWidth()-28;
        original.call(renderer,com.cranks.dungeons.client.tooltip.TooltipWrapping.fit(renderer,lines,available),x,y);
    }

    @WrapMethod(method="drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V")
    private void cranks$fitText(TextRenderer renderer,List<net.minecraft.text.Text> lines,
                               java.util.Optional<net.minecraft.item.tooltip.TooltipData> data,int x,int y,Operation<Void> original){
        int available=((DrawContext)(Object)this).getScaledWindowWidth()-28;
        original.call(renderer,com.cranks.dungeons.client.tooltip.TooltipWrapping.fit(renderer,lines,available),data,x,y);
    }

    @WrapMethod(method="drawItemTooltip")
    private void cranks$itemContext(TextRenderer renderer,ItemStack stack,int x,int y,Operation<Void> original) {
        ItemStack previous=cranks$tooltipStack;CustomRarity previousRarity=RarityTooltipContext.get();
        cranks$tooltipStack=stack;RarityTooltipContext.set(CustomRarity.getRarity(stack));
        try{original.call(renderer,stack,x,y);}finally{cranks$tooltipStack=previous;RarityTooltipContext.set(previousRarity);}
    }

    @WrapMethod(method="drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V")
    private void cranks$layoutCard(TextRenderer renderer,List<TooltipComponent> components,int x,int y,
                                    TooltipPositioner positioner,Operation<Void> original) {
        if(com.cranks.dungeons.client.guide.GuideInteraction.suppressTooltip(x,y))return;
        CustomRarity previous=RarityTooltipContext.get();
        if(!components.isEmpty() && components.getFirst() instanceof EquipmentTooltipPart part && part.kind.equals("title")) {
            var card=new EquipmentTooltipCard(components,renderer,cranks$tooltipStack);
            components=List.of(card);RarityTooltipContext.set(card.rarity);
        } else EquipmentTooltipCard.clearScrollTarget();
        com.cranks.dungeons.client.guide.GuideInteraction.startTooltip(components,renderer,x,y);
        TooltipPositioner padded=(sw,sh,mx,my,w,h)->{
            var position=positioner.getPosition(sw,sh,com.cranks.dungeons.client.guide.GuideInteraction.anchorX(mx),com.cranks.dungeons.client.guide.GuideInteraction.anchorY(my),w,h);
            var fitted=new Vector2i(Math.clamp(position.x(),10,Math.max(10,sw-w-10)),Math.clamp(position.y(),8,Math.max(8,sh-h-8)));
            com.cranks.dungeons.client.guide.GuideInteraction.tooltipBounds(fitted.x(),fitted.y(),w,h);return fitted;
        };
        try{original.call(renderer,components,x,y,padded);com.cranks.dungeons.client.guide.GuideInteraction.finishTooltip();}
        finally{com.cranks.dungeons.client.guide.GuideInteraction.abortTooltip();RarityTooltipContext.set(previous);}
    }
}
