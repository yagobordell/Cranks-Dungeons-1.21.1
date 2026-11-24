package com.cranks.dungeons.progression;

import com.cranks.dungeons.component.*;
import com.cranks.dungeons.config.ProgressionConfig;
import com.cranks.dungeons.inventory.*;
import com.cranks.dungeons.alchemy.AlchemyRecipes;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.*;

/** All discovery claims and eligibility checks run on the server. No block coordinate history is needed. */
public final class WorldDiscovery {
    private record Candidate(String key,Recipe<?> recipe) {}
    private static final Map<RecipeManager,List<Candidate>> catalogues=new WeakHashMap<>();
    private static final Map<ServerPlayerEntity,Scan> scans=new WeakHashMap<>();
    private static final class Scan { int revision=-1; List<Candidate> catalogue; final Map<String,Integer> visits=new HashMap<>(); }
    private WorldDiscovery() {}
    public static void invalidate(RecipeManager manager) { catalogues.remove(manager); }
    public static void clear() { catalogues.clear(); scans.clear(); }
    public static void baseline(ServerPlayerEntity player) { scan(player,true); }
    public static void tick(ServerPlayerEntity player) {
        if(!player.isAlive()||player.isSpectator()||player.isCreative())return;
        scan(player,false);
        explore(player);
    }
    public static void firstAnimal(ServerPlayerEntity player,LivingEntity animal,String activity) {
        if(player==null||player.isCreative()||player.isSpectator()||!player.isAlive())return;
        var bonus=activity.equals("breed")?ModAttributes.BREEDING_EXPERIENCE:null;
        award(player,activity+"/"+Registries.ENTITY_TYPE.getId(animal.getType()),ProgressionConfig.RULES.firstAnimalXp,bonus,false);
    }
    public static void firstFish(ServerPlayerEntity player,net.minecraft.item.ItemStack fish) {
        if(fish.isIn(net.minecraft.registry.tag.ItemTags.FISHES))award(player,"fish/"+Registries.ITEM.getId(fish.getItem()),
                ProgressionConfig.RULES.firstAnimalXp,ModAttributes.FISHING_EXPERIENCE,false);
    }
    public static void formula(ServerPlayerEntity player,String formula) {
        award(player,"formula/"+formula,ProgressionConfig.RULES.recipeXp*2,null,false);
    }
    public static void award(ServerPlayerEntity player,String key,int amount,RegistryEntry<EntityAttribute> source,boolean baseline) {
        var data=ModComponents.PROGRESSION.get(player);
        if(!data.initialized())return;
        if(!data.claim(key)||baseline||player.isCreative()||player.isSpectator()||!player.isAlive())return;
        double bonus=player.getAttributeValue(ModAttributes.WORLD_DISCOVERY_EXPERIENCE)+player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS)+(source==null?0:player.getAttributeValue(source));
        data.addWorld(ProgressionMath.boosted(amount*ProgressionMath.UNIT,bonus));
    }
    private static void scan(ServerPlayerEntity player,boolean baseline) {
        var discovery=ModComponents.ITEM_DISCOVERIES.get(player);
        var scan=scans.computeIfAbsent(player,p->new Scan());
        var manager=player.getWorld().getRecipeManager();
        var catalogue=catalogues.computeIfAbsent(manager,m->catalogue(player));
        if(!baseline && scan.revision==discovery.revision() && scan.catalogue==catalogue)return;
        scan.revision=discovery.revision(); scan.catalogue=catalogue;
        var rules=ProgressionConfig.RULES;
        for(var item:discovery.knownIds()) {
            String family=DiscoveryFamilies.resource(item.toString());
            if(family==null)continue;
            RegistryEntry<EntityAttribute> source=family.startsWith("mineral/")?ModAttributes.MINING_EXPERIENCE:
                family.startsWith("crop/")?ModAttributes.CROP_EXPERIENCE:null;
            award(player,family,rules.resourceXp,source,baseline);
        }
        for(var candidate:catalogue)if(RecipeDiscovery.isDiscovered(candidate.recipe,discovery))
            award(player,"recipe/"+candidate.key,rules.recipeXp,null,baseline);
        for(var extraction:AlchemyRecipes.extractions())
            if(extraction.needs().stream().allMatch(need->need.alternatives().stream().anyMatch(discovery::contains)))
                award(player,"extraction/"+extraction.key(),rules.recipeXp,null,baseline);
        if(baseline)for(var formula:ModComponents.ALCHEMY_KNOWLEDGE.get(player).knownFormulas())formulaBaseline(player,formula);
    }
    private static void formulaBaseline(ServerPlayerEntity p,String key) { award(p,"formula/"+key,0,null,true); }
    private static List<Candidate> catalogue(ServerPlayerEntity player) {
        var result=new ArrayList<Candidate>(); var world=player.getWorld();
        for(var entry:world.getRecipeManager().values()) {
            var recipe=entry.value(); var unwrapped=MaterialCrafting.unwrap(recipe);
            if(entry.id().getPath().contains("salvage")||entry.id().getPath().contains("recycl")||GearSalvage.isBlastRecipe(entry))continue;
            boolean supported=recipe instanceof StationAssemblyRecipe assembly?MaterialCrafting.supportsAssembly(recipe,assembly.station(),world):
                recipe instanceof CraftingRecipe crafting?MaterialCrafting.supports(crafting,3,world):
                recipe instanceof StonecuttingRecipe?MaterialCrafting.supportsCutting(recipe,world):recipe instanceof AbstractCookingRecipe;
            if(!supported||unwrapped instanceof SpecialCraftingRecipe)continue;
            var output=recipe.getResult(world.getRegistryManager());
            if(output.isEmpty()||!output.isItemEnabled(world.getEnabledFeatures()))continue;
            result.add(new Candidate(DiscoveryFamilies.recipe(Registries.ITEM.getId(output.getItem()).toString()),recipe));
        }
        return List.copyOf(result);
    }
    private static void explore(ServerPlayerEntity player) {
        var world=player.getServerWorld(); var pos=player.getBlockPos();
        if(!world.isChunkLoaded(pos))return;
        var biome=world.getBiome(pos).getKey();
        biome.ifPresent(key->award(player,"biome/"+key.getValue(),ProgressionConfig.RULES.biomeXp,null,false));
        // Use only full chunks already loaded for gameplay; never load a structure origin for XP.
        var chunk=world.getChunkManager().getWorldChunk(pos.getX()>>4,pos.getZ()>>4);
        if(chunk==null)return;
        var scan=scans.computeIfAbsent(player,p->new Scan());
        var present=new HashSet<String>();
        for(var reference:chunk.getStructureReferences().entrySet())for(long packed:reference.getValue()) {
            var origin=new net.minecraft.util.math.ChunkPos(packed);
            var originChunk=world.getChunkManager().getWorldChunk(origin.x,origin.z);
            if(originChunk==null)continue;
            var start=originChunk.getStructureStart(reference.getKey());
            if(start==null||!start.hasChildren()||start.getChildren().stream().noneMatch(piece->piece.getBoundingBox().contains(pos)))continue;
            Identifier type=world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(start.getStructure());
            String key=world.getRegistryKey().getValue()+"/"+type+"/"+origin.toLong();
            present.add(key);
            int dwell=scan.visits.merge(key,1,Integer::sum);
            if(dwell<3)continue;
            award(player,"structure/"+key,ProgressionConfig.RULES.structureXp,null,false);
            award(player,"structure_type/"+type,ProgressionConfig.RULES.firstStructureTypeXp,null,false);
        }
        scan.visits.keySet().retainAll(present);
    }
}
