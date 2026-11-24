package com.cranks.dungeons.mixin;

import com.cranks.dungeons.alchemy.AlchemyItems;
import com.cranks.dungeons.alchemy.AlchemyPotions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.TippedArrowRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TippedArrowRecipe.class)
abstract class AlchemyArrowRecipeMixin {
    @WrapOperation(method = {"matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z",
            "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean lingeringIngredient(ItemStack stack, Item item, Operation<Boolean> original) {
        if (item == Items.LINGERING_POTION && stack.getItem() instanceof AlchemyItems.Brew brew) {
            return brew.delivery == AlchemyItems.Delivery.LINGERING && !AlchemyPotions.isFailure(stack);
        }
        return original.call(stack, item);
    }
}
