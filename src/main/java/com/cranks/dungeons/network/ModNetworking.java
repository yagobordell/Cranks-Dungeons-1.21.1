// src/main/java/com/cranks/dungeons/network/ModNetworking.java
package com.cranks.dungeons.network;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.event.EnchantmentDisableTickHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

import com.cranks.dungeons.party.Party;
import com.cranks.dungeons.party.PartyManager;
import com.cranks.dungeons.party.PartyMemberData;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;

public class ModNetworking {

    private static final int MAX_BULK_UNLOCKS = BulkUnlockNodesPayload.MAX_ENTRIES;
    private static final long PROGRESSION_REQUEST_COOLDOWN_NANOS = 50_000_000L;
    private static final java.util.Map<java.util.UUID, Long> LAST_PROGRESSION_REQUEST = new java.util.HashMap<>();

    private static boolean allowProgressionRequest(net.minecraft.server.network.ServerPlayerEntity player, int entryCount) {
        if (entryCount < 1 || entryCount > MAX_BULK_UNLOCKS) return false;

        long now = System.nanoTime();
        Long previous = LAST_PROGRESSION_REQUEST.get(player.getUuid());
        if (previous != null && now - previous < PROGRESSION_REQUEST_COOLDOWN_NANOS) return false;
        LAST_PROGRESSION_REQUEST.put(player.getUuid(), now);
        return true;
    }

    public static void clearPlayerState(java.util.UUID playerUuid) {
        LAST_PROGRESSION_REQUEST.remove(playerUuid);
    }

    public static void clearAllState() {
        LAST_PROGRESSION_REQUEST.clear();
    }

    private static void runBatched(com.cranks.dungeons.component.PassiveDataComponent data, Runnable action) {
        data.beginBatch();
        try {
            action.run();
        } finally {
            data.endBatch();
        }
    }

    private static void runBatched(com.cranks.dungeons.component.WorldTreeDataComponent data, Runnable action) {
        data.beginBatch();
        try {
            action.run();
        } finally {
            data.endBatch();
        }
    }

    private static String validateNodeChoice(com.cranks.dungeons.stat.passive.PassiveNode node,
                                             String rawChoice, boolean worldTree) {
        String choice = com.cranks.dungeons.stat.passive.TreeAttributeRules.normalizeChoice(rawChoice);
        if (choice == null) return null;
        if (choice.isEmpty()) return "";
        if (!node.isAttributeSelection()) return null;
        final String normalizedChoice = choice;

        if (!worldTree) {
            return Double.isFinite(node.attributeSelectionAmount())
                    && com.cranks.dungeons.stat.passive.PassiveStatHelper.WHITELISTED_PASSIVE_ATTRIBUTES.contains(normalizedChoice)
                    && java.util.Set.of("strength", "dexterity", "intelligence").contains(normalizedChoice)
                    ? normalizedChoice : null;
        }

        if (!com.cranks.dungeons.stat.passive.PassiveStatHelper.WHITELISTED_WORLD_ATTRIBUTES.contains(normalizedChoice)
                || node.modifiers() == null) {
            return null;
        }
        boolean offeredByNode = node.modifiers().entrySet().stream()
                .anyMatch(entry -> entry.getValue() != null && Double.isFinite(entry.getValue())
                        && entry.getKey().getKey().map(key ->
                            com.cranks.dungeons.stat.passive.TreeAttributeRules.supported(key.getValue().getNamespace(),
                                key.getValue().getPath(),com.cranks.dungeons.stat.passive.PassiveStatHelper.WHITELISTED_WORLD_ATTRIBUTES)
                            && key.getValue().getPath().equals(normalizedChoice)).orElse(false));
        return offeredByNode ? normalizedChoice : null;
    }

    private static void rejectNodeChoice(net.minecraft.server.network.ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new ShowNotificationPayload(
                Text.literal("That node choice is no longer valid").formatted(Formatting.RED), 60));
    }

    private static void rejectDraft(net.minecraft.server.network.ServerPlayerEntity player, String message) {
        ServerPlayNetworking.send(player, new ShowNotificationPayload(
                Text.literal(message).formatted(Formatting.RED), 80));
    }

    private static java.util.Set<String> getNodeNeighbors(String nodeId, boolean worldTree) {
        return worldTree ? com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNeighbors(nodeId)
                : com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNeighbors(nodeId);
    }

    private static net.minecraft.entity.LivingEntity activeMount(net.minecraft.server.network.ServerPlayerEntity player) {
        if (!player.currentScreenHandler.canUse(player)) return null;
        if (player.currentScreenHandler instanceof com.cranks.dungeons.screen.MountCargoScreenHandler cargo) return cargo.mount();
        if (player.currentScreenHandler instanceof com.cranks.dungeons.mount.HorseScreenHandlerMountAccess horse) return horse.cranks$getMountEntity();
        return null;
    }
    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(RangedFirePayload.ID, RangedFirePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TradeActionPayload.ID,TradeActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TradeStatePayload.ID,TradeStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CoinGainPayload.ID,CoinGainPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CreativeHotbarPayload.ID,CreativeHotbarPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CreativeCatalogPayload.ID,CreativeCatalogPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProgramCrafterPayload.ID,ProgramCrafterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StorageSortPayload.ID,StorageSortPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ArtificialAltarPayload.ID, ArtificialAltarPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RunicAltarPayload.ID, RunicAltarPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AlchemyPayload.ID, AlchemyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CraftMaterialsPayload.ID, CraftMaterialsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SalvageGearPayload.ID, SalvageGearPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RepairGearPayload.ID, RepairGearPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EnchantGearPayload.ID, EnchantGearPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpgradeStationPayload.ID, UpgradeStationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CraftMaterialsResultPayload.ID, CraftMaterialsResultPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                ToggleEnchantmentDisablePayload.ID,
                ToggleEnchantmentDisablePayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ShowNotificationPayload.ID,
                ShowNotificationPayload.CODEC
        );
        PayloadTypeRegistry.playC2S().register(UnlockNodePayload.ID, UnlockNodePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectClassPayload.ID, SelectClassPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BulkUnlockNodesPayload.ID, BulkUnlockNodesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DamageNumberPayload.ID, DamageNumberPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShockArcPayload.ID, ShockArcPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AilmentSyncPayload.ID, AilmentSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AilmentResistedPayload.ID, AilmentResistedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BleedVisualPayload.ID, BleedVisualPayload.CODEC);
        
        // Party packets
        PayloadTypeRegistry.playS2C().register(PartySyncPacket.ID, PartySyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyHealthPacket.ID, PartyHealthPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyInvitePacket.ID, PartyInvitePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPassiveTreePayload.ID, SyncPassiveTreePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerInfoSyncPacket.ID, PlayerInfoSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EntityShieldSyncPacket.ID, EntityShieldSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ManaSyncPacket.ID, ManaSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(HorseGeneticsSyncPacket.ID, HorseGeneticsSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MountDataSyncPacket.ID, MountDataSyncPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(HorseGallopPayload.ID, HorseGallopPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenMountCargoPayload.ID, OpenMountCargoPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetHorseBreedingFocusPayload.ID, SetHorseBreedingFocusPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetMountSpecializationPayload.ID, SetMountSpecializationPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetMountAccessPayload.ID, SetMountAccessPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReleaseMountPayload.ID, ReleaseMountPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MountTransferPayload.ID,MountTransferPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MountWhistlePayload.ID,MountWhistlePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MountWhistleListPayload.ID,MountWhistleListPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MountTransferOfferPayload.ID,MountTransferOfferPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenNameTagScreenPayload.ID, OpenNameTagScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ApplyNameTagPayload.ID, ApplyNameTagPayload.CODEC);
        
        PayloadTypeRegistry.playS2C().register(SyncWorldTreePayload.ID, SyncWorldTreePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UnlockWorldNodePayload.ID, UnlockWorldNodePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BulkUnlockWorldNodesPayload.ID, BulkUnlockWorldNodesPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateNodeChoicePayload.ID, UpdateNodeChoicePayload.CODEC);
    }

    private static boolean isNodeUnlockable(com.cranks.dungeons.component.PassiveDataComponent data, String nodeId) {
        return getNodeNeighbors(nodeId, false).stream().anyMatch(data::hasNode);
    }

    private static boolean isWorldNodeUnlockable(com.cranks.dungeons.component.WorldTreeDataComponent data, String nodeId) {
        if (data.getUnlockedNodes().isEmpty()) return "world_start".equals(nodeId);
        return getNodeNeighbors(nodeId, true).stream().anyMatch(data::hasNode);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(RangedFirePayload.ID, (payload, context) -> context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(TradeActionPayload.ID,(payload,context)->context.server().execute(()->payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CreativeHotbarPayload.ID,(payload,context)->context.server().execute(()->payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CreativeCatalogPayload.ID,(payload,context)->context.server().execute(()->payload.handle(context.player())));
        com.cranks.dungeons.mount.MountWhistleNetworking.register();
        ServerPlayNetworking.registerGlobalReceiver(StorageSortPayload.ID,(payload,context)->context.server().execute(()->payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ProgramCrafterPayload.ID,(payload,context)->context.server().execute(()->payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ArtificialAltarPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(RunicAltarPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(AlchemyPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CraftMaterialsPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(SalvageGearPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(RepairGearPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(EnchantGearPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(UpgradeStationPayload.ID, (payload, context) ->
                context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ApplyNameTagPayload.ID, (payload, context) ->
                context.server().execute(() ->
                        com.cranks.dungeons.name.NameTagManager.applyName(context.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(OpenMountCargoPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!(player.getVehicle() instanceof net.minecraft.entity.LivingEntity mount)
                            || mount.getId() != payload.entityId()
                            || !com.cranks.dungeons.mount.MountManager.usesCustomCargoScreen(mount)) {
                        return;
                    }
                    com.cranks.dungeons.mount.MountManager.openCargo(player, mount);
                }));

        ServerPlayNetworking.registerGlobalReceiver(HorseGallopPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (player.getVehicle() instanceof net.minecraft.entity.passive.HorseEntity horse) {
                        com.cranks.dungeons.horse.HorseBreedingManager.setGallopRequested(
                                horse, player, payload.active());
                    } else if (player.getVehicle() instanceof net.minecraft.entity.LivingEntity mount
                            && mount instanceof com.cranks.dungeons.mount.MountDataProvider) {
                        com.cranks.dungeons.mount.MountManager.setAbilityRequested(
                                mount, player, payload.active());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(SetHorseBreedingFocusPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    var selectedMount = activeMount(player);
                    if (selectedMount == null || selectedMount.getId() != payload.entityId()) return;
                    net.minecraft.entity.Entity entity = player.getEntityWorld().getEntityById(payload.entityId());
                    if (entity instanceof net.minecraft.entity.passive.HorseEntity horse) {
                        com.cranks.dungeons.horse.HorseBreedingManager.setBreedingFocus(
                                horse, player,
                                com.cranks.dungeons.horse.HorseBreedingFocus.fromId(payload.focus()));
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(SetMountSpecializationPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    var selectedMount = activeMount(player);
                    if (selectedMount == null || selectedMount.getId() != payload.entityId()) return;
                    net.minecraft.entity.Entity entity = player.getEntityWorld().getEntityById(payload.entityId());
                    if (entity instanceof net.minecraft.entity.mob.SkeletonHorseEntity skeleton) {
                        com.cranks.dungeons.mount.MountManager.chooseSkeletonSpecialization(
                                skeleton, player, payload.specialization());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(SetMountAccessPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    net.minecraft.entity.LivingEntity mount = null;
                    if (player.currentScreenHandler instanceof net.minecraft.screen.HorseScreenHandler horseHandler
                            && horseHandler instanceof com.cranks.dungeons.mount.HorseScreenHandlerMountAccess access) {
                        mount = access.cranks$getMountEntity();
                    } else if (player.currentScreenHandler
                            instanceof com.cranks.dungeons.screen.MountCargoScreenHandler cargoHandler) {
                        mount = cargoHandler.mount();
                    }
                    if (mount == null || mount.getId() != payload.entityId()
                            || !player.canInteractWithEntity(mount, 4.0)
                            || !com.cranks.dungeons.mount.MountManager.canManageAccess(player, mount)) {
                        return;
                    }
                    if(payload.breeding())com.cranks.dungeons.mount.MountManager.setBreedingAccess(player,mount,
                            com.cranks.dungeons.mount.MountAccessMode.fromId(payload.accessMode()));
                    else com.cranks.dungeons.mount.MountManager.setAccessMode(player, mount,
                            com.cranks.dungeons.mount.MountAccessMode.fromId(payload.accessMode()), payload.storage());
                }));

        ServerPlayNetworking.registerGlobalReceiver(ReleaseMountPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    net.minecraft.entity.LivingEntity mount = null;
                    if (player.currentScreenHandler instanceof net.minecraft.screen.HorseScreenHandler horseHandler
                            && horseHandler instanceof com.cranks.dungeons.mount.HorseScreenHandlerMountAccess access) {
                        mount = access.cranks$getMountEntity();
                    } else if (player.currentScreenHandler
                            instanceof com.cranks.dungeons.screen.MountCargoScreenHandler cargoHandler) {
                        mount = cargoHandler.mount();
                    }
                    if (mount == null || mount.getId() != payload.entityId()
                            || !player.canInteractWithEntity(mount, 4.0)) {
                        return;
                    }
                    if (com.cranks.dungeons.mount.MountManager.releaseOwnership(player, mount)) {
                        player.sendMessage(Text.literal("You released ownership of "
                                + mount.getName().getString() + "."), true);
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(MountTransferPayload.ID,(payload,context)->context.server().execute(()->{
            var player=context.player();
            if(payload.action()==MountTransferPayload.OFFER){
                var mount=activeMount(player);var target=context.server().getPlayerManager().getPlayer(payload.target());
                if(mount!=null&&mount.getId()==payload.entityId()&&player.canInteractWithEntity(mount,4))
                    com.cranks.dungeons.mount.MountTransfers.offer(player,mount,target);
            }else com.cranks.dungeons.mount.MountTransfers.reply(player,payload);
        }));

        ServerPlayNetworking.registerGlobalReceiver(
                ToggleEnchantmentDisablePayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        ItemStack tool = player.getMainHandStack();

                        List<Identifier> relevant = EnchantmentDisableManager.getRelevantEnchantments(tool);

                        if (relevant.isEmpty()) {
                            ServerPlayNetworking.send(
                                    player,
                                    new ShowNotificationPayload(
                                            Text.literal("No toggleable enchantments on this tool").formatted(Formatting.GRAY),
                                            60
                                    )
                            );
                            return;
                        }

                        EnchantmentDisableManager.toggleNext(player.getUuid(), relevant);

                        String label = EnchantmentDisableManager.getDisabledLabel(player.getUuid(), relevant);
                        Text message = label.isEmpty() ? Text.literal("All Enchantments Active").formatted(Formatting.GREEN)
                                : Text.literal("Disabled: " + label).formatted(Formatting.RED);

                        ServerPlayNetworking.send(player, new ShowNotificationPayload(message, 60));
                        // If something is disabled, the tick handler will show it continuously
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UnlockNodePayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        if (!allowProgressionRequest(player, 1)) return;
                        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                        String nodeId = payload.nodeId() != null ? payload.nodeId().trim() : "";
                        var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(nodeId);
                        
                        if (node != null && !node.isAttributeSelection()
                                && !data.hasNode(nodeId) && data.getUnspentPoints() > 0) {
                            if (isNodeUnlockable(data, nodeId)) {
                                runBatched(data, () -> {
                                    data.setUnspentPoints(data.getUnspentPoints() - 1);
                                    data.unlockNode(nodeId);
                                });
                                com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);
                                
                                // Persist to world backup
                                com.cranks.dungeons.component.PassiveTreePersistentState.get(context.server())
                                        .savePlayerData(player.getUuid(), data);
                            }
                        }
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                SelectClassPayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        if (!allowProgressionRequest(player, 1)) return;
                        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                        
                        // Prevent changing class if already chosen
                        if (data.getPlayerClass().isEmpty()) {
                            String cName = payload.className() != null ? payload.className().trim() : "";
                            com.cranks.dungeons.config.PassiveTreeConfig.ClassDefinition def = com.cranks.dungeons.config.PassiveTreeConfig.getClasses().stream()
                                    .filter(c -> c.id().equals(cName))
                                    .findFirst().orElse(null);
                            
                            if (def != null) {
                                runBatched(data, () -> {
                                    data.setPlayerClass(cName);
                                    // Unlocking start node for free
                                    data.unlockNode(def.startNode());
                                });
                                com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);

                                // Persist to world backup
                                com.cranks.dungeons.component.PassiveTreePersistentState.get(context.server())
                                        .savePlayerData(player.getUuid(), data);
                            }
                        }
                    });
                }
        );
        ServerPlayNetworking.registerGlobalReceiver(
                BulkUnlockNodesPayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        if (!allowProgressionRequest(player, payload.nodeChoices().size())) return;
                        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                        java.util.Map<String, String> choices = new java.util.LinkedHashMap<>();
                        for (var entry : payload.nodeChoices().entrySet()) {
                            String nodeId = entry.getKey() == null ? "" : entry.getKey().trim();
                            var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(nodeId);
                            String choice = node == null ? null : validateNodeChoice(node, entry.getValue(), false);
                            if (nodeId.isEmpty() || node == null || choice == null
                                    || (node.isAttributeSelection() && choice.isEmpty())) {
                                rejectDraft(player, "The passive-tree draft contains an invalid node or selection");
                                return;
                            }
                            if (!data.hasNode(nodeId)) choices.put(nodeId, choice);
                        }

                        var plan = com.cranks.dungeons.util.TreeUnlockPlanner.plan(
                                data.getUnlockedNodes(), choices.keySet(), data.getUnspentPoints(),
                                nodeId -> getNodeNeighbors(nodeId, false));
                        if (!plan.successful()) {
                            rejectDraft(player, plan.failure() == com.cranks.dungeons.util.TreeUnlockPlanner.Failure.NOT_ENOUGH_POINTS
                                    ? "Not enough passive-tree points for that draft"
                                    : "That passive-tree draft is no longer connected to an unlocked node");
                            return;
                        }
                        if (plan.unlockOrder().isEmpty()) return;

                        runBatched(data, () -> {
                            data.setUnspentPoints(data.getUnspentPoints() - plan.unlockOrder().size());
                            for (String nodeId : plan.unlockOrder()) {
                                data.unlockNode(nodeId);
                                String choice = choices.get(nodeId);
                                if (choice != null && !choice.isEmpty()) data.setNodeChoice(nodeId, choice);
                            }
                        });

                        com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);
                        com.cranks.dungeons.component.PassiveTreePersistentState.get(context.server())
                                .savePlayerData(player.getUuid(), data);
                    });
                }
        );

        // Unlock Single World Node
        ServerPlayNetworking.registerGlobalReceiver(UnlockWorldNodePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (!allowProgressionRequest(player, 1)) return;
                com.cranks.dungeons.component.WorldTreeDataComponent data = com.cranks.dungeons.component.ModComponents.WORLD_TREE_DATA.get(player);
                String nodeId = payload.nodeId() == null ? "" : payload.nodeId().trim();
                var node = com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNode(nodeId);
                String choice = node == null ? null : validateNodeChoice(node, payload.choice(), true);
                if (data != null && node != null && choice != null
                        && (!node.isAttributeSelection() || !choice.isEmpty())
                        && data.getUnspentPoints() > 0) {
                    if (!data.hasNode(nodeId) && isWorldNodeUnlockable(data, nodeId)) {
                        runBatched(data, () -> {
                            data.setUnspentPoints(data.getUnspentPoints() - 1);
                            data.unlockNode(nodeId);
                            if (!choice.isEmpty()) data.setNodeChoice(nodeId, choice);
                        });
                        player.playSound(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                        com.cranks.dungeons.component.WorldTreePersistentState.get(context.server()).savePlayerData(player.getUuid(), data);
                        com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);
                    }
                }
            });
        });

        // Bulk Unlock World Nodes (Used when user creates nodes in designer mode)
        ServerPlayNetworking.registerGlobalReceiver(BulkUnlockWorldNodesPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (!allowProgressionRequest(player, payload.nodeChoices().size())) return;
                com.cranks.dungeons.component.WorldTreeDataComponent data = com.cranks.dungeons.component.ModComponents.WORLD_TREE_DATA.get(player);
                if (data != null) {
                    java.util.Map<String, String> choices = new java.util.LinkedHashMap<>();
                    for (var entry : payload.nodeChoices().entrySet()) {
                        String nodeId = entry.getKey() == null ? "" : entry.getKey().trim();
                        var node = com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNode(nodeId);
                        String choice = node == null ? null : validateNodeChoice(node, entry.getValue(), true);
                        if (nodeId.isEmpty() || node == null || choice == null
                                || (node.isAttributeSelection() && choice.isEmpty())) {
                            rejectDraft(player, "The world-tree draft contains an invalid node or selection");
                            return;
                        }
                        if (!data.hasNode(nodeId)) choices.put(nodeId, choice);
                    }

                    var plan = com.cranks.dungeons.util.TreeUnlockPlanner.plan(
                            data.getUnlockedNodes(), choices.keySet(), data.getUnspentPoints(),
                            nodeId -> getNodeNeighbors(nodeId, true));
                    if (!plan.successful()) {
                        rejectDraft(player, plan.failure() == com.cranks.dungeons.util.TreeUnlockPlanner.Failure.NOT_ENOUGH_POINTS
                                ? "Not enough world-tree points for that draft"
                                : "That world-tree draft is no longer connected to an unlocked node");
                        return;
                    }
                    if (plan.unlockOrder().isEmpty()) return;

                    runBatched(data, () -> {
                        data.setUnspentPoints(data.getUnspentPoints() - plan.unlockOrder().size());
                        for (String nodeId : plan.unlockOrder()) {
                            data.unlockNode(nodeId);
                            String choice = choices.get(nodeId);
                            if (choice != null && !choice.isEmpty()) data.setNodeChoice(nodeId, choice);
                        }
                    });

                    player.playSound(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                    com.cranks.dungeons.component.WorldTreePersistentState.get(context.server()).savePlayerData(player.getUuid(), data);
                    com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateNodeChoicePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (!allowProgressionRequest(player, 1)) return;

                String nodeId = payload.nodeId() == null ? "" : payload.nodeId().trim();
                if (payload.worldTree()) {
                    var node = com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNode(nodeId);
                    var data = com.cranks.dungeons.component.ModComponents.WORLD_TREE_DATA.get(player);
                    String choice = node == null ? null : validateNodeChoice(node, payload.choice(), true);
                    if (node == null || choice == null || choice.isEmpty() || !data.hasNode(nodeId)) {
                        rejectNodeChoice(player);
                        return;
                    }

                    if (!choice.equals(data.getNodeChoice(nodeId))) {
                        data.setNodeChoice(nodeId, choice);
                        com.cranks.dungeons.component.WorldTreePersistentState.get(context.server())
                                .savePlayerData(player.getUuid(), data);
                        com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);
                    }
                } else {
                    var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(nodeId);
                    var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                    String choice = node == null ? null : validateNodeChoice(node, payload.choice(), false);
                    if (node == null || choice == null || choice.isEmpty() || !data.hasNode(nodeId)) {
                        rejectNodeChoice(player);
                        return;
                    }

                    if (!choice.equals(data.getNodeChoice(nodeId))) {
                        data.setNodeChoice(nodeId, choice);
                        com.cranks.dungeons.component.PassiveTreePersistentState.get(context.server())
                                .savePlayerData(player.getUuid(), data);
                        com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);
                    }
                }
            });
        });
    }
    
    public static void syncPartyToPlayer(net.minecraft.server.MinecraftServer server, net.minecraft.server.network.ServerPlayerEntity player, Party party, boolean joined) {
        List<PartyMemberData> memberData = new java.util.ArrayList<>();
        
        for (java.util.UUID memberUuid : party.getMembers()) {
            net.minecraft.server.network.ServerPlayerEntity memberPlayer = server.getPlayerManager().getPlayer(memberUuid);
            if (memberPlayer != null) {
                float health = memberPlayer.getHealth();
                float maxHealth = memberPlayer.getMaxHealth();
                float energyShield = 0;
                if (memberPlayer instanceof EnergyShieldProvider provider) {
                    energyShield = provider.cranksdungeons$getCurrentEnergyShield();
                }
                float maxEnergyShield = (float) memberPlayer.getAttributeValue(ModAttributes.ENERGY_SHIELD);
                
                memberData.add(new PartyMemberData(
                    memberUuid,
                    memberPlayer.getName().getString(),
                    health,
                    maxHealth,
                    memberPlayer.getAbsorptionAmount(),
                    energyShield,
                    maxEnergyShield
                ));
            }
        }
        
        ServerPlayNetworking.send(player, new PartySyncPacket(memberData, party.getLeader(), joined));
    }
    
    public static void syncPartyToAll(net.minecraft.server.MinecraftServer server, Party party, boolean joined) {
        for (java.util.UUID memberUuid : party.getMembers()) {
            net.minecraft.server.network.ServerPlayerEntity player = server.getPlayerManager().getPlayer(memberUuid);
            if (player != null) {
                syncPartyToPlayer(server, player, party, joined);
            }
        }
    }
    
    public static void sendPartyHealthUpdate(net.minecraft.server.MinecraftServer server, Party party, java.util.UUID playerUuid) {
        net.minecraft.server.network.ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
        if (player == null) return;
        
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float energyShield = 0;
        if (player instanceof EnergyShieldProvider provider) {
            energyShield = provider.cranksdungeons$getCurrentEnergyShield();
        }
        float maxEnergyShield = (float) player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        
        PartyHealthPacket packet = new PartyHealthPacket(playerUuid, health, maxHealth, player.getAbsorptionAmount(), energyShield, maxEnergyShield);
        
        // Send to ALL party members including the player themselves
        for (java.util.UUID memberUuid : party.getMembers()) {
            net.minecraft.server.network.ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberUuid);
            if (member != null) {
                ServerPlayNetworking.send(member, packet);
            }
        }
    }
    
    public static void sendLeavePacket(net.minecraft.server.MinecraftServer server, java.util.UUID playerUuid) {
        net.minecraft.server.network.ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
        if (player != null) {
            ServerPlayNetworking.send(player, new PartySyncPacket(java.util.Collections.emptyList(), null, false));
        }
    }

    public static void syncPassiveTreeToPlayer(net.minecraft.server.network.ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncPassiveTreePayload(
                com.cranks.dungeons.config.PassiveTreeConfig.getNodes(),
                com.cranks.dungeons.config.PassiveTreeConfig.getClasses()
        ));
    }

    public static void syncPassiveTreeToAll(net.minecraft.server.MinecraftServer server) {
        SyncPassiveTreePayload payload = new SyncPassiveTreePayload(
                com.cranks.dungeons.config.PassiveTreeConfig.getNodes(),
                com.cranks.dungeons.config.PassiveTreeConfig.getClasses()
        );
        for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncWorldTreeToPlayer(net.minecraft.server.network.ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncWorldTreePayload(
                com.cranks.dungeons.config.WorldTreeConfig.getNodes()
        ));
    }

    public static void syncWorldTreeToAll(net.minecraft.server.MinecraftServer server) {
        SyncWorldTreePayload payload = new SyncWorldTreePayload(
                com.cranks.dungeons.config.WorldTreeConfig.getNodes()
        );
        for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncPlayerInfoToTrackers(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.server.MinecraftServer server) {
        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
        String className = data.getPlayerClass();
        int level = player.experienceLevel;
        
        String partyLeaderName = "";
        var party = PartyManager.getParty(player.getUuid()).orElse(null);
        if (party != null) {
            net.minecraft.server.network.ServerPlayerEntity leader = server.getPlayerManager().getPlayer(party.getLeader());
            if (leader != null) {
                partyLeaderName = leader.getName().getString();
            }
        }

        PlayerInfoSyncPacket packet = new PlayerInfoSyncPacket(player.getId(), level, className, partyLeaderName);
        
        // Send to trackers
        for (net.minecraft.server.network.ServerPlayerEntity tracker : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(tracker, packet);
        }
        // Also send to self (for HUD consistency if needed)
        ServerPlayNetworking.send(player, packet);
    }
}
