package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleEnchantmentDisablePayload() implements CustomPayload {

    public static final Id<ToggleEnchantmentDisablePayload> ID =
            new Id<>(Identifier.of("cranks-dungeons", "toggle_enchantment_disable"));

    public static final PacketCodec<RegistryByteBuf, ToggleEnchantmentDisablePayload> CODEC =
            PacketCodec.unit(new ToggleEnchantmentDisablePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}