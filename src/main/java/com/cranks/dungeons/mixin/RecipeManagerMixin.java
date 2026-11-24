package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.AnvilRecipes;
import com.cranks.dungeons.inventory.CuttingBenchRecipes;
import com.cranks.dungeons.inventory.HeatStationRecipes;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RepairItemRecipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow @Final
    private RegistryWrapper.WrapperLookup registryLookup;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    private void cranksDungeons$onApply(Map<Identifier, com.google.gson.JsonElement> recipes,
                                       net.minecraft.resource.ResourceManager resources,
                                       net.minecraft.util.profiler.Profiler profiler, CallbackInfo ci) {
        cranksDungeons$filterRecipes(resources);
    }

    @Inject(method = "setRecipes", at = @At("RETURN"))
    private void cranksDungeons$onSetRecipes(Iterable<RecipeEntry<?>> recipes, CallbackInfo ci) {
        cranksDungeons$filterRecipes(null);
    }

    private void cranksDungeons$filterRecipes(net.minecraft.resource.ResourceManager resources) {
        ImmutableMap.Builder<Identifier, RecipeEntry<?>> newById = ImmutableMap.builder();
        ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> newByType = ImmutableMultimap.builder();

        var heatRecipes = HeatStationRecipes.route(recipesById.values(), registryLookup);
        for (RecipeEntry<?> original : com.cranks.dungeons.inventory.TailoringRecipes.route(
                com.cranks.dungeons.inventory.CartographyRecipes.route(
                        CuttingBenchRecipes.route(heatRecipes, registryLookup), registryLookup), registryLookup, resources)) {
            RecipeEntry<?> routed = com.cranks.dungeons.inventory.FletchingRecipes.assign(original, registryLookup);
            if (routed == null) continue;
            RecipeEntry<?> entry = AnvilRecipes.assign(routed, registryLookup);
            String path = entry.id().getPath();
            RecipeType<?> type = entry.value().getType();
            
            // Remove all smithing recipes since the Smithing Table is repurposed,
            // and remove all recipes that copy smithing templates/armor trims.
            if (type == RecipeType.SMITHING || entry.value() instanceof RepairItemRecipe
                    || path.contains("smithing_template") || path.contains("armor_trim")) {
                continue;
            }
            newById.put(entry.id(), entry);
            newByType.put(type, entry);
        }

        this.recipesById = newById.build();
        this.recipesByType = newByType.build();
        com.cranks.dungeons.progression.WorldDiscovery.invalidate((RecipeManager)(Object)this);
    }
}
