package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Map;

public record BulkUnlockWorldNodesPayload(Map<String, String> nodeChoices) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "bulk_unlock_world_nodes");
    public static final CustomPayload.Id<BulkUnlockWorldNodesPayload> ID = new CustomPayload.Id<>(ID_IDENT);

    public static final PacketCodec<RegistryByteBuf, BulkUnlockWorldNodesPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.map(java.util.HashMap::new, PacketCodecs.string(128), PacketCodecs.string(128)), BulkUnlockWorldNodesPayload::nodeChoices,
            BulkUnlockWorldNodesPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
