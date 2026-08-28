package com.cranks.dungeons.event;

import com.cranks.dungeons.network.ModNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerInfoSyncHandler {
    private static int tickCounter = 0;
    private static final int SYNC_INTERVAL = 20;
    private static final java.util.Map<java.util.UUID, String> LAST_STATE = new java.util.HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % SYNC_INTERVAL == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    var passive = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                    String leader = com.cranks.dungeons.party.PartyManager.getParty(player.getUuid())
                            .map(party -> party.getLeader().toString()).orElse("");
                    String state = passive.getPlayerClass() + '|' + player.experienceLevel + '|' + leader;
                    if (!state.equals(LAST_STATE.put(player.getUuid(), state))) {
                        ModNetworking.syncPlayerInfoToTrackers(player, server);
                    }
                }
                LAST_STATE.keySet().removeIf(uuid -> server.getPlayerManager().getPlayer(uuid) == null);
            }
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            LAST_STATE.clear();
            tickCounter = 0;
        });
    }
}
