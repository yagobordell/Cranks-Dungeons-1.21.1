package com.cranks.dungeons.village;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class ModEnchantedBookTradeFactory implements TradeOffers.Factory {
    
    public enum Tier {
        STANDARD,
        GREATER,
        PERFECT,
        ANY
    }

    private final Tier tier;
    private final int minPrice;
    private final int maxPrice;
    
    public ModEnchantedBookTradeFactory(Tier tier, int minPrice, int maxPrice) {
        this.tier = tier;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    @Override
    public TradeOffer create(Entity entity, Random random) {
        var enchantmentRegistry = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        
        List<RegistryEntry<Enchantment>> pool = new ArrayList<>();
        enchantmentRegistry.streamEntries().forEach(entry -> {
            if (entry.isIn(EnchantmentTags.IN_ENCHANTING_TABLE) || entry.isIn(EnchantmentTags.ON_RANDOM_LOOT)) {
                String path = entry.getKey().map(k -> k.getValue().getPath()).orElse("");
                boolean isGreater = path.startsWith("greater_");
                boolean isPerfect = path.startsWith("perfect_");
                boolean isStandard = !isGreater && !isPerfect;
                
                if (tier == Tier.PERFECT && isPerfect) {
                    pool.add(entry);
                } else if (tier == Tier.GREATER && isGreater) {
                    pool.add(entry);
                } else if (tier == Tier.STANDARD && isStandard) {
                    pool.add(entry);
                } else if (tier == Tier.ANY) {
                    pool.add(entry);
                }
            }
        });
        
        if (pool.isEmpty()) {
            return null;
        }
        
        RegistryEntry<Enchantment> chosen = pool.get(random.nextInt(pool.size()));
        
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(chosen, 1);
        
        int price = minPrice + (maxPrice > minPrice ? random.nextInt(maxPrice - minPrice + 1) : 0);
        price = Math.max(1, Math.min(64, price));
        
        return new TradeOffer(
            new TradedItem(Items.EMERALD, price),
            book,
            12,  // max uses
            10,  // experience
            0.2f // price multiplier
        );
    }
}
