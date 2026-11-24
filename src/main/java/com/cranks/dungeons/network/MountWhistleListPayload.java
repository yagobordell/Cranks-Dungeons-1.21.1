package com.cranks.dungeons.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.*;

public record MountWhistleListPayload(List<Entry> mounts) implements CustomPayload {
    public record Entry(UUID uuid,int entityId,String name,String kind,float distance,String order){}
    public static final Id<MountWhistleListPayload> ID=new Id<>(Identifier.of("cranks-dungeons","mount_whistle_list"));
    public static final PacketCodec<RegistryByteBuf,MountWhistleListPayload> CODEC=PacketCodec.of(MountWhistleListPayload::write,MountWhistleListPayload::read);
    private void write(RegistryByteBuf b){
        b.writeVarInt(mounts.size());for(var m:mounts){b.writeUuid(m.uuid);b.writeVarInt(m.entityId);b.writeString(m.name,64);b.writeString(m.kind,24);b.writeFloat(m.distance);b.writeString(m.order,16);}
    }
    private static MountWhistleListPayload read(RegistryByteBuf b){
        int count=b.readVarInt();if(count<0||count>64)throw new IllegalArgumentException("Invalid nearby mount count");
        var mounts=new ArrayList<Entry>(count);for(int i=0;i<count;i++)mounts.add(new Entry(b.readUuid(),b.readVarInt(),b.readString(64),b.readString(24),b.readFloat(),b.readString(16)));
        return new MountWhistleListPayload(List.copyOf(mounts));
    }
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
