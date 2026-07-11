/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
 *  net.minecraft.class_2561
 */
package punchy.client.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.class_2561;
import punchy.client.debug.PunchyDebug;
import punchy.config.PunchyConfig;

public class PunchyCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal((String)"punchy").then(ClientCommandManager.literal((String)"debugposition").executes(context -> {
            boolean enabled = PunchyDebug.toggleDebugPosition();
            PunchyConfig.Data data = PunchyConfig.snapshot();
            if (enabled) {
                data.enableTuning = true;
                data.enableMod = true;
                PunchyConfig.applyAndSave(data);
            } else if (data.enableTuning) {
                data.enableTuning = false;
                PunchyConfig.applyAndSave(data);
            }
            ((FabricClientCommandSource)context.getSource()).sendFeedback((class_2561)class_2561.method_43469((String)"punchy.commands.debug_position", (Object[])new Object[]{enabled ? class_2561.method_43471((String)"punchy.toggle.on") : class_2561.method_43471((String)"punchy.toggle.off")}));
            return 1;
        }))).then(ClientCommandManager.literal((String)"pos").then(ClientCommandManager.argument((String)"x", (ArgumentType)FloatArgumentType.floatArg()).then(ClientCommandManager.argument((String)"y", (ArgumentType)FloatArgumentType.floatArg()).then(ClientCommandManager.argument((String)"z", (ArgumentType)FloatArgumentType.floatArg()).executes(context -> {
            PunchyDebug.POS_X = FloatArgumentType.getFloat((CommandContext)context, (String)"x");
            PunchyDebug.POS_Y = FloatArgumentType.getFloat((CommandContext)context, (String)"y");
            PunchyDebug.POS_Z = FloatArgumentType.getFloat((CommandContext)context, (String)"z");
            ((FabricClientCommandSource)context.getSource()).sendFeedback((class_2561)class_2561.method_43469((String)"punchy.commands.pos_set", (Object[])new Object[]{Float.valueOf(PunchyDebug.POS_X), Float.valueOf(PunchyDebug.POS_Y), Float.valueOf(PunchyDebug.POS_Z)}));
            return 1;
        })))))).then(ClientCommandManager.literal((String)"rot").then(ClientCommandManager.argument((String)"x", (ArgumentType)FloatArgumentType.floatArg()).then(ClientCommandManager.argument((String)"y", (ArgumentType)FloatArgumentType.floatArg()).then(ClientCommandManager.argument((String)"z", (ArgumentType)FloatArgumentType.floatArg()).executes(context -> {
            PunchyDebug.ROT_X = FloatArgumentType.getFloat((CommandContext)context, (String)"x");
            PunchyDebug.ROT_Y = FloatArgumentType.getFloat((CommandContext)context, (String)"y");
            PunchyDebug.ROT_Z = FloatArgumentType.getFloat((CommandContext)context, (String)"z");
            ((FabricClientCommandSource)context.getSource()).sendFeedback((class_2561)class_2561.method_43469((String)"punchy.commands.rot_set", (Object[])new Object[]{Float.valueOf(PunchyDebug.ROT_X), Float.valueOf(PunchyDebug.ROT_Y), Float.valueOf(PunchyDebug.ROT_Z)}));
            return 1;
        })))))).then(ClientCommandManager.literal((String)"scale").then(ClientCommandManager.argument((String)"s", (ArgumentType)FloatArgumentType.floatArg()).executes(context -> {
            PunchyDebug.SCALE = FloatArgumentType.getFloat((CommandContext)context, (String)"s");
            ((FabricClientCommandSource)context.getSource()).sendFeedback((class_2561)class_2561.method_43469((String)"punchy.commands.scale_set", (Object[])new Object[]{Float.valueOf(PunchyDebug.SCALE)}));
            return 1;
        })))));
    }
}

