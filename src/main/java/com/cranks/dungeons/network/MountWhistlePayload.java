package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

/** -1 requests nearby mounts; other values are the stable MountOrder.Mode ordinals. */
public record MountWhistlePayload(UUID mount,int action) implements CustomPayload {
    public static final Id<MountWhistlePayload> ID=new Id<>(Identifier.of("cranks-dungeons","mount_whistle"));
    public static final PacketCodec<RegistryByteBuf,MountWhistlePayload> CODEC=PacketCodec.of(
            (p,b)->{b.writeUuid(p.mount);b.writeVarInt(p.action);},b->new MountWhistlePayload(b.readUuid(),b.readVarInt()));
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
