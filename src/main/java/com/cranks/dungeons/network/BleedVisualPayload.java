package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

/** Confirmed hit/burst feedback, independent of damage-number preferences and DoT ticks. */
public record BleedVisualPayload(int entityId, UUID entityUuid, boolean burst) implements CustomPayload {
    public static final Id<BleedVisualPayload> ID = new Id<>(Identifier.of("cranks-dungeons", "bleed_visual"));
    public static final PacketCodec<RegistryByteBuf, BleedVisualPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BleedVisualPayload::entityId,
            Uuids.PACKET_CODEC, BleedVisualPayload::entityUuid,
            PacketCodecs.BOOL, BleedVisualPayload::burst, BleedVisualPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
