package com.cranks.dungeons.util;

import java.util.Map;

public interface ReactionCooldownProvider {
    boolean cranks$canTriggerReaction(String type);
    void cranks$setReactionCooldown(String type, int ticks);
    
    // New methods for syncing
    Map<String, CooldownInfo> cranks$getReactionCooldowns();
    
    record CooldownInfo(long endTime, int totalDuration) {}
}
