package com.cranks.dungeons.enchantment;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.*;

public class EnchantmentDisableManager {

    private static final List<Identifier> TOGGLEABLE_ENCHANTMENTS = new ArrayList<>();
    private static final Map<UUID, Set<Identifier>> playerDisabledEnchantments = new WeakHashMap<>();

    public static void registerToggleable(Identifier enchantmentId) {
        if (!TOGGLEABLE_ENCHANTMENTS.contains(enchantmentId)) {
            TOGGLEABLE_ENCHANTMENTS.add(enchantmentId);
        }
    }

    /**
     * Get which registered toggleable enchantments are actually present on the given tool.
     */
    public static List<Identifier> getRelevantEnchantments(ItemStack tool) {
        if (tool.isEmpty()) return Collections.emptyList();

        var enchants = tool.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null) return Collections.emptyList();

        Set<Identifier> toolEnchantIds = new HashSet<>();
        enchants.getEnchantments().forEach(entry ->
                entry.getKey().ifPresent(key -> toolEnchantIds.add(key.getValue()))
        );

        // Optimization: use retainAll to keep only the intersection
        Set<Identifier> relevant = new HashSet<>(TOGGLEABLE_ENCHANTMENTS);
        relevant.retainAll(toolEnchantIds);
        return new ArrayList<>(relevant);
    }

    /**
     * Cycle to the next disable state based on relevant enchantments on the current tool.
     *
     * For N relevant enchantments:
     *   State 0:    none disabled
     *   State 1..N: individual enchantment disabled
     *   State N+1:  all disabled (only if N > 1)
     */
    public static void toggleNext(UUID playerId, List<Identifier> relevant) {
        if (relevant.isEmpty()) return;

        Set<Identifier> disabled = playerDisabledEnchantments.computeIfAbsent(playerId, k -> new HashSet<>());

        int size = relevant.size();
        int stateCount = (size > 1) ? size + 2 : 2;

        int currentState = determineCurrentState(disabled, relevant);
        int nextState = (currentState + 1) % stateCount;

        applyState(disabled, relevant, nextState);
    }

    private static int determineCurrentState(Set<Identifier> disabled, List<Identifier> relevant) {
        List<Identifier> relevantDisabled = new ArrayList<>();
        for (Identifier id : relevant) {
            if (disabled.contains(id)) {
                relevantDisabled.add(id);
            }
        }

        int size = relevant.size();

        // Nothing disabled
        if (relevantDisabled.isEmpty()) return 0;

        // All disabled
        if (relevantDisabled.size() == size && size > 1) return size + 1;

        // Exactly one disabled — find which one
        if (relevantDisabled.size() == 1) {
            int index = relevant.indexOf(relevantDisabled.get(0));
            if (index >= 0) return index + 1;
        }

        // Unrecognized combination, reset to beginning
        return 0;
    }

    private static void applyState(Set<Identifier> disabled, List<Identifier> relevant, int state) {
        // Remove all relevant enchantments from disabled first
        for (Identifier id : relevant) {
            disabled.remove(id);
        }

        int size = relevant.size();

        if (state == 0) {
            // All enabled — nothing to add back
        } else if (state >= 1 && state <= size) {
            // Disable one specific enchantment
            disabled.add(relevant.get(state - 1));
        } else if (size > 1 && state == size + 1) {
            // Disable all relevant
            disabled.addAll(relevant);
        }
    }

    public static boolean isDisabled(UUID playerId, Identifier enchantmentId) {
        Set<Identifier> disabled = playerDisabledEnchantments.get(playerId);
        if (disabled == null) return false;
        return disabled.contains(enchantmentId);
    }

    /**
     * Get label for currently disabled enchantments among the relevant ones.
     * Returns null if nothing is disabled.
     */
    public static String getDisabledLabel(UUID playerId, List<Identifier> relevant) {
        Set<Identifier> disabled = playerDisabledEnchantments.get(playerId);
        if (disabled == null) return null;

        List<String> names = new ArrayList<>();
        for (Identifier id : relevant) {
            if (disabled.contains(id)) {
                names.add(formatName(id));
            }
        }

        if (names.isEmpty()) return null;
        return String.join(" & ", names);
    }

    public static void removePlayer(UUID playerId) {
        playerDisabledEnchantments.remove(playerId);
    }

    private static String formatName(Identifier id) {
        String path = id.getPath();
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}