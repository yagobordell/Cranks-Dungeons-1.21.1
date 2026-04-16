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

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                ToggleEnchantmentDisablePayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        ItemStack tool = player.getMainHandStack();

                        List<Identifier> relevant = EnchantmentDisableManager.getRelevantEnchantments(tool);

                        if (relevant.isEmpty()) {
                            player.sendMessage(
                                    Text.literal("No toggleable enchantments on this tool")
                                            .formatted(Formatting.GRAY),
                                    true
                            );
                            return;
                        }

                        EnchantmentDisableManager.toggleNext(player.getUuid(), relevant);

                        String label = EnchantmentDisableManager.getDisabledLabel(player.getUuid(), relevant);
                        if (label == null) {
                            // Toggled to "all enabled"
                            EnchantmentDisableTickHandler.triggerEnabledMessage(player.getUuid());
                        }
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
                        java.util.List<String> remainingNodes = new java.util.ArrayList<>(payload.nodeIds());
                        
                        boolean totalChanged = false;
                        boolean progress = true;
                        
                        // Keep trying to unlock nodes as long as we make progress in a pass
                        while (progress && !remainingNodes.isEmpty()) {
                            progress = false;
                            java.util.Iterator<String> it = remainingNodes.iterator();
                            while (it.hasNext()) {
                                String rawId = it.next();
                                String nodeId = rawId != null ? rawId.trim() : "";
                                if (nodeId.isEmpty()) { it.remove(); continue; }
                                var node = com.cranks.dungeons.stat.passive.PassiveNodeRegistry.getNode(nodeId);
                                
                                if (node != null && !data.hasNode(nodeId) && data.getUnspentPoints() > 0) {
                                    if (isNodeUnlockable(data, nodeId)) {
                                        data.setUnspentPoints(data.getUnspentPoints() - 1);
                                        data.unlockNode(nodeId);
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
}
