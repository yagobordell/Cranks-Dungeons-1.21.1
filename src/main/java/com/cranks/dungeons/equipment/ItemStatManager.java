package com.cranks.dungeons.equipment;

import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class ItemStatManager {
    private static final String STATS_KEY = "CustomStats";
    private static final String ARTIFICIAL_KEY = "ArtificialStat";
    private static final int MAX_STATS = 5;

    public static class ItemStat {
        public final String statId;
        public final double value;
        public final int tier;
        public final boolean isArtificial;

        public ItemStat(String statId, double value, int tier, boolean isArtificial) {
            this.statId = statId;
            this.value = value;
            this.tier = tier;
            this.isArtificial = isArtificial;
        }

        public ItemStat(String statId, double value, int tier) {
            this(statId, value, tier, false);
        }

        // Legacy constructor for backwards compatibility
        public ItemStat(String statId, double value) {
            this(statId, value, 1, false);
        }
    }

    public static List<ItemStat> getStats(ItemStack stack) {
        List<ItemStat> stats = new ArrayList<>();
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        if (nbt.contains(STATS_KEY)) {
            Optional<NbtList> statsListOpt = nbt.getList(STATS_KEY);
            if (statsListOpt.isPresent()) {
                NbtList statsList = statsListOpt.get();
                for (int i = 0; i < statsList.size(); i++) {
                    Optional<NbtCompound> statNbtOpt = statsList.getCompound(i);
                    if (statNbtOpt.isPresent()) {
                        NbtCompound statNbt = statNbtOpt.get();
                        Optional<String> idOpt = statNbt.getString("id");
                        Optional<Double> valueOpt = statNbt.getDouble("value");

                        // Read tier (default to 1 if not present for backwards compatibility)
                        int tier = statNbt.getInt("tier").orElse(1);
                        boolean isArtificial = statNbt.getBoolean("artificial").orElse(false);

                        if (idOpt.isPresent() && valueOpt.isPresent()) {
                            stats.add(new ItemStat(idOpt.get(), valueOpt.get(), tier, isArtificial));
                        }
                    }
                }
            }
        }

        return stats;
    }

    public static boolean canAddStat(ItemStack stack) {
        int count = getStats(stack).size();
        if (hasCrypticStat(stack)) {
            count++;
        }
        return count < MAX_STATS;
    }

    /**
     * Check if the item already has an artificial stat.
     */
    public static boolean hasArtificialStat(ItemStack stack) {
        return getStats(stack).stream().anyMatch(s -> s.isArtificial);
    }

    /**
     * Check if the item has a cryptic (unrevealed) artificial stat.
     */
    public static boolean hasCrypticStat(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        return nbt.getBoolean(ARTIFICIAL_KEY).orElse(false);
    }

    /**
     * Add a cryptic artificial stat placeholder to the item.
     * This marks the item as having a cryptic stat that needs to be revealed
     * at the Artificial Enhancement Altar.
     */
    public static boolean addCrypticStat(ItemStack stack) {
        if (!canAddStat(stack)) return false;
        if (hasArtificialStat(stack)) return false;
        if (hasCrypticStat(stack)) return false;

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        if (equipType.isEmpty()) return false;

        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        // Mark as having a cryptic stat
        nbt.putBoolean(ARTIFICIAL_KEY, true);

        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        return true;
    }

    /**
     * Reveal the cryptic stat by replacing it with a chosen actual stat.
     */
    public static boolean revealCrypticStat(ItemStack stack, String statId, int tier) {
        if (!hasCrypticStat(stack)) return false;

        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) return false;

        // Check for duplicate attributes
        List<ItemStat> existingStats = getStats(stack);
        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;
                    if (s.statId.equals(statId)) return true;
                    if (existingStat.getAttribute().equals(stat.getAttribute())) return true;
                    return false;
                });

        if (alreadyHasStat) return false;

        double value = stat.rollValue(tier);

        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        // Remove the cryptic marker
        nbt.remove(ARTIFICIAL_KEY);

        // Add the actual stat
        NbtList statsList;
        Optional<NbtList> statsListOpt = nbt.getList(STATS_KEY);
        if (statsListOpt.isPresent()) {
            statsList = statsListOpt.get();
        } else {
            statsList = new NbtList();
        }

        NbtCompound newStat = new NbtCompound();
        newStat.putString("id", statId);
        newStat.putDouble("value", value);
        newStat.putInt("tier", tier);
        newStat.putBoolean("artificial", true);
        statsList.add(newStat);

        nbt.put(STATS_KEY, statsList);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        EquipmentStatApplier.updateItemAttributes(stack);
        return true;
    }

    private static final String CRYPTIC_OPTIONS_KEY = "CrypticOptions";

    /**
     * Store the rolled stat options on the item itself so they can't be re-rolled.
     */
    public static void storeCrypticOptions(ItemStack stack, String[] statIds, int[] tiers) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        NbtCompound options = new NbtCompound();
        for (int i = 0; i < 3; i++) {
            options.putString("stat" + i, statIds[i] != null ? statIds[i] : "");
            options.putInt("tier" + i, tiers[i]);
        }

        nbt.put(CRYPTIC_OPTIONS_KEY, options);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
    }

    /**
     * Read stored cryptic options from the item. Returns null if none stored.
     */
    public static String[] getStoredCrypticStatIds(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        if (!nbt.contains(CRYPTIC_OPTIONS_KEY)) return null;

        Optional<NbtCompound> optionsOpt = nbt.getCompound(CRYPTIC_OPTIONS_KEY);
        if (optionsOpt.isEmpty()) return null;

        NbtCompound options = optionsOpt.get();
        String[] statIds = new String[3];
        for (int i = 0; i < 3; i++) {
            statIds[i] = options.getString("stat" + i).orElse("");
        }

        // Check if any are actually set
        if (statIds[0].isEmpty() && statIds[1].isEmpty() && statIds[2].isEmpty()) return null;

        return statIds;
    }

    /**
     * Read stored cryptic tiers from the item. Returns null if none stored.
     */
    public static int[] getStoredCrypticTiers(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        if (!nbt.contains(CRYPTIC_OPTIONS_KEY)) return null;

        Optional<NbtCompound> optionsOpt = nbt.getCompound(CRYPTIC_OPTIONS_KEY);
        if (optionsOpt.isEmpty()) return null;

        NbtCompound options = optionsOpt.get();
        int[] tiers = new int[3];
        for (int i = 0; i < 3; i++) {
            tiers[i] = options.getInt("tier" + i).orElse(1);
        }

        return tiers;
    }

    /**
     * Remove stored cryptic options from the item (called after revealing).
     */
    public static void removeCrypticOptions(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        nbt.remove(CRYPTIC_OPTIONS_KEY);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
    }

    public static boolean addRandomStat(ItemStack stack, int tier) {
        if (!canAddStat(stack)) {
            return false;
        }

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        if (equipType.isEmpty()) {
            return false;
        }

        // Get a random stat for this equipment type
        CustomStat stat = StatRegistry.getRandomStatForEquipmentType(equipType.get());
        if (stat == null) {
            return false;
        }

        // Check if item already has this stat or one with the same attribute
        List<ItemStat> existingStats = getStats(stack);
        CustomStat newTmpStat = stat;

        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;

                    // Check if same stat ID
                    if (s.statId.equals(newTmpStat.getId())) return true;

                    // Check if same attribute (prevent duplicate attributes)
                    if (existingStat.getAttribute().equals(newTmpStat.getAttribute())) {
                        return true;
                    }

                    return false;
                });

        if (alreadyHasStat) {
            return false;
        }

        double value = stat.rollValue(tier);

        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        NbtList statsList;
        Optional<NbtList> statsListOpt = nbt.getList(STATS_KEY);
        if (statsListOpt.isPresent()) {
            statsList = statsListOpt.get();
        } else {
            statsList = new NbtList();
        }

        NbtCompound newStat = new NbtCompound();
        newStat.putString("id", stat.getId());
        newStat.putDouble("value", value);
        newStat.putInt("tier", tier); // Store the tier
        statsList.add(newStat);

        nbt.put(STATS_KEY, statsList);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        // Update item attributes for weapons/tools/armor
        EquipmentStatApplier.updateItemAttributes(stack);
        return true;
    }

    /**
     * Add a specific stat to an item at the given tier.
     * Used by elemental tomes that grant a predetermined stat.
     */
    public static boolean addSpecificStat(ItemStack stack, String statId, int tier) {
        if (!canAddStat(stack)) {
            return false;
        }

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        if (equipType.isEmpty()) {
            return false;
        }

        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) {
            return false;
        }

        // Check if item already has this stat or one with the same attribute
        List<ItemStat> existingStats = getStats(stack);
        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;
                    if (s.statId.equals(statId)) return true;
                    if (existingStat.getAttribute().equals(stat.getAttribute())) return true;
                    return false;
                });

        if (alreadyHasStat) {
            return false;
        }

        double value = stat.rollValue(tier);

        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        NbtList statsList;
        Optional<NbtList> statsListOpt = nbt.getList(STATS_KEY);
        if (statsListOpt.isPresent()) {
            statsList = statsListOpt.get();
        } else {
            statsList = new NbtList();
        }

        NbtCompound newStat = new NbtCompound();
        newStat.putString("id", statId);
        newStat.putDouble("value", value);
        newStat.putInt("tier", tier);
        statsList.add(newStat);

        nbt.put(STATS_KEY, statsList);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        // Update item attributes
        EquipmentStatApplier.updateItemAttributes(stack);

        return true;
    }

    public static double getTotalStatValue(ItemStack stack, String baseStatId) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        double total = 0;
        List<ItemStat> stats = getStats(stack);
        for (ItemStat itemStat : stats) {
            if (itemStat.statId.equals(baseStatId) || itemStat.statId.endsWith("_" + baseStatId)) {
                total += itemStat.value;
            }
        }
        return total;
    }

    private static Text createStatTooltip(CustomStat stat, double value, int tier) {
        String valueStr = stat.formatValue(value);

        Formatting tierColor = getTierColor(tier);

        return Text.literal(valueStr)
                .formatted(stat.getColor())
                .append(Text.literal(" (T" + tier + ")")
                        .formatted(tierColor));
    }

    private static Formatting getTierColor(int tier) {
        return AbstractTomeItem.getTierColor(tier);
    }
}