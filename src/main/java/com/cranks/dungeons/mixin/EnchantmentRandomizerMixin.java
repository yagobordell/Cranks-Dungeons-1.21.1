package com.cranks.dungeons.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(EnchantmentHelper.class)
public class EnchantmentRandomizerMixin {

    @Inject(method = "generateEnchantments", at = @At("HEAD"), cancellable = true)
    private static void forceRandomEnchantments(Random random, ItemStack stack, int level,
                                                java.util.stream.Stream<RegistryEntry<Enchantment>> possibleEnchantments,
                                                CallbackInfoReturnable<List<net.minecraft.enchantment.EnchantmentLevelEntry>> cir) {

        List<RegistryEntry<Enchantment>> pool = possibleEnchantments
                .filter(enchant -> enchant.value().isAcceptableItem(stack))
                .collect(Collectors.toCollection(ArrayList::new));

        if (pool.isEmpty()) return;

        long seed = ((long) stack.hashCode() * 31L + level) * 48271L;
        java.util.Random rng = new java.util.Random(seed);

        java.util.Collections.shuffle(pool, rng);

        int slotOffset = (level / 5) % 3;

        int baseIndex = slotOffset % pool.size();

        int index;
        if (pool.size() >= 3) {
            index = baseIndex;
        } else {
            index = (slotOffset * 13) % pool.size();
        }

        RegistryEntry<Enchantment> selectedEnchant = pool.get(index);

        List<net.minecraft.enchantment.EnchantmentLevelEntry> result = new ArrayList<>();
        int maxLvl = selectedEnchant.value().getMaxLevel();
        int enchantedLevel = maxLvl > 1 ? 1 + (rng.nextInt(maxLvl)) : 1;

        result.add(new net.minecraft.enchantment.EnchantmentLevelEntry(selectedEnchant, enchantedLevel));

        cir.setReturnValue(result);
    }
}