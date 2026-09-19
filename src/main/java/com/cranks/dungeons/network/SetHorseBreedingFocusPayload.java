package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetHorseBreedingFocusPayload(int entityId, String focus) implements CustomPayload {
    public static final Id<SetHorseBreedingFocusPayload> ID =
            new Id<>(Identifier.of(CranksDungeons.MOD_ID, "set_horse_breeding_focus"));
    public static final PacketCodec<RegistryByteBuf, SetHorseBreedingFocusPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SetHorseBreedingFocusPayload::entityId,
            PacketCodecs.string(16), SetHorseBreedingFocusPayload::focus,
            SetHorseBreedingFocusPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
