package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record PartyInvitePacket(
    UUID inviterUuid,
    String inviterName,
    boolean accepted
) implements CustomPayload {
    
    public static final CustomPayload.Id<PartyInvitePacket> ID = 
        new CustomPayload.Id<>(Identifier.of(CranksDungeons.MOD_ID, "party_invite"));
    
    public static final PacketCodec<RegistryByteBuf, PartyInvitePacket> CODEC = 
        PacketCodec.of(PartyInvitePacket::write, PartyInvitePacket::read);
    
    private void write(RegistryByteBuf buf) {
        buf.writeUuid(inviterUuid);
        buf.writeString(inviterName);
        buf.writeBoolean(accepted);
    }
    
    private static PartyInvitePacket read(RegistryByteBuf buf) {
        UUID uuid = buf.readUuid();
        String name = buf.readString();
        boolean accepted = buf.readBoolean();
        return new PartyInvitePacket(uuid, name, accepted);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
