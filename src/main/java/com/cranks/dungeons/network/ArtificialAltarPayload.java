package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record ArtificialAltarPayload(int syncId, int request, int revision, ItemStack equipment, int option, boolean confirmed) implements CustomPayload {
    public static final Id<ArtificialAltarPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "artificial_altar"));
    public static final PacketCodec<RegistryByteBuf, ArtificialAltarPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ArtificialAltarPayload::syncId, PacketCodecs.VAR_INT, ArtificialAltarPayload::request,
            PacketCodecs.VAR_INT, ArtificialAltarPayload::revision, ItemStack.OPTIONAL_PACKET_CODEC, ArtificialAltarPayload::equipment,
            PacketCodecs.VAR_INT, ArtificialAltarPayload::option, PacketCodecs.BOOL, ArtificialAltarPayload::confirmed, ArtificialAltarPayload::new);
    @Override public Id<ArtificialAltarPayload> getId() { return ID; }
    public int perform(ServerPlayerEntity player) { return player.currentScreenHandler instanceof ArtificialEnhancementAltarScreenHandler altar && altar.syncId == syncId
            ? altar.apply(player, revision, equipment, option, confirmed) : CraftMaterialsResultPayload.UNAVAILABLE; }
    void handle(ServerPlayerEntity player) { ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, perform(player))); }
}
