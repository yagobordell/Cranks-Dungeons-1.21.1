package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetMountSpecializationPayload(int entityId, String specialization)
        implements CustomPayload {
    public static final Id<SetMountSpecializationPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "set_mount_specialization"));
    public static final PacketCodec<RegistryByteBuf, SetMountSpecializationPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SetMountSpecializationPayload::entityId,
            PacketCodecs.STRING, SetMountSpecializationPayload::specialization,
            SetMountSpecializationPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
