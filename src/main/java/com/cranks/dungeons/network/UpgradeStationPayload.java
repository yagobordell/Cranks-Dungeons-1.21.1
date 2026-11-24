package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record UpgradeStationPayload(int syncId, int request, int amount, int jobId, ItemStack equipment, ItemStack material)
        implements CustomPayload {
    public static final Id<UpgradeStationPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "upgrade_station"));
    public static final PacketCodec<RegistryByteBuf, UpgradeStationPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, UpgradeStationPayload::syncId,
            PacketCodecs.VAR_INT, UpgradeStationPayload::request,
            PacketCodecs.VAR_INT, UpgradeStationPayload::amount,
            PacketCodecs.VAR_INT, UpgradeStationPayload::jobId,
            ItemStack.OPTIONAL_PACKET_CODEC, UpgradeStationPayload::equipment,
            ItemStack.OPTIONAL_PACKET_CODEC, UpgradeStationPayload::material,
            UpgradeStationPayload::new);

    @Override
    public Id<UpgradeStationPayload> getId() {
        return ID;
    }

    void handle(ServerPlayerEntity player) {
        int result = CraftMaterialsResultPayload.UNAVAILABLE;
        if (player.currentScreenHandler instanceof QualityStationScreenHandler bench && bench.syncId == syncId
                && player.isAlive() && !player.isSpectator() && bench.canUse(player)) {
            if (amount == 0 && bench.processing() && !bench.canCancel()) {
                result = CraftMaterialsResultPayload.LOCKED;
            } else {
                result = bench.updateJob(amount, jobId, equipment, material)
                        ? Math.max(1, amount) : CraftMaterialsResultPayload.INVENTORY_CHANGED;
            }
        }
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, result));
    }
}
