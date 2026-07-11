/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 */
package punchy.compat;

import net.minecraft.class_1657;
import punchy.platform.Services;

public final class CrawlCompat {
    private static final String MOD_ID = "crawl";
    private static boolean checked;
    private static boolean available;

    private CrawlCompat() {
    }

    public static boolean isAvailable() {
        if (!checked) {
            checked = true;
            available = Services.PLATFORM != null && Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return available;
    }

    public static boolean isCrawling(class_1657 player) {
        if (player == null) {
            return false;
        }
        String pose = player.method_18376().name();
        if ("CRAWLING".equals(pose)) {
            return true;
        }
        if ("SWIMMING".equals(pose)) {
            return !player.method_5799() && !player.method_5869();
        }
        return false;
    }
}

