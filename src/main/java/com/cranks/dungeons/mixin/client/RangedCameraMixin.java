package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.RangedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class RangedCameraMixin {
    @Shadow public abstract float getYaw();
    @Shadow public abstract float getPitch();
    @Shadow protected abstract void setRotation(float yaw,float pitch);
    @Inject(method="update",at=@At("TAIL"))
    private void cranks$shotFeedback(BlockView area,Entity entity,boolean thirdPerson,boolean inverse,float delta,CallbackInfo ci) {
        if(!thirdPerson && entity==MinecraftClient.getInstance().player)setRotation(getYaw(),getPitch()-RangedClient.recoil());
    }
}
