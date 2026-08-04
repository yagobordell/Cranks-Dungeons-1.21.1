package com.cranks.dungeons.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;

import java.util.Optional;

public enum CustomRarity {
    NORMAL("Normal", Formatting.WHITE, 0, 0),
    MAGIC("Magic", Formatting.AQUA, 1, 1),
    RARE("Rare", Formatting.YELLOW, 3, 3),
    UNIQUE("Unique", Formatting.GOLD, 6, 6);

    private final String name;
    private final Formatting color;
    private final int maxPrefixes;
    private final int maxSuffixes;

    CustomRarity(String name, Formatting color, int maxPrefixes, int maxSuffixes) {
        this.name = name;
        this.color = color;
        this.maxPrefixes = maxPrefixes;
        this.maxSuffixes = maxSuffixes;
    }

    public String getName() {
        return name;
    }

    public Formatting getColor() {
        return color;
    }

    public int getMaxPrefixes() {
        return maxPrefixes;
    }

    public int getMaxSuffixes() {
        return maxSuffixes;
    }

    public static CustomRarity getRarity(ItemStack stack) {
        if (stack.isEmpty()) return NORMAL;

        // 1. Check for Uniques (Fixed Uniques)
        if (stack.isOf(Items.ELYTRA) || stack.isOf(Items.TRIDENT) || stack.isOf(Items.MACE) || stack.isOf(Items.TURTLE_HELMET)) {
            return UNIQUE;
        }

        // 2. Check NBT for rarity
        NbtCompound nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        if (nbt.contains("Rarity")) {
            String rarityStr = nbt.contains("Rarity") ? nbt.getString("Rarity") : "NORMAL";
            try {
                return CustomRarity.valueOf(rarityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return NORMAL;
            }
        }

        return NORMAL;
    }
}
