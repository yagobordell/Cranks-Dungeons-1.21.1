package com.cranks.dungeons.util;

import java.util.UUID;
import java.util.Map;

public interface MobDamageTracker {
    Map<UUID, Float> cranks$getDamageContributions();
    void cranks$addDamageContribution(UUID playerUuid, float damage);
    void cranks$clearDamageContributions();
}
