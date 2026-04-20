package com.cranks.dungeons.stat.passive;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class PassiveNode {
    private final String id;
    private final String name;
    private final String description;
    private double gridX;
    private double gridY;
    private final List<String> connections;
    private final Map<RegistryEntry<EntityAttribute>, Double> modifiers;
    private final Identifier icon;
    private final String shape;

    public PassiveNode(String id, String name, String description, double gridX, double gridY,
                       List<String> connections, Map<RegistryEntry<EntityAttribute>, Double> modifiers,
                       Identifier icon, String shape) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.gridX = gridX;
        this.gridY = gridY;
        this.connections = connections;
        this.modifiers = modifiers;
        this.icon = icon;
        this.shape = shape;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public double gridX() { return gridX; }
    public double gridY() { return gridY; }
    public List<String> connections() { return connections; }
    public Map<RegistryEntry<EntityAttribute>, Double> modifiers() { return modifiers; }
    public Identifier icon() { return icon; }
    public String shape() { return shape; }

    public void setGridX(double gridX) { this.gridX = gridX; }
    public void setGridY(double gridY) { this.gridY = gridY; }
}
