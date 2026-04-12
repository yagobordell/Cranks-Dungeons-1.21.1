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
        PayloadTypeRegistry.playS2C().register(DamageNumberPayload.ID, DamageNumberPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShockArcPayload.ID, ShockArcPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AilmentSyncPayload.ID, AilmentSyncPayload.CODEC);
        
        // Party packets
        PayloadTypeRegistry.playS2C().register(PartySyncPacket.ID, PartySyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyHealthPacket.ID, PartyHealthPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PartyInvitePacket.ID, PartyInvitePacket.CODEC);
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
}
