package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.EnchantmentFilterUtil;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTraderEntity.class)
public class WanderingTraderTradeFilteringMixin {

    @Inject(method = "fillRecipes", at = @At("TAIL"))
    private void dungeons$filterGeneratedTrades(CallbackInfo ci) {
        WanderingTraderEntity trader = (WanderingTraderEntity) (Object) this;
        TradeOfferList offers = trader.getOffers();
        
        if (offers == null || offers.isEmpty()) return;

        for (TradeOffer offer : offers) {
            ItemStack result = offer.getSellItem();
            EnchantmentFilterUtil.filterStack(result);
        }
    }
}
