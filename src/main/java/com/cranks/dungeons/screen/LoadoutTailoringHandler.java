package com.cranks.dungeons.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class LoadoutTailoringHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;
    public LoadoutTailoringHandler(int id, PlayerInventory inventory) { this(id, inventory, ScreenHandlerContext.EMPTY); }
    public LoadoutTailoringHandler(int id, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_TAILORING, id, inventory);
        this.context = context;
    }
    @Override public boolean canUse(PlayerEntity player) { return canUse(context, player, Blocks.LOOM); }
    public void playCraftSound() {
        context.run((world, pos) -> world.playSound(null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 0.5F, 1));
    }
}
