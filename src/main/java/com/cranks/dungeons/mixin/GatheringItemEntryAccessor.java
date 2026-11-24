package com.cranks.dungeons.mixin;

import net.minecraft.item.Item;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntry.class)
public interface GatheringItemEntryAccessor {
    @Accessor("item") RegistryEntry<Item> cranks$lootItem();
}
