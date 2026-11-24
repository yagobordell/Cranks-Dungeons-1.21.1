package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

/** A brief visual event, separate from persistent ailment state. */
public record AilmentResistedPayload(int entityId, UUID entityUuid) implements CustomPayload {
    public static final Id<AilmentResistedPayload> ID = new Id<>(Identifier.of("cranks-dungeons", "ailment_resisted"));
    public static final PacketCodec<RegistryByteBuf, AilmentResistedPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, AilmentResistedPayload::entityId,
            Uuids.PACKET_CODEC, AilmentResistedPayload::entityUuid, AilmentResistedPayload::new);

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
