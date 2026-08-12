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
import com.cranks.dungeons.client.NotificationHudOverlay;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.network.PartyHealthPacket;
import com.cranks.dungeons.network.PartyInvitePacket;
import com.cranks.dungeons.network.PartySyncPacket;
import com.cranks.dungeons.damage.DamageNumber;
import com.cranks.dungeons.damage.DamageNumberManager;
import com.cranks.dungeons.damage.DamageNumberRenderer;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.network.DamageNumberPayload;
import com.cranks.dungeons.network.StatOptionsSyncPacket;
import com.cranks.dungeons.network.ShockArcPayload;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreen;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler;
import net.minecraft.client.MinecraftClient;
import com.cranks.dungeons.screen.ModScreenHandlers;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class CranksDungeonsClient implements ClientModInitializer {

    static class DamageNumberFormatter {
        public static String formatDamage(double damage) {
            if (damage >= 1000) {
                double k = damage / 1000.0;
                if (Math.abs(k - Math.round(k)) < 0.05) {
                    return Math.round(k) + "k";
                }
                return String.format(java.util.Locale.US, "%.1f", k).replace(".0", "") + "k";
            }
            if (Math.abs(damage - Math.round(damage)) < 0.05) {
                return String.valueOf(Math.round(damage));
            }
            return String.format(java.util.Locale.US, "%.1f", damage).replace(".0", "");
        }
    }

    private static java.lang.reflect.Method cinematicActiveMethod = null;
    private static boolean cinematicMethodLookedUp = false;

    public static boolean isCinematicRespawnActive() {
        if (!cinematicMethodLookedUp) {
            cinematicMethodLookedUp = true;
            try {
                Class<?> clazz = Class.forName("net.gamev.cinematic_respawn.client.RespawnCinematicController");
                cinematicActiveMethod = clazz.getMethod("isActive");
            } catch (Exception ignored) {}
        }
        if (cinematicActiveMethod != null) {
            try {
                return (Boolean) cinematicActiveMethod.invoke(null);
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Override
    public void onInitializeClient() {
        // Register default HUD component (stamina/energy overlay);

        ClientConfigCommand.register();

        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
            net.minecraft.client.render.RenderLayer.getCutout(),
            com.cranks.dungeons.block.ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR,
            com.cranks.dungeons.block.ModBlocks.RUNIC_ENHANCEMENT_ALTAR
        );

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
            com.cranks.dungeons.registry.ModEntities.LEVEL_UP_VFX, 
            com.cranks.dungeons.client.render.entity.LevelUpVfxEntityRenderer::new
        );

        HandledScreens.register(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, RunicEnhancementAltarScreen::new);
        HandledScreens.register(ModScreenHandlers.ARTIFICIAL_ENHANCEMENT_ALTAR, ArtificialEnhancementAltarScreen::new);
        HandledScreens.register(ModScreenHandlers.QUALITY_STATION_SCREEN_HANDLER, com.cranks.dungeons.client.screen.QualityStationScreen::new);
        ModKeybinds.register();
        EnchantmentToggleHandler.register();
        com.cranks.dungeons.client.PassiveTreeKeybindHandler.register();

        // Register built-in Compendium tabs
        registerCompendiumTabs();
        
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
        
        // Register custom notifications HUD
        NotificationHudOverlay.register();
        ClientTickEvents.END_CLIENT_TICK.register(c -> NotificationHudOverlay.tick());
        
        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.ShowNotificationPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.message() != null && !payload.message().getString().isEmpty()) {
                    NotificationHudOverlay.addNotification(payload.message(), payload.durationTicks());
                } else {
                    NotificationHudOverlay.clearNotifications();
                }
            });
        });
        
        // Register the payload receiver
        ClientPlayNetworking.registerGlobalReceiver(DamageNumberPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player == null) return;
                
                UUID myUuid = context.client().player.getUuid();
                com.cranks.dungeons.config.ModConfig config = CranksDungeons.CONFIG;
                
                // Determine source type
                boolean isDealt = payload.attackerUuid() != null && payload.attackerUuid().equals(myUuid);
                boolean isReceived = payload.victimUuid() != null && payload.victimUuid().equals(myUuid);
                boolean isOther = !isDealt && !isReceived;

                Vec3d pos = new Vec3d(payload.x(), payload.y(), payload.z());

                // Calculate hit offset from entity center
                Vec3d hitOffset = Vec3d.ZERO;
                if (payload.victimUuid() != null && context.client().world != null) {
                    net.minecraft.entity.Entity entity = null;
                    for (net.minecraft.entity.Entity e : context.client().world.getEntities()) {
                        if (e.getUuid().equals(payload.victimUuid())) {
                            entity = e;
                            break;
                        }
                    }
                    if (entity != null) {
                        // Blood splash effect on hit if they are bleeding
                        if (entity instanceof LivingEntity living && !payload.entries().isEmpty() && !payload.entries().get(0).isHeal()) {
                            com.cranks.dungeons.client.render.AilmentParticleManager.onEntityHit(living);
                        }
                        // Calculate offset from entity center to hit position
                        Vec3d entityCenter = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
                        hitOffset = pos.subtract(entityCenter);
                    }
                }

                boolean playedCritSound = false;

                int count = payload.entries().size();
                for (int i = 0; i < count; i++) {
                    DamageNumberPayload.DamageEntry entry = payload.entries().get(i);
                    DamageElement element = DamageElement.values()[Math.min(entry.elementId(), DamageElement.values().length - 1)];

                    // Filter based on config
                    if (entry.isHeal()) {
                        if (isReceived && !config.showMyReceivedHealing()) continue;
                        if (!isReceived && !config.showOtherPlayerHealing()) continue;
                    } else {
                        if (isDealt && !config.showMyDealtDamage()) continue;
                        if (isReceived && !config.showMyReceivedDamage()) continue;
                        if (isOther && !config.showOtherPlayerDamage()) continue;
                    }

                    // Play combat sound for this element ONLY if we dealt the damage AND it's a direct hit
                    if (!entry.isHeal() && isDealt && payload.forceDisplay()) {
                        if (entry.isCrit()) {
                            if (!playedCritSound) {
                                com.cranks.dungeons.client.CombatAudioHelper.playCombatSound(element, entry.damage(), true);
                                playedCritSound = true;
                            }
                        } else {
                            com.cranks.dungeons.client.CombatAudioHelper.playCombatSound(element, entry.damage(), false);
                        }
                    }

                    double offsetX = 0;
                    double offsetZ = 0;
                    if (count > 1) {
                        // Multi-element hits: still use slight spiral spread + vertical stacking (handled by rowIndex)
                        double angle = (2.0 * Math.PI * i) / count;
                        double spread = 0.15; // smaller horizontal spread since we have vertical stacking now
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
                            System.currentTimeMillis() - (i * 120) // Increased stagger for better layout
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
            com.cranks.dungeons.client.render.LightningArcManager.tick();
            com.cranks.dungeons.client.render.AilmentParticleManager.tick(client);
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            DamageNumberRenderer.renderHud(context, 0f);
        });

        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            net.minecraft.client.render.VertexConsumerProvider.Immediate consumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            com.cranks.dungeons.client.render.LightningArcRenderer.render(context, consumers);
            com.cranks.dungeons.client.render.AilmentIconRenderer.render(context, consumers);
            com.cranks.dungeons.client.render.EntityStatBarRenderer.render(context, consumers);
            consumers.draw();
        });
        
        // Register party HUD renderer
        PartyHudRenderer.register();
        
        // Register new Stat Bar HUD renderer
        com.cranks.dungeons.client.render.StatBarHudRenderer.register();
        
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
                    payload.absorption(),
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

        ClientPlayNetworking.registerGlobalReceiver(ShockArcPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.client.render.LightningArcManager.addArc(payload.start(), payload.end(), payload.color());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.SyncWorldTreePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.config.WorldTreeConfig.setNodes(payload.nodes());
                com.cranks.dungeons.CranksDungeons.LOGGER.info("[WorldTreeClient] Synced {} nodes from server.", payload.nodes().size());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.AilmentSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.client.render.AilmentIconRenderer.updateAilments(payload.entityId(), payload.ailments());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.PlayerInfoSyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.network.PlayerInfoSyncPacket.handle(payload, context.client());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.EntityShieldSyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.network.EntityShieldSyncPacket.handle(payload, context.client());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.ManaSyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.network.ManaSyncPacket.handle(payload, context.client());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.SyncPassiveTreePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.config.PassiveTreeConfig.setNodesAndClasses(payload.nodes(), payload.classes());
                com.cranks.dungeons.stat.passive.PassiveNodeRegistry.loadFromConfig();
            });
        });
    }

    /**
     * Registers the built-in Compendium tabs (Guide, Passive Tree, World Tree).
     * Called during client initialization before entrypoint discovery.
     */
    private void registerCompendiumTabs() {
        com.cranks.dungeons.client.compendium.CompendiumTabRegistry.registerTab(
                com.cranks.dungeons.client.compendium.CompendiumTabRegistry.GUIDE_TAB_ID,
                net.minecraft.text.Text.literal("Compendium"),
                net.minecraft.util.Identifier.of("cranks-dungeons", "compendium/compendium_icon"),
                com.cranks.dungeons.client.compendium.CompendiumGuideTabRenderer::new,
                com.cranks.dungeons.client.compendium.CompendiumTab.PRIORITY_GUIDE
        );

        com.cranks.dungeons.client.compendium.CompendiumTabRegistry.registerTab(
                com.cranks.dungeons.client.compendium.CompendiumTabRegistry.PASSIVE_TREE_TAB_ID,
                net.minecraft.text.Text.literal("Passive Tree"),
                net.minecraft.util.Identifier.of("cranks-dungeons", "compendium/passive_tree_icon"),
                com.cranks.dungeons.client.compendium.PassiveTreeTabRenderer::new,
                com.cranks.dungeons.client.compendium.CompendiumTab.PRIORITY_PASSIVE_TREE
        );

        com.cranks.dungeons.client.compendium.CompendiumTabRegistry.registerTab(
                com.cranks.dungeons.client.compendium.CompendiumTabRegistry.WORLD_TREE_TAB_ID,
                net.minecraft.text.Text.literal("World Tree"),
                net.minecraft.util.Identifier.of("cranks-dungeons", "compendium/world_tree_icon"),
                com.cranks.dungeons.client.compendium.WorldTreeTabRenderer::new,
                com.cranks.dungeons.client.compendium.CompendiumTab.PRIORITY_WORLD_TREE
        );
    }
}
