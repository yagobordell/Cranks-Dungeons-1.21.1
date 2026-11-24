package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.EquipmentRepair;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.screen.LoadoutGrindstoneHandler;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record RepairGearPayload(int syncId, int request, Source target, int materials, Optional<Source> donor)
        implements CustomPayload {
    public record Source(int slot, ItemStack expected) {
        private static final PacketCodec<RegistryByteBuf, Source> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Source::slot, ItemStack.PACKET_CODEC, Source::expected, Source::new);
    }

    public static final Id<RepairGearPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "repair_gear"));
    public static final PacketCodec<RegistryByteBuf, RepairGearPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, RepairGearPayload::syncId,
            PacketCodecs.VAR_INT, RepairGearPayload::request,
            Source.CODEC, RepairGearPayload::target,
            PacketCodecs.VAR_INT, RepairGearPayload::materials,
            PacketCodecs.optional(Source.CODEC), RepairGearPayload::donor,
            RepairGearPayload::new);

    @Override
    public Id<RepairGearPayload> getId() {
        return ID;
    }

    void handle(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, repair(player)));
    }

    private int repair(ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof LoadoutGrindstoneHandler screen) || screen.syncId != syncId
                || !screen.canUse(player) || !player.isAlive() || player.isSpectator()) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        PlayerInventory inventory = player.getInventory();
        Slot targetSlot = slot(screen, inventory, target);
        if (targetSlot == null || !EquipmentRepair.canRepair(targetSlot.getStack())) {
            return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        }
        ItemStack gear = targetSlot.getStack();
        var storage = new MaterialCrafting.Storage(inventory);
        int restored;
        if (donor.isPresent()) {
            Slot source = slot(screen, inventory, donor.get());
            if (materials != 0 || source == null || source.getIndex() == targetSlot.getIndex()
                    || !source.canTakeItems(player) || storage.stackAtSlot(source.getIndex()).isEmpty()
                    || !EquipmentRepair.isDonor(gear, source.getStack())) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
            restored = Math.min(gear.getDamage(), EquipmentRepair.remaining(source.getStack()));
            source.setStack(ItemStack.EMPTY);
        } else {
            var cost = EquipmentRepair.materials(gear);
            if (materials < 1 || materials > cost.needed(gear)
                    || materials > EquipmentRepair.available(storage, cost.ingredient(), targetSlot.getIndex())) {
                return CraftMaterialsResultPayload.INVENTORY_CHANGED;
            }
            restored = (int) Math.min(gear.getDamage(), (long) materials * cost.durabilityPerMaterial(gear));
            int remaining = materials;
            for (int i = 0; i < storage.size() && remaining > 0; i++) {
                if (storage.slot(i) == targetSlot.getIndex()) continue;
                ItemStack stack = storage.stack(i);
                if (stack.isEmpty() || !cost.ingredient().test(stack)) continue;
                int used = Math.min(remaining, stack.getCount());
                inventory.setStack(storage.slot(i), stack.copyWithCount(stack.getCount() - used));
                remaining -= used;
            }
        }
        gear.setDamage(gear.getDamage() - restored);
        inventory.markDirty();
        screen.sendContentUpdates();
        screen.playRepairSound();
        return restored;
    }

    private static Slot slot(LoadoutGrindstoneHandler screen, PlayerInventory inventory, Source source) {
        if (source.slot < 5 || source.slot >= LoadoutStationHandler.PLAYER_SLOT_END) return null;
        Slot slot = screen.getSlot(source.slot);
        return slot.inventory == inventory && slot.isEnabled() && ItemStack.areEqual(slot.getStack(), source.expected)
                ? slot : null;
    }
}
