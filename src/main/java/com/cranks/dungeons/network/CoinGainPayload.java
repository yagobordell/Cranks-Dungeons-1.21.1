package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** A reward delta, separate from text notifications and wallet balance synchronization. */
public record CoinGainPayload(long coins) implements CustomPayload {
    public static final Id<CoinGainPayload> ID = new Id<>(Identifier.of("cranks-dungeons", "coin_gain"));
    public static final PacketCodec<RegistryByteBuf, CoinGainPayload> CODEC = new PacketCodec<>() {
        @Override public CoinGainPayload decode(RegistryByteBuf buffer) { return new CoinGainPayload(buffer.readVarLong()); }
        @Override public void encode(RegistryByteBuf buffer, CoinGainPayload payload) { buffer.writeVarLong(payload.coins()); }
    };

    @Override public Id<CoinGainPayload> getId() { return ID; }
}
