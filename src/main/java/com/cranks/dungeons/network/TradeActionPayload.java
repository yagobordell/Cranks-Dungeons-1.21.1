package com.cranks.dungeons.network;
import com.cranks.dungeons.economy.TradeManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;
public record TradeActionPayload(int action,UUID target,UUID session,int revision,int slot,int count,long coins) implements CustomPayload {
    public static final int REQUEST=0,ACCEPT=1,CANCEL=2,OFFER=3,REMOVE=4,COINS=5,READY=6,EDIT=7,CONFIRM=8,REQUESTS=9;
    public static final UUID NONE=new UUID(0,0);
    public static final Id<TradeActionPayload> ID=new Id<>(Identifier.of("cranks-dungeons","trade_action"));
    public static final PacketCodec<RegistryByteBuf,TradeActionPayload> CODEC=new PacketCodec<>(){
        @Override public TradeActionPayload decode(RegistryByteBuf b){return new TradeActionPayload(b.readVarInt(),b.readUuid(),b.readUuid(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarLong());}
        @Override public void encode(RegistryByteBuf b,TradeActionPayload p){b.writeVarInt(p.action);b.writeUuid(p.target);b.writeUuid(p.session);b.writeVarInt(p.revision);b.writeVarInt(p.slot);b.writeVarInt(p.count);b.writeVarLong(p.coins);}
    };
    @Override public Id<TradeActionPayload> getId(){return ID;}
    public void handle(net.minecraft.server.network.ServerPlayerEntity player){TradeManager.action(player,this);}
}
