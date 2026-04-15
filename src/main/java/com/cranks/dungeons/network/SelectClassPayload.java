package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SelectClassPayload(String className) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "select_class");
    public static final CustomPayload.Id<SelectClassPayload> ID = new CustomPayload.Id<>(ID_IDENT);
    
    public static final PacketCodec<RegistryByteBuf, SelectClassPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectClassPayload::className,
            SelectClassPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
