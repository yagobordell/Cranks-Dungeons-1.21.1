package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DamageNumberPayload(
        double x, double y, double z,
        List<DamageEntry> entries,
        boolean forceDisplay,
        UUID attackerUuid,
        UUID victimUuid
) implements CustomPayload {

    public static final Id<DamageNumberPayload> ID =
            new Id<>(Identifier.of("cranks_dungeons", "damage_numbers"));

    public static final PacketCodec<RegistryByteBuf, DamageNumberPayload> CODEC =
            PacketCodec.of(DamageNumberPayload::write, DamageNumberPayload::read);

    public record DamageEntry(double damage, int color, boolean isHeal, int elementId, boolean isCrit) {}

    private void write(RegistryByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(forceDisplay);
        
        buf.writeBoolean(attackerUuid != null);
        if (attackerUuid != null) buf.writeUuid(attackerUuid);
        
        buf.writeBoolean(victimUuid != null);
        if (victimUuid != null) buf.writeUuid(victimUuid);

        buf.writeVarInt(entries.size());
        for (DamageEntry entry : entries) {
            buf.writeDouble(entry.damage());
            buf.writeInt(entry.color());
            buf.writeBoolean(entry.isHeal());
            buf.writeByte(entry.elementId());
            buf.writeBoolean(entry.isCrit());
        }
    }

    private static DamageNumberPayload read(RegistryByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        boolean forceDisplay = buf.readBoolean();
        
        UUID attackerUuid = buf.readBoolean() ? buf.readUuid() : null;
        UUID victimUuid = buf.readBoolean() ? buf.readUuid() : null;

        int count = buf.readVarInt();
        List<DamageEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entries.add(new DamageEntry(buf.readDouble(), buf.readInt(), buf.readBoolean(), buf.readByte(), buf.readBoolean()));
        }
        return new DamageNumberPayload(x, y, z, entries, forceDisplay, attackerUuid, victimUuid);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
