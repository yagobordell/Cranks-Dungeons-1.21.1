package com.cranks.dungeons.mixin.client;
import com.cranks.dungeons.client.guide.GuideOverlay;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
abstract class GuideMovementMixin {
    @Inject(method="tick",at=@At("TAIL"))
    private void cranks$readWithoutMoving(CallbackInfo ci){if(GuideOverlay.isOpen()){var input=(Input)(Object)this;input.movementForward=0;input.movementSideways=0;input.jumping=false;input.sneaking=false;}}
}
