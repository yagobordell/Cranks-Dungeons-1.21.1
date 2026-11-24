package com.cranks.dungeons.network;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.*;
public record TradeStatePayload(int phase,UUID session,UUID partner,String name,int revision,long yourCoins,long theirCoins,
        boolean yourReady,boolean theirReady,boolean yourConfirmed,boolean theirConfirmed,List<ItemStack> yours,List<ItemStack> theirs,String message) implements CustomPayload {
    public static final int IDLE=0,OUTGOING=1,INCOMING=2,ACTIVE=3;
    public static final Id<TradeStatePayload> ID=new Id<>(Identifier.of("cranks-dungeons","trade_state"));
    public static final PacketCodec<RegistryByteBuf,TradeStatePayload> CODEC=new PacketCodec<>(){
        private List<ItemStack> read(RegistryByteBuf b){var items=new ArrayList<ItemStack>();for(int i=0;i<9;i++)items.add(ItemStack.OPTIONAL_PACKET_CODEC.decode(b));return items;}
        private void write(RegistryByteBuf b,List<ItemStack> items){for(int i=0;i<9;i++)ItemStack.OPTIONAL_PACKET_CODEC.encode(b,i<items.size()?items.get(i):ItemStack.EMPTY);}
        @Override public TradeStatePayload decode(RegistryByteBuf b){return new TradeStatePayload(b.readVarInt(),b.readUuid(),b.readUuid(),b.readString(64),b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean(),read(b),read(b),b.readString(200));}
        @Override public void encode(RegistryByteBuf b,TradeStatePayload p){b.writeVarInt(p.phase);b.writeUuid(p.session);b.writeUuid(p.partner);b.writeString(p.name,64);b.writeVarInt(p.revision);b.writeVarLong(p.yourCoins);b.writeVarLong(p.theirCoins);b.writeBoolean(p.yourReady);b.writeBoolean(p.theirReady);b.writeBoolean(p.yourConfirmed);b.writeBoolean(p.theirConfirmed);write(b,p.yours);write(b,p.theirs);b.writeString(p.message,200);}
    };
    @Override public Id<TradeStatePayload> getId(){return ID;}
    public static TradeStatePayload idle(String message){return new TradeStatePayload(0,TradeActionPayload.NONE,TradeActionPayload.NONE,"",0,0,0,false,false,false,false,List.of(),List.of(),message);}
}
