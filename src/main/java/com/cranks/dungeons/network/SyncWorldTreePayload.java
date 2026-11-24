package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record SyncWorldTreePayload(
        List<PassiveNode> nodes
) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "sync_world_tree");
    public static final CustomPayload.Id<SyncWorldTreePayload> ID = new CustomPayload.Id<>(ID_IDENT);

    public static final PacketCodec<RegistryByteBuf, SyncWorldTreePayload> CODEC = PacketCodec.tuple(
            PassiveNodePacketCodec.CODEC.collect(PacketCodecs.toList()), SyncWorldTreePayload::nodes,
            SyncWorldTreePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
