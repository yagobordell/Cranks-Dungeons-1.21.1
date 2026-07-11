/*
 * Decompiled with CFR 0.152.
 */
package punchy.platform.services;

public interface IPlatformHelper {
    public String getPlatformName();

    public boolean isModLoaded(String var1);

    default public String getModDisplayName(String modId) {
        return modId;
    }

    public boolean isDevelopmentEnvironment();
}

