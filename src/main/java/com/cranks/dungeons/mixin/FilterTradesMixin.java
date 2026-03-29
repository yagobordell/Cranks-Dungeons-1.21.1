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
    
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onStaticInit(CallbackInfo ci) {
        // Clear all librarian enchanted book trades and add custom ones
        var librarianTrades = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.LIBRARIAN);
        if (librarianTrades != null) {
            librarianTrades.forEach((level, trades) -> {
                if (trades != null) {
                    List<TradeOffers.Factory> filteredTrades = new ArrayList<>();
                    for (TradeOffers.Factory trade : trades) {
                        // Filter out ALL enchanted book trades by checking class name
                        // This catches SellEnchantedToolFactory, EnchantBookFactory, and any other enchanted book trades
                        String tradeClassName = trade.getClass().getSimpleName().toLowerCase();
                        boolean isEnchantedBookTrade = tradeClassName.contains("enchant") && tradeClassName.contains("book");
                        if (!isEnchantedBookTrade) {
                            filteredTrades.add(trade);
                        }
                    }
                    librarianTrades.put(level, filteredTrades.toArray(new TradeOffers.Factory[0]));
                }
            });
            
            // Add custom mod enchantment trades at various levels
            // Level 1: Basic enchantments (levels 1-2)
            addTrade(librarianTrades, 1, new ModEnchantedBookTradeFactory(1, 2, 5, 3));
            addTrade(librarianTrades, 1, new ModEnchantedBookTradeFactory(1, 1, 3, 2));
            
            // Level 2: Low-mid tier enchantments (levels 1-3)
            addTrade(librarianTrades, 2, new ModEnchantedBookTradeFactory(1, 3, 8, 4));
            addTrade(librarianTrades, 2, new ModEnchantedBookTradeFactory(2, 3, 10, 5));
            
            // Level 3: Mid tier enchantments (levels 2-4)
            addTrade(librarianTrades, 3, new ModEnchantedBookTradeFactory(2, 4, 12, 5));
            addTrade(librarianTrades, 3, new ModEnchantedBookTradeFactory(2, 3, 14, 6));
            
            // Level 4: High tier enchantments (levels 3-5)
            addTrade(librarianTrades, 4, new ModEnchantedBookTradeFactory(3, 5, 18, 7));
            addTrade(librarianTrades, 4, new ModEnchantedBookTradeFactory(3, 4, 20, 8));
            
            // Level 5: Max tier enchantments (levels 4-max)
            addTrade(librarianTrades, 5, new ModEnchantedBookTradeFactory(4, 10, 25, 10));
            addTrade(librarianTrades, 5, new ModEnchantedBookTradeFactory(5, 10, 30, 12));
        }
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
