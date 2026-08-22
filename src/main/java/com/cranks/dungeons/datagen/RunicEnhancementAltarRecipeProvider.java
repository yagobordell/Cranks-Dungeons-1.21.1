package com.cranks.dungeons.datagen;

import com.cranks.dungeons.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class RunicEnhancementAltarRecipeProvider extends FabricRecipeProvider {

    public RunicEnhancementAltarRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.RUNIC_ENHANCEMENT_ALTAR, 1)
                .pattern(" B ")
                .pattern("SSS")
                .pattern("SSS")
                .input('B', Items.BOOK)
                .input('S', Blocks.STONE)
                .criterion(hasItem(Items.BOOK), conditionsFromItem(Items.BOOK))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR, 1)
                .pattern("AEA")
                .pattern("NNN")
                .pattern("OOO")
                .input('A', Items.AMETHYST_SHARD)
                .input('E', Items.ENDER_EYE)
                .input('N', Blocks.END_STONE)
                .input('O', Blocks.OBSIDIAN)
                .criterion(hasItem(Items.ENDER_EYE), conditionsFromItem(Items.ENDER_EYE))
                .offerTo(exporter);
    }

    @Override
    public String getName() {
        return "Runic Altar Recipes";
    }
}