package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EntityShieldSyncPacket(int entityId, float currentShield, float maxShield) implements CustomPayload {
    public static final CustomPayload.Id<EntityShieldSyncPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("cranks-dungeons", "entity_shield_sync"));

    public static final PacketCodec<RegistryByteBuf, EntityShieldSyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, EntityShieldSyncPacket::entityId,
            PacketCodecs.FLOAT, EntityShieldSyncPacket::currentShield,
            PacketCodecs.FLOAT, EntityShieldSyncPacket::maxShield,
            EntityShieldSyncPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void handle(EntityShieldSyncPacket payload, net.minecraft.client.MinecraftClient client) {
        client.execute(() -> {
            com.cranks.dungeons.client.render.EntityStatBarRenderer.updateEntityShield(
                payload.entityId(), payload.currentShield(), payload.maxShield()
            );
        });
    }
}
