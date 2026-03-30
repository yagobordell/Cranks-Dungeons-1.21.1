package com.cranks.dungeons.party;

import com.cranks.dungeons.network.ModNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyHealthSyncHandler {
    
    private static final Map<UUID, HealthData> lastHealthData = new HashMap<>();
    
    // Sync every 2 ticks (0.1 seconds at 20 TPS) for near real-time updates
    private static final int SYNC_INTERVAL = 2;
    private static int tickCounter = 0;
    
    private static class HealthData {
        float health;
        float maxHealth;
        float shield;
        float maxShield;
        
        HealthData(float health, float maxHealth, float shield, float maxShield) {
            this.health = health;
            this.maxHealth = maxHealth;
            this.shield = shield;
            this.maxShield = maxShield;
        }
    }
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= SYNC_INTERVAL) {
                tickCounter = 0;
                syncPartyHealth(server);
            }
        });
    }
    
    private static void syncPartyHealth(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerUuid = player.getUuid();
            
            Party party = PartyManager.getParty(playerUuid).orElse(null);
            if (party == null) {
                lastHealthData.remove(playerUuid);
                continue;
            }
            
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float shield = 0;
            if (player instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
                shield = provider.cranksdungeons$getCurrentEnergyShield();
            }
            float maxShield = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
            
            HealthData lastData = lastHealthData.get(playerUuid);
            if (lastData == null || Math.abs(lastData.health - health) > 0.01f || Math.abs(lastData.shield - shield) > 0.01f) {
                lastHealthData.put(playerUuid, new HealthData(health, maxHealth, shield, maxShield));
                ModNetworking.sendPartyHealthUpdate(server, party, playerUuid);
            }
        }
    }
    
    public static void clearPlayerData(UUID playerUuid) {
        lastHealthData.remove(playerUuid);
    }
}
