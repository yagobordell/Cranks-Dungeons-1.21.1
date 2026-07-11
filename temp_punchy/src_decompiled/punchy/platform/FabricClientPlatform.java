/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
 *  net.fabricmc.fabric.api.resource.ResourceManagerHelper
 *  net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3264
 *  net.minecraft.class_3300
 */
package punchy.platform;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import punchy.platform.services.IClientPlatform;

public class FabricClientPlatform
implements IClientPlatform {
    @Override
    public void registerKeyMapping(class_304 mapping) {
        KeyBindingHelper.registerKeyBinding((class_304)mapping);
    }

    @Override
    public void registerClientTickEnd(Consumer<class_310> handler) {
        ClientTickEvents.END_CLIENT_TICK.register(handler::accept);
    }

    @Override
    public void registerReloadListener(final class_2960 id, final Consumer<class_3300> handler) {
        ResourceManagerHelper.get((class_3264)class_3264.field_14188).registerReloadListener((IdentifiableResourceReloadListener)new SimpleSynchronousResourceReloadListener(){

            public class_2960 getFabricId() {
                return id;
            }

            public void method_14491(class_3300 resourceManager) {
                handler.accept(resourceManager);
            }
        });
    }
}

