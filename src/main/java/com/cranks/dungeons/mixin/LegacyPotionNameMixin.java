package com.cranks.dungeons.mixin;

import com.cranks.dungeons.alchemy.AlchemyItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Existing loot and saved vanilla potions use the same descriptive tier names. */
@Mixin(PotionItem.class)
abstract class LegacyPotionNameMixin {
    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    private void descriptiveName(ItemStack stack, CallbackInfoReturnable<String> result) {
        if (stack.getItem() instanceof AlchemyItems.Brew) return;
        stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().flatMap(potion -> potion.getKey()).ifPresent(key -> {
            String path = key.getValue().getPath();
            if (key.getValue().getNamespace().equals("minecraft")) {
                String delivery = stack.isOf(net.minecraft.item.Items.SPLASH_POTION) ? "splash" : stack.isOf(net.minecraft.item.Items.LINGERING_POTION) ? "lingering" : "drink";
                String translation = "alchemy.cranks-dungeons.legacy." + delivery + "." + path;
                // The vanilla base potion name also covers strong variants, so use the actual registry key.
                result.setReturnValue(translation);
            }
        });
    }
}
