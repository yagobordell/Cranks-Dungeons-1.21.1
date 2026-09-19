package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.network.OpenMountCargoPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opens the shared mount cargo screen from the normal inventory key. */
@Mixin(MinecraftClient.class)
public abstract class MountInventoryKeyMixin {
    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void cranks$openCustomMountInventory(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.player == null
                || client.currentScreen != null
                || !(client.player.getVehicle() instanceof LivingEntity mount)
                || !MountManager.usesCustomCargoScreen(mount)) {
            return;
        }
        // Do not consume the key until we know this is one of our custom
        // mounts; vanilla horses and the normal player inventory must retain
        // their own inventory-key behavior.
        if (!client.options.inventoryKey.wasPressed()
                || !ClientPlayNetworking.canSend(OpenMountCargoPayload.ID)) {
            return;
        }

        ClientPlayNetworking.send(new OpenMountCargoPayload(mount.getId()));
        // Vanilla would otherwise open the player's inventory because custom
        // mounts do not advertise a vanilla riding inventory.
        ci.cancel();
    }
}
