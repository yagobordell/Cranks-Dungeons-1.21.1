package com.cranks.dungeons.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
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

    private static final String ITEM_SEED_KEY = "EnchantItemSeed";
    private static final String LEVEL_TO_INDEX_KEY = "LevelMapping_";

    @Inject(method = "generateEnchantments", at = @At("HEAD"), cancellable = true)
    private static void forceRandomEnchantments(Random random, ItemStack stack, int level,
                                                java.util.stream.Stream<RegistryEntry<Enchantment>> possibleEnchantments,
                                                CallbackInfoReturnable<List<net.minecraft.enchantment.EnchantmentLevelEntry>> cir) {

        List<RegistryEntry<Enchantment>> pool = possibleEnchantments
                .filter(enchant -> enchant.value().isAcceptableItem(stack))
                .collect(Collectors.toCollection(ArrayList::new));

        if (pool.isEmpty()) return;

        // Get or create NBT data for this item
        NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = nbtComponent.copyNbt();

        long itemSeed;

        if (nbt.contains(ITEM_SEED_KEY)) {
            itemSeed = nbt.getLong(ITEM_SEED_KEY).orElse(System.nanoTime() ^ (long) stack.hashCode());
        } else {
            // Generate a unique seed for this item and store it permanently
            itemSeed = System.nanoTime() ^ (long) stack.hashCode();
            nbt.putLong(ITEM_SEED_KEY, itemSeed);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }

        // Use the stored item seed (without level) for shuffling - consistent across all slots
        long seed = itemSeed * 48271L;
        java.util.Random rng = new java.util.Random(seed);

        java.util.Collections.shuffle(pool, rng);

        // Check if we already have a mapping for this level
        String levelKey = LEVEL_TO_INDEX_KEY + level;
        int index;

        if (nbt.contains(levelKey)) {
            index = nbt.getInt(levelKey).orElse(0);
        } else {
            // Find the next available index not already assigned to another level
            List<Integer> assignedIndices = new ArrayList<>();
            for (String key : nbt.getKeys()) {
                if (key.startsWith(LEVEL_TO_INDEX_KEY)) {
                    assignedIndices.add(nbt.getInt(key).orElse(-1));
                }
            }

            // Find first unassigned index
            index = 0;
            while (assignedIndices.contains(index) && index < pool.size()) {
                index++;
            }

            // If we've run out of indices, wrap around
            if (index >= pool.size()) {
                index = index % pool.size();
            }

            // Store this mapping permanently
            nbt.putInt(levelKey, index);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }

        RegistryEntry<Enchantment> selectedEnchant = pool.get(index);

        List<net.minecraft.enchantment.EnchantmentLevelEntry> result = new ArrayList<>();
        int maxLvl = selectedEnchant.value().getMaxLevel();
        int enchantedLevel = maxLvl > 1 ? 1 + (rng.nextInt(maxLvl)) : 1;

        result.add(new net.minecraft.enchantment.EnchantmentLevelEntry(selectedEnchant, enchantedLevel));

        cir.setReturnValue(result);
    }
}