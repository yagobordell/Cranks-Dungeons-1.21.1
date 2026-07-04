package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SyncWorldTreePayload(
        List<PassiveNode> nodes
) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "sync_world_tree");
    public static final CustomPayload.Id<SyncWorldTreePayload> ID = new CustomPayload.Id<>(ID_IDENT);

    private static final PacketCodec<RegistryByteBuf, Map<RegistryEntry<EntityAttribute>, Double>> MODIFIERS_CODEC =
            PacketCodecs.map(
                    java.util.LinkedHashMap::new,
                    PacketCodecs.registryEntry(RegistryKeys.ATTRIBUTE),
                    PacketCodecs.DOUBLE
            );

    private static final PacketCodec<RegistryByteBuf, PassiveNode> NODE_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, PassiveNode::id,
            PacketCodecs.STRING, PassiveNode::name,
            PacketCodecs.STRING, PassiveNode::description,
            PacketCodecs.DOUBLE, PassiveNode::gridX,
            PacketCodecs.DOUBLE, PassiveNode::gridY,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), PassiveNode::connections,
            MODIFIERS_CODEC, PassiveNode::modifiers,
            PacketCodecs.optional(Identifier.PACKET_CODEC).xmap(opt -> opt.orElse(null), Optional::ofNullable), PassiveNode::icon,
            PacketCodecs.STRING, PassiveNode::shape,
            PacketCodecs.BOOLEAN, PassiveNode::isAttributeSelection,
            PacketCodecs.DOUBLE, PassiveNode::attributeSelectionAmount,
            PassiveNode::new
    );

    public static final PacketCodec<RegistryByteBuf, SyncWorldTreePayload> CODEC = PacketCodec.tuple(
            NODE_CODEC.collect(PacketCodecs.toList()), SyncWorldTreePayload::nodes,
            SyncWorldTreePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
