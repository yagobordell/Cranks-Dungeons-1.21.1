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
     */
    public static Map<String, Double> getDisplayableSummary(PlayerEntity player) {
        Map<RegistryEntry<EntityAttribute>, Double> raw = getPassiveTreeSummary(player);
        Map<String, Double> display = new TreeMap<>(); // Sorted by name

        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : raw.entrySet()) {
            String name = entry.getKey().value().getTranslationKey();
            // Try to get a more readable name if possible, or use the key
            String readableName = name.replace("attribute.name.minecraft.", "")
                                      .replace("attribute.name.cranks-dungeons.", "")
                                      .replace("_", " ");
            
            // Capitalize
            readableName = readableName.substring(0, 1).toUpperCase() + readableName.substring(1);
            
            display.put(readableName, entry.getValue());
        }

        return display;
    }
}
