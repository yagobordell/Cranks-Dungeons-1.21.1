package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.LinkedHashMap;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public record BulkUnlockWorldNodesPayload(Map<String, String> nodeChoices) implements CustomPayload {
    public static final int MAX_ENTRIES = 512;
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "bulk_unlock_world_nodes");
    public static final CustomPayload.Id<BulkUnlockWorldNodesPayload> ID = new CustomPayload.Id<>(ID_IDENT);

    public static final PacketCodec<RegistryByteBuf, BulkUnlockWorldNodesPayload> CODEC =
            PacketCodec.of(BulkUnlockWorldNodesPayload::write, BulkUnlockWorldNodesPayload::read);

    private void write(RegistryByteBuf buf) {
        if (nodeChoices == null || nodeChoices.size() > MAX_ENTRIES) {
            throw new EncoderException("World bulk unlock contains too many entries");
        }
        buf.writeVarInt(nodeChoices.size());
        for (Map.Entry<String, String> entry : nodeChoices.entrySet()) {
            buf.writeString(entry.getKey(), 128);
            buf.writeString(entry.getValue(), 128);
        }
    }

    private static BulkUnlockWorldNodesPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) {
            throw new DecoderException("World bulk unlock entry count " + size + " exceeds " + MAX_ENTRIES);
        }
        Map<String, String> choices = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            choices.put(buf.readString(128), buf.readString(128));
        }
        return new BulkUnlockWorldNodesPayload(choices);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
