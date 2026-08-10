package com.cranks.dungeons.network;

import com.cranks.dungeons.ManaProvider;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import com.cranks.dungeons.CranksDungeons;

public record ManaSyncPacket(float currentMana, float maxMana) implements CustomPayload {

    public static final CustomPayload.Id<ManaSyncPacket> ID =
            new CustomPayload.Id<>(Identifier.of(CranksDungeons.MOD_ID, "mana_sync"));

    public static final PacketCodec<RegistryByteBuf, ManaSyncPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.FLOAT,
                    ManaSyncPacket::currentMana,
                    PacketCodecs.FLOAT,
                    ManaSyncPacket::maxMana,
                    ManaSyncPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(ManaSyncPacket packet, net.minecraft.client.MinecraftClient client) {
        client.execute(() -> {
            if (client.player instanceof ManaProvider provider) {
                provider.cranksdungeons$setCurrentMana(packet.currentMana());
            }
        });
    }
}
