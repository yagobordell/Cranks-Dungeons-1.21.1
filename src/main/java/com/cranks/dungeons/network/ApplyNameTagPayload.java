package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

/** Server-authoritative request to apply the name entered in the naming screen. */
public record ApplyNameTagPayload(int entityId, String name, boolean offHand, UUID token)
        implements CustomPayload {
    public ApplyNameTagPayload(int entityId,String name,boolean offHand){this(entityId,name,offHand,OpenNameTagScreenPayload.PAID);}
    public static final Id<ApplyNameTagPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "apply_name_tag"));
    public static final PacketCodec<RegistryByteBuf, ApplyNameTagPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ApplyNameTagPayload::entityId,
            PacketCodecs.string(64), ApplyNameTagPayload::name,
            PacketCodecs.BOOL, ApplyNameTagPayload::offHand,
            Uuids.PACKET_CODEC, ApplyNameTagPayload::token,
            ApplyNameTagPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
