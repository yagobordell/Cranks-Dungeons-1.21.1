package com.cranks.dungeons.mixin;

import com.cranks.dungeons.village.ModEnchantedBookTradeFactory;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(TradeOffers.class)
public class FilterTradesMixin {
    
    // Equipment types to check for in enchanted trade names
    private static final String[] ENCHANTED_EQUIPMENT_TYPES = {
        "sword", "axe", "pickaxe", "shovel", "hoe", "spade",
        "armor", "helmet", "chestplate", "leggings", "boots",
        "bow", "crossbow", "trident", "rod", "fishing",
        "shield", "shears", "flint", "steel"
    };
    
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onStaticInit(CallbackInfo ci) {
        // Process ALL villager professions to remove enchanted equipment trades
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.forEach((profession, trades) -> {
            if (trades == null) return;
            
            boolean isLibrarian = profession.getValue().getPath().equals("librarian");
            
            trades.forEach((level, tradeArray) -> {
                if (tradeArray == null) return;
                
                List<TradeOffers.Factory> filteredTrades = new ArrayList<>();
                
                for (TradeOffers.Factory trade : tradeArray) {
                    String tradeClassName = trade.getClass().getSimpleName().toLowerCase();
                    
                    // For Librarian: only filter enchanted BOOK trades, keep custom books later
                    if (isLibrarian) {
                        boolean isEnchantedBookTrade = tradeClassName.contains("enchant") && tradeClassName.contains("book");
                        if (!isEnchantedBookTrade) {
                            filteredTrades.add(trade);
                        }
                        continue;
                    }
                    
                    // For ALL other professions: filter ANY enchanted equipment
                    // Check if trade involves enchanted equipment
                    if (isEnchantedEquipmentTrade(tradeClassName, profession)) {
                        // Skip this trade (remove enchanted equipment)
                        continue;
                    }
                    
                    // Keep all other trades (unenchanted equipment, item trades, etc.)
                    filteredTrades.add(trade);
                }
                
                trades.put(level, filteredTrades.toArray(new TradeOffers.Factory[0]));
            });
            
            // Add custom mod enchantment book trades only for Librarian
            if (isLibrarian) {
                addCustomLibrarianTrades(trades);
            }
        });
    }
    
    /**
     * Check if a trade is likely for enchanted equipment.
     */
    private static boolean isEnchantedEquipmentTrade(String tradeClassName, net.minecraft.registry.RegistryKey<VillagerProfession> profession) {
        String professionPath = profession.getValue().getPath();
        
        // If it's an anonymous class, simplify name will be empty.
        // In that case, we should be more aggressive if it's a combat/tool profession.
        if (tradeClassName.isEmpty()) {
            return professionPath.equals("weaponsmith") || 
                   professionPath.equals("armorer") || 
                   professionPath.equals("toolsmith");
        }

        // Check for common keywords in vanilla and modded factories
        if (tradeClassName.contains("enchant") || 
            tradeClassName.contains("sellenchanted") || 
            tradeClassName.contains("processenchanted")) {
            
            // It involves enchantment. Now check if it's equipment.
            for (String equipmentType : ENCHANTED_EQUIPMENT_TYPES) {
                if (tradeClassName.contains(equipmentType)) {
                    return true;
                }
            }
            
            // Also check for generic "tool", "weapon", "armor" trades
            if (tradeClassName.contains("tool") || 
                tradeClassName.contains("weapon") || 
                tradeClassName.contains("armor") || 
                tradeClassName.contains("armour")) {
                return !tradeClassName.contains("book");
            }
        }
        
        return false;
    }
    
    /**
     * Add custom mod enchantment book trades to Librarian
     */
    private static void addCustomLibrarianTrades(Int2ObjectMap<TradeOffers.Factory[]> trades) {
        // Level 1: Basic enchantments (levels 1-2)
        addTrade(trades, 1, new ModEnchantedBookTradeFactory(1, 2, 5, 3));
        addTrade(trades, 1, new ModEnchantedBookTradeFactory(1, 1, 3, 2));
        
        // Level 2: Low-mid tier enchantments (levels 1-3)
        addTrade(trades, 2, new ModEnchantedBookTradeFactory(1, 3, 8, 4));
        addTrade(trades, 2, new ModEnchantedBookTradeFactory(2, 3, 10, 5));
        
        // Level 3: Mid tier enchantments (levels 2-4)
        addTrade(trades, 3, new ModEnchantedBookTradeFactory(2, 4, 12, 5));
        addTrade(trades, 3, new ModEnchantedBookTradeFactory(2, 3, 14, 6));
        
        // Level 4: High tier enchantments (levels 3-5)
        addTrade(trades, 4, new ModEnchantedBookTradeFactory(3, 5, 18, 7));
        addTrade(trades, 4, new ModEnchantedBookTradeFactory(3, 4, 20, 8));
        
        // Level 5: Max tier enchantments (levels 4-max)
        addTrade(trades, 5, new ModEnchantedBookTradeFactory(4, 10, 25, 10));
        addTrade(trades, 5, new ModEnchantedBookTradeFactory(5, 10, 30, 12));
    }
    
    private static void addTrade(Int2ObjectMap<TradeOffers.Factory[]> trades, int level, TradeOffers.Factory factory) {
        TradeOffers.Factory[] existing = trades.get(level);
        if (existing == null) {
            trades.put(level, new TradeOffers.Factory[]{factory});
        } else {
            TradeOffers.Factory[] newTrades = Arrays.copyOf(existing, existing.length + 1);
            newTrades[existing.length] = factory;
            trades.put(level, newTrades);
        }
    }
}
