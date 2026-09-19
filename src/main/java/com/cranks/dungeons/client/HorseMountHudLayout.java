package com.cranks.dungeons.client;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseEntity;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.util.math.MathHelper;

/** Shared mount-heart sizing used by both the vanilla-style hearts and custom mana HUD. */
public final class HorseMountHudLayout {
    public static final float HORSE_HEALTH_PER_HEART = 5.0f;
    public static final int HEARTS_PER_ROW = 10;
    public static final int MAX_VISIBLE_HEARTS = 30;

    private HorseMountHudLayout() {
    }

    public static int visibleHeartCount(LivingEntity mount) {
        if (mount == null || !mount.isAlive()) return 0;
        float healthPerHeart = MountKind.of(mount) != MountKind.UNSUPPORTED
                ? HORSE_HEALTH_PER_HEART : 2.0f;
        return Math.min(MAX_VISIBLE_HEARTS,
                MathHelper.ceil(mount.getMaxHealth() / healthPerHeart));
    }

    public static int visibleHeartRows(LivingEntity mount) {
        int hearts = visibleHeartCount(mount);
        return hearts == 0 ? 0 : MathHelper.ceil(hearts / (float) HEARTS_PER_ROW);
    }

    public static int horseHealthHalfUnits(float health) {
        return Math.max(0, MathHelper.ceil(health / (HORSE_HEALTH_PER_HEART / 2.0f)));
    }

    public static int mountHealthHalfUnits(float health) {
        return horseHealthHalfUnits(health);
    }
}
