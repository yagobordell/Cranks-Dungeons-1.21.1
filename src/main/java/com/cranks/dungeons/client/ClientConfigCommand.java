package com.cranks.dungeons.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientConfigCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("cranksconfig")
                .executes(ctx -> {
                    com.cranks.dungeons.config.ModConfig config = com.cranks.dungeons.CranksDungeons.CONFIG;
                    ctx.getSource().sendFeedback(Text.literal("§6--- Cranks Dungeons Config ---"));
                    ctx.getSource().sendFeedback(Text.literal("§eDamage (Dealt): §f" + config.showMyDealtDamage()));
                    ctx.getSource().sendFeedback(Text.literal("§eDamage (Received): §f" + config.showMyReceivedDamage()));
                    ctx.getSource().sendFeedback(Text.literal("§eDamage (Other): §f" + config.showOtherPlayerDamage()));
                    ctx.getSource().sendFeedback(Text.literal("§aHealing (Self): §f" + config.showMyReceivedHealing()));
                    ctx.getSource().sendFeedback(Text.literal("§aHealing (Other): §f" + config.showOtherPlayerHealing()));
                    return 1;
                })
                .then(literal("damage")
                    .then(literal("other").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        com.cranks.dungeons.CranksDungeons.CONFIG.showOtherPlayerDamage(BoolArgumentType.getBool(ctx, "value"));
                        ctx.getSource().sendFeedback(Text.literal("Show Other Player Damage set to: " + com.cranks.dungeons.CranksDungeons.CONFIG.showOtherPlayerDamage()));
                        return 1;
                    })))
                    .then(literal("dealt").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        com.cranks.dungeons.CranksDungeons.CONFIG.showMyDealtDamage(BoolArgumentType.getBool(ctx, "value"));
                        ctx.getSource().sendFeedback(Text.literal("Show My Dealt Damage set to: " + com.cranks.dungeons.CranksDungeons.CONFIG.showMyDealtDamage()));
                        return 1;
                    })))
                    .then(literal("received").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        com.cranks.dungeons.CranksDungeons.CONFIG.showMyReceivedDamage(BoolArgumentType.getBool(ctx, "value"));
                        ctx.getSource().sendFeedback(Text.literal("Show My Received Damage set to: " + com.cranks.dungeons.CranksDungeons.CONFIG.showMyReceivedDamage()));
                        return 1;
                    })))
                )
                .then(literal("healing")
                    .then(literal("other").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        com.cranks.dungeons.CranksDungeons.CONFIG.showOtherPlayerHealing(BoolArgumentType.getBool(ctx, "value"));
                        ctx.getSource().sendFeedback(Text.literal("Show Other Player Healing set to: " + com.cranks.dungeons.CranksDungeons.CONFIG.showOtherPlayerHealing()));
                        return 1;
                    })))
                    .then(literal("self").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        com.cranks.dungeons.CranksDungeons.CONFIG.showMyReceivedHealing(BoolArgumentType.getBool(ctx, "value"));
                        ctx.getSource().sendFeedback(Text.literal("Show Self Healing set to: " + com.cranks.dungeons.CranksDungeons.CONFIG.showMyReceivedHealing()));
                        return 1;
                    })))
                )
            );
        });
    }
}
