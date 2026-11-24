package com.cranks.dungeons;

import com.cranks.dungeons.client.ClientConfigCommand;
import com.cranks.dungeons.client.ClientEquipmentHandler;
import com.cranks.dungeons.client.ClientShieldEffects;
import com.cranks.dungeons.client.EnchantmentToggleHandler;
import com.cranks.dungeons.client.EnergyShieldHudRenderer;
import com.cranks.dungeons.client.ManaHudRenderer;
import com.cranks.dungeons.client.ModKeybinds;
import com.cranks.dungeons.client.ModParticleProviders;
import com.cranks.dungeons.client.ShieldNumberSprites;
import com.cranks.dungeons.client.party.PartyClientData;
import com.cranks.dungeons.client.party.PartyHudRenderer;
import com.cranks.dungeons.client.NotificationHudOverlay;
import com.cranks.dungeons.client.PlayerLoadoutScreen;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.network.PartyHealthPacket;
import com.cranks.dungeons.network.PartyInvitePacket;
import com.cranks.dungeons.network.PartySyncPacket;
import com.cranks.dungeons.damage.DamageNumber;
import com.cranks.dungeons.damage.DamageNumberManager;
import com.cranks.dungeons.damage.DamageNumberRenderer;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.network.DamageNumberPayload;
import com.cranks.dungeons.network.ShockArcPayload;
import com.cranks.dungeons.network.OpenNameTagScreenPayload;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import net.minecraft.client.MinecraftClient;
import com.cranks.dungeons.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;

import java.util.UUID;

public class CranksDungeonsClient implements ClientModInitializer {

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
        com.cranks.dungeons.client.render.MountChestRenderRegistration.register();
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!stack.contains(net.minecraft.component.DataComponentTypes.POTION_CONTENTS)) return;
            if (stack.isOf(net.minecraft.item.Items.SPLASH_POTION) || stack.getItem() instanceof com.cranks.dungeons.alchemy.AlchemyItems.Brew brew
                    && brew.delivery == com.cranks.dungeons.alchemy.AlchemyItems.Delivery.SPLASH) {
                lines.add(net.minecraft.text.Text.translatable("potion.cranks-dungeons.splash_delivery")
                        .formatted(net.minecraft.util.Formatting.GRAY));
            } else if (stack.isOf(net.minecraft.item.Items.LINGERING_POTION) || stack.getItem() instanceof com.cranks.dungeons.alchemy.AlchemyItems.Brew brew
                    && brew.delivery == com.cranks.dungeons.alchemy.AlchemyItems.Delivery.LINGERING) {
                lines.add(net.minecraft.text.Text.translatable("potion.cranks-dungeons.cloud_delivery",
                        com.cranks.dungeons.effect.PotionRules.CLOUD_DURATION / 20)
                        .formatted(net.minecraft.util.Formatting.GRAY));
            }
            var client = MinecraftClient.getInstance();
            int width = Math.max(80, Math.min(240, client.getWindow().getScaledWidth() - 24));
            var wrapped = new java.util.ArrayList<net.minecraft.text.Text>();
            for (var line : lines) {
                for (var fragment : client.textRenderer.getTextHandler().wrapLines(line, width, net.minecraft.text.Style.EMPTY)) {
                    var text = net.minecraft.text.Text.empty();
                    fragment.visit((style, content) -> {
                        text.append(net.minecraft.text.Text.literal(content).setStyle(style));
                        return java.util.Optional.empty();
                    }, net.minecraft.text.Style.EMPTY);
                    wrapped.add(text);
                }
            }
            lines.clear();
            lines.addAll(wrapped);
        });
        com.cranks.dungeons.client.HorseStaminaHudRenderer.register();
        com.cranks.dungeons.client.SpyglassMountScoutHudRenderer.register();
        com.cranks.dungeons.client.MountHealthHudRenderer.register();
        com.cranks.dungeons.client.HorseGallopInputHandler.register();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    com.cranks.dungeons.client.render.AilmentIconRenderer.clear();
                    com.cranks.dungeons.client.CreativeCatalogPanel.clearSession();
                    com.cranks.dungeons.client.TradeClient.clear();
                    NotificationHudOverlay.clearNotifications();
                    com.cranks.dungeons.client.HorseGeneticsClientData.clear();
                    com.cranks.dungeons.client.MountClientData.clear();
                    com.cranks.dungeons.client.HorseGallopInputHandler.reset();
                    com.cranks.dungeons.client.render.EntityStatBarRenderer.clearProjection();
                    DamageNumberManager.clear();
                    com.cranks.dungeons.client.RangedClient.reset();
                    com.cranks.dungeons.client.guide.GuideInteraction.reset();
                });
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents.ENTITY_UNLOAD.register(
                (entity, world) -> com.cranks.dungeons.client.render.AilmentIconRenderer.remove(entity.getId()));
        // Register default HUD component (stamina/energy overlay);

        ClientConfigCommand.register();
        ClientTickEvents.END_CLIENT_TICK.register(com.cranks.dungeons.client.RangedClient::tick);
        com.cranks.dungeons.client.RangedClient.registerTooltips();
        com.cranks.dungeons.client.guide.GuideInteraction.register();

        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
            net.minecraft.client.render.RenderLayer.getCutout(),
            com.cranks.dungeons.block.ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR,
            com.cranks.dungeons.block.ModBlocks.RUNIC_ENHANCEMENT_ALTAR
        );

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
            com.cranks.dungeons.registry.ModEntities.LEVEL_UP_VFX, 
            com.cranks.dungeons.client.render.entity.LevelUpVfxEntityRenderer::new
        );

        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.RUNIC_ENHANCEMENT_ALTAR, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.ARTIFICIAL_ENHANCEMENT_ALTAR, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.QUALITY_STATION_SCREEN_HANDLER, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_FLETCHING, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_TAILORING, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_CARTOGRAPHY, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler,PlayerLoadoutScreen>register(ModScreenHandlers.MOUNT_CARGO,PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_CRAFTING_TABLE, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_CUTTING_BENCH, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_ANVIL, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_GRINDSTONE, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_ENCHANTING, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_FURNACE, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_BLAST_FURNACE, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(
                ModScreenHandlers.LOADOUT_SMOKER, PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_ALCHEMY, PlayerLoadoutScreen::new);
        for (var delivery : com.cranks.dungeons.alchemy.AlchemyItems.Delivery.values()) {
            for (boolean lesser : new boolean[]{true, false}) {
                net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register((stack, tint) -> tint == 0
                        ? stack.getOrDefault(net.minecraft.component.DataComponentTypes.POTION_CONTENTS, net.minecraft.component.type.PotionContentsComponent.DEFAULT).getColor() : -1,
                        com.cranks.dungeons.alchemy.AlchemyItems.filled(lesser, delivery));
            }
        }
        ModKeybinds.register();
        com.cranks.dungeons.client.MountWhistleClient.register();
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_STORAGE_3,PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_STORAGE_6,PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_SHULKER,PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_HOPPER,PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_CRAFTER,PlayerLoadoutScreen::new);
        HandledScreens.<ScreenHandler, PlayerLoadoutScreen>register(ModScreenHandlers.LOADOUT_DISPENSER,PlayerLoadoutScreen::new);
        com.cranks.dungeons.client.RecipeDiscoveryNotifications.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen != null || client.player == null || client.player.isSpectator()) return;
            while (ModKeybinds.TENTH_HOTBAR.wasPressed()) {
                client.player.getInventory().selectedSlot = com.cranks.dungeons.inventory.PlayerLoadout.EXTRA_HOTBAR;
            }
            while (ModKeybinds.SWAP_ARMOR.wasPressed()) {
                client.interactionManager.clickButton(client.player.playerScreenHandler.syncId,
                        com.cranks.dungeons.inventory.PlayerLoadout.SWAP_BUTTON);
            }
        });
        EnchantmentToggleHandler.register();
        com.cranks.dungeons.client.PassiveTreeKeybindHandler.register();

        com.cranks.dungeons.client.NameTagClient.register();
        ClientPlayNetworking.registerGlobalReceiver(CraftMaterialsResultPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    com.cranks.dungeons.client.CreativeHotbarPresets.receiveResult(payload);
                    if (context.client().currentScreen instanceof PlayerLoadoutScreen screen) screen.receiveCraftingResult(payload);
                }));
        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.TradeStatePayload.ID,(payload,context)->context.client().execute(()->com.cranks.dungeons.client.TradeClient.receive(payload)));
        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.CoinGainPayload.ID,(payload,context)->context.client().execute(()->NotificationHudOverlay.addCoinGain(payload.coins())));

        // Register built-in Compendium tabs
        registerCompendiumTabs();
        

        
        ClientShieldEffects.init();
        
        // Register shield number sprites for row counter
        ShieldNumberSprites.register();
        
        // Register particle providers for custom poison particles
        ModParticleProviders.register();
        
        // Register client-side equipment change handler for proper stat sync
        ClientEquipmentHandler.register();
        
        // Register energy shield HUD renderer (works independently of health bar mods)
        EnergyShieldHudRenderer.register();
        
        // Register mana HUD renderer for vanilla hearts mode
        ManaHudRenderer.register();
        
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

                var kind = payload.kind();
                if (!Double.isFinite(payload.x()) || !Double.isFinite(payload.y()) || !Double.isFinite(payload.z())) return;
                var entries = new java.util.ArrayList<DamageNumberPayload.DamageEntry>();
                for (var entry : payload.entries()) {
                    if (!com.cranks.dungeons.damage.CombatNumberStyle.valid(entry.damage())
                            || entry.elementId() < 0 || entry.elementId() >= DamageElement.values().length
                            || entry.isHeal() != kind.healing()) continue;
                    if (entry.isHeal()) {
                        if (isReceived ? !config.showMyReceivedHealing() : !config.showOtherPlayerHealing()) continue;
                    } else {
                        if (isDealt && !config.showMyDealtDamage() || isReceived && !config.showMyReceivedDamage()
                                || isOther && !config.showOtherPlayerDamage()) continue;
                    }
                    entries.add(entry);
                }
                if (entries.isEmpty()) return;
                if (isDealt && !kind.healing()) {
                    var entity = context.client().world == null ? null : context.client().world.getEntityById(payload.victimEntityId());
                    if (entity != null && entity.getUuid().equals(payload.victimUuid()))
                        com.cranks.dungeons.client.render.EntityStatBarRenderer.onEntityAttacked(entity.getId());
                }
                DamageNumberManager.addDamageNumber(new DamageNumber(payload, entries, isDealt || isReceived, DamageNumberManager.now()));

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

        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.START.register(
                com.cranks.dungeons.client.render.EntityStatBarRenderer::captureProjection);
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            com.cranks.dungeons.client.render.AilmentParticleManager.renderHud(context);
            com.cranks.dungeons.client.render.EntityStatBarRenderer.renderHud(context);
            DamageNumberRenderer.renderHud(context, tickCounter.getTickDelta(true));
        });

        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(context -> {
            net.minecraft.client.render.VertexConsumerProvider.Immediate consumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            com.cranks.dungeons.client.render.LightningArcRenderer.render(context, consumers);
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

        com.cranks.dungeons.client.MountTransferOfferScreen.register();

        ClientPlayNetworking.registerGlobalReceiver(ShockArcPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.client.render.LightningArcManager.addArc(payload.start(), payload.end(), payload.color());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.SyncWorldTreePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (!context.client().isIntegratedServerRunning()) {
                    com.cranks.dungeons.config.WorldTreeConfig.setNodes(payload.nodes());
                    com.cranks.dungeons.stat.passive.WorldNodeRegistry.loadFromConfig();
                }
                if (context.client().currentScreen instanceof com.cranks.dungeons.client.compendium.CompendiumScreen screen) {
                    screen.refreshTab();
                }
                com.cranks.dungeons.CranksDungeons.LOGGER.info("[WorldTreeClient] Synced {} nodes from server.", payload.nodes().size());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.AilmentSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                com.cranks.dungeons.client.render.AilmentIconRenderer.updateAilments(payload.entityId(), payload.ailments());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.AilmentResistedPayload.ID, (payload, context) -> {
            context.client().execute(() -> com.cranks.dungeons.client.render.AilmentIconRenderer.resisted(
                    payload.entityId(), payload.entityUuid()));
        });
        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.BleedVisualPayload.ID, (payload, context) -> {
            context.client().execute(() -> com.cranks.dungeons.client.render.AilmentParticleManager.bleedEvent(payload));
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

        ClientPlayNetworking.registerGlobalReceiver(
                com.cranks.dungeons.network.HorseGeneticsSyncPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        com.cranks.dungeons.client.HorseGeneticsClientData.update(payload)));

        ClientPlayNetworking.registerGlobalReceiver(
                com.cranks.dungeons.network.MountDataSyncPacket.ID,
                (payload, context) -> context.client().execute(() ->
                        com.cranks.dungeons.client.MountClientData.update(payload)));

        ClientPlayNetworking.registerGlobalReceiver(com.cranks.dungeons.network.SyncPassiveTreePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (!context.client().isIntegratedServerRunning()) {
                    com.cranks.dungeons.config.PassiveTreeConfig.setNodesAndClasses(payload.nodes(), payload.classes());
                    com.cranks.dungeons.stat.passive.PassiveNodeRegistry.loadFromConfig();
                }
                if (context.client().currentScreen instanceof com.cranks.dungeons.client.compendium.CompendiumScreen screen) {
                    screen.refreshTab();
                }
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
