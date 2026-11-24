package com.cranks.dungeons.mixin.client;
import com.cranks.dungeons.client.guide.GuideInteraction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(DrawContext.class)
abstract class GuideTooltipTextMixin {
    @Inject(method="drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I",at=@At("TAIL"))
    private void cranks$links(TextRenderer font,OrderedText text,int x,int y,int color,boolean shadow,CallbackInfoReturnable<Integer> cir){GuideInteraction.record(text,font,x,y,(DrawContext)(Object)this);}
}
