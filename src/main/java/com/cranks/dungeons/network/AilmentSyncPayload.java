package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record AilmentSyncPayload(int entityId, List<AilmentEntry> ailments) implements CustomPayload {
    public static final Id<AilmentSyncPayload> ID = new Id<>(Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "ailment_sync"));

    public static final PacketCodec<RegistryByteBuf, AilmentSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, AilmentSyncPayload::entityId,
            AilmentEntry.CODEC.collect(PacketCodecs.toList()), AilmentSyncPayload::ailments,
            AilmentSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record AilmentEntry(Identifier texture, int stacks, boolean buildup, float magnitude,
                               float damagePerSecond, float storedDamage, float remainingSeconds) {
        public AilmentEntry(Identifier texture, int stacks) {
            this(texture,stacks,texture.getPath().contains("buildup"),0,0,0,0);
        }
        public static final PacketCodec<RegistryByteBuf, AilmentEntry> CODEC = new PacketCodec<>() {
            public AilmentEntry decode(RegistryByteBuf buf) {
                return new AilmentEntry(buf.readIdentifier(),buf.readVarInt(),buf.readBoolean(),
                        buf.readFloat(),buf.readFloat(),buf.readFloat(),buf.readFloat());
            }
            public void encode(RegistryByteBuf buf, AilmentEntry value) {
                buf.writeIdentifier(value.texture());buf.writeVarInt(value.stacks());buf.writeBoolean(value.buildup());
                buf.writeFloat(value.magnitude());buf.writeFloat(value.damagePerSecond());
                buf.writeFloat(value.storedDamage());buf.writeFloat(value.remainingSeconds());
            }
        };
    }
}
