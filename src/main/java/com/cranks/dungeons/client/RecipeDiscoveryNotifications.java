package com.cranks.dungeons.client;

import com.cranks.dungeons.alchemy.AlchemyItems;
import com.cranks.dungeons.alchemy.AlchemyPotions;
import com.cranks.dungeons.alchemy.AlchemyRecipes;
import com.cranks.dungeons.component.ItemDiscoveryComponent;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.GearSalvage;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.RecipeDiscovery;
import com.cranks.dungeons.inventory.StationAssemblyRecipe;
import com.cranks.dungeons.mixin.client.RecipeManagerSnapshotAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import java.util.*;
import java.util.function.Predicate;

/** Observes the owner's server-synchronized discovery data, not the vanilla recipe book. */
public final class RecipeDiscoveryNotifications {
    public record Notice(String key, Text name, ItemStack icon, Text station) {}
    private record Candidate(Notice notice, Predicate<ItemDiscoveryComponent> available) {}
    private static final Set<String> seen = new HashSet<>();
    private static final Map<String, Notice> pending = new LinkedHashMap<>();
    private static List<Candidate> catalogue = List.of();
    private static PlayerEntity owner;
    private static World world;
    private static Object recipeSnapshot;
    private static int itemRevision=-1,alchemyRevision=-1,batchTicks;
    private static int generation;
    private RecipeDiscoveryNotifications() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(RecipeDiscoveryNotifications::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->reset());
    }
    public static void reset() {
        generation++;
        owner=null;world=null;recipeSnapshot=null;catalogue=List.of();seen.clear();pending.clear();
        itemRevision=alchemyRevision=-1;batchTicks=0;
    }
    public static void tick(MinecraftClient client) {
        if(client.player==null||client.world==null){if(owner!=null)reset();return;}
        var discoveries=ModComponents.ITEM_DISCOVERIES.get(client.player);
        var knowledge=ModComponents.ALCHEMY_KNOWLEDGE.get(client.player);
        // Wait for both initial component packets, including empty saves. Do not announce login history.
        if(!discoveries.hasLoadedData()||!knowledge.hasLoadedData())return;
        var snapshot=((RecipeManagerSnapshotAccessor)client.world.getRecipeManager()).cranksdungeons$recipeSnapshot();
        if(snapshot.isEmpty())return;
        boolean baseline=owner!=client.player||world!=client.world||recipeSnapshot!=snapshot;
        if(baseline){
            generation++;
            owner=client.player;world=client.world;recipeSnapshot=snapshot;pending.clear();batchTicks=0;seen.clear();
            catalogue=buildCatalogue(client.world,snapshot.values());
            itemRevision=alchemyRevision=-1;
        }
        if(itemRevision!=discoveries.revision()||alchemyRevision!=knowledge.revision()){
            itemRevision=discoveries.revision();alchemyRevision=knowledge.revision();
            for(var candidate:catalogue)if(candidate.available.test(discoveries))discover(candidate.notice,baseline);
            for(var formula:AlchemyPotions.FORMULAS)if(knowledge.knows(formula.key())){
                var icon=formula.stack(AlchemyItems.Delivery.DRINK);
                discover(new Notice("alchemy/formula/"+formula.key(),icon.getName(),icon,Items.BREWING_STAND.getName()),baseline);
            }
        }
        if(batchTicks>0&&--batchTicks==0&&!pending.isEmpty()){
            RecipeDiscoveryToast.show(client.getToastManager(),List.copyOf(pending.values()));pending.clear();
        }
    }
    private static void discover(Notice notice,boolean baseline){
        if(!seen.add(notice.key)||baseline)return;
        if(pending.isEmpty())batchTicks=8;
        pending.put(notice.key,notice);
    }
    private static List<Candidate> buildCatalogue(World world,Collection<RecipeEntry<?>> recipes){
        var result=new ArrayList<Candidate>();
        recipes.stream().sorted(Comparator.comparing(RecipeEntry::id)).forEach(entry->{
            var station=station(entry,world);
            if(station==null)return;
            var icon=MaterialCraftingPanel.recipeIcon(entry,world);
            if(icon.isEmpty())return;
            var notice=new Notice("recipe/"+entry.id(),MaterialCraftingPanel.recipeName(entry,world),icon.copyWithCount(1),station);
            result.add(new Candidate(notice,data->RecipeDiscovery.isDiscovered(entry.value(),data)));
        });
        for(var extraction:AlchemyRecipes.extractions()){
            var icon=extraction.output().copyWithCount(1);
            result.add(new Candidate(new Notice("alchemy/extraction/"+extraction.key(),icon.getName(),icon,Items.BREWING_STAND.getName()),
                    data->extraction.needs().stream().allMatch(need->need.alternatives().stream().anyMatch(data::contains))));
        }
        return List.copyOf(result);
    }
    static int generation(){return generation;}
    private static Text station(RecipeEntry<?> entry,World world){
        var recipe=entry.value();
        if(recipe instanceof StationAssemblyRecipe assembly)return MaterialCrafting.supportsAssembly(recipe,assembly.station(),world)?recipe.createIcon().getName():null;
        if(recipe instanceof CraftingRecipe crafting)return MaterialCrafting.supports(crafting,3,world)
                ?MaterialCrafting.supports(crafting,2,world)?MaterialCraftingPanel.message("hand"):Items.CRAFTING_TABLE.getName():null;
        if(recipe instanceof StonecuttingRecipe)return MaterialCrafting.supportsCutting(recipe,world)?Items.STONECUTTER.getName():null;
        if(recipe instanceof AbstractCookingRecipe && !GearSalvage.isBlastRecipe(entry)){
            var icon=recipe.getResult(world.getRegistryManager());
            if(icon.isEmpty()||!icon.isItemEnabled(world.getEnabledFeatures()))return null;
            var type=recipe.getType();
            if(type==RecipeType.SMELTING||type==RecipeType.BLASTING||type==RecipeType.SMOKING||type==RecipeType.CAMPFIRE_COOKING)return recipe.createIcon().getName();
        }
        return null;
    }
}
