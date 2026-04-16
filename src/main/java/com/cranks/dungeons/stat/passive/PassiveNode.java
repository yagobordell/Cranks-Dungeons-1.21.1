package com.cranks.dungeons.stat.passive;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public record PassiveNode(
        String id,
        String name,
        String description,
        double gridX,
        double gridY,
        List<String> connections,
        Map<RegistryEntry<EntityAttribute>, Double> modifiers,
        Identifier icon,
        String shape  // "square", "circle", "diamond", "hexagon"
) {}
