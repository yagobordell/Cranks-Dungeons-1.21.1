package com.cranks.dungeons.equipment;

import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.item.CustomRarity;
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
    private static final String RARITY_KEY = "Rarity";

    public static class ItemStat {
        public final String statId;
        public final double[] values;
        public final int tier;
        public final boolean isArtificial;

        public ItemStat(String statId, double[] values, int tier, boolean isArtificial) {
            this.statId = statId;
            this.values = values;
            this.tier = tier;
            this.isArtificial = isArtificial;
        }

        public ItemStat(String statId, double value, int tier, boolean isArtificial) {
            this(statId, new double[]{value}, tier, isArtificial);
        }

        public ItemStat(String statId, double value, int tier) {
            this(statId, value, tier, false);
        }

        public ItemStat(String statId, double value) {
            this(statId, value, 1, false);
        }

        public double getValue(int index) {
            if (values == null || index >= values.length) return 0;
            return values[index];
        }

        public double getValue() {
            return getValue(0);
        }
    }

    public static double getAttributeValue(ItemStack stack, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute) {
        double total = 0;
        for (ItemStat s : getStats(stack)) {
            CustomStat stat = StatRegistry.getStat(s.statId);
            if (stat != null) {
                for (int i = 0; i < stat.getComponents().size(); i++) {
                    if (stat.getComponents().get(i).getAttributes().contains(attribute)) {
                        total += s.getValue(i);
                    }
                }
            }
        }
        return total;
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

                        double[] valuesArray;
                        if (statNbt.contains("values")) {
                            Optional<NbtList> nbtListOpt = statNbt.getList("values");
                            if (nbtListOpt.isPresent()) {
                                NbtList nbtList = nbtListOpt.get();
                                valuesArray = new double[nbtList.size()];
                                for (int j = 0; j < nbtList.size(); j++) valuesArray[j] = nbtList.getDouble(j).orElse(0.0);
                            } else {
                                valuesArray = new double[0];
                            }
                        } else if (statNbt.contains("value")) {
                            valuesArray = new double[]{statNbt.getDouble("value").orElse(0.0)};
                        } else {
                            continue;
                        }

                        int tier = statNbt.getInt("tier").orElse(1);
                        boolean isArtificial = statNbt.getBoolean("artificial").orElse(false);

                        if (idOpt.isPresent()) {
                            stats.add(new ItemStat(idOpt.get(), valuesArray, tier, isArtificial));
                        }
                    }
                }
            }
        }

        return stats;
    }

    public static int getAffixCount(ItemStack stack, CustomStat.AffixType type) {
        return (int) getStats(stack).stream()
                .filter(s -> {
                    CustomStat stat = StatRegistry.getStat(s.statId);
                    return stat != null && stat.getAffixType() == type;
                })
                .count();
    }

    public static boolean canAddStat(ItemStack stack, CustomStat.AffixType type) {
        // Tools cannot have stats
        if (isTool(stack)) return false;

        CustomRarity rarity = CustomRarity.getRarity(stack);
        int currentCount = getAffixCount(stack, type);

        if (type == CustomStat.AffixType.PREFIX) {
            return currentCount < rarity.getMaxPrefixes();
        } else {
            return currentCount < rarity.getMaxSuffixes();
        }
    }

    private static boolean isTool(ItemStack stack) {
        return stack.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.AXES) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.SHOVELS) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.HOES);
    }

    public static void setRarity(ItemStack stack, CustomRarity rarity) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        nbt.putString(RARITY_KEY, rarity.name());
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
    }
    public static void clearStats(ItemStack stack) {
        if (stack.isEmpty()) return;
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        nbt.remove(STATS_KEY);
        nbt.remove(ARTIFICIAL_KEY);
        nbt.remove(CRYPTIC_AFFIX_KEY);
        nbt.remove(CRYPTIC_OPTIONS_KEY);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
        EquipmentStatApplier.updateItemAttributes(stack);
    }

    public static boolean removeRandomAffixOfType(ItemStack stack, CustomStat.AffixType type) {
        List<ItemStat> stats = getStats(stack);
        List<Integer> validIndices = new ArrayList<>();
        
        for (int i = 0; i < stats.size(); i++) {
            CustomStat stat = StatRegistry.getStat(stats.get(i).statId);
            if (stat != null && stat.getAffixType() == type) {
                validIndices.add(i);
            }
        }
        
        if (validIndices.isEmpty()) return false;
        
        int indexToRemove = validIndices.get(com.cranks.dungeons.util.RandomUtil.nextInt(validIndices.size()));
        
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        Optional<NbtList> statsListOpt = nbt.getList(STATS_KEY);
        if (statsListOpt.isPresent()) {
            NbtList statsList = statsListOpt.get();
            statsList.remove(indexToRemove);
            nbt.put(STATS_KEY, statsList);
            stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                    net.minecraft.component.type.NbtComponent.of(nbt));
            EquipmentStatApplier.updateItemAttributes(stack);
            return true;
        }
        return false;
    }

    public static boolean addRandomStat(ItemStack stack, CustomStat.AffixType type) {
        return addRandomStat(stack, type, 1, 100);
    }

    public static boolean addRandomStat(ItemStack stack, CustomStat.AffixType type, int minLevel, int maxLevel) {
        if (!canAddStat(stack, type)) return false;

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        if (equipType.isEmpty()) return false;

        com.cranks.dungeons.equipment.ArmorBaseType armorBase = com.cranks.dungeons.equipment.ArmorBaseType.getFromItem(stack);
        StatRegistry.StatAndTier statAndTier = StatRegistry.getRandomStatForEquipmentType(equipType.get(), armorBase, type, minLevel, maxLevel);
        if (statAndTier == null || statAndTier.stat == null) return false;
        
        CustomStat stat = statAndTier.stat;
        int tier = statAndTier.tier;

        // Check for duplicate attributes
        List<ItemStat> existingStats = getStats(stack);
        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;
                    return existingStat.getGroupId().equals(stat.getGroupId());
                });

        if (alreadyHasStat) return false;

        return addSpecificStat(stack, stat.getId(), tier);
    }

    public static boolean addRandomStatWithFixedTier(ItemStack stack, int tier, CustomStat.AffixType type) {
        if (!canAddStat(stack, type)) return false;

        Optional<EquipmentType> equipType = EquipmentType.getTypeForItem(stack);
        if (equipType.isEmpty()) return false;

        com.cranks.dungeons.equipment.ArmorBaseType armorBase = com.cranks.dungeons.equipment.ArmorBaseType.getFromItem(stack);
        StatRegistry.StatAndTier statAndTier = StatRegistry.getRandomStatForEquipmentType(equipType.get(), armorBase, type, 1, 100);
        if (statAndTier == null || statAndTier.stat == null) return false;

        CustomStat stat = statAndTier.stat;

        List<ItemStat> existingStats = getStats(stack);
        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;
                    return existingStat.getGroupId().equals(stat.getGroupId());
                });

        if (alreadyHasStat) return false;

        return addSpecificStat(stack, stat.getId(), tier);
    }

    public static boolean addSpecificStat(ItemStack stack, String statId, int tier) {
        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) return false;

        if (!canAddStat(stack, stat.getAffixType())) return false;

        // Check for duplicate attributes
        List<ItemStat> existingStats = getStats(stack);
        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;
                    return existingStat.getGroupId().equals(stat.getGroupId());
                });

        if (alreadyHasStat) return false;

        double[] values = stat.rollValues(tier);

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
        NbtList nbtValues = new NbtList();
        for (double v : values) nbtValues.add(net.minecraft.nbt.NbtDouble.of(v));
        newStat.put("values", nbtValues);
        newStat.putInt("tier", tier);
        statsList.add(newStat);

        nbt.put(STATS_KEY, statsList);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        EquipmentStatApplier.updateItemAttributes(stack);
        return true;
    }

    public static double getTotalStatValue(ItemStack stack, String baseStatId) {
        if (stack.isEmpty()) return 0.0;
        double total = 0;
        List<ItemStat> stats = getStats(stack);
        for (ItemStat itemStat : stats) {
            if (itemStat.statId.equals(baseStatId) || itemStat.statId.endsWith("_" + baseStatId)) {
                total += itemStat.getValue(0);
            }
        }
        return total;
    }

    // --- Cryptic Stat Methods (Legacy support, maybe refactor later) ---

    public static final String CRYPTIC_AFFIX_KEY = "CrypticAffix";

    public static boolean hasArtificialStat(ItemStack stack) {
        return getStats(stack).stream().anyMatch(s -> s.isArtificial);
    }

    public static boolean hasCrypticStat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        return nbt.getBoolean(ARTIFICIAL_KEY).orElse(false);
    }

    public static boolean addCrypticStat(ItemStack stack, CustomStat.AffixType type) {
        if (stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        nbt.putBoolean(ARTIFICIAL_KEY, true);
        nbt.putString(CRYPTIC_AFFIX_KEY, type.name());
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
        return true;
    }

    public static CustomStat.AffixType getCrypticAffixType(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        String name = nbt.getString(CRYPTIC_AFFIX_KEY).orElse("PREFIX");
        try {
            return CustomStat.AffixType.valueOf(name);
        } catch (Exception e) {
            return CustomStat.AffixType.PREFIX;
        }
    }

    public static boolean revealCrypticStat(ItemStack stack, String statId, int tier) {
        if (!hasCrypticStat(stack)) return false;

        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) return false;

        double value = stat.rollValue(tier);

        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        nbt.remove(ARTIFICIAL_KEY);
        nbt.remove(CRYPTIC_AFFIX_KEY);

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

    // ... (other cryptic methods remain same)
    private static final String CRYPTIC_OPTIONS_KEY = "CrypticOptions";

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
        if (statIds[0].isEmpty() && statIds[1].isEmpty() && statIds[2].isEmpty()) return null;
        return statIds;
    }

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

    public static void removeCrypticOptions(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        nbt.remove(CRYPTIC_OPTIONS_KEY);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
    }
}