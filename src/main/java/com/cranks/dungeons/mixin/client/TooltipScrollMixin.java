package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.tooltip.EquipmentTooltipCard;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
abstract class TooltipScrollMixin {
    @Inject(method="onMouseScroll",at=@At("HEAD"),cancellable=true)
    private void cranks$scrollLargeCard(long window,double horizontal,double vertical,CallbackInfo ci) {
        if(com.cranks.dungeons.client.guide.GuideOverlay.isOpen())return;
        if(MinecraftClient.getInstance().currentScreen!=null&&Screen.hasAltDown()&&EquipmentTooltipCard.scroll(vertical))ci.cancel();
    }
}
