package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

/** Offer: target is the nearby recipient. Replies: target is the server-issued offer token. */
public record MountTransferPayload(int entityId,UUID target,int action) implements CustomPayload {
    public static final int OFFER=0,ACCEPT=1,DECLINE=2;
    public static final Id<MountTransferPayload> ID=new Id<>(Identifier.of("cranks-dungeons","mount_transfer"));
    public static final PacketCodec<RegistryByteBuf,MountTransferPayload> CODEC=PacketCodec.of(
            (p,b)->{b.writeVarInt(p.entityId);b.writeUuid(p.target);b.writeVarInt(p.action);},
            b->new MountTransferPayload(b.readVarInt(),b.readUuid(),b.readVarInt()));
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
