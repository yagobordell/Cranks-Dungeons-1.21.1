package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record StartQualityUpgradePayload(boolean start) implements CustomPayload {
    public static final Id<StartQualityUpgradePayload> ID = new Id<>(Identifier.of("cranks-dungeons", "start_quality_upgrade"));

    public static final PacketCodec<RegistryByteBuf, StartQualityUpgradePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, StartQualityUpgradePayload::start,
            StartQualityUpgradePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
