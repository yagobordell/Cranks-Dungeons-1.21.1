package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.GearSalvage;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract class FurnaceSalvageMixin {
    @Inject(method = "canAcceptRecipeOutput", at = @At("HEAD"), cancellable = true)
    private static void checkSalvageSpace(DynamicRegistryManager registries, RecipeEntry<?> recipe,
            DefaultedList<ItemStack> slots, int limit, CallbackInfoReturnable<Boolean> result) {
        if (GearSalvage.isBlastRecipe(recipe)) {
            result.setReturnValue(GearSalvage.fits(GearSalvage.output(slots.getFirst(), true), slots.get(2), limit));
        }
    }

    @Inject(method = "craftRecipe", at = @At("HEAD"), cancellable = true)
    private static void finishSalvage(DynamicRegistryManager registries, RecipeEntry<?> recipe,
            DefaultedList<ItemStack> slots, int limit, CallbackInfoReturnable<Boolean> result) {
        if (!GearSalvage.isBlastRecipe(recipe)) return;
        ItemStack input = slots.getFirst();
        ItemStack recovered = GearSalvage.output(input, true);
        ItemStack existing = slots.get(2);
        boolean fits = GearSalvage.fits(recovered, existing, limit);
        if (fits) {
            if (existing.isEmpty()) slots.set(2, recovered);
            else existing.increment(recovered.getCount());
            input.decrement(1);
        }
        result.setReturnValue(fits);
    }

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void protectHopperInput(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> result) {
        if (slot == 0 && (Object) this instanceof BlastFurnaceBlockEntity && GearSalvage.supports(stack, true)
                && (GearSalvage.needsConfirmation(stack) || GearSalvage.output(stack, true).isEmpty())) {
            result.setReturnValue(false);
        }
    }
}
