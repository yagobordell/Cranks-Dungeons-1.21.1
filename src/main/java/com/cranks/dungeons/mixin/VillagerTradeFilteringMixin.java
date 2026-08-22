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

        var registryOpt = villager.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
        java.util.List<net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment>> approved = new java.util.ArrayList<>();
        registryOpt.ifPresent(registry -> {
            for (var entry : registry.streamEntries().toList()) {
                if (EnchantmentFilterUtil.isApproved(entry)) {
                    approved.add(entry);
                }
            }
        });

        for (TradeOffer offer : offers) {
            ItemStack result = offer.getSellItem();
            EnchantmentFilterUtil.filterStack(result);

            if (result.isOf(net.minecraft.item.Items.ENCHANTED_BOOK) && !approved.isEmpty()) {
                var stored = result.get(net.minecraft.component.DataComponentTypes.STORED_ENCHANTMENTS);
                if (stored == null || stored.isEmpty()) {
                    var randomEnchant = approved.get(villager.getRandom().nextInt(approved.size()));
                    net.minecraft.component.type.ItemEnchantmentsComponent.Builder builder = 
                            new net.minecraft.component.type.ItemEnchantmentsComponent.Builder(net.minecraft.component.type.ItemEnchantmentsComponent.DEFAULT);
                    builder.add(randomEnchant, 1);
                    result.set(net.minecraft.component.DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
                }
            }
        }
    }
}
