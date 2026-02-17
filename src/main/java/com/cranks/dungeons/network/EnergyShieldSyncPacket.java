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
                float old = provider.cranksdungeons$getCurrentEnergyShield();
                float now = packet.currentShield();

                provider.cranksdungeons$setCurrentEnergyShield(now);

                // Trigger camera shake only when shield actually loses value (hit/break), not regen
                if (now < old - 0.05f) { // small epsilon
                    com.cranks.dungeons.client.ClientShieldEffects.triggerShake();
                }
            }
        });
    }
}