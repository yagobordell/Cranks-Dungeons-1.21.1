package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateNodeChoicePayload(String nodeId, String choice, boolean worldTree) implements CustomPayload {
    public static final Id<UpdateNodeChoicePayload> ID =
            new Id<>(Identifier.of(CranksDungeons.MOD_ID, "update_node_choice"));

    public static final PacketCodec<RegistryByteBuf, UpdateNodeChoicePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(128), UpdateNodeChoicePayload::nodeId,
            PacketCodecs.string(128), UpdateNodeChoicePayload::choice,
            PacketCodecs.BOOL, UpdateNodeChoicePayload::worldTree,
            UpdateNodeChoicePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
