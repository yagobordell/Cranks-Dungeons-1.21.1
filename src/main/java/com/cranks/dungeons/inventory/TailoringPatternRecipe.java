package com.cranks.dungeons.inventory;

import com.cranks.dungeons.CranksDungeons;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;

/** A selected loom pattern; the banner and dye are consumed, its template is returned. */
public final class TailoringPatternRecipe extends SpecialCraftingRecipe {
    private static final MapCodec<TailoringPatternRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("pattern").forGetter(TailoringPatternRecipe::pattern),
            Ingredient.ALLOW_EMPTY_CODEC.optionalFieldOf("template", Ingredient.EMPTY).forGetter(TailoringPatternRecipe::template)
    ).apply(instance, TailoringPatternRecipe::new));
    public static final RecipeSerializer<TailoringPatternRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override public MapCodec<TailoringPatternRecipe> codec() { return CODEC; }
        @Override public PacketCodec<RegistryByteBuf, TailoringPatternRecipe> packetCodec() {
            return PacketCodec.tuple(Identifier.PACKET_CODEC, TailoringPatternRecipe::pattern,
                    Ingredient.PACKET_CODEC, TailoringPatternRecipe::template, TailoringPatternRecipe::new);
        }
    };
    private final Identifier pattern;
    private final Ingredient template;

    public TailoringPatternRecipe(Identifier pattern, Ingredient template) {
        super(CraftingRecipeCategory.MISC);
        this.pattern = pattern;
        this.template = template;
    }
    public Identifier pattern() { return pattern; }
    public Ingredient template() { return template; }
    public static void register() {
        Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(CranksDungeons.MOD_ID, "tailoring_pattern"), SERIALIZER);
    }
    @Override public boolean fits(int width, int height) { return width * height >= (template.isEmpty() ? 2 : 3); }
    @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }

    @Override public DefaultedList<Ingredient> getIngredients() {
        var ingredients = DefaultedList.<Ingredient>of();
        ingredients.add(Ingredient.fromTag(ItemTags.BANNERS));
        ingredients.add(Ingredient.ofItems(Registries.ITEM.stream().filter(DyeItem.class::isInstance).toArray(net.minecraft.item.Item[]::new)));
        if (!template.isEmpty()) ingredients.add(template);
        return ingredients;
    }
    @Override public boolean matches(CraftingRecipeInput input, World world) {
        return !craft(input, world.getRegistryManager()).isEmpty();
    }
    @Override public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack banner = ItemStack.EMPTY;
        DyeItem dye = null;
        boolean foundTemplate = template.isEmpty();
        for (var stack : input.getStacks()) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof BannerItem && banner.isEmpty()) banner = stack;
            else if (stack.getItem() instanceof DyeItem color && dye == null) dye = color;
            else if (!foundTemplate && template.test(stack)) foundTemplate = true;
            else return ItemStack.EMPTY;
        }
        if (banner.isEmpty() || dye == null || !foundTemplate) return ItemStack.EMPTY;
        var layers = banner.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers();
        if (layers.size() >= 6) return ItemStack.EMPTY;
        var entry = registries.getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)
                .getOptional(RegistryKey.of(RegistryKeys.BANNER_PATTERN, pattern)).orElse(null);
        if (entry == null) return ItemStack.EMPTY;
        var next = new ArrayList<>(layers);
        next.add(new BannerPatternsComponent.Layer(entry, dye.getColor()));
        var result = banner.copyWithCount(1);
        result.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(next));
        return result;
    }
    @Override public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput input) {
        var remainder = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        if (!template.isEmpty()) for (int i = 0; i < input.getSize(); i++) {
            if (template.test(input.getStackInSlot(i))) remainder.set(i, input.getStackInSlot(i).copyWithCount(1));
        }
        return remainder;
    }
    public ItemStack icon(RegistryWrapper.WrapperLookup registries) {
        var entry = registries.getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)
                .getOptional(RegistryKey.of(RegistryKeys.BANNER_PATTERN, pattern)).orElse(null);
        var icon = Items.WHITE_BANNER.getDefaultStack();
        if (entry != null) icon.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(
                java.util.List.of(new BannerPatternsComponent.Layer(entry, net.minecraft.util.DyeColor.BLACK))));
        return icon;
    }
    public net.minecraft.text.Text title(RegistryWrapper.WrapperLookup registries) {
        return registries.getWrapperOrThrow(RegistryKeys.BANNER_PATTERN)
                .getOptional(RegistryKey.of(RegistryKeys.BANNER_PATTERN, pattern))
                .<net.minecraft.text.Text>map(entry -> net.minecraft.text.Text.translatable(entry.value().translationKey() + ".black"))
                .orElseGet(() -> net.minecraft.text.Text.literal(pattern.getPath().replace('_', ' ')));
    }
}
