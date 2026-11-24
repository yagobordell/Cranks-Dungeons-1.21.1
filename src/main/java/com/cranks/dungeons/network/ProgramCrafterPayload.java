package com.cranks.dungeons.network;
import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.screen.LoadoutCrafterHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.List;
public record ProgramCrafterPayload(int syncId,int request,int revision,String recipe,List<ItemStack> expected) implements CustomPayload {
    public static final Id<ProgramCrafterPayload> ID=new Id<>(Identifier.of(CranksDungeons.MOD_ID,"program_crafter"));
    public static final PacketCodec<RegistryByteBuf,ProgramCrafterPayload> CODEC=PacketCodec.tuple(
            PacketCodecs.VAR_INT,ProgramCrafterPayload::syncId,PacketCodecs.VAR_INT,ProgramCrafterPayload::request,
            PacketCodecs.VAR_INT,ProgramCrafterPayload::revision,PacketCodecs.STRING,ProgramCrafterPayload::recipe,
            ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList(9)),ProgramCrafterPayload::expected,ProgramCrafterPayload::new);
    @Override public Id<ProgramCrafterPayload> getId(){return ID;}
    public int perform(ServerPlayerEntity player){
        var id=recipe.isEmpty()?null:Identifier.tryParse(recipe);
        if(!recipe.isEmpty()&&id==null)return CraftMaterialsResultPayload.UNAVAILABLE;
        return player.currentScreenHandler instanceof LoadoutCrafterHandler crafter&&crafter.syncId==syncId?crafter.program(player,revision,id,expected):CraftMaterialsResultPayload.UNAVAILABLE;
    }
    void handle(ServerPlayerEntity player){ServerPlayNetworking.send(player,new CraftMaterialsResultPayload(request,perform(player)));}
}
