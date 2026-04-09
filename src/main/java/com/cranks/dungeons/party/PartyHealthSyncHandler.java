package com.cranks.dungeons.party;

import com.cranks.dungeons.network.ModNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PartyHealthSyncHandler {
    
    private static final Map<UUID, HealthData> lastHealthData = new HashMap<>();
    private static final Set<UUID> pendingFastSync = new HashSet<>();
    
    // Configurable sync intervals
    private static final int NORMAL_SYNC_INTERVAL = 4; // Check every 0.2 seconds normally (faster for smooth regen)
    private static final int FAST_SYNC_INTERVAL = 1;   // Instant sync after damage (every tick for 3 seconds)
    
    private static int tickCounter = 0;
    private static int currentSyncInterval = NORMAL_SYNC_INTERVAL;
    private static int fastSyncEndTick = 0;
    private static MinecraftServer serverInstance = null;
    
    private static class HealthData {
        float health;
        float maxHealth;
        float absorption;
        float shield;
        float maxShield;
        
        HealthData(float health, float maxHealth, float absorption, float shield, float maxShield) {
            this.health = health;
            this.maxHealth = maxHealth;
            this.absorption = absorption;
            this.shield = shield;
            this.maxShield = maxShield;
        }
        
        boolean hasMeaningfulChange(float health, float maxHealth, float absorption, float shield, float maxShield) {
            // Remove the threshold entirely. Sending a few floats every sync interval (200ms) 
            // is worth it to ensure the HUD is always 100% accurate and never 'stuck'.
            return this.health != health || this.maxHealth != maxHealth || this.absorption != absorption ||
                   this.shield != shield || this.maxShield != maxShield;
        }
    }
    
    public static void register() {
        // Tick-based sync
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            serverInstance = server;
            tickCounter++;
            
            // Check if we should revert to normal sync speed
            if (fastSyncEndTick > 0 && tickCounter >= fastSyncEndTick) {
                currentSyncInterval = NORMAL_SYNC_INTERVAL;
                fastSyncEndTick = 0;
            }
            
            if (tickCounter % currentSyncInterval == 0 || !pendingFastSync.isEmpty()) {
                syncPartyHealth(server);
            }
        });
        
        // Event-driven sync for damage - sync immediately when damage happens
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, dealt, blocked) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Sync immediately using stored server reference
                if (serverInstance != null) {
                    syncSinglePlayer(player, serverInstance);
                }
                
                // Also enable fast sync for next 3 seconds (60 ticks) to catch regen
                // Extended to 3 seconds to ensure we catch all healing ticks
                enableFastSync(60);
                pendingFastSync.add(player.getUuid());
            }
        });
    }
    
    private static void enableFastSync(int durationTicks) {
        currentSyncInterval = FAST_SYNC_INTERVAL;
        fastSyncEndTick = tickCounter + durationTicks;
    }
    
    private static void syncSinglePlayer(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerUuid = player.getUuid();
        
        Party party = PartyManager.getParty(playerUuid).orElse(null);
        if (party == null) return;
        
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();
        float shield = 0;
        if (player instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
            shield = provider.cranksdungeons$getCurrentEnergyShield();
        }
        float maxShield = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
        
        lastHealthData.put(playerUuid, new HealthData(health, maxHealth, absorption, shield, maxShield));
        ModNetworking.sendPartyHealthUpdate(server, party, playerUuid);
    }
    
    private static void syncPartyHealth(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerUuid = player.getUuid();
            
            Party party = PartyManager.getParty(playerUuid).orElse(null);
            if (party == null) {
                lastHealthData.remove(playerUuid);
                pendingFastSync.remove(playerUuid);
                continue;
            }
            
            // Check if this player needs immediate sync (damage) 
            boolean needsImmediateSync = pendingFastSync.remove(playerUuid);
            boolean inFastSync = fastSyncEndTick > 0 && tickCounter < fastSyncEndTick;
            
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float absorption = player.getAbsorptionAmount();
            float shield = 0;
            if (player instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
                shield = provider.cranksdungeons$getCurrentEnergyShield();
            }
            float maxShield = (float) player.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
            
            HealthData lastData = lastHealthData.get(playerUuid);
            
            // Sync if: first time, damage event, during fast sync period, or meaningful change
            // During fast sync: always sync regardless of threshold to catch every tick
            if (lastData == null || needsImmediateSync || inFastSync || lastData.hasMeaningfulChange(health, maxHealth, absorption, shield, maxShield)) {
                lastHealthData.put(playerUuid, new HealthData(health, maxHealth, absorption, shield, maxShield));
                ModNetworking.sendPartyHealthUpdate(server, party, playerUuid);
            }
        }
    }
    
    public static void clearPlayerData(UUID playerUuid) {
        lastHealthData.remove(playerUuid);
        pendingFastSync.remove(playerUuid);
    }
}
