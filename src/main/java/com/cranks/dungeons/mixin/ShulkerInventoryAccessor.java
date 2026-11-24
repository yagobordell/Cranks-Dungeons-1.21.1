package com.cranks.dungeons.mixin;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ShulkerBoxScreenHandler.class)
public interface ShulkerInventoryAccessor {@Accessor("inventory") Inventory cranksdungeons$inventory();}
