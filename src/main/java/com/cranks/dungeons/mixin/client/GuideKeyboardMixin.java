package com.cranks.dungeons.mixin.client;
import com.cranks.dungeons.client.guide.GuideOverlay;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Keyboard.class)
abstract class GuideKeyboardMixin {
    @Inject(method="onChar",at=@At("HEAD"),cancellable=true)
    private void cranks$type(long window,int codepoint,int modifiers,CallbackInfo ci){if(GuideOverlay.isOpen()){GuideOverlay.charTyped(codepoint,modifiers);ci.cancel();}}
}
