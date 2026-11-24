package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record RunicAltarPayload(int syncId, int request, int revision, ItemStack equipment, ItemStack tome, boolean confirmed,int rune,int action) implements CustomPayload {
    public RunicAltarPayload(int syncId,int request,int revision,ItemStack equipment,ItemStack tome,boolean confirmed){this(syncId,request,revision,equipment,tome,confirmed,-1,0);}
    public static final Id<RunicAltarPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "runic_altar"));
    public static final PacketCodec<RegistryByteBuf, RunicAltarPayload> CODEC = PacketCodec.of((value,buf)->{
        buf.writeVarInt(value.syncId);buf.writeVarInt(value.request);buf.writeVarInt(value.revision);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf,value.equipment);ItemStack.OPTIONAL_PACKET_CODEC.encode(buf,value.tome);
        buf.writeBoolean(value.confirmed);buf.writeVarInt(value.rune);buf.writeVarInt(value.action);
    },buf->new RunicAltarPayload(buf.readVarInt(),buf.readVarInt(),buf.readVarInt(),ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
            ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),buf.readBoolean(),buf.readVarInt(),buf.readVarInt()));
    @Override public Id<RunicAltarPayload> getId() { return ID; }
    void handle(ServerPlayerEntity player) {
        int result = player.currentScreenHandler instanceof RunicEnhancementAltarScreenHandler altar && altar.syncId == syncId
                ? altar.apply(player, revision, equipment, tome, confirmed,rune,action) : CraftMaterialsResultPayload.UNAVAILABLE;
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, result));
    }
}
