package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.MountWhistleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MountWhistleOutlineMixin {
    @Inject(method="hasOutline",at=@At("HEAD"),cancellable=true)
    private void highlightSelectedMount(Entity entity,CallbackInfoReturnable<Boolean> cir){
        if(((MinecraftClient)(Object)this).currentScreen instanceof MountWhistleScreen screen&&screen.highlights(entity))cir.setReturnValue(true);
    }
}
