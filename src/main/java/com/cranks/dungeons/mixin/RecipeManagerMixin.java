package com.cranks.dungeons.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow
    private Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType;

    @Shadow
    private Map<Identifier, RecipeEntry<?>> recipesById;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    private void cranksDungeons$onApply(CallbackInfo ci) {
        cranksDungeons$filterRecipes();
    }

    @Inject(method = "setRecipes", at = @At("RETURN"))
    private void cranksDungeons$onSetRecipes(Iterable<RecipeEntry<?>> recipes, CallbackInfo ci) {
        cranksDungeons$filterRecipes();
    }

    private void cranksDungeons$filterRecipes() {
        ImmutableMap.Builder<Identifier, RecipeEntry<?>> newById = ImmutableMap.builder();
        ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> newByType = ImmutableMultimap.builder();

        for (Map.Entry<Identifier, RecipeEntry<?>> entry : this.recipesById.entrySet()) {
            String path = entry.getKey().getPath();
            RecipeType<?> type = entry.getValue().value().getType();
            
            // Remove all smithing recipes since the Smithing Table is repurposed,
            // and remove all recipes that copy smithing templates/armor trims.
            if (type == RecipeType.SMITHING || path.contains("smithing_template") || path.contains("armor_trim")) {
                continue;
            }

            newById.put(entry.getKey(), entry.getValue());
            newByType.put(type, entry.getValue());
        }

        this.recipesById = newById.build();
        this.recipesByType = newByType.build();
    }
}
