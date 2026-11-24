package com.cranks.dungeons.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import java.util.List;

/** Saved machine configuration; no online player or profession state is required to run it. */
public interface ProgrammedCrafter {
    Identifier cranksdungeons$recipe();
    List<ItemStack> cranksdungeons$choices();
    int cranksdungeons$revision();
    void cranksdungeons$program(Identifier recipe, List<ItemStack> choices);
}
