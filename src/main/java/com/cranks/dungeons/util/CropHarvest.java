package com.cranks.dungeons.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;

public final class CropHarvest {
    private CropHarvest() {}

    public static boolean isMature(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) return crop.isMature(state);
        if (state.getBlock() instanceof NetherWartBlock) return state.get(NetherWartBlock.AGE) == 3;
        return state.getBlock() instanceof CocoaBlock && state.get(CocoaBlock.AGE) == 2;
    }
}
