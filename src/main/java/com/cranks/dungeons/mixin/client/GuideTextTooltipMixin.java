package com.cranks.dungeons.mixin.client;
import com.cranks.dungeons.client.guide.GuideInteraction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(OrderedTextTooltipComponent.class)
abstract class GuideTextTooltipMixin {
    @Shadow @Final private OrderedText text;
    @Inject(method="drawText",at=@At("TAIL"))
    private void cranks$links(TextRenderer font,int x,int y,Matrix4f matrix,VertexConsumerProvider.Immediate consumers,CallbackInfo ci){GuideInteraction.record(text,font,x,y,null);}
}
