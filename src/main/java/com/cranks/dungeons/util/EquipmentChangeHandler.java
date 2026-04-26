package com.cranks.dungeons.util;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.event.EnchantmentDisableTickHandler;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EquipmentChangeHandler {

    private EquipmentChangeHandler() {}

    // Track players that need shield sync on next tick
    private static final Map<UUID, Integer> pendingShieldSyncs = new HashMap<>();

    public static void scheduleSync(ServerPlayerEntity player, int ticks) {
        pendingShieldSyncs.put(player.getUuid(), ticks);
    }

    public static void register() {
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previousStack, newStack) -> {
            if (entity instanceof ServerPlayerEntity player) {
                EquipmentStatApplier.updatePlayerStats(player);

                // Mark player for shield sync on next server tick
                // We delay by 1 tick to ensure attributes have updated
                pendingShieldSyncs.put(player.getUuid(), 1);

                if (slot == EquipmentSlot.MAINHAND) {
                    EnchantmentDisableTickHandler.onMainHandChanged(player, newStack);
                }
            }
        });

        // Sync shield on server tick to ensure attributes are updated
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (Map.Entry<UUID, Integer> entry : new HashMap<>(pendingShieldSyncs).entrySet()) {
                UUID playerUuid = entry.getKey();
                int ticksRemaining = entry.getValue();

                if (ticksRemaining <= 0) {
                    pendingShieldSyncs.remove(playerUuid);

                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
                    if (player != null) {
                        syncEnergyShieldToClient(player);
                    }
                } else {
                    pendingShieldSyncs.put(playerUuid, ticksRemaining - 1);
                }
            }
        });
    }

    /**
     * Syncs the current energy shield value to the client after equipment changes.
     * Ensures the shield display updates immediately when max shield changes.
     */
    public static void syncEnergyShieldToClient(ServerPlayerEntity player) {
        if (player instanceof EnergyShieldProvider provider) {
            double maxShield = player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
            float currentShield = provider.cranksdungeons$getCurrentEnergyShield();

            // Cap shield at new max if necessary
            if (currentShield > maxShield) {
                currentShield = (float) maxShield;
                provider.cranksdungeons$setCurrentEnergyShield(currentShield);
            }

            // Always sync to client after equipment change to ensure display is correct
            ServerPlayNetworking.send(player, new EnergyShieldSyncPacket(currentShield));

            CranksDungeons.LOGGER.debug("Synced energy shield to {} (max: {}) for player {}",
                    currentShield, maxShield, player.getName().getString());
        }
    }
}
