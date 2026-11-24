package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

/** Apply a complete preset only after validating all ten requested and current slots. */
public record CreativeHotbarPayload(int request, List<ItemStack> stacks, List<ItemStack> expected) implements CustomPayload {
    public static final Id<CreativeHotbarPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "creative_hotbar"));
    public static final PacketCodec<RegistryByteBuf, CreativeHotbarPayload> CODEC = new PacketCodec<>() {
        private List<ItemStack> read(RegistryByteBuf buf) {
            if (buf.readVarInt() != PlayerLoadout.HOTBAR_COUNT) throw new IllegalArgumentException("Expected ten hotbar slots");
            var result = new ArrayList<ItemStack>();
            for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) result.add(ItemStack.OPTIONAL_PACKET_CODEC.decode(buf));
            return result;
        }
        private void write(RegistryByteBuf buf, List<ItemStack> list) {
            if (list.size() != PlayerLoadout.HOTBAR_COUNT) throw new IllegalArgumentException("Expected ten hotbar slots");
            buf.writeVarInt(list.size());
            for (var item : list) ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, item);
        }
        @Override public CreativeHotbarPayload decode(RegistryByteBuf buf) { return new CreativeHotbarPayload(buf.readVarInt(), read(buf), read(buf)); }
        @Override public void encode(RegistryByteBuf buf, CreativeHotbarPayload payload) { buf.writeVarInt(payload.request); write(buf, payload.stacks); write(buf, payload.expected); }
    };
    @Override public Id<CreativeHotbarPayload> getId() { return ID; }

    public int perform(ServerPlayerEntity player) {
        if (!player.isCreative() || !player.isAlive() || player.isSpectator() || player.currentScreenHandler != player.playerScreenHandler
                || stacks.size() != PlayerLoadout.HOTBAR_COUNT || expected.size() != PlayerLoadout.HOTBAR_COUNT) return CraftMaterialsResultPayload.UNAVAILABLE;
        if (!player.playerScreenHandler.getCursorStack().isEmpty()) return CraftMaterialsResultPayload.LOCKED;
        for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) {
            var stack = stacks.get(i);
            if (!ItemStack.areEqual(player.getInventory().getStack(PlayerLoadout.getHotbarSlot(i)), expected.get(i))) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
            if (!stack.isEmpty() && (stack.getCount() > stack.getMaxCount() || !stack.isItemEnabled(player.getWorld().getEnabledFeatures())
                    || CranksDungeons.shouldRemoveFromCreative(stack))) return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) player.getInventory().setStack(PlayerLoadout.getHotbarSlot(i), stacks.get(i).copy());
        player.getInventory().markDirty();
        player.playerScreenHandler.sendContentUpdates();
        return 1;
    }
    void handle(ServerPlayerEntity player) {
        int result = perform(player);
        if (result <= 0) player.currentScreenHandler.syncState();
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, result));
    }
}
