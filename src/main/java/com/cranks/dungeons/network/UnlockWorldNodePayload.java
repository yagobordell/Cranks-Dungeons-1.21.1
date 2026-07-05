package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UnlockWorldNodePayload(String nodeId, String choice) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "unlock_world_node");
    public static final CustomPayload.Id<UnlockWorldNodePayload> ID = new CustomPayload.Id<>(ID_IDENT);

    public static final PacketCodec<RegistryByteBuf, UnlockWorldNodePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(128), UnlockWorldNodePayload::nodeId,
            PacketCodecs.string(128), UnlockWorldNodePayload::choice,
            UnlockWorldNodePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
