package com.cranks.dungeons.inventory;

import com.cranks.dungeons.equipment.ArmorBaseType;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.*;

/** Read-only choice validation and previews. Only the altar generates missing rolls. */
public final class ArtificialRunes {
    public record Option(int index, CustomStat stat, int tier, double[] values) {
        public String name() { return stat.getComponents().stream().map(CustomStat.StatComponent::getDisplayLabel)
                .distinct().collect(java.util.stream.Collectors.joining(" / ")); }
    }
    private ArtificialRunes() {}
    public static Text text(String key, Object... args) { return Text.translatable("artificial.cranks-dungeons." + key, args); }
    public static String reason(ItemStack gear) {
        if (gear.isEmpty()) return "empty";
        if (gear.getCount() != 1 || !RunePool.supports(gear)) return "unsupported";
        if(RuneCrafting.pending(gear))return "pending";
        if (CustomRarity.getRarity(gear) != CustomRarity.RARE) return "rare_required";
        if (ItemStatManager.hasArtificialStat(gear)) return "already_revealed";
        return ItemStatManager.hasCrypticStat(gear) ? null : "no_marker";
    }
    public static boolean hasSavedChoices(ItemStack gear) {
        return gear.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().contains("CrypticOptions");
    }
    public static boolean compatible(ItemStack gear, CustomStat stat) {
        if (reason(gear) != null || !StatRegistry.isRandomlyRollable(stat)
                || stat.getEquipmentType() != EquipmentType.getTypeForItem(gear).orElse(null)
                || stat.getAffixType() != ItemStatManager.getCrypticAffixType(gear)
                || !ArmorBaseType.getFromItem(gear).canRoll(stat.getArmorBaseType())) return false;
        int cap = stat.getAffixType() == CustomStat.AffixType.PREFIX ? CustomRarity.RARE.getMaxPrefixes() : CustomRarity.RARE.getMaxSuffixes();
        return ItemStatManager.getAffixCount(gear, stat.getAffixType()) < cap
                && ItemStatManager.getStats(gear).stream().map(roll -> StatRegistry.getStat(roll.statId))
                .filter(Objects::nonNull).noneMatch(existing -> existing.getGroupId().equals(stat.getGroupId()));
    }
    public static List<Option> options(ItemStack gear) {
        if (reason(gear) != null) return List.of();
        var ids = ItemStatManager.getStoredCrypticStatIds(gear);
        var tiers = ItemStatManager.getStoredCrypticTiers(gear);
        var values = ItemStatManager.getStoredCrypticValues(gear);
        if (ids == null || tiers == null || values == null) return List.of();
        var result = new ArrayList<Option>(); var groups = new HashSet<String>();
        for (int i = 0; i < 3; i++) {
            var stat = StatRegistry.getStat(ids[i]);
            if (!compatible(gear, stat) || tiers[i]<com.cranks.dungeons.equipment.RuneTiers.best(gear) || tiers[i]>10 || values[i] == null
                    || values[i].length != stat.getComponents().size() || Arrays.stream(values[i]).anyMatch(v -> !Double.isFinite(v))
                    || !groups.add(stat.getGroupId())) continue;
            result.add(new Option(i, stat, tiers[i], values[i]));
        }
        return List.copyOf(result);
    }
    public static ItemStack preview(ItemStack gear, int index) {
        var option = options(gear).stream().filter(row -> row.index == index).findFirst().orElse(null);
        if (option == null) return ItemStack.EMPTY;
        var result = gear.copy();
        if (!ItemStatManager.revealCrypticStat(result, option.stat.getId(), option.tier, option.values)) return ItemStack.EMPTY;
        ItemStatManager.removeCrypticOptions(result);
        return result;
    }
}
