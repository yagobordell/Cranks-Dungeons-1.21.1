package com.cranks.dungeons.mixin;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(CrafterBlock.class)
public interface CrafterOutputInvoker {
    @Invoker("transferOrSpawnStack") void cranksdungeons$output(ServerWorld world,BlockPos pos,CrafterBlockEntity crafter,ItemStack stack,BlockState state,RecipeEntry<CraftingRecipe> recipe);
}
