package com.cranks.dungeons.inventory;

import com.cranks.dungeons.mixin.CrafterOutputInvoker;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;

public final class AutomaticCrafting {
    private AutomaticCrafting() {}
    public static List<RecipeEntry<CraftingRecipe>> recipes(World world) {
        return world.getRecipeManager().listAllOfType(RecipeType.CRAFTING).stream()
                .filter(entry -> MaterialCrafting.supports(entry.value(), 3, world))
                .sorted(Comparator.comparing(RecipeEntry::id)).toList();
    }
    public static RecipeEntry<CraftingRecipe> recipe(World world, Identifier id) {
        var entry = id == null ? null : world.getRecipeManager().get(id).orElse(null);
        return entry != null && entry.value() instanceof CraftingRecipe crafting && MaterialCrafting.supports(crafting,3,world)
                ? new RecipeEntry<>(entry.id(),crafting) : null;
    }
    public static List<ItemStack> sample(Inventory inventory) {
        var choices = new ArrayList<ItemStack>();
        for(int i=0;i<inventory.size();i++) if(!inventory.getStack(i).isEmpty()) choices.add(inventory.getStack(i).copyWithCount(1));
        return List.copyOf(choices);
    }
    public static MaterialCrafting.Selection selection(Inventory inventory,RecipeEntry<CraftingRecipe> recipe,World world,List<ItemStack> choices) {
        var storage = new MaterialCrafting.Storage(inventory);
        var sources = new ArrayList<Integer>();
        if(MaterialCrafting.usesChoices(recipe.value())) {
            if(choices.isEmpty() || choices.size()>9) return null;
            for(var choice:choices) {
                int slot=-1;
                for(int i=0;i<storage.size();i++) if(ItemStack.areItemsAndComponentsEqual(choice,storage.stack(i))) {slot=i;break;}
                if(slot<0) return null;
                sources.add(slot);
            }
        } else if(!choices.isEmpty()) return null;
        return new MaterialCrafting.Selection(storage,recipe,world,3,sources);
    }
    public static MaterialCrafting.AutomationPlan plan(Inventory inventory,RecipeEntry<CraftingRecipe> recipe,World world,List<ItemStack> choices) {
        var selection=selection(inventory,recipe,world,choices);
        return selection==null?null:selection.automationPlan();
    }
    public static void pulse(CrafterBlock block,BlockState state,ServerWorld world,BlockPos pos) {
        if(!(world.getBlockEntity(pos) instanceof CrafterBlockEntity crafter)) return;
        var program=(ProgrammedCrafter)crafter;
        var entry=recipe(world,program.cranksdungeons$recipe());
        var plan=entry==null?null:plan(crafter,entry,world,program.cranksdungeons$choices());
        if(plan==null || !plan.storage().isCurrent()) {world.syncWorldEvent(1050,pos,0);return;}
        // The entire input update happens on the server thread before any output can enter another inventory.
        for(int i=0;i<crafter.size();i++) crafter.setStack(i,plan.remaining().get(i));
        crafter.markDirty();
        crafter.setCraftingTicksRemaining(6);
        world.setBlockState(pos,state.with(CrafterBlock.CRAFTING,true),2);
        plan.outputs().getFirst().onCraftByCrafter(world);
        for(var output:plan.outputs()) {
            ((CrafterOutputInvoker)block).cranksdungeons$output(world,pos,crafter,output,state,entry);
        }
    }
}
