package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client request to open the shared cargo screen for a ridden custom mount. */
public record OpenMountCargoPayload(int entityId) implements CustomPayload {
    public static final Id<OpenMountCargoPayload> ID =
            new Id<>(Identifier.of(CranksDungeons.MOD_ID, "open_mount_cargo"));
    public static final PacketCodec<RegistryByteBuf, OpenMountCargoPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, OpenMountCargoPayload::entityId,
            OpenMountCargoPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
