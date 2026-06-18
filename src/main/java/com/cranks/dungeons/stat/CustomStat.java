package com.cranks.dungeons.stat;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.util.RandomUtil;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.List;

public class CustomStat {
    public enum AffixType {
        PREFIX,
        SUFFIX
    }

    public static class StatComponent {
        private final String displayName;
        private final List<RegistryEntry<EntityAttribute>> attributes;
        private final double[] normalRanges;
        private final double[] uncommonRanges;
        private final boolean isPercentage;
        private final boolean displayAsPercentage;

        public StatComponent(String displayName, List<RegistryEntry<EntityAttribute>> attributes,
                             double[] normalRanges, double[] uncommonRanges,
                             boolean isPercentage, boolean displayAsPercentage) {
            this.displayName = displayName;
            this.attributes = attributes;
            this.normalRanges = normalRanges;
            this.uncommonRanges = uncommonRanges;
            this.isPercentage = isPercentage;
            this.displayAsPercentage = displayAsPercentage;
        }

        public StatComponent(String displayName, RegistryEntry<EntityAttribute> attribute,
                             double[] normalRanges, double[] uncommonRanges,
                             boolean isPercentage, boolean displayAsPercentage) {
            this(displayName, List.of(attribute), normalRanges, uncommonRanges, isPercentage, displayAsPercentage);
        }

        public List<RegistryEntry<EntityAttribute>> getAttributes() { return attributes; }
        public boolean isPercentage() { return isPercentage; }
        public String getDisplayName() { return displayName; }

        public double rollValue(int tier) {
            if (tier < 1 || tier > 10) throw new IllegalArgumentException("Tier must be between 1 and 10");
            int index = 10 - tier;
            if (index >= normalRanges.length) index = normalRanges.length - 1;
            double min = normalRanges[index];
            double max = uncommonRanges[index];
            double value = min + (max - min) * RandomUtil.nextDouble(1.0);
            if (isPercentage || displayAsPercentage) return Math.round(value * 1000.0) / 1000.0;
            return Math.round(value);
        }

        public String getFormattedRange(int minTier, int maxTier) {
            DecimalFormat df = new DecimalFormat("#.##");
            int minIndex = 10 - minTier;
            int maxIndex = 10 - maxTier;
            double minVal = normalRanges[minIndex];
            double maxVal = uncommonRanges[maxIndex];

            if (displayAsPercentage || isPercentage) {
                String range = "(" + df.format(minVal * 100) + " - " + df.format(maxVal * 100) + ")%";
                if (displayName.contains("Extra")) return "Gain " + range + " of " + displayName;
                return range + " " + displayName;
            } else {
                String range = "(" + Math.round(minVal) + " - " + Math.round(maxVal) + ")";
                if (displayName.endsWith("Damage") && !displayName.contains("Thorns")) {
                    return "Adds " + range + " " + displayName + " to Attacks";
                }
                return "+" + range + " to " + displayName;
            }
        }

        public String formatValue(double value) {
            DecimalFormat df = new DecimalFormat("#.##");
            if (displayAsPercentage || isPercentage) {
                String formattedValue = df.format(value * 100) + "%";
                if (displayName.contains("Extra")) return "Gain " + formattedValue + " of " + displayName;
                return formattedValue + " " + displayName;
            } else {
                if (displayName.endsWith("Damage") && !displayName.contains("Thorns")) {
                    return "Adds " + Math.round(value) + " " + displayName + " to Attacks";
                }
                return "+" + Math.round(value) + " to " + displayName;
            }
        }
    }

    private final String id;
    private final String groupId;
    private final EquipmentType equipmentType;
    private final List<StatComponent> components;
    private final Formatting color;
    private final AffixType affixType;
    private final int level;
    private final int weight;
    private com.cranks.dungeons.equipment.ArmorBaseType armorBaseType = com.cranks.dungeons.equipment.ArmorBaseType.ANY;

    // Legacy constructor wrapper for single attribute stats
    public CustomStat(String id, String displayName, EquipmentType equipmentType,
                      RegistryEntry<EntityAttribute> attribute,
                      double[] normalRanges, double[] uncommonRanges,
                      boolean isPercentage, boolean displayAsPercentage,
                      Formatting color, AffixType affixType, int level, int weight) {
        this.id = id;
        this.groupId = id; // Default to id if not specified
        this.equipmentType = equipmentType;
        this.components = List.of(new StatComponent(displayName, attribute, normalRanges, uncommonRanges, isPercentage, displayAsPercentage));
        this.color = color;
        this.affixType = affixType;
        this.level = level;
        this.weight = weight;
    }

    public CustomStat(String id, String groupId, String displayName, EquipmentType equipmentType,
                      RegistryEntry<EntityAttribute> attribute,
                      double[] normalRanges, double[] uncommonRanges,
                      boolean isPercentage, boolean displayAsPercentage,
                      Formatting color, AffixType affixType, int level, int weight) {
        this.id = id;
        this.groupId = groupId;
        this.equipmentType = equipmentType;
        this.components = List.of(new StatComponent(displayName, attribute, normalRanges, uncommonRanges, isPercentage, displayAsPercentage));
        this.color = color;
        this.affixType = affixType;
        this.level = level;
        this.weight = weight;
    }

    public CustomStat(String id, String displayName, EquipmentType equipmentType,
                      RegistryEntry<EntityAttribute> attribute,
                      double[] normalRanges, double[] uncommonRanges,
                      boolean isPercentage, boolean displayAsPercentage,
                      Formatting color, AffixType affixType) {
        this(id, displayName, equipmentType, attribute, normalRanges, uncommonRanges,
             isPercentage, displayAsPercentage, color, affixType, 1, 1000);
    }

    // New constructor for hybrid stats
    public CustomStat(String id, String groupId, EquipmentType equipmentType, List<StatComponent> components,
                      Formatting color, AffixType affixType, int level, int weight) {
        this.id = id;
        this.groupId = groupId;
        this.equipmentType = equipmentType;
        this.components = components;
        this.color = color;
        this.affixType = affixType;
        this.level = level;
        this.weight = weight;
    }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public com.cranks.dungeons.equipment.ArmorBaseType getArmorBaseType() { return armorBaseType; }
    public CustomStat setArmorBaseType(com.cranks.dungeons.equipment.ArmorBaseType type) { this.armorBaseType = type; return this; }
    public EquipmentType getEquipmentType() { return equipmentType; }
    public AffixType getAffixType() { return affixType; }
    public Formatting getColor() { return color; }
    public int getLevel() { return level; }
    public int getWeight() { return weight; }
    
    public List<StatComponent> getComponents() { return components; }

    // Backward compatibility for methods accessing the primary (0th) component
    public String getDisplayName() { return components.get(0).getDisplayName(); }
    public List<RegistryEntry<EntityAttribute>> getAttributes() { return components.get(0).getAttributes(); }
    
    public double[] rollValues(int tier) {
        double[] values = new double[components.size()];
        for (int i = 0; i < components.size(); i++) {
            values[i] = components.get(i).rollValue(tier);
        }
        return values;
    }

    // Legacy support to return primary rolled value
    public double rollValue(int tier) {
        return rollValues(tier)[0];
    }
}