package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PassiveStatHelper {

    /**
     * Aggregates all modifiers from unlocked nodes in the passive tree for a given player.
     * This works on both client and server as long as the component is synced.
     */
    public static Map<RegistryEntry<EntityAttribute>, Double> getPassiveTreeSummary(PlayerEntity player) {
        Map<RegistryEntry<EntityAttribute>, Double> summary = new HashMap<>();
        
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        if (data == null) return summary;

        for (String nodeId : data.getUnlockedNodes()) {
            PassiveNode node = PassiveNodeRegistry.getNode(nodeId);
            if (node != null && node.modifiers() != null) {
                for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                    summary.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }
        
        return summary;
    }

    /**
     * Returns a sorted map of attribute names to their total passive values for display purposes.
     * Optionally includes a set of drafted node IDs to provide a "preview" summary.
     */
    public static Map<String, Double> getDisplayableSummary(PlayerEntity player, java.util.Set<String> draftNodes) {
        Map<RegistryEntry<EntityAttribute>, Double> raw = getPassiveTreeSummary(player);
        
        // Add draft nodes to the summary calculation
        if (draftNodes != null && !draftNodes.isEmpty()) {
            for (String nodeId : draftNodes) {
                PassiveNode node = PassiveNodeRegistry.getNode(nodeId);
                if (node != null && node.modifiers() != null) {
                    for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : node.modifiers().entrySet()) {
                        raw.merge(entry.getKey(), entry.getValue(), Double::sum);
                    }
                }
            }
        }

        Map<String, Double> display = new TreeMap<>(); // Sorted by name
        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : raw.entrySet()) {
            String readableName = getReadableName(entry.getKey());
            display.put(readableName, entry.getValue());
        }

        return display;
    }

    /**
     * Unified method to get a player-friendly name for an attribute.
     */
    public static String getReadableName(RegistryEntry<EntityAttribute> attribute) {
        String translationKey = attribute.value().getTranslationKey().toLowerCase();
        
        // Targeted mappings for consistency with the rest of the mod
        if (translationKey.contains("attack_damage")) {
            String name = translationKey.replace("attribute.name.minecraft.", "")
                                      .replace("attribute.name.cranks-dungeons.", "")
                                      .replace("generic.attack_damage", "physical_damage")
                                      .replace("attack_damage", "physical_damage")
                                      .replace("_", " ");
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        if (translationKey.contains("life_regeneration")) {
            return "Health Regen";
        }

        // Default processing for other stats with abbreviations
        String name = translationKey.replace("attribute.name.minecraft.", "")
                                  .replace("attribute.name.cranks-dungeons.", "")
                                  .replace("resistance", "res")
                                  .replace("experience", "exp")
                                  .replace("regeneration", "regen")
                                  .replace("_", " ");
        
        if (name.isEmpty()) return "Unknown Stat";
        
        // Capitalize first letter
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Returns the appropriate unit suffix for a stat name.
     */
    public static String getStatSuffix(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("duration")) return "s";
        if (lower.contains("range")) return " blocks";
        if (lower.contains("stacks")) return " max";
        if (lower.contains("max health")) return " HP";
        if (lower.contains("health regen")) return " HP/s";
        if (lower.contains("xp")) return " xp";
        return "";
    }
}
