package com.cranks.dungeons.client;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side handler for equipment changes.
 * Ensures stats are properly recalculated when equipment changes on the client.
 */
public class ClientEquipmentHandler {
    
    private static final Map<UUID, Map<EquipmentSlot, ItemStack>> lastEquipment = new HashMap<>();
    private static int tickCounter = 0;
    
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            // Check equipment every 5 ticks to avoid excessive checks
            tickCounter++;
            if (tickCounter < 5) return;
            tickCounter = 0;
            
            UUID playerUuid = client.player.getUuid();
            Map<EquipmentSlot, ItemStack> playerLastEquipment = lastEquipment.computeIfAbsent(playerUuid, k -> new HashMap<>());
            
            boolean equipmentChanged = false;
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
                ItemStack current = client.player.getEquippedStack(slot);
                ItemStack last = playerLastEquipment.get(slot);
                
                // Handle null checks before comparing
                if (current == null && last == null) {
                    continue; // Both null, no change
                }
                if (current == null || last == null) {
                    // One is null, the other isn't - equipment changed
                    equipmentChanged = true;
                    playerLastEquipment.put(slot, current == null ? ItemStack.EMPTY : current.copy());
                } else if (!ItemStack.areEqual(current, last)) {
                    equipmentChanged = true;
                    playerLastEquipment.put(slot, current.copy());
                }
            }
            
            // If equipment changed, update player stats on client side
            if (equipmentChanged) {
                EquipmentStatApplier.updatePlayerStats(client.player);
            }
        });
    }
}
