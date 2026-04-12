package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record AilmentSyncPayload(int entityId, List<AilmentEntry> ailments) implements CustomPayload {
    public static final Id<AilmentSyncPayload> ID = new Id<>(Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "ailment_sync"));

    public static final PacketCodec<RegistryByteBuf, AilmentSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, AilmentSyncPayload::entityId,
            AilmentEntry.CODEC.collect(PacketCodecs.toList()), AilmentSyncPayload::ailments,
            AilmentSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record AilmentEntry(Identifier texture, int stacks) {
        public static final PacketCodec<RegistryByteBuf, AilmentEntry> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, AilmentEntry::texture,
                PacketCodecs.VAR_INT, AilmentEntry::stacks,
                AilmentEntry::new
        );
    }
}
