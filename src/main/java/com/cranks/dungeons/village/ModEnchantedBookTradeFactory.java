package com.cranks.dungeons.village;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class ModEnchantedBookTradeFactory implements TradeOffers.Factory {
    
    private static final List<RegistryKey<Enchantment>> MOD_ENCHANTMENT_KEYS = new ArrayList<>();
    private static boolean initialized = false;
    
    private static void initializeEnchantmentKeys() {
        if (initialized) return;
        initialized = true;
        
        // List of mod enchantment IDs - all under minecraft namespace
        String[] modEnchantmentIds = {
            "attack_damage", "attack_range", "attack_speed",
            "bonus_armor", "bonus_knockback",
            "breaking_range", "burn_magnitude_percent", "freeze_magnitude_percent",
            "poison_magnitude_percent", "shock_magnitude_percent", "cold_damage",
            "cold_resistance", "crit_chance", "crop_fortune",
            "depth_strider", "dig_fortune", "durability_bonus",
            "energy_shield", "evasion_rating", "experience_bonus",
            "feather_falling_bonus", "fire_damage", "fire_resistance",
            "frost_walker", "knockback_resistance", "life_regeneration",
            "physical_damage_leeched_as_life", "lightning_damage", "lightning_resistance",
            "log_fortune", "loyalty", "luck_bonus", "lunge",
            "max_health_bonus", "mining_efficiency", "movement_speed_bonus",
            "ore_fortune", "repair_chance", "respiration",
            "riptide", "soul_speed", "void_damage", "void_resistance",
            "wind_burst", "electrocute_magnitude_percent", "bleed_magnitude_percent"
        };
        
        // Populate mod enchantment keys list
        for (String id : modEnchantmentIds) {
            try {
                Identifier enchantmentId = Identifier.of("minecraft", id);
                RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, enchantmentId);
                MOD_ENCHANTMENT_KEYS.add(key);
            } catch (Exception e) {
                CranksDungeons.LOGGER.warn("Could not create registry key for enchantment: {}", id);
            }
        }
        
        CranksDungeons.LOGGER.info("Loaded {} mod enchantment keys for villager trades", MOD_ENCHANTMENT_KEYS.size());
    }
    
    private final int minLevel;
    private final int maxLevel;
    private final int basePrice;
    private final int priceMultiplier;
    
    public ModEnchantedBookTradeFactory(int minLevel, int maxLevel, int basePrice, int priceMultiplier) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.basePrice = basePrice;
        this.priceMultiplier = priceMultiplier;
    }
    
    @Override
    public TradeOffer create(Entity entity, Random random) {
        initializeEnchantmentKeys();
        
        if (MOD_ENCHANTMENT_KEYS.isEmpty()) {
            return null;
        }
        
        // Get the enchantment registry from the world
        var enchantmentRegistry = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        
        // Try to find a valid enchantment (some might not be loaded)
        List<RegistryEntry<Enchantment>> availableEnchantments = new ArrayList<>();
        for (RegistryKey<Enchantment> key : MOD_ENCHANTMENT_KEYS) {
            RegistryEntry<Enchantment> entry = enchantmentRegistry.getEntry(key).orElse(null);
            if (entry != null) {
                availableEnchantments.add(entry);
            }
        }
        
        if (availableEnchantments.isEmpty()) {
            return null;
        }
        
        // Pick a random mod enchantment
        RegistryEntry<Enchantment> enchantmentEntry = availableEnchantments.get(random.nextInt(availableEnchantments.size()));
        Enchantment enchantment = enchantmentEntry.value();
        
        // Determine level based on factory settings
        int maxEnchLevel = enchantment.getMaxLevel();
        int level;
        if (maxLevel <= minLevel) {
            level = minLevel;
        } else {
            level = minLevel + random.nextInt(maxLevel - minLevel + 1);
        }
        level = Math.min(level, maxEnchLevel);
        level = Math.max(level, 1);
        
        // Create enchanted book
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.addEnchantment(enchantmentEntry, level);
        
        // Calculate price based on enchantment rarity and level
        int price = basePrice + (level - 1) * priceMultiplier;
        price += random.nextInt(5); // Add some randomness
        
        // Ensure price is reasonable
        price = Math.max(3, Math.min(64, price));
        
        // Create trade: emeralds -> enchanted book
        return new TradeOffer(
            new TradedItem(Items.EMERALD, price),
            book,
            12,  // max uses
            10,  // experience
            0.2f // price multiplier
        );
    }
}
