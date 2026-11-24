package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.GearSalvage;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.screen.LoadoutCraftingTableHandler;
import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SalvageGearPayload(int syncId, int request, int source, ItemStack expected) implements CustomPayload {
    public static final int CURSOR = -1;
    public static final Id<SalvageGearPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "salvage_gear"));
    public static final PacketCodec<RegistryByteBuf, SalvageGearPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SalvageGearPayload::syncId,
            PacketCodecs.VAR_INT, SalvageGearPayload::request,
            PacketCodecs.VAR_INT, SalvageGearPayload::source,
            ItemStack.PACKET_CODEC, SalvageGearPayload::expected,
            SalvageGearPayload::new);

    @Override
    public Id<SalvageGearPayload> getId() {
        return ID;
    }

    void handle(ServerPlayerEntity player) {
        int result = salvage(player);
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, result));
    }

    private int salvage(ServerPlayerEntity player) {
        var screen = player.currentScreenHandler;
        if (screen.syncId != syncId || !player.isAlive() || player.isSpectator() || !screen.canUse(player)) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        var furnace = screen instanceof LoadoutFurnaceHandler handler && handler.isBlastFurnace() ? handler : null;
        if (furnace == null && !(screen instanceof LoadoutCraftingTableHandler)) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }

        var inventory = player.getInventory();
        Slot slot = null;
        ItemStack actual;
        if (source == CURSOR && furnace != null) {
            actual = screen.getCursorStack();
        } else {
            if (source < 5 || source >= LoadoutStationHandler.PLAYER_SLOT_END) return CraftMaterialsResultPayload.UNAVAILABLE;
            slot = screen.getSlot(source);
            if (slot.inventory != inventory || !slot.isEnabled() || !slot.canTakeItems(player)) {
                return CraftMaterialsResultPayload.UNAVAILABLE;
            }
            actual = slot.getStack();
            if (furnace == null && new MaterialCrafting.Storage(inventory).stackAtSlot(slot.getIndex()).isEmpty()) {
                return CraftMaterialsResultPayload.UNAVAILABLE;
            }
        }
        if (actual.getCount() != 1 || !ItemStack.areEqual(actual, expected)) {
            return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        }
        ItemStack output = GearSalvage.output(actual, furnace != null);
        if (output.isEmpty() || furnace != null && !furnace.canLoadSalvage(actual)) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }

        int recovered = output.getCount();
        if (slot == null) screen.setCursorStack(ItemStack.EMPTY);
        else slot.setStack(ItemStack.EMPTY);
        if (furnace != null) {
            furnace.loadSalvage(actual);
        } else {
            PlayerLoadout.of(inventory).insert(output);
            if (!output.isEmpty()) player.dropItem(output, false);
        }
        inventory.markDirty();
        screen.sendContentUpdates();
        return furnace != null ? 1 : recovered;
    }
}
