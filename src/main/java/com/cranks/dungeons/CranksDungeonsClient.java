package com.cranks.dungeons;

import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.screen.ModScreenHandlers;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class CranksDungeonsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, RunicEnhancementAltarScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(
                EnergyShieldSyncPacket.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        if (context.player() instanceof EnergyShieldProvider provider) {
                            provider.cranksdungeons$setCurrentEnergyShield(payload.currentShield());
                        }
                    });
                }
        );
    }
}
