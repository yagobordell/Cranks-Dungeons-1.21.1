package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CraftMaterialsResultPayload(int request, int result) implements CustomPayload {
    public static final int UNAVAILABLE = 0;
    public static final int LOCKED = -1;
    public static final int INVENTORY_CHANGED = -2;
    public static final Id<CraftMaterialsResultPayload> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "craft_materials_result"));
    public static final PacketCodec<RegistryByteBuf, CraftMaterialsResultPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, CraftMaterialsResultPayload::request,
            PacketCodecs.VAR_INT, CraftMaterialsResultPayload::result,
            CraftMaterialsResultPayload::new);

    @Override
    public Id<CraftMaterialsResultPayload> getId() {
        return ID;
    }
}
