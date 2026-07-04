package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record ShowNotificationPayload(Text message, int durationTicks) implements CustomPayload {
    public static final CustomPayload.Id<ShowNotificationPayload> ID = new CustomPayload.Id<>(Identifier.of("cranks-dungeons", "show_notification"));
    public static final PacketCodec<RegistryByteBuf, ShowNotificationPayload> CODEC = PacketCodec.tuple(
            TextCodecs.REGISTRY_PACKET_CODEC, ShowNotificationPayload::message,
            net.minecraft.network.codec.PacketCodecs.INTEGER, ShowNotificationPayload::durationTicks,
            ShowNotificationPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
