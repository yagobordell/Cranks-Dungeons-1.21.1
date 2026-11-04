package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReleaseMountPayload(int entityId) implements CustomPayload {
    public static final Id<ReleaseMountPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "release_mount"));
    public static final PacketCodec<RegistryByteBuf, ReleaseMountPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ReleaseMountPayload::entityId,
            ReleaseMountPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
