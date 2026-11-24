package com.cranks.dungeons.screen;

import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

/** Station screens use the same player slot IDs and restrictions as the inventory. */
public abstract class LoadoutStationHandler extends ScreenHandler {
    public static final int PLAYER_SLOT_END = PlayerLoadout.handlerSlot(PlayerLoadout.END);
    private final PlayerScreenHandler playerHandler;

    protected LoadoutStationHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory inventory) {
        super(type, syncId);
        playerHandler = inventory.player.playerScreenHandler;
        for (var slot : playerHandler.slots) addSlot(slot);
        PlayerLoadout loadout = PlayerLoadout.of(inventory);
        addProperty(new Property() {
            @Override
            public int get() {
                return loadout.secondSetActive() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                loadout.setSecondSetActive(value == 1);
            }
        });
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return playerHandler.quickMove(player, slot);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        return id == PlayerLoadout.SWAP_BUTTON && PlayerLoadout.of(player.getInventory()).swapArmor(player);
    }
}
