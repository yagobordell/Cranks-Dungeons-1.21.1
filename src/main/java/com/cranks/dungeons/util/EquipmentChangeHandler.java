package com.cranks.dungeons.util;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.event.EnchantmentDisableTickHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;

public final class EquipmentChangeHandler {

    private EquipmentChangeHandler() {}

    public static void register() {
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previousStack, newStack) -> {
            if (entity instanceof ServerPlayerEntity player) {
                EquipmentStatApplier.updatePlayerStats(player);

                if (slot == EquipmentSlot.MAINHAND) {
                    EnchantmentDisableTickHandler.onMainHandChanged(player, newStack);
                }
            }
        });
    }
}
