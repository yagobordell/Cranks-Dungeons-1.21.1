package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DamageNumberPayload(
        double x, double y, double z,
        List<DamageEntry> entries
) implements CustomPayload {

    public static final Id<DamageNumberPayload> ID =
            new Id<>(Identifier.of("cranks_dungeons", "damage_numbers"));

    public static final PacketCodec<RegistryByteBuf, DamageNumberPayload> CODEC =
            PacketCodec.of(DamageNumberPayload::write, DamageNumberPayload::read);

    public record DamageEntry(double damage, int color) {}

    private void write(RegistryByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeVarInt(entries.size());
        for (DamageEntry entry : entries) {
            buf.writeDouble(entry.damage());
            buf.writeInt(entry.color());
        }
    }

    private static DamageNumberPayload read(RegistryByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int count = buf.readVarInt();
        List<DamageEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entries.add(new DamageEntry(buf.readDouble(), buf.readInt()));
        }
        return new DamageNumberPayload(x, y, z, entries);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}