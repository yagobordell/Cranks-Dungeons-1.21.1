package com.cranks.dungeons.mount;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.SimpleInventory;

/** Implemented by the AbstractHorseEntity mixin; kept outside the mixin package for safe use by other mods. */
public interface AbstractHorseInventoryAccess {
    SimpleInventory cranksdungeons$getHorseItems();

    void cranksdungeons$resizeHorseInventory();

    static AbstractHorseInventoryAccess of(AbstractHorseEntity horse) {
        return (AbstractHorseInventoryAccess) horse;
    }
}
