package com.cranks.dungeons.economy;
import com.cranks.dungeons.component.ModComponents;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class EconomyManager {
    public static void register(){
        EconomyConfig.load();
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->{EconomyState.get(server).recover(handler.player);ModComponents.WALLET.sync(handler.player);});
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->TradeManager.cancel(handler.player,"Trade cancelled: player disconnected"));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer,newPlayer,alive)->ModComponents.WALLET.sync(newPlayer));
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity,source,amount)->{if(entity instanceof ServerPlayerEntity player)TradeManager.cancel(player,"Trade cancelled: player died");return true;});
        ServerTickEvents.END_SERVER_TICK.register(TradeManager::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server->TradeManager.clear());
        CommandRegistrationCallback.EVENT.register((dispatcher,registries,environment)->dispatcher.register(CommandManager.literal("coins")
            .executes(context->{var player=context.getSource().getPlayerOrThrow();context.getSource().sendFeedback(()->Text.literal("Coins: "+ModComponents.WALLET.get(player).balance()),false);return 1;})
            .then(CommandManager.literal("give").requires(source->source.hasPermissionLevel(2)).then(CommandManager.argument("player",EntityArgumentType.player()).then(CommandManager.argument("amount",LongArgumentType.longArg(1,EconomyState.MAX_COINS)).executes(context->{var player=EntityArgumentType.getPlayer(context,"player");long amount=EconomyState.get(player.getServer()).credit(player,LongArgumentType.getLong(context,"amount"));context.getSource().sendFeedback(()->Text.literal("Granted "+amount+" coins to "+player.getName().getString()),true);return 1;}))))
            .then(CommandManager.literal("set").requires(source->source.hasPermissionLevel(2)).then(CommandManager.argument("player",EntityArgumentType.player()).then(CommandManager.argument("amount",LongArgumentType.longArg(0,EconomyState.MAX_COINS)).executes(context->{var player=EntityArgumentType.getPlayer(context,"player");EconomyState.get(player.getServer()).setBalance(player.getUuid(),LongArgumentType.getLong(context,"amount"));ModComponents.WALLET.sync(player);context.getSource().sendFeedback(()->Text.literal("Updated coin balance for "+player.getName().getString()),true);return 1;}))))));
    }
}
