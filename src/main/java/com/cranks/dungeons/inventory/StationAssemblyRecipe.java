package com.cranks.dungeons.inventory;

import com.cranks.dungeons.CranksDungeons;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/** A shaped or shapeless recipe assigned to a specific station. */
public record StationAssemblyRecipe(Station station, CraftingRecipe assembly) implements Recipe<CraftingRecipeInput> {
    public enum Station {
        CUTTING_BENCH("cutting_bench_assembly", Items.STONECUTTER),
        ANVIL("anvil_assembly", Items.ANVIL),
        FLETCHING_TABLE("fletching_assembly", Items.FLETCHING_TABLE),
        TAILORING_BENCH("tailoring_assembly", Items.LOOM),
        CARTOGRAPHY_TABLE("cartography_assembly", Items.CARTOGRAPHY_TABLE);

        private final Identifier id;
        private final Item icon;
        private final RecipeType<StationAssemblyRecipe> type;
        private final RecipeSerializer<StationAssemblyRecipe> serializer;

        Station(String path, Item icon) {
            id = Identifier.of(CranksDungeons.MOD_ID, path);
            this.icon = icon;
            type = new RecipeType<>() {
                @Override
                public String toString() { return id.toString(); }
            };
            serializer = new RecipeSerializer<>() {
                @Override
                public MapCodec<StationAssemblyRecipe> codec() {
                    return Recipe.CODEC.comapFlatMap(Station.this::decode, StationAssemblyRecipe::assembly).fieldOf("recipe");
                }

                @Override
                public PacketCodec<RegistryByteBuf, StationAssemblyRecipe> packetCodec() {
                    return Recipe.PACKET_CODEC.xmap(recipe -> decode(recipe).getOrThrow(), StationAssemblyRecipe::assembly);
                }
            };
        }

        public RecipeType<StationAssemblyRecipe> type() { return type; }

        private DataResult<StationAssemblyRecipe> decode(Recipe<?> recipe) {
            if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe
                    || this == TAILORING_BENCH && (recipe instanceof net.minecraft.recipe.ArmorDyeRecipe
                    || recipe instanceof net.minecraft.recipe.BannerDuplicateRecipe
                    || recipe instanceof net.minecraft.recipe.ShieldDecorationRecipe || recipe instanceof TailoringPatternRecipe)
                    || this == CARTOGRAPHY_TABLE && recipe instanceof CartographyRecipe) {
                return DataResult.success(new StationAssemblyRecipe(this, (CraftingRecipe) recipe));
            }
            return DataResult.error(() -> id + " requires a supported assembly recipe");
        }
    }

    public static void register() {
        for (Station station : Station.values()) {
            Registry.register(Registries.RECIPE_TYPE, station.id, station.type);
            Registry.register(Registries.RECIPE_SERIALIZER, station.id, station.serializer);
        }
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return assembly.matches(input, world);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return assembly.craft(input, registries);
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return assembly.getIngredients();
    }

    @Override
    public boolean fits(int width, int height) {
        return assembly.fits(width, height);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
        return assembly.getResult(registries);
    }

    @Override
    public ItemStack createIcon() {
        return station.icon.getDefaultStack();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeType<StationAssemblyRecipe> getType() {
        return station.type;
    }

    @Override
    public RecipeSerializer<StationAssemblyRecipe> getSerializer() {
        return station.serializer;
    }
}
