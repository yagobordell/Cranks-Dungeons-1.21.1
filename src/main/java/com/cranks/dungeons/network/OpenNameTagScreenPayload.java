package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

/** Opens the client naming screen for the entity the player just selected. */
public record OpenNameTagScreenPayload(int entityId, String currentName, boolean offHand, UUID token)
        implements CustomPayload {
    public static final UUID PAID = new UUID(0, 0);
    public OpenNameTagScreenPayload(int entityId,String currentName,boolean offHand){this(entityId,currentName,offHand,PAID);}
    public boolean free(){return !PAID.equals(token);}
    public static final Id<OpenNameTagScreenPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "open_name_tag_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenNameTagScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, OpenNameTagScreenPayload::entityId,
            PacketCodecs.string(64), OpenNameTagScreenPayload::currentName,
            PacketCodecs.BOOL, OpenNameTagScreenPayload::offHand,
            Uuids.PACKET_CODEC, OpenNameTagScreenPayload::token,
            OpenNameTagScreenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
