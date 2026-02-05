package com.cranks.dungeons.network;

import com.cranks.dungeons.EnergyShieldProvider;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs; // <--- You need this import
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import com.cranks.dungeons.CranksDungeons;

public record EnergyShieldSyncPacket(float currentShield) implements CustomPayload {

    public static final CustomPayload.Id<EnergyShieldSyncPacket> ID =
            new CustomPayload.Id<>(Identifier.of(CranksDungeons.MOD_ID, "energy_shield_sync"));

    public static final PacketCodec<RegistryByteBuf, EnergyShieldSyncPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.FLOAT,
                    EnergyShieldSyncPacket::currentShield,
                    EnergyShieldSyncPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(EnergyShieldSyncPacket packet, net.minecraft.client.MinecraftClient client) {
        client.execute(() -> {
            if (client.player instanceof EnergyShieldProvider provider) {
                provider.cranksdungeons$setCurrentEnergyShield(packet.currentShield());
            }
        });
    }
}