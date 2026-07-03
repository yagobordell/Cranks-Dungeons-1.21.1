package com.cranks.dungeons.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class HeartbeatSoundMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void cancelHeartbeatIfHealthy(SoundInstance sound, CallbackInfoReturnable<Object> cir) {
        if (sound.getId().getPath().contains("heartbeat")) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                float health = client.player.getHealth();
                float maxHealth = client.player.getMaxHealth();
                
                if (health >= maxHealth || (health / maxHealth) > 0.3f) {
                    cir.setReturnValue(null);
                }
            }
        }
    }
}
