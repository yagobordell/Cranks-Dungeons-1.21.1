package com.cranks.dungeons.client;

import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class EnchantingStatPreview {
    private record Stat(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation) {
        Text label() {
            return Text.translatableWithFallback(attribute.value().getTranslationKey(),
                    PassiveStatHelper.getReadableName(attribute));
        }

        String value(double value) {
            String path = attribute.getKey().orElseThrow().getValue().getPath();
            String unit = operation == EntityAttributeModifier.Operation.ADD_VALUE ? CharacterStats.unit(path) : "%";
            return (value > 0 ? "+" : "") + CharacterStats.formatValue(value, unit);
        }
    }

    record Change(Text label, String before, String after, boolean increased) {
        String values() {
            return before + " → " + after;
        }

        Text tooltip() {
            return Text.translatable("crafting.cranks-dungeons.enchant_stat_change", label, values());
        }
    }

    private EnchantingStatPreview() {}

    static List<Change> compare(ItemStack before, ItemStack after, EquipmentSlot slot) {
        var original = bonuses(before, slot);
        var updated = bonuses(after, slot);
        Set<Stat> stats = new HashSet<>(original.keySet());
        stats.addAll(updated.keySet());
        var changes = new ArrayList<Change>();
        for (Stat stat : stats) {
            double from = original.getOrDefault(stat, 0.0);
            double to = updated.getOrDefault(stat, 0.0);
            if (Math.abs(from - to) < 1.0e-6) continue;
            changes.add(new Change(stat.label(), stat.value(from), stat.value(to), to > from));
        }
        changes.sort(Comparator.comparing(change -> change.label().getString()));
        return List.copyOf(changes);
    }

    private static Map<Stat, Double> bonuses(ItemStack stack, EquipmentSlot slot) {
        Set<Identifier> enchantments = new HashSet<>();
        EnchantmentHelper.applyAttributeModifiers(stack, slot, (attribute, modifier) -> enchantments.add(modifier.id()));
        var result = new LinkedHashMap<Stat, Double>();
        // Use the same filtering and Enchantment Effectiveness scaling as equipped gear.
        stack.applyAttributeModifiers(slot, (attribute, modifier) -> {
            if (!enchantments.contains(modifier.id())) return;
            var stat = new Stat(attribute, modifier.operation());
            if (modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                result.merge(stat, modifier.value(), (first, second) -> (1 + first) * (1 + second) - 1);
            } else {
                result.merge(stat, modifier.value(), Double::sum);
            }
        });
        return result;
    }
}
