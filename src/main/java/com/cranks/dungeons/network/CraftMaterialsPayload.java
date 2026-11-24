package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.FurnaceLoading;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.StationAssemblyRecipe;
import com.cranks.dungeons.screen.LoadoutAnvilHandler;
import com.cranks.dungeons.screen.LoadoutCraftingTableHandler;
import com.cranks.dungeons.screen.LoadoutCuttingBenchHandler;
import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public record CraftMaterialsPayload(int syncId, int request, Identifier recipe, int quantity, List<Source> sources)
        implements CustomPayload {
    public record Source(int slot, ItemStack expected) {
        private static final PacketCodec<RegistryByteBuf, Source> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Source::slot, ItemStack.PACKET_CODEC, Source::expected, Source::new);
    }

    public static final Id<CraftMaterialsPayload> ID = new Id<>(Identifier.of(CranksDungeons.MOD_ID, "craft_materials"));
    public static final PacketCodec<RegistryByteBuf, CraftMaterialsPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, CraftMaterialsPayload::syncId,
            PacketCodecs.VAR_INT, CraftMaterialsPayload::request,
            Identifier.PACKET_CODEC, CraftMaterialsPayload::recipe,
            PacketCodecs.VAR_INT, CraftMaterialsPayload::quantity,
            Source.CODEC.collect(PacketCodecs.toList(MaterialCrafting.MAX_INPUTS)), CraftMaterialsPayload::sources,
            CraftMaterialsPayload::new);

    @Override
    public Id<CraftMaterialsPayload> getId() {
        return ID;
    }

    void handle(ServerPlayerEntity player) {
        int result = craft(player);
        ServerPlayNetworking.send(player, new CraftMaterialsResultPayload(request, result));
    }

    private int craft(ServerPlayerEntity player) {
        var screen = player.currentScreenHandler;
        if (screen.syncId != syncId || !screen.canUse(player) || !player.isAlive() || player.isSpectator()
                || quantity < 1 || quantity > MaterialCrafting.MAX_BATCH) return CraftMaterialsResultPayload.UNAVAILABLE;
        var world = player.getServerWorld();
        var recipeEntry = world.getRecipeManager().get(recipe).orElse(null);
        if (screen instanceof LoadoutFurnaceHandler furnace) {
            return sources.isEmpty() ? FurnaceLoading.load(player, furnace, recipeEntry, quantity)
                    : CraftMaterialsResultPayload.UNAVAILABLE;
        }
        if (screen instanceof LoadoutCuttingBenchHandler bench) {
            if (recipeEntry == null || !sources.isEmpty() || !MaterialCrafting.supportsCutting(recipeEntry.value(), world)) {
                return CraftMaterialsResultPayload.UNAVAILABLE;
            }
            int result = craftFromInventory(player, recipeEntry, 3);
            if (result > 0) bench.playCutSound();
            return result;
        }
        if (screen instanceof LoadoutAnvilHandler anvil) {
            if (recipeEntry == null || !sources.isEmpty()
                    || !MaterialCrafting.supportsAssembly(recipeEntry.value(), StationAssemblyRecipe.Station.ANVIL, world)) {
                return CraftMaterialsResultPayload.UNAVAILABLE;
            }
            int result = craftFromInventory(player, recipeEntry, 3);
            if (result > 0) anvil.playForgeSound();
            return result;
        }
        if (screen instanceof com.cranks.dungeons.screen.LoadoutFletchingHandler bench) {
            if (recipeEntry == null || !sources.isEmpty() || !MaterialCrafting.supportsAssembly(recipeEntry.value(), StationAssemblyRecipe.Station.FLETCHING_TABLE, world))
                return CraftMaterialsResultPayload.UNAVAILABLE;
            int result = craftFromInventory(player, recipeEntry, 3);
            if (result > 0) bench.playCraftSound();
            return result;
        }
        if (screen instanceof com.cranks.dungeons.screen.LoadoutTailoringHandler bench) {
            if (recipeEntry == null || !MaterialCrafting.supportsAssembly(recipeEntry.value(), StationAssemblyRecipe.Station.TAILORING_BENCH, world))
                return CraftMaterialsResultPayload.UNAVAILABLE;
            var assembly = ((StationAssemblyRecipe) recipeEntry.value()).assembly();
            if (MaterialCrafting.usesChoices(assembly) ? sources.size() > MaterialCrafting.choiceLimit(assembly, 3) : !sources.isEmpty())
                return CraftMaterialsResultPayload.UNAVAILABLE;
            int result = craftFromInventory(player, recipeEntry, 3);
            if (result > 0) bench.playCraftSound();
            return result;
        }
        if (screen instanceof com.cranks.dungeons.screen.LoadoutCartographyHandler table) {
            if (recipeEntry == null || !MaterialCrafting.supportsAssembly(recipeEntry.value(), StationAssemblyRecipe.Station.CARTOGRAPHY_TABLE, world))
                return CraftMaterialsResultPayload.UNAVAILABLE;
            var assembly = ((StationAssemblyRecipe) recipeEntry.value()).assembly();
            if (MaterialCrafting.usesChoices(assembly) ? sources.size() != 1 : !sources.isEmpty())
                return CraftMaterialsResultPayload.UNAVAILABLE;
            int result = craftFromInventory(player, recipeEntry, 3);
            if (result > 0) table.playCraftSound();
            return result;
        }
        int grid;
        if (screen instanceof LoadoutCraftingTableHandler) grid = 3;
        else if (screen == player.playerScreenHandler && !player.isCreative()) grid = 2;
        else return CraftMaterialsResultPayload.UNAVAILABLE;

        if (recipeEntry == null || !(recipeEntry.value() instanceof CraftingRecipe crafting)
                || !MaterialCrafting.supports(crafting, grid, world)
                || (MaterialCrafting.usesChoices(crafting)
                    ? sources.size() > MaterialCrafting.choiceLimit(crafting, grid) : !sources.isEmpty())) {
            return CraftMaterialsResultPayload.UNAVAILABLE;
        }
        return craftFromInventory(player, recipeEntry, grid);
    }

    private int craftFromInventory(ServerPlayerEntity player, RecipeEntry<?> entry, int grid) {
        if (!MaterialCrafting.allowed(player, entry)) return CraftMaterialsResultPayload.LOCKED;
        var storage = new MaterialCrafting.Storage(player.getInventory());
        for (Source source : sources) {
            ItemStack actual = storage.stackAtSlot(source.slot());
            if (actual.isEmpty() || !ItemStack.areItemsAndComponentsEqual(actual, source.expected())) {
                return CraftMaterialsResultPayload.INVENTORY_CHANGED;
            }
        }
        var selection = new MaterialCrafting.Selection(storage, entry, player.getServerWorld(), grid, sources.stream().map(Source::slot).toList());
        var plan = selection.plan(quantity);
        if (plan == null) return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        MaterialCrafting.commit(player, entry, plan, quantity);
        player.currentScreenHandler.sendContentUpdates();
        return plan.output().getCount() * quantity;
    }
}
