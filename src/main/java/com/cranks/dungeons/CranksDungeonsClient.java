package com.cranks.dungeons;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.client.ClientShieldEffects;
import com.cranks.dungeons.damage.DamageNumber;
import com.cranks.dungeons.damage.DamageNumberManager;
import com.cranks.dungeons.damage.DamageNumberRenderer;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.network.DamageNumberPayload;
import com.cranks.dungeons.network.StatOptionsSyncPacket;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreen;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler;
import com.cranks.dungeons.screen.ModScreenHandlers;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.math.Vec3d;

public class CranksDungeonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, RunicEnhancementAltarScreen::new);
        HandledScreens.register(ModScreenHandlers.ARTIFICIAL_ENHANCEMENT_ALTAR, ArtificialEnhancementAltarScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(StatOptionsSyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player != null && context.client().player.currentScreenHandler instanceof ArtificialEnhancementAltarScreenHandler handler) {
                    if (handler.syncId == payload.syncId()) {
                        handler.setClientStatOptions(
                                new String[]{payload.stat0(), payload.stat1(), payload.stat2()},
                                new int[]{payload.tier0(), payload.tier1(), payload.tier2()}
                        );
                    }
                }
            });
        });
        ClientShieldEffects.init();
        // Register the payload receiver
        ClientPlayNetworking.registerGlobalReceiver(DamageNumberPayload.ID, (payload, context) -> {
            System.out.println("[DamageNumbers CLIENT] Received packet with " + payload.entries().size() + " entries");
            context.client().execute(() -> {
                Vec3d pos = new Vec3d(payload.x(), payload.y(), payload.z());
                System.out.println("[DamageNumbers CLIENT] Processing at " + pos);

                int count = payload.entries().size();
                for (int i = 0; i < payload.entries().size(); i++) {
                    DamageNumberPayload.DamageEntry entry = payload.entries().get(i);

                    // Spread numbers in a circle pattern
                    double offsetX = 0;
                    double offsetZ = 0;
                    if (count > 1) {
                        double angle = (2.0 * Math.PI * i) / count;
                        double spread = 0.25; // Reduced spread for tighter grouping
                        offsetX = Math.cos(angle) * spread;
                        offsetZ = Math.sin(angle) * spread;
                    }

                    // Add small random offset for natural look
                    offsetX += (Math.random() - 0.5) * 0.1;
                    offsetZ += (Math.random() - 0.5) * 0.1;

                    DamageNumber damageNumber = new DamageNumber(
                            entry.damage(),
                            entry.color(),
                            pos,
                            offsetX,
                            offsetZ
                    );
                    DamageNumberManager.addDamageNumber(damageNumber);
                    System.out.println("[DamageNumbers CLIENT] Added number: " + entry.damage() + " color=0x" + Integer.toHexString(entry.color()));
                }
                System.out.println("[DamageNumbers CLIENT] Active numbers: " + DamageNumberManager.getActiveNumbers().size());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(
                EnergyShieldSyncPacket.ID,
                (payload, context) -> EnergyShieldSyncPacket.handle(payload, context.client())
        );

        // Register tick event for cleanup
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DamageNumberManager.tick();
        });

        // Render damage numbers as HUD overlay
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            DamageNumberRenderer.renderHud(context, 0f);
        });
    }
}