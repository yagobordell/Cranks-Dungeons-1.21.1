/*
 * Decompiled with CFR 0.152.
 */
package punchy.platform;

import java.util.ServiceLoader;
import punchy.platform.services.IClientPlatform;
import punchy.platform.services.IPlatformHelper;

public final class Services {
    public static final IPlatformHelper PLATFORM = Services.load(IPlatformHelper.class);
    public static final IClientPlatform CLIENT = Services.load(IClientPlatform.class);

    private Services() {
    }

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}

