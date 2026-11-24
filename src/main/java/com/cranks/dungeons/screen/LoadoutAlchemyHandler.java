package com.cranks.dungeons.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;

public final class LoadoutAlchemyHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;
    public LoadoutAlchemyHandler(int syncId, PlayerInventory inventory) { this(syncId, inventory, ScreenHandlerContext.EMPTY); }
    public LoadoutAlchemyHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_ALCHEMY, syncId, inventory);
        this.context = context;
    }
    @Override public boolean canUse(PlayerEntity player) { return canUse(context, player, Blocks.BREWING_STAND); }
}
