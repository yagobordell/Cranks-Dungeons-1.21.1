package com.cranks.dungeons.inventory;

import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.item.*;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import java.util.*;

/** A deterministic catalogue of possible results, never a prospective item roll. */
public final class RunePool {
    public enum Kind { RANDOM, TARGETED, ARTIFICIAL, REROLL }
    public record Route(Kind kind, ItemStack tome, List<Integer> tiers) {}
    public record Entry(CustomStat stat, List<Route> routes, boolean present, boolean full) {
        public boolean targetedOnly() { return routes.stream().allMatch(route -> route.kind == Kind.TARGETED); }
        public String name() { return stat.getComponents().stream().map(CustomStat.StatComponent::getDisplayLabel)
                .distinct().collect(java.util.stream.Collectors.joining(" / ")); }
    }
    private RunePool() {}
    public static boolean supports(ItemStack gear) {
        return !gear.isEmpty() && EquipmentType.getTypeForItem(gear).isPresent()
                && CustomRarity.getRarity(gear) != CustomRarity.UNIQUE && !AnimalArmorHelper.isSupported(gear)
                && !gear.isIn(ItemTags.AXES) && !gear.isIn(ItemTags.PICKAXES) && !gear.isIn(ItemTags.SHOVELS) && !gear.isIn(ItemTags.HOES);
    }
    public static List<Entry> entries(ItemStack gear, ItemStack selectedTome, boolean selectedOnly) {
        if (!supports(gear) || selectedOnly && selectedTome.isEmpty()) return List.of();
        var type = EquipmentType.getTypeForItem(gear).orElseThrow();
        var base = ArmorBaseType.getFromItem(gear);
        var routes = new LinkedHashMap<CustomStat,List<Route>>();
        RunicTomeItem runic = selectedTome.getItem() instanceof RunicTomeItem item ? item : null;
        boolean valueReroll = selectedOnly && runic != null && runic.getTomeType() == RunicTomeItem.TomeType.DIVINE;
        if (selectedOnly && runic != null && runic.getTomeType() == RunicTomeItem.TomeType.ANNULMENT) return List.of();
        if (valueReroll) {
            for (var roll : ItemStatManager.getStats(gear)) {
                var stat = StatRegistry.getStat(roll.statId);
                if (stat != null && roll.tier >= 1 && roll.tier <= 10) add(routes, stat,
                        new Route(Kind.REROLL, selectedTome.copyWithCount(1), List.of(RuneTiers.clamp(gear,roll.tier))));
            }
        } else {
            if (!selectedOnly || runic != null) {
                var candidates = StatRegistry.getStatsByEquipmentType(type).stream().filter(StatRegistry::isRandomlyRollable)
                        .filter(stat -> base.canRoll(stat.getArmorBaseType())).toList();
                for (var stat : candidates) {
                    var tiers = RuneTiers.random(gear,selectedOnly?RuneCrafting.strength(runic):0);
                    if (!tiers.isEmpty()) add(routes,stat,new Route(Kind.RANDOM,selectedOnly ? selectedTome.copyWithCount(1) : ItemStack.EMPTY,tiers));
                }
            }
            if (!selectedOnly || selectedTome.getItem() instanceof AbstractTomeItem) {
                var tomes = selectedOnly ? List.of(selectedTome.getItem()) : Registries.ITEM.stream()
                        .filter(AbstractTomeItem.class::isInstance).sorted(Comparator.comparing(item -> Registries.ITEM.getId(item).toString())).toList();
                for (var item : tomes) {
                    var tome = (AbstractTomeItem)item;
                    var stat = StatRegistry.getStat(tome.getStatIdForEquipment(type));
                    if (stat != null && base.canRoll(stat.getArmorBaseType())) add(routes,stat,new Route(Kind.TARGETED,item.getDefaultStack(),RuneCrafting.targetedTiers(gear,tome)));
                }
            }
            if (!selectedOnly || selectedTome.getItem() instanceof ArtificialTomeItem) {
                for (var stat : StatRegistry.getStatsByEquipmentType(type)) {
                    if (StatRegistry.isRandomlyRollable(stat) && base.canRoll(stat.getArmorBaseType())
                            && (!selectedOnly || !ItemStatManager.hasCrypticStat(gear) || stat.getAffixType() == ItemStatManager.getCrypticAffixType(gear))) {
                        add(routes,stat,new Route(Kind.ARTIFICIAL,ModItems.ARTIFICIAL_TOME.getDefaultStack(),RuneTiers.artificial(gear)));
                    }
                }
            }
        }
        var rarity = CustomRarity.getRarity(gear);
        if (selectedOnly && selectedTome.getItem() instanceof AbstractTomeItem) rarity = CustomRarity.RARE;
        if (selectedOnly && runic != null) rarity = switch (runic.getTomeType()) {
            case TRANSMUTATION, GREATER_TRANSMUTATION, PERFECT_TRANSMUTATION -> CustomRarity.MAGIC;
            case REGAL, GREATER_REGAL, PERFECT_REGAL, ALCHEMY -> CustomRarity.RARE;
            default -> rarity;
        };
        final var capacity = rarity;
        var existing = ItemStatManager.getStats(gear).stream().map(roll -> StatRegistry.getStat(roll.statId))
                .filter(Objects::nonNull).map(CustomStat::getGroupId).collect(java.util.stream.Collectors.toSet());
        boolean reset = selectedOnly && runic != null && runic.getTomeType() == RunicTomeItem.TomeType.ALCHEMY;
        return routes.entrySet().stream().map(pair -> new Entry(pair.getKey(),List.copyOf(pair.getValue()),
                !reset && existing.contains(pair.getKey().getGroupId()), !reset && RunicAltarRules.occupied(gear,pair.getKey().getAffixType())
                >= (pair.getKey().getAffixType() == CustomStat.AffixType.PREFIX ? capacity.getMaxPrefixes() : capacity.getMaxSuffixes())))
                .sorted(Comparator.comparing((Entry row) -> row.stat.getAffixType()).thenComparing(Entry::name,String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(row -> row.stat.getId())).toList();
    }
    private static void add(Map<CustomStat,List<Route>> all, CustomStat stat, Route route) {
        all.computeIfAbsent(stat,ignored -> new ArrayList<>()).add(route);
    }
}
