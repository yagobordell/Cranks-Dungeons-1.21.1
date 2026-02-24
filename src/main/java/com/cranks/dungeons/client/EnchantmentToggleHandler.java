package com.cranks.dungeons.client;

import com.cranks.dungeons.network.ToggleEnchantmentDisablePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EnchantmentToggleHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (ModKeybinds.TOGGLE_ENCHANTMENT_DISABLE.wasPressed()) {
                ClientPlayNetworking.send(new ToggleEnchantmentDisablePayload());
            }
        });
    }
}