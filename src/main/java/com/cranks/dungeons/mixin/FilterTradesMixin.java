package com.cranks.dungeons.mixin;

import com.cranks.dungeons.village.ModEnchantedBookTradeFactory;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(TradeOffers.class)
public class FilterTradesMixin {
    
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onStaticInit(CallbackInfo ci) {
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.forEach((profession, trades) -> {
            boolean librarian = profession == VillagerProfession.LIBRARIAN;
            trades.replaceAll((level, factories) -> Arrays.stream(factories)
                    .filter(factory -> !(factory instanceof TradeOffers.SellEnchantedToolFactory))
                    .filter(factory -> !librarian || !(factory instanceof TradeOffers.EnchantBookFactory))
                    .toArray(TradeOffers.Factory[]::new));
            if (librarian) addCustomLibrarianTrades(trades);
        });
    }

    /**
     * Add custom mod enchantment book trades to Librarian
     */
    private static void addCustomLibrarianTrades(Int2ObjectMap<TradeOffers.Factory[]> trades) {
        // Level 1: Standard enchantments (5-12 emeralds)
        addTrade(trades, 1, new ModEnchantedBookTradeFactory(ModEnchantedBookTradeFactory.Tier.LESSER, 5, 12));
        
        // Level 2: Standard enchantments (8-16 emeralds)
        addTrade(trades, 2, new ModEnchantedBookTradeFactory(ModEnchantedBookTradeFactory.Tier.LESSER, 8, 16));
        
        // Level 3: Greater enchantments (15-28 emeralds)
        addTrade(trades, 3, new ModEnchantedBookTradeFactory(ModEnchantedBookTradeFactory.Tier.STANDARD, 15, 28));
        
        // Level 4: Greater enchantments (22-40 emeralds)
        addTrade(trades, 4, new ModEnchantedBookTradeFactory(ModEnchantedBookTradeFactory.Tier.STANDARD, 22, 40));
        
        // Level 5: Perfect enchantments (35-64 emeralds)
        addTrade(trades, 5, new ModEnchantedBookTradeFactory(ModEnchantedBookTradeFactory.Tier.STANDARD, 22, 40));
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
