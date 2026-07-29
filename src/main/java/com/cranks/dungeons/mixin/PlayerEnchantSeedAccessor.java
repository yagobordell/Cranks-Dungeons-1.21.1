package com.cranks.dungeons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEnchantSeedAccessor {

    @Accessor("enchantmentTableSeed")
    int getEnchantingTableSeed();

    @Accessor("enchantmentTableSeed")
    void setEnchantingTableSeed(int seed);
}