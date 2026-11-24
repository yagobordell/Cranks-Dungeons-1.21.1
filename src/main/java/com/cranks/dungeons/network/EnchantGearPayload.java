package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.EquipmentEnchanting;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.screen.LoadoutEnchantingHandler;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record EnchantGearPayload(int syncId, int request, int slot, ItemStack expected, Optional<Identifier> enchantment,
                                 Optional<Identifier> removing)
        implements CustomPayload {
    public static final Id<EnchantGearPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "enchant_gear"));
    public static final PacketCodec<RegistryByteBuf, EnchantGearPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, EnchantGearPayload::syncId,
            PacketCodecs.VAR_INT, EnchantGearPayload::request,
            PacketCodecs.VAR_INT, EnchantGearPayload::slot,
            ItemStack.PACKET_CODEC, EnchantGearPayload::expected,
            PacketCodecs.optional(Identifier.PACKET_CODEC), EnchantGearPayload::enchantment,
            PacketCodecs.optional(Identifier.PACKET_CODEC), EnchantGearPayload::removing,
            EnchantGearPayload::new);

    @Override
    public Id<EnchantGearPayload> getId() {
        return ID;
    }

    void handle(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, enchant(player)));
    }

    private int enchant(ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof LoadoutEnchantingHandler table) || table.syncId != syncId
                || !player.isAlive() || player.isSpectator() || !table.canUse(player)
                || slot < 5 || slot >= LoadoutStationHandler.PLAYER_SLOT_END) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        PlayerInventory inventory = player.getInventory();
        var target = table.getSlot(slot);
        ItemStack stack = target.getStack();
        if (target.inventory != inventory || !target.isEnabled() || !ItemStack.areEqual(stack, expected)) {
            return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        }
        if (enchantment.equals(removing)) return CraftMaterialsResultPayload.LOCKED;

        var registry = player.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        ItemStack result = stack;
        if (removing.isPresent()) {
            var removed = registry.getEntry(removing.get()).orElse(null);
            if (removed == null) return CraftMaterialsResultPayload.LOCKED;
            result = EquipmentEnchanting.withoutOneCopy(stack, removed);
            if (result.isEmpty()) return CraftMaterialsResultPayload.LOCKED;
        }

        if (enchantment.isPresent()) {
            var entry = registry.getEntry(enchantment.get()).orElse(null);
            if (entry == null) return CraftMaterialsResultPayload.LOCKED;
            result = EquipmentEnchanting.withEnchantment(result, entry);
            if (result.isEmpty()) return CraftMaterialsResultPayload.LOCKED;

            if (!player.isCreative()) {
                int cost = EquipmentEnchanting.lapisCost(entry);
                var storage = new MaterialCrafting.Storage(inventory);
                if (EquipmentEnchanting.availableLapis(storage) < cost) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
                spendLapis(inventory, storage, cost);
            }
        }
        target.setStack(result);
        inventory.markDirty();
        table.sendContentUpdates();
        if (enchantment.isPresent()) {
            player.incrementStat(Stats.ENCHANT_ITEM);
            Criteria.ENCHANTED_ITEM.trigger(player, result, 1);
        }
        table.playEnchantSound();
        return 1;
    }

    private static void spendLapis(PlayerInventory inventory, MaterialCrafting.Storage storage, int remaining) {
        for (int i = 0; i < storage.size() && remaining > 0; i++) {
            ItemStack stack = storage.stack(i);
            if (!stack.isOf(Items.LAPIS_LAZULI)) continue;
            int used = Math.min(remaining, stack.getCount());
            inventory.setStack(storage.slot(i), stack.copyWithCount(stack.getCount() - used));
            remaining -= used;
        }
    }
}
