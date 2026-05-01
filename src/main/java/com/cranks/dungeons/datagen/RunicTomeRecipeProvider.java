package com.cranks.dungeons.datagen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class RunicTomeRecipeProvider extends FabricRecipeProvider {

    public RunicTomeRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                var itemLookup = wrapperLookup.getOrThrow(RegistryKeys.ITEM);

                createTomeUpgradeRecipe(itemLookup, ModItems.TOME_OF_TRANSMUTATION, ModItems.REGAL_TOME, "transmutation_to_regal");
                createTomeUpgradeRecipe(itemLookup, ModItems.REGAL_TOME, ModItems.TOME_OF_AUGMENTATION, "regal_to_augmentation");
                createTomeUpgradeRecipe(itemLookup, ModItems.TOME_OF_AUGMENTATION, ModItems.TOME_OF_ALCHEMY, "augmentation_to_alchemy");
                createTomeUpgradeRecipe(itemLookup, ModItems.TOME_OF_ALCHEMY, ModItems.EXALTED_TOME, "alchemy_to_exalted");
            }

            private void createTomeUpgradeRecipe(RegistryWrapper.Impl<Item> itemLookup, Item input, Item output, String name) {
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.MISC, output, 1)
                        .pattern("###")
                        .pattern("###")
                        .pattern("###")
                        .input('#', input)
                        .criterion(hasItem(input), conditionsFromItem(input))
                        .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Runic Tome Recipes";
    }
}