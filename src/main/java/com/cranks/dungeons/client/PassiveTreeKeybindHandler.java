package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumScreen;
import com.cranks.dungeons.client.compendium.CompendiumTabRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class PassiveTreeKeybindHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (ModKeybinds.OPEN_PASSIVE_TREE.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CompendiumScreen(CompendiumTabRegistry.PASSIVE_TREE_TAB_ID));
                }
            }
            while (ModKeybinds.OPEN_WORLD_TREE.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CompendiumScreen(CompendiumTabRegistry.WORLD_TREE_TAB_ID));
                }
            }
            while (ModKeybinds.OPEN_COMPENDIUM.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CompendiumScreen(CompendiumTabRegistry.GUIDE_TAB_ID));
                }
            }
        });
    }
}
