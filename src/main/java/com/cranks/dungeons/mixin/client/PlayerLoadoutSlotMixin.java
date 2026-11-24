package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.PlayerLoadoutScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
abstract class PlayerLoadoutSlotMixin {
    @Shadow protected Slot focusedSlot;
    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
    private void matchPointerBoundsToSlot(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerLoadoutScreen inventory) {
            cir.setReturnValue(inventory.isPointerOverSlot(slot, mouseX, mouseY));
        }else if((Object)this instanceof net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen){
            cir.setReturnValue(mouseX>=x+slot.x-1&&mouseX<x+slot.x+16&&mouseY>=y+slot.y-1&&mouseY<y+slot.y+16);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/gui/DrawContext;III)V"))
    private void highlightWholeSlot(DrawContext context, int x, int y, int z, Operation<Void> original) {
        if ((Object) this instanceof PlayerLoadoutScreen inventory && focusedSlot != null) {
            inventory.drawSlotHover(context, focusedSlot, z);
        } else if((Object)this instanceof net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen && focusedSlot!=null){
            context.fillGradient(net.minecraft.client.render.RenderLayer.getGuiOverlay(),focusedSlot.x-1,focusedSlot.y-1,focusedSlot.x+16,focusedSlot.y+16,0x80FFFFFF,0x80FFFFFF,z);
        } else {
            original.call(context, x, y, z);
        }
    }
}
