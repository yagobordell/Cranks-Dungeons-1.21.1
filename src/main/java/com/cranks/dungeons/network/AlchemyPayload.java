package com.cranks.dungeons.network;

import com.cranks.dungeons.alchemy.*;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.screen.LoadoutAlchemyHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record AlchemyPayload(int syncId, int request, int action, int quantity, String selection) implements CustomPayload {
    public static final Id<AlchemyPayload> ID = new Id<>(AlchemyItems.id("alchemy"));
    public static final PacketCodec<RegistryByteBuf, AlchemyPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, AlchemyPayload::syncId, PacketCodecs.VAR_INT, AlchemyPayload::request,
            PacketCodecs.VAR_INT, AlchemyPayload::action, PacketCodecs.VAR_INT, AlchemyPayload::quantity,
            PacketCodecs.string(160), AlchemyPayload::selection, AlchemyPayload::new);
    @Override public Id<AlchemyPayload> getId() { return ID; }

    void handle(ServerPlayerEntity player) { ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, perform(player))); }

    public int perform(ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof LoadoutAlchemyHandler station) || station.syncId != syncId
                || !station.canUse(player) || !player.isAlive() || player.isSpectator()
                || quantity < 1 || quantity > MaterialCrafting.MAX_BATCH || selection.length() > 160) return CraftMaterialsResultPayload.UNAVAILABLE;
        var knowledge = ModComponents.ALCHEMY_KNOWLEDGE.get(player);
        List<AlchemyRecipes.Need> needs;
        ItemStack output;
        String learned = null;
        boolean success = true;
        if (action == 0) {
            var recipe = AlchemyRecipes.extractions().stream().filter(entry -> entry.key().equals(selection)).findFirst().orElse(null);
            if (recipe == null) return CraftMaterialsResultPayload.UNAVAILABLE;
            needs = recipe.needs();
            output = recipe.output();
        } else if (action == 1) {
            if (quantity != 1) return CraftMaterialsResultPayload.UNAVAILABLE;
            String[] parts = selection.split(",", -1);
            if (parts.length != 5 || !parts[3].equals("0") && !parts[3].equals("1")) return CraftMaterialsResultPayload.UNAVAILABLE;
            AlchemyItems.Delivery delivery = delivery(parts[4]);
            var experiment = new AlchemyRecipes.Experiment(parts[0], parts[1], parts[2], parts[3].equals("0"), delivery);
            if (!AlchemyRecipes.valid(experiment)) return CraftMaterialsResultPayload.UNAVAILABLE;
            var formula = AlchemyRecipes.resolve(experiment);
            needs = AlchemyRecipes.needs(experiment);
            success = formula != null;
            learned = success ? formula.key() : experiment.key();
            output = success ? formula.stack(delivery) : AlchemyPotions.failed(experiment.key(), experiment.lesser(), delivery);
        } else if (action == 2) {
            String[] parts = selection.split(",", -1);
            if (parts.length != 2) return CraftMaterialsResultPayload.UNAVAILABLE;
            var formula = AlchemyPotions.get(parts[0]);
            var delivery = delivery(parts[1]);
            if (formula == null || delivery == null || !knowledge.knows(formula.key())) return CraftMaterialsResultPayload.LOCKED;
            needs = AlchemyRecipes.needs(AlchemyRecipes.forFormula(formula, delivery));
            output = formula.stack(delivery);
        } else return CraftMaterialsResultPayload.UNAVAILABLE;
        var plan = AlchemyRecipes.plan(new MaterialCrafting.Storage(player.getInventory()), needs, output, quantity);
        if (plan == null || !plan.commit(player.getInventory())) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        if (learned != null) knowledge.learn(learned, success);
        station.sendContentUpdates();
        player.getWorld().playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.BLOCK_BREWING_STAND_BREW,
                net.minecraft.sound.SoundCategory.BLOCKS, 0.7F, 1);
        return quantity;
    }

    private static AlchemyItems.Delivery delivery(String id) {
        return switch (id) { case "0" -> AlchemyItems.Delivery.DRINK; case "1" -> AlchemyItems.Delivery.SPLASH; case "2" -> AlchemyItems.Delivery.LINGERING; default -> null; };
    }
}
