package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetMountAccessPayload(int entityId, String accessMode) implements CustomPayload {
    public static final Id<SetMountAccessPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "set_mount_access"));
    public static final PacketCodec<RegistryByteBuf, SetMountAccessPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SetMountAccessPayload::entityId,
            PacketCodecs.STRING, SetMountAccessPayload::accessMode,
            SetMountAccessPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
