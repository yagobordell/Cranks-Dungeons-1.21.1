package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UnlockNodePayload(String nodeId) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "unlock_node");
    public static final CustomPayload.Id<UnlockNodePayload> ID = new CustomPayload.Id<>(ID_IDENT);
    
    public static final PacketCodec<RegistryByteBuf, UnlockNodePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, UnlockNodePayload::nodeId,
            UnlockNodePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
