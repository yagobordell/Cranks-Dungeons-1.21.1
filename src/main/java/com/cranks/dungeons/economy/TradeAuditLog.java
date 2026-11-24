package com.cranks.dungeons.economy;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.component.ModComponents;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/** Server-only records, written after the durable exchange succeeds. Not a source of balances. */
public final class TradeAuditLog {
    private TradeAuditLog() {}

    public static void completed(ServerPlayerEntity a, ServerPlayerEntity b, long beforeA, long beforeB,
                                 long afterA, long afterB, long givenA, long givenB,
                                 List<ItemStack> itemsA, List<ItemStack> itemsB) {
        var transaction = ModComponents.WALLET.get(a).transaction();
        CranksDungeons.LOGGER.info("Completed trade {}: {} ({}) gave {} coins and {} stacks to {} ({}) for {} coins and {} stacks",
                transaction,a.getGameProfile().getName(),a.getUuid(),givenA,itemsA.size(),
                b.getGameProfile().getName(),b.getUuid(),givenB,itemsB.size());
        try {
            Instant now = Instant.now();
            var record = new JsonObject();
            record.addProperty("version",1);
            record.addProperty("transaction",transaction.toString());
            record.addProperty("time",now.toString());
            record.addProperty("dimension",a.getWorld().getRegistryKey().getValue().toString());
            record.add("first",participant(a,beforeA,afterA,givenA,itemsA));
            record.add("second",participant(b,beforeB,afterB,givenB,itemsB));
            var directory = a.getServer().getSavePath(WorldSavePath.ROOT).resolve("data/cranks-dungeons/trades");
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(now.atZone(ZoneOffset.UTC).toLocalDate()+".jsonl"),record+"\n",
                    StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        } catch (Exception error) {
            // The exchange has committed. A logging failure must never invite a duplicate retry.
            CranksDungeons.LOGGER.error("Could not write detailed audit for completed trade {}",transaction,error);
        }
    }

    private static JsonObject participant(ServerPlayerEntity player, long before, long after,
                                          long coins, List<ItemStack> items) {
        var result = new JsonObject();
        result.addProperty("uuid",player.getUuidAsString());
        result.addProperty("name",player.getGameProfile().getName());
        result.addProperty("balanceBefore",before);
        result.addProperty("balanceAfter",after);
        result.addProperty("coinsGiven",coins);
        var given = new JsonArray();
        var ops = player.getRegistryManager().getOps(JsonOps.INSTANCE);
        for (var stack : items) given.add(ItemStack.CODEC.encodeStart(ops,stack).getOrThrow());
        result.add("itemsGiven",given);
        return result;
    }
}
