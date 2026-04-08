package com.cranks.dungeons;

import com.cranks.dungeons.client.ClientConfigCommand;
import com.cranks.dungeons.client.ClientEquipmentHandler;
import com.cranks.dungeons.client.ClientShieldEffects;
import com.cranks.dungeons.client.EnchantmentToggleHandler;
import com.cranks.dungeons.client.EnergyShieldHudRenderer;
import com.cranks.dungeons.client.ModKeybinds;
import com.cranks.dungeons.client.ModParticleProviders;
import com.cranks.dungeons.client.ShieldNumberSprites;
import com.cranks.dungeons.client.party.PartyClientData;
import com.cranks.dungeons.client.party.PartyHudRenderer;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.network.PartyHealthPacket;
import com.cranks.dungeons.network.PartyInvitePacket;
import com.cranks.dungeons.network.PartySyncPacket;
import com.cranks.dungeons.config.ModConfig;
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

import java.util.UUID;

public class CranksDungeonsClient implements ClientModInitializer {

    static class DamageNumberFormatter {
        public static String formatDamage(double damage) {
            if (damage >= 1000) {
                return String.format("%.1fk", damage / 1000.0);
            }
            if (damage < 10) {
                return String.format("%.1f", damage);
            }
            return String.format("%.0f", damage);
        }
    }

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        ClientConfigCommand.register();

        HandledScreens.register(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, RunicEnhancementAltarScreen::new);
        HandledScreens.register(ModScreenHandlers.ARTIFICIAL_ENHANCEMENT_ALTAR, ArtificialEnhancementAltarScreen::new);
        ModKeybinds.register();
        EnchantmentToggleHandler.register();
        
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
        
        // Register shield number sprites for row counter
        ShieldNumberSprites.register();
        
        // Register particle providers for custom poison particles
        ModParticleProviders.register();
        
        // Register client-side equipment change handler for proper stat sync
        ClientEquipmentHandler.register();
        
        // Register energy shield HUD renderer (works independently of health bar mods)
        EnergyShieldHudRenderer.register();
        
        // Register the payload receiver
        ClientPlayNetworking.registerGlobalReceiver(DamageNumberPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player == null) return;
                
                UUID myUuid = context.client().player.getUuid();
                ModConfig config = ModConfig.get();
                
                // Determine source type
                boolean isDealt = payload.attackerUuid() != null && payload.attackerUuid().equals(myUuid);
                boolean isReceived = payload.victimUuid() != null && payload.victimUuid().equals(myUuid);
                boolean isOther = !isDealt && !isReceived;

                Vec3d pos = new Vec3d(payload.x(), payload.y(), payload.z());

                // Calculate hit offset from entity center
                Vec3d hitOffset = Vec3d.ZERO;
                if (payload.victimUuid() != null && context.client().world != null) {
                    var entity = context.client().world.getEntity(payload.victimUuid());
                    if (entity != null) {
                        // Calculate offset from entity center to hit position
                        Vec3d entityCenter = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
                        hitOffset = pos.subtract(entityCenter);
                    }
                }

                int count = payload.entries().size();
                for (int i = 0; i < count; i++) {
                    DamageNumberPayload.DamageEntry entry = payload.entries().get(i);
                    DamageElement element = DamageElement.values()[Math.min(entry.elementId(), DamageElement.values().length - 1)];

                    // Filter based on config
                    if (entry.isHeal()) {
                        if (isReceived && !config.showMyReceivedHealing) continue;
                        if (!isReceived && !config.showOtherPlayerHealing) continue;
                    } else {
                        if (isDealt && !config.showMyDealtDamage) continue;
                        if (isReceived && !config.showMyReceivedDamage) continue;
                        if (isOther && !config.showOtherPlayerDamage) continue;
                    }

                    // Play combat sound for this element if it's damage
                    if (!entry.isHeal() && (isDealt || isReceived)) {
                        com.cranks.dungeons.client.CombatAudioHelper.playCombatSound(element, entry.damage(), entry.isCrit());
                    }

                    double offsetX = 0;
                    double offsetZ = 0;
                    if (count > 1) {
                        double angle = (2.0 * Math.PI * i) / count;
                        double spread = 0.25;
                        offsetX = Math.cos(angle) * spread;
                        offsetZ = Math.sin(angle) * spread;
                    }

                    offsetX += (Math.random() - 0.5) * 0.1;
                    offsetZ += (Math.random() - 0.5) * 0.1;

                    String formattedDamageText = (entry.isHeal() ? "+" : "") + DamageNumberFormatter.formatDamage(entry.damage());
                    int formattedDamageWidth = context.client().textRenderer.getWidth(formattedDamageText);
                    DamageNumber damageNumber = new DamageNumber(
                            entry.damage(),
                            entry.color(),
                            pos,
                            offsetX,
                            offsetZ,
                            i,
                            formattedDamageText,
                            formattedDamageWidth,
                            payload.forceDisplay(),
                            payload.victimUuid(),
                            hitOffset,
                            element,
                            entry.isCrit(),
                            entry.isHeal(),
                            System.currentTimeMillis() - (i * 80) // Stagger by 80ms each so oldest appear higher
                    );
                    DamageNumberManager.addDamageNumber(damageNumber);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(
                EnergyShieldSyncPacket.ID,
                (payload, context) -> EnergyShieldSyncPacket.handle(payload, context.client())
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DamageNumberManager.tick();
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            DamageNumberRenderer.renderHud(context, 0f);
        });
        
        // Register party HUD renderer
        PartyHudRenderer.register();
        
        // Register party packet handlers
        ClientPlayNetworking.registerGlobalReceiver(PartySyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.members().isEmpty()) {
                    // Empty party list = leave party
                    PartyClientData.leaveParty();
                } else {
                    PartyClientData.setPartyData(payload.members(), payload.leaderUuid());
                }
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(PartyHealthPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                PartyClientData.updateMemberHealth(
                    payload.playerUuid(),
                    payload.health(),
                    payload.maxHealth(),
                    payload.energyShield(),
                    payload.maxEnergyShield()
                );
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(PartyInvitePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.accepted()) {
                    context.client().player.sendMessage(
                        net.minecraft.text.Text.literal(payload.inviterName() + " accepted your party invite!")
                            .formatted(net.minecraft.util.Formatting.GREEN),
                        false
                    );
                } else {
                    context.client().player.sendMessage(
                        net.minecraft.text.Text.literal(payload.inviterName() + " invited you to their party!")
                            .formatted(net.minecraft.util.Formatting.YELLOW)
                            .append(net.minecraft.text.Text.literal(" Use /party accept " + payload.inviterName())
                                 .formatted(net.minecraft.util.Formatting.GREEN)),
                        false
                    );
                }
            });
        });
    }
}
