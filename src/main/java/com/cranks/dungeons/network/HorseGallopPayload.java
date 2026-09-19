package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HorseGallopPayload(boolean active) implements CustomPayload {
    public static final Id<HorseGallopPayload> ID =
            new Id<>(Identifier.of(CranksDungeons.MOD_ID, "horse_gallop"));
    public static final PacketCodec<RegistryByteBuf, HorseGallopPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, HorseGallopPayload::active,
            HorseGallopPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
