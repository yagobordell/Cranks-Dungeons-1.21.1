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
            this(displayName, attribute == null ? List.of() : List.of(attribute), normalRanges, uncommonRanges, isPercentage, displayAsPercentage);
        }

        public List<RegistryEntry<EntityAttribute>> getAttributes() { return attributes; }
        public boolean isPercentage() { return isPercentage; }
        public String getDisplayName() { return displayName; }
        public String getDisplayLabel() {
            return displayName.replace("# of ", "").replace("#%", "").replace("#", "")
                    .replaceFirst("^%\\s*", "").replaceAll("\\s+", " ").strip();
        }

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

        public String getFormattedMinMax(int tier) {
            int index = 10 - tier;
            if (index >= normalRanges.length) index = normalRanges.length - 1;
            double min = normalRanges[index];
            double max = uncommonRanges[index];
            DecimalFormat df = new DecimalFormat("#.##");
            if (isPercentage || displayAsPercentage) {
                String minStr = df.format(min * 100);
                String maxStr = df.format(max * 100);
                if (minStr.equals(maxStr)) {
                    return "[" + minStr + "%]";
                } else if (min < 0 || max < 0) {
                    return "[(" + minStr + "%)-(" + maxStr + "%)]";
                } else {
                    return "[" + minStr + "-" + maxStr + "%]";
                }
            } else {
                long minRounded = Math.round(min);
                long maxRounded = Math.round(max);
                if (minRounded == maxRounded) {
                    return "[" + minRounded + "]";
                } else if (minRounded < 0 || maxRounded < 0) {
                    return "[(" + minRounded + ")-(" + maxRounded + ")]";
                } else {
                    return "[" + minRounded + "-" + maxRounded + "]";
                }
            }
        }

        private boolean isWeapon(EquipmentType type) {
            if (type == null) return false;
            switch (type) {
                case SWORD: case SPEAR: case MACE: case AXE: case PICKAXE: case SHOVEL: case HOE:
                case BOW: case CROSSBOW: case TRIDENT:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isBooleanStat() {
            return displayName.equals("Returns when thrown") || 
                   displayName.equals("Bounces on Hit") ||
                   displayName.equals("Extended Breathing Time") ||
                   displayName.equals("Increases Underwater Mining Speed");
        }

        public String getFormattedRange(int minTier, int maxTier, EquipmentType eqType) {
            if (isBooleanStat()) {
                return displayName;
            }
            DecimalFormat df = new DecimalFormat("#.##");
            double minVal = Double.POSITIVE_INFINITY, maxVal = Double.NEGATIVE_INFINITY;
            for (int tier = Math.min(minTier, maxTier); tier <= Math.max(minTier, maxTier); tier++) {
                if (tier < 1 || tier > 10) throw new IllegalArgumentException("Tier must be between 1 and 10");
                int index = Math.min(10 - tier, normalRanges.length - 1);
                minVal = Math.min(minVal, Math.min(normalRanges[index], uncommonRanges[index]));
                maxVal = Math.max(maxVal, Math.max(normalRanges[index], uncommonRanges[index]));
            }

            if (displayAsPercentage || isPercentage) {
                String minStr = df.format(minVal * 100);
                String maxStr = df.format(maxVal * 100);
                String range;
                if (minStr.equals(maxStr)) {
                    range = "(" + minStr + ")%";
                } else if (minVal < 0 || maxVal < 0) {
                    range = "((" + minStr + "%) - (" + maxStr + "%))";
                } else {
                    range = "(" + minStr + " - " + maxStr + ")%";
                }
                return formatDescription(range, eqType);
            } else {
                long minRounded = Math.round(minVal);
                long maxRounded = Math.round(maxVal);
                String range;
                if (minRounded == maxRounded) {
                    range = "(" + minRounded + ")";
                } else if (minRounded < 0 || maxRounded < 0) {
                    range = "((" + minRounded + ") - (" + maxRounded + "))";
                } else {
                    range = "(" + minRounded + " - " + maxRounded + ")";
                }
                return formatDescription(range, eqType);
            }
        }

        public String formatTierRange(int tier, EquipmentType eqType) {
            return formatDescription(getFormattedMinMax(tier), eqType);
        }

        public String formatValue(double value, EquipmentType eqType) {
            if (isBooleanStat()) {
                return displayName;
            }
            DecimalFormat df = new DecimalFormat("#.##");
            String formattedValue;
            if (displayAsPercentage || isPercentage) {
                formattedValue = df.format(value * 100) + "%";
            } else {
                formattedValue = String.valueOf(Math.round(value));
            }

            return formatDescription(formattedValue, eqType);
        }

        private String formatDescription(String formattedValue, EquipmentType eqType) {
            if (isBooleanStat()) return displayName;
            if (displayName.contains("#")) return displayName.replaceAll("#\\s*%", "#").replace("#", formattedValue);

            if (displayAsPercentage || isPercentage) {
                if (displayName.contains("Extra")) return "Gain " + formattedValue + " of " + displayName;
                return formattedValue + " " + displayName.replaceFirst("^%\\s*", "");
            } else {
                if (displayName.endsWith("Damage") && !displayName.contains("Thorns")) {
                    if (isWeapon(eqType)) {
                        return "Adds " + formattedValue + " to " + displayName;
                    }
                    return "Adds " + formattedValue + " " + displayName + " to Attacks";
                }
                return "+" + formattedValue + " to " + displayName;
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
    private final int[] tierWeights;
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
        this.tierWeights = generateTierWeights(weight);
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
        this.tierWeights = generateTierWeights(weight);
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
        this.tierWeights = generateTierWeights(weight);
    }

    private int[] generateTierWeights(int baseWeight) {
        int[] weights = new int[10];
        // Bell curve centered around T7: T10 to T1
        double[] curve = { 0.40, 0.65, 0.85, 1.00, 0.85, 0.65, 0.45, 0.25, 0.10, 0.02 }; 
        for(int i=0; i<10; i++) {
            weights[i] = Math.max(1, (int)(baseWeight * curve[i]));
        }
        return weights;
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
    public int getTierWeight(int tier) {
        if (tier < 1 || tier > 10) return 0;
        return tierWeights[10 - tier];
    }
    
    public List<StatComponent> getComponents() { return components; }

    // Backward compatibility for methods accessing the primary (0th) component
    public String getDisplayName() { return components.get(0).getDisplayLabel(); }
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
