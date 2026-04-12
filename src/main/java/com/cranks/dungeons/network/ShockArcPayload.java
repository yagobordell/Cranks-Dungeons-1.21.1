package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record ShockArcPayload(Vec3d start, Vec3d end, int color) implements CustomPayload {
    public static final Id<ShockArcPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "shock_arc"));
    
    public static final PacketCodec<RegistryByteBuf, ShockArcPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeDouble(value.start.x);
                buf.writeDouble(value.start.y);
                buf.writeDouble(value.start.z);
                buf.writeDouble(value.end.x);
                buf.writeDouble(value.end.y);
                buf.writeDouble(value.end.z);
                buf.writeInt(value.color);
            },
            buf -> new ShockArcPayload(
                    new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readInt()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
