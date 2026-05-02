package com.cranks.dungeons.stat;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.util.RandomUtil;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;

public class CustomStat {
    public enum AffixType {
        PREFIX,
        SUFFIX
    }

    private final String id;
    private final String displayName;
    private final EquipmentType equipmentType;
    private final RegistryEntry<EntityAttribute> attribute;
    private final double[] normalRanges;
    private final double[] uncommonRanges;
    private final boolean isPercentage;
    private final boolean displayAsPercentage;
    private final Formatting color;
    private final AffixType affixType;
    private final int level;
    private final int weight;

    // Constructor with all parameters
    public CustomStat(String id, String displayName, EquipmentType equipmentType,
                      RegistryEntry<EntityAttribute> attribute,
                      double[] normalRanges, double[] uncommonRanges,
                      boolean isPercentage, boolean displayAsPercentage,
                      Formatting color, AffixType affixType, int level, int weight) {
        this.id = id;
        this.displayName = displayName;
        this.equipmentType = equipmentType;
        this.attribute = attribute;
        this.normalRanges = normalRanges;
        this.uncommonRanges = uncommonRanges;
        this.isPercentage = isPercentage;
        this.displayAsPercentage = displayAsPercentage;
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

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public RegistryEntry<EntityAttribute> getAttribute() {
        return attribute;
    }

    public AffixType getAffixType() {
        return affixType;
    }

    public double rollValue(int tier) {
        // PoE Tiers are 1 to 10, where 1 is best.
        // Array index should be 10 - tier (0 to 9)
        if (tier < 1 || tier > 10) {
            throw new IllegalArgumentException("Tier must be between 1 and 10");
        }

        int index = 10 - tier;
        // Safety check if ranges array is shorter
        if (index >= normalRanges.length) index = normalRanges.length - 1;

        double min = normalRanges[index];
        double max = uncommonRanges[index];

        double value = min + (max - min) * RandomUtil.nextDouble(1.0);

        return Math.round(value * 1000.0) / 1000.0;
    }

    public String getFormattedRange(int minTier, int maxTier) {
        DecimalFormat df = new DecimalFormat("#.##");
        // minTier is higher number (e.g. 3), maxTier is lower number (e.g. 1)
        int minIndex = 10 - minTier;
        int maxIndex = 10 - maxTier;
        
        double minVal = normalRanges[minIndex];
        double maxVal = uncommonRanges[maxIndex]; // Correctly show full potential range

        if (displayAsPercentage || isPercentage) {
            return "+(" + df.format(minVal * 100) + " - " + df.format(maxVal * 100) + ")% " + displayName;
        } else {
            return "+(" + df.format(minVal) + " - " + df.format(maxVal) + ") " + displayName;
        }
    }

    public String formatValue(double value) {
        DecimalFormat df = new DecimalFormat("#.#");

        if (displayAsPercentage) {
            String formattedValue = df.format(value * 100) + "%";
            if (displayName.contains("Extra")) {
                return "Gain " + formattedValue + " of " + displayName;
            }
            return formattedValue + " " + displayName;
        } else if (isPercentage) {
            return df.format(value * 100) + " " + displayName;
        } else {
            // Whole numbers
            if (displayName.endsWith("Damage")) {
                return "Adds " + df.format(value) + " " + displayName + " to Attacks";
            } else {
                return "+" + df.format(value) + " to " + displayName;
            }
        }
    }

    public Formatting getColor() {
        return color;
    }

    public int getLevel() {
        return level;
    }

    public int getWeight() {
        return weight;
    }
}