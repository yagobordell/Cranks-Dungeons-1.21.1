package com.cranks.dungeons.client;

import com.cranks.dungeons.config.ModConfig;
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
                    ModConfig config = ModConfig.get();
                    ctx.getSource().sendFeedback(Text.literal("§6--- Cranks Dungeons Config ---"));
                    ctx.getSource().sendFeedback(Text.literal("§eDamage (Dealt): §f" + config.showMyDealtDamage));
                    ctx.getSource().sendFeedback(Text.literal("§eDamage (Received): §f" + config.showMyReceivedDamage));
                    ctx.getSource().sendFeedback(Text.literal("§eDamage (Other): §f" + config.showOtherPlayerDamage));
                    ctx.getSource().sendFeedback(Text.literal("§aHealing (Self): §f" + config.showMyReceivedHealing));
                    ctx.getSource().sendFeedback(Text.literal("§aHealing (Other): §f" + config.showOtherPlayerHealing));
                    return 1;
                })
                .then(literal("damage")
                    .then(literal("other").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        ModConfig.get().showOtherPlayerDamage = BoolArgumentType.getBool(ctx, "value");
                        ModConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("Show Other Player Damage set to: " + ModConfig.get().showOtherPlayerDamage));
                        return 1;
                    })))
                    .then(literal("dealt").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        ModConfig.get().showMyDealtDamage = BoolArgumentType.getBool(ctx, "value");
                        ModConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("Show My Dealt Damage set to: " + ModConfig.get().showMyDealtDamage));
                        return 1;
                    })))
                    .then(literal("received").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        ModConfig.get().showMyReceivedDamage = BoolArgumentType.getBool(ctx, "value");
                        ModConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("Show My Received Damage set to: " + ModConfig.get().showMyReceivedDamage));
                        return 1;
                    })))
                )
                .then(literal("healing")
                    .then(literal("other").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        ModConfig.get().showOtherPlayerHealing = BoolArgumentType.getBool(ctx, "value");
                        ModConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("Show Other Player Healing set to: " + ModConfig.get().showOtherPlayerHealing));
                        return 1;
                    })))
                    .then(literal("self").then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
                        ModConfig.get().showMyReceivedHealing = BoolArgumentType.getBool(ctx, "value");
                        ModConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("Show Self Healing set to: " + ModConfig.get().showMyReceivedHealing));
                        return 1;
                    })))
                )
            );
        });
    }
}
