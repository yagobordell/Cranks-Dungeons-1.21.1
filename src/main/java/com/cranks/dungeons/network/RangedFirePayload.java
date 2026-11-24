package com.cranks.dungeons.network;

import com.cranks.dungeons.ranged.RangedActions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record RangedFirePayload(Hand hand, int selectedSlot) implements CustomPayload {
    public static final Id<RangedFirePayload> ID = new Id<>(Identifier.of("cranks-dungeons", "ranged_fire"));
    public static final PacketCodec<RegistryByteBuf,RangedFirePayload> CODEC = PacketCodec.of(
            (value,buf) -> { buf.writeEnumConstant(value.hand); buf.writeVarInt(value.selectedSlot); },
            buf -> new RangedFirePayload(buf.readEnumConstant(Hand.class),buf.readVarInt()));
    @Override public Id<? extends CustomPayload> getId() { return ID; }
    public void handle(net.minecraft.server.network.ServerPlayerEntity player) {
        if (!RangedActions.fire(player, hand, selectedSlot)) {
            if (player.isUsingItem() && player.getActiveItem().getItem() instanceof net.minecraft.item.BowItem) player.stopUsingItem();
            player.playerScreenHandler.syncState();
        }
    }
}
