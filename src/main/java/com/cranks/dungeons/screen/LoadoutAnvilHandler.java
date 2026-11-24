package com.cranks.dungeons.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class LoadoutAnvilHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;

    public LoadoutAnvilHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public LoadoutAnvilHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_ANVIL, syncId, inventory);
        this.context = context;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return context.get((world, pos) -> world.getBlockState(pos).isIn(BlockTags.ANVIL)
                && player.canInteractWithBlockAt(pos, 4.0), true);
    }

    public void playForgeSound() {
        context.run((world, pos) -> world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE,
                SoundCategory.BLOCKS, 0.4F, 1));
    }
}
