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

    public static boolean isUnique(ItemStack stack) {
        if (stack.isEmpty()) return false;
        net.minecraft.item.Item item = stack.getItem();
        return item == net.minecraft.item.Items.ELYTRA ||
               item == net.minecraft.item.Items.TRIDENT ||
               item == net.minecraft.item.Items.MACE ||
               item == net.minecraft.item.Items.TURTLE_HELMET;
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
            NbtList statsList = nbt.getList(STATS_KEY, net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < statsList.size(); i++) {
                NbtCompound statNbt = statsList.getCompound(i);
                String id = statNbt.getString("id");

                double[] valuesArray;
                if (statNbt.contains("values")) {
                    NbtList nbtList = statNbt.getList("values", net.minecraft.nbt.NbtElement.DOUBLE_TYPE);
                    valuesArray = new double[nbtList.size()];
                    for (int j = 0; j < nbtList.size(); j++) valuesArray[j] = nbtList.getDouble(j);
                } else if (statNbt.contains("value")) {
                    valuesArray = new double[]{statNbt.getDouble("value")};
                } else {
                    continue;
                }

                int tier = statNbt.contains("tier") ? statNbt.getInt("tier") : 1;
                boolean isArtificial = statNbt.getBoolean("artificial");

                if (!id.isEmpty()) {
                    stats.add(new ItemStat(id, valuesArray, tier, isArtificial));
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
        if (AnimalArmorHelper.isSupported(stack)) return false;

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
                
        if (nbt.contains(STATS_KEY, net.minecraft.nbt.NbtElement.LIST_TYPE)) {
            NbtList statsList = nbt.getList(STATS_KEY, net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
            if (indexToRemove >= 0 && indexToRemove < statsList.size()) {
                statsList.remove(indexToRemove);
                nbt.put(STATS_KEY, statsList);
                stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                        net.minecraft.component.type.NbtComponent.of(nbt));
                EquipmentStatApplier.updateItemAttributes(stack);
                return true;
            }
        }
        return false;
    }

    public static boolean removeRandomStat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        if (nbt.contains(STATS_KEY, net.minecraft.nbt.NbtElement.LIST_TYPE)) {
            NbtList statsList = nbt.getList(STATS_KEY, net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
            if (statsList.isEmpty()) return false;

            int indexToRemove = com.cranks.dungeons.util.RandomUtil.nextInt(statsList.size());
            statsList.remove(indexToRemove);

            nbt.put(STATS_KEY, statsList);
            stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                    net.minecraft.component.type.NbtComponent.of(nbt));
            EquipmentStatApplier.updateItemAttributes(stack);
            return true;
        }
        return false;
    }

    public static boolean rerollAllStatValues(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        if (nbt.contains(STATS_KEY, net.minecraft.nbt.NbtElement.LIST_TYPE)) {
            NbtList statsList = nbt.getList(STATS_KEY, net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
            if (statsList.isEmpty()) return false;

            for (int i = 0; i < statsList.size(); i++) {
                NbtCompound statNbt = statsList.getCompound(i);
                String id = statNbt.getString("id");
                int tier = statNbt.contains("tier") ? statNbt.getInt("tier") : 1;

                CustomStat stat = StatRegistry.getStat(id);
                if (stat != null && !statNbt.getBoolean("protected")) {
                    if(!isUnique(stack))tier=RuneTiers.clamp(stack,tier);
                    statNbt.putInt("tier",tier);
                    double[] newValues = stat.rollValues(tier);
                    NbtList nbtValues = new NbtList();
                    for (double v : newValues) {
                        nbtValues.add(net.minecraft.nbt.NbtDouble.of(v));
                    }
                    statNbt.put("values", nbtValues);
                }
            }

            nbt.put(STATS_KEY, statsList);
            stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                    net.minecraft.component.type.NbtComponent.of(nbt));
            EquipmentStatApplier.updateItemAttributes(stack);
            return true;
        }
        return false;
    }

    public static int getBestAllowedTier(ItemStack stack) {
        return RuneTiers.best(stack);
    }

    public static boolean addRandomStat(ItemStack stack, CustomStat.AffixType type) {
        return addRandomStat(stack, type, 1, 100);
    }

    public static boolean addRandomStat(ItemStack stack, CustomStat.AffixType type, int minLevel, int maxLevel) {
        if(isUnique(stack)||!canAddStat(stack,type))return false;
        var candidates=com.cranks.dungeons.inventory.RuneCrafting.candidates(stack,type).stream()
                .filter(stat->stat.getLevel()>=minLevel&&stat.getLevel()<=maxLevel).toList();
        var roll=StatRegistry.rollFamilyAndTier(candidates,RuneTiers.random(stack,0));
        return roll!=null&&addSpecificStat(stack,roll.stat.getId(),roll.tier);
    }

    public static boolean addRandomStatWithFixedTier(ItemStack stack, int tier, CustomStat.AffixType type) {
        if(isUnique(stack)||!canAddStat(stack,type))return false;
        var roll=StatRegistry.rollFamilyAndTier(com.cranks.dungeons.inventory.RuneCrafting.candidates(stack,type),List.of(RuneTiers.clamp(stack,tier)));
        return roll!=null&&addSpecificStat(stack,roll.stat.getId(),roll.tier);
    }

    public static boolean addSpecificStat(ItemStack stack, String statId, int tier) {
        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) return false;

        if(!isUnique(stack))tier=RuneTiers.clamp(stack,tier);

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
        if (nbt.contains(STATS_KEY, net.minecraft.nbt.NbtElement.LIST_TYPE)) {
            statsList = nbt.getList(STATS_KEY, net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
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
        return nbt.getBoolean(ARTIFICIAL_KEY);
    }

    public static boolean addCrypticStat(ItemStack stack, CustomStat.AffixType type) {
        if (stack.isEmpty()) return false;
        if (AnimalArmorHelper.isSupported(stack)) return false;
        if (isTool(stack)) return false;
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
        String name = nbt.contains(CRYPTIC_AFFIX_KEY) ? nbt.getString(CRYPTIC_AFFIX_KEY) : "PREFIX";
        try {
            return CustomStat.AffixType.valueOf(name);
        } catch (Exception e) {
            return CustomStat.AffixType.PREFIX;
        }
    }

    public static boolean revealCrypticStat(ItemStack stack, String statId, int tier, double[] values) {
        if (!hasCrypticStat(stack)) return false;
        if (AnimalArmorHelper.isSupported(stack)) return false;
        if (isTool(stack)) return false;

        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) return false;

        int capped=RuneTiers.clamp(stack,tier);
        if(capped!=tier){tier=capped;values=null;}

        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();

        nbt.remove(ARTIFICIAL_KEY);
        nbt.remove(CRYPTIC_AFFIX_KEY);

        NbtList statsList = nbt.contains(STATS_KEY) ? nbt.getList(STATS_KEY, 10) : new NbtList();

        NbtCompound newStat = new NbtCompound();
        newStat.putString("id", statId);
        
        NbtList nbtValues = new NbtList();
        if (values != null && values.length > 0) {
            for (double v : values) nbtValues.add(net.minecraft.nbt.NbtDouble.of(v));
        } else {
            for (double v : stat.rollValues(tier)) nbtValues.add(net.minecraft.nbt.NbtDouble.of(v));
        }
        newStat.put("values", nbtValues);
        newStat.putInt("tier", tier);
        newStat.putBoolean("artificial", true);
        statsList.add(newStat);

        nbt.put(STATS_KEY, statsList);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));

        EquipmentStatApplier.updateItemAttributes(stack);
        return true;
    }

    public static boolean revealCrypticStat(ItemStack stack, String statId, int tier) {
        return revealCrypticStat(stack, statId, tier, null);
    }

    // ... (other cryptic methods remain same)
    private static final String CRYPTIC_OPTIONS_KEY = "CrypticOptions";

    public static void storeCrypticOptions(ItemStack stack, String[] statIds, int[] tiers, double[][] values) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        NbtCompound options = new NbtCompound();
        for (int i = 0; i < 3; i++) {
            options.putString("stat" + i, statIds[i] != null ? statIds[i] : "");
            options.putInt("tier" + i, tiers[i]);
            if (values != null && i < values.length && values[i] != null) {
                NbtList valList = new NbtList();
                for (double v : values[i]) valList.add(net.minecraft.nbt.NbtDouble.of(v));
                options.put("values" + i, valList);
            }
        }
        nbt.put(CRYPTIC_OPTIONS_KEY, options);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
    }

    public static void storeCrypticOptions(ItemStack stack, String[] statIds, int[] tiers) {
        storeCrypticOptions(stack, statIds, tiers, null);
    }

    public static String[] getStoredCrypticStatIds(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        if (!nbt.contains(CRYPTIC_OPTIONS_KEY)) return null;
        NbtCompound options = nbt.getCompound(CRYPTIC_OPTIONS_KEY);
        String[] statIds = new String[3];
        for (int i = 0; i < 3; i++) {
            statIds[i] = options.getString("stat" + i);
        }
        if (statIds[0].isEmpty() && statIds[1].isEmpty() && statIds[2].isEmpty()) return null;
        return statIds;
    }

    public static int[] getStoredCrypticTiers(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        if (!nbt.contains(CRYPTIC_OPTIONS_KEY)) return null;
        NbtCompound options = nbt.getCompound(CRYPTIC_OPTIONS_KEY);
        int[] tiers = new int[3];
        for (int i = 0; i < 3; i++) {
            tiers[i] = options.contains("tier" + i) ? options.getInt("tier" + i) : 1;
        }
        return tiers;
    }

    public static double[][] getStoredCrypticValues(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        if (!nbt.contains(CRYPTIC_OPTIONS_KEY)) return null;
        NbtCompound options = nbt.getCompound(CRYPTIC_OPTIONS_KEY);
        double[][] values = new double[3][];
        for (int i = 0; i < 3; i++) {
            if (options.contains("values" + i)) {
                NbtList list = options.getList("values" + i, net.minecraft.nbt.NbtElement.DOUBLE_TYPE);
                values[i] = new double[list.size()];
                for (int j = 0; j < list.size(); j++) {
                    values[i][j] = list.getDouble(j);
                }
            } else {
                values[i] = null;
            }
        }
        return values;
    }

    public static void removeCrypticOptions(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        nbt.remove(CRYPTIC_OPTIONS_KEY);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.of(nbt));
    }
}
