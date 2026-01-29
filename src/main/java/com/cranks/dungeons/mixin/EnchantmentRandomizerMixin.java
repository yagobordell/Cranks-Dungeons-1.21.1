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

        int currentSlot = slotCounter.get();
        slotCounter.set((currentSlot + 1) % 3);

        long stableSeed = stack.hashCode();
        java.util.Collections.shuffle(pool, new java.util.Random(stableSeed));

        int safeIndex = currentSlot % pool.size();
        RegistryEntry<Enchantment> selectedEnchant = pool.get(safeIndex);

        List<net.minecraft.enchantment.EnchantmentLevelEntry> result = new ArrayList<>();
        int maxLvl = selectedEnchant.value().getMaxLevel();
        int enchantedLevel = maxLvl > 1 ? 1 + new java.util.Random(stableSeed + currentSlot).nextInt(maxLvl) : 1;

        result.add(new net.minecraft.enchantment.EnchantmentLevelEntry(selectedEnchant, enchantedLevel));

        cir.setReturnValue(result);
    }
}