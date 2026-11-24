package com.cranks.dungeons.screen;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class LoadoutCartographyHandler extends LoadoutStationHandler {
    private final ScreenHandlerContext context;
    private final PlayerInventory inventory;
    private final Set<MapIdComponent> sentMaps = new HashSet<>();
    public LoadoutCartographyHandler(int id, PlayerInventory inventory) { this(id, inventory, ScreenHandlerContext.EMPTY); }
    public LoadoutCartographyHandler(int id, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.LOADOUT_CARTOGRAPHY, id, inventory);
        this.context = context;
        this.inventory = inventory;
        sendMapPreviews();
    }
    @Override public boolean canUse(PlayerEntity player) { return canUse(context, player, Blocks.CARTOGRAPHY_TABLE); }
    @Override public void sendContentUpdates() { super.sendContentUpdates(); sendMapPreviews(); }
    private void sendMapPreviews() {
        if (!(inventory.player instanceof ServerPlayerEntity player)) return;
        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            var id = stack.get(DataComponentTypes.MAP_ID);
            if (id == null || sentMaps.contains(id)) continue;
            var state = FilledMapItem.getMapState(stack, player.getWorld());
            if (state == null) continue;
            var decorations = new ArrayList<net.minecraft.item.map.MapDecoration>();
            state.getDecorations().forEach(decorations::add);
            player.networkHandler.sendPacket(new MapUpdateS2CPacket(id, state.scale, state.locked, decorations,
                    new MapState.UpdateData(0, 0, 128, 128, state.colors.clone())));
            sentMaps.add(id);
        }
    }
    public void playCraftSound() {
        context.run((world, pos) -> world.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 0.5F, 1));
    }
}
