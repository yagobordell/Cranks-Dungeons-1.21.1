package com.cranks.dungeons.command;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Command to apply a specific rune/stat to the held item.
 * Usage: /rune <stat_id> <tier>
 * Example: /rune sword_crit_chance 3
 */
public class RuneCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("rune")
                    .then(argument("stat_id", StringArgumentType.word())
                            .suggests(STAT_ID_SUGGESTIONS)
                            .then(argument("tier", IntegerArgumentType.integer(1, 5))
                                    .executes(RuneCommand::execute))));
        });
    }

    private static final SuggestionProvider<ServerCommandSource> STAT_ID_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(
                StatRegistry.getAllStatIds(),
                builder
        );
    };

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String statId = StringArgumentType.getString(context, "stat_id");
        int tier = IntegerArgumentType.getInteger(context, "tier");

        // Get held item
        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty()) {
            context.getSource().sendError(Text.literal("You must hold an item in your main hand!").formatted(Formatting.RED));
            return 0;
        }

        // Validate stat exists
        CustomStat stat = StatRegistry.getStat(statId);
        if (stat == null) {
            context.getSource().sendError(Text.literal("Invalid stat ID: '" + statId + "'").formatted(Formatting.RED));
            context.getSource().sendMessage(Text.literal("Use tab completion to see valid stat IDs").formatted(Formatting.GRAY));
            return 0;
        }

        // Get item type
        Optional<EquipmentType> equipTypeOpt = EquipmentType.getTypeForItem(heldItem);
        if (equipTypeOpt.isEmpty()) {
            context.getSource().sendError(Text.literal("The held item cannot have stats applied!").formatted(Formatting.RED));
            context.getSource().sendMessage(Text.literal("Only weapons, tools, and armor can have stats.").formatted(Formatting.GRAY));
            return 0;
        }

        EquipmentType equipType = equipTypeOpt.get();

        // Validate stat is appropriate for item type
        if (stat.getEquipmentType() != equipType) {
            context.getSource().sendError(Text.literal("Stat '" + statId + "' is not valid for " + formatEquipmentType(equipType) + "!").formatted(Formatting.RED));
            context.getSource().sendMessage(Text.literal("This stat is for: " + formatEquipmentType(stat.getEquipmentType())).formatted(Formatting.GRAY));
            return 0;
        }

        // Check if item already has this stat or same attribute
        var existingStats = ItemStatManager.getStats(heldItem);
        boolean alreadyHasStat = existingStats.stream()
                .anyMatch(s -> {
                    CustomStat existingStat = StatRegistry.getStat(s.statId);
                    if (existingStat == null) return false;
                    if (s.statId.equals(statId)) return true;
                    if (existingStat.getGroupId().equals(stat.getGroupId())) return true;
                    return false;
                });

        if (alreadyHasStat) {
            context.getSource().sendError(Text.literal("This item already has '" + stat.getDisplayName() + "' or a stat with the same attribute!").formatted(Formatting.RED));
            return 0;
        }

        // Check if item is full
        if (!ItemStatManager.canAddStat(heldItem, stat.getAffixType())) {
            context.getSource().sendError(Text.literal("This item already has the maximum number of " + stat.getAffixType().name().toLowerCase() + " stats!").formatted(Formatting.RED));
            return 0;
        }

        // Apply the stat
        boolean success = ItemStatManager.addSpecificStat(heldItem, statId, tier);

        if (success) {
            double value = ItemStatManager.getStats(heldItem).stream()
                    .filter(s -> s.statId.equals(statId))
                    .findFirst()
                    .map(s -> s.getValue(0))
                    .orElse(0.0);

            String formattedValue = stat.getComponents().get(0).formatValue(value, stat.getEquipmentType());

            context.getSource().sendMessage(Text.literal("✓ Successfully applied ")
                    .formatted(Formatting.GREEN)
                    .append(Text.literal(stat.getDisplayName()).formatted(stat.getColor()))
                    .append(Text.literal(" (" + formattedValue + ") to your "))
                    .append(heldItem.getName().copy()));

            context.getSource().sendMessage(Text.literal("Tier: ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal("T" + tier).formatted(getTierColor(tier))));

            return 1;
        } else {
            context.getSource().sendError(Text.literal("Failed to apply stat! The item may be full or already has this stat.").formatted(Formatting.RED));
            return 0;
        }
    }

    private static String formatEquipmentType(EquipmentType type) {
        return switch (type) {
            case SWORD -> "Swords";
            case AXE -> "Axes";
            case PICKAXE -> "Pickaxes";
            case SHOVEL -> "Shovels";
            case HOE -> "Hoes";
            case BOW -> "Bows";
            case CROSSBOW -> "Crossbows";
            case TRIDENT -> "Tridents";
            case MACE -> "Maces";
            case SPEAR -> "Spears";
            case HELMET -> "Helmets";
            case CHESTPLATE -> "Chestplates";
            case LEGGINGS -> "Leggings";
            case BOOTS -> "Boots";
            case SHIELD -> "Shields";
            case ELYTRA -> "Elytra";
        };
    }

    private static Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1 -> Formatting.GRAY;
            case 2 -> Formatting.GRAY;
            case 3 -> Formatting.GREEN;
            case 4 -> Formatting.GREEN;
            case 5 -> Formatting.GOLD;
            default -> Formatting.WHITE;
        };
    }
}
