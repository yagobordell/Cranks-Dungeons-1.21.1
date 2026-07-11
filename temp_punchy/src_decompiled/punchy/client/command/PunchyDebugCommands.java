/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.class_2168
 *  net.minecraft.class_2170
 *  net.minecraft.class_2561
 */
package punchy.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2561;
import punchy.client.debug.PunchyDebug;
import punchy.config.PunchyConfig;

public final class PunchyDebugCommands {
    private PunchyDebugCommands() {
    }

    public static void register(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247((String)"punchy").then(class_2170.method_9247((String)"debugposition").executes(context -> {
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
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43469((String)"punchy.commands.debug_position", (Object[])new Object[]{enabled ? class_2561.method_43471((String)"punchy.toggle.on") : class_2561.method_43471((String)"punchy.toggle.off")}), false);
            return 1;
        }))).then(class_2170.method_9247((String)"pos").then(class_2170.method_9244((String)"x", (ArgumentType)FloatArgumentType.floatArg()).then(class_2170.method_9244((String)"y", (ArgumentType)FloatArgumentType.floatArg()).then(class_2170.method_9244((String)"z", (ArgumentType)FloatArgumentType.floatArg()).executes(context -> {
            PunchyDebug.POS_X = FloatArgumentType.getFloat((CommandContext)context, (String)"x");
            PunchyDebug.POS_Y = FloatArgumentType.getFloat((CommandContext)context, (String)"y");
            PunchyDebug.POS_Z = FloatArgumentType.getFloat((CommandContext)context, (String)"z");
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43469((String)"punchy.commands.pos_set", (Object[])new Object[]{Float.valueOf(PunchyDebug.POS_X), Float.valueOf(PunchyDebug.POS_Y), Float.valueOf(PunchyDebug.POS_Z)}), false);
            return 1;
        })))))).then(class_2170.method_9247((String)"rot").then(class_2170.method_9244((String)"x", (ArgumentType)FloatArgumentType.floatArg()).then(class_2170.method_9244((String)"y", (ArgumentType)FloatArgumentType.floatArg()).then(class_2170.method_9244((String)"z", (ArgumentType)FloatArgumentType.floatArg()).executes(context -> {
            PunchyDebug.ROT_X = FloatArgumentType.getFloat((CommandContext)context, (String)"x");
            PunchyDebug.ROT_Y = FloatArgumentType.getFloat((CommandContext)context, (String)"y");
            PunchyDebug.ROT_Z = FloatArgumentType.getFloat((CommandContext)context, (String)"z");
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43469((String)"punchy.commands.rot_set", (Object[])new Object[]{Float.valueOf(PunchyDebug.ROT_X), Float.valueOf(PunchyDebug.ROT_Y), Float.valueOf(PunchyDebug.ROT_Z)}), false);
            return 1;
        })))))).then(class_2170.method_9247((String)"scale").then(class_2170.method_9244((String)"s", (ArgumentType)FloatArgumentType.floatArg()).executes(context -> {
            PunchyDebug.SCALE = FloatArgumentType.getFloat((CommandContext)context, (String)"s");
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43469((String)"punchy.commands.scale_set", (Object[])new Object[]{Float.valueOf(PunchyDebug.SCALE)}), false);
            return 1;
        }))));
    }
}

