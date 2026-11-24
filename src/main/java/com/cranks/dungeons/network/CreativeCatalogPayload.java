package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.inventory.SupportedContent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Creative item creation is explicit; ordinary inventory clicks keep the normal slot restrictions. */
public record CreativeCatalogPayload(int request,int action,ItemStack item,ItemStack expectedCursor) implements CustomPayload {
    public static final int TAKE_ONE=0,TAKE_STACK=1,GIVE_STACK=2,REDUCE_CURSOR=3,DELETE_CURSOR=4;
    public static final Id<CreativeCatalogPayload> ID=new Id<>(Identifier.of(CranksDungeons.MOD_ID,"creative_catalog"));
    public static final PacketCodec<RegistryByteBuf,CreativeCatalogPayload> CODEC=new PacketCodec<>(){
        @Override public CreativeCatalogPayload decode(RegistryByteBuf buf){return new CreativeCatalogPayload(buf.readVarInt(),buf.readVarInt(),ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),ItemStack.OPTIONAL_PACKET_CODEC.decode(buf));}
        @Override public void encode(RegistryByteBuf buf,CreativeCatalogPayload packet){buf.writeVarInt(packet.request);buf.writeVarInt(packet.action);ItemStack.OPTIONAL_PACKET_CODEC.encode(buf,packet.item);ItemStack.OPTIONAL_PACKET_CODEC.encode(buf,packet.expectedCursor);}
    };
    @Override public Id<CreativeCatalogPayload> getId(){return ID;}
    public int perform(ServerPlayerEntity player){
        if(!player.isCreative()||!player.isAlive()||player.isSpectator()||player.currentScreenHandler!=player.playerScreenHandler
                ||!(action>=TAKE_ONE&&action<=DELETE_CURSOR||action>=10&&action<10+PlayerLoadout.HOTBAR_COUNT))return CraftMaterialsResultPayload.UNAVAILABLE;
        var handler=player.playerScreenHandler;
        var cursor=handler.getCursorStack();
        if(!ItemStack.areEqual(cursor,expectedCursor))return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        if(action==DELETE_CURSOR){handler.setCursorStack(ItemStack.EMPTY);handler.sendContentUpdates();return 1;}
        if(item.isEmpty()||!item.isItemEnabled(player.getWorld().getEnabledFeatures())||!SupportedContent.isSupported(item)||CranksDungeons.shouldRemoveFromCreative(item))return CraftMaterialsResultPayload.UNAVAILABLE;
        if(action>=10){
            player.getInventory().setStack(PlayerLoadout.getHotbarSlot(action-10),item.copyWithCount(item.getMaxCount()));
            player.getInventory().markDirty();handler.sendContentUpdates();return 1;
        }
        if(action==GIVE_STACK){
            var given=item.copyWithCount(item.getMaxCount());int count=given.getCount();
            PlayerLoadout.of(player.getInventory()).insert(given);player.getInventory().markDirty();handler.sendContentUpdates();
            return count-given.getCount();
        }
        if(!cursor.isEmpty()&&!ItemStack.areItemsAndComponentsEqual(cursor,item))return CraftMaterialsResultPayload.LOCKED;
        if(action==REDUCE_CURSOR){if(!cursor.isEmpty())cursor.decrement(1);}
        else if(action==TAKE_STACK)handler.setCursorStack(item.copyWithCount(item.getMaxCount()));
        else if(cursor.isEmpty())handler.setCursorStack(item.copyWithCount(1));
        else cursor.setCount(Math.min(cursor.getMaxCount(),cursor.getCount()+1));
        handler.sendContentUpdates();return 1;
    }
    void handle(ServerPlayerEntity player){
        int result=perform(player);
        if(result<=0)player.currentScreenHandler.syncState();
        ServerPlayNetworking.send(player,new CraftMaterialsResultPayload(request,result));
    }
}
