package com.cranks.dungeons.command;

import com.cranks.dungeons.network.ModNetworking;
import com.cranks.dungeons.party.Party;
import com.cranks.dungeons.party.PartyHealthSyncHandler;
import com.cranks.dungeons.party.PartyInvite;
import com.cranks.dungeons.party.PartyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PartyCommand {

    public static void register() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("party")
                .then(literal("create")
                    .executes(PartyCommand::create))
                .then(literal("invite")
                    .then(argument("player", EntityArgumentType.player())
                        .executes(PartyCommand::invite)))
                .then(literal("accept")
                    .then(argument("player", EntityArgumentType.player())
                        .executes(PartyCommand::accept)))
                .then(literal("kick")
                    .then(argument("player", EntityArgumentType.player())
                        .executes(PartyCommand::kick)))
                .then(literal("leave")
                    .executes(PartyCommand::leave))
                .then(literal("leader")
                    .then(argument("player", EntityArgumentType.player())
                        .executes(PartyCommand::transferLeader)))
                .then(literal("disband")
                    .executes(PartyCommand::disband))
                .then(literal("info")
                    .executes(PartyCommand::info)));
        });
    }

    private static int create(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        UUID uuid = player.getUuid();

        if (PartyManager.isInParty(uuid)) {
            context.getSource().sendError(Text.literal("You are already in a party!").formatted(Formatting.RED));
            return 0;
        }

        Party party = PartyManager.createParty(uuid);
        if (party != null) {
            context.getSource().sendFeedback(() -> 
                Text.literal("Party created! You are now the leader.").formatted(Formatting.GREEN), false);
            // Sync party to creator so HUD appears
            ModNetworking.syncPartyToAll(context.getSource().getServer(), party, true);
            return 1;
        }

        return 0;
    }

    private static int invite(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        UUID inviterUuid = player.getUuid();
        UUID targetUuid = target.getUuid();

        if (inviterUuid.equals(targetUuid)) {
            context.getSource().sendError(Text.literal("You cannot invite yourself!").formatted(Formatting.RED));
            return 0;
        }

        Party party = PartyManager.getParty(inviterUuid).orElse(null);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party! Create one with /party create").formatted(Formatting.RED));
            return 0;
        }

        if (!party.isLeader(inviterUuid)) {
            context.getSource().sendError(Text.literal("Only the party leader can invite players!").formatted(Formatting.RED));
            return 0;
        }

        if (party.isFull()) {
            context.getSource().sendError(Text.literal("Party is full! (Max 10 members)").formatted(Formatting.RED));
            return 0;
        }

        if (PartyManager.isInParty(targetUuid)) {
            context.getSource().sendError(Text.literal(target.getName().getString() + " is already in a party!").formatted(Formatting.RED));
            return 0;
        }

        if (PartyManager.invitePlayer(inviterUuid, targetUuid)) {
            context.getSource().sendFeedback(() -> 
                Text.literal("Invited " + target.getName().getString() + " to the party! (Expires in 2 minutes)").formatted(Formatting.GREEN), false);
            target.sendMessage(Text.literal(player.getName().getString() + " invited you to join their party!").formatted(Formatting.YELLOW)
                .append(Text.literal(" Use /party accept " + player.getName().getString()).formatted(Formatting.GREEN)), false);
            return 1;
        }

        context.getSource().sendError(Text.literal("Failed to send invite. They may already have a pending invite.").formatted(Formatting.RED));
        return 0;
    }

    private static int accept(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity inviter = EntityArgumentType.getPlayer(context, "player");

        UUID inviteeUuid = player.getUuid();
        UUID inviterUuid = inviter.getUuid();

        if (PartyManager.isInParty(inviteeUuid)) {
            context.getSource().sendError(Text.literal("You are already in a party! Leave it first with /party leave").formatted(Formatting.RED));
            return 0;
        }

        Optional<PartyInvite> invite = PartyManager.getPendingInvite(inviteeUuid);
        if (invite.isEmpty() || !invite.get().getInviter().equals(inviterUuid)) {
            context.getSource().sendError(Text.literal("You don't have a pending invite from that player!").formatted(Formatting.RED));
            return 0;
        }

        if (PartyManager.acceptInvite(inviteeUuid, inviterUuid)) {
            context.getSource().sendFeedback(() -> 
                Text.literal("You joined the party!").formatted(Formatting.GREEN), false);
            
            Party party = PartyManager.getParty(inviteeUuid).orElseThrow();
            ModNetworking.syncPartyToAll(context.getSource().getServer(), party, true);
            
            for (UUID member : party.getMembers()) {
                if (!member.equals(inviteeUuid)) {
                    ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(member);
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(Text.literal(player.getName().getString() + " joined the party!").formatted(Formatting.YELLOW), false);
                    }
                }
            }
            return 1;
        }

        context.getSource().sendError(Text.literal("Failed to join party. It may be full or disbanded.").formatted(Formatting.RED));
        return 0;
    }

    private static int kick(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        UUID leaderUuid = player.getUuid();
        UUID targetUuid = target.getUuid();

        if (leaderUuid.equals(targetUuid)) {
            context.getSource().sendError(Text.literal("You cannot kick yourself! Use /party leave instead.").formatted(Formatting.RED));
            return 0;
        }

        Party party = PartyManager.getParty(leaderUuid).orElse(null);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party!").formatted(Formatting.RED));
            return 0;
        }

        if (!party.isLeader(leaderUuid)) {
            context.getSource().sendError(Text.literal("Only the party leader can kick players!").formatted(Formatting.RED));
            return 0;
        }

        if (!party.isMember(targetUuid)) {
            context.getSource().sendError(Text.literal(target.getName().getString() + " is not in your party!").formatted(Formatting.RED));
            return 0;
        }

        if (PartyManager.kickPlayer(leaderUuid, targetUuid)) {
            context.getSource().sendFeedback(() -> 
                Text.literal("Kicked " + target.getName().getString() + " from the party!").formatted(Formatting.GREEN), false);
            target.sendMessage(Text.literal("You were kicked from the party!").formatted(Formatting.RED), false);
            PartyHealthSyncHandler.clearPlayerData(targetUuid);
            ModNetworking.sendLeavePacket(context.getSource().getServer(), targetUuid);
            
            ModNetworking.syncPartyToAll(context.getSource().getServer(), party, false);
            
            for (UUID member : party.getMembers()) {
                ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(member);
                if (memberPlayer != null && !member.equals(leaderUuid)) {
                    memberPlayer.sendMessage(Text.literal(target.getName().getString() + " was kicked from the party!").formatted(Formatting.YELLOW), false);
                }
            }
            return 1;
        }

        return 0;
    }

    private static int leave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        UUID uuid = player.getUuid();

        Party party = PartyManager.getParty(uuid).orElse(null);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party!").formatted(Formatting.RED));
            return 0;
        }

        boolean wasLeader = party.isLeader(uuid);
        
        if (PartyManager.leaveParty(uuid)) {
            context.getSource().sendFeedback(() -> 
                Text.literal("You left the party!").formatted(Formatting.YELLOW), false);
            
            PartyHealthSyncHandler.clearPlayerData(uuid);
            ModNetworking.sendLeavePacket(context.getSource().getServer(), uuid);
            
            if (!wasLeader) {
                ModNetworking.syncPartyToAll(context.getSource().getServer(), party, false);
                
                for (UUID member : party.getMembers()) {
                    ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(member);
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(Text.literal(player.getName().getString() + " left the party!").formatted(Formatting.YELLOW), false);
                    }
                }
            } else {
                for (UUID member : party.getMembers()) {
                    ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(member);
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(Text.literal(player.getName().getString() + " disbanded the party!").formatted(Formatting.RED), false);
                        ModNetworking.sendLeavePacket(context.getSource().getServer(), member);
                    }
                }
            }
            return 1;
        }

        return 0;
    }

    private static int transferLeader(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        UUID currentLeaderUuid = player.getUuid();
        UUID newLeaderUuid = target.getUuid();

        if (currentLeaderUuid.equals(newLeaderUuid)) {
            context.getSource().sendError(Text.literal("You are already the leader!").formatted(Formatting.RED));
            return 0;
        }

        Party party = PartyManager.getParty(currentLeaderUuid).orElse(null);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party!").formatted(Formatting.RED));
            return 0;
        }

        if (!party.isLeader(currentLeaderUuid)) {
            context.getSource().sendError(Text.literal("Only the party leader can transfer leadership!").formatted(Formatting.RED));
            return 0;
        }

        if (!party.isMember(newLeaderUuid)) {
            context.getSource().sendError(Text.literal(target.getName().getString() + " is not in your party!").formatted(Formatting.RED));
            return 0;
        }

        if (PartyManager.transferLeadership(currentLeaderUuid, newLeaderUuid)) {
            context.getSource().sendFeedback(() -> 
                Text.literal("Party leadership transferred to " + target.getName().getString() + "!").formatted(Formatting.GREEN), false);
            target.sendMessage(Text.literal("You are now the party leader!").formatted(Formatting.GREEN), false);
            
            ModNetworking.syncPartyToAll(context.getSource().getServer(), party, false);
            
            for (UUID member : party.getMembers()) {
                if (!member.equals(currentLeaderUuid) && !member.equals(newLeaderUuid)) {
                    ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(member);
                    if (memberPlayer != null) {
                        memberPlayer.sendMessage(Text.literal(target.getName().getString() + " is now the party leader!").formatted(Formatting.YELLOW), false);
                    }
                }
            }
            return 1;
        }

        return 0;
    }

    private static int disband(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        UUID uuid = player.getUuid();

        Party party = PartyManager.getParty(uuid).orElse(null);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party!").formatted(Formatting.RED));
            return 0;
        }

        if (!party.isLeader(uuid)) {
            context.getSource().sendError(Text.literal("Only the party leader can disband the party!").formatted(Formatting.RED));
            return 0;
        }

        // Send leave packets to ALL members including leader
        for (UUID member : party.getMembers()) {
            PartyHealthSyncHandler.clearPlayerData(member);
            ModNetworking.sendLeavePacket(context.getSource().getServer(), member);
            
            ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(member);
            if (memberPlayer != null && !member.equals(uuid)) {
                memberPlayer.sendMessage(Text.literal("The party was disbanded by " + player.getName().getString() + "!").formatted(Formatting.RED), false);
            }
        }

        if (PartyManager.disbandParty(uuid)) {
            context.getSource().sendFeedback(() -> 
                Text.literal("Party disbanded!").formatted(Formatting.GREEN), false);
            return 1;
        }

        return 0;
    }

    private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        UUID uuid = player.getUuid();

        Party party = PartyManager.getParty(uuid).orElse(null);
        if (party == null) {
            context.getSource().sendError(Text.literal("You are not in a party!").formatted(Formatting.RED));
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Party Info ===\n");
        sb.append("Members (").append(party.getSize()).append("/").append(Party.MAX_MEMBERS).append("):\n");
        
        for (UUID memberUuid : party.getMembers()) {
            ServerPlayerEntity memberPlayer = context.getSource().getServer().getPlayerManager().getPlayer(memberUuid);
            String name = memberPlayer != null ? memberPlayer.getName().getString() : "Offline";
            boolean isLeader = party.isLeader(memberUuid);
            sb.append(isLeader ? "★ " : "  ").append(name).append(isLeader ? " (Leader)" : "").append("\n");
        }

        final String message = sb.toString();
        context.getSource().sendFeedback(() -> Text.literal(message).formatted(Formatting.YELLOW), false);
        return 1;
    }
}
