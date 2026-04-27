package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerInfoSyncPacket(int entityId, int level, String className, String partyLeaderName) implements CustomPayload {
    public static final CustomPayload.Id<PlayerInfoSyncPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("cranks-dungeons", "player_info_sync"));

    public static final PacketCodec<RegistryByteBuf, PlayerInfoSyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PlayerInfoSyncPacket::entityId,
            PacketCodecs.VAR_INT, PlayerInfoSyncPacket::level,
            PacketCodecs.STRING, PlayerInfoSyncPacket::className,
            PacketCodecs.STRING, PlayerInfoSyncPacket::partyLeaderName,
            PlayerInfoSyncPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(PlayerInfoSyncPacket payload, net.minecraft.client.MinecraftClient client) {
        client.execute(() -> {
            com.cranks.dungeons.client.render.EntityStatBarRenderer.updatePlayerInfo(
                payload.entityId(), payload.level(), payload.className(), payload.partyLeaderName()
            );
        });
    }
}
