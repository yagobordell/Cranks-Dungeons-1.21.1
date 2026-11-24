package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

public record MountTransferOfferPayload(int entityId,UUID token,String ownerName,String mountName) implements CustomPayload {
    public static final Id<MountTransferOfferPayload> ID=new Id<>(Identifier.of("cranks-dungeons","mount_transfer_offer"));
    public static final PacketCodec<RegistryByteBuf,MountTransferOfferPayload> CODEC=PacketCodec.of(
            (p,b)->{b.writeVarInt(p.entityId);b.writeUuid(p.token);b.writeString(p.ownerName,64);b.writeString(p.mountName,128);},
            b->new MountTransferOfferPayload(b.readVarInt(),b.readUuid(),b.readString(64),b.readString(128)));
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
