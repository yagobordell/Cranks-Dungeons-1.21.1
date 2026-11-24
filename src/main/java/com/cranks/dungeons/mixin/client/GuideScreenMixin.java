package com.cranks.dungeons.mixin.client;
import com.cranks.dungeons.client.guide.GuideInteraction;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Screen.class)
abstract class GuideScreenMixin {
    @Inject(method="renderWithTooltip",at=@At("HEAD"))
    private void cranks$begin(DrawContext context,int mx,int my,float delta,CallbackInfo ci){GuideInteraction.beginFrame((Screen)(Object)this,mx,my);}
    @WrapOperation(method="renderWithTooltip",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"))
    private void cranks$pointer(Screen screen,DrawContext context,int mx,int my,float delta,Operation<Void> original){original.call(screen,context,GuideInteraction.renderMouseX(mx),GuideInteraction.renderMouseY(my),delta);}
    @Inject(method="renderWithTooltip",at=@At("TAIL"))
    private void cranks$end(DrawContext context,int mx,int my,float delta,CallbackInfo ci){GuideInteraction.endFrame(context,delta);}
}
