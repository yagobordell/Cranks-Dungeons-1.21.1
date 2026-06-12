package com.cranks.dungeons.util;

import com.cranks.dungeons.client.render.AilmentIconRenderer;
import com.cranks.dungeons.network.AilmentSyncPayload;
import net.minecraft.entity.LivingEntity;

public class ClientAilmentHelper {
    public static boolean isStunned(LivingEntity entity) {
        java.util.List<AilmentSyncPayload.AilmentEntry> ailments = AilmentIconRenderer.getAilments(entity.getId());
        for (AilmentSyncPayload.AilmentEntry entry : ailments) {
            String path = entry.texture().getPath();
            if (path.equals("textures/mob_effect/freeze.png") || path.equals("textures/mob_effect/electrocute.png")) {
                return true;
            }
        }
        return false;
    }
}
