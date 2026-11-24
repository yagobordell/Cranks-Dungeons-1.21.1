package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.tooltip.EquipmentTooltipPart;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipComponent.class)
interface EquipmentTooltipComponentMixin {
    @Inject(method="of(Lnet/minecraft/text/OrderedText;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;",at=@At("HEAD"),cancellable=true)
    private static void cranks$itemCardLine(OrderedText text,CallbackInfoReturnable<TooltipComponent> cir) {
        var part=EquipmentTooltipPart.of(text);
        if(part!=null)cir.setReturnValue(part);
    }
}
