package com.cranks.dungeons.event;

import com.cranks.dungeons.network.ModNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerInfoSyncHandler {
    private static int tickCounter = 0;
    private static final int SYNC_INTERVAL = 40; // Sync info every 2 seconds

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % SYNC_INTERVAL == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    ModNetworking.syncPlayerInfoToTrackers(player, server);
                }
            }
        });
    }
}
