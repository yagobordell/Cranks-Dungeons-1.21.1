package com.cranks.dungeons.network;
import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.screen.LoadoutStorageHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.*;
public record StorageSortPayload(int syncId,int request,List<ItemStack> expected) implements CustomPayload {
    public static final Id<StorageSortPayload> ID=new Id<>(Identifier.of(CranksDungeons.MOD_ID,"sort_storage"));
    public static final PacketCodec<RegistryByteBuf,StorageSortPayload> CODEC=new PacketCodec<>(){
        @Override public StorageSortPayload decode(RegistryByteBuf buf){int sync=buf.readVarInt(),request=buf.readVarInt(),size=buf.readVarInt();checkSize(size);var stacks=new ArrayList<ItemStack>(size);for(int i=0;i<size;i++)stacks.add(ItemStack.OPTIONAL_PACKET_CODEC.decode(buf));return new StorageSortPayload(sync,request,List.copyOf(stacks));}
        @Override public void encode(RegistryByteBuf buf,StorageSortPayload value){checkSize(value.expected.size());buf.writeVarInt(value.syncId);buf.writeVarInt(value.request);buf.writeVarInt(value.expected.size());for(var stack:value.expected)ItemStack.OPTIONAL_PACKET_CODEC.encode(buf,stack);}
    };
    private static void checkSize(int size){if(size!=5&&size!=9&&size!=27&&size!=54)throw new IllegalArgumentException("Invalid storage snapshot size");}
    @Override public Id<StorageSortPayload> getId(){return ID;}
    public int perform(ServerPlayerEntity player){return player.currentScreenHandler instanceof LoadoutStorageHandler storage&&storage.syncId==syncId?storage.sort(player,expected):CraftMaterialsResultPayload.UNAVAILABLE;}
    void handle(ServerPlayerEntity player){ServerPlayNetworking.send(player,new CraftMaterialsResultPayload(request,perform(player)));}
}
