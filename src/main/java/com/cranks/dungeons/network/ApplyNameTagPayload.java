package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server-authoritative request to apply the name entered in the naming screen. */
public record ApplyNameTagPayload(int entityId, String name, boolean offHand)
        implements CustomPayload {
    public static final Id<ApplyNameTagPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "apply_name_tag"));
    public static final PacketCodec<RegistryByteBuf, ApplyNameTagPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ApplyNameTagPayload::entityId,
            PacketCodecs.STRING, ApplyNameTagPayload::name,
            PacketCodecs.BOOL, ApplyNameTagPayload::offHand,
            ApplyNameTagPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
