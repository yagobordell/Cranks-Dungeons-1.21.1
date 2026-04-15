package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record BulkUnlockNodesPayload(List<String> nodeIds) implements CustomPayload {
    public static final Id<BulkUnlockNodesPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "bulk_unlock_nodes"));

    public static final PacketCodec<RegistryByteBuf, BulkUnlockNodesPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()), BulkUnlockNodesPayload::nodeIds,
            BulkUnlockNodesPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
