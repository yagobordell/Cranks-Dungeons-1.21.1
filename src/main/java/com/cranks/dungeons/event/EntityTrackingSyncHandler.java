package com.cranks.dungeons.event;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EntityShieldSyncPacket;
import com.cranks.dungeons.network.ModNetworking;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class EntityTrackingSyncHandler {

    public static void register() {
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            // Initial sync for Energy Shield
            if (trackedEntity instanceof LivingEntity living) {
                if (living instanceof EnergyShieldProvider provider) {
                    float currentShield = provider.cranksdungeons$getCurrentEnergyShield();
                    // No need to send if shield is 0
                    if (currentShield > 0) {
                        ServerPlayNetworking.send(player, new EntityShieldSyncPacket(living.getId(), currentShield));
                    }
                }

                // Initial sync for Player Info (Level, Class, Party)
                if (living instanceof ServerPlayerEntity trackedPlayer) {
                    ModNetworking.syncPlayerInfoToTrackers(trackedPlayer, ((net.minecraft.server.world.ServerWorld)trackedPlayer.getEntityWorld()).getServer());
                }
            }
        });
    }
}
