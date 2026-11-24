package com.cranks.dungeons.inventory;

import com.cranks.dungeons.CranksDungeons;
import com.mojang.serialization.MapCodec;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/** Vanilla cartography operations with an explicit map choice and automatic material input. */
public final class CartographyRecipe extends SpecialCraftingRecipe {
    public enum Operation implements StringIdentifiable {
        COPY(Items.MAP), EXPAND(Items.PAPER), LOCK(Items.GLASS_PANE);
        private final Item material;
        Operation(Item material) { this.material = material; }
        public Item material() { return material; }
        @Override public String asString() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    private static final MapCodec<CartographyRecipe> CODEC = StringIdentifiable.createCodec(Operation::values)
            .fieldOf("operation").xmap(CartographyRecipe::new, CartographyRecipe::operation);
    public static final RecipeSerializer<CartographyRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override public MapCodec<CartographyRecipe> codec() { return CODEC; }
        @Override public PacketCodec<RegistryByteBuf, CartographyRecipe> packetCodec() {
            return PacketCodecs.STRING.<RegistryByteBuf>cast().xmap(value -> new CartographyRecipe(Operation.valueOf(value.toUpperCase(java.util.Locale.ROOT))),
                    recipe -> recipe.operation.asString());
        }
    };
    private final Operation operation;
    public CartographyRecipe(Operation operation) { super(CraftingRecipeCategory.MISC); this.operation = operation; }
    public Operation operation() { return operation; }
    public static void register() {
        Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(CranksDungeons.MOD_ID, "cartography_operation"), SERIALIZER);
    }
    @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
    @Override public boolean fits(int width, int height) { return width * height >= 2; }
    @Override public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.FILLED_MAP), Ingredient.ofItems(operation.material));
    }
    @Override public boolean matches(CraftingRecipeInput input, World world) {
        var map = selectedMap(input);
        if (map.isEmpty()) return false;
        var state = FilledMapItem.getMapState(map, world);
        if (state == null) return false;
        return switch (operation) {
            case COPY -> true;
            case EXPAND -> !state.locked && state.scale < 4;
            case LOCK -> !state.locked;
        };
    }
    private ItemStack selectedMap(CraftingRecipeInput input) {
        ItemStack map = ItemStack.EMPTY;
        boolean material = false;
        for (var stack : input.getStacks()) {
            if (stack.isEmpty()) continue;
            if (stack.isOf(Items.FILLED_MAP) && map.isEmpty() && stack.contains(DataComponentTypes.MAP_ID)
                    && !stack.contains(DataComponentTypes.MAP_POST_PROCESSING)) map = stack;
            else if (stack.isOf(operation.material) && !material) material = true;
            else return ItemStack.EMPTY;
        }
        return material ? map : ItemStack.EMPTY;
    }
    @Override public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        var map = selectedMap(input);
        if (map.isEmpty()) return ItemStack.EMPTY;
        var output = map.copyWithCount(1);
        if (operation != Operation.COPY) output.set(DataComponentTypes.MAP_POST_PROCESSING,
                operation == Operation.EXPAND ? MapPostProcessingComponent.SCALE : MapPostProcessingComponent.LOCK);
        return output;
    }
    @Override public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput input) {
        var remainder = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        if (operation == Operation.COPY) for (int i = 0; i < input.getSize(); i++) {
            if (input.getStackInSlot(i).isOf(Items.FILLED_MAP)) remainder.set(i, input.getStackInSlot(i).copyWithCount(1));
        }
        return remainder;
    }
}
