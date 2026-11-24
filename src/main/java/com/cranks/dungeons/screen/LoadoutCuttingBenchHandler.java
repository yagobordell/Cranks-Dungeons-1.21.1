package com.cranks.dungeons.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class LoadoutCuttingBenchHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;
    private long lastCutSound = Long.MIN_VALUE;

    public LoadoutCuttingBenchHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public LoadoutCuttingBenchHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_CUTTING_BENCH, syncId, inventory);
        this.context = context;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, Blocks.STONECUTTER);
    }

    public void playCutSound() {
        context.run((world, pos) -> {
            if (world.getTime() == lastCutSound) return;
            lastCutSound = world.getTime();
            world.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1, 1);
        });
    }
}
