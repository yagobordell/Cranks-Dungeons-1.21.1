/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.resource.ResourceManagerHelper
 *  net.fabricmc.fabric.api.resource.ResourcePackActivationType
 *  net.fabricmc.loader.api.FabricLoader
 *  net.fabricmc.loader.api.ModContainer
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 */
package punchy.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import punchy.client.PunchyClient;
import punchy.client.command.PunchyCommands;

public class PunchyFabricClient
implements ClientModInitializer {
    private static final class_2960 BUILTIN_PACK_ID = class_2960.method_60655((String)"punchy", (String)"punchy");

    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer("punchy").ifPresent(container -> ResourceManagerHelper.registerBuiltinResourcePack((class_2960)BUILTIN_PACK_ID, (ModContainer)container, (class_2561)class_2561.method_43470((String)"\u00a7bPunchy!"), (ResourcePackActivationType)ResourcePackActivationType.DEFAULT_ENABLED));
        PunchyClient.init();
        PunchyCommands.register();
    }
}

