package com.cranks.dungeons.command;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AilmentCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("ailment")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument("type", StringArgumentType.word())
                            .then(argument("targets", EntityArgumentType.entities())
                                    .then(argument("duration", FloatArgumentType.floatArg(0.1f, 60.0f))
                                            .executes(AilmentCommand::executeApplyNoDamage)
                                            .then(argument("damage", FloatArgumentType.floatArg(0.1f, 1000.0f))
                                                    .executes(AilmentCommand::executeApply)))))
                    .then(literal("clear")
                            .then(argument("targets", EntityArgumentType.entities())
                                    .executes(AilmentCommand::executeClearAll))));
        });
    }

    private static int executeApplyNoDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeApplyWithDamage(context, 0.0f);
    }

    private static int executeApply(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        float damage = FloatArgumentType.getFloat(context, "damage");
        return executeApplyWithDamage(context, damage);
    }

    private static int executeApplyWithDamage(CommandContext<ServerCommandSource> context, float damage) throws CommandSyntaxException {
        String typeStr = StringArgumentType.getString(context, "type");
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
        float duration = FloatArgumentType.getFloat(context, "duration");

        // Handle buildup types separately
        if (typeStr.equalsIgnoreCase("freeze_buildup")) {
            int[] affected = {0};
            targets.forEach(entity -> {
                if (entity instanceof LivingEntity living) {
                    AilmentManager.addFreezeBuildup(living, duration, AilmentType.FREEZE.getBaseDuration(), context.getSource().getEntity());
                    affected[0]++;
                }
            });
            final int finalAffected = affected[0];
            context.getSource().sendFeedback(() -> Text.literal("Added " + (int)(duration * 100) + "% freeze buildup to " + finalAffected + " entities"), true);
            return finalAffected;
        }

        if (typeStr.equalsIgnoreCase("electrocute_buildup")) {
            int[] affected = {0};
            targets.forEach(entity -> {
                if (entity instanceof LivingEntity living) {
                    AilmentManager.addElectrocuteBuildup(living, duration, AilmentType.ELECTROCUTE.getBaseDuration(), context.getSource().getEntity());
                    affected[0]++;
                }
            });
            final int finalAffected = affected[0];
            context.getSource().sendFeedback(() -> Text.literal("Added " + (int)(duration * 100) + "% electrocute buildup to " + finalAffected + " entities"), true);
            return finalAffected;
        }

        AilmentType type = AilmentType.fromId(typeStr.toLowerCase());
        if (type == null) {
            context.getSource().sendError(Text.literal("Invalid ailment type: " + typeStr + ". Valid types: burn, bleed, poison, shock, freeze, electrocute, freeze_buildup, electrocute_buildup"));
            return 0;
        }

        // For damaging ailments without damage parameter, use default damage
        final float finalDamage = (damage == 0.0f && type.dealsDamage()) ? 1.0f : damage;

        int[] affected = {0};
        targets.forEach(entity -> {
            if (entity instanceof LivingEntity living) {
                AilmentManager.applyAilment(living, type, finalDamage, (double)duration, context.getSource().getEntity());
                affected[0]++;
            }
        });

        final int finalAffected = affected[0];
        if (type.dealsDamage()) {
            context.getSource().sendFeedback(() -> Text.literal("Applied " + typeStr + " to " + finalAffected + " entities (duration: " + duration + "s, damage: " + finalDamage + ")"), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("Applied " + typeStr + " to " + finalAffected + " entities (duration: " + duration + "s)"), true);
        }
        return finalAffected;
    }

    private static int executeClearAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
        int[] cleared = {0};
        targets.forEach(entity -> {
            if (entity instanceof LivingEntity living) {
                AilmentManager.clearAllAilments(living);
                cleared[0]++;
            }
        });

        final int finalCleared = cleared[0];
        context.getSource().sendFeedback(() -> Text.literal("Cleared ailments from " + finalCleared + " entities"), true);
        return finalCleared;
    }
}
