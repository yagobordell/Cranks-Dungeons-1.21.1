package com.cranks.dungeons.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;

/** The table exposes the same player slots and rules as the standalone inventory. */
public final class LoadoutCraftingTableHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;

    public LoadoutCraftingTableHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public LoadoutCraftingTableHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_CRAFTING_TABLE, syncId, inventory);
        this.context = context;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, Blocks.CRAFTING_TABLE);
    }
}
