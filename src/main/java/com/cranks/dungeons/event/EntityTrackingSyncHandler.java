package com.cranks.dungeons.event;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EntityShieldSyncPacket;
import com.cranks.dungeons.network.ModNetworking;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.passive.HorseEntity;

public class EntityTrackingSyncHandler {

    public static void register() {
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            // Initial sync for Energy Shield
            if (trackedEntity instanceof LivingEntity living) {
                com.cranks.dungeons.ailment.AilmentManager.syncTo(living, player);
                if (living instanceof com.cranks.dungeons.mount.MountDataProvider
                        && com.cranks.dungeons.mount.MountKind.of(living)
                        != com.cranks.dungeons.mount.MountKind.HORSE) {
                    com.cranks.dungeons.mount.MountManager.ensureInitialized(living);
                    ServerPlayNetworking.send(player,
                            com.cranks.dungeons.network.MountDataSyncPacket.from(living));
                }
                if (living instanceof HorseEntity horse) {
                    com.cranks.dungeons.horse.HorseBreedingManager.ensureInitialized(horse);
                    ServerPlayNetworking.send(player,
                            com.cranks.dungeons.network.HorseGeneticsSyncPacket.from(horse));
                }

                if (living instanceof EnergyShieldProvider provider) {
                    float currentShield = provider.cranksdungeons$getCurrentEnergyShield();
                    float maxShield = (float) living.getAttributeValue(com.cranks.dungeons.registry.ModAttributes.ENERGY_SHIELD);
                    if (maxShield > 0) {
                        ServerPlayNetworking.send(player, new EntityShieldSyncPacket(living.getId(), currentShield, maxShield));
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
