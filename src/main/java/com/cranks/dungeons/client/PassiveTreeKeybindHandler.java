package com.cranks.dungeons.client;

import com.cranks.dungeons.client.screen.PassiveSkillTreeScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class PassiveTreeKeybindHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (ModKeybinds.OPEN_PASSIVE_TREE.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PassiveSkillTreeScreen(null));
                }
            }
            while (ModKeybinds.OPEN_WORLD_TREE.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new com.cranks.dungeons.client.screen.WorldSkillTreeScreen(null));
                }
            }
        });
    }
}
