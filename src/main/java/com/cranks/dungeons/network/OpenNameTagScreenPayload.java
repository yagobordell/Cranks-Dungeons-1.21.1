package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Opens the client naming screen for the entity the player just selected. */
public record OpenNameTagScreenPayload(int entityId, String currentName, boolean offHand)
        implements CustomPayload {
    public static final Id<OpenNameTagScreenPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "open_name_tag_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenNameTagScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, OpenNameTagScreenPayload::entityId,
            PacketCodecs.STRING, OpenNameTagScreenPayload::currentName,
            PacketCodecs.BOOL, OpenNameTagScreenPayload::offHand,
            OpenNameTagScreenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
