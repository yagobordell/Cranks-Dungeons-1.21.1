package com.cranks.dungeons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEnchantSeedAccessor {

    @Accessor("enchantingTableSeed")
    int getEnchantingTableSeed();

    @Accessor("enchantingTableSeed")
    void setEnchantingTableSeed(int seed);
}