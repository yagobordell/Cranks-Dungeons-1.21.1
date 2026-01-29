package com.cranks.dungeons.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(EnchantmentHelper.class)
public class EnchantmentRandomizerMixin {

    @Unique
    private static final ThreadLocal<Integer> slotCounter = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "generateEnchantments", at = @At("HEAD"), cancellable = true)
    private static void forceRandomEnchantments(Random random, ItemStack stack, int level, java.util.stream.Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<net.minecraft.enchantment.EnchantmentLevelEntry>> cir) {

        List<RegistryEntry<Enchantment>> pool = possibleEnchantments
                .filter(enchant -> enchant.value().isAcceptableItem(stack))
                .collect(Collectors.toCollection(ArrayList::new));

        if (pool.isEmpty()) return;

        // 1. STABLE SEEDING
        // We use a fixed seed so the shuffle is ALWAYS the same for this item.
        long stableSeed = stack.hashCode();
        java.util.Collections.shuffle(pool, new java.util.Random(stableSeed));

        // 2. CONSISTENT INDEXING
        // 'level' is the bookshelf-based power level (e.g., 2, 8, 30).
        // Because Slot 1, 2, and 3 always have different power levels,
        // they will always pick different, consistent indices from the pool.
        int index = Math.abs(level) % pool.size();
        RegistryEntry<Enchantment> selectedEnchant = pool.get(index);

        // 3. GENERATE RESULT
        List<net.minecraft.enchantment.EnchantmentLevelEntry> result = new ArrayList<>();
        int maxLvl = selectedEnchant.value().getMaxLevel();

        // We use the level here too to keep the enchantment level consistent
        int enchantedLevel = maxLvl > 1 ? 1 + (Math.abs(level * 31) % maxLvl) : 1;

        result.add(new net.minecraft.enchantment.EnchantmentLevelEntry(selectedEnchant, enchantedLevel));

        cir.setReturnValue(result);
    }
}