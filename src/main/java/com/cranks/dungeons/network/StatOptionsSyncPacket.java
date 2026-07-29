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

    public static final PacketCodec<RegistryByteBuf, StatOptionsSyncPacket> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeInt(value.syncId());
            PacketCodecs.STRING.encode(buf, value.stat0());
            PacketCodecs.STRING.encode(buf, value.stat1());
            PacketCodecs.STRING.encode(buf, value.stat2());
            buf.writeInt(value.tier0());
            buf.writeInt(value.tier1());
            buf.writeInt(value.tier2());
        },
        buf -> {
            return new StatOptionsSyncPacket(
                buf.readInt(),
                PacketCodecs.STRING.decode(buf),
                PacketCodecs.STRING.decode(buf),
                PacketCodecs.STRING.decode(buf),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
            );
        }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}