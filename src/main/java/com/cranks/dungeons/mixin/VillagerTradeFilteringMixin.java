package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.EnchantmentFilterUtil;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public class VillagerTradeFilteringMixin {

    @Inject(method = "fillRecipes", at = @At("TAIL"))
    private void dungeons$filterGeneratedTrades(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        TradeOfferList offers = villager.getOffers();
        
        if (offers == null || offers.isEmpty()) return;

        for (TradeOffer offer : offers) {
            // Filter both reward items (the things the villager is selling)
            // Just in case they are selling enchanted items in both slots
            ItemStack result = offer.getSellItem();
            EnchantmentFilterUtil.filterStack(result);
        }
    }
}
