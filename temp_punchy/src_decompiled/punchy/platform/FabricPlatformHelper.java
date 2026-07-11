/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package punchy.platform;

import net.fabricmc.loader.api.FabricLoader;
import punchy.platform.services.IPlatformHelper;

public class FabricPlatformHelper
implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String getModDisplayName(String modId) {
        if (modId == null || modId.isBlank()) {
            return "";
        }
        return FabricLoader.getInstance().getModContainer(modId).map(container -> container.getMetadata().getName()).filter(name -> name != null && !name.isBlank()).orElse(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}

