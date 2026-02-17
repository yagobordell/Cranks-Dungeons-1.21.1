package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StatOptionsSyncPacket(
        int syncId,
        String stat0, String stat1, String stat2,
        int tier0, int tier1, int tier2
) implements CustomPayload {

    public static final Id<StatOptionsSyncPacket> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "stat_options_sync"));

    public static final PacketCodec<RegistryByteBuf, StatOptionsSyncPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, StatOptionsSyncPacket::syncId,
            PacketCodecs.STRING, StatOptionsSyncPacket::stat0,
            PacketCodecs.STRING, StatOptionsSyncPacket::stat1,
            PacketCodecs.STRING, StatOptionsSyncPacket::stat2,
            PacketCodecs.INTEGER, StatOptionsSyncPacket::tier0,
            PacketCodecs.INTEGER, StatOptionsSyncPacket::tier1,
            PacketCodecs.INTEGER, StatOptionsSyncPacket::tier2,
            StatOptionsSyncPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}