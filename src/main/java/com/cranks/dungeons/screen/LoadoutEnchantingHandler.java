package com.cranks.dungeons.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class LoadoutEnchantingHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;

    public LoadoutEnchantingHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public LoadoutEnchantingHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_ENCHANTING, syncId, inventory);
        this.context = context;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, Blocks.ENCHANTING_TABLE);
    }

    public void playEnchantSound() {
        context.run((world, pos) -> world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS, 1, 1));
    }
}
