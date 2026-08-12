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

    public static void registerPayloads() {
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
        
        // Party packets
        PayloadTypeRegistry.playS2C().register(PartySyncPacket.ID, PartySyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyHealthPacket.ID, PartyHealthPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyInvitePacket.ID, PartyInvitePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncPassiveTreePayload.ID, SyncPassiveTreePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerInfoSyncPacket.ID, PlayerInfoSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EntityShieldSyncPacket.ID, EntityShieldSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ManaSyncPacket.ID, ManaSyncPacket.CODEC);
        
        PayloadTypeRegistry.playS2C().register(SyncWorldTreePayload.ID, SyncWorldTreePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UnlockWorldNodePayload.ID, UnlockWorldNodePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BulkUnlockWorldNodesPayload.ID, BulkUnlockWorldNodesPayload.CODEC);
    }

    private static boolean isNodeUnlockable(com.cranks.dungeons.component.PassiveDataComponent data, String nodeId) {
        if (nodeId == null) return false;
        String id = nodeId.trim();
        var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(id);
        if (node == null) return false;

        // 1. Check if ANY node I connect to is already unlocked
        for (String connId : node.connections()) {
            // Verify node existence before checking status
            if (com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNodes().containsKey(connId)) {
                if (data.hasNode(connId)) return true;
            }
        }

        // 2. Check if ANY already unlocked node connects TO me
        for (var entry : com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNodes().entrySet()) {
            if (data.hasNode(entry.getKey())) {
                boolean linksToMe = false;
                for (String c : entry.getValue().connections()) {
                    if (c != null && c.trim().equals(id)) {
                        linksToMe = true;
                        break;
                    }
                }
                if (linksToMe) return true;
            }
        }

        return false;
    }

    private static boolean isWorldNodeUnlockable(com.cranks.dungeons.component.WorldTreeDataComponent data, String nodeId) {
        if (nodeId == null) return false;
        String id = nodeId.trim();
        var node = com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNode(id);
        if (node == null) return false;
        
        // Allow unlocking any node if the tree is completely empty
        if (data.getUnlockedNodes().isEmpty()) return true;

        for (String connId : node.connections()) {
            if (com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNodes().containsKey(connId)) {
                if (data.hasNode(connId)) return true;
            }
        }

        for (var entry : com.cranks.dungeons.stat.passive.WorldNodeRegistry.getNodes().entrySet()) {
            if (data.hasNode(entry.getKey())) {
                boolean linksToMe = false;
                for (String c : entry.getValue().connections()) {
                    if (c != null && c.trim().equals(id)) {
                        linksToMe = true;
                        break;
                    }
                }
                if (linksToMe) return true;
            }
        }
        return false;
    }

    public static void registerServerReceivers() {
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
                        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                        String nodeId = payload.nodeId() != null ? payload.nodeId().trim() : "";
                        var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(nodeId);
                        
                        if (node != null && !data.hasNode(nodeId) && data.getUnspentPoints() > 0) {
                            if (isNodeUnlockable(data, nodeId)) {
                                data.setUnspentPoints(data.getUnspentPoints() - 1);
                                data.unlockNode(nodeId);
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
                        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                        
                        // Prevent changing class if already chosen
                        if (data.getPlayerClass().isEmpty()) {
                            String cName = payload.className() != null ? payload.className().trim() : "";
                            com.cranks.dungeons.config.PassiveTreeConfig.ClassDefinition def = com.cranks.dungeons.config.PassiveTreeConfig.getClasses().stream()
                                    .filter(c -> c.id().equals(cName))
                                    .findFirst().orElse(null);
                            
                            if (def != null) {
                                data.setPlayerClass(cName);
                                // Unlocking start node for free
                                data.unlockNode(def.startNode());
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
                        var data = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player);
                        java.util.Map<String, String> remainingNodes = new java.util.HashMap<>(payload.nodeChoices());
                        
                        boolean totalChanged = false;
                        boolean progress = true;
                        
                        // Keep trying to unlock nodes as long as we make progress in a pass
                        while (progress && !remainingNodes.isEmpty()) {
                            progress = false;
                            java.util.Iterator<java.util.Map.Entry<String, String>> it = remainingNodes.entrySet().iterator();
                            while (it.hasNext()) {
                                java.util.Map.Entry<String, String> entry = it.next();
                                String rawId = entry.getKey();
                                String nodeId = rawId != null ? rawId.trim() : "";
                                String choice = entry.getValue() != null ? entry.getValue().trim() : "";
                                if (nodeId.isEmpty()) { it.remove(); continue; }
                                var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(nodeId);
                                
                                if (node != null && !data.hasNode(nodeId) && data.getUnspentPoints() > 0) {
                                    if (isNodeUnlockable(data, nodeId)) {
                                        data.setUnspentPoints(data.getUnspentPoints() - 1);
                                        data.unlockNode(nodeId);
                                        if (!choice.isEmpty()) {
                                            data.setNodeChoice(nodeId, choice);
                                        }
                                        it.remove();
                                        progress = true;
                                        totalChanged = true;
                                    }
                                } else if (data.hasNode(nodeId)) {
                                    it.remove(); // Already unlocked, just clean up
                                }
                            }
                        }
                        
                        if (totalChanged) {
                            com.cranks.dungeons.stat.passive.PassiveSkillApplier.updatePlayerPassives(player);

                            // Persist to world backup
                            com.cranks.dungeons.component.PassiveTreePersistentState.get(context.server())
                                    .savePlayerData(player.getUuid(), data);
                        }
                    });
                }
        );

        // Unlock Single World Node
        ServerPlayNetworking.registerGlobalReceiver(UnlockWorldNodePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                com.cranks.dungeons.component.WorldTreeDataComponent data = com.cranks.dungeons.component.ModComponents.WORLD_TREE_DATA.get(player);
                if (data != null && data.getUnspentPoints() > 0) {
                    if (!data.hasNode(payload.nodeId()) && isWorldNodeUnlockable(data, payload.nodeId())) {
                        data.setUnspentPoints(data.getUnspentPoints() - 1);
                        data.unlockNode(payload.nodeId());
                        if (payload.choice() != null && !payload.choice().isEmpty()) {
                            data.setNodeChoice(payload.nodeId(), payload.choice());
                        }
                        player.playSound(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                        com.cranks.dungeons.component.WorldTreePersistentState.get(context.server()).savePlayerData(player.getUuid(), data);
                    }
                }
            });
        });

        // Bulk Unlock World Nodes (Used when user creates nodes in designer mode)
        ServerPlayNetworking.registerGlobalReceiver(BulkUnlockWorldNodesPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                com.cranks.dungeons.component.WorldTreeDataComponent data = com.cranks.dungeons.component.ModComponents.WORLD_TREE_DATA.get(player);
                if (data != null) {
                    // For bulk unlock, we simulate the unlocks to ensure the path is valid
                    java.util.Set<String> simulatedUnlocked = new java.util.HashSet<>(data.getUnlockedNodes());
                    int requiredPoints = 0;
                    boolean validPath = true;
                    
                    // Simple validation: just ensure we have enough points for the ones we don't have
                    for (String id : payload.nodeChoices().keySet()) {
                        if (!data.hasNode(id)) requiredPoints++;
                    }

                    if (data.getUnspentPoints() >= requiredPoints) {
                        data.setUnspentPoints(data.getUnspentPoints() - requiredPoints);
                        for (var entry : payload.nodeChoices().entrySet()) {
                            data.unlockNode(entry.getKey());
                            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                                data.setNodeChoice(entry.getKey(), entry.getValue());
                            }
                        }
                        player.playSound(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                        com.cranks.dungeons.component.WorldTreePersistentState.get(context.server()).savePlayerData(player.getUuid(), data);
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
