package com.cranks.dungeons.mixin;

import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.component.type.ItemEnchantmentsComponent; // Updated symbol
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilSlotLimitMixin {

    @Shadow @Final private Property levelCost;

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void enforceEnchantmentSlotLimit(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        ItemStack resultStack = handler.getSlot(2).getStack();

        if (resultStack.isEmpty()) {
            return;
        }

        // Apply global filtering first (removes unapproved enchantments)
        com.cranks.dungeons.util.EnchantmentFilterUtil.filterStack(resultStack);
        
        // Re-check visibility after filtering
        if (resultStack.isEmpty()) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
            this.levelCost.set(0);
            return;
        }

        ItemStack leftStack = handler.getSlot(0).getStack();
        ItemStack rightStack = handler.getSlot(1).getStack();

        int maxSlots;

        // Enchanted books are ALWAYS limited to 1 slot
        if (resultStack.isOf(net.minecraft.item.Items.ENCHANTED_BOOK)) {
            maxSlots = 1;
            ItemEnchantmentsComponent stored = resultStack.getOrDefault(net.minecraft.component.DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            if (stored.isEmpty() || stored.getEnchantmentEntries().size() > 1) {
                handler.getSlot(2).setStack(ItemStack.EMPTY);
                this.levelCost.set(0);
                return;
            }
        } else {
            // Inherit the higher slot count when combining items of the same type
            int leftSlots = com.cranks.dungeons.equipment.EnchantmentSlotHelper.getEnchantmentSlots(leftStack);
            int rightSlots = com.cranks.dungeons.equipment.EnchantmentSlotHelper.getEnchantmentSlots(rightStack);
            int maxAllowed = com.cranks.dungeons.equipment.EnchantmentSlotHelper.getMaxEnchantmentSlots(resultStack);
            int inheritedSlots = Math.min(maxAllowed, Math.max(leftSlots, rightSlots));

            if (inheritedSlots > 0) {
                com.cranks.dungeons.equipment.EnchantmentSlotHelper.setEnchantmentSlots(resultStack, inheritedSlots);
            }

            // If leftStack had natural enchantments (e.g. mob drop with 0 slots), allow existing enchantments for repair/rename
            int leftEnchants = leftStack.getEnchantments().getEnchantmentEntries().size();
            maxSlots = Math.max(inheritedSlots, leftEnchants);
        }

        ItemEnchantmentsComponent enchantments = resultStack.getEnchantments();
        int currentEnchantCount = enchantments.getEnchantmentEntries().size();

        if (currentEnchantCount > maxSlots) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
            this.levelCost.set(0);
        }
    }
}