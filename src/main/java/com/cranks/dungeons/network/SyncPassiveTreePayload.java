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
    private static final PacketCodec<RegistryByteBuf, PassiveNode> NODE_CODEC = PacketCodec.of(
        (value, buf) -> {
            PacketCodecs.STRING.encode(buf, value.id());
            PacketCodecs.STRING.encode(buf, value.name());
            PacketCodecs.STRING.encode(buf, value.description());
            buf.writeDouble(value.gridX());
            buf.writeDouble(value.gridY());
            PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, value.connections());
            MODIFIERS_CODEC.encode(buf, value.modifiers());
            PacketCodecs.optional(Identifier.PACKET_CODEC).xmap(opt -> opt.orElse(null), Optional::ofNullable).encode(buf, value.icon());
            PacketCodecs.optional(PacketCodecs.STRING).xmap(opt -> opt.orElse(null), Optional::ofNullable).encode(buf, value.shape());
            buf.writeBoolean(value.isAttributeSelection());
            buf.writeDouble(value.attributeSelectionAmount());
        },
        buf -> {
            return new PassiveNode(
                PacketCodecs.STRING.decode(buf),
                PacketCodecs.STRING.decode(buf),
                PacketCodecs.STRING.decode(buf),
                buf.readDouble(),
                buf.readDouble(),
                PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf),
                MODIFIERS_CODEC.decode(buf),
                PacketCodecs.optional(Identifier.PACKET_CODEC).xmap(opt -> opt.orElse(null), Optional::ofNullable).decode(buf),
                PacketCodecs.optional(PacketCodecs.STRING).xmap(opt -> opt.orElse(null), Optional::ofNullable).decode(buf),
                buf.readBoolean(),
                buf.readDouble()
            );
        }
    );

    // Codec for ClassDefinition
    private static final PacketCodec<RegistryByteBuf, PassiveTreeConfig.ClassDefinition> CLASS_CODEC = PacketCodec.of(
        (value, buf) -> {
            PacketCodecs.STRING.encode(buf, value.id());
            PacketCodecs.STRING.encode(buf, value.name());
            PacketCodecs.STRING.encode(buf, value.subtitle());
            PacketCodecs.STRING.encode(buf, value.description());
            PacketCodecs.STRING.encode(buf, value.recommendedStats());
            PacketCodecs.STRING.encode(buf, value.color());
            PacketCodecs.STRING.encode(buf, value.startNode());
            
            int attrsSize = value.baseAttributes() != null ? value.baseAttributes().size() : 0;
            buf.writeInt(attrsSize);
            if (attrsSize > 0) {
                for (java.util.Map.Entry<String, Double> entry : value.baseAttributes().entrySet()) {
                    PacketCodecs.STRING.encode(buf, entry.getKey());
                    buf.writeDouble(entry.getValue());
                }
            }
        },
        buf -> {
            String id = PacketCodecs.STRING.decode(buf);
            String name = PacketCodecs.STRING.decode(buf);
            String subtitle = PacketCodecs.STRING.decode(buf);
            String description = PacketCodecs.STRING.decode(buf);
            String recStats = PacketCodecs.STRING.decode(buf);
            String color = PacketCodecs.STRING.decode(buf);
            String startNode = PacketCodecs.STRING.decode(buf);
            
            int attrsSize = buf.readInt();
            java.util.Map<String, Double> baseAttributes = new java.util.HashMap<>();
            for (int i = 0; i < attrsSize; i++) {
                baseAttributes.put(PacketCodecs.STRING.decode(buf), buf.readDouble());
            }

            return new com.cranks.dungeons.config.PassiveTreeConfig.ClassDefinition(
                id, name, subtitle, description, recStats, color, startNode, baseAttributes
            );
        }
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
