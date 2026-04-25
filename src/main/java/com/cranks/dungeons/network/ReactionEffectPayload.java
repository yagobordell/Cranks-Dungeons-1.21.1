package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static com.cranks.dungeons.CranksDungeons.MOD_ID;

import net.minecraft.network.codec.PacketCodecs;

public record ReactionEffectPayload(BlockPos pos, String reactionType) implements CustomPayload {
    public static final Id<ReactionEffectPayload> ID = new Id<>(Identifier.of(MOD_ID, "reaction_effect"));

    public static final PacketCodec<RegistryByteBuf, ReactionEffectPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ReactionEffectPayload::pos,
            PacketCodecs.STRING, ReactionEffectPayload::reactionType,
            ReactionEffectPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
