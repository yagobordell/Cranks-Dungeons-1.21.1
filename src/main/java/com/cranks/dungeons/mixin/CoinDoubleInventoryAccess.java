package com.cranks.dungeons.mixin;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(DoubleInventory.class)
public interface CoinDoubleInventoryAccess {
    @Accessor("first") Inventory cranks$first();
    @Accessor("second") Inventory cranks$second();
}
