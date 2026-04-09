package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.party.PartyMemberData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PartySyncPacket(
    List<PartyMemberData> members,
    UUID leaderUuid,
    boolean joined
) implements CustomPayload {
    
    public static final CustomPayload.Id<PartySyncPacket> ID = 
        new CustomPayload.Id<>(Identifier.of(CranksDungeons.MOD_ID, "party_sync"));
    
    public static final PacketCodec<RegistryByteBuf, PartySyncPacket> CODEC = 
        PacketCodec.of(PartySyncPacket::write, PartySyncPacket::read);
    
    private void write(RegistryByteBuf buf) {
        buf.writeBoolean(leaderUuid != null);
        if (leaderUuid != null) {
            buf.writeUuid(leaderUuid);
        }
        buf.writeBoolean(joined);
        buf.writeVarInt(members.size());
        for (PartyMemberData member : members) {
            buf.writeUuid(member.uuid());
            buf.writeString(member.name());
            buf.writeFloat(member.health());
            buf.writeFloat(member.maxHealth());
            buf.writeFloat(member.absorption());
            buf.writeFloat(member.energyShield());
            buf.writeFloat(member.maxEnergyShield());
        }
    }
    
    private static PartySyncPacket read(RegistryByteBuf buf) {
        UUID leader = buf.readBoolean() ? buf.readUuid() : null;
        boolean joined = buf.readBoolean();
        int count = buf.readVarInt();
        List<PartyMemberData> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID uuid = buf.readUuid();
            String name = buf.readString();
            float health = buf.readFloat();
            float maxHealth = buf.readFloat();
            float absorption = buf.readFloat();
            float energyShield = buf.readFloat();
            float maxEnergyShield = buf.readFloat();
            members.add(new PartyMemberData(uuid, name, health, maxHealth, absorption, energyShield, maxEnergyShield));
        }
        return new PartySyncPacket(members, leader, joined);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
