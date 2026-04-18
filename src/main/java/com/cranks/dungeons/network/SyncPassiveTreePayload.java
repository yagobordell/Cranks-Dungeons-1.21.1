package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.config.PassiveTreeConfig;
import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SyncPassiveTreePayload(
        List<PassiveNode> nodes,
        List<PassiveTreeConfig.ClassDefinition> classes
) implements CustomPayload {
    public static final Identifier ID_IDENT = Identifier.of(CranksDungeons.MOD_ID, "sync_passive_tree");
    public static final CustomPayload.Id<SyncPassiveTreePayload> ID = new CustomPayload.Id<>(ID_IDENT);

    // Codec for PassiveNode modifiers map
    private static final PacketCodec<RegistryByteBuf, Map<RegistryEntry<EntityAttribute>, Double>> MODIFIERS_CODEC =
            PacketCodecs.map(
                    java.util.LinkedHashMap::new,
                    PacketCodecs.registryEntry(RegistryKeys.ATTRIBUTE),
                    PacketCodecs.DOUBLE
            );

    // Codec for PassiveNode
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
            PassiveNode::new
    );

    // Codec for ClassDefinition
    private static final PacketCodec<RegistryByteBuf, PassiveTreeConfig.ClassDefinition> CLASS_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, PassiveTreeConfig.ClassDefinition::id,
            PacketCodecs.STRING, PassiveTreeConfig.ClassDefinition::name,
            PacketCodecs.STRING, PassiveTreeConfig.ClassDefinition::description,
            PacketCodecs.STRING, PassiveTreeConfig.ClassDefinition::startNode,
            PassiveTreeConfig.ClassDefinition::new
    );

    public static final PacketCodec<RegistryByteBuf, SyncPassiveTreePayload> CODEC = PacketCodec.tuple(
            NODE_CODEC.collect(PacketCodecs.toList()), SyncPassiveTreePayload::nodes,
            CLASS_CODEC.collect(PacketCodecs.toList()), SyncPassiveTreePayload::classes,
            SyncPassiveTreePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
